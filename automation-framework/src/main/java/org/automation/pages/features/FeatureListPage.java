package org.automation.pages.features;

import org.automation.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.logging.Logger;

public class FeatureListPage extends BasePage {

    private static final Logger LOGGER = Logger.getLogger(FeatureListPage.class.getName());

    // ══════════════════════════════════════════════
    //  CARTES ET CHECKBOX
    // ══════════════════════════════════════════════

    private final By featureItems = By.xpath(
            "//div[contains(@class,'q-tab-panel')]" +
                    "//div[contains(@class,'card-container')]" +
                    "[.//div[starts-with(normalize-space(.),'Nom') or starts-with(normalize-space(.),'Name')]]" +
                    "[.//*[contains(@class,'q-checkbox')]]"
    );

    private By featureCheckboxLocator(String featureName) {
        return By.xpath(
                "//div[contains(@class,'q-tab-panel')]" +
                        "//div[contains(@class,'card-container')]" +
                        "[.//div[contains(normalize-space(.),'Nom : " + featureName + "')]]" +
                        "//div[contains(@class,'q-checkbox')]"
        );
    }

    private By featureCheckedIndicator(String featureName) {
        return By.xpath(
                "//div[contains(@class,'q-tab-panel')]" +
                        "//div[contains(@class,'card-container')]" +
                        "[.//div[contains(normalize-space(.),'Nom : " + featureName + "')]]" +
                        "//div[contains(@class,'q-checkbox__inner--truthy')]"
        );
    }

    // ══════════════════════════════════════════════
    //  DROPDOWN INFORMATION OBLIGATOIRE
    // ══════════════════════════════════════════════

    private final By infoObligatoireSelect = By.xpath(
            "//div[contains(@class,'card-container')]" +
                    "[.//span[contains(normalize-space(.),'Information obligatoire')]]" +
                    "//div[contains(@class,'q-select')]"
    );

    private final By featuresSaveBtn = By.xpath(
            "//div[contains(@class,'card-container')]" +
                    "[.//span[contains(normalize-space(.),'Information obligatoire')]]" +
                    "//div[normalize-space(text())='Enregistrer']"
    );

    // ══════════════════════════════════════════════
    //  AJOUT FEATURE (FORMULAIRE INLINE)
    // ══════════════════════════════════════════════

    private final By addFeatureBtn = By.xpath(
            "//span[contains(text(),'Ajouter une nouvelle feature')]"
    );
    private final By featuresSectionOpenBtn = By.xpath(
            "//div[contains(@class,'q-tab-panel')]//div[contains(@class,'row')][.//span[normalize-space(text())='Features']]" +
                    "//div[contains(@class,'col-2') and contains(@class,'cursor-pointer') and contains(normalize-space(.),'Afficher')]"
    );
    private final By featuresSectionCloseBtn = By.xpath(
            "//div[contains(@class,'q-tab-panel')]//div[contains(@class,'row')][.//span[normalize-space(text())='Features']]" +
                    "//div[contains(@class,'col-2') and contains(@class,'cursor-pointer') and contains(normalize-space(.),'Fermer')]"
    );

    private final By featureInlineForm = By.xpath(
            "(//div[contains(@class,'card-container') and contains(@class,'bordered-gray-300')])[4]"
    );

    private final By featureInlineDropdown = By.xpath(
            "(//div[contains(@class,'q-field__native') and contains(@class,'row') and contains(@class,'items-center')])[1]"
    );
    private final By featureDropdownOptions = By.xpath(
            "//div[@role='listbox']//div[@role='option']"
    );

    // Dialog confirmation ajout feature
    private final By featureAddConfirmDialog = By.xpath(
            "//div[@role='dialog' and @aria-modal='true']" +
                    "[.//div[contains(normalize-space(.),'Ajouter une nouvelle feature') " +
                    "or contains(normalize-space(.),'Voulez-vous ajouter cette feature')]]"
    );

    private final By featureAddConfirmBtn = By.xpath(
            "//div[@role='dialog' and @aria-modal='true']" +
                    "[.//div[contains(normalize-space(.),'Ajouter une nouvelle feature') " +
                    "or contains(normalize-space(.),'Voulez-vous ajouter cette feature')]]" +
                    "//span[normalize-space(text())='Valider']" +
                    "/ancestor::button"
    );

    private final By featureAddCancelBtn = By.xpath(
            "//div[@role='dialog' and @aria-modal='true']" +
                    "[.//div[contains(normalize-space(.),'Ajouter une nouvelle feature') " +
                    "or contains(normalize-space(.),'Voulez-vous ajouter cette feature')]]" +
                    "//span[normalize-space(text())='Annuler']" +
                    "/ancestor::button"
    );

    // ══════════════════════════════════════════════
    //  SUPPRESSION
    // ══════════════════════════════════════════════

    private final By deleteIcon = By.xpath(
            "//div[contains(@class,'q-tab-panel')]//i[text()='delete'][1]"
    );

    private final By featureDeleteConfirmDialog = By.xpath(
            "//div[@role='dialog' and @aria-modal='true']" +
                    "[.//span[normalize-space(text())='Valider']]" +
                    "[not(.//div[contains(normalize-space(.),'Voulez-vous ajouter')])]"
    );

    private final By featureDeleteConfirmBtn = By.xpath(
            "//div[@role='dialog' and @aria-modal='true']" +
                    "[not(.//div[contains(normalize-space(.),'Voulez-vous ajouter')])]" +
                    "//span[normalize-space(text())='Valider']/ancestor::button"
    );

    private final By featureDeleteCancelBtn = By.xpath(
            "//div[@role='dialog' and @aria-modal='true']" +
                    "[not(.//div[contains(normalize-space(.),'Voulez-vous ajouter')])]" +
                    "//span[normalize-space(text())='Annuler']/ancestor::button"
    );

    // ══════════════════════════════════════════════
    //  TOASTS
    // ══════════════════════════════════════════════

    private final By featureToggleSuccessToast = By.xpath(
            "//div[contains(@class,'q-notification')]" +
                    "[.//div[contains(normalize-space(.),'Feature modifie avec succes') " +
                    "or contains(normalize-space(.),'Feature modifiée avec succès')]]"
    );

    private final By featureAddSuccessToast = By.xpath(
            "//div[contains(@class,'q-notification')]" +
                    "[.//div[contains(normalize-space(.),'Feature est cr') " +
                    "or contains(normalize-space(.),'Feature créé')]]"
    );

    private final By featureDeleteSuccessToast = By.xpath(
            "//div[contains(@class,'q-notification')]" +
                    "[.//div[contains(normalize-space(.),'Feature supprim')]]"
    );

    private final By successToast = By.xpath(
            "//div[contains(@class,'q-notification__message')]"
    );

    // ══════════════════════════════════════════════
    //  CONSTRUCTEUR
    // ══════════════════════════════════════════════

    public FeatureListPage() { super(); }

    // ══════════════════════════════════════════════
    //  COMPTAGE
    // ══════════════════════════════════════════════

    public int getFeaturesCount() {
        try {
            return resolveSettledFeaturesCount();
        } catch (Exception e) { return 0; }
    }

    // ══════════════════════════════════════════════
    //  CHECK/UNCHECK FEATURE
    // ══════════════════════════════════════════════

    public boolean isFeatureChecked(String featureName) {
        try {
            return !driver.findElements(featureCheckedIndicator(featureName)).isEmpty();
        } catch (Exception e) { return false; }
    }

    public void checkFeature(String featureName) {
        if (!isFeatureChecked(featureName)) {
            scrollAndClick(featureCheckboxLocator(featureName), "Cocher " + featureName);
            waitForToggleToast();
        }
    }

    public void uncheckFeature(String featureName) {
        if (isFeatureChecked(featureName)) {
            scrollAndClick(featureCheckboxLocator(featureName), "Décocher " + featureName);
            waitForToggleToast();
        }
    }

    private void waitForToggleToast() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(featureToggleSuccessToast));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(featureToggleSuccessToast));
        } catch (Exception e) {
            LOGGER.warning("Toast modification feature non trouvé");
        }
    }

    // ══════════════════════════════════════════════
    //  INFORMATION OBLIGATOIRE
    // ══════════════════════════════════════════════

    public void selectInformationObligatoire(String value) {
        scrollAndClick(infoObligatoireSelect, "Dropdown info obligatoire");
        By option = By.xpath(
                "//div[@role='listbox']//div[@role='option'][contains(normalize-space(.),'" + value + "')]"
        );
        wait.until(ExpectedConditions.elementToBeClickable(option)).click();
    }

    public void saveFeaturesConfig() {
        scrollAndClick(featuresSaveBtn, "Enregistrer features config");
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(successToast));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(successToast));
        } catch (Exception e) {
            LOGGER.warning("Toast enregistrement features non trouvé");
        }
    }

    public boolean isFeaturesConfigSaved() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(successToast));
            return true;
        } catch (Exception e) { return false; }
    }

    // ══════════════════════════════════════════════
    //  AJOUT FEATURE
    // ══════════════════════════════════════════════

    public void clickAddFeature() {
        ensureFeatureAddButtonReady();
        scrollAndClick(addFeatureBtn, "Ajouter nouvelle feature");
        //wait.until(ExpectedConditions.visibilityOfElementLocated(featureInlineForm));
        LOGGER.info("Formulaire inline feature ouvert");
    }

    public void selectFeatureFromDropdown(String featureName) {
        WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(featureInlineDropdown));
        new Actions(driver).moveToElement(dropdown).perform();
        try {
            dropdown.click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", dropdown);
        }
        sleep(500);

        By option = By.xpath(
                "//div[@role='listbox']//div[@role='option'][contains(normalize-space(.),'" + featureName + "')]"
        );
        wait.until(ExpectedConditions.elementToBeClickable(option)).click();
        LOGGER.info("Feature sélectionnée: " + featureName);
    }

    public String selectFirstAvailableFeatureFromDropdown() {
        WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(featureInlineDropdown));
        new Actions(driver).moveToElement(dropdown).perform();
        try {
            dropdown.click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", dropdown);
        }
        sleep(500);

        List<WebElement> options = wait.until(d -> {
            List<WebElement> o = d.findElements(featureDropdownOptions);
            return o.isEmpty() ? null : o;
        });

        WebElement firstOption = options.get(0);
        String selectedName = firstOption.getText().trim();
        new Actions(driver).moveToElement(firstOption).perform();
        try {
            firstOption.click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstOption);
        }

        LOGGER.info("Première feature disponible sélectionnée: " + selectedName);
        return selectedName;
    }

    public void confirmFeatureCreation() {
        // Cliquer sur Valider dans le formulaire inline
        scrollAndClick(featureAddConfirmBtn, "Valider ajout feature");
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(featureAddSuccessToast));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(featureAddSuccessToast));
            LOGGER.info("Toast ajout feature traité");
        } catch (Exception e) {
            LOGGER.warning("Toast ajout feature non trouvé : " + e.getMessage());
        }
    }

    public void cancelFeatureCreation() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(featureAddConfirmDialog));
        scrollAndClick(featureAddCancelBtn, "Annuler ajout feature");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(featureAddConfirmDialog));
        // On some contexts (company/group), add button is available again only after section close/open.
        ensureFeatureAddButtonReady();
        LOGGER.info("Ajout feature annulé");
    }

    public void saveFeature() {
        saveFeaturesConfig();
    }

    // ══════════════════════════════════════════════
    //  SUPPRESSION FEATURE
    // ══════════════════════════════════════════════

    public void deleteFirstFeature() {
        scrollAndClick(deleteIcon, "Supprimer première feature");
    }

    public boolean isDeleteConfirmDialogDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(featureDeleteConfirmDialog));
            return true;
        } catch (Exception e) { return false; }
    }

    public void confirmDeletion() {
        wait.until(ExpectedConditions.elementToBeClickable(featureDeleteConfirmBtn)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(featureDeleteConfirmDialog));
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(featureDeleteSuccessToast));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(featureDeleteSuccessToast));
        } catch (Exception e) {
            LOGGER.warning("Toast suppression feature non trouvé");
        }
        sleep(500);
    }

    public void cancelDeletion() {
        wait.until(ExpectedConditions.elementToBeClickable(featureDeleteCancelBtn)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(featureDeleteConfirmDialog));
        sleep(300);
    }

    // ══════════════════════════════════════════════
    //  UTILITAIRES
    // ══════════════════════════════════════════════

    private void scrollAndClick(By locator, String label) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
                new Actions(driver).moveToElement(el).perform();
                try {
                    el.click();
                } catch (ElementClickInterceptedException e) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
                }
                LOGGER.info(label + " cliqué");
                return;
            } catch (StaleElementReferenceException e) {
                attempts++;
                sleep(300);
            }
        }
        throw new RuntimeException("scrollAndClick échoué : " + label);
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }

    private int resolveSettledFeaturesCount() {
        int previous = driver.findElements(featureItems).size();
        int stableReads = 0;
        long deadline = System.currentTimeMillis() + 4000;

        while (System.currentTimeMillis() < deadline) {
            sleep(200);
            int current = driver.findElements(featureItems).size();

            if (current == previous) {
                stableReads++;
                if (stableReads >= 2) {
                    return current;
                }
            } else {
                previous = current;
                stableReads = 0;
            }
        }
        return previous;
    }

    private void ensureFeatureAddButtonReady() {
        if (isClickable(addFeatureBtn, 2)) {
            return;
        }

        if (isClickable(featuresSectionCloseBtn, 2)) {
            scrollAndClick(featuresSectionCloseBtn, "Fermer section Features");
            wait.until(ExpectedConditions.visibilityOfElementLocated(featuresSectionOpenBtn));
        }

        if (isClickable(featuresSectionOpenBtn, 2)) {
            scrollAndClick(featuresSectionOpenBtn, "Afficher section Features");
        }

        wait.until(ExpectedConditions.elementToBeClickable(addFeatureBtn));
    }

    private boolean isClickable(By locator, int timeoutSeconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                    .until(ExpectedConditions.elementToBeClickable(locator));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
