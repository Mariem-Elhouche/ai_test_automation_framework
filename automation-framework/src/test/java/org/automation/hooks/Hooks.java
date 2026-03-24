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

        // Cas 1 — déjà sur le dashboard sans navigation
        if (loginPage.isDashboardDisplayed()) {
            System.out.println("✅ Session active — login ignoré");
            return;
        }

        // Naviguer vers la page de login
        loginPage.navigateToLoginPage();

        // Cas 2 — après navigation, déjà connecté (session Microsoft active)
        if (loginPage.isDashboardDisplayed()) {
            System.out.println("✅ Déjà connecté après navigation — login ignoré");
            return;
        }

        // Cas 3 — login normal requis
        try {
            loginPage.clickLoginButton();
            loginPage.enterEmail();
            loginPage.clickContinue();
            loginPage.enterPassword();
            loginPage.clickSubmit();
            loginPage.selectStayConnectedNo();
        } catch (Exception e) {
            System.out.println("⚠️ Étape login ignorée (session peut-être active) : "
                    + e.getMessage());
        }

        assertTrue(
                loginPage.isDashboardDisplayed(),
                "Dashboard non affiché après login"
        );
        System.out.println("✅ Login réussi");
    }

    @After
    public void tearDown() {
        DriverFactory.quitDriver();
    }
}