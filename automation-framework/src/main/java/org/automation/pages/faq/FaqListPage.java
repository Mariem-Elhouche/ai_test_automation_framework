package org.automation.pages.faq;

import org.automation.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class FaqListPage extends BasePage {

    // Locators pour la page environnement (section FAQ)
    private final By faqSectionTitle = By.xpath("//span[contains(normalize-space(.),'Contenu pour la FAQ')]");
    private final By faqRowsInEnvironment = By.xpath(
            "//span[contains(normalize-space(.),'Contenu pour la FAQ')]" +
                    "/ancestor::div[contains(@class,'row')]/following-sibling::div[1]" +
                    "//div[contains(@class,'card-container')] | " +
                    "//span[contains(normalize-space(.),'Contenu pour la FAQ')]" +
                    "/ancestor::div[contains(@class,'row')]" +
                    "//div[contains(@class,'q-mt-sm')]//div[contains(@class,'card-container')]"
    );

    // Locators pour la page liste /faq
    private final By pageTitle = By.xpath(
            "//h1[contains(normalize-space(.),'FAQ')] | " +
                    "//div[contains(@class,'q-page')]//h2[contains(normalize-space(.),'FAQ')]"
    );
    private final By faqRowsInList = By.xpath("//tbody/tr[.//td]");
    private final By addFaqBtn = By.xpath("//button[.//span[contains(normalize-space(.),'Ajouter')]]");

    // Locators pour la confirmation de suppression
    private final By confirmDialog = By.xpath("//div[@role='dialog']");
    private final By confirmDeleteBtn = By.xpath(
            "//div[@role='dialog']//button[contains(@class,'btn-primary') and .//span[contains(text(),'Valider')]] | " +
                    "//div[@role='dialog']//button[.//span[contains(text(),'Confirmer')]] | " +
                    "//div[@role='dialog']//button[contains(@class,'q-btn--primary')][.//span[contains(text(),'Valider')]]"
    );
    private final By cancelDeleteBtn = By.xpath(
            "//div[@role='dialog']//button[contains(@class,'btn-secondary') and .//span[contains(text(),'Annuler')]] | " +
                    "//div[@role='dialog']//button[.//span[contains(text(),'Annuler')]]"
    );

    public FaqListPage() { super(); }

    /**
     * Attend que la page liste FAQ soit chargée (quand on est sur /faq)
     */
    public FaqListPage waitForFaqListPage() {
        wait.until(ExpectedConditions.urlContains("/faq"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle));
        return this;
    }

    /**
     * Récupère le nombre de FAQ dans l'environnement (section Contenu pour la FAQ)
     */
    public int getFaqCountInEnvironment() {
        try {
            // S'assurer que la section est ouverte
            openFaqSection();
            // Attendre que les cartes FAQ soient visibles
            Thread.sleep(500);
            return driver.findElements(faqRowsInEnvironment).size();
        } catch (Exception e) {
            System.out.println("Erreur comptage FAQ environnement: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Récupère le nombre de FAQ dans la page liste /faq
     */
    public int getFaqCountInList() {
        try {
            return driver.findElements(faqRowsInList).size();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Navigue vers le formulaire de création → retourne FaqFormPage
     */
    public FaqFormPage clickAddFaq() {
        scrollAndClick(addFaqBtn, "Ajouter FAQ");
        return new FaqFormPage().waitUntilLoaded();
    }

    /**
     * Supprime un élément FAQ à l'index donné (1-indexé) dans l'environnement
     */
    public void deleteItemAtIndexInEnvironment(int oneBasedIndex) {
        By nth = By.xpath(
                "(//span[contains(normalize-space(.),'Contenu pour la FAQ')]" +
                        "/ancestor::div[contains(@class,'row')]" +
                        "//i[text()='delete'])[" + oneBasedIndex + "]"
        );
        scrollAndClick(nth, "Supprimer FAQ " + oneBasedIndex);
    }

    /**
     * Supprime le premier élément FAQ dans l'environnement avec confirmation
     */
    public FaqListPage deleteFirstItemWithConfirmationInEnvironment() {
        By firstDelete = By.xpath(
                "(//span[contains(normalize-space(.),'Contenu pour la FAQ')]" +
                        "/ancestor::div[contains(@class,'row')]//i[text()='delete'])[1]"
        );
        scrollAndClick(firstDelete, "Supprimer première FAQ");
        wait.until(ExpectedConditions.visibilityOfElementLocated(confirmDialog));
        return this;
    }

    /**
     * Confirme la suppression dans la boîte de dialogue
     */
    public void confirmDeletion() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(confirmDeleteBtn));
        btn.click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(confirmDialog));
        try { Thread.sleep(500); } catch (InterruptedException e) {}
    }

    /**
     * Annule la suppression dans la boîte de dialogue
     */
    public void cancelDeletion() {
        try {
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(cancelDeleteBtn));
            btn.click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(confirmDialog));
        } catch (TimeoutException e) {
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
            wait.until(ExpectedConditions.invisibilityOfElementLocated(confirmDialog));
        }
    }

    /**
     * Vérifie si la boîte de dialogue de confirmation est affichée
     */
    public boolean isConfirmationDialogDisplayed() {
        try {
            return wait.withTimeout(java.time.Duration.ofSeconds(2))
                    .until(d -> d.findElement(confirmDialog).isDisplayed());
        } catch (Exception e) {
            return false;
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
                return;
            } catch (StaleElementReferenceException e) {
                attempts++;
                try { Thread.sleep(300); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }
        throw new RuntimeException("scrollAndClick échoué : " + label);
    }

    /**
     * Ferme la section FAQ dans la page d'environnement
     */
    public void closeFaqSection() {
        try {
            By closeBtn = By.xpath(
                    "//span[contains(normalize-space(.),'Contenu pour la FAQ')]" +
                            "/ancestor::div[contains(@class,'row')]//div[normalize-space(text())='Fermer']"
            );
            WebElement btn = driver.findElement(closeBtn);
            if (btn.isDisplayed()) {
                btn.click();
                Thread.sleep(500);
            }
        } catch (Exception e) {
            // Déjà fermé
        }
    }

    /**
     * Ouvre la section FAQ dans la page d'environnement
     */
    public void openFaqSection() {
        try {
            By openBtn = By.xpath(
                    "//span[contains(normalize-space(.),'Contenu pour la FAQ')]" +
                            "/ancestor::div[contains(@class,'row')]//div[normalize-space(text())='Afficher']"
            );
            WebElement btn = driver.findElement(openBtn);
            if (btn.isDisplayed()) {
                btn.click();
                Thread.sleep(500);
            }
        } catch (Exception e) {
            // Déjà ouvert
        }
    }
}