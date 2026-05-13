@graphicCharter @requiresLogin @create @regression
Feature: Graphic charter creation — from the standalone list page

  Background:
#    Given the user navigates to the graphic charters list page
#    And the user clicks on new graphic charter button from the list
  Given the user navigates to the graphic charters new form page

  # ─── POSITIVE ─────────────────────────────────────────────────────────────

  @TC_GC_CREATE_LIST_011 @positive
  Scenario: Create a graphic charter with all required fields
    And the user fills in the charter name "Charte test 01"
    And the user uploads the logo principal "C:\Users\mariem.elhouche-ext\Downloads\logoprincipal.jpg"
    And the user uploads the logo secondaire "C:\Users\mariem.elhouche-ext\Downloads\connexion.png"
    And the user uploads the favicon "C:\Users\mariem.elhouche-ext\Downloads\connexion.png"
    And the user uploads the image espace assure "C:\Users\mariem.elhouche-ext\Downloads\connexion.png"
    And the user uploads the image affiliation "C:\Users\mariem.elhouche-ext\Downloads\connexion.png"
    And the user uploads the image backoffice "C:\Users\mariem.elhouche-ext\Downloads\connexion.png"
    And the user uploads the image web rh "C:\Users\mariem.elhouche-ext\Downloads\connexion.png"
    And the user uploads the image home header "C:\Users\mariem.elhouche-ext\Downloads\entete.png"
    And the user uploads the font text "C:\\Users\\mariem.elhouche-ext\\Downloads\\MushaOtf.otf"
    And the user fills in font text name "Roboto"
    And the user uploads the font title "C:\\Users\\mariem.elhouche-ext\\Downloads\\MushaTtf.ttf"
    And the user fills in font title name "Arial"
    And the user selects primary color "#FF0000"
    And the user selects primary color bold "#FF0000"
    And the user selects primary background color "#771577"
    And the user selects secondary color "#00FF00"
    And the user selects secondary bold color "#FF1577"
    And the user selects secondary background color "#001577"
    And the user fills in small border radius "4"
    And the user fills in medium border radius "8"
    And the user fills in large border radius "12"
    And the user fills in description "Test automation charte"
    And the user fills in keywords "test "
    And the user saves the charter
    Then a charter creation success message is displayed


  # ─── VALIDATION DES CHAMPS OBLIGATOIRES ────────────────────────────────

  @TC_GC_CREATE_VALIDATION_001 @negative
  Scenario Outline: Validation message "Le champ ne peut pas être vide" displayed after clearing "<field>"
    When the user clears the graphic charter <action>
    Then the field validation message "Le champ ne peut pas être vide" is displayed for graphic charter field "<field>"
    Examples:
      | field                | action                |
      | charter name         | charter name          |
      | font text name       | font text name        |
      | font title name      | font title name       |
      | description          | description           |
      | small border radius  | small border radius   |
      | medium border radius | medium border radius  |
      | large border radius  | large border radius   |

  # ─── VALIDATION DES CHAMPS COULEURS ────────────────────────────────────

  @TC_GC_CREATE_VALIDATION_002 @negative
  Scenario Outline: Validation message "Couleur invalide" displayed after entering invalid "<field>"
    When the user enters invalid color "<value>" in the graphic charter field "<action>"
    Then the field validation message "Couleur invalide" is displayed for graphic charter field "<field>"

    Examples:
      | field                      | value   | action                     |
      | primary color              | aaaa    | primary color              |
      | secondary color            | red     | secondary color            |
      | primary background color   | 12345   | primary background color   |
      | secondary background color | #ZZZZZZ | secondary background color |