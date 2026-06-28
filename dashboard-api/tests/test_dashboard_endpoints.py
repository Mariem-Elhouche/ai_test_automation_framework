"""
Tests unitaires pour les endpoints de visualisation du dashboard.
"""
import os

os.environ["DATABASE_URL"] = "sqlite:///./src/test_dashboard.db"
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
os.environ["RERUN_ENABLED"] = "true"

import pytest
from fastapi.testclient import TestClient

from src import dashboard_api

app = dashboard_api.app

DB_PATH = "./src/test_dashboard.db"


@pytest.fixture(scope="module")
def client():
    with TestClient(app) as c:
        yield c
    try:
        os.remove(DB_PATH)
    except (FileNotFoundError, PermissionError):
        pass


def _login(client, email, password):
    r = client.post("/auth/login", json={"email": email, "password": password})
    assert r.status_code == 200
    return r.json()["access_token"]


def _auth_headers(client, email, password):
    return {"Authorization": f"Bearer {_login(client, email, password)}"}


TEST_RUN_ID = "run-20250610-143022"


def _admin_headers(client):
    return _auth_headers(client, "admin@test.local", "admin123")


@pytest.fixture(scope="module")
def seeded_data(client):
    headers = _admin_headers(client)

    scenarios = [
        {"feature_name": "Création entreprise", "scenario": "Catégorie valide", "status": "passed", "duration_ns": 1_500_000_000, "tags": "@companies,@positive"},
        {"feature_name": "Création entreprise", "scenario": "SIRET invalide", "status": "passed", "duration_ns": 2_100_000_000, "tags": "@companies,@negative"},
        {"feature_name": "Gestion accès", "scenario": "Modification rôle sans droit", "status": "failed", "duration_ns": 800_000_000, "tags": "@access,@negative"},
        {"feature_name": "Gestion accès", "scenario": "Ajout utilisateur", "status": "passed", "duration_ns": 1_200_000_000, "tags": "@access,@positive"},
        {"feature_name": "Recherche entreprise", "scenario": "Recherche par SIRET", "status": "skipped", "duration_ns": 0, "tags": "@search"},
        {"feature_name": "Création entreprise", "scenario": "Doublon catégorie", "status": "passed", "duration_ns": 1_800_000_000, "tags": "@companies,@negative"},
        {"feature_name": "Gestion accès", "scenario": "Suppression utilisateur", "status": "flaky", "duration_ns": 3_200_000_000, "tags": "@access,@positive"},
    ]
    r = client.post("/api/cucumber-runs", json={"run_id": TEST_RUN_ID, "scenarios": scenarios}, headers=headers)
    assert r.status_code == 201

    healing_events = [
        {
            "scenario_name": "Catégorie valide", "old_locator_type": "id", "old_locator_val": "btn-submit-old",
            "success": True, "score": 0.92, "structural_score": 0.88, "semantic_score": 0.95,
            "new_locator_type": "css", "new_locator_val": ".submit-btn", "healing_time_ms": 342,
            "baseline_hit": False, "elements_extracted": 45, "after_struct_filter": 22,
            "after_spatial_filter": 15, "sent_to_nlp": 8, "exception_type": "NoSuchElementException",
            "run_id": TEST_RUN_ID,
        },
        {
            "scenario_name": "Modification rôle sans droit", "old_locator_type": "xpath", "old_locator_val": "//div[@class='role-select']",
            "success": True, "score": 0.78, "structural_score": 0.85, "semantic_score": 0.70,
            "new_locator_type": "xpath", "new_locator_val": "//div[contains(@class,'role')]//select",
            "healing_time_ms": 521, "baseline_hit": True, "elements_extracted": 32,
            "after_struct_filter": 18, "after_spatial_filter": 10, "sent_to_nlp": 5,
            "exception_type": "StaleElementReferenceException", "run_id": TEST_RUN_ID,
        },
        {
            "scenario_name": "Ajout utilisateur", "old_locator_type": "id", "old_locator_val": "email-input",
            "success": False, "score": 0.45, "structural_score": 0.60, "semantic_score": 0.30,
            "healing_time_ms": 0, "baseline_hit": False, "elements_extracted": 28,
            "after_struct_filter": 14, "after_spatial_filter": 9, "sent_to_nlp": 6,
            "exception_type": "ElementNotInteractableException", "run_id": TEST_RUN_ID,
        },
    ]
    for event in healing_events:
        r = client.post("/api/healing-events", json=event, headers=headers)
        assert r.status_code == 201

    return TEST_RUN_ID


class TestDashboardStructure:
    def test_dashboard_structure_admin(self, client, seeded_data):
        headers = _auth_headers(client, "admin@test.local", "admin123")
        r = client.get(f"/api/dashboard?run_id={seeded_data}", headers=headers)
        assert r.status_code == 200
        data = r.json()

        assert data["run_id"] == seeded_data

        cucumber = data["cucumber"]
        assert cucumber["total"] >= 7
        assert cucumber["passed"] >= 4
        assert cucumber["failed"] >= 1
        assert cucumber["skipped"] >= 1
        assert cucumber["flaky"] >= 1
        assert cucumber["success_rate"] > 0
        assert cucumber["avg_execution_time_ms"] is not None

        assert "metrics" in data
        assert data["metrics"] is not None
        assert data["metrics"]["total_healing_requests"] >= 3
        assert data["metrics"]["successful_healings"] >= 2
        assert data["metrics"]["failed_healings"] >= 1
        assert data["metrics"]["healing_rate"] > 0
        assert data["metrics"]["avg_final_score"] is not None
        assert data["metrics"]["avg_structural_score"] is not None
        assert data["metrics"]["avg_semantic_score"] is not None
        assert data["metrics"]["avg_healing_time_ms"] is not None
        assert data["metrics"]["nlp_filter_efficiency"] > 0
        assert data["metrics"]["baseline_hit_rate"] is not None

        assert isinstance(data["metrics_history"], list)
        assert len(data["metrics_history"]) >= 1

        assert isinstance(data["recent_events"], list)
        assert len(data["recent_events"]) >= 1
        assert "healing_source" in data

    def test_dashboard_no_run_id_resolves_latest(self, client, seeded_data):
        headers = _auth_headers(client, "pm@test.local", "pm123")
        r = client.get("/api/dashboard", headers=headers)
        assert r.status_code == 200
        data = r.json()
        assert data["run_id"] == seeded_data

    def test_full_response_keys_admin(self, client, seeded_data):
        headers = _auth_headers(client, "admin@test.local", "admin123")
        r = client.get(f"/api/dashboard?run_id={seeded_data}", headers=headers)
        assert r.status_code == 200
        data = r.json()

        expected_keys = {"run_id", "metrics", "metrics_history", "recent_events", "healing_source", "cucumber"}
        assert set(data.keys()) == expected_keys

        expected_cucumber_keys = {"passed", "flaky", "failed", "skipped", "total", "avg_execution_time_ms", "success_rate"}
        assert set(data["cucumber"].keys()) == expected_cucumber_keys

    def test_metrics_history_present_for_pm(self, client, seeded_data):
        headers = _auth_headers(client, "pm@test.local", "pm123")
        r = client.get(f"/api/dashboard?run_id={seeded_data}", headers=headers)
        data = r.json()
        assert len(data["metrics_history"]) > 0

    def test_metrics_history_empty_for_qa(self, client, seeded_data):
        headers = _auth_headers(client, "qa@test.local", "qa123")
        r = client.get(f"/api/dashboard?run_id={seeded_data}", headers=headers)
        data = r.json()
        assert data["metrics_history"] == []

    def test_recent_events_present_for_qa(self, client, seeded_data):
        headers = _auth_headers(client, "qa@test.local", "qa123")
        r = client.get(f"/api/dashboard?run_id={seeded_data}", headers=headers)
        data = r.json()
        assert len(data["recent_events"]) > 0
        assert data["healing_source"] != "unavailable"

    def test_recent_events_empty_for_pm(self, client, seeded_data):
        headers = _auth_headers(client, "pm@test.local", "pm123")
        r = client.get(f"/api/dashboard?run_id={seeded_data}", headers=headers)
        data = r.json()
        assert data["recent_events"] == []
        assert data["healing_source"] == "unavailable"

    def test_dashboard_unauthorized(self, client):
        r = client.get("/api/dashboard")
        assert r.status_code == 401

    def test_dashboard_empty_cucumber_for_unknown_run(self, client):
        headers = _auth_headers(client, "pm@test.local", "pm123")
        r = client.get("/api/dashboard?run_id=nonexistent-run", headers=headers)
        assert r.status_code == 200
        data = r.json()
        assert data["run_id"] == "nonexistent-run"
        assert data["cucumber"]["total"] == 0
        assert data["cucumber"]["passed"] == 0
        assert data["cucumber"]["failed"] == 0
        assert data["cucumber"]["success_rate"] == 0.0
