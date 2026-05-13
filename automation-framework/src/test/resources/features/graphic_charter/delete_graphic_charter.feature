@graphicCharter @requiresLogin @delete @regression
Feature: Graphic charter deletion

  Background:
    Given the user navigates to the graphic charters list page

  @TC_GC_DELETE_002 @positive @delete @cancel
  Scenario: Cancel then delete a graphic charter
    When the user deletes the graphic charter "Charte test 01_1777372563334"
    Then a graphic charter deletion confirmation dialog is displayed
    When the user cancels the graphic charter deletion
    Then the graphic charter "Charte test 01_1777372563334" still appears in the graphic charters list
    When the user deletes the graphic charter "Charte test 01_1777372563334"
    Then a graphic charter deletion confirmation dialog is displayed
    When the user confirms the graphic charter deletion
    Then a graphic charter deletion success message is displayed
    And the graphic charter "Charte test 01_1777372563334" no longer appears in the graphic charters list