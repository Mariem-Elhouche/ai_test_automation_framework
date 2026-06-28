import { useCallback, useEffect, useState } from "react";

const ROWS_PER_PAGE = 12;

const ROLE_LABELS = {
  admin: "Administrator",
  project_manager: "Project Manager",
  qa_engineer: "QA Engineer",
};

export default function UserManagementPage({ baseUrl, token, actor }) {
  const [users, setUsers] = useState([]);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [editUser, setEditUser] = useState(null);

  const [formEmail, setFormEmail] = useState("");
  const [formPassword, setFormPassword] = useState("");
  const [formName, setFormName] = useState("");
  const [formRole, setFormRole] = useState("qa_engineer");
  const [formError, setFormError] = useState("");

  const isAdmin = actor?.role === "admin";

  const headers = useCallback(() => ({
    "Content-Type": "application/json",
    Authorization: `Bearer ${token}`,
  }), [token]);

  const loadUsers = useCallback(async () => {
    if (!token) return;
    setLoading(true);
    setError("");
    try {
      const res = await fetch(new URL("/api/users", baseUrl), { headers: headers() });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      setUsers(await res.json());
      setPage(0);
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setLoading(false);
    }
  }, [baseUrl, token, headers]);

  useEffect(() => { loadUsers(); }, [loadUsers]);

  const resetForm = () => {
    setFormEmail("");
    setFormPassword("");
    setFormName("");
    setFormRole("qa_engineer");
    setFormError("");
    setShowForm(false);
    setEditUser(null);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setFormError("");
    try {
      if (editUser) {
        const res = await fetch(new URL(`/api/users/${editUser.id}`, baseUrl), {
          method: "PUT", headers: headers(),
          body: JSON.stringify({ email: formEmail, display_name: formName, role: formRole }),
        });
        if (!res.ok) {
          const errData = await res.json().catch(() => ({}));
          throw new Error(errData.detail || `HTTP ${res.status}`);
        }
        if (formPassword && formPassword.length >= 4) {
          const pwRes = await fetch(new URL(`/api/users/${editUser.id}/password`, baseUrl), {
            method: "PUT", headers: headers(),
            body: JSON.stringify({ new_password: formPassword }),
          });
          if (!pwRes.ok) {
            const errData = await pwRes.json().catch(() => ({}));
            throw new Error("User updated but password change failed: " + (errData.detail || `HTTP ${pwRes.status}`));
          }
        }
      } else {
        const res = await fetch(new URL("/api/users", baseUrl), {
          method: "POST", headers: headers(),
          body: JSON.stringify({ email: formEmail, password: formPassword, display_name: formName, role: formRole }),
        });
        if (!res.ok) {
          const errData = await res.json().catch(() => ({}));
          throw new Error(errData.detail || `HTTP ${res.status}`);
        }
      }
      resetForm();
      loadUsers();
    } catch (err) {
      setFormError(err instanceof Error ? err.message : String(err));
    }
  };

  const handleDelete = async (userId) => {
    if (!window.confirm("Delete this user?")) return;
    try {
      const res = await fetch(new URL(`/api/users/${userId}`, baseUrl), { method: "DELETE", headers: headers() });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      loadUsers();
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    }
  };

  const edit = (user) => {
    setEditUser(user);
    setFormEmail(user.email);
    setFormName(user.display_name);
    setFormRole(user.role);
    setFormPassword("");
    setShowForm(true);
  };

  return (
    <div>
      <section className="band">
        <div className="band-head">
          <h2>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2" />
              <circle cx="9" cy="7" r="4" />
              <path d="M23 21v-2a4 4 0 00-3-3.87" />
              <path d="M16 3.13a4 4 0 010 7.75" />
            </svg>
            User Management
          </h2>
          <div style={{ display: "flex", gap: "8px", alignItems: "center" }}>
            {loading && <span className="chip">Loading...</span>}
            {isAdmin && (
              <button className="secondary-btn" onClick={() => { resetForm(); setShowForm(true); }} style={{ padding: "6px 12px", fontSize: "0.85rem" }}>
                + Add User
              </button>
            )}
          </div>
        </div>

        {error && <div className="band error-box" style={{ marginBottom: "12px" }}>{error}</div>}

        {users.length > 0 ? (
          <>
          <div className="table-wrap" style={{ border: "none", borderRadius: "0" }}>
            <table>
              <thead>
                <tr>
                  <th>Email</th>
                  <th>Display Name</th>
                  <th>Role</th>
                  <th>Active</th>
                  <th>Created</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.slice(page * ROWS_PER_PAGE, (page + 1) * ROWS_PER_PAGE).map((user) => (
                  <tr key={user.id}>
                    <td><code style={{ fontSize: "0.82rem" }}>{user.email}</code></td>
                    <td>{user.display_name}</td>
                    <td><span className={`chip ${user.role === "admin" ? "chip-admin" : user.role === "project_manager" ? "chip-pm" : "chip-qa"}`}>{ROLE_LABELS[user.role] || user.role}</span></td>
                    <td><span className={`status-badge ${user.is_active ? "status-success" : "status-skipped"}`}>{user.is_active ? "Active" : "Disabled"}</span></td>
                    <td style={{ fontSize: "0.82rem", color: "var(--noveocare-gray-500)" }}>{user.created_at ? new Date(user.created_at).toLocaleDateString() : "-"}</td>
                    <td>
                      <div style={{ display: "flex", gap: "6px" }}>
                        <button onClick={() => edit(user)} style={{
                          padding: "4px 12px",
                          fontSize: "0.78rem",
                          borderRadius: "6px",
                          border: "1px solid var(--pastel-turquoise-200)",
                          background: "transparent",
                          color: "var(--pastel-turquoise-600)",
                          cursor: "pointer",
                          fontWeight: 600,
                          transition: "all 0.2s",
                        }}
                          onMouseEnter={(e) => { e.target.style.background = "var(--pastel-turquoise-50)"; }}
                          onMouseLeave={(e) => { e.target.style.background = "transparent"; }}
                        >
                          Edit
                        </button>
                        <button onClick={() => handleDelete(user.id)} style={{
                          padding: "4px 12px",
                          fontSize: "0.78rem",
                          borderRadius: "6px",
                          border: "1px solid var(--pastel-red-200)",
                          background: "transparent",
                          color: "var(--pastel-red-500)",
                          cursor: "pointer",
                          fontWeight: 600,
                          transition: "all 0.2s",
                        }}
                          onMouseEnter={(e) => { e.target.style.background = "var(--pastel-red-50)"; }}
                          onMouseLeave={(e) => { e.target.style.background = "transparent"; }}
                        >
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {users.length > ROWS_PER_PAGE && (
            <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "12px", padding: "12px 0 4px" }}>
              <button onClick={() => setPage(Math.max(0, page - 1))} disabled={page === 0}
                style={{ padding: "4px 12px", fontSize: "0.82rem", borderRadius: "6px" }}>
                Previous
              </button>
              <span style={{ fontSize: "0.85rem", color: "var(--noveocare-gray-500)" }}>
                Page {page + 1} / {Math.ceil(users.length / ROWS_PER_PAGE)}
              </span>
              <button onClick={() => setPage(page + 1)} disabled={(page + 1) * ROWS_PER_PAGE >= users.length}
                style={{ padding: "4px 12px", fontSize: "0.82rem", borderRadius: "6px" }}>
                Next
              </button>
            </div>
          )}
          </>
        ) : (
          <div className="empty-state">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
              <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2" />
              <circle cx="9" cy="7" r="4" />
              <path d="M23 21v-2a4 4 0 00-3-3.87" />
              <path d="M16 3.13a4 4 0 010 7.75" />
            </svg>
            <h3>No users</h3>
            <p>Users will appear here after being created.</p>
          </div>
        )}
      </section>

      {showForm && (
        <div className="modal-overlay" onClick={resetForm}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <button className="modal-close" onClick={resetForm}>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
            <div style={{
              display: "flex", alignItems: "center", gap: "10px",
              marginBottom: "24px", paddingBottom: "16px",
              borderBottom: "1px solid var(--border-color)",
            }}>
              <div style={{
                width: "36px", height: "36px", borderRadius: "var(--radius)",
                background: "var(--brand-primary-100)", display: "flex",
                alignItems: "center", justifyContent: "center", color: "var(--brand-primary-600)",
              }}>
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  {editUser ? <><path d="M20 14.66V20a2 2 0 01-2 2H4a2 2 0 01-2-2V6a2 2 0 012-2h5.34" /><polygon points="18 2 22 6 12 16 8 16 8 12 18 2" /></> : <><path d="M16 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2" /><circle cx="8.5" cy="7" r="4" /><line x1="20" y1="8" x2="20" y2="14" /><line x1="23" y1="11" x2="17" y2="11" /></>}
                </svg>
              </div>
              <div>
                <h3 style={{ margin: "0", fontSize: "1.1rem", fontWeight: 700, color: "var(--text-primary)" }}>{editUser ? "Edit User" : "Add User"}</h3>
                <p style={{ margin: "2px 0 0", fontSize: "0.82rem", color: "var(--text-secondary)" }}>{editUser ? "Update user details and role" : "Create a new platform user"}</p>
              </div>
            </div>
            <form onSubmit={handleSubmit}>
              <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "18px", marginBottom: "20px" }}>
                <div style={{ display: "flex", flexDirection: "column", gap: "6px" }}>
                  <label style={{ fontSize: "0.82rem", fontWeight: 600, color: "var(--text-primary)", textTransform: "uppercase", letterSpacing: "0.3px" }}>
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ verticalAlign: "middle", marginRight: "6px" }}>
                      <circle cx="12" cy="12" r="4" /><path d="M16 8v5a3 3 0 006 0v-1a10 10 0 10-3.92 7.94" />
                    </svg>
                    Email
                  </label>
                  <input value={formEmail} onChange={(e) => setFormEmail(e.target.value)} placeholder="user@domain.com" required
                    style={{ padding: "10px 14px", borderRadius: "var(--radius)", border: "1px solid var(--border-color)", background: "var(--input-bg)", color: "var(--text-primary)", fontSize: "0.92rem", outline: "none", transition: "border-color 0.2s, box-shadow 0.2s" }}
                    onFocus={(e) => { e.target.style.borderColor = "var(--brand-primary-400)"; e.target.style.boxShadow = "0 0 0 3px var(--brand-primary-100)"; }}
                    onBlur={(e) => { e.target.style.borderColor = ""; e.target.style.boxShadow = ""; }} />
                </div>
                <div style={{ display: "flex", flexDirection: "column", gap: "6px" }}>
                  <label style={{ fontSize: "0.82rem", fontWeight: 600, color: "var(--text-primary)", textTransform: "uppercase", letterSpacing: "0.3px" }}>
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ verticalAlign: "middle", marginRight: "6px" }}>
                      <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2" /><circle cx="12" cy="7" r="4" />
                    </svg>
                    Display Name
                  </label>
                  <input value={formName} onChange={(e) => setFormName(e.target.value)} placeholder="Full name" required
                    style={{ padding: "10px 14px", borderRadius: "var(--radius)", border: "1px solid var(--border-color)", background: "var(--input-bg)", color: "var(--text-primary)", fontSize: "0.92rem", outline: "none", transition: "border-color 0.2s, box-shadow 0.2s" }}
                    onFocus={(e) => { e.target.style.borderColor = "var(--brand-primary-400)"; e.target.style.boxShadow = "0 0 0 3px var(--brand-primary-100)"; }}
                    onBlur={(e) => { e.target.style.borderColor = ""; e.target.style.boxShadow = ""; }} />
                </div>
                <div style={{ display: "flex", flexDirection: "column", gap: "6px" }}>
                  <label style={{ fontSize: "0.82rem", fontWeight: 600, color: "var(--text-primary)", textTransform: "uppercase", letterSpacing: "0.3px" }}>
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ verticalAlign: "middle", marginRight: "6px" }}>
                      <rect x="3" y="11" width="18" height="11" rx="2" ry="2" /><path d="M7 11V7a5 5 0 0110 0v4" />
                    </svg>
                    Password {editUser && <span style={{ fontWeight: 400, color: "var(--text-secondary)" }}>(optional)</span>}
                  </label>
                  <input type="password" value={formPassword} onChange={(e) => setFormPassword(e.target.value)}
                    placeholder={editUser ? "Leave empty to keep current" : "Minimum 4 characters"} required={!editUser}
                    style={{ padding: "10px 14px", borderRadius: "var(--radius)", border: "1px solid var(--border-color)", background: "var(--input-bg)", color: "var(--text-primary)", fontSize: "0.92rem", outline: "none", transition: "border-color 0.2s, box-shadow 0.2s" }}
                    onFocus={(e) => { e.target.style.borderColor = "var(--brand-primary-400)"; e.target.style.boxShadow = "0 0 0 3px var(--brand-primary-100)"; }}
                    onBlur={(e) => { e.target.style.borderColor = ""; e.target.style.boxShadow = ""; }} />
                </div>
                <div style={{ display: "flex", flexDirection: "column", gap: "6px" }}>
                  <label style={{ fontSize: "0.82rem", fontWeight: 600, color: "var(--text-primary)", textTransform: "uppercase", letterSpacing: "0.3px" }}>
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ verticalAlign: "middle", marginRight: "6px" }}>
                      <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                    </svg>
                    Role
                  </label>
                  <select value={formRole} onChange={(e) => setFormRole(e.target.value)}
                    style={{ padding: "10px 14px", borderRadius: "var(--radius)", border: "1px solid var(--border-color)", background: "var(--input-bg)", color: "var(--text-primary)", fontSize: "0.92rem", outline: "none", cursor: "pointer", transition: "border-color 0.2s, box-shadow 0.2s", appearance: "auto" }}
                    onFocus={(e) => { e.target.style.borderColor = "var(--brand-primary-400)"; e.target.style.boxShadow = "0 0 0 3px var(--brand-primary-100)"; }}
                    onBlur={(e) => { e.target.style.borderColor = ""; e.target.style.boxShadow = ""; }}>
                    <option value="admin">Administrator</option>
                    <option value="project_manager">Project Manager</option>
                    <option value="qa_engineer">QA Engineer</option>
                  </select>
                </div>
              </div>
              {formError && (
                <div style={{ padding: "10px 14px", borderRadius: "var(--radius)", background: "var(--brand-secondary-50)", border: "1px solid var(--brand-secondary-200)", color: "var(--brand-secondary-600)", fontSize: "0.85rem", marginBottom: "16px", display: "flex", alignItems: "center", gap: "8px" }}>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ flexShrink: 0 }}>
                    <circle cx="12" cy="12" r="10" /><line x1="15" y1="9" x2="9" y2="15" /><line x1="9" y1="9" x2="15" y2="15" />
                  </svg>
                  {formError}
                </div>
              )}
              <div style={{ display: "flex", gap: "10px" }}>
                <button type="submit" style={{ padding: "10px 24px", borderRadius: "var(--radius)", background: "linear-gradient(135deg, var(--brand-secondary-400), var(--brand-secondary-600))", color: "white", border: "none", fontWeight: 600, fontSize: "0.9rem", cursor: "pointer", transition: "all 0.2s", display: "flex", alignItems: "center", gap: "8px" }}>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                    {editUser ? <><path d="M20 14.66V20a2 2 0 01-2 2H4a2 2 0 01-2-2V6a2 2 0 012-2h5.34" /><polygon points="18 2 22 6 12 16 8 16 8 12 18 2" /></> : <><line x1="12" y1="5" x2="12" y2="19" /><line x1="5" y1="12" x2="19" y2="12" /></>}
                  </svg>
                  {editUser ? "Update User" : "Create User"}
                </button>
                <button type="button" onClick={resetForm} style={{ padding: "10px 20px", borderRadius: "var(--radius)", background: "transparent", color: "var(--text-secondary)", border: "1px solid var(--border-color)", fontWeight: 500, fontSize: "0.9rem", cursor: "pointer", transition: "all 0.2s" }}
                  onMouseEnter={(e) => { e.target.style.background = "var(--noveocare-gray-50)"; }}
                  onMouseLeave={(e) => { e.target.style.background = "transparent"; }}>
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

    </div>
  );
}
