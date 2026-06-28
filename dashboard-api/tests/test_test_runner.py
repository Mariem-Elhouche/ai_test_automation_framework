"""
Tests unitaires pour le service TestRunner du Dashboard.
"""
import os
import time

os.environ["DATABASE_URL"] = "sqlite:///./src/test_runner.db"
os.environ["DASHBOARD_JWT_SECRET"] = "test-secret-for-testing"
os.environ["DASHBOARD_ADMIN_EMAIL"] = "admin@test.local"
os.environ["DASHBOARD_ADMIN_PASSWORD"] = "admin123"
os.environ["DASHBOARD_ADMIN_DISPLAY_NAME"] = "Test Admin"
os.environ["DASHBOARD_PM_EMAIL"] = "pm@test.local"
os.environ["DASHBOARD_PM_PASSWORD"] = "pm123"
os.environ["DASHBOARD_PM_DISPLAY_NAME"] = "Test PM"
os.environ["DASHBOARD_QA_EMAIL"] = "qa@test.local"
os.environ["DASHBOARD_QA_PASSWORD"] = "qa123"
os.environ["DASHBOARD_QA_DISPLAY_NAME"] = "Test QA"
os.environ["MAVEN_CMD"] = "mvn"
os.environ["MAVEN_PROJECT_DIR"] = "/tmp/test-maven"
os.environ["RERUN_ENABLED"] = "true"

from unittest.mock import MagicMock, patch

import pytest
from fastapi.testclient import TestClient

from src import dashboard_api

app = dashboard_api.app

DB_PATH = "./src/test_runner.db"


@pytest.fixture(scope="session")
def client():
    try:
        os.remove(DB_PATH)
    except (FileNotFoundError, PermissionError):
        pass
    with TestClient(app) as c:
        yield c
    try:
        os.remove(DB_PATH)
    except (FileNotFoundError, PermissionError):
        pass


# ── Helpers ─────────────────────────────────────────────────────────────────────

def _login(client, email, password):
    r = client.post("/auth/login", json={"email": email, "password": password})
    assert r.status_code == 200, f"Login failed for {email}: {r.text}"
    return r.json()["access_token"]


def _qa_headers(client):
    return {"Authorization": f"Bearer {_login(client, 'qa@test.local', 'qa123')}"}


def _pm_headers(client):
    return {"Authorization": f"Bearer {_login(client, 'pm@test.local', 'pm123')}"}


# ── Tests du service in-memory ─────────────────────────────────────────────────

def _clear_test_runs_db():
    if dashboard_api._db_engine is not None and dashboard_api._test_runs_table is not None:
        with dashboard_api._db_engine.begin() as conn:
            conn.execute(dashboard_api._test_runs_table.delete())


class TestInMemoryStore:
    def setup_method(self):
        dashboard_api._test_runs.clear()
        _clear_test_runs_db()

    def test_create_run(self):
        run_id = dashboard_api._create_test_run("@smoke", "GenericTagRunner", "Smoke Suite")
        assert run_id is not None
        assert len(run_id) == 12
        with dashboard_api._test_runs_lock:
            run = dashboard_api._test_runs[run_id]
        assert run["tags"] == "@smoke"
        assert run["runner"] == "GenericTagRunner"
        assert run["suite_name"] == "Smoke Suite"
        assert run["status"] == "pending"
        assert run["logs"] == []
        assert run["exit_code"] is None

    def test_create_run_default_suite_name(self):
        run_id = dashboard_api._create_test_run("@regression", "GenericTagRunner")
        with dashboard_api._test_runs_lock:
            run = dashboard_api._test_runs[run_id]
        assert run["suite_name"] == "@regression"

    def test_update_run(self):
        run_id = dashboard_api._create_test_run("@smoke", "GenericTagRunner")
        dashboard_api._update_run(run_id, status="running", exit_code=0)
        with dashboard_api._test_runs_lock:
            run = dashboard_api._test_runs[run_id]
        assert run["status"] == "running"
        assert run["exit_code"] == 0

    def test_update_nonexistent_run(self):
        dashboard_api._update_run("nonexistent", status="running")
        assert "nonexistent" not in dashboard_api._test_runs

    def test_append_log(self):
        run_id = dashboard_api._create_test_run("@smoke", "GenericTagRunner")
        dashboard_api._append_log(run_id, "line 1")
        dashboard_api._append_log(run_id, "line 2")
        with dashboard_api._test_runs_lock:
            logs = dashboard_api._test_runs[run_id]["logs"]
        assert logs == ["line 1", "line 2"]

    def test_append_log_nonexistent_run(self):
        dashboard_api._append_log("nonexistent", "should not fail")
        assert True


# ── Tests du endpoint POST /api/run-tests ───────────────────────────────────────

class TestStartTestRun:
    def setup_method(self):
        dashboard_api._test_runs.clear()
        _clear_test_runs_db()

    def test_start_run_as_qa(self, client):
        headers = _qa_headers(client)
        r = client.post("/api/run-tests", json={"tags": "@smoke"}, headers=headers)
        assert r.status_code == 201
        data = r.json()
        assert data["tags"] == "@smoke"
        assert data["runner"] == "GenericTagRunner"
        assert data["status"] in ("pending", "running")
        assert "run_id" in data
        assert isinstance(data["logs"], list)

    def test_start_run_denied_pm(self, client):
        headers = _pm_headers(client)
        r = client.post("/api/run-tests", json={"tags": "@smoke"}, headers=headers)
        assert r.status_code == 403

    def test_start_run_denied_no_auth(self, client):
        r = client.post("/api/run-tests", json={"tags": "@smoke"})
        assert r.status_code in (401, 403)

    def test_start_run_with_custom_params(self, client):
        headers = _qa_headers(client)
        r = client.post("/api/run-tests", json={
            "tags": "@login",
            "runner": "LoginTestRunner",
            "suite_name": "Login Suite",
        }, headers=headers)
        assert r.status_code == 201
        data = r.json()
        assert data["tags"] == "@login"
        assert data["runner"] == "LoginTestRunner"
        assert data["suite_name"] == "Login Suite"

    @patch("subprocess.Popen")
    def test_execute_maven_run_success(self, mock_popen, client):
        mock_process = MagicMock()
        mock_process.stdout.readline.side_effect = ["line 1\n", "line 2\n", ""]
        mock_process.wait.return_value = 0
        mock_popen.return_value = mock_process

        headers = _qa_headers(client)
        r = client.post("/api/run-tests", json={"tags": "@smoke"}, headers=headers)
        run_id = r.json()["run_id"]
        time.sleep(0.5)

        with dashboard_api._test_runs_lock:
            run = dashboard_api._test_runs[run_id]
        assert run["exit_code"] == 0
        assert run["status"] == "completed"
        assert any("line 1" in log for log in run["logs"])
        assert any("mvn" in log for log in run["logs"])

    @patch("subprocess.Popen")
    def test_execute_maven_run_failure(self, mock_popen, client):
        mock_process = MagicMock()
        mock_process.stdout.readline.side_effect = ["error line\n", ""]
        mock_process.wait.return_value = 1
        mock_popen.return_value = mock_process

        headers = _qa_headers(client)
        r = client.post("/api/run-tests", json={"tags": "@smoke"}, headers=headers)
        run_id = r.json()["run_id"]
        time.sleep(0.5)

        with dashboard_api._test_runs_lock:
            run = dashboard_api._test_runs[run_id]
        assert run["exit_code"] == 1
        assert run["status"] == "failed"

    @patch("subprocess.Popen")
    @patch("pathlib.Path.exists")
    @patch("pathlib.Path.stat")
    def test_execute_maven_run_with_rerun(self, mock_stat, mock_exists, mock_popen, client):
        mock_exists.return_value = True
        mock_stat.return_value.st_size = 42
        mock_process = MagicMock()
        mock_process.stdout.readline.side_effect = ["primary\n", "rerun\n", ""]
        mock_process.wait.return_value = 0
        mock_popen.return_value = mock_process

        headers = _qa_headers(client)
        r = client.post("/api/run-tests", json={"tags": "@smoke"}, headers=headers)
        run_id = r.json()["run_id"]
        time.sleep(0.5)

        with dashboard_api._test_runs_lock:
            run = dashboard_api._test_runs[run_id]
        assert run["status"] == "completed"
        assert mock_popen.call_count >= 2

    def test_create_run_unique_ids(self, client):
        headers = _qa_headers(client)
        r1 = client.post("/api/run-tests", json={"tags": "@smoke"}, headers=headers)
        r2 = client.post("/api/run-tests", json={"tags": "@smoke"}, headers=headers)
        assert r1.json()["run_id"] != r2.json()["run_id"]


# ── Tests du endpoint GET /api/run-tests/{run_id} ───────────────────────────────

class TestGetTestRun:
    def setup_method(self):
        dashboard_api._test_runs.clear()
        _clear_test_runs_db()

    def test_get_existing_run(self, client):
        headers = _qa_headers(client)
        create_r = client.post("/api/run-tests", json={"tags": "@smoke"}, headers=headers)
        run_id = create_r.json()["run_id"]
        r = client.get(f"/api/run-tests/{run_id}", headers=headers)
        assert r.status_code == 200
        assert r.json()["run_id"] == run_id

    def test_get_nonexistent_run(self, client):
        headers = _qa_headers(client)
        r = client.get("/api/run-tests/nonexistent", headers=headers)
        assert r.status_code == 404

    def test_get_run_denied_pm(self, client):
        headers = _qa_headers(client)
        create_r = client.post("/api/run-tests", json={"tags": "@smoke"}, headers=headers)
        run_id = create_r.json()["run_id"]
        pm_headers = _pm_headers(client)
        r = client.get(f"/api/run-tests/{run_id}", headers=pm_headers)
        assert r.status_code == 403


# ── Tests du endpoint GET /api/run-tests (liste) ────────────────────────────────

class TestListTestRuns:
    def setup_method(self):
        dashboard_api._test_runs.clear()
        _clear_test_runs_db()

    def test_list_empty(self, client):
        headers = _qa_headers(client)
        r = client.get("/api/run-tests", headers=headers)
        assert r.status_code == 200
        assert r.json() == []

    def test_list_returns_runs(self, client):
        headers = _qa_headers(client)
        client.post("/api/run-tests", json={"tags": "@smoke"}, headers=headers)
        client.post("/api/run-tests", json={"tags": "@regression"}, headers=headers)
        r = client.get("/api/run-tests", headers=headers)
        assert r.status_code == 200
        data = r.json()
        assert len(data) == 2

    def test_list_respects_limit(self, client):
        headers = _qa_headers(client)
        for _ in range(5):
            client.post("/api/run-tests", json={"tags": "@smoke"}, headers=headers)
        r = client.get("/api/run-tests?limit=3", headers=headers)
        assert len(r.json()) == 3

    def test_list_denied_pm(self, client):
        headers = _pm_headers(client)
        r = client.get("/api/run-tests", headers=headers)
        assert r.status_code == 403

    def test_list_ordered_by_created_at_desc(self, client):
        headers = _qa_headers(client)
        r1 = client.post("/api/run-tests", json={"tags": "@smoke"}, headers=headers)
        r2 = client.post("/api/run-tests", json={"tags": "@regression"}, headers=headers)
        r = client.get("/api/run-tests", headers=headers)
        data = r.json()
        assert data[0]["run_id"] == r2.json()["run_id"]
        assert data[1]["run_id"] == r1.json()["run_id"]


# ── Tests de l'exception dans _execute_maven_run ────────────────────────────────

class TestExecuteMavenRunException:
    def setup_method(self):
        dashboard_api._test_runs.clear()
        _clear_test_runs_db()

    @patch("subprocess.Popen", side_effect=Exception("Maven not found"))
    def test_execute_maven_run_exception(self, mock_popen, client):
        headers = _qa_headers(client)
        r = client.post("/api/run-tests", json={"tags": "@smoke"}, headers=headers)
        run_id = r.json()["run_id"]
        time.sleep(0.5)
        with dashboard_api._test_runs_lock:
            run = dashboard_api._test_runs[run_id]
        assert run["status"] == "failed"
        assert "Maven not found" in run["error"]
