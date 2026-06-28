package org.automation.ai.healing;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Snapshot d'un element DOM capture lors d'un acces reussi.
 * Stocke toutes les informations necessaires pour identifier
 * et retrouver un element apres un changement de l'interface.
 */
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

}
