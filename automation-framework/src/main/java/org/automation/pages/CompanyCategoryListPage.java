package org.automation.pages;

import org.automation.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;
import java.util.stream.Collectors;

public class CompanyCategoryListPage extends BasePage {

    // =========================
    // Locators - Filters
    // =========================
    private By nameFilterInput = By.xpath(
            "//input[@placeholder='Nom']"
    );
    private By codeFilterInput = By.xpath(
            "//input[@placeholder='Code']"
    );
    private By companyFilterInput = By.xpath(
            "//input[contains(@placeholder,'Entreprise')]"
    );

    // =========================
    // Locators - Table rows
    // =========================
    private By tableRows = By.xpath(
            "//tbody/tr | //div[contains(@class,'q-table__grid-content')]//div[contains(@class,'q-card')]"
    );
    private By nameCell    = By.xpath("(.//td)[1]");
    private By codeCell    = By.xpath("(.//td)[2]");
    private By companyCell = By.xpath("(.//td)[3]");

    // =========================
    // Locators - Empty state
    // =========================
    private By noDataRow = By.xpath(
            "//td[contains(@class,'q-table__bottom-nodata') or contains(.,'aucun') or contains(.,'Aucun')]" +
                    " | //div[contains(@class,'q-table__bottom') and contains(.,'aucun')]"
    );

    // =========================
    // Locators - Pagination
    // =========================
    private By activePage = By.xpath(
            "//button[contains(@class,'q-btn') and " +
                    "(contains(@class,'bg-primary') or contains(@class,'q-btn--active'))]"
    );

    public CompanyCategoryListPage() {
        super();
    }

    public void navigateToListPage() {
        driver.get("https://stg-bo.noveocare.com/entities/company-sections");
        // Attendre que les filtres soient visibles
        wait.until(ExpectedConditions.visibilityOfElementLocated(nameFilterInput));
        System.out.println("✅ Page liste des catégories chargée");
    }

    // =========================
    // Filter methods
    // =========================
    public void filterByName(String name) {
        System.out.println("Filtrage par nom : " + name);
        WebElement field = wait.until(
                ExpectedConditions.visibilityOfElementLocated(nameFilterInput)
        );
        field.clear();
        field.sendKeys(name);
        waitForTableRefresh();
    }

    public void filterByCode(String code) {
        System.out.println("Filtrage par code : " + code);
        WebElement field = wait.until(
                ExpectedConditions.visibilityOfElementLocated(codeFilterInput)
        );
        field.clear();
        field.sendKeys(code);
        waitForTableRefresh();
    }

    public void filterByCompany(String company) {
        System.out.println("Filtrage par entreprise liée : " + company);
        WebElement field = wait.until(
                ExpectedConditions.visibilityOfElementLocated(companyFilterInput)
        );
        field.clear();
        field.sendKeys(company);
        waitForTableRefresh();
    }

    // =========================
    // Table reading methods
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
        List<WebElement> rows = driver.findElements(tableRows);
        return rows.stream()
                .map(row -> {
                    try {
                        return row.findElement(cellLocator).getText().trim();
                    } catch (Exception e) {
                        return "";
                    }
                })
                .filter(text -> !text.isEmpty())
                .collect(Collectors.toList());
    }

    public boolean isListEmpty() {
        waitForTableRefresh();
        // Vérifier présence d'un message "aucun résultat"
        if (!driver.findElements(noDataRow).isEmpty()) {
            return true;
        }
        // Ou vérifier que le tableau ne contient aucune ligne de données
        return driver.findElements(tableRows).isEmpty();
    }

    // =========================
    // Pagination methods
    // =========================
    public void clickOnPage(int pageNumber) {
        System.out.println("Navigation vers la page : " + pageNumber);
        By pageButton = By.xpath(
                "//button[contains(@class,'q-btn') and " +
                        ".//span[normalize-space(text())='" + pageNumber + "']]"
        );
        WebElement btn = wait.until(
                ExpectedConditions.elementToBeClickable(pageButton)
        );
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", btn
        );
        btn.click();
        waitForTableRefresh();
    }

    public void clickOnLastPage() {
        System.out.println("Navigation vers la dernière page...");
        // La dernière page est le dernier bouton numérique de la pagination
        By lastPageButton = By.xpath(
                "(//button[contains(@class,'q-btn') and " +
                        ".//span[number(normalize-space(text())) = number(normalize-space(text()))]])" +
                        "[last()]"
        );
        WebElement btn = wait.until(
                ExpectedConditions.elementToBeClickable(lastPageButton)
        );
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", btn
        );
        btn.click();
        waitForTableRefresh();
    }

    public int getCurrentPage() {
        try {
            WebElement active = driver.findElement(activePage);
            return Integer.parseInt(active.getText().trim());
        } catch (Exception e) {
            System.out.println("⚠️ Page active non détectée, retour page 1 par défaut");
            return 1;
        }
    }

    public boolean isOnPage(int expectedPage) {
        int current = getCurrentPage();
        System.out.println("Page actuelle : " + current + " | Page attendue : " + expectedPage);
        return current == expectedPage;
    }

    public boolean isLastPage() {
        int current = getCurrentPage();
        System.out.println("Page actuelle (dernière) : " + current);
        return current > 1;
    }

    // =========================
    // Utility
    // =========================
    private void waitForTableRefresh() {
        try { Thread.sleep(800); } catch (InterruptedException ignored) {}
    }
}