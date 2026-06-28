package org.automation.ai.healing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.automation.dashboard.DashboardReporter;
import org.automation.utils.ConfigLoader;
import org.automation.utils.HealingUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Orchestrateur central du processus de self-healing.
 *
 * Coordonne les 3 etapes : 1) cache de locators deja gueris,
 * 2) capture de snapshot des elements reussis, 3) appel a l'API
 * de healing externe via SelfHealingClient en cas d'echec.
 *
 * Les resultats sont pousses vers le Dashboard pour analyse.
 */
public class HealingLocatorResolver {

    private static final Logger log = LoggerFactory.getLogger(HealingLocatorResolver.class);

    private static final boolean HEALING_ENABLED = Boolean.parseBoolean(
            ConfigLoader.getProperty("self.healing.enabled", "true")
    );
    private static final boolean HEALING_DEBUG_REQUEST = Boolean.parseBoolean(
            ConfigLoader.getProperty("self.healing.debug.request", "true")
    );

    private static final ObjectMapper DEBUG_OBJECT_MAPPER = new ObjectMapper();
    private static final DateTimeFormatter DEBUG_FILE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    private static final Map<String, By> healedLocatorsCache = new ConcurrentHashMap<>();
    private static final Map<String, ElementInfo> elementSnapshotCache = new ConcurrentHashMap<>();

    private final SelfHealingClient healingClient = new SelfHealingClient();

    /**
     * Retourne le locator en cache (deja gueri) s'il existe,
     * sinon le locator original. Permet d'eviter un appel API
     * repetitif pour un meme element deja reussi.
     */
    public By getPreferredLocator(WebDriver driver, SearchContext scope, By originalLocator) {
        return healedLocatorsCache.getOrDefault(cacheKey(driver, scope, originalLocator), originalLocator);
    }

    /**
     * Capture un snapshot complet de l'element pour reference future.
     * Stocke : tagName, texte visible, attributs (id, class, placeholder...),
     * coordonnees (x, y), taille (width, height), xpath et flag isRowRelative.
     *
     * Le snapshot est utilise par l'API de healing pour le matching
     * lorsque le locator original echoue.
     */
    public void captureSnapshot(WebDriver driver, SearchContext scope, By originalLocator, WebElement element) {
        if (driver == null || originalLocator == null || element == null) {
            return;
        }

        String cacheKey = cacheKey(driver, scope, originalLocator);

        try {
            String tagName = element.getTagName();

            String visibleText = element.getText();
            if ((visibleText == null || visibleText.isBlank()) && driver instanceof JavascriptExecutor js) {
                visibleText = (String) js.executeScript("return arguments[0].innerText;", element);
            }

            Map<String, String> attrs = new HashMap<>();
            for (String attr : new String[]{
                    "id", "class", "type", "placeholder", "aria-label",
                    "href", "name", "role", "data-testid", "data-cy",
                    "tabindex", "value"
            }) {
                String val = element.getAttribute(attr);
                if (val != null && !val.isBlank()) {
                    attrs.put(attr, val);
                }
            }

            String stableId = null;
            String rawId = attrs.get("id");
            if (rawId != null && !rawId.matches("f_[0-9a-fA-F\\-]{30,}")) {
                stableId = rawId;
            }

            String xpath = buildXpathFromLocator(originalLocator, attrs);
            Map<String, Double> coords = extractCoordinates(driver, element);

            Map<String, Double> size = new HashMap<>();
            if (coords != null) {
                if (coords.containsKey("width")) {
                    size.put("width", coords.get("width"));
                }
                if (coords.containsKey("height")) {
                    size.put("height", coords.get("height"));
                }
            }

            ElementInfo snapshot = new ElementInfo();
            snapshot.setElementType(tagName);
            snapshot.setText(visibleText != null ? visibleText.trim() : originalLocator.toString());
            snapshot.setElementId(stableId);
            snapshot.setAttributes(attrs.isEmpty() ? null : attrs);
            snapshot.setXpath(xpath);
            snapshot.setCoordinates(coords != null && !coords.isEmpty() ? coords : null);
            snapshot.setSize(size.isEmpty() ? null : size);
            snapshot.setRowRelative(isRelativeXpath(originalLocator));

            elementSnapshotCache.put(cacheKey, snapshot);
        } catch (Exception e) {
            log.debug("Snapshot capture failed for {}: {}", originalLocator, e.getMessage());
        }
    }

    /**
     * Pipeline principal de resolution de locator en cas d'echec.
     *
     * Etapes :
     * 1. Construit la requete avec le DOM courant et le snapshot de l'element
     * 2. Appelle l'API de healing (POST /heal)
     * 3. Pousse l'evenement vers le Dashboard
     * 4. Si succes, stocke le nouveau locator dans le cache et le retourne
     * 5. Si echec, retourne null (l'exception originale sera relancee)
     */
    public By resolveLocator(WebDriver driver, SearchContext scope, By failedLocator, RuntimeException firstFailure) {
        if (!HEALING_ENABLED || driver == null || failedLocator == null) {
            return null;
        }

        String cacheKey = cacheKey(driver, scope, failedLocator);
        String logicalName = buildLogicalName(driver, scope, failedLocator);

        log.warn("Locator failed for '{}' with {}: {}. Starting self-healing...",
                logicalName,
                firstFailure == null ? "UnknownException" : firstFailure.getClass().getSimpleName(),
                failedLocator);

        long healingStart = System.currentTimeMillis();
        HealingResponse response = callHealingAPI(driver, failedLocator, logicalName, cacheKey);
        long healingTimeMs = System.currentTimeMillis() - healingStart;

        pushHealingEvent(failedLocator, response, healingTimeMs, firstFailure);

        if (response == null || !response.isSuccess() || response.getNewLocator() == null) {
            if (response != null) {
                log.warn("Self-healing response for '{}': success={}, error={}, score={}, newLocator={}, details={}",
                        logicalName,
                        response.isSuccess(),
                        response.getError(),
                        response.getScore(),
                        response.getNewLocator(),
                        response.getDetails());
            }
            return null;
        }

        By healedBy = buildByFromResponse(response.getNewLocator());
        if (healedBy == null) {
            return null;
        }

        healedLocatorsCache.put(cacheKey, healedBy);
        log.info("Self-healing succeeded for '{}'. New locator: {}", logicalName, healedBy);
        return healedBy;
    }

    public boolean isSameLocator(By a, By b) {
        return HealingUtils.isSameLocator(a, b);
    }

    /**
     * Construit et envoie la requete de healing a l'API externe.
     * Inclut le DOM courant (pageSource), le snapshot de l'element
     * (ou un fallback si absent), et le locator qui a echoue.
     * Sauvegarde les fichiers de debug si active.
     */
    private HealingResponse callHealingAPI(WebDriver driver, By failedLocator, String logicalName, String cacheKey) {
        try {
            String pageSource = driver.getPageSource();
            Map<String, String> oldLocatorMap = convertByToMap(failedLocator);

            ElementInfo oldElement = elementSnapshotCache.containsKey(cacheKey)
                    ? elementSnapshotCache.get(cacheKey)
                    : buildElementInfoFallback(failedLocator, logicalName);

            HealingRequest request = new HealingRequest();
            request.setOldLocator(oldLocatorMap);
            request.setOldElement(oldElement);
            request.setCurrentDom(pageSource);
            request.setRunId(DashboardReporter.getRunId());

            debugHealingRequestPayload(logicalName, oldLocatorMap, oldElement, pageSource, request);
            return healingClient.healSelector(request);
        } catch (Exception e) {
            log.error("Error while calling self-healing API", e);
            return null;
        }
    }

    /**
     * Extrait les informations de la reponse de healing et les pousse
     * vers le Dashboard via DashboardReporter.pushHealingEvent().
     * Les compteurs du pipeline (elements_extracted, after_struct_filter, etc.)
     * sont extraits du champ details de la reponse s'ils sont presents.
     * Non-bloquant : les exceptions sont silencieusement ignorees.
     */
    private void pushHealingEvent(By locator, HealingResponse response, long healingTimeMs, RuntimeException firstFailure) {
        try {
            Map<String, String> locMap = convertByToMap(locator);
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
                    Object rawStruct = response.getDetails().get("structural_score");
                    Object rawSem = response.getDetails().get("semantic_score");
                    if (rawStruct instanceof Number n) {
                        structScore = n.doubleValue();
                    }
                    if (rawSem instanceof Number n) {
                        semScore = n.doubleValue();
                    }
                    Object rawElements = response.getDetails().get("elements_extracted");
                    Object rawAfterStruct = response.getDetails().get("after_struct_filter");
                    Object rawAfterSpatial = response.getDetails().get("after_spatial_filter");
                    Object rawSentToNlp = response.getDetails().get("sent_to_nlp");
                    if (rawElements instanceof Number n) elementsExtracted = n.intValue();
                    if (rawAfterStruct instanceof Number n) afterStructFilter = n.intValue();
                    if (rawAfterSpatial instanceof Number n) afterSpatialFilter = n.intValue();
                    if (rawSentToNlp instanceof Number n) sentToNlp = n.intValue();
                }
            }

            String exceptionType = firstFailure != null ? firstFailure.getClass().getSimpleName() : null;
            DashboardReporter.pushHealingEvent(success, score, oldType, oldVal,
                    newType, newVal, error, healingTimeMs,
                    exceptionType, structScore, semScore,
                    false, elementsExtracted, afterStructFilter, afterSpatialFilter, sentToNlp);
        } catch (Exception ignored) {
            // Non-blocking dashboard push.
        }
    }

    private ElementInfo buildElementInfoFallback(By locator, String logicalName) {
        return HealingUtils.buildElementInfoFallback(locator, logicalName);
    }

    private Map<String, Double> extractCoordinates(WebDriver driver, WebElement element) {
        return HealingUtils.extractCoordinates(driver, element);
    }

    private String buildXpathFromLocator(By locator, Map<String, String> attrs) {
        return HealingUtils.buildXpathFromLocator(locator, attrs);
    }

    private Map<String, String> convertByToMap(By by) {
        return HealingUtils.convertByToMap(by);
    }

    private By buildByFromResponse(Map<String, String> healedSelector) {
        return HealingUtils.buildByFromResponse(healedSelector);
    }

    private String buildLogicalName(WebDriver driver, SearchContext scope, By locator) {
        return scopeDescription(driver, scope) + "::" + locator;
    }

    private String cacheKey(WebDriver driver, SearchContext scope, By locator) {
        return scopeDescription(driver, scope) + "::" + locator;
    }

    /**
     * Decrit le contexte de recherche sous forme de chaine lisible.
     * Pour un scope driver : "driver::hostname"
     * Pour un scope element : "elem::hostname::hex(tag|id|class|text)"
     * En cas d'element stale : "elem::hostname::stale"
     * Utilise pour construire les cles de cache et les noms logiques.
     */
    private String scopeDescription(WebDriver driver, SearchContext scope) {
        String currentUrl = "";
        try {
            String url = driver.getCurrentUrl();
            if (url != null) {
                java.net.URI uri = new java.net.URI(url);
                currentUrl = uri.getHost() != null ? uri.getHost() : "";
            }
        } catch (Exception ignored) {
        }

        if (scope instanceof WebDriver) {
            return "driver::" + currentUrl;
        }

        WebElement contextElement = unwrapElement(scope);
        if (contextElement == null) {
            return "unknown::" + currentUrl;
        }

        try {
            String tag = contextElement.getTagName();
            String id = safe(contextElement.getAttribute("id"));
            String clazz = safe(contextElement.getAttribute("class"));
            String text = safe(contextElement.getText());
            String signature = tag + "|" + id + "|" + truncate(clazz, 50) + "|" + truncate(text, 50);
            return "elem::" + currentUrl + "::" + Integer.toHexString(signature.hashCode());
        } catch (Exception e) {
            return "elem::" + currentUrl + "::stale";
        }
    }

    private WebElement unwrapElement(Object scope) {
        if (scope instanceof WrapsElement wrapsElement) {
            return wrapsElement.getWrappedElement();
        }
        if (scope instanceof WebElement webElement) {
            return webElement;
        }
        return null;
    }

    private boolean isRelativeXpath(By locator) {
        return HealingUtils.isRelativeXpath(locator);
    }

    /**
     * Sauvegarde les fichiers de debug dans target/healing-debug/
     * pour diagnostiquer les echecs de healing.
     * Genere : request.json, current-dom.html, old-locator.json, old-element.json
     * Active uniquement si self.healing.debug.request=true.
     */
    private void debugHealingRequestPayload(String logicalName,
                                             Map<String, String> oldLocatorMap,
                                             ElementInfo oldElement,
                                             String currentDom,
                                             HealingRequest request) {
        if (!HEALING_DEBUG_REQUEST) {
            return;
        }

        try {
            HealingUtils.writeDebugFiles(logicalName, oldLocatorMap, oldElement, currentDom, request,
                    DEBUG_OBJECT_MAPPER, DEBUG_FILE_TIME_FORMAT);
        } catch (IOException e) {
            log.warn("Unable to write self-healing debug payload files", e);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value == null ? "" : value;
        }
        return value.substring(0, maxLength);
    }
}
