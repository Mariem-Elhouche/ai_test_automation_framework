import databases
import sqlalchemy

from src.core.config import settings
from src.models.dashboard_model import (
    CucumberRunModel,
    HealingEventModel,
    MetricsSnapshotModel,
    TestRunModel,
)
from src.models.user_model import UserModel

DATABASE_URL = settings.resolved_database_url
database = databases.Database(DATABASE_URL)
metadata = sqlalchemy.MetaData()
engine = sqlalchemy.create_engine(DATABASE_URL)

# Auto-create all tables on SQLite (required for tests that bypass startup)
if "sqlite" in str(engine.url).lower():
    UserModel.metadata.create_all(engine)
    HealingEventModel.metadata.create_all(engine)
    MetricsSnapshotModel.metadata.create_all(engine)
    CucumberRunModel.metadata.create_all(engine)
    TestRunModel.metadata.create_all(engine)

healing_events = HealingEventModel.__table__
metrics_snapshots = MetricsSnapshotModel.__table__
cucumber_runs = CucumberRunModel.__table__
app_users = UserModel.__table__
test_runs = TestRunModel.__table__
