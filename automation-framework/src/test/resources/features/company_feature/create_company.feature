@companies @requiresLogin @create @regression
Feature: Company creation

  Background:
    Given the user navigates to the companies list page

  # ─── CAS POSITIFS ───

  @TC_COMP_CREATE_001 @positive
  Scenario: Create a company with required fields only
    When the user clicks on create company button
    And the user fills in the company name "Test-Auto-818"
    And the user fills in the company address "10 rue de la Paix"
    And the user fills in the company open id "18181818"
    And the user selects the company environment "MHSANTE"
    And the user saves the company
    Then a company creation success message is displayed
    And the company "Test-Auto-818" appears in the companies list

  @TC_COMP_CREATE_002 @positive
  Scenario: Create a company with all fields filled
    When the user clicks on create company button
    And the user fills in the company name "Test89-Auto-Full"
    And the user fills in the company siret "12345678908817"
    And the user fills in the company address "5 avenue Victor Hugo"
    And the user fills in the company address complement "Bâtiment 10"
    And the user fills in the company postal code "69017"
    And the user fills in the company city "Lyon"
    And the user fills in the company country "France"
    And the user fills in the company email "contact@test.fr"
    And the user fills in the company phone "0612345885"
    And the user fills in the company open id "tst174"
    And the user selects the company environment "MHSANTE"
    And the user saves the company
    Then a company creation success message is displayed
    And the company "Test89-Auto-Full" appears in the companies list

  # ─── CHAMPS OBLIGATOIRES MANQUANTS ───

  @TC_COMP_CREATE_003 @negative @validation
  Scenario Outline: Fail to create a company when a required field is missing
    When the user clicks on create company button
    And the user fills in the company name "<name>"
    And the user fills in the company address "<address>"
    And the user fills in the company open id "<openId>"
    And the user saves the company
    Then a company validation error message is displayed

    Examples:
      | name         | address            | openId     | missing          |
      |              | 10 rue de la Paix  | 3333333333 | nom              |
      | Test-Missing |                    | 3333333334 | adresse          |
      | Test-Missing | 10 rue de la Paix  |            | open id          |

  @TC_COMP_CREATE_004 @negative @validation
  Scenario: Fail to create a company without environment
    When the user clicks on create company button
    And the user fills in the company name "Test-No-Env"
    And the user fills in the company address "10 rue de la Paix"
    And the user fills in the company open id "4444444444"
    And the user saves the company
    Then a company validation error message is displayed

  # ─── FORMAT SIRET ───

  @TC_COMP_CREATE_005 @negative @validation
  Scenario Outline: Fail to create a company with invalid SIRET format
    When the user clicks on create company button
    And the user fills in the company name "Test-Siret"
    And the user fills in the company address "10 rue de la Paix"
    And the user fills in the company siret "<siret>"
    And the user fills in the company open id "5555555555"
    And the user selects the company environment "GFPLAC"
    And the user saves the company
    Then a company validation error message is displayed

    Examples:
      | siret          | reason        |
      | 1234           | trop court    |
      | ABCDEFGHIJKLMN | non numérique |