# Parcours des Métriques Self-Healing vers le Dashboard

## Les 3 modes de guérison (healing)

### 1. Baseline (cache Java côté framework)

**Fichier :** `automation-framework/src/main/java/org/automation/ai/healing/HealingBaseline.java`

- Cache mémoire `ConcurrentHashMap` persisté dans `target/healing-baseline.json`
- Clé : `{locator_type}|{locator_value}|{page_context}`
- **Aucun parsing DOM, aucun appel réseau**
- Si le même localiseur a déjà été guéri avec succès → le résultat est réutilisé
- Le healing event est poussé avec `baseline_hit=true`

### 2. CI Healing Stub (serveur Python standalone)

**Fichier :** `dashboard-api/scripts/ci_healing_stub.py` (et intégré dans `dashboard_service.py`)

- Serveur HTTP standalone sur le port `8090` ou endpoint `/heal` du dashboard API
- Parse le DOM avec `html.parser.HTMLParser` (stdlib Python, pas de ML)
- Algorithme de matching :
  - Texte exact = 1.0
  - Texte partiel = 0.8
  - Attributs (`placeholder`, `aria-label`, `title`, `name`, `data-testid`) = 0.7 à 0.9
  - Traduction FR/EN = 0.75
  - Bonus si la balise correspond = +0.1
- **Pas de modèle NLP, pas de base de données**
- Score toujours renvoyé à 0.95 en cas de succès

### 3. Moteur IA complet (Colab)

**Fichier :** `dashboard-api/notebooks/self_healing_v5.ipynb`

Pipeline complet en 5 étapes :

1. **Baseline** (côté Python) — lookup par MD5( type | valeur | hash du DOM )
2. **Extraction DOM** — BeautifulSoup + lxml, filtrage des éléments interactifs
3. **Similarité structurelle** — tag (20%), attributs (50%), contexte DOM (30%)
4. **Similarité sémantique** — SentenceTransformer `paraphrase-multilingual-mpnet-base-v2`
5. **Similarité spatiale** — distance euclidienne normalisée

Score final = `0.4 × structurel + 0.6 × sémantique` (seuil de succès : 0.75)

---

## Flux complet des données

```
                        AUTOMATION FRAMEWORK (Java)
                        ===========================

HealingWebDriver.findElement(By)
        │
        └── NoSuchElementException
            │
            ├── [BASELINE HIT] HealingBaseline.lookup()
            │     └── DashboardReporter.pushHealingEvent(success, baseline_hit=true, score, run_id)
            │             │
            │             └── POST /api/healing-events ──────────────────────────────────┐
            │                                                                           │
            └── [BASELINE MISS] SelfHealingClient.healSelector()                        │
                    │                                                                   │
                    ├── POST /heal ──→ CI STUB (port 8090)                              │
                    │     │             ou COLAB (ngrok)                                │
                    │     └── Response: new_locator, score, details                     │
                    │                                                                   │
                    └── HealingLocatorResolver.pushHealingEvent()                       │
                          └── DashboardReporter.pushHealingEvent()                      │
                                └── POST /api/healing-events ───────────────────────────┘
                                                                                        │
            DashboardReporter.push() (fin du run)                                       │
              └── POST /api/cucumber-runs ───────────────────┐                         │
                                                             │                         │
                                                             ▼                         ▼
                    ========================================= ==========================
                    DASHBOARD API (FastAPI) ──── port 8080
                    =========================================
                         │                                  │
                    healing_events                    cucumber_runs
                    (table PostgreSQL)               (table PostgreSQL)
                         │                                  │
                         ▼                                  ▼
                    metrics_snapshots (optionnel)
                    (pushé par le Colab : POST /api/metrics)


                    COLAB ENGINE (Python via ngrok)
                    ==============================
                    DashboardPusher.push_metrics_snapshot()
                      └── POST /api/metrics ──→ metrics_snapshots

```

---

## Pousse des métriques vers le Dashboard

### Depuis le framework Java

| Donnée | Endpoint API | Depuis |
|--------|-------------|--------|
| Chaque healing event | `POST /api/healing-events` | `HealingLocatorResolver.pushHealingEvent()` via `DashboardReporter` |
| Résultats Cucumber | `POST /api/cucumber-runs` | `DashboardReporter.push()` (fin du run) |
| Fichiers JSON individuels | `POST /api/cucumber-runs` | `DashboardPushMain` (CLI) |

Payload d'un healing event :
```json
{
  "scenario_name": "Login avec identifiants valides",
  "old_locator_type": "xpath",
  "old_locator_val": "//input[@id='email']",
  "new_locator_type": "xpath",
  "new_locator_val": "//input[@name='username']",
  "success": true,
  "score": 0.92,
  "structural_score": 0.88,
  "semantic_score": 0.95,
  "healing_time_ms": 320,
  "baseline_hit": false,
  "elements_extracted": 45,
  "after_struct_filter": 12,
  "after_spatial_filter": 8,
  "sent_to_nlp": 5,
  "exception_type": "NoSuchElementException",
  "run_id": "run-20260518-143022"
}
```

### Depuis le Colab

Le Colab pousse 2 types de données :

1. **Healing events** (après chaque `/heal`) → `POST /api/healing-events`
2. **Snapshots de métriques agrégées** → `POST /api/metrics`

Payload d'un snapshot metrics :
```json
{
  "total_healing_requests": 150,
  "successful_healings": 132,
  "failed_healings": 18,
  "baseline_hits": 45,
  "total_elements_extracted": 6750,
  "total_after_struct": 1800,
  "total_after_spatial": 900,
  "total_sent_to_nlp": 450,
  "total_healing_time_ms": 144000,
  "healing_rate": 0.88,
  "baseline_hit_rate": 0.30,
  "avg_healing_time_ms": 960.0,
  "avg_final_score": 0.85,
  "avg_structural_score": 0.82,
  "avg_semantic_score": 0.87,
  "nlp_filter_efficiency": 0.50,
  "run_id": "run-20260518-143022"
}
```

---

## Résolution des métriques côté Dashboard

Dans `dashboard_service.py`, la méthode `resolve_healing_payload()` suit un **fallback à 3 niveaux** :

```
1. metrics_snapshots (table)
   └── Cherche un snapshot pour le run_id donné
   └── Si trouvé → retourne les métriques directement

2. healing_events (table)
   └── Si pas de snapshot → agrège les events bruts
   └── aggregate_metrics() calcule :
        • healing_rate = successful_healings / total_healing_requests
        • baseline_hit_rate = baseline_hits / total_healing_requests
        • avg_final_score = moyenne des scores
        • nlp_filter_efficiency = sent_to_nlp / total_elements_extracted

3. SELF_HEALING_METRICS_URL (externe)
   └── Si pas de données en BDD → appelle le endpoint /metrics du Colab
   └── Timeout configurable (SELF_HEALING_METRICS_TIMEOUT_SECONDS)
```

---

## Base de données (PostgreSQL)

### Table `healing_events`
Stocke chaque tentative de healing individuelle :
- `id`, `created_at`, `scenario_name`
- `old_locator_type/val`, `new_locator_type/val`
- `success`, `score`, `structural_score`, `semantic_score`
- `healing_time_ms`, `baseline_hit`, `exception_type`
- `elements_extracted`, `after_struct_filter`, `after_spatial_filter`, `sent_to_nlp`
- `run_id`

### Table `metrics_snapshots`
Stocke des instantanés de métriques agrégées (pushé par le Colab) :
- `captured_at`, `total_healing_requests`, `successful_healings`, `failed_healings`
- `healing_rate`, `baseline_hit_rate`, `avg_healing_time_ms`
- `avg_final_score`, `avg_structural_score`, `avg_semantic_score`
- `nlp_filter_efficiency`, `run_id`

### Table `cucumber_runs`
Stocke les résultats de tests Cucumber :
- `run_at`, `feature_name`, `scenario`, `status`, `duration_ns`, `tags`, `run_id`, `classification`

---

## Configuration

**Fichier :** `automation-framework/src/main/resources/config.properties`

| Propriété | Défaut | Rôle |
|-----------|--------|------|
| `self.healing.enabled` | `true` | Active/désactive le healing |
| `self.healing.api.url` | URL ngrok Colab | Où envoyer les requêtes /heal |
| `self.healing.debug.request` | `true` | Sauvegarde les payloads dans `target/healing-debug/` |
| `dashboard.api.url` | `http://127.0.0.1:8080` | Endpoint du dashboard API |
| `dashboard.api.key` | `change-me` | Clé d'API pour l'authentification |

---

## Résumé des différences entre les 3 modes

| Aspect | Baseline Java | CI Stub | Colab (IA complet) |
|--------|--------------|---------|-------------------|
| Parsing DOM | Aucun | `html.parser` stdlib | BeautifulSoup + lxml |
| Modèle ML | Aucun | Aucun | SentenceTransformer multilingual |
| Matching | Lookup exact | Texte + attributs + FR/EN | Structurel + Sémantique + Spatial |
| Score | Stocké depuis le dernier heal | 0.95 (forcé) | 0.4×struct + 0.6×sem (seuil 0.75) |
| Metrics poussées | healing_event | Aucune | healing_event + metrics_snapshot |
| Usage | Éviter les appels répétés | CI sans accès GPU | Production (Colab + ngrok) |
