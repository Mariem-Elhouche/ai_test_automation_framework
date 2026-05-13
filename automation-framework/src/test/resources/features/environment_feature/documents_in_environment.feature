@environment @documents @requiresLogin
Feature: Onglet Documents dans l'environnement

  Background:
    Given the user navigates to the environments list page
    And the user edits the environment "AG2R"


  @smoke
  Scenario: Navigate to documents tab
    When the user clicks on the documents tab
    Then documents are displayed in the "environnement" section

  @smoke
  Scenario: Environment section documents are editable with delete icon
    When the user clicks on the documents tab
    Then documents are displayed in the "environnement" section
    And each document card in the "environnement" section has a delete icon
    And no eye icon is visible in the "environnement" section


  @groups
  Scenario: Show documents for groups
    When the user clicks on the documents tab
    And the user clicks on show groups documents
    Then documents are displayed in the "groupes" section
    And each document card in the "groupes" section has an eye icon
    And no delete icon is visible in the "groupes" section

  @companies
  Scenario: Show documents for companies
    When the user clicks on the documents tab
    And the user clicks on show companies documents
    Then no document message is displayed in the "entreprises" section

  @categories
  Scenario: Show documents for categories
    When the user clicks on the documents tab
    And the user clicks on show categories documents
    Then no document message is displayed in the "catégories" section

  @groups @navigation
  Scenario: Clicking eye icon on group document navigates to group page
    When the user clicks on the documents tab
    And the user clicks on show groups documents
    Then documents are displayed in the "groupes" section
    When the user clicks the eye icon on the first document in the "groupes" section
    Then the user is redirected to a company group detail page

  @delete @smoke
  Scenario: Delete a document with confirmation
    When the user clicks on the documents tab
    And the user deletes the first document in the "environnement" section
    Then a document delete confirmation dialog is displayed
    When the user confirms the document deletion
    Then the document is deleted successfully

  @delete
  Scenario: Cancel deletion of a document
    When the user clicks on the documents tab
    And the user deletes the first document in the "environnement" section
    Then a document delete confirmation dialog is displayed
    When the user cancels the document deletion
    Then documents are displayed in the "environnement" section