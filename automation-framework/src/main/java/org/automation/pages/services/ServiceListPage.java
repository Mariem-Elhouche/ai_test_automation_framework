package org.automation.pages.services;

import org.automation.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.util.List;
import java.util.logging.Logger;

public class ServiceListPage extends BasePage {

    private static final Logger LOGGER = Logger.getLogger(ServiceListPage.class.getName());

    // ══════════════════════════════════════════════
    //  BOUTONS ET CARTES
    // ══════════════════════════════════════════════

    private final By addServiceBtn = By.xpath(
            "//span[contains(text(),'Cliquer pour ajouter un nouveau élément à la liste')]"
    );

    private final By serviceItems = By.xpath(
            "//div[contains(@class,'q-tab-panel')]" +
                    "//div[contains(@class,'card-container')]" +
                    "[.//div[starts-with(normalize-space(),'Nom :')]]"
    );

    // ══════════════════════════════════════════════
    //  DIALOG DE SÉLECTION
    // ══════════════════════════════════════════════

    private final By selectionDialog = By.xpath(
            "//div[@role='dialog' and @aria-modal='true']" +
                    "//div[contains(@class,'q-card') and contains(@class,'container-xl')]"
    );

    private final By dialogCards = By.xpath(
            "//div[@role='dialog']" +
                    "//div[contains(@class,'card-container')]" +
                    "[not(contains(@class,'service-disabled'))]"
    );

    private final By dialogAllCards = By.xpath(
            "//div[@role='dialog']//div[contains(@class,'card-container')]"
    );
    private final By dialogDisabledCards = By.xpath(
            "//div[@role='dialog']//div[contains(@class,'card-container') and contains(@class,'service-disabled')]"
    );
    private final By dialogAlreadyAttachedBadges = By.xpath(
            "//div[@role='dialog']//*[@role='status' and " +
                    "(contains(normalize-space(.),'Déja rattaché') or contains(normalize-space(.),'Déjà rattaché'))]"
    );

    private final By dialogValidateBtn = By.xpath(
            "//div[@role='dialog']//button[" +
                    ".//span[contains(normalize-space(.),'Ajouter les services')] or " +
                    "contains(@class,'btn-primary')]"
    );

    private final By addConfirmBtn = By.xpath(
            "//div[@role='dialog']" +
                    "//button[.//span[normalize-space(text())='Valider'] or normalize-space(text())='Valider']"
    );

    private final By dialogPaginationLabel = By.xpath(
            "//div[@role='dialog']//div[contains(text(),'Page') and contains(text(),'sur')]"
    );
    private final By dialogScrollableContainer = By.xpath(
            "//div[@role='dialog']//div[contains(@class,'overflow-auto') or contains(@class,'scroll')]"
    );

    private final By dialogNextPageBtn = By.xpath(
            "(//div[@role='dialog']//button)[last()-1]"
    );

    private final By dialogPrevPageBtn = By.xpath(
            "(//div[@role='dialog']//button)[1]"
    );

    // ══════════════════════════════════════════════
    //  SUPPRESSION
    // ══════════════════════════════════════════════

    private final By deleteIcon = By.xpath(
            "//div[contains(@class,'q-tab-panel')]//i[text()='delete'][1]"
    );

    private final By serviceDeleteConfirmDialog = By.xpath(
            "//div[@role='dialog' and @aria-modal='true']" +
                    "[.//div[contains(normalize-space(.),\"Voulez vous Supprimer ce service\")]]"
    );

    private final By serviceDeleteConfirmBtn = By.xpath(
            "//div[@role='dialog' and @aria-modal='true']" +
                    "[.//div[contains(normalize-space(.),\"Voulez vous Supprimer ce service\")]]" +
                    "//button[.//span[normalize-space(text())='Valider']]"
    );

    private final By serviceDeleteCancelBtn = By.xpath(
            "//div[@role='dialog' and @aria-modal='true']" +
                    "[.//div[contains(normalize-space(.),\"Voulez vous Supprimer ce service\")]]" +
                    "//button[.//span[normalize-space(text())='Annuler']]"
    );

    private final By serviceDeleteSuccessToast = By.xpath(
            "//div[contains(@class,'q-notification')]" +
                    "[.//div[contains(text(),'Service supprimé') or contains(text(),'supprimé avec succès')]]"
    );

    private final By successToast = By.xpath(
            "//div[contains(@class,'q-notification__message')] | " +
                    "//div[contains(@class,'text-subtitle2') and contains(normalize-space(.),'Opération terminée')]"
    );

    // ══════════════════════════════════════════════
    //  CONSTRUCTEUR
    // ══════════════════════════════════════════════

    public ServiceListPage() { super(); }

    // ══════════════════════════════════════════════
    //  COMPTAGE
    // ══════════════════════════════════════════════

    public int getServiceCount() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//div[contains(@class,'card-container')]")
            ));
            return driver.findElements(serviceItems).size();
        } catch (Exception e) {
            LOGGER.warning("Erreur comptage services: " + e.getMessage());
            return 0;
        }
    }

    // ══════════════════════════════════════════════
    //  AJOUT
    // ══════════════════════════════════════════════

    public void clickAddService() {
        scrollAndClick(addServiceBtn, "Ajouter service");
        wait.until(ExpectedConditions.visibilityOfElementLocated(selectionDialog));
    }

    public int getDialogCardCount() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(selectionDialog));
        return driver.findElements(dialogCards).size();
    }

    public void selectCardInDialog(int cardIndex) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(selectionDialog));
        List<WebElement> cards = wait.until(d -> {
            List<WebElement> c = d.findElements(dialogCards);
            return c.size() > cardIndex ? c : null;
        });
        WebElement card = cards.get(cardIndex);
        new Actions(driver).moveToElement(card).perform();
        try {
            card.click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", card);
        }
        LOGGER.info("Carte index " + cardIndex + " sélectionnée");
    }

    public void selectFirstNonAttachedCardInDialog() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(selectionDialog));

        int maxPagesToScan = Math.max(getDialogTotalPages(), 1);
        for (int i = 0; i < maxPagesToScan; i++) {
            List<WebElement> cards = driver.findElements(dialogCards);
            if (!cards.isEmpty()) {
                WebElement card = cards.get(0);
                new Actions(driver).moveToElement(card).perform();
                try {
                    card.click();
                } catch (ElementClickInterceptedException e) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", card);
                }
                LOGGER.info("Première carte non rattachée sélectionnée (page " + getDialogCurrentPage() + ")");
                return;
            }

            if (!goToNextDialogPageIfAvailable()) {
                break;
            }
        }

        throw new RuntimeException("Aucune carte service non rattachée trouvée dans le dialogue");
    }

    public void validateDialog() {
        wait.until(ExpectedConditions.elementToBeClickable(dialogValidateBtn)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(selectionDialog));
        sleep(300);
    }

    public void confirmServiceCreation() {
        wait.until(ExpectedConditions.elementToBeClickable(addConfirmBtn)).click();
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(successToast));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(successToast));
        } catch (Exception e) {
            LOGGER.warning("Toast de confirmation non trouvé: " + e.getMessage());
        }
        sleep(500);
    }


    public boolean isSelectionDialogOpen() {
        try {
            return driver.findElement(selectionDialog).isDisplayed();
        } catch (Exception e) { return false; }
    }

    public String getDialogPaginationText() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(selectionDialog));
            WebElement pagination = wait.until(ExpectedConditions.presenceOfElementLocated(dialogPaginationLabel));

            // Ensure footer pagination is visible even when dialog content is long.
            if (driver instanceof JavascriptExecutor) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", pagination);
            }

            try {
                wait.until(ExpectedConditions.visibilityOf(pagination));
            } catch (Exception ignored) {
                // Fallback: some layouts require scrolling inside dialog container first
                List<WebElement> containers = driver.findElements(dialogScrollableContainer);
                if (!containers.isEmpty() && driver instanceof JavascriptExecutor) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollTop = arguments[0].scrollHeight;", containers.get(0));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", pagination);
                }
            }

            return pagination.getText().trim();
        } catch (Exception e) { return ""; }
    }

    public int getDialogCurrentPage() {
        try {
            String text = getDialogPaginationText();
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("Page\\s*(\\d+)\\s*sur");
            java.util.regex.Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
            return 1;
        } catch (Exception e) { return 1; }
    }

    public void clickDialogNextPage() {
        scrollAndClick(dialogNextPageBtn, "Page suivante dialog");
        sleep(600);
    }

    public void clickDialogPrevPage() {
        scrollAndClick(dialogPrevPageBtn, "Page précédente dialog");
        sleep(600);
    }

    public boolean isCardAlreadyAttached(int index) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(selectionDialog));
        List<WebElement> cards = wait.until(d -> {
            List<WebElement> c = d.findElements(dialogAllCards);
            return !c.isEmpty() ? c : null;
        });
        if (cards.size() <= index) {
            throw new RuntimeException("Index carte invalide: " + index);
        }
        WebElement card = cards.get(index);
        boolean hasBadge = !card.findElements(
                By.xpath(".//*[@role='status' and (contains(normalize-space(.),'Déja rattaché') or contains(normalize-space(.),'Déjà rattaché'))]")
        ).isEmpty();
        boolean isDisabled = card.getAttribute("class") != null && card.getAttribute("class").contains("service-disabled");
        return hasBadge || isDisabled;
    }

    public boolean hasAnyAlreadyAttachedCardInDialog() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(selectionDialog));

        int maxPagesToScan = Math.max(getDialogTotalPages(), 1);
        for (int i = 0; i < maxPagesToScan; i++) {
            if (!driver.findElements(dialogAlreadyAttachedBadges).isEmpty()) {
                return true;
            }
            if (!driver.findElements(dialogDisabledCards).isEmpty()) {
                return true;
            }
            if (!goToNextDialogPageIfAvailable()) {
                break;
            }
        }
        return false;
    }

    // ══════════════════════════════════════════════
    //  SUPPRESSION
    // ══════════════════════════════════════════════

    public void deleteFirstService() {
        scrollAndClick(deleteIcon, "Supprimer premier service");
    }

    public boolean isDeleteConfirmDialogDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(serviceDeleteConfirmDialog));
            return true;
        } catch (Exception e) { return false; }
    }

    public void confirmDeletion() {
        wait.until(ExpectedConditions.elementToBeClickable(serviceDeleteConfirmBtn)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(serviceDeleteConfirmDialog));
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(serviceDeleteSuccessToast));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(serviceDeleteSuccessToast));
        } catch (Exception e) {
            LOGGER.warning("Toast de suppression non trouvé");
        }
        sleep(500);
    }

    public void cancelDeletion() {
        wait.until(ExpectedConditions.elementToBeClickable(serviceDeleteCancelBtn)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(serviceDeleteConfirmDialog));
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

    private boolean goToNextDialogPageIfAvailable() {
        List<WebElement> nextButtons = driver.findElements(dialogNextPageBtn);
        if (nextButtons.isEmpty()) return false;

        WebElement nextButton = nextButtons.get(0);
        if (isDisabled(nextButton)) return false;

        int previousPage = getDialogCurrentPage();
        scrollAndClick(dialogNextPageBtn, "Page suivante dialog");
        try {
            wait.until(d -> getDialogCurrentPage() != previousPage);
        } catch (Exception ignored) {
            sleep(500);
        }
        return true;
    }

    private int getDialogTotalPages() {
        try {
            String text = getDialogPaginationText();
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("Page\\s*\\d+\\s*sur\\s*(\\d+)");
            java.util.regex.Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        } catch (Exception ignored) {
        }
        return 1;
    }

    private boolean isDisabled(WebElement button) {
        String disabledAttr = button.getAttribute("disabled");
        String ariaDisabled = button.getAttribute("aria-disabled");
        String classes = button.getAttribute("class");
        return disabledAttr != null
                || "true".equalsIgnoreCase(ariaDisabled)
                || (classes != null && (classes.contains("disabled") || classes.contains("q-btn--disabled")));
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }
}
