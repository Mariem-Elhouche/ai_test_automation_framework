from __future__ import annotations

import json
from datetime import datetime
from typing import Any, Optional

import sqlalchemy


class TestRunRepository:
    def __init__(self, engine: sqlalchemy.Engine, table: sqlalchemy.Table):
        self._engine = engine
        self._table = table

    def upsert(self, run_data: dict[str, Any]) -> None:
        data = dict(run_data)
        data["logs"] = json.dumps(data.get("logs", []))
        if isinstance(data.get("created_at"), str):
            data["created_at"] = datetime.fromisoformat(data["created_at"])
        if isinstance(data.get("completed_at"), str):
            data["completed_at"] = datetime.fromisoformat(data["completed_at"]) if data["completed_at"] else None
        with self._engine.begin() as conn:
            existing = conn.execute(
                sqlalchemy.select(self._table.c.run_id)
                .where(self._table.c.run_id == data["run_id"])
            ).first()
            if existing:
                conn.execute(
                    self._table.update()
                    .where(self._table.c.run_id == data["run_id"])
                    .values(**data)
                )
            else:
                conn.execute(
                    self._table.insert().values(**data)
                )

    def get(self, run_id: str) -> Optional[dict]:
        with self._engine.connect() as conn:
            row = conn.execute(
                sqlalchemy.select(self._table)
                .where(self._table.c.run_id == run_id)
            ).first()
        if not row:
            return None
        d = dict(row._mapping)
        if isinstance(d.get("logs"), str):
            try:
                d["logs"] = json.loads(d["logs"])
            except (json.JSONDecodeError, TypeError):
                d["logs"] = []
        return self._normalize_dt(d)

    def list(self, limit: int, offset: int = 0) -> list[dict]:
        with self._engine.connect() as conn:
            rows = conn.execute(
                sqlalchemy.select(self._table)
                .order_by(self._table.c.created_at.desc())
                .limit(limit)
                .offset(offset)
            ).fetchall()
        result = []
        for row in rows:
            d = dict(row._mapping)
            if isinstance(d.get("logs"), str):
                try:
                    d["logs"] = json.loads(d["logs"])
                except (json.JSONDecodeError, TypeError):
                    d["logs"] = []
            result.append(self._normalize_dt(d))
        return result

    def delete_by_run_id(self, run_id: str) -> None:
        with self._engine.begin() as conn:
            conn.execute(
                self._table.delete().where(self._table.c.run_id == run_id)
            )

    @staticmethod
    def _normalize_dt(d: dict) -> dict:
        result = dict(d)
        if isinstance(result.get("created_at"), datetime):
            result["created_at"] = result["created_at"].isoformat()
        if isinstance(result.get("completed_at"), datetime):
            result["completed_at"] = result["completed_at"].isoformat() if result["completed_at"] else None
        return result
