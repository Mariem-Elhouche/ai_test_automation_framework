@environment @company @requiresLogin
Feature: Create a company within a newly created environment

  @TC_ENV_COMPANY_CREATE_001 @positive
  Scenario: Create an environment then add a company from the Entreprises tab
    Given the user navigates to the environments list page

    # ── Étape 1 : Créer l'environnement ──
    When the user clicks on create environment button
    And the user fills in the environment name "ENV-TEST31"
    And the user selects the environment site de gestion "Lucé1"
    And the user fills in the environment address "Avenue de la gloire"
    And the user fills in the environment address complement "Batiment B"
    And the user fills in the environment email assures "assuresmail@test.com"
    And the user fills in the environment email rh "rhcontact@test.com"
    And the user fills in the environment telephone assures "0677777714"
    And the user fills in the environment telephone professionnels "0677777724"
    And the user fills in the environment nom front "FrontTest"
    And the user fills in the environment nom expediteur "ExpTest"
    And the user fills in the environment open name "env31"
    And the user fills in the environment open id "8822"
    And the user selects the environment carte tp mode "Dématérialisé"
    And the user selects the environment decompte mode "Dématérialisé"
    And the user selects the environment double auth "Activé"
    And the user selects the environment secondary MFA method "Téléphone portable"
    And the user enters the country limiting MFA by SMS "France"
    And the user fills in the environment bank name "Attijari"
    And the user fills in the environment bank ics "4547"
    And the user fills in the environment rum root "test84"
    And the user uploads the environment logo principal "C:\Users\mariem.elhouche-ext\Downloads\logoprincipal.jpg"
    And the user uploads the environment image entete "C:\Users\mariem.elhouche-ext\Downloads\entete.png"
    And the user uploads the environment image bas "C:\Users\mariem.elhouche-ext\Downloads\pied.png"
    And the user selects the environment charte graphique "AON"

    # ── Étape 2 : Sauvegarder → toast succès → onglets débloqués ──
    And the user saves the environment
    Then an environment creation success message is displayed

    # ── Étape 3 : Ouvrir l'onglet Entreprises ──
    When the user clicks on the entreprises tab

    # ── Étape 4 : Créer une entreprise dans cet environnement ──
    And the user clicks on create company in environment button
    And the user fills in the company name "company_in_env14"
    And the user fills in the company siret "12997678887798"
    And the user fills in the company address "Rue de la Paix, 20"
    And the user fills in the company postal code "75003"
    And the user fills in the company city "Paris"
    And the user fills in the company country "France"
    And the user fills in the company email "test@auto.com"
    And the user fills in the company phone "0677799789"
    And the user fills in the company open id "1560"
    And the user selects the company environment "ENV-TEST31"
    And the user saves the company

    # ── Étape 5 : Vérifier le succès ──
    Then the company is created successfully
    When the user navigates to the companies list page
    And the user filters companies by name "company_in_env14"
    And the user filters companies by environment open name "env31" and selects "ENV-TEST31"
    Then the displayed companies all contain "company_in_env14" in their name
    And the displayed companies all have environment "ENV-TEST31"