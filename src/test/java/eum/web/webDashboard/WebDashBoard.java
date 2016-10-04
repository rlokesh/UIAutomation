package eum.web.webDashboard;

import static org.junit.Assert.assertTrue;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import common.controller.ui.modal.Application;
import common.controller.ui.modal.ControllerInfo;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.api.java.gl.E;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by akhilesh.singh on 5/27/16.
 */

public class WebDashBoard {
    private final WebDriver webDriver;
    private final ControllerInfo controllerInfo;
    private final Application application;

    @Inject
    public WebDashBoard(WebDriver webDriver, ControllerInfo controllerInfo, Application application) {
        this.webDriver = webDriver;
        this.controllerInfo = controllerInfo;
        this.application = application;
    }

    @When("^I login to the controller on ([^\"]+)$")
    public void loginIntoController(String browser) throws InterruptedException {

        webDriver.get(controllerInfo.getControllerUrl());
        WebElement submitElement = null;
        try {
            submitElement = new WebDriverWait(webDriver, 60).until(
                    ExpectedConditions.presenceOfElementLocated(By.id("submitInput")));
        } catch (Exception ex) {

        }
        if (submitElement != null && submitElement.isDisplayed()) {
            Map<String , String> elementsMap = new HashMap();
            elementsMap.put("accountNameInput", controllerInfo.getControllerAccount());
            elementsMap.put("userNameInput", controllerInfo.getControllerUser());
            elementsMap.put("passwordInput", controllerInfo.getControllerPassword());
            for (String elementName : elementsMap.keySet()) {
                if (webDriver.findElement(By.id(elementName)).isDisplayed()) {
                    WebElement webElement = new WebDriverWait(webDriver, 60).until(
                            ExpectedConditions.elementToBeClickable(By.id(elementName)));
                    if (webElement != null) {
                        webElement.clear();
                        webElement.sendKeys(elementsMap.get(elementName));
                    }
                }
            }
            new WebDriverWait(webDriver, 10).until(ExpectedConditions.elementToBeClickable(
                    By.id("submitInput")
            )).click();
        }
    }

    @When("I go to my ([^\"]*) application")
    public void navigateToWebApplication(String applicationName) {
        WebElement applicationLink = (new WebDriverWait(webDriver, 30))
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@class='ads-home-list-item-name ads-ellipsis ng-binding' and contains(text(), '" + applicationName + "')]")));
        applicationLink.click();
        System.out.print("test for click to work");
    }

    @And("I am taken to ([^\"]*) screen")
    public void validateTheNavigatedPage(String leftNavTitle) {

        WebElement BrowserAppDashboard = (new WebDriverWait(webDriver, 30))
                .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='ads-display-table-cell ads-vertical-align ng-binding' and contains(text(), '" + leftNavTitle + "')]")));
        assertTrue(BrowserAppDashboard != null);
        // assert that the nav element chosen on left has text 'Browser App Dashboard'
    }

    @And("([^\"]*) tab is open")
    public void checkOverViewTabIsOpen(String tabName) {

        WebElement overViewTab = (new WebDriverWait(webDriver, 30))
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//*[contains(text(), '" + tabName + "')]")));
        assertTrue(overViewTab.getAttribute("class").contains("selected"));
    }

    @Then("^I should see a ([^\"]*) widget with a ([^\"]+)$")
    public void validateAllTheWidget2(String widgetName, String dataType) {
        validateAllTheWidget(widgetName, dataType);
    }

    @Then("^it has a ([^\"]*) widget with a ([^\"]+)$")
    public void validateAllTheWidget(String widgetValue, String dataType) {
        System.out.println("WidgetName : " + widgetValue);
        System.out.println("DataType : " + dataType);
        if (dataType.equals("histogram")) {
            validateWidgetWithCustomTagAndDataTag("ad-histogram-widget-lite", "svg", "widget-title", widgetValue);
        }
        if (dataType.equals("time series")) {
            validateWidgetWithCustomTagAndDataTag("ad-timing-trend-widget-lite", "svg", "widget-title", widgetValue);
        }
        if (dataType.equals("geo map")) {
            validateWidgetWithCustomTagAndDataTag("div", "svg", "widget-title", widgetValue);
        }
        if (dataType.equals("table")) {
            validateWidgetWithCustomTagAndDataTag("ad-grid-widget-lite", "table", "widget-title", widgetValue);
        }
        if (dataType.equals("pie chart")) {
            validateWidgetWithCustomTagAndDataTag("ad-eum-web-stats-pie-widget-lite", "svg", "widget-title", widgetValue);
        }
    }

    private void validateWidgetWithCustomTagAndDataTag(String findByTagName, String validateInnerTag, String attributeName, String attributeValue) {
        List<WebElement> widgetTitle = (new WebDriverWait(webDriver, 30))
                .until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.tagName(findByTagName)));
        boolean elementPresent = false;
        for (WebElement widgetElement: widgetTitle) {
            String widgetElementAttribute = widgetElement.getAttribute(attributeName);
            if (widgetElementAttribute != null && widgetElementAttribute.contains(attributeValue)) {
                WebElement svgElement = widgetElement.findElement(By.tagName(validateInnerTag));
                if (svgElement != null) {
                    elementPresent = true;
                }
            }
        }
        assertTrue (elementPresent);
    }
}
