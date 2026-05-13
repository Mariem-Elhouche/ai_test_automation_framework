package org.automation.pages.document;

public interface DocumentListPage {

    void clickDocumentsTab();

    boolean isDocumentsTabActive();

    boolean isSectionTitleDisplayed(String expectedTitle);

    void clickShowGroups();

    void clickShowCompanies();

    default void clickShowCategories() {
        throw new UnsupportedOperationException("Section categories non supportee pour ce module");
    }

    default void clickShowRhDocuments() {
        throw new UnsupportedOperationException("Section RH non supportee pour ce module");
    }

    default void clickLoadMoreDocumentsInSection(String sectionName) {
        throw new UnsupportedOperationException("Load more non supporte pour ce module");
    }

    boolean areDocumentsDisplayedInSection(String sectionName);

    boolean isNoDocumentMessageDisplayedInSection(String sectionName);

    int getDocumentCountInSection(String sectionName);

    boolean allCardsInSectionHaveEyeIcon(String sectionName);

    boolean noDeleteIconInSection(String sectionName);

    boolean allCardsInSectionHaveDeleteIcon(String sectionName);

    boolean noEyeIconInSection(String sectionName);

    void clickEyeIconOnFirstDocumentInSection(String sectionName);

    boolean isOnEnvironmentDetailPage();

    boolean isOnCompanyGroupDetailPage();

    void deleteFirstDocument();

    boolean isDeleteConfirmDialogDisplayed();

    void confirmDeletion();

    void cancelDeletion();

    boolean isDocumentDeletedSuccessfully();

    void deleteFirstDocumentInSection(String sectionName);
}
