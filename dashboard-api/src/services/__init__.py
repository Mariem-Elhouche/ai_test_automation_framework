from src.services.auth_service import AuthService
from src.services.user_service import UserService

__all__ = ["AuthService", "UserService", "DashboardService", "TestRunManager"]


def __getattr__(name):
    if name == "DashboardService":
        from src.services.dashboard_service import DashboardService
        return DashboardService
    if name == "TestRunManager":
        from src.services.dashboard_service import TestRunManager
        return TestRunManager
    raise AttributeError(f"module {__name__!r} has no attribute {name!r}")
