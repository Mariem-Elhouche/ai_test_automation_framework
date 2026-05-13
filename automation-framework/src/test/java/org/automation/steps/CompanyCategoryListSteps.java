package org.automation.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.automation.pages.companyCategory.CompanyCategoryListPage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompanyCategoryListSteps {

    private CompanyCategoryListPage page;

    private CompanyCategoryListPage getPage() {
        if (page == null) {
            page = new CompanyCategoryListPage();
        }
        return page;
    }

    @Given("the user navigates to the company categories list page")
    public void navigateToListPage() {
        getPage().navigateToListPage();
    }

    @When("the user filters by name {string}")
    public void filterByName(String name) {
        getPage().filterByName(name);
    }

    @When("the user filters by code {string}")
    public void filterByCode(String code) {
        getPage().filterByCode(code);
    }

    @When("the user filters by linked company {string}")
    public void filterByCompany(String company) {
        getPage().filterByCompany(company);
    }

    @Then("the displayed categories all contain {string} in their name")
    public void verifyCategoriesContainName(String expected) {
        List<String> names = getPage().getDisplayedNames();
        assertFalse(names.isEmpty(), "Liste vide pour le filtre nom '" + expected + "'");
        names.forEach(n -> assertTrue(
                n.toUpperCase().contains(expected.toUpperCase()),
                "'" + n + "' ne contient pas '" + expected + "'"
        ));
    }

    @Then("the displayed categories all have code {string}")
    public void verifyCategoriesHaveCode(String expected) {
        List<String> codes = getPage().getDisplayedCodes();
        assertFalse(codes.isEmpty(), "Liste vide pour le filtre code '" + expected + "'");
        codes.forEach(c -> assertTrue(
                c.contains(expected),
                "'" + c + "' ne correspond pas a '" + expected + "'"
        ));
    }

    @Then("the displayed categories all have a linked company containing {string}")
    public void verifyCategoriesHaveCompany(String expected) {
        List<String> companies = getPage().getDisplayedCompanies();
        assertFalse(companies.isEmpty(), "Liste vide pour le filtre entreprise '" + expected + "'");
        companies.forEach(c -> assertTrue(
                c.toUpperCase().contains(expected.toUpperCase()),
                "'" + c + "' ne contient pas '" + expected + "'"
        ));
    }

    @When("the user clears all filters")
    public void clearAllFilters() {
        getPage().clearAllFilters();
    }

    @Then("no categories are displayed in the list")
    public void verifyNoCategoriesDisplayed() {
        assertTrue(getPage().isListEmpty(), "La liste devrait etre vide");
    }

    @When("the user clicks on page {int}")
    public void clickOnPage(int pageNumber) throws InterruptedException {
        getPage().clickOnPage(pageNumber);
    }

    @When("the user clicks on the last page")
    public void clickOnLastPage() {
        getPage().clickOnLastPage();
    }

    @When("the user clicks on the next page")
    public void clickOnNextPage() {
        getPage().clickNextPage();
    }

    @When("the user clicks on the previous page")
    public void clickOnPreviousPage() {
        getPage().clickPreviousPage();
    }

    @Then("the list displays categories from page {int}")
    public void verifyCurrentPage(int expected) {
        assertEquals(expected, getPage().getCurrentPage(),
                "Page attendue : " + expected + " | actuelle : " + getPage().getCurrentPage());
    }

    @Then("the list displays the last page of categories")
    public void verifyLastPage() {
        assertTrue(getPage().getCurrentPage() > 1, "Devrait etre sur la derniere page (> 1)");
    }

    @When("the user deletes the category {string}")
    public void deleteCategory(String name) {
        getPage().filterByName(name);
        getPage().clickDeleteOnFirstRow();
    }

    @Then("a category deletion confirmation dialog is displayed")
    public void verifyDeleteConfirmDialogDisplayed() {
        assertTrue(getPage().isDeleteConfirmDialogDisplayed(),
                "La modale de confirmation devrait etre affichee");
    }

    @When("the user confirms the deletion")
    public void confirmDeletion() {
        getPage().confirmDeletion();
    }

    @When("the user cancels the deletion")
    public void cancelDeletion() {
        getPage().cancelDeletion();
    }

    @Then("a category deletion success message is displayed")
    public void verifyDeletionSuccessMessage() {
        assertFalse(getPage().getDeletionSuccessMessage().isEmpty(),
                "Aucun message de succes apres suppression");
    }

    @Then("the category {string} no longer appears in the list")
    public void verifyCategoryAbsent(String name) {
        assertTrue(getPage().isCategoryAbsentAfterFilter(name),
                "La categorie '" + name + "' devrait etre supprimee");
    }

    @Then("the category {string} still appears in the list")
    public void verifyCategoryStillPresent(String name) {
        assertTrue(getPage().isCategoryPresentAfterFilter(name),
                "La categorie '" + name + "' devrait etre presente");
    }
}
