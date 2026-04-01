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

    private By loginButton = By.id("login-submit-btn-connect-to-application");
    private By emailInput = By.id("i0116");   //id dynamiquen
    private By continueButton = By.id("idSIButton9");
    private By passwordInput = By.id("i0118");
    private By submitButton = By.id("idSIButton9");
    private By stayConnectedNo = By.id("idBtn_Back");
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

        wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
    }

    public void enterEmail() {
        // Attendre que le champ email soit visible avant de saisir
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput))
                .sendKeys(ConfigLoader.getProperty("backoffice.user.email", "mariem.elhouche-ext@noveocare.com"));
    }

    public void clickContinue() {
        wait.until(ExpectedConditions.elementToBeClickable(continueButton)).click();
    }

    public void enterPassword() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput))
                .sendKeys(ConfigLoader.getProperty("backoffice.user.password", "Noveocare.2026**"));
    }

    public void clickSubmit() {
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