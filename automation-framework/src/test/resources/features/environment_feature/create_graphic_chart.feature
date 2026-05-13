@charte @create @requiresLogin
Feature: Graphic charter creation

  Background:
    Given the user navigates to the graphic charter page

  @TC_CHARTE_CREATE_001 @positive @requiresLogin
  Scenario: Create a graphic charter with all required fields
    When the user clicks on add graphic charter button
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
    And the user fills in keywords "test, automation"

    And the user saves the charter
    Then a charter creation success message is displayed