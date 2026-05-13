package org.automation.ai.healing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HealingResponse {

    private boolean success;

    @JsonProperty("new_locator")
    private Map<String, String> newLocator; // { "type": "id", "value": "signinBtn" }

    private double score;

    private Map<String, Object> details; // Informations supplémentaires (scores détaillés, etc.)

    private String error; // Champ pour récupérer le message d'erreur du self-healing

    // Constructeur par défaut
    public HealingResponse() {
    }

    // Constructeur complet
    public HealingResponse(boolean success, Map<String, String> newLocator, double score, Map<String, Object> details, String error) {
        this.success = success;
        this.newLocator = newLocator;
        this.score = score;
        this.details = details;
        this.error = error;
    }

    // Getters et setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Map<String, String> getNewLocator() {
        return newLocator;
    }

    public void setNewLocator(Map<String, String> newLocator) {
        this.newLocator = newLocator;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
