package eum.web.analyze;

import com.google.inject.Inject;
import cucumber.api.java.en.Then;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by akhilesh.singh on 6/23/16.
 */
public class Analyze {
    private final WebDriver webDriver;

    @Inject
    public Analyze(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    @Then("^I should see following column under Analyze screen$")
    public void coloumnShouldBePresent(List<String> columns) {
        Document analyticsScreen = Jsoup.parse(webDriver.getPageSource());
        Elements columnNameElements = analyticsScreen
                .getElementsByAttributeValueStarting("class", "ads-analytics-grid-header-container");
        for (String columnName : columns) {
            Elements spanElement = columnNameElements.select("span:contains(" + columnName + ")");
            assertTrue(columnName + " column Not visible." , spanElement.size() > 0);
        }
    }

    @Then("^Analytic data should be present$")
    public void checkForAnalyticsData() {
        Document analyticsScreen = Jsoup.parse(webDriver.getPageSource());
        Elements analyticsData = analyticsScreen.getElementsByAttributeValue("class", "ads-analytics-grid-row");

        if (analyticsData.size() > 0) {

        }
    }
}
