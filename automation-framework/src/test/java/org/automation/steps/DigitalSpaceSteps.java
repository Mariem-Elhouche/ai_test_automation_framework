package org.automation.steps;

import io.cucumber.java.en.*;
import org.automation.factory.DriverFactory;
import org.automation.pages.actualites.ActualiteListPage;
import org.automation.pages.digitalspace.DigitalSpacePage;
import org.automation.pages.domain.DomainListPage;
import org.automation.pages.environment.EnvironmentDigitalSpacePage;
import org.automation.pages.faq.FaqFormPage;
import org.automation.pages.features.FeatureListPage;
import org.automation.pages.group.GroupDigitalSpacePage;
import org.automation.pages.services.ServiceListPage;
import org.openqa.selenium.WebDriver;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class DigitalSpaceSteps {

    private DigitalSpacePage page;
    private FaqFormPage faqPage;
    private DomainListPage domainPage;
    private ServiceListPage servicePage;
    private ActualiteListPage actualitePage;
    private FeatureListPage featurePage;

    private static final Logger LOGGER = Logger.getLogger(DigitalSpaceSteps.class.getName());

    public WebDriver getDriver() {
        return DriverFactory.getDriver();
    }

    private DigitalSpacePage getPage() {
        // Important: scenarios can be run in the same JVM across modules (environment + group).
        // Re-resolve the implementation based on the current URL to avoid using a stale page object.
        DigitalSpacePage resolved = resolvePage();
        if (page == null || !page.getClass().equals(resolved.getClass())) {
            page = resolved;
        }
        return page;
    }

    private DigitalSpacePage resolvePage() {
        String url = DriverFactory.getDriver().getCurrentUrl();
        if (url != null && url.contains("/entities/company-groups")) {
            return new GroupDigitalSpacePage();
        }
        return new EnvironmentDigitalSpacePage();
    }

    // ══════════════════════════════════════════════
    //  EXPOSITION DES PAGES SPÉCIFIQUES
    // ══════════════════════════════════════════════

    public DomainListPage getDomainPage() {
        if (domainPage == null) domainPage = getPage().openDomainSection();
        return domainPage;
    }

    public FaqFormPage getFaqPage() {
        return faqPage;
    }

    public ServiceListPage getServicePage() {
        if (servicePage == null) servicePage = new ServiceListPage();
        return servicePage;
    }

    public ActualiteListPage getActualitePage() {
        if (actualitePage == null) actualitePage = new ActualiteListPage();
        return actualitePage;
    }

    public FeatureListPage getFeaturePage() {
        if (featurePage == null) featurePage = new FeatureListPage();
        return featurePage;
    }

    // ══════════════════════════════════════════════
    //  ONGLET
    // ══════════════════════════════════════════════

    @When("the user clicks on the digital space tab")
    public void clickDigitalSpaceTab() {
        getPage().clickDigitalSpaceTab();
    }

    // ══════════════════════════════════════════════
    //  SECTIONS
    // ══════════════════════════════════════════════

    @When("the user opens the {string} section")
    public void openSection(String sectionTitle) {
        getPage().openSection(sectionTitle);

        // Réinitialise la page spécifique si on rouvre la section
        switch (sectionTitle) {
            case "Domaine":
                domainPage = new DomainListPage();
                break;
            case "Contenu pour la FAQ":
                // La page FAQ est gérée via clickAddFaqQuestion
                break;
            case "Services":
                servicePage = new ServiceListPage();
                break;
            case "Actualités":
                actualitePage = new ActualiteListPage();
                break;
            case "Features":
                featurePage = new FeatureListPage();
                break;
            default:
                break;
        }
    }

    @When("the user closes the {string} section")
    public void closeSection(String sectionTitle) {
        getPage().closeSection(sectionTitle);
    }

    @When("the user switches from {string} section to {string} section")
    public void switchSection(String from, String to) {
        getPage().closeSection(from);
        getPage().openSection(to);
    }

    // ══════════════════════════════════════════════
    //  FAQ — NAVIGATION UNIQUEMENT
    // ══════════════════════════════════════════════

    @When("the user clicks on add FAQ question button")
    public void clickAddFaqQuestion() {
        faqPage = getPage().clickAddFaqQuestion();
    }

    @Then("the FAQ question count is {int}")
    public void verifyFaqCount(int expected) {
        assertEquals(expected, getPage().getFaqQuestionCount(),
                "Nombre de questions FAQ incorrect");
    }

    // ══════════════════════════════════════════════
    //  COMMUN
    // ══════════════════════════════════════════════

    @When("the user deletes the first item in the current section")
    public void deleteFirstItem() {
        getPage().deleteFirstItemInCurrentSection();
    }

    @When("the user deletes item at index {int}")
    public void deleteItemAtIndex(int index) {
        getPage().deleteItemAtIndex(index);
    }


    @Then("the current url contains {string}")
    public void verifyUrlContains(String fragment) {
        String currentUrl = DriverFactory.getDriver().getCurrentUrl();
        assertTrue(currentUrl.contains(fragment),
                "L'URL '" + currentUrl + "' ne contient pas '" + fragment + "'");
    }

    @Then("the {string} section is open")
    public void verifySectionOpen(String sectionTitle) {
        assertTrue(getPage().isSectionOpen(sectionTitle),
                "La section '" + sectionTitle + "' devrait être ouverte");
    }

    // ══════════════════════════════════════════════
    //  MÉTHODES DE RAFRAÎCHISSEMENT (utilisées par les Steps spécifiques)
    // ══════════════════════════════════════════════

    public void refreshAndOpenServicesSection() {
        getDriver().navigate().refresh();
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        clickDigitalSpaceTab();
        openSection("Services");
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
    }

    public void refreshAndOpenActualitesSection() {
        getDriver().navigate().refresh();
        try { Thread.sleep(2000); } catch (InterruptedException e) {}
        clickDigitalSpaceTab();
        openSection("Actualités");
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
    }

    public void refreshAndOpenFeaturesSection() {
        getDriver().navigate().refresh();
        try { Thread.sleep(2000); } catch (InterruptedException e) {}
        clickDigitalSpaceTab();
        openSection("Features");
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
    }
}
