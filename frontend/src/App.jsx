import { Navigate, Route, Routes } from "react-router-dom";
import StudentLayout from "./layouts/StudentLayout";
import AdminLayout from "./layouts/AdminLayout";
import AdminLogin from "./pages/AdminLogin";
import SyllabusList from "./pages/SyllabusList";
import SyllabusDetail from "./pages/SyllabusDetail";
import TopicPage from "./pages/TopicPage";
import "./App.css";

export default function App() {
  return (
    <Routes>
      {/* localhost:5173  ->  student interface */}
      <Route path="/" element={<Navigate to="/student" replace />} />

      {/* ---------- Student interface (read + learn only) ---------- */}
      <Route path="/student" element={<StudentLayout />}>
        <Route index element={<SyllabusList admin={false} />} />
        <Route path="syllabus/:id" element={<SyllabusDetail admin={false} />} />
        <Route path="topic/:id" element={<TopicPage admin={false} />} />
      </Route>

      {/* ---------- Admin interface (full CRUD, needs login) ---------- */}
      <Route path="/admin/login" element={<AdminLogin />} />
      <Route path="/admin" element={<AdminLayout />}>
        <Route index element={<SyllabusList admin />} />
        <Route path="syllabus/:id" element={<SyllabusDetail admin />} />
        <Route path="topic/:id" element={<TopicPage admin />} />
      </Route>

      <Route path="*" element={<Navigate to="/student" replace />} />
    </Routes>
  );
}
