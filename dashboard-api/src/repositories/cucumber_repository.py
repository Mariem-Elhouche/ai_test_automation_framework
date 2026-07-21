from __future__ import annotations

from datetime import datetime
from typing import Optional

import sqlalchemy
from databases import Database


class CucumberRunRepository:
    def __init__(self, database: Database, table: sqlalchemy.Table):
        self.database = database
        self.table = table

    async def create_scenario(
        self, run_at: datetime, feature_name: str, scenario: str,
        status: str, duration_ns: int, tags: str, run_id: Optional[str],
        classification: Optional[str] = None,
    ) -> None:
        query = self.table.insert().values(
            run_at=run_at,
            feature_name=feature_name,
            scenario=scenario,
            status=status,
            duration_ns=duration_ns,
            tags=tags,
            run_id=run_id,
            classification=classification,
        )
        await self.database.execute(query)

    async def get_latest_run_id(self) -> Optional[str]:
        query = (
            sqlalchemy.select(self.table.c.run_id)
            .where(self.table.c.run_id.is_not(None))
            .order_by(self.table.c.run_at.desc())
            .limit(1)
        )
        return await self.database.fetch_val(query)

    async def get_latest_run_at(self, run_id: str) -> Optional[datetime]:
        query = sqlalchemy.select(sqlalchemy.func.max(self.table.c.run_at)).where(
            self.table.c.run_id == run_id
        )
        return await self.database.fetch_val(query)

    async def get_global_latest_run_at(self) -> Optional[datetime]:
        query = sqlalchemy.select(sqlalchemy.func.max(self.table.c.run_at))
        return await self.database.fetch_val(query)

    async def get_counts_by_run_id(self, run_id: str) -> list[dict]:
        query = (
            sqlalchemy.select(self.table.c.status, sqlalchemy.func.count().label("cnt"))
            .where(self.table.c.run_id == run_id)
            .group_by(self.table.c.status)
        )
        rows = await self.database.fetch_all(query)
        return [dict(r) for r in rows]

    async def get_avg_duration(self, run_id: str) -> Optional[float]:
        query = sqlalchemy.select(
            sqlalchemy.func.avg(self.table.c.duration_ns).label("avg_ns")
        ).where(self.table.c.run_id == run_id)
        row = await self.database.fetch_one(query)
        return row[0] if row and row[0] else None

    async def delete_by_run_id(self, run_id: str) -> None:
        query = self.table.delete().where(self.table.c.run_id == run_id)
        await self.database.execute(query)

    async def get_recent(self, run_id: Optional[str] = None, limit: int = 20) -> list[dict]:
        query = self.table.select()
        if run_id:
            query = query.where(self.table.c.run_id == run_id)
        query = query.order_by(self.table.c.run_at.desc()).limit(limit)
        rows = await self.database.fetch_all(query)
        return [dict(r) for r in rows]
