@company_groups @requiresLogin @delete @regression
Feature: Company group deletion

  Background:
    Given the user navigates to the company groups list page

  Scenario: Display deletion confirmation dialog
    When the user deletes the company group "Grptst"
    Then a company group deletion confirmation dialog is displayed

    #ce test echoue car il y'a un bug dans l'application :
    # le groupe ne se supprime pas de la liste apres le delete
  Scenario: Delete a company group successfully
    When the user deletes the company group "GROUPE MH"
    Then a company group deletion confirmation dialog is displayed
    When the user confirms the company group deletion
    Then a company group deletion success message is displayed
    And the company group "GROUPE DELETE" no longer appears in the list


  Scenario: Cancel company group deletion
    When the user deletes the company group "GROUPE MH"
    Then a company group deletion confirmation dialog is displayed
    When the user cancels the company group deletion
    Then the company group "GROUPE MH" still appears in the list

