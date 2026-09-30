---

description: "Task list template for feature implementation"
---

# Tasks: Les Fragments d'Instants — V1 (tâches, Pomodoro, journée, humeur/fatigue)

**Input**: Design documents from `/specs/001-taches-pomodoro-humeur/`
**Prerequisites**: [plan.md](./plan.md), [spec.md](./spec.md), [research.md](./research.md), [data-model.md](./data-model.md), [contracts/api-overview.md](./contracts/api-overview.md), [quickstart.md](./quickstart.md)

**Tests**: Included. The technical context explicitly requests JUnit tests for the domain (written
before implementation — TDD), controller contract tests for the API, and Angular component tests
for critical screens.

**Organization**: Tasks are grouped by user story. **All 8 user stories in spec.md are Priority
P1** — there is no P2/P3 to sequence by. The phase order below (US1 → US8 → US2 → US4 → US5 → US3
→ US6 → US7) is chosen for **buildability**, not priority: US1 is the data foundation everything
else displays; US8 (Settings) is fully independent and is needed by US2 for configurable
durations; US2 (Pomodoro) produces the sessions US3/US6 aggregate; US4 (Tags) needs Task to exist
for its delete-cascade reassignment; US5 (Mood) is independent; US3 (Dashboard) and US6
(Visualizations) are read-only aggregations over US1/US2/US4/US5/US8 and are naturally built
last among the "producing" stories; US7 (persistence) has no code of its own beyond what
Foundational already builds (real SQLite file, Flyway-managed schema) — its phase is a
verification checkpoint, placed last because it is only meaningful once every other story has
produced data to survive a restart. Each phase is still independently testable per its own
"Independent Test" in spec.md.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: Which user story this task belongs to (US1, US2, US3, US4, US5, US6, US7, US8)
- File paths are exact and relative to the repository root (`C:\Users\4261379\Documents\FragmentsInstants`)

## Path Conventions (per plan.md Structure Decision)

- **Backend** (new Spring Boot 4.1.1 Maven module): `backend/src/main/java/com/fragmentsinstants/{domain,application,infrastructure}/`, `backend/src/main/resources/`, `backend/src/test/java/com/fragmentsinstants/`
- **Frontend** (Angular 22 SPA already scaffolded at the repo root): `src/app/core/`, `src/app/shared/`, `src/app/features/`, `src/styles.scss`

---

## Phase 1: Setup

**Purpose**: Stand up both toolchains so every later task has somewhere to land.

- [X] T001 **Feasibility spike (binding per plan.md "Implementation order note")**: initialize the backend Maven project — `backend/pom.xml` (Spring Boot 4.1.1 parent, Java 21, `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `spring-boot-starter-test`, `flyway-core`, `org.flywaydb:flyway-database-nc-sqlite:13.0.0`, `org.xerial:sqlite-jdbc:3.53.4.0`, `org.hibernate.orm:hibernate-community-dialects`, `org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.1`), `backend/mvnw`/`backend/mvnw.cmd`, `backend/src/main/java/com/fragmentsinstants/FragmentsInstantsApplication.java`, and a minimal `backend/src/main/resources/application.yml` pointing at a SQLite file with `org.hibernate.community.dialect.SQLiteDialect`. Then prove the stack with a throwaway migration `backend/src/main/resources/db/migration/V0__spike.sql` (one scratch table), a scratch `@Entity` with `@GeneratedValue(strategy = GenerationType.IDENTITY)` on a `Long id`, a one-method Spring Data repository, and a `@DataJpaTest` in `backend/src/test/java/com/fragmentsinstants/SpikeRepositoryTest.java` that inserts a row and reads back a generated id. Record the outcome as a short comment block atop `V0__spike.sql`. **If `IDENTITY` generation fails or misbehaves under the community SQLite dialect**: switch the plan to UUID ids (data-model.md fallback) before continuing. **If SQLite/Flyway/Hibernate wiring itself is unreliable**: switch to the embedded H2 fallback (data-model.md) before continuing. Delete `V0__spike.sql`, the scratch entity/repository/test once T005–T017 land with the real schema.
- [ ] T002 [P] Add frontend chart dependencies to `package.json` (`ng2-charts@^10.0.0`, `chart.js@^4`) and run `npm install` at the repository root.
- [ ] T003 [P] Add Angular environment files `src/environments/environment.ts` and `src/environments/environment.development.ts`, each exporting `apiBaseUrl: 'http://localhost:8080/api'`, and wire `angular.json`'s `fileReplacements`/`development` configuration to use them if not already present.
- [ ] T004 [P] Add `provideHttpClient()` to the application providers in `src/app/app.config.ts` so every later feature can inject `HttpClient`.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Real schema, entities, repositories, cross-cutting backend infrastructure, and the
shared frontend scaffolding every user story builds on.

**⚠️ CRITICAL**: No user story work (Phase 3 onward) may begin until this phase is complete.

- [ ] T005 Create `backend/src/main/resources/db/migration/V1__init_schema.sql` with tables `tasks`, `tags`, `task_tag`, `pomodoro_sessions`, `mood_entries`, `settings`, all `Long`/`INTEGER PRIMARY KEY AUTOINCREMENT` ids (per T001's confirmed strategy). Encode exactly, from data-model.md: `tasks.title` NOT NULL; `tasks.estimated_minutes` nullable, no negative values; `tasks.priority` NOT NULL CHECK IN (`BASSE`,`NORMALE`,`HAUTE`,`CRITIQUE`) default `NORMALE`; `tasks.due_date` nullable; `tasks.status` NOT NULL CHECK IN (`ACTIVE`,`EN_PAUSE`,`TERMINEE`) default `ACTIVE`; `tasks.completed_at` nullable; `tasks.created_at`/`updated_at` NOT NULL; `task_tag(task_id, tag_id)` composite PK, both FKs NOT NULL; `pomodoro_sessions.mode` NOT NULL CHECK IN (`FOCUS`,`PAUSE_COURTE`,`PAUSE_LONGUE`); `pomodoro_sessions.task_id` nullable FK to `tasks(id)` `ON DELETE SET NULL`; `pomodoro_sessions.started_at`/`ended_at` NOT NULL; `pomodoro_sessions.actual_duration_seconds` NOT NULL; `pomodoro_sessions.completion_status` NOT NULL CHECK IN (`TERMINEE`,`INTERROMPUE`); `pomodoro_sessions.created_at` NOT NULL; `tags.name` NOT NULL UNIQUE (case-insensitive — use a `COLLATE NOCASE` unique index); `tags.is_default`/`is_protected` NOT NULL booleans; `mood_entries.date` NOT NULL UNIQUE; `mood_entries.mood_score`/`physical_fatigue_score`/`mental_fatigue_score` NOT NULL CHECK BETWEEN 1 AND 7; `mood_entries.updated_at` NOT NULL; `settings` single row (`id` fixed at 1) with `focus_duration_minutes`, `short_break_duration_minutes`, `long_break_duration_minutes`, `pomodoros_before_long_break` NOT NULL INTEGER CHECK > 0, `priority_coefficient_low`/`_normal`/`_high`/`_critical` NOT NULL DECIMAL CHECK > 0, `sound_notifications_enabled` NOT NULL boolean.
- [ ] T006 Create `backend/src/main/resources/db/migration/V2__seed_default_tags.sql` inserting exactly the 8 default tags from FR-014 — `Écriture, Administratif, Maison, Enseignement, Créatif, DIY, Recherche, Autre` — all with `is_default = true`; only `Autre` has `is_protected = true` (FR-036).
- [ ] T007 Create `backend/src/main/resources/db/migration/V3__seed_default_settings.sql` inserting the single settings row (`id = 1`) with the FR-009/FR-025/FR-026/Assumptions defaults: focus=25, short break=5, long break=15, pomodoros before long break=4, coefficients low=1, normal=1.25, high=1.5, critical=2, sound notifications=true.
- [ ] T008 [P] Create `TaskEntity` JPA entity in `backend/src/main/java/com/fragmentsinstants/infrastructure/persistence/TaskEntity.java` mapping every column from T005's `tasks` table (data-model.md Task table), with a `@ManyToMany` to `TagEntity` via the `task_tag` join table.
- [ ] T009 [P] Create `PomodoroSessionEntity` JPA entity in `backend/src/main/java/com/fragmentsinstants/infrastructure/persistence/PomodoroSessionEntity.java` mapping the `pomodoro_sessions` table; `actualDurationSeconds` has no public setter reachable from outside the entity/service layer (server-computed only, FR-012) and `taskId` is a nullable `@ManyToOne` with no cascade-remove.
- [ ] T010 [P] Create `TagEntity` JPA entity in `backend/src/main/java/com/fragmentsinstants/infrastructure/persistence/TagEntity.java` mapping the `tags` table (`name`, `isDefault`, `isProtected`).
- [ ] T011 [P] Create `MoodEntryEntity` JPA entity in `backend/src/main/java/com/fragmentsinstants/infrastructure/persistence/MoodEntryEntity.java` mapping the `mood_entries` table (`date` unique, three 1–7 scores, `updatedAt`).
- [ ] T012 [P] Create `SettingsEntity` JPA entity in `backend/src/main/java/com/fragmentsinstants/infrastructure/persistence/SettingsEntity.java` mapping the single-row `settings` table.
- [ ] T013 [P] Create `TaskRepository` (Spring Data JPA) in `backend/src/main/java/com/fragmentsinstants/infrastructure/persistence/TaskRepository.java` with query methods needed by FR-006/FR-028: find by status not equal, find by `dueDate <= :today` and status not equal, find by `completedAt` between two instants.
- [ ] T014 [P] Create `PomodoroSessionRepository` in `backend/src/main/java/com/fragmentsinstants/infrastructure/persistence/PomodoroSessionRepository.java` with query methods: find by `startedAt` between two instants (day/week ranges), find by `taskId`.
- [ ] T015 [P] Create `TagRepository` in `backend/src/main/java/com/fragmentsinstants/infrastructure/persistence/TagRepository.java` with `findByNameIgnoreCase` plus standard CRUD.
- [ ] T016 [P] Create `MoodEntryRepository` in `backend/src/main/java/com/fragmentsinstants/infrastructure/persistence/MoodEntryRepository.java` with `findByDate` and `findByDateBetween`.
- [ ] T017 [P] Create `SettingsRepository` in `backend/src/main/java/com/fragmentsinstants/infrastructure/persistence/SettingsRepository.java` (standard `JpaRepository`, single row accessed via `findById(1L)`).
- [ ] T018 Finalize `backend/src/main/resources/application.yml`: SQLite JDBC URL pointing at `backend/data/fragments-instants.db` (create the `backend/data/` directory), `spring.jpa.hibernate.ddl-auto: validate`, `spring.jpa.database-platform: org.hibernate.community.dialect.SQLiteDialect`, Flyway enabled against `db/migration`, CORS allowed origin `http://localhost:4200`, springdoc paths (`/v3/api-docs`, `/swagger-ui.html`).
- [ ] T019 Implement business-rule error handling (FR-040): `BusinessRuleException` (unchecked) in `backend/src/main/java/com/fragmentsinstants/application/BusinessRuleException.java`, and a `@RestControllerAdvice` in `backend/src/main/java/com/fragmentsinstants/infrastructure/web/GlobalExceptionHandler.java` mapping it to `422 Unprocessable Entity` with body `{ "code": "REGLE_METIER_VIOLEE", "message": string }` (data-model.md "Business-rule error handling"), leaving standard Spring validation errors as `400`.
- [ ] T020 [P] Add a CORS configuration bean (`WebMvcConfigurer`) in `backend/src/main/java/com/fragmentsinstants/infrastructure/web/CorsConfig.java` restricting all `/api/**` routes to the origin `http://localhost:4200`.
- [ ] T021 [P] Create shared frontend TypeScript models/DTOs matching data-model.md and contracts/api-overview.md in `src/app/core/models/` (one file per concept: `task.model.ts`, `pomodoro-session.model.ts`, `tag.model.ts`, `mood-entry.model.ts`, `settings.model.ts`, `dashboard.model.ts`, `stats.model.ts`), including the `REGLE_METIER_VIOLEE` error body shape.
- [ ] T022 Set up the Angular routing shell in `src/app/app.routes.ts` (lazy-loaded routes for `/tasks`, `/pomodoro`, `/` → dashboard, `/tags`, `/mood`, `/stats`, `/settings`) and a minimal navigation shell component in `src/app/shared/components/app-shell/` (`app-shell.ts`/`.html`/`.scss`), mounted from `src/app/app.html`/`src/app/app.ts`.
- [ ] T023 [P] Define the imposed palette as CSS custom properties on `:root` in `src/styles.scss` — `--color-accent: #DF73FF; --color-bronze: #B08D57; --color-plum: #4B2142; --color-forest: #234236; --color-parchment: #F2E8D5;` — plus derived text/background tokens, dark mode as the base theme, no red token anywhere (constitution VI: AA contrast, no red alarmant).
- [ ] T024 Foundational smoke test: `backend/src/test/java/com/fragmentsinstants/FragmentsInstantsApplicationTests.java`, a `@SpringBootTest` confirming the Spring context loads, Flyway migrations (T005–T007) apply cleanly against a fresh SQLite file, the 8 default tags exist with `Autre` protected, and the single settings row exists with its defaults.

**Checkpoint**: Foundation ready — schema, entities, repositories, error handling, CORS, routing shell, and palette are in place. User story implementation can begin.

---

## Phase 3: User Story 1 - Gérer mes tâches (Priority: P1) 🎯 MVP

**Goal**: Full task CRUD, the four filters, lifecycle transitions (complete/reopen/pause/resume), and the `Autre` tag auto-assign/remove rule (FR-035), end-to-end through the UI.

**Independent Test**: Create a task with all attributes, filter it through Toutes/Aujourd'hui/Importantes/Terminées, then complete, pause, and delete it — no dependency on Pomodoro or statistics (quickstart.md scenario 1).

### Tests for User Story 1 ⚠️

> Write these tests FIRST, ensure they FAIL before implementation.

- [ ] T025 [P] [US1] Contract tests for task creation & retrieval in `backend/src/test/java/com/fragmentsinstants/infrastructure/web/TaskControllerTest.java`: `POST /tasks` with a title/priority/estimate only (no tags) assigns `Autre` (FR-035); `GET /tasks/{id}` returns estimated/real/remaining time (FR-008); `GET /tasks?filter=all` lists it.
- [ ] T026 [P] [US1] Contract tests for the three named filters in `backend/src/test/java/com/fragmentsinstants/infrastructure/web/TaskFilterControllerTest.java`, exercising the FR-006 "tâche prévue aujourd'hui" definition: a task due yesterday and not completed appears under `today` (no alert flag in the response); a task due today appears under `today`; a task with no due date does NOT appear under `today` unless completed today; a task completed today appears under `today` regardless of its due date; `important` includes only `HAUTE`/`CRITIQUE` priority (FR-007).
- [ ] T027 [P] [US1] Contract tests for lifecycle transitions in `backend/src/test/java/com/fragmentsinstants/infrastructure/web/TaskLifecycleControllerTest.java`: `POST /tasks/{id}/complete` sets `completedAt` and removes it from `all`/`today`/`important` by default (FR-004); `POST /tasks/{id}/reopen` clears `completedAt`, returns status to `ACTIVE`, and leaves real time spent unchanged (FR-032); `POST /tasks/{id}/pause` and `/resume` toggle `EN_PAUSE` without touching `completedAt` (FR-005); `DELETE /tasks/{id}` on a task with a past session leaves that session's recorded time intact with `taskId = null`.
- [ ] T028 [P] [US1] Domain unit tests for `TaskWeight` in `backend/src/test/java/com/fragmentsinstants/domain/TaskWeightTest.java`: `estimatedMinutes × priorityCoefficient`; `null`/absent estimate → weight `0` (FR-027, spec edge case).
- [ ] T029 [P] [US1] Domain unit tests for `RemainingTime` in `backend/src/test/java/com/fragmentsinstants/domain/RemainingTimeTest.java`: `max(0, estimated - real)`, including the case where real time exceeds the estimate (FR-029).
- [ ] T030 [P] [US1] Contract tests for the `Autre` auto-remove/auto-reassign rule triggered by **task updates** (FR-035, US4 Acceptance Scenarios 2 & 4 — distinct from T025's creation-time case and from T060's tag-deletion cascade) in `backend/src/test/java/com/fragmentsinstants/infrastructure/web/TaskTagAutoAssignControllerTest.java`: `PUT /tasks/{id}` adding a second tag to a task that currently only has `Autre` removes `Autre` automatically, leaving only the newly added tag(s); `PUT /tasks/{id}` removing the last non-`Autre` tag from a task (leaving zero tags in the request) reassigns `Autre` automatically so the task always has at least one tag.

### Implementation for User Story 1

- [ ] T031 [US1] Implement the `TaskWeight` pure formula class in `backend/src/main/java/com/fragmentsinstants/domain/TaskWeight.java` to pass T028 — no Spring annotation, no entity dependency.
- [ ] T032 [US1] Implement the `RemainingTime` pure formula class in `backend/src/main/java/com/fragmentsinstants/domain/RemainingTime.java` to pass T029.
- [ ] T033 [US1] Implement `TaskService` in `backend/src/main/java/com/fragmentsinstants/application/TaskService.java`: create/update/delete, complete/reopen/pause/resume, the `Autre` auto-assign-on-create-with-no-tags and auto-remove/auto-reassign-on-tag-change rules (FR-035, to pass T030), the FR-006 "tâche prévue aujourd'hui" query logic (used by both the `today` filter and later by `DailyProgress`), and per-task real/remaining time computed via `PomodoroSessionRepository` sums + `RemainingTime`. Task deletion nulls the FK on associated sessions rather than deleting them (`ON DELETE SET NULL` from T005 already handles this at the DB level — verify no JPA cascade contradicts it).
- [ ] T034 [US1] Implement `TaskRequest`/`TaskResponse` DTOs in `backend/src/main/java/com/fragmentsinstants/infrastructure/web/dto/TaskDtos.java` (response includes `estimatedMinutes`, `realMinutes`, `remainingMinutes` per FR-008).
- [ ] T035 [US1] Implement `TaskController` in `backend/src/main/java/com/fragmentsinstants/infrastructure/web/TaskController.java` covering every row of contracts/api-overview.md's Tasks table (depends on T033, T034).
- [ ] T036 [P] [US1] Implement the `TasksApi` Angular service in `src/app/core/services/tasks-api.ts` (CRUD + `filter` query param + the four lifecycle POST endpoints), using the `Task` model from T021.
- [ ] T037 [US1] Implement the task list component in `src/app/features/tasks/task-list/` (`task-list.ts`/`.html`/`.scss`) with the four filter tabs (Toutes/Aujourd'hui/Importantes/Terminées).
- [ ] T038 [US1] Implement the task form component in `src/app/features/tasks/task-form/` (`task-form.ts`/`.html`/`.scss`) for create/edit: title (required), description, tags (placeholder multi-select until T065 wires the live list), `estimatedMinutes`, priority, `dueDate`.
- [ ] T039 [US1] Wire per-task actions (complete/reopen/pause/resume/delete) into the task list, rendering overdue-but-active tasks with no alert styling or red accent (Bienveillance, FR-006) — `src/app/features/tasks/task-list/task-item/` (`task-item.ts`/`.html`/`.scss`).
- [ ] T040 [P] [US1] Angular component test for the task list's filter behavior (critical screen) in `src/app/features/tasks/task-list/task-list.spec.ts` (Vitest + `TestBed`), covering the four filters and the "completed hidden by default" rule.

**Checkpoint**: User Story 1 is fully functional and independently testable (quickstart.md scenario 1).

---

## Phase 4: User Story 8 - Paramètres (Priority: P1)

**Goal**: Configure Pomodoro durations, the long-break threshold, priority coefficients, and sound notifications — fully independent of tasks/tags/sessions, but consumed by US2's durations and (indirectly, via `TaskWeight`) by US3's progression.

**Independent Test**: Change each setting and verify Pomodoro behavior / weight calculations pick up the new values (quickstart.md scenario 8).

### Tests for User Story 8 ⚠️

- [ ] T041 [P] [US8] Contract tests for `GET`/`PUT /settings` in `backend/src/test/java/com/fragmentsinstants/infrastructure/web/SettingsControllerTest.java`: `GET` returns the FR-009/FR-026 defaults on first read; `PUT` with a partial body updates only the given fields (FR-025); `PUT` with a non-positive duration or coefficient is rejected as `422 REGLE_METIER_VIOLEE`.

### Implementation for User Story 8

- [ ] T042 [US8] Implement `SettingsService` in `backend/src/main/java/com/fragmentsinstants/application/SettingsService.java` (get-or-create the single row, partial update with `> 0` validation raising `BusinessRuleException`).
- [ ] T043 [US8] Implement `SettingsDto` + `SettingsController` (`GET`/`PUT /settings`) in `backend/src/main/java/com/fragmentsinstants/infrastructure/web/SettingsController.java`.
- [ ] T044 [P] [US8] Implement the `SettingsApi` Angular service in `src/app/core/services/settings-api.ts`.
- [ ] T045 [US8] Implement the settings screen in `src/app/features/settings/settings-page/` (`settings-page.ts`/`.html`/`.scss`): the three Pomodoro durations, the long-break threshold, the four priority coefficients, and the sound-notifications toggle.

**Checkpoint**: Settings are configurable end-to-end (quickstart.md scenario 8).

---

## Phase 5: User Story 2 - Faire un Pomodoro (Priority: P1)

**Goal**: A timestamp-based, sleep-resistant Pomodoro timer (Focus/pause courte/pause longue), submitting only finished/interrupted sessions to the API, which computes the real duration server-side.

**Independent Test**: Start a default Focus session, pause/resume it, reset it, and step manually to the next mode — verify the recorded real time independently of any associated task (quickstart.md scenario 2).

### Tests for User Story 2 ⚠️

- [ ] T046 [P] [US2] Domain unit tests for `FocusTimeTotal` in `backend/src/test/java/com/fragmentsinstants/domain/FocusTimeTotalTest.java`: sums all `FOCUS` sessions regardless of `completionStatus`; excludes any session with `actualDurationSeconds < 60`.
- [ ] T047 [P] [US2] Domain unit tests for `PomodoroDailyCount` in `backend/src/test/java/com/fragmentsinstants/domain/PomodoroDailyCountTest.java`: counts only `FOCUS` + `TERMINEE` sessions; excludes `INTERROMPUE` and non-`FOCUS` modes (FR-033).
- [ ] T048 [P] [US2] Domain unit tests for `PomodorosBeforeLongBreak` in `backend/src/test/java/com/fragmentsinstants/domain/PomodorosBeforeLongBreakTest.java`, covering every boundary from data-model.md's algorithm: `count = 0` → `remaining = threshold`, `nextBreakType = COURTE`; `count` not a multiple of `threshold` → `remaining = threshold - (count % threshold)`, `COURTE`; `count` a positive exact multiple of `threshold` → `remaining = 0`, `nextBreakType = LONGUE`; `threshold = 1` edge case.
- [ ] T049 [P] [US2] Contract tests for `POST /pomodoro-sessions` in `backend/src/test/java/com/fragmentsinstants/infrastructure/web/PomodoroSessionControllerTest.java`: given `startedAt`/`endedAt` 10 minutes apart, the stored `actualDurationSeconds` is server-computed as `600`, ignoring any `actualDurationSeconds` field present in the request body; `taskId` is optional; `completionStatus` is required.

### Implementation for User Story 2

- [ ] T050 [US2] Implement the `FocusTimeTotal` pure formula in `backend/src/main/java/com/fragmentsinstants/domain/FocusTimeTotal.java` to pass T046.
- [ ] T051 [US2] Implement the `PomodoroDailyCount` pure formula in `backend/src/main/java/com/fragmentsinstants/domain/PomodoroDailyCount.java` to pass T047.
- [ ] T052 [US2] Implement the `PomodorosBeforeLongBreak` pure formula (returns `remaining` + `nextBreakType`) in `backend/src/main/java/com/fragmentsinstants/domain/PomodorosBeforeLongBreak.java` to pass T048.
- [ ] T053 [US2] Implement `PomodoroSessionService` in `backend/src/main/java/com/fragmentsinstants/application/PomodoroSessionService.java`: persists a submitted session, always computing `actualDurationSeconds = endedAt - startedAt` server-side (FR-012) and deriving the session's calendar day from `startedAt` in `Europe/Paris` (plan.md Timezone).
- [ ] T054 [US2] Implement `PomodoroSessionRequest`/`Response` DTOs (no `actualDurationSeconds` accepted in the request) + `PomodoroSessionController` (`POST /pomodoro-sessions`, `GET /pomodoro-sessions?date=`) in `backend/src/main/java/com/fragmentsinstants/infrastructure/web/PomodoroSessionController.java`.
- [ ] T055 [P] [US2] Implement the `PomodoroApi` Angular service in `src/app/core/services/pomodoro-api.ts` (submit-session only; no duration field sent).
- [ ] T056 [US2] Implement `PomodoroTimerService` in `src/app/features/pomodoro/pomodoro-timer.service.ts`: timestamp-based state machine (start/pause/resume/reset/skip) computing elapsed time from `Date.now()` deltas on each tick rather than an incremental counter, so it survives OS sleep/wake (plan.md Constraints); submits to `PomodoroApi` only on completion or reset/interruption.
- [ ] T057 [US2] Implement the Pomodoro screen in `src/app/features/pomodoro/pomodoro-page/` (`pomodoro-page.ts`/`.html`/`.scss`): current mode, countdown, controls, optional task association (uses `TasksApi` from T036).
- [ ] T058 [P] [US2] Angular test for `PomodoroTimerService` (critical screen) in `src/app/features/pomodoro/pomodoro-timer.service.spec.ts`: pause/resume preserves elapsed time; reset still reports the elapsed time before reset; mocked timestamp jumps (simulating sleep/wake) still produce the correct elapsed duration.

**Checkpoint**: Pomodoro is fully functional and independently testable (quickstart.md scenario 2).

---

## Phase 6: User Story 4 - Organiser par tags (Priority: P1)

**Goal**: Tag CRUD with the `Autre` protection rule and the delete-cascade reassignment, plus a live tag selector in the task form.

**Independent Test**: Assign default and custom tags to a task; create a custom tag; attempt to delete/rename `Autre` (quickstart.md scenario 4).

### Tests for User Story 4 ⚠️

- [ ] T059 [P] [US4] Contract tests for tag CRUD + protection in `backend/src/test/java/com/fragmentsinstants/infrastructure/web/TagControllerTest.java`: `POST /tags` creates a custom tag (FR-015); `PUT /tags/{id}` on `Autre` returns `422 REGLE_METIER_VIOLEE`; `DELETE /tags/{id}` on `Autre` returns `422 REGLE_METIER_VIOLEE` (FR-036).
- [ ] T060 [P] [US4] Contract test for the delete-cascade in `backend/src/test/java/com/fragmentsinstants/infrastructure/web/TagDeletionCascadeTest.java`: deleting a custom tag that is a task's *only* tag reassigns that task to `Autre` before the tag row is removed; deleting a tag that is one of several on a task only removes that association (FR-036). Distinct from T030's task-update-triggered `Autre` rule.

### Implementation for User Story 4

- [ ] T061 [US4] Implement `TagService` in `backend/src/main/java/com/fragmentsinstants/application/TagService.java`: create, rename (throws `BusinessRuleException` if `isProtected`), delete (throws `BusinessRuleException` if `isProtected`; otherwise reassigns orphaned tasks to `Autre` before deleting, per T060).
- [ ] T062 [US4] Implement `TagRequest`/`TagResponse` DTOs + `TagController` in `backend/src/main/java/com/fragmentsinstants/infrastructure/web/TagController.java`.
- [ ] T063 [P] [US4] Implement the `TagsApi` Angular service in `src/app/core/services/tags-api.ts`.
- [ ] T064 [US4] Implement the tag management screen in `src/app/features/tags/tags-page/` (`tags-page.ts`/`.html`/`.scss`): list, create, rename, delete, with `Autre`'s rename/delete controls disabled and labeled as protected.
- [ ] T065 [US4] Wire the live tag list from `TagsApi` into the US1 task form's tag selector, replacing the T038 placeholder, in `src/app/features/tasks/task-form/task-form.ts`.

**Checkpoint**: Tag CRUD, protection, and cascade reassignment are functional (quickstart.md scenario 4).

---

## Phase 7: User Story 5 - Suivre humeur et fatigue (Priority: P1)

**Goal**: One upsert-by-day entry of three 1–7 scores, with extremes-explanation text.

**Independent Test**: Submit today's three scores, then resubmit with a changed value and confirm no duplicate entry is created (quickstart.md scenario 5).

### Tests for User Story 5 ⚠️

- [ ] T066 [P] [US5] Domain unit tests for `WeeklyMoodFatigueAverage` in `backend/src/test/java/com/fragmentsinstants/domain/WeeklyMoodFatigueAverageTest.java`: averages each of the 3 scales only over days present in the current week; an empty week returns an explicit "no data" marker, never `0` (FR-030, constitution V).
- [ ] T067 [P] [US5] Contract tests for `GET`/`PUT /mood-entries/today` in `backend/src/test/java/com/fragmentsinstants/infrastructure/web/MoodEntryControllerTest.java`: first `PUT` creates the day's entry; a second `PUT` the same day updates it in place (no duplicate row, FR-019); any score outside 1–7 is rejected as `422 REGLE_METIER_VIOLEE`.

### Implementation for User Story 5

- [ ] T068 [US5] Implement the `WeeklyMoodFatigueAverage` pure formula in `backend/src/main/java/com/fragmentsinstants/domain/WeeklyMoodFatigueAverage.java` to pass T066.
- [ ] T069 [US5] Implement `MoodEntryService` in `backend/src/main/java/com/fragmentsinstants/application/MoodEntryService.java`: upsert-by-date (FR-019), range fetch for trend charts.
- [ ] T070 [US5] Implement `MoodEntryRequest`/`Response` DTOs (including the static extremes-explanation text, FR-018) + `MoodEntryController` in `backend/src/main/java/com/fragmentsinstants/infrastructure/web/MoodEntryController.java`.
- [ ] T071 [P] [US5] Implement the `MoodApi` Angular service in `src/app/core/services/mood-api.ts`.
- [ ] T072 [US5] Implement the daily mood/fatigue entry screen in `src/app/features/mood/mood-entry-page/` (`mood-entry-page.ts`/`.html`/`.scss`): three 1–7 inputs, the extremes-explanation text per scale, upsert on submit.

**Checkpoint**: Mood/fatigue entry is functional (quickstart.md scenario 5).

---

## Phase 8: User Story 3 - Voir ma journée (Priority: P1)

**Goal**: The daily dashboard aggregating current Pomodoro, today's tasks, weighted progress, focus time, completed-today/Pomodoro counts, the server-computed remaining-Pomodoros/next-break-type, and the weekly mood/fatigue cards. Depends on US1, US2, US5, US8 already existing.

**Independent Test**: With tasks and sessions already created for today, verify every dashboard aggregate without navigating elsewhere (quickstart.md scenario 3).

### Tests for User Story 3 ⚠️

- [ ] T073 [P] [US3] Domain unit tests for `DailyProgress` in `backend/src/test/java/com/fragmentsinstants/domain/DailyProgressTest.java`: `(sum weights completed today ÷ sum weights planned today) × 100`; denominator `0` → `0%`, never an error (FR-028).
- [ ] T074 [P] [US3] Contract tests for `GET /dashboard/today` in `backend/src/test/java/com/fragmentsinstants/infrastructure/web/DashboardControllerTest.java`: weighted progress %, cumulative focus time, completed-today count, Pomodoro-completed-today count, `pomodorosRemainingBeforeLongBreak` + `nextBreakType` (FR-038), `weeklyMoodFatigueAverages` with `null` per scale when the week is empty (FR-039).

### Implementation for User Story 3

- [ ] T075 [US3] Implement the `DailyProgress` pure formula in `backend/src/main/java/com/fragmentsinstants/domain/DailyProgress.java` to pass T073.
- [ ] T076 [US3] Implement `DashboardService` in `backend/src/main/java/com/fragmentsinstants/application/DashboardService.java`, composing `TaskService` (FR-006 "prévue aujourd'hui" set + `TaskWeight`), `PomodoroSessionService`/`FocusTimeTotal`/`PomodoroDailyCount`/`PomodorosBeforeLongBreak` (reading the threshold from `SettingsService`), and `MoodEntryService`/`WeeklyMoodFatigueAverage` into one aggregate — nothing here is persisted (constitution III).
- [ ] T077 [US3] Implement `DashboardResponse` DTO + `DashboardController` (`GET /dashboard/today`) in `backend/src/main/java/com/fragmentsinstants/infrastructure/web/DashboardController.java`.
- [ ] T078 [P] [US3] Implement the `DashboardApi` Angular service in `src/app/core/services/dashboard-api.ts`.
- [ ] T079 [US3] Implement the dashboard screen in `src/app/features/dashboard/dashboard-page/` (`dashboard-page.ts`/`.html`/`.scss`): current Pomodoro state, today's tasks, weighted progress, focus time, completed/Pomodoro counts, remaining-before-long-break + next break type, and the three weekly mood/fatigue cards (each showing "aucune donnée" when `null`).
- [ ] T080 [P] [US3] Angular component test for the dashboard (critical screen) in `src/app/features/dashboard/dashboard-page/dashboard-page.spec.ts`: renders `0%` progress without error when no tasks are planned; renders "aucune donnée" cards when the week has no mood entries.

**Checkpoint**: The dashboard is functional end-to-end (quickstart.md scenario 3).

---

## Phase 9: User Story 6 - Visualiser (Priority: P1)

**Goal**: Tag donut (today/week), weekly focus histogram, mood/fatigue trend (3 toggleable series), completed-tasks-per-day bars. Depends on US1, US2, US4 (tags), US5 already existing.

**Independent Test**: With multi-day data already generated, verify each chart independently of where the underlying data was entered (quickstart.md scenario 6).

### Tests for User Story 6 ⚠️

- [ ] T081 [P] [US6] Domain unit tests for `TagPercentageBreakdown` in `backend/src/test/java/com/fragmentsinstants/domain/TagPercentageBreakdownTest.java`: percentages from a pre-aggregated tag→time map; total `0` → every tag `0%` (FR-031).
- [ ] T082 [P] [US6] Contract tests for the four stats endpoints in `backend/src/test/java/com/fragmentsinstants/infrastructure/web/StatsControllerTest.java`: `/stats/tag-breakdown` sums to 100% once ≥1 minute is recorded (SC-008) and attributes a taskless/orphaned-task Focus session's time entirely to `Autre` (FR-031); `/stats/weekly-focus` returns all 7 days Monday→Sunday including 0-value days (FR-021); `/stats/mood-fatigue-trend` returns 3 independent series with no entry substituted as 0 on missing days; `/stats/completed-tasks-by-day` counts by `completedAt` date (FR-023).

### Implementation for User Story 6

- [ ] T083 [US6] Implement the `TagPercentageBreakdown` pure formula in `backend/src/main/java/com/fragmentsinstants/domain/TagPercentageBreakdown.java` to pass T081.
- [ ] T084 [US6] Implement `StatsService` in `backend/src/main/java/com/fragmentsinstants/application/StatsService.java`: the session-by-session tag allocation described in data-model.md's TaskTag section (each eligible `FOCUS` session split across its task's *current* tags, or attributed wholly to `Autre` if `taskId` is null) feeding `TagPercentageBreakdown`; the Monday→Sunday focus histogram via `FocusTimeTotal` per day; the mood/fatigue trend via `MoodEntryRepository` range queries; completed-tasks-by-day via `TaskRepository`.
- [ ] T085 [US6] Implement the stats DTOs + `StatsController` (the four `GET` endpoints from contracts/api-overview.md) in `backend/src/main/java/com/fragmentsinstants/infrastructure/web/StatsController.java`.
- [ ] T086 [P] [US6] Implement the `StatsApi` Angular service in `src/app/core/services/stats-api.ts`.
- [ ] T087 [US6] Implement the tag donut chart component (ng2-charts, today/week toggle) in `src/app/features/charts/tag-donut/` (`tag-donut.ts`/`.html`/`.scss`).
- [ ] T088 [P] [US6] Implement the weekly focus histogram component (Mon→Sun, including 0-value days) in `src/app/features/charts/weekly-focus-chart/`.
- [ ] T089 [P] [US6] Implement the mood/fatigue trend chart component with 3 independently toggleable series in `src/app/features/charts/mood-fatigue-trend-chart/`.
- [ ] T090 [P] [US6] Implement the completed-tasks-by-day bar chart component in `src/app/features/charts/completed-tasks-chart/`.
- [ ] T091 [US6] Implement the visualizations screen assembling the four chart components in `src/app/features/charts/charts-page/` (`charts-page.ts`/`.html`/`.scss`).

**Checkpoint**: All visualizations are functional (quickstart.md scenario 6).

---

## Phase 10: User Story 7 - Ne rien perdre (Priority: P1)

**Goal**: Confirm the persistence built into Foundational (real SQLite file, Flyway-managed schema, no in-memory fallback) actually survives a full restart, now that US1–US6 have produced real data of every kind.

**Independent Test**: Create data via every other story, fully close both processes, reopen them, and verify nothing was lost (quickstart.md scenario 7).

- [ ] T092 [US7] Add/confirm a `backend/.gitignore` entry excluding the runtime database file (`data/*.db`, `data/*.db-shm`, `data/*.db-wal`) so it is never committed.
- [ ] T093 [US7] Add `backend/src/test/java/com/fragmentsinstants/PersistenceAcrossRestartTest.java`: insert one row of each kind (task, pomodoro session, tag, mood entry, non-default settings) via the repositories, close and recreate the Spring context against the same on-disk SQLite file (not `@DirtiesContext` with an in-memory DB), and assert every row is still readable — the automated equivalent of quickstart.md scenario 7.

**Checkpoint**: quickstart.md scenario 7 passes.

---

## Phase 11: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that span multiple user stories.

- [ ] T094 [P] Audit the T023 palette tokens for WCAG AA text/background contrast across every screen built in Phases 3–9, adjusting derived tokens in `src/styles.scss` if any combination fails (constitution VI).
- [ ] T095 [P] Run the full backend suite (`backend/mvnw test`) and fix any failing/flaky test surfaced across Phases 1–10.
- [ ] T096 [P] Run the full frontend suite (`ng test` at the repository root) and fix any failing/flaky test surfaced across Phases 1–10.
- [ ] T097 Execute the complete [quickstart.md](./quickstart.md) validation pass (all 8 scenarios, backend + frontend running together) and fix any gap found.
- [ ] T098 [P] Compare the live OpenAPI document at `/v3/api-docs` against [contracts/api-overview.md](./contracts/api-overview.md) and reconcile any drift (either doc or code).
- [ ] T099 [P] Review all user-facing copy introduced in Phases 3–9 (task list, dashboard, error messages surfaced from `REGLE_METIER_VIOLEE`, mood extremes text) for Bienveillance compliance — no red-as-alarm, no punitive/blaming language (constitution I).

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately. T001 (backend spike) gates every later *backend* task; T002–T004 (frontend) are independent of T001 and of each other.
- **Foundational (Phase 2)**: Depends on Setup (specifically T001's confirmed id/stack strategy) — BLOCKS all user stories.
- **User Stories (Phase 3–10)**: All depend on Foundational completion. Among themselves:
  - **US1 (Phase 3)**: No dependency on other stories.
  - **US8 (Phase 4)**: No dependency on other stories.
  - **US2 (Phase 5)**: Reads Pomodoro durations from US8's `SettingsService`; can associate sessions with tasks from US1 (optional — `taskId` nullable).
  - **US4 (Phase 6)**: Its delete-cascade (T060) needs `TaskRepository`/`TaskService` from US1; its task-form wiring (T065) edits a file US1 created.
  - **US5 (Phase 7)**: No dependency on other stories.
  - **US3 (Phase 8)**: Aggregates US1 (`TaskWeight`/"prévue aujourd'hui"), US2 (`FocusTimeTotal`/`PomodoroDailyCount`/`PomodorosBeforeLongBreak`), US5 (`WeeklyMoodFatigueAverage`), and US8 (threshold setting) — must come after all four.
  - **US6 (Phase 9)**: Aggregates US1/US2/US4 (tag-based allocation) and US5 (trend) — must come after all three (US4 in particular, for the tag-allocation semantics to be meaningful).
  - **US7 (Phase 10)**: Verification-only; most meaningful after US1–US6 have produced data of every kind, though its own tasks (T092–T093) have no hard code dependency beyond Foundational.
- **Polish (Phase 11)**: Depends on all preceding phases.

### Within Each User Story

- Tests (marked ⚠️) are written first and must fail before their matching implementation task.
- Domain formulas before the application-layer service that calls them.
- Service before controller/DTOs.
- Backend endpoint before the frontend API service that calls it.
- Frontend API service before the screen/component that uses it.

### Parallel Opportunities

- Setup: T002, T003, T004 in parallel (T001 is backend-only and independent of these, but is treated as the phase's anchor task).
- Foundational: T008–T012 (entities) in parallel; T013–T017 (repositories) in parallel once their entity exists; T020, T021, T023 in parallel.
- Once Foundational is done, **US1, US8, and US5 have no dependencies on each other** and can be staffed in parallel; US2 can start as soon as US8 is far enough along for `SettingsService` to exist; US4 can start as soon as US1's `TaskService`/`TaskRepository` exist; US3 and US6 are the last to start since they aggregate the others.
- Within any story, all `[P]`-marked test tasks run in parallel with each other; all `[P]`-marked frontend-API-service tasks run in parallel with unrelated backend implementation tasks in the same story.

---

## Parallel Example: User Story 1

```bash
# Launch US1's test tasks together:
Task: "Contract tests for task creation & retrieval in backend/src/test/java/com/fragmentsinstants/infrastructure/web/TaskControllerTest.java"
Task: "Contract tests for the three named filters in backend/src/test/java/com/fragmentsinstants/infrastructure/web/TaskFilterControllerTest.java"
Task: "Contract tests for lifecycle transitions in backend/src/test/java/com/fragmentsinstants/infrastructure/web/TaskLifecycleControllerTest.java"
Task: "Domain unit tests for TaskWeight in backend/src/test/java/com/fragmentsinstants/domain/TaskWeightTest.java"
Task: "Domain unit tests for RemainingTime in backend/src/test/java/com/fragmentsinstants/domain/RemainingTimeTest.java"
Task: "Contract tests for the Autre auto-remove/auto-reassign rule triggered by task updates in backend/src/test/java/com/fragmentsinstants/infrastructure/web/TaskTagAutoAssignControllerTest.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 (Setup) and Phase 2 (Foundational) — CRITICAL, blocks everything.
2. Complete Phase 3 (US1 — task management).
3. **STOP and VALIDATE**: run quickstart.md scenario 1 independently.
4. This is the smallest usable slice: a local to-do list with filters and lifecycle, backed by the real persistent stack.

### Incremental Delivery

1. Setup + Foundational → foundation ready.
2. US1 → validate → MVP.
3. US8 → validate (independent; unblocks realistic US2 testing).
4. US2 → validate (now durations are configurable).
5. US4 → validate (tag CRUD + protection; task form gains live tags).
6. US5 → validate (mood/fatigue independent of the above).
7. US3 → validate (dashboard aggregates US1/US2/US5/US8 — first point where "the whole app" is visible in one screen).
8. US6 → validate (visualizations aggregate US1/US2/US4/US5).
9. US7 → validate (restart persistence, now that every data type exists).
10. Polish (Phase 11).

Each step adds value without breaking the previous ones, since Phase 2's schema and Phase 1's spike already fixed the storage/id strategy before any story-specific code was written.

---

## Notes

- `[P]` tasks touch different files with no unfinished-task dependency.
- `[Story]` labels map every Phase 3–10 task to its user story for traceability; Setup, Foundational, and Polish tasks carry no story label by design.
- The T001 spike's outcome (SQLite+`Long` ids vs. the UUID or H2 fallback from data-model.md) is a hard precondition for T005 onward — do not start Foundational's entities/migrations before T001 is resolved.
- Every domain formula task pair (test task → implementation task) follows the TDD flow explicitly requested for this feature: the test must fail first.
- T030 (task-update-triggered `Autre` rule) was added during `/speckit-analyze` remediation (finding C1) to close a coverage gap: T025 only covered the creation-time default-assignment case, and T060 covers a different trigger (tag deletion), leaving FR-035's update-triggered add/remove behavior — and US4 Acceptance Scenarios 2 & 4 — otherwise untested.
- Commit after each task or logical group; stop at any Checkpoint to validate a story independently before moving on.
