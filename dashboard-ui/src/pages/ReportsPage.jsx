import { useCallback, useEffect, useState } from "react";

function formatSize(bytes) {
  if (!bytes) return "-";
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1048576) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / 1048576).toFixed(1)} MB`;
}

function FileIcon({ type, name }) {
  if (type === "directory") {
    return (
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="var(--pastel-yellow-500)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M22 19a2 2 0 01-2 2H4a2 2 0 01-2-2V5a2 2 0 012-2h5l2 3h9a2 2 0 012 2z" />
      </svg>
    );
  }
  const ext = name?.split(".").pop()?.toLowerCase();
  const color = ext === "html" ? "var(--pastel-turquoise-500)" : ext === "json" ? "var(--pastel-yellow-500)" : "var(--noveocare-gray-400)";
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z" /><polyline points="14 2 14 8 20 8" />
    </svg>
  );
}

function TreeNode({ item, depth, onOpen }) {
  const [expanded, setExpanded] = useState(depth < 1);
  const paddingLeft = 16 + depth * 20;

  if (item.type === "directory") {
    return (
      <div>
        <div
          style={{
            display: "flex", alignItems: "center", gap: "8px", padding: "6px 0",
            paddingLeft: `${paddingLeft}px`, cursor: "pointer", transition: "background 0.2s",
            borderRadius: "6px", color: "var(--noveocare-gray-600)", fontSize: "0.9rem",
          }}
          onClick={() => setExpanded(!expanded)}
          onMouseEnter={(e) => e.currentTarget.style.background = "var(--pastel-turquoise-50)"}
          onMouseLeave={(e) => e.currentTarget.style.background = "transparent"}
        >
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"
            style={{ transform: expanded ? "rotate(90deg)" : "none", transition: "transform 0.2s" }}>
            <polyline points="9 18 15 12 9 6" />
          </svg>
          <FileIcon type="directory" name={item.name} />
          <span style={{ fontWeight: 500 }}>{item.name}</span>
        </div>
        {expanded && item.children?.map((child, i) => (
          <TreeNode key={i} item={child} depth={depth + 1} onOpen={onOpen} />
        ))}
      </div>
    );
  }

  return (
    <div
      style={{
        display: "flex", alignItems: "center", gap: "8px", padding: "5px 0",
        paddingLeft: `${paddingLeft}px`, cursor: "pointer", transition: "background 0.2s",
        borderRadius: "6px", color: "var(--noveocare-gray-600)", fontSize: "0.88rem",
      }}
      onClick={() => onOpen(item)}
      onMouseEnter={(e) => e.currentTarget.style.background = "var(--pastel-turquoise-50)"}
      onMouseLeave={(e) => e.currentTarget.style.background = "transparent"}
    >
      <div style={{ width: "12px" }} />
      <FileIcon type="file" name={item.name} />
      <span style={{ flex: 1 }}>{item.name}</span>
      <span style={{ fontSize: "0.78rem", color: "var(--noveocare-gray-400)" }}>{formatSize(item.size)}</span>
    </div>
  );
}

export default function ReportsPage({ baseUrl, token }) {
  const [tree, setTree] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [selectedReport, setSelectedReport] = useState(null);

  const loadReports = useCallback(async () => {
    if (!token) return;
    setLoading(true);
    setError("");
    try {
      const res = await fetch(new URL("/api/reports", baseUrl), {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      setTree(await res.json());
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setLoading(false);
    }
  }, [baseUrl, token]);

  useEffect(() => { loadReports(); }, [loadReports]);

  const handleOpen = (file) => {
    const url = `${baseUrl}/reports/${file.path}`;
    setSelectedReport({ name: file.name, url });
  };

  const totalFiles = tree?.entries ? (function countFiles(entries) {
    let n = 0;
    for (const e of entries) {
      if (e.type === "file") n++;
      if (e.children) n += countFiles(e.children);
    }
    return n;
  })(tree.entries) : 0;

  return (
    <div>
      <section className="band">
        <div className="band-head">
          <h2>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z" /><polyline points="14 2 14 8 20 8" />
            </svg>
            Test Reports
          </h2>
          <div style={{ display: "flex", gap: "8px", alignItems: "center" }}>
            {tree && <span className="chip">{totalFiles} files</span>}
            <button onClick={loadReports} disabled={loading} style={{ padding: "6px 12px", fontSize: "0.85rem" }}>
              Refresh
            </button>
          </div>
        </div>
        {error && <div className="band error-box">{error}</div>}
        {tree?.error ? (
          <div className="empty-state">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
              <circle cx="12" cy="12" r="10" /><line x1="12" y1="8" x2="12" y2="12" /><line x1="12" y1="16" x2="12.01" y2="16" />
            </svg>
            <h3>Reports directory not found</h3>
            <p>Run <code>mvn test</code> to generate reports in <code>target/reports/</code></p>
          </div>
        ) : (
          <div style={{ display: "flex", gap: "24px", flexWrap: "wrap" }}>
            <div style={{ flex: "0 0 320px", minWidth: "260px" }}>
              {tree?.entries ? (
                <div style={{ border: "1px solid var(--pastel-turquoise-100)", borderRadius: "var(--radius)", padding: "8px 12px" }}>
                  <div style={{ fontSize: "0.78rem", fontWeight: 600, color: "var(--noveocare-gray-400)", textTransform: "uppercase", letterSpacing: "0.5px", marginBottom: "4px", padding: "4px 0" }}>
                    target/reports/
                  </div>
                  {tree.entries.map((item, i) => (
                    <TreeNode key={i} item={item} depth={0} onOpen={handleOpen} />
                  ))}
                </div>
              ) : (
                <div className="empty">Loading...</div>
              )}
            </div>
            <div style={{ flex: 1, minWidth: "400px" }}>
              {selectedReport ? (
                <div>
                  <div style={{ display: "flex", alignItems: "center", gap: "12px", marginBottom: "12px" }}>
                    <h3 style={{ margin: 0, fontSize: "1rem", color: "var(--noveocare-gray-700)" }}>{selectedReport.name}</h3>
                    <a href={selectedReport.url} target="_blank" rel="noreferrer" style={{ fontSize: "0.82rem", color: "var(--pastel-turquoise-500)" }}>
                      Open in new tab &nearr;
                    </a>
                    <button onClick={() => setSelectedReport(null)} style={{ padding: "4px 10px", fontSize: "0.8rem", marginLeft: "auto" }}>
                      Close
                    </button>
                  </div>
                  <div style={{ border: "1px solid var(--pastel-turquoise-100)", borderRadius: "var(--radius)", overflow: "hidden", height: "600px" }}>
                    <iframe
                      src={selectedReport.url}
                      title={selectedReport.name}
                      style={{ width: "100%", height: "100%", border: "none" }}
                    />
                  </div>
                </div>
              ) : (
                <div className="empty-state">
                  <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                    <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z" />
                  </svg>
                  <h3>Select a report</h3>
                  <p>Click on an HTML report file in the tree to preview it here.</p>
                </div>
              )}
            </div>
          </div>
        )}
      </section>
    </div>
  );
}
