@eum-Browser-App-Dashboard
Feature: GEO Dashboard
  This test suite deals with verifying the Web Dashboard screen ui.

  @setup
  Scenario: Test for controller web page dashboard screen
#    Given I send beacons for 3 minutes
    When I login to the controller on chrome
    When I create EUM application with name ECommerce-E2E-EUM
    And I go to my ECommerce-E2E-EUM application
    Then I am taken to ECommerce-E2E-EUM screen

  Scenario: Navigate to GeoDashboard
    When I am taken to ECommerce-E2E-EUM screen
    And I go to Geo Dashboard tab
    Then Geo Dashboard tab should be visible
    And all section should have graph plotted
    | Page Requests | End User Response Time  | JavaScripts Errors  |
    And Geo Map should have some region colored
