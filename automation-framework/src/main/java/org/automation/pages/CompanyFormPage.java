package org.automation.pages;

import org.automation.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class CompanyFormPage extends BasePage {

    // =========================
    // Locators - Form fields
    // =========================
    private By companyNameInput = By.id("company-name");
    private By siretInput       = By.id("company-siren");
    private By openIdInput      = By.id("company-open-id");

    // =========================
    // Locators - Save / Actions
    // =========================
    private By saveButton = By.xpath(
            "//button[.//span[text()='Enregistrer']] | " +
                    "//button[.//span[contains(.,'Modifier')]] | " +
                    "//button[.//span[text()='Sauvegarder']]"
    );

    // =========================
    // Locators - Messages
    // =========================
    private By successToast   = By.xpath("//div[contains(@class,'q-notification')]");
    private By validationAlert = By.xpath("//div[@role='alert' and normalize-space(.) != '']");

    public CompanyFormPage() {
        super();
    }

    // =========================
    // Form filling
    // =========================
    public void setCompanyName(String name) {
        fillField(companyNameInput, name, "Nom entreprise");
    }

    public void setSiret(String siret) {
        fillField(siretInput, siret, "Siret");
    }

    public void setOpenId(String openId) {
        fillField(openIdInput, openId, "ID dans Open");
    }

    public void clearSiret() {
        clearField(siretInput, "Siret");
    }

    // =========================
    // Private helpers
    // =========================
    private void fillField(By locator, String value, String fieldLabel) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                WebElement field = wait.until(
                        ExpectedConditions.presenceOfElementLocated(locator)
                );
                scrollToCenter(field);
                field.click();
                Thread.sleep(200);
                field = driver.findElement(locator);
                selectAll(field);
                field.sendKeys(value);
                System.out.println("✅ " + fieldLabel + " saisi : " + value);
                Thread.sleep(300);
                return;
            } catch (StaleElementReferenceException e) {
                System.out.println("⚠️ StaleElement " + fieldLabel + ", tentative " + attempt + "/3");
                sleep(500);
            } catch (InterruptedException ignored) {}
        }
        throw new RuntimeException("Impossible de saisir " + fieldLabel + " après 3 tentatives");
    }

    private void clearField(By locator, String fieldLabel) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                WebElement field = wait.until(
                        ExpectedConditions.presenceOfElementLocated(locator)
                );
                scrollToCenter(field);
                field.click();
                Thread.sleep(200);
                field = driver.findElement(locator);
                selectAll(field);
                field.sendKeys(Keys.DELETE);
                System.out.println("✅ " + fieldLabel + " vidé");
                Thread.sleep(300);
                return;
            } catch (StaleElementReferenceException e) {
                System.out.println("⚠️ StaleElement " + fieldLabel + ", tentative " + attempt + "/3");
                sleep(500);
            } catch (InterruptedException ignored) {}
        }
        throw new RuntimeException("Impossible de vider " + fieldLabel + " après 3 tentatives");
    }

    private void scrollToCenter(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", element
        );
    }

    private void selectAll(WebElement field) {
        field.sendKeys(Keys.CONTROL + "a");
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    // =========================
    // Save
    // =========================
    public void save() {
        wait.until(ExpectedConditions.elementToBeClickable(saveButton)).click();
        System.out.println("✅ Formulaire entreprise sauvegardé");
    }

    // =========================
    // Messages — Succès
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

    public boolean isSuccessMessageDisplayed() {
        try {
            String msg = getSuccessMessage();
            boolean displayed = !msg.isEmpty();
            System.out.println(displayed
                    ? "✅ Message succès entreprise : " + msg
                    : "⚠️ Aucun message succès entreprise");
            return displayed;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isEditSuccessMessageDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[contains(@class,'q-notification') and " +
                            "(contains(.,'modifiée') or contains(.,'mise à jour') or contains(.,'modifié'))]")
            ));
            return true;
        } catch (Exception e) {
            // Fallback : n'importe quel toast présent
            return isSuccessMessageDisplayed();
        }
    }

    // =========================
    // Messages — Erreur
    // =========================
    public String getErrorMessage() {
        try {
            WebElement alert = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(validationAlert));
            String text = alert.getText().trim();
            System.out.println("✅ Erreur entreprise : " + text);
            return text;
        } catch (Exception e) {
            throw new RuntimeException("Aucun message d'erreur trouvé sur le formulaire entreprise", e);
        }
    }

    public boolean isValidationErrorDisplayed() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(validationAlert));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // =========================
    // Utilitaires
    // =========================
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
