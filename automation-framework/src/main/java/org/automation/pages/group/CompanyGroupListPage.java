package org.automation.pages.group;

import org.automation.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class CompanyGroupListPage extends BasePage {

    private static final String LIST_URL = "https://stg-bo.noveocare.com/entities/company-groups";

    // Filters
    private final By nameFilterInput = By.xpath("//input[@placeholder='Nom']");
    private final By typeFilterTrigger = By.xpath("//label[contains(@class,'q-select')][.//span[normalize-space()='Type']]");
    private final By typeFilterMenu = By.xpath("//div[contains(@class,'q-menu')][.//*[@role='option']]");
    private final By typeValidateBtn = By.xpath("//div[contains(@class,'q-menu')]//button[.//span[normalize-space(text())='Valider']]");

    private final By environmentFilterTrigger = By.xpath("//label[contains(@class,'q-select')][.//span[contains(normalize-space(),'Environnement')]]");
    private final By environmentMenuSearchInput = By.xpath("//div[contains(@class,'q-menu')]//input[@placeholder='Rechercher']");
    private final By environmentNoContent = By.xpath("//div[contains(@class,'q-menu')]//div[contains(normalize-space(.),'Pas de contenu')]");
    private final By environmentValidateBtn = By.xpath("//div[contains(@class,'q-menu')]//button[.//span[normalize-space(text())='Valider']]");
    // Table
    private final By tableRows = By.xpath("//tbody/tr");
    private final By nameCell = By.xpath(".//td[1]");
    private final By typeCell = By.xpath(".//td[2]");
    private final By noDataRow = By.xpath("//div[contains(@class,'q-table__bottom--nodata')] | //span[contains(normalize-space(),'Aucun')]");

    // Pagination
    private final By activePage = By.xpath("//div[contains(@class,'q-table__bottom')]//button[contains(@class,'bg-primary') or contains(@class,'q-btn--active')]");
    private final By lastPageButton = By.xpath("//button[@aria-label='Next page']/preceding-sibling::button[1]");

    // Row actions
    private final By eyeIconFirstRow = By.xpath("(//tbody/tr)[1]//i[contains(@class,'icon-eye')]");
    private final By editIconFirstRow = By.xpath("(//tbody/tr)[1]//i[normalize-space()='edit']");
    private final By deleteIconFirstRow = By.xpath("(//tbody/tr)[1]//i[normalize-space()='delete']");

    // Create/Edit drawer
    private final By createGroupButton = By.xpath("//button[.//span[contains(normalize-space(),'Ajouter un groupe')]]");
    private final By rightDrawer = By.xpath("//aside[contains(@class,'q-drawer--right')]");
    private final By drawerNameInput = By.xpath("//aside[contains(@class,'q-drawer--right')]//input[@placeholder='Nom']");
    private final By drawerTypeTrigger = By.xpath("//aside[contains(@class,'q-drawer--right')]//label[contains(@class,'q-select')][.//span[normalize-space()='Type']]");
    private final By drawerEnvironmentTrigger = By.xpath("//aside[contains(@class,'q-drawer--right')]//label[contains(@class,'q-select')][.//span[contains(normalize-space(),'Environnement')]]");
    private final By drawerSaveButton = By.xpath("//aside[contains(@class,'q-drawer--right')]//button[.//span[contains(normalize-space(),'Enregistrer')]]");
    private final By detailsAccessButton = By.xpath("//aside[contains(@class,'q-drawer--right')]//button[.//span[contains(normalize-space(),'Accéder au groupe') or contains(normalize-space(),'Acceder au groupe')]]");
    private final By previousPageButton = By.xpath("//button[@aria-label='Previous page']");
    private final By nextPageButton = By.xpath("//button[@aria-label='Next page']");
    // Delete modal
    private final By deleteConfirmDialog = By.xpath("//div[@role='dialog' and .//span[contains(.,'irr') or contains(.,'supprimer')]]");
    private final By deleteConfirmButton = By.xpath("//button[.//span[contains(normalize-space(text()),'Confirmer')]]");
    private final By deleteCancelButton = By.xpath("//button[.//span[contains(normalize-space(text()),'Ne pas supprimer') or contains(normalize-space(text()),'Annuler')]]");

    // Toast
    private final By successToast = By.xpath("//div[contains(@class,'q-notification__message')] | //div[contains(@class,'q-notification')]");

    public CompanyGroupListPage() {
        super();
    }

    public void navigateToListPage() {
        driver.get(LIST_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(nameFilterInput));
    }

    public void filterByName(String name) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(nameFilterInput));
        field.click();
        field.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        field.sendKeys(name, Keys.ENTER);
        waitForTableStable();
    }

    public void filterByType(String type) {
        WebElement trigger = wait.until(ExpectedConditions.elementToBeClickable(typeFilterTrigger));
        trigger.click();
        trigger.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class,'q-menu')]//*[@role='option']")
        ));
        clickVisibleOption(type);
        wait.until(ExpectedConditions.elementToBeClickable(typeValidateBtn)).click();
        waitForTableStable();
    }

    public void filterByEnvironment(String environment) {
        WebElement trigger = wait.until(ExpectedConditions.elementToBeClickable(environmentFilterTrigger));
        trigger.click();
        trigger.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(environmentMenuSearchInput));

        WebElement searchInput = driver.findElement(environmentMenuSearchInput);
        searchInput.clear();
        searchInput.sendKeys(environment);

        By optionLocator = By.xpath(
                "//div[contains(@class,'q-menu')]//div[@role='option']" +
                        "[.//span[normalize-space(text())='" + environment + "']]"
        );
        wait.until(ExpectedConditions.visibilityOfElementLocated(optionLocator));
        driver.findElement(optionLocator).click();

        wait.until(ExpectedConditions.elementToBeClickable(environmentValidateBtn)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(environmentMenuSearchInput));
        waitForTableStable();
    }

    public List<String> getDisplayedNames() {
        waitForTableStable();
        return driver.findElements(tableRows).stream()
                .map(row -> getCellText(row, nameCell))
                .filter(text -> !text.isEmpty())
                .collect(Collectors.toList());
    }

    public List<String> getDisplayedTypes() {
        waitForTableStable();
        return driver.findElements(tableRows).stream()
                .map(row -> getCellText(row, typeCell))
                .filter(text -> !text.isEmpty())
                .collect(Collectors.toList());
    }



    public boolean isListEmpty() {
        waitForTableStable();
        return !driver.findElements(noDataRow).isEmpty() || driver.findElements(tableRows).isEmpty();
    }

    public void clickOnPage(int pageNumber) {
        By pageBtn = By.xpath("//div[contains(@class,'q-table__bottom')]//button[.//span[normalize-space(text())='" + pageNumber + "']]");
        wait.until(ExpectedConditions.elementToBeClickable(pageBtn)).click();
        waitForTableStable();
    }

    public void clickOnLastPage() {
        wait.until(ExpectedConditions.elementToBeClickable(lastPageButton)).click();
        waitForTableStable();
    }

    public int getCurrentPage() {
        try {
            return Integer.parseInt(wait.until(ExpectedConditions.visibilityOfElementLocated(activePage)).getText().trim());
        } catch (Exception e) {
            return 1;
        }
    }

    public boolean isLastPage() {
        return getCurrentPage() > 1;
    }

    public void clickCreateCompanyGroup() {
        wait.until(ExpectedConditions.elementToBeClickable(createGroupButton)).click();
        // Attendre la page formulaire
        wait.until(ExpectedConditions.urlContains("company-groups/new"));
    }


    public void clickEyeOnFirstRow() {
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(tableRows, 0));
        wait.until(ExpectedConditions.elementToBeClickable(eyeIconFirstRow)).click();
    }

    public void clickEditOnFirstRow() {
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(tableRows, 0));
        wait.until(ExpectedConditions.elementToBeClickable(editIconFirstRow)).click();
        // Attendre la page formulaire d'édition
        wait.until(ExpectedConditions.urlContains("company-groups/"));
    }

    public void editByName(String name) {
        filterByName(name);
        clickEditOnFirstRow();
    }

    public void setDrawerEnvironment(String environment) {
        selectDrawerOption(drawerEnvironmentTrigger, environment);
    }

    public void clickNextPage() {
        wait.until(ExpectedConditions.elementToBeClickable(nextPageButton)).click();
        waitForTableStable();
    }

    public void clickPreviousPage() {
        wait.until(ExpectedConditions.elementToBeClickable(previousPageButton)).click();
        waitForTableStable();
    }

    public boolean isNextPageButtonDisabled() {
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(nextPageButton));
        return btn.getAttribute("disabled") != null || btn.getAttribute("class").contains("disabled");
    }

    public boolean isPreviousPageButtonDisabled() {
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(previousPageButton));
        return btn.getAttribute("disabled") != null || btn.getAttribute("class").contains("disabled");
    }
    public void saveDrawer() {
        wait.until(ExpectedConditions.elementToBeClickable(drawerSaveButton)).click();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(successToast),
                ExpectedConditions.invisibilityOfElementLocated(drawerSaveButton)
        ));
        waitForTableStable();
    }

    public void clickDeleteOnFirstRow() {
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(tableRows, 0));
        wait.until(ExpectedConditions.elementToBeClickable(deleteIconFirstRow)).click();
    }

    public boolean isDeleteConfirmDialogDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(deleteConfirmDialog));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void confirmDeletion() {
        wait.until(ExpectedConditions.elementToBeClickable(deleteConfirmButton)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(deleteConfirmDialog));
        waitForTableStable();
    }

    public void cancelDeletion() {
        wait.until(ExpectedConditions.elementToBeClickable(deleteCancelButton)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(deleteConfirmDialog));
        waitForTableStable();
    }

    public String getToastMessage() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(successToast)).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isGroupPresentInList(String groupName) {
        navigateToListPage();
        filterByName(groupName);
        return getDisplayedNames().stream()
                .anyMatch(n -> n.toUpperCase().contains(groupName.toUpperCase()));
    }

    public boolean isGroupAbsentInList(String groupName) {
        navigateToListPage();
        filterByName(groupName);
        return isListEmpty();
    }

    private String getCellText(WebElement row, By cellLocator) {
        try {
            return row.findElement(cellLocator).getText().trim();
        } catch (StaleElementReferenceException | NoSuchElementException e) {
            return "";
        }
    }

    private void selectFilterOption(By trigger, String optionText) {
        wait.until(ExpectedConditions.elementToBeClickable(trigger)).click();
        clickVisibleOption(optionText);
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
    }

    private void selectDrawerOption(By trigger, String optionText) {
        wait.until(ExpectedConditions.elementToBeClickable(trigger)).click();
        clickVisibleOption(optionText);
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
    }

    private void clickVisibleOption(String optionText) {
        By option = By.xpath(
                "//div[contains(@class,'q-menu')]//*[self::div[@role='option'] or contains(@class,'q-item')]" +
                        "[contains(normalize-space(.)," + xpathSafeString(optionText) + ")]"
        );
        wait.until(ExpectedConditions.elementToBeClickable(option)).click();
    }

    private String xpathSafeString(String value) {
        if (!value.contains("'")) return "'" + value + "'";
        StringBuilder sb = new StringBuilder("concat(");
        String[] parts = value.split("'", -1);
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(", \"'\", ");
            sb.append("'").append(parts[i]).append("'");
        }
        sb.append(")");
        return sb.toString();
    }

    private void waitForTableStable() {
        int sameCount = 0;
        int previousCount = -1;
        for (int i = 0; i < 20; i++) {
            sleep(250);
            int currentCount = driver.findElements(tableRows).size();
            if (currentCount == previousCount) {
                sameCount++;
                if (sameCount >= 2) return;
            } else {
                sameCount = 0;
            }
            previousCount = currentCount;
        }

        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(tableRows),
                    ExpectedConditions.presenceOfElementLocated(noDataRow)
            ));
        } catch (TimeoutException ignored) {
        }
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
