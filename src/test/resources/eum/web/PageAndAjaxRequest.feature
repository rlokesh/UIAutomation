@eum-Page&Ajax
Feature: Page And Ajax
  This test suite deals with verifying the Page & Ajax request` screen ui.

  @setup
  Scenario: Navigate to Page & Ajax screen
#    Given I send beacons for 3 minutes
    Given I login to the controller on firefox
    And I go to my ECommerce-E2E-EUM application
    And i click on Pages & AJAX Requests in left Navigation pane
    Then I am taken to Pages & AJAX Requests screen

  Scenario Outline: Test for common metrics in Page list screen
    When I am taken to Pages & AJAX Requests screen
    Then i should see <PageType> page discovered
    And I should see following column:
      | Requests  | Name  | Requests per Minute  | End User Response Time (ms)  | First Byte Time (ms) |
    And each <PageType> page should have data

    Examples:
      | PageType |
      | Base     |
      | Ajax     |
      | Iframe   |
      | Virtual  |
