import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import api, { errorMessage } from "../api/client";
import FlashcardsPanel from "../components/FlashcardsPanel";
import QuizPanel from "../components/QuizPanel";
import VideoPlayer from "../components/VideoPlayer";

const STATUSES = [
  ["NOT_STARTED", "Not started"],
  ["DEVELOPING", "Developing"],
  ["MASTERED", "Mastered"],
];

// AI-generated micro-video scenes are stored as JSON text on the topic
function parseScenes(text) {
  try {
    const s = JSON.parse(text || "[]");
    return Array.isArray(s) ? s.filter((x) => x && x.title && x.narration) : [];
  } catch {
    return [];
  }
}

// Only YouTube links can be embedded; other links are shown as plain links
function embedUrl(url) {
  if (!url) return null;
  const m = url.match(/(?:youtu\.be\/|v=)([\w-]{11})/);
  return m ? `https://www.youtube.com/embed/${m[1]}` : null;
}

export default function TopicPage({ admin }) {
  const base = admin ? "/admin" : "/student";
  const { id } = useParams();
  const [topic, setTopic] = useState(null);
  const [form, setForm] = useState({ content: "", videoUrl: "" });
  const [tab, setTab] = useState("notes");
  const [msg, setMsg] = useState("");
  const [error, setError] = useState("");
  const [generating, setGenerating] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0); // remounts the panels after AI generation

  const load = () =>
    api
      .get(`/topics/${id}`)
      .then((r) => {
        setTopic(r.data);
        setForm({ content: r.data.content || "", videoUrl: r.data.videoUrl || "" });
      })
      .catch((e) => setError(errorMessage(e)));

  useEffect(() => {
    load();
  }, [id]);

  // admin only: UPDATE notes + video link
  const saveNotes = async () => {
    setError("");
    try {
      await api.put(`/topics/${id}`, { name: topic.name, status: topic.status, ...form });
      setMsg("Saved ✔");
      setTimeout(() => setMsg(""), 2000);
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  // student only: own progress
  const setStatus = async (status) => {
    try {
      await api.patch(`/topics/${id}/status`, { status });
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  // admin only: ask the backend to generate video script / quiz / flashcards with the LLM (one call)
  const generate = async (parts) => {
    setError("");
    try {
      // warn before replacing existing items
      const [q, f] = await Promise.all([api.get(`/topics/${id}/quiz`), api.get(`/topics/${id}/flashcards`)]);
      const replacing =
        (parts.includes("quiz") && q.data.length > 0) ||
        (parts.includes("flashcards") && f.data.length > 0) ||
        (parts.includes("video") && parseScenes(topic.videoScript).length > 0);
      if (replacing && !window.confirm("This will replace the existing items for the selected parts. Continue?")) {
        return;
      }
      setGenerating(true);
      const r = await api.post(`/topics/${id}/generate`, null, { params: { parts: parts.join(",") } });
      setMsg(`Generated ✔ ${r.data.scenes} scenes, ${r.data.quizQuestions} questions, ${r.data.flashcards} flashcards`);
      setTimeout(() => setMsg(""), 4000);
      setRefreshKey((k) => k + 1);
      load();
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setGenerating(false);
    }
  };

  if (!topic) {
    return error ? <div className="alert">{error}</div> : <p>Loading...</p>;
  }

  const embed = embedUrl(topic.videoUrl);
  const scenes = parseScenes(topic.videoScript);

  return (
    <div>
      <Link to={topic.syllabusId ? `${base}/syllabus/${topic.syllabusId}` : base}>← Back to syllabus</Link>
      <header className="page-head">
        <h1>{topic.name}</h1>
        {!admin && (
          <div className="status-row">
            <span className="muted">My progress:</span>
            {STATUSES.map(([value, label]) => (
              <button
                key={value}
                className={`pill ${topic.status === value ? "active " + value : ""}`}
                onClick={() => setStatus(value)}
              >
                {label}
              </button>
            ))}
          </div>
        )}
      </header>

      {error && <div className="alert">{error}</div>}

      {admin && (
        <div className="card ai-box">
          <div>
            <strong>✨ AI study tools</strong>
            <div className="muted">Creates a micro-video, quiz and flashcards for this topic in one go.</div>
          </div>
          <div className="actions">
            <button className="btn alt" disabled={generating} onClick={() => generate(["video", "quiz", "flashcards"])}>
              {generating ? "Generating… (10–40 s)" : "Generate all"}
            </button>
            <button className="btn small ghost" disabled={generating} onClick={() => generate(["video"])}>Video only</button>
            <button className="btn small ghost" disabled={generating} onClick={() => generate(["quiz"])}>Quiz only</button>
            <button className="btn small ghost" disabled={generating} onClick={() => generate(["flashcards"])}>Flashcards only</button>
            {msg && <span className="ok">{msg}</span>}
          </div>
        </div>
      )}

      <div className="tabs">
        {[["notes", "📝 Notes & Video"], ["flashcards", "🃏 Flashcards"], ["quiz", "❓ Quiz"]].map(
          ([key, label]) => (
            <button key={key} className={`tab ${tab === key ? "active" : ""}`} onClick={() => setTab(key)}>
              {label}
            </button>
          )
        )}
      </div>

      {tab === "notes" && (
        <div className="card">
          {scenes.length > 0 && (
            <>
              <strong>Micro-video</strong>
              <VideoPlayer key={topic.videoScript} scenes={scenes} />
            </>
          )}

          {topic.subtopics.length > 0 && (
            <div className="subtopic-list">
              <strong>Subtopics</strong>
              <ul>
                {topic.subtopics.map((s) => (
                  <li key={s.id}>{s.name}</li>
                ))}
              </ul>
            </div>
          )}

          {admin ? (
            <>
              <label className="field-label">Study notes</label>
              <textarea
                rows={10}
                placeholder="Write the explanation / notes for this topic..."
                value={form.content}
                onChange={(e) => setForm({ ...form, content: e.target.value })}
              />

              <label className="field-label">Video link (YouTube)</label>
              <input
                placeholder="https://www.youtube.com/watch?v=..."
                value={form.videoUrl}
                onChange={(e) => setForm({ ...form, videoUrl: e.target.value })}
              />

              <div className="actions">
                <button className="btn" onClick={saveNotes}>Save</button>
                {msg && <span className="ok">{msg}</span>}
              </div>
            </>
          ) : (
            <>
              <strong>Notes</strong>
              <p className="notes">{topic.content || "No notes have been added for this topic yet."}</p>
            </>
          )}

          {embed && (
            <div className="video-wrap">
              <iframe src={embed} title="Topic video" allowFullScreen />
            </div>
          )}
          {topic.videoUrl && !embed && (
            <p>
              <a href={topic.videoUrl} target="_blank" rel="noreferrer">Open video link ↗</a>
            </p>
          )}
        </div>
      )}

      {tab === "flashcards" && <FlashcardsPanel key={refreshKey} topicId={id} admin={admin} />}
      {tab === "quiz" && <QuizPanel key={refreshKey} topicId={id} admin={admin} />}
    </div>
  );
}
