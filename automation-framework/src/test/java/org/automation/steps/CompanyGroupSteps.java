package org.automation.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.automation.pages.group.CompanyGroupFormPage;
import org.automation.pages.group.CompanyGroupListPage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompanyGroupSteps {

    private CompanyGroupListPage page;
    private CompanyGroupFormPage formPage;

    private CompanyGroupFormPage getFormPage() {
        if (formPage == null) formPage = new CompanyGroupFormPage();
        return formPage;
    }
    private CompanyGroupListPage getPage() {
        if (page == null) page = new CompanyGroupListPage();
        return page;
    }

    @Given("the user navigates to the company groups list page")
    public void navigateToCompanyGroupsListPage() {
        getPage().navigateToListPage();
    }

    @When("the user filters company groups by name {string}")
    public void filterCompanyGroupsByName(String name) {
        getPage().filterByName(name);
    }

    @When("the user filters company groups by type {string}")
    public void filterCompanyGroupsByType(String type) {
        getPage().filterByType(type);
    }

    @When("the user filters company groups by environment {string}")
    public void filterCompanyGroupsByEnvironment(String environment) {
        getPage().filterByEnvironment(environment);
    }

    @Then("the displayed company groups all contain {string} in their name")
    public void verifyDisplayedCompanyGroupsContainName(String expected) {
        List<String> names = getPage().getDisplayedNames();
        assertFalse(names.isEmpty(), "La liste est vide pour le filtre nom '" + expected + "'");
        names.forEach(name -> assertTrue(
                name.toUpperCase().contains(expected.toUpperCase()),
                "Le nom '" + name + "' ne contient pas '" + expected + "'"
        ));
    }

    @Then("the displayed company groups all have type containing {string}")
    public void verifyDisplayedCompanyGroupsHaveType(String expectedType) {
        List<String> types = getPage().getDisplayedTypes();
        assertFalse(types.isEmpty(), "La liste est vide pour le filtre type '" + expectedType + "'");
        types.forEach(type -> assertTrue(
                type.toUpperCase().contains(expectedType.toUpperCase()),
                "Le type '" + type + "' ne contient pas '" + expectedType + "'"
        ));
    }

    @Then("the company groups list is not empty")
    public void verifyCompanyGroupsListNotEmpty() {
        assertFalse(getPage().isListEmpty(), "La liste des groupes d'entreprises ne devrait pas etre vide");
    }

    @When("the user clicks on company groups page {int}")
    public void clickOnCompanyGroupsPage(int pageNumber) {
        getPage().clickOnPage(pageNumber);
    }

    @When("the user clicks on the last company groups page")
    public void clickOnLastCompanyGroupsPage() {
        getPage().clickOnLastPage();
    }

    @Then("the company groups list displays page {int}")
    public void verifyCompanyGroupsCurrentPage(int expectedPage) {
        assertTrue(getPage().getCurrentPage() == expectedPage,
                "Page attendue : " + expectedPage + " | actuelle : " + getPage().getCurrentPage());
    }

    @Then("the company groups list displays the last page")
    public void verifyCompanyGroupsLastPage() {
        assertTrue(getPage().isLastPage(), "La liste devrait etre positionnee sur la derniere page");
    }

    @When("the user clicks the eye icon on the first company group row")
    public void clickEyeIconOnFirstCompanyGroupRow() {
        getPage().clickEyeOnFirstRow();
    }

    @Then("the company group details page is displayed")
    public void verifyCompanyGroupDetailsPageDisplayed() {
        assertTrue(getFormPage().isGroupDetailsFormDisplayed(),
                "Le formulaire de détails du groupe devrait être visible");
    }

    @When("the user clicks on create company group button")
    public void clickCreateCompanyGroupButton() {
        getPage().clickCreateCompanyGroup();
    }


    @When("the user fills in the company group name {string}")
    public void fillCompanyGroupName(String name) {
        getFormPage().setName(name);
    }

    @When("the user fills in the company group open id {string}")
    public void fillCompanyGroupOpenId(String siret) {
        getFormPage().setOpenId(siret);
    }


    @When("the user selects company group type {string}")
    public void selectCompanyGroupType(String type) {
        getFormPage().setType(type);
    }
    @Then("a company group name validation error is displayed")
    public void verifyCompanyGroupNameValidationError() {
        assertTrue(getFormPage().isNameValidationErrorDisplayed(),
                "Le message de validation du nom vide devrait être affiché");
    }

    @When("the user selects company group environment {string}")
    public void selectCompanyGroupEnvironment(String environment) {
        getPage().setDrawerEnvironment(environment);
    }

    @When("the user saves the company group")
    public void saveCompanyGroup() {
        getFormPage().save();
    }

    @Then("a company group success message is displayed")
    public void verifyCompanyGroupSuccessMessageDisplayed() {
        assertTrue(getFormPage().isSuccessMessageDisplayed(),
                "Aucun message de succes affiche pour le groupe");
    }
    @When("the user edits the company group {string}")
    public void editCompanyGroup(String groupName) {
        getPage().editByName(groupName);
    }

    @Then("a company group edit success message is displayed")
    public void verifyCompanyGroupEditSuccessMessageDisplayed() {
        assertTrue(getFormPage().isEditSuccessMessageDisplayed(),
                "Aucun message de succès pour la modification du groupe");
    }

    @Then("the company group {string} appears in the list")
    public void verifyCompanyGroupAppearsInList(String groupName) {
        assertTrue(getPage().isGroupPresentInList(groupName),
                "Le groupe '" + groupName + "' devrait être présent dans la liste");
    }

    @When("the user edits company group {string} and changes its name to {string}")
    public void editCompanyName(String oldName, String newName) {
        getPage().filterByName(oldName);
        getPage().clickEditOnFirstRow();
        getFormPage().clearName();
        getFormPage().setName(newName);
        getFormPage().save();
    }

    @Then("the updated company group with name {string} appears in the list")
    public void verifyUpdatedCompanyGroupAppearsInList(String name) {
        assertTrue(
                getPage().isGroupPresentInList(name),
                "Le groupe modifié '" + name + "' devrait être présent dans la liste"
        );
    }
    @When("the user deletes the company group {string}")
    public void deleteCompanyGroup(String groupName) {
        getPage().filterByName(groupName);
        getPage().clickDeleteOnFirstRow();
    }

    @Then("the company group {string} still appears in the list")
    public void verifyCompanyGroupStillPresent(String groupName) {
        assertTrue(getPage().isGroupPresentInList(groupName),
                "Le groupe '" + groupName + "' devrait être présent");
    }

    @Then("a company group deletion confirmation dialog is displayed")
    public void verifyCompanyGroupDeletionConfirmationDialogDisplayed() {
        assertTrue(getPage().isDeleteConfirmDialogDisplayed(),
                "La modale de confirmation de suppression du groupe devrait etre affichee");
    }

    @When("the user confirms the company group deletion")
    public void confirmCompanyGroupDeletion() {
        getPage().confirmDeletion();
    }

    @When("the user cancels the company group deletion")
    public void cancelCompanyGroupDeletion() {
        getPage().cancelDeletion();
    }

    @Then("a company group deletion success message is displayed")
    public void verifyCompanyGroupDeletionSuccessMessageDisplayed() {
        String message = getPage().getToastMessage();
        assertFalse(message.isEmpty(), "Aucun message de succes apres suppression du groupe");
    }

    @Then("the company group {string} no longer appears in the list")
    public void verifyCompanyGroupAbsent(String groupName) {
        assertTrue(getPage().isGroupAbsentInList(groupName),
                "Le groupe '" + groupName + "' devrait être supprimé");
    }

    @When("the user clicks on company groups next page button")
    public void clickNextPageButton() {
        getPage().clickNextPage();
    }

    @When("the user clicks on company groups previous page button")
    public void clickPreviousPageButton() {
        getPage().clickPreviousPage();
    }

    @Then("the company groups next page button is disabled")
    public void verifyNextPageButtonDisabled() {
        assertTrue(getPage().isNextPageButtonDisabled(),
                "Le bouton 'Next page' devrait être désactivé");
    }

    @Then("the company groups previous page button is disabled")
    public void verifyPreviousPageButtonDisabled() {
        assertTrue(getPage().isPreviousPageButtonDisabled(),
                "Le bouton 'Previous page' devrait être désactivé");
    }


}
