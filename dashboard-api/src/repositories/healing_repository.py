from __future__ import annotations

from datetime import datetime, timezone
from typing import Optional

import sqlalchemy
from databases import Database


class HealingEventRepository:
    def __init__(self, database: Database, table: sqlalchemy.Table):
        self.database = database
        self.table = table

    async def create(self, event_data: dict) -> int:
        event_data["run_id"] = event_data.get("run_id")
        query = self.table.insert().values(
            created_at=datetime.now(timezone.utc),
            **event_data
        )
        return await self.database.execute(query)

    async def list(self, limit: int, offset: int, run_id: Optional[str] = None) -> list[dict]:
        query = self.table.select()
        if run_id:
            query = query.where(self.table.c.run_id == run_id)
        query = query.order_by(self.table.c.created_at.desc()).limit(limit).offset(offset)
        rows = await self.database.fetch_all(query)
        return [dict(r) for r in rows]

    async def fetch_all(self) -> list[dict]:
        rows = await self.database.fetch_all(self.table.select())
        return [dict(r) for r in rows]

    async def fetch_by_run_id(self, run_id: str) -> list[dict]:
        rows = await self.database.fetch_all(
            self.table.select().where(self.table.c.run_id == run_id)
        )
        return [dict(r) for r in rows]

    async def fetch_with_null_run_id(self) -> list[dict]:
        rows = await self.database.fetch_all(
            self.table.select().where(self.table.c.run_id.is_(None))
        )
        return [dict(r) for r in rows]

    async def delete_by_run_id(self, run_id: str) -> None:
        query = self.table.delete().where(self.table.c.run_id == run_id)
        await self.database.execute(query)

    async def fetch_recent(self, run_id: Optional[str], cutoff: Optional[datetime], limit: int) -> list[dict]:
        query = self.table.select()
        filters = []
        if run_id:
            filters.append(self.table.c.run_id == run_id)
        if cutoff:
            filters.append(self.table.c.created_at >= cutoff)
        if filters:
            query = query.where(sqlalchemy.and_(*filters))
        query = query.order_by(self.table.c.created_at.desc()).limit(limit)
        rows = await self.database.fetch_all(query)
        return [dict(r) for r in rows]
