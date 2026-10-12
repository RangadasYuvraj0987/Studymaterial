import { useState } from "react";
import { Link } from "react-router-dom";

const STATUS_LABEL = {
  NOT_STARTED: "Not started",
  DEVELOPING: "Developing",
  MASTERED: "Mastered",
};

// node: { key, type: "unit" | "topic" | "subtopic", id, name, status?, children[] }
// readOnly = student view: no add / rename / delete buttons
export default function TreeNode({ node, level = 0, base, readOnly, onAdd, onRename, onDelete }) {
  const [open, setOpen] = useState(false);
  const hasChildren = node.children.length > 0;
  const canAddChild = node.type !== "subtopic";
  const stop = (fn) => (e) => {
    e.stopPropagation();
    fn();
  };

  return (
    <div className="tree-node">
      <div
        className={`tree-row level-${level} ${hasChildren ? "clickable" : ""}`}
        onClick={() => hasChildren && setOpen(!open)}
      >
        <span className="arrow">{hasChildren ? (open ? "▼" : "▶") : "•"}</span>
        <span className="label">{node.name}</span>

        {hasChildren && <span className="count">{node.children.length}</span>}
        {node.type === "topic" && (
          <span className={`badge ${node.status}`}>{STATUS_LABEL[node.status]}</span>
        )}

        <span className="row-actions">
          {node.type === "topic" && (
            <Link className="mini study" to={`${base}/topic/${node.id}`} onClick={(e) => e.stopPropagation()}>
              {readOnly ? "Study" : "Content"}
            </Link>
          )}
          {!readOnly && canAddChild && (
            <button className="mini" title="Add" onClick={stop(() => { setOpen(true); onAdd(node); })}>＋</button>
          )}
          {!readOnly && (
            <>
              <button className="mini" title="Rename" onClick={stop(() => onRename(node))}>✎</button>
              <button className="mini danger" title="Delete" onClick={stop(() => onDelete(node))}>🗑</button>
            </>
          )}
        </span>
      </div>

      {open && hasChildren && (
        <div className="tree-children">
          {node.children.map((child) => (
            <TreeNode
              key={child.key}
              node={child}
              level={level + 1}
              base={base}
              readOnly={readOnly}
              onAdd={onAdd}
              onRename={onRename}
              onDelete={onDelete}
            />
          ))}
        </div>
      )}
    </div>
  );
}
