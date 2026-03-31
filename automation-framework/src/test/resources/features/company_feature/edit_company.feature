@companies @requiresLogin @edit #@regression
Feature: Company edition

  Background:
    Given the user navigates to the companies list page

  # ─────────────────────────────────────────────
  # CAS POSITIFS — Modification du nom
  # ─────────────────────────────────────────────

  @TC_COMP_EDIT_001 @positive
  Scenario: Edit company name successfully
    When the user edits the company "Test-Company-Auto" and changes its name to "Test-Company-Edited"
    Then a company edit success message is displayed
    And the company "Test-Company-Edited" appears in the companies list

  @TC_COMP_EDIT_002 @positive
  Scenario: Edit company name with special characters
    When the user edits the company "Test-Company-Full" and changes its name to "Test-Company-Spécial & Co"
    Then a company edit success message is displayed
    And the company "Test-Company-Spécial & Co" appears in the companies list

  @TC_COMP_EDIT_003 @positive
  Scenario: Edit company name back to its original value
    When the user edits the company "Test-Company-Edited" and changes its name to "Test-Company-Auto"
    Then a company edit success message is displayed
    And the company "Test-Company-Auto" appears in the companies list

  # ─────────────────────────────────────────────
  # CAS POSITIFS — Modification du SIRET
  # ─────────────────────────────────────────────

  @TC_COMP_EDIT_004 @positive
  Scenario: Edit company SIRET successfully
    When the user edits the company "Test-Company-Auto" and changes its siret to "98765432100001"
    Then a company edit success message is displayed

  @TC_COMP_EDIT_005 @positive
  Scenario: Clear company SIRET (optional field)
    When the user edits the company "Test-Company-Auto" and clears its siret
    Then a company edit success message is displayed

  # ─────────────────────────────────────────────
  # CAS POSITIFS — Modification de l'Open ID
  # ─────────────────────────────────────────────

  @TC_COMP_EDIT_006 @positive
  Scenario: Edit company Open ID successfully
    When the user edits the company "Test-Company-Auto" and changes its open id to "8888888888"
    Then a company edit success message is displayed

  # ─────────────────────────────────────────────
  # VALIDATION — Champs obligatoires
  # ─────────────────────────────────────────────

  @TC_COMP_EDIT_007 @validation @negative
  Scenario: Fail to edit a company by clearing the required name field
    When the user clicks on create company button
    And the user fills in the company name "Test-Company-EditValidation"
    And the user fills in the company open id "7777777777"
    And the user saves the company
    And the user navigates to the companies list page
    And the user edits the company "Test-Company-EditValidation" and changes its name to "   "
    Then a company validation error message is displayed

  @TC_COMP_EDIT_008 @validation @negative
  Scenario: Fail to edit a company with an invalid SIRET format
    When the user edits the company "Test-Company-Auto" and changes its siret to "INVALID"
    Then a company validation error message is displayed
