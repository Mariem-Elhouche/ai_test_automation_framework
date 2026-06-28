from datetime import datetime
from typing import Optional

from pydantic import BaseModel, field_validator


VALID_ROLES = {"admin", "project_manager", "qa_engineer"}


class UserBase(BaseModel):
    email: str
    display_name: str
    role: str

    @field_validator("role")
    @classmethod
    def validate_role(cls, v: str) -> str:
        if v not in VALID_ROLES:
            raise ValueError(f"Invalid role: {v}")
        return v


class UserCreate(UserBase):
    password: str


class UserUpdate(BaseModel):
    email: Optional[str] = None
    display_name: Optional[str] = None
    role: Optional[str] = None
    is_active: Optional[bool] = None

    @field_validator("role")
    @classmethod
    def validate_role(cls, v: Optional[str]) -> Optional[str]:
        if v is not None and v not in VALID_ROLES:
            raise ValueError(f"Invalid role: {v}")
        return v


class UserPasswordChange(BaseModel):
    new_password: str


class User(BaseModel):
    id: Optional[int] = None
    email: str
    display_name: str
    role: str
    password_hash: Optional[str] = None
    is_active: bool = True
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None


class UserResponse(BaseModel):
    id: Optional[int] = None
    email: str
    display_name: str
    role: str
    is_active: bool = True
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None


class LoginRequest(BaseModel):
    email: str
    password: str
