package org.automation.ai.healing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.automation.dashboard.DashboardReporter;
import org.automation.utils.ConfigLoader;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    public By getPreferredLocator(WebDriver driver, SearchContext scope, By originalLocator) {
        return healedLocatorsCache.getOrDefault(cacheKey(driver, scope, originalLocator), originalLocator);
    }

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
        return a != null && b != null && a.toString().equals(b.toString());
    }

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
                }
            }

            String exceptionType = firstFailure != null ? firstFailure.getClass().getSimpleName() : null;
            DashboardReporter.pushHealingEvent(success, score, oldType, oldVal,
                    newType, newVal, error, healingTimeMs,
                    exceptionType, structScore, semScore);
        } catch (Exception ignored) {
            // Non-blocking dashboard push.
        }
    }

    private ElementInfo buildElementInfoFallback(By locator, String logicalName) {
        ElementInfo info = new ElementInfo();
        Map<String, String> map = convertByToMap(locator);
        String type = map.get("type");
        String value = map.get("value");

        Map<String, String> attrs = new HashMap<>();
        String inferredTag = inferElementTypeFromLogicalName(logicalName);
        info.setElementType(inferredTag);

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
            case "xpath" -> {
                info.setXpath(value);
                parseXpathAttributes(value, info, attrs);
                if (inferredTag == null) {
                    info.setElementType(parseXpathTag(value));
                }
                info.setRowRelative(value != null && value.startsWith(".//"));
            }
            case "css" -> attrs.put("css", value);
            default -> {
            }
        }

        String text = firstNonBlank(
                attrs.get("placeholder"),
                attrs.get("aria-label"),
                attrs.get("value"),
                attrs.get("title"),
                logicalName
        );
        info.setText(text);

        if (!attrs.isEmpty()) {
            info.setAttributes(attrs);
        }
        return info;
    }

    private Map<String, Double> extractCoordinates(WebDriver driver, WebElement element) {
        if (!(driver instanceof JavascriptExecutor js)) {
            return null;
        }

        try {
            Object result = js.executeScript(
                    "var r = arguments[0].getBoundingClientRect();" +
                            "return {x: r.left, y: r.top, width: r.width, height: r.height};",
                    element
            );
            if (!(result instanceof Map<?, ?> raw)) {
                return null;
            }

            Map<String, Double> coords = new HashMap<>();
            for (Map.Entry<?, ?> entry : raw.entrySet()) {
                if (entry.getValue() instanceof Number n) {
                    coords.put(String.valueOf(entry.getKey()), n.doubleValue());
                }
            }
            return coords.isEmpty() ? null : coords;
        } catch (Exception ignored) {
            return null;
        }
    }

    private String buildXpathFromLocator(By locator, Map<String, String> attrs) {
        Map<String, String> map = convertByToMap(locator);
        String type = map.get("type");
        String value = map.get("value");

        return switch (type != null ? type : "") {
            case "id" -> "//*[@id='" + value + "']";
            case "name" -> "//*[@name='" + value + "']";
            case "xpath" -> value;
            default -> {
                String id = attrs.get("id");
                yield id != null ? "//*[@id='" + id + "']" : null;
            }
        };
    }

    private String parseXpathTag(String xpath) {
        if (xpath == null || xpath.isBlank()) {
            return null;
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("(?:^|/)\\*?([a-zA-Z]+)(?:\\[|$)")
                .matcher(xpath);
        return matcher.find() ? matcher.group(1).toLowerCase() : null;
    }

    private void parseXpathAttributes(String xpath, ElementInfo info, Map<String, String> attrs) {
        if (xpath == null || xpath.isBlank()) {
            return;
        }

        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("@([a-zA-Z-]+)\\s*=\\s*'([^']*)'")
                .matcher(xpath);

        while (matcher.find()) {
            String attrName = matcher.group(1);
            String attrValue = matcher.group(2);
            if ("id".equals(attrName)) {
                info.setElementId(attrValue);
            }
            attrs.put(attrName, attrValue);
        }
    }

    private String inferElementTypeFromLogicalName(String logicalName) {
        if (logicalName == null) {
            return null;
        }

        String name = logicalName.toLowerCase();
        if (name.contains("button") || name.contains("bouton") || name.contains("click")) {
            return "button";
        }
        if (name.contains("input") || name.contains("champ") || name.contains("email") || name.contains("password")) {
            return "input";
        }
        if (name.contains("icon") || name.contains("icone")) {
            return "i";
        }
        return null;
    }

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
        if (healedSelector == null || healedSelector.isEmpty()) {
            return null;
        }

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

        if (type == null) {
            if (healedSelector.containsKey("xpath")) {
                type = "xpath";
                value = healedSelector.get("xpath");
            } else if (healedSelector.containsKey("id")) {
                type = "id";
                value = healedSelector.get("id");
            } else if (healedSelector.containsKey("css")) {
                type = "css";
                value = healedSelector.get("css");
            } else if (healedSelector.containsKey("name")) {
                type = "name";
                value = healedSelector.get("name");
            } else if (healedSelector.containsKey("data-testid")) {
                type = "data-testid";
                value = healedSelector.get("data-testid");
            }
        }

        if (type == null || value == null || value.isBlank()) {
            return null;
        }

        return switch (normalizeLocatorType(type)) {
            case "id" -> By.id(value);
            case "name" -> By.name(value);
            case "xpath" -> By.xpath(value);
            case "css" -> By.cssSelector(value);
            case "className" -> By.className(value);
            case "tagName" -> By.tagName(value);
            case "linkText" -> By.linkText(value);
            case "partialLinkText" -> By.partialLinkText(value);
            case "data-testid" -> By.cssSelector("[data-testid='" + escapeCssAttributeValue(value) + "']");
            default -> null;
        };
    }

    private String normalizeLocatorType(String type) {
        return switch (type.trim().toLowerCase()) {
            case "cssselector", "css_selector", "css" -> "css";
            case "classname", "class_name" -> "className";
            case "tagname", "tag_name" -> "tagName";
            case "linktext", "link_text" -> "linkText";
            case "partiallinktext", "partial_link_text" -> "partialLinkText";
            case "data-testid", "data_testid", "datatestid" -> "data-testid";
            default -> type.trim().toLowerCase();
        };
    }

    private String buildLogicalName(WebDriver driver, SearchContext scope, By locator) {
        return scopeDescription(driver, scope) + "::" + locator;
    }

    private String cacheKey(WebDriver driver, SearchContext scope, By locator) {
        return scopeDescription(driver, scope) + "::" + locator;
    }

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
        Map<String, String> map = convertByToMap(locator);
        return "xpath".equals(map.get("type")) && map.getOrDefault("value", "").startsWith(".//");
    }

    private void debugHealingRequestPayload(String logicalName,
                                            Map<String, String> oldLocatorMap,
                                            ElementInfo oldElement,
                                            String currentDom,
                                            HealingRequest request) {
        if (!HEALING_DEBUG_REQUEST) {
            return;
        }

        String safeName = sanitizeForFilename(logicalName == null ? "unknown" : logicalName);
        String timestamp = LocalDateTime.now().format(DEBUG_FILE_TIME_FORMAT);

        try {
            String oldLocatorJson = DEBUG_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(oldLocatorMap);
            String oldElementJson = DEBUG_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(oldElement);
            String fullRequestJson = DEBUG_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(request);

            Path debugDir = Path.of("target", "healing-debug");
            Files.createDirectories(debugDir);
            Files.writeString(debugDir.resolve(timestamp + "-" + safeName + "-request.json"), fullRequestJson, StandardCharsets.UTF_8);
            Files.writeString(debugDir.resolve(timestamp + "-" + safeName + "-current-dom.html"), currentDom == null ? "" : currentDom, StandardCharsets.UTF_8);
            Files.writeString(debugDir.resolve(timestamp + "-" + safeName + "-old-locator.json"), oldLocatorJson, StandardCharsets.UTF_8);
            Files.writeString(debugDir.resolve(timestamp + "-" + safeName + "-old-element.json"), oldElementJson, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("Unable to write self-healing debug payload files", e);
        }
    }

    private String sanitizeForFilename(String input) {
        if (input == null || input.isBlank()) {
            return "unknown";
        }
        String cleaned = input.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (cleaned.length() > 80) {
            cleaned = cleaned.substring(0, 40) + "..." + cleaned.substring(cleaned.length() - 30);
        }
        return cleaned;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String escapeCssAttributeValue(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'");
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
