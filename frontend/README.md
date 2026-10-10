# MediCore Frontend

React + Vite single-page application for MediCore. JavaScript only — no TypeScript.

## Quick start

```powershell
# from frontend/
npm install
npm run dev        # Vite dev server on http://localhost:5173
```

The backend must be running on `http://localhost:8080` (see the root README).
The backend CORS config allows origins from `CORS_ALLOWED_ORIGINS`
(default: `http://localhost:5173,http://localhost:4173`).

## Scripts

| Command           | Description                                    |
|-------------------|------------------------------------------------|
| `npm run dev`     | Dev server with HMR (port 5173)                |
| `npm run build`   | Production build to `dist/`                    |
| `npm run preview` | Serve the production build (port 4173)         |
| `npm run lint`    | Oxlint                                         |

## Configuration

| Variable            | Default                            | Description                    |
|---------------------|------------------------------------|--------------------------------|
| `VITE_API_BASE_URL` | `http://localhost:8080/api/v1`     | Backend API base URL           |

Vite exposes any `VITE_*` variable from `.env`, `.env.local`, or
`.env.[mode].local` files. Copy `.env.example` to `.env.local` to override
the default. The default is also hard-coded as a fallback in `src/lib/api.js`.

## Structure

```
src/
├─ api/                 # Feature API modules (Fetch-based)
├─ components/
│  ├─ common/           # State blocks, badges, stat cards, modal, confirm dialog, alert, icons
│  └─ layout/           # AppLayout, Sidebar, Topbar
├─ hooks/               # useFetch (loading/error/data helper)
├─ lib/                 # api.js (Fetch layer + ApiError), format.js
├─ pages/               # Route-level pages
└─ styles/              # Design tokens + global styles
```

## Routes

| Path            | Page                                                        |
|-----------------|-------------------------------------------------------------|
| `/`             | Redirects to `/dashboard`                                   |
| `/dashboard`    | Dashboard (summary, status breakdown, today/upcoming)       |
| `/departments`  | Departments (search/filter/paginate, create, edit, deactivate, view details) |
| `/doctors`      | Doctors (search/filter/sort/paginate, create, edit, deactivate, view details) |
| `/patients`     | Patients (search by name/phone, filter/sort/paginate, register, edit, deactivate, view details) |
| `/appointments` | Placeholder — built in a later phase                                 |
| `*`             | 404 page                                                    |
