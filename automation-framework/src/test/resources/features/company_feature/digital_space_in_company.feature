@companies @digitalspace @requiresLogin
Feature: Onglet Contenu pour les espaces digitaux

  Background:
    Given the user navigates to the companies list page
    And the user clicks on edit icon for company "test17-maryem"
    And the user clicks on the digital space tab


  # ══════════════════════════════
  #  SERVICES
  # ══════════════════════════════


  @services @smoke @add
  Scenario: Add first non-attached service and verify count increases
    When the user opens the "Services" section
    And the service count is stored
    And the user clicks on add service button
    And the service dialog pagination shows "Page 1 sur"
    And the user selects the first non-attached service card
    And the user validates the service dialog
    And the user confirms the service creation
    Then the service count increased by 1

  @services @already-attached
  Scenario: At least one service card shows already attached badge
    When the user opens the "Services" section
    And the user clicks on add service button
    And at least one service card has the "Déjà rattaché" badge

  @services @pagination
  Scenario: Navigate forward then backward in service dialog
    When the user opens the "Services" section
    And the user clicks on add service button
    And the user goes to the next page in service dialog
    Then the service dialog is on page 2
    When the user goes to the previous page in service dialog
    Then the service dialog is on page 1

  @services @delete  @cancel
  Scenario: Add then cancel and confirm deletion of a service
    When the user opens the "Services" section
    And the service count is stored
    And the user clicks on add service button
    And the user selects the first non-attached service card
    And the user validates the service dialog
    And the user confirms the service creation
    Then the service count increased by 1
    When the user deletes the first item in the current section
    Then a service delete confirmation dialog is displayed
    When the user cancels the service deletion
    Then the service count increased by 1
    When the user deletes the first item in the current section
    Then a service delete confirmation dialog is displayed
    When the user confirms the service deletion
    Then the service count is restored to stored value

  # ══════════════════════════════
#  FEATURES
# ══════════════════════════════


  @features @smoke @add
  Scenario: Add a feature then uncheck and check it
    When the user opens the "Features" section
    And the features count is stored
    And the user clicks on add feature button
    And the user selects the first available feature from the dropdown
    When the user saves the feature
    And the user confirms the feature creation
    Then the features configuration is saved successfully
    And the features count increased by 1
    And the selected feature is checked
    When the user unchecks the selected feature
    Then the features configuration is saved successfully
    And the selected feature is unchecked
    When the user checks the selected feature
    Then the features configuration is saved successfully
    And the selected feature is checked

  @features @add @delete @cancel @confirm
  Scenario: Add a feature then cancel and confirm deletion
    When the user opens the "Features" section
    And the features count is stored
    And the user clicks on add feature button
    And the user selects the first available feature from the dropdown
    When the user saves the feature
    And the user cancels the feature creation
    Then the features count is restored to stored value
    And the user clicks on add feature button
    And the user selects the first available feature from the dropdown
    When the user saves the feature
    And the user confirms the feature creation
    Then the features configuration is saved successfully
    And the features count increased by 1
    And the features count is stored
    When the user deletes the first item in the current section
    And the user cancels the feature deletion
    Then the features count is restored to stored value
    When the user deletes the first item in the current section
    And the user confirms the feature deletion
    Then the feature deletion is confirmed successfully
    And the features count decreased by 1
