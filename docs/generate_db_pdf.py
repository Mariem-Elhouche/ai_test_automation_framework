#!/usr/bin/env python3
"""Generate comprehensive database documentation PDF for soutenance preparation."""

from fpdf import FPDF
import os

OUTPUT_DIR = r"C:\Users\mariem.elhouche-ext\Projets\ai-test-automation-framework\docs"
OUTPUT_PATH = os.path.join(OUTPUT_DIR, "database-soutenance.pdf")


class PDF(FPDF):
    def __init__(self):
        super().__init__()
        self.set_auto_page_break(auto=True, margin=20)

    def header(self):
        if self.page_no() > 1:
            self.set_font("Helvetica", "I", 7)
            self.set_text_color(120, 120, 120)
            self.cell(0, 5, "AI Test Automation Framework - Base de données", align="L")
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
        self.cell(0, 12, "Base de Données", align="C", new_x="LMARGIN", new_y="NEXT")
        self.ln(4)
        self.set_font("Helvetica", "", 16)
        self.set_text_color(80, 80, 80)
        self.cell(0, 10, "AI Test Automation Framework", align="C", new_x="LMARGIN", new_y="NEXT")
        self.ln(6)
        self.set_font("Helvetica", "", 12)
        self.set_text_color(100, 100, 100)
        self.cell(0, 8, "Documentation detaillee pour la soutenance", align="C", new_x="LMARGIN", new_y="NEXT")
        self.cell(0, 8, "Rapporteur et President - Experts en bases de données", align="C", new_x="LMARGIN", new_y="NEXT")
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
    ("1", "Presentation de la base de données", [
        "1.1 Technologie et choix",
        "1.2 Architecture de la couche données",
        "1.3 Principe dual : SQLAlchemy ORM + Core",
    ]),
    ("2", "Schema des 5 tables", [
        "2.1 Table healing_events",
        "2.2 Table metrics_snapshots",
        "2.3 Table cucumber_runs",
        "2.4 Table test_runs",
        "2.5 Table app_users",
    ]),
    ("3", "Index et performance", [
        "3.1 Index definis",
        "3.2 Strategies d'indexation",
        "3.3 Analyse des requêtes critiques",
    ]),
    ("4", "Repositories et accès aux données", [
        "4.1 Pattern Repository",
        "4.2 CucumberRunRepository",
        "4.3 HealingEventRepository",
        "4.4 MetricsSnapshotRepository",
        "4.5 TestRunRepository (synchrone)",
        "4.6 UserRepository",
    ]),
    ("5", "Mecanismes de migration et schema", [
        "5.1 Initialisation via schema.sql",
        "5.2 Création automatique via SQLAlchemy",
        "5.3 Migrations dynamiques (ensure_schema_compatibility)",
        "5.4 Fallback SQLite pour les tests",
    ]),
    ("6", "Flux de données detailles", [
        "6.1 Insertion des resultats Cucumber",
        "6.2 Insertion des événements de healing",
        "6.3 Calcul et stockage des métriques",
        "6.4 Resolution des métriques (fallback 3 niveaux)",
        "6.5 Gestion des test runs",
    ]),
    ("7", "Q&A - Questions possibles", [
        "7.1 Questions sur le choix de la technologie",
        "7.2 Questions sur le schema et la modelisation",
        "7.3 Questions sur les performances",
        "7.4 Questions sur les migrations et l'evolution",
        "7.5 Questions sur les tests et la fiabilite",
        "7.6 Questions avancees (expert)",
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
# PARTIE 1 : PRESENTATION DE LA BASE DE DONNÉES
# ======================================================================
pdf.add_page()
pdf.chapter_title("1", "Presentation de la base de données")

pdf.section_title("1.1 Technologie et choix")
pdf.body_text(
    "La base de données du projet AI Test Automation Framework utilisé PostgreSQL 16 en "
    "production et SQLite comme fallback pour les tests unitaires. Ce double support est "
    "gere de maniere transparente par SQLAlchemy 2.0, l'ORM (Object-Relational Mapping) "
    "choisi pour le projet."
)
pdf.body_text("Raisons du choix de PostgreSQL :")
pdf.bullet("Robustesse et maturite : PostgreSQL est un SGBD relationnel open-source eprouve, avec un excellent support des types de données avances (TIMESTAMPTZ, JSON, Numeric precis).")
pdf.bullet("Performance analytique : les fonctions d'agrégation (COUNT, AVG, SUM) utilisées pour les métriques de healing sont optimisees par l'optimiseur de requêtes PostgreSQL.")
pdf.bullet("Containerisable : l'image officielle postgres:16-alpine est legere (~200MB) et facile a déployer dans la stack Docker.")
pdf.bullet("Extensions : possibilite d'ajouter des indexes avances (GIN, BRIN) ou des fonctions de fenetrage pour les tendances historiques.")
pdf.body_text("Raisons du choix de SQLAlchemy 2.0 :")
pdf.bullet("Support dual synchrone/asynchrone : le projet utilisé le package databases (asynchrone) pour les operations CRUD principales et SQLAlchemy Core (synchrone) pour le repository TestRunRepository qui utilisé un engine synchrone.")
pdf.bullet("Mappings declaratifs : les modèles SQLAlchemy (DeclarativeBase) permettent de definir les tables en Python avec une syntaxe claire et typee.")
pdf.bullet("Compatibilite PostgreSQL/SQLite : SQLAlchemy abstrait les differences de dialecte. Par exemple, la migration dynamique (ensure_schema_compatibility) detecte 'sqlite' dans l'URL et ignore les ALTER TABLE non supportes.")
pdf.body_text("")
pdf.subsection_title("Explication synchrone vs asynchrone")
pdf.body_text(
    "Un accès synchrone a la base de données signifie que le thread d'exécution est bloque "
    "pendant l'attente de la reponse de la base. Si l'operation prend 200ms, le thread ne peut "
    "rien faire d'autre pendant ce temps. Cela pose problème dans un serveur web comme FastAPI "
    "qui gere des centaines de connexions simultanees avec un nombre limite de threads (workers)."
)
pdf.body_text(
    "Un accès asynchrone (async/await en Python) permet de liberer le thread pendant l'attente "
    "de la reponse. Le thread peut traiter d'autres requêtes pendant que la base traité la "
    "précédente. Cela ameliore considerablement le débit (throughput) du serveur sans multiplier "
    "le nombre de threads."
)
pdf.body_text("Comparaison avec d'autres approches :")
pdf.ln(1)
compare_data = [
    ("Approche", "Modèle", "Performance", "Complexite"),
    ("psycopg2 (synchrone pur)", "Synchrone blocant", "Fiable, bloque le thread", "Faible"),
    ("SQLAlchemy Core + databases", "Async avec await", "Non-blocant, haut débit", "Moyenne"),
    ("SQLAlchemy ORM + AsyncSession", "Async avec session", "Performant, overhead ORM", "Elevee"),
    ("Django ORM", "Synchrone (Celery pour async)", "Bloquant, nécessité file", "Moyenne"),
    ("Tortoise ORM", "Asynchrone natif", "Bon, moins mature", "Moyenne"),
    ("AIOHTTP + asyncpg", "Asynchrone sans ORM", "Très performant", "Elevee"),
]
widths_cmp = [50, 40, 55, 40]
pdf.table_row(compare_data[0], widths_cmp, bold=True, fill=True)
for row in compare_data[1:]:
    pdf.table_row(row, widths_cmp)
pdf.ln(2)
pdf.body_text(
    "Notre projet fait un choix hybride original : les 4 repositories principaux utilisent "
    "databases (async) pour ne pas bloquer FastAPI, tandis que TestRunRepository utilisé "
    "un engine synchrone car il est manipule depuis des threads standards (threading.Thread "
    "pour les executions Maven) qui ne peuvent pas utiliser async/await. Ce compromis offre "
    "le meilleur des deux mondes sans la complexite d'un ORM complet."
)

pdf.section_title("1.2 Architecture de la couche données")
pdf.body_text(
    "La couche d'accès aux données suit une architecture en 3 couches :"
)
pdf.code_block(
    "Couche 1 - Schemas (Pydantic) : validation des entrées/sorties API\n"
    "  src/schemas/dashboard_schemas.py\n"
    "  src/schemas/user_schemas.py\n"
    "\n"
    "Couche 2 - Modèles (SQLAlchemy ORM) : mapping objet-relationnel\n"
    "  src/models/base.py  (DeclarativeBase)\n"
    "  src/models/dashboard_model.py  (4 tables)\n"
    "  src/models/user_model.py  (1 table)\n"
    "\n"
    "Couche 3 - Repositories : accès aux données (CRUD + requêtes metier)\n"
    "  src/repositories/cucumber_repository.py\n"
    "  src/repositories/healing_repository.py\n"
    "  src/repositories/metrics_repository.py\n"
    "  src/repositories/test_run_repository.py\n"
    "  src/repositories/user_repository.py"
)
pdf.body_text(
    "Chaque couche a une responsabilite unique : les schemas valident le format des données, "
    "les modèles definissent la structure des tables, les repositories encapsulent les requêtes "
    "SQL. Les services (src/services/) orchestrent la logique metier en appelant les repositories."
)

pdf.section_title("1.3 Dualite asynchrone/synchrone")
pdf.body_text(
    "Le projet utilisé deux modes d'accès a la base de données :"
)
pdf.bullet("Async (databases) : HealingEventRepository, MetricsSnapshotRepository, CucumberRunRepository, UserRepository utilisent databases.Database asynchrone avec des requêtes SQLAlchemy Core. Cela permet de ne pas bloquer le thread principal de FastAPI pendant les operations I/O.")
pdf.bullet("Sync (SQLAlchemy Engine) : TestRunRepository utilisé sqlalchemy.create_engine synchrone avec des connexions directes (with engine.begin() as conn). Ce choix est lie a l'utilisation de la bibliotheque standard threading pour gerer les executions Maven en arriere-plan, et l'ecriture des logs JSON qui nécessité des operations de serialisation synchrones.")
pdf.body_text(
    "Les deux modes coexistent via le fichier src/core/database.py qui initialise a la fois "
    "l'instance databases.Database et l'engine SQLAlchemy. Les tables sont definies une seule "
    "fois dans les modèles et partagees entre les deux modes."
)

# ======================================================================
# PARTIE 2 : SCHEMA DES 5 TABLES
# ======================================================================
pdf.add_page()
pdf.chapter_title("2", "Schema des 5 tables")

pdf.section_title("2.1 Table healing_events")
pdf.body_text(
    "Stocke chaque tentative de healing (auto-reparation) d'un localisateur Selenium. "
    "C'est la table la plus riche du projet avec 18 colonnes."
)
pdf.ln(1)
healing_cols = [
    ("Colonne", "Type SQL", "Modèle Python", "Description"),
    ("id", "SERIAL", "Integer, PK", "Identifiant unique"),
    ("created_at", "TIMESTAMPTZ", "DateTime(timezone=True)", "Horodatage de l'événement"),
    ("scenario_name", "VARCHAR", "String", "Nom du scénario Cucumber concerne"),
    ("old_locator_type", "VARCHAR(100)", "String", "Type de l'ancien locator (xpath, css, id...)"),
    ("old_locator_val", "TEXT", "Text", "Valeur de l'ancien locator"),
    ("success", "BOOLEAN", "Boolean, NOT NULL", "Succes ou echec de la guerison"),
    ("score", "NUMERIC(10,4)", "Numeric", "Score final de confiance (0-1)"),
    ("structural_score", "NUMERIC(10,4)", "Numeric", "Score de similarite structurelle"),
    ("semantic_score", "NUMERIC(10,4)", "Numeric", "Score de similarite semantique"),
    ("new_locator_type", "VARCHAR(100)", "String", "Type du nouveau locator propose"),
    ("new_locator_val", "TEXT", "Text", "Valeur du nouveau locator"),
    ("healing_time_ms", "INTEGER", "Integer", "Temps de resolution en ms"),
    ("baseline_hit", "BOOLEAN", "Boolean, default=False", "Resolu via le cache baseline ?"),
    ("elements_extracted", "INTEGER", "Integer", "Nb d'éléments extraits du DOM"),
    ("after_struct_filter", "INTEGER", "Integer", "Nb d'éléments après filtre structurel"),
    ("after_spatial_filter", "INTEGER", "Integer", "Nb d'éléments après filtre spatial"),
    ("sent_to_nlp", "INTEGER", "Integer", "Nb d'éléments envoyes au NLP"),
    ("error_message", "TEXT", "Text", "Message d'erreur si echec"),
    ("exception_type", "VARCHAR(64)", "String(64)", "Type d'exception Java (NoSuchElementException...)"),
    ("run_id", "VARCHAR(128)", "String(128)", "Identifiant du run de test associe"),
]
w1 = [38, 35, 40, 75]
pdf.table_row(healing_cols[0], w1, bold=True, fill=True)
for row in healing_cols[1:]:
    pdf.table_row(row, w1)

pdf.ln(2)
pdf.body_text(
    "Les colonnes elements_extracted, after_struct_filter, after_spatial_filter, sent_to_nlp "
    "retracent le pipeline de healing : extraction DOM -> filtre structurel -> filtre spatial -> NLP. "
    "Ces compteurs permettent de diagnostiquer l'efficacite de chaque étape du pipeline."
)

pdf.section_title("2.2 Table metrics_snapshots")
pdf.body_text(
    "Stocke des instantanes periodiques des métriques globales de healing. "
    "Contrairement a healing_events qui stocke des événements individuels, metrics_snapshots "
    "stocke des valeurs agrégées (taux, moyennes) calculees par le notebook Colab ou derivees "
    "des événements."
)
pdf.ln(1)
metrics_cols = [
    ("Colonne", "Type SQL", "Description"),
    ("id", "SERIAL PK", "Identifiant unique"),
    ("captured_at", "TIMESTAMPTZ", "Horodatage de la capture"),
    ("total_healing_requests", "INTEGER", "Nombre total de requêtes de healing"),
    ("successful_healings", "INTEGER", "Guerisons reussies"),
    ("failed_healings", "INTEGER", "Guerisons echouees"),
    ("baseline_hits", "INTEGER", "Guerisons via baseline cache"),
    ("total_elements_extracted", "INTEGER", "Total éléments extraits du DOM"),
    ("total_after_struct", "INTEGER", "Après filtre structurel"),
    ("total_after_spatial", "INTEGER", "Après filtre spatial"),
    ("total_sent_to_nlp", "INTEGER", "Envoyes au NLP"),
    ("total_healing_time_ms", "BIGINT", "Temps total de healing en ms"),
    ("healing_rate", "NUMERIC(6,4)", "Taux de guerison (successful/total)"),
    ("baseline_hit_rate", "NUMERIC(6,4)", "Taux d'utilisation du baseline"),
    ("avg_healing_time_ms", "NUMERIC(12,2)", "Temps moyen de guerison"),
    ("avg_final_score", "NUMERIC(8,4)", "Score moyen pondere"),
    ("avg_structural_score", "NUMERIC(8,4)", "Score structurel moyen"),
    ("avg_semantic_score", "NUMERIC(8,4)", "Score semantique moyen"),
    ("nlp_filter_efficiency", "NUMERIC(6,4)", "Ratio éléments evites / éléments extraits"),
    ("run_id", "VARCHAR(128)", "Run de test associe"),
]
w2 = [38, 35, 115]
pdf.table_row(metrics_cols[0], w2, bold=True, fill=True)
for row in metrics_cols[1:]:
    pdf.table_row(row, w2)

pdf.ln(2)
pdf.body_text(
    "NUMERIC(6,4) permet des taux sur 4 decimales (ex: 0.9534). "
    "BIGINT pour total_healing_time_ms car la somme peut etre grande. "
    "Note : les colonnes run_id sont partagees entre les 3 tables principales "
    "pour permettre les jointures et le filtrage par run."
)

pdf.section_title("2.3 Table cucumber_runs")
pdf.body_text(
    "Stocke les resultats de chaque scénario Cucumber exécuté. Chaque ligne represente "
    "un scénario unique (feature + nom) avec son statut et sa duree."
)
pdf.ln(1)
cucumber_cols = [
    ("Colonne", "Type SQL", "Description"),
    ("id", "SERIAL PK", "Identifiant unique"),
    ("run_at", "TIMESTAMPTZ", "Horodatage de l'exécution"),
    ("feature_name", "VARCHAR(255)", "Nom de la feature Cucumber"),
    ("scénario", "VARCHAR(255)", "Nom du scénario"),
    ("status", "VARCHAR(32)", "passed, failed, skipped, flaky"),
    ("duration_ns", "BIGINT", "Duree en nanosecondes"),
    ("tags", "TEXT", "Tags Cucumber (ex: @login, @smoke)"),
    ("run_id", "VARCHAR(128)", "Run de test associe"),
    ("classification", "VARCHAR(32)", "Classification après rerun : passed/flaky/failed"),
]
w3 = [35, 30, 120]
pdf.table_row(cucumber_cols[0], w3, bold=True, fill=True)
for row in cucumber_cols[1:]:
    pdf.table_row(row, w3)

pdf.ln(2)
pdf.body_text(
    "duration_ns est stocke en nanosecondes (precision Java System.nanoTime()) et converti "
    "en millisecondes au niveau API (division par 1 000 000). La colonne classification est "
    "renseignee par le TestClassifier après re-exécution des scénarios echoues."
)

pdf.section_title("2.4 Table test_runs")
pdf.body_text(
    "Stocke les executions de test lancees via le dashboard (TestRunManager). "
    "Contrairement aux autres tables, elle utilisé run_id comme cle primaire directe "
    "(pas de SERIAL auto-increment)."
)
pdf.ln(1)
testrun_cols = [
    ("Colonne", "Type SQL", "Description"),
    ("run_id", "VARCHAR(128) PK", "UUID hex (12 caracteres) généré par le manager"),
    ("tags", "VARCHAR", "Tags Cucumber passes a l'exécution"),
    ("runner", "VARCHAR", "Classe runner utilisée"),
    ("suite_name", "VARCHAR", "Nom de la suite de tests"),
    ("status", "VARCHAR(32)", "pending, running, completed, failed, cancelled"),
    ("created_at", "TIMESTAMPTZ", "Date de création"),
    ("completed_at", "TIMESTAMPTZ NULLABLE", "Date de fin (NULL si en cours)"),
    ("logs", "TEXT", "Logs d'exécution (JSON array serialise)"),
    ("exit_code", "INTEGER NULLABLE", "Code de sortie Maven"),
    ("error", "TEXT NULLABLE", "Message d'erreur si echec"),
]
pdf.table_row(testrun_cols[0], w3, bold=True, fill=True)
for row in testrun_cols[1:]:
    pdf.table_row(row, w3)

pdf.ln(2)
pdf.body_text(
    "La colonne logs stocke un tableau JSON serialize (ex: [\"[runner] Starting Maven...\", "
    "\"[runner] Tests completed\"]). Le repository TestRunRepository utilisé "
    "json.dumps/json.loads pour la serialisation/deserialisation. Ce choix evite une table "
    "séparée de logs et simplifie le stockage."
)

pdf.section_title("2.5 Table app_users")
pdf.body_text(
    "Stocke les utilisateurs du dashboard avec leur role et leur mot de passe hache."
)
pdf.ln(1)
users_cols = [
    ("Colonne", "Type SQL", "Description"),
    ("id", "SERIAL PK", "Identifiant unique"),
    ("email", "VARCHAR(255) UNIQUE NOT NULL", "Email de l'utilisateur"),
    ("password_hash", "VARCHAR(255) NOT NULL", "Hash bcrypt du mot de passe"),
    ("display_name", "VARCHAR(255) NOT NULL", "Nom affiche"),
    ("role", "VARCHAR(32) NOT NULL", "admin, project_manager, qa_engineer"),
    ("is_active", "BOOLEAN DEFAULT TRUE", "Compte actif ou desactive"),
    ("created_at", "TIMESTAMPTZ", "Date de création"),
    ("updated_at", "TIMESTAMPTZ", "Date de dernière modification"),
]
pdf.table_row(users_cols[0], w3, bold=True, fill=True)
for row in users_cols[1:]:
    pdf.table_row(row, w3)

pdf.ln(2)
pdf.body_text(
    "Le mot de passe est hache avec bcrypt via la bibliotheque passlib. Chaque hash est "
    "unique (sel sale) meme pour deux mots de passe identiques. La contrainte CHECK (role IN (...)) "
    "est definie dans le schema.sql mais pas dans le modèle SQLAlchemy (la validation est faite "
    "par Pydantic dans le schema UserCreate/UserUpdate)."
)

# ======================================================================
# PARTIE 3 : INDEX ET PERFORMANCE
# ======================================================================
pdf.add_page()
pdf.chapter_title("3", "Index et performance")

pdf.section_title("3.1 Index definis")
pdf.body_text(
    "Les indexes sont definis dans le fichier db/schema.sql et complètes automatiquement "
    "par la méthode ensure_schema_compatibility(). Voici la liste complète :"
)
pdf.ln(1)
idx_cols = [
    ("Table", "Index", "Colonne(s)", "Type"),
    ("healing_events", "idx_healing_events_created_at", "created_at DESC", "BTREE"),
    ("healing_events", "idx_healing_events_run_id", "run_id", "BTREE"),
    ("metrics_snapshots", "idx_metrics_snapshots_captured_at", "captured_at DESC", "BTREE"),
    ("metrics_snapshots", "idx_metrics_snapshots_run_id", "run_id", "BTREE"),
    ("cucumber_runs", "idx_cucumber_runs_run_at", "run_at DESC", "BTREE"),
    ("cucumber_runs", "idx_cucumber_runs_status", "status", "BTREE"),
    ("cucumber_runs", "idx_cucumber_runs_run_id", "run_id", "BTREE"),
    ("app_users", "app_users_email_key", "email (UNIQUE)", "UNIQUE BTREE"),
]
y = pdf.get_y()
w4 = [35, 55, 45, 30]
pdf.table_row(idx_cols[0], w4, bold=True, fill=True)
for row in idx_cols[1:]:
    pdf.table_row(row, w4)

pdf.section_title("3.2 Explication des types d'index (BTREE)")
pdf.body_text(
    "BTREE (Balanced Tree) est la structure d'index par défaut dans PostgreSQL et dans "
    "la plupart des SGBD relationnels. Il s'agit d'un arbre equilibre qui stocke les valeurs "
    "dans un ordre trie (ASC ou DESC) et permet les operations suivantes en temps "
    "O(log n) (logarithmique) :"
)
pdf.bullet("Recherche par egalite : WHERE run_id = 'abc123' -> trouve l'enregistrement en quelques étapes, meme avec des millions de lignes.")
pdf.bullet("Recherche par plage : WHERE created_at > '2025-01-01' -> parcourt sequentiellement les feuilles de l'arbre.")
pdf.bullet("Tri : ORDER BY created_at DESC -> lit les feuilles dans l'ordre, pas de sort supplementaire.")
pdf.bullet("Agrégation avec GROUP BY : utilisé le parcours ordonne pour les fonctions COUNT, SUM, AVG.")
pdf.body_text(
    "Sans index, PostgreSQL doit faire un Sequential Scan (parcours integral) de toute la table, "
    "ce qui est O(n) et devient très lent avec le volume. Avec un index BTREE, la recherche "
    "passe en O(log n). Par exemple, pour trouver un enregistrement parmi 1 million de lignes, "
    "un index BTREE nécessité environ 20 étapes (log2(1 000 000) ~= 20) au lieu de 1 million."
)
pdf.body_text(
    "Les indexes DESC sont declares explicitement dans le schema.sql car la plupart des "
    "requêtes trient par date decroissante (du plus recent au plus ancien). Un index DESC "
    "evite a PostgreSQL de parcourir l'index a l'envers, ce qui offre un gain de performance "
    "marginal mais systematique."
)
pdf.ln(1)
pdf.subsection_title("Analyse des indexes du projet")
pdf.body_text(
    "Les indexes DESC sur created_at/captured_at/run_at optimisent la requête "
    "'ORDER BY ... DESC LIMIT 1' qui récupéré le dernier enregistrement "
    "(utilisée par get_latest_metrics(), get_latest_run_id(), etc.). "
    "L'index sur run_id est utilisé par toutes les requêtes filtrees par run "
    "(get_counts_by_run_id(), fetch_by_run_id(), etc.). "
    "L'index sur status est utilisé par les requêtes d'agrégation GROUP BY status "
    "(get_counts_by_run_id() dans cucumber_summary())."
)

pdf.section_title("3.3 Requêtes critiques et leur plan")
pdf.body_text("Requête la plus fréquente : récupération du dashboard")
pdf.code_block(
    "-- Resumé des resultats Cucumber pour un run\n"
    "SELECT status, COUNT(*) as cnt\n"
    "FROM cucumber_runs\n"
    "WHERE run_id = 'abc123' AND run_id IS NOT NULL\n"
    "GROUP BY status;\n"
    "-> Utilisé l'index idx_cucumber_runs_run_id, pas de seq scan"
)
pdf.body_text("Requête la plus couteuse : historique des métriques")
pdf.code_block(
    "-- Historique des snapshots (dashboard: historique des tendances)\n"
    "SELECT * FROM metrics_snapshots\n"
    "WHERE run_id = 'abc123'\n"
    "ORDER BY captured_at ASC\n"
    "LIMIT 50;\n"
    "-> Utilisé l'index idx_metrics_snapshots_run_id"
)
pdf.body_text("Requête de calcul de métriques :")
pdf.code_block(
    "-- Calcul du healing_rate et avg_final_score (fallback healing_events)\n"
    "SELECT COUNT(*), SUM(success::int), AVG(score), ...\n"
    "FROM healing_events\n"
    "WHERE run_id = 'abc123';\n"
    "-> Utilisé l'index idx_healing_events_run_id"
)

# ======================================================================
# PARTIE 4 : REPOSITORIES ET ACCES AUX DONNÉES
# ======================================================================
pdf.add_page()
pdf.chapter_title("4", "Repositories et accès aux données")

pdf.section_title("4.1 Pattern Repository")
pdf.body_text(
    "Le pattern Repository est utilisé pour encapsuler l'accès aux données. Chaque table "
    "a son propre repository qui expose des méthodes metier (create_scenario(), get_latest_metrics(), "
    "fetch_by_run_id(), etc.). Les repositories ne connaissent pas la couche service : "
    "ils recoivent des parametres simples et retournent des dictionnaires Python."
)

pdf.section_title("4.2 CucumberRunRepository")
pdf.body_text("Méthodes exposees :")
pdf.bullet("create_scenario(run_at, feature_name, scénario, status, duration_ns, tags, run_id, classification) : insere un resultat de scénario.")
pdf.bullet("get_latest_run_id() : SELECT MAX(run_id) avec tri par run_at DESC, retourné le dernier run_id non-null.")
pdf.bullet("get_latest_run_at(run_id) : SELECT MAX(run_at) pour un run_id donne.")
pdf.bullet("get_global_latest_run_at() : SELECT MAX(run_at) global, utilisé quand aucun run_id n'est fourni.")
pdf.bullet("get_counts_by_run_id(run_id) : SELECT status, COUNT(*) GROUP BY status pour un run. Utilisé par cucumber_summary() pour calculer passed/failed/skipped/total.")
pdf.bullet("get_avg_duration(run_id) : SELECT AVG(duration_ns) pour le temps moyen par scénario.")
pdf.bullet("get_recent(run_id, limit) : SELECT * FROM cucumber_runs ORDER BY run_at DESC LIMIT limit. Utilisé par la page Tests du dashboard.")

pdf.section_title("4.3 HealingEventRepository")
pdf.body_text("Méthodes exposees :")
pdf.bullet("create(event_data) : insere un événement avec created_at = now(UTC).")
pdf.bullet("list(limit, offset, run_id) : pagination avec tri par created_at DESC, filtre optionnel par run_id.")
pdf.bullet("fetch_all() : toutes les données, utilisé par l'agrégation de métriques derivees.")
pdf.bullet("fetch_by_run_id(run_id) : tous les événements d'un run spécifique.")
pdf.bullet("fetch_with_null_run_id() : événements sans run_id (cas legacy).")
pdf.bullet("fetch_recent(run_id, cutoff, limit) : événements recents avec cut-off temporel (run_at - 3h) pour eviter les doublons entre runs.")

pdf.section_title("4.4 MetricsSnapshotRepository")
pdf.body_text("Méthodes exposees :")
pdf.bullet("create(snapshot_data) : insere un snapshot avec captured_at = now(UTC).")
pdf.bullet("get_latest(run_id) : récupéré le snapshot le plus recent (ORDER BY captured_at DESC LIMIT 1), filtre optionnel par run_id.")
pdf.bullet("get_history(run_id, limit) : historique trie par captured_at ASC pour le graphique d'evolution. Limite par défaut a 50 points.")
pdf.bullet("fetch_all(), fetch_by_run_id(run_id), fetch_with_null_run_id() : memes patterns que HealingEventRepository.")

pdf.section_title("4.5 TestRunRepository (synchrone)")
pdf.body_text(
    "Ce repository utilisé un engine SQLAlchemy synchrone (sqlalchemy.create_engine) "
    "contrairement aux autres qui utilisent databases.Database asynchrone."
)
pdf.bullet("upsert(run_data) : INSERT OR UPDATE (upsert). Vérifié l'existence par run_id, exécuté UPDATE si existant, INSERT sinon. Les logs sont serialises en JSON.")
pdf.bullet("get(run_id) : SELECT avec deserialisation JSON des logs et normalisation des datetime.")
pdf.bullet("list(limit, offset) : SELECT pagine avec ORDER BY created_at DESC. Utilisé par TestRunManager.list_runs() qui merge les données mémoire et BDD.")

pdf.section_title("4.6 UserRepository")
pdf.body_text(
    "Repository asynchrone pour la gestion des utilisateurs. Utilisé le schema Pydantic User "
    "pour le typage des retours (find_by_email, find_by_id retournent des instances User)."
)
pdf.bullet("find_by_email(email) / find_by_id(user_id) : recherche par email ou id, retourné User ou None.")
pdf.bullet("create(email, password_hash, display_name, role) : création avec horodatage.")
pdf.bullet("update(user_id, updates) : mise a jour partielle, leve HTTPException 404 si inexistant.")
pdf.bullet("delete(user_id) : suppression physique.")
pdf.bullet("change_password(user_id, password_hash) : mise a jour du hash uniquement.")
pdf.bullet("list_all() : tous les utilisateurs tries par created_at ASC.")

# ======================================================================
# PARTIE 5 : MECANISMES DE MIGRATION
# ======================================================================
pdf.add_page()
pdf.chapter_title("5", "Mecanismes de migration et schema")

pdf.section_title("5.1 Initialisation via schema.sql")
pdf.body_text(
    "Le fichier db/schema.sql est monte dans le conteneur PostgreSQL via "
    "docker-compose.yml comme script d'initialisation :"
)
pdf.code_block(
    "services:\n"
    "  postgres:\n"
    "    ...\n"
    "    volumes:\n"
    "      - ./db/schema.sql:/docker-entrypoint-initdb.d/schema.sql"
)
pdf.body_text(
    "Au démarrage du conteneur, PostgreSQL exécuté automatiquement tous les scripts "
    "dans /docker-entrypoint-initdb.d/ par ordre alphabetique, avant d'accepter les "
    "connexions. Ce mécanisme garantit que les tables existent avant le démarrage de l'API."
)

pdf.section_title("5.2 Création automatique via SQLAlchemy")
pdf.body_text(
    "Au démarrage de l'API FastAPI, une deuxieme couche de création est executee :"
)
pdf.code_block(
    "@app.on_event(\"startup\")\n"
    "async def startup():\n"
    "    metadata.create_all(engine)        # Tables healing_events, metrics_snapshots,\n"
    "                                       #   cucumber_runs, test_runs\n"
    "    UserModel.metadata.create_all(engine)  # Table app_users\n"
    "    _run_schema_compat()               # Migrations ALTER TABLE\n"
    "    await database.connect()\n"
    "    await _seed_default_users()         # 3 comptes par défaut"
)
pdf.body_text(
    "metadata.create_all(engine) utilisé SQLAlchemy pour créer les tables si elles "
    "n'existent pas encore. Cette approche est idempotente : si la table existe deja, "
    "elle n'est pas recreee. Cela permet au schema.sql et a SQLAlchemy de coexister."
)

pdf.section_title("5.3 Migrations dynamiques")
pdf.body_text(
    "La méthode DashboardService.ensure_schema_compatibility() gere les evolutions "
    "du schema au fil des versions. Au lieu d'utiliser un outil de migration (Alembic), "
    "le projet exécuté des ALTER TABLE ADD COLUMN IF NOT EXISTS au démarrage :"
)
pdf.code_block(
    "@staticmethod\n"
    "def ensure_schema_compatibility(engine):\n"
    "    statements = [\n"
    "        \"ALTER TABLE healing_events ADD COLUMN IF NOT EXISTS exception_type VARCHAR(64)\",\n"
    "        \"ALTER TABLE healing_events ADD COLUMN IF NOT EXISTS run_id VARCHAR(128)\",\n"
    "        \"ALTER TABLE metrics_snapshots ADD COLUMN IF NOT EXISTS run_id VARCHAR(128)\",\n"
    "        \"ALTER TABLE cucumber_runs ADD COLUMN IF NOT EXISTS run_id VARCHAR(128)\",\n"
    "        \"CREATE INDEX IF NOT EXISTS idx_healing_events_run_id ON healing_events (run_id)\",\n"
    "        \"CREATE INDEX IF NOT EXISTS idx_metrics_snapshots_run_id ON metrics_snapshots (run_id)\",\n"
    "        \"CREATE INDEX IF NOT EXISTS idx_cucumber_runs_run_id ON cucumber_runs (run_id)\",\n"
    "        \"ALTER TABLE cucumber_runs ADD COLUMN IF NOT EXISTS classification VARCHAR(32)\",\n"
    "    ]\n"
    "    with engine.begin() as conn:\n"
    "        for sql in statements:\n"
    "            conn.exécuté(sqlalchemy.text(sql))"
)
pdf.body_text(
    "Cette approche est deliberement simple et adaptee a un projet de cette taille. "
    "Pour un projet en production avec plusieurs environnements, Alembic serait recommande "
    "car il permet des rollbacks structures et un versionnage explicite des migrations."
)

pdf.section_title("5.4 Fallback SQLite pour les tests")
pdf.body_text(
    "Quand l'URL de base de données contient \"sqlite\" (detecte par la comparaison "
    "case-insensitive de l'URL), plusieurs comportements changent :"
)
pdf.bullet("Tables creees immediatement : database.py exécuté create_all pour toutes les tables au moment de l'import (pas besoin du lifecycle FastAPI).")
pdf.bullet("Migrations ignorees : _run_schema_compat() ne fait rien ('if sqlite in str(engine.url).lower(): return').")
pdf.bullet("Tests isoles : le module test_auth.db est un fichier SQLite dedie aux tests d'authentification.")
pdf.bullet("Seed users integre : les comptes par défaut sont crees via _seed_default_users() meme en SQLite.")

# ======================================================================
# PARTIE 6 : FLUX DE DONNÉES
# ======================================================================
pdf.add_page()
pdf.chapter_title("6", "Flux de données detailles")

pdf.section_title("6.1 Insertion des resultats Cucumber")
pdf.body_text(
    "Les resultats Cucumber sont inseres via deux chemins :"
)
pdf.ln(1)
pdf.subsection_title("Chemin 1 : Dashboard Test Runner (Python)")
pdf.body_text(
    "Le TestRunManager.execute_maven_run() exécuté Maven avec DashboardCucumberRunner, "
    "puis appelle dashboard_api.push_cucumber_json() qui parse le fichier cucumber-{run_id}.json "
    "et appelle POST /api/cucumber-runs pour chaque scénario."
)
pdf.code_block(
    "Thread Maven -> cucumber-{run_id}.json\n"
    "  -> parse JSON (feature.éléments)\n"
    "  -> compute_scenario_status (steps + hooks)\n"
    "  -> POST /api/cucumber-runs -> create_scenario() x N"
)

pdf.subsection_title("Chemin 2 : CI/CD DashboardPushMain (Java)")
pdf.body_text(
    "DashboardPushMain est exécuté par le plugin exec-maven-plugin pendant la phase test. "
    "Il lit target/cucumber.json et appelle l'API REST du dashboard."
)
pdf.code_block(
    "Maven test -> cucumber.json\n"
    "  -> DashboardPushMain.java\n"
    "  -> POST /api/cucumber-runs (machine-to-machine via X-API-Key)"
)

pdf.section_title("6.2 Insertion des événements de healing")
pdf.body_text(
    "Chaque tentative de healing est inseree via DashboardReporter.pushHealingEvent() "
    "qui appelle POST /api/healing-events. Les champs envoyes incluent tous les scores, "
    "les compteurs du pipeline (elements_extracted, after_struct_filter, ...) et le "
    "type d'exception Java."
)
pdf.code_block(
    "BasePage.findElement() -> NoSuchElementException\n"
    "  -> callHealingAPI() -> POST /heal\n"
    "  -> pushHealingEvent()\n"
    "    -> POST /api/healing-events\n"
    "      -> HealingEventRepository.create() -> INSERT INTO healing_events"
)

pdf.section_title("6.3 Calcul et stockage des métriques")
pdf.body_text(
    "Les snapshots de métriques sont inseres via POST /api/metrics par le notebook Colab. "
    "Chaque snapshot contient les valeurs agrégées calculees sur l'ensemble des données."
)
pdf.body_text(
    "Si aucun snapshot n'est disponible (fallback), DashboardService.aggregate_metrics() "
    "calcule les métriques a partir des données brutes de healing_events :"
)
pdf.code_block(
    "healing_rate = successful_healings / total_healing_requests\n"
    "baseline_hit_rate = baseline_hits / total\n"
    "avg_healing_time_ms = total_healing_time_ms / total\n"
    "avg_final_score = AVG(score) sur les événements avec score non-null\n"
    "nlp_filter_efficiency = 1 - (total_sent_to_nlp / total_elements_extracted)"
)

pdf.section_title("6.4 Resolution des métriques (fallback 3 niveaux)")
pdf.body_text(
    "La méthode resolve_healing_payload() implemente un fallback en 3 étapes :"
)
pdf.ln(1)
pdf.body_text("Niveau 1 : metrics_snapshots (table)")
pdf.bullet("Cherche le snapshot le plus recent pour le run_id donne.")
pdf.bullet("Requête : SELECT * FROM metrics_snapshots WHERE run_id = ? ORDER BY captured_at DESC LIMIT 1.")
pdf.bullet("Si trouve, retourné les métriques + historique + événements recents.")

pdf.body_text("Niveau 2 : agrégation depuis healing_events (table)")
pdf.bullet("Si aucun snapshot, agrégé les événements de healing bruts.")
pdf.bullet("Requête : SELECT COUNT(*), SUM(success), AVG(score), ... FROM healing_events WHERE run_id = ?.")
pdf.bullet("Calcule healing_rate, baseline_hit_rate, avg_healing_time_ms, etc. via aggregate_metrics().")

pdf.body_text("Niveau 3 : endpoint externe (SELF_HEALING_METRICS_URL)")
pdf.bullet("Si aucune donnee en base, appelle l'API externe (notebook Colab /metrics).")
pdf.bullet("Requête HTTP GET avec timeout configurable (5s par défaut).")
pdf.bullet("Normalise la reponse via normalize_external_metrics().")

pdf.body_text(
    "Le scope de recherche suit aussi un fallback : run_id spécifique -> run_id=NULL -> "
    "n'importe quel run (latest_available)."
)

pdf.section_title("6.5 Gestion des test runs")
pdf.body_text(
    "TestRunManager gere les executions de test de maniere duale :"
)
pdf.bullet("Mémoire vive : dictionnaire _test_runs (dict[str, dict]) avec verrou threading.Lock() pour la concurrence.")
pdf.bullet("Persistence BDD : TestRunRepository.upsert() sauvegarde le run a la fin de l'exécution (status, logs, exit_code).")
pdf.bullet("Merge a la lecture : list_runs() fusionne les runs mémoire et BDD, deduplication par run_id, tri par created_at DESC, pagination.")
pdf.code_block(
    "1. create_run() -> généré UUID, stocke dans _test_runs[run_id]\n"
    "2. execute_maven_run() -> status=running, lance Maven\n"
    "3. A chaque ligne de log -> _append_log(run_id, line)\n"
    "4. Fin exécution -> status=completed/failed, persist en BDD\n"
    "5. list_runs() -> merge mémoire + BDD, tri DESC, pagination"
)

# ======================================================================
# PARTIE 7 : Q&A
# ======================================================================
pdf.add_page()
pdf.chapter_title("7", "Q&A - Questions possibles")
pdf.body_text(
    "Cette section contient les questions probables lors de la soutenance, "
    "classees par theme et par niveau de difficulte. Les questions marquees "
    "avec un asterisque sont des questions expertes pour les rapporteurs "
    "specialises en bases de données."
)
pdf.ln(2)

pdf.section_title("7.1 Questions sur le choix de la technologie")

pdf.qa_item(
    "Pourquoi PostgreSQL plutot que MySQL ou MariaDB ?",
    "PostgreSQL offre un meilleur support des types temporels (TIMESTAMPTZ avec fuseau), "
    "des fonctions de fenetrage pour les tendances historiques, et des indexes avances "
    "(partiels, GIN). La precision numérique (NUMERIC(10,4)) est mieux respectee que MySQL. "
    "De plus, Docker l'image Alpine est plus leger que MariaDB."
)

pdf.qa_item(
    "Pourquoi ne pas utiliser MongoDB ou une autre base NoSQL ?",
    "Les données sont parfaitement relationnelles : les resultats Cucumber ont un schema fixe, "
    "les événements de healing ont des relations avec les runs, et les utilisateurs sont "
    "typiques d'un systeme d'authentification. Les requêtes d'agrégation (GROUP BY, AVG, SUM) "
    "sont natives en SQL. MongoDB aurait ajouté de la complexite sans benefice."
)

pdf.qa_item(
    "Pourquoi deux modes d'accès (async databases et sync engine) ?",
    "FastAPI est asynchrone, donc les repositories utilisant databases.Database ne bloquent "
    "pas la boucle d'événements pendant les I/O. Cependant, TestRunRepository utilisé un "
    "engine synchrone car il est appele depuis des threads standards (threading.Thread) "
    "qui ne peuvent pas utiliser les ressources asynchrones de l'API. Le choix est pragmatique "
    "et chaque mode est utilisé dans le contexte le plus adapte."
)

pdf.qa_item(
    "Pourquoi utiliser SQLAlchemy Core plutot que l'ORM complet dans les repositories ?",
    "Les repositories utilisent SQLAlchemy Core (sqlalchemy.select(), table.insert(), etc.) "
    "plutot que l'ORM (session, unit of work) car les operations sont simples et directes. "
    "L'ORM aurait ajouté un surcout de performance et une complexite inutile pour des "
    "insertions et des select basiques. Les modèles (DeclarativeBase) sont uniquement "
    "utilisés pour la definition des tables et la création du schema."
)

pdf.section_title("7.2 Questions sur le schema et la modelisation")

pdf.qa_item(
    "Pourquoi les colonnes run_id sont partagees entre 3 tables sans cle etrangere explicite ?",
    "C'est un choix delibere de simplicite : les run_id sont des chaînes de caracteres "
    "générées par le TestRunner Python (UUID hex 12 caracteres) ou par le CI/CD. "
    "L'absence de contrainte FOREIGN KEY permet une flexibilite : les données peuvent "
    "arriver de sources différentes (tests locaux, CI/CD, Colab) sansordre strict. "
    "Les indexes BTREE sur run_id compensent le manque de contrainte pour les performances."
)

pdf.qa_item(
    "Comment est garanti le type des colonnes NUMERIC si SQLite ne les supporte pas ?",
    "SQLite stocke tous les types numériques dans un format dynamique (pas de distinction "
    "INTEGER/FLOAT/NUMERIC). Le modèle Python declare NUMERIC(10,4) mais SQLAlchemy ne "
    "force pas la precision sur SQLite. C'est acceptable car SQLite est uniquement utilisé "
    "pour les tests unitaires, pas en production. En production PostgreSQL, le type "
    "NUMERIC(10,4) est strictement respecte avec arrondi a 4 decimales."
)

pdf.qa_item(
    "Pourquoi la colonne logs de test_runs est un TEXT contenant du JSON et pas une table séparée ?",
    "Cette decision simplifie le modèle et evite une jointure supplementaire a chaque lecture. "
    "Les logs sont rarement consultes individuellement (toujours en bloc avec le run). "
    "Cependant, cela rend les requêtes d'indexation ou de recherche dans les logs impossibles "
    "au niveau SQL. Pour une version future, une table separate logs serait souhaitable si "
    "la recherche dans les logs devient un besoin."
)

pdf.qa_item(
    "Quelle est la difference entre classification et status dans cucumber_runs ?",
    "status est le resultat brut du premier run (passed/failed/skipped). classification "
    "est le resultat après re-exécution (rerun) determine par TestClassifier : 'passed' "
    "(passe du premier coup), 'flaky' (echoue puis passe au rerun), 'failed' (echoue "
    "aux deux runs). La separation permet de suivre l'historique des tests flaky."
)

pdf.section_title("7.3 Questions sur les performances")

pdf.qa_item(
    "Quel est le volume de données attendu et comment evolue-t-il ?",
    " healing_events : 1 a 10 événements par test, 13 suites x ~50 scénarios = 650 runs "
    "par CI/CD, soit 650 a 6500 événements par pipeline. metrics_snapshots : 1 snapshot "
    "par run Colab. cucumber_runs : ~50 scénarios/suite x 13 suites = 650 lignes par run. "
    "A raison de 10 pipelines/jour, on obtient ~6500 lignes/jour pour healing_events. "
    "Aucun problème de performance pour PostgreSQL 16 avec les indexes en place."
)

pdf.qa_item(
    "Les indexes (DESC sur created_at) sont-ils vraiment utilisés ?",
    "Oui. PostgreSQL utilisé un Index Scan Backward sur un index BTREE standard (ASC) "
    "pour les ORDER BY DESC, ce qui est aussi efficace qu'un index DESC natif. "
    "Les indexes DESC sont declares explicitement dans le schema.sql pour une question "
    "de clarte, mais le comportement est identique. Les requêtes de type 'get_latest' "
    "sont executees en O(log n) au lieu de O(n log n) pour un seq scan + sort."
)

pdf.qa_item(
    "Comment est calculee la limite de 50 points pour l'historique des métriques ?",
    "C'est un choix arbitraire pour le dashboard. 50 points suffisent pour visualiser "
    "une tendance sans surcharger le navigateur. La requête utilisé LIMIT 50 avec un "
    "ORDER BY captured_at ASC, ce qui est efficace avec l'index. Si l'utilisateur souhaite "
    "plus d'historique, le parametre limit peut etre augmente sans impact majeur."
)

pdf.qa_item(
    "Y a-t-il des risques de contention sur la table healing_events ?",
    "Non, car les insertions se font depuis le CI/CD (un seul thread a la fois, "
    "suite séquentielle) ou depuis un utilisateur local. En mode multi-utilisateur, "
    "les insertions sont rapides et PostgreSQL gere bien la concurrence en ecriture. "
    "Les lectures sont des SELECT agregeant qui sont bloquantes mais très rapides "
    "avec les indexes."
)

pdf.section_title("7.4 Questions sur les migrations et l'evolution")

pdf.qa_item(
    "Pourquoi ne pas utiliser Alembic pour les migrations ?",
    "Alembic est excellent mais introduit une complexite (fichiers de migration, "
    "gestion des versions, commandes upgrade/downgrade) qui n'est pas justifiee par "
    "la taille du projet. Le nombre de tables (5) et la frequence des changements "
    "de schema (rare) rendent l'approche ALTER TABLE IF NOT EXISTS suffisante. "
    "Pour un projet d'entreprise, Alembic serait recommande."
)

pdf.qa_item(
    "Comment gerer un rollback de migration ?",
    "Actuellement, les migrations sont uniquement additives (ADD COLUMN, CREATE INDEX). "
    "Un rollback consisterait a supprimer les colonnes ou indexes ajoutes, ce qui "
    "n'est pas automatise. C'est un point d'amelioration : ajouter des versions "
    "de migration avec Alembic permettrait de monter et descendre entre versions."
)

pdf.qa_item(
    "Comment la base est-elle versionnée avec le code ?",
    "Le schema.sql est stocke dans le repertoire db/ du projet et versionné dans Git. "
    "La méthode ensure_schema_compatibility est dans le code Python. Les deux sont "
    "deployes ensemble via Docker. Il n'y a pas de versionnage explicite du schema, "
    "ce qui est une limite : on ne peut pas savoir quelle version du schema correspond "
    "a quel commit. Un fichier de version (ex: schema_version.txt) serait utile."
)

pdf.qa_item(
    "*Comment evolueraient les besoins de stockage avec l'historique des tendances ?",
    "Si on souhaite conserver un historique long (6 mois, 1 an), il faudrait "
    "soit augmenter la limite de 50 points (ce qui impacterait les perfs du dashboard), "
    "soit implementer une retention policy (nettoyage des données > 3 mois), "
    "soit ajouter une table de métriques agrégées par jour/semaine avec des vues "
    "materialisees pour les tendances. Actuellement, l'historique est limite aux "
    "50 derniers snapshots, ce qui represente environ 50 pipelines."
)

pdf.section_title("7.5 Questions sur les tests et la fiabilite")

pdf.qa_item(
    "Comment les tests unitaires utilisent-ils la base de données ?",
    "Les tests utilisent un fichier SQLite dedie (test_auth.db pour l'authentification). "
    "L'URL de connexion contient 'sqlite' ce qui active le fallback SQLite. "
    "Les tables sont creees automatiquement par SQLAlchemy, sans migration. "
    "Chaque test peut créer et supprimer des données sans impact sur la base de production."
)

pdf.qa_item(
    "Comment est assuree la fiabilite des données en cas de crash ?",
    "Trois mécanismes : 1) PostgreSQL est configuré avec WAL (Write-Ahead Logging) "
    "pour la récupération après crash. 2) TestRunRepository utilisé des transactions "
    "explicites (with engine.begin() as conn) qui rollback automatiquement en cas "
    "d'erreur. 3) Les données sont dupliquees : mémoire (_test_runs) + BDD "
    "(test_runs table), ce qui permet de resister a un crash du processus Python "
    "sans perdre les runs complètes."
)

pdf.qa_item(
    "Comment tester le comportement asynchrone des repositories ?",
    "Les tests utilisent pytest-asyncio avec asyncio_mode = 'auto' dans "
    "pyproject.toml. Les fixtures creent un engine SQLite en mémoire, initialisent "
    "les tables, et les repositories sont testes avec des coroutines. "
    "Exemple : await repository.create_scenario(...) puis await repository.get_recent(...) -> assert."
)

pdf.qa_item(
    "*Y a-t-il des tests d'integration qui verifient les migrations ?",
    "Actuellement non. Les migrations dynamiques (ensure_schema_compatibility) sont "
    "testees manuellement. Un test d'integration ideal creerait une base vierge, "
    "appliquerait les migrations, verifierait la presence des colonnes et indexes, "
    "puis executerait des insertions/lectures pour valider le schema final."
)

pdf.section_title("7.6 Questions avancees (niveau expert)")

pdf.qa_item(
    "*Pourquoi le TestRunRepository utilisé-t-il un upsert manuel (SELECT + INSERT/UPDATE) "
    "plutot qu'un vrai UPSERT PostgreSQL (INSERT ... ON CONFLICT DO UPDATE) ?",
    "Le choix de l'upsert manuel permet de rester compatible avec SQLite pour les tests. "
    "SQLite supporte INSERT OR REPLACE mais avec un comportement différent (suppression "
    "puis insertion, ce qui reset les colonnes non fournies). PostgreSQL supporte "
    "ON CONFLICT (run_id) DO UPDATE SET ... qui serait plus efficace. Une amelioration "
    "serait d'utiliser le dialecte SQLAlchemy avec un insert().on_conflict_do_update() "
    "qui s'adapte automatiquement au dialecte."
)

pdf.qa_item(
    "*Comment sont geres les TIMESTAMPTZ entre Python (avec timezone) et PostgreSQL ?",
    "Les modèles utilisent DateTime(timezone=True) qui stocke les timestamps avec "
    "timezone (TIMESTAMPTZ en PostgreSQL). Python fournit datetime.now(timezone.utc) "
    "pour les nouvelles dates. A la lecture, databases.Database retourné des datetime "
    "avec timezone, et les services les serialisent en ISO 8601 via .isoformat(). "
    "C'est coherent, mais il faut etre vigilant : Python naive datetime vs aware "
    "datetime peut causer des bugs subtils si tous les appels n'utilisent pas timezone.utc."
)

pdf.qa_item(
    "*Quel est l'impact de l'utilisation de databases.Database (asyncio) sur les transactions ?",
    "databases.Database supporte les transactions via async with database.transaction(): "
    "mais le projet ne les utilisé pas systematiquement. Chaque insert/select est "
    "une transaction implicite (autocommit). Cela peut poser problème en cas d'insertion "
    "en masse : si une insertion echoue, les précédentes ne sont pas rollbackees. "
    "Pour un usage critique, les insertions de scénarios Cucumber devraient etre "
    "dans une transaction explicite pour garantir l'atomicite du run complet."
)

pdf.qa_item(
    "*Quelle strategie de cache pourrait etre ajoutée pour les requêtes fréquentes ?",
    "Deux niveaux de cache pourraient etre ajoutes : 1) Redis en mémoire pour les "
    "requêtes les plus fréquentes (get_latest_metrics, get_counts_by_run_id) avec "
    "une expiration de quelques secondes. 2) Vues materialisees PostgreSQL pour "
    "les aggregations lourdes (healing_rate moyen par jour, tendances hebdomadaires). "
    "Actuellement, le seul cache est le healing-baseline.json côté Java pour les "
    "locators, pas pour les données du dashboard."
)

pdf.qa_item(
    "*Si vous deviez partitionner healing_events, comment le feriez-vous ?",
    "Le choix le plus naturel serait un partitionnement par plage de dates (range "
    "partitioning) sur created_at, par exemple un partition par mois. Cela permettrait "
    "de : 1) supprimer rapidement les vieilles partitions (DROP TABLE au lieu de "
    "DELETE lent). 2) ameliorer les performances des requêtes recentes (pruning de "
    "partition). 3) stocker les partitions recentes sur du stockage rapide et les "
    "anciennes sur du stockage lent. L'index run_id serait local a chaque partition."
)

pdf.ln(6)
pdf.set_font("Helvetica", "I", 9)
pdf.set_text_color(100, 100, 100)
pdf.cell(0, 5, "--- Fin du document ---", align="C", new_x="LMARGIN", new_y="NEXT")

# =========== SAVE ===========
pdf.output(OUTPUT_PATH)
print(f"PDF généré avec succes: {OUTPUT_PATH}")
print(f"Nombre de pages: {pdf.page_no()}")
