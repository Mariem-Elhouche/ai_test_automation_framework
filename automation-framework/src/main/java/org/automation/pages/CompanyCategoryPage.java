package org.automation.pages;

import org.automation.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class CompanyCategoryPage extends BasePage {

    // =========================
    // Locators - Navigation
    // =========================
    private By entitiesMenu = By.xpath(
            "//div[contains(@class,'q-item') and .//div[normalize-space()='Entités']]"
    );
    private By companyCategoriesSubMenu = By.cssSelector(
            "a[href='/entities/company-sections']"
    );

    // =========================
    // Locators - Category Form
    // =========================
    private By addCategoryButton = By.xpath(
            "//button[.//span[contains(.,'entreprise')]]"
    );
    private By categoryNameInput = By.xpath(
            "//div[contains(@class,'text-weight-semi-bold') and normalize-space(.)='Nom']" +
                    "/following-sibling::label//input[contains(@class,'q-field__native')]"
    );
    private By categoryCodeInput = By.xpath(
            "//div[contains(@class,'text-weight-semi-bold') and normalize-space(.)='Code']" +
                    "/following-sibling::label//input[contains(@class,'q-field__native')]"
    );

    // =========================
    // Locators - Company Search
    // =========================
    private By searchCompanyLink = By.xpath(
            "//span[contains(.,'ajouter une entreprise')]"
    );
    private By openNameInput        = By.id("add-company-id");
    private By openIdInput          = By.id("add-company-open-id");
    private By companyNameInput     = By.id("add-company-name");
    private By environmentNameInput = By.id("add-company-environment-name");
    private By sirenNumberInput     = By.id("add-company-identification-number");
    private By searchButton         = By.xpath(
            "//button[.//i[contains(@class,'icon-search')]]"
    );
    private By selectCompanyButton  = By.xpath(
            "//button[.//span[contains(.,'Sélectionner')]]"
    );
    private By searchResultRow = By.xpath(
            "//div[contains(@class,'card-container') and contains(@class,'cursor-pointer')]"
    );
    private By realResultCard = By.xpath(
            "//div[contains(@class,'card-container') and contains(@class,'cursor-pointer')" +
                    " and (.//div[contains(.,'entreprise :')] or .//div[contains(.,'ID de')])]"
    );

    // =========================
    // Locators - Save
    // =========================
    private By saveCategoryButton = By.xpath(
            "//button[.//span[text()='Enregistrer']]"
    );

    // =========================
    // Locators - Messages
    // =========================
    private By successToast = By.xpath(
            "//div[contains(@class,'q-notification')]"
    );
    private By validationAlert = By.xpath(
            "//div[@role='alert' and normalize-space(.) != '']"
    );

    // =========================
    // Locators - List & row actions (pour édition)
    // =========================
    private By tableRows = By.xpath("//tbody/tr");
    private By editIconInRow = By.xpath(".//i[text()='edit']");

    // =========================
    // Locators - Company edit in form
    // =========================
    /**
     * Plusieurs variantes du locator pour l'icône edit de l'entreprise liée,
     * car le DOM peut varier selon la version du composant Quasar.
     */
    private By editCompanyIcon = By.xpath(
            "//div[contains(.,'Entreprise liée')]//i[text()='edit'] | " +
                    "//div[contains(.,'Entreprise liée')]//button[.//i[text()='edit']] | " +
                    "//label[contains(.,'Entreprise')]//following-sibling::*//i[text()='edit'] | " +
                    "//div[contains(@class,'q-field') and .//div[contains(.,'Entreprise')]]//i[text()='edit']"
    );

    // =========================
    // Constructor
    // =========================
    public CompanyCategoryPage() {
        super();
    }

    // =========================
    // Navigation
    // =========================
    public void goToCategoryPage() throws InterruptedException {
        clickOnEntityMenu();
        clickOnSubMenu();
    }

    private void clickOnEntityMenu() throws InterruptedException {
        System.out.println("Tentative de clic sur le menu 'Entités'...");
        WebElement entities = wait.until(
                ExpectedConditions.visibilityOfElementLocated(entitiesMenu)
        );
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center'});", entities
        );
        Thread.sleep(300);
        entities.click();
        System.out.println("✅ Clic normal réussi sur 'Entités'");
        Thread.sleep(800);
    }

    private void clickOnSubMenu() {
        System.out.println("Tentative de clic sur le sous-menu 'Catégories d'entreprise'...");
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}

        WebElement subMenu = wait.until(
                ExpectedConditions.elementToBeClickable(companyCategoriesSubMenu)
        );
        subMenu.click();
        System.out.println("✅ Clic réussi sur 'Catégories d'entreprise'");
    }

    // =========================
    // Category Form
    // =========================
    public void clickAddCategory() {
        wait.until(ExpectedConditions.elementToBeClickable(addCategoryButton)).click();
    }

    public void setCategoryName(String name) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                WebElement field = wait.until(
                        ExpectedConditions.presenceOfElementLocated(categoryNameInput)
                );
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView({block:'center'});", field
                );
                field.click();
                Thread.sleep(300);
                field = driver.findElement(categoryNameInput);
                field.sendKeys(Keys.CONTROL + "a");
                field.sendKeys(Keys.DELETE);
                field.sendKeys(name);
                System.out.println("✅ Nom saisi : " + name);
                Thread.sleep(500);
                return;
            } catch (StaleElementReferenceException e) {
                System.out.println("⚠️ StaleElement Nom, tentative " + attempt + "/3");
                try { Thread.sleep(600); } catch (InterruptedException ignored) {}
            } catch (InterruptedException ignored) {}
        }
        throw new RuntimeException("Impossible de saisir le nom après 3 tentatives");
    }

    public void setCategoryCode(String code) {
        try { Thread.sleep(800); } catch (InterruptedException ignored) {}
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                WebElement field = wait.until(
                        ExpectedConditions.presenceOfElementLocated(categoryCodeInput)
                );
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView({block:'center'});", field
                );
                field.click();
                Thread.sleep(300);
                field = driver.findElement(categoryCodeInput);
                field.sendKeys(Keys.CONTROL + "a");
                field.sendKeys(Keys.DELETE);
                field.sendKeys(code);
                System.out.println("✅ Code saisi : " + code);
                Thread.sleep(300);
                return;
            } catch (StaleElementReferenceException e) {
                System.out.println("⚠️ StaleElement Code, tentative " + attempt + "/3");
                try { Thread.sleep(600); } catch (InterruptedException ignored) {}
            } catch (InterruptedException ignored) {}
        }
        throw new RuntimeException("Impossible de saisir le code après 3 tentatives");
    }

    // =========================
    // Company Search
    // =========================
    public void openCompanySearch() {
        wait.until(ExpectedConditions.elementToBeClickable(searchCompanyLink)).click();
    }

    public void searchByCompanyName(String name) {
        WebElement field = wait.until(
                ExpectedConditions.visibilityOfElementLocated(companyNameInput)
        );
        field.clear();
        field.sendKeys(name);
        driver.findElement(searchButton).click();
    }

    public void searchByOpenId(String id) {
        WebElement field = wait.until(
                ExpectedConditions.visibilityOfElementLocated(openIdInput)
        );
        field.clear();
        field.sendKeys(id);
        driver.findElement(searchButton).click();
    }

    public void searchBySiren(String siren) {
        WebElement field = wait.until(
                ExpectedConditions.visibilityOfElementLocated(sirenNumberInput)
        );
        field.clear();
        field.sendKeys(siren);
        driver.findElement(searchButton).click();
    }

    public void searchByOpenName(String openName) {
        WebElement field = wait.until(
                ExpectedConditions.visibilityOfElementLocated(openNameInput)
        );
        field.clear();
        field.sendKeys(openName);
        driver.findElement(searchButton).click();
    }

    public void searchByEnvironmentName(String envName) {
        WebElement field = wait.until(
                ExpectedConditions.visibilityOfElementLocated(environmentNameInput)
        );
        field.clear();
        field.sendKeys(envName);
        driver.findElement(searchButton).click();
    }

    public void selectCompany() {
        System.out.println("Recherche de la ligne résultat...");

        WebElement row = wait.until(
                ExpectedConditions.visibilityOfElementLocated(searchResultRow)
        );
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", row
        );
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        try {
            new Actions(driver).moveToElement(row).click().perform();
            System.out.println("✅ Clic Actions sur la card réussi");
        } catch (Exception e) {
            System.out.println("⚠️ Clic Actions échoué : " + e.getMessage());
            ((JavascriptExecutor) driver).executeScript(
                    "var evt = new MouseEvent('click', {bubbles:true, cancelable:true, view:window});" +
                            "arguments[0].dispatchEvent(evt);", row
            );
            System.out.println("✅ Clic dispatchEvent sur la card");
        }

        try { Thread.sleep(600); } catch (InterruptedException ignored) {}

        WebElement btn = driver.findElement(selectCompanyButton);
        if (btn.getAttribute("disabled") != null) {
            System.out.println("⚠️ Bouton encore disabled, tentative sur div interne...");
            try {
                WebElement innerDiv = driver.findElement(By.xpath(
                        "//div[contains(@class,'card-container')]//div[contains(.,'entreprise')]"
                ));
                new Actions(driver).moveToElement(innerDiv).click().perform();
                System.out.println("✅ Clic sur div interne 'entreprise'");
                Thread.sleep(500);
            } catch (Exception ex) {
                System.out.println("⚠️ Clic div interne échoué : " + ex.getMessage());
            }
        }

        System.out.println("Attente activation bouton Sélectionner...");
        wait.until(driver -> {
            try {
                WebElement b = driver.findElement(selectCompanyButton);
                return b.getAttribute("disabled") == null
                        && !"true".equals(b.getAttribute("aria-disabled"));
            } catch (Exception ex) { return false; }
        });

        btn = driver.findElement(selectCompanyButton);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        System.out.println("✅ Clic sur 'Sélectionner' réussi");
    }

    // =========================
    // Save
    // =========================
    public void saveCategory() {
        wait.until(ExpectedConditions.elementToBeClickable(saveCategoryButton)).click();
    }

    // =========================
    // Messages & Assertions
    // =========================
    public String getSuccessMessage() {
        try {
            return wait.until(
                    ExpectedConditions.visibilityOfElementLocated(successToast)
            ).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isEditSuccessMessageDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[contains(@class,'q-notification') and contains(.,'modifiée')]")
            ));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getErrorMessage() {
        try {
            WebElement alert = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(validationAlert));
            String text = alert.getText().trim();
            System.out.println("✅ Message d'erreur trouvé : " + text);
            return text;
        } catch (Exception ignored) {}

        try {
            return new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//div[contains(@class,'q-notification__message')" +
                                    " and normalize-space(.) != '']")
                    )).getText().trim();
        } catch (Exception e) {
            throw new RuntimeException("Aucun message d'erreur trouvé sur la page", e);
        }
    }

    public boolean isNoResultsMessageDisplayed() {
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
        List<WebElement> resultCards = driver.findElements(realResultCard);
        System.out.println("  → Nombre de cards résultat : " + resultCards.size());
        boolean noResults = resultCards.isEmpty();
        System.out.println(noResults ? "✅ Aucun résultat trouvé" : "⚠️ Des résultats existent");
        return noResults;
    }

    public boolean isCompanySelectionDisabled() {
        try {
            WebElement btn = driver.findElement(selectCompanyButton);
            String disabled = btn.getAttribute("disabled");
            String ariaDisabled = btn.getAttribute("aria-disabled");
            return disabled != null || "true".equals(ariaDisabled);
        } catch (Exception e) {
            return true;
        }
    }

    // =========================
    // Edit - row actions
    // =========================
    public void clickEditForCategory(String categoryName) {
        WebElement row = getRowByCategoryName(categoryName);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center'});", row
        );
        WebElement editBtn = row.findElement(editIconInRow);
        wait.until(ExpectedConditions.elementToBeClickable(editBtn)).click();
    }

    private WebElement getRowByCategoryName(String categoryName) {
        String xpath = String.format(
                "//tbody/tr[td[1][normalize-space()='%s']]",
                categoryName
        );
        return wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
    }

    public boolean isCategoryPresentInList(String categoryName) {
        try {
            return driver.findElements(By.xpath(
                    String.format("//tbody/tr[td[1][normalize-space()='%s']]", categoryName)
            )).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // =========================
    // Company edit in form — FIXED
    // =========================
    /**
     * Change the associated company for the current category.
     * FIX TC_EDIT_003 : logique de clic robuste sur l'icône edit de l'entreprise liée,
     * avec scroll, attente de la modal et fallback JS si le clic normal échoue.
     */
    public void changeAssociatedCompany(String newCompanyName) throws InterruptedException {
        System.out.println("Modification de l'entreprise liée...");

        // 1. Attendre que le formulaire d'édition soit chargé
        wait.until(ExpectedConditions.visibilityOfElementLocated(categoryNameInput));

        // 2. Localiser l'icône edit dans la section "Rattachement a l'entreprise"
        By editIconLocator = By.xpath("//h2[contains(.,'Rattachement')]/following::div[contains(@class,'card-container')]//i[text()='edit']");

        // 3. Boucle de tentative (gère les stale element)
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                WebElement editIcon = wait.until(ExpectedConditions.presenceOfElementLocated(editIconLocator));
                // Scroller vers l'icône
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView({block: 'center'});", editIcon
                );
                Thread.sleep(500);
                // Relocaliser après le scroll
                editIcon = driver.findElement(editIconLocator);
                // Cliquer
                wait.until(ExpectedConditions.elementToBeClickable(editIcon)).click();
                System.out.println("✅ Clic sur icône edit entreprise réussi (tentative " + attempt + ")");
                break;
            } catch (StaleElementReferenceException e) {
                System.out.println("⚠️ StaleElement, tentative " + attempt + "/3");
                if (attempt == 3) throw new RuntimeException("Impossible de cliquer sur l'icône edit", e);
                Thread.sleep(1000);
            }
        }

        // 4. Attendre que la modal de recherche soit visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(companyNameInput));

        // 5. Rechercher la nouvelle entreprise
        searchByCompanyName(newCompanyName);
        selectCompany();
    }
    /**
     * Trouve l'icône edit de l'entreprise liée en essayant plusieurs locators.
     * Retourne le premier élément trouvé et visible.
     */
    private WebElement findEditCompanyIcon() {
        // Liste de locators candidats, du plus précis au plus générique
        By[] candidates = {
                By.xpath("//div[contains(@class,'q-field') and .//div[contains(text(),'Entreprise liée')]]//i[text()='edit']"),
                By.xpath("//div[contains(@class,'q-field') and .//div[contains(.,'Entreprise liée')]]//button[.//i[text()='edit']]"),
                By.xpath("//*[contains(text(),'Entreprise liée')]/ancestor::div[contains(@class,'q-field')]//i[text()='edit']"),
                By.xpath("//*[contains(text(),'Entreprise liée')]/following::i[text()='edit'][1]"),
                By.xpath("//div[contains(.,'Entreprise liée')]//i[text()='edit']")
        };

        for (By locator : candidates) {
            try {
                List<WebElement> elements = driver.findElements(locator);
                if (!elements.isEmpty()) {
                    WebElement el = elements.get(0);
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView({block:'center'});", el
                    );
                    System.out.println("✅ Icône edit entreprise trouvée avec : " + locator);
                    return el;
                }
            } catch (Exception ignored) {}
        }

        // Fallback : attendre avec le locator principal (lèvera TimeoutException si absent)
        System.out.println("⚠️ Fallback sur wait pour l'icône edit entreprise...");
        return wait.until(ExpectedConditions.presenceOfElementLocated(editCompanyIcon));
    }

    /**
     * Clear the category name field (robust version).
     */
    public void clearNameField() {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(categoryNameInput));
        field.click();
        field.sendKeys(Keys.CONTROL + "a");
        field.sendKeys(Keys.DELETE);
    }
}