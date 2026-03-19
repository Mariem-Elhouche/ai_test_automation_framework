package org.automation.ai.healing;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class HealingRequest {

    @JsonProperty("old_locator")
    private Map<String, String> oldLocator;   // { "type": "id", "value": "loginBtn" }

    @JsonProperty("old_element")
    private ElementInfo oldElement;

    @JsonProperty("current_dom")
    private String currentDom;                 // HTML brut de la page

    // Constructeur par défaut
    public HealingRequest() {
    }

    // Constructeur avec paramètres
    public HealingRequest(Map<String, String> oldLocator, ElementInfo oldElement, String currentDom) {
        this.oldLocator = oldLocator;
        this.oldElement = oldElement;
        this.currentDom = currentDom;
    }

    // Getters et setters
    public Map<String, String> getOldLocator() {
        return oldLocator;
    }

    public void setOldLocator(Map<String, String> oldLocator) {
        this.oldLocator = oldLocator;
    }

    public ElementInfo getOldElement() {
        return oldElement;
    }

    public void setOldElement(ElementInfo oldElement) {
        this.oldElement = oldElement;
    }

    public String getCurrentDom() {
        return currentDom;
    }

    public void setCurrentDom(String currentDom) {
        this.currentDom = currentDom;
    }
}