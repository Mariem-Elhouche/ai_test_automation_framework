package org.automation.dashboard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.automation.utils.ConfigLoader;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DashboardReporter - reads cucumber.json and pushes results to the Dashboard API.
 */
public class DashboardReporter {

    private static final String API_URL = resolveApiUrl();
    private static final String API_KEY = resolveApiKey();
    private static final String RUN_ID = resolveRunId();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void push(String cucumberJsonPath) {
        try {
            File file = new File(cucumberJsonPath);
            if (!file.exists()) {
                System.err.println("[DashboardReporter] File not found: " + cucumberJsonPath);
                return;
            }

            String json = Files.readString(file.toPath());
            JsonNode features = MAPPER.readTree(json);
            List<Map<String, Object>> scenarios = new ArrayList<>();

            for (JsonNode feature : features) {
                String featureName = feature.path("name").asText("unknown");
                JsonNode elements = feature.path("elements");

                for (JsonNode element : elements) {
                    String type = element.path("type").asText();
                    if (!"scenario".equals(type) && !"scenario_outline".equals(type)) {
                        continue;
                    }

                    String scenarioName = element.path("name").asText();
                    String status = computeStatus(
                            element.path("before"),
                            element.path("steps"),
                            element.path("after")
                    );
                    long durationNs = computeDuration(
                            element.path("before"),
                            element.path("steps"),
                            element.path("after")
                    );

                    List<String> tags = new ArrayList<>();
                    for (JsonNode tag : element.path("tags")) {
                        tags.add(tag.path("name").asText());
                    }

                    scenarios.add(Map.of(
                            "feature_name", featureName,
                            "scenario", scenarioName,
                            "status", status,
                            "duration_ns", durationNs,
                            "tags", String.join(",", tags),
                            "run_id", RUN_ID
                    ));
                }
            }

            if (scenarios.isEmpty()) {
                System.out.println("[DashboardReporter] No scenarios found.");
                return;
            }

            String payload = MAPPER.writeValueAsString(Map.of(
                    "run_id", RUN_ID,
                    "scenarios", scenarios
            ));

            HttpURLConnection connection = (HttpURLConnection) URI.create(API_URL + "/api/cucumber-runs")
                    .toURL()
                    .openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
            connection.setReadTimeout((int) Duration.ofSeconds(10).toMillis());
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            if (!API_KEY.isBlank()) {
                connection.setRequestProperty("X-API-Key", API_KEY);
            }

            byte[] body = payload.getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(body.length);
            try (OutputStream os = connection.getOutputStream()) {
                os.write(body);
            }

            int statusCode = connection.getResponseCode();
            String responseBody = readResponseBody(connection, statusCode);

            System.out.printf("[DashboardReporter] Pushed %d scenarios -> HTTP %d%n",
                    scenarios.size(), statusCode);
            if (statusCode >= 400) {
                System.err.printf("[DashboardReporter] Response body: %s%n", responseBody);
            }
        } catch (Exception e) {
            System.err.println("[DashboardReporter] Failed to push results: " + e.getMessage());
        }
    }

    private static String readResponseBody(HttpURLConnection connection, int statusCode) {
        try (InputStream stream = statusCode >= 400 ? connection.getErrorStream() : connection.getInputStream()) {
            if (stream == null) {
                return "";
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    private static String computeStatus(JsonNode before, JsonNode steps, JsonNode after) {
        boolean sawSkipped = false;

        for (String status : extractStatuses(before)) {
            if ("failed".equals(status)) {
                return "failed";
            }
            if ("skipped".equals(status) || "pending".equals(status)) {
                sawSkipped = true;
            }
        }

        for (JsonNode step : steps) {
            String status = step.path("result").path("status").asText("skipped");
            if ("failed".equals(status)) {
                return "failed";
            }
            if ("skipped".equals(status) || "pending".equals(status)) {
                sawSkipped = true;
            }
        }

        for (String status : extractStatuses(after)) {
            if ("failed".equals(status)) {
                return "failed";
            }
            if ("skipped".equals(status) || "pending".equals(status)) {
                sawSkipped = true;
            }
        }

        return sawSkipped ? "skipped" : "passed";
    }

    private static long computeDuration(JsonNode before, JsonNode steps, JsonNode after) {
        long total = 0;
        total += sumDurations(before);
        for (JsonNode step : steps) {
            total += step.path("result").path("duration").asLong(0);
        }
        total += sumDurations(after);
        return total;
    }

    private static List<String> extractStatuses(JsonNode nodes) {
        List<String> statuses = new ArrayList<>();
        if (nodes == null || nodes.isMissingNode()) {
            return statuses;
        }
        for (JsonNode node : nodes) {
            statuses.add(node.path("result").path("status").asText("skipped"));
        }
        return statuses;
    }

    private static long sumDurations(JsonNode nodes) {
        long total = 0;
        if (nodes == null || nodes.isMissingNode()) {
            return total;
        }
        for (JsonNode node : nodes) {
            total += node.path("result").path("duration").asLong(0);
        }
        return total;
    }

    private static String resolveApiUrl() {
        String fromEnv = System.getenv("DASHBOARD_API_URL");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim();
        }
        return ConfigLoader.getProperty("dashboard.api.url", "http://localhost:8080").trim();
    }

    private static String resolveApiKey() {
        String fromEnv = System.getenv("DASHBOARD_API_KEY");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim();
        }
        return ConfigLoader.getProperty("dashboard.api.key", "").trim();
    }

    private static String resolveRunId() {
        String fromEnv = System.getenv("DASHBOARD_RUN_ID");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim();
        }
        String fromConfig = ConfigLoader.getProperty("dashboard.run.id", "").trim();
        if (!fromConfig.isBlank()) {
            return fromConfig;
        }
        return DateTimeFormatter.ISO_INSTANT.format(Instant.now()) + "_" + UUID.randomUUID();
    }
}
