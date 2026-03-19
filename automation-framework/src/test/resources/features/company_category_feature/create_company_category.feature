@categories @requiresLogin
Feature: Company category creation
  Allow back-office users to create a company category and associate it with an existing company.

  Background:
    Given the user navigates to the company categories page

  @categories @requiresLogin
  Scenario Outline: Create a company category with an associated existing company
    When the user starts creating a new company category
    And the user enters the category information with name "<categoryName>" and code "<categoryCode>"
    And the user searches for an existing company by "<searchField>" with value "<searchValue>"
    And the user selects a company from the search results
    And the user saves the new category
    Then the selected company is associated with the category
    And a category creation confirmation message is displayed
    And the new category appears in the categories list with the associated company

    Examples:
      | categoryName     | categoryCode | searchField    | searchValue |
    #  | Cas-test2-Maryem | CTM123       | nom entreprise | test_maryem |
      | Cas-test3-Maryem | 123          | open id        | 98765       |
    # | Cas-test4-Maryem | 456          | siren          | 123456789   |

  @TC_CATEGORY_002 @validation
  Scenario: Fail to create a category without required information
    When the user starts creating a new company category
    And the user saves the category without filling the required fields
    Then an error message indicating that required fields must be filled is displayed
    And the category is not created

  @TC_CATEGORY_003 @negative
  Scenario: No results when searching for a company
    When the user starts creating a new company category
    And the user enters the category information
    And the user searches for a company with a non existing criteria
    Then no search results are displayed
    And no company can be selected

  @TC_CATEGORY_004 @negative
  Scenario: Attempt to save a category without associating a company
    When the user starts creating a new company category
    And the user enters the category information
    And the user does not associate any company to the category
    And the user saves the new category
    Then an error message indicating that a company must be associated is displayed
    And the category is not created