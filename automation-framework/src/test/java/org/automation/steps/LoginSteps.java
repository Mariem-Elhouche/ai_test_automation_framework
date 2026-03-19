package org.automation.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.automation.pages.LoginPage;
import org.automation.base.BasePage;
import org.openqa.selenium.WebDriver;
import org.automation.factory.DriverFactory;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginSteps {

   private LoginPage loginPage = new LoginPage();

    // --------------------------
    // Gherkin Steps
    // --------------------------

    @Given("the user navigates to the login page")
    public void navigate_to_login_page() {
        loginPage.navigateToLoginPage();
    }

    @When("the user clicks {string}")
    public void click_login_button(String buttonText) {
        try {
            Thread.sleep(5000); // temporaire
        } catch (InterruptedException e) {
        }
        loginPage.clickLoginButton();
    }

    @When("the user enters a valid email")
    public void enter_valid_email() {
        loginPage.enterEmail();
    }

    @When("the user clicks the continue button")
    public void click_continue_button() {
        loginPage.clickContinue();
    }

    @When("the user enters a valid password")
    public void enter_valid_password() {
        loginPage.enterPassword();
    }

    @When("the user clicks the login button")
    public void click_submit_button() {
        loginPage.clickSubmit();
    }

    @When("the user selects {string} on the {string} page")
    public void select_option_on_page(String option, String pageName) {
        // Ici on ne teste que "Non" sur "Rester connecté ?" page
        if (option.equalsIgnoreCase("Non")) {
            loginPage.selectStayConnectedNo();
        }
    }

    @Then("the user is redirected to the dashboard")
    public void verify_dashboard_redirect() {
        assertTrue(loginPage.isDashboardDisplayed(), "Le dashboard n'est pas affiché");
        System.out.println("URL après login : " + loginPage.getCurrentUrl());
        try {
            Thread.sleep(5000); // Pause de 5 secondes pour observer
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}