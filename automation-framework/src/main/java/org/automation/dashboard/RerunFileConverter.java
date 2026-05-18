package org.automation.dashboard;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class RerunFileConverter {

    public static void main(String[] args) {
        try {
            String targetDir = args.length > 0 ? args[0] : "target";
            Path rerunFile = Paths.get(targetDir, "failed_scenarios.txt");
            Path rerunOut = Paths.get(targetDir, "failed_scenarios_rerun.txt");
            if (!Files.exists(rerunFile)) {
                System.out.println("[RerunFileConverter] No rerun file found at " + rerunFile.toAbsolutePath());
                return;
            }

            Path featureRoot = Paths.get(targetDir, "test-classes", "features").normalize();

            List<String> lines = Files.readAllLines(rerunFile);
            List<String> converted = lines.stream()
                    .map(line -> {
                        String bare = line.startsWith("classpath:") ? line.substring("classpath:".length()) : line;
                        String stripped = bare.replaceFirst("^features/", "");
                        int featureEnd = stripped.indexOf(".feature");
                        if (featureEnd < 0) {
                            Path absPath = featureRoot.resolve(stripped).normalize();
                            return absPath.toAbsolutePath().toString();
                        }
                        featureEnd += ".feature".length();
                        String featurePath = stripped.substring(0, featureEnd);
                        String lineNos = stripped.substring(featureEnd);
                        Path absPath = featureRoot.resolve(featurePath).normalize();
                        return absPath.toAbsolutePath().toString() + lineNos;
                    })
                    .collect(Collectors.toList());

            Files.write(rerunOut, converted);
            System.out.printf("[RerunFileConverter] Converted %d line(s) -> %s%n",
                    converted.size(), rerunOut.toAbsolutePath());
            converted.forEach(l -> System.out.println("  " + l));
        } catch (Exception e) {
            System.err.println("[RerunFileConverter] Error: " + e.getMessage());
        }
    }
}
