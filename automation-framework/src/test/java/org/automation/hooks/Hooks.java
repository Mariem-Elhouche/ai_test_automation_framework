package org.automation.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.AfterAll;
import org.automation.factory.DriverFactory;
import org.automation.pages.LoginPage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Hooks {

    private static final int TIMEOUT_SECONDS = 5;
    private static boolean isLoggedIn = false;

    @Before(order = 0)
    public void setupDriver() {
        // Plus de référence à formPage/listPage ici
        if (DriverFactory.getDriver() == null) {
            DriverFactory.initDriver();
            System.out.println("Driver initialized");
        }
    }

    @Before(value = "@requiresLogin", order = 1)
    public void loginBeforeScenario() {
        WebDriver driver = DriverFactory.getDriver();
        LoginPage loginPage = new LoginPage();

        if (isLoggedIn || isDashboardDisplayed(driver)) {
            isLoggedIn = true;
            System.out.println("Login skipped (session already active)");
            return;
        }

        loginPage.navigateToLoginPage();

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("i0116")));
        } catch (TimeoutException e) {
            if (isDashboardDisplayed(driver)) {
                isLoggedIn = true;
                return;
            }
        }

        try {
            loginPage.clickLoginButton();
            loginPage.enterEmail();
            loginPage.clickContinue();
            loginPage.enterPassword();
            loginPage.clickSubmit();
            loginPage.selectStayConnectedNo();
        } catch (Exception e) {
            System.out.println("Login steps error: " + e.getMessage());
        }

        boolean dashboardVisible = waitForDashboard(driver, TIMEOUT_SECONDS);
        assertTrue(dashboardVisible, "Dashboard not displayed after login");

        isLoggedIn = true;
        System.out.println("Login successful");
    }

    @After
    public void tearDown() {
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
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[contains(@class,'q-drawer')] | //a[contains(@href,'/entities')]")
            ));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }
}