@group @group_attachments @requiresLogin
Feature: Rattachements tab in company group

  Background:
    Given the user navigates to the company groups list page
    When the user edits the company group "Grptst"
    And the user clicks on the group attachments tab

  # ── Tab & Title ───────────────────────────────────────────────────────────────

  @smoke
  Scenario: Rattachements tab is active and title is displayed
    Then the group attachments tab is active
    And the group attachment section title "Entreprises dans" is displayed

  # ── Table ─────────────────────────────────────────────────────────────────────

  @smoke
  Scenario: Attachments table is displayed with data
    Then the group attachments tab is active
    And the group attachments table is displayed

  # ── Filtering by name ─────────────────────────────────────────────────────────

  @filter
  Scenario: Filter by company name narrows the list
    When the user filters group attachments by name "test17-maryem"
    Then the group attachments table is displayed
    And all displayed group attachments contain "test17-maryem" in their name

  @filter
  Scenario: Clearing the name filter restores the full list
    When the user filters group attachments by name "Test"
    And the user clears the group attachments name filter
    Then the group attachments table is displayed

  # ── Filtering by Open ID ──────────────────────────────────────────────────────

  @filter
  Scenario: Filter by Open ID narrows the list
    When the user filters group attachments by open id "5555555"
    Then the group attachments table is displayed

  @filter
  Scenario: Clearing the Open ID filter restores the full list
    When the user filters group attachments by open id "1522"
    And the user clears the group attachments open id filter
    Then the group attachments table is displayed

  # ── Pagination: initial state ─────────────────────────────────────────────────

  @pagination @smoke
  Scenario: On page 1 the previous button is disabled and page 1 is active
    Then the group attachments previous page button is disabled
    And group attachments page 1 is active in the pagination
    And the group attachments current page is 1

  # ── Pagination: navigate next then back ───────────────────────────────────────

  @pagination
  Scenario: Navigate to next page then back to page 1
    Then the group attachments next page button is enabled
    When the user clicks on the group attachments next page
    Then the group attachments current page is 2
    And group attachments page 2 is active in the pagination
    And the group attachments previous page button is enabled
    When the user clicks on the group attachments previous page
    Then the group attachments current page is 1
    And group attachments page 1 is active in the pagination
    And the group attachments previous page button is disabled


# ── Add element: add form state ───────────────────────────────────────────────

  @add @smoke
  Scenario: Opening add attachment shows dropdown and disabled save button
    When the user clicks on add group attachment button
    Then the group attachment dropdown is open
    And the group attachment save button is disabled

# ── Add element: full flow ────────────────────────────────────────────────────

  @add
  Scenario: Add an existing company to the group via dropdown search
    When the user clicks on add group attachment button
    And the user searches for "test-auto-" in the group attachment dropdown
    And the user selects the company "Test-Auto-002" in the group attachment dropdown
    When the user closes the group attachment dropdown
    And the user saves the group attachment
    And a group attachment creation success toast is displayed
    Then the company "Test-Auto-002" is present in the group attachments table

  # ── Create new company ────────────────────────────────────────────────────────

  @create @smoke
  Scenario: Create new company button redirects to company creation page
    When the user clicks on create new company in this group
    Then the user is redirected to the company creation page from group

  # ── Delete: cancel ────────────────────────────────────────────────────────────

  @delete @cancel
  Scenario: Cancelling deletion keeps the row in the table
    Given the group attachments table is displayed
    When the user clicks the delete icon on group attachment row 1
    Then a group attachment delete confirmation dialog is displayed
    When the user cancels the group attachment deletion
    Then the group attachments table is displayed

  # ── Delete: confirm ───────────────────────────────────────────────────────────

  @delete @confirm
  Scenario: Confirming deletion removes the attachment
    Given the group attachments table is displayed
    When the user clicks the delete icon on group attachment row 1
    Then a group attachment delete confirmation dialog is displayed
    When the user confirms the group attachment deletion
    Then the group attachment is deleted successfully
