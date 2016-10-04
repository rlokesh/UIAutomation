package eum.web.geoDashboard;

import com.google.inject.Inject;
import common.controller.ui.modal.Application;
import common.controller.ui.modal.ControllerInfo;
import cucumber.api.DataTable;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by akhilesh.singh on 6/22/16.
 */
public class GeoDashboard {
    private final WebDriver webDriver;
    private final ControllerInfo controllerInfo;
    private final Application application;

    @Inject
    public GeoDashboard(WebDriver webDriver, ControllerInfo controllerInfo,
                        Application application) {
        this.webDriver = webDriver;
        this.controllerInfo = controllerInfo;
        this.application = application;
    }

    @When("^I go to ([^\"]*) tab$")
    public void i_go_to_Geo_Dashboard_tab(String tabName) throws Throwable {
        WebElement GeoDashboardTab = new WebDriverWait(webDriver, 30).until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//div[@class='ng-binding ng-scope adsTabLevel1' " +
                                         "and contains(text(), '" + tabName + "')]"))
        );
        if (GeoDashboardTab != null) {
            GeoDashboardTab.click();
        }

    }

    @Then("^([^\"]*) tab should be visible")
    public void geo_Dashboard_should_be_present(String tabName) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        WebElement GeoDashboardTab = new WebDriverWait(webDriver, 30).until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//div[@class='ng-binding ng-scope adsTabLevel1 selected' " +
                                         "and contains(text(), '" + tabName + "')]"))
        );
        assertTrue(GeoDashboardTab != null);
        WebElement geoMapElement = new WebDriverWait(webDriver, 30).until(
                ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//div[@class='ads-geo-map-svg-layer']")
                ));
        assertTrue(geoMapElement.isDisplayed());
    }

    @Then("^all section should have graph plotted$")
    public void section_should_have_graph_plotted(DataTable arg1) throws Throwable {

    }

    @Then("^Geo Map should have some region colored$")
    public void geo_Map_should_have_some_region_colored() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        List<WebElement> coloredImage = new WebDriverWait(webDriver, 30).until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(
                        By.xpath("//*[@class='ads-geo-map-load-circle']")
                )
        );
        assertTrue(coloredImage.size() > 0);

        List<WebElement> countryPresent = new WebDriverWait(webDriver, 30).until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(
                        By.xpath("//*[@class='adsCursorPointer']")
                )
        );

        assertTrue(countryPresent.size() > 0);
    }


}
