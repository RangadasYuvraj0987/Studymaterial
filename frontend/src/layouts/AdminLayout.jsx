import { useEffect, useState } from "react";
import { Link, Navigate, Outlet, useNavigate } from "react-router-dom";
import api from "../api/client";
import { clearToken, getToken } from "../auth";

export default function AdminLayout() {
  const navigate = useNavigate();
  const hasToken = !!getToken();
  const [checked, setChecked] = useState(false);

  // Confirm the saved token is still accepted by the backend
  useEffect(() => {
    if (!hasToken) return;
    api
      .get("/admin/check")
      .then(() => setChecked(true))
      .catch(() => {
        clearToken();
        navigate("/admin/login", { replace: true });
      });
  }, [hasToken, navigate]);

  const logout = async () => {
    try {
      await api.post("/admin/logout");
    } catch {
      /* ignore */
    }
    clearToken();
    navigate("/admin/login", { replace: true });
  };

  if (!hasToken) return <Navigate to="/admin/login" replace />;
  if (!checked) return <p className="container">Checking login...</p>;

  return (
    <div className="layout">
      <header className="topbar admin-bar">
        <Link to="/admin" className="brand">🛠 Admin Panel</Link>
        <span className="tag">Content management</span>
        <button className="btn ghost small push" onClick={logout}>Log out</button>
      </header>
      <main className="container">
        <Outlet />
      </main>
    </div>
  );
}
