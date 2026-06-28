from sqlalchemy import BigInteger, Boolean, Column, DateTime, Integer, Numeric, String, Text

from src.models.base import Base


class HealingEventModel(Base):
    __tablename__ = "healing_events"

    id = Column(Integer, primary_key=True)
    created_at = Column(DateTime(timezone=True))
    scenario_name = Column(String)
    old_locator_type = Column(String)
    old_locator_val = Column(Text)
    success = Column(Boolean)
    score = Column(Numeric)
    structural_score = Column(Numeric)
    semantic_score = Column(Numeric)
    new_locator_type = Column(String)
    new_locator_val = Column(Text)
    healing_time_ms = Column(Integer)
    baseline_hit = Column(Boolean)
    elements_extracted = Column(Integer)
    after_struct_filter = Column(Integer)
    after_spatial_filter = Column(Integer)
    sent_to_nlp = Column(Integer)
    error_message = Column(Text)
    exception_type = Column(String(64))
    run_id = Column(String(128))


class MetricsSnapshotModel(Base):
    __tablename__ = "metrics_snapshots"

    id = Column(Integer, primary_key=True)
    captured_at = Column(DateTime(timezone=True))
    total_healing_requests = Column(Integer)
    successful_healings = Column(Integer)
    failed_healings = Column(Integer)
    baseline_hits = Column(Integer)
    total_elements_extracted = Column(Integer)
    total_after_struct = Column(Integer)
    total_after_spatial = Column(Integer)
    total_sent_to_nlp = Column(Integer)
    total_healing_time_ms = Column(BigInteger)
    healing_rate = Column(Numeric)
    baseline_hit_rate = Column(Numeric)
    avg_healing_time_ms = Column(Numeric)
    avg_final_score = Column(Numeric)
    avg_structural_score = Column(Numeric)
    avg_semantic_score = Column(Numeric)
    nlp_filter_efficiency = Column(Numeric)
    run_id = Column(String(128))


class CucumberRunModel(Base):
    __tablename__ = "cucumber_runs"

    id = Column(Integer, primary_key=True)
    run_at = Column(DateTime(timezone=True))
    feature_name = Column(String)
    scenario = Column(String)
    status = Column(String)
    duration_ns = Column(BigInteger)
    tags = Column(Text)
    run_id = Column(String(128))
    classification = Column(String(32))


class TestRunModel(Base):
    __tablename__ = "test_runs"

    run_id = Column(String(128), primary_key=True)
    tags = Column(String)
    runner = Column(String)
    suite_name = Column(String)
    status = Column(String(32))
    created_at = Column(DateTime(timezone=True))
    completed_at = Column(DateTime(timezone=True), nullable=True)
    logs = Column(Text)
    exit_code = Column(Integer, nullable=True)
    error = Column(Text, nullable=True)
