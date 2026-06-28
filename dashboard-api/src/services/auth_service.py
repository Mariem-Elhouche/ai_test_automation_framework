from datetime import datetime, timedelta, timezone
from typing import Any

from fastapi import HTTPException, status
from jose import JWTError, jwt
from passlib.context import CryptContext

from src.schemas import User


class AuthService:
    def __init__(self, jwt_secret: str, jwt_algorithm: str, jwt_expire_minutes: int):
        self.pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
        self.jwt_secret = jwt_secret
        self.jwt_algorithm = jwt_algorithm
        self.jwt_expire_minutes = jwt_expire_minutes

    def hash_password(self, plain: str) -> str:
        return self.pwd_context.hash(plain)

    def verify_password(self, plain_password: str, password_hash: str) -> bool:
        try:
            return self.pwd_context.verify(plain_password, password_hash)
        except Exception:
            return False

    def create_access_token(self, account: User) -> str:
        now = datetime.now(timezone.utc)
        expire_at = now + timedelta(minutes=self.jwt_expire_minutes)
        payload = {
            "sub": account.email,
            "name": account.display_name or "",
            "role": account.role,
            "iat": int(now.timestamp()),
            "exp": int(expire_at.timestamp()),
        }
        if account.id:
            payload["user_id"] = account.id
        return jwt.encode(payload, self.jwt_secret, algorithm=self.jwt_algorithm)

    def decode_access_token(self, token: str) -> dict[str, Any]:
        try:
            payload = jwt.decode(token, self.jwt_secret, algorithms=[self.jwt_algorithm])
        except JWTError as exc:
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid or expired token") from exc
        subject = payload.get("sub")
        role = payload.get("role")
        if not isinstance(subject, str) or not isinstance(role, str):
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token payload")
        return payload
