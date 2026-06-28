@company_groups @requiresLogin @create @regression
Feature: Company group creation

  Background:
    Given the user navigates to the company groups list page

  @create @positive
  Scenario: Create a company group successfully
    When the user clicks on create company group button
    And the user fills in the company group name "Grptst"
    And the user fills in the company group open id "222"
    And the user selects company group type "Courtier"
    And the user saves the company group
    Then a company group success message is displayed
    Then the company group "Grptst" appears in the list

  @create @negative
  Scenario: Create a company group without name shows validation error
    When the user clicks on create company group button
    And the user fills in the company group name ""
    And the user fills in the company group open id "222"
    And the user selects company group type "Autres"
    Then a company group name validation error is displayed