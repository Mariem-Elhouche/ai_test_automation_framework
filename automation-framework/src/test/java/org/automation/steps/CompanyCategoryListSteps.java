package org.automation.steps;

import static org.junit.jupiter.api.Assertions.*;
import io.cucumber.java.en.*;
import org.automation.pages.CompanyCategoryListPage;

import java.util.List;

public class CompanyCategoryListSteps {

    private CompanyCategoryListPage page;

    private CompanyCategoryListPage getPage() {
        if (page == null) page = new CompanyCategoryListPage();
        return page;
    }

    // =========================
    // NAVIGATION
    // =========================
    @Given("the user navigates to the company categories list page")
    public void navigateToListPage() {
        getPage().navigateToListPage();
    }

    // =========================
    // FILTER STEPS
    // =========================
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

    // =========================
    // FILTER ASSERTIONS
    // =========================
    @Then("the displayed categories all contain {string} in their name")
    public void verifyCategoriesContainName(String expected) {
        List<String> names = getPage().getDisplayedNames();
        assertFalse(names.isEmpty(),
                "La liste est vide — aucune catégorie affichée pour le filtre '" + expected + "'");
        names.forEach(name -> assertTrue(
                name.toUpperCase().contains(expected.toUpperCase()),
                "❌ Le nom '" + name + "' ne contient pas '" + expected + "'"
        ));
    }

    @Then("the displayed categories all have code {string}")
    public void verifyCategoriesHaveCode(String expectedCode) {
        List<String> codes = getPage().getDisplayedCodes();
        assertFalse(codes.isEmpty(),
                "La liste est vide — aucune catégorie avec le code '" + expectedCode + "'");
        codes.forEach(code -> assertTrue(
                code.contains(expectedCode),
                "❌ Le code '" + code + "' ne correspond pas à '" + expectedCode + "'"
        ));
    }

    @Then("the displayed categories all have a linked company containing {string}")
    public void verifyCategoriesHaveCompany(String expectedCompany) {
        List<String> companies = getPage().getDisplayedCompanies();
        assertFalse(companies.isEmpty(),
                "La liste est vide — aucune catégorie avec l'entreprise '" + expectedCompany + "'");
        companies.forEach(company -> assertTrue(
                company.toUpperCase().contains(expectedCompany.toUpperCase()),
                "❌ L'entreprise '" + company + "' ne contient pas '" + expectedCompany + "'"
        ));
    }

    // =========================
    // EMPTY TABLE
    // =========================
    @Then("no categories are displayed in the list")
    public void verifyNoCategoriesDisplayed() {
        assertTrue(getPage().isListEmpty(),
                "❌ La liste devrait être vide mais affiche des résultats");
    }

    // =========================
    // PAGINATION
    // =========================
    @When("the user clicks on page {int}")
    public void clickOnPage(int pageNumber) {
        getPage().clickOnPage(pageNumber);
    }

    @When("the user clicks on the last page")
    public void clickOnLastPage() {
        getPage().clickOnLastPage();
    }

    @Then("the list displays categories from page {int}")
    public void verifyCurrentPage(int expectedPage) {
        assertTrue(getPage().isOnPage(expectedPage),
                "❌ Page attendue : " + expectedPage + " | Page actuelle : " + getPage().getCurrentPage());
    }

    @Then("the list displays the last page of categories")
    public void verifyLastPage() {
        assertTrue(getPage().isLastPage(),
                "❌ On devrait être sur la dernière page (> page 1)");
    }

    // =========================
    // DELETE STEPS
    // =========================

    /**
     * Filtre la liste par nom puis clique sur l'icône delete de la première ligne.
     */
    @When("the user deletes the category {string}")
    public void deleteCategory(String categoryName) {
        getPage().filterByName(categoryName);
        getPage().clickDeleteOnFirstRow();
    }

    /**
     * Vérifie que la modale de confirmation de suppression est affichée.
     */
    @Then("a category deletion confirmation dialog is displayed")
    public void verifyDeleteConfirmDialogDisplayed() {
        assertTrue(
                getPage().isDeleteConfirmDialogDisplayed(),
                "❌ La modale de confirmation de suppression devrait être affichée"
        );
    }

    /**
     * Confirme la suppression en cliquant sur "Confirmer la suppression".
     */
    @When("the user confirms the deletion")
    public void confirmDeletion() {
        getPage().confirmDeletion();
    }

    /**
     * Annule la suppression en cliquant sur "Ne pas supprimer".
     */
    @When("the user cancels the deletion")
    public void cancelDeletion() {
        getPage().cancelDeletion();
    }

    /**
     * Vérifie qu'un message de succès est affiché après la suppression.
     */
    @Then("a category deletion success message is displayed")
    public void verifyDeletionSuccessMessage() {
        String msg = getPage().getDeletionSuccessMessage();
        assertFalse(msg.isEmpty(),
                "❌ Aucun message de succès affiché après la suppression");
        System.out.println("✅ Message de succès suppression : " + msg);
    }

    /**
     * Vérifie que la catégorie supprimée n'apparaît plus dans la liste.
     */
    @Then("the category {string} no longer appears in the list")
    public void verifyCategoryAbsent(String categoryName) {
        assertTrue(
                getPage().isCategoryAbsentAfterFilter(categoryName),
                "❌ La catégorie '" + categoryName + "' devrait avoir été supprimée mais est encore visible"
        );
    }

    /**
     * Vérifie que la catégorie est toujours présente dans la liste (après annulation).
     */
    @Then("the category {string} still appears in the list")
    public void verifyCategoryStillPresent(String categoryName) {
        assertTrue(
                getPage().isCategoryPresentAfterFilter(categoryName),
                "❌ La catégorie '" + categoryName + "' devrait être présente mais est introuvable"
        );
    }
}
