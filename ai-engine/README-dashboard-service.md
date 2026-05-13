# Dashboard Service (API + PostgreSQL)

## Prerequisites

- Docker Desktop running
- Port `5432` free (PostgreSQL)
- Port `8080` free (Dashboard API)

## Configuration

Secrets are centralized in:

`ai-engine/.env`

Example:

```env
POSTGRES_DB=ai_test_dashboard
POSTGRES_USER=postgres
POSTGRES_PASSWORD=mypush@02
DASHBOARD_API_KEY=change-me
DASHBOARD_JWT_SECRET=rqiWfqpMcr6tlw66ud3TQvBEktLmaLkqDVY8csCLtRmAaMRLYgRlgTdsroYmQZOqB
DASHBOARD_JWT_EXPIRE_MINUTES=480

DASHBOARD_PM_EMAIL=chef.projet@dashboard.local
DASHBOARD_PM_PASSWORD=chef_projet
DASHBOARD_PM_PASSWORD_HASH=
DASHBOARD_PM_DISPLAY_NAME=Chef de projet

DASHBOARD_QA_EMAIL=qa.engineer@dashboard.local
DASHBOARD_QA_PASSWORD=qa_engineer
DASHBOARD_QA_PASSWORD_HASH=
DASHBOARD_QA_DISPLAY_NAME=QA Engineer

# Optional live fallback to the Colab notebook metrics endpoint
SELF_HEALING_METRICS_URL=https://<ngrok>/metrics
SELF_HEALING_METRICS_TIMEOUT_SECONDS=5
```

Template file:

`ai-engine/.env.example`

## Start

From repo root:

```powershell
docker compose --env-file ai-engine/.env -f ai-engine/docker-compose.yml up -d --build
```

This starts:

- PostgreSQL: `127.0.0.1:5432`
- Dashboard API: `http://127.0.0.1:8080`
- Dashboard UI (React in Docker): `http://127.0.0.1:5173`

## Verification

Health check:

```powershell
curl http://localhost:8080/health
```

Expected response: `{"status":"ok", ...}`

## Run API locally (without Docker API service)

With `ai-engine/.env` in place, you can run:

```powershell
cd ai-engine
python -m uvicorn src.dashboard_api:app --host 127.0.0.1 --port 8080 --reload
```

## Healing metrics fallback

The dashboard first reads healing KPIs from PostgreSQL snapshots/events. If those rows are missing or not correlated with the latest Cucumber `run_id`, it can also fallback to the live Colab metrics endpoint:

- `SELF_HEALING_METRICS_URL=https://<ngrok>/metrics`

This is useful when the notebook is running in Colab and the dashboard must still display KPI values even if healing snapshots were pushed without `run_id`, or were not persisted yet.

## Authentication (API key)

`X-API-Key` is used for machine-to-machine ingestion (framework -> API):

```http
X-API-Key: change-me
```

Used by:

- `POST /api/cucumber-runs`
- `POST /api/metrics`
- `POST /api/healing-events`

## Authentication (users + roles)

UI and read APIs use JWT bearer auth via:

- `POST /auth/login`
- `Authorization: Bearer <token>`

Roles:

- `project_manager` (Chef de projet)
- `qa_engineer` (QA Engineer)

Login example:

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/auth/login" `
  -ContentType "application/json" `
  -Body '{"email":"chef.projet@dashboard.local","password":"ChangeMePM123!"}'
```

## Java framework integration

`DashboardReporter` pushes `target/cucumber.json` to:

- `DASHBOARD_API_URL` (default: `http://localhost:8080`)
- API key via `DASHBOARD_API_KEY`
- `run_id` correlation:
  - uses `DASHBOARD_RUN_ID` when defined
  - otherwise generates a unique value

Before test execution:

```powershell
$env:DASHBOARD_API_URL="http://localhost:8080"
$env:DASHBOARD_API_KEY="change-me"
$env:DASHBOARD_RUN_ID="run-2026-05-11-regression"
```

## Filter by run_id

```powershell
$token = "<JWT_TOKEN>"
curl -H "Authorization: Bearer $token" "http://localhost:8080/api/cucumber-runs/summary?run_id=run-2026-05-11-regression"
curl -H "Authorization: Bearer $token" "http://localhost:8080/api/dashboard?run_id=run-2026-05-11-regression"
```
