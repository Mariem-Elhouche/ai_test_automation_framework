package org.automation.steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import org.automation.pages.group.GroupAttachmentPage;

import static org.junit.jupiter.api.Assertions.*;

public class GroupAttachmentSteps {

    private GroupAttachmentPage page;

    @Before
    public void init() {
        page = new GroupAttachmentPage();
    }

    // ── Tab ───────────────────────────────────────────────────────────────────────

    @When("the user clicks on the group attachments tab")
    public void clickGroupAttachmentsTab() {
        page.clickAttachmentsTab();
    }

    @Then("the group attachments tab is active")
    public void verifyGroupAttachmentsTabActive() {
        assertTrue(page.isAttachmentsTabActive(),
                "L'onglet Rattachements devrait être actif");
    }

    @Then("the group attachment section title {string} is displayed")
    public void verifyGroupAttachmentSectionTitleDisplayed(String expectedTitle) {
        assertTrue(page.isSectionTitleDisplayed(expectedTitle),
                "Le titre '" + expectedTitle + "' devrait être affiché");
    }

    // ── Table ─────────────────────────────────────────────────────────────────────

    @Then("the group attachments table is displayed")
    public void verifyGroupAttachmentsTableDisplayed() {
        assertTrue(page.isTableDisplayed(),
                "Le tableau des rattachements devrait être affiché");
    }

    @Then("the group attachments table is empty")
    public void verifyGroupAttachmentsTableEmpty() {
        assertTrue(page.isEmptyTableMessageDisplayed(),
                "Le message 'Aucun' devrait être affiché");
    }

    @Then("the group attachments table has {int} row(s)")
    public void verifyGroupAttachmentsTableRowCount(int expectedCount) {
        int actual = page.getRowCount();
        assertEquals(expectedCount, actual,
                "Le tableau devrait avoir " + expectedCount + " ligne(s), mais en a " + actual);
    }

    // ── Filtering ─────────────────────────────────────────────────────────────────

    @When("the user filters group attachments by name {string}")
    public void filterGroupAttachmentsByName(String name) {
        page.filterByName(name);
    }

    @When("the user filters group attachments by open id {string}")
    public void filterGroupAttachmentsByOpenId(String openId) {
        page.filterByOpenId(openId);
    }

    @When("the user clears the group attachments name filter")
    public void clearGroupAttachmentsNameFilter() {
        page.clearNameFilter();
    }

    @When("the user clears the group attachments open id filter")
    public void clearGroupAttachmentsOpenIdFilter() {
        page.clearOpenIdFilter();
    }

    @Then("all displayed group attachments contain {string} in their name")
    public void verifyGroupAttachmentsFilteredByName(String name) {
        assertTrue(page.areRowsFilteredByName(name),
                "Toutes les lignes devraient contenir '" + name + "' dans le nom");
    }

    // ── Pagination ────────────────────────────────────────────────────────────────

    @Then("the group attachments next page button is enabled")
    public void verifyGroupNextPageButtonEnabled() {
        assertTrue(page.isNextPageButtonEnabled(),
                "Le bouton 'page suivante' devrait être activé");
    }

    @Then("the group attachments previous page button is enabled")
    public void verifyGroupPreviousPageButtonEnabled() {
        assertTrue(page.isPreviousPageButtonEnabled(),
                "Le bouton 'page précédente' devrait être activé");
    }

    @Then("the group attachments previous page button is disabled")
    public void verifyGroupPreviousPageButtonDisabled() {
        assertFalse(page.isPreviousPageButtonEnabled(),
                "Le bouton 'page précédente' devrait être désactivé");
    }

    @When("the user clicks on the group attachments next page")
    public void clickGroupAttachmentsNextPage() {
        page.clickNextPage();
    }

    @When("the user clicks on the group attachments previous page")
    public void clickGroupAttachmentsPreviousPage() {
        page.clickPreviousPage();
    }

    @Then("the group attachments current page is {int}")
    public void verifyGroupAttachmentsCurrentPage(int expectedPage) {
        assertEquals(expectedPage, page.getCurrentPage(),
                "La page courante devrait être " + expectedPage);
    }

    @Then("group attachments page {int} is active in the pagination")
    public void verifyGroupAttachmentsPageActive(int pageNumber) {
        assertTrue(page.isPageActive(pageNumber),
                "La page " + pageNumber + " devrait être active");
    }

    // ── Row icons ─────────────────────────────────────────────────────────────────

    @When("the user clicks the view icon on group attachment row {int}")
    public void clickViewIconOnGroupAttachmentRow(int rowIndex) {
        page.clickViewIconOnRow(rowIndex - 1);
    }

    @When("the user clicks the edit icon on group attachment row {int}")
    public void clickEditIconOnGroupAttachmentRow(int rowIndex) {
        page.clickEditIconOnRow(rowIndex - 1);
    }

    @When("the user clicks the delete icon on group attachment row {int}")
    public void clickDeleteIconOnGroupAttachmentRow(int rowIndex) {
        page.clickDeleteIconOnRow(rowIndex - 1);
    }

    // ── Add element dropdown ──────────────────────────────────────────────────────

    @When("the user clicks on add group attachment button")
    public void clickAddGroupAttachmentButton() {
        page.clickAddElementButton();
    }

    @Then("the group attachment dropdown is open")
    public void verifyGroupAttachmentDropdownOpen() {
        assertTrue(page.isAddElementDropdownOpen(),
                "Le dropdown 'Ajouter un élément' devrait être ouvert");
    }

    @When("the user searches for {string} in the group attachment dropdown")
    public void typeSearchInGroupAttachmentDropdown(String text) {
        page.typeSearchInDropdown(text);
    }

    @When("the user selects the company {string} in the group attachment dropdown")
    public void selectCompanyInGroupAttachmentDropdown(String companyName) {
        page.selectCompanyInDropdown(companyName);
    }

    @When("the user closes the group attachment dropdown")
    public void closeGroupAttachmentDropdown() {
        page.closeAddElementDropdown();
    }

    @Then("the group attachment save button is disabled")
    public void verifyGroupAttachmentSaveButtonDisabled() {
        assertFalse(page.isSaveButtonEnabled(),
                "Le bouton 'Enregistrer' devrait être désactivé");
    }

    @Then("the group attachment save button is enabled")
    public void verifyGroupAttachmentSaveButtonEnabled() {
        assertTrue(page.isSaveButtonEnabled(),
                "Le bouton 'Enregistrer' devrait être activé");
    }

    @When("the user saves the group attachment")
    public void saveGroupAttachment() {
        page.clickSaveButton();
    }

    @Then("a group attachment creation success toast is displayed")
    public void verifyGroupAttachmentCreationSuccessToastDisplayed() {
        assertTrue(page.isAttachmentCreationSuccessToastDisplayed(),
                "Un toast de succès de création du rattachement devrait être affiché");
    }

    @Then("the company {string} is present in the group attachments table")
    public void verifyCompanyPresentInGroupAttachmentsTable(String companyName) {
        assertTrue(page.isCompanyPresentInTable(companyName),
                "L'entreprise '" + companyName + "' devrait être dans le tableau");
    }

    // ── Create new company ────────────────────────────────────────────────────────

    @When("the user clicks on create new company in this group")
    public void clickCreateNewCompanyInGroup() {
        page.clickCreateNewCompanyButton();
    }

    @Then("the user is redirected to the company creation page from group")
    public void verifyRedirectedToCompanyCreationPageFromGroup() {
        assertTrue(page.isOnCompanyCreationPage(),
                "L'utilisateur devrait être redirigé vers la page de création d'entreprise");
    }

    // ── Delete flow ───────────────────────────────────────────────────────────────

    @Then("a group attachment delete confirmation dialog is displayed")
    public void verifyGroupAttachmentDeleteConfirmDialogDisplayed() {
        assertTrue(page.isDeleteConfirmDialogDisplayed(),
                "Le dialog de confirmation de suppression devrait être affiché");
    }

    @When("the user confirms the group attachment deletion")
    public void confirmGroupAttachmentDeletion() {
        page.confirmDeletion();
    }

    @When("the user cancels the group attachment deletion")
    public void cancelGroupAttachmentDeletion() {
        page.cancelDeletion();
    }

    @Then("the group attachment is deleted successfully")
    public void verifyGroupAttachmentDeletedSuccessfully() {
        assertTrue(page.isDeletionSuccessful(),
                "Le rattachement devrait être supprimé avec succès");
    }
}
