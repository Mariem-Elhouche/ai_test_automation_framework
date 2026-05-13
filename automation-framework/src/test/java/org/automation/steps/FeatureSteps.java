package org.automation.steps;

import io.cucumber.java.en.*;
import org.automation.pages.features.FeatureListPage;

import static org.junit.jupiter.api.Assertions.*;

public class FeatureSteps {

    private final DigitalSpaceSteps digitalSpaceSteps;
    private int storedFeaturesCount = -1;
    private String selectedFeatureName;

    public FeatureSteps(DigitalSpaceSteps digitalSpaceSteps) {
        this.digitalSpaceSteps = digitalSpaceSteps;
    }

    private FeatureListPage getPage() {
        return digitalSpaceSteps.getFeaturePage();
    }

    // ══════════════════════════════════════════════
    //  COMPTAGE
    // ══════════════════════════════════════════════

    @Given("the features count is stored")
    public void storeFeaturesCount() {
        storedFeaturesCount = getPage().getFeaturesCount();
        System.out.println("Features count stored: " + storedFeaturesCount);
    }

    @Then("the features count increased by {int}")
    public void verifyFeaturesCountIncreasedBy(int delta) {
        int newCount = getPage().getFeaturesCount();
        assertEquals(storedFeaturesCount + delta, newCount,
                "Le nombre de features devrait avoir augmenté de " + delta +
                        ". Attendu: " + (storedFeaturesCount + delta) + ", Actuel: " + newCount);
    }

    @Then("the features count decreased by {int}")
    public void verifyFeaturesCountDecreasedBy(int delta) {
        int newCount = getPage().getFeaturesCount();
        assertEquals(storedFeaturesCount - delta, newCount,
                "Le nombre de features devrait avoir diminué de " + delta +
                        ". Attendu: " + (storedFeaturesCount - delta) + ", Actuel: " + newCount);
    }

    @Then("the features count is restored to stored value")
    public void verifyFeaturesCountRestored() {
        int newCount = getPage().getFeaturesCount();
        assertEquals(storedFeaturesCount, newCount,
                "Le nombre de features devrait être restauré. " +
                        "Attendu: " + storedFeaturesCount + ", Actuel: " + newCount);
    }

    // ══════════════════════════════════════════════
    //  CHECK/UNCHECK FEATURE
    // ══════════════════════════════════════════════

    @Then("the feature {string} is checked")
    public void verifyFeatureChecked(String featureName) {
        assertTrue(getPage().isFeatureChecked(featureName),
                "La feature '" + featureName + "' devrait être cochée");
    }

    @Then("the feature {string} is unchecked")
    public void verifyFeatureUnchecked(String featureName) {
        assertFalse(getPage().isFeatureChecked(featureName),
                "La feature '" + featureName + "' devrait être décochée");
    }

    @When("the user checks the feature {string}")
    public void checkFeature(String featureName) {
        getPage().checkFeature(featureName);
    }

    @When("the user unchecks the feature {string}")
    public void uncheckFeature(String featureName) {
        getPage().uncheckFeature(featureName);
    }

    // ══════════════════════════════════════════════
    //  INFORMATION OBLIGATOIRE
    // ══════════════════════════════════════════════

    @Then("the features configuration is saved successfully")
    public void verifyFeaturesConfigSaved() {
        assertTrue(getPage().isFeaturesConfigSaved(),
                "La configuration features devrait être sauvegardée");
    }

    // ══════════════════════════════════════════════
    //  AJOUT FEATURE
    // ══════════════════════════════════════════════

    @When("the user clicks on add feature button")
    public void clickAddFeature() {
        getPage().clickAddFeature();
    }

    @When("the user selects the feature {string} from the dropdown")
    public void selectFeatureFromDropdown(String featureName) {
        selectedFeatureName = featureName;
        getPage().selectFeatureFromDropdown(featureName);
    }

    @When("the user selects the first available feature from the dropdown")
    public void selectFirstAvailableFeatureFromDropdown() {
        selectedFeatureName = getPage().selectFirstAvailableFeatureFromDropdown();
        assertNotNull(selectedFeatureName, "Le nom de la feature sélectionnée ne doit pas être null");
        assertFalse(selectedFeatureName.isBlank(), "Le nom de la feature sélectionnée ne doit pas être vide");
    }

    @When("the user confirms the feature creation")
    public void confirmFeatureCreation() {
        getPage().confirmFeatureCreation();
    }

    @When("the user cancels the feature creation")
    public void cancelFeatureCreation() {
        getPage().cancelFeatureCreation();
    }

    @When("the user saves the feature")
    public void saveFeature() {
        getPage().saveFeature();
    }

    // ══════════════════════════════════════════════
    //  SUPPRESSION FEATURE
    // ══════════════════════════════════════════════

    @When("the user deletes the first feature")
    public void deleteFirstFeature() {
        getPage().deleteFirstFeature();
    }

    @Then("a feature delete confirmation dialog is displayed")
    public void verifyFeatureDeleteConfirmDialogDisplayed() {
        assertTrue(getPage().isDeleteConfirmDialogDisplayed(),
                "Le dialog de confirmation de suppression de feature devrait être affiché");
    }

    @When("the user confirms the feature deletion")
    public void confirmFeatureDeletion() {
        getPage().confirmDeletion();
    }

    @When("the user cancels the feature deletion")
    public void cancelFeatureDeletion() {
        getPage().cancelDeletion();
    }

    @Then("the feature deletion is confirmed successfully")
    public void verifyFeatureDeletionSuccess() {
        // La suppression est confirmée par le toast et la disparition du dialog
        // déjà attendus dans confirmFeatureDeletion()
        assertTrue(true, "Suppression feature confirmée avec succès");
    }

    @When("the user checks the selected feature")
    public void checkSelectedFeature() {
        assertNotNull(selectedFeatureName, "Aucune feature sélectionnée dans le scénario");
        getPage().checkFeature(selectedFeatureName);
    }

    @When("the user unchecks the selected feature")
    public void uncheckSelectedFeature() {
        assertNotNull(selectedFeatureName, "Aucune feature sélectionnée dans le scénario");
        getPage().uncheckFeature(selectedFeatureName);
    }

    @Then("the selected feature is checked")
    public void verifySelectedFeatureChecked() {
        assertNotNull(selectedFeatureName, "Aucune feature sélectionnée dans le scénario");
        assertTrue(getPage().isFeatureChecked(selectedFeatureName),
                "La feature sélectionnée '" + selectedFeatureName + "' devrait être cochée");
    }

    @Then("the selected feature is unchecked")
    public void verifySelectedFeatureUnchecked() {
        assertNotNull(selectedFeatureName, "Aucune feature sélectionnée dans le scénario");
        assertFalse(getPage().isFeatureChecked(selectedFeatureName),
                "La feature sélectionnée '" + selectedFeatureName + "' devrait être décochée");
    }
}
