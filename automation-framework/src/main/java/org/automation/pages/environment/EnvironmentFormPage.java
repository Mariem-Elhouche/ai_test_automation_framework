package org.automation.pages.environment;

import org.automation.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;
import java.util.logging.Logger;

public class EnvironmentFormPage extends BasePage {

    private static final Logger LOGGER = Logger.getLogger(EnvironmentFormPage.class.getName());

    // ─── Infos générales ───
    private final By nameInput              = By.id("environment-general");
    private final By siteDeGestionInput     = By.xpath("//div[contains(text(),'Site de gestion')]/following::input[@role='combobox'][1]");
    private final By addressInput           = By.id("environment-address");
    private final By addressComplementInput = By.id("environment-additional-address");

    // ─── Contact ───
    private final By emailAssuresInput     = By.id("environment-email");
    private final By emailRhInput          = By.id("environment-contact-eptica-email");
    private final By telephoneAssuresInput = By.xpath("//div[@id='environment-insured-phone']//input[@type='text']");
    private final By telephoneProInput     = By.xpath("//div[@id='environment-insured-professional-phone']//input[@type='text']");
    private final By nomFrontInput         = By.id("environment-brand");
    private final By nomExpediteurInput    = By.id("environment-email-sender-name");

    // ─── Calibrage ───
    private final By openNameInput              = By.id("environment-open-name");
    private final By openIdInput                = By.id("environment-open-id");
    private final By carteTpSelect              = By.id("environment-tpcard-mode");
    private final By modeDecompteSelect         = By.id("environment-decompte-mode");
    private final By doubleAuthSelect           = By.id("environment-insured-mfa");
    private final By methodeMfaSelect           = By.id("environment-insured-mfa-type");
    private final By methodeSecondaireMfaSelect = By.id("environment-insured-backup-mfa-type");
    private final By paysSmsSelect              = By.id("environment-mfa-sms-country-limitation");
    private final By bankNameInput              = By.id("environment-bank-name");
    private final By bankIcsInput               = By.id("environment-bank-ics");
    private final By rumRootInput               = By.id("environment-rum-roots");

    // ─── Images — uniquement les input[type=file], PAS les icônes edit ───
    // La clé du fix : on n'interagit JAMAIS avec l'icône (qui ouvre le dialog OS).
    // On attend directement l'input[type=file] rendu visible par le framework
    // et on y injecte le chemin via sendKeys — aucun JS, aucun dialog OS.
    private final By logoPrincipalUpload = By.xpath(
            "//div[contains(@class,'q-mt-lg')][.//div[contains(.,'Logo principal')]]//input[@type='file']"
    );
    private final By imageEnteteUpload = By.xpath(
            "//div[contains(@class,'q-mt-lg')][.//div[contains(.,'en-tête')]]//input[@type='file']"
    );
    private final By imageBasUpload = By.xpath(
            "//div[contains(@class,'q-mt-lg')][.//div[contains(.,'bas')]]//input[@type='file']"
    );

    // ─── Cancel icons ───
    private final By siteDeGestionCancelIcon = By.xpath(
            "//div[contains(@class,'q-field')][.//label[contains(.,'Site de gestion')]]" +
                    "//i[@aria-label='Clear' and normalize-space()='cancel']"
    );

    // ─── Save ───
    private final By saveButton = By.xpath(
            "//button[.//span[text()='Enregistrer']] | " +
                    "//button[.//span[contains(.,'Modifier')]]  | " +
                    "//button[.//span[text()='Sauvegarder']]"
    );

    // ─── Toasts ───
    private final By successToast = By.xpath(
            "//div[contains(@class,'q-notifications__list--top')]" +
                    "//div[contains(@class,'text-subtitle2') and contains(normalize-space(.), 'Opération terminée')]"
    );
    private final By duplicateError = By.xpath(
            "//div[contains(@class,'text-subtitle2') " +
                    "and contains(normalize-space(.), 'Veuillez vérifier vos informations')]"
    );
    private final By anyValidationAlert = By.xpath("//div[@role='alert']");

    public EnvironmentFormPage() { super(); }

    // ════════════════════════════════════════════
    //  Infos générales
    // ════════════════════════════════════════════

    public void setName(String name)                    { fillField(nameInput, name, "Nom"); }
    public void setAddress(String address)              { fillField(addressInput, address, "Adresse"); }
    public void setAddressComplement(String complement) { fillField(addressComplementInput, complement, "Complément adresse"); }
    public void clearName()                             { clearAndBlur(nameInput, "Nom"); }
    public void clearAddress()                          { clearAndBlur(addressInput, "Adresse"); }
    public void selectSiteDeGestion(String site)        { selectFromAutocomplete(siteDeGestionInput, site, "Site de gestion"); }

    public void clearSiteDeGestion() {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(siteDeGestionCancelIcon)).click();
            LOGGER.info("Site de gestion vidé via icône cancel");
        } catch (Exception e) {
            LOGGER.warning("Impossible de clear Site de gestion : " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════
    //  Contact
    // ════════════════════════════════════════════

    public void setEmailAssures(String email)          { fillField(emailAssuresInput, email, "Email assurés"); }
    public void setEmailRh(String email)               { fillField(emailRhInput, email, "Email RH"); }
    public void setTelephoneAssures(String tel)        { fillField(telephoneAssuresInput, tel, "Téléphone assurés"); }
    public void setTelephoneProfessionnels(String tel) { fillField(telephoneProInput, tel, "Téléphone professionnels"); }
    public void setNomFront(String nom)                { fillField(nomFrontInput, nom, "Nom front"); }
    public void setNomExpediteur(String nom)           { fillField(nomExpediteurInput, nom, "Nom expéditeur mails"); }

    public void clearEmailAssures()            { clearAndBlur(emailAssuresInput, "Email assurés"); }
    public void clearEmailRh()                 { clearAndBlur(emailRhInput, "Email RH"); }
    public void clearTelephoneAssures()        { clearAndBlur(telephoneAssuresInput, "Tel assurés"); }
    public void clearTelephoneProfessionnels() { clearAndBlur(telephoneProInput, "Tel pro"); }
    public void clearNomFront()                { clearAndBlur(nomFrontInput, "Nom front"); }

    // ════════════════════════════════════════════
    //  Calibrage
    // ════════════════════════════════════════════

    public void setOpenName(String v)  { fillField(openNameInput, v, "Nom dans Open"); }
    public void setOpenId(String v)    { fillField(openIdInput, v, "ID dans Open"); }
    public void setBankName(String v)  { fillField(bankNameInput, v, "Nom banque"); }
    public void setBankIcs(String v)   { fillField(bankIcsInput, v, "ICS banque"); }
    public void setRumRoot(String v)   { fillField(rumRootInput, v, "Racine RUM"); }

    public void clearOpenName() { clearAndBlur(openNameInput, "Open name"); }
    public void clearOpenId()   { clearAndBlur(openIdInput, "Open id"); }

    public void selectCarteTpMode(String mode)   { selectFromListbox(carteTpSelect, mode, "Mode Carte TP"); }
    public void selectModeDecompte(String value) { selectFromListbox(modeDecompteSelect, value, "Mode décompte"); }
    public void selectDoubleAuth(String value)   { selectFromListbox(doubleAuthSelect, value, "Double Auth"); }
    public void selectMethodeMfa(String value)   { selectFromListbox(methodeMfaSelect, value, "Méthode MFA"); }

    public void selectPaysSms(String pays) { fillField(paysSmsSelect, pays, "Pays SMS"); }

    public void clearDoubleAuth()  { clearDropdownCancelById("environment-insured-mfa", "Double Auth"); }
    public void clearCarteTpMode() { clearDropdownCancelById("environment-tpcard-mode", "Mode Carte TP"); }

    public void selectMethodeSecondaireMfa(String value)   { selectFromListbox(methodeSecondaireMfaSelect, value, "Méthode secondaire MFA"); }


    public void uploadLogoPrincipal(String absolutePath) {
        uploadFile(logoPrincipalUpload, absolutePath, "Logo principal");
    }

    public void uploadImageEntete(String absolutePath) {
        uploadFile(imageEnteteUpload, absolutePath, "Image entête");
    }

    public void uploadImageBas(String absolutePath) {
        uploadFile(imageBasUpload, absolutePath, "Image bas");
    }

    // ════════════════════════════════════════════
    //  Charte graphique
    // ════════════════════════════════════════════

    public void selectCharteGraphique(String charte) {

        //ce code à ajouter dans les scenarios de modification seulement
       /* By btnchange=By.xpath("//div[contains(text(),'Changer la charte graphique')]");
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(btnchange));
        btn.click();*/

        By combobox = By.xpath(
                "//h2[contains(normalize-space(.), 'Charte graphique')]" +
                        "/following::input[@role='combobox'][1]"
        );
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(combobox));
        input.clear();
        input.sendKeys(charte);

        // 3. Sélectionner la suggestion correspondante dans le menu
        By suggestion = By.xpath(
                "//div[contains(@class,'q-menu') or contains(@class,'q-virtual-scroll')]" +
                        "//div[contains(@class,'q-item')][contains(normalize-space(),'" + charte + "')]"
        );
        scrollAndClick(suggestion, "Suggestion charte " + charte);
        LOGGER.info("Charte graphique sélectionnée : " + charte);
    }

    // ════════════════════════════════════════════
    //  Save & Messages
    // ════════════════════════════════════════════

    public void save() {
        // 🔥 attendre que tout soit stable (petit délai UI)
        try {
            Thread.sleep(1300); // ou mieux : wait custom si loader existe
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(saveButton));
        btn.click();

        System.out.println("Formulaire entreprise sauvegardé");
    }


    public boolean isSuccessMessageDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(successToast));
            LOGGER.info("Toast succès détecté");
            return true;
        } catch (Exception e) {
            LOGGER.warning("Aucun toast de succès : " + e.getMessage());
            return false;
        }
    }

    public boolean isDuplicateErrorDisplayed() {
        try {
            wait.withTimeout(java.time.Duration.ofSeconds(15))
                    .until(ExpectedConditions.visibilityOfElementLocated(duplicateError));
            LOGGER.info("Toast doublon détecté");
            return true;
        } catch (Exception e) {
            LOGGER.warning("Aucun toast de doublon : " + e.getMessage());
            return false;
        }
    }

    public boolean isFieldValidationMessageDisplayed(String expectedMessage) {
        try {
            By locator = By.xpath(
                    "//div[@role='alert' and normalize-space(.)='" + expectedMessage + "']"
            );
            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            LOGGER.info("Message validé : " + expectedMessage);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAnyValidationErrorDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(anyValidationAlert));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isNoValidationErrorDisplayed() {
        try {
            // Attente courte intentionnelle : on vérifie l'ABSENCE d'alerte
            wait.withTimeout(java.time.Duration.ofSeconds(3))
                    .until(ExpectedConditions.visibilityOfElementLocated(anyValidationAlert));
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    public void waitForFormReady() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(nameInput));
        LOGGER.info("Formulaire environnement prêt");
    }

    // ════════════════════════════════════════════
    //  Utilitaires privés
    // ════════════════════════════════════════════

    /**
     * Remplit un champ texte standard ou Quasar (CTRL+A pour sélectionner tout,
     * puis saisie + TAB pour déclencher la validation).
     * Le paramètre isQuasar a été supprimé : CTRL+A fonctionne dans les deux cas.
     */
    private void fillField(By locator, String value, String label) {
        WebElement field = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        new Actions(driver).moveToElement(field).perform();
        field.click();
        field.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        field.sendKeys(value);
        field.sendKeys(Keys.TAB);
        LOGGER.info(label + " saisi : " + value);
    }

    private void clearAndBlur(By locator, String label) {
        WebElement field = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        new Actions(driver).moveToElement(field).perform();
        field.click();
        field.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        field.sendKeys(Keys.TAB);
        LOGGER.info(label + " vidé + blur");
    }

    /**
     * Upload sans JavascriptExecutor et sans dialog OS.
     * Selenium sait nativement envoyer un chemin de fichier à un input[type=file]
     * même si cet input est hidden/invisible — sendKeys contourne le dialog OS.
     */
    private void uploadFile(By fileInputLocator, String absolutePath, String label) {
        try {
            // Pas de scroll sur un input caché : presenceOfElementLocated suffit
            WebElement input = wait.until(ExpectedConditions.presenceOfElementLocated(fileInputLocator));
            input.sendKeys(absolutePath);
            LOGGER.info(label + " uploadé : " + absolutePath);
        } catch (Exception e) {
            LOGGER.severe("Upload échoué pour " + label + " : " + e.getMessage());
        }
    }

    private void selectFromAutocomplete(By locator, String value, String label) {
        WebElement field = wait.until(ExpectedConditions.elementToBeClickable(locator));

        field.click();
        field.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        field.sendKeys(value);

        By suggestion = By.xpath(
                "//div[contains(@class,'q-menu') or contains(@class,'q-virtual-scroll')]" +
                        "//div[contains(@class,'q-item')][contains(normalize-space(),'" + value + "')]"
        );

        WebElement option = wait.until(ExpectedConditions.visibilityOfElementLocated(suggestion));

        try {
            option.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", option);
        }

        LOGGER.info(label + " sélectionné : " + value);
    }

    private void selectFromListbox(By triggerLocator, String value, String label) {
        // 1. S'assurer qu'aucun listbox n'est déjà ouvert avant d'ouvrir le nouveau
        closeAnyOpenListbox();

        // 2. Ouvrir le dropdown
        scrollAndClick(triggerLocator, "Dropdown " + label);

        // 3. Attendre que le listbox soit visible
        By listbox = By.xpath("//div[@role='listbox']");
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(listbox));
        } catch (TimeoutException e) {
            // Deuxième tentative si le listbox ne s'est pas ouvert
            LOGGER.warning("Listbox non visible après premier clic pour " + label + " — re-tentative");
            scrollAndClick(triggerLocator, "Dropdown " + label + " (retry)");
            wait.until(ExpectedConditions.visibilityOfElementLocated(listbox));
        }

        // 4. Construire le locator de l'option DANS ce listbox précis
        By option = By.xpath(
                "//div[@role='listbox'][not(contains(@style,'display: none'))]" +
                        "//div[@role='option'][contains(normalize-space(.), '" + value + "')]"
        );

        // 5. Cliquer l'option
        scrollAndClick(option, "Option " + value + " pour " + label);

        // 6. Attendre la fermeture complète du listbox avant de continuer
        wait.until(ExpectedConditions.invisibilityOfElementLocated(listbox));

        // 7. Petit délai pour laisser Vue.js mettre à jour le modèle
        try { Thread.sleep(300); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }

        LOGGER.info(label + " sélectionné : " + value);
    }

    /**
     * Ferme tout listbox éventuellement ouvert en appuyant sur ESCAPE.
     * Protège contre le cas où le listbox précédent ne s'est pas fermé.
     */
    private void closeAnyOpenListbox() {
        try {
            List<WebElement> openListboxes = driver.findElements(
                    By.xpath("//div[@role='listbox'][not(contains(@style,'display: none'))]")
            );
            if (!openListboxes.isEmpty()) {
                LOGGER.warning("Listbox encore ouvert détecté — fermeture via ESCAPE");
                driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
                wait.until(ExpectedConditions.invisibilityOfElementLocated(
                        By.xpath("//div[@role='listbox']")
                ));
                Thread.sleep(200);
            }
        } catch (Exception e) {
            // Pas bloquant si aucun listbox ouvert
        }
    }

    private void clearDropdownCancelById(String fieldId, String label) {
        By cancelIcon = By.xpath(
                "//div[@id='" + fieldId + "']//i[@aria-label='Clear' and normalize-space(.)='cancel']"
        );
        try {
            scrollAndClick(cancelIcon, "Icône cancel pour " + label);
        } catch (Exception e) {
            LOGGER.warning("Icône cancel introuvable pour " + label + " : " + e.getMessage());
        }
    }

    /** scroll + click en une seule opération — remplace l'ancien duo scrollTo/clickElement. */
    private void scrollAndClick(By locator, String label) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
                try {
                    el.click();
                } catch (ElementClickInterceptedException e) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
                }
                LOGGER.info(label + " cliqué");
                return;
            } catch (StaleElementReferenceException e) {
                attempts++;
                LOGGER.warning("Stale sur '" + label + "', re-tentative " + attempts + "/3");
                try { Thread.sleep(300); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }
        throw new RuntimeException("scrollAndClick échoué après 3 tentatives pour : " + label);
    }




    // ─── Onglets ───
    private final By entreprisesTab = By.xpath("//div[@role='tab']//div[text()='Entreprises']"    );

    // ─── Bouton créer une entreprise dans cet environnement ───
    private final By createCompanyInEnvBtn = By.xpath(
            "//button[.//span[contains(normalize-space(),'Créer une entreprise dans cet environnement')]] | " +
                    "//span[contains(normalize-space(),'Créer une entreprise dans cet environnement')]"
    );

    // ─── Filtres dans l'onglet Entreprises ───
    private final By companyNameFilter = By.xpath(
            "//input[@placeholder=\"Nom de l'entreprise\"]"
    );
    private final By companyOpenIdFilter = By.xpath(
            "//input[@placeholder='ID dans open']"
    );




    // ════════════════════════════════════════════
//  Navigation onglets
// ════════════════════════════════════════════

    /**
     * Clique sur l'onglet "Entreprises" de la fiche environnement.
     * Disponible uniquement après la création réussie de l'environnement.
     */
    public void clickEntreprisesTab() {
        // 1. Attendre la disparition du toast (indique que l'UI est stabilisée)
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(
                    By.xpath("//div[contains(@class,'q-notification')]")
            ));
        } catch (Exception ignored) {
            // Si le toast est déjà disparu, on continue
        }

        // 2. Attendre que l'onglet soit présent ET enabled (pas aria-disabled)
        By tabLocator = By.xpath(
                "//div[@role='tab'][.//div[text()='Entreprises']]" +
                        "[not(contains(@class,'disabled'))]" +
                        "[not(@aria-disabled='true')]"
        );

        try {
            wait.until(ExpectedConditions.elementToBeClickable(tabLocator));
        } catch (TimeoutException e) {
            // Log l'état de l'onglet pour diagnostiquer
            try {
                WebElement tab = driver.findElement(
                        By.xpath("//div[@role='tab'][.//div[text()='Entreprises']]")
                );
                LOGGER.warning("État onglet Entreprises — class: [" + tab.getAttribute("class")
                        + "] aria-disabled: [" + tab.getAttribute("aria-disabled") + "]");
            } catch (Exception ex) {
                LOGGER.warning("Onglet Entreprises introuvable dans le DOM");
            }
            throw e;
        }

        scrollAndClick(tabLocator, "Onglet Entreprises");

        // 3. Attendre que le contenu de l'onglet soit chargé
        // (le bouton "Créer une entreprise" ou le tableau doivent apparaître)
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//button[.//span[contains(normalize-space(),'Créer une entreprise dans cet environnement')]]")
                ),
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//tbody")
                )
        ));

        LOGGER.info("Onglet Entreprises ouvert et contenu chargé");
    }

    /**
     * Clique sur le bouton "Créer une entreprise dans cet environnement".
     */
    public void clickCreateCompanyInEnvironment() {
        // Attendre le bouton visible et scroller vers lui
        By btnLocator = By.xpath(
                "//button[.//span[contains(normalize-space(),'Créer une entreprise dans cet environnement')]]"
        );

        WebElement btn = wait.until(ExpectedConditions.visibilityOfElementLocated(btnLocator));
        new Actions(driver).moveToElement(btn).perform();

        try {
            btn.click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }

        LOGGER.info("Formulaire création entreprise ouvert");
    }

    /**
     * Filtre les entreprises de l'onglet par nom.
     */
    public void filterCompaniesByName(String name) throws InterruptedException {
        Thread.sleep(500);
        fillField(companyNameFilter, name, "Filtre nom entreprise");
    }

    /**
     * Filtre les entreprises de l'onglet par ID dans Open.
     */
    public void filterCompaniesByOpenId(String openId) throws InterruptedException {
        Thread.sleep(500);
        fillField(companyOpenIdFilter, openId, "Filtre ID dans Open");
    }



    // ─── Locators onglet Entreprises — actions sur lignes ───
    private final By firstCompanyEditIcon   = By.xpath("(//tbody/tr)[1]//i[text()='edit']");
    private final By firstCompanyDeleteIcon = By.xpath("(//tbody/tr)[1]//i[text()='delete']");
    private final By companyDeleteDialog    = By.xpath(
            "//div[@role='dialog' and .//span[contains(.,'irréversible')]]");
    private final By companyDeleteConfirm   = By.xpath(
            "//button[.//span[contains(normalize-space(),'Confirmer la suppression')]]");
    private final By companyDeleteCancel    = By.xpath(
            "//button[.//span[contains(normalize-space(),'Ne pas supprimer')]]");
    private final By companyTabToast        = By.xpath("//div[contains(@class,'q-notification')]");
    private final By companyTabRows         = By.xpath("//tbody/tr");

    public void clickEditOnFirstCompanyInTab() {
        scrollAndClick(firstCompanyEditIcon, "Edit première entreprise onglet");
    }

    public void clickDeleteOnFirstCompanyInTab() {
        scrollAndClick(firstCompanyDeleteIcon, "Delete première entreprise onglet");
    }


    public String getCompanyTabToastMessage() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(companyTabToast))
                    .getText().trim();
        } catch (Exception e) { return ""; }
    }

    public boolean isCompanyTabListEmpty() {
        return driver.findElements(companyTabRows).isEmpty();
    }
}
