@companies @requiresLogin @create #@regression
Feature: Company creation

  Background:
    Given the user navigates to the companies list page

  # ─────────────────────────────────────────────
  # CAS POSITIFS
  # ─────────────────────────────────────────────

  @TC_COMP_CREATE_001 @positive
  Scenario: Create a company with all required fields
    When the user clicks on create company button
    And the user fills in the company name "Test-Company-Auto"
    And the user fills in the company open id "9999999999"
    And the user saves the company
    Then a company creation success message is displayed
    And the company "Test-Company-Auto" appears in the companies list

  @TC_COMP_CREATE_002 @positive
  Scenario: Create a company with all fields filled
    When the user clicks on create company button
    And the user fills in the company name "Test-Company-Full"
    And the user fills in the company siret "12345678900001"
    And the user fills in the company open id "9999999998"
    And the user saves the company
    Then a company creation success message is displayed
    And the company "Test-Company-Full" appears in the companies list

  @TC_COMP_CREATE_003 @positive
  Scenario: Create a company with only the mandatory name field
    When the user clicks on create company button
    And the user fills in the company name "Test-Company-MinFields"
    And the user saves the company
    Then a company creation success message is displayed
    And the company "Test-Company-MinFields" appears in the companies list

  # ─────────────────────────────────────────────
  # VALIDATION — Champs obligatoires
  # ─────────────────────────────────────────────

  @TC_COMP_CREATE_004 @validation @negative
  Scenario: Fail to create a company without any field filled
    When the user clicks on create company button
    And the user saves the company
    Then a company validation error message is displayed

  @TC_COMP_CREATE_005 @validation @negative
  Scenario: Fail to create a company without the company name
    When the user clicks on create company button
    And the user fills in the company siret "12345678900001"
    And the user fills in the company open id "9999999999"
    And the user saves the company
    Then a company validation error message is displayed

  @TC_COMP_CREATE_006 @validation @negative
  Scenario: Fail to create a company with blank spaces only in the name field
    When the user clicks on create company button
    And the user fills in the company name "   "
    And the user fills in the company open id "9999999999"
    And the user saves the company
    Then a company validation error message is displayed

  # ─────────────────────────────────────────────
  # VALIDATION — Format SIRET
  # ─────────────────────────────────────────────

  @TC_COMP_CREATE_007 @validation @negative
  Scenario: Fail to create a company with an invalid SIRET (too short)
    When the user clicks on create company button
    And the user fills in the company name "Test-Siret-Short"
    And the user fills in the company siret "1234"
    And the user saves the company
    Then a company validation error message is displayed

  @TC_COMP_CREATE_008 @validation @negative
  Scenario: Fail to create a company with a non-numeric SIRET
    When the user clicks on create company button
    And the user fills in the company name "Test-Siret-Alpha"
    And the user fills in the company siret "ABCDEFGHIJKLMN"
    And the user saves the company
    Then a company validation error message is displayed
