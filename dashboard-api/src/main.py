from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Optional

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles

from src.core.config import settings
from src.core.database import (
    app_users,
    cucumber_runs,
    database,
    engine,
    healing_events,
    metadata,
    metrics_snapshots,
    test_runs,
)
from src.core.dependencies import (
    create_dependency_getter,
    make_require_ingest_auth,
    make_role_checker,
)
from src.controllers.auth_controller import create_auth_router
from src.controllers.dashboard_controller import create_dashboard_router
from src.controllers.user_controller import create_user_router
from src.models.user_model import UserModel
from src.repositories.cucumber_repository import CucumberRunRepository
from src.repositories.healing_repository import HealingEventRepository
from src.repositories.metrics_repository import MetricsSnapshotRepository
from src.repositories.test_run_repository import TestRunRepository
from src.repositories.user_repository import UserRepository
from src.schemas import User
from src.services.auth_service import AuthService
from src.services.dashboard_service import DashboardService, TestRunManager
from src.services.user_service import UserService

try:
    from dotenv import load_dotenv
except Exception:
    load_dotenv = None

if load_dotenv is not None:
    load_dotenv(Path(__file__).resolve().parents[1] / ".env")

# ── Core services ──────────────────────────────────────────────────────────────
auth_service = AuthService(
    settings.DASHBOARD_JWT_SECRET,
    settings.DASHBOARD_JWT_ALGORITHM,
    settings.DASHBOARD_JWT_EXPIRE_MINUTES,
)

user_repository = UserRepository(database, app_users)
user_service = UserService(user_repository, auth_service)

healing_repository = HealingEventRepository(database, healing_events)
metrics_repository = MetricsSnapshotRepository(database, metrics_snapshots)
cucumber_repository = CucumberRunRepository(database, cucumber_runs)
test_run_repository = TestRunRepository(engine, test_runs)

dashboard_service = DashboardService(
    healing_repository=healing_repository,
    metrics_repository=metrics_repository,
    cucumber_repository=cucumber_repository,
    self_healing_metrics_url=settings.SELF_HEALING_METRICS_URL,
    self_healing_metrics_timeout=settings.SELF_HEALING_METRICS_TIMEOUT_SECONDS,
)

test_run_manager = TestRunManager(
    maven_project_dir=settings.MAVEN_PROJECT_DIR,
    maven_cmd=settings.MAVEN_CMD,
    backoffice_url=settings.BACKOFFICE_URL,
    backoffice_user_email=settings.BACKOFFICE_USER_EMAIL,
    backoffice_user_password=settings.BACKOFFICE_USER_PASSWORD,
    self_healing_enabled=settings.SELF_HEALING_ENABLED,
    rerun_enabled=settings.RERUN_ENABLED,
    dashboard_api_key_for_tests=settings.DASHBOARD_API_KEY_FOR_TESTS,
    dashboard_api=dashboard_service,
    test_run_repository=test_run_repository,
)

# ── Dependencies ────────────────────────────────────────────────────────────────
get_current_actor = create_dependency_getter(auth_service)
require_role = make_role_checker(get_current_actor)
require_ingest_auth = make_require_ingest_auth(auth_service, get_current_actor)

require_dashboard_reader = require_role({"admin", "project_manager", "qa_engineer"})
require_admin = require_role({"admin"})
require_project_manager = require_role({"admin", "project_manager"})
require_qa_engineer = require_role({"admin", "qa_engineer"})

# ── Seed users ──────────────────────────────────────────────────────────────────
def _resolve_password_hash(explicit_hash: str, plain: str, fallback_plain: str) -> str:
    if explicit_hash.strip():
        return explicit_hash.strip()
    candidate = plain.strip() or fallback_plain
    return auth_service.hash_password(candidate)


def _make_env_seed_users() -> list[dict[str, str]]:
    pm_email = settings.DASHBOARD_PM_EMAIL.strip().lower()
    pm_hash = _resolve_password_hash(
        settings.DASHBOARD_PM_PASSWORD_HASH,
        settings.DASHBOARD_PM_PASSWORD,
        "ChangeMePM123!",
    )
    pm_name = settings.DASHBOARD_PM_DISPLAY_NAME.strip() or "Chef de projet"

    qa_email = settings.DASHBOARD_QA_EMAIL.strip().lower()
    qa_hash = _resolve_password_hash(
        settings.DASHBOARD_QA_PASSWORD_HASH,
        settings.DASHBOARD_QA_PASSWORD,
        "ChangeMeQA123!",
    )
    qa_name = settings.DASHBOARD_QA_DISPLAY_NAME.strip() or "QA Engineer"

    return [
        {
            "email": settings.DASHBOARD_ADMIN_EMAIL,
            "password_hash": auth_service.hash_password(settings.DASHBOARD_ADMIN_PASSWORD or "admin123"),
            "display_name": settings.DASHBOARD_ADMIN_DISPLAY_NAME,
            "role": "admin",
        },
        {"email": pm_email, "password_hash": pm_hash, "display_name": pm_name, "role": "project_manager"},
        {"email": qa_email, "password_hash": qa_hash, "display_name": qa_name, "role": "qa_engineer"},
    ]


ENV_SEED_USERS = _make_env_seed_users()
_ACCOUNTS_FALLBACK = {u["email"]: u for u in ENV_SEED_USERS}


async def _find_user_by_email(email: str) -> Optional[User]:
    account = await user_service.find_by_email(email)
    if account:
        return account
    fallback = _ACCOUNTS_FALLBACK.get(email)
    if fallback:
        return User(**fallback, id=None, is_active=True)
    return None


async def _seed_default_users():
    for user in ENV_SEED_USERS:
        existing = await database.fetch_one(app_users.select().where(app_users.c.email == user["email"]))
        if existing:
            if existing["password_hash"] != user["password_hash"]:
                query = (
                    app_users.update()
                    .where(app_users.c.email == user["email"])
                    .values(
                        password_hash=user["password_hash"],
                        display_name=user["display_name"],
                        updated_at=datetime.now(timezone.utc),
                    )
                )
                await database.execute(query)
        else:
            query = app_users.insert().values(
                email=user["email"],
                password_hash=user["password_hash"],
                display_name=user["display_name"],
                role=user["role"],
                is_active=True,
                created_at=datetime.now(timezone.utc),
                updated_at=datetime.now(timezone.utc),
            )
            await database.execute(query)


# ── App ─────────────────────────────────────────────────────────────────────────
app = FastAPI(title="AI Test Automation Dashboard API", version="1.0.0")

# ── Reports static files ──────────────────────────────────────────────────────
reports_path = Path(settings.REPORTS_DIR)
try:
    reports_path.mkdir(parents=True, exist_ok=True)
except Exception:
    pass
if reports_path.is_dir():
    app.mount("/reports", StaticFiles(directory=str(reports_path), html=True), name="reports")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# ── Routers ─────────────────────────────────────────────────────────────────────
auth_router = create_auth_router(auth_service, user_service, get_current_actor)
user_router = create_user_router(user_service, require_admin)
dashboard_router = create_dashboard_router(
    dashboard_service=dashboard_service,
    test_run_manager=test_run_manager,
    require_ingest_auth=require_ingest_auth,
    require_dashboard_reader=require_dashboard_reader,
    require_admin=require_admin,
    require_project_manager=require_project_manager,
    require_qa_engineer=require_qa_engineer,
    reports_dir=settings.REPORTS_DIR,
    test_run_repository=test_run_repository,
)

app.include_router(auth_router)
app.include_router(user_router)
app.include_router(dashboard_router)


# ── Schema compatibility (auto-skip on SQLite for tests) ────────────────────────
def _run_schema_compat():
    if "sqlite" in str(engine.url).lower():
        return
    DashboardService.ensure_schema_compatibility(engine)


# ── Lifecycle events ────────────────────────────────────────────────────────────
@app.on_event("startup")
async def startup():
    metadata.create_all(engine)
    UserModel.metadata.create_all(engine)
    _run_schema_compat()
    await database.connect()
    await _seed_default_users()


@app.on_event("shutdown")
async def shutdown():
    await database.disconnect()


# ── Backward-compatible aliases (used by test files) ────────────────────────────
_test_runs = test_run_manager._test_runs
_test_runs_lock = test_run_manager._lock
_create_test_run = test_run_manager.create_run
_update_run = test_run_manager._update_run
_append_log = test_run_manager._append_log
_db_engine = engine
_test_runs_table = test_runs
