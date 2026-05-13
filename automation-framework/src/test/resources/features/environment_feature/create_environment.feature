@environments @requiresLogin @create
Feature: Environment creation

  Background:
    Given the user navigates to the environments list page

  @TC_ENV_CREATE_001 @positive
  Scenario: Create an environment with all required fields
    When the user clicks on create environment button
    And the user fills in the environment name "Test-Env-Auto-001"
    And the user selects the environment site de gestion "Annecy"
    And the user fills in the environment open name "TestOpenName001"
    And the user fills in the environment open id "ENV_001"
    And the user selects the environment carte tp mode "Standard"
    And the user selects the environment double auth "Non"
    And the user selects the environment charte graphique "Charte1"
    And the user saves the environment
    Then an environment creation success message is displayed
    And the environment "Test-Env-Auto-001" appears in the environments list

  @TC_ENV_CREATE_002 @positive
  Scenario: Create an environment with all fields filled
    When the user clicks on create environment button
    And the user fills in the environment name "ENV-TEST8"
    And the user selects the environment site de gestion "Lucé2"
    And the user fills in the environment address "Avenue de la gloire"
    And the user fills in the environment address complement "Batiment B"
    And the user fills in the environment email assures "assuresmail@test.com"
    And the user fills in the environment email rh "rhcontact@test.com"
    And the user fills in the environment telephone assures "0677777714"
    And the user fills in the environment telephone professionnels "0677777724"
    And the user fills in the environment nom front "FronteeeTest"
    And the user fills in the environment nom expediteur "ExpeeTest"
    And the user fills in the environment open name "test88"
    And the user fills in the environment open id "15"
    And the user selects the environment carte tp mode "Dématérialisé"
    And the user selects the environment decompte mode "Dématérialisé"
    And the user selects the environment double auth "Activé"
    And the user selects the environment secondary MFA method "Téléphone portable"
    And the user enters the country limiting MFA by SMS "France"
    And the user fills in the environment bank name "AG2R LA MONDIALE / TEST"
    And the user fills in the environment bank ics "FR82ZZZ387504"
    And the user fills in the environment rum root "AG2R1"
    And the user uploads the environment logo principal "C:\Users\mariem.elhouche-ext\Downloads\logoprincipal.jpg"
    And the user uploads the environment image entete "C:\Users\mariem.elhouche-ext\Downloads\entete.png"
    And the user uploads the environment image bas "C:\Users\mariem.elhouche-ext\Downloads\pied.png"
    And the user selects the environment charte graphique "Noveocare"
    And the user saves the environment
    Then an environment creation success message is displayed
    And the environment "ENV-TEST8" appears in the environments list
#ajouter une etape de suppression de l'environnement crée pour ne pas dupliquer les "open names" : on ne peut pas car la suppression ne s'effectue pas (bug )

  @TC_ENV_CREATE_003 @negative @validation
  Scenario: Fail to create an environment with blank spaces in required fields
    When the user clicks on create environment button
    And the user fills in the environment name "   "
    #And the user selects the environment site de gestion "lucé1"
    And the user fills in the environment address "   "
    And the user fills in the environment email assures "   "
    And the user fills in the environment email rh "          "
    And the user fills in the environment telephone assures "    "
    And the user fills in the environment telephone professionnels "  "
    And the user fills in the environment nom front "   "
    And the user fills in the environment open name "   "
    And the user fills in the environment open id "   "
    And the user selects the environment carte tp mode "Dématérialisé"
    And the user selects the environment decompte mode "Dématérialisé"
    And the user selects the environment double auth "Désactivé"
    And the user fills in the environment bank name "  "
    And the user fills in the environment bank ics "   "
    And the user fills in the environment rum root "   "
    And the user uploads the environment logo principal "  "
    And the user uploads the environment image entete "  "
    And the user uploads the environment image bas "  "
    #And the user selects the environment charte graphique "  "
    And the user saves the environment
    Then an environment validation error message is displayed

  @TC_ENV_CREATE_004 @negative @validation
  Scenario: Fail to create an environment with blank spaces in required fields
    When the user clicks on create environment button
    And the user fills in the environment name "   "
    And the user selects the environment site de gestion "Lucé2"
    And the user fills in the environment address "   "
    And the user fills in the environment address complement "   "
    And the user fills in the environment email assures "assuresmail@test.com"
    And the user fills in the environment email rh "rhcontact@test.com"
    And the user fills in the environment telephone assures "0677777714"
    And the user fills in the environment telephone professionnels "0677777724"
    And the user fills in the environment nom front "FronteeeTest"
    And the user fills in the environment nom expediteur "ExpeeTest"
    And the user fills in the environment open name "test89"
    And the user fills in the environment open id "15"
    And the user selects the environment carte tp mode "Dématérialisé"
    And the user selects the environment decompte mode "Dématérialisé"
    And the user selects the environment double auth "Activé"
    And the user selects the environment secondary MFA method "Téléphone portable"
    And the user enters the country limiting MFA by SMS "France"
    And the user fills in the environment bank name "AG2R LA MONDIALE / TEST"
    And the user fills in the environment bank ics "FR82ZZZ387504"
    And the user fills in the environment rum root "AG2R1"
    And the user uploads the environment logo principal "C:\Users\mariem.elhouche-ext\Downloads\logoprincipal.jpg"
    And the user uploads the environment image entete "C:\Users\mariem.elhouche-ext\Downloads\entete.png"
    And the user uploads the environment image bas "C:\Users\mariem.elhouche-ext\Downloads\pied.png"
    And the user selects the environment charte graphique "Noveocare"
    And the user saves the environment
    Then an environment validation error message is displayed

  @TC_ENV_CREATE_005 @negative @validation
  Scenario: Fail to create an environment with an already existing open name
    When the user clicks on create environment button
    And the user fills in the environment name "Test-Duplication"
    And the user selects the environment site de gestion "Annecy"
    And the user fills in the environment address "Avenue de la gloire"
    And the user fills in the environment email assures "assuresmail@test.com"
    And the user fills in the environment email rh "rhcontact@test.com"
    And the user fills in the environment telephone assures "0677777714"
    And the user fills in the environment telephone professionnels "0677777724"
    And the user fills in the environment nom front "FronteeeTest"
    And the user fills in the environment open name "test"
    And the user fills in the environment open id "15"
    And the user selects the environment carte tp mode "Dématérialisé"
    And the user selects the environment decompte mode "Dématérialisé"
    And the user selects the environment double auth "Désactivé"
    And the user enters the country limiting MFA by SMS "France"
    And the user fills in the environment bank name "Banque central"
    And the user fills in the environment bank ics "MHZ387504"
    And the user fills in the environment rum root "AG2R1"
    And the user uploads the environment logo principal "C:\Users\mariem.elhouche-ext\Downloads\logoprincipal.jpg"
    And the user uploads the environment image entete "C:\Users\mariem.elhouche-ext\Downloads\entete.png"
    And the user uploads the environment image bas "C:\Users\mariem.elhouche-ext\Downloads\pied.png"
    And the user selects the environment charte graphique "Noveocare"
    And the user saves the environment
    Then an environment duplicate error message is displayed