@categories @requiresLogin @regression @create
Feature: Create Company

  Scenario @TC_COMPANY_001 @positive
    Given the user is logged in as a back-office administrator
    And the user navigates to the companies page
    When the user clicks on the "Create New Company" button
    And the user fills in the required fields: Name, Address, Open ID, Environment
    And the user submits the form
    Then the user should see a message indicating successful creation
    And the newly created company should appear on the companies page

  Scenario @TC_COMPANY_002 @allFields @positive
    Given the user is logged in as a back-office administrator
    And the user navigates to the companies page
    When the user clicks on the "Create New Company" button
    And the user fills in all fields: Name, Address, Siret, Postal Code, City, Country, Email, Phone
    And the user submits the form
    Then the user should see a message indicating successful creation
    And the newly created company should appear on the companies page

  Scenario @TC_COMPANY_003 @requiredFieldMissing @negative
    Given the user is logged in as a back-office administrator
    And the user navigates to the companies page
    When the user clicks on the "Create New Company" button
    And the user attempts to submit the form without a required field (Name, Address, Open ID, or Environment)
    Then the user should see a validation error for the missing field(s)

  Scenario @TC_COMPANY_004 @environmentMissing @negative
    Given the user is logged in as a back-office administrator
    And the user navigates to the companies page
    When the user clicks on the "Create New Company" button
    And the user attempts to submit the form without an environment
    Then the user should see a validation error for the missing environment field

  Scenario @TC_COMPANY_005 @invalidSIRET @negative
    Given the user is logged in as a back-office administrator
    And the user navigates to the companies page
    When the user clicks on the "Create New Company" button
    And the user fills in the SIRET field with an invalid format (e.g., non-numeric values or incorrect length)
    And the user submits the form
    Then the user should see a validation error for the SIRET field