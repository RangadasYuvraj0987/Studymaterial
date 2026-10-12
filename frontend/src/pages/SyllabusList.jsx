import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api, { errorMessage } from "../api/client";

const empty = { subjectName: "", description: "" };

export default function SyllabusList({ admin }) {
  const base = admin ? "/admin" : "/student";
  const [items, setItems] = useState([]);
  const [form, setForm] = useState(empty);
  const [editId, setEditId] = useState(null);
  const [error, setError] = useState("");

  const load = () =>
    api
      .get("/syllabi")
      .then((r) => setItems(r.data))
      .catch((e) => setError(errorMessage(e)));

  useEffect(() => {
    load();
  }, []);

  // ---------- admin only: CREATE / UPDATE / DELETE / IMPORT ----------
  const submit = async (e) => {
    e.preventDefault();
    setError("");
    try {
      if (editId) await api.put(`/syllabi/${editId}`, form);
      else await api.post("/syllabi", form);
      setForm(empty);
      setEditId(null);
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  const remove = async (id) => {
    if (!window.confirm("Delete this syllabus and everything inside it?")) return;
    try {
      await api.delete(`/syllabi/${id}`);
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  const startEdit = (s) => {
    setEditId(s.id);
    setForm({ subjectName: s.subjectName, description: s.description || "" });
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const importFile = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    setError("");
    try {
      const json = JSON.parse(await file.text());
      await api.post("/syllabi/import", json);
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
    e.target.value = "";
  };

  return (
    <div>
      <header className="page-head">
        <h1>{admin ? "Manage subjects" : "Choose a subject to study"}</h1>
        <p className="muted">
          {admin
            ? "Create a syllabus, then open it to add units, topics and subtopics."
            : "Pick a subject, open a topic, then read, watch, revise with flashcards and take the quiz."}
        </p>
      </header>

      {error && <div className="alert">{error}</div>}

      {admin && (
        <>
          <form onSubmit={submit} className="card form-row">
            <input
              placeholder="Subject name"
              value={form.subjectName}
              onChange={(e) => setForm({ ...form, subjectName: e.target.value })}
            />
            <input
              placeholder="Description (optional)"
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
            />
            <button className="btn">{editId ? "Update" : "Add Syllabus"}</button>
            {editId && (
              <button
                type="button"
                className="btn ghost"
                onClick={() => {
                  setEditId(null);
                  setForm(empty);
                }}
              >
                Cancel
              </button>
            )}
          </form>

          <div className="import-row">
            <label className="btn ghost file-btn">
              ⬆ Import syllabus from JSON
              <input type="file" accept=".json,application/json" onChange={importFile} hidden />
            </label>
          </div>
        </>
      )}

      <div className="grid">
        {items.map((s) => (
          <div className="card" key={s.id}>
            <h3>{s.subjectName}</h3>
            <p className="muted">{s.description || "No description"}</p>
            <p className="chip">{s.units.length} units</p>
            <div className="actions">
              <Link className="btn" to={`${base}/syllabus/${s.id}`}>
                {admin ? "Open" : "Start learning"}
              </Link>
              {admin && (
                <>
                  <button className="btn alt" onClick={() => startEdit(s)}>Edit</button>
                  <button className="btn danger" onClick={() => remove(s.id)}>Delete</button>
                </>
              )}
            </div>
          </div>
        ))}
        {items.length === 0 && (
          <p className="muted">
            {admin ? "No syllabus yet. Add one above." : "No subjects available yet. Please check back later."}
          </p>
        )}
      </div>
    </div>
  );
}
