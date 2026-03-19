package org.automation.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
            } else {
                System.err.println("Fichier config.properties non trouvé dans le classpath. Utilisation des valeurs par défaut.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Récupère une propriété avec priorité : système > fichier > valeur par défaut.
     * @param key clé de la propriété
     * @param defaultValue valeur par défaut
     * @return valeur de la propriété
     */
    public static String getProperty(String key, String defaultValue) {
        String systemValue = System.getProperty(key);
        if (systemValue != null) {
            return systemValue;
        }
        return properties.getProperty(key, defaultValue);
    }
}