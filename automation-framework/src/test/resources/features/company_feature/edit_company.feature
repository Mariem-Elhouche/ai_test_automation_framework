@companies @requiresLogin @editCompany @regression
Feature: Company edition

  Background:
    Given the user navigates to the companies list page

    #modif nom

  @TC_COMP_EDIT_001 @positive
  Scenario: Edit company name successfully
    When the user edits the company "Test-Auto-Edited" and changes its name to "Test7-Auto-Edited"
    Then a company edit success message is displayed
    And the company "Test7-Auto-Edited" appears in the companies list


  #modif siret

  @TC_COMP_EDIT_003 @positive
  Scenario: Edit company SIRET successfully
    When the user edits the company "Testtest5" and changes its siret to "11111111555555"
    Then a company edit success message is displayed
 #---clear siret
  @TC_COMP_EDIT_004 @positive
  Scenario: Clear company SIRET (optional field)
    When the user edits the company "Test17-Auto-Full" and clears its siret
    Then a company edit success message is displayed

  # modif open id

  @TC_COMP_EDIT_005 @positive
  Scenario: Edit company Open ID successfully
    When the user edits the company "Testtest3" and changes its open id to "3333333"
    Then a company edit success message is displayed

  # validation: clearing required field

  @TC_COMP_EDIT_006 @negative @validation
  Scenario: Fail to edit a company by clearing the required name field
    When the user edits the company "Test3-Auto-Full" and clears its name
    And the user saves the company
    Then a company validation error message is displayed
# invalid siret format
  @TC_COMP_EDIT_007 @negative @validation
  Scenario: Fail to edit a company with an invalid SIRET format
    When the user edits the company "Test4-Auto-Full" and changes its siret to "INVALID"
    Then a company validation error message is displayed