from datetime import datetime, timezone
from typing import Optional

import sqlalchemy
from databases import Database
from fastapi import HTTPException, status

from src.schemas import User


class UserRepository:
    def __init__(self, database: Database, table: sqlalchemy.Table):
        self.database = database
        self.table = table

    async def find_by_email(self, email: str) -> Optional[User]:
        row = await self.database.fetch_one(
            self.table.select().where(self.table.c.email == email)
        )
        if row:
            return User(**dict(row._mapping))
        return None

    async def find_by_id(self, user_id: int) -> Optional[User]:
        row = await self.database.fetch_one(
            self.table.select().where(self.table.c.id == user_id)
        )
        if row:
            return User(**dict(row._mapping))
        return None

    async def create(
        self, email: str, password_hash: str, display_name: str, role: str
    ) -> User:
        now = datetime.now(timezone.utc)
        query = self.table.insert().values(
            email=email,
            password_hash=password_hash,
            display_name=display_name,
            role=role,
            is_active=True,
            created_at=now,
            updated_at=now,
        )
        user_id = await self.database.execute(query)
        return await self.find_by_id(user_id)

    async def update(self, user_id: int, updates: dict) -> User:
        existing = await self.find_by_id(user_id)
        if not existing:
            raise HTTPException(status_code=404, detail="User not found")
        if updates:
            updates["updated_at"] = datetime.now(timezone.utc)
            await self.database.execute(
                self.table.update()
                .where(self.table.c.id == user_id)
                .values(**updates)
            )
        return await self.find_by_id(user_id)

    async def delete(self, user_id: int) -> None:
        existing = await self.find_by_id(user_id)
        if not existing:
            raise HTTPException(status_code=404, detail="User not found")
        await self.database.execute(
            self.table.delete().where(self.table.c.id == user_id)
        )

    async def change_password(self, user_id: int, password_hash: str) -> None:
        existing = await self.find_by_id(user_id)
        if not existing:
            raise HTTPException(status_code=404, detail="User not found")
        await self.database.execute(
            self.table.update()
            .where(self.table.c.id == user_id)
            .values(
                password_hash=password_hash,
                updated_at=datetime.now(timezone.utc),
            )
        )

    async def list_all(self) -> list[User]:
        rows = await self.database.fetch_all(
            self.table.select().order_by(self.table.c.created_at.asc())
        )
        return [User(**dict(r._mapping)) for r in rows]
