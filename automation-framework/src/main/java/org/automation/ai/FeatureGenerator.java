// FeatureGenerator.java
package org.automation.ai;

import java.nio.file.Paths;

public class FeatureGenerator {

    public static void main(String[] args) throws Exception {

        ColabClient client = new ColabClient();

        String userStoriesDir = Paths.get("src/test/resources/userstories")
                .toAbsolutePath()
                .toString();

        String featuresDir = Paths.get("src/test/resources/testgeneration/features")
                .toAbsolutePath()
                .toString();

        client.generateFeatureFromFile("company_creation_us.txt", userStoriesDir, featuresDir);

        System.out.println("Generation complete!");
    }
}
