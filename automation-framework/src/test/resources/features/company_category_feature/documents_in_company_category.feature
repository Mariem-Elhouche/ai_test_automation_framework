@categories @documents @requiresLogin
Feature: Documents tab in company categories
  Background:
    Given the user navigates to the company categories page
    And the user starts creating a new company category
    And the user enters the category information with name "DOC-TEST" and code "CDT"
    And the user searches for an existing company by "nom entreprise" with value "Test_maryem"
    And the user selects a company from the search results with value "Test_maryem"
    And the user saves the new category
    And the user clicks on the documents tab

  # ══════════════════════════════════════════════
  #  NAVIGATION & CHARGEMENT
  # ══════════════════════════════════════════════

  @smoke
  Scenario: Documents tab loads and displays environment section by default
    Then the documents tab is active
    And the section title "Documents rattachés à l'espace assuré" is displayed
    And documents are displayed in the "environnement" section
    When the user navigates to the company categories list page
    And the user deletes the category "DOC-TEST"
    Then a category deletion confirmation dialog is displayed
    When the user confirms the deletion
    Then a category deletion success message is displayed
    And the category "DOC-TEST" no longer appears in the list
  # ══════════════════════════════════════════════
  #  SECTION ENVIRONNEMENT
  # ══════════════════════════════════════════════

  @smoke
  Scenario: Environment section documents are read-only with eye icon
    Then documents are displayed in the "environnement" section
    And each document card in the "environnement" section has an eye icon
    And no delete icon is visible in the "environnement" section
    When the user navigates to the company categories list page
    And the user deletes the category "DOC-TEST"
    Then a category deletion confirmation dialog is displayed
    When the user confirms the deletion
    Then a category deletion success message is displayed
    And the category "DOC-TEST" no longer appears in the list

  @smoke @navigation
  Scenario: Clicking eye icon on environment document navigates to environment page
    Then documents are displayed in the "environnement" section
    When the user clicks the eye icon on the first document in the "environnement" section
    Then the user is redirected to an environment detail page
    When the user navigates to the company categories list page
    And the user deletes the category "DOC-TEST"
    Then a category deletion confirmation dialog is displayed
    When the user confirms the deletion
    Then a category deletion success message is displayed
    And the category "DOC-TEST" no longer appears in the list

  # ══════════════════════════════════════════════
  #  SECTION GROUPES
  # ══════════════════════════════════════════════

  @groups
  Scenario: Groups section shows empty state on newly created category
    When the user clicks on show groups documents
    Then no document message is displayed in the "groupes" section
    When the user navigates to the company categories list page
    And the user deletes the category "DOC-TEST"
    Then a category deletion confirmation dialog is displayed
    When the user confirms the deletion
    Then a category deletion success message is displayed
    And the category "DOC-TEST" no longer appears in the list

  # ══════════════════════════════════════════════
  #  SECTION ENTREPRISES — read-only (hérité)
  # ══════════════════════════════════════════════

  @companies
  Scenario: Companies section shows empty state on newly created category
    When the user clicks on show companies documents
    Then no document message is displayed in the "entreprises" section
    When the user navigates to the company categories list page
    And the user deletes the category "DOC-TEST"
    Then a category deletion confirmation dialog is displayed
    When the user confirms the deletion
    Then a category deletion success message is displayed
    And the category "DOC-TEST" no longer appears in the list

  # ══════════════════════════════════════════════
  #  SECTION CATÉGORIES
  # ══════════════════════════════════════════════

  @smoke
  Scenario: Categories section shows empty state on newly created category
    When the user clicks on show categories documents
    Then no document message is displayed in the "catégories" section
    When the user navigates to the company categories list page
    And the user deletes the category "DOC-TEST"
    Then a category deletion confirmation dialog is displayed
    When the user confirms the deletion
    Then a category deletion success message is displayed
    And the category "DOC-TEST" no longer appears in the list