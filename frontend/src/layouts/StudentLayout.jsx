import { Link, Outlet } from "react-router-dom";

export default function StudentLayout() {
  return (
    <div className="layout">
      <header className="topbar student-bar">
        <Link to="/student" className="brand">📚 Study Material</Link>
        <span className="tag">Student</span>
      </header>
      <main className="container">
        <Outlet />
      </main>
    </div>
  );
}
