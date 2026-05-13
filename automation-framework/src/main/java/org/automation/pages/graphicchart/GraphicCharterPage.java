package org.automation.pages.graphicchart;

import org.automation.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class GraphicCharterPage extends BasePage {


    private static final String FORM_URL = "https://stg-bo.noveocare.com/general/graphical-charter/new";

    // ════════════════════════════════════
    // LOCATORS
    // ════════════════════════════════════
    private final By addCharterBtn = By.xpath("//span[@class='q-mr-sm text-link']");
    private final By inputName = By.id("add-graphical-charter-name");
    // ════ IMAGES — labels q-file (par ordre d'apparition dans le DOM) ════
    private final By logoPrincipalLabel    = By.xpath("(//label[contains(@class,'q-file')])[1]");
    private final By logoSecondaireLabel   = By.xpath("(//label[contains(@class,'q-file')])[2]");
    private final By faviconLabel          = By.xpath("(//label[contains(@class,'q-file')])[3]");
    private final By imageAssureLabel      = By.xpath("(//label[contains(@class,'q-file')])[4]");
    private final By imageAffiliationLabel = By.xpath("(//label[contains(@class,'q-file')])[5]");
    private final By imageBackofficeLabel  = By.xpath("(//label[contains(@class,'q-file')])[6]");
    private final By imageWebRHLabel       = By.xpath("(//label[contains(@class,'q-file')])[7]");
    private final By imageHeaderLabel      = By.xpath("(//label[contains(@class,'q-file')])[8]");

    // ════ FONTS ════
    private final By fontTextLabel  = By.xpath("(//label[contains(@class,'q-file')])[9]");
    private final By fontTitleLabel = By.xpath("(//label[contains(@class,'q-file')])[10]");
    // Fonts
    private final By fontTextInput = By.xpath(
            "(//label[contains(@class,'q-file') and contains(@class,'hidden')])[1]" +
                    "/following-sibling::input[@type='file'] | " +
                    "(//label[contains(@class,'q-file')])[1]//input[@type='file'] | " +
                    "(//input[@type='file'])[9]"
    );
    private final By inputFontTextName  = By.xpath("(//input[@id='add-graphical-charter-font-title'])[1]");
    private final By fontTitleInput = By.xpath(
            "(//label[contains(@class,'q-file') and contains(@class,'hidden')])[2]" +
                    "/following-sibling::input[@type='file'] | " +
                    "(//label[contains(@class,'q-file')])[2]//input[@type='file'] | " +
                    "(//input[@type='file'])[10]"
    );

    private final By inputFontTitleName = By.xpath("(//input[@id='add-graphical-charter-font-title'])[2]");
    // Colors
    private final By inputPrimaryColor = By.xpath("//label[contains(.,'Couleur primaire')]/following-sibling::input");
    private final By primaryBoldColorInput = By.xpath("(//input[@name='add-graphical-charter-name'])[3]");
    private final By primaryBgColorInput = By.xpath("(//input[@name='add-graphical-charter-name'])[4]");
    private final By inputSecondaryColor = By.xpath("(//input[@name='add-graphical-charter-name'])[5]");
    private final By secondaryBoldColorInput = By.xpath("(//input[@name='add-graphical-charter-name'])[6]");
    private final By secondaryBgColorInput = By.xpath("(//input[@name='add-graphical-charter-name'])[7]");

    // Border radius
    private final By smallBorderRadiusInput = By.xpath("(//input[@name='add-graphical-charter-name'])[8]");
    private final By mediumBorderRadiusInput = By.xpath("(//input[@name='add-graphical-charter-name'])[9]");
    private final By largeBorderRadiusInput = By.xpath("(//input[@name='add-graphical-charter-name'])[10]");

    // Textareas
    private final By textareaDescription = By.id("add-graphical-charter-description");
    private final By keywordsCombobox = By.xpath(
            "//label[contains(@class,'q-field') and .//input[@role='combobox']]" +
                    "//input[@role='combobox']"
    );
    // Save
    private final By saveBtn = By.xpath("//button[.//span[normalize-space()='Enregistrer']]");
    private final By toastSuccess = By.xpath("//div[contains(@class,'q-notification')]//div[contains(text(),'Opération terminée')]");
    private final By emptyFieldError = By.xpath("//div[@role='alert' and text()='Le champ ne peut pas être vide']");
    private final By invalidColorError = By.xpath("//div[@role='alert' and text()='Couleur invalide']");
    // ════════════════════════════════════
    // CONSTRUCTEUR
    // ════════════════════════════════════
    public GraphicCharterPage() { super(); }

    public void navigateToFormPage() {
        driver.get(FORM_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(inputName));
        System.out.println("Formulaire des Chartes Graphiques chargée");
    }

    // ════════════════════════════════════
    // ACTIONS
    // ════════════════════════════════════
    public void clickAddCharter() {
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(addCharterBtn));
        // 1. Scroll vers l'élément
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", btn);
        // 2. Attendre que plus rien ne le recouvre
        wait.until(ExpectedConditions.elementToBeClickable(btn)); // 3. Essayer le clic normal, sinon fallback JS
         try { btn.click(); } catch (ElementClickInterceptedException e) {
             ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn); }
    }
    public void setName(String name) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(inputName));
        input.clear();
        input.sendKeys(name);
    }

    private void uploadFile(By labelLocator, String path) {
        WebElement label = wait.until(ExpectedConditions.presenceOfElementLocated(labelLocator));
        String inputId = label.getAttribute("for");
        System.out.println("Label class: " + label.getAttribute("class"));
        System.out.println("Input id dynamique : " + inputId);

        if (inputId == null || inputId.isEmpty()) {
            throw new RuntimeException("Label sans attribut 'for' : " + labelLocator);
        }

        WebElement input = driver.findElement(By.id(inputId));

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].style.display = 'block';" +
                        "arguments[0].style.opacity = '1';" +
                        "arguments[0].style.visibility = 'visible';" +
                        "arguments[0].removeAttribute('hidden');",
                input
        );

        input.sendKeys(path);

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));" +
                        "arguments[0].dispatchEvent(new Event('input',  { bubbles: true }));",
                input
        );

        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // ══ Vérification : est-ce que Quasar a affiché un aperçu ? ══
        // Après un upload réussi, le label q-file affiche le nom du fichier
        String labelText = label.getText();
        String inputValue = (String) ((JavascriptExecutor) driver)
                .executeScript("return arguments[0].value;", input);
        System.out.println("Label text après upload : '" + labelText + "'");
        System.out.println("Input value après upload : '" + inputValue + "'");
        System.out.println(" Upload envoyé : " + path);
    }


    private void uploadViaModifierButton(String labelText, String path) {
        By labelInSection = By.xpath(
                "//span[contains(.,'" + labelText + "')]/following-sibling::div//label[contains(@class,'q-file')] | " +
                        "//span[contains(.,'" + labelText + "')]/../following-sibling::div//label[contains(@class,'q-file')]"
        );

        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        WebElement label = wait.until(ExpectedConditions.presenceOfElementLocated(labelInSection));

        // Scroll vers le label avant l'upload
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", label);
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}

        String inputId = label.getAttribute("for");
        System.out.println("[" + labelText + "] input id: " + inputId);

        // === reste de la logique inchangée ===
        WebElement input = driver.findElement(By.id(inputId));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].style.display='block';" +
                        "arguments[0].style.opacity='1';" +
                        "arguments[0].style.visibility='visible';" +
                        "arguments[0].removeAttribute('hidden');",
                input
        );
        input.sendKeys(path);

        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        try {
            WebElement freshInput = driver.findElement(By.id(inputId));
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));" +
                            "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));",
                    freshInput
            );
        } catch (Exception e) {
            System.out.println("[" + labelText + "] dispatchEvent ignoré (fichier déjà traité) : " + e.getClass().getSimpleName());
        }

        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        System.out.println("✔ [" + labelText + "] Upload : " + path);
    }


    public void uploadLogoPrincipal(String path)    { uploadViaModifierButton("Logo", path); }
    public void uploadLogoSecondaire(String path)    { uploadViaModifierButton("Logo 2", path); }
    public void uploadFavicon(String path)           { uploadViaModifierButton("Favicon", path); }
    public void uploadImageEspaceAssure(String path) { uploadViaModifierButton("Image page connexion pour l’espace assuré", path); }
    public void uploadImageAffiliation(String path)  { uploadViaModifierButton("Image page connexion pour le parcours d’affiliation", path); }
    public void uploadImageBackoffice(String path) {
        uploadViaModifierButton("backoffice", path);
    }
    public void uploadImageWebRH(String path) {
        uploadViaModifierButton("Web RH", path);
    }
    public void uploadImageHomeHeader(String path) {
        uploadViaModifierButton("Home header", path);
    }
    public void uploadFontText(String path) {
        uploadViaModifierButton("Font de texte", path);
    }
    public void uploadFontTitle(String path) {
        uploadViaModifierButton("Font des titres", path);
    }
    public void fillFontTextName(String name) { driver.findElement(inputFontTextName).sendKeys(name); }
    public void fillFontTitleName(String name) { driver.findElement(inputFontTitleName).sendKeys(name); }

    private WebElement findFirstDisplayed(By... locators) {
        for (By locator : locators) {
            java.util.List<WebElement> elements = driver.findElements(locator);
            if (elements.isEmpty()) {
                continue;
            }
            for (WebElement element : elements) {
                if (element.isDisplayed()) {
                    return element;
                }
            }
            return elements.get(0);
        }
        throw new NoSuchElementException("Aucun élément trouvé pour les locators fournis");
    }

    private void clearAndType(WebElement input, String value) {
        try {
            input.click();
            input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
            input.sendKeys(value);
        } catch (InvalidElementStateException e) {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].value = arguments[1];" +
                            "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));" +
                            "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                    input, value
            );
        }
    }

    private int getColorOrder(String fieldKey) {
        switch (fieldKey) {
            case "primary color": return 1;
            case "primary background color": return 3;
            case "secondary color": return 4;
            case "secondary background color": return 6;
            default: return 1;
        }
    }

    private WebElement resolveColorInput(String fieldKey, By... preferredLocators) {
        if (driver.findElements(By.xpath("//input[@name='add-graphical-charter-name']")).isEmpty()) {
            java.util.List<WebElement> colorSectionToggles = driver.findElements(
                    By.xpath("//*[contains(@class,'q-expansion-item') and .//*[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'couleur') or contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'color')]]")
            );
            if (!colorSectionToggles.isEmpty()) {
                try {
                    WebElement toggle = colorSectionToggles.get(0);
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", toggle);
                    toggle.click();
                } catch (Exception ignored) {
                }
            }
        }

        new WebDriverWait(driver, Duration.ofSeconds(10)).until(d ->
                !d.findElements(By.id("add-graphical-charter-name")).isEmpty()
                        || !d.findElements(By.xpath("//input[not(@type='hidden') and not(@type='file')]")).isEmpty()
                        || !d.findElements(By.xpath("//textarea")).isEmpty()
                        || !d.findElements(By.xpath("//input[@id='add-graphical-charter-font-title']")).isEmpty()
                        || !d.findElements(By.xpath("//input[@id='add-graphical-charter-description']")).isEmpty()
                        || !d.findElements(By.xpath("//input[@name='add-graphical-charter-name']")).isEmpty()
                        || !d.findElements(By.xpath("//input[contains(@placeholder,'#') or contains(@class,'color')]")).isEmpty()
                        || !d.findElements(By.xpath("//input[contains(translate(@id,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'color') or contains(translate(@name,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'color')]")).isEmpty()
        );

        try {
            return findFirstDisplayed(preferredLocators);
        } catch (NoSuchElementException ignored) {
        }

        int expectedOrder = getColorOrder(fieldKey);

        java.util.List<WebElement> placeholderCandidates = driver.findElements(
                By.xpath("//input[contains(@placeholder,'#') and not(@type='hidden') and not(@type='file')]")
        );
        if (placeholderCandidates.size() >= expectedOrder) {
            return placeholderCandidates.get(expectedOrder - 1);
        }

        java.util.List<WebElement> nameCandidates = driver.findElements(
                By.xpath("//input[@name='add-graphical-charter-name' and not(@type='hidden') and not(@type='file')]")
        );
        int legacyIndex;
        switch (fieldKey) {
            case "primary color": legacyIndex = 2; break;
            case "primary background color": legacyIndex = 4; break;
            case "secondary color": legacyIndex = 5; break;
            case "secondary background color": legacyIndex = 7; break;
            default: legacyIndex = -1;
        }
        if (legacyIndex > 0 && nameCandidates.size() >= legacyIndex) {
            return nameCandidates.get(legacyIndex - 1);
        }

        java.util.List<WebElement> genericCandidates = driver.findElements(
                By.xpath("//input[not(@type='hidden') and not(@type='file')]")
        );
        if (legacyIndex > 0 && genericCandidates.size() >= legacyIndex) {
            return genericCandidates.get(legacyIndex - 1);
        }

        throw new NoSuchElementException(
                "Champ couleur introuvable (" + fieldKey + "). placeholderCandidates=" + placeholderCandidates.size()
                        + ", nameCandidates=" + nameCandidates.size()
                        + ", genericCandidates=" + genericCandidates.size()
        );
    }

    // Colors
    public void selectPrimaryColor(String hex) {
        WebElement input = resolveColorInput(
                "primary color",
                By.xpath("//label[contains(normalize-space(.),'Couleur primaire')]/ancestor::*[contains(@class,'q-field')][1]//input[not(@type='hidden')]"),
                By.xpath("//*[contains(normalize-space(.),'Couleur primaire')]/following::input[not(@type='hidden')][1]"),
                By.xpath("(//input[@name='add-graphical-charter-name'])[2]"),
                inputPrimaryColor
        );
        clearAndType(input, hex);
        blurField(input);
    }

    public void selectPrimaryBoldColor(String hex) {
        WebElement input = findFirstDisplayed(primaryBoldColorInput);
        clearAndType(input, hex);
    }

    public void selectPrimaryBackgroundColor(String hex) {
        WebElement input = resolveColorInput(
                "primary background color",
                By.xpath("//*[contains(normalize-space(.),'background') and contains(normalize-space(.),'primaire')]/following::input[not(@type='hidden')][1]"),
                By.xpath("//*[contains(normalize-space(.),'fond') and contains(normalize-space(.),'primaire')]/following::input[not(@type='hidden')][1]"),
                primaryBgColorInput
        );
        clearAndType(input, hex);
        blurField(input);
    }

    public void selectSecondaryColor(String hex) {
        WebElement input = resolveColorInput(
                "secondary color",
                By.xpath("//label[contains(normalize-space(.),'Couleur secondaire')]/ancestor::*[contains(@class,'q-field')][1]//input[not(@type='hidden')]"),
                By.xpath("//*[contains(normalize-space(.),'Couleur secondaire')]/following::input[not(@type='hidden')][1]"),
                inputSecondaryColor
        );
        clearAndType(input, hex);
        blurField(input);
    }

    public void selectSecondaryBoldColor(String hex) {
        WebElement input = findFirstDisplayed(secondaryBoldColorInput);
        clearAndType(input, hex);
    }

    public void selectSecondaryBackgroundColor(String hex) {
        WebElement input = resolveColorInput(
                "secondary background color",
                By.xpath("//*[contains(normalize-space(.),'background') and contains(normalize-space(.),'secondaire')]/following::input[not(@type='hidden')][1]"),
                By.xpath("//*[contains(normalize-space(.),'fond') and contains(normalize-space(.),'secondaire')]/following::input[not(@type='hidden')][1]"),
                secondaryBgColorInput
        );
        clearAndType(input, hex);
        blurField(input);
    }

    // Border radius
    public void fillSmallBorderRadius(String value) { driver.findElement(smallBorderRadiusInput).clear(); driver.findElement(smallBorderRadiusInput).sendKeys(value); }
    public void fillMediumBorderRadius(String value) { driver.findElement(mediumBorderRadiusInput).clear(); driver.findElement(mediumBorderRadiusInput).sendKeys(value); }
    public void fillLargeBorderRadius(String value) { driver.findElement(largeBorderRadiusInput).clear(); driver.findElement(largeBorderRadiusInput).sendKeys(value); }

    // Text
    public void fillDescription(String text) { driver.findElement(textareaDescription).sendKeys(text); }
    public void fillKeywords(String keywordsRaw) {
        // Scroll vers le bas de la page sans dépendre du bouton Créer
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        try { Thread.sleep(400); } catch (InterruptedException ignored) {}

        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(keywordsCombobox));

        for (String token : keywordsRaw.split(",")) {
            String keyword = token.trim();
            if (keyword.isEmpty()) continue;
            try { input.click(); }
            catch (ElementClickInterceptedException e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", input);
            }
            input.sendKeys(keyword);
            try { Thread.sleep(300); } catch (InterruptedException ignored) {}
            input.sendKeys(Keys.ENTER);
            try { Thread.sleep(300); } catch (InterruptedException ignored) {}
            System.out.println("Mot clé ajouté : " + keyword);
        }
    }
    // Save
    public void save() {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(saveBtn));
        button.click();
        System.out.println("Bouton Enregistrer cliqué");
    }

    private By getFieldControlLocator(String fieldName) {
        switch (fieldName.trim().toLowerCase()) {
            case "charter name": return By.xpath("(//input[@id='add-graphical-charter-name'])[1]");
            case "font text name": return By.xpath("(//input[@id='add-graphical-charter-font-title'])[1]");
            case "font title name": return By.xpath("(//input[@id='add-graphical-charter-font-title'])[2]");
            case "description": return textareaDescription;
            case "small border radius": return smallBorderRadiusInput;
            case "medium border radius": return mediumBorderRadiusInput;
            case "large border radius": return largeBorderRadiusInput;
            case "logo principal": return By.xpath("(//label[contains(@class,'q-file')])[1]");
            case "favicon": return By.xpath("(//label[contains(@class,'q-file')])[3]");
            case "primary color": return inputPrimaryColor;
            case "secondary color": return inputSecondaryColor;
            case "primary background color": return primaryBgColorInput;
            case "secondary background color": return secondaryBgColorInput;
            default: throw new IllegalArgumentException("Champ non géré: " + fieldName);
        }
    }

    private WebElement getFieldControlElement(String fieldName) {
        switch (fieldName.trim().toLowerCase()) {
            case "primary color":
                return resolveColorInput(
                        "primary color",
                        By.xpath("//label[contains(normalize-space(.),'Couleur primaire')]/ancestor::*[contains(@class,'q-field')][1]//input[not(@type='hidden')]"),
                        By.xpath("//*[contains(normalize-space(.),'Couleur primaire')]/following::input[not(@type='hidden')][1]"),
                        By.xpath("(//input[@name='add-graphical-charter-name'])[2]")
                );
            case "secondary color":
                return resolveColorInput(
                        "secondary color",
                        By.xpath("//label[contains(normalize-space(.),'Couleur secondaire')]/ancestor::*[contains(@class,'q-field')][1]//input[not(@type='hidden')]"),
                        By.xpath("//*[contains(normalize-space(.),'Couleur secondaire')]/following::input[not(@type='hidden')][1]"),
                        inputSecondaryColor
                );
            case "primary background color":
                return resolveColorInput(
                        "primary background color",
                        By.xpath("//*[contains(normalize-space(.),'background') and contains(normalize-space(.),'primaire')]/following::input[not(@type='hidden')][1]"),
                        By.xpath("//*[contains(normalize-space(.),'fond') and contains(normalize-space(.),'primaire')]/following::input[not(@type='hidden')][1]"),
                        primaryBgColorInput
                );
            case "secondary background color":
                return resolveColorInput(
                        "secondary background color",
                        By.xpath("//*[contains(normalize-space(.),'background') and contains(normalize-space(.),'secondaire')]/following::input[not(@type='hidden')][1]"),
                        By.xpath("//*[contains(normalize-space(.),'fond') and contains(normalize-space(.),'secondaire')]/following::input[not(@type='hidden')][1]"),
                        secondaryBgColorInput
                );
            default:
                return wait.until(ExpectedConditions.presenceOfElementLocated(getFieldControlLocator(fieldName)));
        }
    }

    private String readInlineValidationMessage(WebElement field) {
        Object text = ((JavascriptExecutor) driver).executeScript(
                "const field = arguments[0];" +
                        "const container = field.closest('.q-field') || field.closest('[class*=q-field]');" +
                        "if (!container) return '';" +
                        "const alert = container.querySelector('[role=\"alert\"]');" +
                        "return alert ? alert.textContent.trim() : '';",
                field
        );
        return text == null ? "" : text.toString().trim();
    }

    public void focusAndBlurField(String fieldName) {
        WebElement field = getFieldControlElement(fieldName);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", field);
        try {
            field.click();
        } catch (ElementNotInteractableException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", field);
        }
        blurField(field);
    }

    public void clearAndBlurField(String fieldName) {
        WebElement field = getFieldControlElement(fieldName);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", field);
        try {
            field.click();
        } catch (ElementNotInteractableException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", field);
        }

        String tagName = field.getTagName().toLowerCase();
        if ("input".equals(tagName) || "textarea".equals(tagName)) {
            try {
                field.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
            } catch (InvalidElementStateException ignored) {
                ((JavascriptExecutor) driver).executeScript(
                        "if (arguments[0].value !== undefined) { arguments[0].value = ''; arguments[0].dispatchEvent(new Event('input', {bubbles:true})); }",
                        field
                );
            }
        }
        blurField(field);
    }

    private void blurField(WebElement field) {
        try {
            field.sendKeys(Keys.TAB);
        } catch (InvalidElementStateException ignored) {
            ((JavascriptExecutor) driver).executeScript("if (arguments[0].blur) { arguments[0].blur(); }", field);
            driver.findElement(By.tagName("body")).click();
        }
    }

    public String getFieldValidationMessage(String fieldName) {
        String normalized = fieldName.trim().toLowerCase();
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(d -> {
                        WebElement field = getFieldControlElement(fieldName);
                        String text = readInlineValidationMessage(field);
                        return text.isEmpty() ? null : text;
                    });
        } catch (TimeoutException e) {
            if (normalized.contains("color")) {
                try {
                    WebElement alert = new WebDriverWait(driver, Duration.ofSeconds(3))
                            .until(ExpectedConditions.visibilityOfElementLocated(invalidColorError));
                    return alert.getText().trim();
                } catch (TimeoutException ignored) {
                    return "";
                }
            }
            return "";
        }
    }


    public boolean isFieldValidationMessageDisplayed(String fieldLabel, String expectedMessage) {
        String normalizedExpected = normalizeText(expectedMessage);
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(d -> {
                        try {
                            WebElement el = getValidationMessageElement(fieldLabel);
                            // Lire via JS pour éviter les caractères fantômes insérés par le navigateur
                            Object raw = ((JavascriptExecutor) d)
                                    .executeScript("return arguments[0].textContent;", el);

                            String actual = normalizeText(raw == null ? "" : raw.toString());
                            System.out.println("[" + fieldLabel + "] actual (js): [" + actual + "]");
                            System.out.println("[" + fieldLabel + "] expected:    [" + normalizedExpected + "]");
                            return actual.equals(normalizedExpected);
                        } catch (Exception e) {
                            return null;
                        }
                    });
        } catch (Exception e) {
            return false;
        }
    }

    private String normalizeText(String text) {
        if (text == null) return "";

        return text
                .replace("\u00A0", " ")
                .replace("\u200B", "")
                .replace("\u200C", "")
                .replace("\u200D", "")
                .replace("\uFEFF", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private WebElement getValidationMessageElement(String fieldLabel) {
        switch (fieldLabel.trim().toLowerCase()) {

            case "primary color":
                return getAlertNearField(2);

            case "primary background color":
                return getAlertNearField(4);

            case "secondary color":
                return getAlertNearField(5);

            case "secondary background color":
                return getAlertNearField(7);

            default:
                throw new IllegalArgumentException("Label inconnu: " + fieldLabel);
        }
    }

    private WebElement getAlertNearField(int inputIndex) {
        By input = By.xpath("(//input[@name='add-graphical-charter-name'])[" + inputIndex + "]");

        WebElement field = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.presenceOfElementLocated(input));

        WebElement container = field.findElement(By.xpath("./ancestor::div[contains(@class,'q-field')]"));

        return new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> {
                    WebElement alert = container.findElement(By.xpath(".//div[@role='alert']"));
                    return (alert.isDisplayed() && !alert.getText().trim().isEmpty()) ? alert : null;
                });
    }

    private String getLabelTextForField(String fieldLabel) {
        switch (fieldLabel.trim().toLowerCase()) {
            case "primary color":           return "Couleur primaire";
            case "secondary color":         return "Couleur secondaire";
            case "primary background color":return "arri\u00e8re-plan primaire";
            case "secondary background color": return "arri\u00e8re-plan secondaire";
            case "charter name":            return "Nom";
            case "font text name":          return "Nom de font de texte";
            case "font title name":         return "Nom de font des titres";
            case "description":             return "Description";
            case "small border radius":     return "Arrondi petit";
            case "medium border radius":    return "Arrondi moyen";
            case "large border radius":     return "Arrondi large";
            default: throw new IllegalArgumentException("Label inconnu: " + fieldLabel);
        }
    }



    public void enterAndBlurColorField(String fieldLabel, String value) {
        WebElement input = getColorInputByLabel(fieldLabel);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", input);
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        input.sendKeys(value);
        input.sendKeys(Keys.TAB);
    }

    private WebElement getColorInputByLabel(String fieldLabel) {
        switch (fieldLabel.trim().toLowerCase()) {
            case "primary color":
                return driver.findElement(By.xpath("(//input[@name='add-graphical-charter-name'])[2]"));
            case "primary color bold":
                return driver.findElement(By.xpath("(//input[@name='add-graphical-charter-name'])[3]"));
            case "primary background color":
                return driver.findElement(By.xpath("(//input[@name='add-graphical-charter-name'])[4]"));
            case "secondary color":
                return driver.findElement(By.xpath("(//input[@name='add-graphical-charter-name'])[5]"));
            case "secondary bold color":
                return driver.findElement(By.xpath("(//input[@name='add-graphical-charter-name'])[6]"));
            case "secondary background color":
                return driver.findElement(By.xpath("(//input[@name='add-graphical-charter-name'])[7]"));
            default:
                throw new IllegalArgumentException("Champ couleur inconnu: " + fieldLabel);
        }
    }


    public int getEmptyFieldErrorCount() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(d -> !d.findElements(emptyFieldError).isEmpty());
            return driver.findElements(emptyFieldError).size();
        } catch (TimeoutException e) {
            return 0;
        }
    }

    public int getInvalidColorErrorCount() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(d -> !d.findElements(invalidColorError).isEmpty());
            return driver.findElements(invalidColorError).size();
        } catch (TimeoutException e) {
            return 0;
        }
    }
    public boolean isCharterCreationSuccessDisplayed() {
        try {
            Thread.sleep(2000);

            // Debug temporaire
            driver.findElements(By.xpath("//*[contains(@class,'q-notif') or contains(@class,'q-notification')]"))
                    .forEach(el -> System.out.println(
                            "TOAST CANDIDAT → class: " + el.getAttribute("class") +
                                    " | text: " + el.getText()
                    ));

            WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(15));
            return longWait.until(ExpectedConditions.visibilityOfElementLocated(toastSuccess)).isDisplayed();

        } catch (Exception e) {
            System.out.println("Toast non trouvé : " + e.getMessage());
            return false;
        }
    }

    // Modifier / Supprimer générique
    public void clickActionOnElement(String elementName, String action) {
        String xpath = String.format("//div[contains(.,'%s')]/following-sibling::div//%s",
                elementName,
                action.equalsIgnoreCase("Modifier") ? "span[text()='Modifier']" : "i[text()='delete']");
        driver.findElement(By.xpath(xpath)).click();
    }
}
