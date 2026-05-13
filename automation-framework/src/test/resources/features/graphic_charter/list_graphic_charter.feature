@graphicCharter @requiresLogin @list @regression
Feature: Graphic charter list — filters and pagination

  Background:
    Given the user navigates to the graphic charters list page

  # ─── FILTER BY NAME ───────────────────────────────────────────────────────

  @TC_GC_LIST_001 @filter @positive
  Scenario: Filter graphic charters by existing name
    When the user filters graphic charters by name "Noveocare"
    Then the displayed graphic charters all contain "Noveocare" in their name

  @TC_GC_LIST_002 @filter @negative
  Scenario: No results when filtering with a non-existing name
    When the user filters graphic charters by name "XXXXXXXXX_INEXISTANT"
    Then no graphic charters are displayed in the list

  @TC_GC_LIST_004 @filter @positive
  Scenario: Filter graphic charters by partial name — lower case input
    When the user filters graphic charters by name "howden"
    Then the displayed graphic charters all contain "HOWDEN" in their name

  # ─── PAGINATION ───────────────────────────────────────────────────────────

  @TC_GC_LIST_006 @pagination @positive
  Scenario: Navigate to last page
    When the user clicks on the last graphic charters page
    Then the graphic charters list displays the last page

  @TC_GC_LIST_007 @pagination @positive
  Scenario: Navigate back to page 1 after going to page 2
    When the user clicks on graphic charters page 2
    And the user clicks on graphic charters page 1
    Then the graphic charters list displays page 1

  @TC_GC_LIST_009 @pagination @positive
  Scenario: Navigate to next page then  previous page using previous and next buttons
    When the user clicks on the next page button
    Then the graphic charters list displays page 2
    And the user clicks on the previous page button
    Then the graphic charters list displays page 1