package org.automation.steps;

import io.cucumber.java.en.*;
import org.automation.pages.actualites.ActualiteListPage;

import static org.junit.jupiter.api.Assertions.*;

public class ActualiteSteps {

    private final DigitalSpaceSteps digitalSpaceSteps;
    private int storedActualitesCount = -1;

    public ActualiteSteps(DigitalSpaceSteps digitalSpaceSteps) {
        this.digitalSpaceSteps = digitalSpaceSteps;
    }

    private ActualiteListPage getPage() {
        return digitalSpaceSteps.getActualitePage();
    }

    // ══════════════════════════════════════════════
    //  COMPTAGE
    // ══════════════════════════════════════════════

    @Given("the actualites count is stored")
    public void storeActualitesCount() {
        storedActualitesCount = getPage().getActualitesCount();
        System.out.println("Actualites count stored: " + storedActualitesCount);
    }

    @Then("the actualites count increased by {int}")
    public void verifyActualitesCountIncreasedBy(int delta) {
        int newCount = getPage().getActualitesCount();
        assertEquals(storedActualitesCount + delta, newCount,
                "Le nombre d'actualités devrait avoir augmenté de " + delta +
                        ". Attendu: " + (storedActualitesCount + delta) + ", Actuel: " + newCount);
    }

    @Then("the actualites count decreased by {int}")
    public void verifyActualitesCountDecreasedBy(int delta) {
        int newCount = getPage().getActualitesCount();
        assertEquals(storedActualitesCount - delta, newCount,
                "Le nombre d'actualités devrait avoir diminué de " + delta +
                        ". Attendu: " + (storedActualitesCount - delta) + ", Actuel: " + newCount);
    }

    // ══════════════════════════════════════════════
    //  AJOUT ET FORMULAIRE
    // ══════════════════════════════════════════════

    @When("the user clicks on add actualite button")
    public void clickAddActualite() {
        getPage().clickAddActualite();
    }

    @When("the user fills in the actualite title fr with {string}")
    public void fillActualiteTitleFr(String text) {
        getPage().fillTitleFr(text);
    }

    @When("the user fills in the actualite title en with {string}")
    public void fillActualiteTitleEn(String text) {
        getPage().fillTitleEn(text);
    }

    @When("the user fills in the actualite description fr with {string}")
    public void fillActualiteDescFr(String text) {
        getPage().fillDescFr(text);
    }

    @When("the user fills in the actualite description en with {string}")
    public void fillActualiteDescEn(String text) {
        getPage().fillDescEn(text);
    }

    @When("the user fills in the actualite link with {string}")
    public void fillActualiteLink(String url) {
        getPage().fillLink(url);
    }

    @When("the user fills in the actualite link text fr with {string}")
    public void fillActualiteLinkTextFr(String text) {
        getPage().fillLinkTextFr(text);
    }

    @When("the user fills in the actualite link text en with {string}")
    public void fillActualiteLinkTextEn(String text) {
        getPage().fillLinkTextEn(text);
    }

    @When("the user fills in the actualite start date with {string}")
    public void fillActualiteStartDate(String date) {
        getPage().fillDate(date);
    }

    @When("the user saves the actualite")
    public void saveActualite() {
        getPage().saveActualite();
    }

    @Then("the actualite is saved successfully")
    public void verifyActualiteSaved() {
        assertTrue(getPage().isActualiteSavedSuccessfully(),
                "L'actualité devrait être sauvegardée avec succès");
    }

    @Then("a validation error is displayed on the actualite form")
    public void verifyActualiteValidationError() {
        assertFalse(getPage().isActualiteSavedSuccessfully(),
                "L'actualité ne devrait pas être sauvegardée sans les champs requis");
    }

    @When("the user confirms the actualite creation")
    public void confirmActualiteCreation() {
        getPage().confirmActualiteCreation();
    }


    @When("the user cancels the actualite creation")
    public void cancelActualiteCreation() {
        getPage().cancelActualiteCreation();
    }

    @Then("the actualites count is restored to stored value")
    public void verifyActualiteCountRestored() {
        int newCount = getPage().getActualitesCount();
        assertEquals(storedActualitesCount, newCount,
                "Le nombre des actualités devrait être restauré. " +
                        "Attendu: " + storedActualitesCount + ", Actuel: " + newCount);
    }

    // ══════════════════════════════════════════════
    //  UPLOAD IMAGE
    // ══════════════════════════════════════════════

    @And("the user uploads the photo {string}")
    public void uploadPhoto(String path) {
        getPage().uploadPhoto(path);
    }

    @Then("the image is uploaded successfully")
    public void verifyImageUploaded() {
        assertTrue(getPage().isImageUploaded(),
                "L'image devrait être uploadée avec succès");
    }

    @When("the user deletes the actualite image")
    public void deleteActualiteImage() {
        getPage().deleteActualiteImage();
    }

    @Then("the actualite image is deleted")
    public void verifyActualiteImageDeleted() {
        assertTrue(getPage().isImageDeleted(),
                "L'image devrait avoir été supprimée");
    }

    // ══════════════════════════════════════════════
    //  SUPPRESSION
    // ══════════════════════════════════════════════

    @When("the user deletes the first actualite")
    public void deleteFirstActualite() {
        getPage().deleteFirstActualite();
    }

    @Then("an actualite delete confirmation dialog is displayed")
    public void verifyActualiteDeleteConfirmDialogDisplayed() {
        assertTrue(getPage().isDeleteConfirmDialogDisplayed(),
                "Le dialog de confirmation de suppression d'actualité devrait être affiché");
    }

    @When("the user confirms the actualite deletion")
    public void confirmActualiteDeletion() {
        getPage().confirmDeletion();
    }

    @When("the user cancels the actualite deletion")
    public void cancelActualiteDeletion() {
        getPage().cancelDeletion();
    }
}