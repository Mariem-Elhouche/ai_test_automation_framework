from __future__ import annotations

from typing import TYPE_CHECKING, Any, Optional

from fastapi import Depends, Header, HTTPException, Security, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer

from src.core.config import settings

if TYPE_CHECKING:
    from src.services.auth_service import AuthService

AUTH_ENABLED = bool(settings.DASHBOARD_API_KEY)
API_KEY = settings.DASHBOARD_API_KEY
http_bearer = HTTPBearer(auto_error=False)


def _raise_unauthorized(detail: str = "Unauthorized") -> None:
    raise HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail=detail,
        headers={"WWW-Authenticate": "Bearer"},
    )


def create_dependency_getter(auth_service: AuthService):
    def get_current_actor(
        credentials: Optional[HTTPAuthorizationCredentials] = Security(http_bearer),
    ) -> dict[str, Any]:
        if credentials is None or credentials.scheme.lower() != "bearer":
            _raise_unauthorized("Missing bearer token")
        return auth_service.decode_access_token(credentials.credentials)

    return get_current_actor


def make_role_checker(get_current_actor):
    def require_role(allowed_roles: set[str]):
        def _checker(actor: dict[str, Any] = Depends(get_current_actor)) -> dict[str, Any]:
            role = str(actor.get("role", ""))
            if role not in allowed_roles:
                raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Forbidden")
            return actor
        return _checker
    return require_role


def make_require_ingest_auth(auth_service: AuthService, get_current_actor):
    def require_ingest_auth(
        x_api_key: Optional[str] = Header(default=None, alias="X-API-Key"),
        credentials: Optional[HTTPAuthorizationCredentials] = Security(http_bearer),
    ) -> dict[str, Any]:
        if AUTH_ENABLED and x_api_key == API_KEY:
            return {"auth_type": "api_key"}

        if credentials is not None and credentials.scheme.lower() == "bearer":
            actor = auth_service.decode_access_token(credentials.credentials)
            if actor.get("role") in ("admin", "project_manager", "qa_engineer"):
                return actor

        _raise_unauthorized("Invalid credentials")

    return require_ingest_auth
