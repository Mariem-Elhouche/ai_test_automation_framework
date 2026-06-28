#!/usr/bin/env python3
"""
Generate a local HTML dashboard by aggregating:
- Self-healing metrics from Colab API
- Framework execution metrics from Cucumber JSON reports
"""

from __future__ import annotations

import argparse
import json
import re
from collections import Counter
from datetime import datetime, timezone
from html import escape
from pathlib import Path
from typing import Any
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen


FAILED_STEP_STATUSES = {"failed", "ambiguous", "undefined", "pending"}


def parse_properties(file_path: Path) -> dict[str, str]:
    props: dict[str, str] = {}
    if not file_path.exists():
        return props

    for raw_line in file_path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#"):
            continue
        if "=" not in line:
            continue
        key, value = line.split("=", 1)
        props[key.strip()] = value.strip()
    return props


def infer_colab_base_url(properties: dict[str, str]) -> str | None:
    direct = properties.get("self.healing.api.url", "").strip()
    if direct:
        return re.sub(r"/heal/?$", "", direct)
    fallback = properties.get("colab.url", "").strip()
    return fallback or None


def collect_framework_metrics(target_dir: Path) -> dict[str, Any]:
    cucumber_files = sorted(target_dir.glob("cucumber*.json"))

    scenario_total = scenario_passed = scenario_failed = scenario_skipped = 0
    step_total = step_passed = step_failed = step_skipped = 0
    total_step_duration_ns = 0
    tag_counter: Counter[str] = Counter()

    for report in cucumber_files:
        try:
            features = json.loads(report.read_text(encoding="utf-8"))
        except (OSError, json.JSONDecodeError):
            continue

        if not isinstance(features, list):
            continue

        for feature in features:
            elements = feature.get("elements", [])
            if not isinstance(elements, list):
                continue

            for scenario in elements:
                if scenario.get("type") != "scenario":
                    continue

                scenario_total += 1
                scenario_state = "passed"
                steps = scenario.get("steps", [])

                for step in steps:
                    result = step.get("result", {}) if isinstance(step, dict) else {}
                    status = str(result.get("status", "unknown")).lower()
                    duration_ns = result.get("duration", 0)
                    if isinstance(duration_ns, (int, float)):
                        total_step_duration_ns += int(duration_ns)

                    step_total += 1
                    if status == "passed":
                        step_passed += 1
                    elif status == "skipped":
                        step_skipped += 1
                    else:
                        step_failed += 1

                    if status in FAILED_STEP_STATUSES:
                        scenario_state = "failed"
                    elif status == "skipped" and scenario_state == "passed":
                        scenario_state = "skipped"

                if scenario_state == "passed":
                    scenario_passed += 1
                elif scenario_state == "failed":
                    scenario_failed += 1
                else:
                    scenario_skipped += 1

                tags = scenario.get("tags", [])
                if isinstance(tags, list):
                    for t in tags:
                        if isinstance(t, dict):
                            name = t.get("name")
                            if isinstance(name, str) and name:
                                tag_counter[name] += 1

    request_files = list((target_dir / "healing-debug").glob("*-request.json"))
    reports_html = list((target_dir / "reports").glob("**/*.html"))

    scenario_pass_rate = round((scenario_passed / scenario_total) * 100, 1) if scenario_total else 0.0
    avg_step_duration_ms = round((total_step_duration_ns / step_total) / 1_000_000, 1) if step_total else 0.0

    latest_source_mtime = 0.0
    source_files = cucumber_files + request_files + reports_html
    if source_files:
        latest_source_mtime = max(file.stat().st_mtime for file in source_files if file.exists())

    last_run_iso = (
        datetime.fromtimestamp(latest_source_mtime, tz=timezone.utc).isoformat() if latest_source_mtime else None
    )

    return {
        "cucumber_files": [str(p.name) for p in cucumber_files],
        "scenarios": {
            "total": scenario_total,
            "passed": scenario_passed,
            "failed": scenario_failed,
            "skipped": scenario_skipped,
            "pass_rate_percent": scenario_pass_rate,
        },
        "steps": {
            "total": step_total,
            "passed": step_passed,
            "failed": step_failed,
            "skipped": step_skipped,
            "avg_duration_ms": avg_step_duration_ms,
        },
        "healing_debug": {
            "request_payload_count": len(request_files),
        },
        "reports": {
            "html_report_count": len(reports_html),
        },
        "top_tags": [{"tag": tag, "count": count} for tag, count in tag_counter.most_common(10)],
        "last_run_utc": last_run_iso,
    }


def try_fetch_json(url: str, timeout_seconds: float) -> dict[str, Any]:
    request = Request(
        url=url,
        headers={
            "Accept": "application/json",
            "ngrok-skip-browser-warning": "true",
            "User-Agent": "ai-test-dashboard/1.0",
        },
        method="GET",
    )
    with urlopen(request, timeout=timeout_seconds) as response:
        payload = response.read().decode("utf-8")
        data = json.loads(payload)
        if not isinstance(data, dict):
            raise ValueError("Response is not a JSON object")
        return data


def collect_colab_metrics(metrics_url: str | None, base_url: str | None, timeout_seconds: float) -> dict[str, Any]:
    candidates: list[str] = []
    if metrics_url:
        candidates.append(metrics_url.rstrip("/"))
    elif base_url:
        base = base_url.rstrip("/")
        candidates.extend(
            [
                f"{base}/metrics",
                f"{base}/healing/metrics",
                f"{base}/self-healing/metrics",
            ]
        )

    if not candidates:
        return {"status": "unavailable", "error": "No Colab URL configured."}

    errors: list[str] = []
    for url in candidates:
        try:
            raw = try_fetch_json(url, timeout_seconds=timeout_seconds)
            if "metrics" in raw and isinstance(raw["metrics"], dict):
                metrics = raw["metrics"]
            else:
                metrics = raw
            return {"status": "ok", "url": url, "metrics": metrics}
        except HTTPError as exc:
            errors.append(f"{url} -> HTTP {exc.code}")
        except URLError as exc:
            errors.append(f"{url} -> {exc.reason}")
        except Exception as exc:  # noqa: BLE001
            errors.append(f"{url} -> {exc}")

    return {"status": "unavailable", "error": " ; ".join(errors)}


def card(title: str, value: Any) -> str:
    return (
        "<div class='card'>"
        f"<div class='label'>{escape(str(title))}</div>"
        f"<div class='value'>{escape(str(value))}</div>"
        "</div>"
    )


def render_dashboard(
    generated_at: str,
    framework_metrics: dict[str, Any],
    colab_metrics: dict[str, Any],
) -> str:
    scenarios = framework_metrics["scenarios"]
    steps = framework_metrics["steps"]
    healing_debug = framework_metrics["healing_debug"]
    reports = framework_metrics["reports"]
    tags = framework_metrics["top_tags"]

    framework_cards = "".join(
        [
            card("Scenarios", scenarios["total"]),
            card("Pass rate", f"{scenarios['pass_rate_percent']}%"),
            card("Scenarios failed", scenarios["failed"]),
            card("Steps total", steps["total"]),
            card("Avg step duration", f"{steps['avg_duration_ms']} ms"),
            card("Healing payloads", healing_debug["request_payload_count"]),
            card("HTML reports", reports["html_report_count"]),
        ]
    )

    if colab_metrics.get("status") == "ok":
        metrics = colab_metrics.get("metrics", {})
        colab_cards = "".join(
            [
                card("Endpoint", colab_metrics.get("url", "n/a")),
                card("Healing requests", metrics.get("total_healing_requests", "n/a")),
                card("Healing success", metrics.get("successful_healings", "n/a")),
                card("Healing failed", metrics.get("failed_healings", "n/a")),
                card("Healing rate", metrics.get("healing_rate", "n/a")),
                card("Baseline hit rate", metrics.get("baseline_hit_rate", "n/a")),
                card("Avg healing time", f"{metrics.get('avg_healing_time_ms', 'n/a')} ms"),
                card("Avg final score", metrics.get("avg_final_score", "n/a")),
                card("Avg structural score", metrics.get("avg_structural_score", "n/a")),
                card("Avg semantic score", metrics.get("avg_semantic_score", "n/a")),
                card("NLP filter efficiency", metrics.get("nlp_filter_efficiency", "n/a")),
            ]
        )
        colab_raw = json.dumps(metrics, indent=2, ensure_ascii=False)
    else:
        colab_cards = (
            "<div class='warning'>Métriques Colab indisponibles: "
            f"{escape(str(colab_metrics.get('error', 'unknown error')))}</div>"
        )
        colab_raw = "{}"

    tags_rows = "".join(
        f"<tr><td>{escape(t['tag'])}</td><td>{escape(str(t['count']))}</td></tr>" for t in tags
    ) or "<tr><td colspan='2'>Aucun tag détecté.</td></tr>"

    framework_raw = json.dumps(framework_metrics, indent=2, ensure_ascii=False)

    return f"""<!doctype html>
<html lang="fr">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Automation Dashboard</title>
  <style>
    body {{
      margin: 0;
      font-family: Arial, sans-serif;
      background: #f5f7fb;
      color: #1a1f36;
    }}
    .container {{
      max-width: 1200px;
      margin: 24px auto;
      padding: 0 16px 24px;
    }}
    h1, h2 {{
      margin: 0 0 12px;
      font-weight: 600;
    }}
    .meta {{
      color: #4a5674;
      margin-bottom: 20px;
      font-size: 14px;
    }}
    .grid {{
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
      gap: 12px;
      margin-bottom: 16px;
    }}
    .card {{
      background: #fff;
      border: 1px solid #dce1ef;
      border-radius: 8px;
      padding: 12px;
    }}
    .label {{
      color: #59627a;
      font-size: 12px;
      margin-bottom: 6px;
    }}
    .value {{
      font-size: 18px;
      font-weight: 600;
      word-break: break-word;
    }}
    .section {{
      margin-top: 24px;
    }}
    .warning {{
      background: #fff7ed;
      border: 1px solid #fed7aa;
      color: #9a3412;
      border-radius: 8px;
      padding: 10px 12px;
    }}
    table {{
      width: 100%;
      border-collapse: collapse;
      background: #fff;
      border: 1px solid #dce1ef;
      border-radius: 8px;
      overflow: hidden;
    }}
    th, td {{
      text-align: left;
      padding: 10px;
      border-bottom: 1px solid #edf0f7;
      font-size: 14px;
    }}
    th {{
      background: #f9fbff;
      font-weight: 600;
    }}
    pre {{
      background: #0f172a;
      color: #e2e8f0;
      border-radius: 8px;
      padding: 12px;
      overflow-x: auto;
      font-size: 12px;
      margin: 0;
    }}
  </style>
</head>
<body>
  <div class="container">
    <h1>AI Test Automation Dashboard</h1>
    <div class="meta">Généré le: {escape(generated_at)} | Dernier run (UTC): {escape(str(framework_metrics.get('last_run_utc')))}</div>

    <div class="section">
      <h2>Métriques Framework</h2>
      <div class="grid">{framework_cards}</div>
    </div>

    <div class="section">
      <h2>Métriques Self-Healing (Colab)</h2>
      <div class="grid">{colab_cards}</div>
    </div>

    <div class="section">
      <h2>Top Tags Cucumber</h2>
      <table>
        <thead><tr><th>Tag</th><th>Occurrences</th></tr></thead>
        <tbody>{tags_rows}</tbody>
      </table>
    </div>

    <div class="section">
      <h2>Raw JSON - Framework</h2>
      <pre>{escape(framework_raw)}</pre>
    </div>

    <div class="section">
      <h2>Raw JSON - Colab</h2>
      <pre>{escape(colab_raw)}</pre>
    </div>
  </div>
</body>
</html>
"""


def main() -> int:
    parser = argparse.ArgumentParser(description="Generate automation dashboard HTML")
    parser.add_argument(
        "--framework-root",
        type=Path,
        default=Path(__file__).resolve().parents[1],
        help="Path to automation-framework module root",
    )
    parser.add_argument(
        "--colab-metrics-url",
        default=None,
        help="Explicit Colab metrics endpoint (ex: https://<ngrok>/metrics)",
    )
    parser.add_argument(
        "--timeout-seconds",
        type=float,
        default=6.0,
        help="HTTP timeout for Colab metrics call",
    )
    args = parser.parse_args()

    framework_root: Path = args.framework_root
    target_dir = framework_root / "target"
    output_file = target_dir / "dashboard" / "index.html"
    config_path = framework_root / "src" / "main" / "resources" / "config.properties"

    framework_metrics = collect_framework_metrics(target_dir)

    props = parse_properties(config_path)
    base_url = infer_colab_base_url(props)
    colab_metrics = collect_colab_metrics(
        metrics_url=args.colab_metrics_url,
        base_url=base_url,
        timeout_seconds=args.timeout_seconds,
    )

    output_file.parent.mkdir(parents=True, exist_ok=True)
    html = render_dashboard(
        generated_at=datetime.now().astimezone().isoformat(timespec="seconds"),
        framework_metrics=framework_metrics,
        colab_metrics=colab_metrics,
    )
    output_file.write_text(html, encoding="utf-8")
    print(f"Dashboard generated: {output_file}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
