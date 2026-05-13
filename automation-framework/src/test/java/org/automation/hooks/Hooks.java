package org.automation.hooks;

import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import org.automation.factory.DriverFactory;
import org.automation.pages.LoginPage;
import org.automation.utils.ConfigLoader;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Hooks {

    private static final int TIMEOUT_SECONDS = 5;
    private static final boolean REUSE_DRIVER = Boolean.parseBoolean(
            ConfigLoader.getProperty("driver.reuse.enabled", "false")
    );
    private static final String RESET_STRATEGY = ConfigLoader.getProperty("driver.reuse.reset.strategy", "full");

    @Before(order = 0)
    public void setupDriver() {
        DriverFactory.initDriver();
        if (REUSE_DRIVER) {
            resetBrowserState(DriverFactory.getDriver());
        }
        System.out.println("Driver initialized");
    }

    @Before(value = "@requiresLogin", order = 1)
    public void loginBeforeScenario() {

        WebDriver driver = DriverFactory.getDriver();

        if (driver == null) {
            DriverFactory.initDriver();
            driver = DriverFactory.getDriver();
        }

        // ✅ si déjà connecté → on skip login
        if (isDashboardDisplayed(driver)) {
            System.out.println("Login skipped (already on dashboard)");
            return;
        }

        LoginPage loginPage = new LoginPage();

        loginPage.navigateToLoginPage();

        try {
            // Check quickly if we are already on the Microsoft email form.
            if (!loginPage.isEmailInputVisible(2)) {
                loginPage.clickLoginButton();
            }
            loginPage.enterEmail();
            loginPage.clickContinue();
            loginPage.enterPassword();
            loginPage.clickSubmit();
            loginPage.selectStayConnectedNo();
        } catch (Exception e) {
            throw new RuntimeException("Login steps failed: " + e.getMessage(), e);
        }

        boolean dashboardVisible = waitForDashboard(driver, TIMEOUT_SECONDS);
        assertTrue(dashboardVisible, "Dashboard not displayed after login");

        System.out.println("Login successful");
    }

    @After
    public void tearDown() {
        if (!REUSE_DRIVER) {
            DriverFactory.quitDriver();
        }
    }

    @AfterAll
    public static void globalTearDown() {
        if (REUSE_DRIVER) {
            DriverFactory.quitDriver();
        }
    }

    private boolean isDashboardDisplayed(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[contains(@class,'q-drawer')] | //a[contains(@href,'/entities')]")
            ));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    private boolean waitForDashboard(WebDriver driver, int timeoutSeconds) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[contains(@class,'q-drawer')] | //a[contains(@href,'/entities')]")
            ));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    private void resetBrowserState(WebDriver driver) {
        if (driver == null) return;

        closeExtraTabs(driver);

        if ("none".equalsIgnoreCase(RESET_STRATEGY)) {
            return;
        }

        if ("full".equalsIgnoreCase(RESET_STRATEGY)) {
            driver.manage().deleteAllCookies();
            clearStorage(driver);
            driver.navigate().to("about:blank");
        }
    }

    private void closeExtraTabs(WebDriver driver) {
        Set<String> handles = driver.getWindowHandles();
        if (handles.isEmpty()) return;

        List<String> tabs = new ArrayList<>(handles);
        String mainTab = tabs.get(0);
        for (int i = 1; i < tabs.size(); i++) {
            driver.switchTo().window(tabs.get(i));
            driver.close();
        }
        driver.switchTo().window(mainTab);
    }

    private void clearStorage(WebDriver driver) {
        if (!(driver instanceof JavascriptExecutor)) return;
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.localStorage.clear();");
        js.executeScript("window.sessionStorage.clear();");
    }
}
