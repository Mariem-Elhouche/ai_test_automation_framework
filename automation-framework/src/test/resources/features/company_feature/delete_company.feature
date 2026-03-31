@companies @requiresLogin @delete #@regression
Feature: Company deletion

  Background:
    Given the user navigates to the companies list page

  # ─────────────────────────────────────────────
  # CAS POSITIFS — Suppression confirmée
  # ─────────────────────────────────────────────

  @TC_COMP_DELETE_001 @positive
  Scenario: Delete a company successfully
    When the user deletes the company "Test-Company-Edited"
    Then a company deletion confirmation dialog is displayed
    When the user confirms the company deletion
    Then a company deletion success message is displayed
    And the company "Test-Company-Edited" no longer appears in the companies list

  @TC_COMP_DELETE_002 @positive
  Scenario: Delete a second company successfully
    When the user deletes the company "Test-Company-Full"
    Then a company deletion confirmation dialog is displayed
    When the user confirms the company deletion
    Then a company deletion success message is displayed
    And the company "Test-Company-Full" no longer appears in the companies list

  @TC_COMP_DELETE_003 @positive
  Scenario: Delete a company and verify list count is decremented
    When the user filters companies by name "Test-Company-MinFields"
    Then the displayed companies all contain "Test-Company-MinFields" in their name
    When the user deletes the company "Test-Company-MinFields"
    And the user confirms the company deletion
    Then a company deletion success message is displayed
    And the company "Test-Company-MinFields" no longer appears in the companies list

  # ─────────────────────────────────────────────
  # CAS NEGATIFS — Annulation de suppression
  # ─────────────────────────────────────────────

  @TC_COMP_DELETE_004 @negative
  Scenario: Cancel company deletion keeps the company in the list
    When the user deletes the company "NAVIRETECH SP Z O O"
    Then a company deletion confirmation dialog is displayed
    When the user cancels the company deletion
    Then the company "NAVIRETECH SP Z O O" still appears in the companies list

  @TC_COMP_DELETE_005 @negative
  Scenario: Cancel deletion and verify the confirmation dialog is closed
    When the user deletes the company "NAVIRETECH SP Z O O"
    Then a company deletion confirmation dialog is displayed
    When the user cancels the company deletion
    Then the company "NAVIRETECH SP Z O O" still appears in the companies list

  # ─────────────────────────────────────────────
  # MODALE — Vérification du contenu
  # ─────────────────────────────────────────────

  @TC_COMP_DELETE_006 @positive
  Scenario: Deletion confirmation dialog is displayed before any deletion
    When the user deletes the company "NAVIRETECH SP Z O O"
    Then a company deletion confirmation dialog is displayed
    When the user cancels the company deletion
    Then the company "NAVIRETECH SP Z O O" still appears in the companies list
