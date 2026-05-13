package org.automation.ai.healing;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class ElementInfo {

    @JsonProperty("element_id")
    private String elementId;

    @JsonProperty("element_type")
    private String elementType;

    private String text;
    private Map<String, String> attributes;
    private String xpath;

    @JsonProperty("is_row_relative")
    private boolean isRowRelative = false;

    // x, y, width, height
    private Map<String, Double> coordinates;

    private Map<String, Double> size;

    public ElementInfo() {
    }

    public ElementInfo(String elementId, String elementType, String text,
                       Map<String, String> attributes, String xpath,
                       Map<String, Double> coordinates, Map<String, Double> size) {
        this.elementId = elementId;
        this.elementType = elementType;
        this.text = text;
        this.attributes = attributes;
        this.xpath = xpath;
        this.coordinates = coordinates;
        this.size = size;
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public String getElementType() {
        return elementType;
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    public boolean isRowRelative() {
        return isRowRelative;
    }

    public void setRowRelative(boolean rowRelative) {
        isRowRelative = rowRelative;
    }

    public Map<String, Double> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Map<String, Double> coordinates) {
        this.coordinates = coordinates;
    }

    public Map<String, Double> getSize() {
        return size;
    }

    public void setSize(Map<String, Double> size) {
        this.size = size;
    }

    public static ElementInfo fromMap(Map<String, Object> map) {
        ElementInfo info = new ElementInfo();
        info.setElementId((String) map.get("element_id"));
        info.setElementType((String) map.get("element_type"));
        info.setText((String) map.get("text"));
        info.setAttributes(castStringMap(map.get("attributes")));
        info.setXpath((String) map.get("xpath"));
        info.setCoordinates(castNumericMap(map.get("coordinates")));
        info.setSize(castNumericMap(map.get("size")));
        Object isRowRelativeValue = map.get("is_row_relative");
        if (isRowRelativeValue instanceof Boolean) {
            info.setRowRelative((Boolean) isRowRelativeValue);
        }
        return info;
    }

    private static Map<String, String> castStringMap(Object value) {
        if (!(value instanceof Map<?, ?> rawMap)) {
            return null;
        }

        Map<String, String> out = new HashMap<>();
        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                out.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            }
        }
        return out;
    }

    private static Map<String, Double> castNumericMap(Object value) {
        if (!(value instanceof Map<?, ?> rawMap)) {
            return null;
        }

        Map<String, Double> out = new HashMap<>();
        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            if (entry.getValue() instanceof Number n) {
                out.put(String.valueOf(entry.getKey()), n.doubleValue());
            } else {
                try {
                    out.put(String.valueOf(entry.getKey()), Double.parseDouble(String.valueOf(entry.getValue())));
                } catch (NumberFormatException ignored) {
                    // Ignore values that are not numeric.
                }
            }
        }
        return out;
    }
}
