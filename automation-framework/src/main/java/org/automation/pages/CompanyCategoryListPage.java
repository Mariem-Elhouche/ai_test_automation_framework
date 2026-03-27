package org.automation.pages;

import org.automation.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class CompanyCategoryListPage extends BasePage {

    // Locators - Row actions
    private final By editIconInRow   = By.xpath(".//i[text()='edit']");
    private final By deleteIconInRow = By.xpath(".//i[text()='delete']");
    // Locators - Filters
    private final By nameFilterInput    = By.xpath("//input[@placeholder='Nom']");
    private final By codeFilterInput    = By.xpath("//input[@placeholder='Code']");
    private final By companyFilterInput = By.xpath(
            "(//div[contains(@class,'q-field') and .//i[contains(@class,'icon-search')]]//input)[3]"
    );
    // Locators - Clear icons
    private final By clearNameIcon    = By.xpath("(//i[contains(@class,'icon-x') and contains(@class,'cursor-pointer')])[1]");
    private final By clearCodeIcon    = By.xpath("(//i[contains(@class,'icon-x') and contains(@class,'cursor-pointer')])[2]");
    private final By clearCompanyIcon = By.xpath("(//i[contains(@class,'icon-x') and contains(@class,'cursor-pointer')])[3]");
    // Locators - Table
    private final By tableRows       = By.xpath("//tbody/tr");
    private final By allNameCells    = By.xpath("//tbody/tr/td[1]");
    private final By allCodeCells    = By.xpath("//tbody/tr/td[2]");
    private final By allCompanyCells = By.xpath("//tbody/tr/td[3]");
    // Locators - Empty row after filter
    private final By noDataRow = By.xpath("//span[@class='text-weight-semi-bold text-subtitle2']");
    // Locators - Delete modal
    private final By deleteConfirmDialog = By.xpath("//div[@role='dialog' and .//span[contains(.,'irréversible')]]");
    private final By deleteConfirmButton = By.xpath("//button[.//span[contains(text(),'Confirmer la suppression')]]");
    private final By deleteCancelButton = By.xpath("//button[.//span[contains(text(),'Ne pas supprimer')]]");
    // Locators - Toast
    private final By successToast = By.xpath("//div[contains(@class,'q-notification')]");
    // Locators - Pagination
    private final By activePage = By.xpath("//button[contains(@class,'q-btn') and " +
            "(contains(@class,'bg-primary') or contains(@class,'q-btn--active'))]");
    private final By lastPageButton = By.xpath("//button[@aria-label='Next page']/preceding-sibling::button[1]");



    //*********************************//
    //     METHODS
    //*********************************//

    //constructor
    public CompanyCategoryListPage() {
        super();
    }

    // Navigation
    public void navigateToListPage() {
        driver.get("https://stg-bo.noveocare.com/entities/company-sections");
        wait.until(ExpectedConditions.visibilityOfElementLocated(nameFilterInput));
    }

    // Filters

    private void applyFilter(By locator, By clearIcon, String value) {
        List<WebElement> clears = driver.findElements(clearIcon);
        if (!clears.isEmpty()) clears.get(0).click();
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        // Capturer une ligne existante AVANT filtrage pour détecter le changement
        List<WebElement> rowsBefore = driver.findElements(tableRows);
        field.sendKeys(value, Keys.ENTER);

        if (!rowsBefore.isEmpty()) {
            wait.until(ExpectedConditions.stalenessOf(rowsBefore.get(0)));
        }
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(allNameCells),
                ExpectedConditions.visibilityOfElementLocated(noDataRow)
        ));
    }

    public void filterByName(String name)       { applyFilter(nameFilterInput,    clearNameIcon,    name); }
    public void filterByCode(String code)       { applyFilter(codeFilterInput,    clearCodeIcon,    code); }
    public void filterByCompany(String company) { applyFilter(companyFilterInput, clearCompanyIcon, company); }

    // Table - cell values
    private List<String> getCellValues(By cellLocator) {
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(cellLocator));
        // Re-fetcher après le wait pour éviter le stale
        return driver.findElements(cellLocator).stream()
                .map(WebElement::getText)
                .map(String::trim)
                .filter(text -> !text.isEmpty())
                .collect(Collectors.toList());
    }

    public List<String> getDisplayedNames()     { return getCellValues(allNameCells); }
    public List<String> getDisplayedCodes()     { return getCellValues(allCodeCells); }
    public List<String> getDisplayedCompanies() { return getCellValues(allCompanyCells); }

    public void clearAllFilters() {
        clearFilter(clearNameIcon);
        clearFilter(clearCodeIcon);
        clearFilter(clearCompanyIcon);

        // Attendre le refresh de la table
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(allNameCells),
                ExpectedConditions.visibilityOfElementLocated(noDataRow)
        ));
    }

    private void clearFilter(By clearIcon) {
        List<WebElement> icons = driver.findElements(clearIcon);
        if (!icons.isEmpty() && icons.get(0).isDisplayed()) {
            icons.get(0).click();
        }
    }

    public boolean isListEmpty() {
            return !driver.findElements(noDataRow).isEmpty();
        }

    // Pagination

    public void clickOnPage(int pageNumber) throws InterruptedException {
        By btn = By.xpath(
                "//button[contains(@class,'q-btn') and .//span[normalize-space(text())='" + pageNumber + "']]"
        );
        // Attendre que le bouton soit cliquable
        WebElement button = wait.withTimeout(Duration.ofSeconds(10)).until(ExpectedConditions.elementToBeClickable(btn));
        button.click();
        wait.withTimeout(Duration.ofSeconds(50)).until(ExpectedConditions.presenceOfElementLocated(tableRows));
    }


    public void clickOnLastPage() {

        WebElement button = wait.withTimeout(Duration.ofSeconds(10)).until(ExpectedConditions.elementToBeClickable(lastPageButton));

        button.click();
        wait.withTimeout(Duration.ofSeconds(60)).until(ExpectedConditions.presenceOfElementLocated(tableRows));
    }

    public int getCurrentPage() {
        return Integer.parseInt(
                wait.until(ExpectedConditions.visibilityOfElementLocated(activePage)).getText().trim()
        );
    }


    // click : Edit / Delete

    public void clickEditOnFirstRow() {
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(tableRows, 0));
        driver.findElements(tableRows).get(0).findElement(editIconInRow).click();
    }

    public void clickDeleteOnFirstRow() {
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(tableRows, 0));
        WebElement deleteBtn = driver.findElements(tableRows).get(0).findElement(deleteIconInRow);
        wait.until(ExpectedConditions.elementToBeClickable(deleteBtn)).click();
    }

    // Delete modal

    public boolean isDeleteConfirmDialogDisplayed() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(deleteConfirmDialog));
        return true;
    }

    public void confirmDeletion() {
        wait.until(ExpectedConditions.elementToBeClickable(deleteConfirmButton)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(deleteConfirmDialog));
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(tableRows));
    }

    public void cancelDeletion() {
        wait.until(ExpectedConditions.elementToBeClickable(deleteCancelButton)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(deleteConfirmDialog));
    }

    public String getDeletionSuccessMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(successToast)).getText().trim();
    }

    // delete / cancel checks

    public boolean isCategoryAbsentAfterFilter(String name) {
        navigateToListPage();
        filterByName(name);
        return isListEmpty();
    }

    public boolean isCategoryPresentAfterFilter(String name) {
        List<String> names = getDisplayedNames();
        if (names.isEmpty()) {
            navigateToListPage();
            filterByName(name);
            names = getDisplayedNames();
        }
        return names.stream().anyMatch(n -> n.equalsIgnoreCase(name));
    }
}