package org.automation.pages;

import org.automation.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
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
    // Constructor
    // =========================
    public CompanyCategoryPage() {
        super();
    }

    // =========================
    // Navigation
    // =========================
    public void goToCategoryPage() {
        clickOnEntityMenu();
        clickOnSubMenu();
    }

    private void clickOnEntityMenu() {
        System.out.println("Tentative de clic sur le menu 'Entités'...");
        try {
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
            return;
        } catch (Exception e) {
            System.out.println("⚠️ Clic normal échoué : " + e.getMessage());
        }

//        try {
//            WebElement entities = wait.until(
//                    ExpectedConditions.presenceOfElementLocated(entitiesMenu)
//            );
//            new Actions(driver).moveToElement(entities).click().perform();
//            System.out.println("✅ Clic Actions réussi sur 'Entités'");
//            Thread.sleep(800);
//            return;
//        } catch (Exception e) {
//            System.out.println("⚠️ Clic Actions échoué : " + e.getMessage());
//        }
//
//        try {
//            WebElement entities = wait.until(
//                    ExpectedConditions.presenceOfElementLocated(entitiesMenu)
//            );
//            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", entities);
//            System.out.println("✅ Clic JS réussi sur 'Entités'");
//            Thread.sleep(800);
//        } catch (Exception e) {
//            System.out.println("❌ Toutes les tentatives ont échoué pour 'Entités'");
//            throw new RuntimeException("Impossible de cliquer sur le menu Entités", e);
//        }
    }

    private void clickOnSubMenu() {
        System.out.println("Tentative de clic sur le sous-menu 'Catégories d'entreprise'...");
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("div.slide-down")
            ));
            Thread.sleep(300);
        } catch (Exception e) {
            System.out.println("⚠️ slide-down non détecté, on continue...");
        }

//        try {
//            WebElement subMenu = wait.until(
//                    ExpectedConditions.elementToBeClickable(companyCategoriesSubMenu)
//            );
//            subMenu.click();
//            System.out.println("✅ Clic réussi sur 'Catégories d'entreprise'");
//            return;
//        } catch (Exception e) {
//            System.out.println("⚠️ Clic normal échoué : " + e.getMessage());
//        }
//
//        try {
//            WebElement subMenu = driver.findElement(companyCategoriesSubMenu);
//            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", subMenu);
//            System.out.println("✅ Clic JS réussi sur 'Catégories d'entreprise'");
//        } catch (Exception e) {
//            System.out.println("❌ Échec total sur le sous-menu");
//            throw new RuntimeException("Impossible de cliquer sur Catégories d'entreprise", e);
//        }
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
                field.clear();
                Thread.sleep(200);
                field.sendKeys(name);
                System.out.println("✅ Nom saisi : " + name);
                Thread.sleep(500);
                return;
            } catch (org.openqa.selenium.StaleElementReferenceException e) {
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
                field.clear();
                Thread.sleep(200);
                field.sendKeys(code);
                System.out.println("✅ Code saisi : " + code);
                Thread.sleep(300);
                return;
            } catch (org.openqa.selenium.StaleElementReferenceException e) {
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

        // Étape 1 — attendre que la card soit visible
        WebElement row = wait.until(
                ExpectedConditions.visibilityOfElementLocated(searchResultRow)
        );

        // Étape 2 — scroller
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", row
        );
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        // Étape 3 — clic Actions (vrai clic natif)
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

        // Étape 4 — si bouton toujours disabled, clic sur div interne
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

        // Étape 5 — attendre activation du bouton
        System.out.println("Attente activation bouton Sélectionner...");
        wait.until(driver -> {
            try {
                WebElement b = driver.findElement(selectCompanyButton);
                return b.getAttribute("disabled") == null
                        && !"true".equals(b.getAttribute("aria-disabled"));
            } catch (Exception ex) { return false; }
        });

        // Étape 6 — cliquer Sélectionner
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

    public String getErrorMessage() {
        // Priorité 1 : validation inline Quasar (role="alert")
        try {
            WebElement alert = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(validationAlert));
            String text = alert.getText().trim();
            System.out.println("✅ Message d'erreur trouvé : " + text);
            return text;
        } catch (Exception ignored) {}

        // Priorité 2 : toast Quasar
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

   // attend 1.5s que la recherche se termine, puis cherche dans le DOM les cards contenant des données réelles
    // d'entreprise  (texte "entreprise :" ou "ID de"). Si aucune card de ce type n'existe → pas de résultats → retourne true.

    public boolean isNoResultsMessageDisplayed() {
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
        List<WebElement> resultCards = driver.findElements(realResultCard);
        System.out.println("  → Nombre de cards résultat : " + resultCards.size());
        boolean noResults = resultCards.isEmpty();
        System.out.println(noResults ? "✅ Aucun résultat trouvé" : "⚠️ Des résultats existent");
        return noResults;
    }


    //cherche le bouton "Sélectionner" et vérifie s'il est désactivé via les attributs disabled ou aria-disabled.
   // Si le bouton est absent du DOM (exception), retourne true aussi car bouton absent = non cliquable.
    public boolean isCompanySelectionDisabled() {
        try {  //assertion pas de resultat
            WebElement btn = driver.findElement(selectCompanyButton);
            String disabled = btn.getAttribute("disabled");
            String ariaDisabled = btn.getAttribute("aria-disabled");
            return disabled != null || "true".equals(ariaDisabled);
        } catch (Exception e) {
            return true;
        }
    }
}