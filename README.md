# MediCore — Hospital Management System

A local-development Hospital Management System built as a **single-repository modular monolith**.

> **Data disclaimer:** This project is for local development and demonstration with **fictional data only**.
> It is not suitable for real patient information or production deployment without appropriate security
> and privacy controls.

## Stack

| Layer     | Technology                                                                 |
|-----------|----------------------------------------------------------------------------|
| Backend   | Java 21, Spring Boot 3.3.x, Spring MVC, Spring Data JPA, Hibernate, Maven   |
| Validation| Jakarta Bean Validation                                                    |
| Database  | MySQL 8 with Flyway versioned migrations                                   |
| API docs  | Swagger / OpenAPI (springdoc)                                              |
| Testing   | JUnit 5, Mockito, Spring Boot Test                                         |
| Frontend  | React 19, Vite 8, React Router 7, JavaScript (Oxlint)                      |

## Out of scope (this phase)

No JWT, no Spring Security, no authentication, no microservices/Eureka/gateway, no Kafka/Redis,
no pharmacy/billing/insurance/lab/inpatient/AI features.

## Repository layout

```
MediCore/
├─ backend-spring/     # Spring Boot application (Maven)
│  └─ src/main/resources/db/migration/   # Flyway migrations
├─ frontend/           # React + Vite SPA (see frontend/README.md)
│  ├─ src/api/         # Fetch-based feature API modules
│  ├─ src/components/  # Reusable layout + common components
│  └─ src/pages/       # Route-level pages
├─ TODO.md             # Master task checklist and phase log
└─ README.md
```

## Prerequisites

- JDK 21
- Maven 3.9+
- MySQL 8 running locally
- Node.js 18+ and npm (Node 24 / npm 11 tested) for the frontend

## Database setup

Create the application and test schemas plus a dedicated user:

```sql
CREATE DATABASE IF NOT EXISTS medicore      CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS medicore_test CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER IF NOT EXISTS 'medicore'@'localhost' IDENTIFIED BY 'change-me';
GRANT ALL PRIVILEGES ON medicore.*      TO 'medicore'@'localhost';
GRANT ALL PRIVILEGES ON medicore_test.* TO 'medicore'@'localhost';
FLUSH PRIVILEGES;
```

Flyway runs automatically on application startup and applies everything under
`backend-spring/src/main/resources/db/migration`.

## Configuration

Environment variables are read with safe defaults:

| Variable        | Default                                                          |
|-----------------|-----------------------------------------------------------------|
| `DB_URL`        | `jdbc:mysql://localhost:3306/medicore?...&serverTimezone=UTC`    |
| `DB_USERNAME`   | `medicore`                                                       |
| `DB_PASSWORD`   | _(empty)_                                                        |
| `SERVER_PORT`   | `8080`                                                           |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost:4173`             |

For local development you can either export the variables:

```powershell
$env:DB_USERNAME = "medicore"
$env:DB_PASSWORD = "change-me"
```

or create a **gitignored** `backend-spring/src/main/resources/application-local.yml`
(see `application-local.example.yml`) and run with the `local` profile.

### Date/time policy

All timestamps are stored in **UTC** as MySQL `DATETIME(6)` columns and exposed as ISO-8601
`Instant` values. Hibernate is configured with `hibernate.jdbc.time_zone=UTC`. Clients are
responsible for converting to local time for display.

## Run the backend

```powershell
# from the repository root
mvn -f backend-spring/pom.xml spring-boot:run "-Dspring-boot.run.profiles=local"
```

The API is served under `http://localhost:8080/api/v1`.

Swagger UI: `http://localhost:8080/swagger-ui.html`
OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### CORS

The backend allows cross-origin calls to `/api/**` from the origins listed in
`CORS_ALLOWED_ORIGINS` (default: the Vite dev and preview servers). This is plain
Spring MVC CORS configuration — **no Spring Security** is used anywhere in this
project. Disallowed origins receive no `Access-Control-Allow-Origin` header.

## Run the frontend

```powershell
# from the repository root
cd frontend
npm install        # first time only
npm run dev        # http://localhost:5173
```

The frontend calls the backend directly using `VITE_API_BASE_URL`
(default `http://localhost:8080/api/v1`). Start the backend first. To override
the URL, copy `frontend/.env.example` to `frontend/.env.local` and edit it.

| Command           | Description                                    |
|-------------------|------------------------------------------------|
| `npm run dev`     | Dev server with HMR (port 5173)                |
| `npm run build`   | Production build to `frontend/dist/`           |
| `npm run preview` | Serve the production build (port 4173)         |
| `npm run lint`    | Lint the source (Oxlint)                         |

Frontend routes: `/dashboard`, `/departments`, `/doctors` and `/patients`
(implemented), plus an `/appointments` placeholder and a 404 page.
See `frontend/README.md` for the source layout.

## Backend tests

Tests use the `medicore_test` schema and roll back per test.

```powershell
mvn -f backend-spring/pom.xml test
```

## API endpoints (Phases 1–5)

### Departments

| Method | Path                                  | Description                          |
|--------|---------------------------------------|--------------------------------------|
| POST   | `/api/v1/departments`                 | Create a department                  |
| GET    | `/api/v1/departments`                 | List departments (filter/paginate)   |
| GET    | `/api/v1/departments/{id}`            | Retrieve a department                |
| PUT    | `/api/v1/departments/{id}`            | Update a department                  |
| DELETE | `/api/v1/departments/{id}`            | Deactivate a department (soft delete)|
| POST   | `/api/v1/departments/{id}/activate`   | Reactivate a department              |

List query parameters: `name`, `active`, `page`, `size`, `sort`, `direction`.

Deactivating a department that still has **active doctors** returns **409 Conflict**.

### Doctors

| Method | Path                              | Description                       |
|--------|-----------------------------------|-----------------------------------|
| POST   | `/api/v1/doctors`                 | Create a doctor                   |
| GET    | `/api/v1/doctors`                 | List doctors (filter/paginate)    |
| GET    | `/api/v1/doctors/{id}`            | Retrieve a doctor                 |
| PUT    | `/api/v1/doctors/{id}`            | Update a doctor                   |
| DELETE | `/api/v1/doctors/{id}`            | Deactivate a doctor (soft delete) |
| POST   | `/api/v1/doctors/{id}/activate`   | Reactivate a doctor               |

List query parameters: `name`, `specialization`, `departmentId`, `active`, `page`, `size`, `sort`, `direction`.

Doctor creation/update rules:

- The referenced department must exist (**404** if missing) and be active (**409** if inactive).
- `email`, when provided, must be unique (**409** on duplicate).
- `consultationFee` must be `>= 0`.

### Patients

| Method | Path                              | Description                        |
|--------|-----------------------------------|------------------------------------|
| POST   | `/api/v1/patients`                | Register a patient                 |
| GET    | `/api/v1/patients`                | List patients (search/filter/page) |
| GET    | `/api/v1/patients/{id}`           | Retrieve a patient                 |
| PUT    | `/api/v1/patients/{id}`           | Update a patient                   |
| DELETE | `/api/v1/patients/{id}`           | Deactivate a patient (soft delete) |
| POST   | `/api/v1/patients/{id}/activate`  | Reactivate a patient               |

List query parameters: `name`, `phone`, `active`, `page`, `size`, `sort`, `direction`.

Patient rules:

- Patient numbers are generated **server-side** as `PAT-%06d` from a database sequence and
  guaranteed unique by a DB constraint. Clients cannot supply a patient number.
- `fullName`, `dateOfBirth` (must be in the past) and `phone` are required; phone must match
  `^[0-9+()\-\s]{7,30}$`.
- List responses use a summary shape (`id`, `patientNumber`, `fullName`, `phone`, `active`) to
  avoid exposing date of birth/address; the detail endpoint returns the full record.

### Appointments

| Method | Path                                  | Description                             |
|--------|---------------------------------------|-----------------------------------------|
| POST   | `/api/v1/appointments`                | Book an appointment                     |
| GET    | `/api/v1/appointments`                | List appointments (filter/paginate)     |
| GET    | `/api/v1/appointments/{id}`           | Retrieve an appointment                 |
| PUT    | `/api/v1/appointments/{id}`           | Reschedule/update an appointment        |
| POST   | `/api/v1/appointments/{id}/cancel`    | Cancel an appointment                   |

List query parameters: `date` (UTC calendar day, `yyyy-MM-dd`), `doctorId`, `patientId`, `status`,
`page`, `size`, `sort` (default `startTime`), `direction`.

Appointment rules:

- `startTime`/`endTime` are UTC `Instant` values; `endTime` must be after `startTime` (**400** otherwise).
- References must exist (**404**); the patient, the doctor and the doctor's department must all be
  active (**409** otherwise).
- A doctor cannot have two active (`SCHEDULED`/`CONFIRMED`) appointments whose time ranges overlap
  (**409**). Back-to-back appointments are allowed. Cancelled/completed/no-show appointments do not
  block a slot.
- Status transitions: `SCHEDULED → {CONFIRMED, COMPLETED, CANCELLED, NO_SHOW}`,
  `CONFIRMED → {COMPLETED, CANCELLED, NO_SHOW}`. `COMPLETED`, `CANCELLED` and `NO_SHOW` are terminal:
  they cannot be rescheduled or transition further (**409**). Omitting `status` on update keeps the
  current status (allowing reschedule without a status change).
- Booking/reschedule serialize per doctor using a pessimistic write lock on the doctor row
  (`READ_COMMITTED` isolation), guaranteeing the overlap check cannot race.

### Dashboard

| Method | Path                                     | Description                                            |
|--------|------------------------------------------|--------------------------------------------------------|
| GET    | `/api/v1/dashboard/summary`              | Active entity counts + appointment counts by status    |
| GET    | `/api/v1/dashboard/appointments/today`   | Today's appointments (UTC day), ordered by start time  |
| GET    | `/api/v1/dashboard/appointments/upcoming`| Upcoming active appointments, ordered by start time    |

Query parameters: `/appointments/today` accepts `page`, `size` (default 20, max 100);
`/appointments/upcoming` accepts `limit` (default 10, max 100).

Dashboard rules:

- **Time handling:** all values are UTC. "Today" is the UTC calendar day
  `[00:00:00Z, 24:00:00Z)` (the next day's start is exclusive). "Upcoming" means
  `startTime >= now` **and** status is `SCHEDULED` or `CONFIRMED`.
- Counts are produced by database aggregates (`COUNT`, `GROUP BY`) — no records are loaded
  into memory to compute totals.
- `appointmentsByStatus` always lists all five statuses in declaration order, zero-filled, so
  the response shape is stable.
- Today/upcoming appointment details are fetched with a single fetch join (JPA `EntityGraph`
  over patient, doctor, and doctor's department) to avoid N+1 queries.
- Active counts respect the existing active/inactive flag on departments, doctors, and patients.

Example `GET /api/v1/dashboard/summary`:

```json
{
  "date": "2026-10-09",
  "generatedAt": "2026-10-09T10:32:44Z",
  "totalActivePatients": 1,
  "totalActiveDoctors": 1,
  "totalActiveDepartments": 1,
  "todayAppointments": 1,
  "upcomingAppointments": 3,
  "appointmentsByStatus": [
    { "status": "SCHEDULED", "count": 2 },
    { "status": "CONFIRMED", "count": 1 },
    { "status": "COMPLETED", "count": 1 },
    { "status": "CANCELLED", "count": 1 },
    { "status": "NO_SHOW",   "count": 1 }
  ]
}
```

Example `GET /api/v1/dashboard/appointments/today` (paginated):

```json
{
  "content": [
    {
      "id": 3,
      "patientId": 1,
      "patientNumber": "PAT-000003",
      "patientName": "Jane Doe",
      "doctorId": 1,
      "doctorName": "Dr. Gregory House",
      "departmentId": 1,
      "departmentName": "Cardiology",
      "startTime": "2026-10-09T12:00:00Z",
      "endTime": "2026-10-09T12:30:00Z",
      "status": "SCHEDULED"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

Example `GET /api/v1/dashboard/appointments/upcoming?limit=5` (array):

```json
[
  {
    "id": 3,
    "patientId": 1,
    "patientNumber": "PAT-000003",
    "patientName": "Jane Doe",
    "doctorId": 1,
    "doctorName": "Dr. Gregory House",
    "departmentId": 1,
    "departmentName": "Cardiology",
    "startTime": "2026-10-14T10:32:44Z",
    "endTime": "2026-10-14T11:02:44Z",
    "status": "CONFIRMED"
  }
]
```

### Error response shape

```json
{
  "timestamp": "2026-01-01T00:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed.",
  "path": "/api/v1/departments",
  "fieldErrors": [{ "field": "name", "message": "name is required" }]
}
```
