/*
 * Copyright (c) AppDynamics, Inc., and its affiliates
 * 2016
 * All Rights Reserved
 * THIS IS UNPUBLISHED PROPRIETARY CODE OF APPDYNAMICS, INC.
 * The copyright notice above does not evidence any actual or intended publication of such source code
 */

package eum.web.pageAndAjaxRequest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.inject.Inject;
import com.thoughtworks.selenium.webdriven.commands.WaitForCondition;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

/**
 * Created by akhilesh.singh on 6/3/16.
 */
public class PageAndAjaxRequest {
    private final WebDriver webDriver;

    @Inject
    public PageAndAjaxRequest(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    @When("^i click on ([^\"]+) in left Navigation pane$")
    public void navigateTopageAndAjaxRequest(String leftNavTitleValue) {
        WebElement leftNavTitle = new WebDriverWait(webDriver, 60).until(
                ExpectedConditions.elementToBeClickable(By.xpath(
                        "//div[@class='ads-display-table-cell ads-left-nav-main-item-label ng-binding' and contains(text(), '"
                                + leftNavTitleValue + "')]")));
        assertTrue(leftNavTitle != null);
        leftNavTitle.click();
    }

    @Then("each ([^\"]+) page should have data")
    public void validatePages(String pageType) {
        String iconType = getIconNamePartFor(pageType);
        Document pageListScreen = Jsoup.parse(webDriver.getPageSource());
        // Here data-recordindex is an attribute of the tag <tr>. so it resembles 1 record in the page list screen
        Elements elements = pageListScreen.getElementsByAttribute("data-recordindex");
        for (Element element : elements) {
            if (element.select("img[src=images/eum/eum_" + iconType + ".svg]").size() > 0) {
                ((JavascriptExecutor) webDriver).executeScript("arguments[0].scrollIntoView(true)",
                                                               webDriver.findElement(
                                                                       By.id(element.id())));
                // each element is one record and has 8 <TD> coloumn that is displayed. the child index starts
                // with 0 So at index 1 we get the Name column
                String pageName = element.child(1).text();
                checkNotEmpty(pageType, pageName);

                // index 2 has the Requests column value
                checkNotEmptyOrNegative(pageType, pageName, element.child(2).text());

                // index 3 has the Requests per minute column value
                checkNotEmptyOrNegative(pageType, pageName, element.child(3).text());

                // index 6 has the First Byte Time column value
                checkNotEmptyOrNegative(pageType, pageName, element.child(6).text());
            }
        }
    }

    @Then("^I should see following column:")
    public void validateColumn(List<String> columnNames) {
        Document pageListScreen = Jsoup.parse(webDriver.getPageSource());
        Elements columnNameElements = pageListScreen.getElementsByAttributeValueStarting("id",
                                                                                         "headercontainer-");
        for (String columnName : columnNames) {
            Elements spanElement = columnNameElements.select("span:contains(" + columnName + ")");
            assertTrue(columnName + " column Not visible.", spanElement.size() > 0);
        }
    }

    @Then("i should see ([^\"]+) page discovered")
    public void validatePageTypeFor(String pageType) {
        String iconType = getIconNamePartFor(pageType);
        Document pageListScreen = Jsoup.parse(webDriver.getPageSource());
        Elements pages = pageListScreen.getElementsByAttribute("data-recordindex").select(
                "[src=images/eum/eum_" + iconType + ".svg]");
        System.out.println("Pages discovered : " + iconType + ", number : " + pages.size());
        assertTrue(pages.size() > 0);
    }

    private String getIconNamePartFor(String pageType) {
        return pageType.equals("Base") ? "page" : pageType.equals("Ajax") ? "ajax" :
                pageType.equals("Iframe") ? "iframe" : pageType.equals("Virtual") ? "vpage" : null;
    }

    private void checkNotEmpty(String pageType, String checkString) {
        if (checkString != null) {
            assertFalse(pageType + " page has one empty name ", (checkString.trim().isEmpty()));
        }
    }

    private void checkNotEmptyOrNegative(String pageType, String pageName, String checkString) {
        if (checkString != null) {
            try {
                long numberValue = Long.parseLong(checkString);
                assertTrue(pageType + " page has one column either empty or negative value for " +
                                   "page Name :" + pageName,
                           (!checkString.trim().isEmpty() || Integer.parseInt(checkString) > 0));
            } catch (Exception ex) {
                throw new RuntimeException(
                        pageType + " page has one column either empty or negative value for " +
                                "page Name :" + pageName);
            }
        }
    }

}
