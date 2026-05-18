import { useCallback, useEffect, useMemo, useState } from "react";
import "./sidebar-styles.css";
import TestsPage from "./pages/TestsPage";
import ReportsPage from "./pages/ReportsPage";
import HealingPage from "./pages/HealingPage";
import SettingsPage from "./pages/SettingsPage";

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
      return `${index === 0 ? "M" : "L"}${x} ${y}`;
    })
    .join(" ");
}

function TrendChart({ title, values, color }) {
  const width = 580;
  const height = 220;
  const padding = 24;
  const path = makeLinePath(values, width, height, padding);

  let areaPath = "";
  let avgRef = null;
  let avgY = null;
  if (values.length >= 2) {
    const min = Math.min(...values);
    const max = Math.max(...values);
    const span = max - min || 1;
    const xStep = (width - padding * 2) / (values.length - 1);
    areaPath = values
      .map((value, index) => {
        const x = padding + index * xStep;
        const y = height - padding - ((value - min) / span) * (height - padding * 2);
        return `${index === 0 ? "M" : "L"}${x} ${y}`;
      })
      .join(" ");
    areaPath += `L${width - padding} ${height - padding} L${padding} ${height - padding} Z`;
    avgRef = values.reduce((a, b) => a + b, 0) / values.length;
    avgY = height - padding - ((avgRef - min) / span) * (height - padding * 2);
  }

  const gradientId = `grad-${title.toLowerCase().replace(/\s+/g, '-')}`;
  const dotColor = color || "var(--pastel-turquoise-400)";

  return (
    <section className="band">
      <div className="band-head">
        <h2>{title}</h2>
        <span className="chip">{values.length} points</span>
      </div>
      {values.length > 1 ? (
        <div className="chart-shell">
          <svg viewBox={`0 0 ${width} ${height}`} className="chart">
            <defs>
              <linearGradient id={gradientId} x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor={dotColor} stopOpacity="0.15" />
                <stop offset="100%" stopColor={dotColor} stopOpacity="0" />
              </linearGradient>
            </defs>
            {avgY !== null && (
              <>
                <line x1={padding} y1={avgY} x2={width - padding} y2={avgY} stroke="var(--noveocare-gray-300)" strokeWidth="1" strokeDasharray="6 4" />
                <rect x={width - padding - 52} y={avgY - 9} width={52} height={18} rx={4} fill="var(--noveocare-gray-100)" opacity={0.85} />
                <text x={width - padding - 26} y={avgY + 4} textAnchor="middle" fontSize="10" fill="var(--noveocare-gray-500)">avg {avgRef.toFixed(2)}</text>
              </>
            )}
            <path d={areaPath} fill={`url(#${gradientId})`} />
            <path d={path} fill="none" stroke={dotColor} strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" />
            {values.map((value, index) => {
              const mn = Math.min(...values);
              const mx = Math.max(...values);
              const sp = mx - mn || 1;
              const xS = (width - padding * 2) / (values.length - 1);
              const x = padding + index * xS;
              const y = height - padding - ((value - mn) / sp) * (height - padding * 2);
              return (
                <circle key={index} cx={x} cy={x} r={3.5} fill={dotColor} stroke="white" strokeWidth="2" />
              );
            })}
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
        <h2>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <rect x="3" y="3" width="7" height="7" />
            <rect x="14" y="3" width="7" height="7" />
            <rect x="3" y="14" width="7" height="7" />
            <rect x="14" y="14" width="7" height="7" />
          </svg>
          Cucumber Distribution
        </h2>
        <span className="chip">{total} scenarios</span>
      </div>
      <div className="stackbar" aria-label="cucumber distribution">
        <div className="seg passed" style={{ width: `${pct(passed)}%`, minWidth: passed > 0 ? '4px' : '0' }} />
        <div className="seg failed" style={{ width: `${pct(failed)}%`, minWidth: failed > 0 ? '4px' : '0' }} />
        <div className="seg skipped" style={{ width: `${pct(skipped)}%`, minWidth: skipped > 0 ? '4px' : '0' }} />
      </div>
      <div className="legend">
        <span><i className="dot passed" />Passed {passed} ({pct(passed)}%)</span>
        <span><i className="dot failed" />Failed {failed} ({pct(failed)}%)</span>
        <span><i className="dot skipped" />Skipped {skipped} ({pct(skipped)}%)</span>
      </div>
    </section>
  );
}

function LogoImage({ src, alt, className }) {
  const [hasError, setHasError] = useState(false);
  if (!src || hasError) {
    const initials = (alt || "NC").substring(0, 2).toUpperCase();
    return (
      <div className={className + " sidebar-logo-fallback"}>
        {initials}
      </div>
    );
  }
  return (
    <img
      src={src}
      alt={alt}
      className={className}
      onError={() => setHasError(true)}
    />
  );
}

function LoginScreen({
  email, setEmail, password, setPassword,
  loginError, loginLoading, onSubmit,
}) {
  return (
    <main className="login-main">
      <div className="login-card">
        <div className="login-card-inner">
          <div className="login-decoration">
            <span /><span /><span />
          </div>
          <div className="login-brand">
            <LogoImage src="/logo.png" alt="NoveoCare" className="login-brand-logo" />
            <div className="login-brand-text">
            <h1>Sign In</h1>
            <span className="login-subtitle">Secure Access</span>
            </div>
          </div>
          <p>Access reserved for project managers and QA engineers.</p>
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
              {loginLoading ? "Signing in..." : "Sign in"}
            </button>
          </form>
        </div>
      </div>
    </main>
  );
}

function Sidebar({ actor, activeNav, onNavClick, onLogout, totalScenarios }) {
  const navItems = [
    {
      id: "dashboard",
      label: "Dashboard",
      icon: (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <rect x="3" y="3" width="7" height="9" /><rect x="14" y="3" width="7" height="5" /><rect x="14" y="12" width="7" height="9" /><rect x="3" y="16" width="7" height="5" />
        </svg>
      ),
      badge: totalScenarios > 0 ? totalScenarios : null,
    },
    {
      id: "tests",
      label: "Tests",
      icon: (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <polyline points="9 11 12 14 22 4" /><path d="M21 12v7a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h11" />
        </svg>
      ),
    },
    {
      id: "reports",
      label: "Reports",
      icon: (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z" /><polyline points="14 2 14 8 20 8" /><line x1="16" y1="13" x2="8" y2="13" /><line x1="16" y1="17" x2="8" y2="17" /><polyline points="10 9 9 9 8 9" />
        </svg>
      ),
    },
    {
      id: "healing",
      label: "Self-Healing",
      icon: (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
        </svg>
      ),
    },
    {
      id: "settings",
      label: "Settings",
      icon: (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <circle cx="12" cy="12" r="3" /><path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 01-2.83 2.83l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 010-4h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 012.83-2.83l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 014 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 010 4h-.09a1.65 1.65 0 00-1.51 1z" />
        </svg>
      ),
    },
  ];

  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <LogoImage src="/logo.png" alt="NoveoCare" className="sidebar-logo" />
        <div className="sidebar-brand">
          <h1>NoveoCare</h1>
          <p>AI Test Dashboard</p>
        </div>
      </div>

      <nav className="sidebar-nav">
        <div className="sidebar-section-label">Menu</div>
        {navItems.map((item) => (
          <button
            key={item.id}
            className={`nav-item${activeNav === item.id ? " active" : ""}`}
            onClick={() => onNavClick(item.id)}
          >
            {item.icon}
            <span>{item.label}</span>
            {item.badge != null && <span className="nav-count">{item.badge}</span>}
          </button>
        ))}
      </nav>

      <div className="sidebar-footer">
        {actor && (
          <button className="nav-item logout-btn" onClick={onLogout}>
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4" /><polyline points="16 17 21 12 16 7" /><line x1="21" y1="12" x2="9" y2="12" />
            </svg>
            <span>Sign out</span>
          </button>
        )}
      </div>
    </aside>
  );
}

function DecorativeCircles() {
  return (
    <svg style={{ display: 'none' }}>
      <defs>
        <filter id="blur-filter">
          <feGaussianBlur stdDeviation="3" />
        </filter>
      </defs>
    </svg>
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
  const [activeNav, setActiveNav] = useState("dashboard");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loginLoading, setLoginLoading] = useState(false);
  const [loginError, setLoginError] = useState("");
  const [autoRefresh, setAutoRefresh] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [lastLoadedAt, setLastLoadedAt] = useState("");
  const [payload, setPayload] = useState(null);
  const [recentPage, setRecentPage] = useState(0);

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
      setToken(data.access_token);
      setActor({ email: data.email, role: data.role, display_name: data.display_name });
      localStorage.setItem("dashboard.token", data.access_token);
      localStorage.setItem("dashboard.actor", JSON.stringify({ email: data.email, role: data.role, display_name: data.display_name }));
      setPassword("");
    } catch (err) {
      setLoginError(err instanceof Error ? err.message : String(err));
    } finally {
      setLoginLoading(false);
    }
  }, [baseUrl, email, password]);

  const loadData = useCallback(async (specificRunId) => {
    if (!token) return;
    setLoading(true);
    setError("");
    try {
      const url = new URL("/api/dashboard", baseUrl);
      if (specificRunId?.trim()) {
        url.searchParams.set("run_id", specificRunId.trim());
      }
      const res = await fetch(url.toString(), {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (res.status === 401) { handleLogout(); throw new Error("Session expired. Please sign in again."); }
      if (!res.ok) { throw new Error(`HTTP ${res.status} ${res.statusText}`); }
      const json = await res.json();
      setPayload(json);
      setRecentPage(0);
      setLastLoadedAt(new Date().toLocaleTimeString());
      if (json.run_id) {
        setRunId(json.run_id);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setLoading(false);
    }
  }, [token, baseUrl, handleLogout]);

  useEffect(() => { localStorage.setItem("dashboard.baseUrl", baseUrl); }, [baseUrl]);
  useEffect(() => { localStorage.setItem("dashboard.runId", runId); }, [runId]);
  useEffect(() => { if (token) loadData(); }, [token, loadData]);
  useEffect(() => {
    if (!autoRefresh || !token) return undefined;
    const timer = setInterval(() => loadData(), REFRESH_SECONDS * 1000);
    return () => clearInterval(timer);
  }, [autoRefresh, token, loadData]);

  const handleNavClick = (id) => {
    setActiveNav(id);
    if (id === "dashboard") loadData();
  };

  if (!token) {
    return (
      <LoginScreen
        email={email} setEmail={setEmail}
        password={password} setPassword={setPassword}
        loginError={loginError} loginLoading={loginLoading}
        onSubmit={handleLogin}
      />
    );
  }

  const metrics = payload?.metrics || {};
  const cucumber = payload?.cucumber || {};
  const recentEvents = payload?.recent_events || [];
  const PAGE_SIZE = 12;
  const totalPages = Math.ceil(recentEvents.length / PAGE_SIZE) || 1;
  const pageEvents = recentEvents.slice(recentPage * PAGE_SIZE, (recentPage + 1) * PAGE_SIZE);
  const history = payload?.metrics_history || [];
  const totalScenarios = (cucumber.total || 0);

  const healingRateTrend = buildSeries(history, "healing_rate");
  const scoreTrend = buildSeries(history, "avg_final_score");
  const structuralScoreTrend = buildSeries(history, "avg_structural_score");
  const semanticScoreTrend = buildSeries(history, "avg_semantic_score");
  const baselineHitRateTrend = buildSeries(history, "baseline_hit_rate");

  const kpis = [
    { label: "Scenarios total", value: formatValue(cucumber.total, 0), tone: "tone-a" },
    { label: "Passed", value: formatValue(cucumber.passed, 0), tone: "tone-b" },
    { label: "Failed", value: formatValue(cucumber.failed, 0), tone: "tone-c" },
    { label: "Skipped", value: formatValue(cucumber.skipped, 0), tone: "tone-d" },
    { label: "Healing rate", value: formatValue(metrics.healing_rate), tone: (metrics.healing_rate ?? 0) < 0.3 ? "tone-c" : (metrics.healing_rate ?? 0) < 0.7 ? "tone-d" : "tone-e" },
    { label: "Baseline hit rate", value: formatValue(metrics.baseline_hit_rate), tone: "tone-f" },
    { label: "Avg final score", value: formatValue(metrics.avg_final_score), tone: "tone-g" },
    { label: "Avg structural score", value: formatValue(metrics.avg_structural_score), tone: "tone-h" },
    { label: "Avg semantic score", value: formatValue(metrics.avg_semantic_score), tone: "tone-i" },
    { label: "Avg healing ms", value: formatValue(metrics.avg_healing_time_ms), tone: "tone-j" },
    { label: "NLP filter efficiency", value: formatValue(metrics.nlp_filter_efficiency), tone: "tone-k" },
  ];

  return (
    <div className="app-container">
      <DecorativeCircles />
      <Sidebar
        actor={actor}
        activeNav={activeNav}
        onNavClick={handleNavClick}
        onLogout={handleLogout}
        totalScenarios={totalScenarios}
      />
      <main className="main-content">
        <header className="topbar">
          <div className="title-zone">
            <h1>{activeNav === "tests" ? "Tests" : activeNav === "reports" ? "Reports" : activeNav === "healing" ? "Self-Healing" : activeNav === "settings" ? "Settings" : "Dashboard"}</h1>
            <p>{activeNav === "dashboard" ? "AI Test Automation" : ""}</p>
          </div>
          <div className="actions">
            <div className="search-bar">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="11" cy="11" r="8" /><line x1="21" y1="21" x2="16.65" y2="16.65" />
              </svg>
              <input type="text" placeholder="Run ID..." value={runId} onChange={(event) => { const val = event.target.value; setRunId(val); loadData(val); }} />
            </div>
            {lastLoadedAt && (
              <div className="topbar-info">
                <span className="topbar-info-dot active" />
                Last update: {lastLoadedAt}
              </div>
            )}
              <button onClick={() => loadData()} disabled={loading}
              style={{
                padding: "6px 10px", fontSize: "0.8rem", borderRadius: "8px",
                background: loading ? "var(--noveocare-gray-300)" : "var(--pastel-turquoise-500)",
                color: "white", border: "none", cursor: "pointer", display: "flex", alignItems: "center", gap: "4px",
                transition: "all 0.2s", fontWeight: 600,
              }}>
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <polyline points="23 4 23 10 17 10" /><path d="M20.49 15a9 9 0 11-2.12-9.36L23 10" />
              </svg>
              {loading ? "..." : "Refresh"}
            </button>
            <label className="toggle" style={{
              fontSize: "0.78rem", display: "flex", alignItems: "center", gap: "4px",
              color: "var(--pastel-turquoise-700, #0d5e4e)", cursor: "pointer", userSelect: "none",
            }}>
              <input type="checkbox" checked={autoRefresh} onChange={(event) => setAutoRefresh(event.target.checked)}
                style={{ accentColor: "var(--pastel-turquoise-500)", cursor: "pointer" }} />
              Auto
            </label>
            <div className="actor-chip" style={{ padding: "4px 10px", minWidth: "auto", gap: "2px", borderRadius: "8px" }}>
              <strong style={{ fontSize: "0.78rem" }}>{actor?.display_name || actor?.email}</strong>
              <span style={{ fontSize: "0.7rem" }}>{actor?.role === "project_manager" ? "Project Manager" : "QA Engineer"}</span>
            </div>
          </div>
        </header>

        {error ? (
          <section className="band error-box">
            <strong>Fetch error:</strong> {error}
          </section>
        ) : null}

        {activeNav === "tests" ? (
          <TestsPage baseUrl={baseUrl} token={token} />
        ) : activeNav === "reports" ? (
          <ReportsPage baseUrl={baseUrl} token={token} />
        ) : activeNav === "healing" ? (
          <HealingPage baseUrl={baseUrl} token={token} runId={runId} />
        ) : activeNav === "settings" ? (
          <SettingsPage baseUrl={baseUrl} setBaseUrl={setBaseUrl} runId={runId} setRunId={setRunId} autoRefresh={autoRefresh} setAutoRefresh={setAutoRefresh} actor={actor} />
        ) : (
          <div className="dashboard-content">
            <section className="cards">
              {kpis.map((kpi) => (
                <article className={`kpi ${kpi.tone}`} key={kpi.label}>
                  <h3>{kpi.label}</h3>
                  <p>{kpi.value}</p>
                </article>
              ))}
            </section>

            <div className="charts-grid">
              <TrendChart title="Healing Rate Trend" values={healingRateTrend} color="var(--pastel-turquoise-400)" />
              <TrendChart title="Baseline Hit Rate Trend" values={baselineHitRateTrend} color="var(--pastel-yellow-500)" />
              <TrendChart title="Final Score Trend" values={scoreTrend} color="var(--pastel-turquoise-500)" />
              <TrendChart title="Structural Score Trend" values={structuralScoreTrend} color="var(--pastel-red-400)" />
              <TrendChart title="Semantic Score Trend" values={semanticScoreTrend} color="var(--pastel-yellow-500)" />
              <TrendChart title="Avg Healing Time Trend" values={buildSeries(history, "avg_healing_time_ms")} color="var(--pastel-turquoise-400)" />
            </div>

            <CucumberDistribution cucumber={cucumber} />

            <section className="band">
              <div className="band-head">
                <h2>
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                  </svg>
                  Recent Healing Events
                </h2>
                <span>{recentEvents.length} rows</span>
              </div>
              {recentEvents.length ? (
                <div className="table-wrap">
                  <table>
                    <thead>
                      <tr>
                        <th>Time</th>
                        <th>Old locator</th>
                        <th>Healed locator</th>
                        <th>Exception</th>
                        <th>Score</th>
                        <th>Structural</th>
                        <th>Semantic</th>
                        <th>Status</th>
                        <th>ms</th>
                      </tr>
                    </thead>
                    <tbody>
                      {pageEvents.map((row) => (
                        <tr key={row.id}>
                          <td style={{ whiteSpace: "nowrap", fontSize: "0.82rem" }}>{formatDate(row.created_at)}</td>
                          <td>
                            <code style={{ fontSize: "0.78rem", background: "var(--pastel-turquoise-50)", padding: "2px 6px", borderRadius: "4px", wordBreak: "break-all" }}>
                              {row.old_locator_type ? `${row.old_locator_type}: ${row.old_locator_val || ""}` : "-"}
                            </code>
                          </td>
                          <td>
                            {row.new_locator_type ? (
                              <code style={{ fontSize: "0.78rem", background: "var(--pastel-yellow-50)", padding: "2px 6px", borderRadius: "4px", wordBreak: "break-all" }}>
                                {row.new_locator_type}: {row.new_locator_val || ""}
                              </code>
                            ) : <span className="no-error">-</span>}
                          </td>
                          <td><code style={{ fontSize: "0.75rem", background: "var(--pastel-red-50)", padding: "2px 6px", borderRadius: "4px", color: "var(--pastel-red-600)" }}>{row.exception_type || "-"}</code></td>
                          <td><strong>{formatValue(row.score)}</strong></td>
                          <td>{formatValue(row.structural_score)}</td>
                          <td>{formatValue(row.semantic_score)}</td>
                          <td>
                            <span className={`status-badge ${row.success ? "status-success" : "status-failed"}`}>
                              {row.success ? "OK" : "FAIL"}
                            </span>
                          </td>
                          <td style={{ fontSize: "0.82rem", color: "var(--noveocare-gray-400)" }}>{formatValue(row.healing_time_ms, 0)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                  {totalPages > 1 && (
                    <div className="pagination" style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "8px", padding: "12px 0" }}>
                      <button
                        disabled={recentPage === 0}
                        onClick={() => setRecentPage(recentPage - 1)}
                        style={{ padding: "4px 12px", borderRadius: "6px", border: "1px solid var(--noveocare-gray-300)", background: "var(--pastel-turquoise-50)", cursor: recentPage === 0 ? "default" : "pointer", opacity: recentPage === 0 ? 0.5 : 1 }}
                      >
                        Prev
                      </button>
                      <span style={{ fontSize: "0.82rem", color: "var(--noveocare-gray-500)" }}>
                        {recentPage + 1} / {totalPages}
                      </span>
                      <button
                        disabled={recentPage >= totalPages - 1}
                        onClick={() => setRecentPage(recentPage + 1)}
                        style={{ padding: "4px 12px", borderRadius: "6px", border: "1px solid var(--noveocare-gray-300)", background: "var(--pastel-turquoise-50)", cursor: recentPage >= totalPages - 1 ? "default" : "pointer", opacity: recentPage >= totalPages - 1 ? 0.5 : 1 }}
                      >
                        Next
                      </button>
                    </div>
                  )}
                </div>
              ) : (
                <div className="empty-state">
                  <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                  </svg>
                  <h3>No healing events yet</h3>
                  <p>Healing events will appear here when tests are executed and self-healing occurs.</p>
                </div>
              )}
            </section>
          </div>
        )}
      </main>
    </div>
  );
}
