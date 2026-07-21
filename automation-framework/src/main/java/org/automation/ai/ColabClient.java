// ColabClient.java
package org.automation.ai;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.time.Duration;
import org.automation.utils.ConfigLoader;
 
public class ColabClient {
 
    private static final String COLAB_BASE_URL = resolveColabUrl();
    private static final String GENERATE_ENDPOINT = COLAB_BASE_URL + "/generate";

    private static String resolveColabUrl() {
        String configured = ConfigLoader.getProperty("colab.url", "").trim();
        if (!configured.isEmpty()) {
            return configured;
        }
        return "https://epigastric-troy-calculating.ngrok-free.dev";
    }

    /**
     * Lit une user story depuis un fichier .txt et génère le .feature localement.
     * Les user stories sont lues depuis userStoriesDir, le .feature est écrit dans featuresDir.
     *
     * @param userStoryFile   Nom du fichier .txt (ex: "login_us.txt")
     * @param userStoriesDir  Chemin absolu du dossier contenant les user stories
     * @param featuresDir     Chemin absolu du dossier de sortie des .feature
     */
    public void generateFeatureFromFile(String userStoryFile, String userStoriesDir, String featuresDir) throws Exception {

        // 1️⃣ Lire la user story depuis le fichier local
        Path userStoryPath = Paths.get(userStoriesDir).resolve(userStoryFile);

        if (!Files.exists(userStoryPath)) {
            throw new FileNotFoundException("User story file not found: " + userStoryPath);
        }

        String userStory = Files.readString(userStoryPath);
        System.out.println("User story loaded from: " + userStoryPath);

        // 2️⃣ Appeler l'API Colab
        String featureContent = callColabAPI(userStory);

        // 3️⃣ Déterminer le nom du fichier .feature
        String featureFileName = userStoryFile.replace("_us.txt", ".feature")
                .replace(".txt", ".feature");

        // 4️⃣ Écrire le fichier .feature LOCALEMENT dans featuresDir/
        Path outputPath = Paths.get(featuresDir).resolve(featureFileName);
        Files.createDirectories(Paths.get(featuresDir));
        Files.writeString(outputPath, featureContent);

        System.out.println("Feature file generated: " + outputPath.toAbsolutePath());
    }

    /**
     * Envoie la user story à l'API Flask sur Colab et retourne le contenu Gherkin.
     */
    private String callColabAPI(String userStory) throws Exception {

        // Construire le JSON body
        String jsonBody = "{\"user_story\": " + escapeJson(userStory) + "}";

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GENERATE_ENDPOINT))
                .header("Content-Type", "application/json")
                // Header requis par ngrok pour éviter le browser warning
                .header("ngrok-skip-browser-warning", "true")
                .timeout(Duration.ofMinutes(3)) // Mistral peut être lent
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        System.out.println("🚀 Appel API Colab : " + GENERATE_ENDPOINT);

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("API Error [" + response.statusCode() + "]: " + response.body());
        }

        // Parser le JSON pour extraire "content"
        String responseBody = response.body();
        String content = extractJsonField(responseBody, "content");

        if (content == null || content.isBlank()) {
            throw new RuntimeException("Réponse vide de l'API Colab. Body: " + responseBody);
        }

        System.out.println("✅ Contenu Gherkin reçu depuis Colab");
        return content;
    }

    /**
     * Extraction simple du champ "content" depuis le JSON de réponse.
     * Évite une dépendance externe (Jackson/Gson) si pas déjà présente.
     */
    private String extractJsonField(String json, String field) {
        // Cherche "content": "..."  ou "content":"..."
        String key = "\"" + field + "\"";
        int keyIndex = json.indexOf(key);
        if (keyIndex == -1) return null;

        int colonIndex = json.indexOf(":", keyIndex + key.length());
        if (colonIndex == -1) return null;

        // Trouver le premier guillemet après les espaces
        int startQuote = json.indexOf("\"", colonIndex + 1);
        if (startQuote == -1) return null;

        // Trouver le guillemet fermant (en gérant les échappements)
        StringBuilder sb = new StringBuilder();
        int i = startQuote + 1;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) {
                char next = json.charAt(i + 1);
                switch (next) {
                    case '"':  sb.append('"');  break;
                    case '\\': sb.append('\\'); break;
                    case 'n':  sb.append('\n'); break;
                    case 'r':  sb.append('\r'); break;
                    case 't':  sb.append('\t'); break;
                    default:   sb.append(next); break;
                }
                i += 2;
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }

    /**
     * Échappe une chaîne pour l'inclure dans un JSON.
     */
    private String escapeJson(String value) {
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                + "\"";
    }
}