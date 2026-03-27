@categories @requiresLogin @list @regression
Feature: Company category list - filters and pagination

  Background:
    Given the user navigates to the company categories list page

  @TC_LIST_001 @filter
  Scenario: Valid filters return matching results
    When the user filters by name "Cas-test3-Maryem"
    Then the displayed categories all contain "Cas-test3-Maryem" in their name
    When the user clears all filters
    And the user filters by code "CTM123"
    Then the displayed categories all have code "CTM123"
    When the user clears all filters
    And the user filters by linked company "Test-Maryem"
    Then the displayed categories all have a linked company containing "Test-Maryem"

  @TC_LIST_002 @filter
  Scenario: Invalid filter returns no results
    When the user filters by name "XXXXXXXXXXX_INEXISTANT"
    Then no categories are displayed in the list

  @TC_LIST_003 @pagination
  Scenario: Navigate through pages
    When the user clicks on page 4
    Then the list displays categories from page 4
    When the user clicks on the last page
    Then the list displays the last page of categories