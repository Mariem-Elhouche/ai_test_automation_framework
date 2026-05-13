package org.automation.steps;

import io.cucumber.java.en.*;
import org.automation.pages.domain.DomainListPage;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class DomainSteps {

    private static final Logger LOGGER = Logger.getLogger(DomainSteps.class.getName());
    private final DigitalSpaceSteps digitalSpaceSteps;
    private int storedDomainCount = -1;

    public DomainSteps(DigitalSpaceSteps digitalSpaceSteps) {
        this.digitalSpaceSteps = digitalSpaceSteps;
    }

    private DomainListPage getPage() {
        return digitalSpaceSteps.getDomainPage();
    }

    // ══════════════════════════════════════════════
    //  COMPTAGE
    // ══════════════════════════════════════════════

    @Given("the domain count is stored")
    public void storeDomainCount() {
        storedDomainCount = getPage().getDomainCount();
        LOGGER.info("Domain count stored: " + storedDomainCount);
    }

    @Then("the domain count is {int}")
    public void verifyDomainCount(int expected) {
        assertEquals(expected, getPage().getDomainCount(),
                "Nombre de domaines incorrect");
    }

    @Then("the domain count increased by {int}")
    public void verifyDomainCountIncreasedBy(int delta) {
        refreshAndOpenDomainSection();
        int newCount = getPage().getDomainCount();
        assertEquals(storedDomainCount + delta, newCount,
                "Le nombre de domaines devrait avoir augmenté de " + delta +
                        ". Attendu: " + (storedDomainCount + delta) + ", Actuel: " + newCount);
    }

    @Then("the domain count decreased by {int}")
    public void verifyDomainCountDecreasedBy(int delta) {
        refreshAndOpenDomainSection();
        int newCount = getPage().getDomainCount();
        assertEquals(storedDomainCount - delta, newCount,
                "Le nombre de domaines devrait avoir diminué de " + delta +
                        ". Attendu: " + (storedDomainCount - delta) + ", Actuel: " + newCount);
    }

    @Then("the domain count is restored to stored value")
    public void verifyDomainCountRestored() {
        refreshAndOpenDomainSection();
        int newCount = getPage().getDomainCount();
        assertEquals(storedDomainCount, newCount,
                "Le nombre de domaines devrait être restauré. " +
                        "Attendu: " + storedDomainCount + ", Actuel: " + newCount);
    }

    // Méthode utilitaire pour rafraîchir et revenir à la section Domaine
    private void refreshAndOpenDomainSection() {
        digitalSpaceSteps.getDriver().navigate().refresh();
        try { Thread.sleep(2000); } catch (InterruptedException e) {}
        digitalSpaceSteps.clickDigitalSpaceTab();
        digitalSpaceSteps.openSection("Domaine");
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
    }

    // ══════════════════════════════════════════════
    //  AJOUT
    // ══════════════════════════════════════════════

    @When("the user clicks on add secondary domain button")
    public void clickAddSecondaryDomain() {
        getPage().clickAddSecondaryDomain();
    }

    @Then("the domain selection dialog is open")
    public void verifyDomainDialogOpen() {
        assertTrue(getPage().isSelectionDialogOpen(),
                "Le dialog de sélection de domaine devrait être ouvert");
    }

    @Then("the domain dialog has at least {int} card(s)")
    public void verifyDomainDialogHasCards(int min) {
        assertTrue(getPage().getDialogCardCount() >= min,
                "Le dialog devrait avoir au moins " + min + " carte(s)");
    }

    @When("the user selects the domain card at index {int}")
    public void selectDomainCardAtIndex(int zeroBasedIndex) {
        getPage().selectCardInDialog(zeroBasedIndex);
    }

    @When("the user validates the domain dialog")
    public void validateDomainDialog() {
        getPage().validateDialog();
    }

    @When("the user closes the domain dialog with escape")
    public void closeDomainDialogWithEscape() {
        getPage().closeDialogWithEscape();
    }

    // ══════════════════════════════════════════════
    //  SUPPRESSION
    // ══════════════════════════════════════════════

    @When("the user deletes the first domain")
    public void deleteFirstDomain() {
        getPage().deleteFirstDomain();
    }

    @When("the user deletes domain at index {int}")
    public void deleteDomainAtIndex(int oneBased) {
        getPage().deleteItemAt(oneBased);
    }

    @When("the user deletes the first non-primary domain")
    public void deleteFirstNonPrimaryDomain() {
        int index = getPage().getFirstNonPrimaryDomainIndex();
        assertTrue(index > 0, "Aucun domaine non principal disponible pour suppression");
        assertFalse(getPage().isDomainPrimary(index),
                "Le domaine ciblé pour suppression ne doit pas être principal");
        getPage().deleteItemAt(index);
    }

    @Then("a domain delete confirmation dialog is displayed")
    public void verifyDomainDeleteConfirmDialogDisplayed() {
        assertTrue(getPage().isDomainDeleteConfirmDialogDisplayed(),
                "Le dialog de confirmation de suppression devrait être affiché");
    }

    @When("the user confirms the domain deletion")
    public void confirmDomainDeletion() {
        getPage().confirmDomainDeletion();
    }

    @Then("the domain is deleted successfully")
    public void verifyDomainDeletedSuccessfully() {
        assertTrue(getPage().isDomainDeleteSuccessToastDisplayed(),
                "Le message de confirmation 'Domaine supprimée avec succès' devrait être affiché");
    }

    @When("the user cancels the domain deletion")
    public void cancelDomainDeletion() {
        getPage().cancelDomainDeletion();
    }

    // ══════════════════════════════════════════════
    //  DOMAINE PRINCIPALE
    // ══════════════════════════════════════════════

    @Then("domain at index {int} is the primary domain")
    public void verifyDomainIsPrimary(int oneBased) {
        assertTrue(getPage().isDomainPrimary(oneBased),
                "Le domaine " + oneBased + " devrait être le domaine principal");
    }

    @Then("domain at index {int} is not the primary domain")
    public void verifyDomainIsNotPrimary(int oneBased) {
        assertFalse(getPage().isDomainPrimary(oneBased),
                "Le domaine " + oneBased + " ne devrait pas être le domaine principal");
    }

    @When("the user clicks the primary checkbox of domain at index {int}")
    public void clickPrimaryCheckboxAt(int oneBased) {
        getPage().clickDomainPrimaryCheckbox(oneBased);
    }

    // ══════════════════════════════════════════════
    //  MESSAGES D'ERREUR MÉTIER
    // ══════════════════════════════════════════════

    @Then("cannot delete primary domain message is displayed")
    public void verifyCannotDeletePrimaryMessage() {
        assertTrue(getPage().isCannotDeletePrimaryMessageDisplayed(),
                "Le message 'On ne peut pas supprimer un domaine principal' devrait être affiché");
    }

    @Then("the cannot change primary domain message is displayed")
    public void verifyCannotChangePrimaryMessage() {
        assertTrue(getPage().isCannotChangePrimaryMessageDisplayed(),
                "Le message 'On ne peut pas changer un domaine principal' devrait être affiché");
    }

    @Then("no domain error message is displayed")
    public void verifyNoDomainErrorMessage() {
        assertFalse(getPage().isCannotDeletePrimaryMessageDisplayed(),
                "Aucun message d'erreur de suppression ne devrait être affiché");
        assertFalse(getPage().isCannotChangePrimaryMessageDisplayed(),
                "Aucun message d'erreur de changement ne devrait être affiché");
    }

    // ══════════════════════════════════════════════
    //  SECTION
    // ══════════════════════════════════════════════

    @When("the user closes the domain section")
    public void closeDomainSection() {
        getPage().closeDomainSection();
    }

    @Then("the domain section is closed")
    public void verifyDomainSectionClosed() {
        assertFalse(getPage().isDomainSectionOpen(),
                "La section Domaine devrait être fermée");
    }

    @Then("the domain section is open")
    public void verifyDomainSectionOpen() {
        assertTrue(getPage().isDomainSectionOpen(),
                "La section Domaine devrait être ouverte");
    }
}
