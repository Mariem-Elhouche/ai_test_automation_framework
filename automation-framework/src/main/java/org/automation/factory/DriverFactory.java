package org.automation.factory;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;

public class DriverFactory {

    private static WebDriver driver;

    private DriverFactory() {
        // Utility class
    }

    public static synchronized WebDriver initDriver() {
        if (driver != null) {
            // Si une ancienne session a été fermée/crash, on recrée un driver propre
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
            driver = new ChromeDriver();
            driver.manage().window().maximize();
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
