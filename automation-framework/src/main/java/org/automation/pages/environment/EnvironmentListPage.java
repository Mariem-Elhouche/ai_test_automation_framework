package org.automation.pages.environment;

import org.automation.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.util.List;
import java.util.stream.Collectors;

public class EnvironmentListPage extends BasePage {

    private static final String LIST_URL = "https://stg-bo.noveocare.com/entities/environments";

    // Locators — Filtres
    private final By nameFilterInput       = By.xpath("//input[@placeholder='Nom']");
    private final By siteDeGestionInput    = By.xpath(
            "//div[contains(@class,'q-field') and .//input[@role='combobox']]//input[@role='combobox']"
    );

    // Locators — Table
    private final By tableRows       = By.xpath("//tbody/tr");
    private final By nameCell        = By.xpath(".//td[1]");
    private final By siteCell        = By.xpath(".//td[2]");
    private final By editIconInRow   = By.xpath(".//i[text()='edit']");
    private final By deleteIconInRow = By.xpath(".//i[text()='delete']");

    // Locators — Empty state
    private final By noDataRow = By.xpath(
            "//span[@class='text-weight-semi-bold text-subtitle2']"
    );

    // Locators — Pagination
    private final By activePage     = By.xpath(
            "//button[contains(@class,'bg-primary') or contains(@class,'q-btn--active')]"
    );
    private final By lastPageButton = By.xpath(
            "//button[@aria-label='Next page']/preceding-sibling::button[1]"
    );
    private final By nextPageButton  = By.xpath("//button[@aria-label='Next page']");
    private final By prevPageButton  = By.xpath("//button[@aria-label='Previous page']");

    // Locators — Bouton créer
    private final By createButton = By.xpath(
            "//button[.//span[contains(normalize-space(),'Environnement')]]"
    );

    // Locators — Delete modal
    private final By deleteConfirmDialog = By.xpath(
            "//div[@role='dialog' and .//span[contains(.,'irréversible')]]"
    );
    private final By deleteConfirmButton = By.xpath(
            "//button[.//span[contains(text(),'Confirmer la suppression')]]"
    );
    private final By deleteCancelButton  = By.xpath(
            "//button[.//span[contains(text(),'Ne pas supprimer')]]"
    );

    // Locators — Toast
    private final By successToast = By.xpath(
            "//div[contains(@class,'q-notification__message')]"
    );

    public EnvironmentListPage() { super(); }

    // Navigation
    public void navigateToListPage() {
        driver.get(LIST_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(nameFilterInput));
        System.out.println("Page liste des environnements chargée");
    }

    // Filtres
    public void filterByName(String name) {
        WebElement field = wait.until(
                ExpectedConditions.visibilityOfElementLocated(nameFilterInput)
        );
        field.click();
        field.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        field.sendKeys(name, Keys.ENTER);
        waitForTableStable();
        System.out.println("Filtre Nom appliqué : " + name);
    }

    public void filterBySiteDeGestion(String site) {
        // Comportement : cliquer, taper, cliquer sur la suggestion
        WebElement field = wait.until(
                ExpectedConditions.elementToBeClickable(siteDeGestionInput)
        );
        field.click();
        field.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        field.sendKeys(site);

        By suggestion = By.xpath(
                "//div[contains(@class,'q-menu') or contains(@class,'q-virtual-scroll')]" +
                        "//div[contains(@class,'q-item')][contains(normalize-space(),'" + site + "')]"
        );
        wait.until(ExpectedConditions.elementToBeClickable(suggestion)).click();
        waitForTableStable();
        System.out.println("Filtre Site de gestion appliqué : " + site);
    }

    // Table data
    public List<String> getDisplayedNames() {
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(tableRows));
        return driver.findElements(tableRows).stream()
                .map(row -> {
                    try { return row.findElement(nameCell).getText().trim(); }
                    catch (StaleElementReferenceException | NoSuchElementException e) { return ""; }
                })
                .filter(t -> !t.isEmpty())
                .collect(Collectors.toList());
    }

    public List<String> getDisplayedSites() {
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(tableRows));
        return driver.findElements(tableRows).stream()
                .map(row -> {
                    try { return row.findElement(siteCell).getText().trim(); }
                    catch (StaleElementReferenceException | NoSuchElementException e) { return ""; }
                })
                .filter(t -> !t.isEmpty())
                .collect(Collectors.toList());
    }

    public boolean isListEmpty() {
        waitForTableStable();
        return !driver.findElements(noDataRow).isEmpty()
                || driver.findElements(tableRows).isEmpty();
    }

    // Pagination
    public void clickOnPage(int pageNumber) {
        By btn = By.xpath(
                "//button[.//span[normalize-space(text())='" + pageNumber + "']]"
        );
        wait.until(ExpectedConditions.elementToBeClickable(btn)).click();
        waitForTableStable();
        System.out.println("Navigation vers la page " + pageNumber);
    }

    public void clickOnLastPage() {
        wait.until(ExpectedConditions.elementToBeClickable(lastPageButton)).click();
        waitForTableStable();
        System.out.println("Dernière page atteinte");
    }

    public void clickNextPage() {
        wait.until(ExpectedConditions.elementToBeClickable(nextPageButton)).click();
        waitForTableStable();
        System.out.println("Page suivante");
    }

    public void clickPreviousPage() {
        wait.until(ExpectedConditions.elementToBeClickable(prevPageButton)).click();
        waitForTableStable();
        System.out.println("Page précédente");
    }

    public int getCurrentPage() {
        try {
            return Integer.parseInt(
                    wait.until(ExpectedConditions.visibilityOfElementLocated(activePage))
                            .getText().trim()
            );
        } catch (Exception e) { return 1; }
    }

    public boolean isLastPage() { return getCurrentPage() > 1; }

    // Actions
    public void clickCreateEnvironment() {
        wait.until(ExpectedConditions.elementToBeClickable(createButton)).click();
    }

    public void clickEditOnFirstRow() {
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(tableRows, 0));
        WebElement icon = driver.findElements(tableRows).get(0).findElement(editIconInRow);
        wait.until(ExpectedConditions.elementToBeClickable(icon)).click();
    }

    public void clickDeleteOnFirstRow() {
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(tableRows, 0));
        WebElement icon = driver.findElements(tableRows).get(0).findElement(deleteIconInRow);
        wait.until(ExpectedConditions.elementToBeClickable(icon)).click();
    }

    // Delete modal
    public boolean isDeleteConfirmDialogDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(deleteConfirmDialog));
            return true;
        } catch (Exception e) { return false; }
    }

    public void confirmDeletion() {
        wait.until(ExpectedConditions.elementToBeClickable(deleteConfirmButton)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(deleteConfirmDialog));
        waitForTableStable();
    }

    public void cancelDeletion() {
        wait.until(ExpectedConditions.elementToBeClickable(deleteCancelButton)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(deleteConfirmDialog));
    }

    public String getToastMessage() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(successToast))
                    .getText().trim();
        } catch (Exception e) { return ""; }
    }

    // Présence / Absence
    public boolean isEnvironmentPresentInList(String name) {
        navigateToListPage();
        filterByName(name);
        return getDisplayedNames().stream()
                .anyMatch(n -> n.toUpperCase().contains(name.toUpperCase()));
    }

    public boolean isEnvironmentAbsentAfterFilter(String name) {
        navigateToListPage();
        filterByName(name);
        return isListEmpty();
    }

    public boolean isEnvironmentPresentAfterFilter(String name) {
        return getDisplayedNames().stream()
                .anyMatch(n -> n.toUpperCase().contains(name.toUpperCase()));
    }

    // Utilitaires
    private void waitForTableStable() {
        int prev = -1, stable = 0;
        for (int i = 0; i < 20; i++) {
            sleep(300);
            int curr = driver.findElements(tableRows).size();
            if (curr == prev) { if (++stable >= 2) break; }
            else stable = 0;
            prev = curr;
        }
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}