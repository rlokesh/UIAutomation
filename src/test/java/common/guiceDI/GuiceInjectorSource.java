/*
 * Copyright (c) AppDynamics, Inc., and its affiliates
 * 2016
 * All Rights Reserved
 * THIS IS UNPUBLISHED PROPRIETARY CODE OF APPDYNAMICS, INC.
 * The copyright notice above does not evidence any actual or intended publication of such source code
 */

package common.guiceDI;

import static org.openqa.selenium.phantomjs.PhantomJSDriverService.PHANTOMJS_CLI_ARGS;
import static org.openqa.selenium.phantomjs.PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY;

import com.github.mkolisnyk.cucumber.reporting.CucumberResultsOverview;
import com.github.mkolisnyk.cucumber.reporting.types.result.CucumberFeatureResult;
import com.github.mkolisnyk.cucumber.reporting.types.result.CucumberScenarioResult;
import com.github.mkolisnyk.cucumber.reporting.types.result.CucumberStepResult;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import common.controller.ui.modal.ControllerInfo;
import common.screenRecorder.SpecializedScreenRecorder;
import common.support.LoadGenerator;
import cucumber.api.guice.CucumberModules;
import cucumber.runtime.java.guice.InjectorSource;
import net.anthavio.phanbedder.Phanbedder;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;

import static net.lightbody.bmp.proxy.CaptureType.*;

import org.monte.media.Format;
import org.monte.media.FormatKeys;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariDriver;

import static org.monte.media.AudioFormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

public class GuiceInjectorSource implements InjectorSource {
    public Injector getInjector() {
        Injector injector = Guice.createInjector(
                Stage.PRODUCTION, CucumberModules.SCENARIO, new GuiceModule());
        Runtime.getRuntime().addShutdownHook(injector.getInstance(GuiceModule.ShutdownHook.class));
        return injector;
    }
}


class GuiceModule extends AbstractModule {

    public void configure() {
        Properties properties = new Properties();
        InputStream resource = getClass().getClassLoader().getResourceAsStream(
                "cucumber.properties");
        try {
            properties.load(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Names.bindProperties(binder(), properties);
    }

    @Provides
    @Singleton
    private ControllerInfo buildControllerInfo(@Named("host") String controllerHost,
                                               @Named("port") String port,
                                               @Named("accountName") String accountName,
                                               @Named("userName") String userName,
                                               @Named("password") String password) {
        ControllerInfo controllerInfo = new ControllerInfo.ControllerInfoBuilder()
                .controllerAccount(accountName)
                .controllerUser(userName)
                .controllerPassword(password)
                .controllerHost(controllerHost)
                .controllerPort(port)
                .build();
        return controllerInfo;
    }

    @Provides
    @Singleton
    public BrowserMobProxy startProxyServer() {
        BrowserMobProxy proxyServer = new BrowserMobProxyServer();
        proxyServer.enableHarCaptureTypes(REQUEST_CONTENT, REQUEST_HEADERS,
                RESPONSE_CONTENT, RESPONSE_HEADERS);
        proxyServer.newHar();
        proxyServer.start(0);
        return proxyServer;
    }

    @Provides
    @Singleton
    @Inject
    public WebDriver openBrowser(@Named("browser") String browserFamily,
                                 BrowserMobProxy proxyServer, @Named("chromeDriverPath") String
                                             chromeDriverPath) {
        browserFamily = browserFamily.toLowerCase().trim();

        Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxyServer);
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);

        WebDriver browser;
        if ("firefox".equals(browserFamily) || "ff".equals(browserFamily)) {
            browser = new FirefoxDriver(capabilities);
        } else if ("chrome".equals(browserFamily)) {
            System.setProperty("webdriver.chrome.driver", chromeDriverPath);
            browser = new ChromeDriver(capabilities);
        } else if ("safari".equals(browserFamily)) {
            browser = new SafariDriver();
        } else if ("phantomjs".equals(browserFamily)) {
            File phantomjs = Phanbedder.unpack();
            capabilities.setCapability(PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                    phantomjs.getAbsolutePath());
            capabilities.setCapability(PHANTOMJS_CLI_ARGS, new String[]{
                    "--web-security=no",
                    "--ssl-protocol=any",
                    "--ignore-ssl-errors=yes"
            });
            browser = new PhantomJSDriver(capabilities);
        } else {
            browser = new FirefoxDriver();
        }
        browser.manage().window().maximize();
        return browser;
    }

    @Provides
    @Singleton
    public ScreenRecorder startRecording(@Named("recording") String recording)
            throws IOException, AWTException {
        File file = new File("build/test-results");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = screenSize.width;
        int height = screenSize.height;

        Rectangle captureSize = new Rectangle(0, 0, width, height);

        GraphicsConfiguration gc = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration();

        ScreenRecorder screenRecorder = new SpecializedScreenRecorder(
                gc, captureSize,
                new Format(MediaTypeKey,
                        FormatKeys.MediaType.FILE,
                        MimeTypeKey,
                        MIME_AVI),
                new Format(MediaTypeKey,
                        FormatKeys.MediaType.VIDEO,
                        EncodingKey,
                        ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                        CompressorNameKey,
                        ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                        DepthKey, 24,
                        FrameRateKey,
                        Rational.valueOf(
                                15),
                        QualityKey, 1.0f,
                        KeyFrameIntervalKey,
                        15 * 60),
                new Format(MediaTypeKey,
                        FormatKeys.MediaType.VIDEO,
                        EncodingKey,
                        "black",
                        FrameRateKey,
                        Rational.valueOf(
                                30)),
                null, file, "MyVideo");
        if (Boolean.valueOf(recording)) {
            screenRecorder.start();
        }
        return screenRecorder;
    }

    @Provides
    @Singleton
    public LoadGenerator startLoad(@Named("collectorUrl") String collector) {
        LoadGenerator loadGenerator = new LoadGenerator(collector);
        return loadGenerator;
    }

    static class ShutdownHook extends Thread {
        final WebDriver browser;
        final LoadGenerator loadGenerator;
        final ScreenRecorder screenRecorder;
        CucumberResultsOverview results;


        @Inject
        ShutdownHook(WebDriver browser,
                     LoadGenerator loadGenerator, ScreenRecorder screenRecorder) {
            this.browser = browser;
            this.loadGenerator = loadGenerator;
            this.screenRecorder = screenRecorder;
            this.results = new CucumberResultsOverview();
        }

        @Override
        public void run() {
            results.setOutputDirectory("build/test-results/html");
            results.setOutputName("cucumber-results");
            results.setSourceFile("./build/test-results/result.json");
            if (browser != null) {
                browser.quit();   // I see exception from here
            }

            try {
                results.executeFeaturesOverviewReport();
                screenRecorder.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }

            loadGenerator.stop();
            speak("Test is complete!");
            try {
                int numFailed = 0;
                int numPassed = 0;
                CucumberFeatureResult[] featureResults = results.readFileContent();
                for (CucumberFeatureResult featureResult : featureResults) {
                    CucumberScenarioResult[] scenarioResults = featureResult.getElements();
                    for (CucumberScenarioResult scenarioResult : scenarioResults) {
                        scenarioResult.getStatus();
                        CucumberStepResult[] stepResults = scenarioResult.getSteps();
                        numFailed += scenarioResult.getFailed();
                        numPassed += scenarioResult.getPassed();
                    }
                }
                if (numFailed == 0) {
                    speak("Bingo! All " + numPassed + " test cases are successful!");
                } else {
                    speak("Oops!");
                    speak(numPassed + " test cases are successful!");
                    speak(numFailed + " test cases are failed!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public void speak(String text) {
            Voice voice;
            VoiceManager voiceManager = VoiceManager.getInstance();
            voice = voiceManager.getVoice("kevin");
            voice.allocate();
            voice.speak(text);
        }
    }
}
