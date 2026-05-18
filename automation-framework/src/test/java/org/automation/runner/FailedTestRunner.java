package org.automation.runner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FailedTestRunner {

    public static void main(String[] args) {
        try {
            String targetDir = args.length > 0 ? args[0] : "target";
            Path targetAbs = Paths.get(targetDir).toAbsolutePath().normalize();
            Path rerunFile = Paths.get(targetDir, "failed_scenarios.txt");
            if (!Files.exists(rerunFile)) {
                System.out.println("[FailedTestRunner] No rerun file found at " + rerunFile.toAbsolutePath());
                return;
            }

            Path testClasses = Paths.get(targetDir, "test-classes").normalize();
            Path featureRoot = testClasses.resolve("features").normalize();

            List<String> features = Files.readAllLines(rerunFile).stream()
                    .map(line -> {
                        String bare = line.startsWith("classpath:") ? line.substring("classpath:".length()) : line;
                        String stripped = bare.replaceFirst("^features/", "");
                        int dotIdx = stripped.indexOf(".feature");
                        if (dotIdx < 0) {
                            return featureRoot.resolve(stripped).normalize().toAbsolutePath().toString();
                        }
                        dotIdx += ".feature".length();
                        String featurePath = stripped.substring(0, dotIdx);
                        String lineNos = stripped.substring(dotIdx);
                        return featureRoot.resolve(featurePath).normalize().toAbsolutePath().toString() + lineNos;
                    })
                    .collect(Collectors.toList());

            System.out.println("[FailedTestRunner] Rerunning " + features.size() + " feature(s):");
            features.forEach(f -> System.out.println("  " + f));

            List<String> cmdArgs = new ArrayList<>();
            cmdArgs.add("--glue");
            cmdArgs.add("org.automation.steps");
            cmdArgs.add("--glue");
            cmdArgs.add("org.automation.hooks");
            cmdArgs.add("--plugin");
            cmdArgs.add("pretty");
            cmdArgs.add("--plugin");
            cmdArgs.add("html:" + targetAbs.resolve("reports/attachments/group-attachments-rerun-report.html"));
            cmdArgs.add("--plugin");
            cmdArgs.add("json:" + targetAbs.resolve("cucumber-rerun.json"));
            features.forEach(f -> cmdArgs.add(f));

            byte exitStatus = io.cucumber.core.cli.Main.run(
                    cmdArgs.toArray(new String[0]),
                    Thread.currentThread().getContextClassLoader()
            );

            System.out.println("[FailedTestRunner] Cucumber exit status: " + exitStatus);
        } catch (Exception e) {
            System.err.println("[FailedTestRunner] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
