package org.automation.steps;

import io.cucumber.java.en.*;
import org.automation.pages.faq.FaqFormPage;
import org.automation.pages.faq.FaqListPage;

import static org.junit.jupiter.api.Assertions.*;

public class FaqSteps {

    // PicoContainer injecte DigitalSpaceSteps automatiquement
    private final DigitalSpaceSteps digitalSpaceSteps;

    // Pages FAQ
    private FaqFormPage faqFormPage;
    private FaqListPage faqListPage;

    // Stockage pour le compteur
    private int storedFaqCount = -1;

    public FaqSteps(DigitalSpaceSteps digitalSpaceSteps) {
        this.digitalSpaceSteps = digitalSpaceSteps;
    }

    private FaqFormPage getFormPage() {
        if (faqFormPage == null) {
            faqFormPage = digitalSpaceSteps.getFaqPage();
        }
        if (faqFormPage == null) {
            throw new IllegalStateException(
                    "FaqFormPage non initialisée — exécuter 'the user clicks on add FAQ question button' avant"
            );
        }
        return faqFormPage;
    }

    private FaqListPage getListPage() {
        if (faqListPage == null) {
            faqListPage = new FaqListPage();
        }
        return faqListPage;
    }

    // ══════════════════════════════════════════════
    //  STEPS EXISTANTS (formulaire)
    // ══════════════════════════════════════════════

    @When("the user fills in the french question with {string}")
    public void fillFrenchQuestion(String text) { getFormPage().setQuestionFr(text); }

    @When("the user fills in the english question with {string}")
    public void fillEnglishQuestion(String text) { getFormPage().setQuestionEn(text); }

    @When("the user fills in the french answer with {string}")
    public void fillFrenchAnswer(String text) { getFormPage().setReponseFr(text); }

    @When("the user fills in the english answer with {string}")
    public void fillEnglishAnswer(String text) { getFormPage().setReponseEn(text); }

    @When("the user selects the FAQ theme {string}")
    public void selectFaqTheme(String theme) { getFormPage().selectTheme(theme); }

    @When("the user enables the global question toggle")
    public void enableGlobalToggle() { getFormPage().setQuestionGlobale(true); }

    @When("the user disables the global question toggle")
    public void disableGlobalToggle() { getFormPage().setQuestionGlobale(false); }

    @When("the user saves the FAQ question")
    public void saveFaqQuestion() { getFormPage().save(); }

    @When("the user cancels the FAQ form")
    public void cancelFaqForm() { getFormPage().cancel(); }

    @When("the user clears the FAQ theme")
    public void clearFaqTheme() {
        getFormPage().clearTheme();
    }

    @When("the user enables the question globale toggle")
    public void enableQuestionGlobaleToggle() {
        getFormPage().enableQuestionGlobale();
    }

    @Then("the FAQ form is saved successfully")
    public void verifyFaqSaved() {
        assertTrue(getFormPage().isSavedSuccessfully(),
                "Le formulaire FAQ devrait être sauvegardé avec succès");
    }

    @Then("a validation error is displayed on the FAQ form")
    public void verifyFaqValidationError() {
        assertTrue(getFormPage().isValidationErrorDisplayed(),
                "Une erreur de validation devrait être affichée");
    }

    @Then("the global question toggle is disabled by default")
    public void verifyToggleDisabledByDefault() {
        assertFalse(getFormPage().isQuestionGlobaleEnabled(),
                "Le toggle 'Question globale' devrait être désactivé par défaut");
    }

    // ══════════════════════════════════════════════
    //  STEPS POUR LA SUPPRESSION (PHRASES SPÉCIFIQUES FAQ)
    // ══════════════════════════════════════════════

    @Given("the FAQ count is stored")
    public void storeFaqCount() {
        storedFaqCount = getListPage().getFaqCountInEnvironment();
        System.out.println("FAQ count stored in environment: " + storedFaqCount);
    }

    @When("the user deletes the first FAQ item")  // ← PHRASE MODIFIÉE (plus spécifique)
    public void deleteFirstFaqItem() {
        getListPage().deleteFirstItemWithConfirmationInEnvironment();
    }

    @When("the user confirms the FAQ deletion")
    public void confirmFaqDeletion() {
        getListPage().confirmDeletion();
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
    }

    @When("the user cancels the FAQ deletion")
    public void cancelFaqDeletion() {
        getListPage().cancelDeletion();
    }

    @Then("a delete confirmation dialog is displayed")
    public void verifyDeleteConfirmationDialog() {
        assertTrue(getListPage().isConfirmationDialogDisplayed(),
                "Le dialogue de confirmation de suppression devrait être affiché");
    }

    @Then("the FAQ count decreased by {int}")
    public void verifyFaqCountDecreasedBy(int delta) {
        int newCount = getListPage().getFaqCountInEnvironment();
        assertEquals(storedFaqCount - delta, newCount,
                "Le nombre de FAQ devrait avoir diminué de " + delta +
                        ". Attendu: " + (storedFaqCount - delta) + ", Actuel: " + newCount);
    }

    @Then("the FAQ count is restored to stored value")
    public void verifyFaqCountRestored() {
        int newCount = getListPage().getFaqCountInEnvironment();
        assertEquals(storedFaqCount, newCount,
                "Le nombre de FAQ devrait être restauré à la valeur stockée. " +
                        "Attendu: " + storedFaqCount + ", Actuel: " + newCount);
    }

    // ══════════════════════════════════════════════
    //  STEPS POUR LA FERMETURE/OUVERTURE DE SECTION
    // ══════════════════════════════════════════════

//    @When("the user switches from {string} section to {string} section")
//    public void switchFromSectionToSection(String fromSection, String toSection) {
//        // Fermer la section actuelle
//        try {
//            By closeBtn = By.xpath(
//                    "//span[contains(normalize-space(.),'" + fromSection + "')]" +
//                            "/ancestor::div[contains(@class,'row')]//div[normalize-space(text())='Fermer']"
//            );
//            if (digitalSpaceSteps.getDriver().findElements(closeBtn).size() > 0) {
//                digitalSpaceSteps.getDriver().findElement(closeBtn).click();
//                Thread.sleep(500);
//            }
//        } catch (Exception e) {
//            // Déjà fermé
//        }
//
//        // Ouvrir la nouvelle section
//        try {
//            By openBtn = By.xpath(
//                    "//span[contains(normalize-space(.),'" + toSection + "')]" +
//                            "/ancestor::div[contains(@class,'row')]//div[normalize-space(text())='Afficher']"
//            );
//            digitalSpaceSteps.getDriver().findElement(openBtn).click();
//            Thread.sleep(500);
//        } catch (Exception e) {
//            throw new RuntimeException("Impossible d'ouvrir la section '" + toSection + "'", e);
//        }
//    }

//    @Then("the {string} section is open")
//    public void verifySectionOpen(String sectionName) {
//        By fermerBtn = By.xpath(
//                "//span[contains(normalize-space(.),'" + sectionName + "')]" +
//                        "/ancestor::div[contains(@class,'row')]//div[normalize-space(text())='Fermer']"
//        );
//        boolean isOpen = digitalSpaceSteps.getDriver().findElements(fermerBtn).size() > 0;
//        assertTrue(isOpen, "La section '" + sectionName + "' devrait être ouverte");
//    }

//    @Then("the service selection dialog is open is false")
//    public void verifyServiceDialogNotOpen() {
//        try {
//            boolean isDialogVisible = digitalSpaceSteps.getDriver()
//                    .findElement(By.xpath("//div[@role='dialog']"))
//                    .isDisplayed();
//            assertFalse(isDialogVisible, "Le dialog des services ne devrait pas être ouvert");
//        } catch (Exception e) {
//            assertTrue(true);
//        }
//    }
}