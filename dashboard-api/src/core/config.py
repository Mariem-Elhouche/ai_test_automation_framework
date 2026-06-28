from pathlib import Path
from urllib.parse import quote_plus

from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    # Database
    DATABASE_URL: str = ""
    DB_HOST: str = "localhost"
    DB_PORT: int = 5432
    DB_NAME: str = "ai_test_dashboard"
    DB_USER: str = "postgres"
    DB_PASSWORD: str = "password"
    POSTGRES_DB: str = ""
    POSTGRES_USER: str = ""
    POSTGRES_PASSWORD: str = ""

    # Auth
    DASHBOARD_API_KEY: str = ""
    DASHBOARD_JWT_SECRET: str = "change-this-jwt-secret"
    DASHBOARD_JWT_ALGORITHM: str = "HS256"
    DASHBOARD_JWT_EXPIRE_MINUTES: int = 480

    # Seed users
    DASHBOARD_ADMIN_EMAIL: str = "admin@dashboard.local"
    DASHBOARD_ADMIN_PASSWORD: str = "admin123"
    DASHBOARD_ADMIN_DISPLAY_NAME: str = "Administrator"
    DASHBOARD_PM_EMAIL: str = "chef.projet@dashboard.local"
    DASHBOARD_PM_PASSWORD: str = ""
    DASHBOARD_PM_PASSWORD_HASH: str = ""
    DASHBOARD_PM_DISPLAY_NAME: str = "Chef de projet"
    DASHBOARD_QA_EMAIL: str = "qa.engineer@dashboard.local"
    DASHBOARD_QA_PASSWORD: str = ""
    DASHBOARD_QA_PASSWORD_HASH: str = ""
    DASHBOARD_QA_DISPLAY_NAME: str = "QA Engineer"

    # Healing
    SELF_HEALING_METRICS_URL: str = ""
    SELF_HEALING_METRICS_TIMEOUT_SECONDS: float = 5.0

    # Paths
    REPORTS_DIR: str = "/app/reports"
    MAVEN_PROJECT_DIR: str = "/app/automation-framework"
    MAVEN_CMD: str = "mvn"

    # Backoffice
    BACKOFFICE_URL: str = "https://stg-bo.noveocare.com/login"
    BACKOFFICE_USER_EMAIL: str = ""
    BACKOFFICE_USER_PASSWORD: str = ""

    # Features
    SELF_HEALING_ENABLED: str = "true"
    RERUN_ENABLED: str = "true"
    DASHBOARD_API_KEY_FOR_TESTS: str = ""

    class Config:
        env_file = str(Path(__file__).resolve().parents[2] / ".env")
        env_file_encoding = "utf-8"
        extra = "ignore"

    @property
    def resolved_database_url(self) -> str:
        if self.DATABASE_URL:
            return self.DATABASE_URL
        db_host = self.DB_HOST
        db_port = self.DB_PORT
        db_name = self.DB_NAME or self.POSTGRES_DB or "ai_test_dashboard"
        db_user = self.DB_USER or self.POSTGRES_USER or "postgres"
        db_password = self.DB_PASSWORD or self.POSTGRES_PASSWORD or "password"
        encoded_user = quote_plus(db_user)
        encoded_password = quote_plus(db_password)
        return f"postgresql://{encoded_user}:{encoded_password}@{db_host}:{db_port}/{db_name}"


settings = Settings()
