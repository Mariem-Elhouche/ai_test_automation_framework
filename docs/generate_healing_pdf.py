#!/usr/bin/env python3
"""Generate comprehensive self-healing module documentation PDF for soutenance preparation."""

from fpdf import FPDF
import os

OUTPUT_DIR = r"C:\Users\mariem.elhouche-ext\Projets\ai-test-automation-framework\docs"
OUTPUT_PATH = os.path.join(OUTPUT_DIR, "healing-soutenance.pdf")


class PDF(FPDF):
    def __init__(self):
        super().__init__()
        self.set_auto_page_break(auto=True, margin=20)

    def header(self):
        if self.page_no() > 1:
            self.set_font("Helvetica", "I", 7)
            self.set_text_color(120, 120, 120)
            self.cell(0, 5, "AI Test Automation Framework - Module Self-Healing", align="L")
            self.cell(0, 5, f"Page {self.page_no()}/{{nb}}", align="R", new_x="LMARGIN", new_y="NEXT")
            self.line(10, 12, 200, 12)
            self.ln(4)

    def footer(self):
        if self.page_no() == 1:
            return
        self.set_y(-15)
        self.set_font("Helvetica", "I", 7)
        self.set_text_color(150, 150, 150)

    def title_page(self):
        self.add_page()
        self.ln(40)
        self.set_font("Helvetica", "B", 26)
        self.set_text_color(30, 60, 110)
        self.cell(0, 12, "Module Self-Healing", align="C", new_x="LMARGIN", new_y="NEXT")
        self.ln(4)
        self.set_font("Helvetica", "", 16)
        self.set_text_color(80, 80, 80)
        self.cell(0, 10, "AI Test Automation Framework", align="C", new_x="LMARGIN", new_y="NEXT")
        self.ln(6)
        self.set_font("Helvetica", "", 12)
        self.set_text_color(100, 100, 100)
        self.cell(0, 8, "Documentation detaillee pour la soutenance", align="C", new_x="LMARGIN", new_y="NEXT")
        self.cell(0, 8, "Rapporteur et President - Experts en automatisation et IA", align="C", new_x="LMARGIN", new_y="NEXT")
        self.ln(30)
        self.set_font("Helvetica", "I", 10)
        self.set_text_color(130, 130, 130)
        self.cell(0, 6, "Projet NoveoCare", align="C", new_x="LMARGIN", new_y="NEXT")
        self.cell(0, 6, "Mariem El Houche-Ext", align="C", new_x="LMARGIN", new_y="NEXT")

    def chapter_title(self, num, title):
        self.set_font("Helvetica", "B", 16)
        self.set_text_color(30, 60, 110)
        self.ln(4)
        self.cell(0, 10, f"{num}. {title}", new_x="LMARGIN", new_y="NEXT")
        self.line(10, self.get_y(), 200, self.get_y())
        self.ln(4)

    def section_title(self, title):
        self.set_font("Helvetica", "B", 12)
        self.set_text_color(50, 80, 130)
        self.ln(2)
        self.cell(0, 8, title, new_x="LMARGIN", new_y="NEXT")
        self.ln(1)

    def subsection_title(self, title):
        self.set_font("Helvetica", "B", 10)
        self.set_text_color(70, 100, 150)
        self.ln(1)
        self.cell(0, 7, title, new_x="LMARGIN", new_y="NEXT")
        self.ln(1)

    def body_text(self, text):
        self.set_font("Helvetica", "", 9)
        self.set_text_color(40, 40, 40)
        self.multi_cell(0, 5, text)
        self.ln(1)

    def bullet(self, text, indent=10):
        x = self.get_x()
        self.set_x(x + indent)
        self.set_font("Helvetica", "", 9)
        self.set_text_color(40, 40, 40)
        self.cell(4, 5, "-")
        self.multi_cell(0, 5, text)
        self.ln(0.5)

    def qa_item(self, question, answer):
        self.set_font("Helvetica", "B", 9)
        self.set_text_color(180, 60, 30)
        self.cell(0, 6, f"Q: {question}", new_x="LMARGIN", new_y="NEXT")
        self.set_font("Helvetica", "", 9)
        self.set_text_color(40, 40, 40)
        self.multi_cell(0, 5, f"R: {answer}")
        self.ln(2)

    def table_row(self, cells, widths, bold=False, fill=False):
        self.set_font("Helvetica", "B" if bold else "", 7.5)
        self.set_text_color(40, 40, 40)
        if fill:
            self.set_fill_color(220, 230, 240)
        for i, cell in enumerate(cells):
            self.cell(widths[i], 5.5, cell, border=1, fill=fill)
        self.ln()

    def code_block(self, text):
        self.ln(1)
        self.set_font("Courier", "", 7.5)
        self.set_text_color(30, 30, 30)
        self.set_fill_color(240, 240, 245)
        lines = text.split("\n")
        for line in lines:
            self.set_x(self.get_x() + 5)
            self.cell(0, 4.2, line, fill=True, new_x="LMARGIN", new_y="NEXT")
        self.set_font("Helvetica", "", 9)
        self.ln(2)

    def check_page_space(self, needed_mm):
        if self.get_y() + needed_mm > 270:
            self.add_page()


pdf = PDF()
pdf.alias_nb_pages()

# =========== TITLE PAGE ===========
pdf.title_page()

# =========== TABLE OF CONTENTS ===========
pdf.add_page()
pdf.section_title("Table des matieres")
toc = [
    ("1", "Architecture du module self-healing", [
        "1.1 Principe general",
        "1.2 Les 8 classes du package healing",
    ]),
    ("2", "Pipeline de healing complet", [
        "2.1 Capture de snapshot",
        "2.2 Resolution de locator (cache + API + fallback)",
        "2.3 Decorateurs WebDriver et WebElement",
    ]),
    ("3", "Description detaillee des classes", [
        "3.1 HealingLocatorResolver",
        "3.2 HealingWebDriver",
        "3.3 HealingWebElement",
        "3.4 SelfHealingClient",
        "3.5 HealingBaseline",
        "3.6 HealingRequest et HealingResponse",
        "3.7 ElementInfo",
    ]),
    ("4", "Utilitaires (HealingUtils)", [
        "4.1 Conversion By -> Map et Map -> By",
        "4.2 Capture DOM et debug",
        "4.3 Inference de type et coordonnees",
    ]),
    ("5", "Integration Dashboard (DashboardReporter)", [
        "5.1 Push des resultats Cucumber",
        "5.2 Push des evenements de healing",
    ]),
    ("6", "Generation de features via Colab (ColabClient)", [
        "6.1 Principe et flux",
        "6.2 Appel API et extraction JSON",
    ]),
    ("7", "Les 3 modes de guerison detailles", [
        "7.1 Baseline Java",
        "7.2 CI Healing Stub",
        "7.3 Colab (IA complet)",
        "7.4 Tableau comparatif",
    ]),
    ("8", "Flux de donnees healing vers dashboard", [
        "8.1 Diagramme de flux",
        "8.2 Payload des evenements",
        "8.3 Fallback 3 niveaux des metriques",
    ]),
    ("9", "Q&A - Questions possibles", [
        "9.1 Questions sur l'architecture healing",
        "9.2 Questions sur les scores et le matching",
        "9.3 Questions sur la performance et la fiabilite",
        "9.4 Questions avancees (niveau expert)",
    ]),
]
pdf.set_font("Helvetica", "", 9)
pdf.set_text_color(40, 40, 40)
for num, title, subs in toc:
    pdf.set_font("Helvetica", "B", 10)
    pdf.set_text_color(50, 80, 130)
    pdf.cell(0, 6, f"{num}.  {title}", new_x="LMARGIN", new_y="NEXT")
    for sub in subs:
        pdf.set_font("Helvetica", "", 9)
        pdf.set_text_color(60, 60, 60)
        pdf.set_x(20)
        pdf.cell(0, 5, sub, new_x="LMARGIN", new_y="NEXT")
    pdf.ln(1)

# ======================================================================
# PARTIE 1 : ARCHITECTURE DU MODULE SELF-HEALING
# ======================================================================
pdf.add_page()
pdf.chapter_title("1", "Architecture du module self-healing")

pdf.section_title("1.1 Principe general")
pdf.body_text(
    "Le module self-healing est un systeme de reparation automatique des localisateurs "
    "Selenium (locators). Quand un element n'est plus trouve avec le locator d'origine "
    "(par exemple apres une mise a jour de l'interface), le module tente de trouver "
    "automatiquement un nouveau locator fonctionnel, sans intervention humaine."
)
pdf.body_text("Les etapes du processus de healing :")
pdf.bullet("1. Capture de snapshot : au premier acces reussi, le module capture les attributs (id, class, text, position) de l'element.")
pdf.bullet("2. Echec du locator : si le locator d'origine echoue (NoSuchElementException), le module declenche la reparation.")
pdf.bullet("3. Cache baseline : verifie si ce locator a deja ete repare avec succes (fichier healing-baseline.json).")
pdf.bullet("4. Appel API : envoie le DOM courant + le snapshot de l'element a l'API de healing (POST /heal).")
pdf.bullet("5. Retour et stockage : le nouveau locator est stocke en cache et utilise pour les prochains acces.")
pdf.bullet("6. Dashboard : chaque evenement de healing est pousse vers l'API Dashboard pour analyse et metriques.")
pdf.ln(1)

pdf.section_title("1.2 Les 8 classes du package healing")
pdf.body_text("Le package org.automation.ai.healing contient 8 classes Java organisees en 3 couches :")
pdf.ln(1)
layers = [
    ("Classe", "Role", "Couche"),
    ("ElementInfo", "Modele de donnees : snapshot d'un element DOM", "Modele (DTO)"),
    ("HealingRequest", "DTO de requete vers l'API /heal", "Modele (DTO)"),
    ("HealingResponse", "DTO de reponse de l'API /heal", "Modele (DTO)"),
    ("HealingBaseline", "Cache local persistant des reparations reussies", "Cache"),
    ("SelfHealingClient", "Client HTTP vers l'API de healing externe", "Client"),
    ("HealingLocatorResolver", "Orchestrateur du processus de healing", "Service"),
    ("HealingWebDriver", "Decorateur WebDriver avec injection de healing", "Decorateur"),
    ("HealingWebElement", "Decorateur WebElement avec injection de healing", "Decorateur"),
]
wl = [40, 110, 38]
pdf.table_row(layers[0], wl, bold=True, fill=True)
for row in layers[1:]:
    pdf.table_row(row, wl)
pdf.ln(2)
pdf.body_text(
    "A ces 8 classes s'ajoutent 3 classes externes liees : HealingUtils (utilitaires de "
    "conversion et capture DOM), DashboardReporter (push des evenements vers l'API), "
    "et ColabClient (generation de features Gherkin via IA)."
)

# ======================================================================
# PARTIE 2 : PIPELINE DE HEALING COMPLET
# ======================================================================
pdf.add_page()
pdf.chapter_title("2", "Pipeline de healing complet")

pdf.section_title("2.1 Capture de snapshot")
pdf.body_text(
    "Lorsqu'un element est trouve avec succes, HealingLocatorResolver.captureSnapshot() "
    "extrait et stocke les informations suivantes dans un objet ElementInfo :"
)
pdf.bullet("tagName : le type de balise HTML (input, button, a, div...)")
pdf.bullet("visibleText : le texte visible (innerText) de l'element")
pdf.bullet("Attributs : id, class, type, placeholder, aria-label, href, name, role, data-testid, data-cy, tabindex, value")
pdf.bullet("Coordonnees : x, y (getBoundingClientRect()) via JavaScript")
pdf.bullet("Taille : width, height")
pdf.bullet("XPath : construit a partir du type de locator (ex: //*[@id='email'])")
pdf.bullet("isRowRelative : true si le xpath est relatif (commence par .//)")
pdf.body_text(
    "Le snapshot est stocke dans un ConcurrentHashMap<clé, ElementInfo> avec une cle composee "
    "de l'URL de la page + locator + contexte (driver ou element parent)."
)
pdf.code_block(
    "Exemple de cle de cache :\n"
    "  \"driver::app.example.com::By.xpath://input[@id='email']\"\n"
    "\n"
    "Exemple de snapshot stocke :\n"
    "  ElementInfo {\n"
    "    elementId = \"email\",\n"
    "    elementType = \"input\",\n"
    "    text = \"Adresse e-mail\",\n"
    "    attributes = {type=\"email\", placeholder=\"ex: user@example.com\"},\n"
    "    xpath = \"//*[@id='email']\",\n"
    "    coordinates = {x=120.0, y=350.0, width=300.0, height=40.0}\n"
    "  }"
)

pdf.section_title("2.2 Resolution de locator")
pdf.body_text("La methode resolveLocator() est le coeur du pipeline de healing. Voici son algorithme :")
pdf.code_block(
    "resolveLocator(driver, scope, failedLocator, firstFailure) :\n"
    "\n"
    "1. Verifier si HEALING_ENABLED = true\n"
    "2. Construire la cle de cache + nom logique\n"
    "3. LOG: \"Locator failed for '...' : ...\"\n"
    "4. Mesurer le temps de healing (start = now)\n"
    "5. Appeler callHealingAPI(driver, locator, logicalName, cacheKey)\n"
    "    5a. driver.getPageSource() -> DOM courant\n"
    "    5b. oldLocatorMap = convertByToMap(failedLocator)\n"
    "    5c. oldElement = snapshotCache.get(cacheKey)\n"
    "         sinon = buildElementInfoFallback(locator, logicalName)\n"
    "    5d. Construire HealingRequest\n"
    "    5e. healingClient.healSelector(request) -> POST /heal\n"
    "6. pushHealingEvent() -> POST /api/healing-events\n"
    "7. Si response == null ou !success -> retourner null (echec)\n"
    "8. buildByFromResponse(newLocator) -> By\n"
    "9. Stocker dans healedLocatorsCache\n"
    "10. LOG: \"Self-healing succeeded\"\n"
    "11. Retourner le nouveau By"
)
pdf.body_text(
    "Le cache healedLocatorsCache est un ConcurrentHashMap qui stocke les locators "
    "deja reussis pour eviter de rappeler l'API a chaque echec ulterieur du meme element. "
    "getPreferredLocator() consulte ce cache en priorite avant d'utiliser le locator original."
)

pdf.section_title("2.3 Decorateurs WebDriver et WebElement")
pdf.body_text(
    "HealingWebDriver et HealingWebElement sont des decorateurs du pattern Decorator de Selenium. "
    "Ils implementent les interfaces WebDriver/WebElement en deleguant toutes les methodes "
    "a l'instance reelle, sauf findElement() et findElements() qui ajoutent la logique de healing."
)
pdf.body_text("Algorithme de HealingWebDriver.findElement(By) :")
pdf.code_block(
    "1. preferred = resolver.getPreferredLocator(driver, scope, by)\n"
    "2. Essayer delegate.findElement(preferred)\n"
    "    -> OK : capturer snapshot, retourner HealingWebElement wrappe\n"
    "    -> NoSuchElementException / StaleElementReferenceException :\n"
    "3. Si preferred != original : essayer delegate.findElement(by)\n"
    "    -> OK : capturer snapshot, wrapper\n"
    "    -> Exception : continuer\n"
    "4. healed = resolver.resolveLocator(driver, scope, by, firstFailure)\n"
    "    -> null : re-lancer l'exception originale\n"
    "    -> OK : delegate.findElement(healed), wrapper"
)
pdf.body_text(
    "Le decorateur assure que le healing est totalement transparent pour les Page Objects : "
    "ceux-ci continuent a utiliser les memes By locators, et le decorateur intercepte "
    "automatiquement les echecs pour tenter une reparation."
)

# ======================================================================
# PARTIE 3 : DESCRIPTION DETAILLEE DES CLASSES
# ======================================================================
pdf.add_page()
pdf.chapter_title("3", "Description detaillee des classes")

pdf.section_title("3.1 HealingLocatorResolver")
pdf.body_text(
    "Classe centrale qui orchestre tout le processus de healing. 333 lignes."
)
pdf.ln(1)
methods_hlr = [
    ("Methode", "Description"),
    ("getPreferredLocator()", "Retourne le locator en cache ou l'original"),
    ("captureSnapshot()", "Extrait les attributs de l'element et stocke dans snapshotCache"),
    ("resolveLocator()", "Pipeline principal : cache -> API -> fallback"),
    ("callHealingAPI()", "Construit la requete et appelle SelfHealingClient"),
    ("pushHealingEvent()", "Extrait les scores et pousse via DashboardReporter"),
    ("isSameLocator()", "Compare deux locators via toString()"),
    ("buildLogicalName()", "Construit le nom logique 'driver::url::By'"),
    ("cacheKey()", "Cle unique driver::url::By ou elem::url::signature"),
    ("scopeDescription()", "Decrit le contexte (driver / element avec tag+id+class+text)"),
    ("debugHealingRequestPayload()", "Sauvegarde les fichiers de debug dans target/healing-debug/"),
]
pdf.table_row(methods_hlr[0], [45, 145], bold=True, fill=True)
for row in methods_hlr[1:]:
    pdf.table_row(row, [45, 145])
pdf.ln(2)
pdf.body_text(
    "Proprietes statiques : HEALING_ENABLED (configurable via self.healing.enabled), "
    "HEALING_DEBUG_REQUEST (sauvegarde des payloads de debug), "
    "healedLocatorsCache (ConcurrentHashMap<cle, By>), "
    "elementSnapshotCache (ConcurrentHashMap<cle, ElementInfo>)."
)

pdf.section_title("3.2 HealingWebDriver")
pdf.body_text(
    "Implemente WebDriver, JavascriptExecutor, TakesScreenshot, WrapsDriver. "
    "268 lignes. Decore un WebDriver standard pour injecter la logique de healing "
    "dans les methodes findElement et findElements."
)
pdf.body_text("Methodes principales :")
pdf.bullet("findElement(By) : 3 tentatives : cache -> original -> healing, retourne un HealingWebElement")
pdf.bullet("findElements(By) : meme logique, retourne une liste de HealingWebElement")
pdf.bullet("switchTo().frame() : retourne HealingWebDriver.this pour chainer les appels")
pdf.bullet("executeScript() : unwrap automatiquement les HealingWebElement en WebElements reels")
pdf.bullet("getScreenshotAs() : delegue a ((TakesScreenshot) delegate)")
pdf.ln(1)
pdf.body_text(
    "Le unwrap des arguments JavaScript est crucial : Selenium ne peut pas executer "
    "de script sur des objets HealingWebElement car ils ne sont pas des WebElements reels. "
    "HealingWebDriver unwrap recursive tous les HealingWebElement, List, Map et arrays "
    "dans les arguments avant de les passer a JavascriptExecutor."
)

pdf.section_title("3.3 HealingWebElement")
pdf.body_text(
    "Implemente WebElement et WrapsElement. 192 lignes. Meme principe que HealingWebDriver "
    "mais pour les elements fils : findElement et findElements sur un scope d'element "
    "(par exemple la recherche dans un bloc <div>) declenchent aussi le healing."
)
pdf.body_text(
    "Contrairement a HealingWebDriver, HealingWebElement conserve une reference au "
    "rootDriver et au resolver. Les methodes de base (click, sendKeys, getText, etc.) "
    "sont simplement deleguees a l'element real delegate."
)

pdf.section_title("3.4 SelfHealingClient")
pdf.body_text(
    "Client HTTP vers l'API de healing externe. 136 lignes. Utilise java.net.http.HttpClient "
    "avec HTTP/1.1 (plus stable avec ngrok/Colab que HTTP/2)."
)
pdf.body_text("Caracteristiques :")
pdf.bullet("URL resolue depuis self.healing.api.url ou colab.url avec /heal ajoute automatiquement")
pdf.bullet("Timeout configurable : self.healing.api.timeout (default 10s)")
pdf.bullet("Retry automatique : jusqu'a 2 tentatives pour les erreurs transport (GOAWAY, RST_STREAM, connection reset, broken pipe)")
pdf.bullet("Header 'ngrok-skip-browser-warning: true' requis par ngrok")
pdf.bullet("DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES = false pour ignorer les champs inattendus")
pdf.bullet("En cas d'erreur, retourne une HealingResponse avec success=false et le message d'erreur")
pdf.code_block(
    "healSelector(HealingRequest) :\n"
    "  1. Serialise la requete en JSON\n"
    "  2. POST vers {api_url}/heal\n"
    "  3. sendWithRetry() : 2 tentatives max avec backoff 300ms\n"
    "  4. Si status != 200 -> failureResponse\n"
    "  5. Deserialise la reponse en HealingResponse\n"
    "  6. LOG si l'API a retourne une erreur metier\n"
    "  7. Retourne la HealingResponse"
)

pdf.section_title("3.5 HealingBaseline")
pdf.body_text(
    "Cache local persistant des reparations deja reussies. 106 lignes. "
    "Utilise un ConcurrentHashMap<String, BaselineEntry> et un fichier JSON "
    "de persistances cible/target/healing-baseline.json."
)
pdf.body_text("Mecanismes :")
pdf.bullet("Chargement au demarrage (static initializer) depuis le fichier JSON")
pdf.bullet("Sauvegarde automatique a l'arret via Runtime.getRuntime().addShutdownHook()")
pdf.bullet("Cle : {locator_type}|{locator_value}|{page_context} (cacheKey de HealingLocatorResolver)")
pdf.bullet("stocke : origType, origValue, healedType, healedValue, exceptionType, structuralScore, semanticScore")
pdf.bullet("Utilise par le pipeline : avant d'appeler l'API /heal, le code pourrait verifier la baseline")
pdf.code_block(
    "Exemple d'entree baseline :\n"
    "{\n"
    "  \"driver::app.example.com::By.xpath://input[@id='email']\": {\n"
    "    \"origType\": \"xpath\",\n"
    "    \"origValue\": \"//input[@id='email']\",\n"
    "    \"healedType\": \"xpath\",\n"
    "    \"healedValue\": \"//input[@name='username']\",\n"
    "    \"exceptionType\": \"NoSuchElementException\",\n"
    "    \"structuralScore\": 0.88,\n"
    "    \"semanticScore\": 0.95\n"
    "  }\n"
    "}"
)

pdf.section_title("3.6 HealingRequest et HealingResponse")
pdf.body_text(
    "Deux DTOs (Data Transfer Objects) pour la communication avec l'API de healing externe."
)
pdf.ln(1)
pdf.subsection_title("HealingRequest (63 lignes)")
pdf.bullet("old_locator (Map<String, String>) : type + valeur du locator qui a echoue (ex: {type: 'id', value: 'email'})")
pdf.bullet("old_element (ElementInfo) : snapshot de l'element capture lors du dernier acces reussi")
pdf.bullet("current_dom (String) : HTML brut de la page au moment de l'echec (via driver.getPageSource())")
pdf.bullet("run_id (String) : identifiant du run de test associe")
pdf.ln(1)
pdf.subsection_title("HealingResponse (75 lignes)")
pdf.bullet("success (boolean) : succes ou echec de la reparation")
pdf.bullet("new_locator (Map<String, String>) : nouveau locator propose (ex: {type: 'name', value: 'username'})")
pdf.bullet("score (double) : score de confiance du nouveau locator (0-1)")
pdf.bullet("details (Map<String, Object>) : informations supplementaires (structural_score, semantic_score, pipeline counts)")
pdf.bullet("error (String) : message d'erreur si echec")
pdf.ln(1)
pdf.body_text(
    "L'annotation @JsonIgnoreProperties(ignoreUnknown = true) sur HealingResponse permet "
    "de tolerer des champs supplementaires dans la reponse JSON sans casser la deserialisation."
)

pdf.section_title("3.7 ElementInfo")
pdf.body_text(
    "Modele de snapshot d'un element DOM. 159 lignes. Stocke toutes les informations "
    "necessaires pour identifier et retrouver un element apres un changement de l'interface."
)
pdf.ln(1)
ei_cols = [
    ("Champ", "Type Java", "Annotation JSON", "Description"),
    ("elementId", "String", "@JsonProperty('element_id')", "ID stable de l'element (exclut les IDs auto-generes)"),
    ("elementType", "String", "@JsonProperty('element_type')", "Tag HTML (input, button, a...)"),
    ("text", "String", "-", "Texte visible ou innerText"),
    ("attributes", "Map<String, String>", "-", "Attributs HTML (id, class, placeholder...)"),
    ("xpath", "String", "-", "XPath construit depuis le locator"),
    ("isRowRelative", "boolean", "@JsonProperty('is_row_relative')", "True si xpath relatif (.//)"),
    ("coordinates", "Map<String, Double>", "-", "x, y (getBoundingClientRect)"),
    ("size", "Map<String, Double>", "-", "width, height"),
]
pdf.table_row(ei_cols[0], [30, 35, 47, 78], bold=True, fill=True)
for row in ei_cols[1:]:
    pdf.table_row(row, [30, 35, 47, 78])
pdf.ln(2)
pdf.body_text(
    "La methode statique fromMap() permet de reconstruire un ElementInfo a partir "
    "d'une Map<String, Object> (utilisee lors de la deserialisation depuis le cache "
    "ou l'API colab). Les methodes castStringMap() et castNumericMap() gerent "
    "les conversions de type en toute securite."
)

# ======================================================================
# PARTIE 4 : UTILITAIRES
# ======================================================================
pdf.add_page()
pdf.chapter_title("4", "Utilitaires (HealingUtils)")

pdf.section_title("4.1 Conversion By -> Map et Map -> By")
pdf.body_text(
    "HealingUtils fournit les methodes de conversion entre les objets Selenium By "
    "et les Map<String, String> utilisees pour la serialisation JSON."
)
pdf.ln(1)
pdf.subsection_title("convertByToMap(By) -> Map<String, String>")
pdf.bullet("Analyse le toString() du By (ex: 'By.xpath://input[@id='email']')")
pdf.bullet("Supporte 8 types : id, name, xpath, css, className, tagName, linkText, partialLinkText")
pdf.bullet("Retourne {type: 'xpath', value: '//input[@id=\"email\"]'}")
pdf.ln(1)
pdf.subsection_title("buildByFromResponse(Map<String, String>) -> By")
pdf.bullet("Recherche le type parmi 4 alias : type, strategy, locatorType, locator_type")
pdf.bullet("Recherche la valeur parmi 3 alias : value, selector, locator")
pdf.bullet("Fallback : si type absent, detecte la cle presente (xpath -> type=xpath, value=...)")
pdf.bullet("Supporte data-testid comme cssSelector : By.cssSelector('[data-testid=\"...\"]')")
pdf.ln(1)
pdf.subsection_title("normalizeLocatorType(String)")
pdf.bullet("Normalise les variantes : 'cssselector'/'css_selector'/'css' -> 'css'")
pdf.bullet("Idem pour className, tagName, linkText, partialLinkText, data-testid")

pdf.section_title("4.2 Capture DOM et debug")
pdf.body_text("Methode writeDebugFiles() : sauvegarde les payloads de healing dans target/healing-debug/")
pdf.bullet("Fichier {timestamp}-{logicalName}-request.json : la requete complete vers /heal")
pdf.bullet("Fichier {timestamp}-{logicalName}-current-dom.html : le DOM brut de la page")
pdf.bullet("Fichier {timestamp}-{logicalName}-old-locator.json : le locator qui a echoue")
pdf.bullet("Fichier {timestamp}-{logicalName}-old-element.json : le snapshot de l'element")
pdf.ln(1)
pdf.body_text(
    "Ces fichiers de debug sont generes uniquement si self.healing.debug.request=true "
    "(par defaut). Ils sont extremement utiles pour diagnostiquer les echecs de healing "
    "en comparant le DOM et les snapshots."
)

pdf.section_title("4.3 Inference de type et coordonnees")
pdf.body_text("Methodes auxiliaires :")
pdf.bullet("inferElementTypeFromLogicalName(String) : deduit le type HTML depuis le nom logique. Ex: 'bouton'/'button'/'click' -> 'button', 'champ'/'input'/'email'/'password' -> 'input', 'icon'/'icone' -> 'i'.")
pdf.bullet("extractCoordinates(WebDriver, WebElement) : execute JavaScript getBoundingClientRect() pour obtenir x, y, width, height.")
pdf.bullet("buildElementInfoFallback(By, logicalName) : construit un ElementInfo minimal a partir du locator seul (utilise quand le snapshot fait defaut).")
pdf.bullet("parseXpathTag(String) : extrait le tag HTML d'un xpath (ex: //input[@id='x'] -> 'input').")
pdf.bullet("parseXpathAttributes(String, ElementInfo, Map) : extrait les attributs d'un xpath (ex: @id='email' -> id=email).")
pdf.bullet("buildSnapshotAttributes(WebElement, Map) : extrait 15 attributs standards d'un element Selenium.")
pdf.bullet("getStableId(Map<String, String>) : retourne l'id seulement s'il n'est pas auto-genere (pattern f_<uuid>).")

# ======================================================================
# PARTIE 5 : INTEGRATION DASHBOARD
# ======================================================================
pdf.add_page()
pdf.chapter_title("5", "Integration Dashboard (DashboardReporter)")

pdf.section_title("5.1 Push des resultats Cucumber")
pdf.body_text(
    "DashboardReporter.push(String cucumberJsonPath) lit le fichier cucumber.json "
    "genere par Maven et pousse les resultats de chaque scenario vers le Dashboard API."
)
pdf.code_block(
    "1. Lire le fichier cucumber.json\n"
    "2. Pour chaque feature -> elements (scenarios) :\n"
    "   - Extraire feature_name, scenario_name\n"
    "   - computeStatus(before, steps, after) :\n"
    "       - 'failed' si un step/hook a failed\n"
    "       - 'skipped' si tous sont skipped/pending\n"
    "       - 'passed' sinon\n"
    "   - computeDuration(before, steps, after) : somme des durees\n"
    "   - Extraire les tags\n"
    "3. POST /api/cucumber-runs avec la liste de scenarios\n"
    "4. Headers : Content-Type: application/json, X-API-Key si configuré"
)

pdf.section_title("5.2 Push des evenements de healing")
pdf.body_text(
    "DashboardReporter.pushHealingEvent() pousse chaque tentative de healing vers le Dashboard."
)
pdf.body_text("Parametres (15+) :")
pdf.bullet("success (boolean), score (double), structural_score, semantic_score")
pdf.bullet("old_locator_type, old_locator_val, new_locator_type, new_locator_val")
pdf.bullet("error_message, healing_time_ms, exception_type, baseline_hit")
pdf.bullet("elements_extracted, after_struct_filter, after_spatial_filter, sent_to_nlp")
pdf.bullet("run_id (inferé depuis DASHBOARD_RUN_ID env ou date courante)")
pdf.ln(1)
pdf.body_text(
    "Les champs de comptage du pipeline (elements_extracted, after_struct_filter, "
    "after_spatial_filter, sent_to_nlp) sont envoyes optionnellement (nullable). "
    "Ils permettent de tracer l'efficacite de chaque etape du pipeline de healing "
    "cote IA (Colab)."
)

# ======================================================================
# PARTIE 6 : GENERATION DE FEATURES VIA COLAB
# ======================================================================
pdf.add_page()
pdf.chapter_title("6", "Generation de features via Colab (ColabClient)")

pdf.section_title("6.1 Principe et flux")
pdf.body_text(
    "ColabClient est un pont entre les user stories (format texte) et les fichiers "
    ".feature Gherkin generes par l'IA (Mistral LLM heberge sur Google Colab)."
)
pdf.code_block(
    "Flux de generation :\n"
    "\n"
    "1. Fichier user story (ex: login_us.txt)\n"
    "   Contenu : format texte libre (As a..., I want to..., So that...)\n"
    "\n"
    "2. ColabClient.generateFeatureFromFile()\n"
    "   - Lit le fichier user story\n"
    "   - Appelle l'API Colab via POST /generate\n"
    "   - Recupere le contenu Gherkin genere par le LLM\n"
    "\n"
    "3. Fichier .feature (ex: login.feature)\n"
    "   - Ecrit dans test/resources/features/\n"
    "   - Contenu : Gherkin standard (Feature:, Scenario:, Given/When/Then)\n"
    "\n"
    "URL Colab : configurable via system property 'colab.url'\n"
    "  (defaut: https://epigastric-troy-calculating.ngrok-free.dev)"
)

pdf.section_title("6.2 Appel API et extraction JSON")
pdf.body_text(
    "La methode callColabAPI() envoie la user story a l'API Flask et recupere la reponse."
)
pdf.bullet("Timeout : 30s connexion, 3 minutes reponse (Mistral peut etre lent)")
pdf.bullet("Header 'ngrok-skip-browser-warning: true' requis par ngrok")
pdf.bullet("Body : {\"user_story\": \"...\"} (JSON avec echappement manuel)")
pdf.bullet("Reponse : JSON avec champ 'content' contenant le Gherkin genere")
pdf.ln(1)
pdf.body_text(
    "L'extraction du champ 'content' est faite manuellement (sans bibliotheque JSON) "
    "pour eviter une dependance Jackson/Gson. L'echappement et le desechappement "
    "gerent les caracteres speciaux (\\n, \\\", \\\\, \\t, \\r)."
)

# ======================================================================
# PARTIE 7 : LES 3 MODES DE GUERISON
# ======================================================================
pdf.add_page()
pdf.chapter_title("7", "Les 3 modes de guerison detailles")

pdf.section_title("7.1 Baseline Java (HealingBaseline)")
pdf.body_text(
    "Le mode Baseline est le plus rapide car il ne necessite aucun parsing DOM ni "
    "appel reseau. Il consiste en un cache memoire (ConcurrentHashMap) persiste "
    "dans target/healing-baseline.json."
)
pdf.bullet("Cle : {locator_type}|{locator_value}|{page_context} (cacheKey)")
pdf.bullet("Stocke : le resultat de la derniere reparation reussie pour ce locator")
pdf.bullet("Persistance : charge au demarrage, sauvegarde a l'arret (shutdown hook)")
pdf.bullet("Utilisation : si un locator echoue, le code verifie d'abord la baseline avant d'appeler l'API")

pdf.section_title("7.2 CI Healing Stub")
pdf.body_text(
    "Le CI Healing Stub est un serveur Python standalone (ou endpoint /heal integre "
    "dans le dashboard API) qui effectue un matching base sur des regles simples :"
)
pdf.bullet("Parse le DOM avec html.parser.HTMLParser (stdlib Python, pas de ML)")
pdf.bullet("Matching par texte exact (1.0), texte partiel (0.8), attributs (0.7 a 0.9)")
pdf.bullet("Traduction FR/EN (0.75), bonus si balise correspond (+0.1)")
pdf.bullet("Pas de modele NLP, pas de base de donnees")
pdf.bullet("Score toujours a 0.95 en cas de succes")

pdf.section_title("7.3 Colab (IA complet)")
pdf.body_text(
    "Le mode Colab execute un pipeline NLP complet en 5 etapes via un notebook "
    "heberge sur Google Colab avec acces GPU."
)
pdf.code_block(
    "Pipeline Colab (self_healing_v5.ipynb) :\n"
    "\n"
    "1. Baseline Python : lookup par MD5(type|valeur|hash du DOM)\n"
    "\n"
    "2. Extraction DOM : BeautifulSoup + lxml\n"
    "   - Filtre les elements interactifs (input, button, a, select...)\n"
    "   - Extrait les attributs (id, name, class, placeholder...)\n"
    "\n"
    "3. Similarite structurelle (poids 20% tag + 50% attributs + 30% contexte)\n"
    "   - Tag identique ? Attributs communs ? Contexte DOM parent identique ?\n"
    "\n"
    "4. Similarite semantique (poids 0.6 dans le score final)\n"
    "   - Modele : SentenceTransformer paraphrase-multilingual-mpnet-base-v2\n"
    "   - Multilingue : support FR/EN\n"
    "\n"
    "5. Similarite spatiale\n"
    "   - Distance euclidienne normalisee entre les coordonnees x, y\n"
    "\n"
    "Score final = 0.4 * structurel + 0.6 * semantique\n"
    "Seuil de succes : 0.75"
)
pdf.body_text(
    "Le Colab pousse aussi des metrics_snapshots vers le Dashboard via "
    "DashboardPusher.push_metrics_snapshot() (POST /api/metrics)."
)

pdf.section_title("7.4 Tableau comparatif")
pdf.ln(1)
cmp = [
    ("Critere", "Baseline Java", "CI Stub", "Colab IA"),
    ("Parsing DOM", "Aucun", "html.parser stdlib", "BeautifulSoup + lxml"),
    ("Modele ML", "Aucun", "Aucun", "SentenceTransformer"),
    ("Matching", "Lookup exact", "Texte + attributs", "Struct + Sem + Spatial"),
    ("Score", "Stocke depuis heal", "0.95 force", "0.4*struct + 0.6*sem"),
    ("Metrics", "healing_event", "Aucune", "healing_event + snapshot"),
    ("Performance", "Instantané (ms)", "< 200ms", "1-5s (GPU)"),
    ("Usage", "Cache local", "CI sans GPU", "Production"),
]
wc = [32, 42, 42, 72]
pdf.table_row(cmp[0], wc, bold=True, fill=True)
for row in cmp[1:]:
    pdf.table_row(row, wc)

# ======================================================================
# PARTIE 8 : FLUX DE DONNEES
# ======================================================================
pdf.add_page()
pdf.chapter_title("8", "Flux de donnees healing vers dashboard")

pdf.section_title("8.1 Diagramme de flux")
pdf.code_block(
    "HealingWebDriver.findElement(By)\n"
    "        |\n"
    "        +-- [REUSSI] -> captureSnapshot() -> elementSnapshotCache\n"
    "        |\n"
    "        +-- [ECHEC] NoSuchElementException\n"
    "              |\n"
    "              +-- [BASELINE HIT] HealingBaseline.lookup()\n"
    "              |     +-- pushHealingEvent(success, baseline_hit=true)\n"
    "              |\n"
    "              +-- [BASELINE MISS] SelfHealingClient.healSelector()\n"
    "                    |\n"
    "                    +-- POST /heal -> CI Stub ou Colab\n"
    "                    |     <- new_locator, score, details\n"
    "                    |\n"
    "                    +-- pushHealingEvent()\n"
    "                    |     +-- POST /api/healing-events\n"
    "                    |\n"
    "                    +-- healedLocatorsCache.put(cle, new By)\n"
    "\n"
    "DashboardReporter.push() (fin du run)\n"
    "  +-- POST /api/cucumber-runs\n"
    "\n"
    "Colab (optionnel)\n"
    "  +-- POST /api/metrics -> metrics_snapshots"
)

pdf.section_title("8.2 Payload des evenements de healing")
pdf.body_text("Exemple de payload envoye a POST /api/healing-events :")
pdf.code_block(
    "{\n"
    "  \"success\": true,\n"
    "  \"score\": 0.92,\n"
    "  \"old_locator_type\": \"xpath\",\n"
    "  \"old_locator_val\": \"//input[@id='email']\",\n"
    "  \"new_locator_type\": \"xpath\",\n"
    "  \"new_locator_val\": \"//input[@name='username']\",\n"
    "  \"error_message\": null,\n"
    "  \"healing_time_ms\": 320,\n"
    "  \"exception_type\": \"NoSuchElementException\",\n"
    "  \"baseline_hit\": false,\n"
    "  \"elements_extracted\": 45,\n"
    "  \"after_struct_filter\": 12,\n"
    "  \"after_spatial_filter\": 8,\n"
    "  \"sent_to_nlp\": 5,\n"
    "  \"structural_score\": 0.88,\n"
    "  \"semantic_score\": 0.95,\n"
    "  \"run_id\": \"run-20260518-143022\"\n"
    "}"
)

pdf.section_title("8.3 Fallback 3 niveaux des metriques cote Dashboard")
pdf.body_text(
    "La methode resolve_healing_payload() du DashboardService implemente un fallback "
    "en 3 etapes pour fournir les metriques de healing meme en l'absence de snapshots :"
)
pdf.code_block(
    "Niveau 1 : metrics_snapshots (table PostgreSQL)\n"
    "  - SELECT * FROM metrics_snapshots WHERE run_id = ?\n"
    "  - ORDER BY captured_at DESC LIMIT 1\n"
    "  - Si trouve -> retourne les metriques directement\n"
    "\n"
    "Niveau 2 : healing_events (table PostgreSQL)\n"
    "  - SELECT COUNT(*), SUM(success), AVG(score) FROM healing_events\n"
    "  - WHERE run_id = ?\n"
    "  - Calcule healing_rate = successful/total\n"
    "  - baseline_hit_rate = baseline_hits/total\n"
    "  - avg_final_score = AVG(score)\n"
    "\n"
    "Niveau 3 : endpoint externe (SELF_HEALING_METRICS_URL)\n"
    "  - GET /metrics (Colab ou stub)\n"
    "  - Timeout configurable (5s par defaut)\n"
    "  - Normalise via normalize_external_metrics()"
)

# ======================================================================
# PARTIE 9 : Q&A
# ======================================================================
pdf.add_page()
pdf.chapter_title("9", "Q&A - Questions possibles")
pdf.body_text(
    "Cette section contient les questions probables lors de la soutenance, "
    "classees par theme et par niveau de difficulte."
)
pdf.ln(2)

pdf.section_title("9.1 Questions sur l'architecture healing")

pdf.qa_item(
    "Pourquoi un Decorateur WebDriver plutot qu'un EventListener (WebDriverListener) ?",
    "Un WebDriverListener (Selenium 4) permet d'intercepter les evenements avant/apres "
    "chaque appel, mais ne permet pas de modifier le comportement de findElement "
    "(on ne peut pas changer le By apres un echec). Le Decorateur nous donne le controle "
    "total sur le flux : on peut essayer le cache, puis l'original, puis le healing, "
    "et wrapper les resultats en HealingWebElement. C'est plus flexible mais necessite "
    "d'implementer toutes les methodes de WebDriver (268 lignes au total, dont beaucoup "
    "sont de simples delegations)."
)

pdf.qa_item(
    "Pourquoi deux caches (healedLocators et elementSnapshot) ?",
    "healedLocatorsCache stocke les By reussis pendant la session (cle -> By). "
    "elementSnapshotCache stocke les snapshots d'elements (cle -> ElementInfo). "
    "Le premier evite de rappeler l'API, le second fournit les donnees de l'element "
    "original (attributs, coordonnees) a l'API de healing pour le matching. "
    "Les deux sont independants car un By peut etre en cache sans snapshot "
    "(si captureSnapshot n'a pas pu extraire les donnees) et vice-versa."
)

pdf.qa_item(
    "Comment est calculee la cle de cache (cacheKey) ?",
    "La cle est construite par scopeDescription(driver, scope) + '::' + locator. "
    "Pour un scope driver : 'driver::' + host (ex: driver::app.example.com). "
    "Pour un scope element : 'elem::' + host + '::' + hex(hash(tag|id|class|text)). "
    "Le hash hex permet de differencier les elements parents memes sans ID unique. "
    "En cas d'element stale, le fallback est 'elem::' + host + '::stale'."
)

pdf.qa_item(
    "Quel est l'impact du healing sur les performances des tests ?",
    "L'impact est negligeable en cas de succes (captureSnapshot apres findElement "
    "reussi ajoute ~5ms). En cas d'echec, le cout est plus eleve : getPageSource() "
    "(DOM complet) + POST /heal (1-5s avec Colab, <200ms avec stub) + "
    "POST /api/healing-events. Si le healing est declare prioritairement dans les "
    "configs (self.healing.api.url pointe vers le stub rapide), le temps total reste "
    "sous les 500ms. Le cache healedLocators evite les appels repetitifs."
)

pdf.section_title("9.2 Questions sur les scores et le matching")

pdf.qa_item(
    "Que signifient les 4 compteurs du pipeline (elements_extracted, after_struct_filter, after_spatial_filter, sent_to_nlp) ?",
    "Ces compteurs retracent le pipeline de l'IA Colab : elements_extracted = nombre "
    "d'elements interactifs extraits du DOM ; after_struct_filter = elements restants "
    "apres filtrage par similarite structurelle (tag + attributs) ; after_spatial_filter "
    "= elements restants apres filtrage par position spatiale ; sent_to_nlp = elements "
    "envoyes au modele SentenceTransformer pour la similarite semantique. "
    "Si sent_to_nlp est bas (ex: 5 sur 45 extractions), le pipeline a bien filtre "
    "les elements non pertinents avant le NLP couteux."
)

pdf.qa_item(
    "Pourquoi le score final est-il pondere 0.4 * structurel + 0.6 * semantique ?",
    "La ponderation favorise la semantique car le texte et le sens sont plus robustes "
    "que la structure HTML face aux changements d'interface. Un bouton peut changer "
    "de classe CSS ou de position, mais son texte ('Se connecter') reste le meme. "
    "Le seuil de 0.75 est un equilibre : trop bas (ex: 0.5) genererait trop de faux "
    "positifs, trop haut (ex: 0.9) rendrait le healing inutile car il echouerait "
    "sauf cas presque identiques."
)

pdf.qa_item(
    "Comment le healing gere-t-il les elements dynamiques (ID generees, listes) ?",
    "Pour les ID generees (pattern f_<uuid> de 30+ caracteres hex), la methode "
    "getStableId() dans HealingUtils detecte le pattern et retourne null. L'element "
    "sera alors identifie par d'autres attributs (name, class, text, placeholder, "
    "aria-label, data-testid). Pour les elements de liste (table rows), le flag "
    "isRowRelative (= true si xpath commence par .//) permet a l'IA de savoir "
    "que le locator est relatif a un element parent et d'ajuster la recherche."
)

pdf.section_title("9.3 Questions sur la performance et la fiabilite")

pdf.qa_item(
    "Que se passe-t-il si l'API de healing est injoignable (timeout) ?",
    "SelfHealingClient a un timeout configurable (10s par defaut) et un retry "
    "automatique (2 tentatives avec backoff 300ms). Si les deux tentatives echouent, "
    "la methode healSelector retourne une HealingResponse avec success=false et "
    "le message d'erreur. HealingLocatorResolver.resolveLocator() propage alors "
    "l'exception originale (NoSuchElementException). Le test echoue normalement, "
    "sans faux positif. Les evenements d'echec sont tout de meme pousses au Dashboard."
)

pdf.qa_item(
    "Comment le module assure-t-il la compatibilite ascendante des locators ?",
    "Le cache healedLocators stocke les reparations par cle (URL + scope + locator). "
    "Si un locator a ete repare avec succes, tous les appels ulterieurs utiliseront "
    "directement le nouveau By sans passer par l'API. Si le nouveau locator echoue "
    "a son tour (par exemple apres un second changement d'interface), le pipeline "
    "se declenche a nouveau et peut produire un troisieme locator. Le fichier "
    "healing-baseline.json conserve l'historique des reparations entre les sessions."
)

pdf.qa_item(
    "Quels types d'exceptions declenchent le healing ?",
    "Le healing est declenche pour NoSuchElementException et "
    "StaleElementReferenceException. Ce sont les deux exceptions les plus frequentes "
    "liees a des changements d'interface. Les autres exceptions (TimeoutException, "
    "ElementNotInteractableException, etc.) ne sont pas capturees par le decorateur "
    "et remontent normalement. Le type d'exception est stocke dans healing_events "
    "(colonne exception_type) pour analyse."
)

pdf.section_title("9.4 Questions avancees (niveau expert)")

pdf.qa_item(
    "*Pourquoi HTTP/1.1 plutot que HTTP/2 pour SelfHealingClient ?",
    "HTTP/2 multiplexe les requetes sur une seule connexion TCP, ce qui est "
    "generalement plus performant. Cependant, avec ngrok (qui fait office de proxy), "
    "les connexions HTTP/2 peuvent etre interrompues par des trames GOAWAY. "
    "Le code Java detecte d'ailleurs 'goaway' dans la methode isRetryableTransportError(). "
    "HTTP/1.1 est plus stable avec ngrok/Colab car chaque requete est une connexion "
    "TCP independante. Si le service etait en production sans ngrok, HTTP/2 serait prefere."
)

pdf.qa_item(
    "*Comment evoluerait l'architecture pour supporter le multi-navigateur ?",
    "Actuellement, le healing est agnostique du navigateur car il travaille sur le "
    "DOM HTML et les attributs standards. Pour le multi-navigateur (Chrome, Firefox, "
    "Safari), il faudrait ajouter le navigateur dans la cle de cache (ex: driver::chrome::"
    "app.example.com::By.id::email) car certains attributs ou comportements diffèrent "
    "(ex: getBoundingClientRect peut retourner des valeurs legerement différentes). "
    "Le ElementInfo pourrait etre etendu avec un champ 'browser' pour que l'IA de "
    "healing adapte ses strategies par navigateur."
)

pdf.qa_item(
    "*Quelle est la strategie de test unitaire pour le module healing ?",
    "Le module healing est difficile a tester unitairement car il depend de Selenium "
    "(WebDriver, WebElement). Les strategies possibles : 1) Mokcer WebDriver et "
    "WebElement avec Mockito pour tester HealingLocatorResolver.resolveLocator() "
    "en isolation. 2) Utiliser HtmlUnitDriver (Selenium headless sans navigateur) "
    "pour les tests d'integration. 3) Tester SelfHealingClient avec un serveur mock "
    "(WireMock) pour verifier la construction des requetes et le retry. 4) Tester "
    "HealingUtils avec des By simules (pas de navigateur necessaire). Actuellement, "
    "les tests sont principalement manuels via l'execution des tests d'acceptation."
)

pdf.qa_item(
    "*Comment le healing pourrait-il etre etendu a d'autres types de localisateurs (React, Angular) ?",
    "Pour les frameworks JS modernes, les localisateurs optimaux sont souvent : "
    "data-testid (React Testing Library), data-cy (Cypress), ou aria-label "
    "(accessibilite). Le module supporte deja data-testid (converti en cssSelector). "
    "L'extension consisterait a : 1) Ajouter des strategies de capture pour les "
    "attributs specifiques (data-testid, data-cy, aria-label, role). 2) Ajouter "
    "un poids plus eleve pour ces attributs dans le matching. 3) Generer preferentiellement "
    "des locators data-testid dans les suggestions de l'IA, car ce sont les plus "
    "stables. Le code actuel capture deja data-testid et data-cy dans les attributs "
    "(buildSnapshotAttributes)."
)

pdf.qa_item(
    "*Quels sont les risques de faux positifs avec le healing automatique ?",
    "Le risque principal est que le healing trouve un element different de celui "
    "attendu mais avec un score > 0.75. Par exemple, trouver un bouton 'Annuler' "
    "a la place de 'Confirmer' si leurs textes sont proches et leurs positions "
    "similaires. Plusieurs mecanismes reduisent ce risque : 1) Le seuil de 0.75 "
    "est eleve. 2) Le score semantique (poids 0.6) compare les textes via un "
    "modele entraine, ce qui evite les confusions. 3) Les tests continuent de "
    "verifier les comportements apres le clic : si le healing trouve le mauvais "
    "element, l'assertion suivante echouera. 4) Le suivi via healing_events permet "
    "d'analyser les cas limites. Le risque zero n'existe pas, mais le systeme est "
    "concu pour minimiser l'impact."
)

pdf.ln(6)
pdf.set_font("Helvetica", "I", 9)
pdf.set_text_color(100, 100, 100)
pdf.cell(0, 5, "--- Fin du document ---", align="C", new_x="LMARGIN", new_y="NEXT")

# =========== SAVE ===========
pdf.output(OUTPUT_PATH)
print(f"PDF genere avec succes: {OUTPUT_PATH}")
print(f"Nombre de pages: {pdf.page_no()}")
