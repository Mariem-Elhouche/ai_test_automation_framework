import { useCallback, useEffect, useState } from "react";
import "./sidebar-styles.css";
import TestsPage from "./pages/TestsPage";
import ReportsPage from "./pages/ReportsPage";
import HealingPage from "./pages/HealingPage";
import SettingsPage from "./pages/SettingsPage";
import TestRunnerPage from "./pages/TestRunnerPage";
import UserManagementPage from "./pages/UserManagementPage";

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

function buildSeries(rows, key) {
  return rows
    .map((row) => Number(row?.[key]))
    .filter((value) => Number.isFinite(value));
}

function TrendChart({ title, values, color }) {
  const H = 240, padL = 72, padR = 28, padT = 28, padB = 28;
  if (values.length < 2) return null;

  const min = Math.min(...values);
  const max = Math.max(...values);
  const span = max - min || 1;
  const minStep = 55;
  const W = Math.max(580, values.length * minStep);
  const xStep = (W - padL - padR) / (values.length - 1);
  const avg = values.reduce((a, b) => a + b, 0) / values.length;

  const path = values.map((v, i) => {
    const x = padL + i * xStep;
    const y = H - padB - ((v - min) / span) * (H - padT - padB);
    return `${i === 0 ? "M" : "L"}${x} ${y}`;
  }).join(" ");

  const area = path + `L${W - padR} ${H - padB} L${padL} ${H - padB} Z`;
  const gid = `grad-${title.toLowerCase().replace(/\s+/g, '-')}`;

  const avgY = H - padB - ((avg - min) / span) * (H - padT - padB);

  const yTicks = [min, min + span * 0.25, min + span * 0.5, min + span * 0.75, max];

  const xTickStep = Math.max(1, Math.floor(values.length / 12));
  const xTicks = Array.from({ length: values.length }, (_, i) => i).filter(i => i % xTickStep === 0 || i === 0 || i === values.length - 1);

  return (
    <section className="band">
      <div className="band-head">
        <h2><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>{title}</h2>
        <span className="chip">{values.length} data points</span>
      </div>
      <div className="chart-shell chart-shell-scroll">
        <svg viewBox={`0 0 ${W} ${H}`} className="chart" style={{ minWidth: `${W}px`, width: `${W}px`, height: `${H}px` }}>
          <defs>
            <linearGradient id={gid} x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor={color || "var(--pastel-turquoise-400)"} stopOpacity="0.15" />
              <stop offset="100%" stopColor={color || "var(--pastel-turquoise-400)"} stopOpacity="0" />
            </linearGradient>
            <filter id={`shadow-${gid}`}>
              <feDropShadow dx="0" dy="4" stdDeviation="6" floodColor={color || "var(--pastel-turquoise-400)"} floodOpacity="0.45" />
            </filter>
          </defs>

          {yTicks.map((t, i) => {
            const y = H - padB - ((t - min) / span) * (H - padT - padB);
            return (
              <g key={i}>
                <line x1={padL} y1={y} x2={W - padR} y2={y} stroke="var(--noveocare-gray-200)" strokeWidth="1" />
                <text x={padL - 8} y={y + 3} textAnchor="end" fontSize="9" fill="var(--noveocare-gray-400)">{i === 0 ? `${t.toFixed(2)} min` : i === yTicks.length - 1 ? `${t.toFixed(2)} max` : t.toFixed(2)}</text>
              </g>
            );
          })}

          <line x1={padL} y1={padT} x2={padL} y2={H - padB} stroke="var(--noveocare-gray-300)" strokeWidth="1" />

          {xTicks.map((i) => {
            const x = padL + i * xStep;
            return (
              <g key={i}>
                <line x1={x} y1={H - padB} x2={x} y2={H - padB + 4} stroke="var(--noveocare-gray-300)" strokeWidth="1" />
                <text x={x} y={H - padB + 14} textAnchor="middle" fontSize="9" fill="var(--noveocare-gray-400)">{i + 1}</text>
              </g>
            );
          })}

          <text x={(padL + W - padR) / 2} y={H - 3} textAnchor="middle" fontSize="10" fill="var(--noveocare-gray-400)">Run (historical)</text>

          <path d={area} fill={`url(#${gid})`} />
          <path d={path} fill="none" stroke={color || "var(--pastel-turquoise-400)"} strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" filter={`url(#shadow-${gid})`} />
          <line x1={padL} y1={avgY} x2={W - padR} y2={avgY} stroke="var(--noveocare-gray-500)" strokeWidth="1.5" strokeDasharray="6,4" opacity="0.6" />
          <text x={W - padR + 2} y={avgY + 3} fontSize="9" fill="var(--noveocare-gray-500)" opacity="0.6">avg</text>
        </svg>
      </div>
    </section>
  );
}

function HealingDonut({ rate }) {
  const pct = rate != null ? Math.min(rate, 1) : 0;
  const c = 2 * Math.PI * 40;
  const healed = pct * c;
  const failed = (1 - pct) * c;
  return (
    <div style={{ position: "relative", width: "140px", height: "140px", display: "inline-block" }}>
      <svg width="140" height="140" viewBox="0 0 100 100">
        <circle cx="50" cy="50" r="40" fill="none" stroke="var(--noveocare-gray-100)" strokeWidth="10" />
        {rate != null ? (
          <>
            <circle cx="50" cy="50" r="40" fill="none" stroke="var(--brand-secondary-300)" strokeWidth="10"
              strokeDasharray={`${failed} ${c - failed}`} strokeDashoffset={0}
              transform="rotate(-90 50 50)" style={{ transition: "stroke-dasharray 0.5s ease" }} />
            <circle cx="50" cy="50" r="40" fill="none" stroke="var(--brand-green-300)" strokeWidth="10"
              strokeDasharray={`${healed} ${c - healed}`} strokeDashoffset={-failed}
              transform="rotate(-90 50 50)" style={{ transition: "stroke-dasharray 0.5s ease" }} />
          </>
        ) : null}
        {rate != null ? (
          <text x="50" y="46" textAnchor="middle" fontSize="20" fontWeight="700" fill="var(--noveocare-gray-700)">
            {(rate * 100).toFixed(0)}%
          </text>
        ) : (
          <text x="50" y="54" textAnchor="middle" fontSize="12" fill="var(--noveocare-gray-400)">-</text>
        )}
        {rate != null ? (
          <text x="50" y="60" textAnchor="middle" fontSize="7" fill="var(--noveocare-gray-500)">of locators healed</text>
        ) : null}
      </svg>
    </div>
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
          <h1 className="login-title">LOGIN</h1>
          <p className="login-note">Accès réservé aux chefs de projet, ingénieurs QA et administrateurs.</p>
          <form onSubmit={onSubmit} className="login-form">
            <label>
              Email
              <input value={email} onChange={(event) => setEmail(event.target.value)} placeholder="votre.email@entreprise.com" />
            </label>
            <label>
              Password
              <input type="password" value={password} onChange={(event) => setPassword(event.target.value)} placeholder="••••••••" />
            </label>
            {loginError ? <div className="login-error">{loginError}</div> : null}
            <button type="submit" disabled={loginLoading}>
              {loginLoading ? "Login..." : "Login"}
            </button>
          </form>
        </div>
      </div>
    </main>
  );
}

const ROLE_NAV = {
  project_manager: new Set(["dashboard", "tests", "runner", "reports", "healing", "settings"]),
  qa_engineer: new Set(["dashboard", "tests", "runner", "reports", "healing", "settings"]),
  admin: new Set(["dashboard", "tests", "runner", "reports", "healing", "settings", "users"]),
};

function Sidebar({ actor, activeNav, onNavClick, onLogout, totalScenarios }) {
  const visibleIds = ROLE_NAV[actor?.role] || new Set();
  const allNavItems = [
    {
      id: "dashboard",
      label: "Overview",
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
      id: "runner",
      label: "Test Runner",
      icon: (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <polyline points="23 4 23 10 17 10" /><path d="M20.49 15a9 9 0 11-2.12-9.36L23 10" />
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
      id: "users",
      label: "Users",
      icon: (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2" />
          <circle cx="9" cy="7" r="4" />
          <path d="M23 21v-2a4 4 0 00-3-3.87" />
          <path d="M16 3.13a4 4 0 010 7.75" />
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
  const navItems = allNavItems.filter((item) => visibleIds.has(item.id));

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
  const [darkMode, setDarkMode] = useState(localStorage.getItem("dashboard.darkMode") === "true");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [lastLoadedAt, setLastLoadedAt] = useState("");
  const [payload, setPayload] = useState(null);


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
  useEffect(() => { localStorage.setItem("dashboard.darkMode", darkMode); }, [darkMode]);
  useEffect(() => { if (token) loadData(); }, [token, loadData]);
  useEffect(() => {
    if (!autoRefresh || !token) return undefined;
    const timer = setInterval(() => loadData(), REFRESH_SECONDS * 1000);
    return () => clearInterval(timer);
  }, [autoRefresh, token, loadData]);

  useEffect(() => {
    const allowed = ROLE_NAV[actor?.role];
    if (allowed && !allowed.has(activeNav)) {
      setActiveNav(Array.from(allowed)[0]);
    }
  }, [actor, activeNav]);

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
  const totalScenarios = (cucumber.total || 0);
  const history = payload?.metrics_history || [];

  return (
    <div className="app-container" data-theme={darkMode ? "dark" : "light"}>
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
            <h1>{activeNav === "dashboard" ? "Overview" : activeNav === "tests" ? "Tests" : activeNav === "runner" ? "Test Runner" : activeNav === "reports" ? "Reports" : activeNav === "healing" ? "Self-Healing" : activeNav === "users" ? "Users" : activeNav === "settings" ? "Settings" : ""}</h1>
            <p>{activeNav === "dashboard" ? "Test health & healing metrics at a glance" : activeNav === "tests" ? "Execution results and scenario history" : activeNav === "runner" ? "Execute tests on demand" : activeNav === "reports" ? "Browse generated test reports" : activeNav === "healing" ? "Self-healing events and performance" : activeNav === "users" ? "Manage platform users and roles" : activeNav === "settings" ? "API connection and display preferences" : ""}</p>
          </div>
          <div className="topbar-actions">
            <div className="search-bar">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="11" cy="11" r="8" /><line x1="21" y1="21" x2="16.65" y2="16.65" />
              </svg>
              <input type="text" placeholder="Run ID..." value={runId} onChange={(event) => { const val = event.target.value; setRunId(val); loadData(val); }} />
            </div>
            <div className="topbar-action-group">
              {lastLoadedAt && (
                <div className="topbar-info">
                  <span className="topbar-info-dot active" />
                  {lastLoadedAt}
                </div>
              )}
              <button className="topbar-refresh-btn" onClick={() => loadData()} disabled={loading}>
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                  <polyline points="23 4 23 10 17 10" /><path d="M20.49 15a9 9 0 11-2.12-9.36L23 10" />
                </svg>
                {loading ? "..." : "Refresh"}
              </button>
            </div>
            <div className="actor-chip">
              <strong>{actor?.display_name || actor?.email}</strong>
              <span>{actor?.role === "admin" ? "Administrator" : actor?.role === "project_manager" ? "Project Manager" : "QA Engineer"}</span>
            </div>
          </div>
        </header>

        {error ? (
          <section className="band error-box">
            <strong>Fetch error:</strong> {error}
          </section>
        ) : null}

        {activeNav === "runner" ? (
          <TestRunnerPage baseUrl={baseUrl} token={token} />
        ) : activeNav === "tests" ? (
          <TestsPage baseUrl={baseUrl} token={token} />
        ) : activeNav === "reports" ? (
          <ReportsPage baseUrl={baseUrl} token={token} />
        ) : activeNav === "healing" ? (
          <HealingPage baseUrl={baseUrl} token={token} runId={runId} />
        ) : activeNav === "users" ? (
          <UserManagementPage baseUrl={baseUrl} token={token} actor={actor} />
        ) : activeNav === "settings" ? (
          <SettingsPage baseUrl={baseUrl} setBaseUrl={setBaseUrl} runId={runId} setRunId={setRunId} autoRefresh={autoRefresh} setAutoRefresh={setAutoRefresh} actor={actor} darkMode={darkMode} setDarkMode={setDarkMode} />
        ) : (
          <div className="dashboard-content">

            <div className="section-divider">
              <span className="section-divider-label"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>Current Run</span>
              <span className="section-divider-line"></span>
            </div>

            <section className="band section-band">
              <div className="band-head">
                <h2 className="band-heading"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M9 12l2 2 4-4"/><path d="M12 2a10 10 0 1 0 10 10"/></svg>Global Test Health</h2>
                <button className="healing-link" onClick={() => setActiveNav("tests")}>
                  View Tests →
                </button>
              </div>
              <div className="overview-cards">
              <article className="kpi tone-a">
                <h3>Tests</h3>
                <p>{formatValue(cucumber.total, 0)}</p>
              </article>
              <article className="kpi tone-b">
                <h3>Passed</h3>
                <p>{formatValue(cucumber.passed, 0)}</p>
              </article>
              <article className="kpi tone-c">
                <h3>Failed</h3>
                <p>{formatValue(cucumber.failed, 0)}</p>
              </article>
              <article className="kpi tone-e">
                <h3>Success Rate</h3>
                <p>{cucumber.success_rate != null ? `${(cucumber.success_rate * 100).toFixed(1)}%` : "-"}</p>
              </article>
              <article className="kpi tone-f">
                <h3>Duration</h3>
                <p>{cucumber.avg_execution_time_ms != null ? `${cucumber.avg_execution_time_ms.toFixed(0)} ms` : "-"}</p>
              </article>
            </div>
            </section>

            <div className="overview-split">
              <div className="overview-card healing-card">
                <div className="healing-card-header">
                  <h3><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>Self-Healing Overview</h3>
                </div>
                <div className="healing-card-body">
                  <div style={{ textAlign: "center", marginBottom: "16px" }}>
                    <HealingDonut rate={metrics.healing_rate} />
                  </div>
                    <div className="healing-stats">
                    <div className="healing-stat" style={{ background: "linear-gradient(135deg, var(--brand-green-100) 0%, var(--brand-green-200) 100%)" }}>
                      <span className="healing-stat-label" style={{ color: "var(--brand-green-500)" }}>Recovered Locators</span>
                      <span className="healing-stat-value" style={{ color: "var(--brand-green-500)" }}>{formatValue(metrics.successful_healings ?? (metrics.total_healing_requests != null && metrics.healing_rate != null ? Math.round(metrics.total_healing_requests * metrics.healing_rate) : null), 0)}</span>
                    </div>
                    <div className="healing-stat" style={{ background: "linear-gradient(135deg, var(--brand-secondary-100) 0%, var(--brand-secondary-200) 100%)" }}>
                      <span className="healing-stat-label" style={{ color: "var(--brand-secondary-500)" }}>Failed Recovery</span>
                      <span className="healing-stat-value" style={{ color: "var(--brand-secondary-600)" }}>{formatValue(metrics.failed_healings ?? (metrics.total_healing_requests != null && metrics.successful_healings != null ? metrics.total_healing_requests - metrics.successful_healings : null), 0)}</span>
                    </div>
                    <div className="healing-stat" style={{ background: "linear-gradient(135deg, var(--brand-blue-100) 0%, var(--brand-blue-200) 100%)" }}>
                      <span className="healing-stat-label" style={{ color: "var(--brand-blue-500)" }}>Avg Recovery Time</span>
                      <span className="healing-stat-value" style={{ color: "var(--brand-blue-500)" }}>{metrics.avg_healing_time_ms != null ? `${metrics.avg_healing_time_ms.toFixed(0)} ms` : "-"}</span>
                    </div>
                  </div>
                  <button className="healing-link" onClick={() => setActiveNav("healing")}>
                    View Details →
                  </button>
                </div>
              </div>

              <div className="overview-card ai-card">
                <h3><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M12 20V10"/><path d="M18 20V4"/><path d="M6 20v-4"/></svg>Self-Healing Performance Metrics</h3>
                <div className="ai-big-score">
                  <span className="ai-score-label">Final Score</span>
                  <span className="ai-score-value">{metrics.avg_final_score != null ? metrics.avg_final_score.toFixed(2) : "-"}</span>
                </div>
                <div className="ai-breakdown">
                  <div className="ai-row">
                    <div className="ai-row-top">
                      <span>Structural Score</span>
                      <span className="ai-row-value">{metrics.avg_structural_score != null ? metrics.avg_structural_score.toFixed(2) : "-"}</span>
                    </div>
                    <div className="ai-row-bar-track"><div className="ai-row-bar-fill" style={{ width: metrics.avg_structural_score != null ? `${Math.min(metrics.avg_structural_score * 100, 100)}%` : "0%", background: "var(--brand-blue-300)" }}></div></div>
                  </div>
                  <div className="ai-row">
                    <div className="ai-row-top">
                      <span>Semantic Score</span>
                      <span className="ai-row-value">{metrics.avg_semantic_score != null ? metrics.avg_semantic_score.toFixed(2) : "-"}</span>
                    </div>
                    <div className="ai-row-bar-track"><div className="ai-row-bar-fill" style={{ width: metrics.avg_semantic_score != null ? `${Math.min(metrics.avg_semantic_score * 100, 100)}%` : "0%", background: "var(--brand-purple-300)" }}></div></div>
                  </div>
                  <div className="ai-row">
                    <div className="ai-row-top">
                      <span>NLP Filter Efficiency</span>
                      <span className="ai-row-value">{metrics.nlp_filter_efficiency != null ? metrics.nlp_filter_efficiency.toFixed(2) : "-"}</span>
                    </div>
                    <div className="ai-row-bar-track"><div className="ai-row-bar-fill" style={{ width: metrics.nlp_filter_efficiency != null ? `${Math.min(metrics.nlp_filter_efficiency * 100, 100)}%` : "0%", background: "var(--brand-orange-300)" }}></div></div>
                  </div>
                  <div className="ai-row">
                    <div className="ai-row-top">
                      <span>Baseline hit rate</span>
                      <span className="ai-row-value">{metrics.baseline_hit_rate != null ? metrics.baseline_hit_rate.toFixed(2) : "-"}</span>
                    </div>
                    <div className="ai-row-bar-track"><div className="ai-row-bar-fill" style={{ width: metrics.baseline_hit_rate != null ? `${Math.min(metrics.baseline_hit_rate * 100, 100)}%` : "0%", background: "var(--brand-green-300)" }}></div></div>
                  </div>
                </div>
              </div>
            </div>

            {history.length > 0 && (
              <>
              <div className="section-divider">
                <span className="section-divider-label"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>Historical Trends</span>
                <span className="section-divider-line"></span>
              </div>
              <div className="charts-grid">
                <TrendChart title="Healing Rate Trend" values={buildSeries(history, "healing_rate")} color="var(--brand-green-400)" />
                <TrendChart title="Baseline Hit Rate Trend" values={buildSeries(history, "baseline_hit_rate")} color="var(--brand-green-400)" />
                <TrendChart title="Final Score Trend" values={buildSeries(history, "avg_final_score")} color="var(--brand-purple-400)" />
                <TrendChart title="Structural Score Trend" values={buildSeries(history, "avg_structural_score")} color="var(--brand-blue-400)" />
                <TrendChart title="Semantic Score Trend" values={buildSeries(history, "avg_semantic_score")} color="var(--brand-purple-400)" />
                <TrendChart title="Avg Healing Time Trend" values={buildSeries(history, "avg_healing_time_ms")} color="var(--brand-orange-400)" />
              </div>
              </>
            )}
            </div>
            )}
      </main>
    </div>
    );
  }
