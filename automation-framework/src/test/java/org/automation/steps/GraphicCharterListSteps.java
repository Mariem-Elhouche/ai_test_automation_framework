package org.automation.steps;

import io.cucumber.java.en.*;
import org.automation.pages.graphicchart.GraphicCharterListPage;
import org.automation.pages.graphicchart.GraphicCharterPage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for the standalone Graphic Charter list page:
 *   https://stg-bo.noveocare.com/general/graphical-charter
 *
 * The creation-form steps (inside the Environment tab) are handled
 * by the existing GraphicCharterSteps class — this class does NOT
 * duplicate those step definitions.
 */
public class GraphicCharterListSteps {

    private GraphicCharterListPage listPage;
    private GraphicCharterPage formPage;

    private GraphicCharterListPage getListPage() {
        if (listPage == null) listPage = new GraphicCharterListPage();
        return listPage;
    }

    private GraphicCharterPage getFormPage() {
        if (listPage == null) formPage = new GraphicCharterPage();
        return formPage;
    }

    // =========================================================================
    // NAVIGATION
    // =========================================================================
    @Given("the user navigates to the graphic charters list page")
    public void navigateToListPage() {
        getListPage().navigateToListPage();
    }

    @Given("the user navigates to the graphic charters new form page")
    public void navigateToFormPage() {
        getFormPage().navigateToFormPage();
    }

    // =========================================================================
    // FILTER
    // =========================================================================
    @When("the user filters graphic charters by name {string}")
    public void filterByName(String name) {
        getListPage().filterByName(name);
    }

    // =========================================================================
    // FILTER ASSERTIONS
    // =========================================================================
    @Then("the displayed graphic charters all contain {string} in their name")
    public void verifyChartersContainName(String expected) {
        List<String> names = getListPage().getDisplayedNames();
        assertFalse(names.isEmpty(),
                "La liste est vide — aucune charte affichée pour le filtre '" + expected + "'");
        names.forEach(name -> assertTrue(
                name.toUpperCase().contains(expected.toUpperCase()),
                "Le nom '" + name + "' ne contient pas '" + expected + "'"
        ));
    }

    @Then("no graphic charters are displayed in the list")
    public void verifyNoChartersDisplayed() {
        assertTrue(getListPage().isListEmpty(),
                "La liste devrait être vide mais affiche des résultats");
    }

    // =========================================================================
    // PAGINATION
    // =========================================================================
    @When("the user clicks on graphic charters page {int}")
    public void clickOnPage(int pageNumber) {
        getListPage().clickOnPage(pageNumber);
    }

    @When("the user clicks on the last graphic charters page")
    public void clickOnLastPage() {
        getListPage().clickOnLastPage();
    }

    @Then("the graphic charters list displays page {int}")
    public void verifyCurrentPage(int expectedPage) {
        assertTrue(getListPage().isOnPage(expectedPage),
                "Page attendue : " + expectedPage + " | Page actuelle : " + getListPage().getCurrentPage());
    }

    @Then("the graphic charters list displays the last page")
    public void verifyLastPage() {
        assertTrue(getListPage().isLastPage(),
                "On devrait être sur une page > 1");
    }

    @When("the user clicks on the next page button")
    public void clickNextPage() {
        getListPage().clickNextPage();
    }

    @When("the user clicks on the previous page button")
    public void clickPreviousPage() {
        getListPage().clickPreviousPage();
    }

    // =========================================================================
    // CREATE — navigate to the form
    // =========================================================================
    @When("the user clicks on new graphic charter button from the list")
    public void clickCreateCharter() {
        getListPage().clickCreateCharter();
    }

    // =========================================================================
    // EDIT
    // =========================================================================
    @When("the user clicks on edit icon for charter {string}")
    public void clickEditIcon(String charterName) {
        getListPage().filterByNameAndClickEdit(charterName);
    }

    @Then("the graphic charter {string} appears in the graphic charters list")
    public void verifyCharterPresentInList(String charterName) {
        assertTrue(getListPage().isCharterPresentInList(charterName),
                "La charte '" + charterName + "' devrait être présente dans la liste");
    }

    // =========================================================================
    // DELETE
    // =========================================================================
    @When("the user deletes the graphic charter {string}")
    public void deleteCharter(String charterName) {
        getListPage().filterByName(charterName);
        getListPage().clickDeleteOnFirstRow();
    }

    @Then("a graphic charter deletion confirmation dialog is displayed")
    public void verifyDeleteDialog() {
        assertTrue(getListPage().isDeleteConfirmDialogDisplayed(),
                "La modale de confirmation de suppression devrait être affichée");
    }

    @When("the user confirms the graphic charter deletion")
    public void confirmDeletion() {
        getListPage().confirmDeletion();
    }

    @When("the user cancels the graphic charter deletion")
    public void cancelDeletion() {
        getListPage().cancelDeletion();
    }

    @Then("a graphic charter deletion success message is displayed")
    public void verifyDeletionSuccess() {
        String msg = getListPage().getToastMessage();
        assertFalse(msg.isEmpty(), "Aucun message de succès après suppression de la charte");
        System.out.println("Toast suppression : " + msg);
    }

    @Then("the graphic charter {string} no longer appears in the graphic charters list")
    public void verifyCharterAbsent(String charterName) {
        assertTrue(getListPage().isCharterAbsentAfterFilter(charterName),
                "La charte '" + charterName + "' devrait être supprimée mais est encore visible");
    }

    @Then("the graphic charter {string} still appears in the graphic charters list")
    public void verifyCharterStillPresent(String charterName) {
        assertTrue(getListPage().isCharterPresentAfterFilter(charterName),
                "La charte '" + charterName + "' devrait être présente mais est introuvable");
    }
}