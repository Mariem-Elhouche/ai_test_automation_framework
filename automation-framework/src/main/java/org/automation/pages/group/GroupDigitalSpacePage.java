package org.automation.pages.group;

import org.automation.base.BasePage;
import org.automation.pages.digitalspace.DigitalSpacePage;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;
import java.util.logging.Logger;

/**
 * Onglet "Contenu pour les espaces digitaux" pour le module Groupes d'entreprises.
 *
 * Particularite: dans ce module, les sections sont rendues avec un bouton "Afficher" (pas de "Fermer"
 * comme dans Environnements sur certaines versions UI).
 */
public class GroupDigitalSpacePage extends BasePage implements DigitalSpacePage {

    private static final Logger LOGGER = Logger.getLogger(GroupDigitalSpacePage.class.getName());

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";

    private final By digitalSpaceTab = By.xpath(
            "//div[@role='tab']" +
                    "[.//div[contains(text(),'Contenu pour les espaces digitaux')]]" +
                    "[not(contains(@class,'disabled'))][not(@aria-disabled='true')]"
    );

    private static final String ROOT =
            "//div[contains(@class,'q-tab-panel')][.//h2[normalize-space()='Contenu pour les espaces digitaux']]";

    // Generic markers once a section is opened (used for waits and "isSectionOpen").
    private final By addServiceBtn = By.xpath(
            "//span[contains(translate(normalize-space(.),'" + UPPER + "','" + LOWER + "'),'cliquer pour ajouter un nouveau')]"
    );
    private final By addActualiteBtn = By.xpath(
            "//span[contains(translate(normalize-space(.),'" + UPPER + "','" + LOWER + "'),'ajouter une nouvelle actualit')]"
    );
    private final By addFeatureBtn = By.xpath(
            "//span[contains(translate(normalize-space(.),'" + UPPER + "','" + LOWER + "'),'ajouter une nouvelle feature')]"
    );
    private final By anyFeatureCheckbox = By.xpath(
            "//div[contains(@class,'q-tab-panel')]//div[contains(@class,'q-checkbox')]"
    );

    private final By firstDeleteInSection = By.xpath(
            "(//div[contains(@class,'q-tab-panel')]//i[normalize-space(text())='delete'])[1]"
    );

    private String normalizeSectionKey(String sectionTitle) {
        if (sectionTitle == null) return "";
        String s = sectionTitle.toLowerCase().trim();
        if (s.contains("service")) return "services";
        if (s.contains("actualit")) return "actualites";
        if (s.contains("feature")) return "features";
        return s;
    }

    private String sectionLabelPredicate(String sectionKey) {
        switch (sectionKey) {
            case "services":
                return "normalize-space(.)='Services'";
            case "actualites":
                // tolerate accents/encoding issues by matching the stem "actualit"
                return "contains(translate(normalize-space(.),'" + UPPER + "','" + LOWER + "'),'actualit')";
            case "features":
                return "contains(translate(normalize-space(.),'" + UPPER + "','" + LOWER + "'),'feature')";
            default:
                // fallback: match whatever was passed (best effort)
                return "contains(translate(normalize-space(.),'" + UPPER + "','" + LOWER + "'),'" +
                        sectionKey.replace("'", "") + "')";
        }
    }

    private By sectionActionBtn(String sectionTitle) {
        String key = normalizeSectionKey(sectionTitle);
        String pred = sectionLabelPredicate(key);
        return By.xpath(
                ROOT +
                        "//div[contains(@class,'row') and contains(@class,'items-start')]" +
                        "[.//span[" + pred + "]]" +
                        "//div[contains(@class,'cursor-pointer')]" +
                        "[contains(translate(normalize-space(.),'" + UPPER + "','" + LOWER + "'),'affich') " +
                        "or contains(translate(normalize-space(.),'" + UPPER + "','" + LOWER + "'),'ferm')]"
        );
    }

    private By sectionFermerBtn(String sectionTitle) {
        String key = normalizeSectionKey(sectionTitle);
        String pred = sectionLabelPredicate(key);
        return By.xpath(
                ROOT +
                        "//div[contains(@class,'row') and contains(@class,'items-start')]" +
                        "[.//span[" + pred + "]]" +
                        "//div[contains(@class,'cursor-pointer')]" +
                        "[contains(translate(normalize-space(.),'" + UPPER + "','" + LOWER + "'),'ferm')]"
        );
    }

    @Override
    public void clickDigitalSpaceTab() {
        wait.until(ExpectedConditions.elementToBeClickable(digitalSpaceTab));
        scrollAndClick(digitalSpaceTab, "Onglet espaces digitaux (groupe)");
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h2[normalize-space()='Contenu pour les espaces digitaux']")
        ));
    }

    @Override
    public void openSection(String sectionTitle) {
        By action = sectionActionBtn(sectionTitle);
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(action));
        String txt = "";
        try {
            txt = el.getText() == null ? "" : el.getText().toLowerCase().trim();
        } catch (Exception ignored) {}

        if (txt.contains("ferm")) {
            LOGGER.info("Section deja ouverte: " + sectionTitle);
            return;
        }

        scrollAndClick(action, "Afficher section (groupe) " + sectionTitle);
        waitForSectionOpen(sectionTitle);
    }

    @Override
    public void closeSection(String sectionTitle) {
        // Best effort: if UI provides a "Fermer" action in this module/version, click it.
        try {
            List<WebElement> fermer = driver.findElements(sectionFermerBtn(sectionTitle));
            if (fermer.isEmpty()) {
                return;
            }
            scrollAndClick(sectionFermerBtn(sectionTitle), "Fermer section (groupe) " + sectionTitle);
            sleep(300);
        } catch (Exception e) {
            // no-op: this module often doesn't expose a close action.
        }
    }

    @Override
    public boolean isSectionOpen(String sectionTitle) {
        try {
            if (!driver.findElements(sectionFermerBtn(sectionTitle)).isEmpty()) {
                return true;
            }
        } catch (Exception ignored) {}

        String key = normalizeSectionKey(sectionTitle);
        try {
            switch (key) {
                case "services":
                    return !driver.findElements(addServiceBtn).isEmpty();
                case "actualites":
                    return !driver.findElements(addActualiteBtn).isEmpty();
                case "features":
                    return !driver.findElements(addFeatureBtn).isEmpty() || !driver.findElements(anyFeatureCheckbox).isEmpty();
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void deleteFirstItemInCurrentSection() {
        scrollAndClick(firstDeleteInSection, "Supprimer premier element");
    }

    @Override
    public void deleteItemAtIndex(int oneBasedIndex) {
        By nthDelete = By.xpath(
                "(//div[contains(@class,'q-tab-panel')]//i[normalize-space(text())='delete'])[" + oneBasedIndex + "]"
        );
        scrollAndClick(nthDelete, "Supprimer element " + oneBasedIndex);
    }

    private void waitForSectionOpen(String sectionTitle) {
        String key = normalizeSectionKey(sectionTitle);
        try {
            switch (key) {
                case "services":
                    wait.until(ExpectedConditions.or(
                            ExpectedConditions.presenceOfElementLocated(addServiceBtn),
                            ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@class,'q-tab-panel')]//div[contains(@class,'card-container')]"))
                    ));
                    break;
                case "actualites":
                    wait.until(ExpectedConditions.or(
                            ExpectedConditions.presenceOfElementLocated(addActualiteBtn),
                            ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@class,'q-tab-panel')]//div[contains(@class,'card-container')]"))
                    ));
                    break;
                case "features":
                    wait.until(ExpectedConditions.or(
                            ExpectedConditions.presenceOfElementLocated(addFeatureBtn),
                            ExpectedConditions.presenceOfElementLocated(anyFeatureCheckbox)
                    ));
                    break;
                default:
                    // fallback: at least wait for a tab panel to render some content
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(ROOT)));
                    break;
            }
        } catch (Exception e) {
            LOGGER.warning("waitForSectionOpen(" + sectionTitle + "): " + e.getMessage());
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

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}

