package org.automation.pages.companyCategory;

import org.automation.base.BasePage;
import org.automation.pages.document.DocumentListPage;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.util.List;
import java.util.logging.Logger;

public class CompanyCategoryDocumentPage extends BasePage implements DocumentListPage {

    private static final Logger LOGGER = Logger.getLogger(CompanyCategoryDocumentPage.class.getName());

    private final By documentsTab  = By.xpath("//div[@class='q-tab__label'][normalize-space()='Documents']");
    private final By activeDocTab  = By.xpath("//div[contains(@class,'q-tab--active')]//div[@class='q-tab__label'][normalize-space()='Documents']");
    private final By confirmDialog = By.xpath("//div[@role='dialog']");
    private final By confirmBtn    = By.xpath("//div[@role='dialog']//button[contains(.,'Confirmer')]");
    private final By cancelBtn     = By.xpath("//div[@role='dialog']//button[contains(.,'Annuler')]");
    private final By successToast  = By.xpath("//div[contains(@class,'q-notification__message')]");
    private final By anyDocCard    = By.xpath("//div[contains(@class,'card-container')]");
    private final By anyNoDocMsg   = By.xpath("//div[contains(@class,'text-center') and contains(.,'Aucun document')]");

    private static final String EYE_ICON    = ".//i[contains(@class,'icon-eye')]";
    private static final String DELETE_ICON = ".//i[contains(@class,'material-icons') and normalize-space()='delete']";
    private static final String CARD        = ".//div[contains(@class,'card-container')]";
    private static final String LOAD_BTN    = ".//div[contains(@class,'cursor-pointer') and (contains(.,'Afficher') or contains(.,'charger') or contains(.,'Charger'))]";
    private static final String NO_DOC_MSG  = ".//div[contains(@class,'text-center') and contains(.,'Aucun document')]";

    private static final String SEC_ENVIRONNEMENT = "//span[contains(.,'environment')]/ancestor::div[contains(@class,'col-10')][1]/following-sibling::div[1]";
    private static final String SEC_GROUPES       = "//span[contains(.,'groupes de cet')]/ancestor::div[contains(@class,'col-10')][1]/following-sibling::div[1]";
    private static final String SEC_ENTREPRISES   = "//span[contains(.,'entreprises de cet')]/ancestor::div[contains(@class,'col-10')][1]/following-sibling::div[1]";
    private static final String SEC_CATEGORIES    = "//span[contains(.,'la cat')]/ancestor::div[contains(@class,'col-10')][1]/following-sibling::div[1]";

    public CompanyCategoryDocumentPage() { super(); }

    private String getSectionXpath(String sectionName) {
        switch (sectionName.toLowerCase()) {
            case "environnement": return SEC_ENVIRONNEMENT;
            case "groupes":       return SEC_GROUPES;
            case "entreprises":   return SEC_ENTREPRISES;
            case "catégories":
            case "categories":    return SEC_CATEGORIES;
            default: throw new IllegalArgumentException("Section inconnue : " + sectionName);
        }
    }

    private WebElement getSectionContent(String sectionName) {
        return driver.findElement(By.xpath(getSectionXpath(sectionName)));
    }

    public List<WebElement> getCardsInSection(String sectionName) {
        WebElement content = getSectionContent(sectionName);
        List<WebElement> cards = content.findElements(By.xpath(CARD));
        if (!cards.isEmpty()) return cards;
        List<WebElement> btns = content.findElements(By.xpath(LOAD_BTN));
        if (!btns.isEmpty() && btns.get(0).isDisplayed()) {
            clickElement(btns.get(0));
            waitForDocumentsToLoad();
            content = getSectionContent(sectionName);
            cards   = content.findElements(By.xpath(CARD));
        }
        return cards;
    }

    public void clickDocumentsTab() {
        scrollAndClick(documentsTab, "Onglet Documents");
        try { wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h2[contains(.,'Documents rattach')]"))); }
        catch (Exception e) { LOGGER.warning("Tab load: " + e.getMessage()); }
    }

    public boolean isDocumentsTabActive() {
        try { wait.until(ExpectedConditions.visibilityOfElementLocated(activeDocTab)); return true; }
        catch (Exception e) { return false; }
    }

    public boolean isSectionTitleDisplayed(String expectedTitle) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//h2[contains(.," + xpathSafeString(expectedTitle) + ")]")));
            return true;
        } catch (Exception e) { return false; }
    }

    public boolean isSectionLabelPresent(String sectionName) {
        try { getSectionContent(sectionName); return true; }
        catch (NoSuchElementException e) { return false; }
    }

    public void clickShowGroups()     { getCardsInSection("groupes"); }
    public void clickShowCompanies()  { getCardsInSection("entreprises"); }
    public void clickShowCategories() { getCardsInSection("catégories"); }

    public boolean areDocumentsDisplayedInSection(String sectionName) {
        return !getCardsInSection(sectionName).isEmpty();
    }

    public boolean isNoDocumentMessageDisplayedInSection(String sectionName) {
        if (!getCardsInSection(sectionName).isEmpty()) return false;
        try {
            WebElement content = getSectionContent(sectionName);
            List<WebElement> msgs = content.findElements(By.xpath(NO_DOC_MSG));
            if (msgs.isEmpty()) msgs = driver.findElements(anyNoDocMsg);
            return !msgs.isEmpty() && msgs.get(0).isDisplayed();
        } catch (Exception e) { return false; }
    }

    public int getDocumentCountInSection(String sectionName) {
        return getCardsInSection(sectionName).size();
    }

    public boolean allCardsInSectionHaveEyeIcon(String sectionName) {
        List<WebElement> cards = getCardsInSection(sectionName);
        if (cards.isEmpty()) return false;
        return cards.stream().allMatch(c -> !c.findElements(By.xpath(EYE_ICON)).isEmpty());
    }

    public boolean allCardsInSectionHaveDeleteIcon(String sectionName) {
        List<WebElement> cards = getCardsInSection(sectionName);
        if (cards.isEmpty()) return false;
        return cards.stream().allMatch(c -> !c.findElements(By.xpath(DELETE_ICON)).isEmpty());
    }

    public boolean noDeleteIconInSection(String sectionName) {
        List<WebElement> cards = getCardsInSection(sectionName);
        return cards.isEmpty() || cards.stream().allMatch(c -> c.findElements(By.xpath(DELETE_ICON)).isEmpty());
    }

    public boolean noEyeIconInSection(String sectionName) {
        List<WebElement> cards = getCardsInSection(sectionName);
        return cards.isEmpty() || cards.stream().allMatch(c -> c.findElements(By.xpath(EYE_ICON)).isEmpty());
    }

    public void clickEyeIconOnFirstDocumentInSection(String sectionName) {
        List<WebElement> cards = getCardsInSection(sectionName);
        if (cards.isEmpty()) throw new RuntimeException("Aucune carte dans : " + sectionName);
        clickElement(cards.get(0).findElement(By.xpath(EYE_ICON)));
        sleep(1000);
    }

    public boolean isOnEnvironmentDetailPage() {
        return driver.getCurrentUrl().contains("/entities/environment");
    }

    public boolean isOnCompanyGroupDetailPage() {
        return driver.getCurrentUrl().contains("/entities/company-group");
    }

    public void deleteFirstDocumentInSection(String sectionName) {
        List<WebElement> cards = getCardsInSection(sectionName);
        if (cards.isEmpty()) throw new RuntimeException("Aucune carte dans : " + sectionName);
        clickElement(cards.get(0).findElement(By.xpath(DELETE_ICON)));
    }

    public void deleteFirstDocument() {
        clickElement(driver.findElement(By.xpath(
                "(//div[contains(@class,'card-container')]//i[contains(@class,'material-icons') and normalize-space()='delete'])[1]")));
    }

    public boolean isDeleteConfirmDialogDisplayed() {
        try { wait.until(ExpectedConditions.visibilityOfElementLocated(confirmDialog)); return true; }
        catch (Exception e) { return false; }
    }

    public void confirmDeletion() {
        scrollAndClick(confirmBtn, "Confirmer");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(confirmDialog));
        sleep(500);
    }

    public void cancelDeletion() {
        scrollAndClick(cancelBtn, "Annuler");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(confirmDialog));
        sleep(300);
    }

    public boolean isDocumentDeletedSuccessfully() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(successToast));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(successToast));
            return true;
        } catch (Exception e) { return false; }
    }

    public void logAllSectionSpans() {
        driver.findElements(By.xpath("//div[contains(@class,'col-10')]//span"))
                .forEach(s -> LOGGER.info("SPAN: [" + s.getText() + "]"));
    }

    private void clickElement(WebElement el) {
        try { new Actions(driver).moveToElement(el).perform(); el.click(); }
        catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }

    private void scrollAndClick(By locator, String label) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
                new Actions(driver).moveToElement(el).perform();
                try { el.click(); }
                catch (ElementClickInterceptedException e) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
                }
                LOGGER.info(label + " cliqué");
                return;
            } catch (StaleElementReferenceException e) { attempts++; sleep(300); }
        }
        throw new RuntimeException("scrollAndClick échoué : " + label);
    }

    private void waitForDocumentsToLoad() {
        sleep(800);
        try { wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(anyDocCard),
                ExpectedConditions.presenceOfElementLocated(anyNoDocMsg)));
        } catch (Exception e) { LOGGER.warning("waitForDocumentsToLoad: " + e.getMessage()); }
    }

    private String xpathSafeString(String value) {
        if (!value.contains("'")) return "'" + value + "'";
        StringBuilder sb = new StringBuilder("concat(");
        String[] parts = value.split("'", -1);
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(", \"'\", ");
            sb.append("'").append(parts[i]).append("'");
        }
        sb.append(")");
        return sb.toString();
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }
}
