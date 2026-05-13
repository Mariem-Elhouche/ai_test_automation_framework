@company_groups @requiresLogin  @regression @list
Feature: Company groups pagination , filtering

  Background:
    Given the user navigates to the company groups list page

  @filters
  Scenario: Filter company groups by name
    When the user filters company groups by name "TEST"
    Then the displayed company groups all contain "TEST" in their name

  @filters
  Scenario: Filter company groups by type
    When the user filters company groups by type "GROUP"
    Then the displayed company groups all have type containing "GROUP"

  @filters
  Scenario: Filter company groups by environment
    When the user filters company groups by environment "AG2R"
    Then the company groups list is not empty

  @pagination
  Scenario: Navigate pagination in company groups list
    When the user clicks on company groups page 2
    Then the company groups list displays page 2
    When the user clicks on the last company groups page
    Then the company groups list displays the last page

  @pagination @previous
  Scenario: Navigate with next and previous page buttons
    Given the user navigates to the company groups list page
    Then the company groups previous page button is disabled
    When the user clicks on company groups next page button
    Then the company groups list displays page 2
    When the user clicks on company groups previous page button
    Then the company groups list displays page 1
    Then the company groups previous page button is disabled

  @pagination @next
  Scenario: Next page button is disabled on last page
    Given the user navigates to the company groups list page
    When the user clicks on the last company groups page
    Then the company groups next page button is disabled