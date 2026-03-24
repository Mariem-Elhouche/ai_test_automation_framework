@categories @requiresLogin @list @regression
Feature: Company category list - filters and pagination
  Allow back-office users to filter and paginate the company categories list.

  Background:
    Given the user navigates to the company categories list page

  @TC_LIST_001 @filter
  Scenario: Filter categories by name
    When the user filters by name "Cas-test3-Maryem"
    Then the displayed categories all contain "Cas-test3-Maryem" in their name

  @TC_LIST_002 @filter
  Scenario: Filter categories by code
    When the user filters by code "CTM123"
    Then the displayed categories all have code "CTM123"

  @TC_LIST_003 @filter
  Scenario: Filter categories by linked company
    When the user filters by linked company "EANM LIMOUX LES HIRONDELLES"
    Then the displayed categories all have a linked company containing "EANM LIMOUX LES HIRONDELLES"

  @TC_LIST_004 @filter
  Scenario: No results when filtering with non-existing criteria
    When the user filters by name "XXXXXXXXXXX_INEXISTANT"
    Then no categories are displayed in the list

  @TC_LIST_005 @pagination
  Scenario: Navigate to next page
    When the user clicks on page 2
    Then the list displays categories from page 2

  @TC_LIST_006 @pagination
  Scenario: Navigate to last page
    When the user clicks on the last page
    Then the list displays the last page of categories