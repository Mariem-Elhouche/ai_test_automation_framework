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
        names.forEach(name ->
                assertTrue(
                        name.toUpperCase().contains(expected.toUpperCase()),
                        "❌ Le nom '" + name + "' ne contient pas '" + expected + "'"
                )
        );
        System.out.println("✅ " + names.size() + " catégories affichées contenant '" + expected + "'");
    }

    @Then("the displayed categories all have code {string}")
    public void verifyCategoriesHaveCode(String expectedCode) {
        List<String> codes = getPage().getDisplayedCodes();
        assertFalse(codes.isEmpty(),
                "La liste est vide — aucune catégorie avec le code '" + expectedCode + "'");
        codes.forEach(code ->
                assertTrue(
                        code.contains(expectedCode),
                        "❌ Le code '" + code + "' ne correspond pas à '" + expectedCode + "'"
                )
        );
        System.out.println("✅ " + codes.size() + " catégories avec code '" + expectedCode + "'");
    }

    @Then("the displayed categories all have a linked company containing {string}")
    public void verifyCategoriesHaveCompany(String expectedCompany) {
        List<String> companies = getPage().getDisplayedCompanies();
        assertFalse(companies.isEmpty(),
                "La liste est vide — aucune catégorie avec l'entreprise '" + expectedCompany + "'");
        companies.forEach(company ->
                assertTrue(
                        company.toUpperCase().contains(expectedCompany.toUpperCase()),
                        "❌ L'entreprise '" + company + "' ne contient pas '" + expectedCompany + "'"
                )
        );
        System.out.println("✅ " + companies.size() +
                " catégories avec entreprise contenant '" + expectedCompany + "'");
    }

    @Then("no categories are displayed in the list")
    public void verifyNoCategoriesDisplayed() {
        assertTrue(
                getPage().isListEmpty(),
                "❌ La liste devrait être vide mais affiche des résultats"
        );
        System.out.println("✅ Aucune catégorie affichée — filtre sans résultat confirmé");
    }

    // =========================
    // PAGINATION STEPS
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
        assertTrue(
                getPage().isOnPage(expectedPage),
                "❌ Page attendue : " + expectedPage +
                        " | Page actuelle : " + getPage().getCurrentPage()
        );
        System.out.println("✅ Navigation vers page " + expectedPage + " confirmée");
    }

    @Then("the list displays the last page of categories")
    public void verifyLastPage() {
        assertTrue(
                getPage().isLastPage(),
                "❌ On devrait être sur la dernière page (> page 1)"
        );
        System.out.println("✅ Dernière page affichée : page " + getPage().getCurrentPage());
    }
}