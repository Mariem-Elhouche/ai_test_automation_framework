package org.automation.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.automation.pages.company.CompanyAttachmentPage;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CompanyAttachmentSteps {

    private CompanyAttachmentPage attachmentPage;

    private CompanyAttachmentPage getPage() {
        if (attachmentPage == null) attachmentPage = new CompanyAttachmentPage();
        return attachmentPage;
    }

    @When("the user clicks on the attachments tab")
    public void clickAttachmentsTab() {
        getPage().clickAttachmentsTab();
    }

    @Then("the attachments tab is active")
    public void verifyAttachmentsTabActive() {
        assertTrue(getPage().isAttachmentsTabActive(),
                "L'onglet Rattachements devrait être actif");
    }

    @When("the user attaches the company group {string}")
    public void attachCompanyGroup(String groupName) {
        getPage().attachCompanyGroup(groupName);
    }

    @Then("the company group {string} is attached")
    public void verifyGroupAttached(String groupName) {
        assertTrue(getPage().isGroupAttached(groupName),
                "Le groupe '" + groupName + "' devrait être rattaché");
    }

    @When("the user opens the add category form")
    public void openAddCategoryForm() {
        getPage().openAddCategoryForm();
    }

    @When("the user fills the category name {string} and code {string}")
    public void fillCategory(String name, String code) {
        getPage().fillCategory(name, code);
    }

    @When("the user saves the attachments category")
    public void saveCategory() {
        getPage().saveCategory();
    }

    @When("the user deletes the attached company group {string}")
    public void deleteCompanyGroup(String groupName) {
        getPage().clickDeleteOnGroup(groupName);
    }

    @Then("an attachments delete confirmation dialog is displayed")
    public void verifyDeleteConfirmDialogDisplayed() {
        assertTrue(getPage().isDeleteConfirmDialogDisplayed(),
                "La modale de confirmation devrait être affichée");
    }

    @Then("an attachments category delete confirmation dialog is displayed")
    public void verifyCategoryDeleteConfirmDialogDisplayed() {
        assertTrue(getPage().isDeleteConfirmDialogDisplayed(),
                "La modale de confirmation de suppression de la catégorie devrait être affichée");
    }

    @When("the user cancels the attachments deletion")
    public void cancelDeletion() {
        getPage().cancelDeletion();
    }

    @When("the user confirms the attachments deletion")
    public void confirmDeletion() {
        getPage().confirmDeletion();
    }

    @When("the user cancels the attachments category deletion")
    public void cancelCategoryDeletion() {
        getPage().cancelDeletion();
    }

    @When("the user confirms the attachments category deletion")
    public void confirmCategoryDeletion() {
        getPage().confirmDeletion();
    }

    @Then("the company group {string} is not attached")
    public void verifyGroupNotAttached(String groupName) {
        getPage().refreshAndReopenAttachmentsTab();
        getPage().waitUntilGroupNotAttached(groupName);
        assertFalse(getPage().isGroupAttached(groupName),
                "Le groupe '" + groupName + "' ne devrait plus être rattaché");
    }

    @Then("no delete confirmation dialog is displayed")
    public void verifyNoDeleteConfirmDialogDisplayed() {
        assertFalse(getPage().isDeleteConfirmDialogDisplayed(),
                "Aucune modale de confirmation ne devrait être affichée");
    }

    @Then("the category {string} with code {string} is present")
    public void verifyCategoryPresent(String name, String code) {
        getPage().waitUntilCategoryPresent(name, code);
        assertTrue(getPage().isCategoryPresent(name, code),
                "La catégorie '" + name + "' (" + code + ") devrait être visible");
    }

    @When("the user deletes the category {string} with code {string}")
    public void deleteCategory(String name, String code) {
        getPage().deleteCategory(name, code);
    }

    @Then("the category {string} with code {string} is not present")
    public void verifyCategoryNotPresent(String name, String code) {
        getPage().refreshAndReopenAttachmentsTab();
        getPage().waitUntilCategoryNotPresent(name, code);
        assertFalse(getPage().isCategoryPresent(name, code),
                "La catégorie '" + name + "' (" + code + ") ne devrait plus être visible");
    }
}

