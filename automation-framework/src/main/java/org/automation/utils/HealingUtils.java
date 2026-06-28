package org.automation.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.automation.ai.healing.ElementInfo;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public final class HealingUtils {

    private HealingUtils() {
    }

    public static Map<String, String> convertByToMap(By by) {
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

    public static By buildByFromResponse(Map<String, String> healedSelector) {
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

        return buildByFromMap(type, value);
    }

    public static By buildByFromMap(String type, String value) {
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

    public static String normalizeLocatorType(String type) {
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

    public static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }

    public static String escapeCssAttributeValue(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }

    public static String sanitizeForFilename(String input) {
        if (input == null || input.isBlank()) {
            return "unknown";
        }
        String cleaned = input.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (cleaned.length() > 80) {
            cleaned = cleaned.substring(0, 40) + "..." + cleaned.substring(cleaned.length() - 30);
        }
        return cleaned;
    }

    public static String parseXpathTag(String xpath) {
        if (xpath == null || xpath.isBlank()) {
            return null;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(?:^|/)\\*?([a-zA-Z]+)(?:\\[|$)")
                .matcher(xpath);
        return m.find() ? m.group(1).toLowerCase() : null;
    }

    public static void parseXpathAttributes(String xpath, ElementInfo info, Map<String, String> attrs) {
        if (xpath == null || xpath.isBlank()) {
            return;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("@([a-zA-Z-]+)\\s*=\\s*'([^']*)'")
                .matcher(xpath);
        while (m.find()) {
            String attrName = m.group(1);
            String attrValue = m.group(2);
            if ("id".equals(attrName)) {
                info.setElementId(attrValue);
            }
            attrs.put(attrName, attrValue);
        }
    }

    public static String buildXpathFromLocator(By locator, Map<String, String> attrs) {
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

    public static boolean isSameLocator(By a, By b) {
        return a != null && b != null && a.toString().equals(b.toString());
    }

    public static boolean isRelativeXpath(By locator) {
        Map<String, String> map = convertByToMap(locator);
        return "xpath".equals(map.get("type")) && map.getOrDefault("value", "").startsWith(".//");
    }

    public static Map<String, Double> extractCoordinates(WebDriver driver, WebElement element) {
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
        } catch (Exception e) {
            return null;
        }
    }

    public static String inferElementTypeFromLogicalName(String logicalName) {
        if (logicalName == null) {
            return null;
        }
        String n = logicalName.toLowerCase();
        if (n.contains("bouton") || n.contains("button") || n.contains("se connecter") || n.contains("click")) {
            return "button";
        }
        if (n.contains("champ") || n.contains("input") || n.contains("email") || n.contains("password")) {
            return "input";
        }
        if (n.contains("icon") || n.contains("icone")) {
            return "i";
        }
        return null;
    }

    public static ElementInfo buildElementInfoFallback(By locator, String logicalName) {
        return buildElementInfoFallback(locator, logicalName, null);
    }

    public static ElementInfo buildElementInfoFallback(By locator, String logicalName, String elementTypeHint) {
        ElementInfo info = new ElementInfo();
        Map<String, String> map = convertByToMap(locator);
        String type = map.get("type");
        String value = map.get("value");

        Map<String, String> attrs = new HashMap<>();
        String inferredTag = firstNonBlank(elementTypeHint, inferElementTypeFromLogicalName(logicalName));
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

    public static void writeDebugFiles(String logicalName,
                                       Map<String, String> oldLocatorMap,
                                       ElementInfo oldElement,
                                       String currentDom,
                                       org.automation.ai.healing.HealingRequest request,
                                       ObjectMapper objectMapper,
                                       DateTimeFormatter timeFormatter) throws IOException {
        String safeName = sanitizeForFilename(logicalName == null ? "unknown" : logicalName);
        String timestamp = LocalDateTime.now().format(timeFormatter);

        String oldLocatorJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(oldLocatorMap);
        String oldElementJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(oldElement);
        String fullRequestJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request);

        Path debugDir = Path.of("target", "healing-debug");
        Files.createDirectories(debugDir);
        Files.writeString(debugDir.resolve(timestamp + "-" + safeName + "-request.json"), fullRequestJson, StandardCharsets.UTF_8);
        Files.writeString(debugDir.resolve(timestamp + "-" + safeName + "-current-dom.html"), currentDom == null ? "" : currentDom, StandardCharsets.UTF_8);
        Files.writeString(debugDir.resolve(timestamp + "-" + safeName + "-old-locator.json"), oldLocatorJson, StandardCharsets.UTF_8);
        Files.writeString(debugDir.resolve(timestamp + "-" + safeName + "-old-element.json"), oldElementJson, StandardCharsets.UTF_8);
    }

}
