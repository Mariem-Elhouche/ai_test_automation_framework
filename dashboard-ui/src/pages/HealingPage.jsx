import { useCallback, useEffect, useState } from "react";

function formatDate(value) {
  if (!value) return "-";
  const dt = new Date(value);
  return Number.isNaN(dt.getTime()) ? value : dt.toLocaleString();
}

function makeLinePath(values, width, height, padding) {
  if (values.length < 2) return "";
  const min = Math.min(...values);
  const max = Math.max(...values);
  const span = max - min || 1;
  const xStep = (width - padding * 2) / (values.length - 1);
  return values.map((v, i) => {
    const x = padding + i * xStep;
    const y = height - padding - ((v - min) / span) * (height - padding * 2);
    return `${i === 0 ? "M" : "L"}${x} ${y}`;
  }).join(" ");
}

function MiniChart({ values, color, height = 80 }) {
  const width = 200;
  const padding = 8;
  if (values.length < 1) return <span className="empty" style={{ fontSize: "0.78rem" }}>No data</span>;
  if (values.length === 1) {
    const cx = width / 2;
    const cy = height / 2;
    return (
      <svg width={width} height={height} viewBox={`0 0 ${width} ${height}`}>
        <circle cx={cx} cy={cy} r="4" fill={color} />
        <text x={cx} y={cy + 14} textAnchor="middle" fontSize="10" fill="var(--noveocare-gray-400)">1 data point</text>
      </svg>
    );
  }
  const path = makeLinePath(values, width, height, padding);
  return (
    <svg width={width} height={height} viewBox={`0 0 ${width} ${height}`}>
      <defs>
        <linearGradient id={`h-${color.replace(/\W/g, "")}`} x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stopColor={color} stopOpacity="0.15" />
          <stop offset="100%" stopColor={color} stopOpacity="0" />
        </linearGradient>
      </defs>
      <path d={path + `L${width - padding} ${height - padding} L${padding} ${height - padding} Z`}
        fill={`url(#h-${color.replace(/\W/g, "")})`} />
      <path d={path} fill="none" stroke={color} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

const ROWS_PER_PAGE = 12;

export default function HealingPage({ baseUrl, token, runId }) {
  const [metrics, setMetrics] = useState(null);
  const [events, setEvents] = useState([]);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [page, setPage] = useState(0);

  const loadData = useCallback(async () => {
    if (!token) return;
    setLoading(true);
    setError("");
    try {
      const headers = { Authorization: `Bearer ${token}` };
      const evUrl = new URL("/api/healing-events", baseUrl);
      evUrl.searchParams.set("limit", "500");
      const dashUrl = new URL("/api/dashboard", baseUrl);
      if (runId?.trim()) dashUrl.searchParams.set("run_id", runId.trim());
      const [dRes, eRes, mRes] = await Promise.all([
        fetch(dashUrl, { headers }),
        fetch(evUrl, { headers }),
        fetch(new URL("/api/metrics/history?limit=50", baseUrl), { headers }),
      ]);
      let loadedEvents = null;
      if (eRes.ok) {
        const evData = await eRes.json();
        if (evData && evData.length > 0) {
          loadedEvents = evData;
        }
      }
      if (dRes.ok) {
        const data = await dRes.json();
        setMetrics(data.metrics);
        if (!loadedEvents && data.recent_events?.length > 0) {
          loadedEvents = data.recent_events;
        }
      }
      if (loadedEvents) { setEvents(loadedEvents); setPage(0); }
      if (mRes.ok) setHistory(await mRes.json());
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setLoading(false);
    }
  }, [baseUrl, token, runId]);

  useEffect(() => { loadData(); }, [loadData]);

  const buildSeries = (key) => history.map((r) => Number(r[key])).filter(Number.isFinite);

  return (
    <div>
      <section className="band">
        <div className="band-head">
          <h2>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
            </svg>
            Self-Healing Overview
          </h2>
        </div>
        {error && <div className="band error-box">{error}</div>}
        {metrics ? (
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(280px, 1fr))", gap: "20px" }}>
            <div style={{ border: "1px solid var(--brand-green-100)", borderRadius: "var(--radius)", padding: "16px", background: "linear-gradient(135deg, var(--brand-green-50) 0%, var(--bg-card) 100%)", boxShadow: "var(--shadow-sm)" }}>
              <div style={{ fontSize: "0.8rem", color: "var(--noveocare-gray-400)", fontWeight: 600, textTransform: "uppercase", letterSpacing: "0.3px" }}>Healing Rate</div>
              <div style={{ fontSize: "2rem", fontWeight: 700, color: "var(--brand-green-500)" }}>
                {metrics.healing_rate != null ? `${(metrics.healing_rate * 100).toFixed(1)}%` : "-"}
              </div>
              <MiniChart values={buildSeries("healing_rate")} color="var(--brand-green-500)" />
            </div>
            <div style={{ border: "1px solid var(--brand-blue-100)", borderRadius: "var(--radius)", padding: "16px", background: "linear-gradient(135deg, var(--brand-blue-50) 0%, var(--bg-card) 100%)", boxShadow: "var(--shadow-sm)" }}>
              <div style={{ fontSize: "0.8rem", color: "var(--noveocare-gray-400)", fontWeight: 600, textTransform: "uppercase", letterSpacing: "0.3px" }}>Baseline Hit Rate</div>
              <div style={{ fontSize: "2rem", fontWeight: 700, color: "var(--brand-blue-500)" }}>
                {metrics.baseline_hit_rate != null ? `${(metrics.baseline_hit_rate * 100).toFixed(1)}%` : "-"}
              </div>
              <MiniChart values={buildSeries("baseline_hit_rate")} color="var(--brand-blue-500)" />
            </div>
            <div style={{ border: "1px solid var(--brand-green-100)", borderRadius: "var(--radius)", padding: "16px", background: "linear-gradient(135deg, var(--brand-green-50) 0%, var(--bg-card) 100%)", boxShadow: "var(--shadow-sm)" }}>
              <div style={{ fontSize: "0.8rem", color: "var(--noveocare-gray-400)", fontWeight: 600, textTransform: "uppercase", letterSpacing: "0.3px" }}>Avg Final Score</div>
              <div style={{ fontSize: "2rem", fontWeight: 700, color: "var(--brand-green-500)" }}>
                {metrics.avg_final_score != null ? metrics.avg_final_score.toFixed(4) : "-"}
              </div>
              <MiniChart values={buildSeries("avg_final_score")} color="var(--brand-green-500)" />
            </div>
            <div style={{ border: "1px solid var(--brand-secondary-100)", borderRadius: "var(--radius)", padding: "16px", background: "linear-gradient(135deg, var(--brand-secondary-50) 0%, var(--bg-card) 100%)", boxShadow: "var(--shadow-sm)" }}>
              <div style={{ fontSize: "0.8rem", color: "var(--noveocare-gray-400)", fontWeight: 600, textTransform: "uppercase", letterSpacing: "0.3px" }}>Avg Semantic Score</div>
              <div style={{ fontSize: "2rem", fontWeight: 700, color: "var(--brand-secondary-500)" }}>
                {metrics.avg_semantic_score != null ? metrics.avg_semantic_score.toFixed(4) : "-"}
              </div>
              <MiniChart values={buildSeries("avg_semantic_score")} color="var(--brand-secondary-500)" />
            </div>
            <div style={{ border: "1px solid var(--brand-blue-100)", borderRadius: "var(--radius)", padding: "16px", background: "linear-gradient(135deg, var(--brand-blue-50) 0%, var(--bg-card) 100%)", boxShadow: "var(--shadow-sm)" }}>
              <div style={{ fontSize: "0.8rem", color: "var(--noveocare-gray-400)", fontWeight: 600, textTransform: "uppercase", letterSpacing: "0.3px" }}>Avg Healing Time</div>
              <div style={{ fontSize: "2rem", fontWeight: 700, color: "var(--brand-blue-500)" }}>
                {metrics.avg_healing_time_ms != null ? `${metrics.avg_healing_time_ms.toFixed(0)} ms` : "-"}
              </div>
              <MiniChart values={buildSeries("avg_healing_time_ms")} color="var(--brand-blue-500)" />
            </div>
            <div style={{ border: "1px solid var(--brand-secondary-100)", borderRadius: "var(--radius)", padding: "16px", background: "linear-gradient(135deg, var(--brand-secondary-50) 0%, var(--bg-card) 100%)", boxShadow: "var(--shadow-sm)" }}>
              <div style={{ fontSize: "0.8rem", color: "var(--noveocare-gray-400)", fontWeight: 600, textTransform: "uppercase", letterSpacing: "0.3px" }}>NLP Filter Efficiency</div>
              <div style={{ fontSize: "2rem", fontWeight: 700, color: "var(--brand-secondary-500)" }}>
                {metrics.nlp_filter_efficiency != null ? `${(metrics.nlp_filter_efficiency * 100).toFixed(1)}%` : "-"}
              </div>
              <MiniChart values={buildSeries("nlp_filter_efficiency")} color="var(--brand-secondary-500)" />
            </div>
          </div>
        ) : (
          <div className="empty">No healing data available yet.</div>
        )}
      </section>

      <section className="band" style={{ padding: "0" }}>
        <div className="band-head" style={{ padding: "20px 24px 12px", margin: "0" }}>
          <h2>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <line x1="8" y1="6" x2="21" y2="6" /><line x1="8" y1="12" x2="21" y2="12" /><line x1="8" y1="18" x2="21" y2="18" /><line x1="3" y1="6" x2="3.01" y2="6" /><line x1="3" y1="12" x2="3.01" y2="12" /><line x1="3" y1="18" x2="3.01" y2="18" />
            </svg>
            All Healing Events
          </h2>
          <span className="chip">{events.length} events</span>
        </div>
        {events.length > 0 ? (
          <div>
            <div className="table-wrap" style={{ border: "none", borderRadius: "0" }}>
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
                    <th>Baseline</th>
                  </tr>
                </thead>
                <tbody>
                  {events.slice(page * ROWS_PER_PAGE, (page + 1) * ROWS_PER_PAGE).map((row) => (
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
                      <td><strong>{row.score != null ? Number(row.score).toFixed(4) : "-"}</strong></td>
                      <td>{row.structural_score != null ? Number(row.structural_score).toFixed(4) : "-"}</td>
                      <td>{row.semantic_score != null ? Number(row.semantic_score).toFixed(4) : "-"}</td>
                      <td>
                        <span className={`status-badge ${row.success ? "status-success" : "status-failed"}`}>
                          {row.success ? "OK" : "FAIL"}
                        </span>
                      </td>
                      <td style={{ fontSize: "0.82rem", color: "var(--noveocare-gray-400)" }}>{row.healing_time_ms != null ? `${row.healing_time_ms}` : "-"}</td>
                      <td>{row.baseline_hit ? <span style={{ color: "var(--pastel-turquoise-500)" }}>Yes</span> : "No"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            {events.length > ROWS_PER_PAGE && (
              <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "12px", padding: "12px 0 4px" }}>
                <button onClick={() => setPage(Math.max(0, page - 1))} disabled={page === 0}
                  style={{ padding: "4px 12px", fontSize: "0.82rem", borderRadius: "6px" }}>
                  Previous
                </button>
                <span style={{ fontSize: "0.85rem", color: "var(--noveocare-gray-500)" }}>
                  Page {page + 1} / {Math.ceil(events.length / ROWS_PER_PAGE)}
                </span>
                <button onClick={() => setPage(page + 1)} disabled={(page + 1) * ROWS_PER_PAGE >= events.length}
                  style={{ padding: "4px 12px", fontSize: "0.82rem", borderRadius: "6px" }}>
                  Next
                </button>
              </div>
            )}
          </div>
        ) : (
          <div className="empty-state">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
            </svg>
            <h3>No healing events recorded</h3>
            <p>Events appear here when self-healing occurs during test execution.</p>
          </div>
        )}
      </section>
    </div>
  );
}
