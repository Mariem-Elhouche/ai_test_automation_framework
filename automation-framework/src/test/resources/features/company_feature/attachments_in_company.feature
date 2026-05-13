@companies @attachments @requiresLogin
Feature: Rattachements tab in company

  Background:
    Given the user navigates to the companies list page
    And the user clicks on edit icon for company "test17-maryem"
    And the category edit form is displayed
    When the user clicks on the attachments tab
    Then the attachments tab is active


  @group @delete @cancel @confirm
  Scenario: Delete a company group (cancel then confirm)
    When the user attaches the company group "Grptst"
    Then the company group "Grptst" is attached
    When the user deletes the attached company group "Grptst"
    Then an attachments delete confirmation dialog is displayed
    When the user cancels the attachments deletion
    Then the company group "Grptst" is attached
    When the user deletes the attached company group "Grptst"
    Then an attachments delete confirmation dialog is displayed
    When the user confirms the attachments deletion
    Then the company group "Grptst" is not attached


  @category @delete
  Scenario: Delete a category (cancel then confirm)
    When the user opens the add category form
    And the user fills the category name "AutoCatDel" and code "AUTO_CAT_DEL"
    And the user saves the attachments category
    Then the category "AutoCatDel" with code "AUTO_CAT_DEL" is present
    When the user deletes the category "AutoCatDel" with code "AUTO_CAT_DEL"
    Then an attachments category delete confirmation dialog is displayed
    When the user cancels the attachments category deletion
    Then the category "AutoCatDel" with code "AUTO_CAT_DEL" is present
    When the user deletes the category "AutoCatDel" with code "AUTO_CAT_DEL"
    Then an attachments category delete confirmation dialog is displayed
    When the user confirms the attachments category deletion
    Then the category "AutoCatDel" with code "AUTO_CAT_DEL" is not present

