package org.automation.steps;

import io.cucumber.java.en.*;
import org.automation.pages.CompanyFormPage;
import org.automation.pages.CompanyListPage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CompanySteps {

    private CompanyListPage listPage;
    private CompanyFormPage formPage;

    private CompanyListPage getListPage() {
        if (listPage == null) listPage = new CompanyListPage();
        return listPage;
    }

    private CompanyFormPage getFormPage() {
        if (formPage == null) formPage = new CompanyFormPage();
        return formPage;
    }
    // NAVIGATION
    @Given("the user navigates to the companies list page")
    public void navigateToCompaniesListPage() {
        getListPage().navigateToListPage();
    }

    // FILTER STEPS
    @When("the user filters companies by name {string}")
    public void filterByName(String name) {
        getListPage().filterByName(name);
    }

    @When("the user filters companies by open id {string}")
    public void filterByOpenId(String openId) {
        getListPage().filterByOpenId(openId);
    }

    @When("the user filters companies by environment {string}")
    public void filterByEnvironment(String environment) {
        getListPage().filterByEnvironment(environment);
    }
    // FILTER ASSERTIONS
    @Then("the displayed companies all contain {string} in their name")
    public void verifyCategoriesContainName(String expected) {
        List<String> names = getListPage().getDisplayedNames();
        assertFalse(names.isEmpty(),
                "La liste est vide — aucune entreprise affichée pour le filtre '" + expected + "'");
        names.forEach(name -> assertTrue(
                name.toUpperCase().contains(expected.toUpperCase()),
                " Le nom '" + name + "' ne contient pas '" + expected + "'"
        ));
    }

    @Then("the displayed companies all have open id {string}")
    public void verifyOpenId(String expectedId) {
        List<String> ids = getListPage().getDisplayedOpenIds();
        assertFalse(ids.isEmpty(),
                "La liste est vide — aucune entreprise avec l'ID '" + expectedId + "'");
        ids.forEach(id -> assertTrue(
                id.contains(expectedId),
                "L'ID '" + id + "' ne correspond pas à '" + expectedId + "'"
        ));
    }

    @Then("the displayed companies all have environment {string}")
    public void verifyEnvironment(String expectedEnv) {
        List<String> envs = getListPage().getDisplayedEnvironments();
        assertFalse(envs.isEmpty(),
                "La liste est vide — aucune entreprise avec l'environnement '" + expectedEnv + "'");
        envs.forEach(env -> assertTrue(
                env.toUpperCase().contains(expectedEnv.toUpperCase()),
                "L'environnement '" + env + "' ne contient pas '" + expectedEnv + "'"
        ));
    }

    @Then("no environment option is found in the dropdown")
    public void verifyNoEnvironmentOptionFound() {
        assertTrue(getListPage().wasLastEnvironmentNotFound(),
                "Une option environnement a été trouvée alors qu'aucune n'était attendue");
        System.out.println("Confirmé : aucun environnement trouvé dans le dropdown");
    }

    @Then("no companies are displayed in the list")
    public void verifyNoCompaniesDisplayed() {
        assertTrue(getListPage().isListEmpty(),
                "La liste devrait être vide mais affiche des résultats");
    }
    // PAGINATION
    @When("the user clicks on companies page {int}")
    public void clickOnPage(int pageNumber) {
        getListPage().clickOnPage(pageNumber);
    }

    @When("the user clicks on the last companies page")
    public void clickOnLastPage() {
        getListPage().clickOnLastPage();
    }

    @Then("the companies list displays page {int}")
    public void verifyCurrentPage(int expectedPage) {
        assertTrue(getListPage().isOnPage(expectedPage),
                "Page attendue : " + expectedPage
                        + " | Page actuelle : " + getListPage().getCurrentPage());
    }

    @Then("the companies list displays the last page")
    public void verifyLastPage() {
        assertTrue(getListPage().isLastPage(),
                "On devrait être sur la dernière page (> page 1)");
    }
    // CREATE
    @When("the user clicks on create company button")
    public void clickCreateCompany() {
        getListPage().clickCreateCompany();
    }

    @And("the user fills in the company name {string}")
    public void fillCompanyName(String name) {
        getFormPage().setCompanyName(name);
    }

    @And("the user fills in the company siret {string}")
    public void fillSiret(String siret) {
        getFormPage().setSiret(siret);
    }

    @And("the user fills in the company open id {string}")
    public void fillOpenId(String openId) {
        getFormPage().setOpenId(openId);
    }

    @And("the user saves the company")
    public void saveCompany() {
        getFormPage().save();
    }

    @Then("a company creation success message is displayed")
    public void verifyCreationSuccess() {
        assertTrue(getFormPage().isSuccessMessageDisplayed(),
                "Aucun message de succès affiché après la création de l'entreprise");
    }

    @Then("a company validation error message is displayed")
    public void verifyValidationError() {
        assertTrue(getFormPage().isValidationErrorDisplayed(),
                "Aucun message de validation affiché");
    }

    @Then("the company {string} appears in the companies list")
    public void verifyCompanyPresentInList(String companyName) {
        assertTrue(getListPage().isCompanyPresentInList(companyName),
                "L'entreprise '" + companyName + "' devrait être présente dans la liste");
    }
    // EDIT
    @When("the user edits the company {string} and changes its name to {string}")
    public void editCompanyName(String oldName, String newName) {
        getListPage().filterByName(oldName);
        getListPage().clickEditOnFirstRow();
        getFormPage().setCompanyName(newName);
        getFormPage().save();
    }

    @Then("a company edit success message is displayed")
    public void verifyEditSuccess() {
        assertTrue(getFormPage().isEditSuccessMessageDisplayed(),
                "Aucun message de succès d'édition affiché");
    }

    // DELETE
    @When("the user deletes the company {string}")
    public void deleteCompany(String companyName) {
        getListPage().filterByName(companyName);
        getListPage().clickDeleteOnFirstRow();
    }

    @Then("a company deletion confirmation dialog is displayed")
    public void verifyDeleteDialog() {
        assertTrue(getListPage().isDeleteConfirmDialogDisplayed(),
                "La modale de confirmation de suppression devrait être affichée");
    }

    @When("the user confirms the company deletion")
    public void confirmDeletion() {
        getListPage().confirmDeletion();
    }

    @When("the user cancels the company deletion")
    public void cancelDeletion() {
        getListPage().cancelDeletion();
    }

    @Then("a company deletion success message is displayed")
    public void verifyDeletionSuccess() {
        String msg = getListPage().getToastMessage();
        assertFalse(msg.isEmpty(),
                "Aucun message de succès après suppression de l'entreprise");
        System.out.println("Message suppression entreprise : " + msg);
    }

    @Then("the company {string} no longer appears in the companies list")
    public void verifyCompanyAbsent(String companyName) {
        assertTrue(getListPage().isCompanyAbsentAfterFilter(companyName),
                "L'entreprise '" + companyName + "' devrait être supprimée mais est encore visible");
    }

    @Then("the company {string} still appears in the companies list")
    public void verifyCompanyStillPresent(String companyName) {
        assertTrue(getListPage().isCompanyPresentAfterFilter(companyName),
                "L'entreprise '" + companyName + "' devrait être présente mais est introuvable");
    }
    @And("the user fills in the company address {string}")
    public void fillAddress(String address) {
        getFormPage().setAddress(address);
    }

    @And("the user fills in the company address complement {string}")
    public void fillAddressComplement(String complement) {
        getFormPage().setAddressComplement(complement);
    }

    @And("the user fills in the company postal code {string}")
    public void fillPostalCode(String postalCode) {
        getFormPage().setPostalCode(postalCode);
    }

    @And("the user fills in the company city {string}")
    public void fillCity(String city) {
        getFormPage().setCity(city);
    }

    @And("the user fills in the company country {string}")
    public void fillCountry(String country) {
        getFormPage().setCountry(country);
    }

    @And("the user fills in the company email {string}")
    public void fillEmail(String email) {
        getFormPage().setEmail(email);
    }

    @And("the user fills in the company phone {string}")
    public void fillPhone(String phone) {
        getFormPage().setPhone(phone);
    }

    @And("the user selects the company environment {string}")
    public void selectEnvironment(String environment) {
        getFormPage().selectEnvironment(environment);
    }

    @When("the user edits the company {string} and changes its siret to {string}")
    public void editCompanySiret(String companyName, String siret) {
        getListPage().filterByName(companyName);
        getListPage().clickEditOnFirstRow();
        getFormPage().setSiret(siret);
        getFormPage().save();
    }

    @When("the user edits the company {string} and clears its siret")
    public void editCompanyClearSiret(String companyName) {
        getListPage().filterByName(companyName);
        getListPage().clickEditOnFirstRow();
        getFormPage().clearSiret();
        getFormPage().save();
    }

    @When("the user edits the company {string} and changes its open id to {string}")
    public void editCompanyOpenId(String companyName, String openId) {
        getListPage().filterByName(companyName);
        getListPage().clickEditOnFirstRow();
        getFormPage().setOpenId(openId);
        getFormPage().save();
    }

    @When("the user edits the company {string} and clears its name")
    public void editCompanyClearName(String companyName) {
        getListPage().filterByName(companyName);
        getListPage().clickEditOnFirstRow();
        getFormPage().clearName();
    }
}
