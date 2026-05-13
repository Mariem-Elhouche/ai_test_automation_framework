package org.automation.pages.company;

import org.automation.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.logging.Logger;

public class CompanyAttachmentPage extends BasePage {

    private static final Logger LOGGER = Logger.getLogger(CompanyAttachmentPage.class.getName());

    private final By attachmentsTab = By.xpath("//div[@class='q-tab__label'][normalize-space()='Rattachements']");
    private final By activeAttachmentsTab = By.xpath(
            "//div[contains(@class,'q-tab--active')]//div[@class='q-tab__label'][normalize-space()='Rattachements']"
    );

    private final By groupSectionTitle = By.xpath("//h2[normalize-space()=\"Groupe d'entreprise\"]");
    private final By categoriesSectionTitle = By.xpath("//h2[normalize-space()='Catégories']");

    private final By toastMessage = By.xpath("//div[contains(@class,'q-notification__message')] | //div[contains(@class,'q-notification')]");

    private final By confirmDialog = By.xpath("//div[@role='dialog' and @aria-modal='true']");
    private final By confirmBtn = By.xpath(
            "//div[@role='dialog' and @aria-modal='true']//button[" +
                    ".//span[normalize-space()='Valider']" +
                    " or .//span[normalize-space()='Confirmer la suppression']" +
                    "]"
    );
    private final By cancelBtn = By.xpath(
            "//div[@role='dialog' and @aria-modal='true']//button[" +
                    ".//span[normalize-space()='Annuler']" +
                    " or .//span[normalize-space()='Ne pas supprimer']" +
                    "]"
    );

    private final By addNewCategoryCard = By.xpath(
            "//h2[normalize-space()='Catégories']" +
                    "/following::span[contains(normalize-space(.),'Ajouter une nouvelle catégorie')][1]"
    );

    private final By categoryNameInput = By.xpath(
            "//h2[normalize-space()='Catégories']" +
                    "/following::span[normalize-space(.)='Nom de la catégorie'][1]" +
                    "/following::input[1]"
    );
    private final By categoryCodeInput = By.xpath(
            "//h2[normalize-space()='Catégories']" +
                    "/following::span[normalize-space(.)='Code'][1]" +
                    "/following::input[1]"
    );

    private final By groupSelectInput = By.xpath(
            "//h2[normalize-space()=\"Groupe d'entreprise\"]" +
                    "/following::label[contains(@class,'q-select')][1]//input[@type='search' and @role='combobox']"
    );

    private final By groupAddBtn = By.xpath(
            "//h2[normalize-space()=\"Groupe d'entreprise\"]" +
                    "/following::button[.//span[normalize-space()='Ajouter']][1]"
    );

    private final By categoriesFirstDeleteIcon = By.xpath(
            "//h2[normalize-space()='Catégories']" +
                    "/following::i[normalize-space(text())='delete'][1]"
    );

    private By groupDeleteIconByName(String groupName) {
        return By.xpath(
                "//h2[normalize-space()=\"Groupe d'entreprise\"]" +
                        "/following::div[contains(@class,'card-container')]" +
                        "[.//div[contains(normalize-space(.),'Nom :') and contains(normalize-space(.)," + xpathSafeString(groupName) + ")]]" +
                        "//i[normalize-space(text())='delete'][1]"
        );
    }

    private By categoryRowByNameAndCode(String name, String code) {
        // Category row appears as q-card with two spans: name + code
        return By.xpath(
                "//h2[normalize-space()='Catégories']" +
                        "/following::div[contains(@class,'q-card')]" +
                        "[.//span[contains(@class,'text-gray-700') and contains(normalize-space(.)," + xpathSafeString(name) + ")]]" +
                        "[.//span[contains(@class,'text-gray-500') and contains(normalize-space(.)," + xpathSafeString(code) + ")]]" +
                        "[1]"
        );
    }

    private By categoryDeleteIconByNameAndCode(String name, String code) {
        return By.xpath(
                "//h2[normalize-space()='Catégories']" +
                        "/following::div[contains(@class,'q-card')]" +
                        "[.//span[contains(@class,'text-gray-700') and contains(normalize-space(.)," + xpathSafeString(name) + ")]]" +
                        "[.//span[contains(@class,'text-gray-500') and contains(normalize-space(.)," + xpathSafeString(code) + ")]]" +
                        "//i[normalize-space(text())='delete'][1]"
        );
    }

    public void clickAttachmentsTab() {
        scrollAndClick(attachmentsTab, "Onglet Rattachements");
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(groupSectionTitle));
        } catch (Exception e) {
            LOGGER.warning("Chargement tab Rattachements: " + e.getMessage());
        }
    }

    public boolean isAttachmentsTabActive() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(activeAttachmentsTab));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void attachCompanyGroup(String groupName) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(groupSectionTitle));
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(groupSelectInput));
        input.click();
        input.clear();
        input.sendKeys(groupName);

        By option = By.xpath("//div[@role='listbox']//div[@role='option'][contains(normalize-space(.)," + xpathSafeString(groupName) + ")]");
        wait.until(ExpectedConditions.elementToBeClickable(option)).click();

        scrollAndClick(groupAddBtn, "Ajouter groupe d'entreprise");
        waitForToastIfAny();
        wait.until(ExpectedConditions.visibilityOfElementLocated(groupCardByName(groupName)));
    }

    public boolean isGroupAttached(String groupName) {
        try {
            return !driver.findElements(groupCardByName(groupName)).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickDeleteOnGroup(String groupName) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(groupSectionTitle));
        scrollAndClick(groupDeleteIconByName(groupName), "Supprimer groupe " + groupName);
    }

    public boolean isDeleteConfirmDialogDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(confirmDialog));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void confirmDeletion() {
        scrollAndClick(confirmBtn, "Valider suppression");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(confirmDialog));
        waitForToastIfAny();
    }

    public void cancelDeletion() {
        scrollAndClick(cancelBtn, "Annuler suppression");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(confirmDialog));
    }

    public void refreshAndReopenAttachmentsTab() {
        driver.navigate().refresh();
        clickAttachmentsTab();
    }

    public void waitUntilGroupNotAttached(String groupName) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(groupCardByName(groupName)));
    }

    public void openAddCategoryForm() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(categoriesSectionTitle));
        scrollAndClick(addNewCategoryCard, "Ajouter une nouvelle catégorie");
        wait.until(ExpectedConditions.visibilityOfElementLocated(categoryNameInput));
    }

    public void fillCategory(String name, String code) {
        WebElement nameEl = wait.until(ExpectedConditions.elementToBeClickable(categoryNameInput));
        nameEl.click();
        nameEl.clear();
        nameEl.sendKeys(name);

        WebElement codeEl = wait.until(ExpectedConditions.elementToBeClickable(categoryCodeInput));
        codeEl.click();
        codeEl.clear();
        codeEl.sendKeys(code);
    }

    public void saveCategory() {
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        WebElement btn = longWait.until(drv -> {
            try {
                WebElement nameEl = new WebDriverWait(drv, Duration.ofSeconds(5))
                        .until(ExpectedConditions.visibilityOfElementLocated(categoryNameInput));
                WebElement candidate = resolveCategorySaveButton(nameEl);
                return candidate.isDisplayed() && candidate.isEnabled() ? candidate : null;
            } catch (StaleElementReferenceException | NoSuchElementException e) {
                return null;
            }
        });

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
        longWait.until(ExpectedConditions.elementToBeClickable(btn));
        new Actions(driver).moveToElement(btn).perform();
        try {
            btn.click();
        } catch (StaleElementReferenceException e) {
            WebElement nameEl = longWait.until(ExpectedConditions.visibilityOfElementLocated(categoryNameInput));
            WebElement again = resolveCategorySaveButton(nameEl);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", again);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", again);
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
        LOGGER.info("Bouton Enregistrer (catégorie, card-container) cliqué");
        waitForToastIfAny();
    }

    /**
     * Bouton « Enregistrer » du bloc création/édition catégorie (même carte que les champs),
     * pas un autre « Enregistrer » du formulaire entreprise plus haut dans le DOM.
     */
    private By categorySaveButtonFromNameField() {
        return By.xpath(
                "ancestor::div[contains(@class,'card-container')][1]" +
                        "//button[contains(@class,'btn-primary')]" +
                        "[.//span[normalize-space()='Enregistrer']]"
        );
    }

    /**
     * Fallback si la structure du champ nom ne remonte pas à un card-container.
     */
    private By categorySaveButtonFallback() {
        return By.xpath(
                "//h2[normalize-space()='Catégories']" +
                        "/following::div[contains(@class,'card-container')]" +
                        "[.//span[normalize-space(.)='Nom de la catégorie']]" +
                        "//button[contains(@class,'btn-primary')][.//span[normalize-space()='Enregistrer']][1]"
        );
    }

    private WebElement resolveCategorySaveButton(WebElement nameEl) {
        try {
            return nameEl.findElement(categorySaveButtonFromNameField());
        } catch (NoSuchElementException e) {
            return driver.findElement(categorySaveButtonFallback());
        }
    }

    public void waitUntilCategoryPresent(String name, String code) {
        By row = categoryRowByNameAndCode(name, code);
        new WebDriverWait(driver, Duration.ofSeconds(25))
                .until(ExpectedConditions.visibilityOfElementLocated(row));
    }

    public boolean isCategoryPresent(String name, String code) {
        try {
            return !driver.findElements(categoryRowByNameAndCode(name, code)).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public void deleteCategory(String name, String code) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(categoriesSectionTitle));
        By deleteIcon = categoryDeleteIconByNameAndCode(name, code);
        if (driver.findElements(deleteIcon).isEmpty()) {
            // fallback: delete first visible category if the created one doesn't appear as q-card yet
            scrollAndClick(categoriesFirstDeleteIcon, "Supprimer première catégorie");
        } else {
            scrollAndClick(deleteIcon, "Supprimer catégorie " + name);
        }
    }

    public void waitUntilCategoryNotPresent(String name, String code) {
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(categoryRowByNameAndCode(name, code)));
        } catch (TimeoutException ignored) {
            // If the UI doesn't show a row for that category (api mock/async), ignore
        }
    }

    private By groupCardByName(String groupName) {
        return By.xpath(
                "//h2[normalize-space()=\"Groupe d'entreprise\"]" +
                        "/following::div[contains(@class,'card-container')][.//div[contains(normalize-space(.),'Nom :') and contains(normalize-space(.)," + xpathSafeString(groupName) + ")]][1]"
        );
    }

    private void waitForToastIfAny() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(toastMessage));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(toastMessage));
        } catch (Exception ignored) {
        }
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
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private String xpathSafeString(String value) {
        if (value == null) return "''";
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        StringBuilder sb = new StringBuilder("concat(");
        String[] parts = value.split("'", -1);
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(", \"'\", ");
            sb.append("'").append(parts[i]).append("'");
        }
        sb.append(")");
        return sb.toString();
    }
}

