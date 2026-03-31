package org.automation.pages;

import org.automation.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class CompanyCategoryPage extends BasePage {

    private By entitiesMenu = By.xpath("//div[contains(@class,'q-item') and .//div[normalize-space()='Entités']]");
    private By companyCategoriesSubMenu = By.cssSelector("a[href='/entities/company-sections']");
    private By addCategoryButton = By.xpath("//button[.//span[contains(.,'entreprise')]]");
    private By categoryNameInput = By.xpath("(//div[normalize-space(.)='Nom']/following-sibling::label//input)[1]");
    private By categoryCodeInput = By.xpath("(//div[normalize-space(.)='Code']/following-sibling::label//input)[1]");
    private By searchCompanyLink = By.xpath("//span[contains(.,'ajouter une entreprise')]");
    private By openNameInput        = By.id("add-company-id");
    private By openIdInput          = By.id("add-company-open-id");
    private By companyNameInput     = By.id("add-company-name");
    private By environmentNameInput = By.id("add-company-environment-name");
    private By sirenNumberInput     = By.id("add-company-identification-number");
    private By searchButton         = By.xpath("//button[.//i[contains(@class,'icon-search')]]");
    private By realResultCard = By.xpath(
            "//div[contains(@class,'card-container') and contains(@class,'cursor-pointer')" +
                    " and (.//div[contains(.,'entreprise :')] or .//div[contains(.,'ID de')])]"
    );
    private By saveCategoryButton = By.xpath("//button[.//span[text()='Enregistrer']]");
    private final By successToast = By.xpath("//div[contains(@class,'q-notification')]");
    private By validationAlert = By.xpath("//div[@role='alert' and normalize-space(.) != '']");
    private By editCompanyIcon = By.xpath("//i[@class='q-icon notranslate material-icons q-mr-md cursor-pointer text-link']");
    private By editSuccessToast = By.xpath("//div[contains(@class,'q-notification') and contains(.,'modifiée')]");
    private By searchResultCard = By.xpath("//div[contains(@class,'card-container')]//div[contains(@class,'column')]");
    private By selectCompanyButton = By.xpath("//button[.//span[contains(normalize-space(),'lectionner')]]");

    public CompanyCategoryPage() {
        super();
    }

    public String getCategoryName() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(categoryNameInput))
                .getAttribute("value").trim();
    }

    public void goToCategoryPage() throws InterruptedException {
        List<WebElement> backdrops = driver.findElements(By.cssSelector(".q-dialog__backdrop"));
        if (!backdrops.isEmpty() && backdrops.get(0).isDisplayed()) {
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
            wait.until(ExpectedConditions.invisibilityOf(backdrops.get(0)));
            Thread.sleep(500);
        }
        List<WebElement> subMenu = driver.findElements(companyCategoriesSubMenu);
        if (subMenu.isEmpty() || !subMenu.get(0).isDisplayed()) {
            clickOnEntityMenu();
        }
        clickOnSubMenu();
        Thread.sleep(800);
    }

    private void clickOnEntityMenu() throws InterruptedException {
        wait.until(ExpectedConditions.visibilityOfElementLocated(entitiesMenu)).click();
        Thread.sleep(800);
    }

    private void clickOnSubMenu() {
        wait.until(ExpectedConditions.elementToBeClickable(companyCategoriesSubMenu)).click();
    }

    public void clickAddCategory() {
        wait.until(ExpectedConditions.elementToBeClickable(addCategoryButton)).click();
    }

    public void setCategoryName(String name) {
        WebElement field = wait.until(ExpectedConditions.elementToBeClickable(categoryNameInput));
        field.click();
        field.sendKeys(Keys.CONTROL + "a");
        field.sendKeys(Keys.DELETE);
        field.sendKeys(name);
    }

    public void setCategoryCode(String code) {
        WebElement field = wait.until(ExpectedConditions.elementToBeClickable(categoryCodeInput));
        field.click();
        field.sendKeys(Keys.CONTROL + "a");
        field.sendKeys(Keys.DELETE);
        field.sendKeys(code);
    }

    public void openCompanySearch() {
        wait.until(ExpectedConditions.elementToBeClickable(searchCompanyLink)).click();
    }

    private void searchByField(By fieldLocator, String value) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(fieldLocator));
        field.clear();
        field.sendKeys(value);
        wait.until(ExpectedConditions.elementToBeClickable(searchButton)).click();
    }

    public void searchByCompanyName(String name)        { searchByField(companyNameInput, name); }
    public void searchByOpenId(String id)               { searchByField(openIdInput, id); }
    public void searchBySiren(String siren)             { searchByField(sirenNumberInput, siren); }
    public void searchByOpenName(String openName)       { searchByField(openNameInput, openName); }
    public void searchByEnvironmentName(String envName) { searchByField(environmentNameInput, envName); }

    public void selectCompanyByCriteria(String value) throws InterruptedException {
        Actions actions = new Actions(driver);
        actions.moveToElement(
                wait.until(ExpectedConditions.visibilityOfElementLocated(searchResultCard))
        ).click().perform();
        Thread.sleep(500);
        actions.moveToElement(
                wait.until(ExpectedConditions.elementToBeClickable(selectCompanyButton))
        ).click().perform();
    }

    public void saveCategory() {
        wait.until(ExpectedConditions.elementToBeClickable(saveCategoryButton)).click();
    }

    public String getSuccessMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(successToast))
                .getText().trim();
    }

    public boolean isEditSuccessMessageDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(editSuccessToast));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getErrorMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(validationAlert))
                .getText().trim();
    }

    public boolean isNoResultsMessageDisplayed() {
        return driver.findElements(realResultCard).isEmpty();
    }

    public boolean isCompanySelectionDisabled() {
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(selectCompanyButton));
        String disabled = btn.getAttribute("disabled");
        String ariaDisabled = btn.getAttribute("aria-disabled");
        String classAttr = btn.getAttribute("class");
        return disabled != null
                || "true".equals(ariaDisabled)
                || (classAttr != null && classAttr.contains("disabled"));
    }

    public void changeAssociatedCompany(String newCompanyValue) throws InterruptedException {
        wait.until(ExpectedConditions.visibilityOfElementLocated(categoryNameInput));
        wait.until(ExpectedConditions.elementToBeClickable(editCompanyIcon)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(companyNameInput));
        searchByCompanyName(newCompanyValue);
        selectCompanyByCriteria(newCompanyValue);
    }
}
