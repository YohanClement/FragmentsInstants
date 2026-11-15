# Implementation Plan: Les Fragments d'Instants — V1 (tâches, Pomodoro, journée, humeur/fatigue)

**Branch**: `001-taches-pomodoro-humeur` | **Date**: 2026-09-28 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-taches-pomodoro-humeur/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command; its definition describes the execution workflow.

## Summary

Local, single-user personal productivity app ("Les Fragments d'Instants") combining task
management, a Pomodoro timer, a daily dashboard, tag-based organization, mood/fatigue tracking,
and visualizations — all data local-only, no account. Technical approach: a two-process
client-server architecture running entirely on the user's machine — an Angular 22 SPA (already
scaffolded at the repository root) driving a local-only Pomodoro timer via timestamps, talking
over CORS-enabled REST to a layered Spring Boot 4.1.1 backend (domain/application/infrastructure)
that persists raw tasks, sessions, tags, and mood entries to a local SQLite file via Flyway
migrations, and computes every statistic on read from that raw data — never storing a total.

## Technical Context

**Language/Version**: Java 21 (backend); TypeScript ~6.0.2 strict (frontend, Angular 22.2.0 — already scaffolded)

**Primary Dependencies**: Spring Boot 4.1.1, Spring Data JPA, Flyway (`flyway-core` + `flyway-database-nc-sqlite`), `sqlite-jdbc`, `hibernate-community-dialects` (SQLite dialect), `springdoc-openapi-starter-webmvc-ui` 3.1.1, JUnit 5 + AssertJ (via `spring-boot-starter-test`) — backend. Angular 22 standalone components + signals, RxJS 7.8, `ng2-charts` 10.0.0 + `chart.js` 4.x, Vitest (Angular's default test runner) — frontend. See [research.md](./research.md) for verified versions and rationale.

**Storage**: SQLite file (`backend/data/fragments-instants.db`), schema managed by Flyway migrations; no ORM-generated schema (`ddl-auto=validate`).

**Testing**: JUnit 5 + AssertJ for domain formulas (written before implementation, per requested TDD flow) and for application/use-case logic; Spring `@WebMvcTest`/`@SpringBootTest` + `MockMvc` for controller contract tests; Angular `TestBed` under Vitest for critical screens (dashboard, task list, Pomodoro timer, mood entry).

**Target Platform**: Local desktop use via browser (`http://localhost:4200`) against a locally-running Spring Boot API (`http://localhost:8080`); both processes run on the same machine, no deployment target in V1.

**Project Type**: Web application (two processes: Angular SPA + Spring Boot REST API), both running locally with no remote hosting.

**Performance Goals**: No explicit throughput targets — single local user, local SQLite; interactions (task creation, mood entry) must feel instant (<1s round trip) per SC-001/SC-004, trivially met at this scale.

**Constraints**: Fully offline-capable (Local-first, constitution II); Pomodoro timer must survive OS sleep/wake via timestamp-based elapsed-time computation rather than an incremental counter, sending only raw timestamps to the API (server always computes the actual duration, never trusting a client-sent value — FR-012); no total/statistic ever persisted (constitution III); all 8 domain formulas are pure, tested Java classes (constitution IV, see data-model.md); missing-data handling never substitutes 0 for "no entry" (constitution V); dark, botanical/féerique palette with AA contrast (constitution VI, palette in research/data-model as needed by later UI tasks); every business-rule rejection across the API uses one single error code, `422 REGLE_METIER_VIOLEE` (FR-040), never a different code per rule.

**Timezone**: `Europe/Paris` (Windows timezone ID `Romance Standard Time`, UTC+1/+2 with DST) — the fixed timezone of the workstation running both processes, confirmed via `[System.TimeZoneInfo]::Local` on 2026-09-28. Every "calendar day" notion in this feature (a session's day, "today", the week used for the histogram/trend/tag-breakdown/weekly-average endpoints, the midnight reset of the long-break counter) is computed in this timezone on the backend — never UTC, never the browser's timezone — since front and back always run on the same local machine in V1. If the app is ever run on a different machine, this value must be re-verified rather than assumed.

**Scale/Scope**: 8 user stories (all P1), 5 backend entities (Task, PomodoroSession, Tag, MoodEntry, Settings — see data-model.md for the `Long` vs. UUID identifier decision, pending Task 1's spike), ~8 REST resource groups, ~8 frontend feature areas (tasks, Pomodoro, dashboard, tags, mood, charts, settings, shared/core) — single user, no multi-tenancy.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Check | Result |
|---|---|---|
| I. Bienveillance | No streak-loss logic anywhere in scope (out of scope per spec); UI/error-message wording is a later UI-task concern, flagged in `contracts/api-overview.md`'s error shape; no red-as-alarm requirement introduced. | PASS |
| II. Local-first | SQLite file on disk, no auth, no remote service; both processes run on localhost only. | PASS |
| III. Données brutes d'abord | Data model stores only raw `Task`/`PomodoroSession`/`Tag`/`MoodEntry`/`Settings` rows; every statistic (progress %, tag %, focus time, weekly averages) is computed on read in `domain/`+`application/`, never persisted as a column. | PASS |
| IV. Exactitude des calculs | All 8 formulas (weight, daily progress, remaining time, tag % — computed session by session, weekly mood/fatigue average, daily Pomodoro count, Pomodoros-before-long-break + next break type, focus-time total) are pure classes in `domain/`, no Spring dependency, JUnit-tested first. See data-model.md's numbered table. | PASS |
| V. Gestion des données manquantes | `WeeklyMoodFatigueAverage` excludes days with no entry rather than treating them as 0; `DailyProgress` and `TagPercentageBreakdown` return 0% (not an error) on a zero denominator. | PASS |
| VI. Direction artistique | Out of this plan's backend/data scope; will constrain the SCSS variables and component styling in later implementation tasks (not a gate failure at planning stage). | DEFERRED TO UI TASKS |
| VII. Simplicité | Dropped the unused "variation vs previous value" formula (see research.md) rather than building speculative code; V1 scope only, no V2/V3 features (YouTube, ambiances, gamification, week comparisons, weekly recap) touched. | PASS |

No violations requiring justification. Complexity Tracking table below is empty.

## Project Structure

### Documentation (this feature)

```text
specs/001-taches-pomodoro-humeur/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md         # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
│   └── api-overview.md
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
# Frontend: Angular 22 SPA, already scaffolded at the repository root (package.json, angular.json,
# src/ present before this feature). This feature adds feature folders under src/app/.
src/
├── app/
│   ├── core/                 # HTTP services (TasksApi, PomodoroApi, TagsApi, MoodApi,
│   │                          # DashboardApi, StatsApi, SettingsApi), shared models/DTOs
│   ├── shared/                # Reusable standalone UI components (buttons, cards, form fields)
│   ├── features/
│   │   ├── tasks/             # Task list, filters, task form (US1)
│   │   ├── pomodoro/          # Timer component (timestamp-based, sleep-resistant) (US2)
│   │   ├── dashboard/         # Daily dashboard (US3)
│   │   ├── tags/              # Tag management UI (US4)
│   │   ├── mood/              # Daily mood/fatigue entry (US5)
│   │   ├── charts/            # Donut, histogram, trend lines, bar chart via ng2-charts (US6)
│   │   └── settings/          # Settings screen (US8)
│   └── (existing app.ts, app.routes.ts, app.config.ts — extended, not replaced)
├── styles.scss                # Extended with CSS custom properties for the imposed palette
└── main.ts

# Backend: new Spring Boot 4.1.1 app in its own Maven module, sibling to the Angular root.
backend/
├── pom.xml
├── mvnw / mvnw.cmd
├── src/main/java/com/fragmentsinstants/
│   ├── domain/                # Pure formulas: TaskWeight, DailyProgress, RemainingTime,
│   │                          # TagPercentageBreakdown, WeeklyMoodFatigueAverage,
│   │                          # PomodoroDailyCount, PomodorosBeforeLongBreak, FocusTimeTotal
│   ├── application/           # Use cases/services orchestrating domain + repositories:
│   │                          # TaskService, PomodoroSessionService, TagService,
│   │                          # MoodEntryService, DashboardService, StatsService, SettingsService
│   └── infrastructure/
│       ├── persistence/       # JPA entities, Spring Data repositories
│       └── web/                # REST controllers, request/response DTOs, exception handling
├── src/main/resources/
│   ├── application.yml        # SQLite JDBC URL, Flyway, CORS origin, springdoc config
│   └── db/migration/          # Flyway migrations (schema + default tag seed data)
├── src/test/java/com/fragmentsinstants/
│   ├── domain/                 # JUnit 5 + AssertJ, written before each formula's implementation
│   ├── application/            # Use-case tests
│   └── infrastructure/web/     # MockMvc controller contract tests
└── data/                      # SQLite file at runtime (gitignored)
```

**Structure Decision**: Two sibling toolchains in one repository: the Angular SPA stays at the
repository root, where it was already scaffolded (`package.json`, `angular.json`, `src/`
pre-existed this feature) — relocating it under a generic `frontend/` folder would be unrelated
churn. The new Spring Boot backend is added as an independent Maven module under `backend/`, so
each side keeps its own build tool, dependency manager, and test runner while communicating only
over the documented REST contract (`contracts/api-overview.md`) with CORS restricted to the
local dev origin. This matches the user's explicit two-process client-server request and keeps
the domain/application/infrastructure layering entirely inside `backend/`.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

No violations — table intentionally left empty.

## Implementation order note (binding on `/speckit-tasks`)

**The first task in `tasks.md` MUST be a feasibility spike**, before any other backend task is
built on top of an unverified stack:

1. Stand up one Flyway-migrated table and read/write a row through Spring Data JPA against a real
   SQLite file, using `hibernate-community-dialects`' `SQLiteDialect` (research.md) and the
   `Long`/`IDENTITY` id strategy (data-model.md, "Identifier strategy").
2. Confirm auto-increment `Long` ids work end-to-end (insert, generated-id read-back, a basic
   query) under this dialect/driver combination — this is the explicit "vérifier UUID contre
   identifiants entiers" check: if `IDENTITY` generation misbehaves, switch to UUID ids per the
   documented fallback instead of proceeding on an assumption.
3. If SQLite/Flyway/Hibernate wiring itself is unreliable (not just the id strategy), fall back
   to an embedded H2 database (PostgreSQL-compatibility mode) as documented in data-model.md,
   before any other entity/migration is written.

Every subsequent backend task assumes this spike's outcome (SQLite+`Long` ids, SQLite+UUID ids,
or H2 fallback) as settled fact.
