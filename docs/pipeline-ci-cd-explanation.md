# Pipeline CI/CD — Explication détaillée

Fichier : `.github/workflows/ci-cd.yml`

## 1. Déclencheurs (`on:`)

```yaml
on:
  push:
    branches: [main, develop]
    paths:
      - 'automation-framework/**'
      - '.github/workflows/**'
      - 'pom.xml'
  pull_request:
    branches: [main, develop]
    paths:
      - 'automation-framework/**'
      - '.github/workflows/**'
      - 'pom.xml'
  workflow_dispatch:
    inputs:
      suite:
        description: 'Test suite to run'
        type: choice
        options:
          - all
          - login
          - companies
          ...
        default: all
      tags:
        description: 'Cucumber tags (used when suite=custom, ...)'
        required: false
        default: ''
```

- **`push`** : Déclenché sur les pushes vers `main` et `develop`, mais seulement si les fichiers modifiés sont dans `automation-framework/`, `.github/workflows/` ou `pom.xml` (filtre `paths:`)
- **`pull_request`** : Idem pour les PR vers `main`/`develop`
- **`workflow_dispatch`** : Déclenchement manuel depuis l'interface GitHub Actions, permet de choisir :
  - `suite` : la suite de tests à exécuter (menu déroulant)
  - `tags` : les tags Cucumber personnalisés (quand `suite=custom`)

---

## 2. Variables d'environnement (`env:`)

```yaml
env:
  MAVEN_CLI_OPTS: '-pl automation-framework -P ci --no-transfer-progress'
  BACKOFFICE_URL: 'https://stg-bo.noveocare.com/login'
```

- `MAVEN_CLI_OPTS` :
  - `-pl automation-framework` → cible le module `automation-framework` dans le projet multi-module Maven (évite un `cd`)
  - `-P ci` → active le profil Maven `ci` (défini dans `automation-framework/pom.xml`)
  - `--no-transfer-progress` → désactive l'affichage du téléchargement Maven (logs plus propres)
- `BACKOFFICE_URL` → URL du site à tester (staging NoveoCare)

---

## 3. Job `build` (Build & Compile)

```yaml
build:
  name: Build & Compile
  runs-on: [self-hosted, linux]
  timeout-minutes: 10
```

### Étapes :

**a. `actions/checkout@v4`**
Clone le dépôt dans `$GITHUB_WORKSPACE` (répertoire de travail du runner).

**b. Cache Maven**
```yaml
uses: actions/cache@v4
with:
  path: ~/.m2/repository
  key: maven-${{ hashFiles('**/pom.xml') }}
```
Cache le dossier `.m2/repository` (dépendances Maven). La clé est un hash des `pom.xml` — si un `pom.xml` change, le cache est invalidé.

**c. `mvn compile`**
```yaml
run: mvn compile -q ${{ env.MAVEN_CLI_OPTS }}
```
Compile le code source Java (`src/main/java`) uniquement.

**d. `mvn test-compile`**
```yaml
run: mvn test-compile -q ${{ env.MAVEN_CLI_OPTS }}
```
Compile le code de test (`src/test/java`).

> **Pourquoi séparer compile et test-compile ?** Pour valider que la compilation passe avant de lancer les tests.

---

## 4. Job `feature-tests` (tests matriciels)

```yaml
feature-tests:
  name: ${{ matrix.suite }}
  needs: [build]
  runs-on: [self-hosted, linux]
  timeout-minutes: 30
  continue-on-error: true
  strategy:
    max-parallel: 1
    matrix:
      include:
        - suite: Login
          tags: '@login'
          runner: LoginTestRunner
          report-dir: login
          json-file: cucumber-login.json
        - suite: Companies
          tags: '@companies'
          runner: GenericTagRunner
        # ... 11 autres entrées
```

- **`needs: [build]`** → attend que le job `build` réussisse
- **`continue-on-error: true`** → même si une suite échoue, les autres continuent
- **`max-parallel: 1`** → les suites s'exécutent en séquence (1 à la fois) pour éviter les conflits sur le self-hosted runner
- **`matrix.include`** → définit 13 configurations de tests, chacune avec :
  - `suite` : nom d'affichage
  - `tags` : tag Cucumber à filtrer
  - `runner` : classe Java du runner Cucumber
  - `report-dir` : dossier de rapport
  - `json-file` : fichier JSON Cucumber

### Étapes :

**a. `actions/checkout@v4`** + **Cache Maven** (identiques au job `build`)

**b. Resolve effective tags**
```yaml
- name: Resolve effective tags
  id: tags
  run: |
    if [ "${{ github.event.inputs.suite }}" = "custom" ] && [ -n "${{ github.event.inputs.tags }}" ]; then
      echo "value=${{ github.event.inputs.tags }}" >> $GITHUB_OUTPUT
    elif [ "${{ github.event.inputs.suite }}" != "" ] && [ "${{ github.event.inputs.suite }}" != "all" ] && [ "${{ github.event.inputs.suite }}" != "custom" ]; then
      echo "value=${{ matrix.tags }}" >> $GITHUB_OUTPUT
    else
      echo "value=${{ matrix.tags }}" >> $GITHUB_OUTPUT
    fi
```
Détermine les tags à utiliser :
- Si `workflow_dispatch` avec `suite=custom` et des tags fournis → utilise les tags saisis
- Si `workflow_dispatch` avec une suite spécifique (ex: `login`) → utilise les tags de la matrice
- Par défaut (push/PR) → utilise les tags de la matrice

**c. Health check**
```yaml
- name: Health check — site accessible ?
  id: health
  run: |
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
      --connect-timeout 10 \
      ${{ env.BACKOFFICE_URL }})
    echo "HTTP $HTTP_CODE"
    if [ "$HTTP_CODE" = "403" ] || [ "$HTTP_CODE" = "502" ] || [ "$HTTP_CODE" = "503" ]; then
      echo "Site indisponible ($HTTP_CODE) — tests ignorés"
      echo "skip=true" >> $GITHUB_OUTPUT
    else
      echo "skip=false" >> $GITHUB_OUTPUT
    fi
```
Vérifie que le site de staging est accessible avant de lancer les tests. Si le site répond 403, 502 ou 503 → les tests sont ignorés (évite des faux négatifs).

**d. Run primary tests**
```yaml
- name: Run ${{ matrix.suite }} tests
  if: steps.health.outputs.skip != 'true'
  id: primary-run
  run: |
    TAGS="${{ steps.tags.outputs.value }}"
    REPORT_DIR="automation-framework/target/reports/${{ matrix.report-dir }}"
    mkdir -p "$REPORT_DIR"
    set +e
    xvfb-run --auto-servernum \
      mvn test ${{ env.MAVEN_CLI_OPTS }} \
        -Dtest=${{ matrix.runner }} \
        -Dcucumber.filter.tags="$TAGS" \
        -Dcucumber.plugin="pretty, html:$REPORT_DIR/report.html, json:automation-framework/target/${{ matrix.json-file }}, rerun:automation-framework/target/failed-${{ matrix.report-dir }}.txt" \
        -Dbackoffice.url=${{ env.BACKOFFICE_URL }} \
        -Dbackoffice.user.email=${{ secrets.BACKOFFICE_USER_EMAIL }} \
        -Dbackoffice.user.password=${{ secrets.BACKOFFICE_USER_PASSWORD }} \
        -Dself.healing.enabled=true \
        -Ddashboard.api.url=http://127.0.0.1:8080
    PRIMARY_EXIT=$?
    echo "primary_exit=$PRIMARY_EXIT" >> "$GITHUB_OUTPUT"
```

- `xvfb-run --auto-servernum` → lance un serveur X virtuel (nécessaire pour Chrome headless)
- `mvn test` avec `-Dtest=${{ matrix.runner }}` → exécute le runner Cucumber spécifique
- `-Dcucumber.filter.tags="$TAGS"` → filtre les scénarios par tag
- `-Dcucumber.plugin` configure 4 plugins Cucumber :
  - `pretty` → logs colorés dans la console
  - `html:` → rapport HTML lisible
  - `json:` → rapport JSON structuré
  - `rerun:` → fichier texte listant les scénarios échoués (pour la ré-exécution)
- `-Dself.healing.enabled=true` → active le self-healing
- `-Ddashboard.api.url` → endpoint du dashboard API pour envoyer les metrics
- `set +e` → empêche le script de s'arrêter si une commande échoue (on veut capturer le code d'erreur)
- `PRIMARY_EXIT=$?` → capture le code de sortie pour le transmettre aux étapes suivantes

**e. Rerun failed scenarios**
```yaml
- name: Rerun failed scenarios (${{ matrix.suite }})
  if: steps.health.outputs.skip != 'true'
  id: rerun
  run: |
    RERUN_FILE="automation-framework/target/failed-${{ matrix.report-dir }}.txt"
    RERUN_JSON="automation-framework/target/cucumber-rerun-${{ matrix.report-dir }}.json"
    RERUN_REPORT_DIR="automation-framework/target/reports/${{ matrix.report-dir }}-rerun"
    if [ -f "$RERUN_FILE" ] && [ -s "$RERUN_FILE" ]; then
      echo "Found $(wc -l < "$RERUN_FILE") failed scenario lines — re-running"
      mkdir -p "$RERUN_REPORT_DIR"
      set +e
      xvfb-run --auto-servernum \
        mvn test ${{ env.MAVEN_CLI_OPTS }} \
          -Dtest=${{ matrix.runner }} \
          -Dcucumber.features="@$RERUN_FILE" \
          -Dcucumber.plugin="pretty, html:$RERUN_REPORT_DIR/report.html, json:$RERUN_JSON" \
          -Dbackoffice.url=${{ env.BACKOFFICE_URL }} \
          -Dbackoffice.user.email=${{ secrets.BACKOFFICE_USER_EMAIL }} \
          -Dbackoffice.user.password=${{ secrets.BACKOFFICE_USER_PASSWORD }} \
          -Dself.healing.enabled=true \
          -Ddashboard.api.url=http://127.0.0.1:8080
      RERUN_EXIT=$?
      echo "rerun_exit=$RERUN_EXIT" >> "$GITHUB_OUTPUT"
    else
      echo "No failed scenarios to re-run"
      echo "rerun_exit=0" >> "$GITHUB_OUTPUT"
    fi
```

- Vérifie si le fichier `failed-*.txt` existe et n'est pas vide
- Si oui, relance uniquement les scénarios échoués via `-Dcucumber.features="@$RERUN_FILE"`
- Le rapport du rerun est stocké dans un dossier séparé (`*-rerun`)
- Cela permet de distinguer les **vrais échecs** (fail au premier run ET au rerun) vs **flaky** (fail au premier run, pass au rerun)

**f. Classify flaky scenarios**
```yaml
- name: Classify flaky scenarios (${{ matrix.suite }})
  if: steps.health.outputs.skip != 'true'
  run: |
    RERUN_JSON="automation-framework/target/cucumber-rerun-${{ matrix.report-dir }}.json"
    if [ -f "$RERUN_JSON" ]; then
      echo "Classifying flaky vs truly failed..."
      java -cp automation-framework/target/classes:automation-framework/target/test-classes \
        org.automation.dashboard.TestClassifier \
        "automation-framework/target/${{ matrix.json-file }}" \
        "$RERUN_JSON" \
        "${{ matrix.suite }}"
    else
      echo "No rerun JSON — skipping classification"
    fi
```

- Compare le JSON du premier run avec celui du rerun
- Classe chaque scénario comme : `passed`, `failed`, `flaky`, ou `skipped`
- Utilise `TestClassifier` (classe Java dans `org.automation.dashboard`)
- Les résultats sont envoyés au dashboard via l'API

**g. Skipped — maintenance**
```yaml
- name: Skipped — maintenance
  if: steps.health.outputs.skip == 'true'
  run: echo "::warning::Tests ${{ matrix.suite }} ignorés — site staging indisponible"
```
Affiche un avertissement dans l'interface GitHub Actions si le site est indisponible.

**h. Upload reports**
```yaml
- name: Upload ${{ matrix.suite }} reports
  if: always()
  uses: actions/upload-artifact@v4
  with:
    name: reports-${{ matrix.report-dir }}
    path: |
      automation-framework/target/reports/${{ matrix.report-dir }}/
      automation-framework/target/reports/${{ matrix.report-dir }}-rerun/
      automation-framework/target/${{ matrix.json-file }}
      automation-framework/target/cucumber-rerun-${{ matrix.report-dir }}.json
      automation-framework/target/failed-${{ matrix.report-dir }}.txt
    if-no-files-found: warn
```
- `if: always()` → même si les tests échouent, on upload les rapports
- Téléchargeable depuis l'interface GitHub Actions (artefacts)

**i. Upload healing debug**
```yaml
- name: Upload ${{ matrix.suite }} healing debug
  if: always()
  uses: actions/upload-artifact@v4
  with:
    name: healing-debug-${{ matrix.report-dir }}
    path: automation-framework/target/healing-debug/
    if-no-files-found: ignore
```
- Upload les payloads JSON de self-healing pour déboguer les échecs de healing

---

## 5. Job `deploy` (Dashboard)

```yaml
deploy:
  name: Deploy Dashboard
  needs: [build, feature-tests]
  runs-on: [self-hosted, linux]
  if: github.ref == 'refs/heads/main'
```

- **`needs: [build, feature-tests]`** → attend que tous les tests soient finis
- **`if: github.ref == 'refs/heads/main'`** → ne s'exécute que sur la branche `main`

### Étapes :

**a. `actions/checkout@v4`**

**b. Download all test artifacts**
```yaml
uses: actions/download-artifact@v4
with:
  pattern: reports-*
  merge-multiple: true
  path: automation-framework/target
```

- Télécharge tous les artefacts `reports-*` (toutes les suites)
- Les fusionne dans `automation-framework/target/` (les rapports JSON sont ensuite envoyés au dashboard)

**c. Deploy stack**
```yaml
- name: Deploy stack
  run: |
    docker compose -f dashboard-api/docker-compose.yml down -v
    docker compose --env-file dashboard-api/.env \
      -f dashboard-api/docker-compose.yml up -d --build
```

- Arrête les conteneurs existants (`down -v`)
- Reconstruit et relance toute la stack Docker (PostgreSQL + Dashboard API + Dashboard UI)
- `--build` force la reconstruction des images Docker

**d. Push all test results to Dashboard**
```yaml
- name: Push all test results to Dashboard
  run: |
    for json in automation-framework/target/cucumber-*.json; do
      if [ -f "$json" ]; then
        echo "Pushing $json ..."
        java -cp automation-framework/target/classes \
          org.automation.dashboard.DashboardPushMain "$json"
      fi
    done
  env:
    DASHBOARD_API_URL: http://localhost:8080
    DASHBOARD_API_KEY: ${{ secrets.DASHBOARD_API_KEY }}
```

- Parcourt tous les fichiers `cucumber-*.json` dans `target/`
- Pour chacun, exécute `DashboardPushMain` qui lit le JSON et envoie les données à l'API du dashboard
- La variable `DASHBOARD_API_KEY` est passée via les secrets GitHub (authentification)

---

## Ce qu'il manque (Continuous Deployment)

Ce pipeline fait de la **CI** (intégration continue) complète :
- Compilation ✓
- Tests unitaires ✓
- Tests fonctionnels (13 suites) ✓
- Rapports et classification flaky ✓

Mais le **CD** (déploiement continu) est incomplet :
- Le job `deploy` ne déploie que le **dashboard de monitoring**, pas l'application testée
- Il n'y a pas de déploiement du back-office NoveoCare vers un environnement de staging/production
- Pour un vrai CD, il faudrait ajouter :
  - Déploiement de l'application cible (ex: via Ansible, Kubernetes, rsync, etc.)
  - Tests d'intégration post-déploiement (smoke tests)
  - Promotion automatique staging → production
  - Rollback en cas d'échec
