package com.redmath.training.cucumber.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class WebDriverConfiguration {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${webdriver.browser:firefox}")
    private String browser;

    @Value("${webdriver.browser.firefox.location:}")
    private String firefoxLocation;

    @Bean(destroyMethod = "quit")
    public RemoteWebDriver webDriver() {
        logger.debug("webDriver(): browser={}", browser);

        if (!"firefox".equalsIgnoreCase(browser)) {
            throw new IllegalStateException(
                    "This configuration is wired for Firefox only. Unsupported webdriver.browser: " + browser);
        }

        return firefoxDriver();
    }

    private RemoteWebDriver firefoxDriver() {
        // Downloads/matches the correct geckodriver binary automatically,
        // so you don't need geckodriver on the PATH yourself.
        WebDriverManager.firefoxdriver().setup();

        FirefoxOptions options = new FirefoxOptions();

        if (StringUtils.hasText(firefoxLocation)) {
            logger.debug("firefoxDriver(): using binary at {}", firefoxLocation);
            options.setBinary(firefoxLocation);
        }

        // Uncomment for CI / headless runs:
        // options.addArguments("-headless");

        return new FirefoxDriver(options);
    }
}
