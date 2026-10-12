import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import api, { errorMessage } from "../api/client";
import TreeNode from "../components/TreeNode";

// REST paths for each level
const PATH = { unit: "units", topic: "topics", subtopic: "subtopics" };
const CHILD_PATH = { unit: "topics", topic: "subtopics" };
const CHILD_LABEL = { unit: "topic", topic: "subtopic" };

const buildTree = (syllabus) =>
  syllabus.units.map((u) => ({
    key: `u${u.id}`,
    type: "unit",
    id: u.id,
    name: u.name,
    payload: {},
    children: u.topics.map((t) => ({
      key: `t${t.id}`,
      type: "topic",
      id: t.id,
      name: t.name,
      status: t.status,
      payload: { content: t.content, videoUrl: t.videoUrl, status: t.status },
      children: t.subtopics.map((s) => ({
        key: `s${s.id}`,
        type: "subtopic",
        id: s.id,
        name: s.name,
        payload: { content: s.content },
        children: [],
      })),
    })),
  }));

export default function SyllabusDetail({ admin }) {
  const base = admin ? "/admin" : "/student";
  const { id } = useParams();
  const [syllabus, setSyllabus] = useState(null);
  const [error, setError] = useState("");

  const load = () =>
    api
      .get(`/syllabi/${id}`)
      .then((r) => setSyllabus(r.data))
      .catch((e) => setError(errorMessage(e)));

  useEffect(() => {
    load();
  }, [id]);

  const run = async (fn) => {
    setError("");
    try {
      await fn();
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  // ---------- admin only ----------
  const addUnit = () => {
    const name = window.prompt("Unit name:");
    if (name && name.trim()) run(() => api.post(`/syllabi/${id}/units`, { name: name.trim() }));
  };

  const addChild = (node) => {
    const name = window.prompt(`New ${CHILD_LABEL[node.type]} name:`);
    if (name && name.trim())
      run(() => api.post(`/${PATH[node.type]}/${node.id}/${CHILD_PATH[node.type]}`, { name: name.trim() }));
  };

  const rename = (node) => {
    const name = window.prompt("New name:", node.name);
    if (name && name.trim())
      run(() => api.put(`/${PATH[node.type]}/${node.id}`, { ...node.payload, name: name.trim() }));
  };

  const remove = (node) => {
    if (window.confirm(`Delete "${node.name}" and everything inside it?`))
      run(() => api.delete(`/${PATH[node.type]}/${node.id}`));
  };

  if (!syllabus) {
    return error ? <div className="alert">{error}</div> : <p>Loading...</p>;
  }

  const tree = buildTree(syllabus);
  const topics = tree.flatMap((u) => u.children);
  const mastered = topics.filter((t) => t.status === "MASTERED").length;
  const percent = topics.length ? Math.round((mastered / topics.length) * 100) : 0;

  return (
    <div>
      <Link to={base}>← All subjects</Link>
      <header className="page-head">
        <h1>{syllabus.subjectName}</h1>
        {syllabus.description && <p className="muted">{syllabus.description}</p>}
      </header>

      {error && <div className="alert">{error}</div>}

      {!admin && topics.length > 0 && (
        <div className="card">
          <strong>Your progress: {mastered} of {topics.length} topics mastered ({percent}%)</strong>
          <div className="bar"><div className="bar-fill" style={{ width: `${percent}%` }} /></div>
        </div>
      )}

      <div className="toolbar">
        {admin && <button className="btn" onClick={addUnit}>＋ Add Unit</button>}
        <span className="legend">
          <i className="dot l0" /> Unit <i className="dot l1" /> Topic <i className="dot l2" /> Subtopic
        </span>
      </div>

      {tree.length === 0 && (
        <p className="muted">{admin ? "No units yet. Click “Add Unit” to start." : "No content has been added yet."}</p>
      )}

      {tree.map((unit) => (
        <TreeNode
          key={unit.key}
          node={unit}
          level={0}
          base={base}
          readOnly={!admin}
          onAdd={addChild}
          onRename={rename}
          onDelete={remove}
        />
      ))}
    </div>
  );
}
