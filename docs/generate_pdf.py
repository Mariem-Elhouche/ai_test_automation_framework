#!/usr/bin/env python3
"""Generate comprehensive project PDF for soutenance preparation."""

from fpdf import FPDF
import os

OUTPUT_DIR = r"C:\Users\mariem.elhouche-ext\Projets\ai-test-automation-framework\docs"
OUTPUT_PATH = os.path.join(OUTPUT_DIR, "soutenance_preparation.pdf")


class PDF(FPDF):
    def __init__(self):
        super().__init__()
        self.set_auto_page_break(auto=True, margin=20)

    def header(self):
        if self.page_no() > 1:
            self.set_font("Helvetica", "I", 7)
            self.set_text_color(120, 120, 120)
            self.cell(0, 5, "AI Test Automation Framework - Preparation Soutenance", align="L")
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
        self.cell(0, 12, "AI Test Automation Framework", align="C", new_x="LMARGIN", new_y="NEXT")
        self.ln(4)
        self.set_font("Helvetica", "", 16)
        self.set_text_color(80, 80, 80)
        self.cell(0, 10, "Dossier de Preparation - Soutenance", align="C", new_x="LMARGIN", new_y="NEXT")
        self.ln(6)
        self.set_font("Helvetica", "", 12)
        self.set_text_color(100, 100, 100)
        self.cell(0, 8, "Projet complet : Architecture, Auto-Healing, CI/CD", align="C", new_x="LMARGIN", new_y="NEXT")
        self.ln(4)
        self.cell(0, 8, "Questions & Reponses pour Soutenance orale et technique", align="C", new_x="LMARGIN", new_y="NEXT")
        self.ln(30)
        self.set_font("Helvetica", "I", 10)
        self.set_text_color(130, 130, 130)
        self.cell(0, 6, "Genere le 24/06/2026", align="C", new_x="LMARGIN", new_y="NEXT")
        self.cell(0, 6, "Projet NoveoCare - Mariem El Houche-Ext", align="C", new_x="LMARGIN", new_y="NEXT")

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
        bullet_char = "-"
        self.cell(4, 5, bullet_char)
        self.multi_cell(0, 5, text)
        self.ln(0.5)

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

    def qa_item(self, question, answer):
        self.set_font("Helvetica", "B", 9)
        self.set_text_color(180, 60, 30)
        self.cell(0, 6, f"Q: {question}", new_x="LMARGIN", new_y="NEXT")
        self.set_font("Helvetica", "", 9)
        self.set_text_color(40, 40, 40)
        self.multi_cell(0, 5, f"R: {answer}")
        self.ln(2)

    def table_row(self, cells, widths, bold=False, fill=False):
        self.set_font("Helvetica", "B" if bold else "", 8)
        self.set_text_color(40, 40, 40)
        if fill:
            self.set_fill_color(220, 230, 240)
        for i, cell in enumerate(cells):
            self.cell(widths[i], 5.5, cell, border=1, fill=fill)
        self.ln()

    def check_page_space(self, needed_mm):
        if self.get_y() + needed_mm > 270:
            self.add_page()


pdf = PDF()
pdf.alias_nb_pages()

# =========== TITLE PAGE ===========
pdf.title_page()

# =========== TABLE OF CONTENTS ===========
pdf.add_page()
pdf.set_font("Helvetica", "B", 18)
pdf.set_text_color(30, 60, 110)
pdf.cell(0, 12, "Table des Matieres", new_x="LMARGIN", new_y="NEXT")
pdf.line(10, pdf.get_y(), 200, pdf.get_y())
pdf.ln(6)

toc = [
    ("1", "Presentation du Projet", [
        "1.1 Contexte et Objectifs",
        "1.2 Architecture Globale (5 Modules)",
        "1.3 Stack Technologique",
    ]),
    ("2", "Description Detaillee des Modules", [
        "2.1 AI Engine (Python FastAPI + Colab)",
        "2.2 Automation Framework (Java 17 + Selenium + Cucumber)",
        "2.3 Dashboard API (FastAPI + PostgreSQL)",
        "2.4 Dashboard UI (React 18 + Vite)",
        "2.5 CI/CD Pipeline (GitHub Actions)",
    ]),
    ("3", "Flux Cles", [
        "3.1 Flux de bout en bout (End-to-End)",
        "3.2 Flux Auto-Healing (3 niveaux)",
        "3.3 Flux Authentification JWT",
        "3.4 Pipeline CI/CD",
    ]),
    ("4", "Auto-Healing Approfondi", [
        "4.1 Algorithme de Scoring (formule S_agrege)",
        "4.2 Les 3 modes de guerison",
        "4.3 Metriques et Dashboard",
    ]),
    ("5", "Q&A - Soutenance Presentation", [
        "5.1 Questions generales sur le projet",
        "5.2 Questions sur l'architecture",
        "5.3 Questions sur le self-healing",
        "5.4 Questions sur l'authentification",
        "5.5 Questions sur le pipeline CI/CD",
    ]),
    ("6", "Q&A - Soutenance Technique", [
        "6.1 Java / Maven / Selenium",
        "6.2 Python / FastAPI / Authentification",
        "6.3 React / Dashboard UI",
        "6.4 Docker / CI-CD / GitHub Actions",
        "6.5 Base de donnees PostgreSQL",
    ]),
    ("7", "Annexes", [
        "7.1 Configuration (config.properties)",
        "7.2 Acces et URLs",
        "7.3 Diagrammes PlantUML",
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
# PARTIE 1 : PRESENTATION DU PROJET
# ======================================================================
pdf.add_page()
pdf.chapter_title("1", "Presentation du Projet")

pdf.section_title("1.1 Contexte et Objectifs")
pdf.body_text(
    "Le projet AI Test Automation Framework a ete developpe pour NoveoCare, une application "
    "web de gestion de sante (backoffice). L'objectif est d'automatiser les tests fonctionnels "
    "de l'application de maniere intelligente, en integrant un mecanisme d'auto-reparation "
    "(self-healing) des localisateurs (locators) Selenium."
)
pdf.body_text(
    "Les defis adresses par ce projet :"
)
pdf.bullet("Maintenance reduite : les localisateurs changent souvent avec les mises a jour UI, le self-healing permet d'eviter de reecrire les tests a chaque changement.")
pdf.bullet("Flaky tests : le pipeline CI/CD detecte et classifie les tests flaky (re-execution automatique des scenarios echoues).")
pdf.bullet("Visibilite : un dashboard centralise avec des KPIs (taux de guerison, taux de reussite, historique) permet de monitorer la sante des tests.")
pdf.bullet("CI/CD integre : GitHub Actions avec 13 suites de tests, execution sequentielle sur self-hosted runner Docker.")

pdf.section_title("1.2 Architecture Globale (5 Modules)")
pdf.body_text("Le projet est divise en 5 modules interconnectes :")

modules = [
    ("AI Engine (Python FastAPI + Notebook Colab)", 
     "Moteur d'IA pour le self-healing. Expose un endpoint /heal qui analyse le DOM, "
     "calcule la similarite structurelle, semantique et spatiale, et retourne un nouveau localisateur."),
    ("Automation Framework (Java 17 + Maven + Selenium + Cucumber)",
     "Framework de tests fonctionnels. 13 suites de tests Cucumber, mecanisme de healing "
     "a 3 niveaux (cache Java -> baseline fichier -> API /heal), reporters vers le dashboard."),
    ("Dashboard API (FastAPI + PostgreSQL)",
     "API REST pour stocker et exposer les resultats de tests et les metriques de healing. "
     "Authentification JWT (3 roles) + API Key pour machine-to-machine."),
    ("Dashboard UI (React 18 + Vite)",
     "Interface utilisateur pour visualiser les resultats des tests, les metriques de healing, "
     "lancer des executions et gerer les utilisateurs."),
    ("CI/CD (GitHub Actions + Docker)",
     "Pipeline automatise : build -> 13 suites de tests matricielles avec re-execution -> "
     "classification flaky -> deploiement de la stack Docker (dashboard)."),
]

for name, desc in modules:
    pdf.set_font("Helvetica", "B", 9)
    pdf.set_text_color(50, 80, 130)
    pdf.cell(0, 5, name, new_x="LMARGIN", new_y="NEXT")
    pdf.body_text(desc)

pdf.section_title("1.3 Stack Technologique")
tech_data = [
    ("Module", "Technologie", "Version"),
    ("Langage Backend", "Python", "3.13"),
    ("Framework API", "FastAPI", "0.115+"),
    ("Auth", "PyJWT + passlib (bcrypt)", "-"),
    ("Base de donnees", "PostgreSQL", "16+"),
    ("Langage Tests", "Java", "17"),
    ("Build", "Maven", "3.9+"),
    ("Test Framework", "Cucumber", "7.x"),
    ("Browser Automation", "Selenium", "4.x"),
    ("Frontend", "React", "18"),
    ("Build Frontend", "Vite", "5.x"),
    ("CI/CD", "GitHub Actions", "-"),
    ("Conteneurisation", "Docker + Docker Compose", "-"),
]
widths = [50, 60, 30]
pdf.table_row(tech_data[0], widths, bold=True, fill=True)
for row in tech_data[1:]:
    pdf.table_row(row, widths)

# ======================================================================
# PARTIE 2 : DESCRIPTION DETAILLEE DES MODULES
# ======================================================================
pdf.add_page()
pdf.chapter_title("2", "Description Detaillee des Modules")

pdf.section_title("2.1 AI Engine (Python FastAPI + Colab)")
pdf.body_text(
    "Le module AI Engine est le cerveau du self-healing. Il est compose de deux parties :"
)
pdf.subsection_title("2.1.1 Dashboard API (dashboard_api.py)")
pdf.body_text(
    "Point d'entree de l'API FastAPI sur le port 8080. Expose les endpoints suivants :"
)
pdf.bullet("POST /auth/login - Authentification (email + password) -> retourne un JWT")
pdf.bullet("POST /api/users (CRUD) - Gestion des utilisateurs (admin only)")
pdf.bullet("POST /api/run-tests - Lancement d'une execution Maven avec tags Cucumber")
pdf.bullet("GET /api/dashboard - Resume des metriques de healing + resultats Cucumber")
pdf.bullet("POST /api/cucumber-runs - Push des resultats Cucumber (machine-to-machine)")
pdf.bullet("POST /api/healing-events - Push des evenements de healing")
pdf.bullet("POST /heal - Endpoint de healing (utilise par le framework Java)")
pdf.bullet("GET /api/metrics/history - Historique des metriques")

pdf.subsection_title("2.1.2 Service d'authentification (auth_service.py)")
pdf.body_text(
    "Deux mecanismes d'authentification :"
)
pdf.bullet("JWT : HS256 avec expire de 480 minutes. 3 roles : admin, project_manager, qa_engineer.")
pdf.bullet("API Key : pour les communications machine-to-machine (poussee des resultats Cucumber).")
pdf.body_text(
    "Les mots de passe sont haches avec bcrypt via la librairie passlib. "
    "3 comptes par defaut sont seedes au demarrage : administrateur, chef de projet, ingenieur QA."
)

pdf.subsection_title("2.1.3 Self-Healing Notebook (self_healing_v5.ipynb)")
pdf.body_text(
    "Notebook Colab qui implemente le pipeline complet de healing :"
)
pdf.bullet("Etape 1 - Baseline : lookup par MD5(type|valeur|hash du DOM) dans un cache fichier.")
pdf.bullet("Etape 2 - Extraction DOM : parsing HTML avec BeautifulSoup + lxml, filtrage des elements interactifs (input, button, a, select, textarea, label).")
pdf.bullet("Etape 3 - Similarite structurelle : tag (20%), attributs (50%), contexte DOM (30%).")
pdf.bullet("Etape 4 - Similarite semantique : SentenceTransformer paraphrase-multilingual-mpnet-base-v2.")
pdf.bullet("Etape 5 - Similarite spatiale : distance euclidienne normalisee entre les positions des elements.")
pdf.bullet("Score final : S_agrege = 0.40 x S_structurelle + 0.50 x S_semantique + 0.10 x S_spatiale")
pdf.bullet("Seuil de succes : 0.75")

pdf.section_title("2.2 Automation Framework (Java 17 + Selenium + Cucumber)")
pdf.body_text(
    "Framework de tests fonctionnels automatises pour l'application NoveoCare Backoffice. "
    "Architecture en couches :"
)
pdf.subsection_title("2.2.1 Couche Page Objects (BasePage.java)")
pdf.body_text(
    "Classe parente de toutes les pages. La methode findElement(By) intercepte les "
    "NoSuchElementException et declenche le processus de healing :"
)
pdf.bullet("Capture de l'ecran (screenshot avec element encadre en rouge)")
pdf.bullet("Recuperation du HTML de la page")
pdf.bullet("Appel du HealingLocatorResolver pour trouver un nouveau localisateur")
pdf.bullet("Snapshot : sauvegarde du screenshot + payload JSON dans target/healing-debug/")

pdf.subsection_title("2.2.2 Module Self-Healing (HealingLocatorResolver.java)")
pdf.body_text(
    "Resolution du healing en 3 niveaux (fallback) :"
)
pdf.bullet("Niveau 1 - Cache chaud (ConcurrentHashMap) : si le meme localisateur a deja ete gueri dans le meme run, on reuse le resultat.")
pdf.bullet("Niveau 2 - Baseline fichier (HealingBaseline.java) : fichier JSON persistant (target/healing-baseline.json) avec les guerisons des runs precedents.")
pdf.bullet("Niveau 3 - API /heal : appel HTTP POST vers l'API distante (Colab/ngrok ou CI Stub ou Dashboard API).")
pdf.body_text(
    "La classe SelfHealingClient.java est le client HTTP avec retry (2 tentatives). "
    "DashboardReporter.java pousse les evenements de healing vers l'API dashboard."
)

pdf.subsection_title("2.2.3 Runners et Organisation des Tests")
pdf.body_text(
    "GenericTagRunner.java : runner Cucumber parametre par tag, utilise pour 12 des 13 suites."
)
pdf.body_text("Les 13 suites de tests couvrent les fonctionnalites suivantes :")
suites = [
    "Login", "Companies", "Adherents", "Structures", "Appointments",
    "Prescriptions", "Billing", "Documents", "Users", "Roles",
    "Settings", "Notifications", "Logs"
]
pdf.body_text(", ".join([f"@{s.lower()}" for s in suites]))

pdf.subsection_title("2.2.4 Configuration (config.properties)")
pdf.body_text(
    "Proprietes cles : self.healing.enabled=true, self.healing.api.url (URL du service de healing), "
    "self.healing.debug.request=true (sauvegarde des payloads de debug), "
    "dashboard.api.url=http://127.0.0.1:8080, dashboard.api.key (cle API)."
)

pdf.section_title("2.3 Dashboard API (FastAPI + PostgreSQL)")
pdf.body_text(
    "API REST centralisee qui recoit et expose les donnees de test et de healing."
)
pdf.subsection_title("2.3.1 Base de donnees (3 tables principales)")
pdf.body_text("Table cucumber_runs : stocke les resultats de chaque scenario Cucumber (feature_name, scenario, status, duration_ns, tags, run_id, classification).")
pdf.body_text("Table healing_events : stocke chaque tentative de healing (old/new locator, scores, success, baseline_hit, healing_time_ms, elements counts).")
pdf.body_text("Table metrics_snapshots : instantanes periodiques des metriques agregees depuis le Colab.")

pdf.subsection_title("2.3.2 Resolution des metriques (fallback 3 niveaux)")
pdf.body_text(
    "La methode resolve_healing_payload() dans dashboard_service.py suit un fallback :"
)
pdf.bullet("1. metrics_snapshots (table) : cherche un snapshot pour le run_id donne.")
pdf.bullet("2. healing_events (table) : si pas de snapshot, agrege les evenements bruts (calcule healing_rate, baseline_hit_rate, avg_final_score, etc.).")
pdf.bullet("3. SELF_HEALING_METRICS_URL (externe) : si pas de donnees en BDD, appelle le endpoint /metrics du Colab.")

pdf.section_title("2.4 Dashboard UI (React 18 + Vite)")
pdf.body_text(
    "Interface utilisateur single-page (SPA) avec 4 pages principales :"
)
pdf.bullet("DashboardPage : vue d'ensemble avec KPIs (healing rate, baseline hit rate, avg score), graphiques d'evolution, tableau des resultats Cucumber.")
pdf.bullet("TestRunnerPage : lancement des tests (selection de tags), affichage des logs en temps reel (polling 2s).")
pdf.bullet("HealingPage : details de chaque evenement de healing (ancien/nouveau locator, scores, temps, statut) avec MiniChart pour chaque metrique.")
pdf.bullet("SettingsPage : configuration (URL API, run ID, dark mode, compte utilisateur).")

pdf.section_title("2.5 CI/CD Pipeline (GitHub Actions)")
pdf.body_text(
    "Pipeline complet defini dans .github/workflows/ci-cd.yml."
)
pdf.body_text("Declencheurs : push sur main/develop (filtre paths: automation-framework/**, .github/workflows/**, pom.xml), pull_request, workflow_dispatch (manuel avec choix de suite).")

pdf.subsection_title("Jobs :")
pdf.bullet("build : compile le code (compile + test-compile) avec cache Maven (~/.m2/repository).")
pdf.bullet("feature-tests (matriciel) : 13 suites en sequence (max-parallel: 1) sur self-hosted runner. Chaque suite execute : health check du site -> tests primaires -> rerun des echoues -> classification flaky (TestClassifier) -> upload artifacts (rapports + debug).")
pdf.bullet("deploy : fusionne les artefacts, reconstruit et deploye la stack Docker (PostgreSQL + API + UI), pousse les resultats via DashboardPushMain.")

# ======================================================================
# PARTIE 3 : FLUX CLES
# ======================================================================
pdf.add_page()
pdf.chapter_title("3", "Flux Cles")

pdf.section_title("3.1 Flux de bout en bout (End-to-End)")
pdf.body_text(
    "1. User story (nouveau besoin) -> 2. FeatureGenerator (Colab IA) genere le fichier feature "
    "-> 3. Commit dans le repo -> 4. CI/CD declenche -> 5. Maven compile -> 6. Tests Cucumber "
    "executes (Selenium headless) -> 7. Auto-healing si locator casse -> 8. Rerun des echoues "
    "-> 9. TestClassifier (passe/flaky/failed) -> 10. Push des resultats au dashboard -> "
    "11. Deploiement stack Docker."
)

pdf.section_title("3.2 Flux Auto-Healing (3 niveaux)")
pdf.code_block(
    "HealingWebDriver.findElement(By)\n"
    "  |\n"
    "  +-- NoSuchElementException\n"
    "       |\n"
    "       +-- [NIVEAU 1] Cache chaud (ConcurrentHashMap)\n"
    "       |     Si deja gueri dans ce meme run -> retour direct\n"
    "       |\n"
    "       +-- [NIVEAU 2] Baseline fichier (healing-baseline.json)\n"
    "       |     Si deja gueri dans un run precedent -> retour + baseline_hit=true\n"
    "       |\n"
    "       +-- [NIVEAU 3] API /heal (POST HTTP)\n"
    "             Soit Colab (ngrok), soit CI Stub (port 8090), soit Dashboard API\n"
    "             Reponse: {new_locator_type, new_locator_val, score, ...}"
)

pdf.section_title("3.3 Flux Authentification JWT")
pdf.code_block(
    "1. Utilisateur -> POST /auth/login {email, password}\n"
    "2. AuthService.find_user_by_email(email) -> SELECT * FROM app_users\n"
    "3. AuthService.verify_password(plain, hash) -> bcrypt compare\n"
    "4. AuthService.create_access_token(account) -> JWT (HS256, 480min)\n"
    "   Payload: {sub: user_id, role: role, exp: expiry}\n"
    "5. Reponse: {access_token, token_type: bearer, role, display_name, ...}\n"
    "6. Toutes les routes protegees -> verify_token(headers.Authorization)\n"
)
pdf.body_text(
    "API Key : header X-API-Key pour les appels machine-to-machine "
    "(ex: push cucumber-runs depuis le CI/CD)."
)

pdf.section_title("3.4 Pipeline CI/CD")
pdf.code_block(
    "Déclencheur (push/PR/manual)\n"
    "  |\n"
    "  +-- Job: build (compile + test-compile)\n"
    "  |\n"
    "  +-- Job: feature-tests (matrix: 13 suites)\n"
    "  |     Pour chaque suite:\n"
    "  |       1. Health check site staging\n"
    "  |       2. Tests primaires (xvfb-run mvn test)\n"
    "  |       3. Rerun des scenarios echoues\n"
    "  |       4. TestClassifier (passe/flaky/failed)\n"
    "  |       5. Upload artifacts (reports, debug)\n"
    "  |\n"
    "  +-- Job: deploy (branche main seulement)\n"
    "       1. Download all artifacts\n"
    "       2. docker compose down -v\n"
    "       3. docker compose up -d --build\n"
    "       4. DashboardPushMain pour chaque fichier JSON"
)

# ======================================================================
# PARTIE 4 : AUTO-HEALING APPROFONDI
# ======================================================================
pdf.add_page()
pdf.chapter_title("4", "Auto-Healing Approfondi")

pdf.section_title("4.1 Algorithme de Scoring")
pdf.body_text(
    "Le score final est calcule selon la formule (version mise a jour avec le score spatial) :"
)
pdf.body_text("S_agrege = 0.40 x S_structurelle + 0.50 x S_semantique + 0.10 x S_spatiale")
pdf.body_text("Seuil de succes : 0.75")
pdf.body_text("")

pdf.subsection_title("S_structurelle (poids 0.40)")
pdf.body_text("Composants :")
pdf.bullet("Tag HTML (20%) : correspondance exacte du type d'element.")
pdf.bullet("Attributs (50%) : placeholder, aria-label, title, name, data-testid, class, etc.")
pdf.bullet("Contexte DOM (30%) : parent immediat, elements voisins, profondeur dans l'arbre.")

pdf.subsection_title("S_semantique (poids 0.50)")
pdf.body_text(
    "Utilise SentenceTransformer paraphrase-multilingual-mpnet-base-v2. "
    "Calcule la similarite cosinus entre les embeddings du texte de l'ancien element et du nouveau. "
    "Modele multilingue (francais + anglais)."
)

pdf.subsection_title("S_spatiale (poids 0.10)")
pdf.body_text(
    "Distance euclidienne normalisee entre les coordonnees (x, y, largeur, hauteur) "
    "des elements. Ajoutee recemment dans la version mise a jour du notebook pour "
    "ameliorer la precision du matching lorsque les coordonnees sont disponibles."
)

pdf.section_title("4.2 Les 3 modes de guerison")

pdf.subsection_title("Mode 1 : Baseline Java (cache + fichier)")
pdf.body_text("Usage : pendant l'execution des tests, pour eviter des appels reseau repetes.")
pdf.body_text("Performance : aucun parsing DOM, aucun appel reseau. Cache memoire persistee dans target/healing-baseline.json.")

pdf.subsection_title("Mode 2 : CI Healing Stub (Python standalone)")
pdf.body_text("Usage : dans le pipeline CI/CD, quand le Colab n'est pas accessible.")
pdf.body_text("Implementation : parse le DOM avec html.parser (stdlib, pas de ML). Matching par texte exact (1.0), texte partiel (0.8), attributs (0.7-0.9), traduction FR/EN (0.75). Score force a 0.95 en cas de succes.")

pdf.subsection_title("Mode 3 : Moteur IA complet (Colab)")
pdf.body_text("Usage : en production, avec acces GPU pour le modele NLP.")
pdf.body_text("Pipeline complet : extraction DOM -> similarite structurelle -> similarite semantique (SentenceTransformer) -> similarite spatiale -> score agrege avec seuil 0.75.")

pdf.section_title("4.3 Metriques et Dashboard")
pdf.body_text("Metriques cles exposees dans le dashboard :")
pdf.bullet("Healing Rate : pourcentage de guerisons reussies (successful_healings / total_healing_requests)")
pdf.bullet("Baseline Hit Rate : pourcentage de guerisons resolues via le cache/baseline")
pdf.bullet("Avg Final Score : moyenne des scores finaux (S_agrege)")
pdf.bullet("Avg Healing Time : temps moyen de resolution du healing en millisecondes")
pdf.bullet("NLP Filter Efficiency : ratio elements envoyes au NLP / elements extraits")
pdf.bullet("Evolution graphique : historique des metriques sur les 50 derniers snapshots")

# ======================================================================
# PARTIE 5 : Q&A SOUTENANCE PRESENTATION
# ======================================================================
pdf.add_page()
pdf.chapter_title("5", "Q&A - Soutenance Presentation")

pdf.body_text("Cette section contient les questions possibles lors de la soutenance orale (presentation generale du projet) avec les reponses recommandees.")
pdf.ln(2)

pdf.section_title("5.1 Questions generales sur le projet")

pdf.qa_item(
    "Quel est l'objectif principal de ce projet ?",
    "Automatiser les tests fonctionnels du backoffice NoveoCare avec un mecanisme "
    "d'auto-reparation (self-healing) des localisateurs Selenium, afin de reduire "
    "la maintenance des tests et d'ameliorer la fiabilite du pipeline CI/CD."
)

pdf.qa_item(
    "Quelle est la valeur ajoutee par rapport a un framework de test classique ?",
    "Le self-healing permet d'eviter de reecrire les tests a chaque changement d'interface. "
    "Le pipeline detecte et classifie les tests flaky. Le dashboard offre une visibilite "
    "complete sur la sante des tests et l'efficacite du healing."
)

pdf.qa_item(
    "Quels sont les defis techniques que vous avez rencontres ?",
    "1) Le matching des localisateurs dans un DOM complexe avec des elements dynamiques. "
    "2) L'integration entre Java (framework de test) et Python (moteur IA). "
    "3) Le timeout des sessions Colab/ngrok. "
    "4) La classification flaky necessitant une re-execution fiable."
)

pdf.qa_item(
    "Combien de temps a dure le developpement ?",
    "Le projet a ete developpe sur plusieurs iterations. La phase initiale a couvert "
    "la creation du framework de test, suivie par l'ajout du self-healing (notebook Colab), "
    "puis l'integration du dashboard et du pipeline CI/CD."
)

pdf.section_title("5.2 Questions sur l'architecture")

pdf.qa_item(
    "Pourquoi avoir separe le moteur IA (Python) du framework de test (Java) ?",
    "Python offre un ecosysteme NLP plus riche (SentenceTransformer, BeautifulSoup) et "
    "est plus adapte au prototypage rapide. Java est le standard pour les frameworks "
    "de test d'entreprise (Selenium + Cucumber + Maven). La separation permet aussi "
    "de deployer et faire evoluer chaque composant independamment."
)

pdf.qa_item(
    "Comment les 5 modules communiquent-ils entre eux ?",
    "Via des API REST. Le framework Java appelle le moteur IA via POST /heal. "
    "Le framework envoie les resultats au Dashboard via POST /api/cucumber-runs et "
    "POST /api/healing-events. Le Dashboard UI consulte l'API via GET. Le CI/CD "
    "orchestre le tout via GitHub Actions."
)

pdf.qa_item(
    "Pourquoi avoir choisi PostgreSQL comme base de donnees ?",
    "PostgreSQL est robuste, open-source, et bien adapte aux donnees structurees "
    "de ce projet. Il est facilement containerisable avec Docker et offre "
    "de bonnes performances pour les requetes analytiques (aggregation de metriques)."
)

pdf.section_title("5.3 Questions sur le self-healing")

pdf.qa_item(
    "Comment fonctionne le self-healing ?",
    "Quand Selenium ne trouve pas un element (NoSuchElementException), le framework "
    "capture le HTML de la page et appelle l'API de healing qui analyse le DOM, "
    "calcule la similarite entre l'ancien localisateur et chaque element candidat, "
    "et retourne le meilleur nouveau localisateur avec un score de confiance."
)

pdf.qa_item(
    "Quels sont les 3 niveaux de healing et pourquoi ?",
    "Cache chaud (meme run) pour la rapidite, baseline fichier (runs precedents) "
    "pour la persistance, API /heal pour les cas jamais rencontres. Ce fallback "
    "optimise le temps de reponse tout en garantissant une couverture maximale."
)

pdf.qa_item(
    "Quelle est la formule du score et pourquoi ces poids ?",
    "S = 0.40 x S_struct + 0.50 x S_sem + 0.10 x S_spat. Le poids fort du "
    "semantique (50%) car le sens du texte est le meilleur indicateur pour retrouver "
    "un element. Le structurel (40%) capture les attributs techniques. Le spatial "
    "(10%) est un adjuvant qui utilise les coordonnees quand elles sont disponibles."
)

pdf.qa_item(
    "Quel est le taux de reussite du healing ?",
    "Dans le CI Stub (sans ML), le taux est eleve grace au matching par attributs. "
    "Avec le Colab complet, le taux depend de la qualite du DOM mais le seuil de 0.75 "
    "garantit que seuls les matches fiables sont retournes."
)

pdf.section_title("5.4 Questions sur l'authentification")

pdf.qa_item(
    "Quels sont les deux mecanismes d'authentification ?",
    "JWT (HS256, expire 480min) pour les utilisateurs humains avec 3 roles. "
    "API Key (via header X-API-Key) pour les communications machine-to-machine, "
    "notamment la poussee des resultats depuis le CI/CD."
)

pdf.qa_item(
    "Quels sont les 3 roles et leurs permissions ?",
    "admin : acces total (CRUD utilisateurs, lancement tests, toutes les metriques). "
    "project_manager : visualisation des resultats et metriques. "
    "qa_engineer : lancement des tests et visualisation."
)

pdf.section_title("5.5 Questions sur le pipeline CI/CD")

pdf.qa_item(
    "Pourquoi max-parallel: 1 dans la matrice de tests ?",
    "Le self-hosted runner a des ressources limitees (CPU, memoire, navigateur). "
    "Executer les 13 suites en parallele provoquerait des conflits (ports, sessions "
    "navigateur) et des faux positifs. La sequence garantit des resultats fiables."
)

pdf.qa_item(
    "Comment sont geres les tests flaky ?",
    "Apres le premier run, les scenarios echoues sont re-executes automatiquement "
    "(rerun). Le TestClassifier compare les deux rapports JSON : passe aux deux = "
    "'passed', passe au rerun seulement = 'flaky', echoue aux deux = 'failed'."
)

pdf.qa_item(
    "Que fait le job 'deploy' exactement ?",
    "Il telecharge tous les artefacts de test, arrete la stack Docker existante, "
    "la reconstruit et la relance, puis pousse tous les resultats Cucumber au dashboard "
    "via DashboardPushMain. Il ne s'execute que sur la branche main."
)

# ======================================================================
# PARTIE 6 : Q&A SOUTENANCE TECHNIQUE
# ======================================================================
pdf.add_page()
pdf.chapter_title("6", "Q&A - Soutenance Technique")

pdf.body_text("Cette section contient les questions techniques approfondies pour la soutenance technique.")
pdf.ln(2)

pdf.section_title("6.1 Java / Maven / Selenium / Cucumber")

pdf.qa_item(
    "Comment est organise le projet Maven ?",
    "Le projet a un pom.xml parent a la racine avec le module automation-framework. "
    "Le pom.xml d'automation-framework declare les dependances : Selenium 4.x, "
    "Cucumber 7.x (with JUnit Platform Suite), JUnit 5, Jackson (JSON), "
    "et Apache HttpClient pour les appels REST."
)

pdf.qa_item(
    "Pourquoi utiliser JUnit Platform Suite (@Suite) pour Cucumber ?",
    "Cela permet d'utiliser le nouveau moteur Cucumber avec JUnit 5, offrant "
    "une meilleure integration avec le reporting, les tags, et la re-execution. "
    "GenericTagRunner est configure avec @ConfigurationParameter pour les features, "
    "le glue code, et les plugins."
)

pdf.qa_item(
    "Comment fonctionne HealingLocatorResolver en details ?",
    "La methode resolve(By originalLocator, String pageSource) : 1) Verifie le "
    "cache chaud (ConcurrentHashMap<String, By>). 2) Si miss, verifie le fichier "
    "baseline (healing-baseline.json) charge au demarrage. 3) Si miss, construit "
    "un payload JSON (old locator, DOM, screenshots) et appelle SelfHealingClient "
    "qui fait POST /heal avec retry. 4) Met a jour le cache et le fichier baseline."
)

pdf.qa_item(
    "Comment BasePage.findElement intercepte l'exception ?",
    "La methode custom findElement(By) est la seule utilisee dans tous les Page Objects. "
    "Elle wrappe l'appel Selenium standard et capture NoSuchElementException. "
    "En cas d'exception, elle prend un screenshot (avec encadrement rouge de "
    "l'emplacement attendu), recupere le page source, et appelle le resolver."
)

pdf.qa_item(
    "Quel est le role de DashboardReporter ?",
    "C'est un hook Cucumber (@After) qui collecte les resultats de chaque scenario "
    "et les envoie au Dashboard API apres l'execution. Il pousse aussi les "
    "evenements de healing cumules pendant le run. Il implemente l'interface "
    "ConcurrentHashMap pour accumuler les evenements de facon thread-safe."
)

pdf.qa_item(
    "Comment fonctionne le rerun des tests echoues ?",
    "Cucumber genere un fichier failed-{suite}.txt via le plugin rerun. "
    "Dans le pipeline CI/CD, si ce fichier existe et n'est pas vide, "
    "Maven est relance avec -Dcucumber.features=@failed-{suite}.txt, "
    "ce qui execute uniquement les scenarios listes dans ce fichier."
)

pdf.section_title("6.2 Python / FastAPI / Authentification")

pdf.qa_item(
    "Comment FastAPI gere-t-il l'authentification ?",
    "La fonction verify_token() est un middleware (FastAPI Depends) qui extrait "
    "le header Authorization: Bearer <token>, decode le JWT avec PyJWT, "
    "verifie la signature HS256 et l'expiration, puis injecte le payload "
    "(user_id, role) dans la requete. Les routes sont protegees par "
    "des dependences qui verifient le role."
)

pdf.qa_item(
    "Comment le hash bcrypt est-il implemente ?",
    "La librairie passlib avec le backend bcrypt genere un hash different "
    "a chaque appel (sel sale). Le hash est stocke en base et verifie "
    "via passlib.verify(plain_password, hash). 3 comptes par defaut sont "
    "crees au demarrage avec des mots de passe haches."
)

pdf.qa_item(
    "Comment l'API Key est-elle validee ?",
    "La fonction verify_api_key() lit le header X-API-Key et le compare "
    "a la variable d'environnement DASHBOARD_API_KEY. Si les deux correspondent, "
    "l'acces est autorise sans JWT (pour les communications machine-to-machine)."
)

pdf.qa_item(
    "Comment le lancement de tests Maven est-il integre dans l'API ?",
    "La classe TestRunManager execute mvn test en subprocess avec les options "
    "configurees (tags, runner, plugins, healing enabled). Les logs sont "
    "streames en temps reel vers le dashboard via des endpoints websocket "
    "ou des fichiers de log polles toutes les 2 secondes."
)

pdf.section_title("6.3 React / Dashboard UI")

pdf.qa_item(
    "Comment les donnees sont-elles chargees dans le dashboard ?",
    "Chaque page fait des fetch() vers l'API FastAPI avec le token JWT "
    "dans le header Authorization. HealingPage utilise Promise.all() pour "
    "charger les metriques, les evenements et l'historique en parallele."
)

pdf.qa_item(
    "Comment est implemente le MiniChart ?",
    "Composant SVG custom qui dessine une ligne (path) a partir d'un tableau "
    "de valeurs. Utilise un gradient lineaire pour le remplissage. "
    "Il gere les cas particuliers : 0 valeur (affiche 'No data'), "
    "1 valeur (affiche un point avec '1 data point')."
)

pdf.qa_item(
    "Quelles sont les optimisations de performance ?",
    "Le polling est limite a 2 secondes, la pagination des evenements "
    "(12 par page), le chargement parallele des donnees, et les appels "
    "API avec useCallback/useEffect propres (sans fuites memoires)."
)

pdf.section_title("6.4 Docker / CI-CD / GitHub Actions")

pdf.qa_item(
    "Comment est configure le self-hosted runner ?",
    "Le runner est Dockerise (Dockerfile dans tools/runner/) avec Chrome, "
    "chromedriver, xvfb (serveur X virtuel) et le logiciel GitHub Actions "
    "runner. Il est lance avec docker-compose et s'enregistre aupres de GitHub."
)

pdf.qa_item(
    "Pourquoi utiliser xvfb-run ?",
    "xvfb-run lance un serveur X virtuel (framebuffer) pour permettre "
    "a Chrome de fonctionner en mode headless sur un serveur sans ecran. "
    "--auto-servernum choisit un port de maniere dynamique pour eviter les conflits."
)

pdf.qa_item(
    "Comment les secrets sont-ils geres ?",
    "Les mots de passe (BACKOFFICE_USER_EMAIL/PASSWORD, DASHBOARD_API_KEY) "
    "sont stockes dans les secrets GitHub et injectes via ${{ secrets.X }}. "
    "Ils ne sont jamais visibles dans les logs. Le .env de la stack Docker "
    "contient la configuration sensible."
)

pdf.qa_item(
    "Que manque-t-il pour un vrai CD (Continuous Deployment) ?",
    "Le deploiement de l'application cible (NoveoCare backoffice) vers le staging "
    "ou la production. Actuellement, seul le dashboard de monitoring est deploye. "
    "Il faudrait ajouter des smoke tests post-deploiement et un rollback automatique."
)

pdf.section_title("6.5 Base de donnees PostgreSQL")

pdf.qa_item(
    "Quelle est la structure de la table healing_events ?",
    "id (SERIAL PK), created_at (TIMESTAMP), scenario_name, old_locator_type, "
    "old_locator_val, new_locator_type, new_locator_val, success (BOOLEAN), "
    "score (FLOAT), structural_score, semantic_score, healing_time_ms (INT), "
    "baseline_hit (BOOLEAN), exception_type, elements_extracted, "
    "after_struct_filter, after_spatial_filter, sent_to_nlp, run_id (VARCHAR)."
)

pdf.qa_item(
    "Comment les donnees sont-elles inserees ?",
    "Les evenements de healing sont inseres un par un via POST /api/healing-events "
    "depuis DashboardReporter (Java) ou depuis le Colab (Python). "
    "Les resultats Cucumber sont inseres en lot via POST /api/cucumber-runs. "
    "Les snapshots de metriques via POST /api/metrics."
)

pdf.qa_item(
    "Comment la base est-elle initialisee au demarrage ?",
    "Un script SQL (init.sql) dans le docker-compose cree les 3 tables "
    "et insere les donnees initiales. L'application FastAPI utilise "
    "un event de demarrage (@app.on_event('startup')) pour verifier "
    "la connexion et seeder les comptes utilisateurs si la table est vide."
)

# ======================================================================
# PARTIE 7 : ANNEXES
# ======================================================================
pdf.add_page()
pdf.chapter_title("7", "Annexes")

pdf.section_title("7.1 Configuration (config.properties)")
pdf.code_block(
    "# Fichier: automation-framework/src/main/resources/config.properties\n"
    "\n"
    "# Backoffice\n"
    "backoffice.url=https://stg-bo.noveocare.com/login\n"
    "backoffice.user.email=\n"
    "backoffice.user.password=\n"
    "\n"
    "# Self-Healing\n"
    "self.healing.enabled=true\n"
    "self.healing.api.url=\n"
    "self.healing.debug.request=true\n"
    "self.healing.healed.by.cache=0\n"
    "self.healing.healed.by.healing=0\n"
    "\n"
    "# Dashboard API\n"
    "dashboard.api.url=http://127.0.0.1:8080\n"
    "dashboard.api.key=change-me"
)

pdf.section_title("7.2 Acces et URLs")
url_data = [
    ("Composant", "URL / Acces", "Port"),
    ("Dashboard API (FastAPI)", "http://localhost:8080", "8080"),
    ("Dashboard UI (React)", "http://localhost:5173 (dev) / 80 (prod)", "5173 / 80"),
    ("PostgreSQL", "localhost:5432", "5432"),
    ("CI Healing Stub", "http://localhost:8090", "8090"),
    ("Backoffice Staging", "https://stg-bo.noveocare.com/login", "443"),
    ("Self-Hosted Runner", "Docker container (tools/runner/)", "-"),
    ("Colab (via ngrok)", "URL dynamique (ngrok)", "-"),
]
widths2 = [45, 95, 30]
pdf.table_row(url_data[0], widths2, bold=True, fill=True)
for row in url_data[1:]:
    pdf.table_row(row, widths2)

pdf.ln(4)
pdf.section_title("Comptes par defaut (seed)")
seed_data = [
    ("Role", "Email", "Mot de passe"),
    ("Administrateur", "admin@noveocare.com", "(hache bcrypt)"),
    ("Project Manager", "pm@noveocare.com", "(hache bcrypt)"),
    ("QA Engineer", "qa@noveocare.com", "(hache bcrypt)"),
]
pdf.table_row(seed_data[0], [45, 60, 60], bold=True, fill=True)
for row in seed_data[1:]:
    pdf.table_row(row, [45, 60, 60])

pdf.section_title("7.3 Diagrammes PlantUML")
pdf.body_text(
    "Les diagrammes de sequence et d'activite sont disponibles au format PlantUML "
    "dans le repertoire docs/diagrams/ :"
)
pdf.bullet("seq_authentification.puml - Authentification JWT")
pdf.bullet("seq_create_user.puml - Creation d'utilisateur")
pdf.bullet("seq_classification_reexecution.puml - Classification et re-execution")
pdf.bullet("seq_extraction_exposition_donnees.puml - Extraction et exposition des donnees")
pdf.bullet("activity_pipeline_ci_cd.puml - Pipeline CI/CD")
pdf.bullet("seq_create_category.puml - Creation de categorie (vide)")

pdf.ln(6)
pdf.set_font("Helvetica", "I", 9)
pdf.set_text_color(100, 100, 100)
pdf.cell(0, 5, "--- Fin du document ---", align="C", new_x="LMARGIN", new_y="NEXT")

# =========== SAVE ===========
pdf.output(OUTPUT_PATH)
print(f"PDF genere avec succes: {OUTPUT_PATH}")
print(f"Nombre de pages: {pdf.page_no()}")
