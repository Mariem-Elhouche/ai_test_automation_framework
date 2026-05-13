package org.automation.ai.healing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.automation.utils.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class SelfHealingClient {

    private static final Logger log = LoggerFactory.getLogger(SelfHealingClient.class);
    private static final int DEFAULT_TIMEOUT_SECONDS = 10;
    private static final int MAX_RETRY_ATTEMPTS = 2;

    private static final String API_URL = resolveApiUrl();
    private static final int API_TIMEOUT_SECONDS = resolveTimeoutSeconds();

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SelfHealingClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(Math.max(5, API_TIMEOUT_SECONDS)))
                .version(HttpClient.Version.HTTP_1_1) // plus stable avec ngrok/Colab que HTTP/2
                .build();
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public HealingResponse healSelector(HealingRequest request) {
        try {
            String requestBody = objectMapper.writeValueAsString(request);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("ngrok-skip-browser-warning", "true")
                    .timeout(Duration.ofSeconds(API_TIMEOUT_SECONDS))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = sendWithRetry(httpRequest);
            if (response.statusCode() != 200) {
                return failureResponse("Self-healing API error [" + response.statusCode() + "]: " + response.body());
            }

            HealingResponse healingResponse = objectMapper.readValue(response.body(), HealingResponse.class);
            if (healingResponse == null) {
                return failureResponse("Self-healing API returned an empty response.");
            }

            if (healingResponse.getError() != null && !healingResponse.getError().isBlank()) {
                log.warn("Self-healing service reported an error: {}", healingResponse.getError());
            }
            return healingResponse;
        } catch (Exception e) {
            log.error("Self-healing call failed", e);
            return failureResponse("Self-healing call failed: " + e.getMessage());
        }
    }

    private HttpResponse<String> sendWithRetry(HttpRequest httpRequest) throws IOException, InterruptedException {
        IOException lastIo = null;

        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            } catch (IOException ioe) {
                lastIo = ioe;
                boolean retryable = isRetryableTransportError(ioe);
                if (!retryable || attempt >= MAX_RETRY_ATTEMPTS) {
                    throw ioe;
                }

                log.warn("Self-healing transport error (attempt {}/{}): {}. Retrying...",
                        attempt, MAX_RETRY_ATTEMPTS, ioe.getMessage());
                Thread.sleep(300L * attempt);
            }
        }

        throw lastIo != null ? lastIo : new IOException("Unknown transport error while calling self-healing API");
    }

    private boolean isRetryableTransportError(IOException ioe) {
        String msg = String.valueOf(ioe.getMessage()).toLowerCase();
        return msg.contains("goaway")
                || msg.contains("rst_stream")
                || msg.contains("connection reset")
                || msg.contains("broken pipe")
                || msg.contains("remote host terminated");
    }

    private static HealingResponse failureResponse(String errorMessage) {
        HealingResponse response = new HealingResponse();
        response.setSuccess(false);
        response.setError(errorMessage);
        return response;
    }

    private static String resolveApiUrl() {
        String configured = ConfigLoader.getProperty("self.healing.api.url", "").trim();
        if (!configured.isEmpty()) {
            return normalizeHealEndpoint(configured);
        }

        String colabBase = ConfigLoader.getProperty("colab.url", "https://epigastric-troy-calculating.ngrok-free.dev");
        return normalizeHealEndpoint(colabBase);
    }

    private static String normalizeHealEndpoint(String rawUrl) {
        String trimmed = rawUrl.trim();
        if (trimmed.endsWith("/heal")) {
            return trimmed;
        }
        if (trimmed.endsWith("/")) {
            return trimmed + "heal";
        }
        return trimmed + "/heal";
    }

    private static int resolveTimeoutSeconds() {
        String raw = ConfigLoader.getProperty("self.healing.api.timeout", String.valueOf(DEFAULT_TIMEOUT_SECONDS));
        try {
            int timeout = Integer.parseInt(raw.trim());
            return timeout > 0 ? timeout : DEFAULT_TIMEOUT_SECONDS;
        } catch (NumberFormatException e) {
            return DEFAULT_TIMEOUT_SECONDS;
        }
    }
}
