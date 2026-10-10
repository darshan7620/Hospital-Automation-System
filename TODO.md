# MediCore — Master Project To-Do List

Status legend: `[x]` done & verified, `[ ]` not started, `[~]` in progress / partial.

## 1. Project Configuration
- [x] Inspect the current workspace; never overwrite existing work without permission.
- [x] Set up a single-repository modular monolith.
- [x] Backend: Java 21, Spring Boot 3.x, Maven.
- [x] Frontend: React, Vite, JavaScript. _(Phase 6)_
- [x] Database: MySQL with Flyway migrations.
- [x] Backend libraries: Spring MVC, Spring Data JPA, Hibernate, Jakarta Bean Validation.
- [x] Testing: JUnit 5, Mockito, Spring Boot Test.
- [x] API documentation: Swagger/OpenAPI.
- [x] Configure environment variables for database credentials.
- [x] Create a root README with setup and run instructions.

## 2. Backend Foundation
- [x] Establish clean package structure and consistent naming conventions.
- [x] Create common DTO conventions, validation, exception handling, and API error responses.
- [x] Use `/api/v1` as the API prefix.
- [x] Use DTOs rather than exposing JPA entities directly.
- [x] Use appropriate HTTP status codes and transactional service methods.
- [x] Add pagination and filtering where appropriate.
- [x] Add tests for success cases, validation failures, missing resources, and conflicts.

## 3. Department Management
- [x] Create Department entity, repository, DTOs, service, and controller.
- [x] Support create, list, retrieve, update, and deactivate operations.
- [x] Enforce unique department names.
- [x] Prevent unsafe deletion when linked doctors or records exist. _(No hard delete is exposed; departments are soft-deactivated. Deactivation returns HTTP 409 when active doctors are linked — see Phase 2.)_
- [x] Add tests and verify the endpoints.

## 4. Doctor Management
- [x] Create Doctor entity, repository, DTOs, service, and controller.
- [x] Store name, specialization, contact details, department, consultation fee, and active status.
- [x] Support create, list, retrieve, update, and deactivate operations.
- [x] Validate department references and input data.
- [x] Add filtering by name, specialization, department, and active status.
- [x] Add tests.

## 5. Patient Management
- [x] Create Patient entity, repository, DTOs, service, and controller.
- [x] Store patient number, full name, date of birth, phone, address, and creation timestamp.
- [x] Generate a unique patient number.
- [x] Support create, list, retrieve, update, and search operations.
- [x] Validate input and handle missing patients properly.
- [x] Avoid exposing unnecessary personal data in API responses.
- [x] Add tests.

## 6. Appointment Management
- [x] Create Appointment entity, repository, DTOs, service, and controller.
- [x] Support booking, listing, retrieval, rescheduling, and cancellation.
- [x] Link appointments to existing patients and doctors.
- [x] Include start time, end time, reason, status, and creation timestamp.
- [x] Use statuses: SCHEDULED, CONFIRMED, COMPLETED, CANCELLED, NO_SHOW.
- [x] Validate appointment times and status transitions.
- [x] Prevent overlapping active appointments for the same doctor.
- [x] Account for concurrent booking requests, not just a preliminary availability query. _(Pessimistic write lock on the doctor row + `READ_COMMITTED` isolation; verified by a concurrency test.)_
- [x] Allow filtering by date, doctor, patient, and status.
- [x] Add tests for conflicts, invalid times, cancellations, and normal bookings.

## 7. Dashboard APIs
- [x] Provide summary counts for patients, doctors, departments, and appointments.
- [x] Provide appointment counts by status.
- [x] Provide today's appointment list and upcoming appointments.
- [x] Ensure dashboard results come from real database data.

## 8. Frontend Foundation
- [x] Create the React + Vite application.
- [x] Set up React Router.
- [x] Establish a reusable layout with sidebar, top navigation, and content area.
- [x] Create a reusable API service layer using Fetch API.
- [x] Configure the backend base URL through environment variables.
- [x] Add loading, empty, success, and error states.
- [x] Keep components modular and responsive.

## 9. Frontend Modules
- [x] Dashboard with summary cards and appointment overview.
- [x] Department list, create, edit, and deactivate screens.
- [x] Doctor list, create, edit, and deactivate screens.
- [x] Patient list, registration, editing, and search screens.
- [x] Appointment booking form with doctor and patient selection.
- [x] Appointment schedule/list with filters and status actions.
- [x] Form validation and user-friendly API error messages. _(All four modules — Departments, Doctors, Patients, and Appointments — use the shared Modal/Alert/fieldErrors/409 pattern.)_
- [x] Consistent design system, accessible forms, and responsive layout. _(All four modules adopt the tokens/accessibility/responsive pattern.)_

## 10. Database and Quality
- [x] Create versioned Flyway migrations with primary keys, foreign keys, unique constraints, and indexes.
- [x] Avoid unsafe cascade deletes.
- [x] Use a consistent date/time policy and document it.
- [ ] Add representative development-only sample data if needed.
- [x] Never use real patient information in test data.
- [x] Run backend tests and frontend build checks. _(backend tests green; frontend lint + production build green in Phase 6)_
- [x] Fix errors before marking tasks complete.
- [x] Document API endpoints, database configuration, and startup commands.

## 11. Explicitly Out of Scope
- No JWT.
- No Spring Security.
- No login or authentication in this phase.
- No microservices, Eureka, API Gateway, Kafka, or Redis.
- No pharmacy, billing, insurance, laboratory, inpatient beds, or AI features.

---

## Phase 1 Progress Log

### Files created (Phase 1)
- `TODO.md`, `README.md`, `.gitignore`
- `backend/pom.xml`
- `backend/src/main/resources/application.yml`
- `backend/src/main/resources/application-local.yml` (gitignored, local creds)
- `backend/src/main/resources/db/migration/V1__create_departments.sql`
- `backend/src/main/java/com/medicore/MediCoreApplication.java`
- `backend/src/main/java/com/medicore/config/OpenApiConfig.java`
- `backend/src/main/java/com/medicore/common/dto/PageResponse.java`
- `backend/src/main/java/com/medicore/common/exception/{ApiErrorResponse,FieldValidationError,ResourceNotFoundException,ConflictException,GlobalExceptionHandler}.java`
- `backend/src/main/java/com/medicore/department/{Department,DepartmentRepository,DepartmentService,DepartmentController,DepartmentMapper}.java`
- `backend/src/main/java/com/medicore/department/dto/{DepartmentRequest,DepartmentResponse}.java`
- `backend/src/test/resources/application.yml`
- `backend/src/test/java/com/medicore/department/{DepartmentServiceTest,DepartmentControllerTest,DepartmentPersistenceTest}.java`

### Environment verified
- Java 21.0.12, Maven 3.9.16, MySQL 8.0.46 (service `MySQL80`, running), Node 24.17.0, npm 11.13.0.
- Workspace `D:\MediCore` was **empty** at start (no existing work overwritten).
- Provisioned schemas `medicore` (app) and `medicore_test` (tests) plus dedicated user `medicore`.

### Commands executed / results
| Command | Result |
|---------|--------|
| `mvn -f backend/pom.xml test` | **BUILD SUCCESS** — `Tests run: 12, Failures: 0, Errors: 0, Skipped: 0` (DepartmentServiceTest 5, DepartmentControllerTest 5, DepartmentPersistenceTest 2). Flyway applied `V1` to `medicore_test`. |
| `mvn -f backend/pom.xml -DskipTests package` | **BUILD SUCCESS** — produced `target/medicore-backend-0.0.1-SNAPSHOT.jar`. |
| `java -jar ... --spring.profiles.active=local` + REST smoke test | App started on :8080; Flyway applied `V1` to `medicore`. `POST` → **201**; duplicate `POST` → **409**; missing `GET` → **404**; blank-name `POST` → **400**; `DELETE` → **204**. Timestamps returned in UTC (e.g. `09:49:42Z`). |

### Phase 1 status: COMPLETE
All Phase 1 checklist items are done and verified. Awaiting review before Phase 2 (Doctor Management).

### Notes / follow-ups
- `DELETE /departments/{id}` is a **soft delete (deactivate)**; no hard delete is exposed, satisfying the "prevent unsafe deletion" requirement. The link-aware guard (doctors/appointments) will be added when those entities exist.
- `application-local.yml` is gitignored; `application-local.example.yml` documents the shape.
- No authentication/security (out of scope per requirements).

---

## Phase 2 Progress Log — Doctor Management

### Files created
- `backend/src/main/resources/db/migration/V2__create_doctors.sql`
- `backend/src/main/java/com/medicore/doctor/Doctor.java`
- `backend/src/main/java/com/medicore/doctor/DoctorRepository.java`
- `backend/src/main/java/com/medicore/doctor/DoctorMapper.java`
- `backend/src/main/java/com/medicore/doctor/DoctorService.java`
- `backend/src/main/java/com/medicore/doctor/DoctorController.java`
- `backend/src/main/java/com/medicore/doctor/dto/DoctorRequest.java`
- `backend/src/main/java/com/medicore/doctor/dto/DoctorResponse.java`
- `backend/src/test/java/com/medicore/doctor/DoctorServiceTest.java`
- `backend/src/test/java/com/medicore/doctor/DoctorControllerTest.java`
- `backend/src/test/java/com/medicore/doctor/DoctorPersistenceTest.java`

### Files modified
- `backend/src/main/java/com/medicore/department/DepartmentService.java` — added `DoctorRepository` dependency and the department link-guard in `deactivate`.
- `backend/src/test/java/com/medicore/department/DepartmentServiceTest.java` — added `DoctorRepository` mock + link-guard conflict test.
- `README.md`, `TODO.md`.

### Behavior added
- `doctors` table (V2) with FK `fk_doctors_department` → `departments(id)` (no cascade), unique `uk_doctors_email`, and indexes on department_id/active/specialization/full_name.
- CRUD at `/api/v1/doctors` with filters `name`, `specialization`, `departmentId`, `active` and pagination.
- Department reference validation: missing → **404**, inactive → **409**.
- Duplicate email → **409**.
- Department link-guard: deactivating a department with **active** doctors → **409** (HTTP), plus a DB-level FK RESTRICT that prevents hard deletion.

### Commands executed / results
| Command | Result |
|---------|--------|
| `mvn -B -ntp test` | **BUILD SUCCESS** — `Tests run: 29, Failures: 0, Errors: 0, Skipped: 0`. Breakdown: DepartmentServiceTest 6, DepartmentControllerTest 5, DepartmentPersistenceTest 2, DoctorServiceTest 8, DoctorControllerTest 5, DoctorPersistenceTest 3. Flyway applied `V1` + `V2` to `medicore_test`. |
| `mvn -B -ntp -DskipTests package` | **BUILD SUCCESS** — jar produced. |
| Live smoke test (jar, `local` profile) | `POST /doctors` → 201; `DELETE /departments/{id}` with active doctor → **409**; after doctor deactivated → **204**. Flyway applied `V1` + `V2` to `medicore`. |

### Phase 2 status: COMPLETE
Awaiting review before Phase 3 (Patient Management).

### Unresolved issues
- None blocking. The link-guard currently counts only **active** doctors; inactive doctors linked to an inactive department are permitted by design.

---

## Phase 3 Progress Log — Patient Management

### Files created
- `backend/src/main/resources/db/migration/V3__create_patients.sql`
- `backend/src/main/java/com/medicore/patient/Patient.java`
- `backend/src/main/java/com/medicore/patient/PatientNumberSequence.java`
- `backend/src/main/java/com/medicore/patient/PatientRepository.java`
- `backend/src/main/java/com/medicore/patient/PatientNumberSequenceRepository.java`
- `backend/src/main/java/com/medicore/patient/PatientMapper.java`
- `backend/src/main/java/com/medicore/patient/PatientService.java`
- `backend/src/main/java/com/medicore/patient/PatientController.java`
- `backend/src/main/java/com/medicore/patient/dto/PatientRequest.java`
- `backend/src/main/java/com/medicore/patient/dto/PatientResponse.java`
- `backend/src/main/java/com/medicore/patient/dto/PatientSummaryResponse.java`
- `backend/src/test/java/com/medicore/patient/PatientServiceTest.java`
- `backend/src/test/java/com/medicore/patient/PatientControllerTest.java`
- `backend/src/test/java/com/medicore/patient/PatientApiIntegrationTest.java`
- `backend/src/test/java/com/medicore/patient/PatientPersistenceTest.java`

### Files modified
- `backend/src/main/java/com/medicore/common/exception/GlobalExceptionHandler.java` — added `HttpMessageNotReadableException` → 400 handler (needed so malformed bodies such as an invalid date return 400 instead of 500).
- `README.md`, `TODO.md`.

### Behavior added
- `patients` table (V3) with unique `uk_patients_patient_number` and indexes on full_name/phone/active; supporting `patient_number_sequence` table for server-side number generation.
- Patient numbers generated server-side as `PAT-%06d` from the DB sequence; the request DTO has no patient-number field, so client-supplied values are ignored and a DB unique constraint is the final safety net.
- Validation: required `fullName`/`dateOfBirth`/`phone`; `dateOfBirth` must be in the past; phone pattern; unique patient number.
- List returns `PatientSummaryResponse` (id, patientNumber, fullName, phone, active) to avoid exposing date of birth/address; detail returns full `PatientResponse`.
- Endpoints: POST, GET (filters name/phone/active + pagination), GET /{id}, PUT /{id}, DELETE /{id} (deactivate), POST /{id}/activate (convention-consistent, mirrors Department/Doctor).

### Commands executed / results
| Command | Result |
|---------|--------|
| `mvn -B -ntp test` | **BUILD SUCCESS** — `Tests run: 48, Failures: 0, Errors: 0, Skipped: 0`. Patient: 6 service + 7 controller + 3 integration + 3 persistence. Flyway applied V1+V2+V3 to `medicore_test`. |
| `mvn -B -ntp -DskipTests package` | **BUILD SUCCESS** — jar produced. |
| Live smoke test (jar, `local` profile) | POST with `patientNumber=HACKED` → 201 with `PAT-000001`; list returned summary keys only (`id,patientNumber,fullName,phone,active`); invalid input → **400**; missing → **404**; `DELETE` → patient `active=false`. Flyway applied V1+V2+V3 to `medicore`. |

### Phase 3 status: COMPLETE
Awaiting review before Phase 4 (Appointment Management).

### Unresolved issues
- None blocking. Added `POST /patients/{id}/activate` for consistency with the existing modules; it can be removed if you prefer a strict endpoint set.

---

## Phase 4 Progress Log — Appointment Management

### Files created
- `backend/src/main/resources/db/migration/V4__create_appointments.sql`
- `backend/src/main/java/com/medicore/appointment/Appointment.java`
- `backend/src/main/java/com/medicore/appointment/AppointmentStatus.java`
- `backend/src/main/java/com/medicore/appointment/AppointmentRepository.java`
- `backend/src/main/java/com/medicore/appointment/AppointmentMapper.java`
- `backend/src/main/java/com/medicore/appointment/AppointmentService.java`
- `backend/src/main/java/com/medicore/appointment/AppointmentController.java`
- `backend/src/main/java/com/medicore/appointment/dto/{AppointmentRequest,AppointmentUpdateRequest,AppointmentResponse}.java`
- `backend/src/main/java/com/medicore/common/exception/BadRequestException.java`
- `backend/src/test/java/com/medicore/appointment/{AppointmentServiceTest,AppointmentControllerTest,AppointmentPersistenceTest,AppointmentConcurrencyTest}.java`

### Files modified
- `backend/src/main/java/com/medicore/common/exception/GlobalExceptionHandler.java` — added `BadRequestException` → 400 handler.
- `backend/src/main/java/com/medicore/doctor/DoctorRepository.java` — added `findByIdForUpdate` (pessimistic write lock).
- `README.md`, `TODO.md`.

### Behavior added
- `appointments` table (V4) with FKs to `patients`/`doctors` (no cascade), `chk_appointments_time` (`end_time > start_time`), and indexes including `idx_appointments_doctor_status_start`.
- Endpoints under `/api/v1/appointments`: POST (book), GET (filters `date`/`doctorId`/`patientId`/`status` + pagination), GET /{id}, PUT /{id} (reschedule/status change), POST /{id}/cancel.
- Times are UTC `Instant`; `endTime` must be after `startTime` (**400**).
- References must exist (**404**) and the patient, doctor and doctor's department must be active (**409** if inactive).
- Overlap prevention for active statuses (`SCHEDULED`/`CONFIRMED`) using strict interval overlap (back-to-back allowed); CANCELLED/COMPLETED/NO_SHOW are terminal and do not block slots.
- Status transition map enforced; omitting `status` on update keeps the current status (so an appointment can be rescheduled without a status change).
- Concurrency: booking/reschedule take a pessimistic write lock on the doctor row under `READ_COMMITTED` isolation, so the overlap check cannot race two simultaneous bookings.
- Fixed a defect surfaced by the tests: `update` previously rejected keeping an unchanged non-terminal status (self-transition), which blocked reschedules; now `target != current && !current.canTransitionTo(target)` triggers the transition error.

### Commands executed / results
| Command | Result |
|---------|--------|
| `mvn -B -ntp test` | **BUILD SUCCESS** — `Tests run: 82, Failures: 0, Errors: 0, Skipped: 0`. Appointment: 12 service + 9 controller + 12 persistence + 1 concurrency = 34 new. Flyway applied V1–V4 to `medicore_test`. |
| `mvn -B -ntp -DskipTests package` | **BUILD SUCCESS** — jar produced. |
| Live smoke test (jar, `local` profile, :8090) | Book → **201** `SCHEDULED`; overlapping book → **409**; list by doctor+date → 1; cancel → `CANCELLED`; rebook same slot after cancel → **201**; inverted range → **400**; missing id → **404**. Flyway applied V1–V4 to `medicore`; smoke rows removed afterward. |

### Phase 4 status: COMPLETE
Awaiting review before Phase 5 (Dashboard APIs).

### Unresolved issues
- None blocking. Appointments have no delete/deactivate endpoint (by design — cancellation is the terminal action); the table grows monotonically.

---

## Phase 5 Progress Log — Dashboard APIs

### Files created
- `backend/src/main/java/com/medicore/appointment/AppointmentStatusCount.java` (aggregate projection)
- `backend/src/main/java/com/medicore/dashboard/DashboardService.java`
- `backend/src/main/java/com/medicore/dashboard/DashboardController.java`
- `backend/src/main/java/com/medicore/dashboard/DashboardMapper.java`
- `backend/src/main/java/com/medicore/dashboard/dto/{DashboardSummaryResponse,DashboardAppointmentResponse,AppointmentStatusCountResponse}.java`
- `backend/src/test/java/com/medicore/dashboard/{DashboardServiceTest,DashboardControllerTest,DashboardIntegrationTest,DashboardQueryEfficiencyTest}.java`

### Files modified
- `backend/src/main/java/com/medicore/department/DepartmentRepository.java` — added `countByActiveTrue`.
- `backend/src/main/java/com/medicore/doctor/DoctorRepository.java` — added `countByActiveTrue`.
- `backend/src/main/java/com/medicore/patient/PatientRepository.java` — added `countByActiveTrue`.
- `backend/src/main/java/com/medicore/appointment/AppointmentRepository.java` — added today/upcoming counts, `countGroupedByStatus`, and fetch-join (`@EntityGraph`) detail queries.
- `README.md`, `TODO.md`.

### Behavior added
- `GET /api/v1/dashboard/summary`: active patient/doctor/department counts, today's appointment count, upcoming count, and `appointmentsByStatus` (all five statuses, zero-filled).
- `GET /api/v1/dashboard/appointments/today`: paged (default 20, max 100), ordered by `startTime asc`, with patient number/name, doctor name, department name, start/end, status.
- `GET /api/v1/dashboard/appointments/upcoming`: `limit` (default 10, max 100), `startTime >= now`, status in `{SCHEDULED, CONFIRMED}`, ordered by `startTime asc`.
- All aggregates use DB `COUNT`/`GROUP BY`; detail queries use `@EntityGraph` fetch joins (patient, doctor, doctor.department) to avoid N+1.
- Time policy: UTC only; "today" = `[00:00:00Z, next-day 00:00:00Z)`; upcoming lower bound = `Instant.now()`.

### Commands executed / results
| Command | Result |
|---------|--------|
| `mvn -B -ntp test` | **BUILD SUCCESS** — `Tests run: 101, Failures: 0, Errors: 0, Skipped: 0`. Dashboard: 6 service + 6 controller + 6 integration + 1 query-efficiency = 19 new. |
| `mvn -B -ntp -DskipTests package` | **BUILD SUCCESS** — jar produced. |
| Live smoke test (jar, `local` profile, :8091) | Created 6 appointments across all statuses. Summary: `patients=1 doctors=1 departments=1 today=1 upcoming=3`; status counts `SCHEDULED=2,CONFIRMED=1,COMPLETED=1,CANCELLED=1,NO_SHOW=1`; today returned full patient/doctor/department info; upcoming returned only `SCHEDULED`/`CONFIRMED` ordered by start. Smoke rows removed afterward (dev DB back to 0/0/0/0). |
| Query-efficiency test | With `hibernate.generate_statistics=true`, loading 5 appointments (distinct departments) used `<= 4` prepared statements (no N+1). |

### Phase 5 status: COMPLETE
Awaiting review before Phase 6 (Frontend).

### Unresolved issues
- None blocking. Dashboard endpoints are read-only and add no new persisted state. Appointments remain the only non-deletable table (unchanged from Phase 4).

---

## Phase 6 Progress Log — Frontend Foundation (React + Vite)

### Files created
- `frontend/package.json`, `frontend/package-lock.json`, `frontend/vite.config.js`, `frontend/index.html`, `frontend/.gitignore`, `frontend/.oxlintrc.json` (scaffolded by create-vite, then customized), plus `frontend/.env.example` and `frontend/README.md` (project-specific)
- `frontend/public/favicon.svg`
- `frontend/src/main.jsx`, `frontend/src/App.jsx`
- `frontend/src/styles/tokens.css`, `frontend/src/styles/global.css`
- `frontend/src/lib/api.js` (Fetch layer, `ApiError`, query-string builder), `frontend/src/lib/format.js` (status labels/classes, UTC-aware date/time formatting)
- `frontend/src/api/dashboardApi.js`
- `frontend/src/hooks/useFetch.js`
- `frontend/src/components/common/{StateBlocks.jsx,StatCard.jsx,icons.jsx,common.css}`
- `frontend/src/components/layout/{AppLayout.jsx,Sidebar.jsx,Topbar.jsx,layout.css}`
- `frontend/src/pages/{DashboardPage.jsx,dashboard.css,PlaceholderPage.jsx,pages.css}`

### Files created (backend, CORS support for the frontend origin)
- `backend/src/main/java/com/medicore/config/WebConfig.java` — plain Spring MVC CORS on `/api/**` (no Spring Security).

### Files modified
- `backend/src/main/resources/application.yml` — added `medicore.cors.allowed-origins` (`CORS_ALLOWED_ORIGINS`, default `http://localhost:5173,http://localhost:4173`).
- `TODO.md`, `README.md`.
- `frontend/index.html` — title/meta/favicon set to MediCore (template default replaced).

### Behavior added
- React 19 + Vite 8 + React Router 7 SPA in `frontend/` (JavaScript, no TypeScript).
- Reusable layout: dark sidebar nav (Dashboard/Departments/Doctors/Patients/Appointments), sticky topbar with page title + hamburger, content area. Off-canvas sidebar below 900 px with overlay; pages/tables collapse responsively.
- Routes: `/` → `/dashboard`; module routes render Phase-7 placeholder pages; unknown routes render a 404 page.
- Fetch API service layer (`src/lib/api.js`): base URL from `VITE_API_BASE_URL` (default `http://localhost:8080/api/v1`), JSON handling for GET/POST/PUT/DELETE, 204 → `null`, backend `ApiErrorResponse` parsed into `ApiError` (`status`, `message`, `fieldErrors`), network failure → friendly "backend not reachable" message.
- Shared `useFetch` hook drives four explicit UI states everywhere: **loading** (spinner), **error** (message + HTTP status + Retry), **empty** (illustration + text), **success**.
- Dashboard page wired to the real Phase 5 endpoints only: `GET /dashboard/summary` (5 stat cards + by-status bar breakdown, zero-filled from the stable response shape), `GET /dashboard/appointments/today` (table with Prev/Next pagination using `page/size/totalElements/totalPages/last`), `GET /dashboard/appointments/upcoming` (top-10 list). Each section loads, errors, and retries independently; a global Refresh reloads all three.
- Status badges for all five appointment statuses; timestamps are UTC on the wire and rendered in the browser's local zone (noted in the UI).
- Backend CORS: allowed origins configurable via `CORS_ALLOWED_ORIGINS`; disallowed origins receive no `Access-Control-Allow-Origin` header.

### Commands executed / results
| Command | Result |
|---------|--------|
| `npm create vite@latest frontend -- --template react` | Scaffolded `frontend/` (create-vite 9.2.1). Versions after install: react/react-dom 19.2.8, react-router-dom 7.18.4, vite 8.3.4, @vitejs/plugin-react 6.1.1, oxlint 1.81.0. `npm install` → 0 vulnerabilities. Template demo files removed and replaced with the app. |
| `npm run lint` (oxlint) | **Clean on the final tree** — 0 errors, 0 warnings. Warnings surfaced during development were fixed, not suppressed: `AppLayout` `set-state-in-effect` (close-on-navigation switched to the render-adjust pattern) and three `useFetch` warnings (refs/`set-state-in-effect`/`exhaustive-deps` — hook rewritten with keyed state, a ref-sync effect, and a static effect dependency array). |
| `npm run build` (vite 8.3.4) | **BUILD SUCCESS** — `dist/index.html` 0.54 kB (gzip 0.33), CSS 11.53 kB (gzip 2.89), JS 277.98 kB (gzip 87.54); built in ~0.25 s. |
| `mvn -B -ntp test` | **BUILD SUCCESS** — `Tests run: 101, Failures: 0, Errors: 0, Skipped: 0` (run after each backend change: adding `WebConfig`, widening default CORS origins, and removing comments). |
| `mvn -B -ntp -DskipTests package` | **BUILD SUCCESS** — jar produced (includes `WebConfig`). |
| Live API smoke (jar, `local` profile, :8080) | `GET /dashboard/summary` + `Origin: http://localhost:5173` → **200** + `Access-Control-Allow-Origin`; `OPTIONS` preflight → **200** + allow-origin/methods/headers/max-age; `appointments/today` → **200** `PageResponse` shape (`totalElements`, `size`, `last`); `appointments/upcoming` → **200** JSON array; disallowed `Origin: http://evil.example` → **no ACAO header** (Spring MVC answers 403). Summary parsed by the same JSON shape the frontend uses: `date`, `generatedAt`, 5 counts, `appointmentsByStatus` (5 entries). Dev DB is empty (0/0/0/0) as left by Phase 5. |
| `npm run preview` smoke (served `dist/`) | `GET /` → **200**, title `MediCore`, `#root` present; `GET /dashboard` and `GET /appointments` → **200** (SPA history fallback); built JS → **200** `text/javascript`, CSS → **200** `text/css`; built bundle contains the `/dashboard` and `/departments` routes and the `localhost:8080/api/v1` base URL. Preview server stopped afterwards. |

### Phase 6 status: COMPLETE
All Phase 8 checklist items and the Dashboard item of Phase 9 are done and verified. Awaiting review before Phase 7 (remaining Frontend Modules: Department/Doctor/Patient/Appointment screens).

### Unresolved issues
- No browser automation (Playwright/Selenium) was used; verification = lint + production build + `vite preview` HTTP checks + live CORS/API smoke tests.
- The dev database is empty, so the dashboard currently renders its empty states; "development-only sample data" (TODO §10) remains open and would make the dashboard more demonstrable.
- Disallowed cross-origin origins receive HTTP 403 from Spring MVC CORS (browsers block them regardless); allowed origins are controlled by `CORS_ALLOWED_ORIGINS`.

---

## Phase 7 Progress Log — Department Management UI

### Files created (frontend)
- `frontend/src/api/departmentApi.js` — department API module (list/get/create/update/deactivate/activate) built on the shared Fetch layer (`api`, `toQueryString`).
- `frontend/src/pages/DepartmentsPage.jsx` and `frontend/src/pages/departments.css` — list page: debounced name search, active/inactive filter, pagination, loading/error/empty/empty-filtered states, per-row view/edit/deactivate/reactivate actions, success/error feedback banner, and the deactivate-confirm dialog with conflict messages.
- `frontend/src/pages/DepartmentFormModal.jsx` — create/edit modal with client-side validation matching backend limits (name required ≤100, description ≤255, location ≤150), server `fieldErrors` mapped per-field, 409 conflict shown as a form alert, and duplicate-submission prevention while saving.
- `frontend/src/pages/DepartmentDetailModal.jsx` — read-only detail view backed by `GET /departments/{id}` (name, status badge, description, location, created/updated timestamps, id) with its own loading/error/retry states.
- `frontend/src/components/common/{Modal,ConfirmDialog,Alert}.jsx` — reusable accessible dialog (Escape/overlay close, focus management, scroll lock), confirm dialog with busy/error states, and a type-aware alert banner (`success`/`error`/`info`, `role="status"`/`role="alert"`).

### Files modified (frontend)
- `frontend/src/App.jsx` — replaced the `/departments` placeholder route with the real `DepartmentsPage`.
- `frontend/src/components/common/common.css` — added alert, modal, form-field, and `badge--active`/`badge--inactive` styles (design tokens only, no new dependencies).
- `frontend/src/components/common/icons.jsx` — added `PlusIcon`, `EyeIcon`, `EditIcon`, `BanIcon`, `RefreshIcon`.
- `frontend/src/styles/global.css` — added `btn--danger` and `btn--icon` button variants.

### Behavior
- The page shows a live department count for the active filter, searches by name (300 ms debounce, resets to page 0), and filters by active/inactive/all status.
- Create, edit, deactivate, and reactivate all refresh the list and count immediately and surface the backend response (`409` duplicate name shown in the form; `409` active-doctors conflict shown in the deactivate dialog; per-row busy state prevents double submission).
- No frontend API fallback/fake data: every test below ran against the real backend.

### Commands executed / results
| Command | Result |
|---------|--------|
| `npm run lint` (oxlint) | **Clean** — 0 errors, 0 warnings on the final tree. One `set-state-in-effect` warning was resolved during development by moving the page-clamp logic out of an effect and into the deactivate handler instead. |
| `npm run build` (vite) | **BUILD SUCCESS** — `dist/index.html` 0.54 kB, CSS 15.69 kB (gzip 3.53), JS 292.86 kB (gzip 90.59). Built in ~0.5 s. |
| Live API smoke (backend, `local` profile, :8080) | All department flows verified against real HTTP endpoints (see below). |
| `mvn -B -ntp test` | Not re-run — no backend source changed in this phase. |

### Live smoke tests (dev DB was empty before/after; fictional data, cleaned up afterward)
| # | Scenario | Endpoint | Result |
|---|----------|----------|--------|
| 1 | List departments on an empty database | `GET /departments` | **200** `{"content":[],"totalElements":0,...}` |
| 2 | Create a department | `POST /departments` | **201** → `id=5` "Phase7 Smoke Test Clinic" |
| 3 | Retrieve department details | `GET /departments/5` | **200** full response (name/description/location/active/timestamps) |
| 4 | Update a department | `PUT /departments/5` | **200** — description/location updated, name unchanged |
| 5 | Duplicate department name | `POST /departments` (same name) | **409 Conflict** |
| 6 | Deactivate department, no active doctors | `DELETE /departments/5` | **204** → `GET /departments/5` `active=false` |
| 7 | Reactivate a department | `POST /departments/5/activate` | **200** → `active=true` |
| 8 | Deactivate department with an active doctor | `DELETE /departments/5` (after creating `id=4` active doctor in it) | **409** body: `"Department with id 5 has 1 active doctor(s) and cannot be deactivated."` |

### Cleanup
- Created two fictional records: department `id=5`, doctor `id=4` (email `faye.halifax@example.test`). Both were deleted **by exact primary key** via MySQL against the dev `medicore` schema; column counts verified 0/0 afterwards. No other rows were touched; the database was not reset.

### Phase 7 status: Department Management UI COMPLETE
- §9 "Department list, create, edit, and deactivate screens" marked done.
- `/departments` is documented as implemented in both `README.md` and `frontend/README.md`.
- Optional checklist item "Form validation and user-friendly API error messages" is complete for Departments (part of §9's Department item), but it remains open in the checklist until all modules (Doctor/Patient/Appointment) adopt the same validation patterns.

### Unresolved issues
- No browser automation used; verification = lint + production build + live API smoke tests against the real backend.
- Dev database left empty (as before this phase); sample-data item (TODO §10) still open.
- `README.md` still references the pre-rename `backend/` folder for backend commands; the folder is now `backend-spring/`. Path fixes deferred to keep this change set focused on Phase 7; flagged here per the "report, don't silently fix" rule.

---

## Phase 8 Progress Log — Doctor Management UI

### Files created (frontend)
- `frontend/src/api/doctorApi.js` — doctor API module (list with `name`/`specialization`/`departmentId`/`active`/`page`/`size`/`sort`/`direction`, get, create, update, deactivate, activate) built on the shared Fetch layer.
- `frontend/src/pages/DoctorsPage.jsx` and `frontend/src/pages/doctors.css` — list page: debounced name + specialization search, department and active/inactive filters, pagination, sortable columns (fullName, specialization, consultationFee), loading/error/empty/empty-filtered states, per-row view/edit/deactivate/reactivate actions, success/error feedback banner, and the deactivate-confirm dialog.
- `frontend/src/pages/DoctorFormModal.jsx` — create/edit modal with client-side validation matching backend limits (fullName ≤150, specialization ≤100, phone ≤30, email ≤150 + format, consultationFee ≥0 with ≤8 integer + ≤2 fraction digits, department required), active-department dropdown (prepend current department when editing), server `fieldErrors` mapped per-field, 409 duplicate-email shown as a form alert, duplicate-submission prevention while saving.
- `frontend/src/pages/DoctorDetailModal.jsx` — read-only detail view backed by `GET /doctors/{id}` (full name, specialization, department, status badge, phone/email, formatted consultation fee, created/updated timestamps, id) with its own loading/error/retry states.

### Files modified (frontend)
- `frontend/src/App.jsx` — replaced the `/doctors` placeholder route with the real `DoctorsPage`.
- `frontend/src/lib/format.js` — added `formatCurrency` (USD, 2 fraction digits).

### Behavior
- The page reuses the established Phase 7 UI patterns end-to-end: the `useFetch` hook (loading/error/empty/success), `Alert`, `ConfirmDialog`, `Modal`, `StateBlocks`, and icon components — no new dependencies.
- Filters/search reset to page 0; per-row busy state prevents double submission; create/edit/deactivate/reactivate refresh the list and live doctor count and surface backend responses (409 duplicate email in the form, 409 inactive-department on submit, 404/400 surfaced from the API error shape).
- No frontend API fallback/fake data: every smoke check below ran against the real backend.

### Commands executed / results
| Command | Result |
|---------|--------|
| `npm run lint` (oxlint) | **Clean** — 0 errors, 0 warnings on the final tree. |
| `npm run build` (vite 8.3.4) | **BUILD SUCCESS** — `dist/index.html` 0.54 kB, CSS 16.90 kB (gzip 3.67), JS 311.83 kB (gzip 93.30). Built in ~667 ms. |
| Live API smoke (backend jar, `local` profile, :8080; Vite preview :4173) | 27/27 checks PASS (see table below). Dev DB cleaned to 0/0/0/0 afterwards. |
| `mvn -B -ntp test` | Not re-run — no backend source changed in this phase. Backend jar `medicore-backend-0.0.1-SNAPSHOT.jar` from Phase 7 used. |

### Live smoke tests (dev DB was 0/0/0/0 before and after; fictional data, cleaned up by exact primary key afterward)
| # | Scenario | Endpoint | Result |
|---|----------|----------|--------|
| 1 | Create department (assignment target) | `POST /departments` | **201** id=9 (dept A) |
| 2 | Create department (inactive-candidate) | `POST /departments` | **201** id=10 (dept B) |
| 3 | Create doctor A | `POST /doctors` | **201** id=5, `"Dr. Phase8 Alpha"`, fee 150.00 |
| 4 | Create doctor B | `POST /doctors` | **201** id=6, `"Dr. Phase8 Beta"`, fee 200.00 |
| 5 | List by department + active | `GET /doctors?departmentId=9&active=true` | **200** `totalElements=2`, page 0 |
| 6 | Filter by name | `GET /doctors?name=Phase8+Alpha` | **200** → 1 row `"Dr. Phase8 Alpha"` |
| 7 | Filter by specialization | `GET /doctors?specialization=neurology` | **200** → 1 row `Neurology` |
| 8 | Filter active=false | `GET /doctors?active=false` | **200** `totalElements=0` |
| 9 | Retrieve doctor A | `GET /doctors/5` | **200** full response (dept name, fee, active=true) |
| 10 | Update doctor A | `PUT /doctors/5` | **200** — specialization `Cardiology & Imaging`, fee 175.50, phone updated |
| 11 | Duplicate email | `POST /doctors` (same email as A) | **409** `"A doctor with email 'phase8.alpha@example.test' already exists."` |
| 12 | Blank full name | `POST /doctors` | **400** `fieldErrors[0]` = `fullName` |
| 13 | Negative fee | `POST /doctors` | **400** `fieldErrors[0]` = `consultationFee` |
| 14 | Invalid email format | `POST /doctors` | **400** `fieldErrors[0]` = `email` |
| 15 | Missing department | `POST /doctors` (`departmentId=999999`) | **404** |
| 16 | Deactivate dept B (no doctors) | `DELETE /departments/10` | **204** |
| 17 | Inactive department assignment | `POST /doctors` (`departmentId=10`) | **409** |
| 18 | Missing doctor | `GET /doctors/999999` | **404** |
| 19 | Sort asc by fee | `GET /doctors?sort=consultationFee&direction=asc` | **200** first row = id 5 (175.50) |
| 20 | Sort desc by fee | `GET /doctors?sort=consultationFee&direction=desc` | **200** first row = id 6 (200.00) |
| 21 | Deactivate doctor A | `DELETE /doctors/5` | **204** |
| 22 | Confirm deactivated | `GET /doctors/5` | **200** `active=false` |
| 23 | Reactivate doctor A | `POST /doctors/5/activate` | **200** `active=true` |
| 24 | CORS allowed origin | `GET /doctors` + `Origin: http://localhost:5173` | **200** + `Access-Control-Allow-Origin: http://localhost:5173` |
| 25 | CORS preflight | `OPTIONS /doctors` | **200** + ACAO |
| 26 | CORS disallowed origin | `GET /doctors` + `Origin: http://evil.example` | no ACAO header |
| 27 | Frontend route/bundle | `GET http://localhost:4173/doctors` (Vite preview) | **200** (SPA fallback); built JS contains 29 `doctors` references + `http://localhost:8080/api/v1` base URL |

### Cleanup
- Deleted **by exact primary key** via MySQL against the dev `medicore` schema: departments `9, 10` and doctors `5, 6` (which the tests created). Verified `doctors=0 departments=0 patients=0 appointments=0` afterwards. No other rows were touched; the database was not reset.
- Stopped both backend Java processes (PID 3260 app + PID 2812 `javapath` launcher) and the Vite preview server after tests/cleanup; ports 8080/4173 verified free.

### Phase 8 status: Doctor Management UI COMPLETE
- §9 "Doctor list, create, edit, and deactivate screens." marked done.
- A note for reviewers: the code files for this phase existed before this session (left when the previous terminal closed before verification). This session verified them end-to-end (lint, production build, 27/27 live API smoke checks, frontend route/bundle checks), cleaned up all test data, and completed the documentation. No application code was recreated or reverted.

### Unresolved issues
- No browser automation used; verification = oxlint + production build + live API smoke tests against the real backend (as in Phases 6/7).
- Dev database left empty (as before this phase); sample-data item (TODO §10) still open.
- Optional checklist item "Form validation and user-friendly API error messages" is now satisfied for Departments and Doctors but remains open until Patients/Appointments adopt the same patterns.

---

## Phase 9 Progress Log — Patient Management UI

### Files created (frontend)
- `frontend/src/api/patientApi.js` — patient API module (list with `name`/`phone`/`active`/`page`/`size`/`sort`/`direction`, get, create, update, deactivate, activate) built on the shared Fetch layer.
- `frontend/src/pages/PatientsPage.jsx` and `frontend/src/pages/patients.css` — list page: debounced name + phone search, active/inactive filter, pagination, sortable columns (patientNumber, fullName, phone), loading/error/empty/empty-filtered states, per-row view/edit/deactivate/reactivate actions, success/error feedback banner, and the deactivate-confirm dialog. The list renders **summary-only** fields (`patientNumber`, `fullName`, `phone`, `active`) — date of birth and address are shown only in the detail view.
- `frontend/src/pages/PatientFormModal.jsx` — create/edit modal with client-side validation matching backend limits (fullName ≤150, dateOfBirth required + must be past, phone ≤30 + pattern, address ≤255), an info note that the patient number is assigned by the server (never entered client-side), server `fieldErrors` mapped per-field, and duplicate-submission prevention while saving.
- `frontend/src/pages/PatientDetailModal.jsx` — read-only detail view backed by `GET /patients/{id}` (patient number, full name, date of birth, phone, address, status badge, created/updated timestamps, id) with its own loading/error/retry states.

### Files modified (frontend)
- `frontend/src/App.jsx` — replaced the `/patients` placeholder route with the real `PatientsPage`; the `/appointments` placeholder remains.
- `frontend/src/pages/PatientFormModal.jsx` — hoisted the computed "today" max-date to a module-level constant to satisfy oxlint's `react/purity` rule.
- `TODO.md`, `README.md`, `frontend/README.md`.

### Behavior
- Reuses the established Phase 7/8 UI patterns end-to-end: the `useFetch` hook, `Alert`, `ConfirmDialog`, `Modal`, `StateBlocks`, icons, and page CSS conventions — no new dependencies.
- Patient numbers are never generated or stored client-side; the list/detail show the server-assigned `PAT-xxxxxx` number.
- Filters/search reset to page 0; per-row busy state prevents double submission; create/edit/deactivate/reactivate refresh the list and live patient count and surface backend responses (400 `fieldErrors`, 404) from the standard API error shape.
- No frontend API fallback/fake data: every smoke check below ran against the real backend.

### Commands executed / results
| Command | Result |
|---------|--------|
| `npm run lint` (oxlint) | **Clean** — 0 errors, 0 warnings on the final tree. |
| `npm run build` (vite 8.3.4) | **BUILD SUCCESS** — `dist/index.html` 0.54 kB, CSS 17.91 kB (gzip 3.81), JS 326.96 kB (gzip 94.84). Built in ~470 ms. |
| Live API smoke (backend jar, `local` profile, :8080; Vite preview :4173) | 25/25 checks PASS (see table below). Dev DB cleaned to 0/0/0/0 afterwards. |
| `mvn -B -ntp test` | Not re-run — no backend source changed in this phase. Backend jar `medicore-backend-0.0.1-SNAPSHOT.jar` from Phase 7 used. |

### Live smoke tests (dev DB was 0/0/0/0 before; fictional data, cleaned up by exact primary key afterward)
| # | Scenario | Endpoint | Result |
|---|----------|----------|--------|
| 1 | Register patient A | `POST /patients` | **201** `id=4`, server-generated `PAT-000004`, `active=true` |
| 2 | Register patient B (address omitted) | `POST /patients` | **201** `id=5`, `PAT-000005`, `address=""` |
| 3 | List all patients | `GET /patients?size=20&sort=fullName&direction=asc` | **200** `totalElements=2` |
| 4 | List rows are summary-only (no `dateOfBirth`/`address`) | keys of row | `id,patientNumber,fullName,phone,active` only |
| 5 | Filter by name | `GET /patients?name=Sarah` | **200** → 1 row |
| 6 | Filter by phone | `GET /patients?phone=+15550101002` | **200** → 1 row |
| 7 | Filter active=false (none yet) | `GET /patients?active=false` | **200** `totalElements=0` |
| 8 | Retrieve patient A detail | `GET /patients/4` | **200** full record incl. `dateOfBirth=1992-04-18` + address |
| 9 | Update patient A | `PUT /patients/4` | **200** — name/phone/address updated, `patientNumber` unchanged |
| 10 | Blank full name | `POST /patients` | **400** `fieldErrors[0]` = `fullName` |
| 11 | Future date of birth | `POST /patients` | **400** `fieldErrors[0]` = `dateOfBirth` |
| 12 | Invalid phone (`abc`) | `POST /patients` | **400** `fieldErrors[0]` = `phone` |
| 13 | Missing date of birth | `POST /patients` | **400** `fieldErrors[0]` = `dateOfBirth` |
| 14 | Missing patient | `GET /patients/999999` | **404** |
| 15 | Sort asc by patientNumber | `GET /patients?sort=patientNumber&direction=asc` | **200** first row = id 4 |
| 16 | Sort desc by patientNumber | `GET /patients?sort=patientNumber&direction=desc` | **200** first row = id 5 |
| 17 | Deactivate patient A | `DELETE /patients/4` | **204** |
| 18 | Confirm deactivated | `GET /patients/4` | **200** `active=false` |
| 19 | Reactivate patient A | `POST /patients/4/activate` | **200** `active=true` |
| 20 | CORS allowed origin | `GET /patients` + `Origin: http://localhost:5173` | **200** + `Access-Control-Allow-Origin` |
| 21 | CORS preflight | `OPTIONS /patients` | **200** + ACAO |
| 22 | CORS disallowed origin | `GET /patients` + `Origin: http://evil.example` | no ACAO header |
| 23 | Frontend route/bundle | `GET http://localhost:4173/patients` (Vite preview) | **200** (SPA fallback); built JS contains 26 `patients` references + `http://localhost:8080/api/v1` base URL; only one "Coming in Phase" string remains (the Appointments placeholder) |

### Cleanup
- Deleted **by exact primary key** via MySQL against the dev `medicore` schema: patients `4, 5` (created by these smoke tests). Verified `patients=0 departments=1 doctors=1 appointments=0` afterwards. The `Dentist` department (id=11) and `Dr. Smith` doctor (id=7) are pre-existing rows and were **not** touched; the database was not reset.
- Stopped both backend Java processes (PID 11032 app + PID 7496 `javapath` launcher) and the Vite preview server (PID 3684 + npm wrapper 14944) after tests/cleanup; ports 8080/4173 verified free.
- Note: the backend `Start-Process` returns the `oraclejavapath` launcher PID, not the real JVM PID; the actual app JVM (`jdk-21.0.12\bin\java.exe`) holds port 8080. This looked like a startup hang but startup completed in ~10 s — see the diagnostic note in the Phase 9 session.

### Phase 9 status: Patient Management UI COMPLETE
- §9 "Patient list, registration, editing, and search screens" marked done.
- `/patients` is documented as implemented in both `README.md` and `frontend/README.md`.
- Optional checklist item "Form validation and user-friendly API error messages" is now satisfied for Patients too; it stays `[~]` only because Appointments is still pending.

### Unresolved issues
- No browser automation used; verification = oxlint + production build + live API smoke tests against the real backend (as in Phases 6-8).
- Dev database now contains two pre-existing demo rows left by a reviewer (`Dentist` department id=11, `Dr. Smith` doctor id=7); `patients=0`, `appointments=0`. Sample-data item (TODO §10) still open.
- `PlaceholderPage.jsx` still contains a leftover `Coming in Phase 7` tag (used only by the pending `/appointments` page); it can be updated when the Appointments screen ships in Phase 10.

### Phase 10 Progress Log — Appointment Management UI (COMPLETE, 2026-10-10)
Built on top of the Phase 9 tree (Commit 8c6629c); no new commit made.

**New files** (frontend only, no new backend changes):
- `frontend/src/api/appointmentApi.js` — `/api/v1/appointments` Fetch layer + `ApiError` (matches the existing api.js pattern).
- `frontend/src/pages/AppointmentsPage.jsx` — schedule page: status/doctor/patient/date filters, sort + pagination, actions (book, reschedule, confirm, complete, cancel).
- `frontend/src/pages/AppointmentFormModal.jsx` — booking/reschedule form with doctor + patient selection, client validation, and 409-conflict handling.
- `frontend/src/pages/AppointmentDetailModal.jsx` — detail view with audit + status history.
- `frontend/src/pages/appointments.css` — styles following the existing design tokens.

**Modified files:**
- `frontend/src/App.jsx` — `/appointments` now routes to `AppointmentsPage` (placeholder import removed).
- `frontend/src/lib/format.js` — added `formatDateTime`, status helpers (`allowedNextStatuses`, `isTerminalStatus`).

**Behavior:** book a validated appointment, reschedule it, confirm it, complete it, and cancel one (cancelled appointments cannot be rescheduled). Filter, sort, paginate, open detail modals, and jump to doctor/patient detail views. No new libraries added.

**Verification (evidence in this session):**
- `npm run lint` → oxlint clean (0 errors / 0 warnings).
- `npm run build` → vite v8.3.4, 64 modules transformed; `dist/index.html` 0.54 kB, `dist/assets/index-CW-mGKur.css` 19.12 kB (gzip 3.95 kB), `dist/assets/index-Cyh3vgCf.js` 346.71 kB.
- Vite preview on :4173 served built `/appointments` and `/patients` → **200** (SPA fallback).
- Live API smoke (`phase10_smoke.ps1`, 42 `Check` invocations) against the real backend on :8080 → **PASSED=42 FAILED=0**; created test rows `patientA=6 patientB=7 patientC=8 appointmentA=9 appointmentB=10`.

**Cleanup:**
- Deleted by exact primary key: patients `6, 7, 8` and appointments `9, 10`. Verified `appointments=0 patients=0 departments=1 doctors=1` afterwards (the pre-existing `Dentist`/`Dr. Smith` rows untouched).
- Stopped backend JVM (PID 12868 on :8080) + javapath launcher (PID 3680), and the preview server (PID 15248 wrapper / 11204 child); ports 8080/4173 verified free.

**Java process identification:** the last remaining `java.exe` (PID 12256) was an **Eclipse IDE bundled JRE** under `C:\Users\Asus\.p2\pool\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32...` — unrelated to MediCore, correctly **left running** (it has since exited on its own; no `java.exe` remains).

**Phase 10 status: all §9 Appointments items checked.** `/appointments` documented as implemented in both READMEs.

**Open items:**
- `PlaceholderPage.jsx` is now dead code (its component/`Coming in Phase 7` tag is no longer imported by `App.jsx`). Left untouched per "no app-code changes"; safe to remove later.
- No browser automation; verification = oxlint + production build + preview route 200s + 42/42 live API smoke.
- Sample-data item (TODO §10) still open.
