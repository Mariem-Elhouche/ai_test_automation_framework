from typing import Any

from fastapi import APIRouter, Depends

from src.schemas import UserCreate, UserPasswordChange, UserResponse, UserUpdate
from src.services.user_service import UserService


def create_user_router(user_service: UserService, require_admin: Any) -> APIRouter:
    router = APIRouter(tags=["users"])

    @router.get("/api/users", response_model=list[UserResponse])
    async def list_users(_admin: dict[str, Any] = Depends(require_admin)):
        users = await user_service.repository.list_all()
        return [UserResponse(**u.model_dump()) for u in users]

    @router.post("/api/users", status_code=201, response_model=UserResponse)
    async def create_user(user: UserCreate, _admin: dict[str, Any] = Depends(require_admin)):
        created = await user_service.create_user(user)
        return UserResponse(**created.model_dump())

    @router.put("/api/users/{user_id}", response_model=UserResponse)
    async def update_user(user_id: int, user: UserUpdate, _admin: dict[str, Any] = Depends(require_admin)):
        updates = {}
        if user.email is not None:
            updates["email"] = user.email.strip().lower()
        if user.display_name is not None:
            updates["display_name"] = user.display_name.strip()
        if user.role is not None:
            updates["role"] = user.role
        if user.is_active is not None:
            updates["is_active"] = user.is_active
        updated = await user_service.update_user(user_id, updates)
        return UserResponse(**updated.model_dump())

    @router.delete("/api/users/{user_id}", status_code=204)
    async def delete_user(user_id: int, _admin: dict[str, Any] = Depends(require_admin)):
        await user_service.delete_user(user_id)

    @router.put("/api/users/{user_id}/password")
    async def change_password(user_id: int, body: UserPasswordChange, _admin: dict[str, Any] = Depends(require_admin)):
        await user_service.change_password(user_id, body.new_password)
        return {"detail": "Password updated"}

    return router
