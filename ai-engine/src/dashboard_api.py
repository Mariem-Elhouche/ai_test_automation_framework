"""
dashboard_api.py — FastAPI REST API for the AI Test Automation Dashboard
Place in: ai-engine/src/dashboard_api.py

Run locally:
    uvicorn dashboard_api:app --host 0.0.0.0 --port 8080 --reload

From Colab (via ngrok):
    !pip install fastapi uvicorn asyncpg databases psycopg2-binary pyngrok -q
    import threading, uvicorn
    threading.Thread(target=lambda: uvicorn.run(app, host="0.0.0.0", port=8080), daemon=True).start()
    from pyngrok import ngrok
    public_url = ngrok.connect(8080)
    print("Dashboard API:", public_url)
"""

import json
import os
from datetime import datetime, timedelta, timezone
from pathlib import Path
from typing import Any, List, Optional
from urllib.parse import quote_plus

import databases
import httpx
import sqlalchemy
from fastapi import Depends, FastAPI, Header, HTTPException, Security, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from jose import JWTError, jwt
from passlib.context import CryptContext
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

try:
    from dotenv import load_dotenv
except Exception:  # noqa: BLE001
    load_dotenv = None

if load_dotenv is not None:
    load_dotenv(Path(__file__).resolve().parents[1] / ".env")

# ── Configuration ─────────────────────────────────────────────────────────────
def _build_database_url() -> str:
    explicit_url = os.getenv("DATABASE_URL", "").strip()
    if explicit_url:
        return explicit_url

    db_host = os.getenv("DB_HOST", "localhost")
    db_port = os.getenv("DB_PORT", "5432")
    db_name = os.getenv("DB_NAME") or os.getenv("POSTGRES_DB", "ai_test_dashboard")
    db_user = os.getenv("DB_USER") or os.getenv("POSTGRES_USER", "postgres")
    db_password = os.getenv("DB_PASSWORD") or os.getenv("POSTGRES_PASSWORD", "password")

    encoded_user = quote_plus(db_user)
    encoded_password = quote_plus(db_password)
    return f"postgresql://{encoded_user}:{encoded_password}@{db_host}:{db_port}/{db_name}"


DATABASE_URL = _build_database_url()
API_KEY = os.getenv("DASHBOARD_API_KEY", "").strip()
AUTH_ENABLED = bool(API_KEY)
JWT_SECRET = os.getenv("DASHBOARD_JWT_SECRET", "").strip() or "change-this-jwt-secret"
JWT_ALGORITHM = "HS256"
JWT_EXPIRE_MINUTES = int(os.getenv("DASHBOARD_JWT_EXPIRE_MINUTES", "480"))
ROLE_PROJECT_MANAGER = "project_manager"
ROLE_QA_ENGINEER = "qa_engineer"
SELF_HEALING_METRICS_URL = os.getenv("SELF_HEALING_METRICS_URL", "").strip()
try:
    SELF_HEALING_METRICS_TIMEOUT_SECONDS = float(os.getenv("SELF_HEALING_METRICS_TIMEOUT_SECONDS", "5"))
except ValueError:
    SELF_HEALING_METRICS_TIMEOUT_SECONDS = 5.0

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
http_bearer = HTTPBearer(auto_error=False)


def _resolve_password_hash(
    explicit_hash: str,
    plain: str,
    fallback_plain: str,
) -> str:
    if explicit_hash.strip():
        return explicit_hash.strip()
    candidate = plain.strip() or fallback_plain
    return pwd_context.hash(candidate)


def _build_actor_accounts() -> dict[str, dict[str, str]]:
    pm_email = os.getenv("DASHBOARD_PM_EMAIL", "chef.projet@dashboard.local").strip().lower()
    pm_hash = _resolve_password_hash(
        os.getenv("DASHBOARD_PM_PASSWORD_HASH", ""),
        os.getenv("DASHBOARD_PM_PASSWORD", ""),
        "ChangeMePM123!",
    )
    pm_name = os.getenv("DASHBOARD_PM_DISPLAY_NAME", "Chef de projet").strip() or "Chef de projet"

    qa_email = os.getenv("DASHBOARD_QA_EMAIL", "qa.engineer@dashboard.local").strip().lower()
    qa_hash = _resolve_password_hash(
        os.getenv("DASHBOARD_QA_PASSWORD_HASH", ""),
        os.getenv("DASHBOARD_QA_PASSWORD", ""),
        "ChangeMeQA123!",
    )
    qa_name = os.getenv("DASHBOARD_QA_DISPLAY_NAME", "QA Engineer").strip() or "QA Engineer"

    return {
        pm_email: {"email": pm_email, "name": pm_name, "role": ROLE_PROJECT_MANAGER, "password_hash": pm_hash},
        qa_email: {"email": qa_email, "name": qa_name, "role": ROLE_QA_ENGINEER, "password_hash": qa_hash},
    }


ACCOUNTS = _build_actor_accounts()


def _verify_password(plain_password: str, password_hash: str) -> bool:
    try:
        return pwd_context.verify(plain_password, password_hash)
    except Exception:  # noqa: BLE001
        return False


def _create_access_token(account: dict[str, str]) -> str:
    now = datetime.now(timezone.utc)
    expire_at = now + timedelta(minutes=JWT_EXPIRE_MINUTES)
    payload = {
        "sub": account["email"],
        "name": account["name"],
        "role": account["role"],
        "iat": int(now.timestamp()),
        "exp": int(expire_at.timestamp()),
    }
    return jwt.encode(payload, JWT_SECRET, algorithm=JWT_ALGORITHM)


def _decode_access_token(token: str) -> dict[str, Any]:
    try:
        payload = jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGORITHM])
    except JWTError as exc:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid or expired token") from exc

    subject = payload.get("sub")
    role = payload.get("role")
    if not isinstance(subject, str) or not isinstance(role, str):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token payload")
    return payload

database = databases.Database(DATABASE_URL)
metadata = sqlalchemy.MetaData()
engine = sqlalchemy.create_engine(DATABASE_URL)

# ── SQLAlchemy table definitions ───────────────────────────────────────────────
healing_events = sqlalchemy.Table(
    "healing_events", metadata,
    sqlalchemy.Column("id", sqlalchemy.Integer, primary_key=True),
    sqlalchemy.Column("created_at", sqlalchemy.DateTime(timezone=True)),
    sqlalchemy.Column("scenario_name", sqlalchemy.String),
    sqlalchemy.Column("old_locator_type", sqlalchemy.String),
    sqlalchemy.Column("old_locator_val", sqlalchemy.Text),
    sqlalchemy.Column("success", sqlalchemy.Boolean),
    sqlalchemy.Column("score", sqlalchemy.Numeric),
    sqlalchemy.Column("structural_score", sqlalchemy.Numeric),
    sqlalchemy.Column("semantic_score", sqlalchemy.Numeric),
    sqlalchemy.Column("new_locator_type", sqlalchemy.String),
    sqlalchemy.Column("new_locator_val", sqlalchemy.Text),
    sqlalchemy.Column("healing_time_ms", sqlalchemy.Integer),
    sqlalchemy.Column("baseline_hit", sqlalchemy.Boolean),
    sqlalchemy.Column("elements_extracted", sqlalchemy.Integer),
    sqlalchemy.Column("after_struct_filter", sqlalchemy.Integer),
    sqlalchemy.Column("after_spatial_filter", sqlalchemy.Integer),
    sqlalchemy.Column("sent_to_nlp", sqlalchemy.Integer),
    sqlalchemy.Column("error_message", sqlalchemy.Text),
    sqlalchemy.Column("run_id", sqlalchemy.String(128)),
)

metrics_snapshots = sqlalchemy.Table(
    "metrics_snapshots", metadata,
    sqlalchemy.Column("id", sqlalchemy.Integer, primary_key=True),
    sqlalchemy.Column("captured_at", sqlalchemy.DateTime(timezone=True)),
    sqlalchemy.Column("total_healing_requests", sqlalchemy.Integer),
    sqlalchemy.Column("successful_healings", sqlalchemy.Integer),
    sqlalchemy.Column("failed_healings", sqlalchemy.Integer),
    sqlalchemy.Column("baseline_hits", sqlalchemy.Integer),
    sqlalchemy.Column("total_elements_extracted", sqlalchemy.Integer),
    sqlalchemy.Column("total_after_struct", sqlalchemy.Integer),
    sqlalchemy.Column("total_after_spatial", sqlalchemy.Integer),
    sqlalchemy.Column("total_sent_to_nlp", sqlalchemy.Integer),
    sqlalchemy.Column("total_healing_time_ms", sqlalchemy.BigInteger),
    sqlalchemy.Column("healing_rate", sqlalchemy.Numeric),
    sqlalchemy.Column("baseline_hit_rate", sqlalchemy.Numeric),
    sqlalchemy.Column("avg_healing_time_ms", sqlalchemy.Numeric),
    sqlalchemy.Column("avg_final_score", sqlalchemy.Numeric),
    sqlalchemy.Column("avg_structural_score", sqlalchemy.Numeric),
    sqlalchemy.Column("avg_semantic_score", sqlalchemy.Numeric),
    sqlalchemy.Column("nlp_filter_efficiency", sqlalchemy.Numeric),
    sqlalchemy.Column("run_id", sqlalchemy.String(128)),
)

cucumber_runs = sqlalchemy.Table(
    "cucumber_runs", metadata,
    sqlalchemy.Column("id", sqlalchemy.Integer, primary_key=True),
    sqlalchemy.Column("run_at", sqlalchemy.DateTime(timezone=True)),
    sqlalchemy.Column("feature_name", sqlalchemy.String),
    sqlalchemy.Column("scenario", sqlalchemy.String),
    sqlalchemy.Column("status", sqlalchemy.String),
    sqlalchemy.Column("duration_ns", sqlalchemy.BigInteger),
    sqlalchemy.Column("tags", sqlalchemy.Text),
    sqlalchemy.Column("run_id", sqlalchemy.String(128)),
)

# ── Pydantic schemas ───────────────────────────────────────────────────────────

class HealingEventIn(BaseModel):
    scenario_name: Optional[str] = None
    old_locator_type: Optional[str] = None
    old_locator_val: Optional[str] = None
    success: bool
    score: Optional[float] = None
    structural_score: Optional[float] = None
    semantic_score: Optional[float] = None
    new_locator_type: Optional[str] = None
    new_locator_val: Optional[str] = None
    healing_time_ms: Optional[int] = None
    baseline_hit: bool = False
    elements_extracted: Optional[int] = None
    after_struct_filter: Optional[int] = None
    after_spatial_filter: Optional[int] = None
    sent_to_nlp: Optional[int] = None
    error_message: Optional[str] = None
    run_id: Optional[str] = None

class MetricsSnapshotIn(BaseModel):
    total_healing_requests: int = 0
    successful_healings: int = 0
    failed_healings: int = 0
    baseline_hits: int = 0
    total_elements_extracted: int = 0
    total_after_struct: int = 0
    total_after_spatial: int = 0
    total_sent_to_nlp: int = 0
    total_healing_time_ms: int = 0
    healing_rate: Optional[float] = None
    baseline_hit_rate: Optional[float] = None
    avg_healing_time_ms: Optional[float] = None
    avg_final_score: Optional[float] = None
    avg_structural_score: Optional[float] = None
    avg_semantic_score: Optional[float] = None
    nlp_filter_efficiency: Optional[float] = None
    run_id: Optional[str] = None

class CucumberScenario(BaseModel):
    feature_name: str
    scenario: str
    status: str  # passed / failed / skipped
    duration_ns: Optional[int] = None
    tags: Optional[str] = None
    run_id: Optional[str] = None

class CucumberRunIn(BaseModel):
    scenarios: List[CucumberScenario]
    run_id: Optional[str] = None

class LoginRequest(BaseModel):
    email: str
    password: str

# ── App ────────────────────────────────────────────────────────────────────────
app = FastAPI(title="AI Test Automation Dashboard API", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],   # Restrict in production
    allow_methods=["*"],
    allow_headers=["*"],
)

def _raise_unauthorized(detail: str = "Unauthorized") -> None:
    raise HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail=detail,
        headers={"WWW-Authenticate": "Bearer"},
    )


def get_current_actor(
    credentials: Optional[HTTPAuthorizationCredentials] = Security(http_bearer),
) -> dict[str, Any]:
    if credentials is None or credentials.scheme.lower() != "bearer":
        _raise_unauthorized("Missing bearer token")
    return _decode_access_token(credentials.credentials)


def require_role(allowed_roles: set[str]):
    def _checker(actor: dict[str, Any] = Depends(get_current_actor)) -> dict[str, Any]:
        role = str(actor.get("role", ""))
        if role not in allowed_roles:
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Forbidden")
        return actor

    return _checker


require_dashboard_reader = require_role({ROLE_PROJECT_MANAGER, ROLE_QA_ENGINEER})


def require_ingest_auth(
    x_api_key: Optional[str] = Header(default=None, alias="X-API-Key"),
    credentials: Optional[HTTPAuthorizationCredentials] = Security(http_bearer),
) -> dict[str, Any]:
    if AUTH_ENABLED and x_api_key == API_KEY:
        return {"auth_type": "api_key"}

    if credentials is not None and credentials.scheme.lower() == "bearer":
        actor = _decode_access_token(credentials.credentials)
        if actor.get("role") in {ROLE_PROJECT_MANAGER, ROLE_QA_ENGINEER}:
            return actor

    _raise_unauthorized("Invalid credentials")

def _norm_run_id(value: Optional[str]) -> Optional[str]:
    if value is None:
        return None
    run_id = value.strip()
    return run_id if run_id else None


def _apply_run_scope(query: sqlalchemy.sql.Select, run_column: Any, scope_mode: str, run_id: Optional[str]):
    if scope_mode == "run" and run_id:
        return query.where(run_column == run_id)
    if scope_mode == "null":
        return query.where(run_column.is_(None))
    return query


def _build_healing_scopes(selected_run_id: Optional[str]) -> list[tuple[str, Optional[str], str]]:
    scopes: list[tuple[str, Optional[str], str]] = []
    if selected_run_id:
        scopes.append(("run", selected_run_id, "matched_run"))
        scopes.append(("null", None, "uncorrelated"))
    scopes.append(("any", None, "latest_available"))
    return scopes


def _normalize_external_metrics(raw: dict[str, Any]) -> dict[str, Any]:
    return {
        "captured_at": datetime.now(timezone.utc).isoformat(),
        "total_healing_requests": raw.get("total_healing_requests", 0),
        "successful_healings": raw.get("successful_healings", 0),
        "failed_healings": raw.get("failed_healings", 0),
        "baseline_hits": raw.get("baseline_hits", 0),
        "total_elements_extracted": raw.get("total_elements_extracted", 0),
        "total_after_struct": raw.get("total_after_struct", raw.get("total_after_struct_filter", 0)),
        "total_after_spatial": raw.get("total_after_spatial", raw.get("total_after_spatial_filter", 0)),
        "total_sent_to_nlp": raw.get("total_sent_to_nlp", 0),
        "total_healing_time_ms": raw.get("total_healing_time_ms", 0),
        "healing_rate": raw.get("healing_rate"),
        "baseline_hit_rate": raw.get("baseline_hit_rate"),
        "avg_healing_time_ms": raw.get("avg_healing_time_ms"),
        "avg_final_score": raw.get("avg_final_score"),
        "avg_structural_score": raw.get("avg_structural_score"),
        "avg_semantic_score": raw.get("avg_semantic_score"),
        "nlp_filter_efficiency": raw.get("nlp_filter_efficiency"),
        "run_id": _norm_run_id(raw.get("run_id")),
    }


async def _fetch_snapshot_for_scope(scope_mode: str, run_id: Optional[str]) -> Optional[dict[str, Any]]:
    query = metrics_snapshots.select()
    query = _apply_run_scope(query, metrics_snapshots.c.run_id, scope_mode, run_id)
    query = query.order_by(metrics_snapshots.c.captured_at.desc()).limit(1)
    row = await database.fetch_one(query)
    return dict(row) if row else None


async def _fetch_history_for_scope(scope_mode: str, run_id: Optional[str], limit: int = 50) -> list[dict[str, Any]]:
    query = metrics_snapshots.select()
    query = _apply_run_scope(query, metrics_snapshots.c.run_id, scope_mode, run_id)
    query = query.order_by(metrics_snapshots.c.captured_at.asc()).limit(limit)
    rows = await database.fetch_all(query)
    return [dict(r) for r in rows]


async def _fetch_recent_events_for_scope(scope_mode: str, run_id: Optional[str], limit: int = 10) -> list[dict[str, Any]]:
    query = healing_events.select()
    query = _apply_run_scope(query, healing_events.c.run_id, scope_mode, run_id)
    query = query.order_by(healing_events.c.created_at.desc()).limit(limit)
    rows = await database.fetch_all(query)
    return [dict(r) for r in rows]


async def _derive_metrics_from_events(scope_mode: str, run_id: Optional[str]) -> Optional[dict[str, Any]]:
    query = healing_events.select()
    query = _apply_run_scope(query, healing_events.c.run_id, scope_mode, run_id)
    rows = await database.fetch_all(query)
    if not rows:
        return None

    items = [dict(row) for row in rows]
    total = len(items)
    successful = sum(1 for row in items if row.get("success") is True)
    failed = total - successful
    baseline_hits = sum(1 for row in items if row.get("baseline_hit") is True)
    total_elements = sum(int(row.get("elements_extracted") or 0) for row in items)
    total_after_struct = sum(int(row.get("after_struct_filter") or 0) for row in items)
    total_after_spatial = sum(int(row.get("after_spatial_filter") or 0) for row in items)
    total_sent_to_nlp = sum(int(row.get("sent_to_nlp") or 0) for row in items)
    total_healing_time_ms = sum(int(row.get("healing_time_ms") or 0) for row in items)

    score_values = [float(row["score"]) for row in items if row.get("score") is not None]
    structural_values = [float(row["structural_score"]) for row in items if row.get("structural_score") is not None]
    semantic_values = [float(row["semantic_score"]) for row in items if row.get("semantic_score") is not None]
    captured_at = max((row.get("created_at") for row in items if row.get("created_at") is not None), default=None)

    return {
        "captured_at": captured_at,
        "total_healing_requests": total,
        "successful_healings": successful,
        "failed_healings": failed,
        "baseline_hits": baseline_hits,
        "total_elements_extracted": total_elements,
        "total_after_struct": total_after_struct,
        "total_after_spatial": total_after_spatial,
        "total_sent_to_nlp": total_sent_to_nlp,
        "total_healing_time_ms": total_healing_time_ms,
        "healing_rate": round(successful / total, 4) if total else 0.0,
        "baseline_hit_rate": round(baseline_hits / total, 4) if total else 0.0,
        "avg_healing_time_ms": round(total_healing_time_ms / total, 2) if total else 0.0,
        "avg_final_score": round(sum(score_values) / len(score_values), 4) if score_values else None,
        "avg_structural_score": round(sum(structural_values) / len(structural_values), 4) if structural_values else None,
        "avg_semantic_score": round(sum(semantic_values) / len(semantic_values), 4) if semantic_values else None,
        "nlp_filter_efficiency": round(1.0 - (total_sent_to_nlp / total_elements), 4) if total_elements else 0.0,
        "run_id": run_id if scope_mode == "run" else None,
    }


async def _fetch_external_healing_metrics() -> Optional[dict[str, Any]]:
    if not SELF_HEALING_METRICS_URL:
        return None

    try:
        async with httpx.AsyncClient(timeout=SELF_HEALING_METRICS_TIMEOUT_SECONDS) as client:
            response = await client.get(
                SELF_HEALING_METRICS_URL,
                headers={"ngrok-skip-browser-warning": "true"},
            )
            response.raise_for_status()
            payload = response.json()
    except Exception:  # noqa: BLE001
        return None

    if not isinstance(payload, dict):
        return None
    return _normalize_external_metrics(payload)


async def _resolve_healing_payload(selected_run_id: Optional[str]) -> tuple[Optional[dict[str, Any]], list[dict[str, Any]], list[dict[str, Any]], str]:
    for scope_mode, scope_run_id, scope_label in _build_healing_scopes(selected_run_id):
        metrics_row = await _fetch_snapshot_for_scope(scope_mode, scope_run_id)
        if metrics_row:
            history_rows = await _fetch_history_for_scope(scope_mode, scope_run_id)
            events_rows = await _fetch_recent_events_for_scope(scope_mode, scope_run_id)
            return metrics_row, history_rows, events_rows, f"metrics_snapshots:{scope_label}"

        derived_metrics = await _derive_metrics_from_events(scope_mode, scope_run_id)
        if derived_metrics:
            events_rows = await _fetch_recent_events_for_scope(scope_mode, scope_run_id)
            return derived_metrics, [derived_metrics], events_rows, f"healing_events:{scope_label}"

    external_metrics = await _fetch_external_healing_metrics()
    if external_metrics:
        return external_metrics, [external_metrics], [], "external_metrics_url"

    return None, [], [], "unavailable"

def _ensure_schema_compatibility() -> None:
    statements = [
        "ALTER TABLE healing_events ADD COLUMN IF NOT EXISTS run_id VARCHAR(128)",
        "ALTER TABLE metrics_snapshots ADD COLUMN IF NOT EXISTS run_id VARCHAR(128)",
        "ALTER TABLE cucumber_runs ADD COLUMN IF NOT EXISTS run_id VARCHAR(128)",
        "CREATE INDEX IF NOT EXISTS idx_healing_events_run_id ON healing_events (run_id)",
        "CREATE INDEX IF NOT EXISTS idx_metrics_snapshots_run_id ON metrics_snapshots (run_id)",
        "CREATE INDEX IF NOT EXISTS idx_cucumber_runs_run_id ON cucumber_runs (run_id)",
    ]
    with engine.begin() as conn:
        for sql in statements:
            conn.execute(sqlalchemy.text(sql))

@app.on_event("startup")
async def startup():
    metadata.create_all(engine)
    _ensure_schema_compatibility()
    await database.connect()

@app.on_event("shutdown")
async def shutdown():
    await database.disconnect()

# ── Health ─────────────────────────────────────────────────────────────────────
@app.get("/health")
async def health():
    return {"status": "ok", "timestamp": datetime.now(timezone.utc).isoformat()}


@app.post("/auth/login")
async def login(payload: LoginRequest):
    email = payload.email.strip().lower()
    account = ACCOUNTS.get(email)
    if not account or not _verify_password(payload.password, account["password_hash"]):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid email or password")

    token = _create_access_token(account)
    return {
        "access_token": token,
        "token_type": "bearer",
        "expires_in": JWT_EXPIRE_MINUTES * 60,
        "role": account["role"],
        "display_name": account["name"],
        "email": account["email"],
    }


@app.get("/auth/me")
async def me(actor: dict[str, Any] = Depends(require_dashboard_reader)):
    return {
        "email": actor.get("sub"),
        "display_name": actor.get("name"),
        "role": actor.get("role"),
    }

# ── Healing Events ─────────────────────────────────────────────────────────────
@app.post("/api/healing-events", status_code=201)
async def create_healing_event(event: HealingEventIn, _auth: dict[str, Any] = Depends(require_ingest_auth)):
    event_data = event.dict()
    event_data["run_id"] = _norm_run_id(event_data.get("run_id"))
    query = healing_events.insert().values(
        created_at=datetime.now(timezone.utc),
        **event_data
    )
    event_id = await database.execute(query)
    return {"id": event_id}

@app.get("/api/healing-events")
async def list_healing_events(
    limit: int = 50,
    offset: int = 0,
    run_id: Optional[str] = None,
    _auth: dict[str, Any] = Depends(require_dashboard_reader),
):
    query = healing_events.select()
    normalized_run_id = _norm_run_id(run_id)
    if normalized_run_id:
        query = query.where(healing_events.c.run_id == normalized_run_id)
    query = query.order_by(healing_events.c.created_at.desc()).limit(limit).offset(offset)
    rows = await database.fetch_all(query)
    return [dict(r) for r in rows]

# ── Metrics Snapshots ──────────────────────────────────────────────────────────
@app.post("/api/metrics", status_code=201)
async def push_metrics(snapshot: MetricsSnapshotIn, _auth: dict[str, Any] = Depends(require_ingest_auth)):
    snapshot_data = snapshot.dict()
    snapshot_data["run_id"] = _norm_run_id(snapshot_data.get("run_id"))
    query = metrics_snapshots.insert().values(
        captured_at=datetime.now(timezone.utc),
        **snapshot_data
    )
    snap_id = await database.execute(query)
    return {"id": snap_id}

@app.get("/api/metrics/latest")
async def get_latest_metrics(run_id: Optional[str] = None, _auth: dict[str, Any] = Depends(require_dashboard_reader)):
    query = metrics_snapshots.select()
    normalized_run_id = _norm_run_id(run_id)
    if normalized_run_id:
        query = query.where(metrics_snapshots.c.run_id == normalized_run_id)
    query = query.order_by(metrics_snapshots.c.captured_at.desc()).limit(1)
    row = await database.fetch_one(query)
    if not row:
        raise HTTPException(status_code=404, detail="No metrics yet")
    return dict(row)

@app.get("/api/metrics/history")
async def get_metrics_history(
    limit: int = 100,
    run_id: Optional[str] = None,
    _auth: dict[str, Any] = Depends(require_dashboard_reader),
):
    query = metrics_snapshots.select()
    normalized_run_id = _norm_run_id(run_id)
    if normalized_run_id:
        query = query.where(metrics_snapshots.c.run_id == normalized_run_id)
    query = query.order_by(metrics_snapshots.c.captured_at.asc()).limit(limit)
    rows = await database.fetch_all(query)
    return [dict(r) for r in rows]

# ── Cucumber Runs ──────────────────────────────────────────────────────────────
@app.post("/api/cucumber-runs", status_code=201)
async def push_cucumber_run(run: CucumberRunIn, _auth: dict[str, Any] = Depends(require_ingest_auth)):
    now = datetime.now(timezone.utc)
    parent_run_id = _norm_run_id(run.run_id)
    inserted = 0
    for sc in run.scenarios:
        scenario_run_id = _norm_run_id(sc.run_id) or parent_run_id
        query = cucumber_runs.insert().values(
            run_at=now,
            feature_name=sc.feature_name,
            scenario=sc.scenario,
            status=sc.status,
            duration_ns=sc.duration_ns,
            tags=sc.tags,
            run_id=scenario_run_id,
        )
        await database.execute(query)
        inserted += 1
    return {"inserted": inserted, "run_id": parent_run_id}

@app.get("/api/cucumber-runs/summary")
async def cucumber_summary(run_id: Optional[str] = None, _auth: dict[str, Any] = Depends(require_dashboard_reader)):
    """Aggregated pass/fail/skip counts for the latest run batch."""
    selected_run_id = _norm_run_id(run_id)
    latest_run_at = None

    if selected_run_id:
        run_info_q = sqlalchemy.select(sqlalchemy.func.max(cucumber_runs.c.run_at)).where(
            cucumber_runs.c.run_id == selected_run_id
        )
        latest_run_at = await database.fetch_val(run_info_q)
        if not latest_run_at:
            return {"passed": 0, "failed": 0, "skipped": 0, "total": 0, "run_id": selected_run_id, "run_at": None}
    else:
        latest_run_id_q = (
            sqlalchemy.select(cucumber_runs.c.run_id)
            .where(cucumber_runs.c.run_id.is_not(None))
            .order_by(cucumber_runs.c.run_at.desc())
            .limit(1)
        )
        latest_non_null_run_id = await database.fetch_val(latest_run_id_q)
        if latest_non_null_run_id:
            selected_run_id = latest_non_null_run_id
            run_info_q = sqlalchemy.select(sqlalchemy.func.max(cucumber_runs.c.run_at)).where(
                cucumber_runs.c.run_id == selected_run_id
            )
            latest_run_at = await database.fetch_val(run_info_q)
        else:
            latest_run_q = sqlalchemy.select(sqlalchemy.func.max(cucumber_runs.c.run_at))
            latest_run_at = await database.fetch_val(latest_run_q)

    if not latest_run_at:
        return {"passed": 0, "failed": 0, "skipped": 0, "total": 0}

    if selected_run_id:
        query = (
            sqlalchemy.select(cucumber_runs.c.status, sqlalchemy.func.count().label("cnt"))
            .where(cucumber_runs.c.run_id == selected_run_id)
            .group_by(cucumber_runs.c.status)
        )
        rows = await database.fetch_all(query)
    else:
        query = sqlalchemy.text("""
                                SELECT status, COUNT(*) as cnt
                                FROM cucumber_runs
                                WHERE run_at >= (SELECT MAX(run_at) - INTERVAL '5 seconds' FROM cucumber_runs)
                                GROUP BY status
                                """)
        rows = await database.fetch_all(query)

    counts = {r["status"]: r["cnt"] for r in rows}
    total = sum(counts.values())
    return {
        "passed": counts.get("passed", 0),
        "failed": counts.get("failed", 0),
        "skipped": counts.get("skipped", 0),
        "total": total,
        "run_id": selected_run_id,
        "run_at": latest_run_at.isoformat() if latest_run_at else None,
    }

@app.get("/api/cucumber-runs/recent")
async def recent_cucumber_runs(
    limit: int = 20,
    run_id: Optional[str] = None,
    _auth: dict[str, Any] = Depends(require_dashboard_reader),
):
    query = cucumber_runs.select()
    normalized_run_id = _norm_run_id(run_id)
    if normalized_run_id:
        query = query.where(cucumber_runs.c.run_id == normalized_run_id)
    query = query.order_by(cucumber_runs.c.run_at.desc()).limit(limit)
    rows = await database.fetch_all(query)
    return [dict(r) for r in rows]

# ── Dashboard summary (single call for the UI) ─────────────────────────────────
@app.get("/api/dashboard")
async def dashboard_summary(run_id: Optional[str] = None, _auth: dict[str, Any] = Depends(require_dashboard_reader)):
    """All data needed by the frontend in one call."""
    selected_run_id = _norm_run_id(run_id)
    if not selected_run_id:
        latest_run_id_q = (
            sqlalchemy.select(cucumber_runs.c.run_id)
            .where(cucumber_runs.c.run_id.is_not(None))
            .order_by(cucumber_runs.c.run_at.desc())
            .limit(1)
        )
        selected_run_id = await database.fetch_val(latest_run_id_q)

    metrics_payload, history_rows, events_rows, healing_source = await _resolve_healing_payload(selected_run_id)

    # Cucumber summary
    if selected_run_id:
        cucumber_q = (
            sqlalchemy.select(cucumber_runs.c.status, sqlalchemy.func.count().label("cnt"))
            .where(cucumber_runs.c.run_id == selected_run_id)
            .group_by(cucumber_runs.c.status)
        )
        cucumber_rows = await database.fetch_all(cucumber_q)
    else:
        cucumber_q = sqlalchemy.text("""
                                     SELECT status, COUNT(*) as cnt
                                     FROM cucumber_runs
                                     WHERE run_at >= (SELECT MAX(run_at) - INTERVAL '5 seconds' FROM cucumber_runs)
                                     GROUP BY status
                                     """)
        cucumber_rows = await database.fetch_all(cucumber_q)
    cuc_counts = {r["status"]: r["cnt"] for r in cucumber_rows}

    return {
        "run_id": selected_run_id,
        "metrics": metrics_payload,
        "metrics_history": history_rows,
        "recent_events": events_rows,
        "healing_source": healing_source,
        "cucumber": {
            "passed": cuc_counts.get("passed", 0),
            "failed": cuc_counts.get("failed", 0),
            "skipped": cuc_counts.get("skipped", 0),
            "total": sum(cuc_counts.values()),
        },
    }
