@environment @company @requiresLogin
Feature: Manage companies within an environment


  @TC_ENV_COMPANY_CREATE_001 @positive
  Scenario: Create a company from the environment Entreprises tab
    Given the user navigates to the environments list page
    When the user edits the environment "ENV-TEST27"
    And the user clicks on the entreprises tab
    And the user clicks on create company in environment button
    And the user fills in the company name "CompanyAttached12"
    And the user fills in the company siret "12345678998765"
    And the user fills in the company address "Rue de la Paix, 10"
    And the user fills in the company city "Paris"
    And the user fills in the company country "France"
    And the user fills in the company email "test@auto.com"
    And the user fills in the company phone "0677777777"
    And the user fills in the company open id "1587"
    And the user selects the company environment "ENV-TEST27"
    And the user saves the company
    Then the company is created successfully
    When the user navigates to the companies list page
    And the user filters companies by name "CompanyAttached12"
    And the user filters companies by environment open name "env27" and selects "ENV-TEST27"
    Then the displayed companies all contain "CompanyAttached12" in their name
    And the displayed companies all have environment "ENV-TEST27"
#  @TC_ENV_COMPANY_CREATE_001 @positive
#  Scenario: Create a company from the environment Entreprises tab
#    Given the user navigates to the environments list page
#    When the user edits the environment "ENV-TEST27"
#    And the user clicks on the entreprises tab
#    And the user clicks on create company in environment button
#    And the user fills in the company name "CompanyAttached11"
#    And the user fills in the company siret "12345678998765"
#    And the user fills in the company address "Rue de la Paix, 10"
#    #And the user fills in the company postal code "75300"
#    And the user fills in the company city "Paris"
#    And the user fills in the company country "France"
#    And the user fills in the company email "test@auto.com"
#    And the user fills in the company phone "0677777777"
#    And the user fills in the company open id "1586"
#    And the user selects the company environment "ENV-TEST27"
#    And the user saves the company
#    Then the company is created successfully
#    When the user navigates to the companies list page
#    And the user filters companies by name "CompanyAttached11"
#    And the user filters companies by environment "env27"
#    Then the displayed companies all contain "CompanyAttached11" in their name
#    And the displayed companies all have environment "ENV-TEST29"

  @TC_ENV_COMPANY_FILTER_001 @positive
  Scenario: Filter companies in environment tab by name
    Given the user navigates to the environments list page
    And the user edits the environment "ENV-TEST1"
    When the user clicks on the entreprises tab
    And the user filters environment companies by name "CompanyAttached1"
    Then the displayed companies all contain "CompanyAttached1" in their name


  @TC_ENV_COMPANY_FILTER_002 @positive
  Scenario: Filter companies in environment tab by name
    Given the user navigates to the environments list page
    And the user edits the environment "ENV-TEST1"
    When the user clicks on the entreprises tab
    And the user filters environment companies by open id "1582"
    Then the displayed companies all have open id "1582"


  @TC_ENV_COMPANY_EDIT_001 @positive
  Scenario: Edit a company from the environment Entreprises tab
    Given the user navigates to the environments list page
    And the user edits the environment "ENV-TEST1"
    When the user clicks on the entreprises tab
    And the user filters environment companies by name "CompanyAttached3"
    And the user edits the first company in environment
    And the user fills in the company name "CompanyAttached3-Edited"
    And the user fills in the company siret "99999999999999"
    And the user fills in the company address "12 rue de la paix"
    And the user fills in the company city "Lyon"
    And the user fills in the company country "France"
    And the user fills in the company email "edited@auto.com"
    And the user fills in the company phone "0611111111"
    And the user fills in the company open id "9999"
    And the user saves the company
    Then the company edit is successful

  @TC_ENV_COMPANY_EDIT_002 @negative @validation
  Scenario: Fail to edit a company by clearing the required name field
    Given the user navigates to the environments list page
    And the user edits the environment "ENV-TEST1"
    When the user clicks on the entreprises tab
    And the user filters environment companies by name "CompanyAttached3"
    And the user edits the first company in environment
    And the user clears the company name
    And the user saves the company
    Then a company validation error message is displayed

  @TC_ENV_COMPANY_EDIT_003 @negative @validation
  Scenario: Fail to edit a company with an invalid SIRET format
    Given the user navigates to the environments list page
    And the user edits the environment "ENV-TEST1"
    When the user clicks on the entreprises tab
    And the user filters environment companies by name "CompanyAttached3"
    And the user edits the first company in environment
    And the user fills in the company siret "1234"
    And the user saves the company
    Then a company validation error message is displayed

  @TC_ENV_COMPANY_DELETE_001 @positive
  Scenario: Delete a company from the environment Entreprises tab
    Given the user navigates to the environments list page
    And the user edits the environment "ENV-TEST1"
    When the user clicks on the entreprises tab
    And the user filters environment companies by name "CompanyAttached3-Edited"
    And the user deletes the first company in environment
    Then a company deletion confirmation dialog is displayed
    When the user confirms the company deletion
    Then the company deletion in environment is successful

  @TC_ENV_COMPANY_DELETE_002 @positive
  Scenario: Cancel deletion of a company from the environment Entreprises tab
    Given the user navigates to the environments list page
    And the user edits the environment "ENV-TEST1"
    When the user clicks on the entreprises tab
    And the user filters environment companies by name "CompanyAttached1"
    And the user deletes the first company in environment
    Then a company deletion confirmation dialog is displayed
    When the user cancels the company deletion
    Then the company list in environment still contains companies