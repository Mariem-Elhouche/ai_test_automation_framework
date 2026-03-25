@categories @requiresLogin @delete @regression
Feature: Delete company category

  Background:
    Given the user navigates to the company categories list page

  @TC_DELETE_001 @positive
  Scenario: Delete a category successfully
    When the user deletes the category "Cas-Test5-Maryem"
    Then a category deletion confirmation dialog is displayed
    When the user confirms the deletion
    Then a category deletion success message is displayed
    And the category "Cas-Test5-Maryem" no longer appears in the list

  @TC_DELETE_002 @negative
  Scenario: Cancel category deletion
    When the user deletes the category "Cas-Test3-Maryem"
    Then a category deletion confirmation dialog is displayed
    When the user cancels the deletion
    Then the category "Cas-Test3-Maryem" still appears in the list
