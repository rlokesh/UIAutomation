package eum.web;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import common.controller.restapi.client.admin.ServerLogClient;
import common.controller.ui.helper.ApplicationServiceFactory;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.core.har.Har;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.openqa.selenium.OutputType.BYTES;

public class TagHooks {
    public static final int LINE_LIMIT = 60;
    private final WebDriver webDriver;
    private final ApplicationServiceFactory applicationServiceFactory;
    private final BrowserMobProxy proxyServer;
    private final ServerLogClient serverLogClient;
    private final String needBrowserLog;
    private final String needControllerServerLog;
    private final String needHARFile;
    private final String needScreenShot;

    @Inject
    public TagHooks(WebDriver webDriver, ApplicationServiceFactory applicationServiceFactory,
                    BrowserMobProxy proxyServer, ServerLogClient serverLogClient,
                    @Named("needBrowserLog") String needBrowserLog,
                    @Named("needControllerServerLog") String needControllerServerLog,
                    @Named("needHARFile") String needHARFile,
                    @Named("needScreenShot") String needScreenShot) {
        this.webDriver = webDriver;
        this.applicationServiceFactory = applicationServiceFactory;
        this.proxyServer = proxyServer;
        this.serverLogClient = serverLogClient;
        this.needBrowserLog = needBrowserLog;
        this.needControllerServerLog = needControllerServerLog;
        this.needHARFile = needHARFile;
        this.needScreenShot = needScreenShot;
    }

    @After
    public void collectBrowserConsoleLog(Scenario result) throws
            Exception {
        if (result.isFailed() && Boolean.parseBoolean(needBrowserLog)) {
            LogEntries logs = webDriver.manage().logs().get("browser");
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Browser Console logs :");
            for (LogEntry entry : logs) {
                if (entry.getTimestamp() > System.currentTimeMillis() - 1 * 60 * 1000) {
                    stringBuilder.append(System.lineSeparator());
                    String msg = entry.getMessage();
                    String firstLine = msg.length() > LINE_LIMIT + 5 ? msg.substring(0, LINE_LIMIT) : msg;
                    stringBuilder.append(new Date(entry.getTimestamp()) + " " + entry.getLevel() + " " + firstLine);
                    if (msg.length() > LINE_LIMIT + 5) {
                        stringBuilder.append(System.lineSeparator())
                                .append(breakDownLongStringToNewline(msg.substring(LINE_LIMIT)));
                    }
                }
            }
            result.write(stringBuilder.toString());
        }
    }

    @After
    public void collectServerLog(Scenario result) throws IOException {
        if (result.isFailed() && Boolean.parseBoolean(needControllerServerLog)) {
            result.write(serverLogClient.giveMeRelatedLog(System.currentTimeMillis(), 1));
        }
    }

    @After
    public void collectHarFile(Scenario result) throws IOException {
        if (result.isFailed() && Boolean.parseBoolean(needHARFile)) {
            Har har = proxyServer.getHar();
            File harFile = new File("build/test-results/html/" + result.getId() + ".har");
            har.writeTo(harFile);
            result.write("Har File path : " + harFile.getAbsolutePath());
        }
    }

    @After
    public void collectScreenShot(Scenario result) {
        if (result.isFailed() && Boolean.parseBoolean(needScreenShot)) {
            byte[] screenShot = ((TakesScreenshot) webDriver).getScreenshotAs(BYTES);

            result.embed(screenShot, "image/png");
        }
    }


    @Before("@setup")
    public void createApplicationTypeMobile() {

//        applicationServiceFactory.createApplication();
    }

    private String breakDownLongStringToNewline(String s) {
        int MAX_LENGTH = 100;
        if (s.length() <= MAX_LENGTH + 3) {
            return s;
        } else {
            return s.substring(0, MAX_LENGTH) + System.lineSeparator()
                    + breakDownLongStringToNewline(s.substring(MAX_LENGTH));
        }
    }
}
