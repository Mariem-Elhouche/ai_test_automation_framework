import { useCallback, useEffect, useRef, useState } from "react";

const SUITE_OPTIONS = [
  { id: "login", label: "Login", tag: "@login", default: true },
  { id: "companies", label: "Companies", tag: "@companies" },
  { id: "categories", label: "Categories", tag: "@categories" },
  { id: "groups", label: "Groups", tag: "@company_groups" },
  { id: "environments", label: "Environments", tag: "@environments" },
  { id: "graphic-charter", label: "Graphic Charter", tag: "@graphicCharter" },
  { id: "regression", label: "Regression", tag: "@regression" },
  { id: "smoke", label: "Smoke", tag: "@smoke" },
  { id: "documents", label: "Documents", tag: "@documents" },
  { id: "digitalspace", label: "Digital Space", tag: "@digitalspace" },
  { id: "assures", label: "Assures", tag: "@assures" },
  { id: "env-company", label: "Env. Company", tag: "@company" },
  { id: "attachments", label: "Attachments", tag: "@attachments" },
];

function formatDate(value) {
  if (!value) return "-";
  const dt = new Date(value);
  return Number.isNaN(dt.getTime()) ? value : dt.toLocaleString();
}

function TagSelector({ selected, onChange }) {
  return (
    <div className="filters">
      {SUITE_OPTIONS.map((opt) => (
        <label key={opt.id} className="field" style={{ flexDirection: "row", alignItems: "center", gap: "8px", cursor: "pointer" }}>
          <input
            type="checkbox"
            checked={selected.includes(opt.tag)}
            onChange={(e) => {
              if (e.target.checked) {
                onChange([...selected, opt.tag]);
              } else {
                onChange(selected.filter((t) => t !== opt.tag));
              }
            }}
            style={{ accentColor: "var(--brand-green-400)", cursor: "pointer", width: "16px", height: "16px" }}
          />
          <span style={{ fontSize: "0.9rem", color: "var(--noveocare-gray-600)" }}>{opt.label}</span>
          <code style={{ fontSize: "0.75rem", color: "var(--noveocare-gray-400)", background: "var(--noveocare-gray-100)", padding: "1px 6px", borderRadius: "4px" }}>{opt.tag}</code>
        </label>
      ))}
    </div>
  );
}

function LogViewer({ logs }) {
  const bottomRef = useRef(null);
  useEffect(() => {
    if (bottomRef.current) {
      bottomRef.current.scrollIntoView({ behavior: "smooth" });
    }
  }, [logs.length]);
  return (
    <div style={{
      background: "#1e1e2e", color: "#cdd6f4", fontFamily: "'JetBrains Mono', 'Fira Code', 'Consolas', monospace",
      fontSize: "0.78rem", lineHeight: "1.5", padding: "16px", borderRadius: "var(--radius)",
      maxHeight: "400px", overflow: "auto", whiteSpace: "pre-wrap", wordBreak: "break-all",
      border: "1px solid var(--noveocare-gray-300)", marginTop: "12px",
    }}>
      {logs.length === 0 ? (
        <span style={{ opacity: 0.5 }}>Waiting for output...</span>
      ) : (
        logs.map((line, i) => (
          <div key={i} style={{
            color: line.includes("[runner]") ? "var(--brand-green-400)" :
                   line.includes("PASSED") || line.includes("passed") ? "#a6e3a1" :
                   line.includes("FAILED") || line.includes("failed") ? "#f38ba8" :
                   line.includes("WARN") ? "#f9e2af" :
                   line.includes("ERROR") ? "#f38ba8" : "inherit",
          }}>{line}</div>
        ))
      )}
      <div ref={bottomRef} />
    </div>
  );
}

function StatusBadge({ status }) {
  const colors = {
    pending: { bg: "var(--pastel-yellow-100)", color: "#8b7a2e" },
    running: { bg: "var(--brand-green-100)", color: "var(--brand-green-400)" },
    completed: { bg: "var(--brand-green-100)", color: "var(--brand-green-400)" },
    failed: { bg: "var(--pastel-red-100)", color: "var(--pastel-red-600)" },
    cancelled: { bg: "var(--noveocare-gray-200)", color: "var(--noveocare-gray-500)" },
  };
  const c = colors[status] || colors.pending;
  return (
    <span className="status-badge" style={{ background: c.bg, color: c.color }}>
      {status === "running" && <span className="topbar-info-dot run" style={{ animation: "pulse 1s infinite" }} />}
      {status}
    </span>
  );
}

const PAGE_SIZE = 10;

export default function TestRunnerPage({ baseUrl, token }) {
  const [selectedTags, setSelectedTags] = useState(["@login"]);
  const [customTags, setCustomTags] = useState("");
  const [runnerType, setRunnerType] = useState("GenericTagRunner");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [activeRun, setActiveRun] = useState(null);
  const [history, setHistory] = useState([]);
  const [currentPage, setCurrentPage] = useState(0);
  const pollRef = useRef(null);

  const totalPages = Math.ceil(history.length / PAGE_SIZE) || 1;
  const pageItems = history.slice(currentPage * PAGE_SIZE, (currentPage + 1) * PAGE_SIZE);

  const fetchHistory = useCallback(async () => {
    if (!token) return;
    try {
      const res = await fetch(new URL("/api/run-tests?limit=100", baseUrl), {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (res.ok) {
        const data = await res.json();
        setHistory(data);
        setCurrentPage(0);
      }
    } catch {}
  }, [baseUrl, token]);

  useEffect(() => { fetchHistory(); }, [fetchHistory]);

  const pollRun = useCallback(async (runId) => {
    if (!token || !runId) return;
    try {
      const res = await fetch(new URL(`/api/run-tests/${runId}`, baseUrl), {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (res.ok) {
        const data = await res.json();
        setActiveRun(data);
        if (data.status === "completed" || data.status === "failed" || data.status === "cancelled") {
          fetchHistory();
          if (pollRef.current) { clearInterval(pollRef.current); pollRef.current = null; }
        }
      }
    } catch {}
  }, [baseUrl, token, fetchHistory]);

  const startRun = async () => {
    setError("");
    setLoading(true);
    setActiveRun(null);
    const tags = customTags.trim() || selectedTags.join(" or ");
    try {
      const res = await fetch(new URL("/api/run-tests", baseUrl), {
        method: "POST",
        headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
        body: JSON.stringify({ tags, runner: runnerType }),
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();
      setActiveRun(data);
      if (pollRef.current) clearInterval(pollRef.current);
      pollRef.current = setInterval(() => pollRun(data.run_id), 2000);
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setLoading(false);
    }
  };

  const cancelRun = async () => {
    if (!activeRun?.run_id) return;
    try {
      const res = await fetch(new URL(`/api/run-tests/${activeRun.run_id}`, baseUrl), {
        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` },
      });
      if (res.ok) {
        setActiveRun((prev) => ({ ...prev, status: "cancelled" }));
        if (pollRef.current) { clearInterval(pollRef.current); pollRef.current = null; }
        fetchHistory();
      }
    } catch {}
  };

  const stopPolling = () => {
    if (pollRef.current) { clearInterval(pollRef.current); pollRef.current = null; }
    setActiveRun(null);
  };

  useEffect(() => {
    return () => { if (pollRef.current) clearInterval(pollRef.current); };
  }, []);

  const tagsForDisplay = customTags.trim() || selectedTags.join(" or ");

  return (
    <div>
      <section className="band">
        <div className="band-head">
          <h2>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <polyline points="23 4 23 10 17 10" /><path d="M20.49 15a9 9 0 11-2.12-9.36L23 10" />
            </svg>
            Test Runner
          </h2>
        </div>

        <div style={{ display: "grid", gap: "20px" }}>
          <div>
            <h3 style={{ margin: "0 0 12px", fontSize: "0.95rem", color: "var(--noveocare-gray-600)" }}>Select feature tags</h3>
            <TagSelector selected={selectedTags} onChange={setSelectedTags} />
          </div>

          <div className="field">
            <label>Custom tag expression (overrides selection above)</label>
            <input
              value={customTags}
              onChange={(e) => setCustomTags(e.target.value)}
              placeholder={'e.g. @smoke and @positive or @regression'}
              style={{ fontFamily: "monospace", fontSize: "0.85rem" }}
            />
          </div>

          <div className="field">
            <label>Runner class</label>
            <select
              value={runnerType}
              onChange={(e) => setRunnerType(e.target.value)}
              style={{
                border: "2px solid var(--noveocare-gray-200)", borderRadius: "var(--radius)",
                padding: "10px 14px", fontSize: "0.9rem", background: "white",
                color: "var(--noveocare-gray-700)", cursor: "pointer",
              }}
            >
              <option value="GenericTagRunner">GenericTagRunner (tag-based)</option>
              <option value="AllTestsRunner">AllTestsRunner (all scenarios)</option>
              <option value="LoginTestRunner">LoginTestRunner (@login only)</option>
            </select>
          </div>

          {error && <div className="error-box">{error}</div>}

          <div style={{ display: "flex", gap: "12px", alignItems: "center" }}>
            <button onClick={startRun} disabled={loading || !tagsForDisplay || (activeRun?.status === "running")}
              style={{ background: "var(--brand-green-400)" }}>
              {loading ? "Starting..." : (activeRun?.status === "running" ? "Running..." : "Run Tests")}
            </button>
            {activeRun && (
              <button className="secondary-btn" onClick={stopPolling}>Stop watching</button>
            )}
            <span style={{ fontSize: "0.85rem", color: "var(--noveocare-gray-400)" }}>
              Tags: <code style={{ background: "var(--noveocare-gray-100)", padding: "2px 6px", borderRadius: "4px" }}>{tagsForDisplay || "-"}</code>
            </span>
          </div>
        </div>
      </section>

      {activeRun && (
        <section className="band">
          <div className="band-head">
            <h2>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <line x1="8" y1="6" x2="21" y2="6" /><line x1="8" y1="12" x2="21" y2="12" /><line x1="8" y1="18" x2="21" y2="18" /><line x1="3" y1="6" x2="3.01" y2="6" /><line x1="3" y1="12" x2="3.01" y2="12" /><line x1="3" y1="18" x2="3.01" y2="18" />
              </svg>
              Active Run
            </h2>
            <div style={{ display: "flex", gap: "8px", alignItems: "center" }}>
              <StatusBadge status={activeRun.status} />
              <button onClick={cancelRun} style={{ background: "#f38ba8", color: "white", border: "none", padding: "6px 14px", borderRadius: "var(--radius)", cursor: "pointer", fontWeight: 600, fontSize: "0.82rem" }}>Cancel</button>
              {activeRun.exit_code !== null && (
                <span className="chip">Exit code: {activeRun.exit_code}</span>
              )}
            </div>
          </div>
          <div style={{ display: "flex", gap: "16px", flexWrap: "wrap", marginBottom: "8px", fontSize: "0.85rem", color: "var(--noveocare-gray-500)" }}>
            <span>Run ID: <code style={{ background: "var(--noveocare-gray-100)", padding: "2px 6px", borderRadius: "4px" }}>{activeRun.run_id}</code></span>
            <span>Started: {formatDate(activeRun.created_at)}</span>
            {activeRun.completed_at && <span>Completed: {formatDate(activeRun.completed_at)}</span>}
            <span>Tags: {activeRun.tags}</span>
          </div>
          <LogViewer logs={activeRun.logs || []} />
        </section>
      )}

      <section className="band">
        <div className="band-head">
          <h2>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <circle cx="12" cy="12" r="10" /><polyline points="12 6 12 12 16 14" />
            </svg>
            Recent runs
          </h2>
          <span className="chip">{history.length} runs</span>
        </div>
        {history.length > 0 ? (
          <div>
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Run ID</th>
                    <th>Tags</th>
                    <th>Status</th>
                    <th>Started</th>
                    <th>Exit</th>
                    <th>Logs</th>
                  </tr>
                </thead>
                <tbody>
                  {pageItems.map((run) => (
                    <tr key={run.run_id} onClick={() => setActiveRun(run)} style={{ cursor: "pointer" }}>
                      <td><code style={{ fontSize: "0.75rem", background: "var(--noveocare-gray-100)", padding: "2px 4px", borderRadius: "4px" }}>{run.run_id}</code></td>
                      <td style={{ fontSize: "0.82rem" }}>{run.tags}</td>
                      <td><StatusBadge status={run.status} /></td>
                      <td style={{ fontSize: "0.82rem" }}>{formatDate(run.created_at)}</td>
                      <td>{run.exit_code !== null ? run.exit_code : "-"}</td>
                      <td style={{ fontSize: "0.82rem", color: "var(--noveocare-gray-400)" }}>{run.logs?.length || 0} lines</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            {totalPages > 1 && (
              <div className="pagination" style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "8px", padding: "12px 0" }}>
                <button
                  disabled={currentPage === 0}
                  onClick={() => setCurrentPage(currentPage - 1)}
                  style={{ padding: "4px 12px", borderRadius: "6px", border: "1px solid var(--noveocare-gray-300)", background: "var(--noveocare-gray-100)", cursor: currentPage === 0 ? "default" : "pointer", opacity: currentPage === 0 ? 0.5 : 1 }}
                >
                  Prev
                </button>
                <span style={{ fontSize: "0.82rem", color: "var(--noveocare-gray-500)" }}>
                  {currentPage + 1} / {totalPages}
                </span>
                <button
                  disabled={currentPage >= totalPages - 1}
                  onClick={() => setCurrentPage(currentPage + 1)}
                  style={{ padding: "4px 12px", borderRadius: "6px", border: "1px solid var(--noveocare-gray-300)", background: "var(--noveocare-gray-100)", cursor: currentPage >= totalPages - 1 ? "default" : "pointer", opacity: currentPage >= totalPages - 1 ? 0.5 : 1 }}
                >
                  Next
                </button>
              </div>
            )}
          </div>
        ) : (
          <div className="empty">No runs yet. Select tags and click "Run Tests".</div>
        )}
      </section>
    </div>
  );
}
