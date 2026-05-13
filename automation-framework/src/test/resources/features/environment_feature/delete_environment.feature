@environments @requiresLogin @delete
Feature: Environment deletion

  Background:
    Given the user navigates to the environments list page

    # ce teste supposé etre correct juste il y'a un bug dans la suppression
  @TC_ENV_DELETE_001 @positive
  Scenario: Delete an environment successfully
    When the user deletes the environment "ENV-TEST5"
    Then an environment deletion confirmation dialog is displayed
    When the user confirms the environment deletion
    Then an environment deletion success message is displayed
    And the environment "ENV-TEST5" no longer appears in the environments list

  @TC_ENV_DELETE_002 @negative
  Scenario: Cancel environment deletion keeps the environment in the list
    When the user deletes the environment "ENV-TEST4"
    Then an environment deletion confirmation dialog is displayed
    When the user cancels the environment deletion
    Then the environment "ENV-TEST4" still appears in the environments list