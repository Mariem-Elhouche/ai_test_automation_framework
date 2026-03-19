// FeatureGenerator.java
package org.automation.ai;

import java.nio.file.Paths;

public class FeatureGenerator {

    public static void main(String[] args) throws Exception {

        ColabClient client = new ColabClient();

        // Chemin absolu vers test/resources/features/
        String featuresDir = Paths.get("src/test/resources/features")
                .toAbsolutePath()
                .toString();

        // Génère un .feature pour chaque user story
        // login_us.txt  →  login.feature
        client.generateFeatureFromFile("login_us.txt", featuresDir);

        System.out.println("🎉 Génération terminée !");
    }
}