# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Comment travailler avec moi

J'écris cette application moi-même, pour apprendre. **N'écris jamais de code
d'implémentation à ma place**, même si je décris précisément ce que je veux.

Ce que je te demande à la place :
- expliquer un concept, une API, un message d'erreur ;
- me dire pourquoi un test échoue, sans me donner le code corrigé ;
- relire du code que j'ai déjà écrit (`/code-review`, sans `--fix`) ;
- lancer des commandes (`mvn`, `ng`, `git`) et me montrer la sortie.

Si je te demande d'écrire du code, rappelle-moi cette règle et propose-moi
plutôt un indice ou une piste. N'utilise jamais `/speckit-implement`.

Mon niveau : à l'aise en Java et Spring Boot (mais sans pratique depuis 2 ans,
donc signale-moi ce qui a changé), débutant en Angular et TypeScript.

## Project status

This repo is a [GitHub Spec Kit](https://github.com/github/spec-kit) project. Planning is
complete; implementation is done **manually by the user, task by task** — not by the agent. The
**authoritative design docs live under `pilotage/specs/001-taches-pomodoro-humeur/`**, not in this file —
read them before answering questions about the design:

- `spec.md` — feature spec: user stories (US1-US8, all Priority P1), functional requirements
  (FR-001..FR-040), success criteria, clarifications, edge cases.
- `plan.md` — architecture decisions, tech stack with verified versions, timezone (`Europe/Paris`),
  constitution gate check.
- `data-model.md` — entities, the 8 pure domain formulas (numbered table), id strategy decision.
- `contracts/api-overview.md` — REST endpoint contract (human-readable; springdoc generates the
  live OpenAPI spec at `/v3/api-docs` once the backend runs).
- `research.md` — dependency version research and rationale.
- `quickstart.md` — manual validation scenarios, one per user story.
- `tasks.md` — the dependency-ordered, numbered (T001-T099) execution plan. **This is the source
  of truth for "what to build next."** The user works through it manually, in order; don't
  reorder phases without checking the Dependencies section first.

**Where we stopped (2026-11-20)**: all JPA entities (T008–T012) and all repositories
(T013–T017) are done, each repository with a `@DataJpaTest` test; the full backend suite passes.
`TaskRepository` keeps a few extra query methods commented out (not required by T013, untested);
revive them only when a service needs one. `SettingsEntity` has no all-args constructor on purpose
(single row seeded by `V3`, never created by the app). T018 is done: `application.yml` sets
`open-in-view: false`, `flyway.locations`, springdoc paths, and a custom property
`fragments-instants.cors.origine-frontend` (Spring has no standard MVC CORS property) that T020
`CorsConfig` must read instead of hardcoding the origin; `backend/data/.gitkeep` keeps the folder.
T019 is done: `application/BusinessRuleException` (unchecked, message only) and
`infrastructure/web/GlobalExceptionHandler` mapping it to 422 with the `BusinessRuleErrorResponse`
record; it uses `HttpStatus.UNPROCESSABLE_CONTENT` (`UNPROCESSABLE_ENTITY` is deprecated in Spring 7)
and does not extend `ResponseEntityExceptionHandler`, so 400 validation errors keep Spring's default
body. Not exercised by a test yet: check it with the first controller that throws it (tags, T061+).
**Resume at T020 (`CorsConfig`)**.

Open points to revisit (not blockers):
- `PomodoroSessionEntity` (T009): FR-012 says `actualDurationSeconds` must be server-computed with
  no public setter reachable from outside the entity/service layer. Not re-reviewed after the
  `Long`→`Integer` and `@ManyToOne` fixes; check it before the session service.
- Validation ranges (`@Min`/`@Max`/`@Positive`) belong on the request DTOs, not on entities.
- The root `README.md` still describes the Angular CLI commands; move it into `frontend/` or rewrite it.
- Case-insensitive tag uniqueness does not cover accented letters under SQLite: `upper()` and
  `COLLATE NOCASE` only fold A–Z, so `findByNameIgnoreCase("écriture")` does not find `Écriture`
  (documented by `TagRepositoryTest`). Handle it in the tag service (e.g. Java
  `equalsIgnoreCase`) in T061 `TagService`, with a case in T059.
- SQLite stores `NUMERIC(4,2)` as a float; `BigDecimal` stays the Java type for exact calculations.
- Never edit an applied migration, even for whitespace: Flyway checksums the file text and refuses
  to start (`Migration checksum mismatch`), in tests (`target/spike-test.db`) and in the app
  (`backend/data/fragments-instants.db`). Revert the edit (`git restore`), or delete both `.db`
  files (disconnect DBeaver first). Real changes go in a new `V4__…` migration.
- The spike files (`V0__spike.sql`, `SpikeItem`, `SpikeItemRepository`, `SpikeRepositoryTest`) can
  now be deleted, since T005–T017 have landed; the test DB name `spike-test.db` can be renamed then.

**Current state** (as of 2026-11-15, see "Where we stopped" above for later progress; `tasks.md`
checkboxes are the source of truth):

- Done: **T001** (feasibility spike — SQLite + Flyway + Hibernate with `Long`/`IDENTITY` ids
  validated, 9 backend tests pass), **T002** (chart dependencies), **T003** (Angular environment
  files), **T005** (`V1__init_schema.sql`, all tables), **T006** (`V2__seed_default_tags.sql`),
  **T007** (`V3__seed_default_settings.sql`), **T008–T012** (all JPA entities). **T004** is marked
  not applicable (`HttpClient` is provided by default since Angular 21).
- Next: the repositories (T013–T017), then `application.yml` (T018).
- The `backend/` Spring Boot module exists. It still contains the throwaway spike files
  (`V0__spike.sql`, `SpikeItem`, `SpikeItemRepository`, `SpikeRepositoryTest`), to be deleted once
  T005–T017 have landed with the real schema.
- No Angular feature code yet beyond the `ng new` scaffold and the environment files.
- Differences from `tasks.md` T001 text: `pom.xml` uses `spring-boot-starter-webmvc` (Boot 4
  naming) and `flyway-database-nc-sqlite` 12.4.0 (not 13.0.0); springdoc is commented out, so
  Swagger UI is not available yet.

If you're picking up mid-implementation, check `tasks.md` checkboxes and `git log`/`git status`
against the plan before assuming what exists.

## Architecture (per plan.md — target state)

The repository is split into three folders at its root:

- `frontend/` — the Angular app
- `backend/` — the Spring Boot module
- `pilotage/` — project steering: `specs/`, `.specify/` (Spec Kit), `.claude/` (skills) and this
  `CLAUDE.md`. Spec Kit scripts locate the project root by searching upward for `.specify/`, so
  `pilotage/` is the Spec Kit root.

Two independent local processes, no shared build:

- **Frontend**: Angular 22 SPA in `frontend/`. Talks to the backend over HTTP at
  `http://localhost:8080/api`.
- **Backend**: Spring Boot 4.1.1 (Java 21) in a new `backend/` Maven module, layered as
  `domain/` (pure formula classes, zero Spring dependency) → `application/` (use-case services) →
  `infrastructure/` (JPA entities/repositories + REST controllers). Persists to a local SQLite
  file (`backend/data/fragments-instants.db`) via Flyway migrations. No auth, CORS restricted to
  `http://localhost:4200`.

Key architectural rules from the constitution (`.specify/memory/constitution.md`) that constrain
every implementation decision:

- **Données brutes d'abord**: only raw rows are ever persisted (tasks, sessions, tags, mood
  entries, settings). Every statistic (progress %, tag %, focus time, weekly averages) is computed
  on read — never stored as a column or cached total.
- **Exactitude des calculs**: the 8 domain formulas (data-model.md's numbered table — `TaskWeight`,
  `DailyProgress`, `RemainingTime`, `TagPercentageBreakdown`, `WeeklyMoodFatigueAverage`,
  `PomodoroDailyCount`, `PomodorosBeforeLongBreak`, `FocusTimeTotal`) must be pure Java classes
  under `domain/`, no Spring annotations, tests written before implementation (tasks.md pairs each
  formula's test task before its implementation task).
- **Bienveillance**: no punitive/streak-loss logic, no red-as-alarm UI, kind wording even in error
  messages. All business-rule rejections use one single error shape: `422` with
  `{ "code": "REGLE_METIER_VIOLEE", "message": string }` — never a per-rule code.
- **Gestion des données manquantes**: a day with no entry is never treated as `0` in an average; a
  zero denominator (no tasks planned, no time recorded) always renders `0%`, never an error.
- **Direction artistique**: dark, botanical palette — `#DF73FF`, `#B08D57`, `#4B2142`, `#234236`,
  `#F2E8D5` — as CSS custom properties, AA contrast required, no red token.

Pomodoro timing is timestamp-based on the frontend (never an incrementing counter, so it survives
OS sleep/wake); only finished/interrupted sessions are POSTed to the backend, which recomputes the
actual duration server-side from the timestamps — the client-sent duration, if any, is ignored.

## Commands

### Frontend (from `frontend/`)

```bash
npm install          # first time / after pulling dependency changes
ng serve              # dev server at http://localhost:4200
ng build               # production build to dist/
ng test                 # unit tests via Vitest (not Karma/Jasmine — this is Angular 22's default)
```

Run a single test file: `ng test -- src/app/features/tasks/task-list/task-list.spec.ts` (Vitest
CLI args pass through after `--`).

Formatting: Prettier is configured (`.prettierrc`: 100-char width, single quotes, Angular parser
for `*.html`) — run via `npx prettier --write .`.

### Backend (from `backend/`; on Windows use `./mvnw.cmd`)

```bash
./mvnw spring-boot:run       # runs at http://localhost:8080, Flyway migrates on startup
./mvnw test                    # full suite
./mvnw test -Dtest=TaskWeightTest   # single test class
```

Swagger UI (`http://localhost:8080/swagger-ui.html`) is only available once the springdoc
dependency, currently commented out in `pom.xml`, is re-enabled.

### Full-stack validation

Follow `quickstart.md` — it has one runnable scenario per user story, cross-referenced to the
exact acceptance criteria in `spec.md`.

## Working in this repo

- **New feature work**: this repo uses Spec Kit slash commands (`/speckit-specify`,
  `/speckit-clarify`, `/speckit-plan`, `/speckit-tasks`, `/speckit-analyze`) rather than ad hoc
  planning. If asked to add or change something non-trivial, prefer routing through these commands
  so spec/plan/tasks stay in sync. `/speckit-implement` is deliberately excluded — see "Comment
  travailler avec moi".
- **Implementing tasks**: `tasks.md` tasks are self-contained (exact file path, referenced FR/SC,
  and for schema/entity tasks the field constraints are quoted verbatim from `data-model.md`) —
  read the task text itself rather than re-deriving requirements from `spec.md` each time, but
  check `contracts/api-overview.md` for the exact endpoint shape before discussing a controller.
- **IDs**: entities use `Long`/`IDENTITY` auto-increment (not UUID) per `data-model.md`'s
  "Identifier strategy". **Validated by tasks.md's T001 feasibility spike** (SQLite + Flyway +
  Hibernate): `IDENTITY` ids work under the community SQLite dialect, so neither the UUID nor the
  H2 fallback is needed.
- TypeScript is strict (see `tsconfig.json`); Angular components are standalone with signals, no
  NgModules.