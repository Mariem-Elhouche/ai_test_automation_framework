@company_groups @requiresLogin @editCompany @regression
Feature: Company group edition

  Background:
    Given the user navigates to the company groups list page


    Scenario:edit name of a company group already created
      When the user edits company group "grp-ent" and changes its name to "GROUPE MH"
      Then a company group edit success message is displayed
      Then the updated company group with name "GROUPE MH" appears in the list


  Scenario: Edit company group and clear name shows validation error
    When the user edits company group "GROUPE MH" and changes its name to ""
    Then a company group name validation error is displayed