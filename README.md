# Studymaterial - Java Full Stack Project

Spring Boot + MySQL backend, React + CSS frontend.
Full CRUD for Syllabus, Unit, Topic, Subtopic, Flashcard and Quiz question.

```
Studymaterial/
  backend/            Spring Boot project (pom.xml, src/...)
  frontend/           React (Vite) project (package.json, src/...)
  vlsi-import.json    Sample data: the 5-unit VLSI syllabus from your diagram
```

Default settings (override them in `backend/.env`):

| Setting        | Value            |
|----------------|------------------|
| Database name  | `studymaterial_db` (created automatically on first run) |
| MySQL user     | `root`           |
| MySQL password | Set in `backend/.env` |
| Backend URL    | http://localhost:8080 |
| Frontend URL   | http://localhost:5173 |

## The two interfaces

| URL | Who | What they can do |
|-----|-----|------------------|
| http://localhost:5173 and http://localhost:5173/student | Student | Browse subjects, expand the syllabus tree, read notes, watch the video, flip flashcards, take the quiz, mark their own progress. **Cannot add, edit or delete anything.** |
| http://localhost:5173/admin | Admin (faculty) | Needs login. Full CRUD: subjects, units, topics, subtopics, notes, video links, flashcards, quiz questions, JSON import. |

**Admin login:** configured through `ADMIN_USER` and `ADMIN_PASS` in `backend/.env` (or environment variables).
For local development, copy `backend/.env.example` to `backend/.env` and replace the placeholder password.

The backend enforces this too: every POST / PUT / DELETE needs the admin token, so a student
cannot change content by calling the API directly. The only write a student may do is
updating their own topic progress.

Note: there are no student accounts, so the "My progress" status is shared by everyone using the site.

## Quick start (Windows)

Needs: JDK 21, Maven (or VS Code Java extension pack), MySQL 8 running, Node.js 20+.

1. Unzip so you get `C:\Full Stack Java\Studymaterial\backend` and `...\frontend`.
   Do NOT keep it inside OneDrive.
2. Make sure the MySQL service is running. Set `DB_USER` and `DB_PASS` in `backend/.env`
   to match your local MySQL credentials.

### Backend

```
cd "C:\Full Stack Java\Studymaterial\backend"
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
# Edit .env and replace the placeholder passwords.
mvn spring-boot:run
```

(Run `Copy-Item` only the first time so an existing `.env` is not overwritten. Or open the
`backend` folder in VS Code / IntelliJ and run `StudymaterialApplication`; the app reads
`.env` from its working directory.)
Wait for `Started StudymaterialApplication`. Test: open http://localhost:8080/api/syllabi
in the browser. You should see `[]`.

### Frontend (new terminal)

```
cd "C:\Full Stack Java\Studymaterial\frontend"
npm install
npm run dev
```

Open http://localhost:5173

### Load the sample VLSI data

1. Open http://localhost:5173/admin and log in using `ADMIN_USER` / `ADMIN_PASS` from `backend/.env`.
2. Click "Import syllabus from JSON" and choose `vlsi-import.json`.
3. Open http://localhost:5173/student to see it as a student: open the syllabus, then click
   a unit, then a topic, to expand the tree. Topics that have subtopics show an arrow and a
   count; topics without them show a dot.

## What you can do in the app (all CRUD, admin only)

- Syllabus: add, edit, delete (admin home page)
- Unit / Topic / Subtopic: the plus button adds a child, the pencil renames, the bin deletes
- Topic page ("Content" button): notes, YouTube link
- Flashcards: add, edit, delete
- Quiz: add, edit, delete questions

Students use the same data read-only, plus progress (Not started / Developing / Mastered),
flip flashcards and the scored quiz.

## API summary

| Resource   | Endpoints |
|------------|-----------|
| Admin auth | POST `/api/admin/login`, GET `/api/admin/check`, POST `/api/admin/logout` |
| Syllabus   | GET/POST `/api/syllabi`, GET/PUT/DELETE `/api/syllabi/{id}`, POST `/api/syllabi/import` |
| Unit       | GET/POST `/api/syllabi/{id}/units`, GET/PUT/DELETE `/api/units/{id}` |
| Topic      | GET/POST `/api/units/{id}/topics`, GET/PUT/DELETE `/api/topics/{id}`, PATCH `/api/topics/{id}/status` |
| Subtopic   | GET/POST `/api/topics/{id}/subtopics`, GET/PUT/DELETE `/api/subtopics/{id}` |
| Flashcard  | GET/POST `/api/topics/{id}/flashcards`, PUT/DELETE `/api/flashcards/{id}` |
| Quiz       | GET/POST `/api/topics/{id}/quiz`, PUT/DELETE `/api/quiz/{id}` |

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `Access denied for user 'root'` | MySQL credentials are incorrect. Update `DB_USER` / `DB_PASS` in `backend/.env` |
| `Communications link failure` | MySQL service is not running |
| Maven cannot find `spring-boot-starter-webmvc` | In `backend/pom.xml` change it to `spring-boot-starter-web` |
| Port 8080 in use | Close the other app or set `server.port=8081` (and update `frontend/.env`) |
| Frontend shows "Cannot reach the server" | Backend is not running, or `VITE_API_URL` in `frontend/.env` is wrong. Restart `npm run dev` after editing `.env` |
| CORS error in browser console | Frontend must run on http://localhost:5173 (or add your URL to `app.cors.origin`) |
| Postman POST/PUT/DELETE returns 401 "Admin login required" | Expected. POST `/api/admin/login` first, then send header `Authorization: Bearer <token>` |
| Admin is sent back to the login page | The backend restarted (tokens are kept in memory). Log in again |
| Old `syllabus` table exists from earlier tests | Nothing to do. Hibernate adds the new tables and columns automatically |

## Deployment

1. Cloud MySQL (Aiven / Railway / TiDB). Copy host, port, database, user, password.
2. Backend on Render (Docker, uses `backend/Dockerfile`). Set these environment variables
   in Render (the local `.env` is intentionally excluded from the Docker image):
   - `DB_URL` = `jdbc:mysql://HOST:PORT/DBNAME?sslMode=REQUIRED`
   - `DB_USER`, `DB_PASS`
   - `ADMIN_USER`, `ADMIN_PASS` (use a strong password for the live site)
   - `APP_CORS_ORIGIN` = your Vercel URL (no trailing slash; several allowed, comma-separated)
3. Frontend on Vercel (root directory `frontend`, framework Vite). Set
   `VITE_API_URL` = `https://YOUR-BACKEND.onrender.com/api` BEFORE the first deploy.
   `vercel.json` is included so page refreshes do not give 404.
4. Test the live site. The free backend sleeps when idle, so open it a minute before a demo.


## AI generation (micro-video, quiz, flashcards)

In the admin interface, open a topic and click **Generate all**. One LLM call creates:
a 5-6 scene micro-video script, 5 quiz questions and 8 flashcards. Students see the video
(slides with spoken narration) and the quiz / flashcards on the student side.
Use "Video only", "Quiz only" or "Flashcards only" to regenerate a single part
(this replaces the existing items of that part).

Setup (the key stays on the backend, never in the frontend):

1. Get an API key from any OpenAI-compatible provider (default config targets Google Gemini).
2. Add it as `LLM_API_KEY` in `backend/.env`, or set it as an environment variable before
   starting the backend. You can also configure `LLM_BASE_URL`, `LLM_MODEL` (default
   `gemini-flash-latest`) and `LLM_TIMEOUT` (seconds) in the same file.

API: `POST /api/topics/{id}/generate?parts=video,quiz,flashcards` (admin token required,
like every other write request).
