@environments @requiresLogin @list @regression
Feature: Environment list — filters and pagination

  Background:
    Given the user navigates to the environments list page

  # ─── FILTRE PAR NOM ───

  @TC_ENV_LIST_001 @filter @positive
  Scenario: Filter environments by name
    When the user filters environments by name "GFPLAC"
    Then the displayed environments all contain "GFPLAC" in their name

  @TC_ENV_LIST_002 @filter @negative
  Scenario: No results when filtering with non-existing name
    When the user filters environments by name "XXXXXXX_INEXISTANT"
    Then no environments are displayed in the list

  # ─── FILTRE PAR SITE DE GESTION ───

  @TC_ENV_LIST_003 @filter @positive
  Scenario: Filter environments by site de gestion
    When the user filters environments by site de gestion "Annecy"
    Then the displayed environments all have site de gestion "Annecy"

  @TC_ENV_LIST_004 @filter @negative
  Scenario: No results when filtering with incompatible name and site de gestion
    When the user filters environments by name "GFPLAC"
    And the user filters environments by site de gestion "Annecy"
    Then no environments are displayed in the list

  # ─── PAGINATION ───

  @TC_ENV_LIST_005 @pagination @positive
  Scenario: Navigate to page 2
    When the user clicks on environments page 2
    Then the environments list displays page 2

  @TC_ENV_LIST_006 @pagination @positive
  Scenario: Navigate to last page
    When the user clicks on the last environments page
    Then the environments list displays the last page

  @TC_ENV_LIST_007 @pagination @positive
  Scenario: Navigate using next button
    When the user clicks the next page button
    Then the environments list displays page 2

  @TC_ENV_LIST_008 @pagination @positive
  Scenario: Navigate using previous button
    When the user clicks on environments page 2
    And the user clicks the previous page button
    Then the environments list displays page 1