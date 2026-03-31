@companies @requiresLogin @list @regression
Feature: Company list — filters and pagination

  Background:
    Given the user navigates to the companies list page

  # ═══════════════════════════════════════════════════════════
  # FILTRE — Nom
  # ═══════════════════════════════════════════════════════════

  @TC_COMP_LIST_001 @filter @positive
  Scenario: Filter companies by name
    When the user filters companies by name "ECOLE LANGEVIN"
    Then the displayed companies all contain "ECOLE LANGEVIN" in their name

  @TC_COMP_LIST_002 @filter @negative
  Scenario: No results when filtering with non-existing name
    When the user filters companies by name "XXXXXXXXX_INEXISTANT"
    Then no companies are displayed in the list

  # FILTRE par Open ID

  @TC_COMP_LIST_003 @filter @positive
  Scenario: Filter companies by Open ID
    When the user filters companies by open id "1000121622"
    Then the displayed companies all have open id "1000121622"

  @TC_COMP_LIST_004 @filter @negative
  Scenario: No results when filtering with non-existing Open ID
    When the user filters companies by open id "0000000000"
    Then no companies are displayed in the list

  # FILTRE par Environnement (dropdown multi-select + Valider)

  @TC_COMP_LIST_005 @filter @positive
  Scenario: Filter companies by environment
    When the user filters companies by environment "GFPLAC"
    Then the displayed companies all have environment "GFPLAC"

  @TC_COMP_LIST_006 @filter @negative
  Scenario: No environment option found when filtering with non-existing environment
    When the user filters companies by environment "ENV_INEXISTANT"
    Then no environment option is found in the dropdown

  # ═══════════════════════════════════════════════════════════
  # FILTRE — Combinaison
  # ═══════════════════════════════════════════════════════════

  @TC_COMP_LIST_007 @filter @positive
  Scenario: Filter companies by name and environment combined
    When the user filters companies by environment "GFPLAC"
    And the user filters companies by name "MOMENTUM"
    Then the displayed companies all contain "MOMENTUM" in their name
    And the displayed companies all have environment "GFPLAC"

  @TC_COMP_LIST_008 @filter @negative
  Scenario: No results when name and environment filters are incompatible
    When the user filters companies by name "ECOLE LANGEVIN"
    And the user filters companies by environment "AG2R"
    Then no companies are displayed in the list

  # ═══════════════════════════════════════════════════════════
  # PAGINATION
  # ═══════════════════════════════════════════════════════════

  @TC_COMP_LIST_009 @pagination @positive
  Scenario: Navigate to page 2
    When the user clicks on companies page 2
    Then the companies list displays page 2

  @TC_COMP_LIST_010 @pagination @positive
  Scenario: Navigate to last page
    When the user clicks on the last companies page
    Then the companies list displays the last page
