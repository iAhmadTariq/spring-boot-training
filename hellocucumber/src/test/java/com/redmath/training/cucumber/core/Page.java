package com.redmath.training.cucumber.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class Page implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ApplicationContext context;
    private final RemoteWebDriver webDriver;
    private final WebDriverWait webDriverWait;
    private final long sleepInMillis;
    private final long timeOutInSeconds;
    private final SimpleDateFormat fileNameFormat;

    public Page(ApplicationContext context, RemoteWebDriver webDriver,
            @Value("${webdriver.wait.sleepInMillis:500}") long sleepInMillis,
            @Value("${webdriver.wait.timeOutInSeconds:5}") long timeOutInSeconds) {
        this.context = context;
        this.webDriver = webDriver;
        this.sleepInMillis = sleepInMillis;
        this.timeOutInSeconds = timeOutInSeconds;
        this.webDriverWait = driverWait(this.webDriver, this.timeOutInSeconds, this.sleepInMillis);
        this.fileNameFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        webDriver.manage().window().maximize();
    }

    /**
     * Resolves a "$some.property.path" reference against the Spring
     * Environment (i.e. application.yml). Values that don't start with
     * "$" are returned unchanged, so literal strings still work in steps.
     */
    public String resolve(String value) {
        if (value != null && value.startsWith("$")) {
            return context.getEnvironment().getProperty(value.substring(1));
        }
        return value;
    }

    public WebDriver webDriver() {
        return webDriver;
    }

    public WebDriverWait driverWait() {
        return webDriverWait;
    }

    public WebDriverWait driverWait(long timeOutInSeconds) {
        return driverWait(webDriver, timeOutInSeconds, sleepInMillis);
    }

    public WebDriverWait driverWait(long timeOutInSeconds, long sleepInMillis) {
        return driverWait(webDriver, timeOutInSeconds, sleepInMillis);
    }

    public WebDriverWait driverWait(WebDriver webDriver, long timeOutInSeconds, long sleepInMillis) {
        logger.debug("driverWait({}, {})", timeOutInSeconds, sleepInMillis);
        return new WebDriverWait(webDriver, Duration.ofSeconds(timeOutInSeconds), Duration.ofMillis(sleepInMillis));
    }

    public ExpectedCondition<WebElement> elementBy(By locator) {
        return ExpectedConditions.presenceOfElementLocated(locator);
    }

    public void visit(String url) {
        logger.debug("load({})", url);
        webDriver().get(resolve(url));
    }

    public String title() {
        logger.debug("title()");
        String title = webDriver().getTitle();
        logger.debug("title(): {}", title);
        return title;
    }

    public void visible(By locator) {
        logger.debug("visible({})", locator);
        driverWait().until(elementBy(locator)).isDisplayed();
    }

    public void click(By locator) {
        logger.debug("click({})", locator);
        driverWait().until(elementBy(locator)).click();
    }

    public void text(String text, By locator) {
        logger.debug("text({}, {})", text, locator);
        WebElement element = driverWait().until(elementBy(locator));
        element.clear();
        element.sendKeys(text);
    }

    public Path screenshot() {
        logger.debug("screenshot()");
        try {
            Path directory = Paths.get("target", "screenshots");
            Files.createDirectories(directory);
            Path destination = directory.resolve(fileNameFormat.format(new Date()) + ".png");
            File source = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
            Files.copy(source.toPath(), destination);
            logger.debug("screenshot(): {}", destination);
            return destination;
        } catch (IOException e) {
            throw new RuntimeException("Unable to take screenshot", e);
        }
    }
}
