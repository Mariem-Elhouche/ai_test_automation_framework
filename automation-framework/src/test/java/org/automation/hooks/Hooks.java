package org.automation.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.automation.factory.DriverFactory;
import org.automation.pages.LoginPage;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Hooks {

    @Before(order = 0)
    public void setupDriver() {
        if (DriverFactory.getDriver() != null) {
            try { DriverFactory.quitDriver(); } catch (Exception ignored) {}
        }
        DriverFactory.initDriver();
    }

    @Before(value = "@requiresLogin", order = 1)
    public void loginBeforeScenario() {
        LoginPage loginPage = new LoginPage();
        if (loginPage.isDashboardDisplayed()) {
            return; // Déjà connecté
        }
        loginPage.navigateToLoginPage();
        loginPage.clickLoginButton();
        loginPage.enterEmail();
        loginPage.clickContinue();
        loginPage.enterPassword();
        loginPage.clickSubmit();
        loginPage.selectStayConnectedNo();
        assertTrue(loginPage.isDashboardDisplayed(), "Dashboard non affiché après login");
    }

    @After
    public void tearDown() {
        DriverFactory.quitDriver();
    }
}