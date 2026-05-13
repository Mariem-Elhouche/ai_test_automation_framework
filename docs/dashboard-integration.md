# Intégration Dashboard (Self-Healing + Framework)

## 1) Exposer les métriques côté Colab

Ton moteur Python calcule déjà `PerformanceMetrics`.  
Il faut exposer un endpoint HTTP qui retourne `healing_engine.metrics.to_dict()`.

Exemple Flask:

```python
from flask import Flask, jsonify

app = Flask(__name__)

@app.get("/metrics")
def metrics():
    return jsonify(healing_engine.metrics.to_dict())
```

## 2) Exécuter les tests du framework

Les métriques framework sont lues depuis:
- `automation-framework/target/cucumber*.json`
- `automation-framework/target/reports/**/*.html`
- `automation-framework/target/healing-debug/*-request.json`

## 3) Générer le dashboard

Option recommandée (PowerShell, sans dépendance):

```powershell
powershell -ExecutionPolicy Bypass -File automation-framework/tools/generate-dashboard.ps1
```

Avec endpoint explicite:

```powershell
powershell -ExecutionPolicy Bypass -File automation-framework/tools/generate-dashboard.ps1 -ColabMetricsUrl "https://<ngrok>/metrics"
```

Option Python (si Python est installé):

```powershell
python automation-framework/tools/generate_dashboard.py --colab-metrics-url "https://<ngrok>/metrics"
```

## 4) Ouvrir le résultat

Fichier généré:

`automation-framework/target/dashboard/index.html`

Le dashboard inclut:
- Scénarios/steps passés/failed/skipped
- Pass rate, durée moyenne des steps
- Nombre de payloads self-healing capturés côté framework
- Métriques Colab (`healing_rate`, `avg_final_score`, `nlp_filter_efficiency`, etc.)
