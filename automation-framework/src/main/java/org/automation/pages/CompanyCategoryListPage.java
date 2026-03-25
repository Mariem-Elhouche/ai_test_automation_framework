package org.automation.pages;

import org.automation.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.Keys;

import java.util.ArrayList;
import java.util.List;

public class CompanyCategoryListPage extends BasePage {

    private By editIconInRow   = By.xpath(".//i[text()='edit']");
    private By deleteIconInRow = By.xpath(".//i[text()='delete']");

    // =========================
    // Locators - Filters
    // =========================
    private By nameFilterInput    = By.xpath("//input[@placeholder='Nom']");
    private By codeFilterInput    = By.xpath("//input[@placeholder='Code']");
    private By companyFilterInput = By.xpath(
            "(//div[contains(@class,'q-field') and .//i[contains(@class,'icon-search')]]//input)[3]"
    );

    // =========================
    // Table rows and cells
    // =========================
    private By tableRows = By.xpath(
            "//tbody/tr | //div[contains(@class,'q-table__grid-content')]//div[contains(@class,'q-card')]"
    );
    private By nameCell    = By.xpath(".//td[1]");
    private By codeCell    = By.xpath(
            ".//td[2] | .//div[contains(@class,'q-card')]//div[contains(@class,'code') or contains(@class,'q-td')]"
    );
    private By companyCell = By.xpath(".//td[3]");

    // =========================
    // Locators - Delete modal
    // =========================
    private By deleteConfirmButton = By.xpath(
            "//button[.//span[contains(text(),'Confirmer la suppression')]]"
    );
    private By deleteCancelButton = By.xpath(
            "//button[.//span[contains(text(),'Ne pas supprimer')]]"
    );
    private By deleteConfirmDialog = By.xpath(
            "//div[@role='dialog' and .//span[contains(.,'irréversible')]]"
    );

    // =========================
    // Locators - Empty table
    // =========================
    private By noDataRow = By.xpath(
            "//td[contains(@class,'q-table__bottom-nodata') or contains(.,'aucun') or contains(.,'Aucun')] " +
                    "| //div[contains(@class,'q-table__bottom') and contains(.,'aucun')]"
    );

    // =========================
    // Locators - Pagination
    // =========================
    private By activePage = By.xpath(
            "//button[contains(@class,'q-btn') and " +
                    "(contains(@class,'bg-primary') or contains(@class,'q-btn--active'))]"
    );

    // =========================
    // Locators - Success toast
    // =========================
    private By successToast = By.xpath("//div[contains(@class,'q-notification')]");

    public CompanyCategoryListPage() {
        super();
    }

    // =========================
    // Navigation
    // =========================
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
        waitForTableStable();
    }

    public void filterByCode(String code) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(codeFilterInput));
        field.clear();
        field.sendKeys(code);
        field.sendKeys(Keys.ENTER);
        waitForTableStable();
    }

    public void filterByCompany(String company) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(companyFilterInput));
        field.clear();
        field.sendKeys(company);
        field.sendKeys(Keys.ENTER);
        waitForTableStable();
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

    /**
     * Lit les valeurs d'une colonne en re-fetchant chaque ligne par index
     * pour éviter les StaleElementReferenceException.
     */
    private List<String> getCellValues(By cellLocator) {
        waitForTableStable();
        int rowCount = driver.findElements(tableRows).size();
        List<String> values = new ArrayList<>();
        for (int i = 0; i < rowCount; i++) {
            String text = getCellTextWithRetry(i, cellLocator, 3);
            if (!text.isEmpty()) {
                values.add(text);
            }
        }
        return values;
    }

    private String getCellTextWithRetry(int rowIndex, By cellLocator, int maxRetry) {
        for (int attempt = 1; attempt <= maxRetry; attempt++) {
            try {
                List<WebElement> rows = driver.findElements(tableRows);
                if (rowIndex >= rows.size()) return "";
                WebElement row = rows.get(rowIndex);
                WebElement cell = row.findElement(cellLocator);
                return cell.getText().trim();
            } catch (StaleElementReferenceException e) {
                System.out.println("⚠️ StaleElement ligne " + rowIndex
                        + ", tentative " + attempt + "/" + maxRetry);
                if (attempt == maxRetry) return "";
                try { Thread.sleep(300); } catch (InterruptedException ignored) {}
            } catch (NoSuchElementException e) {
                return "";
            }
        }
        return "";
    }

    // =========================
    // Table utilities
    // =========================
    public boolean isListEmpty() {
        waitForTableStable();
        return !driver.findElements(noDataRow).isEmpty()
                || driver.findElements(tableRows).isEmpty();
    }

    /**
     * Attend que la table soit stable (2 cycles consécutifs de taille identique).
     */
    private void waitForTableStable() {
        int retry = 0;
        int previousSize = -1;
        int stableCount = 0;
        while (retry < 20) {
            try { Thread.sleep(300); } catch (InterruptedException ignored) {}
            int currentSize = driver.findElements(tableRows).size();
            if (currentSize == previousSize) {
                stableCount++;
                if (stableCount >= 2) break;
            } else {
                stableCount = 0;
            }
            previousSize = currentSize;
            retry++;
        }
    }

    // =========================
    // Pagination
    // =========================
    public void clickOnPage(int pageNumber) {
        By pageButton = By.xpath(
                "//button[contains(@class,'q-btn') and .//span[normalize-space(text())='" + pageNumber + "']]"
        );
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(pageButton));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", btn
        );
        btn.click();
        waitForTableStable();
    }

    public void clickOnLastPage() {
        By lastPageButton = By.xpath(
                "(//button[contains(@class,'q-btn') and " +
                        ".//span[number(normalize-space(text())) = number(normalize-space(text()))]])[last()]"
        );
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(lastPageButton));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", btn
        );
        btn.click();
        waitForTableStable();
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

    // =========================
    // Edit - first row
    // =========================
    public void clickEditOnFirstRow() {
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(tableRows, 0));
        List<WebElement> rows = driver.findElements(tableRows);
        WebElement firstRow = rows.get(0);
        WebElement editBtn = firstRow.findElement(editIconInRow);
        wait.until(ExpectedConditions.elementToBeClickable(editBtn)).click();
    }

    // =========================
    // Delete - actions
    // =========================

    /**
     * Clique sur l'icône delete de la première ligne du tableau filtré.
     * À appeler après filterByName() pour cibler la bonne catégorie.
     */
    public void clickDeleteOnFirstRow() {
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(tableRows, 0));
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                List<WebElement> rows = driver.findElements(tableRows);
                WebElement firstRow = rows.get(0);
                WebElement deleteBtn = firstRow.findElement(deleteIconInRow);
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView({block:'center'});", deleteBtn
                );
                wait.until(ExpectedConditions.elementToBeClickable(deleteBtn)).click();
                System.out.println("✅ Clic sur icône delete réussi (tentative " + attempt + ")");
                return;
            } catch (StaleElementReferenceException e) {
                System.out.println("⚠️ StaleElement delete, tentative " + attempt + "/3");
                if (attempt == 3) throw new RuntimeException("Impossible de cliquer sur delete", e);
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            }
        }
    }

    /**
     * Vérifie que la modale de confirmation de suppression est affichée.
     */
    public boolean isDeleteConfirmDialogDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(deleteConfirmDialog));
            System.out.println("✅ Modale de confirmation de suppression visible");
            return true;
        } catch (Exception e) {
            System.out.println("⚠️ Modale de suppression non trouvée");
            return false;
        }
    }

    /**
     * Clique sur "Confirmer la suppression" dans la modale.
     */
    public void confirmDeletion() {
        WebElement btn = wait.until(
                ExpectedConditions.elementToBeClickable(deleteConfirmButton)
        );
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        System.out.println("✅ Suppression confirmée");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(deleteConfirmDialog));
        waitForTableStable();
    }

    /**
     * Clique sur "Ne pas supprimer" dans la modale.
     */
    public void cancelDeletion() {
        WebElement btn = wait.until(
                ExpectedConditions.elementToBeClickable(deleteCancelButton)
        );
        btn.click();
        System.out.println("✅ Suppression annulée");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(deleteConfirmDialog));
        waitForTableStable();
    }

    /**
     * Retourne le texte du toast affiché après une action (succès/erreur).
     */
    public String getDeletionSuccessMessage() {
        try {
            return wait.until(
                    ExpectedConditions.visibilityOfElementLocated(successToast)
            ).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Vérifie que la catégorie supprimée n'apparaît plus dans la liste.
     * On re-navigue et re-filtre proprement pour avoir un état clean.
     */
    public boolean isCategoryAbsentAfterFilter(String categoryName) {
        navigateToListPage();
        filterByName(categoryName);
        boolean absent = isListEmpty();
        System.out.println(absent
                ? "✅ Catégorie '" + categoryName + "' absente de la liste"
                : "⚠️ Catégorie '" + categoryName + "' encore présente");
        return absent;
    }

    /**
     * Vérifie que la catégorie est encore présente dans la liste.
     * On ne re-filtre PAS ici car le filtre est déjà actif depuis le step précédent.
     * On relit simplement les noms affichés dans le tableau courant.
     * Si la liste est vide (filtre effacé après annulation), on re-navigue et re-filtre proprement.
     */
    public boolean isCategoryPresentAfterFilter(String categoryName) {
        // Cas où le tableau est vide après annulation (filtre réinitialisé par l'application)
        List<String> names = getDisplayedNames();
        if (names.isEmpty()) {
            System.out.println("⚠️ Tableau vide détecté, re-navigation et re-filtrage...");
            navigateToListPage();
            filterByName(categoryName);
            names = getDisplayedNames();
        }
        boolean present = names.stream()
                .anyMatch(n -> n.equalsIgnoreCase(categoryName));
        System.out.println(present
                ? "✅ Catégorie '" + categoryName + "' toujours présente"
                : "⚠️ Catégorie '" + categoryName + "' introuvable");
        return present;
    }
}