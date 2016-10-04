/*
 * Copyright (c) AppDynamics, Inc., and its affiliates
 * 2016
 * All Rights Reserved
 * THIS IS UNPUBLISHED PROPRIETARY CODE OF APPDYNAMICS, INC.
 * The copyright notice above does not evidence any actual or intended publication of such source code
 */

package eum.web;

import com.google.inject.Inject;
import common.controller.ui.helper.ApplicationServiceFactory;
import common.support.LoadGenerator;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;


public class WebAppSetup {
    private final WebDriver webDriver;
    private final LoadGenerator loadGenerator;
    private final ApplicationServiceFactory applicationServiceFactory;

    @Inject
    public WebAppSetup(LoadGenerator loadGenerator,
                       WebDriver webDriver,
                       ApplicationServiceFactory application) {
        this.loadGenerator = loadGenerator;
        this.webDriver = webDriver;
        this.applicationServiceFactory = application;
    }

    @When("^I send beacons for (\\d+) minutes$")
    public void generateLoadUsingLoadGen(int minutes) {
        loadGenerator.blockUntilLoadGeneratedFor(applicationServiceFactory.getAppKey(), 3);
    }

    @When("^All the test executed$")
    public void allTestExecuted() {
    }

    @When("^I create ([^\"]+) application with name ([^\"]+)$")
    public void createApplicationUsingWebDriver(String applicationType, String applicationName) {
        applicationServiceFactory.createApplication();
    }

    @Then("^Capture the screenShot as ([^\"]+)$")
    public void takeScreenShotAndPrintReport(String fileName) {
        File screenShot = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(screenShot, new File(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
