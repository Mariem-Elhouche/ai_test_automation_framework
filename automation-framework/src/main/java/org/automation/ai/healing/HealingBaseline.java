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

public class HealingBaseline {

    private static final Logger log = LoggerFactory.getLogger(HealingBaseline.class);
    private static final String BASELINE_FILE = "target/healing-baseline.json";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Map<String, BaselineEntry> baseline = new ConcurrentHashMap<>();

    static {
        loadFromDisk();
        Runtime.getRuntime().addShutdownHook(new Thread(HealingBaseline::saveToDisk));
    }

    private HealingBaseline() {
    }

    public static void store(String cacheKey, String origType, String origValue,
                              String healedType, String healedValue) {
        baseline.put(cacheKey, new BaselineEntry(origType, origValue, healedType, healedValue));
    }

    public static BaselineEntry lookup(String cacheKey) {
        return baseline.get(cacheKey);
    }

    public static boolean hasEntry(String cacheKey) {
        return baseline.containsKey(cacheKey);
    }

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

        public BaselineEntry() {
        }

        public BaselineEntry(String origType, String origValue, String healedType, String healedValue) {
            this.origType = origType;
            this.origValue = origValue;
            this.healedType = healedType;
            this.healedValue = healedValue;
        }
    }
}
