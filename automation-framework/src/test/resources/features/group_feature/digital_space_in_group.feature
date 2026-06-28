@company_groups @digitalspace @requiresLogin
Feature: Onglet Contenu pour les espaces digitaux

  Background:
    Given the user navigates to the company groups list page
    When the user edits the company group "Grptst"
    And the user clicks on the digital space tab


  # ══════════════════════════════
  #  SERVICES
  # ══════════════════════════════


  @services @smoke @add
  Scenario: Add first non-attached service and verify count increases
    When the user opens the "Services" section
    And the service count is stored
    And the user clicks on add service button
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
    Then the service dialog pagination shows "Page 1 sur"
    And the service dialog is on page 1
    And the user goes to the next page in service dialog
    Then the service dialog is on page 2
    When the user goes to the previous page in service dialog
    Then the service dialog is on page 1

  @services @delete
  Scenario: Add then delete a service restores count
    When the user opens the "Services" section
    And the service count is stored
    And the user clicks on add service button
    And the user selects the first non-attached service card
    And the user validates the service dialog
    And the user confirms the service creation
    Then the service count increased by 1
    When the user deletes the first item in the current section
    Then a service delete confirmation dialog is displayed
    When the user confirms the service deletion
    Then the service count is restored to stored value


    @services @cancel
  Scenario: Cancel deletion of a service keeps count unchanged
    When the user opens the "Services" section
    And the service count is stored
    When the user deletes the first item in the current section
    Then a service delete confirmation dialog is displayed
    When the user cancels the service deletion
    Then the service count is restored to stored value


  # ══════════════════════════════
  #  ACTUALITÉS
  # ══════════════════════════════

  @actualites @smoke
  Scenario: Add a news item with required fields
    When the user opens the "Actualités" section
    And the actualites count is stored
    And the user clicks on add actualite button
    And the user fills in the actualite title fr with "Test Creation"
    And the user fills in the actualite description fr with "Description de test."
    And the user fills in the actualite link with "https://example.com"
    And the user fills in the actualite link text fr with "Lire la suite"
    And the user uploads the photo "C:\Users\mariem.elhouche-ext\Downloads\logoprincipal.jpg"
    Then the image is uploaded successfully
    And the user fills in the actualite start date with "01/06/2025"
    And the user saves the actualite
    And the user confirms the actualite creation
    Then the actualite is saved successfully
    And the actualites count increased by 1

    ##tester l'ajout et la suppression de l'image
  @actualites @image @delete
  Scenario: Upload then delete an image on a news item form
    When the user opens the "Actualités" section
    And the user clicks on add actualite button
    And the user uploads the photo "C:\Users\mariem.elhouche-ext\Downloads\logoprincipal.jpg"
    Then the image is uploaded successfully
    When the user deletes the actualite image
    Then the actualite image is deleted

    #scenario complet
  @actualites @smoke
  Scenario: Add a complete news item with all fields
    When the user opens the "Actualités" section
    And the actualites count is stored
    And the user clicks on add actualite button
    And the user fills in the actualite title fr with "Actualité complète"
    And the user fills in the actualite title en with "Complete news"
    And the user fills in the actualite description fr with "Description française."
    And the user fills in the actualite description en with "English description."
    And the user fills in the actualite link with "https://example.com"
    And the user fills in the actualite link text fr with "Lire la suite"
    And the user fills in the actualite link text en with "Read more"
    And the user uploads the photo "C:\Users\mariem.elhouche-ext\Downloads\logoprincipal.jpg"
    Then the image is uploaded successfully
    And the user fills in the actualite start date with "01/06/2025"
    And the user saves the actualite
    And the user confirms the actualite creation
    Then the actualite is saved successfully


  @actualites @smoke
  Scenario: Cancel the creation of a news item
    When the user opens the "Actualités" section
    And the actualites count is stored
    And the user clicks on add actualite button
    And the user fills in the actualite title fr with "Testcancel"
    And the user fills in the actualite description fr with "Description de test."
    And the user fills in the actualite link with "https://example.com"
    And the user fills in the actualite link text fr with "Lire la suite"
    And the user uploads the photo "C:\Users\mariem.elhouche-ext\Downloads\logoprincipal.jpg"
    Then the image is uploaded successfully
    And the user fills in the actualite start date with "01/06/2025"
    And the user saves the actualite
    And the user cancels the actualite creation
    Then the actualites count is restored to stored value

  @actualites @delete
  Scenario: Delete an existing news item
    When the user opens the "Actualités" section
    And the actualites count is stored
    When the user deletes the first item in the current section
    Then an actualite delete confirmation dialog is displayed
    When the user confirms the actualite deletion
    Then the actualites count decreased by 1

  @actualites @delete
  Scenario: Cancel the delete of an existing news item
    When the user opens the "Actualités" section
    And the actualites count is stored
    When the user deletes the first item in the current section
    Then an actualite delete confirmation dialog is displayed
    And the user cancels the actualite deletion
    Then the actualites count is restored to stored value


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
