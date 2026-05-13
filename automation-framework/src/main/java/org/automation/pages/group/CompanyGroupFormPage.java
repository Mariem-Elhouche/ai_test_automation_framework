package org.automation.pages.group;

import org.automation.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class CompanyGroupFormPage extends BasePage {

    // Champs
    private final By nameInput = By.id("new-company-group-name");
    private final By openIdInput = By.id("company-group-open-id");
    private final By typeTrigger = By.xpath("//label[contains(@class,'q-select')][.//span[normalize-space()='Type'] or .//div[@id='new-company-group-type']]");

    // Bouton save
    private final By saveButton = By.xpath("//button[.//span[contains(normalize-space(),'Enregistrer')] or normalize-space(.)='Enregistrer']");

    // Message d'erreur validation nom vide
    private final By nameValidationError = By.xpath(
            "//*[contains(normalize-space(),'Le champ ne peut pas être vide')]"
    );

    // Toast succès
    private final By successToast = By.xpath("//div[contains(@class,'q-notification')]");
    private final By editSuccessToast = By.xpath(
            "//div[contains(@class,'q-notification')]" +
                    "[contains(.,'modifié') or contains(.,'modifie') or contains(.,'modification')]"
    );
    public CompanyGroupFormPage() {
        super();
    }

    public void setName(String name) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(nameInput));
        field.click();
        field.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        field.sendKeys(name);
    }

    public void clearName() {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(nameInput));
        field.click();
        field.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
    }

    public void setOpenId(String openId) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(openIdInput));
        field.click();
        field.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        field.sendKeys(openId);
    }

    public void setType(String type) {
        WebElement trigger = wait.until(ExpectedConditions.elementToBeClickable(typeTrigger));
        trigger.click();
        By option = By.xpath(
                "//div[contains(@class,'q-menu')]//*[@role='option' or contains(@class,'q-item')]" +
                        "[contains(normalize-space(.),'" + type + "')]"
        );
        wait.until(ExpectedConditions.elementToBeClickable(option)).click();
    }

    public void save() {
        wait.until(ExpectedConditions.elementToBeClickable(saveButton)).click();
    }


    public boolean isGroupDetailsFormDisplayed() {
        try {
            // Vérifie qu'on est bien sur la page details
            wait.until(ExpectedConditions.urlMatches(".*/company-groups/\\d+$"));

            // Champs du formulaire (réutilisation des locators existants)
            wait.until(ExpectedConditions.visibilityOfElementLocated(nameInput));
            wait.until(ExpectedConditions.visibilityOfElementLocated(openIdInput));
            wait.until(ExpectedConditions.visibilityOfElementLocated(typeTrigger));

            return true;

        } catch (Exception e) {
            return false;
        }
    }
    public boolean isSuccessMessageDisplayed() {
        try {
            WebElement toast = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[contains(@class,'q-notification')]")
            ));

            wait.until(ExpectedConditions.textToBePresentInElement(toast, "créé"));

            String text = toast.getText();
            System.out.println("Toast: " + text);

            return true;
        } catch (Exception e) {
            return false;
        }
    }



    public boolean isEditSuccessMessageDisplayed() {
        try {
            WebElement toast = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(editSuccessToast)
            );

            String text = toast.getText();
            System.out.println("EDIT Toast: " + text);

            return text.toLowerCase().contains("modifié")
                    || text.toLowerCase().contains("modifie")
                    || text.toLowerCase().contains("terminée")
                    || text.toLowerCase().contains("terminee");

        } catch (Exception e) {
            return false;
        }
    }

    public boolean isNameValidationErrorDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(nameValidationError));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isFormDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(nameInput));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}