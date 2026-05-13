import { useCallback, useEffect, useMemo, useState } from "react";

const DEFAULT_BASE_URL = "http://127.0.0.1:8080";
const REFRESH_SECONDS = 20;

function formatValue(value, digits = 2) {
  if (value === null || value === undefined || value === "") {
    return "-";
  }
  const n = Number(value);
  if (Number.isFinite(n)) {
    return n.toFixed(digits);
  }
  return String(value);
}

function formatDate(value) {
  if (!value) {
    return "-";
  }
  const dt = new Date(value);
  if (Number.isNaN(dt.getTime())) {
    return value;
  }
  return dt.toLocaleString();
}

function buildSeries(rows, key) {
  return rows
    .map((row) => Number(row?.[key]))
    .filter((value) => Number.isFinite(value));
}

function makeLinePath(values, width, height, padding) {
  if (values.length < 2) {
    return "";
  }
  const min = Math.min(...values);
  const max = Math.max(...values);
  const span = max - min || 1;
  const xStep = (width - padding * 2) / (values.length - 1);

  return values
    .map((value, index) => {
      const x = padding + index * xStep;
      const y = height - padding - ((value - min) / span) * (height - padding * 2);
      return `${index === 0 ? "M" : "L"}${x.toFixed(1)} ${y.toFixed(1)}`;
    })
    .join(" ");
}

function TrendChart({ title, values, color }) {
  const width = 580;
  const height = 200;
  const padding = 20;
  const path = makeLinePath(values, width, height, padding);

  return (
    <section className="band">
      <div className="band-head">
        <h2>{title}</h2>
        <span className="chip">{values.length} points</span>
      </div>
      {values.length > 1 ? (
        <div className="chart-shell">
          <svg viewBox={`0 0 ${width} ${height}`} className="chart">
            <path d={path} fill="none" stroke={color} strokeWidth="3" strokeLinecap="round" />
          </svg>
        </div>
      ) : (
        <div className="empty">Not enough points to render trend.</div>
      )}
    </section>
  );
}

function CucumberDistribution({ cucumber }) {
  const passed = Number(cucumber?.passed || 0);
  const failed = Number(cucumber?.failed || 0);
  const skipped = Number(cucumber?.skipped || 0);
  const total = passed + failed + skipped;

  const pct = (value) => (total ? ((value / total) * 100).toFixed(1) : "0.0");

  return (
    <section className="band">
      <div className="band-head">
        <h2>Cucumber Distribution</h2>
        <span className="chip">{total} scenarios</span>
      </div>
      <div className="stackbar" aria-label="cucumber distribution">
        <div className="seg passed" style={{ width: `${pct(passed)}%` }} />
        <div className="seg failed" style={{ width: `${pct(failed)}%` }} />
        <div className="seg skipped" style={{ width: `${pct(skipped)}%` }} />
      </div>
      <div className="legend">
        <span><i className="dot passed" />Passed {passed} ({pct(passed)}%)</span>
        <span><i className="dot failed" />Failed {failed} ({pct(failed)}%)</span>
        <span><i className="dot skipped" />Skipped {skipped} ({pct(skipped)}%)</span>
      </div>
    </section>
  );
}

function LoginScreen({
  email,
  setEmail,
  password,
  setPassword,
  loginError,
  loginLoading,
  onSubmit,
}) {
  return (
    <main className="login-main">
      <section className="login-card">
        <div className="login-brand">
          <h1>Connexion</h1>
          <span className="login-subtitle">Secure Access</span>
        </div>
        <p>Accès réservé au chef de projet et au QA engineer.</p>
        <form onSubmit={onSubmit} className="login-form">
          <label>
            Email
            <input value={email} onChange={(event) => setEmail(event.target.value)} placeholder="votre.email@entreprise.com" />
          </label>
          <label>
            Password
            <input type="password" value={password} onChange={(event) => setPassword(event.target.value)} />
          </label>
          {loginError ? <div className="login-error">{loginError}</div> : null}
          <button type="submit" disabled={loginLoading}>
            {loginLoading ? "Connexion..." : "Se connecter"}
          </button>
        </form>
      </section>
    </main>
  );
}

export default function App() {
  const [baseUrl, setBaseUrl] = useState(localStorage.getItem("dashboard.baseUrl") || DEFAULT_BASE_URL);
  const [runId, setRunId] = useState(localStorage.getItem("dashboard.runId") || "");
  const [token, setToken] = useState(localStorage.getItem("dashboard.token") || "");
  const [actor, setActor] = useState(() => {
    const raw = localStorage.getItem("dashboard.actor");
    if (!raw) return null;
    try {
      return JSON.parse(raw);
    } catch {
      return null;
    }
  });
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loginLoading, setLoginLoading] = useState(false);
  const [loginError, setLoginError] = useState("");

  const [autoRefresh, setAutoRefresh] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [lastLoadedAt, setLastLoadedAt] = useState("");
  const [payload, setPayload] = useState(null);

  const requestUrl = useMemo(() => {
    const url = new URL("/api/dashboard", baseUrl);
    if (runId.trim()) {
      url.searchParams.set("run_id", runId.trim());
    }
    return url.toString();
  }, [baseUrl, runId]);

  const handleLogout = useCallback(() => {
    setToken("");
    setActor(null);
    setPayload(null);
    localStorage.removeItem("dashboard.token");
    localStorage.removeItem("dashboard.actor");
  }, []);

  const handleLogin = useCallback(async (event) => {
    event.preventDefault();
    setLoginLoading(true);
    setLoginError("");
    try {
      const response = await fetch(new URL("/auth/login", baseUrl), {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });
      if (!response.ok) {
        throw new Error(response.status === 401 ? "Identifiants invalides" : `HTTP ${response.status}`);
      }
      const data = await response.json();
      const authActor = {
        email: data.email,
        role: data.role,
        display_name: data.display_name,
      };
      setToken(data.access_token);
      setActor(authActor);
      localStorage.setItem("dashboard.token", data.access_token);
      localStorage.setItem("dashboard.actor", JSON.stringify(authActor));
      setPassword("");
    } catch (err) {
      setLoginError(err instanceof Error ? err.message : String(err));
    } finally {
      setLoginLoading(false);
    }
  }, [baseUrl, email, password]);

  const loadData = useCallback(async () => {
    if (!token) return;
    setLoading(true);
    setError("");
    try {
      const res = await fetch(requestUrl, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      if (res.status === 401) {
        handleLogout();
        throw new Error("Session expiree. Reconnectez-vous.");
      }
      if (!res.ok) {
        throw new Error(`HTTP ${res.status} ${res.statusText}`);
      }
      const json = await res.json();
      setPayload(json);
      setLastLoadedAt(new Date().toLocaleTimeString());
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setLoading(false);
    }
  }, [token, requestUrl, handleLogout]);

  useEffect(() => {
    localStorage.setItem("dashboard.baseUrl", baseUrl);
  }, [baseUrl]);

  useEffect(() => {
    localStorage.setItem("dashboard.runId", runId);
  }, [runId]);

  useEffect(() => {
    if (token) {
      loadData();
    }
  }, [token, loadData]);

  useEffect(() => {
    if (!autoRefresh || !token) {
      return undefined;
    }
    const timer = setInterval(loadData, REFRESH_SECONDS * 1000);
    return () => clearInterval(timer);
  }, [autoRefresh, token, loadData]);

  if (!token) {
    return (
      <LoginScreen
        email={email}
        setEmail={setEmail}
        password={password}
        setPassword={setPassword}
        loginError={loginError}
        loginLoading={loginLoading}
        onSubmit={handleLogin}
      />
    );
  }

  const metrics = payload?.metrics || {};
  const cucumber = payload?.cucumber || {};
  const recentEvents = payload?.recent_events || [];
  const history = payload?.metrics_history || [];

  const healingRateTrend = buildSeries(history, "healing_rate");
  const scoreTrend = buildSeries(history, "avg_final_score");
  const kpis = [
    { label: "Scenarios total", value: formatValue(cucumber.total, 0), tone: "tone-a" },
    { label: "Passed", value: formatValue(cucumber.passed, 0), tone: "tone-b" },
    { label: "Failed", value: formatValue(cucumber.failed, 0), tone: "tone-c" },
    { label: "Skipped", value: formatValue(cucumber.skipped, 0), tone: "tone-d" },
    { label: "Healing rate", value: formatValue(metrics.healing_rate), tone: "tone-e" },
    { label: "Avg final score", value: formatValue(metrics.avg_final_score), tone: "tone-f" },
    { label: "Avg healing ms", value: formatValue(metrics.avg_healing_time_ms), tone: "tone-g" },
    { label: "NLP filter efficiency", value: formatValue(metrics.nlp_filter_efficiency), tone: "tone-h" },
  ];

  return (
    <main>
      <header className="topbar">
        <div className="title-zone">
          <h1>AI Test Dashboard</h1>
          <p>Live view from FastAPI + PostgreSQL</p>
        </div>
        <div className="actions">
          <div className="actor-chip">
            <strong>{actor?.display_name || actor?.email}</strong>
            <span>{actor?.role === "project_manager" ? "Chef de projet" : "QA Engineer"}</span>
          </div>
          <button onClick={loadData} disabled={loading}>
            {loading ? "Loading..." : "Refresh"}
          </button>
          <button className="secondary-btn" onClick={handleLogout}>Logout</button>
          <label className="toggle">
            <input
              type="checkbox"
              checked={autoRefresh}
              onChange={(event) => setAutoRefresh(event.target.checked)}
            />
            Auto refresh ({REFRESH_SECONDS}s)
          </label>
        </div>
      </header>

      <section className="band filters">
        <div className="field">
          <label>API URL</label>
          <input value={baseUrl} onChange={(event) => setBaseUrl(event.target.value)} />
        </div>
        <div className="field">
          <label>Run ID (optional)</label>
          <input value={runId} onChange={(event) => setRunId(event.target.value)} placeholder="run-2026-05-11-01" />
        </div>
      </section>

      <section className="band status-line">
        <div><span className="status-label">Request</span> {requestUrl}</div>
        <div><span className="status-label">Run</span> {payload?.run_id || "-"}</div>
        <div><span className="status-label">Healing source</span> {payload?.healing_source || "-"}</div>
        <div><span className="status-label">Last update</span> {lastLoadedAt || "-"}</div>
      </section>

      {error ? (
        <section className="band error-box">
          <strong>Fetch error:</strong> {error}
        </section>
      ) : null}

      <section className="cards">
        {kpis.map((kpi) => (
          <article className={`kpi ${kpi.tone}`} key={kpi.label}>
            <h3>{kpi.label}</h3>
            <p>{kpi.value}</p>
          </article>
        ))}
      </section>

      <CucumberDistribution cucumber={cucumber} />
      <TrendChart title="Healing Rate Trend" values={healingRateTrend} color="#2563eb" />
      <TrendChart title="Final Score Trend" values={scoreTrend} color="#0f766e" />

      <section className="band">
        <div className="band-head">
          <h2>Recent Healing Events</h2>
          <span>{recentEvents.length} rows</span>
        </div>
        {recentEvents.length ? (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Created at</th>
                  <th>Scenario</th>
                  <th>Success</th>
                  <th>Score</th>
                  <th>Structural</th>
                  <th>Semantic</th>
                  <th>Time ms</th>
                  <th>Error</th>
                </tr>
              </thead>
              <tbody>
                {recentEvents.map((row) => (
                  <tr key={row.id}>
                    <td>{formatDate(row.created_at)}</td>
                    <td>{row.scenario_name || "-"}</td>
                    <td>{row.success ? "yes" : "no"}</td>
                    <td>{formatValue(row.score)}</td>
                    <td>{formatValue(row.structural_score)}</td>
                    <td>{formatValue(row.semantic_score)}</td>
                    <td>{formatValue(row.healing_time_ms, 0)}</td>
                    <td>{row.error_message || "-"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="empty">No events in database for this run yet.</div>
        )}
      </section>
    </main>
  );
}
