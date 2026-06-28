@categories @requiresLogin @list @regression
Feature: Company category list - filters and pagination

  Background:
    Given the user navigates to the company categories list page

  @TC_LIST_001 @filter
  Scenario: Valid filters return matching results
    When the user filters by name "Cas-test1-Maryem"
    Then the displayed categories all contain "Cas-test1-Maryem" in their name
    When the user clears all filters
    And the user filters by code "55"
    Then the displayed categories all have code "55"
    When the user clears all filters
    And the user filters by linked company "Test17-Auto-Full"
    Then the displayed categories all have a linked company containing "Test17-Auto-Full"

  @TC_LIST_002 @filter
  Scenario: Invalid filter returns no results
    When the user filters by name "XXXXXXXXXXX_INEXISTANT"
    Then no categories are displayed in the list

  @TC_LIST_003 @pagination
  Scenario: Navigate through pages
    When the user clicks on page 2
    Then the list displays categories from page 2
    When the user clicks on the last page
    Then the list displays the last page of categories
    When the user clicks on page 1
    Then the list displays categories from page 1

  @TC_LIST_004 @pagination
  Scenario: Navigate using next and previous buttons
    When the user clicks on the next page
    Then the list displays categories from page 2
    And the user clicks on the next page
    Then the list displays categories from page 3
    When the user clicks on the previous page
    Then the list displays categories from page 2
