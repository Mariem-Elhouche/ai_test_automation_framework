package org.automation.ai.healing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.automation.utils.ConfigLoader;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class SelfHealingClient {

    private static final String API_URL = ConfigLoader.getProperty("self.healing.api.url", "https://epigastric-troy-calculating.ngrok-free.dev/heal");
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SelfHealingClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public HealingResponse healSelector(HealingRequest request) {
        try {
            String requestBody = objectMapper.writeValueAsString(request);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                HealingResponse healingResponse = objectMapper.readValue(response.body(), HealingResponse.class);

                // Affiche le message d'erreur si présent
                if (healingResponse.getError() != null) {
                    System.err.println("Self-Healing API Error: " + healingResponse.getError());
                }

                return healingResponse;
            } else {
                throw new RuntimeException("Erreur API self-healing: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Échec de l'appel self-healing", e);
        }
    }
}