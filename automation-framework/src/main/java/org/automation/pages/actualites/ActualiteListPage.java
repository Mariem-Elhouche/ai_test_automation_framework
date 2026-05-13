package org.automation.pages.actualites;

import org.automation.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.util.List;
import java.util.logging.Logger;

public class ActualiteListPage extends BasePage {

    private static final Logger LOGGER = Logger.getLogger(ActualiteListPage.class.getName());

    // ══════════════════════════════════════════════
    //  BOUTONS ET FORMULAIRES
    // ══════════════════════════════════════════════

    private final By addActualiteBtn = By.xpath(
            "//span[contains(text(),'Ajouter une nouvelle actualité')]"
    );

    private final By actualiteTitleFr = By.xpath(
            "//input[@name='news-title-fr' or @id='news-title-fr']"
    );

    private final By actualiteTitleEn = By.xpath(
            "//input[@name='news-title-en' or @id='news-title-en']"
    );

    private final By actualiteDescFr = By.xpath(
            "//textarea[@name='news-title-description-fr' or @id='news-title-description-fr']"
    );

    private final By actualiteDescEn = By.xpath(
            "//textarea[@name='news-title-description-en' or @id='news-title-description-en']"
    );

    private final By actualiteLien = By.xpath(
            "//input[@name='news-link' or @id='news-link']"
    );

    private final By actualiteLienTextFr = By.xpath(
            "//input[@name='news-link-fr' or @id='news-link-fr']"
    );

    private final By actualiteLienTextEn = By.xpath(
            "//input[@name='news-link-en' or @id='news-link-en']"
    );

    private final By actualiteDate = By.xpath(
            "//input[@name='news-start-date' or @id='news-start-date']"
    );

    private final By actualiteSaveBtn = By.xpath(
            "//div[contains(@class,'q-tab-panel')]" +
                    "//button[.//span[normalize-space(text())='Enregistrer']]"
    );

    private final By addConfirmBtn = By.xpath(
            "//div[@role='dialog']" +
                    "//button[.//span[normalize-space(text())='Valider'] or normalize-space(text())='Valider']"
    );
    private final By cancelActualiteCreationBtn = By.xpath(
            "//div[@role='dialog' and @aria-modal='true']" +
                    "[.//div[contains(normalize-space(.),'Voulez vous Ajouter cet actual')]]" +
                    "//span[normalize-space(text())='Annuler']" +
                    "/ancestor::button"
    );

    private final By actualiteCancelConfirmDialog = By.xpath(
            "//div[@role='dialog' and @aria-modal='true']" +
                    "[.//div[contains(normalize-space(.),\"Voulez vous Ajouter cet actualité\")]]"
    );

    private final By actualiteItems = By.xpath(
            "//div[contains(@class,'q-tab-panel')]" +
                    "//div[contains(@class,'card-container')]" +
                    "[.//div[contains(normalize-space(.),'Nom :')]]" +
                    "[not(.//input) and not(.//textarea)]"
    );

    private final By actualiteFormInline = By.xpath(
            "//div[contains(@class,'card-container')][.//input[@name='news-title-fr']]"
    );

    // ══════════════════════════════════════════════
    //  UPLOAD IMAGE
    // ══════════════════════════════════════════════

    private final By actualiteImageInput = By.xpath(
            "//div[contains(@class,'card-container')][.//input[@name='news-title-fr']]//input[@type='file']"
    );

    private final By actualiteImageDeleteBtn = By.xpath(
            "//div[contains(@class,'card-container')][.//input[@name='news-title-fr']]" +
                    "//div[contains(@class,'flex') and contains(@class,'items-center')]//i[text()='delete']"
    );

    private final By actualiteImagePreview = By.xpath(
            "//div[contains(@class,'card-container')][.//input[@name='news-title-fr']]" +
                    "//div[contains(@class,'container-picture')][.//img]"
    );

    // ══════════════════════════════════════════════
    //  SUPPRESSION
    // ══════════════════════════════════════════════

    private final By deleteIcon = By.xpath(
            "//div[contains(@class,'q-tab-panel')]//i[text()='delete'][1]"
    );

    private final By actualiteDeleteConfirmDialog = By.xpath(
            "//div[@role='dialog' and @aria-modal='true']" +
                    "[.//div[contains(normalize-space(.),\"Voulez vous Supprimer cet actualité\")]]"
    );



    private final By actualiteDeleteConfirmBtn = By.xpath(
            "//div[@role='dialog' and @aria-modal='true']" +
                    "[.//div[contains(normalize-space(.),\"Voulez vous Supprimer cet actualité\")]]" +
                    "//button[.//span[normalize-space(text())='Valider']]"
    );

    private final By actualiteDeleteCancelBtn = By.xpath(
            "//div[@role='dialog' and @aria-modal='true']" +
                    "[.//div[contains(normalize-space(.),\"Voulez vous Supprimer cet actualité\")]]" +
                    "//button[.//span[normalize-space(text())='Annuler']]"
    );

    private final By actualiteDeleteSuccessToast = By.xpath(
            "//div[contains(@class,'q-notification')]" +
                    "[.//div[contains(text(),'actualité supprimée') or contains(text(),'supprimé avec succès')]]"
    );

    private final By successToast = By.xpath(
            "//div[contains(@class,'q-notification__message')]"
    );

    // ══════════════════════════════════════════════
    //  CONSTRUCTEUR
    // ══════════════════════════════════════════════

    public ActualiteListPage() { super(); }

    // ══════════════════════════════════════════════
    //  COMPTAGE
    // ══════════════════════════════════════════════

    public int getActualitesCount() {
        try {
            return driver.findElements(actualiteItems).size();
        } catch (Exception e) { return 0; }
    }

    // ══════════════════════════════════════════════
    //  AJOUT ET FORMULAIRE
    // ══════════════════════════════════════════════

    public void clickAddActualite() {
        scrollAndClick(addActualiteBtn, "Ajouter actualité");
        wait.until(ExpectedConditions.visibilityOfElementLocated(actualiteTitleFr));
        LOGGER.info("Formulaire actualité inline ouvert");
    }

    public void fillTitleFr(String value) { fillField(actualiteTitleFr, value, "Titre FR"); }
    public void fillTitleEn(String value) { fillField(actualiteTitleEn, value, "Titre EN"); }
    public void fillDescFr(String value)  { fillTextarea(actualiteDescFr, value, "Desc FR"); }
    public void fillDescEn(String value)  { fillTextarea(actualiteDescEn, value, "Desc EN"); }
    public void fillLink(String value)    { fillField(actualiteLien, value, "Lien"); }
    public void fillLinkTextFr(String value) { fillField(actualiteLienTextFr, value, "Texte lien FR"); }
    public void fillLinkTextEn(String value) { fillField(actualiteLienTextEn, value, "Texte lien EN"); }
    public void fillDate(String value)    { fillField(actualiteDate, value, "Date début"); }

    public void saveActualite() {
        scrollAndClick(actualiteSaveBtn, "Enregistrer actualité");
    }

    public boolean isActualiteSavedSuccessfully() {
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(actualiteFormInline));
            List<WebElement> items = driver.findElements(actualiteItems);
            return !items.isEmpty();
        } catch (Exception e) {
            LOGGER.warning("Vérification sauvegarde actualité échouée: " + e.getMessage());
            return false;
        }
    }

    public void confirmActualiteCreation() {
        wait.until(ExpectedConditions.elementToBeClickable(addConfirmBtn)).click();

        // Attendre que le toast de succès apparaisse
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(successToast));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(successToast));
            LOGGER.info("Toast de confirmation d'ajout de service affiché");
        } catch (Exception e) {
            LOGGER.warning("Toast de confirmation non trouvé: " + e.getMessage());
        }

        sleep(500);
        LOGGER.info("Ajout de service confirmé avec succès");
    }


    public void cancelActualiteCreation() {
        scrollAndClick(cancelActualiteCreationBtn, "Annuler ajout feature");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(actualiteCancelConfirmDialog));
        LOGGER.info("Ajout feature annulé");
    }

    // ══════════════════════════════════════════════
    //  UPLOAD IMAGE
    // ══════════════════════════════════════════════

    public void uploadPhoto(String filePath) {
        WebElement input = driver.findElement(actualiteImageInput);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].style.display='block'; arguments[0].style.visibility='visible';",
                input
        );
        input.sendKeys(filePath);
        sleep(800);
        LOGGER.info("Photo uploadée : " + filePath);
    }

    public boolean isImageUploaded() {
        try {
            return !driver.findElements(actualiteImagePreview).isEmpty();
        } catch (Exception e) { return false; }
    }

    public void deleteActualiteImage() {
        scrollAndClick(actualiteImageDeleteBtn, "Supprimer image actualité");
        sleep(500);
    }

    public boolean isImageDeleted() {
        try {
            return driver.findElements(actualiteImagePreview).isEmpty();
        } catch (Exception e) { return true; }
    }

    // ══════════════════════════════════════════════
    //  SUPPRESSION
    // ══════════════════════════════════════════════

    public void deleteFirstActualite() {
        scrollAndClick(deleteIcon, "Supprimer première actualité");
    }

    public boolean isDeleteConfirmDialogDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(actualiteDeleteConfirmDialog));
            return true;
        } catch (Exception e) { return false; }
    }

    public void confirmDeletion() {
        wait.until(ExpectedConditions.elementToBeClickable(actualiteDeleteConfirmBtn)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(actualiteDeleteConfirmDialog));
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(actualiteDeleteSuccessToast));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(actualiteDeleteSuccessToast));
        } catch (Exception e) {
            LOGGER.warning("Toast suppression actualité non trouvé");
        }
        sleep(500);
    }

    public void cancelDeletion() {
        wait.until(ExpectedConditions.elementToBeClickable(actualiteDeleteCancelBtn)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(actualiteDeleteConfirmDialog));
        sleep(300);
    }

    // ══════════════════════════════════════════════
    //  UTILITAIRES
    // ══════════════════════════════════════════════

    private void fillField(By locator, String value, String label) {
        WebElement field = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        new Actions(driver).moveToElement(field).perform();
        field.click();
        field.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        field.sendKeys(value);
        field.sendKeys(Keys.TAB);
        LOGGER.info(label + " : " + value);
    }

    private void fillTextarea(By locator, String value, String label) {
        WebElement field = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        new Actions(driver).moveToElement(field).perform();
        field.click();
        field.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        field.sendKeys(value);
        LOGGER.info(label + " : " + value);
    }

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
}