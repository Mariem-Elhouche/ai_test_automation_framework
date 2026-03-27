package org.automation.steps;

import org.automation.pages.CompanyCategoryListPage;
import static org.junit.jupiter.api.Assertions.*;

import io.cucumber.java.en.*;
import org.automation.pages.CompanyCategoryPage;
import org.automation.pages.LoginPage;

import java.util.List;

public class CompanyCategorySteps {

    private LoginPage loginPage;
    private CompanyCategoryPage page;

    private LoginPage getLoginPage() {
        if (loginPage == null) loginPage = new LoginPage();
        return loginPage;
    }

    private CompanyCategoryPage getPage() {
        if (page == null) page = new CompanyCategoryPage();
        return page;
    }

    @Given("the user navigates to the company categories page")
    public void navigateToCategoryPage() throws InterruptedException {
        getPage().goToCategoryPage();
    }

    @When("the user starts creating a new company category")
    public void startCreatingCategory() throws InterruptedException {
        Thread.sleep(600);
        getPage().clickAddCategory();
        Thread.sleep(600);
    }

    @And("the user enters the category information with name {string} and code {string}")
    public void enterCategoryInfo(String name, String code) throws InterruptedException {
        getPage().setCategoryName(name);
        Thread.sleep(400);
        getPage().setCategoryCode(code);
        Thread.sleep(400);
    }

    @And("the user enters the category information")
    public void enterCategoryInfoDefault() throws InterruptedException {
        getPage().setCategoryName(" test-default");
        Thread.sleep(400);
        getPage().setCategoryCode("CTD000");
        Thread.sleep(400);
    }

    @When("the user searches for an existing company by {string} with value {string}")
    public void searchForCompanyBy(String criteria, String value) throws InterruptedException {
        Thread.sleep(500);
        getPage().openCompanySearch();
        Thread.sleep(500);
        switch (criteria.toLowerCase().trim()) {
            case "nom entreprise":   getPage().searchByCompanyName(value);     break;
            case "open id":          getPage().searchByOpenId(value);          break;
            case "open name":        getPage().searchByOpenName(value);        break;
            case "siren":            getPage().searchBySiren(value);           break;
            case "environment name": getPage().searchByEnvironmentName(value); break;
            default: throw new IllegalArgumentException("Critère non reconnu : '" + criteria + "'");
        }
        Thread.sleep(600);
    }

    @When("the user selects a company from the search results with value {string}")
    public void selectCompany(String searchValue) throws InterruptedException {
        Thread.sleep(500);
        getPage().selectCompanyByCriteria(searchValue);
        Thread.sleep(500);
    }

    @When("the user searches for a company with a non existing criteria")
    public void searchForNonExistingCompany() {
        getPage().openCompanySearch();
        getPage().searchByCompanyName("INEXISTANT_COMPANY");
    }

    @And("the user does not associate any company to the category")
    public void doNotAssociateCompany() {}

    @And("the user saves the new category")
    @And("the user saves the category")
    public void saveCategory() throws InterruptedException {
        Thread.sleep(500);
        getPage().saveCategory();
        Thread.sleep(800);
    }

    @When("the user saves the category without filling the required fields")
    public void saveCategoryWithoutFillingFields() {
        getPage().setCategoryName("incomplete-test");
        getPage().saveCategory();
    }

    @Then("a category creation confirmation message is displayed")
    public void verifySuccessMessage() {
        String msg = getPage().getSuccessMessage();
        assertNotNull(msg, "Le message de confirmation est manquant");
        assertFalse(msg.isBlank(), "Le message de confirmation est vide");
    }

    @Then("the selected company is associated with the category")
    public void verifyCompanyAssociated() {
        assertTrue(getPage().getCurrentUrl().contains("company-sections"));
    }

    @Then("the new category appears in the categories list with the associated company")
    public void verifyCategoryInList() {
        String categoryName = getPage().getCategoryName();
        CompanyCategoryListPage listPage = new CompanyCategoryListPage();
        listPage.navigateToListPage();
        listPage.filterByName(categoryName);
        assertTrue(
                listPage.isCategoryPresentAfterFilter(categoryName),
                "La catégorie '" + categoryName + "' devrait être présente dans la liste"
        );
    }

    @Then("an error message indicating that required fields must be filled is displayed")
    public void verifyRequiredFieldsErrorMessage() {
        assertEquals("Le champ est invalide", getPage().getErrorMessage());
    }

    @Then("an error message indicating that a company must be associated is displayed")
    public void verifyCompanyRequiredErrorMessage() {
        assertTrue(getPage().getCurrentUrl().contains("company-sections"));
    }

    @Then("the category is not created")
    public void verifyCategoryNotCreated() {
        String categoryName = getPage().getCategoryName();
        CompanyCategoryListPage listPage = new CompanyCategoryListPage();
        listPage.navigateToListPage();
        assertTrue(
                listPage.isCategoryAbsentAfterFilter(categoryName),
                "La catégorie '" + categoryName + "' ne devrait pas être présente dans la liste"
        );
    }

    @Then("no search results are displayed")
    public void verifyNoSearchResults() {
        assertTrue(getPage().isNoResultsMessageDisplayed());
    }

    @Then("no company can be selected")
    public void verifyNoCompanyCanBeSelected() {
        assertTrue(getPage().isCompanySelectionDisabled());
    }

    @When("the user edits the category {string} and changes its name to {string}")
    public void editCategoryName(String oldName, String newName) {
        CompanyCategoryListPage listPage = new CompanyCategoryListPage();
        listPage.filterByName(oldName);
        listPage.clickEditOnFirstRow();
        getPage().setCategoryName(newName);
        getPage().saveCategory();
    }

    @When("the user edits the category {string} and changes its code to {string}")
    public void editCategoryCode(String oldName, String newCode) {
        CompanyCategoryListPage listPage = new CompanyCategoryListPage();
        listPage.filterByName(oldName);
        listPage.clickEditOnFirstRow();
        getPage().setCategoryCode(newCode);
        getPage().saveCategory();
    }

    @When("the user edits the category {string} and associates the company {string}")
    public void editCategoryCompany(String categoryName, String companyName) throws InterruptedException {
        CompanyCategoryListPage listPage = new CompanyCategoryListPage();
        listPage.filterByName(categoryName);
        listPage.clickEditOnFirstRow();
        getPage().changeAssociatedCompany(companyName);
        getPage().saveCategory();
    }

    @When("the user edits the category {string} and clears the name field")
    public void editCategoryClearName(String categoryName) {
        CompanyCategoryListPage listPage = new CompanyCategoryListPage();
        listPage.filterByName(categoryName);
        listPage.clickEditOnFirstRow();
        getPage().setCategoryName("");
    }

    @Then("a category edit confirmation message is displayed")
    public void verifyEditSuccessMessage() {
        assertTrue(getPage().isEditSuccessMessageDisplayed());
    }

    @Then("the category {string} appears in the list with the same code {string}")
    public void verifyCategoryWithCode(String name, String expectedCode) {
        CompanyCategoryListPage listPage = new CompanyCategoryListPage();
        listPage.navigateToListPage();
        listPage.filterByName(name);
        List<String> codes = listPage.getDisplayedCodes();
        assertEquals(1, codes.size());
        assertEquals(expectedCode, codes.get(0));
    }

    @Then("the category {string} appears in the list with the new code {string}")
    public void verifyCategoryWithNewCode(String name, String expectedCode) {
        verifyCategoryWithCode(name, expectedCode);
    }

    @Then("the category {string} has the company {string} associated")
    public void verifyCategoryCompany(String categoryName, String expectedCompany) {
        CompanyCategoryListPage listPage = new CompanyCategoryListPage();
        listPage.navigateToListPage();
        listPage.filterByName(categoryName);
        List<String> companies = listPage.getDisplayedCompanies();
        assertEquals(1, companies.size());
        assertTrue(companies.get(0).toUpperCase().contains(expectedCompany.toUpperCase()));
    }

    @Then("the category is not modified")
    public void verifyCategoryNotModified() {
        assertTrue(getPage().getCurrentUrl().matches(".*/company-sections/(create|edit|\\d+)$"));
    }
}
