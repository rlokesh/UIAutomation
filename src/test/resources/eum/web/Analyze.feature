@eum-analytics
Feature: Analyze Screen
  This test suite deals with verifying the Analyze Screen ui.

  @setup
  Scenario: Test for controller Analyze Screen
#    Given I send beacons for 3 minutes
    When I login to the controller on chrome
    And I go to my ECommerce-E2E-EUM application
    And i click on Analyze in left Navigation pane
    Then I am taken to Analyze screen
    And I should see following column under Analyze screen
    | Timestamp | Page Name | Page Type | Device  | Browser | Country | Region  | Device OS |
    And Analytic data should be present
