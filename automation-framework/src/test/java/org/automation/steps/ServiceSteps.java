package org.automation.steps;

import io.cucumber.java.en.*;
import org.automation.pages.services.ServiceListPage;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceSteps {

    private final DigitalSpaceSteps digitalSpaceSteps;
    private int storedServiceCount = -1;

    public ServiceSteps(DigitalSpaceSteps digitalSpaceSteps) {
        this.digitalSpaceSteps = digitalSpaceSteps;
    }

    private ServiceListPage getPage() {
        return digitalSpaceSteps.getServicePage();
    }

    // ══════════════════════════════════════════════
    //  COMPTAGE
    // ══════════════════════════════════════════════

    @Given("the service count is stored")
    public void storeServiceCount() {
        storedServiceCount = getPage().getServiceCount();
        System.out.println("Service count stored: " + storedServiceCount);
    }

    @Then("the service count is {int}")
    public void verifyServiceCount(int expected) {
        assertEquals(expected, getPage().getServiceCount(),
                "Nombre de services incorrect");
    }

    @Then("the service count increased by {int}")
    public void verifyServiceCountIncreasedBy(int delta) {
        int newCount = getPage().getServiceCount();
        assertEquals(storedServiceCount + delta, newCount,
                "Le nombre de services devrait avoir augmenté de " + delta +
                        ". Attendu: " + (storedServiceCount + delta) + ", Actuel: " + newCount);
    }

    @Then("the service count is restored to stored value")
    public void verifyServiceCountRestored() {
        int newCount = getPage().getServiceCount();
        assertEquals(storedServiceCount, newCount,
                "Le nombre de services devrait être restauré. " +
                        "Attendu: " + storedServiceCount + ", Actuel: " + newCount);
    }

    // ══════════════════════════════════════════════
    //  AJOUT
    // ══════════════════════════════════════════════

    @When("the user clicks on add service button")
    public void clickAddService() {
        getPage().clickAddService();
    }

    @Then("the service selection dialog is open")
    public void verifyServiceDialogOpen() {
        assertTrue(getPage().isSelectionDialogOpen(),
                "Le dialog services devrait être ouvert");
    }

    @Then("the service dialog has at least {int} card")
    public void verifyServiceDialogHasCards(int min) {
        assertTrue(getPage().getDialogCardCount() >= min,
                "Le dialog devrait avoir au moins " + min + " carte(s)");
    }

    @When("the user selects the card at index {int} in the dialog")
    public void selectCardInDialog(int index) {
        getPage().selectCardInDialog(index);
    }

    @When("the user selects the first non-attached service card")
    public void selectFirstNonAttachedService() {
        getPage().selectFirstNonAttachedCardInDialog();
    }

    @When("the user validates the service dialog")
    public void validateServiceDialog() {
        getPage().validateDialog();
    }

    @When("the user confirms the service creation")
    public void confirmServiceCreation() {
        getPage().confirmServiceCreation();
    }


    // ══════════════════════════════════════════════
    //  PAGINATION
    // ══════════════════════════════════════════════

    @Then("the service dialog pagination shows {string}")
    public void verifyServicePagination(String expected) {
        String text = getPage().getDialogPaginationText();
        assertTrue(text.contains(expected),
                "Pagination attendue : '" + expected + "', obtenue : '" + text + "'");
    }

    @Then("the service dialog is on page {int}")
    public void verifyServiceDialogPage(int expected) {
        assertEquals(expected, getPage().getDialogCurrentPage(),
                "La page courante du dialog est incorrecte");
    }

    @When("the user goes to the next page in service dialog")
    public void goToNextPage() {
        getPage().clickDialogNextPage();
    }

    @When("the user goes to the previous page in service dialog")
    public void goToPreviousPage() {
        getPage().clickDialogPrevPage();
    }

    // ══════════════════════════════════════════════
    //  BADGE "Déjà rattaché"
    // ══════════════════════════════════════════════

    @Then("the service card at index {int} has the {string} badge")
    public void verifyServiceCardBadge(int index, String badge) {
        assertTrue(getPage().isCardAlreadyAttached(index),
                "La carte " + index + " devrait afficher '" + badge + "'");
    }

    @Then("at least one service card has the {string} badge")
    public void verifyAnyServiceCardHasBadge(String badge) {
        assertTrue(getPage().hasAnyAlreadyAttachedCardInDialog(),
                "Au moins une carte devrait afficher le badge '" + badge + "'");
    }

    // ══════════════════════════════════════════════
    //  SUPPRESSION
    // ══════════════════════════════════════════════

    @When("the user deletes the first service")
    public void deleteFirstService() {
        getPage().deleteFirstService();
    }

    @Then("a service delete confirmation dialog is displayed")
    public void verifyServiceDeleteConfirmDialogDisplayed() {
        assertTrue(getPage().isDeleteConfirmDialogDisplayed(),
                "Le dialog de confirmation de suppression de service devrait être affiché");
    }

    @When("the user confirms the service deletion")
    public void confirmServiceDeletion() {
        getPage().confirmDeletion();
    }

    @When("the user cancels the service deletion")
    public void cancelServiceDeletion() {
        getPage().cancelDeletion();
    }
}
