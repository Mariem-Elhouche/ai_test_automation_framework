package org.automation.steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import org.automation.factory.DriverFactory;
import org.automation.pages.company.CompanyDocumentPage;
import org.automation.pages.companyCategory.CompanyCategoryDocumentPage;
import org.automation.pages.document.DocumentListPage;
import org.automation.pages.environment.EnvironmentDocumentPage;
import org.automation.pages.group.GroupDocumentPage;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentSteps {

    private DocumentListPage documentPage;

    @Before
    public void resetPage() {
        documentPage = null;
    }

    private DocumentListPage getPage() {
        if (documentPage == null) {
            documentPage = resolvePageFromCurrentUrl();
        }
        return documentPage;
    }

    private DocumentListPage resolvePageFromCurrentUrl() {
        try {
            if (DriverFactory.getDriver() != null) {
                String currentUrl = DriverFactory.getDriver().getCurrentUrl().toLowerCase();
                if (currentUrl.contains("/entities/environment")) return new EnvironmentDocumentPage();
                if (currentUrl.contains("/entities/company-group")) return new GroupDocumentPage();
                if (currentUrl.contains("/entities/company/")) return new CompanyDocumentPage();
                if (currentUrl.contains("/entities/company-section")) return new CompanyCategoryDocumentPage();
            }
        } catch (Exception ignored) {
        }
        return new CompanyCategoryDocumentPage();
    }
    @Given("the document tab uses company labels")
    public void useCompanyLabels() { documentPage = new CompanyDocumentPage(); }

    @Given("the document tab uses environment labels")
    public void useEnvironmentLabels() { documentPage = new EnvironmentDocumentPage(); }

    @Given("the document tab uses group labels")
    public void useGroupLabels() { documentPage = new GroupDocumentPage();}

    @When("the user clicks on the documents tab")
    public void clickDocumentsTab() {
        getPage().clickDocumentsTab();
    }

    @Then("the documents tab is active")
    public void verifyDocumentsTabActive() {
        assertTrue(getPage().isDocumentsTabActive(),
                "L'onglet Documents devrait être actif");
    }

    @Then("the section title {string} is displayed")
    public void verifySectionTitleDisplayed(String expectedTitle) {
        assertTrue(getPage().isSectionTitleDisplayed(expectedTitle),
                "Le titre de section '" + expectedTitle + "' devrait être affiché");
    }

    @When("the user clicks on show groups documents")
    public void clickShowGroups() {
        getPage().clickShowGroups();
    }

    @When("the user clicks on show companies documents")
    public void clickShowCompanies() {
        getPage().clickShowCompanies();
    }

    @When("the user clicks on show categories documents")
    public void clickShowCategories() {
        getPage().clickShowCategories();
    }

    @When("the user clicks on show RH documents")
    public void clickShowRhDocuments() {
        getPage().clickShowRhDocuments();
    }

    @When("the user clicks on load more documents in the {string} section")
    public void clickLoadMoreDocumentsInSection(String sectionName) {
        getPage().clickLoadMoreDocumentsInSection(sectionName);
    }

    @Then("documents are displayed in the {string} section")
    public void verifyDocumentsDisplayedInSection(String sectionName) {
        assertTrue(getPage().areDocumentsDisplayedInSection(sectionName),
                "Des documents devraient être affichés dans la section '" + sectionName + "'");
    }

    @Then("no document message is displayed in the {string} section")
    public void verifyNoDocumentMessageInSection(String sectionName) {
        assertTrue(getPage().isNoDocumentMessageDisplayedInSection(sectionName),
                "Le message 'Aucun document' devrait être affiché dans la section '" + sectionName + "'");
    }

    @Then("the document count in the {string} section is {int}")
    public void verifyDocumentCountInSection(String sectionName, int expectedCount) {
        int actualCount = getPage().getDocumentCountInSection(sectionName);
        assertEquals(expectedCount, actualCount,
                "Le nombre de documents dans la section '" + sectionName +
                        "' devrait être " + expectedCount + ", mais est " + actualCount);
    }

    @Then("each document card in the {string} section has an eye icon")
    public void verifyEyeIconOnAllCardsInSection(String sectionName) {
        assertTrue(getPage().allCardsInSectionHaveEyeIcon(sectionName),
                "Chaque carte de la section '" + sectionName + "' devrait avoir une icône eye");
    }

    @Then("no delete icon is visible in the {string} section")
    public void verifyNoDeleteIconInSection(String sectionName) {
        assertTrue(getPage().noDeleteIconInSection(sectionName),
                "Aucune icône delete ne devrait être visible dans la section '" + sectionName + "'");
    }

    @Then("each document card in the {string} section has a delete icon")
    public void verifyDeleteIconOnAllCardsInSection(String sectionName) {
        assertTrue(getPage().allCardsInSectionHaveDeleteIcon(sectionName),
                "Chaque carte de la section '" + sectionName + "' devrait avoir une icône delete");
    }

    @Then("no eye icon is visible in the {string} section")
    public void verifyNoEyeIconInSection(String sectionName) {
        assertTrue(getPage().noEyeIconInSection(sectionName),
                "Aucune icône eye ne devrait être visible dans la section '" + sectionName + "'");
    }

    @When("the user clicks the eye icon on the first document in the {string} section")
    public void clickEyeIconFirstDocumentInSection(String sectionName) {
        getPage().clickEyeIconOnFirstDocumentInSection(sectionName);
    }

    @Then("the user is redirected to an environment detail page")
    public void verifyRedirectedToEnvironmentPage() {
        assertTrue(getPage().isOnEnvironmentDetailPage(),
                "L'utilisateur devrait être redirigé vers la page de détail de l'environnement");
    }

    @Then("the user is redirected to a company group detail page")
    public void verifyRedirectedToCompanyGroupPage() {
        assertTrue(getPage().isOnCompanyGroupDetailPage(),
                "L'utilisateur devrait être redirigé vers la page de détail du groupe d'entreprises");
    }

    @When("the user deletes the first document")
    public void deleteFirstDocument() {
        getPage().deleteFirstDocument();
    }

    @Then("a document delete confirmation dialog is displayed")
    public void verifyDeleteConfirmDialogDisplayed() {
        assertTrue(getPage().isDeleteConfirmDialogDisplayed(),
                "Le dialog de confirmation de suppression devrait être affiché");
    }

    @When("the user confirms the document deletion")
    public void confirmDocumentDeletion() {
        getPage().confirmDeletion();
    }

    @When("the user cancels the document deletion")
    public void cancelDocumentDeletion() {
        getPage().cancelDeletion();
    }

    @Then("the document is deleted successfully")
    public void verifyDocumentDeletedSuccessfully() {
        assertTrue(getPage().isDocumentDeletedSuccessfully(),
                "Le document devrait être supprimé avec succès");
    }

    @When("the user deletes the first document in the {string} section")
    public void deleteFirstDocumentInSection(String sectionName) {
        getPage().deleteFirstDocumentInSection(sectionName);
    }
}
