from typing import Optional

from fastapi import HTTPException, status

from src.repositories.user_repository import UserRepository
from src.schemas import User, UserCreate
from src.services.auth_service import AuthService


class UserService:
    def __init__(self, repository: UserRepository, auth_service: AuthService):
        self.repository = repository
        self.auth_service = auth_service

    async def find_by_email(self, email: str) -> Optional[User]:
        return await self.repository.find_by_email(email)

    async def create_user(self, data: UserCreate) -> User:
        existing = await self.repository.find_by_email(data.email)
        if existing:
            raise HTTPException(status_code=409, detail="Email already exists")
        return await self.repository.create(
            email=data.email.strip().lower(),
            password_hash=self.auth_service.hash_password(data.password),
            display_name=data.display_name.strip(),
            role=data.role,
        )

    async def update_user(self, user_id: int, updates: dict) -> User:
        return await self.repository.update(user_id, updates)

    async def delete_user(self, user_id: int) -> None:
        await self.repository.delete(user_id)

    async def change_password(self, user_id: int, new_password: str) -> None:
        await self.repository.change_password(
            user_id,
            self.auth_service.hash_password(new_password),
        )
