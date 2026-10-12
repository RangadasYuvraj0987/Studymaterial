import { useEffect, useRef, useState } from "react";

// Plays AI-generated scenes as a micro-video: slide + spoken narration (browser text-to-speech).
// No video file is rendered on the server, so it is free and instant.
export default function VideoPlayer({ scenes }) {
  const [i, setI] = useState(0);
  const [playing, setPlaying] = useState(false);
  const utterRef = useRef(null); // keeps the utterance alive (Chrome can drop it otherwise)
  const speechOk = typeof window !== "undefined" && "speechSynthesis" in window;
  const scene = scenes[i];

  useEffect(() => {
    if (!playing) return;
    let cancelled = false;
    let timer = null;

    const next = () => {
      if (cancelled) return;
      if (i < scenes.length - 1) setI(i + 1);
      else setPlaying(false);
    };

    if (speechOk) {
      window.speechSynthesis.cancel();
      const u = new SpeechSynthesisUtterance(scene.narration);
      u.onend = next;
      u.onerror = next;
      utterRef.current = u;
      window.speechSynthesis.speak(u);
    } else {
      // no speech support: move on after a reading-time delay
      const words = scene.narration.split(/\s+/).length;
      timer = setTimeout(next, Math.max(4000, words * 400));
    }

    return () => {
      cancelled = true;
      clearTimeout(timer);
      if (speechOk) window.speechSynthesis.cancel();
    };
  }, [playing, i, scenes, scene, speechOk]);

  const toggle = () => {
    setPlaying(!playing);
  };
  const jump = (n) => {
    setI(Math.min(Math.max(n, 0), scenes.length - 1));
  };
  const replay = () => {
    setI(0);
    setPlaying(true);
  };

  return (
    <div className="vplayer">
      <div className="vslide">
        <div className="vcount">Scene {i + 1} / {scenes.length}</div>
        <h2>{scene.title}</h2>
        <ul>
          {scene.bullets.map((b, k) => (
            <li key={k}>{b}</li>
          ))}
        </ul>
      </div>

      <div className="vcaption">{scene.narration}</div>

      <div className="vcontrols">
        <button className="btn small ghost" onClick={() => jump(i - 1)} disabled={i === 0}>⏮</button>
        <button className="btn small" onClick={toggle}>{playing ? "⏸ Pause" : "▶ Play"}</button>
        <button className="btn small ghost" onClick={() => jump(i + 1)} disabled={i === scenes.length - 1}>⏭</button>
        <button className="btn small ghost" onClick={replay}>↺ Replay</button>
        <span className="vdots">
          {scenes.map((_, k) => (
            <button key={k} className={`vdot ${k === i ? "on" : ""}`} onClick={() => jump(k)} aria-label={`Scene ${k + 1}`} />
          ))}
        </span>
      </div>
      {!speechOk && <p className="muted">Your browser has no speech support, so slides advance without audio.</p>}
    </div>
  );
}
