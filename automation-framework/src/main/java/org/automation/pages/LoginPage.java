package org.automation.pages;

import org.automation.base.BasePage;
import org.automation.utils.ConfigLoader;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.nio.file.Files;
import java.time.Duration;

public class LoginPage extends BasePage {

    private By loginButton = By.id("login-submit-btn-connect-to-application-broken");
    private By emailInput = By.name("loginfmt");
    private By continueButton = By.id("idSIButton9");
    private By passwordInput = By.name("passwd");
    private By submitButton = By.id("idSIButton9");
    private By stayConnectedNo = By.id("idBtn_Back");
    private By dashboardMenu = By.xpath("//*[contains(.,'Entités')]");
    private static final int LOGIN_CLICK_TIMEOUT_SECONDS = 3;

    public void navigateToLoginPage() {
        navigateTo(ConfigLoader.getProperty("backoffice.url", "https://stg-bo.noveocare.com/login"));
    }

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
        String email = ConfigLoader.getProperty("backoffice.user.email", "");
        if (email.isEmpty()) throw new IllegalStateException("backoffice.user.email is not configured in config.properties");
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput))
                .sendKeys(email);
    }

    public void clickContinue() {
        wait.until(ExpectedConditions.elementToBeClickable(continueButton)).click();
    }

    public void enterPassword() {
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        WebElement pwdField = longWait.until(ExpectedConditions.elementToBeClickable(passwordInput));
        System.out.println("[DEBUG] Password field found. Tag: " + pwdField.getTagName() + " Type: " + pwdField.getAttribute("type"));
        String password = ConfigLoader.getProperty("backoffice.user.password", "");
        if (password.isEmpty()) throw new IllegalStateException("backoffice.user.password is not configured in config.properties");
        pwdField.click();
        pwdField.clear();
        pwdField.sendKeys(password);
        try { Thread.sleep(500); } catch (InterruptedException ignored) { }
        String entered = pwdField.getAttribute("value");
        System.out.println("[DEBUG] Password field value after sendKeys: '" + entered + "' (length: " + (entered != null ? entered.length() : 0) + ")");
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
                    ExpectedConditions.and(
                            ExpectedConditions.urlContains("stg-bo.noveocare.com"),
                            ExpectedConditions.not(ExpectedConditions.urlContains("/login"))
                    )
            );

        } catch (Exception e) {
            System.out.println("[DEBUG] Dashboard URL check failed. Current URL: " + driver.getCurrentUrl());
            System.out.println("[DEBUG] Page title: " + driver.getTitle());
            try {
                File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                Files.copy(screenshot.toPath(), new File("target/dashboard-failure.png").toPath());
                System.out.println("[DEBUG] Screenshot saved to target/dashboard-failure.png");
            } catch (Exception ignored) { }
            return false;
        }
    }
}
