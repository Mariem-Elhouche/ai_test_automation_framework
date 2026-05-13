package org.automation.pages.digitalspace;

import org.automation.pages.domain.DomainListPage;
import org.automation.pages.faq.FaqFormPage;

/**
 * Abstraction de l'onglet "Contenu pour les espaces digitaux".
 * L'implémentation varie selon le module (Environnement vs Groupe d'entreprises).
 */
public interface DigitalSpacePage {

    void clickDigitalSpaceTab();

    void openSection(String sectionTitle);

    void closeSection(String sectionTitle);

    default boolean isSectionOpen(String sectionTitle) {
        return false;
    }

    default DomainListPage openDomainSection() {
        throw new UnsupportedOperationException("Section Domaine non supportee pour ce module");
    }

    default FaqFormPage clickAddFaqQuestion() {
        throw new UnsupportedOperationException("Section FAQ non supportee pour ce module");
    }

    default int getFaqQuestionCount() {
        throw new UnsupportedOperationException("Section FAQ non supportee pour ce module");
    }

    void deleteFirstItemInCurrentSection();

    void deleteItemAtIndex(int oneBasedIndex);
}

