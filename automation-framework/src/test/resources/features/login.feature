@login
Feature: Multi-Step Authentication Process
  This feature describes the login process for the NoveoCare back-office application.

  @TC_Login_001 @positive
  Scenario: Successful login with multi-step authentication
    Given the user navigates to the login page
    When the user clicks "Se connecter avec mon compte NoveoCare"
    And the user enters a valid email
    And the user clicks the continue button
    And the user enters a valid password
    And the user clicks the login button
    And the user selects "Non" on the "Rester connecté ?" page
    Then the user is redirected to the dashboard