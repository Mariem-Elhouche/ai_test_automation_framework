@graphicCharter @requiresLogin @edit @regression
Feature: Graphic charter edition

  Background:
    Given the user navigates to the graphic charters list page

  @TC_GC_EDIT_001 @positive
  Scenario: Edit an existing graphic charter from the list
    When the user clicks on edit icon for charter "Noveocare"
    And the user saves the charter
    Then a charter creation success message is displayed
    And the graphic charter "Noveocare" appears in the graphic charters list