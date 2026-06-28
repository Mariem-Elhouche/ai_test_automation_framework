from datetime import datetime
from typing import Any, Optional

from fastapi import APIRouter, Depends, HTTPException, status

from src.schemas import CucumberRunIn, HealRequest, HealingEventIn, MetricsSnapshotIn, TestRunRequest
from src.services.dashboard_service import DashboardService, TestRunManager


def create_dashboard_router(
    dashboard_service: DashboardService,
    test_run_manager: TestRunManager,
    require_ingest_auth: Any,
    require_dashboard_reader: Any,
    require_admin: Any,
    require_project_manager: Any,
    require_qa_engineer: Any,
    reports_dir: str = "",
) -> APIRouter:
    router = APIRouter(tags=["dashboard"])

    @router.get("/health")
    async def health():
        return {"status": "ok", "timestamp": datetime.now().isoformat()}

    # ── Healing Events ─────────────────────────────────────────────────────────

    @router.post("/api/healing-events", status_code=201)
    async def create_healing_event(event: HealingEventIn, _auth: dict[str, Any] = Depends(require_ingest_auth)):
        event_id = await dashboard_service.create_healing_event(event.dict())
        return {"id": event_id}

    @router.get("/api/healing-events")
    async def list_healing_events(
        limit: int = 50,
        offset: int = 0,
        run_id: Optional[str] = None,
        _auth: dict[str, Any] = Depends(require_qa_engineer),
    ):
        return await dashboard_service.list_healing_events(limit, offset, run_id)

    # ── Metrics Snapshots ──────────────────────────────────────────────────────

    @router.post("/api/metrics", status_code=201)
    async def push_metrics(snapshot: MetricsSnapshotIn, _auth: dict[str, Any] = Depends(require_ingest_auth)):
        snap_id = await dashboard_service.push_metrics(snapshot.dict())
        return {"id": snap_id}

    @router.get("/api/metrics/latest")
    async def get_latest_metrics(
        run_id: Optional[str] = None,
        _auth: dict[str, Any] = Depends(require_dashboard_reader),
    ):
        row = await dashboard_service.get_latest_metrics(run_id)
        if not row:
            raise HTTPException(status_code=404, detail="No metrics yet")
        return row

    @router.get("/api/metrics/history")
    async def get_metrics_history(
        limit: int = 100,
        run_id: Optional[str] = None,
        _auth: dict[str, Any] = Depends(require_dashboard_reader),
    ):
        return await dashboard_service.get_metrics_history(limit, run_id)

    # ── Cucumber Runs ──────────────────────────────────────────────────────────

    @router.post("/api/cucumber-runs", status_code=201)
    async def push_cucumber_run(run: CucumberRunIn, _auth: dict[str, Any] = Depends(require_ingest_auth)):
        scenarios = [s.dict() for s in run.scenarios]
        return await dashboard_service.push_cucumber_run(scenarios, run.run_id)

    @router.post("/api/cucumber-runs/classify", status_code=201)
    async def classify_cucumber_runs(run: CucumberRunIn, _auth: dict[str, Any] = Depends(require_ingest_auth)):
        scenarios = [s.dict() for s in run.scenarios]
        return await dashboard_service.classify_cucumber_runs(scenarios, run.run_id)

    @router.get("/api/cucumber-runs/summary")
    async def cucumber_summary(
        run_id: Optional[str] = None,
        _auth: dict[str, Any] = Depends(require_dashboard_reader),
    ):
        return await dashboard_service.cucumber_summary(run_id)

    @router.get("/api/cucumber-runs/recent")
    async def recent_cucumber_runs(
        limit: int = 20,
        run_id: Optional[str] = None,
        _auth: dict[str, Any] = Depends(require_dashboard_reader),
    ):
        return await dashboard_service.recent_cucumber_runs(limit, run_id)

    # ── Dashboard summary ──────────────────────────────────────────────────────

    @router.get("/api/dashboard")
    async def dashboard_summary(
        run_id: Optional[str] = None,
        _auth: dict[str, Any] = Depends(require_dashboard_reader),
    ):
        return await dashboard_service.dashboard_summary(run_id, _auth)

    # ── Reports Listing ─────────────────────────────────────────────────────────

    @router.get("/api/reports")
    async def list_reports(_auth: dict[str, Any] = Depends(require_dashboard_reader)):
        return DashboardService.list_reports(reports_dir)

    # ── Test Runner Endpoints ───────────────────────────────────────────────────

    @router.post("/api/run-tests", status_code=201)
    async def start_test_run(req: TestRunRequest, actor: dict[str, Any] = Depends(require_qa_engineer)):
        run_id = test_run_manager.create_run(req.tags, req.runner, req.suite_name)
        import threading
        thread = threading.Thread(
            target=test_run_manager.execute_maven_run,
            args=(run_id, req.tags, req.runner, req),
            daemon=True,
        )
        thread.start()
        return test_run_manager.get_run(run_id)

    @router.get("/api/run-tests/{run_id}")
    async def get_test_run(run_id: str, actor: dict[str, Any] = Depends(require_qa_engineer)):
        run = test_run_manager.get_run(run_id)
        if not run:
            raise HTTPException(status_code=404, detail="Run not found")
        return run

    @router.get("/api/run-tests")
    async def list_test_runs(
        limit: int = 50,
        offset: int = 0,
        actor: dict[str, Any] = Depends(require_qa_engineer),
    ):
        return test_run_manager.list_runs(limit, offset)

    @router.delete("/api/run-tests/{run_id}")
    async def cancel_test_run(run_id: str, actor: dict[str, Any] = Depends(require_qa_engineer)):
        cancelled = test_run_manager.cancel_run(run_id)
        if not cancelled:
            raise HTTPException(status_code=404, detail="Run not found or already finished")
        return {"detail": "Run cancelled"}

    # ── Self-Healing Endpoint (CI Stub) ─────────────────────────────────────────

    @router.post("/heal")
    async def heal_endpoint(req: HealRequest, _auth: dict[str, Any] = Depends(require_ingest_auth)):
        return dashboard_service.heal_element(req.current_dom or "", req.old_element or {})

    return router
