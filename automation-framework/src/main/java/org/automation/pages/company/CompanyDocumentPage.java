package org.automation.pages.company;

import org.automation.base.BasePage;
import org.automation.pages.document.DocumentListPage;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class CompanyDocumentPage extends BasePage implements DocumentListPage {

    private static final Logger LOGGER = Logger.getLogger(CompanyDocumentPage.class.getName());

    private final By documentsTab = By.xpath("//div[@class='q-tab__label'][normalize-space()='Documents']");
    private final By activeDocTab = By.xpath("//div[contains(@class,'q-tab--active')]//div[@class='q-tab__label'][normalize-space()='Documents']");
    private final By confirmDialog = By.xpath("//div[@role='dialog']");
    private final By confirmBtn = By.xpath("//div[@role='dialog']//button[contains(.,'Confirmer')]");
    private final By cancelBtn = By.xpath("//div[@role='dialog']//button[contains(.,'Annuler')]");
    private final By successToast = By.xpath("//div[contains(@class,'q-notification__message')]");
    private final By anyDocCard = By.xpath("//div[contains(@class,'card-container')]");
    private final By anyNoDocMsg = By.xpath("//div[contains(@class,'text-center') and contains(.,'Aucun document')]");

    private static final String EYE_ICON = ".//i[contains(@class,'icon-eye')]";
    private static final String DELETE_ICON = ".//i[contains(@class,'material-icons') and normalize-space()='delete']";
    private static final String CARD = ".//div[contains(@class,'card-container')]";
    private static final String LOAD_BTN = ".//div[contains(@class,'cursor-pointer') and (contains(.,'Afficher') or contains(.,'charger') or contains(.,'Charger'))]";
    private static final String GLOBAL_LOAD_MORE_BTN = "//div[contains(@class,'cursor-pointer') and contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'charger plus de documents')]";
    private static final String NO_DOC_MSG = ".//div[contains(@class,'text-center') and contains(.,'Aucun document')]";

    private static final String SEC_ENVIRONNEMENT = "//span[contains(.,'entreprise') and (contains(.,'environ') or contains(.,'envrion'))]/ancestor::div[contains(@class,'row') and contains(@class,'q-mt-md')][1]/following-sibling::div[contains(@class,'col-12')][1]";
    private static final String SEC_GROUPES = "//span[contains(.,'groupes de cette entreprise')]/ancestor::div[contains(@class,'row') and contains(@class,'q-mt-md')][1]/following-sibling::div[contains(@class,'col-12')][1]";
    private static final String SEC_ENTREPRISES = "//span[contains(.,'Rattach') and contains(.,'entreprise') and not(contains(.,'groupes')) and not(contains(.,'environ'))]/ancestor::div[contains(@class,'row') and contains(@class,'q-mt-md')][1]/following-sibling::div[contains(@class,'col-12')][1]";

    private String getSectionXpath(String sectionName) {
        switch (sectionName.toLowerCase()) {
            case "environnement":
                return SEC_ENVIRONNEMENT;
            case "groupes":
                return SEC_GROUPES;
            case "entreprises":
                return SEC_ENTREPRISES;
            default:
                throw new IllegalArgumentException("Section inconnue : " + sectionName);
        }
    }

    private WebElement getSectionContent(String sectionName) {
        waitForDocumentsToLoad();
        List<WebElement> sections = driver.findElements(By.xpath(getSectionXpath(sectionName)));
        if (!sections.isEmpty()) {
            return sections.get(0);
        }
        throw new NoSuchElementException("Section non trouvee: " + sectionName);
    }

    public List<WebElement> getCardsInSection(String sectionName) {
        try {
            WebElement content = getSectionContent(sectionName);
            List<WebElement> cards = content.findElements(By.xpath(CARD));
            if (!cards.isEmpty()) {
                return cards;
            }

            List<WebElement> btns = content.findElements(By.xpath(LOAD_BTN));
            if (!btns.isEmpty() && btns.get(0).isDisplayed()) {
                clickElement(btns.get(0));
                waitForDocumentsToLoad();
                content = getSectionContent(sectionName);
                cards = content.findElements(By.xpath(CARD));
            }
            return cards;
        } catch (NoSuchElementException e) {
            LOGGER.info("Section absente pour '" + sectionName + "' (etat vide probable)");
            return Collections.emptyList();
        }
    }

    @Override
    public void clickDocumentsTab() {
        scrollAndClick(documentsTab, "Onglet Documents");
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(.,'Documents rattach')]")));
        } catch (Exception e) {
            LOGGER.warning("Tab load: " + e.getMessage());
        }
    }

    @Override
    public boolean isDocumentsTabActive() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(activeDocTab));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isSectionTitleDisplayed(String expectedTitle) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(.," + xpathSafeString(expectedTitle) + ")]")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSectionLabelPresent(String sectionName) {
        try {
            getSectionContent(sectionName);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    @Override
    public void clickShowGroups() {
        getCardsInSection("groupes");
    }

    @Override
    public void clickShowCompanies() {
        getCardsInSection("entreprises");
    }

    @Override
    public void clickLoadMoreDocumentsInSection(String sectionName) {
        List<WebElement> buttons = driver.findElements(By.xpath(GLOBAL_LOAD_MORE_BTN));
        WebElement button = buttons.stream().filter(WebElement::isDisplayed).findFirst()
                .orElseThrow(() -> new NoSuchElementException("Bouton charger plus de documents introuvable pour section: " + sectionName));
        clickElement(button);
        waitForDocumentsToLoad();
    }

    @Override
    public boolean areDocumentsDisplayedInSection(String sectionName) {
        return !getCardsInSection(sectionName).isEmpty();
    }

    @Override
    public boolean isNoDocumentMessageDisplayedInSection(String sectionName) {
        if (!getCardsInSection(sectionName).isEmpty()) {
            return false;
        }
        List<WebElement> globalMsgs = driver.findElements(anyNoDocMsg);
        if (!globalMsgs.isEmpty() && globalMsgs.get(0).isDisplayed()) {
            return true;
        }
        try {
            WebElement content = getSectionContent(sectionName);
            List<WebElement> msgs = content.findElements(By.xpath(NO_DOC_MSG));
            return !msgs.isEmpty() && msgs.get(0).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int getDocumentCountInSection(String sectionName) {
        return getCardsInSection(sectionName).size();
    }

    @Override
    public boolean allCardsInSectionHaveEyeIcon(String sectionName) {
        List<WebElement> cards = getCardsInSection(sectionName);
        if (cards.isEmpty()) {
            return false;
        }
        return cards.stream().allMatch(c -> !c.findElements(By.xpath(EYE_ICON)).isEmpty());
    }

    @Override
    public boolean allCardsInSectionHaveDeleteIcon(String sectionName) {
        List<WebElement> cards = getCardsInSection(sectionName);
        if (cards.isEmpty()) {
            return false;
        }
        return cards.stream().allMatch(c -> !c.findElements(By.xpath(DELETE_ICON)).isEmpty());
    }

    @Override
    public boolean noDeleteIconInSection(String sectionName) {
        List<WebElement> cards = getCardsInSection(sectionName);
        return cards.isEmpty() || cards.stream().allMatch(c -> c.findElements(By.xpath(DELETE_ICON)).isEmpty());
    }

    @Override
    public boolean noEyeIconInSection(String sectionName) {
        List<WebElement> cards = getCardsInSection(sectionName);
        return cards.isEmpty() || cards.stream().allMatch(c -> c.findElements(By.xpath(EYE_ICON)).isEmpty());
    }

    @Override
    public void clickEyeIconOnFirstDocumentInSection(String sectionName) {
        List<WebElement> cards = getCardsInSection(sectionName);
        if (cards.isEmpty()) {
            throw new RuntimeException("Aucune carte dans : " + sectionName);
        }
        clickElement(cards.get(0).findElement(By.xpath(EYE_ICON)));
        sleep(1000);
    }

    @Override
    public boolean isOnEnvironmentDetailPage() {
        return driver.getCurrentUrl().contains("/entities/environment");
    }

    @Override
    public boolean isOnCompanyGroupDetailPage() {
        return driver.getCurrentUrl().contains("/entities/company-group");
    }

    @Override
    public void deleteFirstDocumentInSection(String sectionName) {
        List<WebElement> cards = getCardsInSection(sectionName);
        if (cards.isEmpty()) {
            throw new RuntimeException("Aucune carte dans : " + sectionName);
        }
        clickElement(cards.get(0).findElement(By.xpath(DELETE_ICON)));
    }

    @Override
    public void deleteFirstDocument() {
        clickElement(driver.findElement(By.xpath("(//div[contains(@class,'card-container')]//i[contains(@class,'material-icons') and normalize-space()='delete'])[1]")));
    }

    @Override
    public boolean isDeleteConfirmDialogDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(confirmDialog));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void confirmDeletion() {
        scrollAndClick(confirmBtn, "Confirmer");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(confirmDialog));
        sleep(500);
    }

    @Override
    public void cancelDeletion() {
        scrollAndClick(cancelBtn, "Annuler");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(confirmDialog));
        sleep(300);
    }

    @Override
    public boolean isDocumentDeletedSuccessfully() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(successToast));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(successToast));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void clickElement(WebElement el) {
        try {
            new Actions(driver).moveToElement(el).perform();
            el.click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }

    private void scrollAndClick(By locator, String label) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
                new Actions(driver).moveToElement(el).perform();
                try {
                    el.click();
                } catch (ElementClickInterceptedException e) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
                }
                LOGGER.info(label + " clique");
                return;
            } catch (StaleElementReferenceException e) {
                attempts++;
                sleep(300);
            }
        }
        throw new RuntimeException("scrollAndClick echoue : " + label);
    }

    private void waitForDocumentsToLoad() {
        sleep(800);
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(anyDocCard),
                    ExpectedConditions.presenceOfElementLocated(anyNoDocMsg)));
        } catch (Exception e) {
            LOGGER.warning("waitForDocumentsToLoad: " + e.getMessage());
        }
    }

    private String xpathSafeString(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        StringBuilder sb = new StringBuilder("concat(");
        String[] parts = value.split("'", -1);
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                sb.append(", \"'\", ");
            }
            sb.append("'").append(parts[i]).append("'");
        }
        sb.append(")");
        return sb.toString();
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
