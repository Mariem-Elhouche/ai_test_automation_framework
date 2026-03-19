package org.automation.ai.healing;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class ElementInfo {

    @JsonProperty("element_id")
    private String elementId;

    @JsonProperty("element_type")
    private String elementType;

    private String text;

    private Map<String, String> attributes;

    private String xpath;

    private Map<String, Integer> coordinates; // { "x": ..., "y": ... }

    private Map<String, Integer> size;        // { "width": ..., "height": ... }

    // Constructeur par défaut (nécessaire pour Jackson)
    public ElementInfo() {
    }

    // Constructeur avec tous les champs
    public ElementInfo(String elementId, String elementType, String text,
                       Map<String, String> attributes, String xpath,
                       Map<String, Integer> coordinates, Map<String, Integer> size) {
        this.elementId = elementId;
        this.elementType = elementType;
        this.text = text;
        this.attributes = attributes;
        this.xpath = xpath;
        this.coordinates = coordinates;
        this.size = size;
    }

    // Getters et setters
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

    public Map<String, Integer> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Map<String, Integer> coordinates) {
        this.coordinates = coordinates;
    }

    public Map<String, Integer> getSize() {
        return size;
    }

    public void setSize(Map<String, Integer> size) {
        this.size = size;
    }

    // Méthode utilitaire pour créer une instance depuis une Map (si jamais vous devez désérialiser manuellement)
    public static ElementInfo fromMap(Map<String, Object> map) {
        ElementInfo info = new ElementInfo();
        info.setElementId((String) map.get("element_id"));
        info.setElementType((String) map.get("element_type"));
        info.setText((String) map.get("text"));
        info.setAttributes((Map<String, String>) map.get("attributes"));
        info.setXpath((String) map.get("xpath"));
        info.setCoordinates((Map<String, Integer>) map.get("coordinates"));
        info.setSize((Map<String, Integer>) map.get("size"));
        return info;
    }
}