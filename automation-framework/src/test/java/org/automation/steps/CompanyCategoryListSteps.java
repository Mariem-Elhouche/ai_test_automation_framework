package org.automation.steps;

import static org.junit.jupiter.api.Assertions.*;
import io.cucumber.java.en.*;
import org.automation.pages.CompanyCategoryListPage;

import java.util.List;

public class CompanyCategoryListSteps {

    private final CompanyCategoryListPage page = new CompanyCategoryListPage();

    // Navigation
    @Given("the user navigates to the company categories list page")
    public void navigateToListPage() {
        page.navigateToListPage();
    }

    // Filter actions
    @When("the user filters by name {string}")
    public void filterByName(String name) { page.filterByName(name); }

    @When("the user filters by code {string}")
    public void filterByCode(String code) { page.filterByCode(code); }

    @When("the user filters by linked company {string}")
    public void filterByCompany(String company) { page.filterByCompany(company); }

    // Filter assertions
    @Then("the displayed categories all contain {string} in their name")
    public void verifyCategoriesContainName(String expected) {
        List<String> names = page.getDisplayedNames();
        assertFalse(names.isEmpty(), "Liste vide pour le filtre nom '" + expected + "'");
        names.forEach(n -> assertTrue(
                n.toUpperCase().contains(expected.toUpperCase()),
                " '" + n + "' ne contient pas '" + expected + "'"
        ));
    }

    @Then("the displayed categories all have code {string}")
    public void verifyCategoriesHaveCode(String expected) {
        List<String> codes = page.getDisplayedCodes();
        assertFalse(codes.isEmpty(), "Liste vide pour le filtre code '" + expected + "'");
        codes.forEach(c -> assertTrue(
                c.contains(expected),
                " '" + c + "' ne correspond pas à '" + expected + "'"
        ));
    }

    @Then("the displayed categories all have a linked company containing {string}")
    public void verifyCategoriesHaveCompany(String expected) {
        List<String> companies = page.getDisplayedCompanies();
        assertFalse(companies.isEmpty(), "Liste vide pour le filtre entreprise '" + expected + "'");
        companies.forEach(c -> assertTrue(
                c.toUpperCase().contains(expected.toUpperCase()),
                " '" + c + "' ne contient pas '" + expected + "'"
        ));
    }
    @When("the user clears all filters")
    public void clearAllFilters() {
        page.clearAllFilters();
    }
    @Then("no categories are displayed in the list")
    public void verifyNoCategoriesDisplayed() {
        assertTrue(page.isListEmpty(), " La liste devrait être vide");
    }
    // Pagination
    @When("the user clicks on page {int}")
    public void clickOnPage(int pageNumber) throws InterruptedException { page.clickOnPage(pageNumber); }

    @When("the user clicks on the last page")
    public void clickOnLastPage()  { page.clickOnLastPage(); }

    @Then("the list displays categories from page {int}")
    public void verifyCurrentPage(int expected) {
        assertEquals(expected, page.getCurrentPage(),
                " Page attendue : " + expected + " | actuelle : " + page.getCurrentPage());
    }

    @Then("the list displays the last page of categories")
    public void verifyLastPage() {
        assertTrue(page.getCurrentPage() > 1, " Devrait être sur la dernière page (> 1)");
    }

    // Delete
    @When("the user deletes the category {string}")
    public void deleteCategory(String name) {
        page.filterByName(name);
        page.clickDeleteOnFirstRow();
    }

    @Then("a category deletion confirmation dialog is displayed")
    public void verifyDeleteConfirmDialogDisplayed() {
        assertTrue(page.isDeleteConfirmDialogDisplayed(),
                " La modale de confirmation devrait être affichée");
    }

    @When("the user confirms the deletion")
    public void confirmDeletion() { page.confirmDeletion(); }

    @When("the user cancels the deletion")
    public void cancelDeletion() { page.cancelDeletion(); }

    @Then("a category deletion success message is displayed")
    public void verifyDeletionSuccessMessage() {
        assertFalse(page.getDeletionSuccessMessage().isEmpty(),
                " Aucun message de succès après suppression");
    }

    @Then("the category {string} no longer appears in the list")
    public void verifyCategoryAbsent(String name) {
        assertTrue(page.isCategoryAbsentAfterFilter(name),
                " La catégorie '" + name + "' devrait être supprimée");
    }

    @Then("the category {string} still appears in the list")
    public void verifyCategoryStillPresent(String name) {
        assertTrue(page.isCategoryPresentAfterFilter(name),
                " La catégorie '" + name + "' devrait être présente");
    }
}