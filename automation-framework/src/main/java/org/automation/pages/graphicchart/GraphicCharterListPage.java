package org.automation.pages.graphicchart;

import org.automation.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GraphicCharterListPage extends BasePage {

    private static final String LIST_URL = "https://stg-bo.noveocare.com/general/graphical-charter";

    // ── Filters ───────────────────────────────────────────────────────────────
    private final By nameFilterInput = By.xpath("//input[@placeholder='Nom']");

    // ── Table ─────────────────────────────────────────────────────────────────
    private final By tableRows = By.xpath("//tbody/tr");
    private final By nameCell  = By.xpath(".//td[1]");
    private final By noDataRow = By.xpath("//div[contains(@class,'q-table__bottom--nodata')]");

    // ── Pagination ────────────────────────────────────────────────────────────
    private final By activePage = By.xpath(
            "//div[contains(@class,'q-table__bottom')]" +
                    "//button[contains(@class,'bg-primary') or contains(@class,'q-btn--active')]"
    );
    private final By lastNumberedBtn = By.xpath(
            "(//div[contains(@class,'q-table__bottom')]" +
                    "//button[.//span[number(normalize-space(text())) = " +
                    "                 number(normalize-space(text()))]])[last()]"
    );


    private final By nextPageBtn = By.xpath(
            "//button[@aria-label='Next page' and not(@disabled)]"
    );
    private final By prevPageBtn = By.xpath(
            "//button[@aria-label='Previous page' and not(@disabled)]"
    );

    // ── Row actions ───────────────────────────────────────────────────────────
    // The "Nouvelle charte graphique" button in the page header
    private final By createButton = By.xpath(
            "//button[.//span[contains(normalize-space(.),'charte graphique')] or " +
                    "         .//span[contains(normalize-space(.),'Nouvelle charte')]]"
    );
    private final By viewIconFirstRow   = By.xpath("(//tbody/tr)[1]//*[contains(@class,'icon-eye')]");
    private final By editIconFirstRow   = By.xpath("(//tbody/tr)[1]//i[text()='edit']");
    private final By deleteIconFirstRow = By.xpath("(//tbody/tr)[1]//i[text()='delete']");

    // ── Delete confirmation dialog ────────────────────────────────────────────
    private final By deleteConfirmDialog = By.xpath(
            "//div[@role='dialog' and .//span[contains(.,'irréversible')]]"
    );
    private final By deleteConfirmButton = By.xpath(
            "//button[.//span[contains(normalize-space(text()),'Confirmer la suppression')]]"
    );
    private final By deleteCancelButton = By.xpath(
            "//button[.//span[contains(normalize-space(text()),'Ne pas supprimer')]]"
    );

    // ── Toast ─────────────────────────────────────────────────────────────────
    private final By successToast = By.xpath("//div[contains(@class,'q-notification')]");

    // =========================================================================
    // Constructor
    // =========================================================================
    public GraphicCharterListPage() { super(); }

    // =========================================================================
    // Navigation
    // =========================================================================
    public void navigateToListPage() {
        driver.get(LIST_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(nameFilterInput));
        System.out.println("Page liste Chartes Graphiques chargée");
    }


    // =========================================================================
    // Internal helpers
    // =========================================================================
    private void clearAndType(WebElement field, String value) {
        field.click();
        field.sendKeys(Keys.CONTROL + "a");
        field.sendKeys(Keys.DELETE);
        field.sendKeys(value);
    }

    private void scrollToCenter(WebElement element) {
        new Actions(driver).moveToElement(element).perform();
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    /**
     * Waits until the table has rows OR shows the "no data" placeholder,
     * then waits for the content to stop changing (Quasar re-renders after filter).
     */
    private void waitForTableStable() {
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(d ->
                !d.findElements(tableRows).isEmpty()
                        || !d.findElements(noDataRow).isEmpty()
        );
        if (driver.findElements(noDataRow).isEmpty()) {
            String previousText = "";
            int stableCount = 0;
            for (int retry = 0; retry < 12; retry++) {
                sleep(200);
                try {
                    String currentText = driver.findElements(tableRows).stream()
                            .map(r -> {
                                try { return r.getText(); }
                                catch (StaleElementReferenceException e) { return "STALE"; }
                            })
                            .collect(Collectors.joining("|"));
                    if (!currentText.contains("STALE") && currentText.equals(previousText) && !currentText.isEmpty()) {
                        if (++stableCount >= 2) break;
                    } else {
                        stableCount = 0;
                    }
                    previousText = currentText;
                } catch (NoSuchElementException ignored) {
                    stableCount = 0;
                }
            }
        }
    }

    private List<String> getCellValues(By cellLocator) {
        waitForTableStable();
        return wait.until(d -> {
            List<WebElement> rows = d.findElements(tableRows);
            if (rows.isEmpty()) return new ArrayList<>();
            List<String> values = rows.stream()
                    .map(row -> {
                        try { return row.findElement(cellLocator).getText().trim(); }
                        catch (StaleElementReferenceException | NoSuchElementException e) { return null; }
                    })
                    .toList();
            // null signals DOM instability — FluentWait will retry
            return values.contains(null) ? null : values.stream().filter(t -> !t.isEmpty()).toList();
        });
    }

    // =========================================================================
    // Filters
    // =========================================================================
    public void filterByName(String name) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(nameFilterInput));
        clearAndType(field, name);
        field.sendKeys(Keys.ENTER);
        waitForTableStable();
        System.out.println("Filtre Nom appliqué : " + name);
    }

    // =========================================================================
    // Data accessors
    // =========================================================================
    public List<String> getDisplayedNames() { return getCellValues(nameCell); }

    public boolean isListEmpty() {
        waitForTableStable();
        return !driver.findElements(noDataRow).isEmpty() || driver.findElements(tableRows).isEmpty();
    }

    // =========================================================================
    // Pagination
    // =========================================================================
    public void clickOnPage(int pageNumber) {
        By pageBtn = By.xpath(
                "//div[contains(@class,'q-table__bottom')]" +
                        "//button[.//span[normalize-space(text())='" + pageNumber + "']]"
        );
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(pageBtn));
        scrollToCenter(btn);
        btn.click();
        waitForTableStable();
        System.out.println("Navigation vers la page " + pageNumber);
    }

    public void clickOnLastPage() {
        WebElement lastBtn = wait.until(ExpectedConditions.elementToBeClickable(lastNumberedBtn));
        scrollToCenter(lastBtn);
        lastBtn.click();
        waitForTableStable();
        System.out.println("Dernière page atteinte");
    }

    public int getCurrentPage() {
        try {
            return Integer.parseInt(
                    wait.until(ExpectedConditions.presenceOfElementLocated(activePage)).getText().trim()
            );
        } catch (Exception e) { return 1; }
    }

    public boolean isOnPage(int expectedPage) {
        int current = getCurrentPage();
        System.out.println("Page courante : " + current + " | Attendue : " + expectedPage);
        return current == expectedPage;
    }

    public boolean isLastPage() { return getCurrentPage() > 1; }


    public void clickNextPage() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(nextPageBtn));
        scrollToCenter(btn);
        btn.click();
        waitForTableStable();
        System.out.println("Clic bouton page suivante");
    }

    public void clickPreviousPage() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(prevPageBtn));
        scrollToCenter(btn);
        btn.click();
        waitForTableStable();
        System.out.println("Clic bouton page précédente");
    }
    // =========================================================================
    // Row-level actions
    // =========================================================================
    public void clickViewOnFirstRow() {
        waitForTableStable();
        wait.until(ExpectedConditions.elementToBeClickable(viewIconFirstRow)).click();
        System.out.println("Clic icône œil (vue)");
    }

    public void clickEditOnFirstRow() {
        waitForTableStable();
        wait.until(ExpectedConditions.elementToBeClickable(editIconFirstRow)).click();
        System.out.println("Clic icône edit");
    }

    /**
     * Filters by name, waits for the first row to stabilise, then clicks edit.
     * Retries up to 3 times to handle Quasar DOM instability.
     */
    public void filterByNameAndClickEdit(String charterName) {
        RuntimeException lastError = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                filterByName(charterName);
                new WebDriverWait(driver, Duration.ofSeconds(8)).until(d -> {
                    List<WebElement> rows = d.findElements(tableRows);
                    if (rows.isEmpty()) return false;
                    try {
                        String first = rows.get(0).findElement(nameCell).getText().trim().toUpperCase();
                        return first.contains(charterName.toUpperCase());
                    } catch (StaleElementReferenceException | NoSuchElementException e) { return false; }
                });
                WebElement editBtn = wait.until(ExpectedConditions.elementToBeClickable(editIconFirstRow));
                scrollToCenter(editBtn);
                editBtn.click();
                System.out.println("Clic edit '" + charterName + "' (tentative " + attempt + ")");
                return;
            } catch (Exception e) {
                lastError = new RuntimeException(e);
            }
            sleep(400);
        }
        throw new NoSuchElementException(
                "Impossible de cliquer edit pour '" + charterName + "' après 3 tentatives.", lastError
        );
    }

    public void clickDeleteOnFirstRow() {
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(tableRows, 0));
        wait.until(ExpectedConditions.elementToBeClickable(deleteIconFirstRow)).click();
        System.out.println("Clic icône delete");
    }

    // =========================================================================
    // Create
    // =========================================================================
    public void clickCreateCharter() {
        wait.until(ExpectedConditions.elementToBeClickable(createButton)).click();
        System.out.println("Bouton Nouvelle charte graphique cliqué");
    }

    // =========================================================================
    // Delete dialog
    // =========================================================================
    public boolean isDeleteConfirmDialogDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(deleteConfirmDialog));
            System.out.println("Modale suppression visible");
            return true;
        } catch (Exception e) { return false; }
    }

    public void confirmDeletion() {
        wait.until(ExpectedConditions.elementToBeClickable(deleteConfirmButton)).click();
        System.out.println("Suppression confirmée");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(deleteConfirmDialog));
        waitForTableStable();
    }

    public void cancelDeletion() {
        wait.until(ExpectedConditions.elementToBeClickable(deleteCancelButton)).click();
        System.out.println("Suppression annulée");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(deleteConfirmDialog));
        waitForTableStable();
    }

    public String getToastMessage() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(successToast)).getText().trim();
        } catch (Exception e) { return ""; }
    }

    // =========================================================================
    // Assertion helpers
    // =========================================================================
    public boolean isCharterPresentInList(String charterName) {
        navigateToListPage();
        filterByName(charterName);
        return getDisplayedNames().stream()
                .anyMatch(n -> n.toUpperCase().contains(charterName.toUpperCase()));
    }

    public boolean isCharterAbsentAfterFilter(String charterName) {
        navigateToListPage();
        filterByName(charterName);
        boolean absent = isListEmpty();
        System.out.println(absent ? "Charte '" + charterName + "' absente ✔" : "Charte '" + charterName + "' encore présente ✘");
        return absent;
    }

    public boolean isCharterPresentAfterFilter(String charterName) {
        List<String> names = getDisplayedNames();
        if (names.isEmpty()) {
            navigateToListPage();
            filterByName(charterName);
            names = getDisplayedNames();
        }
        boolean present = names.stream().anyMatch(n -> n.toUpperCase().contains(charterName.toUpperCase()));
        System.out.println(present ? "Charte '" + charterName + "' présente ✔" : "Charte '" + charterName + "' introuvable ✘");
        return present;
    }
}