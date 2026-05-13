package org.automation.steps;

import io.cucumber.java.en.*;
import org.automation.pages.company.CompanyFormPage;
import org.automation.pages.environment.EnvironmentFormPage;
import org.automation.pages.environment.EnvironmentListPage;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class EnvironmentSteps {

    private EnvironmentListPage listPage;
    private EnvironmentFormPage formPage;
    private CompanyFormPage companyFormPage;

    private CompanyFormPage getCompanyFormPage() {
        if (companyFormPage == null) companyFormPage = new CompanyFormPage();
        return companyFormPage;
    }
    private EnvironmentListPage getListPage() {
        if (listPage == null) listPage = new EnvironmentListPage();
        return listPage;
    }

    private EnvironmentFormPage getFormPage() {
        if (formPage == null) formPage = new EnvironmentFormPage();
        return formPage;
    }

    // ════════════════════════════════════════════
    //  Navigation
    // ════════════════════════════════════════════

    @Given("the user navigates to the environments list page")
    public void navigateToEnvironmentsListPage() {
        getListPage().navigateToListPage();
    }

    // ════════════════════════════════════════════
    //  Filtres
    // ════════════════════════════════════════════

    @When("the user filters environments by name {string}")
    public void filterByName(String name) { getListPage().filterByName(name); }

    @When("the user filters environments by site de gestion {string}")
    public void filterBySite(String site) { getListPage().filterBySiteDeGestion(site); }

    @Then("the displayed environments all contain {string} in their name")
    public void verifyNamesContain(String expected) {
        List<String> names = getListPage().getDisplayedNames();
        assertFalse(names.isEmpty(), "Liste vide pour le filtre '" + expected + "'");
        names.forEach(n -> assertTrue(
                n.toUpperCase().contains(expected.toUpperCase()),
                "'" + n + "' ne contient pas '" + expected + "'"
        ));
    }

    @Then("the displayed environments all have site de gestion {string}")
    public void verifySites(String expected) {
        List<String> sites = getListPage().getDisplayedSites();
        assertFalse(sites.isEmpty(), "Liste vide pour le site '" + expected + "'");
        sites.forEach(s -> assertTrue(
                s.toUpperCase().contains(expected.toUpperCase()),
                "'" + s + "' ne correspond pas à '" + expected + "'"
        ));
    }

    @Then("no environments are displayed in the list")
    public void verifyNoEnvironments() {
        assertTrue(getListPage().isListEmpty(), "La liste devrait être vide");
    }

    // ════════════════════════════════════════════
    //  Pagination
    // ════════════════════════════════════════════

    @When("the user clicks on environments page {int}")
    public void clickOnPage(int page) { getListPage().clickOnPage(page); }

    @When("the user clicks on the last environments page")
    public void clickLastPage() { getListPage().clickOnLastPage(); }

    @When("the user clicks the next page button")
    public void clickNextPage() { getListPage().clickNextPage(); }

    @When("the user clicks the previous page button")
    public void clickPreviousPage() { getListPage().clickPreviousPage(); }

    @Then("the environments list displays page {int}")
    public void verifyCurrentPage(int expected) {
        assertEquals(expected, getListPage().getCurrentPage(),
                "Page attendue : " + expected + " | actuelle : " + getListPage().getCurrentPage());
    }

    @Then("the environments list displays the last page")
    public void verifyLastPage() {
        assertTrue(getListPage().isLastPage(), "Devrait être sur la dernière page");
    }

    // ════════════════════════════════════════════
    //  Saisie des champs
    // ════════════════════════════════════════════

    @When("the user clicks on create environment button")
    public void clickCreate() { getListPage().clickCreateEnvironment(); }

    @When("the user fills in the environment name {string}")
    public void fillName(String name) { getFormPage().setName(name); }

    @When("the user fills in the environment address {string}")
    public void fillAddress(String address) { getFormPage().setAddress(address); }

    @When("the user fills in the environment address complement {string}")
    public void fillAddressComplement(String c) { getFormPage().setAddressComplement(c); }

    @When("the user fills in the environment email assures {string}")
    public void fillEmailAssures(String email) { getFormPage().setEmailAssures(email); }

    @When("the user fills in the environment email rh {string}")
    public void fillEmailRh(String email) { getFormPage().setEmailRh(email); }

    @When("the user fills in the environment telephone assures {string}")
    public void fillTelAssures(String tel) { getFormPage().setTelephoneAssures(tel); }

    @When("the user fills in the environment telephone professionnels {string}")
    public void fillTelPro(String tel) { getFormPage().setTelephoneProfessionnels(tel); }

    @When("the user fills in the environment nom front {string}")
    public void fillNomFront(String nom) { getFormPage().setNomFront(nom); }

    @When("the user fills in the environment nom expediteur {string}")
    public void fillNomExpediteur(String nom) { getFormPage().setNomExpediteur(nom); }

    @When("the user fills in the environment open name {string}")
    public void fillOpenName(String name) { getFormPage().setOpenName(name); }

    @When("the user fills in the environment open id {string}")
    public void fillOpenId(String id) { getFormPage().setOpenId(id); }

    @When("the user fills in the environment bank name {string}")
    public void fillBankName(String name) { getFormPage().setBankName(name); }

    @When("the user fills in the environment bank ics {string}")
    public void fillBankIcs(String ics) { getFormPage().setBankIcs(ics); }

    @When("the user fills in the environment rum root {string}")
    public void fillRumRoot(String rum) { getFormPage().setRumRoot(rum); }

    @When("the user selects the environment site de gestion {string}")
    public void selectSite(String site) { getFormPage().selectSiteDeGestion(site); }

    @When("the user selects the environment carte tp mode {string}")
    public void selectCarteTp(String mode) { getFormPage().selectCarteTpMode(mode); }

    @When("the user selects the environment double auth {string}")
    public void selectDoubleAuth(String value) { getFormPage().selectDoubleAuth(value); }

    @When("the user selects the environment charte graphique {string}")
    public void selectCharteGraphique(String value) { getFormPage().selectCharteGraphique(value); }

    @When("the user uploads the environment logo principal {string}")
    public void uploadLogo(String path) { getFormPage().uploadLogoPrincipal(path); }

    @When("the user uploads the environment image entete {string}")
    public void uploadImageEntete(String path) { getFormPage().uploadImageEntete(path); }

    @When("the user uploads the environment image bas {string}")
    public void uploadImageBas(String path) { getFormPage().uploadImageBas(path); }

    @When("the user saves the environment")
    public void saveEnvironment() { getFormPage().save(); }

    @When("the user selects the environment MFA method {string}")
    public void selectMfaMethod(String method) {
        getFormPage().selectMethodeMfa(method);
    }

    @When("the user selects the environment secondary MFA method {string}")
    public void selectSecondaryMfaMethod(String method) {
        getFormPage().selectMethodeSecondaireMfa(method);
    }
    @When("the user selects the environment decompte mode {string}")
    public void selectModeDecompte(String method) {
        getFormPage().selectModeDecompte(method);
    }

    /// /selectModeDecompte
    @When("the user enters the country limiting MFA by SMS {string}")
    public void enterMfaSmsCountry(String country) {
        getFormPage().selectPaysSms(country);
    }
    // ════════════════════════════════════════════
    //  Clear — dispatch central
    // ════════════════════════════════════════════

    @When("the user clears the environment {word}")
    public void clearSingleWord(String w1) { clearField(w1); }

    @When("the user clears the environment {word} {word}")
    public void clearTwoWords(String w1, String w2) { clearField(w1 + " " + w2); }

    @When("the user clears the environment {word} {word} {word}")
    public void clearThreeWords(String w1, String w2, String w3) { clearField(w1 + " " + w2 + " " + w3); }

    private void clearField(String action) {
        switch (action.trim().toLowerCase()) {
            case "name"                     -> getFormPage().clearName();
            case "address"                  -> getFormPage().clearAddress();
            case "site de gestion"          -> getFormPage().clearSiteDeGestion();
            case "email assures"            -> getFormPage().clearEmailAssures();
            case "email rh"                 -> getFormPage().clearEmailRh();
            case "telephone assures"        -> getFormPage().clearTelephoneAssures();
            case "telephone professionnels" -> getFormPage().clearTelephoneProfessionnels();
            case "nom front"                -> getFormPage().clearNomFront();
            case "open name"                -> getFormPage().clearOpenName();
            case "open id"                  -> getFormPage().clearOpenId();
            case "double auth"              -> getFormPage().clearDoubleAuth();
            case "carte tp mode"            -> getFormPage().clearCarteTpMode();
            default -> throw new IllegalArgumentException(
                    "Action de clear inconnue : '" + action + "'. Ajoutez-la dans clearField()."
            );
        }
    }

    // ════════════════════════════════════════════
    //  Assertions
    // ════════════════════════════════════════════

    @Then("an environment creation success message is displayed")
    public void verifyCreationSuccess() {
        assertTrue(getFormPage().isSuccessMessageDisplayed(),
                "Aucun message de succès après création");
    }
    @Then("the company is created successfully")
    public void the_company_is_created_successfully() {
        CompanyFormPage companyFormPage = new CompanyFormPage();
        assertTrue(companyFormPage.isCompanyCreatedSuccessfully(), "L'entreprise n'a pas été créée !");
    }


    @Then("an environment edit success message is displayed")
    public void verifyEditSuccess() {
        assertTrue(getFormPage().isSuccessMessageDisplayed(),
                "Aucun message de succès après édition");
    }

    @Then("an environment validation error message is displayed")
    public void verifyValidationError() {
        assertTrue(getFormPage().isAnyValidationErrorDisplayed(),
                "Aucun message de validation affiché");
    }

    @Then("an environment duplicate error message is displayed")
    public void verifyDuplicateError() {
        assertTrue(getFormPage().isDuplicateErrorDisplayed(),
                "Aucun message d'erreur de doublon affiché");
    }

    @Then("the field validation message {string} is displayed for field {string}")
    public void verifyFieldValidationMessage(String expectedMsg, String fieldLabel) {
        assertTrue(
                getFormPage().isFieldValidationMessageDisplayed(expectedMsg),
                "Message '" + expectedMsg + "' absent pour le champ '" + fieldLabel + "'"
        );
    }

    @Then("a validation alert is displayed after clearing {string}")
    public void verifyAnyValidationAlert(String fieldLabel) {
        assertTrue(
                getFormPage().isAnyValidationErrorDisplayed(),
                "BUG — Aucun message de validation pour le champ : '" + fieldLabel + "'"
        );
    }

    @Then("no validation message is displayed for the mandatory field {string}")
    public void verifyNoValidationMessage(String fieldLabel) {
        assertTrue(
                getFormPage().isNoValidationErrorDisplayed(),
                "BUG CORRIGÉ — Un message s'affiche maintenant pour : '" + fieldLabel + "'"
        );
    }

    @Then("the environment {string} appears in the environments list")
    public void verifyPresent(String name) {
        assertTrue(getListPage().isEnvironmentPresentInList(name),
                "L'environnement '" + name + "' devrait être présent");
    }

    @Then("the environment {string} no longer appears in the environments list")
    public void verifyAbsent(String name) {
        assertTrue(getListPage().isEnvironmentAbsentAfterFilter(name),
                "L'environnement '" + name + "' devrait être supprimé");
    }

    @Then("the environment {string} still appears in the environments list")
    public void verifyStillPresent(String name) {
        assertTrue(getListPage().isEnvironmentPresentAfterFilter(name),
                "L'environnement '" + name + "' devrait être présent");
    }

    // ════════════════════════════════════════════
    //  Édition & Suppression
    // ════════════════════════════════════════════

    @When("the user edits the environment {string}")
    public void openEditForm(String env) {
        getListPage().filterByName(env);
        getListPage().clickEditOnFirstRow();
        getFormPage().waitForFormReady();
    }

    @When("the user deletes the environment {string}")
    public void deleteEnvironment(String name) {
        getListPage().filterByName(name);
        getListPage().clickDeleteOnFirstRow();
    }

    @Then("an environment deletion confirmation dialog is displayed")
    public void verifyDeleteDialog() {
        assertTrue(getListPage().isDeleteConfirmDialogDisplayed(),
                "La modale de confirmation devrait être affichée");
    }

    @When("the user confirms the environment deletion")
    public void confirmDeletion() { getListPage().confirmDeletion(); }

    @When("the user cancels the environment deletion")
    public void cancelDeletion() { getListPage().cancelDeletion(); }

    @Then("an environment deletion success message is displayed")
    public void verifyDeletionSuccess() {
        assertFalse(getListPage().getToastMessage().isEmpty(),
                "Aucun message de succès après suppression");
    }



    // ════════════════════════════════════════════
//  Onglet Entreprises
// ════════════════════════════════════════════

    @When("the user clicks on the entreprises tab")
    public void clickEntreprisesTab() {
        getFormPage().clickEntreprisesTab();
    }

    @When("the user clicks on create company in environment button")
    public void clickCreateCompanyInEnvironment() {
        getFormPage().clickCreateCompanyInEnvironment();
    }

    @When("the user filters environment companies by name {string}")
    public void filterEnvCompaniesByName(String name) throws InterruptedException {
        getFormPage().filterCompaniesByName(name);
    }

    @When("the user filters environment companies by open id {string}")
    public void filterEnvCompaniesByOpenId(String openId) throws InterruptedException {
        getFormPage().filterCompaniesByOpenId(openId);
    }

    @When("the user clears the company name")
    public void clearCompanyName() { getCompanyFormPage().clearName(); }

    @When("the user edits the first company in environment")
    public void editFirstCompanyInEnv() {
        getFormPage().clickEditOnFirstCompanyInTab();
    }

    @Then("the company edit is successful")
    public void verifyCompanyEditSuccess() {
        assertTrue(getCompanyFormPage().isCompanyCreatedSuccessfully(),
                "L'édition de l'entreprise a échoué");
    }

    @When("the user deletes the first company in environment")
    public void deleteFirstCompanyInEnv() {
        getFormPage().clickDeleteOnFirstCompanyInTab();
    }

    @Then("the company deletion in environment is successful")
    public void verifyCompanyDeletionSuccess() {
        assertFalse(getFormPage().getCompanyTabToastMessage().isEmpty(),
                "Aucun message de succès après suppression entreprise");
    }

    @Then("the company list in environment still contains companies")
    public void verifyCompanyListNotEmpty() {
        assertFalse(getFormPage().isCompanyTabListEmpty(),
                "La liste devrait encore contenir des entreprises");
    }


}