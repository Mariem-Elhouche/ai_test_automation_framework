package org.automation.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.automation.pages.LoginPage;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginSteps {

    private LoginPage loginPage;

    private LoginPage getLoginPage() {
        if (loginPage == null) {
            loginPage = new LoginPage();
        }
        return loginPage;
    }

    @Given("the user navigates to the login page")
    public void navigate_to_login_page() {
        getLoginPage().navigateToLoginPage();
    }

    @When("the user clicks {string}")
    public void click_login_button(String buttonText) {
        getLoginPage().clickLoginButton();
    }

    @When("the user enters a valid email")
    public void enter_valid_email() {
        getLoginPage().enterEmail();
    }

    @When("the user clicks the continue button")
    public void click_continue_button() {
        getLoginPage().clickContinue();
    }

    @When("the user enters a valid password")
    public void enter_valid_password() {
        getLoginPage().enterPassword();
    }

    @When("the user clicks the login button")
    public void click_submit_button() {
        getLoginPage().clickSubmit();
    }

    @When("the user selects {string} on the {string} page")
    public void select_option_on_page(String option, String pageName) {
        if (option.equalsIgnoreCase("Non")) {
            getLoginPage().selectStayConnectedNo();
        }
    }

    @Then("the user is redirected to the dashboard")
    public void verify_dashboard_redirect() {
        assertTrue(getLoginPage().isDashboardDisplayed(), "Le dashboard n'est pas affiche");
        System.out.println("URL apres login : " + getLoginPage().getCurrentUrl());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
