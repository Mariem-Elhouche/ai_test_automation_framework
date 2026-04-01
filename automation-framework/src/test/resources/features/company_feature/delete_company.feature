@companies @requiresLogin @delete @regression
Feature: Company deletion

  Background:
    Given the user navigates to the companies list page

#juste apres la correction du bug va fonctionner correctement
  @TC_COMP_DELETE_001 @positive
  Scenario: Delete a company successfully
    When the user deletes the company "Testtest3"
    Then a company deletion confirmation dialog is displayed
    When the user confirms the company deletion
    Then a company deletion success message is displayed
    And the company "Testtest3" no longer appears in the companies list


  @TC_COMP_DELETE_002 @negative
  Scenario: Cancel company deletion keeps the company in the list
    When the user deletes the company "Testtest3"
    Then a company deletion confirmation dialog is displayed
    When the user cancels the company deletion
    Then the company "Testtest3" still appears in the companies list

