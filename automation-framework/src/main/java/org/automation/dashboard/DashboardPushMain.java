package org.automation.dashboard;

public class DashboardPushMain {

    public static void main(String[] args) {
        String cucumberJsonPath = args.length > 0 && args[0] != null && !args[0].isBlank()
                ? args[0]
                : "target/cucumber.json";
        DashboardReporter.push(cucumberJsonPath);
    }
}
