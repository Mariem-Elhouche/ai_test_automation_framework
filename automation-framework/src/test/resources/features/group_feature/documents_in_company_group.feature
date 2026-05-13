@groups @documents @requiresLogin
Feature: Documents tab in company group

  Background:
    Given the user navigates to the company groups list page


  @smoke
  Scenario: Documents tab loads and displays the section title
    When the user edits the company group "GE VISION"
    And the document tab uses group labels
    And the user clicks on the documents tab
    Then the documents tab is active
    And the section title "Documents rattachés à l'espace assuré" is displayed

  # ══════════════════════════════════════════════
  #  SECTION ENVIRONNEMENT
  # ══════════════════════════════════════════════

  @smoke @environment
  Scenario: Environment section loads documents after clicking load button
    When the user edits the company group "GE VISION"
    And the document tab uses group labels
    And the user clicks on the documents tab
    And the user clicks on show groups documents
    Then documents are displayed in the "environnement" section

  @environment
  Scenario: Environment section documents are read-only with eye icon
    When the user edits the company group "AVEZ"
    And the document tab uses group labels
    And the user clicks on the documents tab
    And the user clicks on show groups documents
    Then documents are displayed in the "environnement" section
    And each document card in the "environnement" section has an eye icon
    And no delete icon is visible in the "environnement" section

  # ══════════════════════════════════════════════
  #  SECTION GROUPES
  # ══════════════════════════════════════════════


  @smoke @group-section
  Scenario: Group section documents have delete icon and no eye icon
    When the user edits the company group "GE VISION"
    And the document tab uses group labels
    And the user clicks on the documents tab
    Then documents are displayed in the "groupes" section
    And each document card in the "groupes" section has a delete icon
    And no eye icon is visible in the "groupes" section

  @smoke @delete @group-section
  Scenario: Delete first document in group section shows confirmation dialog
    When the user edits the company group "GE VISION"
    And the document tab uses group labels
    And the user clicks on the documents tab
    Then documents are displayed in the "groupes" section
    When the user deletes the first document in the "groupes" section
    Then a document delete confirmation dialog is displayed
    When the user cancels the document deletion

  # ══════════════════════════════════════════════
  #  SECTION ENTREPRISES
  # ══════════════════════════════════════════════


  @companies @positive
  Scenario: Companies section documents are read-only with eye icon
    When the user edits the company group "AVEZ"
    And the document tab uses group labels
    And the user clicks on the documents tab
    When the user clicks on show companies documents
    Then documents are displayed in the "entreprises" section
    And each document card in the "entreprises" section has an eye icon
    And no delete icon is visible in the "entreprises" section

  @companies @negative
  Scenario: Companies section shows empty state or loads after clicking load button
    When the user edits the company group "QUERCY BAS ROUERGUE"
    And the document tab uses group labels
    And the user clicks on the documents tab
    When the user clicks on show companies documents
    Then no document message is displayed in the "entreprises" section
  # ══════════════════════════════════════════════
  #  SECTION CATÉGORIES
  # ══════════════════════════════════════════════

  @categories @positive
  Scenario: Categories section loads documents after clicking load button
    When the user edits the company group "AVEZ"
    And the document tab uses group labels
    And the user clicks on the documents tab
    When the user clicks on show categories documents
    Then documents are displayed in the "catégories" section

  @categories @categories
  Scenario: Categories section documents are read-only with eye icon
    When the user edits the company group "AVEZ"
    And the document tab uses group labels
    And the user clicks on the documents tab
    When the user clicks on show categories documents
    Then documents are displayed in the "catégories" section
    And each document card in the "catégories" section has an eye icon
    And no delete icon is visible in the "catégories" section

  @categories @negative
  Scenario: Categories section shows empty state or loads after clicking load button
    When the user edits the company group "QUERCY BAS ROUERGUE"
    And the document tab uses group labels
    And the user clicks on the documents tab
    When the user clicks on show categories documents
    Then no document message is displayed in the "catégories" section
