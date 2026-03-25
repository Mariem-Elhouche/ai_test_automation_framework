@categories @requiresLogin @edit @regression
Feature: Edit company category
  As a back-office user
  I want to edit an existing company category
  So that I can update its information

  Background:
    Given the user navigates to the company categories list page

  @TC_EDIT_001
  Scenario: Edit category name successfully
    When the user edits the category "Cas-Test2-Maryem" and changes its name to "Edited Category Name"
    Then a category edit confirmation message is displayed
    And the category "Edited Category Name" appears in the list with the same code "CTM123"

  @TC_EDIT_002
  Scenario: Edit category code successfully
    When the user edits the category "Cas-Test3-Maryem" and changes its code to "444444"
    Then a category edit confirmation message is displayed
    And the category "Cas-Test3-Maryem" appears in the list with the new code "444444"

  @TC_EDIT_003
  Scenario: Edit category linked company successfully
    When the user edits the category "Cas-Test3-Maryem" and associates the company "Test-Maryem"
    Then a category edit confirmation message is displayed
    And the category "Cas-Test3-Maryem" has the company "Test-Maryem" associated

  @TC_EDIT_004
  Scenario: Attempt to edit category with missing required fields
    When the user edits the category "Cas-Test4-Maryem" and clears the name field
    And the user saves the category
    Then an error message indicating that required fields must be filled is displayed
    And the category is not modified