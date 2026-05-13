package org.automation.steps;

import io.cucumber.java.en.*;
import org.automation.pages.assure.AssurePage;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AssureSteps {

    private AssurePage assurePage;

    // Stockage du snapshot des stats pour comparaison avant/après filtre
    private Map<String, String> statsSnapshot;

    private AssurePage getPage() {
        if (assurePage == null) {
            assurePage = new AssurePage();
        }
        return assurePage;
    }

    // ══════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════

    @When("the user clicks on the assures tab")
    public void clickAssuresTab() {
        getPage().clickAssuresTab();
    }

    // ══════════════════════════════════════════════
    //  FILTRES — PÉRIODE
    // ══════════════════════════════════════════════

    @Then("the period field displays a valid date range")
    public void verifyPeriodFieldDisplaysValidDateRange() {
        String period = getPage().getPeriodValue();
        // Format attendu : "YYYY-MM-DD - YYYY-MM-DD" ou similaire
        assertTrue(period != null && period.contains("-"),
                "La période devrait contenir un tiret (format date range), valeur : '" + period + "'");
    }

    @When("the user clicks on the calendar icon")
    public void clickCalendarIcon() {
        // Capture les stats avant le changement de période
        statsSnapshot = getPage().captureAllStatValues();
        getPage().clickCalendarIconAndSelectDates();
    }

    @Then("the statistics values have changed")
    public void verifyStatsValuesHaveChanged() {
        Map<String, String> after = getPage().captureAllStatValues();
        assertTrue(getPage().haveStatsChanged(statsSnapshot, after),
                "Au moins une valeur de statistique devrait avoir changé après le changement de période");
    }

    // ══════════════════════════════════════════════
    //  FILTRES — TYPE D'ESPACE
    // ══════════════════════════════════════════════

    @Then("the default selected type espace is {string}")
    public void verifyDefaultSelectedTypeEspace(String expected) {
        String actual = getPage().getSelectedTypeEspace();
        assertEquals(expected, actual,
                "Le type d'espace par défaut devrait être '" + expected + "', mais est '" + actual + "'");
    }

    @When("the user captures the current stats")
    public void captureCurrentStats() {
        statsSnapshot = getPage().captureAllStatValues();
    }

    @When("the user selects type espace {string}")
    public void selectTypeEspace(String option) {
        // Capture les stats avant le changement de filtre
        statsSnapshot = getPage().captureAllStatValues();
        getPage().selectTypeEspace(option);
    }

    @Then("the selected type espace is {string}")
    public void verifySelectedTypeEspace(String expected) {
        String actual = getPage().getSelectedTypeEspace();
        assertEquals(expected, actual,
                "Le type d'espace sélectionné devrait être '" + expected + "', mais est '" + actual + "'");
    }

    @Then("some statistics values have changed compared to before")
    public void verifyStatsChangedAfterFilter() {
        Map<String, String> after = getPage().captureAllStatValues();
        assertTrue(getPage().haveStatsChanged(statsSnapshot, after),
                "Au moins une valeur de statistique devrait avoir changé après le changement de filtre");
    }

    // ══════════════════════════════════════════════
    //  STATISTIQUES
    // ══════════════════════════════════════════════

    @Then("{int} statistics cards are displayed")
    public void verifyStatisticsCardsDisplayed(int expectedCount) {
        int actual = getPage().getStatsCardCount();
        assertEquals(expectedCount, actual,
                "Le nombre de cartes statistiques devrait être " + expectedCount + ", mais est " + actual);
    }

    @Then("the stats card at position {int} shows label {string}")
    public void verifyStatsCardLabel(int position, String expectedLabel) {
        String actual = getPage().getStatLabel(position - 1);
        assertEquals(expectedLabel, actual,
                "Le label de la carte position " + position + " devrait être '" + expectedLabel + "', mais est '" + actual + "'");
    }

    // ══════════════════════════════════════════════
    //  TÉLÉCHARGEMENT
    // ══════════════════════════════════════════════

    @When("the user clicks on the download statistics button")
    public void clickDownloadStatisticsButton() {
        getPage().clickDownloadDropdown();
    }

    @Then("the download dropdown menu is visible")
    public void verifyDownloadDropdownMenuVisible() {
        assertTrue(getPage().isDownloadMenuVisible(),
                "Le menu déroulant de téléchargement devrait être visible");
    }

    @Then("the download menu contains the option {string}")
    public void verifyDownloadMenuContainsOption(String option) {
        assertTrue(getPage().isDownloadOptionPresent(option),
                "Le menu devrait contenir l'option '" + option + "'");
    }

    @When("the user clicks on download option {string}")
    public void clickDownloadOption(String option) {
        getPage().clickDownloadOption(option);
    }

    // ══════════════════════════════════════════════
    //  TABLEAU DES ASSURÉS
    // ══════════════════════════════════════════════

    @Then("the assures table is displayed")
    public void verifyAssuresTableDisplayed() {
        assertTrue(getPage().isTableDisplayed(),
                "Le tableau des assurés devrait être affiché");
    }

    @Then("the assures table has a column {string}")
    public void verifyAssuresTableHasColumn(String columnName) {
        assertTrue(getPage().isColumnPresent(columnName),
                "Le tableau devrait avoir une colonne '" + columnName + "'");
    }

    @Then("the assures table shows at most {int} rows")
    public void verifyAssuresTableRowCount(int maxRows) {
        int actual = getPage().getAssureRowCount();
        assertTrue(actual <= maxRows,
                "Le tableau affiche " + actual + " lignes, max attendu : " + maxRows);
    }

    // ══════════════════════════════════════════════
    //  ICÔNE ŒIL — PAGE DÉTAIL
    // ══════════════════════════════════════════════

    @When("the user clicks the eye icon on the first assure row")
    public void clickEyeIconFirstRow() {
        getPage().clickFirstEyeIcon();
    }

    @Then("the assure detail page is displayed")
    public void verifyAssureDetailPageDisplayed() {
        assertTrue(getPage().isDetailPageDisplayed(),
                "La page de détail assuré devrait être affichée (bouton Accéder ou champs en lecture seule)");
    }

    @Then("the acceder a l'espace button is visible")
    public void verifyAccederButtonVisible() {
        assertTrue(getPage().isAccederButtonVisible(),
                "Le bouton 'Accéder à l'espace' devrait être visible");
    }

    @Then("the entities liees table is displayed")
    public void verifyEntitiesLieesTableDisplayed() {
        assertTrue(getPage().isEntitiesLieesTableDisplayed(),
                "La table des entités liées devrait contenir des lignes");
    }

    // ══════════════════════════════════════════════
    //  PAGINATION
    // ══════════════════════════════════════════════

    @Then("the page info displays {string}")
    public void verifyPageInfo(String expected) {
        String actual = getPage().getPageInfo();
        assertEquals(expected, actual,
                "L'info de pagination devrait afficher '" + expected + "', mais affiche '" + actual + "'");
    }

    @Then("the next button is enabled")
    public void verifyNextButtonEnabled() {
        assertTrue(getPage().isNextButtonEnabled(),
                "Le bouton 'Suivant' devrait être activé");
    }

    @Then("the previous button is disabled")
    public void verifyPreviousButtonDisabled() {
        assertFalse(getPage().isPreviousButtonEnabled(),
                "Le bouton 'Précédent' devrait être désactivé");
    }

    @Then("the next button is disabled")
    public void verifyNextButtonDisabled() {
        assertFalse(getPage().isNextButtonEnabled(),
                "Le bouton 'Suivant' devrait être désactivé");
    }

    @Then("the previous button is enabled")
    public void verifyPreviousButtonEnabled() {
        assertTrue(getPage().isPreviousButtonEnabled(),
                "Le bouton 'Précédent' devrait être activé");
    }

    @When("the user clicks the next button")
    public void clickNextButton() {
        getPage().clickNext();
    }

    @When("the user clicks the previous button")
    public void clickPreviousButton() {
        getPage().clickPrevious();
    }
}