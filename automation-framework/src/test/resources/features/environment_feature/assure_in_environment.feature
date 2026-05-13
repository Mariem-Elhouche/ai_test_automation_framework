@environment @assures @requiresLogin
Feature: Onglet Assurés dans l'environnement

  Background:
    Given the user navigates to the environments list page
    And the user edits the environment "AGRICA"

  # ══════════════════════════════
  #  NAVIGATION
  # ══════════════════════════════

  @smoke
  Scenario: Navigate to assures tab
    When the user clicks on the assures tab
    Then 8 statistics cards are displayed
    And the assures table is displayed

  # ══════════════════════════════
  #  FILTRES — PÉRIODE
  # ══════════════════════════════

  @period
  Scenario: Period field displays a valid date range
    When the user clicks on the assures tab
    Then the period field displays a valid date range

  @period
  Scenario: Changing the period updates the statistics
    When the user clicks on the assures tab
    And the user clicks on the calendar icon
    Then 8 statistics cards are displayed
    And the statistics values have changed

  # ══════════════════════════════
  #  FILTRES — TYPE D'ESPACE
  # ══════════════════════════════

  @type-espace
  Scenario: Default type espace is Tout
    When the user clicks on the assures tab
    Then the default selected type espace is "Tout"

  @type-espace
  Scenario: Filter by iOS updates selected value and statistics
    When the user clicks on the assures tab
    And the user selects type espace "Mobile IOS"
    Then the selected type espace is "Mobile IOS"
    And 8 statistics cards are displayed
    And some statistics values have changed compared to before

  @type-espace
  Scenario: Filter by Android updates selected value and statistics
    When the user clicks on the assures tab
    And the user selects type espace "Mobile Android"
    Then the selected type espace is "Mobile Android"
    And 8 statistics cards are displayed
    And some statistics values have changed compared to before

  @type-espace
  Scenario: Filter by Espace assuré updates selected value and statistics
    When the user clicks on the assures tab
    And the user selects type espace "Espace assuré"
    Then the selected type espace is "Espace assuré"
    And 8 statistics cards are displayed
    And some statistics values have changed compared to before

  # ══════════════════════════════
  #  STATISTIQUES — LABELS
  # ══════════════════════════════

  @smoke
  Scenario: Statistics cards display correct labels
    When the user clicks on the assures tab
    Then the stats card at position 1 shows label "Tentatives de connexions"
    And the stats card at position 2 shows label "Nouveaux assurés"
    And the stats card at position 3 shows label "Connexions réussies"
    And the stats card at position 4 shows label "Assurés total"
    And the stats card at position 5 shows label "Demande de remboursement"
    And the stats card at position 6 shows label "Demande par message"
    And the stats card at position 7 shows label "Changement de RIB"
    And the stats card at position 8 shows label "Téléchargement de wallet"

  # ══════════════════════════════
  #  TÉLÉCHARGEMENT
  # ══════════════════════════════

  @download @smoke
  Scenario: Download button opens dropdown with three options
    When the user clicks on the assures tab
    And the user clicks on the download statistics button
    Then the download dropdown menu is visible
    And the download menu contains the option "Télécharger les statistiques simples"
    And the download menu contains the option "Télécharger les statistiques complètes"
    And the download menu contains the option "Télécharger la liste des assurés"

  @download
  Scenario: Download statistiques simples
    When the user clicks on the assures tab
    And the user clicks on the download statistics button
    And the user clicks on download option "Télécharger les statistiques simples"

  @download
  Scenario: Download statistiques complètes
    When the user clicks on the assures tab
    And the user clicks on the download statistics button
    And the user clicks on download option "Télécharger les statistiques complètes"

  @download
  Scenario: Download liste des assurés
    When the user clicks on the assures tab
    And the user clicks on the download statistics button
    And the user clicks on download option "Télécharger la liste des assurés"

  # ══════════════════════════════
  #  TABLEAU DES ASSURÉS
  # ══════════════════════════════

  @table @smoke
  Scenario: Assures table displays expected columns
    When the user clicks on the assures tab
    Then the assures table has a column "Numéro d'assuré"
    And the assures table has a column "Adresse mail"
    And the assures table has a column "Login"

  @table
  Scenario: Assures table shows at most 8 rows per page
    When the user clicks on the assures tab
    Then the assures table shows at most 8 rows

  # ══════════════════════════════
  #  ICÔNE ŒIL — PAGE DÉTAIL
  # ══════════════════════════════

  @eye-icon @smoke
  Scenario: Eye icon opens assure detail page
    When the user clicks on the assures tab
    And the user clicks the eye icon on the first assure row
    Then the assure detail page is displayed
    And the acceder a l'espace button is visible
    And the entities liees table is displayed

  # ══════════════════════════════
  #  PAGINATION
  # ══════════════════════════════

  @pagination @smoke
  Scenario: First page shows correct pagination state
    When the user clicks on the assures tab
    Then the page info displays "Page 1 sur 2"
    And the previous button is disabled
    And the next button is enabled

  @pagination
  Scenario: Next button navigates to next page
    When the user clicks on the assures tab
    And the user clicks the next button
    Then the page info displays "Page 2 sur 2"
    And the previous button is enabled
    And the next button is disabled

  @pagination
  Scenario: Previous button navigates back to first page
    When the user clicks on the assures tab
    And the user clicks the next button
    And the user clicks the previous button
    Then the page info displays "Page 1 sur 2"
    And the previous button is disabled
