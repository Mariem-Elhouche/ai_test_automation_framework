package org.automation.pages;

import org.automation.base.BasePage;
import org.automation.utils.ConfigLoader;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage extends BasePage {

    // Sélecteurs (tous basés sur les IDs réels)
    private By loginButton = By.id("login-submit-btn-connect-to-application");
    private By emailInput = By.id("i0116");
    private By continueButton = By.id("idSIButton9");      // Bouton "Suivant" après email
    private By passwordInput = By.id("i0118");
    private By submitButton = By.id("idSIButton9");        // Bouton "Se connecter" après mot de passe (même ID)
    private By stayConnectedNo = By.id("idBtn_Back");      // Bouton "Non" sur la page "Rester connecté ?"
    private By dashboardMenu = By.cssSelector("aside.q-drawer");

    public void navigateToLoginPage() {
        navigateTo(ConfigLoader.getProperty("backoffice.url", "https://stg-bo.noveocare.com/login"));
    }

    public void clickLoginButton() {
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();

        // Attendre l'ouverture éventuelle d'une nouvelle fenêtre (max 5 secondes)
        try {
            wait.withTimeout(Duration.ofSeconds(5))
                    .until(ExpectedConditions.numberOfWindowsToBe(2));
            // Basculer vers la nouvelle fenêtre
            String mainWindow = driver.getWindowHandle();
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(mainWindow)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }
        } catch (TimeoutException e) {
            // Pas de nouvelle fenêtre, on reste sur la même
        }

        // Attendre que le champ email soit présent (dans la fenêtre active)
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
    }

    public void enterEmail() {
        // Attendre que le champ email soit visible avant de saisir
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput))
                .sendKeys(ConfigLoader.getProperty("backoffice.user.email", "mariem.elhouche-ext@noveocare.com"));
    }

    public void clickContinue() {
        // Attendre que le bouton "Suivant" soit cliquable
        wait.until(ExpectedConditions.elementToBeClickable(continueButton)).click();
    }

    public void enterPassword() {
        // Attendre que le champ mot de passe soit visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput))
                .sendKeys(ConfigLoader.getProperty("backoffice.user.password", "Noveocare.2026**"));
    }

    public void clickSubmit() {
        // Attendre que le bouton "Se connecter" soit cliquable
        // Note : il a le même ID que continueButton, mais on peut attendre que son texte soit "Se connecter"
        // Pour plus de robustesse, on pourrait utiliser un XPath avec le texte, mais ici on suppose que le bouton est présent.
        wait.until(ExpectedConditions.elementToBeClickable(submitButton)).click();
    }

    public void selectStayConnectedNo() {
        // Attendre que le bouton "Non" soit cliquable
        wait.until(ExpectedConditions.elementToBeClickable(stayConnectedNo)).click();
    }
    public boolean isDashboardDisplayed() {
        try {
            WebDriverWait dashboardWait = new WebDriverWait(driver, Duration.ofSeconds(30));

            return dashboardWait.until(
                    ExpectedConditions.visibilityOfElementLocated(dashboardMenu)
            ).isDisplayed();

        } catch (Exception e) {
            return false;
        }
    }
}