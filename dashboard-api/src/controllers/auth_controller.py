
from typing import Any, Callable, Optional

from fastapi import APIRouter, Depends, HTTPException, Security, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer

from src.schemas import LoginRequest
from src.services.auth_service import AuthService
from src.services.user_service import UserService


def create_auth_router(
    auth_service: AuthService,
    user_service: UserService,
    get_current_actor: Callable,
) -> APIRouter:

    router = APIRouter(tags=["auth"])

    @router.post("/auth/login")
    async def login(payload: LoginRequest):
        email = payload.email.strip().lower()
        account = await user_service.find_by_email(email)
        if not account or not auth_service.verify_password(payload.password, account.password_hash or ""):
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid email or password")

        if account.is_active is False:
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Account disabled")

        token = auth_service.create_access_token(account)
        return {
            "access_token": token,
            "token_type": "bearer",
            "expires_in": auth_service.jwt_expire_minutes * 60,
            "role": account.role,
            "display_name": account.display_name,
            "email": account.email,
        }

    @router.get("/auth/me")
    async def me(actor: dict[str, Any] = Depends(get_current_actor)):
        return {
            "email": actor.get("sub"),
            "display_name": actor.get("name"),
            "role": actor.get("role"),
        }

    return router
