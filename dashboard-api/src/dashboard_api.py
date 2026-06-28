"""
Backward-compatible wrapper for existing tests.
Re-exports everything from the new src.main module,
including private backward-compat aliases used by test_test_runner.
"""
from src.main import *  # noqa: F401, F403
from src.main import (  # noqa: F401
    _append_log,
    _create_test_run,
    _db_engine,
    _test_runs,
    _test_runs_lock,
    _test_runs_table,
    _update_run,
)
