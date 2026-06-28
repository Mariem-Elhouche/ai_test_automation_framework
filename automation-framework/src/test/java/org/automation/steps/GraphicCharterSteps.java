package org.automation.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.automation.factory.DriverFactory;
import org.automation.pages.graphicchart.GraphicCharterPage;
import org.openqa.selenium.WebDriver;

public class GraphicCharterSteps {

    private GraphicCharterPage page;

    private GraphicCharterPage getPage() {
        if (page == null) {
            page = new GraphicCharterPage();
        }
        return page;
    }

    private WebDriver getDriver() {
        return DriverFactory.getOrInitDriver();
    }

    @Given("the user navigates to the graphic charter page")
    public void navigateToGraphicCharterPage() {
        WebDriver driver = getDriver();
        driver.manage().window().maximize();
        driver.get("https://stg-bo.noveocare.com/entities/environment/new");
    }

    @When("the user clicks on add graphic charter button")
    public void clickAddGraphicCharter() {
        getPage().clickAddCharter();
    }

    @And("the user fills in the charter name {string}")
    public void fillCharterName(String name) {
        getPage().setName(name + "_" + System.currentTimeMillis());
    }

    @And("the user uploads the logo principal {string}")
    public void uploadLogoPrincipal(String path) { getPage().uploadLogoPrincipal(path); }

    @And("the user uploads the logo secondaire {string}")
    public void uploadLogoSecondaire(String path) { getPage().uploadLogoSecondaire(path); }

    @And("the user uploads the favicon {string}")
    public void uploadFavicon(String path) { getPage().uploadFavicon(path); }

    @And("the user uploads the image espace assure {string}")
    public void uploadImageEspaceAssure(String path) { getPage().uploadImageEspaceAssure(path); }

    @And("the user uploads the image affiliation {string}")
    public void uploadImageAffiliation(String path) { getPage().uploadImageAffiliation(path); }

    @And("the user uploads the image backoffice {string}")
    public void uploadImageBackoffice(String path) { getPage().uploadImageBackoffice(path); }

    @And("the user uploads the image web rh {string}")
    public void uploadImageWebRH(String path) { getPage().uploadImageWebRH(path); }

    @And("the user uploads the image home header {string}")
    public void uploadImageHomeHeader(String path) { getPage().uploadImageHomeHeader(path); }

    @And("the user uploads the font text {string}")
    public void uploadFontText(String path) { getPage().uploadFontText(path); }

    @And("the user fills in font text name {string}")
    public void fillFontTextName(String name) { getPage().fillFontTextName(name); }

    @And("the user uploads the font title {string}")
    public void uploadFontTitle(String path) { getPage().uploadFontTitle(path); }

    @And("the user fills in font title name {string}")
    public void fillFontTitleName(String name) { getPage().fillFontTitleName(name); }

    @And("the user selects primary color {string}")
    public void selectPrimaryColor(String hex) { getPage().selectPrimaryColor(hex); }

    @And("the user selects secondary color {string}")
    public void selectSecondaryColor(String hex) { getPage().selectSecondaryColor(hex); }

    @And("the user selects primary color bold {string}")
    public void selectPrimaryBoldColor(String hex) { getPage().selectPrimaryBoldColor(hex); }

    @And("the user selects primary background color {string}")
    public void selectPrimaryBackgroundColor(String hex) { getPage().selectPrimaryBackgroundColor(hex); }

    @And("the user selects secondary bold color {string}")
    public void selectSecondaryBoldColor(String hex) { getPage().selectSecondaryBoldColor(hex); }

    @And("the user selects secondary background color {string}")
    public void selectSecondaryBackgroundColor(String hex) { getPage().selectSecondaryBackgroundColor(hex); }

    @And("the user fills in small border radius {string}")
    public void fillSmallBorderRadius(String value) { getPage().fillSmallBorderRadius(value); }

    @And("the user fills in medium border radius {string}")
    public void fillMediumBorderRadius(String value) { getPage().fillMediumBorderRadius(value); }

    @And("the user fills in large border radius {string}")
    public void fillLargeBorderRadius(String value) { getPage().fillLargeBorderRadius(value); }

    @And("the user fills in description {string}")
    public void fillDescription(String desc) { getPage().fillDescription(desc); }

    @And("the user fills in keywords {string}")
    public void fillKeywords(String keywords) { getPage().fillKeywords(keywords); }

    @And("the user saves the charter")
    public void saveCharter() { getPage().save(); }

    @Then("a charter creation success message is displayed")
    public void verifyCharterCreationSuccess() {
        if (!getPage().isCharterCreationSuccessDisplayed()) {
            throw new AssertionError("Le message de creation de charte n'a pas ete affiche");
        }
    }
    @When("the user clears the graphic charter {word}")
    public void clearGraphicCharterOneWord(String w1) { clearGraphicCharterField(w1); }

    @When("the user clears the graphic charter {word} {word}")
    public void clearGraphicCharterTwoWords(String w1, String w2) { clearGraphicCharterField(w1 + " " + w2); }

    @When("the user clears the graphic charter {word} {word} {word}")
    public void clearGraphicCharterThreeWords(String w1, String w2, String w3) {
        clearGraphicCharterField(w1 + " " + w2 + " " + w3);
    }

    @When("the user clears the graphic charter {word} {word} {word} {word}")
    public void clearGraphicCharterFourWords(String w1, String w2, String w3, String w4) {
        clearGraphicCharterField(w1 + " " + w2 + " " + w3 + " " + w4);
    }

    private void clearGraphicCharterField(String action) {
        switch (action.trim().toLowerCase()) {
            case "charter name":
            case "font text name":
            case "font title name":
            case "description":
            case "small border radius":
            case "medium border radius":
            case "large border radius":
                getPage().clearAndBlurField(action);
                break;
            case "logo principal":
            case "favicon":
                // Champs fichier vides par défaut : la validation est déclenchée au save.
                break;
            default:
                throw new IllegalArgumentException("Action de clear inconnue: '" + action + "'");
        }
    }

    @When("the user enters invalid color {string} in the graphic charter field {string}")
    public void enterInvalidColorField(String value, String action) {
        enterInvalidColor(value, action);
    }

    private void enterInvalidColor(String value, String action) {
        String normalized = action.trim().toLowerCase();
        switch (normalized) {
            case "primary color":
                getPage().enterAndBlurColorField("primary color", value);
                break;
            case "secondary color":
                getPage().enterAndBlurColorField("secondary color", value);
                break;
            case "primary background color":
                getPage().enterAndBlurColorField("primary background color", value);
                break;
            case "secondary background color":
                getPage().enterAndBlurColorField("secondary background color", value);
                break;
            default:
                throw new IllegalArgumentException("Action couleur inconnue: '" + action + "'");
        }
    }

    @Then("the field validation message {string} is displayed for graphic charter field {string}")
    public void verifyGraphicCharterFieldValidationMessage(String expectedMessage, String fieldLabel) {
        if (!getPage().isFieldValidationMessageDisplayed(fieldLabel, expectedMessage)) {
            throw new AssertionError(
                    "Message invalide pour le champ '" + fieldLabel + "'. Attendu: '"
                            + expectedMessage + "', Obtenu: '" + getPage().getFieldValidationMessage(fieldLabel) + "'"
            );
        }
    }

    @And("the user clicks {string} on {string}")
    public void clickActionOnElement(String action, String elementName) {
        getPage().clickActionOnElement(elementName, action);
    }
}
