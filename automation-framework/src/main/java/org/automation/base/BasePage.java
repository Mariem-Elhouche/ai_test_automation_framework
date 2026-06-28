package org.automation.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.automation.ai.healing.ElementInfo;
import org.automation.ai.healing.HealingBaseline;
import org.automation.ai.healing.HealingRequest;
import org.automation.ai.healing.HealingResponse;
import org.automation.ai.healing.SelfHealingClient;
import org.automation.dashboard.DashboardReporter;
import org.automation.factory.DriverFactory;
import org.automation.utils.ConfigLoader;
import org.automation.utils.HealingUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BasePage {

    private static final int DEFAULT_WAIT_SECONDS = 5;
    private static final Logger log = LoggerFactory.getLogger(BasePage.class);

    private static final boolean HEALING_ENABLED = Boolean.parseBoolean(
            ConfigLoader.getProperty("self.healing.enabled", "true")
    );
    private static final boolean HEALING_DEBUG_REQUEST = Boolean.parseBoolean(
            ConfigLoader.getProperty("self.healing.debug.request", "true")
    );
    private static final ObjectMapper DEBUG_OBJECT_MAPPER = new ObjectMapper();
    private static final DateTimeFormatter DEBUG_FILE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    public static final ThreadLocal<String> scenarioOutcome = ThreadLocal.withInitial(() -> "passed");

    public static void resetScenarioOutcome() {
        scenarioOutcome.set("passed");
    }

    // Key = <PageClass>::<logicalName>, value = healed locator
    private static final Map<String, By> healedLocatorsCache = new ConcurrentHashMap<>();

    // ────────────────────────────────────────────────────────────────────────
    // Snapshot cache : stocke les ElementInfo capturés quand l'élément était
    // encore trouvable. Clé = <PageClass>::<logicalName>
    // Sert de mémoire au moteur Python pour le matching multi-dimensionnel.
    // ────────────────────────────────────────────────────────────────────────
    private static final Map<String, ElementInfo> elementSnapshotCache = new ConcurrentHashMap<>();

    protected WebDriver driver;
    protected WebDriverWait wait;

    private final SelfHealingClient healingClient = new SelfHealingClient();

    public BasePage() {
        refreshDriverReferences();
    }

    protected void refreshDriverReferences() {
        WebDriver currentDriver = DriverFactory.getOrInitDriver();
        if (this.driver != currentDriver || this.wait == null) {
            this.driver = currentDriver;
            this.wait = new WebDriverWait(this.driver, Duration.ofSeconds(DEFAULT_WAIT_SECONDS));
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // findElement — point d'entrée principal pour toutes les pages filles
    //
    // Cycle :
    //   1. Vérifie le cache des locators guéris
    //   2. Tente de trouver l'élément
    //   3. Si trouvé → capture un snapshot riche (tag HTML, classes, texte...)
    //   4. Si NoSuchElementException → lance le self-healing avec le snapshot
    // ════════════════════════════════════════════════════════════════════════
    protected WebElement findElement(By originalLocator, String logicalName) {
        return findElement(originalLocator, logicalName, null);
    }

    protected WebElement findElement(By originalLocator, String logicalName, String elementTypeHint) {
        refreshDriverReferences();

        String safeLogicalName = (logicalName == null || logicalName.isBlank())
                ? originalLocator.toString()
                : logicalName;
        String cacheKey = cacheKey(safeLogicalName);
        boolean baselineHit = false;
        HealingBaseline.BaselineEntry baselineEntry = null;

        // Charger le baseline (locator guéri d'un run précédent)
        if (!healedLocatorsCache.containsKey(cacheKey)) {
            baselineEntry = HealingBaseline.lookup(cacheKey);
            if (baselineEntry != null) {
                By baselineBy = HealingUtils.buildByFromMap(baselineEntry.healedType, baselineEntry.healedValue);
                if (baselineBy != null) {
                    healedLocatorsCache.put(cacheKey, baselineBy);
                    log.info("Healing baseline hit for '{}' -> {}/{}", safeLogicalName, baselineEntry.healedType, baselineEntry.healedValue);
                    baselineHit = true;
                }
            }
        }

        By locatorToUse = healedLocatorsCache.getOrDefault(cacheKey, originalLocator);

        try {
            WebElement element = driver.findElement(locatorToUse);
            captureElementSnapshot(element, originalLocator, safeLogicalName, cacheKey);
            if (baselineHit && baselineEntry != null) {
                Map<String, String> origMap = HealingUtils.convertByToMap(originalLocator);
                double blScore = (baselineEntry.structuralScore != null && baselineEntry.semanticScore != null)
                        ? 0.4 * baselineEntry.structuralScore + 0.6 * baselineEntry.semanticScore
                        : 1.0;
                DashboardReporter.pushHealingEvent(true, blScore,
                        origMap.get("type"), origMap.get("value"),
                        baselineEntry.healedType, baselineEntry.healedValue,
                        null, 0L, baselineEntry.exceptionType,
                        baselineEntry.structuralScore, baselineEntry.semanticScore,
                        true, null, null, null, null);
            }
            return element;

        } catch (RuntimeException firstFailure) {

            if (!HealingUtils.isSameLocator(locatorToUse, originalLocator)) {
                try {
                    WebElement element = driver.findElement(originalLocator);
                    captureElementSnapshot(element, originalLocator, safeLogicalName, cacheKey);
                    return element;
                } catch (RuntimeException ignored) {
                }
            }

            boolean isLocatorError = firstFailure instanceof NoSuchElementException
                    || firstFailure instanceof StaleElementReferenceException;

            if (!isLocatorError) {
                log.warn("Non-locator error for '{}' with {}: {}. Retrying once...",
                        safeLogicalName, firstFailure.getClass().getSimpleName(), firstFailure.getMessage());
                try {
                    Thread.sleep(300);
                    WebElement element = driver.findElement(locatorToUse);
                    captureElementSnapshot(element, originalLocator, safeLogicalName, cacheKey);
                    scenarioOutcome.set("flaky");
                    log.info("Non-locator retry succeeded for '{}'. Marked as flaky.", safeLogicalName);
                    return element;
                } catch (Exception retryEx) {
                    log.warn("Retry also failed for '{}': {}. Proceeding to self-healing...",
                            safeLogicalName, retryEx.getMessage());
                    firstFailure = retryEx instanceof RuntimeException re ? re : firstFailure;
                    isLocatorError = firstFailure instanceof NoSuchElementException
                            || firstFailure instanceof StaleElementReferenceException;
                }
            }

            if (!HEALING_ENABLED) {
                log.warn("Healing disabled - cannot locate '{}'", safeLogicalName);
                throw firstFailure;
            }

            log.warn("Locator failed for '{}' with {}: {}. Starting self-healing...",
                    safeLogicalName, firstFailure.getClass().getSimpleName(), originalLocator);

            long healingStart = System.currentTimeMillis();
            HealingResponse response = callHealingAPI(originalLocator, safeLogicalName, cacheKey, elementTypeHint);
            long healingTimeMs = System.currentTimeMillis() - healingStart;

            pushHealingEvent(originalLocator, response, safeLogicalName, healingTimeMs, firstFailure, baselineHit);

            if (response != null && response.isSuccess() && response.getNewLocator() != null) {
                By healedBy = HealingUtils.buildByFromResponse(response.getNewLocator());
                if (healedBy != null) {
                    healedLocatorsCache.put(cacheKey, healedBy);
                    Map<String, String> origMap = HealingUtils.convertByToMap(originalLocator);
                    Map<String, String> healedMap = response.getNewLocator();
                    String excType = firstFailure != null ? firstFailure.getClass().getSimpleName() : null;
                    Double blStruct = null, blSem = null;
                    if (response.getDetails() != null) {
                        Object rawStruct = response.getDetails().get("structural_score");
                        Object rawSem = response.getDetails().get("semantic_score");
                        if (rawStruct instanceof Number) blStruct = ((Number) rawStruct).doubleValue();
                        if (rawSem instanceof Number) blSem = ((Number) rawSem).doubleValue();
                    }
                    HealingBaseline.store(cacheKey,
                            origMap.get("type"), origMap.get("value"),
                            healedMap.get("type"), healedMap.get("value"),
                            excType, blStruct, blSem);
                    scenarioOutcome.set("healed");
                    log.info("Self-healing succeeded for '{}'. New locator: {}",
                            safeLogicalName, healedBy);
                    return driver.findElement(healedBy);
                }
            }

            if (response != null) {
                log.warn("Self-healing response for '{}': success={}, error={}, score={}, newLocator={}, details={}",
                        safeLogicalName,
                        response.isSuccess(),
                        response.getError(),
                        response.getScore(),
                        response.getNewLocator(),
                        response.getDetails());
            }

            log.error("Self-healing failed for '{}'", safeLogicalName);
            throw firstFailure;
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // captureElementSnapshot
    //
    // Capture les attributs réels de l'élément via Selenium/JS pendant qu'il
    // est encore trouvable. Ces données alimentent le moteur de matching
    // lors d'un prochain heal().
    //
    // Contenu capturé :
    //   - tag HTML réel  (ex: "button", "input", "a")
    //   - texte visible
    //   - id stable (si non généré par Quasar)
    //   - attributs clés : class, type, placeholder, aria-label, href, name,
    //                       data-testid, data-cy, role
    //   - coordonnées et taille via getBoundingClientRect (JS)
    //
    // Le snapshot est mis en cache : il sera réutilisé à chaque appel heal()
    // sur ce même élément logique, même si le cache locator a expiré.
    // ════════════════════════════════════════════════════════════════════════
    private void captureElementSnapshot(WebElement element,
                                        By originalLocator,
                                        String logicalName,
                                        String cacheKey) {
        try {
            // ── Tag HTML réel ──
            String tagName = element.getTagName();  // "button", "input", "a", ...

            // ── Texte visible ──
            String visibleText = element.getText();
            if (visibleText == null || visibleText.isBlank()) {
                // Fallback : innerText via JS (utile pour les boutons Quasar avec <span>)
                visibleText = (String) ((JavascriptExecutor) driver)
                        .executeScript("return arguments[0].innerText;", element);
            }

            // ── Attributs clés ──
            Map<String, String> attrs = new HashMap<>();
            for (String attr : new String[]{
                    "id", "class", "type", "placeholder", "aria-label",
                    "href", "name", "role", "data-testid", "data-cy",
                    "tabindex", "value"}) {
                String val = element.getAttribute(attr);
                if (val != null && !val.isBlank()) {
                    attrs.put(attr, val);
                }
            }

            // ── id stable (pas un ID généré par Vue/Quasar comme f_abc123-...) ──
            String stableId = null;
            String rawId = attrs.get("id");
            if (rawId != null && !rawId.matches("f_[0-9a-fA-F\\-]{30,}")) {
                stableId = rawId;
            }

            // ── XPath de fallback depuis le locator original ──
            String xpath = HealingUtils.buildXpathFromLocator(originalLocator, attrs);

            // ── Coordonnées via JS (getBoundingClientRect) ──
            Map<String, Double> coords = HealingUtils.extractCoordinates(driver, element);

            // ── Taille ──
            Map<String, Double> size = new HashMap<>();
            if (coords != null) {
                if (coords.containsKey("width"))  size.put("width",  coords.get("width"));
                if (coords.containsKey("height")) size.put("height", coords.get("height"));
            }

            // ── Construction du snapshot ──
            ElementInfo snapshot = new ElementInfo();
            snapshot.setElementType(tagName);          // ← tag HTML réel, pas le type locator
            snapshot.setText(visibleText != null ? visibleText.trim() : logicalName);
            snapshot.setElementId(stableId);
            snapshot.setAttributes(attrs.isEmpty() ? null : attrs);
            snapshot.setXpath(xpath);
            snapshot.setCoordinates(coords != null && !coords.isEmpty() ? coords : null);
            snapshot.setSize(size.isEmpty() ? null : size);

            elementSnapshotCache.put(cacheKey, snapshot);

            log.debug("Snapshot captured for '{}' → tag={}, text='{}', id={}",
                    logicalName, tagName,
                    visibleText != null ? visibleText.trim() : "",
                    stableId);

        } catch (Exception e) {
            // Non bloquant : si le snapshot échoue, le healing utilisera
            // le fallback buildElementInfoFromLocator()
            log.debug("Snapshot capture failed for '{}': {}", logicalName, e.getMessage());
        }
    }



    // ════════════════════════════════════════════════════════════════════════
    // pushHealingEvent — pousse l'événement de healing vers le dashboard
    // ════════════════════════════════════════════════════════════════════════
    private void pushHealingEvent(By locator, HealingResponse response, String logicalName,
                                   long healingTimeMs, RuntimeException firstFailure, boolean baselineHit) {
        try {
            Map<String, String> locMap = HealingUtils.convertByToMap(locator);
            String oldType = locMap.get("type");
            String oldVal = locMap.get("value");
            String newType = null;
            String newVal = null;
            String error = null;
            double score = 0.0;
            boolean success = false;
            Double structScore = null;
            Double semScore = null;
            Integer elementsExtracted = null;
            Integer afterStructFilter = null;
            Integer afterSpatialFilter = null;
            Integer sentToNlp = null;

            if (response != null) {
                success = response.isSuccess();
                score = response.getScore();
                error = response.getError();
                if (response.getNewLocator() != null) {
                    newType = response.getNewLocator().get("type");
                    newVal = response.getNewLocator().get("value");
                }
                if (response.getDetails() != null) {
                    System.out.println("[DEBUG HEALING] Details: " + response.getDetails());
                    Object rawStruct = response.getDetails().get("structural_score");
                    Object rawSem = response.getDetails().get("semantic_score");
                    if (rawStruct instanceof Number) structScore = ((Number) rawStruct).doubleValue();
                    if (rawSem instanceof Number) semScore = ((Number) rawSem).doubleValue();
                    Object rawElements = response.getDetails().get("elements_extracted");
                    Object rawAfterStruct = response.getDetails().get("after_struct_filter");
                    Object rawAfterSpatial = response.getDetails().get("after_spatial_filter");
                    Object rawSentToNlp = response.getDetails().get("sent_to_nlp");
                    System.out.println("[DEBUG HEALING] elements_extracted=" + rawElements + "(" + (rawElements != null ? rawElements.getClass().getName() : "null") + ")");
                    System.out.println("[DEBUG HEALING] after_struct_filter=" + rawAfterStruct);
                    System.out.println("[DEBUG HEALING] sent_to_nlp=" + rawSentToNlp);
                    if (rawElements instanceof Number) elementsExtracted = ((Number) rawElements).intValue();
                    if (rawAfterStruct instanceof Number) afterStructFilter = ((Number) rawAfterStruct).intValue();
                    if (rawAfterSpatial instanceof Number) afterSpatialFilter = ((Number) rawAfterSpatial).intValue();
                    if (rawSentToNlp instanceof Number) sentToNlp = ((Number) rawSentToNlp).intValue();
                } else {
                    System.out.println("[DEBUG HEALING] Details is NULL in response");
                }
            } else {
                System.out.println("[DEBUG HEALING] Response is NULL");
            }

            String exceptionType = firstFailure != null ? firstFailure.getClass().getSimpleName() : null;

            DashboardReporter.pushHealingEvent(success, score, oldType, oldVal,
                    newType, newVal, error, healingTimeMs,
                    exceptionType, structScore, semScore,
                    baselineHit, elementsExtracted, afterStructFilter, afterSpatialFilter, sentToNlp);
        } catch (Exception ignored) {
            // Non bloquant
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // callHealingAPI — construit la requête avec le meilleur snapshot disponible
    // ════════════════════════════════════════════════════════════════════════
    private HealingResponse callHealingAPI(By failedLocator,
                                           String logicalName,
                                           String cacheKey,
                                           String elementTypeHint) {
        try {
            String pageSource = driver.getPageSource();
            Map<String, String> oldLocatorMap = HealingUtils.convertByToMap(failedLocator);

            // ── Snapshot enrichi si disponible, sinon fallback minimal ──
            ElementInfo oldElement = elementSnapshotCache.containsKey(cacheKey)
                    ? elementSnapshotCache.get(cacheKey)
                    : HealingUtils.buildElementInfoFallback(failedLocator, logicalName, elementTypeHint);

            HealingRequest request = new HealingRequest();
            request.setOldLocator(oldLocatorMap);
            request.setOldElement(oldElement);
            request.setCurrentDom(pageSource);

            debugHealingRequestPayload(logicalName, oldLocatorMap, oldElement, pageSource, request);
            return healingClient.healSelector(request);

        } catch (Exception e) {
            log.error("Error while calling self-healing API", e);
            return null;
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // buildElementInfoFallback
    //
    // Utilisé UNIQUEMENT si aucun snapshot n'est disponible (premier appel
    // sur un élément qui n'a jamais été trouvé avec succès — cas rare).
    //
    // Différence avec l'ancienne version : element_type est laissé à null
    // au lieu d'être pollué par le type de locator ("id", "xpath"...).
    // Le moteur Python interprète null comme "pas de contrainte sur le tag" →
    // score structurel neutre au lieu d'un score 0 éliminatoire.
    // ════════════════════════════════════════════════════════════════════════
    private ElementInfo buildElementInfoFallback(By locator, String logicalName, String elementTypeHint) {
        return HealingUtils.buildElementInfoFallback(locator, logicalName, elementTypeHint);
    }

    protected By getRuntimeHealedLocator(String logicalName, By originalLocator) {
        String safeLogicalName = (logicalName == null || logicalName.isBlank())
                ? (originalLocator == null ? "unknown" : originalLocator.toString())
                : logicalName;
        return healedLocatorsCache.getOrDefault(cacheKey(safeLogicalName), originalLocator);
    }

    private String cacheKey(String logicalName) {
        return getClass().getName() + "::" + logicalName;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Méthodes utilitaires exposées aux pages filles
    // ════════════════════════════════════════════════════════════════════════

    protected WebElement waitForElementVisible(By locator, String logicalName) {
        refreshDriverReferences();
        WebElement element = findElement(locator, logicalName);
        try {
            return wait.until(ExpectedConditions.visibilityOf(element));
        } catch (StaleElementReferenceException e) {
            log.warn("Stale element after findElement for '{}', retrying...", logicalName);
            element = findElement(locator, logicalName);
            return wait.until(ExpectedConditions.visibilityOf(element));
        }
    }

    protected WebElement waitForElementLocated(By locator, String logicalName) {
        refreshDriverReferences();
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (TimeoutException firstTimeout) {
            log.warn("Timeout waiting for '{}' with {}. Trying self-healing...",
                    logicalName, locator);
            WebElement healed = findElement(locator, logicalName);
            By healedLocator = getRuntimeHealedLocator(logicalName, locator);
            if (!HealingUtils.isSameLocator(healedLocator, locator)) {
                return wait.until(ExpectedConditions.visibilityOfElementLocated(healedLocator));
            }
            return healed;
        }
    }

    protected WebElement waitForElementClickable(By locator, String logicalName) {
        refreshDriverReferences();
        try {
            return wait.until(ExpectedConditions.elementToBeClickable(locator));
        } catch (TimeoutException firstTimeout) {
            log.warn("Timeout waiting clickable '{}' with {}. Trying self-healing...",
                    logicalName, locator);
            WebElement healed = findElement(locator, logicalName);
            By healedLocator = getRuntimeHealedLocator(logicalName, locator);
            if (!HealingUtils.isSameLocator(healedLocator, locator)) {
                return wait.until(ExpectedConditions.elementToBeClickable(healedLocator));
            }
            return wait.until(ExpectedConditions.elementToBeClickable(healed));
        }
    }

    public String getCurrentUrl() {
        refreshDriverReferences();
        return driver.getCurrentUrl();
    }

    public String getPageTitle() {
        refreshDriverReferences();
        return driver.getTitle();
    }

    protected void navigateTo(String url) {
        refreshDriverReferences();
        driver.get(url);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Debug — écriture des payloads de healing sur disque
    // ════════════════════════════════════════════════════════════════════════
    private void debugHealingRequestPayload(String logicalName,
                                            Map<String, String> oldLocatorMap,
                                            ElementInfo oldElement,
                                            String currentDom,
                                            HealingRequest request) {
        if (!HEALING_DEBUG_REQUEST) return;

        try {
            String oldLocatorJson = DEBUG_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(oldLocatorMap);
            String oldElementJson = DEBUG_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(oldElement);
            String fullRequestJson = DEBUG_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(request);

            log.info("Self-healing debug [{}] old_locator={}", logicalName, oldLocatorJson);
            log.info("Self-healing debug [{}] old_element={}", logicalName, oldElementJson);
            log.info("Self-healing debug [{}] current_dom={}", logicalName, currentDom);
            log.info("Self-healing debug [{}] full_request={}", logicalName, fullRequestJson);

            HealingUtils.writeDebugFiles(logicalName, oldLocatorMap, oldElement, currentDom, request,
                    DEBUG_OBJECT_MAPPER, DEBUG_FILE_TIME_FORMAT);

            log.info("Self-healing debug files written in target/healing-debug/");
        } catch (IOException e) {
            log.warn("Unable to write self-healing debug payload files", e);
        }
    }
}
