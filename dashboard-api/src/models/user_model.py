from datetime import datetime
from typing import Optional

from sqlalchemy import Boolean, Column, DateTime, Integer, String

from src.models.base import Base


class UserModel(Base):
    __tablename__ = "app_users"

    id = Column(Integer, primary_key=True)
    email = Column(String(255), unique=True, nullable=False)
    password_hash = Column(String(255), nullable=True)
    display_name = Column(String(255), nullable=False)
    role = Column(String(32), nullable=False)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime(timezone=True))
    updated_at = Column(DateTime(timezone=True))

    def to_pydantic(self) -> "User":
        from src.schemas import User

        return User(
            id=self.id,
            email=self.email,
            display_name=self.display_name,
            role=self.role,
            password_hash=self.password_hash,
            is_active=self.is_active,
            created_at=self.created_at,
            updated_at=self.updated_at,
        )

    @classmethod
    def from_row(cls, row: dict) -> Optional["UserModel"]:
        if not row:
            return None
        return cls(
            id=row.get("id"),
            email=row.get("email"),
            password_hash=row.get("password_hash"),
            display_name=row.get("display_name"),
            role=row.get("role"),
            is_active=row.get("is_active", True),
            created_at=row.get("created_at"),
            updated_at=row.get("updated_at"),
        )
