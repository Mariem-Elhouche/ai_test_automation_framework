@environments @requiresLogin @edit
Feature: Environment edition

  Background:
    Given the user navigates to the environments list page


  @TC_ENV_EDIT_001 @positive
  Scenario: Edit all environment fields successfully
    When the user edits the environment "ENV-TEST7"
    And the user fills in the environment name "ENV-TEST3"
    And the user selects the environment site de gestion "Lucé2"
    And the user fills in the environment address "10 rue de venisse"
    And the user fills in the environment address complement "BâtimentA"
    And the user fills in the environment email assures "assures@test.com"
    And the user fills in the environment email rh "rh@test.com"
    And the user fills in the environment telephone assures "0600000014"
    And the user fills in the environment telephone professionnels "0600000024"
    And the user fills in the environment nom front "FronteeeTest"
    And the user fills in the environment nom expediteur "ExpeeTest"
    And the user fills in the environment open name "test37"
    And the user fills in the environment open id "90"
    And the user selects the environment carte tp mode "Dématérialisé"
    And the user selects the environment double auth "Activé"
    And the user selects the environment MFA method "Email"
    And the user selects the environment secondary MFA method "Téléphone portable"
    And the user enters the country limiting MFA by SMS "France"
    And the user fills in the environment bank name "Banque Editée"
    And the user fills in the environment bank ics "1111"
    And the user fills in the environment rum root "test"
    And the user uploads the environment logo principal "C:\Users\mariem.elhouche-ext\Downloads\logo-edit.png"
    And the user uploads the environment image entete "C:\Users\mariem.elhouche-ext\Downloads\entete-edit.png"
    And the user uploads the environment image bas "C:\Users\mariem.elhouche-ext\Downloads\bas-edit.png"
    And the user selects the environment charte graphique "AON"
    And the user saves the environment
    Then an environment edit success message is displayed
    And the environment "ENV-TEST3" appears in the environments list


  @TC_ENV_EDIT_003 @negative @validation
  Scenario Outline: Validation message "Le champ ne peut pas être vide" displayed after clearing "<field>"
    When the user edits the environment "ENV-TEST2"
    And the user clears the environment <action>
    And the user saves the environment
    Then the field validation message "Le champ ne peut pas être vide" is displayed for field "<field>"

    Examples:
      | field                              | action                    |
      | Nom                                | name                      |
      | Adresse                            | address                   |
      | Nom front                          | nom front                 |
      | Nom dans Open                      | open name                 |
      | ID dans Open                       | open id                   |
      | Email assurés                      | email assures             |
      | Email RH                           | email rh                  |
      | Site de gestion                    | site de gestion           |
      | Numéro de téléphone assurés        | telephone assures         |
      | Numéro de téléphone professionnels | telephone professionnels  |
      | Double Authentification            | double auth               |
      | Mode pour la Carte TP              | carte tp mode             |


  @TC_ENV_EDIT_014 @negative @validation
  Scenario: Fail to edit environment with duplicate open name
    When the user edits the environment "ENV-TEST3"
    And the user fills in the environment open name "test"
    And the user saves the environment
    Then an environment duplicate error message is displayed