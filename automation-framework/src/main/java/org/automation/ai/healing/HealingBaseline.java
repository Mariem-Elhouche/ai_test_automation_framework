package org.automation.ai.healing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache local persistant des reparations de locators deja reussies.
 * Les entrees sont stockees dans un fichier JSON (target/healing-baseline.json)
 * et chargees au demarrage de la JVM via un bloc static initializer.
 * Une sauvegarde automatique est declenchee a l'arret via shutdown hook.
 */
public class HealingBaseline {

    private static final Logger log = LoggerFactory.getLogger(HealingBaseline.class);
    private static final String BASELINE_FILE = System.getProperty(
            "healing.baseline.file",
            System.getProperty("user.dir") + "/target/healing-baseline.json"
    );
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Map<String, BaselineEntry> baseline = new ConcurrentHashMap<>();

    static {
        loadFromDisk();
        Runtime.getRuntime().addShutdownHook(new Thread(HealingBaseline::saveToDisk));
    }

    private HealingBaseline() {
    }

    /**
     * Stocke une entree de reparation reussie dans le cache.
     * @param cacheKey Cle unique (ex: driver::host::By.xpath://id)
     * @param origType Type du locator original (xpath, css, id...)
     * @param origValue Valeur du locator original
     * @param healedType Type du nouveau locator propose
     * @param healedValue Valeur du nouveau locator propose
     * @param exceptionType Type d'exception qui a declenche le healing
     * @param structuralScore Score de similarite structurelle (0-1)
     * @param semanticScore Score de similarite semantique (0-1)
     */
    public static void store(String cacheKey, String origType, String origValue,
                              String healedType, String healedValue,
                              String exceptionType, Double structuralScore, Double semanticScore) {
        baseline.put(cacheKey, new BaselineEntry(origType, origValue, healedType, healedValue,
                exceptionType, structuralScore, semanticScore));
    }

    /**
     * Recupere une entree de reparation depuis le cache.
     * @return BaselineEntry ou null si absente
     */
    public static BaselineEntry lookup(String cacheKey) {
        return baseline.get(cacheKey);
    }

    /**
     * Charge le fichier baseline depuis le disque au demarrage de la JVM.
     * Si le fichier n'existe pas (premiere execution), le cache demarre vide.
     */
    private static void loadFromDisk() {
        File file = new File(BASELINE_FILE);
        if (!file.exists()) {
            return;
        }
        try {
            Map<String, BaselineEntry> loaded = MAPPER.readValue(file,
                    new TypeReference<Map<String, BaselineEntry>>() {
                    });
            baseline.putAll(loaded);
            log.info("Healing baseline loaded from {} with {} entries", BASELINE_FILE, loaded.size());
        } catch (IOException e) {
            log.warn("Could not load healing baseline: {}", e.getMessage());
        }
    }

    /**
     * Persiste le cache baseline sur le disque (format JSON pretty-print).
     * Declenche automatiquement a l'arret de la JVM via Runtime.addShutdownHook().
     * Les entrees sont conservees entre les sessions de test.
     */
    private static void saveToDisk() {
        if (baseline.isEmpty()) {
            return;
        }
        try {
            Path path = Path.of(BASELINE_FILE);
            Files.createDirectories(path.getParent());
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), baseline);
            log.info("Healing baseline saved to {} with {} entries", BASELINE_FILE, baseline.size());
        } catch (IOException e) {
            log.warn("Could not save healing baseline: {}", e.getMessage());
        }
    }

    public static class BaselineEntry {
        public String origType;
        public String origValue;
        public String healedType;
        public String healedValue;
        public String exceptionType;
        public Double structuralScore;
        public Double semanticScore;

        public BaselineEntry() {
        }

        public BaselineEntry(String origType, String origValue, String healedType, String healedValue) {
            this(origType, origValue, healedType, healedValue, null, null, null);
        }

        public BaselineEntry(String origType, String origValue, String healedType, String healedValue,
                              String exceptionType, Double structuralScore, Double semanticScore) {
            this.origType = origType;
            this.origValue = origValue;
            this.healedType = healedType;
            this.healedValue = healedValue;
            this.exceptionType = exceptionType;
            this.structuralScore = structuralScore;
            this.semanticScore = semanticScore;
        }
    }
}
