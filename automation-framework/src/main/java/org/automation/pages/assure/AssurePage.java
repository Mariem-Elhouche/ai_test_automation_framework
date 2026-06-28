package org.automation.pages.assure;

import org.automation.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class AssurePage extends BasePage {

    private static final Logger LOGGER = Logger.getLogger(AssurePage.class.getName());


    private final By assuresTab = By.xpath(
            "//div[@class='q-tab__label'][normalize-space()='Assurés']"
    );

    private final By periodInput = By.xpath(
            "//div[contains(@class,'q-field')]//input[@type='text' and contains(@value,'-')]"
    );

    private final By calendarIcon = By.xpath(
            "//div[contains(@class,'q-field__append')]//i[normalize-space()='event']"
    );

    private final By calendarPopup = By.xpath(
            "//div[contains(@class,'q-date')]"
    );

    private final By calendarDays = By.xpath(
            "//div[contains(@class,'q-date__calendar-item')]" +
                    "//button[not(contains(@class,'disabled')) and not(contains(@class,'q-date__range'))]"
    );

    private final By calendarCloseBtn = By.xpath(
            "//div[contains(@class,'q-date__actions')]//button[last()]"
    );

    private static final String TYPE_ESPACE_ANCHOR =
            "//div[contains(@class,'col-auto')]" +
                    "[.//div[contains(normalize-space(),'Type d') and contains(normalize-space(),'espace')]]";

    private final By typeEspaceDropdownArrow = By.xpath(
            TYPE_ESPACE_ANCHOR +
                    "//label[contains(@class,'q-select')]" +
                    "//i[contains(@class,'q-select__dropdown-icon')]"
    );

    private final By typeEspaceSelected = By.xpath(
            TYPE_ESPACE_ANCHOR +
                    "//label[contains(@class,'q-select')]" +
                    "//div[contains(@class,'q-field__native')]" +
                    "//span[contains(@class,'ellipsis')]"
    );

    private final By typeEspaceOptions = By.xpath(
            "//div[contains(@class,'q-menu')]" +
                    "//div[@role='option']" +
                    "//div[contains(@class,'q-item__section--main')]"
    );

    private final By typeEspaceMenu = By.xpath(
            "//div[contains(@class,'q-menu')]"
    );

    // ══════════════════════════════════════════════
    //  STATISTIQUES
    // ══════════════════════════════════════════════

    private final By anyStatsCard = By.xpath(
            "//div[contains(@class,'stats-card-fixed-width')]"
    );


    private final By downloadDropdownBtn = By.xpath(
            "//button[contains(@class,'btn-export')]"
    );

    // q-menu est un portail Quasar — il se téléporte hors du bouton dans le DOM
    private final By downloadMenu = By.xpath(
            "//div[contains(@class,'q-menu')]"
    );

    private final By downloadMenuItems = By.xpath(
            "//div[contains(@class,'q-menu')]//div[contains(@class,'q-item')]"
    );


    private final By assureTableRows = By.xpath(
            "//table[contains(@class,'q-table')]//tbody//tr"
    );

    private final By tableHeaders = By.xpath(
            "//table[contains(@class,'q-table')]//thead//th"
    );


    private final By firstEyeIcon = By.xpath(
            "(//table[contains(@class,'q-table')]//tbody//tr" +
                    "//i[contains(@class,'material-icons') and normalize-space()='visibility'])[1]"
    );


    private final By anyReadonlyField = By.xpath(
            "//div[contains(@class,'q-field--readonly')]//input | " +
                    "//div[contains(@class,'q-field--readonly')]//span[contains(@class,'ellipsis')]"
    );

    private final By accederEspaceBtn = By.xpath(
            "//button[contains(normalize-space(),'Accéder')]"
    );

    private final By entitiesLieesRows = By.xpath(
            "//*[contains(normalize-space(),'Entités liées')]" +
                    "/following::table[1]//tbody//tr"
    );


    private final By nextButton = By.xpath(
            "//button[.//span[normalize-space()='Suivant']]"
    );

    private final By previousButton = By.xpath(
            "//button[.//span[normalize-space()='Précédent']]"
    );

    private final By pageInfo = By.xpath(
            "//div[contains(@class,'q-ml-auto') and contains(normalize-space(),'Page')]"
    );

    // ══════════════════════════════════════════════
    //  CONSTRUCTEUR
    // ══════════════════════════════════════════════

    public AssurePage() {
        super();
    }

    public void clickAssuresTab() {
        scrollAndClick(assuresTab, "Onglet Assurés");
        waitForTabToLoad();
        LOGGER.info("Onglet Assurés ouvert");
    }


    public String getPeriodValue() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(periodInput))
                .getAttribute("value");
    }

    public void clickCalendarIconAndSelectDates() {
        scrollAndClick(calendarIcon, "Icône calendrier");
        wait.until(ExpectedConditions.visibilityOfElementLocated(calendarPopup));
        sleep(400);

        List<WebElement> days = driver.findElements(calendarDays);
        if (days.size() >= 2) {
            days.get(0).click();
            sleep(200);
            // Rafraîchit la liste après le premier clic (Quasar peut re-rendre le calendrier)
            days = driver.findElements(calendarDays);
            days.get(Math.min(9, days.size() - 1)).click();
            sleep(200);
        } else {
            LOGGER.warning("Pas assez de jours disponibles dans le calendrier");
        }

        try {
            wait.until(ExpectedConditions.elementToBeClickable(calendarCloseBtn)).click();
        } catch (Exception e) {
            LOGGER.info("Pas de bouton close calendrier → Escape");
            new Actions(driver).sendKeys(Keys.ESCAPE).perform();
        }
        sleep(600);
    }


    public String getSelectedTypeEspace() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(typeEspaceSelected))
                .getText().trim();
    }

    public void selectTypeEspace(String label) {
        openTypeEspaceDropdown();
        wait.until(ExpectedConditions.visibilityOfElementLocated(typeEspaceMenu));

        // Cible l'option directement par son texte — pas de boucle Java
        By optionByText = By.xpath(
                "//div[contains(@class,'q-menu')]" +
                        "//div[@role='option']" +
                        "//div[contains(@class,'q-item__section--main')" +
                        " and normalize-space()='" + label + "']"
        );

        WebElement option = wait.until(ExpectedConditions.elementToBeClickable(optionByText));
        new Actions(driver).moveToElement(option).click().perform();
        LOGGER.info("Type d'espace sélectionné : " + label);
        sleep(500);
    }

    private void openTypeEspaceDropdown() {
        WebElement arrow = wait.until(
                ExpectedConditions.elementToBeClickable(typeEspaceDropdownArrow));
        new Actions(driver).moveToElement(arrow).perform();
        sleep(150);
        try {
            arrow.click();
        } catch (ElementClickInterceptedException e) {
            LOGGER.warning("Clic arrow intercepté → JS click fallback");
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", arrow);
        }
        LOGGER.info("Dropdown type d'espace ouvert");
    }


    public int getStatsCardCount() {
        return driver.findElements(anyStatsCard).size();
    }

    public String getStatValue(int index) {
        List<WebElement> cards = driver.findElements(anyStatsCard);
        if (index >= cards.size()) {
            throw new IndexOutOfBoundsException(
                    "Carte index " + index + " inexistante (total: " + cards.size() + ")");
        }
        return cards.get(index)
                .findElement(By.xpath(".//span[contains(@class,'text-h3')]"))
                .getText().trim();
    }

    public String getStatLabel(int index) {
        List<WebElement> cards = driver.findElements(anyStatsCard);
        if (index >= cards.size()) {
            throw new IndexOutOfBoundsException(
                    "Carte index " + index + " inexistante (total: " + cards.size() + ")");
        }
        return cards.get(index)
                .findElement(By.xpath(".//span[contains(@class,'stats-title-wrap')]"))
                .getText().trim();
    }

    public Map<String, String> captureAllStatValues() {
        Map<String, String> snapshot = new HashMap<>();
        for (WebElement card : driver.findElements(anyStatsCard)) {
            try {
                String label = card.findElement(
                        By.xpath(".//span[contains(@class,'stats-title-wrap')]")).getText().trim();
                String value = card.findElement(
                        By.xpath(".//span[contains(@class,'text-h3')]")).getText().trim();
                snapshot.put(label, value);
            } catch (Exception e) {
                LOGGER.warning("Carte stat illisible : " + e.getMessage());
            }
        }
        LOGGER.info("Snapshot stats capturé : " + snapshot);
        return snapshot;
    }

    public boolean haveStatsChanged(Map<String, String> before, Map<String, String> after) {
        return before.entrySet().stream().anyMatch(entry -> {
            String valAfter = after.getOrDefault(entry.getKey(), entry.getValue());
            boolean changed = !entry.getValue().equals(valAfter);
            if (changed) {
                LOGGER.info("Stat changée — [" + entry.getKey() + "] : "
                        + entry.getValue() + " → " + valAfter);
            }
            return changed;
        });
    }


    public void clickDownloadDropdown() {
        scrollAndClick(downloadDropdownBtn, "Bouton télécharger statistiques");
        wait.until(ExpectedConditions.visibilityOfElementLocated(downloadMenu));
    }

    public boolean isDownloadMenuVisible() {
        try {
            return driver.findElement(downloadMenu).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean isDownloadOptionPresent(String label) {
        return driver.findElements(downloadMenuItems).stream()
                .anyMatch(el -> el.getText().trim().equals(label));
    }

    public void clickDownloadOption(String label) {
        // XPath direct par texte — pas de boucle Java
        By optionByText = By.xpath(
                "//div[contains(@class,'q-menu')]" +
                        "//div[contains(@class,'q-item') and normalize-space()='" + label + "']"
        );
        WebElement option = wait.until(ExpectedConditions.elementToBeClickable(optionByText));
        new Actions(driver).moveToElement(option).click().perform();
        LOGGER.info("Option téléchargement cliquée : " + label);
        sleep(400);
    }


    public boolean isTableDisplayed() {
        return !driver.findElements(assureTableRows).isEmpty();
    }

    public int getAssureRowCount() {
        return driver.findElements(assureTableRows).size();
    }

    public boolean isColumnPresent(String columnName) {
        return driver.findElements(tableHeaders).stream()
                .anyMatch(h -> h.getText().trim().equals(columnName));
    }


    public void clickFirstEyeIcon() {
        scrollAndClick(firstEyeIcon, "Icône œil première ligne");
        waitForDetailPageToLoad();
    }

    public boolean isDetailPageDisplayed() {
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(accederEspaceBtn),
                    ExpectedConditions.presenceOfElementLocated(anyReadonlyField)
            ));
            LOGGER.info("Page détail assuré détectée");
            return true;
        } catch (Exception e) {
            LOGGER.warning("Page détail non détectée : " + e.getMessage());
            return false;
        }
    }

    public boolean isAccederButtonVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(accederEspaceBtn))
                    .isDisplayed();
        } catch (Exception e) {
            LOGGER.warning("Bouton Accéder non trouvé : " + e.getMessage());
            return false;
        }
    }

    public boolean isEntitiesLieesTableDisplayed() {
        try {
            List<WebElement> rows = wait.until(
                    ExpectedConditions.presenceOfAllElementsLocatedBy(entitiesLieesRows));
            return !rows.isEmpty();
        } catch (Exception e) {
            LOGGER.warning("Table entités liées non trouvée : " + e.getMessage());
            return false;
        }
    }


    public String getPageInfo() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(pageInfo))
                .getText().trim();
    }

    public boolean isNextButtonEnabled() {
        try {
            WebElement btn = driver.findElement(nextButton);
            return !btn.getAttribute("class").contains("disabled")
                    && btn.getAttribute("disabled") == null;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean isPreviousButtonEnabled() {
        try {
            WebElement btn = driver.findElement(previousButton);
            return !btn.getAttribute("class").contains("disabled")
                    && btn.getAttribute("disabled") == null;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public void clickNext() {
        scrollAndClick(nextButton, "Bouton Suivant");
        waitForTableToRefresh();
    }

    public void clickPrevious() {
        scrollAndClick(previousButton, "Bouton Précédent");
        waitForTableToRefresh();
    }


    private void waitForTabToLoad() {
        sleep(600);
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(anyStatsCard));
        } catch (Exception e) {
            LOGGER.warning("Attente chargement onglet Assurés : " + e.getMessage());
        }
    }

    private void waitForTableToRefresh() {
        sleep(400);
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(assureTableRows));
        } catch (Exception e) {
            LOGGER.warning("Attente rafraîchissement tableau : " + e.getMessage());
        }
    }

    private void waitForDetailPageToLoad() {
        sleep(800);
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(accederEspaceBtn),
                    ExpectedConditions.presenceOfElementLocated(anyReadonlyField)
            ));
        } catch (Exception e) {
            LOGGER.warning("Attente page détail assuré : " + e.getMessage());
        }
    }

    private void scrollAndClick(By locator, String label) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
                try {
                    new Actions(driver).moveToElement(el).click().perform();
                } catch (ElementClickInterceptedException e) {
                    LOGGER.warning(label + " : clic intercepté → JS click fallback");
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
                }
                LOGGER.info(label + " cliqué");
                return;
            } catch (StaleElementReferenceException e) {
                attempts++;
                LOGGER.warning(label + " : StaleElement, tentative " + attempts);
                sleep(200);
            }
        }
        throw new RuntimeException("scrollAndClick échoué après 3 tentatives : " + label);
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}