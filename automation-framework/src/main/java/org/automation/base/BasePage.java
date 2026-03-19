package org.automation.base;

import org.automation.ai.healing.ElementInfo;
import org.automation.ai.healing.HealingRequest;
import org.automation.ai.healing.HealingResponse;
import org.automation.ai.healing.SelfHealingClient;
import org.automation.factory.DriverFactory;
import org.automation.utils.ConfigLoader;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public abstract class BasePage {

    protected WebDriver driver;
    protected WebDriverWait wait;
    private static final Logger log = LoggerFactory.getLogger(BasePage.class);

    // Activation du self-healing via configuration (désactivable)
    private static final boolean HEALING_ENABLED = Boolean.parseBoolean(
            ConfigLoader.getProperty("self.healing.enabled", "true")
    );

    // Cache des sélecteurs guéris : clé = nom logique, valeur = sélecteur guéri
    private static final Map<String, By> healedLocatorsCache = new HashMap<>();




    // Client self-healing
    private final SelfHealingClient healingClient = new SelfHealingClient();

    public BasePage() {
        this.driver = DriverFactory.getDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /**
     * Méthode principale de recherche d'élément avec self-healing.
     * @param originalLocator Sélecteur d'origine (By)
     * @param logicalName Nom logique de l'élément (utilisé pour le cache)
     * @return WebElement trouvé
     */
    protected WebElement findElement(By originalLocator, String logicalName) {
        try {
            // 1. Vérifier si un sélecteur guéri est en cache
            By locatorToUse = healedLocatorsCache.getOrDefault(logicalName, originalLocator);
            log.debug("Tentative avec le sélecteur : {} pour l'élément {}", locatorToUse, logicalName);
            return driver.findElement(locatorToUse);
        } catch (NoSuchElementException e) {
            // Si le healing est désactivé, on relance immédiatement l'exception
            if (!HEALING_ENABLED) {
                log.warn("Healing désactivé – échec de localisation pour '{}'", logicalName);
                throw e;
            }

            log.warn("Échec de localisation pour '{}' avec le sélecteur {}. Déclenchement du self-healing...", logicalName, originalLocator);
            // 2. Échec : appeler l'API de healing
            HealingResponse response = callHealingAPI(originalLocator, logicalName);
            if (response != null && response.isSuccess() && response.getNewLocator() != null) {
                // 3. Construire le nouveau By à partir de la réponse
                By healedBy = buildByFromResponse(response.getNewLocator());
                if (healedBy != null) {
                    // 4. Mettre en cache
                    healedLocatorsCache.put(logicalName, healedBy);
                    log.info("Self-healing réussi pour '{}'. Nouveau sélecteur : {}", logicalName, healedBy);
                    // 5. Réessayer
                    return driver.findElement(healedBy);
                }
            }
            // 6. Si toujours en échec, relancer l'exception
            log.error("Self-healing impossible pour '{}'", logicalName);
            throw e;
        }
    }


    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
    /**
     * Appelle l'API de self-healing.
     */
    private HealingResponse callHealingAPI(By failedLocator, String logicalName) {
        try {
            // Récupérer le HTML de la page actuelle
            String pageSource = driver.getPageSource();

            // Construire l'ElementInfo à partir du sélecteur échoué (autant que possible)
            ElementInfo oldElement = buildElementInfoFromLocator(failedLocator);

            // Construire la Map pour old_locator
            Map<String, String> oldLocatorMap = convertByToMap(failedLocator);

            // Créer la requête
            HealingRequest request = new HealingRequest();
            request.setOldLocator(oldLocatorMap);
            request.setOldElement(oldElement);
            request.setCurrentDom(pageSource);

            // Appeler le client
            return healingClient.healSelector(request);
        } catch (Exception e) {
            log.error("Erreur lors de l'appel à l'API de healing", e);
            return null;
        }
    }

    /**
     * Convertit un objet By en Map {type, value} compréhensible par l'API.
     */
    private Map<String, String> convertByToMap(By by) {
        Map<String, String> map = new HashMap<>();
        String byString = by.toString();
        // Exemple: "By.id: loginBtn" -> type="id", value="loginBtn"
        if (byString.startsWith("By.id:")) {
            map.put("type", "id");
            map.put("value", byString.substring("By.id:".length()).trim());
        } else if (byString.startsWith("By.name:")) {
            map.put("type", "name");
            map.put("value", byString.substring("By.name:".length()).trim());
        } else if (byString.startsWith("By.xpath:")) {
            map.put("type", "xpath");
            map.put("value", byString.substring("By.xpath:".length()).trim());
        } else if (byString.startsWith("By.cssSelector:")) {
            map.put("type", "css");
            map.put("value", byString.substring("By.cssSelector:".length()).trim());
        } else {
            // Fallback
            map.put("type", "unknown");
            map.put("value", byString);
        }
        return map;
    }

    /**
     * Construit un ElementInfo à partir du sélecteur échoué.
     * (Version simplifiée : on remplit seulement les champs que l'on peut deviner)
     */
    private ElementInfo buildElementInfoFromLocator(By locator) {
        ElementInfo info = new ElementInfo();
        Map<String, String> map = convertByToMap(locator);
        String type = map.get("type");
        String value = map.get("value");
        if ("id".equals(type)) {
            info.setElementId(value);
        } else if ("name".equals(type)) {
            Map<String, String> attrs = new HashMap<>();
            attrs.put("name", value);
            info.setAttributes(attrs);
        }
        // On peut aussi tenter de récupérer le type d'élément depuis le DOM ? Trop complexe ici.
        // On laisse les autres champs vides.
        return info;
    }

    /**
     * Convertit la réponse de l'API (Map new_locator) en objet By.
     */
    private By buildByFromResponse(Map<String, String> healedSelector) {
        String type = healedSelector.get("type");
        String value = healedSelector.get("value");
        if (type == null || value == null) return null;
        switch (type) {
            case "id": return By.id(value);
            case "name": return By.name(value);
            case "xpath": return By.xpath(value);
            case "css": return By.cssSelector(value);
            case "data-testid": return By.cssSelector("[data-testid='" + value + "']");
            default: return null;
        }
    }

    /**
     * Méthode utilitaire pour attendre qu'un élément soit visible.
     */
    protected WebElement waitForElementVisible(By locator, String logicalName) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(
                healedLocatorsCache.getOrDefault(logicalName, locator)));
    }

    /**
     * Méthode utilitaire pour obtenir le titre de la page.
     */
    public String getPageTitle() {
        return driver.getTitle();
    }

    /**
     * Navigation vers une URL.
     */
    protected void navigateTo(String url) {
        driver.get(url);
    }
}