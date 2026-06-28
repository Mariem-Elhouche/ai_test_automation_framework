package org.automation.pages.faq;

import org.automation.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class FaqFormPage extends BasePage {

    private final By questionFrInput = By.xpath(
            "//div[contains(normalize-space(.),'Question en français')]" +
                    "/following::input[@type='text'][1]"
    );
    private final By questionEnInput = By.xpath(
            "//div[contains(normalize-space(.),'Question en anglais')]" +
                    "/following::input[@type='text'][1]"
    );
    private final By reponseFrEditor = By.xpath(
            "(//div[contains(@class,'cm-content') and contains(@class,'cm-lineWrapping')" +
                    " and @contenteditable='true'])[1]"
    );
    private final By reponseEnEditor = By.xpath(
            "(//div[contains(@class,'cm-content') and contains(@class,'cm-lineWrapping')" +
                    " and @contenteditable='true'])[2]"
    );
    private final By faqThemeCombobox = By.xpath(
            "//div[contains(normalize-space(.),'Thème lié à la question')]" +
                    "/following::input[@role='combobox'][1]"
    );
    private final By questionGlobaleToggle = By.xpath(
            "//div[contains(@class,'text-weight-semi-bold') and contains(normalize-space(.),'Question globale')]" +
                    "//div[contains(@class,'q-toggle')]"
    );
    private final By saveBtn = By.xpath(
            "//button[.//span[normalize-space(text())='Enregistrer']]"
    );
    private final By cancelBtn = By.xpath(
            "//button[.//span[normalize-space(text())='Annuler']]"
    );
    private final By faqThemeClearBtn = By.xpath(
            "//div[contains(normalize-space(.),'Thème lié à la question')]" +
                    "/following::i[normalize-space(text())='cancel'][1]"
    );
    private final By validationAlerts = By.xpath(
            "//form[contains(@class,'q-form')]" +
                    "//div[@role='alert']" +
                    "[normalize-space(text()) != '']" +
                    "[not(ancestor::div[contains(@style,'display: none')])]" +
                    "[not(ancestor::div[contains(@style,'visibility: hidden')])]"
    );
    private final By successToast = By.xpath(
            "//div[contains(@class,'q-notification__message')] | " +
                    "//div[contains(@class,'text-subtitle2')" +
                    " and contains(normalize-space(.),'Opération terminée')]"
    );

    public FaqFormPage() { super(); }

    public FaqFormPage waitUntilLoaded() {
        // Attendre une URL spécifique au formulaire — exclut la liste /faq
        wait.until(d -> {
            String url = d.getCurrentUrl();
            return url.contains("/faq/new")
                    || url.contains("/faq/create")
                    || url.matches(".*faq/\\d+.*")
                    || (url.contains("/faq") && !url.endsWith("/faq"));
        });
        wait.until(ExpectedConditions.visibilityOfElementLocated(questionFrInput));
        return this;
    }

    public void setQuestionFr(String text)  { fillField(questionFrInput, text, "Question FR"); }
    public void setQuestionEn(String text)  { fillField(questionEnInput, text, "Question EN"); }
    public void setReponseFr(String text)   { fillCodeMirror(reponseFrEditor, text, "Réponse FR"); }
    public void setReponseEn(String text)   { fillCodeMirror(reponseEnEditor, text, "Réponse EN"); }

    public void selectTheme(String theme) {
        WebElement field = wait.until(
                ExpectedConditions.elementToBeClickable(faqThemeCombobox));
        field.click();
        field.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        field.sendKeys(theme);
        By suggestion = By.xpath(
                "//div[contains(@class,'q-menu') or contains(@class,'q-virtual-scroll')]" +
                        "//div[contains(@class,'q-item')][contains(normalize-space(.),'" + theme + "')]"
        );
        wait.until(ExpectedConditions.elementToBeClickable(suggestion)).click();
    }

    public boolean isQuestionGlobaleEnabled() {
        try {
            WebElement t = driver.findElement(questionGlobaleToggle);
            String aria = t.getAttribute("aria-checked");
            String cls  = t.getAttribute("class");
            return "true".equals(aria) || (cls != null && cls.contains("q-toggle--truthy"));
        } catch (Exception e) { return false; }
    }

    public void setQuestionGlobale(boolean target) {
        if (isQuestionGlobaleEnabled() != target)
            scrollAndClick(questionGlobaleToggle, "Toggle Question globale");
    }
    public void clearTheme() {
        try {
            WebElement clearBtn = wait.withTimeout(java.time.Duration.ofSeconds(3))
                    .until(ExpectedConditions.elementToBeClickable(faqThemeClearBtn));
            clearBtn.click();
            // Attendre que le champ soit vide après le clear
            wait.until(d -> {
                try {
                    WebElement input = d.findElement(faqThemeCombobox);
                    String val = input.getAttribute("value");
                    // Le select Quasar vide aussi le texte affiché dans q-field__native
                    WebElement native_ = d.findElement(By.xpath(
                            "//div[contains(normalize-space(.),'Thème lié à la question')]" +
                                    "/following::div[contains(@class,'q-field__native')][1]"
                    ));
                    return val == null || val.isEmpty();
                } catch (Exception e) { return true; }
            });
        } catch (Exception e) {
            System.out.println("clearTheme — icône X non trouvée ou thème déjà vide : " + e.getMessage());
        }
    }

    public void enableQuestionGlobale() {
        setQuestionGlobale(true);
    }

    public void save()   { scrollAndClick(saveBtn,   "Enregistrer FAQ"); }
    public void cancel() { scrollAndClick(cancelBtn, "Annuler FAQ"); }

    public boolean isSavedSuccessfully() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(successToast));
            return true;
        } catch (Exception e) { return false; }
    }

    public boolean isValidationErrorDisplayed() {
        try {
            wait.withTimeout(java.time.Duration.ofSeconds(3))
                    .until(d -> {
                        List<WebElement> alerts = d.findElements(validationAlerts);
                        return alerts.stream().anyMatch(a -> {
                            try {
                                return a.isDisplayed() && !a.getText().trim().isEmpty();
                            } catch (Exception e) { return false; }
                        });
                    });
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void fillField(By locator, String value, String label) {
        WebElement field = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        new Actions(driver).moveToElement(field).perform();
        field.click();
        field.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        field.sendKeys(value);
        field.sendKeys(Keys.TAB);
    }

    private void fillCodeMirror(By locator, String text, String label) {
        try {
            WebElement editor = wait.until(ExpectedConditions.elementToBeClickable(locator));
            new Actions(driver).moveToElement(editor).click().perform();
            editor.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            editor.sendKeys(Keys.DELETE);
            editor.sendKeys(text);
        } catch (Exception e) {
            System.out.println("fillCodeMirror échoué : " + label + " — " + e.getMessage());
        }
    }

    private void scrollAndClick(By locator, String label) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
                new Actions(driver).moveToElement(el).perform();
                try {
                    el.click();
                } catch (ElementClickInterceptedException e) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
                }
                return;
            } catch (StaleElementReferenceException e) {
                attempts++;
                try { Thread.sleep(300); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }
        throw new RuntimeException("scrollAndClick échoué : " + label);
    }
}