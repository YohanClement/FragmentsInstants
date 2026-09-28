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
**authoritative design docs live under `specs/001-taches-pomodoro-humeur/`**, not in this file —
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

**Current state**: the Angular app at the repo root is still the unmodified `ng new` scaffold (no
feature code yet). The `backend/` Spring Boot module does not exist yet — `tasks.md` T001 creates
it. If you're picking up mid-implementation, check `tasks.md` checkboxes and `git log`/`git status`
against the plan before assuming what exists.

## Architecture (per plan.md — target state)

Two independent local processes, no shared build:

- **Frontend**: Angular 22 SPA at the **repository root** (not under `frontend/` — it was already
  scaffolded there before this feature). Talks to the backend over HTTP at
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

### Frontend (repo root — works today)

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

### Backend (once `backend/` exists, from `backend/`)

```bash
./mvnw spring-boot:run       # runs at http://localhost:8080, Flyway migrates on startup
./mvnw test                    # full suite
./mvnw test -Dtest=TaskWeightTest   # single test class
```

Swagger UI: `http://localhost:8080/swagger-ui.html`.

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
  "Identifier strategy". **This decision is still pending validation by tasks.md's T001
  feasibility spike** (SQLite + Flyway + Hibernate), which also documents the H2 fallback
  conditions. Don't treat the id strategy or the SQLite choice as settled until T001 is done and
  its outcome recorded.
- TypeScript is strict (see `tsconfig.json`); Angular components are standalone with signals, no
  NgModules.