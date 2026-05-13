package org.automation.pages.environment;

import org.automation.base.BasePage;
import org.automation.pages.digitalspace.DigitalSpacePage;
import org.automation.pages.domain.DomainListPage;
import org.automation.pages.faq.FaqFormPage;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.util.List;
import java.util.logging.Logger;

public class EnvironmentDigitalSpacePage extends BasePage implements DigitalSpacePage {

    private static final Logger LOGGER = Logger.getLogger(EnvironmentDigitalSpacePage.class.getName());

    // ══════════════════════════════════════════════
    //  ONGLET
    // ══════════════════════════════════════════════

//    private final By digitalSpaceTab = By.xpath(
//            "//div[@role='tab']" +
//                    "[.//div[contains(text(),'Contenu pour les espaces digitaux')]]" +
//                    "[not(contains(@class,'disabled'))][not(@aria-disabled='true')]"
//    );
//



    private final By digitalSpaceTab = By.xpath(
"//div[@role='tab' and .//div[contains(@class,'q-tab__label') and normalize-space()='Contenu pour les espaces digitaux']]"    );




    // ══════════════════════════════════════════════
    //  SECTIONS — Afficher / Fermer
    // ══════════════════════════════════════════════

    private By afficherBtn(String sectionTitle) {
        return By.xpath(
                "//span[contains(normalize-space(.),'" + sectionTitle + "')]" +
                        "/ancestor::div[contains(@class,'row') and contains(@class,'items-start')]" +
                        "//div[normalize-space(text())='Afficher' or normalize-space(text())='Fermer']"
        );
    }

    private By fermerBtn(String sectionTitle) {
        return By.xpath(
                "//span[contains(normalize-space(.),'" + sectionTitle + "')]" +
                        "/ancestor::div[contains(@class,'row') and contains(@class,'items-start')]" +
                        "//div[normalize-space(text())='Fermer']"
        );
    }

    // ══════════════════════════════════════════════
    //  DOMAINE
    // ══════════════════════════════════════════════

    private final By addSecondaryDomainBtn = By.xpath(
            "//span[contains(normalize-space(),'Cliquer pour ajouter un domaine secondaire')]"
    );

    private final By domainRows = By.xpath(
            "//span[contains(normalize-space(.),'Domaine')]" +
                    "/ancestor::div[contains(@class,'q-tab-panel')]//tbody/tr"
    );

    // ══════════════════════════════════════════════
    //  FAQ
    // ══════════════════════════════════════════════

    private final By addFaqBtn = By.xpath(
"//span[contains(text(),'Cliquer pour ajouter une autre Question')]"    );

    // ══════════════════════════════════════════════
    //  SERVICES
    // ══════════════════════════════════════════════

    private final By addServiceBtn = By.xpath(
            "//span[contains(text(),'Cliquer pour ajouter un nouveau élément à la liste')]"
    );

    private final By serviceItems = By.xpath(
            "//div[contains(@class,'q-tab-panel')]" +
                    "//div[contains(@class,'card-container')]" +
                    "[.//div[starts-with(normalize-space(),'Nom :')]]"
    );
    // ══════════════════════════════════════════════
    //  ACTUALITÉS — formulaire inline
    // ══════════════════════════════════════════════

// ══════════════════════════════════════════════
//  ACTUALITÉS — formulaire inline
// ══════════════════════════════════════════════

    private final By addActualiteBtn = By.xpath(
            "//span[contains(text(),'Ajouter une nouvelle actualité')]"
    );

    private final By actualiteTitleFr = By.xpath(
            "//input[@name='news-title-fr' or @id='news-title-fr']"
    );

    private final By actualiteTitleEn = By.xpath(
            "//input[@name='news-title-en' or @id='news-title-en']"
    );

    private final By actualiteDescFr = By.xpath(
            "//textarea[@name='news-title-description-fr' or @id='news-title-description-fr']"
    );

    private final By actualiteDescEn = By.xpath(
            "//textarea[@name='news-title-description-en' or @id='news-title-description-en']"
    );

    private final By actualiteLien = By.xpath(
            "//input[@name='news-link' or @id='news-link']"
    );

    private final By actualiteLienTextFr = By.xpath(
            "//input[@name='news-link-fr' or @id='news-link-fr']"
    );

    private final By actualiteLienTextEn = By.xpath(
            "//input[@name='news-link-en' or @id='news-link-en']"
    );

    private final By actualiteDate = By.xpath(
            "//input[@name='news-start-date' or @id='news-start-date']"
    );

    private final By actualiteSaveBtn = By.xpath(
            "//div[contains(@class,'q-tab-panel')]" +
                    "//button[.//span[normalize-space(text())='Enregistrer']]"
    );

    private final By actualiteItems = By.xpath(
            "//div[contains(@class,'q-tab-panel')]" +
                    "//div[contains(@class,'card-container')]" +
                    "[.//div[contains(normalize-space(.),'Nom :')]]" +
                    "[not(.//input) and not(.//textarea)]"
    );

    // ══════════════════════════════════════════════
//  ACTUALITÉS — upload image
// ══════════════════════════════════════════════

    // Input file caché dans le formulaire inline (dans le card-container avec inputs)
    private final By actualiteImageInput = By.xpath(
            "//div[contains(@class,'card-container')]" +
                    "[.//input[@name='news-title-fr']]" +
                    "//input[@type='file']"
    );

    // Bouton "Modifier" image (pour vérifier qu'une image est chargée)
    private final By actualiteImageModifierBtn = By.xpath(
            "//div[contains(@class,'card-container')]" +
                    "[.//input[@name='news-title-fr']]" +
                    "//span[normalize-space(text())='Modifier']"
    );

    // Bouton delete image (icône delete à côté de Modifier)
    private final By actualiteImageDeleteBtn = By.xpath(
            "//div[contains(@class,'card-container')]" +
                    "[.//input[@name='news-title-fr']]" +
                    "//div[contains(@class,'flex') and contains(@class,'items-center')]" +
                    "//i[normalize-space(text())='delete']"
    );

    // Prévisualisation image : container-picture avec une image chargée
    private final By actualiteImagePreview = By.xpath(
            "//div[contains(@class,'card-container')]" +
                    "[.//input[@name='news-title-fr']]" +
                    "//div[contains(@class,'container-picture')]" +
                    "[.//img]"
    );

    // ══════════════════════════════════════════════
    //  FEATURES — checkbox + dropdown
    // ══════════════════════════════════════════════
    private By featureCheckboxLocator(String featureName) {
        return By.xpath(
                "//div[contains(@class,'q-tab-panel')]" +
                        "//div[contains(@class,'card-container')]" +
                        "[.//div[contains(normalize-space(.),'Nom : " + featureName + "')]]" +
                        "//div[contains(@class,'q-checkbox')]"
        );
    }

    private By featureCheckedIndicator(String featureName) {
        return By.xpath(
                "//div[contains(@class,'q-tab-panel')]" +
                        "//div[contains(@class,'card-container')]" +
                        "[.//div[contains(normalize-space(.),'Nom : " + featureName + "')]]" +
                        "//div[contains(@class,'q-checkbox__inner--truthy')]"
        );
    }

    // Compte les card-container qui ont une checkbox (exclut la carte d'ajout)
    private final By featureItems = By.xpath(
            "//div[contains(@class,'q-tab-panel')]" +
                    "//div[contains(@class,'card-container')]" +
                    "[.//div[starts-with(normalize-space(.),'Nom :')]]" +
                    "[.//*[contains(@class,'q-checkbox')]]"
    );

    // Dropdown Information obligatoire — q-select dans la card dédiée
    private final By infoObligatoireSelect = By.xpath(
            "//div[contains(@class,'card-container')]" +
                    "[.//span[contains(normalize-space(.),'Information obligatoire')]]" +
                    "//div[contains(@class,'q-select')]"
    );

    // Bouton Enregistrer — dans la card Information obligatoire uniquement
    private final By featuresSaveBtn = By.xpath(
            "//div[contains(@class,'card-container')]" +
                    "[.//span[contains(normalize-space(.),'Information obligatoire')]]" +
                    "//div[normalize-space(text())='Enregistrer']"
    );

    // Toast check/uncheck : "Feature modifie avec succes"
    private final By featureToggleSuccessToast = By.xpath(
            "//div[contains(@class,'q-notification')]" +
                    "[.//div[contains(normalize-space(.),'Feature modifie avec succes') " +
                    "or contains(normalize-space(.),'Feature modifiée avec succès')]]"
    );

    // Toast ajout : "Feature est créé avec succès"
    private final By featureAddSuccessToast = By.xpath(
            "//div[contains(@class,'q-notification')]" +
                    "[.//div[contains(normalize-space(.),'Feature est cr') " +
                    "or contains(normalize-space(.),'Feature créé')]]"
    );

    // Toast suppression : "Feature supprimee avec succes"
    private final By featureDeleteSuccessToast = By.xpath(
            "//div[contains(@class,'q-notification')]" +
                    "[.//div[contains(normalize-space(.),'Feature supprim')]]"
    );

// Toast save dropdown : "Opération terminée" (réutilise successToast existant)

    // Bouton "+ Ajouter une nouvelle feature"
    private final By addFeatureBtn = By.xpath(
                    "//span[contains(text(),'Ajouter une nouvelle feature')]"
    );

    // Dialog confirmation ajout feature
    private final By featureAddConfirmDialog = By.xpath(
            "//div[@role='dialog' and @aria-modal='true']" +
                    "[.//div[contains(normalize-space(.),'Voulez-vous ajouter cette feature')]]"
    );

    private final By featureAddConfirmBtn = By.xpath(
            "//div[@role='dialog' and @aria-modal='true']" +
                    "[.//div[contains(normalize-space(.),'Voulez-vous ajouter cette feature')]]" +
                    "//span[normalize-space(text())='Valider']" +
                    "/ancestor::button"
    );

    private final By featureAddCancelBtn = By.xpath(
            "//div[@role='dialog' and @aria-modal='true']" +
                    "[.//div[contains(normalize-space(.),'Voulez-vous ajouter cette feature')]]" +
                    "//span[normalize-space(text())='Annuler']" +
                    "/ancestor::button"
    );


// ══════════════════════════════════════════════
//  FEATURES — locators corrigés pour le formulaire d'ajout inline
// ══════════════════════════════════════════════

    // Formulaire inline - version plus robuste
    private final By featureInlineForm = By.xpath(
            "(//div[contains(@class,'card-container') and contains(@class,'bordered-gray-300')])[4]"
    );

    // Dropdown dans le nouveau formulaire
    private final By featureInlineDropdown = By.xpath(
            "(//div[contains(@class,'q-field__native') and contains(@class,'row') and contains(@class,'items-center')])[1]"
    );

//    private final By featureInlineForm = By.xpath(
//            "//div[contains(@class,'card-container')]" +
//                    "[.//div[contains(@class,'q-select')]]" +
//                    "[not(.//span[contains(normalize-space(.),'Information obligatoire')])]"
//    );
//
//    // Dropdown du formulaire d'ajout uniquement
//    private final By featureInlineDropdown = By.xpath(
//            "//div[contains(@class,'card-container')]" +
//                    "[.//div[contains(@class,'q-select')]]" +
//                    "[not(.//span[contains(normalize-space(.),'Information obligatoire')])]" +
//                    "//div[contains(@class,'q-select')]"
//    );

    // Champ "Nom :" dans le formulaire d'ajout
    private final By featureInlineNameField = By.xpath(
            "//div[contains(@class,'card-container')]" +
                    "[.//div[contains(@class,'q-select')]]" +
                    "[not(.//span[contains(normalize-space(.),'Information obligatoire')])]" +
                    "[.//div[contains(normalize-space(.),'Nom :')]]"
    );

    // Bouton Valider dans le formulaire d'ajout inline
    private final By featureInlineConfirmBtn = By.xpath(
            "//div[contains(@class,'card-container')]" +
                    "[.//div[contains(@class,'q-select')]]" +
                    "[not(.//span[contains(normalize-space(.),'Information obligatoire')])]" +
                    "//button[.//span[normalize-space(text())='Valider'] " +
                    "or .//span[normalize-space(text())='Enregistrer']]"
    );

    // Bouton Annuler dans le formulaire d'ajout inline
    private final By featureInlineCancelBtn = By.xpath(
            "//div[contains(@class,'card-container')]" +
                    "[.//div[contains(@class,'q-select')]]" +
                    "[not(.//span[contains(normalize-space(.),'Information obligatoire')])]" +
                    "//button[.//span[normalize-space(text())='Annuler']]"
    );
    // ══════════════════════════════════════════════
    //  DIALOG commun (Domaine & Services)
    // ══════════════════════════════════════════════

    private final By selectionDialog = By.xpath(
            "//div[@role='dialog' and @aria-modal='true']" +
                    "//div[contains(@class,'q-card') and contains(@class,'container-xl')]"
    );

    // Toutes les cartes du dialog (attachées ou non)
    private final By dialogAllCards = By.xpath(
            "//div[@role='dialog']" +
                    "//div[contains(@class,'card-container')]"
    );
    private final By dialogCards = By.xpath(
            "//div[@role='dialog']" +
                    "//div[contains(@class,'card-container')]" +
                    "[not(contains(@class,'service-disabled'))]"
    );

    private final By dialogValidateBtn = By.xpath(
            "//div[@role='dialog']//button[" +
                    ".//span[contains(normalize-space(.),'Ajouter les domaines')] or " +
                    ".//span[contains(normalize-space(.),'Ajouter les services')] or " +
                    "contains(@class,'btn-primary')]"
    );

    private final By addConfirmBtn = By.xpath(
            "//div[@role='dialog']" +
                    "//button[.//span[normalize-space(text())='Valider'] or normalize-space(text())='Valider']"
    );

    private final By dialogPaginationLabel = By.xpath(
            "//div[@role='dialog']//div[contains(text(),'Page') and contains(text(),'sur')]"
    );
    private final By dialogNextPageBtn = By.xpath(
            "(//div[@role='dialog']//button)[last()-1]"
    );

    private final By dialogPrevPageBtn = By.xpath(
            "(//div[@role='dialog']//button)[1]"
    );

    private By alreadyAttachedBadge() {
        return By.xpath(
                "//div[@role='dialog']" +
                        "//div[contains(@class,'card-container')]" +
                        "[.//div[contains(normalize-space(.),'Nom :')]]" +
                        "//div[contains(@class,'q-badge') and contains(normalize-space(.),'Déjà rattaché')]"
        );
    }

    // ══════════════════════════════════════════════
    //  TOAST succès — commun
    // ══════════════════════════════════════════════

    private final By successToast = By.xpath(
            "//div[contains(@class,'q-notification__message')] | " +
                    "//div[contains(@class,'text-subtitle2') " +
                    "and contains(normalize-space(.),'Opération terminée')]"
    );

    public void confirmServiceCreation() {
        wait.until(ExpectedConditions.elementToBeClickable(addConfirmBtn)).click();

        // Attendre que le toast de succès apparaisse
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(successToast));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(successToast));
            LOGGER.info("Toast de confirmation d'ajout de service affiché");
        } catch (Exception e) {
            LOGGER.warning("Toast de confirmation non trouvé: " + e.getMessage());
        }

        sleep(500);
        LOGGER.info("Ajout de service confirmé avec succès");
    }
    // ══════════════════════════════════════════════
    //  SUPPRESSION
    // ══════════════════════════════════════════════

    private final By firstDeleteInSection = By.xpath(
            "//div[contains(@class,'q-tab-panel')]//i[text()='delete'][1]"
    );
// ══════════════════════════════════════════════
//  SUPPRESSION D'ACTUALITÉS
// ══════════════════════════════════════════════

    private final By actualiteDeleteConfirmDialog = By.xpath(
            "//div[@role='dialog' and @aria-modal='true']" +
                    "[.//div[contains(normalize-space(.),\"Voulez vous Supprimer cet actualité\")]]"
    );

    private final By actualiteDeleteConfirmBtn = By.xpath(
            "//div[@role='dialog' and @aria-modal='true']" +
                    "[.//div[contains(normalize-space(.),\"Voulez vous Supprimer cet actualité\")]]" +
                    "//button[.//span[normalize-space(text())='Valider']]"
    );

    private final By actualiteDeleteCancelBtn = By.xpath(
            "//div[@role='dialog' and @aria-modal='true']" +
                    "[.//div[contains(normalize-space(.),\"Voulez vous Supprimer cet actualité\")]]" +
                    "//button[.//span[normalize-space(text())='Annuler']]"
    );

    private final By actualiteDeleteSuccessToast = By.xpath(
            "//div[contains(@class,'q-notification')]" +
                    "[.//div[contains(text(),'actualité supprimée') or contains(text(),'supprimé avec succès') or contains(text(),'Opération terminée')]]"
    );

    private final By featureDeleteConfirmDialog = By.xpath(
            "//div[@role='dialog' and @aria-modal='true']" +
                    "[.//span[normalize-space(text())='Valider']]" +
                    "[not(.//div[contains(normalize-space(.),'Voulez-vous ajouter')])]"
    );

    private final By featureDeleteConfirmBtn = By.xpath(
            "//div[@role='dialog' and @aria-modal='true']" +
                    "[not(.//div[contains(normalize-space(.),'Voulez-vous ajouter')])]" +
                    "//span[normalize-space(text())='Valider']/ancestor::button"
    );

    private final By featureDeleteCancelBtn = By.xpath(
            "//div[@role='dialog' and @aria-modal='true']" +
                    "[not(.//div[contains(normalize-space(.),'Voulez-vous ajouter')])]" +
                    "//span[normalize-space(text())='Annuler']/ancestor::button"
    );
    // ══════════════════════════════════════════════
//  SUPPRESSION DE SERVICES
// ══════════════════════════════════════════════

    private final By serviceDeleteConfirmDialog = By.xpath(
            "//div[@role='dialog' and @aria-modal='true']" +
                    "[.//div[contains(normalize-space(.),\"Voulez vous Supprimer ce service\")]]"
    );

    private final By serviceDeleteConfirmBtn = By.xpath(
            "//div[@role='dialog' and @aria-modal='true']" +
                    "[.//div[contains(normalize-space(.),\"Voulez vous Supprimer ce service\")]]" +
                    "//button[.//span[normalize-space(text())='Valider']]");

    private final By serviceDeleteCancelBtn = By.xpath(
            "//div[@role='dialog' and @aria-modal='true']" +
                    "[.//div[contains(normalize-space(.),\"Voulez vous Supprimer ce service\")]]" +
                    "//button[.//span[normalize-space(text())='Annuler']]"
    );

    private final By serviceDeleteSuccessToast = By.xpath(
            "//div[contains(@class,'q-notification')]" +
                    "[.//div[contains(text(),'Service supprimé') or contains(text(),'supprimé avec succès')]]"
    );
    public void cancelServiceDeletion() {
        wait.until(ExpectedConditions.elementToBeClickable(serviceDeleteCancelBtn)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(serviceDeleteConfirmDialog));
        sleep(300);
        LOGGER.info("Suppression annulée");
    }
    public boolean isServiceDeleteConfirmDialogDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(serviceDeleteConfirmDialog));
            return true;
        } catch (Exception e) { return false; }
    }

    public void confirmServiceDeletion() {
        wait.until(ExpectedConditions.elementToBeClickable(serviceDeleteConfirmBtn)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(serviceDeleteConfirmDialog));

        waitForServiceDeleteSuccessToast();

        sleep(500);
        LOGGER.info("Suppression confirmée avec succès");
    }

    public void waitForServiceDeleteSuccessToast() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(serviceDeleteSuccessToast));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(serviceDeleteSuccessToast));
            LOGGER.info("Toast de suppression domaine traité");
        } catch (Exception e) {
            LOGGER.warning("Toast de succès non trouvé: " + e.getMessage());
        }
    }
    // ══════════════════════════════════════════════
    //  CONSTRUCTEUR
    // ══════════════════════════════════════════════

    public EnvironmentDigitalSpacePage() { super(); }

    // ══════════════════════════════════════════════
    //  ONGLET
    // ══════════════════════════════════════════════

    @Override
    public void clickDigitalSpaceTab() {
        wait.until(ExpectedConditions.elementToBeClickable(digitalSpaceTab));
        scrollAndClick(digitalSpaceTab, "Onglet espaces digitaux");
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[normalize-space(text())='Afficher']")
        ));
        LOGGER.info("Onglet espaces digitaux ouvert");
    }

    // ══════════════════════════════════════════════
    //  SECTIONS
    // ══════════════════════════════════════════════

    @Override
    public void openSection(String sectionTitle) {
        closeAnyOpenSection();
        By btn = afficherBtn(sectionTitle);
        wait.until(ExpectedConditions.elementToBeClickable(btn));
        scrollAndClick(btn, "Afficher section " + sectionTitle);
        wait.until(ExpectedConditions.visibilityOfElementLocated(fermerBtn(sectionTitle)));
        LOGGER.info("Section '" + sectionTitle + "' ouverte");
    }

    @Override
    public void closeSection(String sectionTitle) {
        try {
            By btn = fermerBtn(sectionTitle);
            wait.until(ExpectedConditions.elementToBeClickable(btn));
            scrollAndClick(btn, "Fermer section " + sectionTitle);
        } catch (Exception e) {
            LOGGER.warning("Section '" + sectionTitle + "' déjà fermée");
        }
    }

    @Override
    public boolean isSectionOpen(String sectionTitle) {
        try {
            return !driver.findElements(fermerBtn(sectionTitle)).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public void closeAnyOpenSection() {
        try {
            By anyFermer = By.xpath(
                    "//div[normalize-space(text())='Fermer' and contains(@class,'cursor-pointer')]"
            );
            List<WebElement> btns = driver.findElements(anyFermer);
            if (!btns.isEmpty()) {
                btns.get(0).click();
                wait.until(ExpectedConditions.invisibilityOf(btns.get(0)));
                sleep(300);
            }
        } catch (Exception ignored) {}
    }

    // ══════════════════════════════════════════════
    //  DOMAINE
    // ══════════════════════════════════════════════

    public DomainListPage openDomainSection() {
        return new DomainListPage();
    }

    // ══════════════════════════════════════════════
    //  FAQ
    // ══════════════════════════════════════════════

    public FaqFormPage clickAddFaqQuestion() {
        scrollAndClick(addFaqBtn, "Ajouter question FAQ");
        // Attendre que la navigation quitte bien la page environnement
        // avant d'instancier FaqFormPage pour éviter un faux match d'URL
        wait.until(d -> !d.getCurrentUrl().contains("/environments"));
        return new FaqFormPage().waitUntilLoaded();
    }

    public int getFaqQuestionCount() {
        try {
            return driver.findElements(By.xpath(
                    "//div[contains(@class,'q-tab-panel') and not(contains(@style,'display: none'))]" +
                            "//tbody/tr[.//td[normalize-space(text()) != '']]"
            )).size();
        } catch (Exception e) { return 0; }
    }

    // ══════════════════════════════════════════════
    //  SERVICES
    // ══════════════════════════════════════════════

    public void clickAddService() {
        scrollAndClick(addServiceBtn, "Ajouter service");
        wait.until(ExpectedConditions.visibilityOfElementLocated(selectionDialog));
    }

    public void addFirstAvailableService() {
        clickAddService();
        selectCardInDialog(1);
        clickDialogValidateIfPresent();
        waitForDialogClosed();
    }

    public int getServiceCount() {
        try { return driver.findElements(serviceItems).size(); }
        catch (Exception e) { return 0; }
    }

    // ══════════════════════════════════════════════
    //  ACTUALITÉS
    // ══════════════════════════════════════════════

    public void clickAddActualite() {
        scrollAndClick(addActualiteBtn, "Ajouter actualité");
        // Attendre que le champ titre FR soit visible (via name/id)
        wait.until(ExpectedConditions.visibilityOfElementLocated(actualiteTitleFr));
        LOGGER.info("Formulaire actualité inline ouvert");
    }

    public void fillActualiteTitleFr(String value) { fillField(actualiteTitleFr, value, "Titre FR"); }
    public void fillActualiteTitleEn(String value) { fillField(actualiteTitleEn, value, "Titre EN"); }
    public void fillActualiteDescFr(String value)  { fillTextarea(actualiteDescFr, value, "Desc FR"); }
    public void fillActualiteDescEn(String value)  { fillTextarea(actualiteDescEn, value, "Desc EN"); }
    public void fillActualiteLien(String value)    { fillField(actualiteLien, value, "Lien"); }
    public void fillActualiteLienTextFr(String value) { fillField(actualiteLienTextFr, value, "Texte lien FR"); }
    public void fillActualiteLienTextEn(String value) { fillField(actualiteLienTextEn, value, "Texte lien EN"); }
    public void fillActualiteDate(String value)    { fillField(actualiteDate, value, "Date début"); }

    public void saveActualite() {
        scrollAndClick(actualiteSaveBtn, "Enregistrer actualité");
    }
    public void saveFeature() {
        scrollAndClick(featuresSaveBtn, "Enregistrer une feature");
    }

    // Dans les locators — à ajouter
    private final By actualiteFormInline = By.xpath(
            "//div[contains(@class,'card-container')]" +
                    "[.//input[@name='news-title-fr']]"
    );

    // Correction de la méthode
    public boolean isActualiteSavedSuccessfully() {
        try {
            // 1. Attendre que le formulaire inline disparaisse (soumission traitée)
            wait.until(ExpectedConditions.invisibilityOfElementLocated(actualiteFormInline));

            // 2. Vérifier qu'il y a au moins une carte d'actualité dans la liste
            List<WebElement> items = driver.findElements(actualiteItems);
            boolean hasSavedItem = !items.isEmpty();

            LOGGER.info("Actualité sauvegardée : " + hasSavedItem +
                    " (" + items.size() + " item(s) dans la liste)");
            return hasSavedItem;
        } catch (Exception e) {
            LOGGER.warning("Vérification sauvegarde actualité échouée : " + e.getMessage());
            return false;
        }
    }
    public int getActualitesCount() {
        try {
            return driver.findElements(actualiteItems).size();
        } catch (Exception e) { return 0; }
    }


    public void confirmActualiteCreation() {
        wait.until(ExpectedConditions.elementToBeClickable(addConfirmBtn)).click();

        // Attendre que le toast de succès apparaisse
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(successToast));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(successToast));
            LOGGER.info("Toast de confirmation d'ajout de service affiché");
        } catch (Exception e) {
            LOGGER.warning("Toast de confirmation non trouvé: " + e.getMessage());
        }

        sleep(500);
        LOGGER.info("Ajout de service confirmé avec succès");
    }

    public void cancelActualiteDeletion() {
        wait.until(ExpectedConditions.elementToBeClickable(actualiteDeleteCancelBtn)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(actualiteDeleteConfirmDialog));
        sleep(300);
        LOGGER.info("Suppression annulée");
    }
    public boolean isActualiteDeleteConfirmDialogDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(actualiteDeleteConfirmDialog));
            return true;
        } catch (Exception e) { return false; }
    }

    public void confirmActualiteDeletion() {
        wait.until(ExpectedConditions.elementToBeClickable(actualiteDeleteConfirmBtn)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(actualiteDeleteConfirmDialog));
        // Toast optionnel — ne bloque pas si absent
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(actualiteDeleteSuccessToast));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(actualiteDeleteSuccessToast));
            LOGGER.info("Toast suppression actualité traité");
        } catch (Exception e) {
            LOGGER.warning("Toast suppression actualité non trouvé : " + e.getMessage());
        }
        sleep(500);
        LOGGER.info("Suppression actualité confirmée");
    }


    public void uploadPhoto(String filePath) {
        // Rendre l'input file visible via JS pour pouvoir lui envoyer le chemin
        WebElement input = driver.findElement(actualiteImageInput);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].style.display='block'; " +
                        "arguments[0].style.visibility='visible'; " +
                        "arguments[0].style.height='1px'; " +
                        "arguments[0].style.width='1px';",
                input
        );
        input.sendKeys(filePath);
        sleep(800); // laisser le temps au composant de traiter le fichier
        LOGGER.info("Photo uploadée : " + filePath);
    }

    public boolean isImageUploaded() {
        try {
            // Vérifie qu'une image est présente dans le container-picture
            return !driver.findElements(actualiteImagePreview).isEmpty();
        } catch (Exception e) { return false; }
    }

    public void deleteActualiteImage() {
        scrollAndClick(actualiteImageDeleteBtn, "Supprimer image actualité");
        sleep(500);
        LOGGER.info("Image actualité supprimée");
    }

    public boolean isImageDeleted() {
        // Après suppression, le container-picture ne devrait plus contenir d'img
        try {
            return driver.findElements(actualiteImagePreview).isEmpty();
        } catch (Exception e) { return true; }
    }
    // ══════════════════════════════════════════════
    //  FEATURES
    // ══════════════════════════════════════════════
// ══════════════════════════════════════════════
//  FEATURES — méthodes
// ══════════════════════════════════════════════

    public boolean isFeatureChecked(String featureName) {
        try {
            return !driver.findElements(featureCheckedIndicator(featureName)).isEmpty();
        } catch (Exception e) { return false; }
    }

    public void checkFeature(String featureName) {
        if (!isFeatureChecked(featureName)) {
            scrollAndClick(featureCheckboxLocator(featureName), "Cocher " + featureName);
            waitForFeatureToggleToast();
        }
    }

    public void uncheckFeature(String featureName) {
        if (isFeatureChecked(featureName)) {
            scrollAndClick(featureCheckboxLocator(featureName), "Décocher " + featureName);
            waitForFeatureToggleToast();
        }
    }

    // Le toast apparaît automatiquement après check/uncheck, sans bouton Enregistrer
    private void waitForFeatureToggleToast() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(featureToggleSuccessToast));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(featureToggleSuccessToast));
            LOGGER.info("Toast modification feature traité");
        } catch (Exception e) {
            LOGGER.warning("Toast modification feature non trouvé : " + e.getMessage());
        }
    }

    // isFeaturesConfigSaved couvre le toast du dropdown Enregistrer ET le toast check/uncheck
    public boolean isFeaturesConfigSaved() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(successToast));
            return true;
        } catch (Exception e) { return false; }
    }

    public void saveFeaturesConfig() {
        scrollAndClick(featuresSaveBtn, "Enregistrer features config");
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(successToast));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(successToast));
            LOGGER.info("Toast enregistrement features traité");
        } catch (Exception e) {
            LOGGER.warning("Toast enregistrement features non trouvé : " + e.getMessage());
        }
    }

    public int getFeaturesCount() {
        try { return driver.findElements(featureItems).size(); }
        catch (Exception e) { return 0; }
    }

// ── Ajout feature ──

//    public void clickAddFeature() {
//        scrollAndClick(addFeatureBtn, "Ajouter nouvelle feature");
//        // Attendre que le formulaire inline apparaisse (pas un dialog)
//       // wait.until(ExpectedConditions.visibilityOfElementLocated(featureInlineForm));
//        LOGGER.info("Formulaire inline feature ouvert");
//    }


    public void clickAddFeature() {
        scrollAndClick(addFeatureBtn, "Ajouter nouvelle feature");
        // Attendre que le formulaire inline apparaisse
        //wait.until(ExpectedConditions.visibilityOfElementLocated(featureInlineForm));
        LOGGER.info("Formulaire inline feature ouvert");
    }

    public void confirmFeatureCreation() {
        // Cliquer sur Valider dans le formulaire inline
        scrollAndClick(featureAddConfirmBtn, "Valider ajout feature");
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(featureAddSuccessToast));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(featureAddSuccessToast));
            LOGGER.info("Toast ajout feature traité");
        } catch (Exception e) {
            LOGGER.warning("Toast ajout feature non trouvé : " + e.getMessage());
        }
        sleep(500);
    }

    public void selectFeatureFromDropdown(String featureName) {
        // 1. Scroller jusqu'au dropdown du formulaire d'ajout
        WebElement dropdown = wait.until(
                ExpectedConditions.elementToBeClickable(featureInlineDropdown)
        );
        new Actions(driver).moveToElement(dropdown).perform();

        // 2. Cliquer pour ouvrir le listbox
        try {
            dropdown.click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", dropdown);
        }
        sleep(500);

        // 3. Sélectionner l'option dans le listbox
        By option = By.xpath(
                "//div[@role='listbox']" +
                        "//div[@role='option']" +
                        "[contains(normalize-space(.),'" + featureName + "')]"
        );
        wait.until(ExpectedConditions.elementToBeClickable(option)).click();
        LOGGER.info("Feature sélectionnée : " + featureName);
    }

    public void cancelFeatureCreation() {
        scrollAndClick(featureAddCancelBtn, "Annuler ajout feature");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(featureAddConfirmDialog));
        sleep(300);
        LOGGER.info("Ajout feature annulé");
    }

// ── Suppression feature ──

    public boolean isFeatureDeleteConfirmDialogDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(featureDeleteConfirmDialog));
            return true;
        } catch (Exception e) { return false; }
    }

    public void confirmFeatureDeletion() {
        wait.until(ExpectedConditions.elementToBeClickable(featureDeleteConfirmBtn)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(featureDeleteConfirmDialog));
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(featureDeleteSuccessToast));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(featureDeleteSuccessToast));
            LOGGER.info("Toast suppression feature traité");
        } catch (Exception e) {
            LOGGER.warning("Toast suppression feature non trouvé : " + e.getMessage());
        }
        sleep(500);
    }

    public void cancelFeatureDeletion() {
        wait.until(ExpectedConditions.elementToBeClickable(featureDeleteCancelBtn)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(featureDeleteConfirmDialog));
        sleep(300);
        LOGGER.info("Suppression feature annulée");
    }
    // ══════════════════════════════════════════════
    //  DIALOG commun
    // ══════════════════════════════════════════════

    public void selectCardInDialog(int cardIndex) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(selectionDialog));
        List<WebElement> cards = wait.until(d -> {
            List<WebElement> c = d.findElements(dialogCards); // cartes non-disabled
            return c.size() > cardIndex ? c : null;
        });
        WebElement card = cards.get(cardIndex);
        new Actions(driver).moveToElement(card).perform();
        try {
            card.click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", card);
        }
        LOGGER.info("Carte index " + cardIndex + " sélectionnée");
    }

    public int getDialogCardCount() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(selectionDialog));
        return driver.findElements(dialogCards).size();
    }

    public String getDialogPaginationText() {
        try {
            WebElement paginationElement = wait.until(ExpectedConditions.visibilityOfElementLocated(dialogPaginationLabel));
            String text = paginationElement.getText().trim();
            LOGGER.info("Texte de pagination: '" + text + "'");
            return text;
        } catch (Exception e) {
            LOGGER.warning("Erreur récupération texte pagination: " + e.getMessage());
            return "";
        }
    }

    public int getDialogCurrentPage() {
        try {
            // Attendre que le texte de pagination soit présent
            wait.until(ExpectedConditions.visibilityOfElementLocated(dialogPaginationLabel));
            String text = getDialogPaginationText();
            LOGGER.info("Texte de pagination trouvé: '" + text + "'");

            // Extraction plus robuste du numéro de page
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("Page\\s*(\\d+)\\s*sur");
            java.util.regex.Matcher matcher = pattern.matcher(text);

            if (matcher.find()) {
                int page = Integer.parseInt(matcher.group(1));
                LOGGER.info("Page courante: " + page);
                return page;
            }

            LOGGER.warning("Impossible d'extraire le numéro de page du texte: " + text);
            return 1;
        } catch (Exception e) {
            LOGGER.warning("Erreur extraction page courante: " + e.getMessage());
            return 1;
        }
    }

    public void clickDialogNextPage() {
        scrollAndClick(dialogNextPageBtn, "Page suivante dialog");
        sleep(600);
    }

    public void clickDialogPrevPage() {
        scrollAndClick(dialogPrevPageBtn, "Page précédente dialog");
        sleep(600);
    }

    public boolean isCardAlreadyAttached(int index) {
        // Attendre que le dialog soit visible et que des cartes soient chargées
        wait.until(ExpectedConditions.visibilityOfElementLocated(selectionDialog));

        List<WebElement> cards = wait.until(d -> {
            List<WebElement> c = d.findElements(dialogAllCards);
            return !c.isEmpty() ? c : null;
        });

        if (cards.size() <= index) {
            throw new RuntimeException("Index carte invalide: " + index +
                    ", nombre de cartes trouvées: " + cards.size());
        }

        WebElement card = cards.get(index);

        // Le badge a role="status" dans le DOM
        boolean hasBadge = !card.findElements(
                By.xpath(".//*[@role='status' and " +
                        "(contains(normalize-space(.),'Déja rattaché') or " +
                        "contains(normalize-space(.),'Déjà rattaché'))]")
        ).isEmpty();

        boolean isDisabled = card.getAttribute("class") != null &&
                card.getAttribute("class").contains("service-disabled");

        LOGGER.info("Carte index " + index + " — badge: " + hasBadge +
                ", service-disabled: " + isDisabled +
                ", classes: " + card.getAttribute("class"));
        return hasBadge || isDisabled;
    }


    public void clickDialogValidate() {
        wait.until(ExpectedConditions.elementToBeClickable(dialogValidateBtn)).click();
        LOGGER.info("Dialog validé");
    }

    public void clickDialogValidateIfPresent() {
        try {
            WebElement btn = wait.withTimeout(java.time.Duration.ofSeconds(2))
                    .until(ExpectedConditions.elementToBeClickable(dialogValidateBtn));
            btn.click();
        } catch (Exception e) {
            LOGGER.info("Pas de bouton valider — sélection directe");
        }
    }

    public void waitForDialogClosed() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(selectionDialog));
        sleep(300);
    }

    public void closeDialogWithEscape() {
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(selectionDialog));
        } catch (Exception e) {
            LOGGER.warning("Dialog non fermé après ESCAPE");
        }
    }

    // ══════════════════════════════════════════════
    //  SUPPRESSION
    // ══════════════════════════════════════════════

    public void deleteFirstItemInCurrentSection() {
        scrollAndClick(firstDeleteInSection, "Supprimer premier élément");
    }

    public void deleteItemAtIndex(int oneBasedIndex) {
        By nthDelete = By.xpath(
                "(//div[contains(@class,'q-tab-panel')]//i[text()='delete'])[" + oneBasedIndex + "]"
        );
        scrollAndClick(nthDelete, "Supprimer élément " + oneBasedIndex);
    }

    // ══════════════════════════════════════════════
    //  UTILITAIRES PRIVÉS
    // ══════════════════════════════════════════════

    private void fillField(By locator, String value, String label) {
        WebElement field = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        new Actions(driver).moveToElement(field).perform();
        field.click();
        field.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        field.sendKeys(value);
        field.sendKeys(Keys.TAB);
        LOGGER.info(label + " : " + value);
    }

    private void fillTextarea(By locator, String value, String label) {
        WebElement field = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        new Actions(driver).moveToElement(field).perform();
        field.click();
        field.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        field.sendKeys(value);
        LOGGER.info(label + " : " + value);
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
                LOGGER.info(label + " cliqué");
                return;
            } catch (StaleElementReferenceException e) {
                attempts++;
                sleep(300);
            }
        }
        throw new RuntimeException("scrollAndClick échoué : " + label);
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }
}
