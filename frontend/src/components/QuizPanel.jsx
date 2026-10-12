import { useEffect, useState } from "react";
import api, { errorMessage } from "../api/client";

const empty = {
  question: "",
  optionA: "",
  optionB: "",
  optionC: "",
  optionD: "",
  correctOption: "A",
};
const LETTERS = ["A", "B", "C", "D"];

export default function QuizPanel({ topicId, admin }) {
  const [questions, setQuestions] = useState([]);
  const [form, setForm] = useState(empty);
  const [editId, setEditId] = useState(null);
  const [answers, setAnswers] = useState({});
  const [submitted, setSubmitted] = useState(false);
  const [error, setError] = useState("");

  const load = () =>
    api
      .get(`/topics/${topicId}/quiz`)
      .then((r) => setQuestions(r.data))
      .catch((e) => setError(errorMessage(e)));

  useEffect(() => {
    load();
  }, [topicId]);

  // admin only: CREATE / UPDATE
  const submitForm = async (e) => {
    e.preventDefault();
    setError("");
    try {
      if (editId) await api.put(`/quiz/${editId}`, form);
      else await api.post(`/topics/${topicId}/quiz`, form);
      setForm(empty);
      setEditId(null);
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  // admin only: DELETE
  const remove = async (id) => {
    if (!window.confirm("Delete this question?")) return;
    try {
      await api.delete(`/quiz/${id}`);
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  const startEdit = (q) => {
    setEditId(q.id);
    setForm({
      question: q.question,
      optionA: q.optionA,
      optionB: q.optionB,
      optionC: q.optionC,
      optionD: q.optionD,
      correctOption: q.correctOption,
    });
  };

  const score = questions.filter((q) => answers[q.id] === q.correctOption).length;

  const reset = () => {
    setAnswers({});
    setSubmitted(false);
  };

  return (
    <div>
      {error && <div className="alert">{error}</div>}

      {/* ---- Take the quiz (students) / preview (admin) ---- */}
      <div className="card">
        <h3>{admin ? "Quiz preview" : "Take the quiz"}</h3>
        {questions.length === 0 && (
          <p className="muted">{admin ? "No questions yet. Add some below." : "No quiz questions yet."}</p>
        )}

        {questions.map((q, i) => (
          <div key={q.id} className="question">
            <p>
              <strong>{i + 1}. {q.question}</strong>
            </p>
            {LETTERS.map((L) => {
              const text = q["option" + L];
              let cls = "option";
              if (submitted) {
                if (L === q.correctOption) cls += " correct";
                else if (answers[q.id] === L) cls += " wrong";
              }
              return (
                <label key={L} className={cls}>
                  <input
                    type="radio"
                    name={`q${q.id}`}
                    disabled={submitted}
                    checked={answers[q.id] === L}
                    onChange={() => setAnswers({ ...answers, [q.id]: L })}
                  />
                  {L}. {text}
                </label>
              );
            })}
            {admin && (
              <div className="actions">
                <button className="btn alt small" onClick={() => startEdit(q)}>Edit</button>
                <button className="btn danger small" onClick={() => remove(q.id)}>Delete</button>
              </div>
            )}
          </div>
        ))}

        {questions.length > 0 && (
          <div className="actions">
            {!submitted ? (
              <button className="btn" onClick={() => setSubmitted(true)}>Submit answers</button>
            ) : (
              <>
                <strong className="score">
                  Score: {score} / {questions.length}
                </strong>
                <button className="btn ghost" onClick={reset}>Try again</button>
              </>
            )}
          </div>
        )}
      </div>

      {/* ---- admin only: add / edit a question ---- */}
      {admin && (
        <form className="card" onSubmit={submitForm}>
          <h3>{editId ? "Edit question" : "Add a question"}</h3>
          <input
            placeholder="Question"
            value={form.question}
            onChange={(e) => setForm({ ...form, question: e.target.value })}
          />
          {LETTERS.map((L) => (
            <input
              key={L}
              placeholder={`Option ${L}`}
              value={form["option" + L]}
              onChange={(e) => setForm({ ...form, ["option" + L]: e.target.value })}
            />
          ))}
          <label className="field-label">Correct answer</label>
          <select
            value={form.correctOption}
            onChange={(e) => setForm({ ...form, correctOption: e.target.value })}
          >
            {LETTERS.map((L) => (
              <option key={L} value={L}>{L}</option>
            ))}
          </select>
          <div className="actions">
            <button className="btn">{editId ? "Update" : "Add question"}</button>
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
    </div>
  );
}
