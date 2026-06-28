import asyncio
import json
import os
import subprocess
import threading
import uuid
from datetime import datetime, timedelta, timezone
from pathlib import Path
from typing import Any, Callable, Optional

import httpx
import sqlalchemy
from html.parser import HTMLParser

from src.repositories.cucumber_repository import CucumberRunRepository
from src.repositories.healing_repository import HealingEventRepository
from src.repositories.metrics_repository import MetricsSnapshotRepository
from src.repositories.test_run_repository import TestRunRepository


class TestRunManager:
    def __init__(
        self,
        maven_project_dir: str,
        maven_cmd: str,
        backoffice_url: str,
        backoffice_user_email: str,
        backoffice_user_password: str,
        self_healing_enabled: str,
        rerun_enabled: str = "true",
        dashboard_api_key_for_tests: str = "",
        dashboard_api: "DashboardService" = None,
        test_run_repository: Optional[TestRunRepository] = None,
    ):
        self._test_runs: dict[str, dict[str, Any]] = {}
        self._processes: dict[str, subprocess.Popen] = {}
        self._lock = threading.Lock()
        self.maven_project_dir = maven_project_dir
        self.maven_cmd = maven_cmd
        self.backoffice_url = backoffice_url
        self.backoffice_user_email = backoffice_user_email
        self.backoffice_user_password = backoffice_user_password
        self.self_healing_enabled = self_healing_enabled
        self.rerun_enabled = rerun_enabled
        self.dashboard_api_key_for_tests = dashboard_api_key_for_tests
        self.dashboard_api = dashboard_api
        self._maven_cwd = str(Path(maven_project_dir).parent)
        self._test_run_repo = test_run_repository

    def create_run(self, tags: str, runner: str, suite_name: Optional[str] = None) -> str:
        run_id = uuid.uuid4().hex[:12]
        now = datetime.now(timezone.utc)
        with self._lock:
            self._test_runs[run_id] = {
                "run_id": run_id,
                "tags": tags,
                "runner": runner,
                "suite_name": suite_name or tags,
                "status": "pending",
                "created_at": now.isoformat(),
                "completed_at": None,
                "logs": [],
                "exit_code": None,
                "error": None,
            }
        return run_id

    def _update_run(self, run_id: str, **kwargs):
        with self._lock:
            if run_id in self._test_runs:
                self._test_runs[run_id].update(kwargs)

    def _append_log(self, run_id: str, line: str):
        with self._lock:
            if run_id in self._test_runs:
                self._test_runs[run_id]["logs"].append(line)

    def _run_maven(self, cmd: list[str], run_id: str) -> int:
        process = subprocess.Popen(
            cmd,
            cwd=self._maven_cwd,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            bufsize=1,
        )
        with self._lock:
            self._processes[run_id] = process
        try:
            for line in iter(process.stdout.readline, ""):
                cleaned = line.rstrip("\n\r")
                self._append_log(run_id, cleaned)
            process.stdout.close()
            return process.wait()
        finally:
            with self._lock:
                self._processes.pop(run_id, None)

    def cancel_run(self, run_id: str) -> bool:
        with self._lock:
            proc = self._processes.get(run_id)
            run = self._test_runs.get(run_id)
        if proc and proc.poll() is None:
            proc.terminate()
            try:
                proc.wait(timeout=5)
            except subprocess.TimeoutExpired:
                proc.kill()
                proc.wait()
            if run:
                run["status"] = "cancelled"
                run["completed_at"] = datetime.now(timezone.utc).isoformat()
            self._append_log(run_id, "[runner] Execution cancelled by user")
            if run:
                self._persist_run(run)
            return True
        return False

    def execute_maven_run(self, run_id: str, tags: str, runner: str, req):
        try:
            self._update_run(run_id, status="running")

            project_dir = self.maven_project_dir
            report_dir = f"{project_dir}/target/reports/dashboard-run-{run_id}"
            json_file = f"{project_dir}/target/cucumber-{run_id}.json"
            rerun_file = f"{project_dir}/target/failed-{run_id}.txt"
            rerun_report_dir = f"{project_dir}/target/reports/dashboard-run-{run_id}-rerun"
            rerun_json_file = f"{project_dir}/target/cucumber-rerun-{run_id}.json"

            exec_args = (
                f"{report_dir} {json_file} {rerun_file}"
            )
            base_cmd = [
                self.maven_cmd, "process-test-resources", "compile", "exec:java",
                "-pl", "automation-framework",
                "-P", "ci",
                "--no-transfer-progress",
                "-Dexec.mainClass=org.automation.runner.DashboardCucumberRunner",
                f"-Dexec.args={exec_args}",
                "-Dexec.classpathScope=test",
                "-Dheadless=true",
                "-Dhealing.baseline.file=/app/automation-framework/target/healing-baseline.json",
                f"-Dcucumber.filter.tags={tags}",
                f"-Dbackoffice.url={req.backoffice_url or self.backoffice_url}",
                f"-Dbackoffice.user.email={req.backoffice_email or self.backoffice_user_email}",
                f"-Dbackoffice.user.password={req.backoffice_password or self.backoffice_user_password}",
                f"-Dself.healing.enabled={self.self_healing_enabled}",
                "-Ddashboard.api.url=http://localhost:8080",
            ]
            if self.dashboard_api_key_for_tests:
                base_cmd.append(f"-Ddashboard.api.key={self.dashboard_api_key_for_tests}")

            self._append_log(run_id, f"[runner] Starting: {' '.join(base_cmd)}")
            self._append_log(run_id, f"[runner] Project dir: {project_dir}")
            exit_code = self._run_maven(base_cmd, run_id)
            self._append_log(run_id, f"[runner] Primary exit code: {exit_code}")

            rerun_path = Path(rerun_file)
            if self.rerun_enabled != "false" and rerun_path.exists() and rerun_path.stat().st_size > 0:
                self._append_log(run_id, f"[runner] Failed scenarios detected — re-running from {rerun_file}")
                rerun_exec_args = f"{rerun_report_dir} {rerun_json_file}"
                rerun_cmd = [
                    self.maven_cmd, "exec:java",
                    "-pl", "automation-framework",
                    "-P", "ci",
                    "--no-transfer-progress",
                    "-Dexec.mainClass=org.automation.runner.DashboardCucumberRunner",
                    f"-Dexec.args={rerun_exec_args}",
                    "-Dexec.classpathScope=test",
                    f"-Dcucumber.features=@{rerun_file}",
                    f"-Dbackoffice.url={req.backoffice_url or self.backoffice_url}",
                    f"-Dbackoffice.user.email={req.backoffice_email or self.backoffice_user_email}",
                    f"-Dbackoffice.user.password={req.backoffice_password or self.backoffice_user_password}",
                    f"-Dself.healing.enabled={self.self_healing_enabled}",
                    "-Dself.healing.api.url=http://localhost:8080",
                    "-Ddashboard.api.url=http://localhost:8080",
                ]
                if self.dashboard_api_key_for_tests:
                    rerun_cmd.append(f"-Ddashboard.api.key={self.dashboard_api_key_for_tests}")
                self._append_log(run_id, f"[runner] Rerun command: {' '.join(rerun_cmd)}")
                rerun_exit = self._run_maven(rerun_cmd, run_id)
                self._append_log(run_id, f"[runner] Rerun exit code: {rerun_exit}")
            else:
                self._append_log(run_id, "[runner] No failed scenarios to re-run")

            self._update_run(
                run_id,
                status="completed" if exit_code == 0 else "failed",
                exit_code=exit_code,
                completed_at=datetime.now(timezone.utc).isoformat(),
            )

            loop = asyncio.new_event_loop()
            json_abs = str(Path(self.maven_project_dir) / json_file)
            self._append_log(run_id, f"[runner] Pushing primary results from {json_abs} ...")
            loop.run_until_complete(
                self.dashboard_api.push_cucumber_json(run_id, json_abs, self._append_log)
            )
            rerun_json_abs = str(Path(self.maven_project_dir) / rerun_json_file)
            if Path(rerun_json_abs).exists():
                self._append_log(run_id, f"[runner] Pushing rerun results from {rerun_json_abs} ...")
                loop.run_until_complete(
                    self.dashboard_api.push_cucumber_json(run_id, rerun_json_abs, self._append_log)
                )
            loop.close()

        except Exception as exc:
            self._update_run(
                run_id,
                status="failed",
                error=str(exc),
                completed_at=datetime.now(timezone.utc).isoformat(),
            )
            self._append_log(run_id, f"[runner] Error: {exc}")
        finally:
            with self._lock:
                run_data = self._test_runs.get(run_id)
            if run_data:
                self._persist_run(run_data)

    def _persist_run(self, run_data: dict):
        if self._test_run_repo is None:
            return
        self._test_run_repo.upsert(run_data)

    def get_run(self, run_id: str) -> Optional[dict]:
        with self._lock:
            run = self._test_runs.get(run_id)
        if run:
            return run
        if self._test_run_repo is None:
            return None
        return self._test_run_repo.get(run_id)

    def list_runs(self, limit: int = 50, offset: int = 0) -> list:
        db_runs = []
        if self._test_run_repo is not None:
            db_runs = self._test_run_repo.list(limit + offset) if offset else self._test_run_repo.list(limit)
        with self._lock:
            memory_runs = list(self._test_runs.values())
        memory_ids = {r["run_id"] for r in memory_runs}
        merged = list(memory_runs)
        for r in db_runs:
            if r["run_id"] not in memory_ids:
                merged.append(r)
        merged.sort(key=lambda r: r["created_at"], reverse=True)
        return merged[offset:offset + limit]


class DashboardService:
    def __init__(
        self,
        healing_repository: HealingEventRepository,
        metrics_repository: MetricsSnapshotRepository,
        cucumber_repository: CucumberRunRepository,
        self_healing_metrics_url: str,
        self_healing_metrics_timeout: float,
    ):
        self.healing_repo = healing_repository
        self.metrics_repo = metrics_repository
        self.cucumber_repo = cucumber_repository
        self.self_healing_metrics_url = self_healing_metrics_url
        self.self_healing_metrics_timeout = self_healing_metrics_timeout

    @property
    def database(self):
        return self.healing_repo.database

    # ── Helpers ─────────────────────────────────────────────────────────────────

    @staticmethod
    def compute_scenario_status(feature_elem: dict) -> str:
        for hook in feature_elem.get("before", []):
            s = hook.get("result", {}).get("status", "skipped")
            if s == "failed":
                return "failed"
        for step in feature_elem.get("steps", []):
            s = step.get("result", {}).get("status", "skipped")
            if s == "failed":
                return "failed"
        for hook in feature_elem.get("after", []):
            s = hook.get("result", {}).get("status", "skipped")
            if s == "failed":
                return "failed"
        saw_skipped = False
        for hook in feature_elem.get("before", []):
            s = hook.get("result", {}).get("status", "skipped")
            if s in ("skipped", "pending"):
                saw_skipped = True
        for step in feature_elem.get("steps", []):
            s = step.get("result", {}).get("status", "skipped")
            if s in ("skipped", "pending"):
                saw_skipped = True
        for hook in feature_elem.get("after", []):
            s = hook.get("result", {}).get("status", "skipped")
            if s in ("skipped", "pending"):
                saw_skipped = True
        return "skipped" if saw_skipped else "passed"

    @staticmethod
    def compute_scenario_duration(feature_elem: dict) -> int:
        total = 0
        for hook in feature_elem.get("before", []):
            total += hook.get("result", {}).get("duration", 0)
        for step in feature_elem.get("steps", []):
            total += step.get("result", {}).get("duration", 0)
        for hook in feature_elem.get("after", []):
            total += hook.get("result", {}).get("duration", 0)
        return total

    @staticmethod
    def norm_run_id(value: Optional[str]) -> Optional[str]:
        if value is None:
            return None
        run_id = value.strip()
        return run_id if run_id else None

    @staticmethod
    def build_healing_scopes(selected_run_id: Optional[str]) -> list[tuple]:
        scopes = []
        if selected_run_id:
            scopes.append(("run", selected_run_id, "matched_run"))
            scopes.append(("null", None, "uncorrelated"))
        scopes.append(("any", None, "latest_available"))
        return scopes

    @staticmethod
    def normalize_external_metrics(raw: dict) -> dict:
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
            "run_id": DashboardService.norm_run_id(raw.get("run_id")),
        }

    # ── Healing scoped queries ──────────────────────────────────────────────────

    async def _fetch_snapshot_for_scope(self, scope_mode: str, run_id: Optional[str]) -> Optional[dict]:
        if scope_mode == "run" and run_id:
            return await self.metrics_repo.get_latest(run_id=run_id)
        if scope_mode == "null":
            return await self.metrics_repo.get_latest(run_id=None)
        return await self.metrics_repo.get_latest()

    async def _fetch_history_for_scope(self, scope_mode: str, run_id: Optional[str], limit: int = 50) -> list:
        if scope_mode == "run" and run_id:
            return await self.metrics_repo.get_history(run_id=run_id, limit=limit)
        if scope_mode == "null":
            return await self.metrics_repo.get_history(run_id=None, limit=limit)
        return await self.metrics_repo.get_history(limit=limit)

    async def _fetch_recent_events_for_scope(self, scope_mode: str, run_id: Optional[str], limit: int = 50) -> list:
        cutoff = None
        effective_run_id = None
        if scope_mode == "run" and run_id:
            effective_run_id = run_id
            latest_run_at = await self.cucumber_repo.get_latest_run_at(run_id)
            if latest_run_at:
                cutoff = latest_run_at - timedelta(hours=3)
        if scope_mode == "null":
            effective_run_id = None
            return await self.healing_repo.fetch_with_null_run_id()
        if scope_mode == "any":
            effective_run_id = None
        return await self.healing_repo.fetch_recent(effective_run_id, cutoff, limit)

    @staticmethod
    def aggregate_metrics(items: list[dict]) -> dict:
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
        run_id_val = next((row.get("run_id") for row in items if row.get("run_id")), None)

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
            "run_id": run_id_val,
        }

    async def _derive_metrics_from_events(self, scope_mode: str, run_id: Optional[str]) -> Optional[dict]:
        if scope_mode == "run" and run_id:
            rows = await self.healing_repo.fetch_by_run_id(run_id)
        elif scope_mode == "null":
            rows = await self.healing_repo.fetch_with_null_run_id()
        else:
            rows = await self.healing_repo.fetch_all()
        if not rows:
            return None
        return self.aggregate_metrics(rows)

    async def _derive_metrics_history_from_events(self, scope_mode: str, run_id: Optional[str]) -> list:
        if scope_mode == "run" and run_id:
            items = await self.healing_repo.fetch_by_run_id(run_id)
        elif scope_mode == "null":
            items = await self.healing_repo.fetch_with_null_run_id()
        else:
            items = await self.healing_repo.fetch_all()
        if not items:
            return []

        buckets = {}
        bucket_order = []
        for item in items:
            rid = item.get("run_id") or "_no_run_id"
            if rid not in buckets:
                buckets[rid] = []
                bucket_order.append(rid)
            buckets[rid].append(item)

        result = []
        for rid in bucket_order:
            bucket_metrics = self.aggregate_metrics(buckets[rid])
            bucket_metrics["run_id"] = None if rid == "_no_run_id" else rid
            result.append(bucket_metrics)
        return result

    async def _fetch_external_healing_metrics(self) -> Optional[dict]:
        if not self.self_healing_metrics_url:
            return None
        try:
            async with httpx.AsyncClient(timeout=self.self_healing_metrics_timeout) as client:
                response = await client.get(
                    self.self_healing_metrics_url,
                    headers={"ngrok-skip-browser-warning": "true"},
                )
                response.raise_for_status()
                payload = response.json()
        except Exception:
            return None
        if not isinstance(payload, dict):
            return None
        return self.normalize_external_metrics(payload)

    async def resolve_healing_payload(self, selected_run_id: Optional[str]) -> tuple:
        for scope_mode, scope_run_id, scope_label in self.build_healing_scopes(selected_run_id):
            metrics_row = await self._fetch_snapshot_for_scope(scope_mode, scope_run_id)
            if metrics_row:
                history_rows = await self._fetch_history_for_scope(scope_mode, scope_run_id)
                events_rows = await self._fetch_recent_events_for_scope(scope_mode, scope_run_id)
                return metrics_row, history_rows, events_rows, f"metrics_snapshots:{scope_label}"

            derived_metrics = await self._derive_metrics_from_events(scope_mode, scope_run_id)
            if derived_metrics:
                events_rows = await self._fetch_recent_events_for_scope(scope_mode, scope_run_id)
                history_rows = await self._derive_metrics_history_from_events(scope_mode, scope_run_id)
                return derived_metrics, history_rows, events_rows, f"healing_events:{scope_label}"

        external = await self._fetch_external_healing_metrics()
        if external:
            return external, [external], [], "external_metrics_url"

        return None, [], [], "unavailable"

    # ── Schema ──────────────────────────────────────────────────────────────────

    @staticmethod
    def ensure_schema_compatibility(engine):
        statements = [
            "ALTER TABLE healing_events ADD COLUMN IF NOT EXISTS exception_type VARCHAR(64)",
            "ALTER TABLE healing_events ADD COLUMN IF NOT EXISTS run_id VARCHAR(128)",
            "ALTER TABLE metrics_snapshots ADD COLUMN IF NOT EXISTS run_id VARCHAR(128)",
            "ALTER TABLE cucumber_runs ADD COLUMN IF NOT EXISTS run_id VARCHAR(128)",
            "CREATE INDEX IF NOT EXISTS idx_healing_events_run_id ON healing_events (run_id)",
            "CREATE INDEX IF NOT EXISTS idx_metrics_snapshots_run_id ON metrics_snapshots (run_id)",
            "CREATE INDEX IF NOT EXISTS idx_cucumber_runs_run_id ON cucumber_runs (run_id)",
            "ALTER TABLE cucumber_runs ADD COLUMN IF NOT EXISTS classification VARCHAR(32)",
        ]
        with engine.begin() as conn:
            for sql in statements:
                conn.execute(sqlalchemy.text(sql))

    # ── Healing Events ──────────────────────────────────────────────────────────

    async def create_healing_event(self, event_data: dict) -> int:
        return await self.healing_repo.create(event_data)

    async def list_healing_events(self, limit: int, offset: int, run_id: Optional[str]) -> list:
        normalized_run_id = self.norm_run_id(run_id)
        if normalized_run_id:
            latest_run_at = await self.cucumber_repo.get_latest_run_at(normalized_run_id)
            if latest_run_at:
                cutoff = latest_run_at - timedelta(hours=3)
                return await self.healing_repo.fetch_recent(normalized_run_id, cutoff, limit)
            return await self.healing_repo.list(limit, offset, normalized_run_id)
        return await self.healing_repo.list(limit, offset)

    # ── Metrics Snapshots ───────────────────────────────────────────────────────

    async def push_metrics(self, snapshot_data: dict) -> int:
        return await self.metrics_repo.create(snapshot_data)

    async def get_latest_metrics(self, run_id: Optional[str]) -> dict:
        return await self.metrics_repo.get_latest(run_id)

    async def get_metrics_history(self, limit: int, run_id: Optional[str]) -> list:
        return await self.metrics_repo.get_history(run_id, limit)

    # ── Cucumber Runs ───────────────────────────────────────────────────────────

    async def push_cucumber_run(self, scenarios: list, parent_run_id: Optional[str]) -> dict:
        now = datetime.now(timezone.utc)
        parent_run_id = self.norm_run_id(parent_run_id)
        inserted = 0
        for sc in scenarios:
            scenario_run_id = self.norm_run_id(sc.get("run_id")) or parent_run_id
            await self.cucumber_repo.create_scenario(
                run_at=now,
                feature_name=sc.get("feature_name"),
                scenario=sc.get("scenario"),
                status=sc.get("status"),
                duration_ns=sc.get("duration_ns"),
                tags=sc.get("tags"),
                run_id=scenario_run_id,
                classification=sc.get("classification"),
            )
            inserted += 1
        return {"inserted": inserted, "run_id": parent_run_id}

    async def classify_cucumber_runs(self, scenarios: list, parent_run_id: Optional[str]) -> dict:
        now = datetime.now(timezone.utc)
        parent_run_id = self.norm_run_id(parent_run_id)
        inserted = 0
        for sc in scenarios:
            scenario_run_id = self.norm_run_id(sc.get("run_id")) or parent_run_id
            classification = sc.get("classification") or sc.get("status")
            await self.cucumber_repo.create_scenario(
                run_at=now,
                feature_name=sc.get("feature_name"),
                scenario=sc.get("scenario"),
                status=classification,
                duration_ns=sc.get("duration_ns"),
                tags=sc.get("tags"),
                run_id=scenario_run_id,
                classification=classification,
            )
            inserted += 1
        return {"inserted": inserted, "run_id": parent_run_id}

    async def cucumber_summary(self, run_id: Optional[str]) -> dict:
        selected_run_id = self.norm_run_id(run_id)
        latest_run_at = None

        if selected_run_id:
            latest_run_at = await self.cucumber_repo.get_latest_run_at(selected_run_id)
            if not latest_run_at:
                return {"passed": 0, "failed": 0, "skipped": 0, "flaky": 0, "total": 0, "run_id": selected_run_id, "run_at": None}
        else:
            latest_non_null_run_id = await self.cucumber_repo.get_latest_run_id()
            if latest_non_null_run_id:
                selected_run_id = latest_non_null_run_id
                latest_run_at = await self.cucumber_repo.get_latest_run_at(selected_run_id)
            else:
                latest_run_at = await self.cucumber_repo.get_global_latest_run_at()

        if not latest_run_at:
            return {"passed": 0, "failed": 0, "skipped": 0, "flaky": 0, "total": 0}

        if selected_run_id:
            rows = await self.cucumber_repo.get_counts_by_run_id(selected_run_id)
        else:
            from sqlalchemy import text
            rows_raw = await self.healing_repo.database.fetch_all(text("""
                SELECT status, COUNT(*) as cnt
                FROM cucumber_runs
                WHERE run_at >= (SELECT MAX(run_at) - INTERVAL '5 seconds' FROM cucumber_runs)
                GROUP BY status
            """))
            rows = [dict(r) for r in rows_raw]

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

    async def recent_cucumber_runs(self, limit: int, run_id: Optional[str]) -> list:
        return await self.cucumber_repo.get_recent(run_id, limit)

    async def dashboard_summary(self, run_id: Optional[str], actor: dict) -> dict:
        selected_run_id = self.norm_run_id(run_id)
        if not selected_run_id:
            selected_run_id = await self.cucumber_repo.get_latest_run_id()

        metrics_payload, history_rows, events_rows, healing_source = await self.resolve_healing_payload(selected_run_id)

        if selected_run_id:
            cucumber_rows = await self.cucumber_repo.get_counts_by_run_id(selected_run_id)
            avg_dur_ns = await self.cucumber_repo.get_avg_duration(selected_run_id)
        else:
            from sqlalchemy import text
            rows_raw = await self.healing_repo.database.fetch_all(text("""
                SELECT status, COUNT(*) as cnt
                FROM cucumber_runs
                WHERE run_at >= (SELECT MAX(run_at) - INTERVAL '5 seconds' FROM cucumber_runs)
                GROUP BY status
            """))
            cucumber_rows = [dict(r) for r in rows_raw]
            avg_dur_ns = None

        cuc_counts = {r["status"]: r["cnt"] for r in cucumber_rows}
        total = sum(cuc_counts.values())
        passed = cuc_counts.get("passed", 0)
        success_rate = round(passed / total, 4) if total else 0.0
        avg_execution_time_ms = round(avg_dur_ns / 1_000_000, 2) if avg_dur_ns else None

        role = str(actor.get("role", ""))
        show_trends = role in ("admin", "project_manager")
        show_healing_events = role in ("admin", "qa_engineer")

        return {
            "run_id": selected_run_id,
            "metrics": metrics_payload,
            "metrics_history": history_rows if show_trends else [],
            "recent_events": events_rows if show_healing_events else [],
            "healing_source": healing_source if show_healing_events else "unavailable",
            "cucumber": {
                "passed": passed,
                "flaky": cuc_counts.get("flaky", 0),
                "failed": cuc_counts.get("failed", 0),
                "skipped": cuc_counts.get("skipped", 0),
                "total": total,
                "avg_execution_time_ms": avg_execution_time_ms,
                "success_rate": success_rate,
            },
        }

    # ── Reports ─────────────────────────────────────────────────────────────────

    @staticmethod
    def list_reports(reports_dir: str) -> dict:
        reports_root = Path(reports_dir)
        if not reports_root.is_dir():
            return {"root": None, "files": [], "error": "Reports directory not found"}

        def scan(dir_path: Path, relative: str = "") -> dict:
            entries = []
            for child in sorted(dir_path.iterdir()):
                rel = f"{relative}/{child.name}" if relative else child.name
                if child.is_dir():
                    entries.append({
                        "name": child.name,
                        "path": rel,
                        "type": "directory",
                        "children": scan(child, rel).get("entries", []),
                    })
                elif child.suffix in (".html", ".json", ".xml", ".txt", ".png", ".jpg", ".svg"):
                    entries.append({
                        "name": child.name,
                        "path": rel,
                        "type": "file",
                        "size": child.stat().st_size,
                    })
            return {"entries": entries}

        return scan(reports_root)

    # ── Cucumber JSON push (for test runner) ────────────────────────────────────

    async def push_cucumber_json(self, run_id: str, json_path: str, append_log: Callable):
        path = Path(json_path)
        if not path.exists():
            append_log(run_id, f"[push] Cucumber JSON not found: {json_path}")
            return

        try:
            raw = json.loads(path.read_text("utf-8"))
        except Exception as exc:
            append_log(run_id, f"[push] Failed to parse {json_path}: {exc}")
            return

        scenarios = []
        for feature in raw if isinstance(raw, list) else [raw]:
            fname = feature.get("name", "unknown")
            for elem in feature.get("elements", []):
                if elem.get("type") not in ("scenario", "scenario_outline"):
                    continue
                tag_names = [t.get("name", "") for t in elem.get("tags", [])]
                scenarios.append({
                    "feature_name": fname,
                    "scenario": elem.get("name", ""),
                    "status": self.compute_scenario_status(elem),
                    "duration_ns": self.compute_scenario_duration(elem),
                    "tags": ",".join(tag_names),
                    "run_id": run_id,
                })

        if not scenarios:
            append_log(run_id, "[push] No scenarios found in JSON")
            return

        payload = {"run_id": run_id, "scenarios": scenarios}
        api_url = os.getenv("DASHBOARD_API_URL", "http://localhost:8080").rstrip("/")
        headers = {"Content-Type": "application/json"}
        dash_key = os.getenv("DASHBOARD_API_KEY_FOR_TESTS", "")
        if dash_key:
            headers["X-API-Key"] = dash_key

        try:
            async with httpx.AsyncClient(timeout=30) as client:
                resp = await client.post(f"{api_url}/api/cucumber-runs", json=payload, headers=headers)
                append_log(run_id, f"[push] Pushed {len(scenarios)} scenarios -> HTTP {resp.status_code}")
        except Exception as exc:
            append_log(run_id, f"[push] HTTP push failed: {exc}")

    # ── Self-Healing Endpoint (CI Stub) ─────────────────────────────────────────

    class _HealElementExtractor(HTMLParser):
        def __init__(self):
            super().__init__()
            self.elements = []
            self._current_tag = ""
            self._current_attrs = {}
            self._current_text = []
            self._in_script = False

        def handle_starttag(self, tag, attrs):
            if tag in ("script", "style"):
                self._in_script = True
                return
            self._current_tag = tag
            self._current_attrs = {k: v for k, v in attrs if v is not None}
            self._current_text = []

        def handle_endtag(self, tag):
            if tag in ("script", "style"):
                self._in_script = False
                return
            if tag == self._current_tag and self._current_tag:
                text = " ".join(t.strip() for t in self._current_text if t.strip())
                if text or self._current_attrs:
                    self.elements.append({
                        "tag": self._current_tag,
                        "text": text,
                        "attrs": dict(self._current_attrs),
                    })
                self._current_tag = ""
                self._current_attrs = {}
                self._current_text = []

        def handle_data(self, data):
            if not self._in_script:
                self._current_text.append(data)

    @staticmethod
    def _match_element(elements, old_element):
        target_text = (old_element.get("text") or "").strip().lower()
        attrs = old_element.get("attributes") or {}
        candidates = []

        for el in elements:
            el_text_raw = (el.get("text") or "").strip()
            el_text_lower = el_text_raw.lower()
            el_attrs = el.get("attrs") or {}
            score = 0.0

            if target_text and el_text_lower == target_text:
                score = 1.0
            elif target_text and (target_text in el_text_lower or el_text_lower in target_text):
                score = 0.8

            for key in ("aria-label", "placeholder", "title", "name", "data-testid"):
                target_val = (attrs.get(key) or "").strip().lower()
                el_val = (el_attrs.get(key) or "").strip().lower()
                if target_val and el_val == target_val:
                    score = max(score, 0.9)
                elif target_val and (target_val in el_val or el_val in target_val):
                    score = max(score, 0.7)

            old_tag = (old_element.get("element_type") or "").strip().lower()
            if old_tag and el.get("tag", "").lower() == old_tag and score > 0:
                score = min(score + 0.1, 1.0)

            if score > 0:
                el_id = el_attrs.get("id")
                if el_id and not el_id.startswith("f_"):
                    xpath = f"//{el['tag']}[@id='{el_id}']"
                else:
                    text_for_xpath = el_text_raw.replace("'", "&apos;")
                    xpath = f"//{el['tag']}[contains(text(), '{text_for_xpath}')]"
                candidates.append((score, {"type": "xpath", "value": xpath}))

        if not candidates:
            return None
        candidates.sort(key=lambda x: -x[0])
        return candidates[0][1]

    def heal_element(self, current_dom: str, old_element: dict) -> dict:
        if not current_dom or not old_element:
            return {"success": False, "error": "Missing current_dom or old_element", "new_locator": None, "score": 0.0}

        parser = self._HealElementExtractor()
        parser.feed(current_dom)
        parser.close()

        new_locator = self._match_element(parser.elements, old_element)
        if new_locator:
            return {"success": True, "score": 0.95, "new_locator": new_locator, "error": None, "details": {"matching_strategy": "text_attribute_fallback"}}

        return {"success": False, "error": "No matching element found in DOM", "new_locator": None, "score": 0.0}
