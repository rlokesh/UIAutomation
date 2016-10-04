@eum-Browser-App-Dashboard
Feature: Web Dashboard
  This test suite deals with verifying the Web Dashboard screen ui.

  @setup
  Scenario: Test for controller web page dashboard screen
#    Given I send beacons for 3 minutes
    When I login to the controller on chrome
    When I create EUM application with name ECommerce-E2E-EUM
    And I go to my ECommerce-E2E-EUM application
    Then I am taken to ECommerce-E2E-EUM screen
    And I go to Overview tab

  Scenario Outline: checking the Browser App Dashboard widgets
    Given Overview tab is open
    Then I should see a <widget> widget with a <type>

    Examples:
    | widget                                 | type        |
    | End User Response Time Distribution    | histogram   |
    | Page Requests per Minute               | time series |
    | End User Response Time Trend           | time series |
    | Top 5 Countries by Total Page Requests | table       |
    | Top 5 Pages by Total Requests          | table       |
    | Browsers                               | pie chart   |
    | Devices                                | pie chart   |
    | End User Response Time                 | geo map     |
    | Total Page Request                     | geo map     |