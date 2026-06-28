package org.automation.pages.domain;

import org.automation.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.util.List;
import java.util.logging.Logger;

/**
 * Page Object pour la section "Domaine" dans l'onglet
 * "Contenu pour les espaces digitaux" d'un environnement.
 *
 * EnvironmentDigitalSpacePage gère uniquement l'ouverture de la section
 * et instancie cette classe — exactement comme pour FaqFormPage.
 */
public class DomainListPage extends BasePage {

    private static final Logger LOGGER = Logger.getLogger(DomainListPage.class.getName());

    // ══════════════════════════════════════════════
    //  CARTES DOMAINE
    // ══════════════════════════════════════════════

    /** Toutes les cartes existantes (exclut le bouton "+ Ajouter"). */
    private final By domainCards = By.xpath(
            "//div[contains(@class,'q-tab-panel')]" +
                    "//div[contains(@class,'card-container')]" +
                    "[.//div[contains(text(),'Nom de domaine :')]]"
    );

    /** Bouton "+ Cliquer pour ajouter un domaine secondaire". */
    private final By addSecondaryDomainBtn = By.xpath(
            "//span[contains(normalize-space(),'Cliquer pour ajouter un domaine secondaire')]"
    );

    // ══════════════════════════════════════════════
    //  MESSAGES D'ERREUR MÉTIER
    // ══════════════════════════════════════════════

    /** Message affiché quand on tente de supprimer un domaine principal. */
    private final By cannotDeletePrimaryMsg = By.xpath(
            "//div[contains(@class,'col-12') and contains(@class,'text-error-500')]" +
                    "[normalize-space(text())='On ne peut pas supprimer un domaine principal']"
    );

    /** Message affiché quand on tente de décocher le domaine principal. */
    private final By cannotChangePrimaryMsg = By.xpath(
            "//div[contains(@class,'col-12') and contains(@class,'text-error-500')]" +
                    "[normalize-space(text())='On ne peut pas changer un domaine principal']"
    );

    // ══════════════════════════════════════════════
    //  BOUTON FERMER DE LA SECTION
    // ══════════════════════════════════════════════

    private final By sectionFermerBtn = By.xpath(
            "//span[contains(normalize-space(.),'Domaine')]" +
                    "/ancestor::div[contains(@class,'row') and contains(@class,'items-start')]" +
                    "//div[normalize-space(text())='Fermer']"
    );

    // ══════════════════════════════════════════════
    //  DIALOG DE SÉLECTION
    // ══════════════════════════════════════════════

    private final By selectionDialog = By.xpath(
            "//div[@role='dialog' and @aria-modal='true']"
    );

    private final By dialogCards = By.xpath(
            "//div[@role='dialog']" +
                    "//div[contains(@class,'cursor-pointer')" +
                    " and contains(normalize-space(.),'Nom de domaine')]"
    );

    private final By dialogValidateBtn = By.xpath(
            "//div[@role='dialog']//button[" +
                    ".//span[contains(normalize-space(.),'Ajouter les domaines')] or " +
                    "contains(@class,'btn-primary')]"
    );



    // ══════════════════════════════════════════════
    //  DIALOG DE CONFIRMATION SUPPRESSION
    // ══════════════════════════════════════════════

    private final By deleteConfirmDialog = By.xpath(
            "//div[contains(@class,'q-card')][.//div[normalize-space()='Voulez vous supprimer ce domaine ?']]"
    );

    private final By deleteConfirmBtn = By.xpath(
            "//div[@role='dialog']" +
                    "//button[.//span[normalize-space(text())='Valider'] or normalize-space(text())='Valider']"
    );

    private final By deleteCancelBtn = By.xpath(
            "//div[@role='dialog']" +
                    "//button[.//span[normalize-space(text())='Annuler'] or normalize-space(text())='Annuler']"
    );


    private final By domainDeleteSuccessToast = By.xpath(
            "//div[contains(@class,'q-notification')]" +
                    "[.//div[contains(text(),'Opération terminée')]]"
    );
    // ══════════════════════════════════════════════
    //  ICÔNE DELETE PAR INDEX
    // ══════════════════════════════════════════════

    private By deleteIconAt(int oneBased) {
        return By.xpath(
                "(//div[contains(@class,'q-tab-panel')]" +
                        "//div[@data-v-68c4a73a and contains(@class,'card-container')]" +
                        "[.//div[contains(normalize-space(.),'Nom de domaine :')]])" +
                        "[" + oneBased + "]" +
                        "//i[normalize-space(text())='delete']"
        );
    }

    // ══════════════════════════════════════════════
    //  CHECKBOX "Domaine principale" PAR INDEX
    // ══════════════════════════════════════════════

    private By domainPrimaryCheckbox(int oneBased) {
        return By.xpath(
                "(//div[contains(@class,'card-container')]" +
                        "[.//div[contains(text(),'Nom de domaine :')]])" +
                        "[" + oneBased + "]" +
                        "//div[@role='checkbox' and .//span[normalize-space()='Domaine principale']]"
        );
    }

    private By domainPrimaryCheckedIndicator(int oneBased) {
        return By.xpath(
                "(//div[contains(@class,'card-container')]" +
                        "[.//div[contains(text(),'Nom de domaine :')]])" +
                        "[" + oneBased + "]" +
                        "//div[@role='checkbox' and @aria-checked='true']"
        );
    }

    public boolean isPrimaryDomain(int oneBased) {
        int primaryIndex = getPrimaryDomainIndex();
        return primaryIndex == oneBased;
    }



    public int getPrimaryDomainIndex() {
        List<WebElement> cards = driver.findElements(
                By.xpath("//div[contains(@class,'card-container')][.//div[contains(text(),'Nom de domaine :')]]")
        );

        for (int i = 0; i < cards.size(); i++) {
            WebElement checkbox = cards.get(i).findElement(By.xpath(".//div[@role='checkbox']"));

            if ("true".equals(checkbox.getAttribute("aria-checked"))) {
                LOGGER.info("Domaine principal trouvé à l'index: " + (i + 1));
                return i + 1;
            }
        }

        LOGGER.warning("Aucun domaine principal trouvé");
        return -1;
    }



    public int getFirstNonPrimaryDomainIndex() {
        List<WebElement> cards = driver.findElements(
                By.xpath("//div[contains(@class,'card-container')][.//div[contains(text(),'Nom de domaine :')]]")
        );

        for (int i = 0; i < cards.size(); i++) {
            WebElement checkbox = cards.get(i).findElement(By.xpath(".//div[@role='checkbox']"));

            if ("false".equals(checkbox.getAttribute("aria-checked"))) {
                LOGGER.info("Premier domaine non principal trouvé à l'index: " + (i + 1));
                return i + 1;
            }
        }

        LOGGER.warning("Aucun domaine non principal trouvé");
        return -1;
    }

    public boolean isDomainPrimaryBeforeDelete(int oneBased) {
        boolean isPrimary = isDomainPrimary(oneBased);
        if (isPrimary) {
            LOGGER.warning("ATTENTION: Tentative de suppression du domaine principal à l'index " + oneBased);
        }
        return isPrimary;
    }

    public boolean deleteDomainIfNotPrimary(int oneBased) {
        if (isDomainPrimary(oneBased)) {
            LOGGER.warning("Impossible de supprimer le domaine principal à l'index " + oneBased);
            return false;
        }
        deleteItemAt(oneBased);
        return true;
    }


    // ══════════════════════════════════════════════
    //  TOAST SUCCÈS
    // ══════════════════════════════════════════════

    private final By successToast = By.xpath(
            "//div[contains(@class,'q-notification__message')] | " +
                    "//div[contains(@class,'text-subtitle2')" +
                    " and contains(normalize-space(.),'Opération terminée')]"
    );

    // ══════════════════════════════════════════════
    //  CONSTRUCTEUR
    // ══════════════════════════════════════════════

    public DomainListPage() { super(); }

    // ══════════════════════════════════════════════
    //  COMPTAGE
    // ══════════════════════════════════════════════

    // ══════════════════════════════════════════════
//  COMPTAGE AVEC ATTENTE
// ══════════════════════════════════════════════

    public int getDomainCount() {
        try {
            // Attendre que les cartes de domaine soient présentes
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//div[contains(@class,'card-container')][.//div[contains(text(),'Nom de domaine :')]]")
            ));
            int count = driver.findElements(domainCards).size();
            LOGGER.info("Nombre de domaines trouvés: " + count);
            return count;
        } catch (Exception e) {
            LOGGER.warning("Erreur comptage domaines: " + e.getMessage());
            return 0;
        }
    }

    // ══════════════════════════════════════════════
    //  AJOUT — bouton + dialog
    // ══════════════════════════════════════════════

    public void clickAddSecondaryDomain() {
        scrollAndClick(addSecondaryDomainBtn, "Ajouter domaine secondaire");
        wait.until(ExpectedConditions.visibilityOfElementLocated(selectionDialog));
        LOGGER.info("Dialog sélection domaine ouvert");
    }

    public int getDialogCardCount() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(selectionDialog));
        return driver.findElements(dialogCards).size();
    }

    public void selectCardInDialog(int zeroBasedIndex) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(selectionDialog));
        List<WebElement> cards = wait.until(d -> {
            List<WebElement> c = d.findElements(dialogCards);
            return c.size() > zeroBasedIndex ? c : null;
        });
        WebElement card = cards.get(zeroBasedIndex);
        new Actions(driver).moveToElement(card).perform();
        try {
            card.click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", card);
        }
        LOGGER.info("Carte dialog index " + zeroBasedIndex + " sélectionnée");
    }

    public void validateDialog() {
        wait.until(ExpectedConditions.elementToBeClickable(dialogValidateBtn)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(selectionDialog));
        sleep(300);
        LOGGER.info("Dialog domaine validé et fermé");

    }

    public void closeDialogWithEscape() {
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(selectionDialog));
        } catch (Exception e) {
            LOGGER.warning("Dialog non fermé après ESCAPE");
        }
    }

    public boolean isSelectionDialogOpen() {
        try {
            return driver.findElement(selectionDialog).isDisplayed();
        } catch (Exception e) { return false; }
    }

    // ══════════════════════════════════════════════
    //  SUPPRESSION
    // ══════════════════════════════════════════════

    /** Clique l'icône delete de la première carte (index 1). */
    public void deleteFirstDomain() {
        deleteItemAt(1);
    }

    /** Clique l'icône delete de la carte à l'index 1-based donné. */
    public void deleteItemAt(int oneBased) {
        scrollAndClick(deleteIconAt(oneBased), "Supprimer domaine index " + oneBased);
        LOGGER.info("Icône delete cliquée sur carte " + oneBased);
    }

    public boolean isDomainDeleteConfirmDialogDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(deleteConfirmDialog));
            return true;
        } catch (Exception e) { return false; }
    }

    public void confirmDomainDeletion() {
        wait.until(ExpectedConditions.elementToBeClickable(deleteConfirmBtn)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(deleteConfirmDialog));

        // Attendre que le toast de succès apparaisse
        waitForDomainDeleteSuccessToast();

        sleep(500);
        LOGGER.info("Suppression confirmée avec succès");
    }

    public void cancelDomainDeletion() {
        wait.until(ExpectedConditions.elementToBeClickable(deleteCancelBtn)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(deleteConfirmDialog));
        sleep(300);
        LOGGER.info("Suppression annulée");
    }


    public boolean isDomainDeleteSuccessToastDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(domainDeleteSuccessToast));
            LOGGER.info("Toast de succès 'Domaine supprimée avec succès' affiché");
            return true;
        } catch (Exception e) {
            LOGGER.warning("Toast de succès non trouvé: " + e.getMessage());
            return false;
        }
    }

    /**
     * Attend que le toast de succès de suppression apparaisse et disparaisse
     */
    public void waitForDomainDeleteSuccessToast() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(domainDeleteSuccessToast));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(domainDeleteSuccessToast));
            LOGGER.info("Toast de suppression domaine traité");
        } catch (Exception e) {
            LOGGER.warning("Toast de succès non trouvé: " + e.getMessage());
        }
    }
    // ══════════════════════════════════════════════
    //  DOMAINE PRINCIPALE — checkbox
    // ══════════════════════════════════════════════

    public boolean isDomainPrimary(int oneBased) {
        try {
            return !driver.findElements(domainPrimaryCheckedIndicator(oneBased)).isEmpty();
        } catch (Exception e) { return false; }
    }

    /**
     * Tente de cocher/décocher la checkbox "Domaine principale" d'une carte.
     * L'appli peut afficher un message d'erreur si c'est le domaine principal.
     */
    public void clickDomainPrimaryCheckbox(int oneBased) {
        scrollAndClick(domainPrimaryCheckbox(oneBased),
                "Checkbox domaine principale carte " + oneBased);
    }

    // ══════════════════════════════════════════════
    //  MESSAGES D'ERREUR MÉTIER
    // ══════════════════════════════════════════════

    public boolean isCannotDeletePrimaryMessageDisplayed() {
        try {
            return !driver.findElements(cannotDeletePrimaryMsg).isEmpty()
                    && driver.findElement(cannotDeletePrimaryMsg).isDisplayed();
        } catch (Exception e) { return false; }
    }

    public boolean isCannotChangePrimaryMessageDisplayed() {
        try {
            return !driver.findElements(cannotChangePrimaryMsg).isEmpty()
                    && driver.findElement(cannotChangePrimaryMsg).isDisplayed();
        } catch (Exception e) { return false; }
    }

    // ══════════════════════════════════════════════
    //  SECTION — bouton Fermer
    // ══════════════════════════════════════════════

    public void closeDomainSection() {
        scrollAndClick(sectionFermerBtn, "Fermer section Domaine");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(addSecondaryDomainBtn));
        LOGGER.info("Section Domaine fermée");
    }

    public boolean isDomainSectionOpen() {
        try {
            return !driver.findElements(addSecondaryDomainBtn).isEmpty()
                    && driver.findElement(addSecondaryDomainBtn).isDisplayed();
        } catch (Exception e) { return false; }
    }

    // ══════════════════════════════════════════════
    //  TOAST
    // ══════════════════════════════════════════════

    public boolean isOperationSuccessful() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(successToast));
            return true;
        } catch (Exception e) { return false; }
    }

    // ══════════════════════════════════════════════
    //  UTILITAIRES PRIVÉS
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
        try { Thread.sleep(ms); } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}