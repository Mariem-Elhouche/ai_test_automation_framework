package org.automation.ai.healing;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * DTO de requete envoyee a l'API de healing (POST /heal).
 * Contient le locator qui a echoue, le snapshot de l'element original
 * et le DOM courant de la page pour permettre le matching.
 */
public class HealingRequest {

    @JsonProperty("old_locator")
    private Map<String, String> oldLocator;   // { "type": "id", "value": "loginBtn" }

    @JsonProperty("old_element")
    private ElementInfo oldElement;

    @JsonProperty("current_dom")
    private String currentDom;                 // HTML brut de la page

    @JsonProperty("run_id")
    private String runId;

    public HealingRequest() {
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

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }
}