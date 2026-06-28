import { useCallback, useEffect, useState } from "react";

function formatDate(value) {
  if (!value) return "-";
  const dt = new Date(value);
  return Number.isNaN(dt.getTime()) ? value : dt.toLocaleString();
}

const ROWS_PER_PAGE = 12;

export default function TestsPage({ baseUrl, token }) {
  const [summary, setSummary] = useState(null);
  const [page, setPage] = useState(0);
  const [recent, setRecent] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const loadData = useCallback(async () => {
    if (!token) return;
    setLoading(true);
    setError("");
    try {
      const headers = { Authorization: `Bearer ${token}` };
      const [sRes, rRes] = await Promise.all([
        fetch(new URL("/api/cucumber-runs/summary", baseUrl), { headers }),
        fetch(new URL("/api/cucumber-runs/recent?limit=200", baseUrl), { headers }),
      ]);
      if (!sRes.ok && !rRes.ok) {
        setError(`API error (${sRes.status}/${rRes.status})`);
        return;
      }
      if (sRes.ok) setSummary(await sRes.json());
      if (rRes.ok) { setRecent(await rRes.json()); setPage(0); }
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setLoading(false);
    }
  }, [baseUrl, token]);

  useEffect(() => { loadData(); }, [loadData]);

  const total = summary ? (summary.passed || 0) + (summary.failed || 0) + (summary.skipped || 0) + (summary.flaky || 0) : 0;
  const pct = (v) => total ? ((v / total) * 100).toFixed(1) : "0.0";
  const currentRunMs = summary?.run_id && recent.length > 0
    ? (() => {
        const runScenarios = recent.filter(r => r.run_id === summary.run_id);
        if (runScenarios.length === 0) return null;
        const totalNs = runScenarios.reduce((s, r) => s + (r.duration_ns || 0), 0);
        return (totalNs / 1_000_000).toFixed(0);
      })()
    : null;
  const successRatePct = total > 0 ? ((summary.passed || 0) / total * 100).toFixed(1) : null;

  return (
    <div>
      <section className="band">
        <div className="band-head">
          <h2>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <polyline points="9 11 12 14 22 4" /><path d="M21 12v7a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h11" />
            </svg>
            Test Results
          </h2>
          <div style={{ display: "flex", gap: "8px", alignItems: "center" }}>
            {loading && <span className="chip">Loading...</span>}
          </div>
        </div>
        {error && <div className="band error-box" style={{ marginBottom: "12px" }}>{error}</div>}
        {summary ? (
          <div>
            <div style={{ marginBottom: "20px", display: "flex", alignItems: "center", gap: "12px" }}>
              <div style={{ flex: 1, height: "32px", borderRadius: "var(--radius)", overflow: "hidden", display: "flex", background: "var(--noveocare-gray-100)", position: "relative" }}>
                {total > 0 && [
                  { v: summary.passed || 0, color: "var(--brand-green-400)" },
                  { v: summary.flaky || 0, color: "#f4a460" },
                  { v: summary.failed || 0, color: "var(--pastel-red-400)" },
                  { v: summary.skipped || 0, color: "var(--pastel-yellow-400)" },
                ].map((item) => {
                  const w = (item.v / total * 100).toFixed(1);
                  return parseFloat(w) > 0 ? (
                    <div key={item.color} style={{ width: w + "%", height: "100%", background: item.color, transition: "width 0.6s ease" }} />
                  ) : null;
                })}
              </div>
              <div style={{ fontSize: "1.1rem", fontWeight: 700, color: "var(--noveocare-gray-700)", whiteSpace: "nowrap" }}>{total} scenarios</div>
            </div>
            <div style={{ display: "flex", gap: "48px", justifyContent: "center" }}>
              <div style={{ textAlign: "center" }}>
                <div style={{ fontSize: "0.7rem", fontWeight: 600, textTransform: "uppercase", letterSpacing: "0.5px", color: "var(--noveocare-gray-400)", marginBottom: "4px" }}>Success Rate</div>
                <div style={{ fontSize: "1.1rem", fontWeight: 700, lineHeight: "1.1", color: successRatePct >= 90 ? "var(--brand-green-500)" : successRatePct >= 70 ? "#d4880f" : "var(--pastel-red-500)" }}>{successRatePct}%</div>
              </div>
              <div style={{ textAlign: "center" }}>
                <div style={{ fontSize: "0.7rem", fontWeight: 600, textTransform: "uppercase", letterSpacing: "0.5px", color: "var(--noveocare-gray-400)", marginBottom: "4px" }}>Run Time</div>
                <div style={{ fontSize: "1.1rem", fontWeight: 700, lineHeight: "1.1" }}>{currentRunMs ? parseInt(currentRunMs).toLocaleString() : "—"} <span style={{ fontSize: "0.85rem", fontWeight: 400, color: "var(--noveocare-gray-400)" }}>ms</span></div>
              </div>
              <div style={{ textAlign: "center" }}>
                <div style={{ fontSize: "0.7rem", fontWeight: 600, textTransform: "uppercase", letterSpacing: "0.5px", color: "var(--noveocare-gray-400)", marginBottom: "4px" }}>Last Run</div>
                {(() => {
                  const parts = formatDate(summary.run_at).split(", ");
                  return (
                    <>
                      <div style={{ fontSize: "1.1rem", fontWeight: 700, lineHeight: "1.1" }}>{parts[0] || formatDate(summary.run_at)}</div>
                      {parts[1] && <div style={{ fontSize: "0.85rem", color: "var(--noveocare-gray-400)", marginTop: "4px" }}>{parts[1]}</div>}
                    </>
                  );
                })()}
              </div>
            </div>
            <div style={{ display: "flex", marginTop: "20px", paddingTop: "16px", borderTop: "1px solid var(--border-color)" }}>
              {[
                { label: "Passed", value: summary.passed || 0, color: "var(--brand-green-400)" },
                { label: "Flaky", value: summary.flaky || 0, color: "#f4a460" },
                { label: "Failed", value: summary.failed || 0, color: "var(--pastel-red-400)" },
                { label: "Skipped", value: summary.skipped || 0, color: "var(--pastel-yellow-400)" },
              ].map((item, i, arr) => (
                <div key={item.label} style={{
                  flex: 1, display: "flex", alignItems: "center", justifyContent: "center", gap: "10px", padding: "6px 0",
                  borderRight: i < arr.length - 1 ? "1px solid var(--border-color)" : "none",
                }}>
                  <span style={{ width: "12px", height: "12px", borderRadius: "50%", background: item.color, flexShrink: 0 }} />
                  <span style={{ color: "var(--noveocare-gray-600)", fontSize: "0.85rem" }}>{item.label}</span>
                  <strong style={{ fontSize: "0.9rem" }}>{item.value}</strong>
                  <span style={{ color: "var(--noveocare-gray-400)", fontSize: "0.8rem" }}>({pct(item.value)}%)</span>
                </div>
              ))}
            </div>
          </div>
        ) : (
          <div className="empty-state">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
              <polyline points="9 11 12 14 22 4" /><path d="M21 12v7a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h11" />
            </svg>
            <h3>No test results yet</h3>
            <p>Go to <strong>Test Runner</strong> to execute tests.</p>
          </div>
        )}
      </section>

      <section className="band" style={{ padding: "0" }}>
        <div className="band-head" style={{ padding: "20px 24px 12px", margin: "0" }}>
          <h2>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <line x1="8" y1="6" x2="21" y2="6" /><line x1="8" y1="12" x2="21" y2="12" /><line x1="8" y1="18" x2="21" y2="18" /><line x1="3" y1="6" x2="3.01" y2="6" /><line x1="3" y1="12" x2="3.01" y2="12" /><line x1="3" y1="18" x2="3.01" y2="18" />
            </svg>
            Recent Scenarios
          </h2>
          <span className="chip">{recent.length} entries</span>
        </div>
        {recent.length > 0 ? (
          <div>
            <div className="table-wrap" style={{ border: "none", borderRadius: "0" }}>
              <table>
                <thead>
                  <tr>
                    <th>Time</th>
                    <th>Feature</th>
                    <th>Scenario</th>
                    <th>Status</th>
                    <th>Duration</th>
                    <th>Tags</th>
                  </tr>
                </thead>
                <tbody>
                  {recent.slice(page * ROWS_PER_PAGE, (page + 1) * ROWS_PER_PAGE).map((row) => (
                    <tr key={row.id}>
                      <td style={{ whiteSpace: "nowrap", fontSize: "0.82rem" }}>{formatDate(row.run_at)}</td>
                      <td>{row.feature_name || "-"}</td>
                      <td><div className="scenario-cell"><span className="scenario-name">{row.scenario || "-"}</span></div></td>
                      <td>
                        <span className={`status-badge ${row.status === "passed" ? "status-success" : row.status === "flaky" ? "status-flaky" : row.status === "failed" ? "status-failed" : "status-skipped"}`}>
                          {row.status || "skipped"}
                        </span>
                      </td>
                      <td>{row.duration_ns ? `${(row.duration_ns / 1_000_000).toFixed(0)} ms` : "-"}</td>
                      <td style={{ fontSize: "0.8rem", color: "var(--noveocare-gray-400)" }}>{row.tags || "-"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            {recent.length > ROWS_PER_PAGE && (
              <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "12px", padding: "12px 0 4px" }}>
                <button onClick={() => setPage(Math.max(0, page - 1))} disabled={page === 0}
                  style={{ padding: "4px 12px", fontSize: "0.82rem", borderRadius: "6px" }}>
                  Previous
                </button>
                <span style={{ fontSize: "0.85rem", color: "var(--noveocare-gray-500)" }}>
                  Page {page + 1} / {Math.ceil(recent.length / ROWS_PER_PAGE)}
                </span>
                <button onClick={() => setPage(page + 1)} disabled={(page + 1) * ROWS_PER_PAGE >= recent.length}
                  style={{ padding: "4px 12px", fontSize: "0.82rem", borderRadius: "6px" }}>
                  Next
                </button>
              </div>
            )}
          </div>
        ) : (
          <div className="empty-state" style={{ padding: "40px 20px" }}>
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
              <polyline points="9 11 12 14 22 4" /><path d="M21 12v7a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h11" />
            </svg>
            <h3>No scenarios recorded</h3>
            <p>Run tests from <strong>Test Runner</strong> to see individual results here.</p>
          </div>
        )}
      </section>
    </div>
  );
}
