package org.automation.pages;

import org.automation.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CompanyListPage extends BasePage {

    private static final String LIST_URL = "https://stg-bo.noveocare.com/entities/companies";


    private boolean lastEnvironmentNotFound = false;  // Mémorise si le dernier filtre environnement n'a trouvé aucune option
    private final By nameFilterInput   = By.xpath("//input[@placeholder='Nom']");
    private final By openIdFilterInput = By.xpath("//input[@placeholder='ID dans Open']");

    private final By environmentSelectTrigger = By.xpath(
            "(//div[contains(@class,'q-field') and " +
                    "  not(ancestor::div[contains(@class,'q-menu')]) and " +
                    "  .//input[@role='combobox']])[1]"
    );
    private final By environmentMenuSearchInput = By.xpath(    // Champ "Rechercher" dans le menu déroulant
            "//div[contains(@class,'q-menu')]//input[@placeholder='Rechercher']");
    private final By environmentNoContent = By.xpath(
            "//div[contains(@class,'q-menu')] " +
                    "//div[contains(normalize-space(.),'Pas de contenu')] ");
    private final By environmentValidateBtn = By.xpath(
            "//div[contains(@class,'q-menu')]" +
                    "//button[.//span[normalize-space(text())='Valider']]"
    );
    private final By tableRows        = By.xpath("//tbody/tr");
    private final By nameCell         = By.xpath(".//td[1]");
    private final By openIdCell       = By.xpath(".//td[3]");
    private final By environmentCell  = By.xpath(".//td[4]");
    private final By deleteIconFirstRow = By.xpath("(//tbody/tr)[1]//i[text()='delete']");

    private final By noDataRow = By.xpath(
            "//div[contains(@class,'q-table__bottom--nodata')]"
    );

    private final By activePage = By.xpath(
            "//div[contains(@class,'q-table__bottom')]" +
                    "//button[contains(@class,'bg-primary') or contains(@class,'q-btn--active')]"
    );

    private By lastNumberedBtn = By.xpath(
            "(//div[contains(@class,'q-table__bottom')]" +
                    "//button[.//span[number(normalize-space(text())) = " +
                    "                 number(normalize-space(text()))]])[last()]"
    );
    private final By createCompanyButton = By.xpath("//button[.//span[contains(normalize-space(.),'entreprise')]]");

    private final By deleteConfirmDialog = By.xpath("//div[@role='dialog' and .//span[contains(.,'irréversible')]]");
    private final By deleteConfirmButton = By.xpath("//button[.//span[contains(normalize-space(text()),'Confirmer la suppression')]]");
    private final By deleteCancelButton = By.xpath("//button[.//span[contains(normalize-space(text()),'Ne pas supprimer')]]");

    private final By successToast = By.xpath("//div[contains(@class,'q-notification')]");


    // ==============
    // METHODS
    // ==============
    public CompanyListPage() {
        super();
    }
    // NAVIGATION
    public void navigateToListPage() {
        driver.get(LIST_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(nameFilterInput));
        System.out.println("Page liste des entreprises chargée");
    }
    public boolean wasLastEnvironmentNotFound() {
        return lastEnvironmentNotFound;
    }

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

    private void waitForTableStable() {
        // Attendre : soit des lignes, soit le message "no data" : les deux sont valides
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(d ->
                !d.findElements(tableRows).isEmpty()
                        || !d.findElements(noDataRow).isEmpty()
        );

        //Vérification de stabilité si des lignes existent
        if (driver.findElements(noDataRow).isEmpty()) {
            String previousText = "";
            int stableCount = 0;
            for (int retry = 0; retry < 12; retry++) {
                sleep(200);
                try {
                    String currentText = driver.findElements(tableRows).stream()
                            .map(r -> { try { return r.getText(); }
                            catch (StaleElementReferenceException e) { return "STALE"; }}) //Si une ligne est encore instable (Stale) → marque "STALE".
                            .collect(Collectors.joining("|"));
                    if (!currentText.contains("STALE") && currentText.equals(previousText) && !currentText.isEmpty()) {
                        if (++stableCount >= 2) break;  //La table est considérée stable si le texte ne change pas 2 fois consécutives
                    } else { stableCount = 0; }
                    previousText = currentText;
                } catch (NoSuchElementException ignored) { stableCount = 0; }
            }
        }
    }


    // FILTRES
    public void filterByName(String name) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(nameFilterInput));
        clearAndType(field, name);
        field.sendKeys(Keys.ENTER);
        waitForTableStable();
        System.out.println("Filtre Nom appliqué : " + name);
    }

    public void filterByOpenId(String openId) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(openIdFilterInput));
        clearAndType(field, openId);
        field.sendKeys(Keys.ENTER);
        waitForTableStable();
        System.out.println("Filtre Open ID appliqué : " + openId);
    }

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
            System.out.println("Filtre Environnement appliqué : " + environmentName);
        } else {
            // Cas négatif attendu : aucun environnement trouvé, fermer le menu
            closeEnvironmentDropdown();
            System.out.println("Aucun environnement '" + environmentName + "' trouvé ");
        }
    }

    private void openEnvironmentDropdown() {
        WebElement trigger = wait.until(
                ExpectedConditions.elementToBeClickable(environmentSelectTrigger)
        );
        // Premier clic : initialise le composant Quasar
        trigger.click();
        // Deuxième clic : ouvre réellement le menu
        driver.findElement(environmentSelectTrigger).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(environmentMenuSearchInput));
        System.out.println("Menu environnement ouvert");
    }

    private void typeInEnvironmentSearch(String environmentName) {
        WebElement searchInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(environmentMenuSearchInput)
        );
        searchInput.clear();
        searchInput.sendKeys(environmentName);
        sleep(700);
        System.out.println("Recherche environnement saisie : " + environmentName);
    }

    //y'a un résultat dans le menu d'environnement ou pas (true : il y'a option , false : pas de contenu)
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
                    ? "Option '" + environmentName + "' trouvée dans le menu"
                    : "'Pas de contenu' affiché pour '" + environmentName + "'");

            return hasOption && !hasNoContent;

        } catch (TimeoutException e) {
            System.out.println("Ni option ni 'Pas de contenu' trouvés");
            return true;
        }
    }

    private void selectEnvironmentOption(String environmentName) {
        By optionLocator = buildEnvironmentOptionLocator(environmentName);
        wait.until(ExpectedConditions.elementToBeClickable(optionLocator)).click();
        System.out.println("Environnement sélectionné : " + environmentName);
    }

    private By buildEnvironmentOptionLocator(String environmentName) {
        return By.xpath(
                "//div[contains(@class,'q-menu')]//div[@role='option']" +
                        "[.//span[normalize-space(text())='" + environmentName + "']]"
        );
    }


    private void clickEnvironmentValidate() {
        wait.until(ExpectedConditions.elementToBeClickable(environmentValidateBtn)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(environmentMenuSearchInput));
        System.out.println("Valider cliqué — menu fermé");
    }

    private void closeEnvironmentDropdown() {
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(environmentMenuSearchInput));
        System.out.println("Menu environnement fermé");
    }

    public List<String> getDisplayedNames()        { return getCellValues(nameCell); }
    public List<String> getDisplayedOpenIds()      { return getCellValues(openIdCell); }
    public List<String> getDisplayedEnvironments() { return getCellValues(environmentCell); }

    //Pour la recuperation des contenus des colonnes :
    //Si le DOM varie → on recommence
    //Si tout est stable → on retourne les valeurs des cellules.
    private List<String> getCellValues(By cellLocator) {
        waitForTableStable();
        //attendre jusqu'a  trouver une valeur : soit trouver  null ou element
        return wait.until(d -> {
            List<WebElement> rows = d.findElements(tableRows);
            if (rows.isEmpty()) return new ArrayList<>();

            List<String> values = rows.stream()
                    .map(row -> {
                        try {
                            return row.findElement(cellLocator).getText().trim();
                        } catch (StaleElementReferenceException | NoSuchElementException e) {
                            return null; // Signal que le DOM a bougé
                        }
                    })
                    .toList();

            // Si null présent → DOM instable → retourner null pour que FluentWait retente
            return values.contains(null) ? null : values.stream()
                    .filter(t -> !t.isEmpty())
                    .toList();
        });
    }

    public boolean isListEmpty() {
        waitForTableStable();
        return !driver.findElements(noDataRow).isEmpty()
                || driver.findElements(tableRows).isEmpty();
    }

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
        System.out.println("Page courante : " + current + " | Attendue : " + expectedPage);
        return current == expectedPage;
    }

    public boolean isLastPage() {
        return getCurrentPage() > 1;
    }



    // ACTIONS — Créer / Éditer / Supprimer

    public void clickCreateCompany() {
        wait.until(ExpectedConditions.elementToBeClickable(createCompanyButton)).click();
        System.out.println("Bouton Créer une entreprise cliqué");
    }


    public void clickEditOnFirstRow() {
        waitForTableStable(); // Assurer que la table est stabilisée
        WebElement editBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//tbody/tr)[1]//i[text()='edit']")));
        editBtn.click();
        System.out.println("Clic icône edit");
    }

    public void clickDeleteOnFirstRow() {
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(tableRows, 0));
        wait.until(ExpectedConditions.elementToBeClickable(deleteIconFirstRow)).click();
        System.out.println("Clic icône delete");
    }

    // SUPPRESSION
    public boolean isDeleteConfirmDialogDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(deleteConfirmDialog));
            System.out.println("Modale suppression entreprise visible");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void confirmDeletion() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(deleteConfirmButton));
        btn.click();
        System.out.println("Suppression entreprise confirmée");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(deleteConfirmDialog));
        waitForTableStable();
    }

    public void cancelDeletion() {
        wait.until(ExpectedConditions.elementToBeClickable(deleteCancelButton)).click();
        System.out.println("Suppression entreprise annulée");
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


    // VÉRIFICATIONS
    public boolean isCompanyPresentInList(String companyName) {
        navigateToListPage();
       // sleep(500);          // attendre Quasar initialiser les composants
        filterByName(companyName);
        return getDisplayedNames().stream()
                .anyMatch(n -> n.toUpperCase().contains(companyName.toUpperCase()));
    }
    public boolean isCompanyAbsentAfterFilter(String companyName) {
        navigateToListPage();
        filterByName(companyName);
        boolean absent = isListEmpty();
        System.out.println(absent
                ? "Entreprise '" + companyName + "' absente"
                : "Entreprise '" + companyName + "' encore présente");
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
                ? "Entreprise '" + companyName + "' présente"
                : "Entreprise '" + companyName + "' introuvable");
        return present;
    }


}
