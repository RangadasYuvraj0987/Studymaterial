import { useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import api, { errorMessage } from "../api/client";
import { getToken, setToken } from "../auth";

export default function AdminLogin() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: "", password: "" });
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  if (getToken()) return <Navigate to="/admin" replace />;

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    setBusy(true);
    try {
      const res = await api.post("/admin/login", form);
      setToken(res.data.token);
      navigate("/admin", { replace: true });
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="login-page">
      <form className="card login-card" onSubmit={submit}>
        <h2>🛠 Admin login</h2>
        <p className="muted">Only the faculty / project owner can manage content.</p>
        {error && <div className="alert">{error}</div>}
        <input
          placeholder="Username"
          autoFocus
          value={form.username}
          onChange={(e) => setForm({ ...form, username: e.target.value })}
        />
        <input
          type="password"
          placeholder="Password"
          value={form.password}
          onChange={(e) => setForm({ ...form, password: e.target.value })}
        />
        <button className="btn" disabled={busy}>{busy ? "Signing in..." : "Log in"}</button>
        <p className="muted small-text">
          Student? <a href="/student">Go to the student page</a>
        </p>
      </form>
    </div>
  );
}
