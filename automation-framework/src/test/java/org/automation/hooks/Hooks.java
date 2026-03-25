package org.automation.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
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

    private static final int TIMEOUT_SECONDS = 15;
    private static final int POLLING_MS = 500;

    @Before(order = 0)
    public void setupDriver() {
        if (DriverFactory.getDriver() != null) {
            try { DriverFactory.quitDriver(); } catch (Exception ignored) {}
        }
        DriverFactory.initDriver();
    }

    @Before(value = "@requiresLogin", order = 1)
    public void loginBeforeScenario() {
        WebDriver driver = DriverFactory.getDriver();
        LoginPage loginPage = new LoginPage();

        // Vérifier si déjà connecté (dashboard visible)
        if (isDashboardDisplayed(driver)) {
            System.out.println("✅ Session active — login ignoré");
            return;
        }

        // Naviguer vers la page de login
        loginPage.navigateToLoginPage();

        // Attendre que la page de login soit chargée (bouton "Se connecter" présent)
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("i0116"))); // champ email
            System.out.println("✅ Page de login chargée");
        } catch (TimeoutException e) {
            // Si pas de champ email, peut-être déjà sur le dashboard après navigation
            if (isDashboardDisplayed(driver)) {
                System.out.println("✅ Déjà connecté après navigation — login ignoré");
                return;
            }
            // Sinon, on continue (peut-être autre page)
        }

        // Tentative de login standard
        try {
            loginPage.clickLoginButton(); // bouton "Se connecter"
            loginPage.enterEmail();
            loginPage.clickContinue();
            loginPage.enterPassword();
            loginPage.clickSubmit();
            loginPage.selectStayConnectedNo(); // clic sur "Non" pour la session persistante
        } catch (Exception e) {
            System.out.println("⚠️ Erreur pendant les étapes de login : " + e.getMessage());
        }

        // Attendre que le dashboard soit visible (plus long timeout)
        boolean dashboardVisible = waitForDashboard(driver, TIMEOUT_SECONDS);
        assertTrue(dashboardVisible, "Dashboard non affiché après login");
        System.out.println("✅ Login réussi");
    }

    @After
    public void tearDown() {
        DriverFactory.quitDriver();
    }

    /**
     * Vérifie si le dashboard est déjà affiché (ex: menu latéral présent)
     */
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

    /**
     * Attend que le dashboard apparaisse avec un timeout donné.
     */
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