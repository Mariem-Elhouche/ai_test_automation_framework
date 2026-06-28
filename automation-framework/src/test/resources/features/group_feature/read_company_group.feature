@company_groups @requiresLogin @read @regression
Feature: Company group information reading

  Background:
    Given the user navigates to the company groups list page

  @smoke
  Scenario: Open company group details from eye icon
    When the user filters company groups by name "BERGERAT"
    And the user clicks the eye icon on the first company group row
    Then the company group details page is displayed