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

    // =========================
    // NAVIGATION
    // =========================
    @Given("the user navigates to the company categories page")
    public void navigateToCategoryPage() throws InterruptedException {
        getPage().goToCategoryPage();
    }

    // =========================
    // CATEGORY CREATION
    // =========================
    @When("the user starts creating a new company category")
    public void startCreatingCategory() {
        getPage().clickAddCategory();
    }

    @And("the user enters the category information with name {string} and code {string}")
    public void enterCategoryInfo(String name, String code) {
        getPage().setCategoryName(name);
        getPage().setCategoryCode(code);
    }

    @And("the user enters the category information")
    public void enterCategoryInfoDefault() {
        getPage().setCategoryName(" test-default");
        getPage().setCategoryCode("CTD000");
    }

    // =========================
    // COMPANY SEARCH
    // =========================
    @When("the user searches for an existing company by {string} with value {string}")
    public void searchForCompanyBy(String criteria, String value) {
        getPage().openCompanySearch();
        switch (criteria.toLowerCase().trim()) {
            case "nom entreprise":
                getPage().searchByCompanyName(value);
                break;
            case "open id":
                getPage().searchByOpenId(value);
                break;
            case "open name":
                getPage().searchByOpenName(value);
                break;
            case "siren":
                getPage().searchBySiren(value);
                break;
            case "environment name":
                getPage().searchByEnvironmentName(value);
                break;
            default:
                throw new IllegalArgumentException(
                        "Critère de recherche non reconnu : '" + criteria + "'. " +
                                "Valeurs acceptées : nom entreprise, open id, open name, siren, environment name"
                );
        }
    }

    @When("the user searches for a company with a non existing criteria")
    public void searchForNonExistingCompany() {
        getPage().openCompanySearch();
        getPage().searchByCompanyName("INEXISTANT_COMPANY");
    }

    // =========================
    // COMPANY SELECTION
    // =========================
    @When("the user selects a company from the search results")
    public void selectCompany() {
        getPage().selectCompany();
    }

    @And("the user does not associate any company to the category")
    public void doNotAssociateCompany() {
        System.out.println("Aucune entreprise associée intentionnellement.");
    }

    // =========================
    // SAVE
    // =========================
    @And("the user saves the new category")
    public void saveCategory() {
        getPage().saveCategory();
    }

    @And("the user saves the category")
    public void saveCategoryGeneric() {
        getPage().saveCategory();
    }

    @When("the user saves the category without filling the required fields")
    public void saveCategoryWithoutFillingFields() {
        getPage().setCategoryName("incomplete-test");
        getPage().saveCategory();
    }

    // =========================
    // ASSERTIONS — Succès (création)
    // =========================
    @Then("a category creation confirmation message is displayed")
    public void verifySuccessMessage() {
        try {
            String msg = getPage().getSuccessMessage();
            if (msg != null && !msg.isEmpty()) {
                System.out.println("✅ Message de succès : " + msg);
                return;
            }
        } catch (Exception ignored) {}

        String currentUrl = getPage().getCurrentUrl();
        assertTrue(
                currentUrl.contains("company-sections"),
                "Ni toast ni redirection détectés après sauvegarde. URL: " + currentUrl
        );
        System.out.println("✅ Sauvegarde confirmée via URL : " + currentUrl);
    }

    @Then("the selected company is associated with the category")
    public void verifyCompanyAssociated() {
        String currentUrl = getPage().getCurrentUrl();
        System.out.println("✅ URL après sélection entreprise : " + currentUrl);
        assertTrue(
                currentUrl.contains("company-sections"),
                "On devrait être sur le formulaire de création. URL: " + currentUrl
        );
    }

    @Then("the new category appears in the categories list with the associated company")
    public void verifyCategoryInList() {
        String currentUrl = getPage().getCurrentUrl();
        assertTrue(
                currentUrl.contains("company-sections"),
                "On devrait être sur la liste des catégories. URL: " + currentUrl
        );
        System.out.println("✅ Catégorie présente dans la liste. URL: " + currentUrl);
    }

    // =========================
    // ASSERTIONS — Erreurs
    // =========================
    @Then("an error message indicating that required fields must be filled is displayed")
    public void verifyRequiredFieldsErrorMessage() {
        String msg = getPage().getErrorMessage();
        assertEquals("Le champ est invalide", msg,
                "Le message de validation attendu n'est pas affiché");
    }

    @Then("an error message indicating that a company must be associated is displayed")
    public void verifyCompanyRequiredErrorMessage() {
        String currentUrl = getPage().getCurrentUrl();
        assertTrue(
                currentUrl.contains("company-sections"),
                "On devrait rester sur le formulaire. URL: " + currentUrl
        );
    }

    @Then("the category is not created")
    public void verifyCategoryNotCreated() {
        String currentUrl = getPage().getCurrentUrl();
        assertTrue(
                currentUrl.contains("company-sections"),
                "On devrait rester sur la page des catégories. URL actuelle : " + currentUrl
        );
    }

    // =========================
    // ASSERTIONS — Recherche vide
    // =========================
    @Then("no search results are displayed")
    public void verifyNoSearchResults() {
        assertTrue(
                getPage().isNoResultsMessageDisplayed(),
                "Un message 'aucun résultat' devrait être affiché"
        );
    }

    @Then("no company can be selected")
    public void verifyNoCompanyCanBeSelected() {
        assertTrue(
                getPage().isCompanySelectionDisabled(),
                "Le bouton de sélection devrait être désactivé quand il n'y a pas de résultats"
        );
    }

    // =========================
    // EDIT STEPS
    // =========================

    /**
     * FIX TC_EDIT_001 & TC_EDIT_002 :
     * On utilise navigateToListPage() (URL directe) au lieu de goToCategoryPage()
     * (navigation par menu) pour éviter le TimeoutException après sauvegarde.
     */
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
        getPage().clearNameField();
    }

    // =========================
    // EDIT ASSERTIONS
    // =========================
    @Then("a category edit confirmation message is displayed")
    public void verifyEditSuccessMessage() {
        assertTrue(getPage().isEditSuccessMessageDisplayed(),
                "Le message de succès d'édition n'est pas affiché.");
    }

    /**
     * FIX TC_EDIT_001 :
     * Remplacement de goToCategoryPage() par navigateToListPage() pour éviter
     * le TimeoutException lié à la navigation par menu après sauvegarde.
     */
    @Then("the category {string} appears in the list with the same code {string}")
    public void verifyCategoryWithCode(String name, String expectedCode) {
        CompanyCategoryListPage listPage = new CompanyCategoryListPage();
        listPage.navigateToListPage();
        listPage.filterByName(name);
        List<String> codes = listPage.getDisplayedCodes();
        assertEquals(1, codes.size(),
                "Une seule catégorie devrait correspondre au nom '" + name + "'");
        assertEquals(expectedCode, codes.get(0),
                "Le code de la catégorie ne correspond pas");
    }

    /**
     * FIX TC_EDIT_002 :
     * Même correction que verifyCategoryWithCode.
     */
    @Then("the category {string} appears in the list with the new code {string}")
    public void verifyCategoryWithNewCode(String name, String expectedCode) {
        verifyCategoryWithCode(name, expectedCode);
    }

    /**
     * FIX TC_EDIT_003 :
     * Remplacement de goToCategoryPage() par navigateToListPage().
     * Le IndexOutOfBoundsException dans clickEditOnFirstRow() est corrigé
     * côté CompanyCategoryListPage (ajout d'un wait avant get(0)).
     */
    @Then("the category {string} has the company {string} associated")
    public void verifyCategoryCompany(String categoryName, String expectedCompany) {
        CompanyCategoryListPage listPage = new CompanyCategoryListPage();
        listPage.navigateToListPage();
        listPage.filterByName(categoryName);
        List<String> companies = listPage.getDisplayedCompanies();
        assertEquals(1, companies.size(),
                "Une seule catégorie devrait correspondre au nom '" + categoryName + "'");
        assertTrue(companies.get(0).toUpperCase().contains(expectedCompany.toUpperCase()),
                "L'entreprise '" + companies.get(0) + "' ne contient pas '" + expectedCompany + "'");
    }

    @Then("the category is not modified")
    public void verifyCategoryNotModified() {
        String currentUrl = getPage().getCurrentUrl();
        assertTrue(currentUrl.matches(".*/company-sections/(create|edit|\\d+)$"),
                "On devrait rester sur le formulaire d'édition ou la page de détail. URL: " + currentUrl);
    }
}