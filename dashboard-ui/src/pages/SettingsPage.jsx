export default function SettingsPage({ baseUrl, setBaseUrl, runId, setRunId, autoRefresh, setAutoRefresh, actor, darkMode, setDarkMode }) {
  return (
    <div>
      <section className="band">
        <div className="band-head">
          <h2>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <circle cx="12" cy="12" r="3" /><path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 01-2.83 2.83l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 010-4h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 012.83-2.83l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 014 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 010 4h-.09a1.65 1.65 0 00-1.51 1z" />
            </svg>
            Settings
          </h2>
        </div>
        <div style={{ display: "grid", gap: "16px", gridTemplateColumns: "1fr 1fr" }}>
          <div style={{ border: "1px solid var(--pastel-turquoise-100)", borderRadius: "var(--radius)", padding: "20px" }}>
            <h3 style={{ margin: "0 0 16px", fontSize: "1rem", color: "var(--noveocare-gray-700)" }}>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ verticalAlign: "middle", marginRight: "8px" }}>
                <circle cx="12" cy="12" r="10" /><line x1="12" y1="16" x2="12" y2="12" /><line x1="12" y1="8" x2="12.01" y2="8" />
              </svg>
              Connection
            </h3>
            <div className="filters" style={{ marginBottom: "0" }}>
              <div className="field">
                <label>API URL</label>
                <input value={baseUrl} onChange={(e) => setBaseUrl(e.target.value)} />
              </div>
              <div className="field">
                <label>Run ID <span style={{ fontWeight: "normal", color: "var(--noveocare-gray-400)" }}>(optional)</span></label>
                <input value={runId} onChange={(e) => setRunId(e.target.value)} placeholder="Auto-detect if empty" />
              </div>
            </div>
          </div>

          <div style={{ border: "1px solid var(--pastel-turquoise-100)", borderRadius: "var(--radius)", padding: "20px" }}>
            <h3 style={{ margin: "0 0 16px", fontSize: "1rem", color: "var(--noveocare-gray-700)" }}>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ verticalAlign: "middle", marginRight: "8px" }}>
                <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2" /><circle cx="12" cy="7" r="4" />
              </svg>
              Account
            </h3>
            {actor && (
              <div style={{ display: "flex", flexDirection: "column", gap: "6px" }}>
                <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                  <span className={`chip ${actor.role === "admin" ? "chip-admin" : actor.role === "project_manager" ? "chip-pm" : "chip-qa"}`} style={{ fontSize: "0.85rem" }}>
                    {actor.role === "admin" ? "Administrator" : actor.role === "project_manager" ? "Project Manager" : "QA Engineer"}
                  </span>
                  <strong style={{ fontSize: "0.95rem", color: "var(--noveocare-gray-700)" }}>{actor.display_name}</strong>
          </div>

          <div style={{ border: "1px solid var(--pastel-turquoise-100)", borderRadius: "var(--radius)", padding: "20px" }}>
            <h3 style={{ margin: "0 0 16px", fontSize: "1rem", color: "var(--noveocare-gray-700)" }}>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ verticalAlign: "middle", marginRight: "8px" }}>
                <circle cx="12" cy="12" r="10" /><path d="M12 2a15.3 15.3 0 014 10 15.3 15.3 0 01-4 10 15.3 15.3 0 01-4-10 15.3 15.3 0 014-10z" />
              </svg>
              Display
            </h3>
            <label className="toggle" style={{ fontSize: "0.95rem", color: "var(--noveocare-gray-600)", cursor: "pointer", display: "flex", alignItems: "center", gap: "10px" }}>
              <input type="checkbox" checked={darkMode} onChange={(e) => setDarkMode(e.target.checked)} style={{ width: "18px", height: "18px", accentColor: "var(--pastel-turquoise-500)" }} />
              <span style={{ display: "flex", alignItems: "center", gap: "6px" }}>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M21 12.79A9 9 0 1111.21 3 7 7 0 0021 12.79z" />
                </svg>
                Dark Mode
              </span>
            </label>
            <p style={{ margin: "8px 0 0", fontSize: "0.8rem", color: "var(--noveocare-gray-400)" }}>
              Switch between light and dark theme for the dashboard.
            </p>
          </div>
                <div style={{ fontSize: "0.85rem", color: "var(--noveocare-gray-500)" }}>{actor.email}</div>
              </div>
            )}
          </div>

          <div style={{ border: "1px solid var(--pastel-turquoise-100)", borderRadius: "var(--radius)", padding: "20px" }}>
            <h3 style={{ margin: "0 0 16px", fontSize: "1rem", color: "var(--noveocare-gray-700)" }}>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ verticalAlign: "middle", marginRight: "8px" }}>
                <polyline points="23 4 23 10 17 10" /><path d="M20.49 15a9 9 0 11-2.12-9.36L23 10" />
              </svg>
              Auto-refresh
            </h3>
            <label className="toggle" style={{ fontSize: "0.95rem", color: "var(--noveocare-gray-600)" }}>
              <input type="checkbox" checked={autoRefresh} onChange={(e) => setAutoRefresh(e.target.checked)} />
              Refresh dashboard every 20 seconds
            </label>
            <p style={{ margin: "8px 0 0", fontSize: "0.8rem", color: "var(--noveocare-gray-400)" }}>
              Automatically reloads dashboard data to keep metrics up to date.
            </p>
          </div>

          <div style={{ border: "1px solid var(--pastel-turquoise-100)", borderRadius: "var(--radius)", padding: "20px" }}>
            <h3 style={{ margin: "0 0 16px", fontSize: "1rem", color: "var(--noveocare-gray-700)" }}>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ verticalAlign: "middle", marginRight: "8px" }}>
                <circle cx="12" cy="12" r="10" /><path d="M12 2a15.3 15.3 0 014 10 15.3 15.3 0 01-4 10 15.3 15.3 0 01-4-10 15.3 15.3 0 014-10z" />
              </svg>
              About
            </h3>
            <div style={{ fontSize: "0.9rem", color: "var(--noveocare-gray-500)", lineHeight: "1.6" }}>
              <p style={{ margin: "0 0 4px" }}><strong>AI Test Automation Dashboard</strong> v1.0.0</p>
              <p style={{ margin: "0 0 4px" }}>React 18 + Vite 5 &middot; FastAPI + PostgreSQL &middot; Python self-healing engine</p>
              <p style={{ margin: "0" }}>Pastel Turquoise &middot; Pastel Yellow &middot; Pastel Red</p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
