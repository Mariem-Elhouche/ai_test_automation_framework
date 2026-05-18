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
        fetch(new URL("/api/cucumber-runs/recent?limit=50", baseUrl), { headers }),
      ]);
      if (sRes.ok) setSummary(await sRes.json());
      if (rRes.ok) { setRecent(await rRes.json()); setPage(0); }
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setLoading(false);
    }
  }, [baseUrl, token]);

  useEffect(() => { loadData(); }, [loadData]);

  const pct = (value) => (total ? ((value / total) * 100).toFixed(1) : "0.0");

  const total = summary ? (summary.passed || 0) + (summary.failed || 0) + (summary.skipped || 0) + (summary.flaky || 0) : 0;

  const passedColor = "var(--pastel-turquoise-400)";
  const failedColor = "var(--pastel-red-400)";
  const skippedColor = "var(--pastel-yellow-400)";
  const flakyColor = "var(--pastel-orange-400, #f4a460)";

  const donutSegments = [];
  if (total > 0) {
    let offset = 0;
    const items = [
      { value: summary.passed || 0, color: passedColor, label: "Passed" },
      { value: summary.flaky || 0, color: flakyColor, label: "Flaky" },
      { value: summary.failed || 0, color: failedColor, label: "Failed" },
      { value: summary.skipped || 0, color: skippedColor, label: "Skipped" },
    ];
    const circumference = 2 * Math.PI * 40;
    items.forEach((item) => {
      const length = (item.value / total) * circumference;
      donutSegments.push({ ...item, offset, length });
      offset += length;
    });
  }

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
            <button onClick={loadData} disabled={loading} style={{ padding: "6px 12px", fontSize: "0.85rem" }}>
              Refresh
            </button>
          </div>
        </div>
        {error && <div className="band error-box">{error}</div>}
        {summary ? (
          <div style={{ display: "flex", gap: "32px", alignItems: "center", flexWrap: "wrap" }}>
            <div style={{ position: "relative", width: "140px", height: "140px" }}>
              <svg width="140" height="140" viewBox="0 0 100 100">
                <circle cx="50" cy="50" r="40" fill="none" stroke="var(--noveocare-gray-100)" strokeWidth="8" />
                {donutSegments.map((seg, i) => (
                  <circle key={i} cx="50" cy="50" r="40" fill="none" stroke={seg.color} strokeWidth="8"
                    strokeDasharray={`${seg.length} ${2 * Math.PI * 40 - seg.length}`}
                    strokeDashoffset={-seg.offset}
                    transform="rotate(-90 50 50)"
                    style={{ transition: "stroke-dasharray 0.5s ease" }}
                  />
                ))}
                <text x="50" y="48" textAnchor="middle" fontSize="18" fontWeight="700" fill="var(--noveocare-gray-700)">{total}</text>
                <text x="50" y="62" textAnchor="middle" fontSize="8" fill="var(--noveocare-gray-500)">scenarios</text>
              </svg>
            </div>
            <div style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
              <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
                <span style={{ width: "12px", height: "12px", borderRadius: "50%", background: passedColor }} />
                <span style={{ minWidth: "80px", color: "var(--noveocare-gray-600)" }}>Passed</span>
                <strong style={{ minWidth: "40px" }}>{summary.passed || 0}</strong>
                <span style={{ color: "var(--noveocare-gray-400)", fontSize: "0.85rem" }}>({pct(summary.passed || 0)}%)</span>
              </div>
              <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
                <span style={{ width: "12px", height: "12px", borderRadius: "50%", background: flakyColor }} />
                <span style={{ minWidth: "80px", color: "var(--noveocare-gray-600)" }}>Flaky</span>
                <strong style={{ minWidth: "40px" }}>{summary.flaky || 0}</strong>
                <span style={{ color: "var(--noveocare-gray-400)", fontSize: "0.85rem" }}>({pct(summary.flaky || 0)}%)</span>
              </div>
              <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
                <span style={{ width: "12px", height: "12px", borderRadius: "50%", background: failedColor }} />
                <span style={{ minWidth: "80px", color: "var(--noveocare-gray-600)" }}>Failed</span>
                <strong style={{ minWidth: "40px" }}>{summary.failed || 0}</strong>
                <span style={{ color: "var(--noveocare-gray-400)", fontSize: "0.85rem" }}>({pct(summary.failed || 0)}%)</span>
              </div>
              <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
                <span style={{ width: "12px", height: "12px", borderRadius: "50%", background: skippedColor }} />
                <span style={{ minWidth: "80px", color: "var(--noveocare-gray-600)" }}>Skipped</span>
                <strong style={{ minWidth: "40px" }}>{summary.skipped || 0}</strong>
                <span style={{ color: "var(--noveocare-gray-400)", fontSize: "0.85rem" }}>({pct(summary.skipped || 0)}%)</span>
              </div>
              <div style={{ marginTop: "8px", fontSize: "0.85rem", color: "var(--noveocare-gray-500)" }}>
                Run ID: <code style={{ background: "var(--pastel-turquoise-50)", padding: "2px 6px", borderRadius: "4px" }}>{summary.run_id || "-"}</code>
                {summary.run_at && <> &middot; {formatDate(summary.run_at)}</>}
              </div>
            </div>
          </div>
        ) : (
          <div className="empty">No test results yet. Run <code>mvn test</code> first.</div>
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
            <p>Run tests first to see results here.</p>
          </div>
        )}
      </section>
    </div>
  );
}
