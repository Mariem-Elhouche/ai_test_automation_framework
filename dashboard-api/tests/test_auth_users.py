"""
Tests unitaires pour l'authentification et la gestion des utilisateurs.
"""
import os
import time

os.environ["DATABASE_URL"] = "sqlite:///./src/test_auth.db"
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

DB_PATH = "./src/test_auth.db"
_PATCHED = False


@pytest.fixture(scope="session")
def client():
    global _PATCHED
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


def _auth_headers(client, email, password):
    return {"Authorization": f"Bearer {_login(client, email, password)}"}


# ── Authentification ────────────────────────────────────────────────────────────

class TestLogin:
    def test_admin_login(self, client):
        r = client.post("/auth/login", json={"email": "admin@test.local", "password": "admin123"})
        assert r.status_code == 200
        d = r.json()
        assert d["role"] == "admin"
        assert d["display_name"] == "Test Admin"
        assert "access_token" in d

    def test_pm_login(self, client):
        r = client.post("/auth/login", json={"email": "pm@test.local", "password": "pm123"})
        assert r.status_code == 200
        assert r.json()["role"] == "project_manager"

    def test_qa_login(self, client):
        r = client.post("/auth/login", json={"email": "qa@test.local", "password": "qa123"})
        assert r.status_code == 200
        assert r.json()["role"] == "qa_engineer"

    def test_invalid_email(self, client):
        r = client.post("/auth/login", json={"email": "unknown@test.local", "password": "x"})
        assert r.status_code == 401

    def test_wrong_password(self, client):
        r = client.post("/auth/login", json={"email": "admin@test.local", "password": "wrong"})
        assert r.status_code == 401

    def test_auth_me(self, client):
        headers = _auth_headers(client, "admin@test.local", "admin123")
        r = client.get("/auth/me", headers=headers)
        assert r.status_code == 200
        assert r.json()["role"] == "admin"

    def test_auth_me_no_token(self, client):
        r = client.get("/auth/me")
        assert r.status_code == 401


# ── Gestion des utilisateurs (admin uniquement) ─────────────────────────────────

class TestUserCRUD:
    def test_list_users_as_admin(self, client):
        headers = _auth_headers(client, "admin@test.local", "admin123")
        r = client.get("/api/users", headers=headers)
        assert r.status_code == 200
        users = r.json()
        emails = [u["email"] for u in users]
        assert "admin@test.local" in emails
        assert "pm@test.local" in emails
        assert "qa@test.local" in emails
        for u in users:
            assert "password_hash" not in u

    def test_list_users_forbidden_pm(self, client):
        headers = _auth_headers(client, "pm@test.local", "pm123")
        r = client.get("/api/users", headers=headers)
        assert r.status_code == 403

    def test_list_users_forbidden_qa(self, client):
        headers = _auth_headers(client, "qa@test.local", "qa123")
        r = client.get("/api/users", headers=headers)
        assert r.status_code == 403

    def test_create_user(self, client):
        headers = _auth_headers(client, "admin@test.local", "admin123")
        r = client.post("/api/users", json={
            "email": "new@test.local",
            "password": "pass1234",
            "display_name": "New User",
            "role": "qa_engineer",
        }, headers=headers)
        assert r.status_code == 201
        assert r.json()["email"] == "new@test.local"
        assert r.json()["role"] == "qa_engineer"

    def test_create_duplicate_email(self, client):
        headers = _auth_headers(client, "admin@test.local", "admin123")
        r = client.post("/api/users", json={
            "email": "new@test.local",
            "password": "other",
            "display_name": "Dup",
            "role": "project_manager",
        }, headers=headers)
        assert r.status_code == 409

    def test_create_user_forbidden_pm(self, client):
        headers = _auth_headers(client, "pm@test.local", "pm123")
        r = client.post("/api/users", json={
            "email": "shouldfail@test.local",
            "password": "test",
            "display_name": "Fail",
            "role": "qa_engineer",
        }, headers=headers)
        assert r.status_code == 403

    def test_create_invalid_role(self, client):
        headers = _auth_headers(client, "admin@test.local", "admin123")
        r = client.post("/api/users", json={
            "email": "badrole@test.local",
            "password": "test",
            "display_name": "Bad Role",
            "role": "super_admin",
        }, headers=headers)
        assert r.status_code == 422

    def test_update_user(self, client):
        headers = _auth_headers(client, "admin@test.local", "admin123")
        r = client.put("/api/users/4", json={
            "display_name": "Updated Name",
            "role": "project_manager",
        }, headers=headers)
        assert r.status_code == 200
        assert r.json()["display_name"] == "Updated Name"
        assert r.json()["role"] == "project_manager"

    def test_update_nonexistent_user(self, client):
        headers = _auth_headers(client, "admin@test.local", "admin123")
        r = client.put("/api/users/999", json={"display_name": "Ghost"}, headers=headers)
        assert r.status_code == 404

    def test_delete_created_user(self, client):
        headers = _auth_headers(client, "admin@test.local", "admin123")
        r = client.delete("/api/users/4", headers=headers)
        assert r.status_code == 204

    def test_delete_nonexistent(self, client):
        headers = _auth_headers(client, "admin@test.local", "admin123")
        r = client.delete("/api/users/999", headers=headers)
        assert r.status_code == 404

    def test_change_password(self, client):
        headers = _auth_headers(client, "admin@test.local", "admin123")
        r = client.put("/api/users/1/password", json={"new_password": "newadmin456"}, headers=headers)
        assert r.status_code == 200
        r2 = client.post("/auth/login", json={"email": "admin@test.local", "password": "newadmin456"})
        assert r2.status_code == 200
        client.put("/api/users/1/password", json={"new_password": "admin123"}, headers=headers)

    def test_change_password_nonexistent(self, client):
        headers = _auth_headers(client, "admin@test.local", "admin123")
        r = client.put("/api/users/999/password", json={"new_password": "test"}, headers=headers)
        assert r.status_code == 404


# ── Contrôle d'accès par rôle ───────────────────────────────────────────────────

class TestRoleAccess:
    def test_dashboard_pm_allowed(self, client):
        headers = _auth_headers(client, "pm@test.local", "pm123")
        r = client.get("/api/dashboard?run_id=test-run", headers=headers)
        assert r.status_code == 200

    def test_dashboard_qa_allowed(self, client):
        headers = _auth_headers(client, "qa@test.local", "qa123")
        r = client.get("/api/dashboard?run_id=test-run", headers=headers)
        assert r.status_code == 200

    def test_reports_all_roles(self, client):
        for email, pw in [("pm@test.local", "pm123"), ("qa@test.local", "qa123")]:
            headers = _auth_headers(client, email, pw)
            r = client.get("/api/reports", headers=headers)
            assert r.status_code == 200

    def test_healing_events_qa_allowed(self, client):
        headers = _auth_headers(client, "qa@test.local", "qa123")
        r = client.get("/api/healing-events", headers=headers)
        assert r.status_code == 200

    def test_healing_events_forbidden_pm(self, client):
        headers = _auth_headers(client, "pm@test.local", "pm123")
        r = client.get("/api/healing-events", headers=headers)
        assert r.status_code == 403

    def test_run_tests_qa_allowed(self, client):
        headers = _auth_headers(client, "qa@test.local", "qa123")
        r = client.get("/api/run-tests", headers=headers)
        assert r.status_code == 200

    def test_run_tests_forbidden_pm(self, client):
        headers = _auth_headers(client, "pm@test.local", "pm123")
        r = client.get("/api/run-tests", headers=headers)
        assert r.status_code == 403

    def test_dashboard_trends_pm(self, client):
        headers = _auth_headers(client, "pm@test.local", "pm123")
        r = client.get("/api/dashboard?run_id=test-run", headers=headers)
        data = r.json()
        assert "metrics_history" in data

    def test_dashboard_no_trends_qa(self, client):
        headers = _auth_headers(client, "qa@test.local", "qa123")
        r = client.get("/api/dashboard?run_id=test-run", headers=headers)
        data = r.json()
        assert data["metrics_history"] == []
