package org.automation.runner;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

@Suite
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "org.automation")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, json:target/reports/companies-edit.json")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@editCompany")
public class EditTestRunner {
}