package org.automation.factory;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class DriverFactory {

    private static WebDriver driver;

    private DriverFactory() {
    }

    public static synchronized WebDriver initDriver() {
        if (driver != null) {
            try {
                driver.getWindowHandles();
            } catch (WebDriverException e) {
                try {
                    driver.quit();
                } catch (Exception ignored) {
                }
                driver = null;
            }
        }

        if (driver == null) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--start-maximized");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            driver = new ChromeDriver(options);
        }
        return driver;
    }

    public static synchronized WebDriver getDriver() {
        return driver;
    }

    public static synchronized WebDriver getOrInitDriver() {
        return initDriver();
    }

    public static synchronized void setDriver(WebDriver driverInstance) {
        driver = driverInstance;
    }

    public static synchronized void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
