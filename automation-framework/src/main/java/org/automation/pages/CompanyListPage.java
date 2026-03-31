package org.automation.pages;

import org.automation.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class CompanyListPage extends BasePage {

    // =========================
    // URL
    // =========================
    private static final String LIST_URL = "https://stg-bo.noveocare.com/entities/companies";

    // Mémorise si le dernier filtre environnement n'a trouvé aucune option
    private boolean lastEnvironmentNotFound = false;

    public boolean wasLastEnvironmentNotFound() {
        return lastEnvironmentNotFound;
    }

    // =========================
    // Locators — Filtres texte
    // =========================
    private final By nameFilterInput   = By.xpath("//input[@placeholder='Nom']");
    private final By openIdFilterInput = By.xpath("//input[@placeholder='ID dans Open']");

    // ===========================================================================================
    // Locators — Filtre Environnement
    //
    // Le dropdown est le 3e champ de la barre de filtres.
    // Il n'a ni placeholder ni label "Environnement" dans le DOM.
    // On le cible par sa structure : c'est le seul q-select (combobox)
    // de la barre de filtres, donc le seul input[@role='combobox'] visible.
    // ============================================================================================
    private final By environmentSelectTrigger = By.xpath(
            "(//div[contains(@class,'q-field') and " +
                    "  not(ancestor::div[contains(@class,'q-menu')]) and " +
                    "  .//input[@role='combobox']])[1]"
    );

    // Champ "Rechercher" dans le menu déroulant
    private final By environmentMenuSearchInput = By.xpath(
            "//div[contains(@class,'q-menu')]//input[@placeholder='Rechercher']"
    );

    // Message "Pas de contenu" quand aucun environnement ne correspond
    private final By environmentNoContent = By.xpath(
            "//div[contains(@class,'q-menu')] " +
                    "//div[contains(normalize-space(.),'Pas de contenu')] "
    );

    // Bouton "Valider" dans le menu
    private final By environmentValidateBtn = By.xpath(
            "//div[contains(@class,'q-menu')]" +
                    "//button[.//span[normalize-space(text())='Valider']]"
    );

    // =========================
    // Locators — Table
    // =========================
    private final By tableRows        = By.xpath("//tbody/tr");
    private final By nameCell         = By.xpath(".//td[1]");
    private final By siretCell        = By.xpath(".//td[2]");
    private final By openIdCell       = By.xpath(".//td[3]");
    private final By environmentCell  = By.xpath(".//td[4]");
    private final By editIconInRow    = By.xpath(".//i[text()='edit']");
    private final By deleteIconInRow  = By.xpath(".//i[text()='delete']");

    // =========================
    // Locators — Table vide
    // =========================
    private final By noDataRow = By.xpath(
            "//td[contains(@class,'q-table__bottom-nodata') " +
                    "     or contains(normalize-space(.),'aucun') " +
                    "     or contains(normalize-space(.),'Aucun')] " +
                    "| //div[contains(@class,'q-table__bottom') and " +
                    "        (contains(.,'aucun') or contains(.,'Aucun'))]"
    );

    // =========================
    // Locators — Pagination
    // =========================
    private final By activePage = By.xpath(
            "//div[contains(@class,'q-table__bottom')]" +
                    "//button[contains(@class,'bg-primary') or contains(@class,'q-btn--active')]"
    );

    // =========================
    // Locators — Bouton Créer
    // =========================
    private final By createCompanyButton = By.xpath(
            "//button[.//span[contains(normalize-space(.),'entreprise')]]"
    );

    // =========================
    // Locators — Modale suppression
    // =========================
    private final By deleteConfirmDialog = By.xpath(
            "//div[@role='dialog' and .//span[contains(.,'irréversible')]]"
    );
    private final By deleteConfirmButton = By.xpath(
            "//button[.//span[contains(normalize-space(text()),'Confirmer la suppression')]]"
    );
    private final By deleteCancelButton = By.xpath(
            "//button[.//span[contains(normalize-space(text()),'Ne pas supprimer')]]"
    );

    // =========================
    // Locators — Toast
    // =========================
    private final By successToast = By.xpath("//div[contains(@class,'q-notification')]");

    public CompanyListPage() {
        super();
    }

    // =========================================================================
    // NAVIGATION
    // =========================================================================
    public void navigateToListPage() {
        driver.get(LIST_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(nameFilterInput));
        System.out.println("✅ Page liste des entreprises chargée");
    }

    // =========================================================================
    // FILTRES — Texte
    // =========================================================================
    public void filterByName(String name) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(nameFilterInput));
        clearAndType(field, name);
        field.sendKeys(Keys.ENTER);
        waitForTableStable();
        System.out.println("✅ Filtre Nom appliqué : " + name);
    }

    public void filterByOpenId(String openId) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(openIdFilterInput));
        clearAndType(field, openId);
        field.sendKeys(Keys.ENTER);
        waitForTableStable();
        System.out.println("✅ Filtre Open ID appliqué : " + openId);
    }

    // =========================================================================
    // FILTRES — Environnement (q-select multi-select Quasar)
    //
    //  Flux :
    //   1. Clic sur le déclencheur  → le menu s'ouvre
    //   2. Saisie dans "Rechercher" → les options sont filtrées
    //   3a. Si option trouvée → clic checkbox + clic Valider
    //   3b. Si "Pas de contenu" → fermer le menu (ENV inexistant = cas négatif)
    // =========================================================================
    public void filterByEnvironment(String environmentName) {
        openEnvironmentDropdown();
        typeInEnvironmentSearch(environmentName);

        // Attendre soit une option, soit "Pas de contenu"
        boolean optionFound = waitForEnvironmentOptionOrNoContent(environmentName);

        lastEnvironmentNotFound = !optionFound;
        if (optionFound) {
            selectEnvironmentOption(environmentName);
            clickEnvironmentValidate();
            waitForTableStable();
            System.out.println("✅ Filtre Environnement appliqué : " + environmentName);
        } else {
            // Cas négatif attendu : aucun environnement trouvé, fermer le menu
            closeEnvironmentDropdown();
            System.out.println("✅ Aucun environnement '" + environmentName + "' trouvé — cas négatif validé");
        }
    }

    /**
     * Ouvre le dropdown en cliquant sur le déclencheur.
     */
    private void openEnvironmentDropdown() {
        // Attendre que le trigger soit présent
        WebElement trigger = wait.until(
                ExpectedConditions.presenceOfElementLocated(environmentSelectTrigger)   // locator qui identifie le bouton ou l’élément à cliquer pour ouvrir le dropdown.
        );
        scrollToCenter(trigger);

        //Stratégie  : essaie le locator CSS puis JS click en fallback.
        try {
            trigger.click();      // Clic normal
        } catch (Exception e) {
            System.out.println("⚠️ Clic normal échoué sur dropdown env, fallback JS");
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", trigger);  //essaie le locator JS click en fallback pour forcer le clic  si le click normal echoue.
        }


        //Si l’attente expire au bout de 5 secondes (TimeoutException), ça fait un deuxième essai :
        //On retrouve le trigger (findElement)
        //On clique à nouveau en JS
        //On attend encore la visibilité du menu

        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(environmentMenuSearchInput));
            System.out.println("✅ Menu environnement ouvert");
        } catch (TimeoutException e) {
            // Second essai : parfois le premier clic ferme le menu si déjà ouvert
            System.out.println("⚠️ Menu non visible au 1er clic, 2e tentative...");
            trigger = driver.findElement(environmentSelectTrigger);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", trigger);
            wait.until(ExpectedConditions.visibilityOfElementLocated(environmentMenuSearchInput));
            System.out.println("✅ Menu environnement ouvert (2e tentative)");
        }
    }

    /**
     * Saisit le nom dans le champ "Rechercher" du menu.
     */
    private void typeInEnvironmentSearch(String environmentName) {
        WebElement searchInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(environmentMenuSearchInput)
        );
        searchInput.clear();
        searchInput.sendKeys(environmentName);
        sleep(700); // laisser le virtual-scroll filtrer
        System.out.println("✅ Recherche environnement saisie : " + environmentName);
    }

    /**
     * Attend que soit une option correspondante, soit "Pas de contenu" apparaisse.
     * @return true si une option a été trouvée, false si "Pas de contenu"
     */


    private boolean waitForEnvironmentOptionOrNoContent(String environmentName) {
        By optionLocator = buildEnvironmentOptionLocator(environmentName);
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(d -> !d.findElements(optionLocator).isEmpty()
                            || !d.findElements(environmentNoContent).isEmpty());

            // Vérifier lequel est apparu
            boolean hasOption   = !driver.findElements(optionLocator).isEmpty();
            boolean hasNoContent = !driver.findElements(environmentNoContent).isEmpty();

            System.out.println(hasOption
                    ? "✅ Option '" + environmentName + "' trouvée dans le menu"
                    : "✅ 'Pas de contenu' affiché pour '" + environmentName + "'");

            return hasOption && !hasNoContent;

        } catch (TimeoutException e) {
            System.out.println("⚠️ Ni option ni 'Pas de contenu' trouvés — on suppose option présente");
            return true;
        }
    }

    /**
     * Clique sur l'option correspondante dans le menu.
     */
    private void selectEnvironmentOption(String environmentName) {
        By optionLocator = buildEnvironmentOptionLocator(environmentName);
        WebElement option = wait.until(ExpectedConditions.elementToBeClickable(optionLocator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", option);
        System.out.println("✅ Environnement sélectionné : " + environmentName);
    }

    private By buildEnvironmentOptionLocator(String environmentName) {
        return By.xpath(
                "//div[contains(@class,'q-menu')]//div[@role='option']" +
                        "[.//span[normalize-space(text())='" + environmentName + "']]"
        );
    }

    /**
     * Clique sur le bouton "Valider" et attend la fermeture du menu.
     */
    private void clickEnvironmentValidate() {
        WebElement validateBtn = wait.until(
                ExpectedConditions.elementToBeClickable(environmentValidateBtn)
        );
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", validateBtn);
        // Attendre fermeture du menu
        wait.until(ExpectedConditions.invisibilityOfElementLocated(environmentMenuSearchInput));
        System.out.println("✅ Valider cliqué — menu fermé");
    }

    /**
     * pour fermer le dropdown : selenium tape sur esc,
     * si ça ne marche pas , on clique à l'exterieur du meni pour se ferme
     */
    private void closeEnvironmentDropdown() {
        try {
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(environmentMenuSearchInput));
            System.out.println("✅ Menu environnement fermé (ESC)");
        } catch (Exception e) {
            // Clic à l'extérieur du menu en fallback
            try {
                driver.findElement(nameFilterInput).click();
                System.out.println("✅ Menu environnement fermé (clic extérieur)");
            } catch (Exception ignored) {}
        }
    }

    // =========================================================================
    // DONNÉES — Table
    // =========================================================================
    public List<String> getDisplayedNames()        { return getCellValues(nameCell); }
    public List<String> getDisplayedSirets()       { return getCellValues(siretCell); }
    public List<String> getDisplayedOpenIds()      { return getCellValues(openIdCell); }
    public List<String> getDisplayedEnvironments() { return getCellValues(environmentCell); }

    private List<String> getCellValues(By cellLocator) {
        waitForTableStable();
        int rowCount = driver.findElements(tableRows).size();
        List<String> values = new ArrayList<>();
        for (int i = 0; i < rowCount; i++) {
            String text = getCellTextWithRetry(i, cellLocator, 3);
            if (!text.isEmpty()) values.add(text);
        }
        return values;
    }

    private String getCellTextWithRetry(int rowIndex, By cellLocator, int maxRetry) {
        for (int attempt = 1; attempt <= maxRetry; attempt++) {
            try {
                List<WebElement> rows = driver.findElements(tableRows);
                if (rowIndex >= rows.size()) return "";
                return rows.get(rowIndex).findElement(cellLocator).getText().trim();
            } catch (StaleElementReferenceException e) {
                if (attempt == maxRetry) return "";
                sleep(300);
            } catch (NoSuchElementException e) {
                return "";
            }
        }
        return "";
    }

    // =========================================================================
    // ÉTAT — Table vide
    // =========================================================================
    public boolean isListEmpty() {
        waitForTableStable();
        return !driver.findElements(noDataRow).isEmpty()
                || driver.findElements(tableRows).isEmpty();
    }

    // =========================================================================
    // PAGINATION
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
        System.out.println("✅ Navigation vers la page " + pageNumber);
    }

    public void clickOnLastPage() {
        By lastNumberedBtn = By.xpath(
                "(//div[contains(@class,'q-table__bottom')]" +
                        "//button[.//span[number(normalize-space(text())) = " +
                        "                 number(normalize-space(text()))]])[last()]"
        );
        WebElement lastBtn = wait.until(ExpectedConditions.elementToBeClickable(lastNumberedBtn));
        scrollToCenter(lastBtn);
        lastBtn.click();
        waitForTableStable();
        System.out.println("✅ Dernière page atteinte");
    }

    public int getCurrentPage() {
        try {
            WebElement active = wait.until(
                    ExpectedConditions.presenceOfElementLocated(activePage)
            );
            return Integer.parseInt(active.getText().trim());
        } catch (Exception e) {
            return 1;
        }
    }

    public boolean isOnPage(int expectedPage) {
        int current = getCurrentPage();
        System.out.println("📄 Page courante : " + current + " | Attendue : " + expectedPage);
        return current == expectedPage;
    }

    public boolean isLastPage() {
        return getCurrentPage() > 1;
    }

























    // =========================================================================
    // ACTIONS — Créer / Éditer / Supprimer
    // =========================================================================
    public void clickCreateCompany() {
        wait.until(ExpectedConditions.elementToBeClickable(createCompanyButton)).click();
        System.out.println("✅ Bouton Créer une entreprise cliqué");
    }

    public void clickEditOnFirstRow() {
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(tableRows, 0));
        clickIconInRow(editIconInRow, "edit");
    }

    public void clickDeleteOnFirstRow() {
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(tableRows, 0));
        clickIconInRow(deleteIconInRow, "delete");
    }

    private void clickIconInRow(By iconLocator, String label) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                List<WebElement> rows = driver.findElements(tableRows);
                WebElement icon = rows.get(0).findElement(iconLocator);
                scrollToCenter(icon);
                wait.until(ExpectedConditions.elementToBeClickable(icon)).click();
                System.out.println("✅ Clic icône '" + label + "' (tentative " + attempt + ")");
                return;
            } catch (StaleElementReferenceException e) {
                if (attempt == 3) throw new RuntimeException("Impossible de cliquer sur '" + label + "'", e);
                sleep(500);
            }
        }
    }

    // =========================================================================
    // SUPPRESSION — Modale
    // =========================================================================
    public boolean isDeleteConfirmDialogDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(deleteConfirmDialog));
            System.out.println("✅ Modale suppression entreprise visible");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void confirmDeletion() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(deleteConfirmButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        System.out.println("✅ Suppression entreprise confirmée");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(deleteConfirmDialog));
        waitForTableStable();
    }

    public void cancelDeletion() {
        wait.until(ExpectedConditions.elementToBeClickable(deleteCancelButton)).click();
        System.out.println("✅ Suppression entreprise annulée");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(deleteConfirmDialog));
        waitForTableStable();
    }

    public String getToastMessage() {
        try {
            return wait.until(
                    ExpectedConditions.visibilityOfElementLocated(successToast)
            ).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    // =========================================================================
    // VÉRIFICATIONS — Présence / Absence
    // =========================================================================
    public boolean isCompanyPresentInList(String companyName) {
        navigateToListPage();
        filterByName(companyName);
        return getDisplayedNames().stream()
                .anyMatch(n -> n.toUpperCase().contains(companyName.toUpperCase()));
    }

    public boolean isCompanyAbsentAfterFilter(String companyName) {
        navigateToListPage();
        filterByName(companyName);
        boolean absent = isListEmpty();
        System.out.println(absent
                ? "✅ Entreprise '" + companyName + "' absente"
                : "⚠️ Entreprise '" + companyName + "' encore présente");
        return absent;
    }

    public boolean isCompanyPresentAfterFilter(String companyName) {
        List<String> names = getDisplayedNames();
        if (names.isEmpty()) {
            navigateToListPage();
            filterByName(companyName);
            names = getDisplayedNames();
        }
        boolean present = names.stream()
                .anyMatch(n -> n.toUpperCase().contains(companyName.toUpperCase()));
        System.out.println(present
                ? "✅ Entreprise '" + companyName + "' présente"
                : "⚠️ Entreprise '" + companyName + "' introuvable");
        return present;
    }

    // =========================================================================
    // UTILITAIRES PRIVÉS
    // =========================================================================
    private void clearAndType(WebElement field, String value) {
        field.click();
        field.sendKeys(Keys.CONTROL + "a");
        field.sendKeys(Keys.DELETE);
        field.sendKeys(value);
    }

    private void scrollToCenter(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", element
        );
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    private void waitForTableStable() {
        int previousSize = -1;
        int stableCount = 0;
        for (int retry = 0; retry < 20; retry++) {
            sleep(300);
            int currentSize = driver.findElements(tableRows).size();
            if (currentSize == previousSize) {
                if (++stableCount >= 2) break;
            } else {
                stableCount = 0;
            }
            previousSize = currentSize;
        }
    }



}
