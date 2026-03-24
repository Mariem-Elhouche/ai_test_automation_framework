package org.automation.pages;

import org.automation.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.Keys;

import java.util.List;
import java.util.stream.Collectors;

public class CompanyCategoryListPage extends BasePage {

    // =========================
    // Locators - Filters
    // =========================
    private By nameFilterInput = By.xpath("//input[@placeholder='Nom']");
    private By codeFilterInput = By.xpath("//input[@placeholder='Code']");
    private By companyFilterInput = By.xpath("(//div[contains(@class,'q-field') and .//i[contains(@class,'icon-search')]]//input)[3]");

    // =========================
    // Table rows and cells
    // =========================
    private By tableRows = By.xpath("//tbody/tr | //div[contains(@class,'q-table__grid-content')]//div[contains(@class,'q-card')]");
    private By nameCell = By.xpath(".//td[1]");
    private By codeCell = By.xpath(".//td[2] | .//div[contains(@class,'q-card')]//div[contains(@class,'code') or contains(@class,'q-td')]");
    private By companyCell = By.xpath(".//td[3]");

    // =========================
    // Empty table
    // =========================
    private By noDataRow = By.xpath(
            "//td[contains(@class,'q-table__bottom-nodata') or contains(.,'aucun') or contains(.,'Aucun')] " +
                    "| //div[contains(@class,'q-table__bottom') and contains(.,'aucun')]"
    );

    // =========================
    // Pagination
    // =========================
    private By activePage = By.xpath("//button[contains(@class,'q-btn') and (contains(@class,'bg-primary') or contains(@class,'q-btn--active'))]");

    public CompanyCategoryListPage() {
        super();
    }

    public void navigateToListPage() {
        driver.get("https://stg-bo.noveocare.com/entities/company-sections");
        wait.until(ExpectedConditions.visibilityOfElementLocated(nameFilterInput));
        wait.until(ExpectedConditions.presenceOfElementLocated(companyFilterInput));
        System.out.println("✅ Page liste des catégories chargée");
    }

    // =========================
    // Filters
    // =========================
    public void filterByName(String name) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(nameFilterInput));
        field.clear();
        field.sendKeys(name);
        field.sendKeys(Keys.ENTER);
        waitForTableRefresh();
    }

    public void filterByCode(String code) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(codeFilterInput));
        field.clear();
        field.sendKeys(code);
        field.sendKeys(Keys.ENTER);
        waitForTableRefresh();
    }

    public void filterByCompany(String company) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(companyFilterInput));
        field.clear();
        field.sendKeys(company);
        field.sendKeys(Keys.ENTER);
        waitForTableRefresh();
    }

    // =========================
    // Getters - table values
    // =========================
    public List<String> getDisplayedNames() {
        return getCellValues(nameCell);
    }

    public List<String> getDisplayedCodes() {
        return getCellValues(codeCell);
    }

    public List<String> getDisplayedCompanies() {
        return getCellValues(companyCell);
    }

    private List<String> getCellValues(By cellLocator) {
        waitForTableRefresh();
        List<WebElement> rows = driver.findElements(tableRows);
        return rows.stream()
                .map(row -> {
                    try {
                        WebElement cell = row.findElement(cellLocator);
                        return cell.getText().trim();
                    } catch (NoSuchElementException e) {
                        return "";
                    }
                })
                .filter(text -> !text.isEmpty())
                .collect(Collectors.toList());
    }

    // =========================
    // Table utilities
    // =========================
    public boolean isListEmpty() {
        waitForTableRefresh();
        return !driver.findElements(noDataRow).isEmpty() || driver.findElements(tableRows).isEmpty();
    }

    private void waitForTableRefresh() {
        int retry = 0;
        List<WebElement> previousRows = driver.findElements(tableRows);
        while (retry < 15) {
            try { Thread.sleep(300); } catch (InterruptedException ignored) {}
            List<WebElement> currentRows = driver.findElements(tableRows);
            if (currentRows.size() == previousRows.size()) break;
            previousRows = currentRows;
            retry++;
        }
    }

    // =========================
    // Pagination
    // =========================
    public void clickOnPage(int pageNumber) {
        By pageButton = By.xpath("//button[contains(@class,'q-btn') and .//span[normalize-space(text())='" + pageNumber + "']]");
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(pageButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
        btn.click();
        waitForTableRefresh();
    }

    public void clickOnLastPage() {
        By lastPageButton = By.xpath("(//button[contains(@class,'q-btn') and .//span[number(normalize-space(text())) = number(normalize-space(text()))]])[last()]");
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(lastPageButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
        btn.click();
        waitForTableRefresh();
    }

    public int getCurrentPage() {
        try {
            WebElement active = driver.findElement(activePage);
            return Integer.parseInt(active.getText().trim());
        } catch (Exception e) {
            return 1;
        }
    }

    public boolean isOnPage(int expectedPage) {
        return getCurrentPage() == expectedPage;
    }

    public boolean isLastPage() {
        return getCurrentPage() > 1;
    }
}