import { useEffect, useState } from "react";
import api, { errorMessage } from "../api/client";

const empty = { question: "", answer: "" };

export default function FlashcardsPanel({ topicId, admin }) {
  const [cards, setCards] = useState([]);
  const [flipped, setFlipped] = useState({});
  const [form, setForm] = useState(empty);
  const [editId, setEditId] = useState(null);
  const [error, setError] = useState("");

  const load = () =>
    api
      .get(`/topics/${topicId}/flashcards`)
      .then((r) => setCards(r.data))
      .catch((e) => setError(errorMessage(e)));

  useEffect(() => {
    load();
  }, [topicId]);

  // admin only: CREATE / UPDATE
  const submit = async (e) => {
    e.preventDefault();
    setError("");
    try {
      if (editId) await api.put(`/flashcards/${editId}`, form);
      else await api.post(`/topics/${topicId}/flashcards`, form);
      setForm(empty);
      setEditId(null);
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  // admin only: DELETE
  const remove = async (id) => {
    if (!window.confirm("Delete this flashcard?")) return;
    try {
      await api.delete(`/flashcards/${id}`);
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  const startEdit = (c) => {
    setEditId(c.id);
    setForm({ question: c.question, answer: c.answer });
  };

  return (
    <div>
      {error && <div className="alert">{error}</div>}

      {admin && (
        <form className="card" onSubmit={submit}>
          <h3>{editId ? "Edit flashcard" : "Add a flashcard"}</h3>
          <input
            placeholder="Question"
            value={form.question}
            onChange={(e) => setForm({ ...form, question: e.target.value })}
          />
          <textarea
            rows={3}
            placeholder="Answer"
            value={form.answer}
            onChange={(e) => setForm({ ...form, answer: e.target.value })}
          />
          <div className="actions">
            <button className="btn">{editId ? "Update" : "Add"}</button>
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
          </div>
        </form>
      )}

      {cards.length === 0 && <p className="muted">No flashcards yet.</p>}
      {cards.length > 0 && <p className="muted">Click a card to flip it.</p>}

      <div className="grid">
        {cards.map((c) => (
          <div key={c.id} className="flash-wrap">
            <div
              className={`flashcard ${flipped[c.id] ? "flipped" : ""}`}
              onClick={() => setFlipped({ ...flipped, [c.id]: !flipped[c.id] })}
            >
              <div className="face front">
                <small>Question</small>
                <p>{c.question}</p>
              </div>
              <div className="face back">
                <small>Answer</small>
                <p>{c.answer}</p>
              </div>
            </div>
            {admin && (
              <div className="actions">
                <button className="btn alt small" onClick={() => startEdit(c)}>Edit</button>
                <button className="btn danger small" onClick={() => remove(c.id)}>Delete</button>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
