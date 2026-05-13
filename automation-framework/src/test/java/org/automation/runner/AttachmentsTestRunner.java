package org.automation.runner;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

@Suite
@IncludeEngines("cucumber")
@ConfigurationParameter(key = FEATURES_PROPERTY_NAME, value = "classpath:features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "org.automation.steps,org.automation.hooks")
@ConfigurationParameter(
        key = PLUGIN_PROPERTY_NAME,
        value = "pretty, html:target/reports/attachments/group-attachments-report.html, json:target/cucumber-attachments.json, rerun:target/failed_scenarios_attachments.txt"
)
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@group_attachments")
public class AttachmentsTestRunner {
}

