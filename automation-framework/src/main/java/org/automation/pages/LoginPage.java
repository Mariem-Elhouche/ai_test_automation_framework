package org.automation.pages;

import org.automation.base.BasePage;
import org.automation.utils.ConfigLoader;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage extends BasePage {

    private By loginButton = By.id("login-submit-btn-connect-to-application-broken");
    private By emailInput = By.id("i0116");   //id dynamiquen
    private By continueButton = By.id("idSIButton9");
    private By passwordInput = By.id("i0118");
    private By submitButton = By.id("idSIButton9");
    private By stayConnectedNo = By.id("idBtn_Back");
    private By dashboardMenu = By.cssSelector("aside.q-drawer");
    private static final int LOGIN_CLICK_TIMEOUT_SECONDS = 3;

    public void navigateToLoginPage() {
        navigateTo(ConfigLoader.getProperty("backoffice.url", "https://stg-bo.noveocare.com/login"));
    }

//    public void clickLoginButton() {
//        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(LOGIN_CLICK_TIMEOUT_SECONDS));
//        shortWait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();
//        shortWait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
//    }


    public void clickLoginButton() {
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(LOGIN_CLICK_TIMEOUT_SECONDS));
        try {
            // Chemin normal (rapide/stable)
            shortWait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();
        } catch (TimeoutException | NoSuchElementException e) {
            // Fallback auto-healing
            WebElement healed = findElement(loginButton, "Se connecter avec mon compte NoveoCare", "button");
            loginButton = getRuntimeHealedLocator("Se connecter avec mon compte NoveoCare", loginButton);
            shortWait.until(ExpectedConditions.elementToBeClickable(healed)).click();
        }

        shortWait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
    }

    public boolean isEmailInputVisible(int timeoutSeconds) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            shortWait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
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
