package org.automation.pages.group;

import org.automation.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.logging.Logger;

public class GroupAttachmentPage extends BasePage {

    private static final Logger LOGGER = Logger.getLogger(GroupAttachmentPage.class.getName());

    // ── Tabs ─────────────────────────────────────────────────────────────────────
    private final By attachmentsTab = By.xpath(
            "//div[@class='q-tab__label'][normalize-space()='Rattachements']");
    private final By activeAttachmentsTab = By.xpath(
            "//div[contains(@class,'q-tab--active')]//div[@class='q-tab__label'][normalize-space()='Rattachements']");

    // ── Buttons ───────────────────────────────────────────────────────────────────
    private final By createNewCompanyBtn = By.xpath(
            "//button[.//span[contains(.,'Créer une nouvelle entreprise dans ce groupe')]]");
    private final By addElementBtn = By.xpath(
            "//button[.//span[contains(.,'Ajouter un élément dans la liste')]]");
    private final By saveBtn = By.xpath(
            "//button[.//span[contains(.,'Enregistrer')]]");

    // ── Filters ───────────────────────────────────────────────────────────────────
    private final By nameFilterInput = By.xpath("//input[@placeholder='Nom']");
    private final By openIdFilterInput = By.xpath("//input[@placeholder='ID dans Open']");
    private final By clearNameFilterIcon = By.xpath(
            "//i[contains(@class,'icon-x') and contains(@class,'cursor-pointer')]");
    private final By clearOpenIdFilterIcon = By.xpath(
            "//input[@placeholder='ID dans Open']/ancestor::label//i[contains(@class,'icon-x')]");

    // ── Table ─────────────────────────────────────────────────────────────────────
    private final By tableRows = By.xpath(
            "//table[.//th[contains(.,'Nom de l')]]//tbody/tr");
    private final By emptyTableMsg = By.xpath(
            "//div[contains(@class,'text-center') and contains(.,'Aucun')]");

    // ── Pagination ────────────────────────────────────────────────────────────────
    private final By prevPageBtn = By.xpath("//button[@aria-label='Previous page']");
    private final By nextPageBtn = By.xpath("//button[@aria-label='Next page']");

    // ── Dialogs ───────────────────────────────────────────────────────────────────
    private final By confirmDialog = By.xpath("//div[@role='dialog']");
    private final By confirmBtn = By.xpath(
            "//div[@role='dialog']//button[contains(.,'Confirmer la suppression')]");
    private final By cancelBtn = By.xpath(
            "//div[@role='dialog']//button[contains(.,'Ne pas supprimer')]");
    private final By successToast = By.xpath(
            "//div[@id='q-notify']//div[@role='alert' and contains(@class,'q-notification')]"
    );
    private final By successToastMessage = By.xpath(
            "//div[@id='q-notify']//div[contains(@class,'q-notification__message')]");
    // ── Add-element dropdown ──────────────────────────────────────────────────────
//    private final By dropdownMenu = By.xpath(
//            "//div[@id[starts-with(.,'q-portal--menu')]]//div[@role='listbox']");
//    private final By dropdownCriterionCombobox = By.xpath(
//            "//div[@id[starts-with(.,'q-portal--menu')]]//input[@role='combobox']");
//    private final By dropdownSearchInput = By.xpath(
//            "//div[@id[starts-with(.,'q-portal--menu')]]//input[not(@role='combobox')]");

    // ── Add-element dropdown ──────────────────────────────────────────────────────
    private final By dropdownCompanyOptions = By.xpath(
            "//div[@id[starts-with(.,'q-portal--menu')][.//div[@role='listbox' and @aria-multiselectable='true']]]"
                    + "//div[@role='option'][.//i[contains(@class,'icon-square')]]");

    // Le select "Entreprises" (on clique le contrôle/label, plus fiable que l'input readonly)
    private final By entreprisesFieldControl = By.xpath(
            "//label[contains(@class,'filter-select')]//div[contains(@class,'q-field__control')]");
    private final By entreprisesFieldLabel = By.xpath(
            "//label[contains(@class,'filter-select')]");
    // Le listbox du dropdown (portal)
    private final By dropdownMenu = By.xpath(
            "//div[@id[starts-with(.,'q-portal--menu')]]//div[@role='listbox' and @aria-multiselectable='true']");
    private final By dropdownMenuAnyListbox = By.xpath(
            "//div[@id[starts-with(.,'q-portal--menu')]]//div[@role='listbox']");

    // Le champ de recherche DANS le dropdown (texte éditable, pas le combobox readonly "name")
    private final By dropdownSearchInput = By.xpath(
            "//div[@id[starts-with(.,'q-portal--menu')][.//div[@role='listbox' and @aria-multiselectable='true']]]"
                    + "//input[not(@role='combobox')]");

    // Row-level icon XPaths (relative to a <tr>)
    private static final String EYE_ICON_REL    = ".//i[contains(@class,'icon-eye')]";
    private static final String EDIT_ICON_REL   = ".//i[contains(@class,'material-icons') and normalize-space()='edit']";
    private static final String DELETE_ICON_REL = ".//i[contains(@class,'material-icons') and normalize-space()='delete']";

    // ── Tab ───────────────────────────────────────────────────────────────────────

    public void clickAttachmentsTab() {
        scrollAndClick(attachmentsTab, "Onglet Rattachements");
        waitForTableToLoad();
    }

    public boolean isAttachmentsTabActive() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(activeAttachmentsTab));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ── Title ─────────────────────────────────────────────────────────────────────

    public boolean isSectionTitleDisplayed(String expectedTitle) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//h2[contains(.,'" + expectedTitle + "')]")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ── Table ─────────────────────────────────────────────────────────────────────

    private List<WebElement> getRows() {
        waitForTableToLoad();
        return driver.findElements(tableRows);
    }

    public int getRowCount() {
        return getRows().size();
    }

    public boolean isTableDisplayed() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//table[.//th[contains(.,'Nom de l')]]")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isEmptyTableMessageDisplayed() {
        try {
            List<WebElement> msgs = driver.findElements(emptyTableMsg);
            return !msgs.isEmpty() && msgs.get(0).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // ── Filtering ─────────────────────────────────────────────────────────────────

    public void filterByName(String name) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(nameFilterInput));
        input.clear();
        input.sendKeys(name);
        waitForTableToLoad();
    }

    public void filterByOpenId(String openId) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(openIdFilterInput));
        input.clear();
        input.sendKeys(openId);
        waitForTableToLoad();
    }

    public void clearNameFilter() {
        scrollAndClick(clearNameFilterIcon, "Clear name filter");
        waitForTableToLoad();
    }

    public void clearOpenIdFilter() {
        scrollAndClick(clearOpenIdFilterIcon, "Clear openId filter");
        waitForTableToLoad();
    }

    public boolean areRowsFilteredByName(String name) {
        List<WebElement> rows = getRows();
        if (rows.isEmpty()) return false;
        return rows.stream().allMatch(row -> {
            String cellText = row.findElements(By.xpath(".//td[1]//span"))
                    .stream().findFirst().map(WebElement::getText).orElse("");
            return cellText.toLowerCase().contains(name.toLowerCase());
        });
    }

    // ── Pagination ────────────────────────────────────────────────────────────────

    public boolean isNextPageButtonEnabled() {
        try {
            WebElement btn = driver.findElement(nextPageBtn);
            return btn.isEnabled() && !btn.getAttribute("class").contains("disabled");
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean isPreviousPageButtonEnabled() {
        try {
            WebElement btn = driver.findElement(prevPageBtn);
            return btn.isEnabled() && !btn.getAttribute("class").contains("disabled");
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public void clickNextPage() {
        scrollAndClick(nextPageBtn, "Page suivante");
        waitForTableToLoad();
    }

    public void clickPreviousPage() {
        scrollAndClick(prevPageBtn, "Page précédente");
        waitForTableToLoad();
    }

    public int getCurrentPage() {
        try {
            List<WebElement> activeBtns = driver.findElements(By.xpath(
                    "//div[contains(@class,'q-pagination')]//button[contains(@class,'bg-primary')]"));
            if (!activeBtns.isEmpty()) {
                return Integer.parseInt(activeBtns.get(0).getText().trim());
            }
        } catch (Exception e) {
            LOGGER.warning("getCurrentPage: " + e.getMessage());
        }
        return 1;
    }

    public boolean isPageActive(int pageNumber) {
        try {
            WebElement btn = driver.findElement(By.xpath(
                    "//div[contains(@class,'q-pagination')]//button[contains(@class,'bg-primary')" +
                            " and .//span[normalize-space()='" + pageNumber + "']]"));
            return btn.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    // ── Row actions ───────────────────────────────────────────────────────────────

    public void clickViewIconOnRow(int rowIndex) {
        clickIconOnRow(rowIndex, EYE_ICON_REL, "Eye icon");
    }

    public void clickEditIconOnRow(int rowIndex) {
        clickIconOnRow(rowIndex, EDIT_ICON_REL, "Edit icon");
    }

    public void clickDeleteIconOnRow(int rowIndex) {
        clickIconOnRow(rowIndex, DELETE_ICON_REL, "Delete icon");
    }

    private void clickIconOnRow(int rowIndex, String iconXpath, String label) {
        List<WebElement> rows = getRows();
        if (rowIndex < 0 || rowIndex >= rows.size()) {
            throw new IllegalArgumentException(
                    label + ": index " + rowIndex + " hors limites (taille=" + rows.size() + ")");
        }
        clickElement(rows.get(rowIndex).findElement(By.xpath(iconXpath)));
        sleep(600);
    }

    // ── Add-element dropdown flow ─────────────────────────────────────────────────

    public void clickAddElementButton() {
        scrollAndClick(addElementBtn, "Ajouter un élément dans la liste");
        sleep(700);
        // Le champ "Entreprises" apparaît → cliquer dessus pour ouvrir le portal
        openEntreprisesDropdown();
    }
    public void openEntreprisesDropdown() {
        boolean opened = false;

        try {
            WebElement control = wait.until(
                    ExpectedConditions.presenceOfElementLocated(entreprisesFieldControl));
            clickElement(control);
            opened = isAddElementDropdownOpen();
        } catch (Exception e) {
            LOGGER.warning("Échec clic sur contrôle Entreprises : " + e.getMessage());
        }

        if (!opened) {
            try {
                WebElement label = wait.until(
                        ExpectedConditions.presenceOfElementLocated(entreprisesFieldLabel));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", label);
                opened = isAddElementDropdownOpen();
            } catch (Exception e) {
                LOGGER.warning("Échec clic fallback sur label Entreprises : " + e.getMessage());
            }
        }

        if (!opened) {
            throw new RuntimeException("Impossible d'ouvrir le dropdown principal 'Entreprises'");
        }

        wait.until(ExpectedConditions.visibilityOfElementLocated(dropdownMenu));
    }

    public boolean isAddElementDropdownOpen() {
        try {
            List<WebElement> menus  = driver.findElements(dropdownMenu);
            return menus.stream().anyMatch(WebElement::isDisplayed);
        } catch (Exception e) {
            return false;
        }
    }

    public void typeSearchInDropdown(String text) {
        if (!isAddElementDropdownOpen()) {
            openEntreprisesDropdown();
        }

        By visibleSearchInputInPortal = By.xpath(
                "//div[@id[starts-with(.,'q-portal--menu')]]//input[not(@role='combobox') and not(@readonly)]");

        WebElement searchInput = null;
        for (int attempt = 0; attempt < 3; attempt++) {
            List<WebElement> inputs = driver.findElements(visibleSearchInputInPortal)
                    .stream()
                    .filter(WebElement::isDisplayed)
                    .toList();
            if (!inputs.isEmpty()) {
                searchInput = inputs.get(0);
                break;
            }
            openEntreprisesDropdown();
            sleep(250);
        }
        if (searchInput == null) {
            throw new RuntimeException("Champ de recherche non trouvé dans le dropdown ouvert");
        }

        // Vider proprement puis saisir via sendKeys sur l'élément focused
        // (pas de clear() qui peut fermer le dropdown Quasar)
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].focus();" +
                        "arguments[0].value = '';" +
                        "arguments[0].dispatchEvent(new Event('input', {bubbles:true}));" +
                        "arguments[0].value = arguments[1];" +
                        "arguments[0].dispatchEvent(new Event('input', {bubbles:true}));" +
                        "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));",
                searchInput, text);
        sleep(250);

        // Fallback clavier (certains composants Quasar réagissent mieux aux touches)
        try {
            searchInput.click();
            searchInput.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            searchInput.sendKeys(text);
        } catch (Exception e) {
            LOGGER.warning("Fallback clavier non appliqué: " + e.getMessage());
        }

        // Attendre que le virtual scroll se repeuple avec les résultats filtrés
        try {
            By visibleOptionsInPortal = By.xpath(
                    "//div[@id[starts-with(.,'q-portal--menu')]]"
                            + "//div[@role='option'][.//i[contains(@class,'icon-square') or contains(@class,'icon-check')]]");
            wait.until(driver -> driver.findElements(visibleOptionsInPortal)
                    .stream().anyMatch(WebElement::isDisplayed));
            LOGGER.info("Options filtrées apparues pour : " + text);
        } catch (Exception e) {
            LOGGER.warning("Aucune option après filtrage : " + text);
        }
    }

    public void selectCompanyInDropdown(String companyName) {
        if (!isAddElementDropdownOpen()) {
            openEntreprisesDropdown();
        }

        String expected = normalizeLabel(companyName);
        By companyOptionsInPortal = By.xpath(
                "//div[@id[starts-with(.,'q-portal--menu')]]"
                        + "//div[@role='option'][.//i[contains(@class,'icon-square') or contains(@class,'icon-check')]]");

        for (int attempt = 0; attempt < 4; attempt++) {
            if (!isAddElementDropdownOpen()) {
                openEntreprisesDropdown();
            }

            List<WebElement> visibleOptions = driver.findElements(companyOptionsInPortal)
                    .stream()
                    .filter(WebElement::isDisplayed)
                    .toList();

            LOGGER.info("Options visibles (" + visibleOptions.size() + ") :");
            visibleOptions.forEach(o -> LOGGER.info("  → [" + normalizeLabel(o.getText()) + "]"));

            WebElement exactOption = visibleOptions.stream()
                    .filter(o -> normalizeLabel(o.getText()).equalsIgnoreCase(expected))
                    .findFirst()
                    .orElse(null);
            if (exactOption != null) {
                clickElement(exactOption);
                sleep(350);
                return;
            }

            // Virtual scroll: charger d'autres options et retenter
            new Actions(driver).sendKeys(Keys.PAGE_DOWN).perform();
            sleep(250);
        }

        throw new RuntimeException("Option entreprise introuvable: " + companyName);
    }

    public void closeAddElementDropdown() {
        new Actions(driver).sendKeys(Keys.ESCAPE).perform();
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(dropdownMenu));
        } catch (Exception e) {
            LOGGER.warning("Le dropdown n'a pas été fermé après ESC : " + e.getMessage());
        }
    }

    public boolean isSaveButtonEnabled() {
        try {
            WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(saveBtn));
            String classes = btn.getAttribute("class");
            String ariaDisabled = btn.getAttribute("aria-disabled");
            return btn.isEnabled()
                    && (classes == null || !classes.contains("disabled"))
                    && !"true".equalsIgnoreCase(ariaDisabled);
        } catch (Exception e) {
            return false;
        }
    }


    public void clickSaveButton() {
        scrollAndClick(saveBtn, "Enregistrer");
        waitForAttachmentCreationToast();
        waitForTableToLoad();
    }

    public boolean isAttachmentCreationSuccessToastDisplayed() {
        try {
            WebDriverWait Wait = new WebDriverWait(driver, Duration.ofSeconds(18));
            WebElement toast = Wait.until(ExpectedConditions.visibilityOfElementLocated(successToast));
            String message = toast.getText();
            if (message == null || message.isBlank()) {
                List<WebElement> messages = driver.findElements(successToastMessage);
                if (!messages.isEmpty()) {
                    message = messages.get(0).getText();
                }
            }
            return isAttachmentCreationSuccessMessage(message);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isCompanyPresentInTable(String companyName) {
        String expected = normalizeLabel(companyName);
        try {
            return wait.until(driver -> getRows().stream().anyMatch(row -> {
                String text = row.findElements(By.xpath(".//td[1]//span"))
                        .stream().findFirst().map(WebElement::getText).orElse("");
                return normalizeLabel(text).equalsIgnoreCase(expected);
            }));
        } catch (Exception e) {
            return false;
        }
    }

    // ── Create new company ────────────────────────────────────────────────────────

    public void clickCreateNewCompanyButton() {
        scrollAndClick(createNewCompanyBtn, "Créer une nouvelle entreprise dans ce groupe");
        sleep(800);
    }

    public boolean isOnCompanyCreationPage() {
        try {
            wait.until(ExpectedConditions.urlContains("/entities/compan"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ── Delete flow ───────────────────────────────────────────────────────────────

    public boolean isDeleteConfirmDialogDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(confirmDialog));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void confirmDeletion() {
        scrollAndClick(confirmBtn, "Confirmer");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(confirmDialog));
    }

    public void cancelDeletion() {
        scrollAndClick(cancelBtn, "Annuler");
        wait.until(ExpectedConditions.invisibilityOfElementLocated(confirmDialog));
    }

    public boolean isDeletionSuccessful() {
        try {
            WebElement toast = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(successToast)
            );
            String message = toast.getText();
            return message != null && message.toLowerCase().contains("supprim");

        } catch (Exception e) {
            return false;
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────────

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
                LOGGER.info(label + " cliqué");
                return;
            } catch (StaleElementReferenceException e) {
                attempts++;
                sleep(300);
            }
        }
        throw new RuntimeException("scrollAndClick échoué : " + label);
    }

    private void waitForTableToLoad() {
        sleep(600);
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(tableRows),
                    ExpectedConditions.presenceOfElementLocated(emptyTableMsg)));
        } catch (Exception e) {
            LOGGER.warning("waitForTableToLoad: " + e.getMessage());
        }
    }

    private void waitForAttachmentCreationToast() {
        try {
            WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(12));
            WebElement toast = longWait.until(ExpectedConditions.visibilityOfElementLocated(successToast));
            if (!isAttachmentCreationSuccessMessage(toast.getText())) {
                List<WebElement> messages = driver.findElements(successToastMessage);
                if (!messages.isEmpty()) {
                    String msg = messages.get(0).getText();
                    if (!isAttachmentCreationSuccessMessage(msg)) {
                        LOGGER.warning("Toast reçu mais message non reconnu: " + msg);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Toast de création non détecté après Enregistrer: " + e.getMessage());
        }
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private String normalizeLabel(String value) {
        if (value == null) return "";
        return value.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
    }

    private boolean isAttachmentCreationSuccessMessage(String message) {
        String text = normalizeLabel(message).toLowerCase();
        return text.contains("liaison établie avec succès")
                || text.contains("liaison etablie avec succes")
                || text.contains("opération terminée")
                || text.contains("operation terminee")
                || text.contains("rattach")
                || text.contains("ajout")
                || text.contains("enregistr")
                || text.contains("cré")
                || text.contains("cree");
    }

    private WebElement getOpenDropdownListbox() {
        for (int attempt = 0; attempt < 3; attempt++) {
            List<WebElement> strictMenus = driver.findElements(dropdownMenu)
                    .stream()
                    .filter(WebElement::isDisplayed)
                    .toList();
            if (!strictMenus.isEmpty()) {
                return strictMenus.get(0);
            }

            List<WebElement> anyMenus = driver.findElements(dropdownMenuAnyListbox)
                    .stream()
                    .filter(WebElement::isDisplayed)
                    .toList();
            if (!anyMenus.isEmpty()) {
                return anyMenus.get(0);
            }

            // Le menu a pu se refermer entre deux steps : on le rouvre puis on retente.
            try {
                openEntreprisesDropdown();
            } catch (Exception e) {
                LOGGER.warning("Réouverture du dropdown échouée (tentative " + (attempt + 1) + "): " + e.getMessage());
            }
            sleep(250);
        }

        throw new RuntimeException("Dropdown des rattachements non ouvert");
    }
}
