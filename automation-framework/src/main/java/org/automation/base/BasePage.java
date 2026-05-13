package org.automation.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.automation.ai.healing.ElementInfo;
import org.automation.ai.healing.HealingRequest;
import org.automation.ai.healing.HealingResponse;
import org.automation.ai.healing.SelfHealingClient;
import org.automation.factory.DriverFactory;
import org.automation.utils.ConfigLoader;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
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

        By locatorToUse = healedLocatorsCache.getOrDefault(cacheKey, originalLocator);

        try {
            WebElement element = driver.findElement(locatorToUse);
            // ── Snapshot enrichi capturé pendant que l'élément est trouvable ──
            captureElementSnapshot(element, originalLocator, safeLogicalName, cacheKey);
            return element;

        } catch (NoSuchElementException firstFailure) {

            // Si on utilisait un locator guéri mais qu'il casse à son tour,
            // retenter avec le locator original avant de re-healer.
            if (!isSameLocator(locatorToUse, originalLocator)) {
                try {
                    WebElement element = driver.findElement(originalLocator);
                    captureElementSnapshot(element, originalLocator, safeLogicalName, cacheKey);
                    return element;
                } catch (NoSuchElementException ignored) {
                    // Continue vers le self-healing.
                }
            }

            if (!HEALING_ENABLED) {
                log.warn("Healing disabled - cannot locate '{}'", safeLogicalName);
                throw firstFailure;
            }

            log.warn("Locator failed for '{}': {}. Starting self-healing...",
                    safeLogicalName, originalLocator);

            HealingResponse response = callHealingAPI(originalLocator, safeLogicalName, cacheKey, elementTypeHint);

            if (response != null && response.isSuccess() && response.getNewLocator() != null) {
                By healedBy = buildByFromResponse(response.getNewLocator());
                if (healedBy != null) {
                    healedLocatorsCache.put(cacheKey, healedBy);
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
            String xpath = buildXpathFromLocator(originalLocator, attrs);

            // ── Coordonnées via JS (getBoundingClientRect) ──
            Map<String, Double> coords = extractCoordinates(element);

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
    // extractCoordinates — getBoundingClientRect via JavaScript
    // ════════════════════════════════════════════════════════════════════════
    @SuppressWarnings("unchecked")
    private Map<String, Double> extractCoordinates(WebElement element) {
        try {
            Object result = ((JavascriptExecutor) driver).executeScript(
                    "var r = arguments[0].getBoundingClientRect();" +
                            "return {x: r.left, y: r.top, width: r.width, height: r.height};",
                    element);
            if (result instanceof Map<?, ?> raw) {
                Map<String, Double> coords = new HashMap<>();
                for (Map.Entry<?, ?> entry : raw.entrySet()) {
                    if (entry.getValue() instanceof Number n) {
                        coords.put(String.valueOf(entry.getKey()), n.doubleValue());
                    }
                }
                return coords.isEmpty() ? null : coords;
            }
        } catch (Exception ignored) {
            // Pas de coordonnées disponibles → le moteur utilisera score neutre 0.5
        }
        return null;
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
            Map<String, String> oldLocatorMap = convertByToMap(failedLocator);

            // ── Snapshot enrichi si disponible, sinon fallback minimal ──
            ElementInfo oldElement = elementSnapshotCache.containsKey(cacheKey)
                    ? elementSnapshotCache.get(cacheKey)
                    : buildElementInfoFallback(failedLocator, logicalName, elementTypeHint);

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
        ElementInfo info = new ElementInfo();
        Map<String, String> map = convertByToMap(locator);
        String type  = map.get("type");
        String value = map.get("value");

        // ── element_type = null (pas de tag connu) ──
        // Le moteur Python traitera un element_type null ou absent comme
        // "aucune contrainte de tag" → score structurel neutre (0.5).
        // NE PAS mettre le type du locator ici : "id" n'est pas un tag HTML.
        info.setElementType(firstNonBlank(elementTypeHint, inferElementTypeFromLogicalName(logicalName)));

        // ── Texte = logicalName (meilleure heuristique disponible sans DOM) ──
        info.setText(logicalName);

        // ── Attributs et xpath déduits du locator ──
        Map<String, String> attrs = new HashMap<>();
        switch (type != null ? type : "") {
            case "id" -> {
                info.setElementId(value);
                attrs.put("id", value);
                info.setXpath("//*[@id='" + value + "']");
            }
            case "name" -> {
                attrs.put("name", value);
                info.setXpath("//*[@name='" + value + "']");
            }
            case "xpath" -> info.setXpath(value);
            case "css"   -> attrs.put("css", value);
            default      -> {}
        }
        if (!attrs.isEmpty()) info.setAttributes(attrs);

        return info;
    }

    private String inferElementTypeFromLogicalName(String logicalName) {
        if (logicalName == null) return null;
        String n = logicalName.toLowerCase();
        if (n.contains("bouton") || n.contains("button") || n.contains("se connecter") || n.contains("click")) {
            return "button";
        }
        if (n.contains("champ") || n.contains("input") || n.contains("email") || n.contains("password")) {
            return "input";
        }
        return null;
    }

    // ════════════════════════════════════════════════════════════════════════
    // buildXpathFromLocator — XPath de fallback déduit du By original
    // ════════════════════════════════════════════════════════════════════════
    private String buildXpathFromLocator(By locator, Map<String, String> attrs) {
        Map<String, String> map = convertByToMap(locator);
        String type  = map.get("type");
        String value = map.get("value");

        return switch (type != null ? type : "") {
            case "id"    -> "//*[@id='" + value + "']";
            case "name"  -> "//*[@name='" + value + "']";
            case "xpath" -> value;
            default      -> {
                // Essayer de construire depuis l'id capturé
                String id = attrs.get("id");
                yield id != null ? "//*[@id='" + id + "']" : null;
            }
        };
    }

    // ════════════════════════════════════════════════════════════════════════
    // Méthodes utilitaires
    // ════════════════════════════════════════════════════════════════════════

    private Map<String, String> convertByToMap(By by) {
        Map<String, String> map = new HashMap<>();
        String byString = by.toString().trim();

        if (byString.startsWith("By.id:")) {
            map.put("type", "id");
            map.put("value", byString.substring("By.id:".length()).trim());
        } else if (byString.startsWith("By.name:")) {
            map.put("type", "name");
            map.put("value", byString.substring("By.name:".length()).trim());
        } else if (byString.startsWith("By.xpath:")) {
            map.put("type", "xpath");
            map.put("value", byString.substring("By.xpath:".length()).trim());
        } else if (byString.startsWith("By.cssSelector:")) {
            map.put("type", "css");
            map.put("value", byString.substring("By.cssSelector:".length()).trim());
        } else if (byString.startsWith("By.className:")) {
            map.put("type", "className");
            map.put("value", byString.substring("By.className:".length()).trim());
        } else if (byString.startsWith("By.tagName:")) {
            map.put("type", "tagName");
            map.put("value", byString.substring("By.tagName:".length()).trim());
        } else if (byString.startsWith("By.linkText:")) {
            map.put("type", "linkText");
            map.put("value", byString.substring("By.linkText:".length()).trim());
        } else if (byString.startsWith("By.partialLinkText:")) {
            map.put("type", "partialLinkText");
            map.put("value", byString.substring("By.partialLinkText:".length()).trim());
        } else {
            map.put("type", "unknown");
            map.put("value", byString);
        }
        return map;
    }

    private By buildByFromResponse(Map<String, String> healedSelector) {
        if (healedSelector == null || healedSelector.isEmpty()) return null;

        String type = firstNonBlank(
                healedSelector.get("type"),
                healedSelector.get("strategy"),
                healedSelector.get("locatorType"),
                healedSelector.get("locator_type")
        );
        String value = firstNonBlank(
                healedSelector.get("value"),
                healedSelector.get("selector"),
                healedSelector.get("locator")
        );

        // Fallback sur les clés directes si type/value absents
        if (type == null) {
            if (healedSelector.containsKey("xpath"))        { type = "xpath";  value = healedSelector.get("xpath"); }
            else if (healedSelector.containsKey("id"))      { type = "id";     value = healedSelector.get("id"); }
            else if (healedSelector.containsKey("css"))     { type = "css";    value = healedSelector.get("css"); }
            else if (healedSelector.containsKey("name"))    { type = "name";   value = healedSelector.get("name"); }
            else if (healedSelector.containsKey("data-testid")) {
                type = "data-testid"; value = healedSelector.get("data-testid");
            }
        }

        if (type == null || value == null || value.isBlank()) return null;

        return switch (normalizeLocatorType(type)) {
            case "id"              -> By.id(value);
            case "name"            -> By.name(value);
            case "xpath"           -> By.xpath(value);
            case "css"             -> By.cssSelector(value);
            case "className"       -> By.className(value);
            case "tagName"         -> By.tagName(value);
            case "linkText"        -> By.linkText(value);
            case "partialLinkText" -> By.partialLinkText(value);
            case "data-testid"     -> By.cssSelector("[data-testid='" + escapeCssAttributeValue(value) + "']");
            default                -> null;
        };
    }

    private String normalizeLocatorType(String type) {
        return switch (type.trim().toLowerCase()) {
            case "cssselector", "css_selector", "css"          -> "css";
            case "classname",   "class_name"                   -> "className";
            case "tagname",     "tag_name"                     -> "tagName";
            case "linktext",    "link_text"                    -> "linkText";
            case "partiallinktext", "partial_link_text"        -> "partialLinkText";
            case "data-testid", "data_testid", "datatestid"    -> "data-testid";
            default                                            -> type.trim().toLowerCase();
        };
    }

    private boolean isSameLocator(By a, By b) {
        return a != null && b != null && a.toString().equals(b.toString());
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

    private String firstNonBlank(String... values) {
        for (String v : values) if (v != null && !v.isBlank()) return v;
        return null;
    }

    private String escapeCssAttributeValue(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }

    // ════════════════════════════════════════════════════════════════════════
    // Méthodes utilitaires exposées aux pages filles
    // ════════════════════════════════════════════════════════════════════════

    protected WebElement waitForElementVisible(By locator, String logicalName) {
        refreshDriverReferences();
        WebElement element = findElement(locator, logicalName);
        return wait.until(ExpectedConditions.visibilityOf(element));
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

        String safeName  = sanitizeForFilename(logicalName == null ? "unknown" : logicalName);
        String timestamp = LocalDateTime.now().format(DEBUG_FILE_TIME_FORMAT);

        try {
            String oldLocatorJson  = DEBUG_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(oldLocatorMap);
            String oldElementJson  = DEBUG_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(oldElement);
            String fullRequestJson = DEBUG_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(request);

            log.info("Self-healing debug [{}] old_locator={}", logicalName, oldLocatorJson);
            log.info("Self-healing debug [{}] old_element={}", logicalName, oldElementJson);
            log.info("Self-healing debug [{}] current_dom={}", logicalName, currentDom);
            log.info("Self-healing debug [{}] full_request={}", logicalName, fullRequestJson);

            Path debugDir = Path.of("target", "healing-debug");
            Files.createDirectories(debugDir);
            Files.writeString(debugDir.resolve(timestamp + "-" + safeName + "-request.json"),    fullRequestJson, StandardCharsets.UTF_8);
            Files.writeString(debugDir.resolve(timestamp + "-" + safeName + "-current-dom.html"), currentDom == null ? "" : currentDom, StandardCharsets.UTF_8);
            Files.writeString(debugDir.resolve(timestamp + "-" + safeName + "-old-locator.json"), oldLocatorJson, StandardCharsets.UTF_8);
            Files.writeString(debugDir.resolve(timestamp + "-" + safeName + "-old-element.json"), oldElementJson, StandardCharsets.UTF_8);

            log.info("Self-healing debug files written in target/healing-debug/");
        } catch (IOException e) {
            log.warn("Unable to write self-healing debug payload files", e);
        }
    }

    private String sanitizeForFilename(String input) {
        if (input == null || input.isBlank()) return "unknown";
        return input.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
