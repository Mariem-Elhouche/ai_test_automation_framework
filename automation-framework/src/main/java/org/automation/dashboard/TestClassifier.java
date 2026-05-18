package org.automation.dashboard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;

public class TestClassifier {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String API_URL = DashboardReporter.resolveApiUrl();
    private static final String API_KEY = DashboardReporter.resolveApiKey();
    private static final String RUN_ID = DashboardReporter.getRunId();

    private static final Set<String> CLASSIFICATION_STATUSES = Set.of("passed", "flaky", "failed");

    public static void main(String[] args) {
        String firstRunPath = args.length > 0 ? args[0] : "target/cucumber.json";
        String rerunPath = args.length > 1 ? args[1] : null;

        try {
            List<ScenarioResult> firstRun = readJson(firstRunPath);
            List<ScenarioResult> rerun = (rerunPath != null && new File(rerunPath).exists())
                    ? readJson(rerunPath)
                    : List.of();

            List<Map<String, Object>> classifiedScenarios = classify(firstRun, rerun);
            pushToDashboard(classifiedScenarios);

            printSummary(classifiedScenarios);
        } catch (Exception e) {
            System.err.println("[TestClassifier] Error: " + e.getMessage());
            System.exit(1);
        }
    }

    static List<Map<String, Object>> classify(List<ScenarioResult> firstRun, List<ScenarioResult> rerun) {
        Set<String> rerunKeys = new HashSet<>();
        for (ScenarioResult r : rerun) {
            rerunKeys.add(key(r.featureName, r.scenario));
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (ScenarioResult sc : firstRun) {
            String k = key(sc.featureName, sc.scenario);
            String classification;

            if ("passed".equals(sc.status)) {
                classification = "passed";
            } else if (rerunKeys.contains(k)) {
                Optional<ScenarioResult> rerunMatch = rerun.stream()
                        .filter(r -> key(r.featureName, r.scenario).equals(k))
                        .findFirst();
                if (rerunMatch.isPresent() && "passed".equals(rerunMatch.get().status)) {
                    classification = "flaky";
                } else {
                    classification = "failed";
                }
            } else {
                classification = "failed";
            }

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("feature_name", sc.featureName);
            entry.put("scenario", sc.scenario);
            entry.put("status", classification);
            entry.put("classification", classification);
            entry.put("duration_ns", sc.durationNs);
            entry.put("tags", sc.tags);
            entry.put("run_id", RUN_ID);
            results.add(entry);
        }

        return results;
    }

    static List<ScenarioResult> readJson(String path) throws Exception {
        File file = new File(path);
        if (!file.exists()) {
            System.err.println("[TestClassifier] File not found: " + path);
            return List.of();
        }

        String json = Files.readString(file.toPath());
        JsonNode features = MAPPER.readTree(json);
        List<ScenarioResult> results = new ArrayList<>();

        for (JsonNode feature : features) {
            String featureName = feature.path("name").asText("unknown");
            JsonNode elements = feature.path("elements");

            for (JsonNode element : elements) {
                String type = element.path("type").asText();
                if (!"scenario".equals(type) && !"scenario_outline".equals(type)) {
                    continue;
                }

                String scenarioName = element.path("name").asText();
                String status = DashboardReporter.computeStatus(
                        element.path("before"),
                        element.path("steps"),
                        element.path("after")
                );
                long durationNs = DashboardReporter.computeDuration(
                        element.path("before"),
                        element.path("steps"),
                        element.path("after")
                );

                List<String> tags = new ArrayList<>();
                for (JsonNode tag : element.path("tags")) {
                    tags.add(tag.path("name").asText());
                }

                results.add(new ScenarioResult(featureName, scenarioName, status, durationNs, String.join(",", tags)));
            }
        }

        return results;
    }

    static void pushToDashboard(List<Map<String, Object>> classifiedScenarios) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("run_id", RUN_ID);
            payload.put("scenarios", classifiedScenarios);

            String body = MAPPER.writeValueAsString(payload);

            HttpURLConnection conn = (HttpURLConnection) URI.create(API_URL + "/api/cucumber-runs/classify")
                    .toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
            conn.setReadTimeout((int) Duration.ofSeconds(10).toMillis());
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            if (API_KEY != null && !API_KEY.isBlank()) {
                conn.setRequestProperty("X-API-Key", API_KEY);
            }

            byte[] b = body.getBytes(StandardCharsets.UTF_8);
            conn.setFixedLengthStreamingMode(b.length);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(b);
            }

            int code = conn.getResponseCode();
            System.out.printf("[TestClassifier] Pushed %d classified scenarios → HTTP %d%n", classifiedScenarios.size(), code);
        } catch (Exception e) {
            System.err.println("[TestClassifier] Push failed: " + e.getMessage());
        }
    }

    private static void printSummary(List<Map<String, Object>> classified) {
        int passed = 0, flaky = 0, failed = 0;
        for (Map<String, Object> entry : classified) {
            switch ((String) entry.get("classification")) {
                case "passed" -> passed++;
                case "flaky" -> flaky++;
                case "failed" -> failed++;
            }
        }
        System.out.printf("[TestClassifier] Summary: %d passed, %d flaky, %d failed (total %d)%n",
                passed, flaky, failed, classified.size());
    }

    private static String key(String feature, String scenario) {
        return feature + "::" + scenario;
    }

    record ScenarioResult(String featureName, String scenario, String status, long durationNs, String tags) {
    }
}
