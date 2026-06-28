from datetime import datetime
from typing import Any, Optional

from pydantic import BaseModel


class HealingEventIn(BaseModel):
    scenario_name: Optional[str] = None
    old_locator_type: Optional[str] = None
    old_locator_val: Optional[str] = None
    success: bool
    score: Optional[float] = None
    structural_score: Optional[float] = None
    semantic_score: Optional[float] = None
    new_locator_type: Optional[str] = None
    new_locator_val: Optional[str] = None
    healing_time_ms: Optional[int] = None
    baseline_hit: bool = False
    elements_extracted: Optional[int] = None
    after_struct_filter: Optional[int] = None
    after_spatial_filter: Optional[int] = None
    sent_to_nlp: Optional[int] = None
    error_message: Optional[str] = None
    exception_type: Optional[str] = None
    run_id: Optional[str] = None


class MetricsSnapshotIn(BaseModel):
    total_healing_requests: int = 0
    successful_healings: int = 0
    failed_healings: int = 0
    baseline_hits: int = 0
    total_elements_extracted: int = 0
    total_after_struct: int = 0
    total_after_spatial: int = 0
    total_sent_to_nlp: int = 0
    total_healing_time_ms: int = 0
    healing_rate: Optional[float] = None
    baseline_hit_rate: Optional[float] = None
    avg_healing_time_ms: Optional[float] = None
    avg_final_score: Optional[float] = None
    avg_structural_score: Optional[float] = None
    avg_semantic_score: Optional[float] = None
    nlp_filter_efficiency: Optional[float] = None
    run_id: Optional[str] = None


class CucumberScenario(BaseModel):
    feature_name: str
    scenario: str
    status: str
    duration_ns: Optional[int] = None
    tags: Optional[str] = None
    run_id: Optional[str] = None
    classification: Optional[str] = None


class CucumberRunIn(BaseModel):
    scenarios: list[CucumberScenario]
    run_id: Optional[str] = None


class TestRunRequest(BaseModel):
    tags: str = "@regression"
    runner: str = "GenericTagRunner"
    suite_name: Optional[str] = None
    backoffice_url: Optional[str] = None
    backoffice_email: Optional[str] = None
    backoffice_password: Optional[str] = None


class HealRequest(BaseModel):
    old_locator: Optional[dict[str, str]] = None
    old_element: Optional[dict[str, Any]] = None
    current_dom: Optional[str] = None
