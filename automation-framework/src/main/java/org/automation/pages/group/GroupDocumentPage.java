package org.automation.pages.group;

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

import java.util.List;
import java.util.logging.Logger;

public class GroupDocumentPage extends BasePage implements DocumentListPage {

    private static final Logger LOGGER = Logger.getLogger(GroupDocumentPage.class.getName());

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
    private static final String NO_DOC_MSG = ".//div[contains(@class,'text-center') and contains(.,'Aucun document')]";
    private static final String TEXT_CENTER = ".//div[contains(@class,'text-center')]";

    // Scope: evite de matcher des libelles dans le menu/sidebars; on se limite au panel Documents visible.
    private static final String DOC_ROOT = "//div[contains(@class,'q-tab-panel')][.//h2[contains(.,'Documents rattach')]]";

    // Sur la page Groupe, la section "environnement" est rendue dans un wrapper qui contient ensuite les autres sections.
    // On cible explicitement le 1er bloc de contenu (col-12) pour ne pas "voir" les cartes des autres sections.
    private static final String SEC_ENVIRONNEMENT =
            DOC_ROOT + "//span[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'environ') " +
                    "and contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'groupe')]" +
                    "/ancestor::div[contains(@class,'col-10')][1]/following-sibling::div[1]" +
                    "/div[contains(@class,'col-12')][1]";
    private static final String SEC_GROUPES =
            DOC_ROOT + "//span[normalize-space()='Rattach\u00e9 au groupe']/ancestor::div[contains(@class,'col-10')][1]" +
                    "/following-sibling::div[contains(@class,'col-12')][1]";
    private static final String SEC_ENTREPRISES =
            DOC_ROOT + "//span[" +
                    "contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'entreprises')" +
                    " and contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'groupe')" +
                    " and not(contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'cat'))" +
                    "]/ancestor::div[contains(@class,'col-10')][1]" +
                    "/following-sibling::div[contains(@class,'col-12')][1]";
    private static final String SEC_CATEGORIES =
            DOC_ROOT + "//span[" +
                    "contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'cat')" +
                    " and contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'groupe')" +
                    "]/ancestor::div[contains(@class,'col-10')][1]" +
                    "/following-sibling::div[contains(@class,'col-12')][1]";

    private String normalizeSectionKey(String sectionName) {
        if (sectionName == null) {
            return "";
        }
        String normalized = sectionName.toLowerCase().trim();
        // Tolere les problemes d'encodage courants dans les .feature (ex: "catÃ©gories").
        if (normalized.startsWith("cat")) {
            return "categories";
        }
        return normalized;
    }

    private String labelXpath(String sectionName) {
        switch (normalizeSectionKey(sectionName)) {
            case "environnement":
                return DOC_ROOT + "//span[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'environ') " +
                        "and contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'groupe')]";
            case "groupes":
                return DOC_ROOT + "//span[normalize-space()='Rattach\u00e9 au groupe']";
            case "entreprises":
                return DOC_ROOT + "//span[" +
                        "contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'entreprises')" +
                        " and contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'groupe')" +
                        " and not(contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'cat'))" +
                        "]";
            case "categories":
                return DOC_ROOT + "//span[" +
                        "contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'cat')" +
                        " and contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'groupe')" +
                        "]";
            default:
                throw new IllegalArgumentException("Section inconnue : " + sectionName);
        }
    }

    private String getSectionXpath(String sectionName) {
        switch (normalizeSectionKey(sectionName)) {
            case "environnement":
                return SEC_ENVIRONNEMENT;
            case "groupes":
                return SEC_GROUPES;
            case "entreprises":
                return SEC_ENTREPRISES;
            case "categories":
                return SEC_CATEGORIES;
            default:
                throw new IllegalArgumentException("Section inconnue : " + sectionName);
        }
    }

    private WebElement getSectionContent(String sectionName) {
        By locator = By.xpath(getSectionXpath(sectionName));
        wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        return driver.findElement(locator);
    }

    private WebElement getSectionLabel(String sectionName) {
        By locator = By.xpath(labelXpath(sectionName));
        wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        return driver.findElement(locator);
    }

    private List<WebElement> findLoadButtonsNearSectionLabel(String sectionName) {
        WebElement label = getSectionLabel(sectionName);
        List<WebElement> buttons = label.findElements(By.xpath(
                "ancestor::div[contains(@class,'col-10')][1]/following-sibling::div[position()<=8]" +
                        "//div[contains(@class,'cursor-pointer') and " +
                        "(contains(.,'Afficher') or contains(.,'charger') or contains(.,'Charger'))]"));
        if (buttons.isEmpty()) {
            return List.of();
        }

        // Filtrage soft pour eviter de cliquer le mauvais bouton quand 2 boutons "Afficher..." sont proches.
        String key = normalizeSectionKey(sectionName);
        return buttons.stream()
                .filter(WebElement::isDisplayed)
                .filter(b -> {
                    String t = (b.getText() == null ? "" : b.getText()).toLowerCase();
                    if ("entreprises".equals(key)) {
                        return t.contains("entrepris") && !t.contains("cat");
                    }
                    if ("categories".equals(key)) {
                        return t.contains("cat");
                    }
                    return true;
                })
                .toList();
    }

    private void clickLoadButtonForSection(String sectionName) {
        WebElement content = getSectionContent(sectionName);
        List<WebElement> inside = content.findElements(By.xpath(LOAD_BTN)).stream()
                .filter(WebElement::isDisplayed)
                .toList();
        if (!inside.isEmpty()) {
            clickElement(inside.get(0));
            return;
        }

        List<WebElement> nearLabel = findLoadButtonsNearSectionLabel(sectionName);
        if (!nearLabel.isEmpty()) {
            clickElement(nearLabel.get(0));
            return;
        }

        throw new NoSuchElementException("Bouton Afficher/Charger introuvable pour section: " + sectionName);
    }

    public List<WebElement> getCardsInSection(String sectionName) {
        WebElement content = getSectionContent(sectionName);
        List<WebElement> cards = content.findElements(By.xpath(CARD));
        if (!cards.isEmpty()) {
            return cards;
        }

        int before = cards.size();
        try {
            clickLoadButtonForSection(sectionName);
        } catch (NoSuchElementException ignored) {
            // Certaines sections peuvent ne pas proposer de bouton (etat deja charge, ou pas de donnees).
        }

        // Important: ne pas attendre un "card-container" global (il peut deja exister dans une autre section).
        waitForSectionDocumentsToLoad(sectionName, before);
        content = getSectionContent(sectionName);
        return content.findElements(By.xpath(CARD));
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
        // Dans l'onglet Documents du groupe, ce bouton correspond au chargement de la section environnement.
        clickLoadButtonForSection("environnement");
        waitForSectionDocumentsToLoad("environnement", 0);
    }

    @Override
    public void clickShowCompanies() {
        clickLoadButtonForSection("entreprises");
        waitForSectionDocumentsToLoad("entreprises", 0);
    }

    @Override
    public void clickShowCategories() {
        clickLoadButtonForSection("categories");
        waitForSectionDocumentsToLoad("categories", 0);
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
        try {
            WebElement content = getSectionContent(sectionName);
            String key = normalizeSectionKey(sectionName);

            if ("entreprises".equals(key)) {
                // Etat vide possible: "Rattaché à des entreprises liées à ce groupe" (pas "Aucun document").
                List<WebElement> msgs = content.findElements(By.xpath(
                        ".//div[contains(@class,'text-center') and contains(.,'Rattach') and contains(.,'entrepris')]"));
                if (!msgs.isEmpty() && msgs.get(0).isDisplayed()) {
                    return true;
                }
                List<WebElement> near = getSectionLabel(sectionName).findElements(By.xpath(
                        "ancestor::div[contains(@class,'col-10')][1]/following-sibling::div[position()<=8]" +
                                "//div[contains(@class,'text-center') and contains(.,'Rattach') and contains(.,'entrepris')]"));
                return !near.isEmpty() && near.get(0).isDisplayed();
            }

            List<WebElement> msgs = content.findElements(By.xpath(NO_DOC_MSG));
            if (msgs.isEmpty()) {
                msgs = driver.findElements(anyNoDocMsg);
            }
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

    private void waitForSectionDocumentsToLoad(String sectionName, int previousCardCount) {
        try {
            wait.until(d -> {
                try {
                    WebElement content = getSectionContent(sectionName);
                    int cardCount = content.findElements(By.xpath(CARD)).size();
                    boolean hasNoDoc = !content.findElements(By.xpath(NO_DOC_MSG)).isEmpty();
                    boolean hasCenteredMsg = !content.findElements(By.xpath(TEXT_CENTER)).isEmpty();
                    if (cardCount > previousCardCount || hasNoDoc || hasCenteredMsg) {
                        return true;
                    }

                    // Certains messages peuvent etre rendus hors du bloc col-12 (ex: categories avant chargement).
                    WebElement label = getSectionLabel(sectionName);
                    List<WebElement> near = label.findElements(By.xpath(
                            "ancestor::div[contains(@class,'col-10')][1]/following-sibling::div[position()<=8]" +
                                    "//div[contains(@class,'text-center') and string-length(normalize-space(.))>0]"));
                    return !near.isEmpty();
                } catch (StaleElementReferenceException | NoSuchElementException ignored) {
                    return false;
                }
            });
        } catch (Exception e) {
            LOGGER.warning("waitForSectionDocumentsToLoad(" + sectionName + "): " + e.getMessage());
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
