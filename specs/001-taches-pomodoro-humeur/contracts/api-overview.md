# API Contract: Les Fragments d'Instants — V1

**Date**: 2026-09-28
**Data model**: [../data-model.md](../data-model.md)

The authoritative, always-current machine-readable contract is generated at runtime by
springdoc-openapi from the controller annotations (`GET /v3/api-docs`, browsable at
`/swagger-ui.html` once the backend is running — FR/tooling choice from the technical context).
This document is the human contract that implementation (`tasks.md`) targets; it must stay in
sync with the controllers as they are built.

Base path: `http://localhost:8080/api`. CORS allows `http://localhost:4200` only (local dev
Angular origin). No authentication (Local-first, single user).

All list/aggregate endpoints that divide by a possibly-zero denominator return the safe default
defined in `data-model.md` (0%, or an explicit "no data" marker for mood/fatigue averages) —
**never** an HTTP error for that reason.

## Tasks

| Method | Path | Purpose |
|---|---|---|
| GET | `/tasks?filter=all\|today\|important\|completed` | List tasks per FR-006/FR-007. Each task includes estimated/real/remaining time (FR-008, FR-029). |
| POST | `/tasks` | Create a task (FR-001). Body: title (required), description, tagIds, estimatedMinutes, priority, dueDate. No `tagIds` → server assigns `Autre` (FR-035). |
| GET | `/tasks/{id}` | Fetch one task. |
| PUT | `/tasks/{id}` | Update any attribute (FR-002), including `tagIds` (triggers the Autre auto-add/remove rule, FR-035). |
| DELETE | `/tasks/{id}` | Delete a task (FR-003); associated past sessions keep their recorded time, `taskId` becomes null (spec edge case). |
| POST | `/tasks/{id}/complete` | Mark terminée; sets `completedAt` (FR-004). |
| POST | `/tasks/{id}/reopen` | Cancel completion; clears `completedAt`, status → active, time spent unchanged (FR-032). |
| POST | `/tasks/{id}/pause` | Status → en pause (FR-005). |
| POST | `/tasks/{id}/resume` | Status → active. |

## Pomodoro sessions

| Method | Path | Purpose |
|---|---|---|
| POST | `/pomodoro-sessions` | Submit one finished/interrupted session (FR-012). Body: mode, taskId (optional), startedAt, endedAt, completionStatus. Server computes/validates `actualDurationSeconds = endedAt - startedAt`. Never called while a timer is running — the front end owns the live countdown via timestamps. |
| GET | `/pomodoro-sessions?date=YYYY-MM-DD` | List a day's sessions (used by the dashboard/debugging; day = `startedAt` date, per spec decision). |

## Tags

| Method | Path | Purpose |
|---|---|---|
| GET | `/tags` | List all tags (default + custom), with `isDefault`/`isProtected` flags. |
| POST | `/tags` | Create a custom tag (FR-015). |
| PUT | `/tags/{id}` | Rename a tag; 409/422 if the tag is protected (FR-036). |
| DELETE | `/tags/{id}` | Delete a custom tag; 409/422 if protected; reassigns orphaned tasks to `Autre` (FR-036). |

## Mood / fatigue

| Method | Path | Purpose |
|---|---|---|
| GET | `/mood-entries/today` | Fetch today's entry if it exists (FR-017), plus static extremes-explanation text (FR-018). |
| PUT | `/mood-entries/today` | Upsert today's three scores (FR-019) — create if absent, overwrite in place if present. |
| GET | `/mood-entries?from=&to=` | Range fetch for the trend charts (FR-022); days without an entry are simply absent from the response (never a 0-value entry — FR-030). |

## Dashboard

| Method | Path | Purpose |
|---|---|---|
| GET | `/dashboard/today` | Aggregate for FR-013: today's tasks, weighted progress % (FR-028), cumulative focus time (FocusTimeTotal), completed-today count, Pomodoros-completed-today count (PomodoroDailyCount). Computed on read from raw sessions/tasks — nothing here is stored. |

## Statistics / visualizations

| Method | Path | Purpose |
|---|---|---|
| GET | `/stats/tag-breakdown?period=today\|week` | Donut data: percentage per tag (FR-020, FR-031), always summing to 100% once ≥1 minute is recorded (SC-008). |
| GET | `/stats/weekly-focus` | Monday→Sunday focus-time histogram for the current week, including 0-value days (FR-021). |
| GET | `/stats/mood-fatigue-trend?from=&to=` | Three independent series (mood, physical fatigue, mental fatigue) for the requested range (FR-022); front end handles per-series show/hide. |
| GET | `/stats/completed-tasks-by-day?from=&to=` | Count of tasks completed per day (FR-023), keyed by `completedAt` date. |

## Settings

| Method | Path | Purpose |
|---|---|---|
| GET | `/settings` | Fetch the single settings row (creates the default row on first read if absent). |
| PUT | `/settings` | Update any subset of settings fields (FR-025); subsequent Pomodoros/calculations use the new values immediately (FR-026, Acceptance Scenarios of US8). |

## Error shape

All validation/business-rule rejections (e.g., renaming `Autre`, an out-of-range mood score)
return a `400`/`409` with a body of `{ "error": string, "message": string }`. Bienveillance
(constitution I) applies to `message` wording even in error paths — no blaming language.
