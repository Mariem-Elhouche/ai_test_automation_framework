package org.automation.runner;

import io.cucumber.core.cli.Main;

public class DashboardCucumberRunner {

    public static void main(String[] args) {
        String tags = System.getProperty("cucumber.filter.tags", "").trim();
        String reportDir = args.length > 0 ? args[0] : "target/reports/dashboard-run";
        String jsonFile = args.length > 1 ? args[1] : "target/cucumber.json";
        String rerunFile = args.length > 2 ? args[2] : "target/failed_scenarios.txt";

        java.util.List<String> cmdArgs = new java.util.ArrayList<>();
        cmdArgs.add("--glue");
        cmdArgs.add("org.automation.steps");
        cmdArgs.add("--glue");
        cmdArgs.add("org.automation.hooks");
        cmdArgs.add("--plugin");
        cmdArgs.add("pretty");
        cmdArgs.add("--plugin");
        cmdArgs.add("html:" + reportDir + "/report.html");
        cmdArgs.add("--plugin");
        cmdArgs.add("json:" + jsonFile);
        cmdArgs.add("--plugin");
        cmdArgs.add("rerun:" + rerunFile);

        if (!tags.isEmpty() && !tags.equals("@all")) {
            cmdArgs.add("--tags");
            cmdArgs.add(tags);
        }

        String features = System.getProperty("cucumber.features", "classpath:features");
        if (features.startsWith("@") && !features.contains(" ")) {
            cmdArgs.add(features);
        } else {
            cmdArgs.add("classpath:features");
        }

        System.out.println("[DashboardCucumberRunner] Tags filter: '" + tags + "'");
        System.out.println("[DashboardCucumberRunner] Command: " + String.join(" ", cmdArgs));

        byte exitStatus = Main.run(cmdArgs.toArray(new String[0]), Thread.currentThread().getContextClassLoader());
        System.out.println("[DashboardCucumberRunner] Exit status: " + exitStatus);
        System.exit(exitStatus);
    }
}
