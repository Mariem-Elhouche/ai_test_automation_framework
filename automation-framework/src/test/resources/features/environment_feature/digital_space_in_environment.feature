@environment @digitalspace @requiresLogin
Feature: Onglet Contenu pour les espaces digitaux

  Background:
    Given the user navigates to the environments list page
    And the user edits the environment "ENV-TEST1"
    And the user clicks on the digital space tab

  # ══════════════════════════════
  #  DOMAINE
  # ══════════════════════════════

  ####scenario complet
  @domaine @smoke
  Scenario: Add then delete a secondary domain restores count
    When the user opens the "Domaine" section
    And the domain count is stored
    And the user clicks on add secondary domain button
    And the user selects the domain card at index 1
    And the user validates the domain dialog
    Then the domain count increased by 1
    When the user deletes the first non-primary domain
    Then a domain delete confirmation dialog is displayed
    When the user confirms the domain deletion
    Then the domain is deleted successfully
    Then the domain count is restored to stored value


  @domaine @smoke
  Scenario: Add a secondary domain increases count
    When the user opens the "Domaine" section
    And the domain count is stored
    And the user clicks on add secondary domain button
    Then the domain selection dialog is open
    And the domain dialog has at least 1 card
    When the user selects the domain card at index 1
    And the user validates the domain dialog
    Then the domain count increased by 1

#+++
  @domaine @delete
  Scenario: Cancel deletion keeps domain count unchanged
    When the user opens the "Domaine" section
    And the domain count is stored
    And the user deletes the first domain
    Then a domain delete confirmation dialog is displayed
    When the user confirms the domain deletion
    And the user cancels the domain deletion
    Then the domain count is restored to stored value
#+++
  @domaine @delete @smoke
  Scenario: Delete a secondary domain with confirmation decreases count
    When the user opens the "Domaine" section
    And the domain count is stored
    And the user deletes domain at index 2
    Then a delete confirmation dialog is displayed
    When the user confirms the domain deletion
    Then the domain is deleted successfully
    Then the domain count decreased by 1

#+++
  @domaine @primary @error
  Scenario: Deleting a primary domain shows error message
    When the user opens the "Domaine" section
    And domain at index 1 is the primary domain
    When the user deletes the first domain
    Then a delete confirmation dialog is displayed
    When the user confirms the domain deletion
    Then cannot delete primary domain message is displayed
    And the domain count is stored
#+++
  @domaine @primary @error
  Scenario: Unchecking primary checkbox shows error message
    When the user opens the "Domaine" section
    And domain at index 1 is the primary domain
    When the user clicks the primary checkbox of domain at index 1
    Then the cannot change primary domain message is displayed

#+++
  @domaine @section @close
  Scenario: Close domain section hides content
    When the user opens the "Domaine" section
    And the domain section is open
    When the user closes the domain section
    Then the domain section is closed

  # ══════════════════════════════
  #  FAQ
  # ══════════════════════════════

  @faq @smoke
  Scenario: Navigate to FAQ creation form
    When the user opens the "Contenu pour la FAQ" section
    And the user clicks on add FAQ question button
    Then the current url contains "/faq"

  @faq @smoke
  Scenario: Create a valid FAQ question with required fields only
    When the user opens the "Contenu pour la FAQ" section
    And the user clicks on add FAQ question button
    And the user fills in the french question with "Comment accéder à mon espace ?"
    And the user fills in the french answer with "Connectez-vous avec vos identifiants."
    And the user selects the FAQ theme "Réussir à me connecter"
    And the user saves the FAQ question
    Then the FAQ form is saved successfully


  @faq @smoke
  Scenario: Create a complete FAQ question with all fields
    When the user opens the "Contenu pour la FAQ" section
    And the user clicks on add FAQ question button
    And the user fills in the french question with "Comment consulter mes cotisations ?"
    And the user fills in the french answer with "Accédez à la rubrique 'Mes cotisations' depuis votre espace assuré pour consulter le détail"
    And the user fills in the english question with "How can I view my contributions?"
    And the user fills in the english answer with "Go to the 'My contributions' section in your member area to view details."
    And the user clears the FAQ theme
    And the user selects the FAQ theme "Comprendre mes cotisations"
    And the user enables the question globale toggle
    And the user saves the FAQ question
    Then the FAQ form is saved successfully

  @faq @validation
  Scenario: Submit FAQ form without french question shows error
    When the user opens the "Contenu pour la FAQ" section
    And the user clicks on add FAQ question button
    And the user fills in the french answer with "Réponse sans question."
    And the user saves the FAQ question
    Then a validation error is displayed on the FAQ form

  @faq @validation
  Scenario: Submit FAQ form with no fields shows validation errors
    When the user opens the "Contenu pour la FAQ" section
    And the user clicks on add FAQ question button
    And the user saves the FAQ question
    Then a validation error is displayed on the FAQ form

  @faq @validation
  Scenario: Submit FAQ form without french answer shows error
    When the user opens the "Contenu pour la FAQ" section
    And the user clicks on add FAQ question button
    And the user fills in the french question with "Question sans réponse ?"
    And the user fills in the french answer with ""
    And the user clears the FAQ theme
    And the user selects the FAQ theme "Comprendre mes cotisations"
    And the user saves the FAQ question
    #pour le moment pas de message d'alerte qui s'affiche
    Then a validation error is displayed on the FAQ form


  @faq @delete @smoke
  Scenario: Delete an added FAQ question with confirmation
    When the user opens the "Contenu pour la FAQ" section
    And the FAQ count is stored
    And the user clicks on add FAQ question button
    And the user fills in the french question with "Question à supprimer"
    And the user fills in the french answer with "Réponse temporaire."
    And the user clears the FAQ theme
    And the user selects the FAQ theme "Comprendre et activer ma télétransmission"
    And the user saves the FAQ question
    Then the FAQ form is saved successfully
    When the user opens the "Contenu pour la FAQ" section
    And the FAQ count is stored
    And the user deletes the first FAQ item
    Then a delete confirmation dialog is displayed
    When the user confirms the FAQ deletion
    #### L'assertion de FAQ count decreased by 1 ne fonctionne par car :
    ##c'est un bug qui ne prend pas en consideration les questions globales
    Then the FAQ count decreased by 1

  @faq @delete
  Scenario: Cancel deletion of a FAQ question keeps count unchanged
    When the user opens the "Contenu pour la FAQ" section
    And the FAQ count is stored
    And the user deletes the first FAQ item
    Then a delete confirmation dialog is displayed
    When the user cancels the FAQ deletion
    Then the FAQ count is restored to stored value

  @faq @section
  Scenario: Close FAQ section and open Services section
    When the user opens the "Contenu pour la FAQ" section
    And the user switches from "Contenu pour la FAQ" section to "Domaine" section
    Then the service selection dialog is open
    And the "Domaine" section is open



  # ══════════════════════════════
  #  SERVICES
  # ══════════════════════════════


  @services @smoke
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
