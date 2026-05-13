@categories @documents @requiresLogin
Feature: Documents tab in company
  Background:
    Given the user navigates to the companies list page
    And the user clicks on edit icon for company "CRCAM ALSACE VOSGE"
    And the category edit form is displayed
    And the document tab uses company labels
    And the user clicks on the documents tab

  @icons
  Scenario: Environment section documents are read-only with eye icon
    Then the documents tab is active
    And the section title "Documents rattach" is displayed
    Then documents are displayed in the "environnement" section
    And each document card in the "environnement" section has an eye icon
    And no delete icon is visible in the "environnement" section

  @icons
  Scenario: Groups section documents are read-only with eye icon
    Then documents are displayed in the "groupes" section
    And each document card in the "groupes" section has an eye icon
    And no delete icon is visible in the "groupes" section

  @icons @smoke
  Scenario: Company section documents are editable with delete icon
    Then documents are displayed in the "entreprises" section
    And each document card in the "entreprises" section has a delete icon
    And no eye icon is visible in the "entreprises" section

  @loadmore
  Scenario: Load more documents in company section
    Then documents are displayed in the "entreprises" section
    When the user clicks on load more documents in the "entreprises" section
    Then documents are displayed in the "entreprises" section

  @delete @cancel @confirm
  Scenario: Cancel and confirm deletion of a company-attached document
    Then documents are displayed in the "entreprises" section
    When the user deletes the first document in the "entreprises" section
    Then a document delete confirmation dialog is displayed
    When the user cancels the document deletion
    Then documents are displayed in the "entreprises" section
    When the user deletes the first document in the "entreprises" section
    Then a document delete confirmation dialog is displayed
    When the user confirms the document deletion
    Then the document is deleted successfully
