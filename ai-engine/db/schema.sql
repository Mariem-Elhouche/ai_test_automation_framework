CREATE TABLE IF NOT EXISTS healing_events (
    id SERIAL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    scenario_name VARCHAR(255),
    old_locator_type VARCHAR(100),
    old_locator_val TEXT,
    success BOOLEAN NOT NULL,
    score NUMERIC(10,4),
    structural_score NUMERIC(10,4),
    semantic_score NUMERIC(10,4),
    new_locator_type VARCHAR(100),
    new_locator_val TEXT,
    healing_time_ms INTEGER,
    baseline_hit BOOLEAN NOT NULL DEFAULT FALSE,
    elements_extracted INTEGER,
    after_struct_filter INTEGER,
    after_spatial_filter INTEGER,
    sent_to_nlp INTEGER,
    error_message TEXT,
    run_id VARCHAR(128)
);

CREATE INDEX IF NOT EXISTS idx_healing_events_created_at
    ON healing_events (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_healing_events_run_id
    ON healing_events (run_id);

CREATE TABLE IF NOT EXISTS metrics_snapshots (
    id SERIAL PRIMARY KEY,
    captured_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    total_healing_requests INTEGER NOT NULL DEFAULT 0,
    successful_healings INTEGER NOT NULL DEFAULT 0,
    failed_healings INTEGER NOT NULL DEFAULT 0,
    baseline_hits INTEGER NOT NULL DEFAULT 0,
    total_elements_extracted INTEGER NOT NULL DEFAULT 0,
    total_after_struct INTEGER NOT NULL DEFAULT 0,
    total_after_spatial INTEGER NOT NULL DEFAULT 0,
    total_sent_to_nlp INTEGER NOT NULL DEFAULT 0,
    total_healing_time_ms BIGINT NOT NULL DEFAULT 0,
    healing_rate NUMERIC(6,4),
    baseline_hit_rate NUMERIC(6,4),
    avg_healing_time_ms NUMERIC(12,2),
    avg_final_score NUMERIC(8,4),
    avg_structural_score NUMERIC(8,4),
    avg_semantic_score NUMERIC(8,4),
    nlp_filter_efficiency NUMERIC(6,4),
    run_id VARCHAR(128)
);

CREATE INDEX IF NOT EXISTS idx_metrics_snapshots_captured_at
    ON metrics_snapshots (captured_at DESC);
CREATE INDEX IF NOT EXISTS idx_metrics_snapshots_run_id
    ON metrics_snapshots (run_id);

CREATE TABLE IF NOT EXISTS cucumber_runs (
    id SERIAL PRIMARY KEY,
    run_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    feature_name VARCHAR(255) NOT NULL,
    scenario VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    duration_ns BIGINT,
    tags TEXT,
    run_id VARCHAR(128)
);

CREATE INDEX IF NOT EXISTS idx_cucumber_runs_run_at
    ON cucumber_runs (run_at DESC);

CREATE INDEX IF NOT EXISTS idx_cucumber_runs_status
    ON cucumber_runs (status);
CREATE INDEX IF NOT EXISTS idx_cucumber_runs_run_id
    ON cucumber_runs (run_id);

ALTER TABLE healing_events ADD COLUMN IF NOT EXISTS run_id VARCHAR(128);
ALTER TABLE metrics_snapshots ADD COLUMN IF NOT EXISTS run_id VARCHAR(128);
ALTER TABLE cucumber_runs ADD COLUMN IF NOT EXISTS run_id VARCHAR(128);
