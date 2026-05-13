package org.automation.pages.company;

import org.automation.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.interactions.Actions;
import java.time.Duration;
import org.openqa.selenium.TimeoutException;

public class CompanyFormPage extends BasePage {

    //Form fields
    private By companyNameInput     = By.id("company-name");
    private By siretInput           = By.id("company-siren");
    private By openIdInput          = By.id("company-open-id");
    private By addressInput         = By.id("company-address");
    private By addressComplementInput = By.id("company-additional-address");
    //postalcode possède un id dynamique qui m'aide dans le self healing ( à changer plus tard )
    //private By postalCodeInput      = By.id("f_62655332-1fa4-48f0-aeb1-8b4953418640");
    private By postalCodeInput = By.xpath("//div[contains(text(),'Code postal')]/following::input[1]");
    private By cityInput            = By.id("company-city");
    private By countryInput         = By.id("company-country");
    private By emailInput           = By.id("company-email");
    private By phoneInput = By.xpath("//div[@id='company-phone']//input[@type='text']");
    private By environmentInput = By.xpath("//div[contains(text(),'Environnement li')]/following::input[@role='combobox'][1]");
    private By saveButton = By.xpath(
            "//button[.//span[text()='Enregistrer']] | " +
                    "//button[.//span[contains(.,'Modifier')]] | " +
                    "//button[.//span[text()='Sauvegarder']]"
    );
    // Messages
    //private final By successToast = By.xpath("//div[contains(@class,'q-notification__message')]");
    private By successToast = By.xpath("//div[contains(@class,'q-notification--positive')]");
    private By validationAlert = By.xpath("//div[@role='alert' and normalize-space(.) != '']");



    //*******************
    // Methods
    //*******************

    public CompanyFormPage() {
        super();
    }
    private void scrollToElement(By locator) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        new Actions(driver).moveToElement(element).perform();
    }

    public void setCompanyName(String name) {
        fillField(companyNameInput, name, "Nom entreprise");
    }

    public void setSiret(String siret) {
        fillField(siretInput, siret, "Siret");
    }

    public void setOpenId(String openId) {
        scrollToElement(openIdInput);
        fillField(openIdInput, openId, "ID dans Open");
    }

    public void clearSiret() {clearField(siretInput, "Siret");}

    public String getCurrentUrl() {return driver.getCurrentUrl();}

    public void setAddress(String address) {fillField(addressInput, address, "Adresse");}

    public void setAddressComplement(String complement) {fillField(addressComplementInput, complement, "Complément adresse");}

    public void setPostalCode(String postalCode) {fillField(postalCodeInput, postalCode, "Code postal");}

    public void setCity(String city) {fillField(cityInput, city, "Ville");}

    public void setCountry(String country) {fillField(countryInput, country, "Pays");}

    public void setEmail(String email) {
        scrollToElement(emailInput);
        fillField(emailInput, email, "Email");
    }

    public void setPhone(String phone) {
        scrollToElement(phoneInput);
        fillField(phoneInput, phone, "Téléphone");
    }
    public void clearName() {clearField(companyNameInput, "Nom entreprise");}


    public void selectEnvironment(String environmentName) {
        scrollToElement(environmentInput);
        By suggestionLocator = By.xpath(
                "//div[contains(@class,'q-menu') or contains(@class,'q-virtual-scroll')]" +
                        "//div[contains(@class,'q-item')][contains(normalize-space(),'" + environmentName + "')]"
        );

        WebElement field = wait.until(ExpectedConditions.elementToBeClickable(environmentInput));
        field.click();
        //sleep(200);
        field.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        field.sendKeys(environmentName);
        wait.until(ExpectedConditions.visibilityOfElementLocated(suggestionLocator));
        wait.until(ExpectedConditions.elementToBeClickable(suggestionLocator)).click();
        System.out.println("Environnement sélectionné : " + environmentName);
       // sleep(300);
    }
    private void fillField(By locator, String value, String label) {
        WebElement field = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        field.click();
       // sleep(200);
        field.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        field.sendKeys(value);
        System.out.println( label + " saisi : " + value);
       // sleep(300);
    }

    private void clearField(By locator, String label) {
        WebElement field = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        field.click();
        //sleep(200);
        field.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        System.out.println( label + " vidé");
        //sleep(300);
    }

    /*private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }*/

    public void save() {
        // 🔥 attendre que tout soit stable (petit délai UI)
        try {
            Thread.sleep(500); // ou mieux : wait custom si loader existe
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(saveButton));
        btn.click();

        System.out.println("Formulaire entreprise sauvegardé");
    }

//ça ne marche pas car le toast apparait dans des cas et d'autres non ,
    // on doit la verifier par une verification par url
    public boolean isSuccessMessageDisplayed() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.visibilityOfElementLocated(successToast));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }


    // Vérifie si la création a réussi en regardant l'URL
    public boolean isCompanyCreatedSuccessfully() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(d -> {
                        String url = d.getCurrentUrl();
                        // La création est réussie si on n'est plus sur /new
                        return !url.contains("/new");
                    });
            System.out.println("Création de l'entreprise réussie, URL : " + driver.getCurrentUrl());
            return true;
        } catch (TimeoutException e) {
            System.out.println("La création de l'entreprise a échoué, toujours sur la page de création.");
            return false;
        }
    }

    public boolean isEditFormDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//form[contains(@class,'q-form')]")
            )).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isEditSuccessMessageDisplayed() {
        return isSuccessMessageDisplayed();
    }

    public boolean isValidationErrorDisplayed() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(validationAlert));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }
}
