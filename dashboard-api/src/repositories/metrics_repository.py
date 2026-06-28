from __future__ import annotations

from datetime import datetime, timezone
from typing import Optional

import sqlalchemy
from databases import Database


class MetricsSnapshotRepository:
    def __init__(self, database: Database, table: sqlalchemy.Table):
        self.database = database
        self.table = table

    async def create(self, snapshot_data: dict) -> int:
        snapshot_data["run_id"] = snapshot_data.get("run_id")
        query = self.table.insert().values(
            captured_at=datetime.now(timezone.utc),
            **snapshot_data
        )
        return await self.database.execute(query)

    async def get_latest(self, run_id: Optional[str] = None) -> Optional[dict]:
        query = self.table.select()
        if run_id:
            query = query.where(self.table.c.run_id == run_id)
        query = query.order_by(self.table.c.captured_at.desc()).limit(1)
        row = await self.database.fetch_one(query)
        return dict(row) if row else None

    async def get_history(self, run_id: Optional[str] = None, limit: int = 50) -> list[dict]:
        query = self.table.select()
        if run_id:
            query = query.where(self.table.c.run_id == run_id)
        query = query.order_by(self.table.c.captured_at.asc()).limit(limit)
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
