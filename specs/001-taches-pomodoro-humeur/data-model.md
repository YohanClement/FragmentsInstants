# Phase 1 Data Model: Les Fragments d'Instants — V1

**Date**: 2026-09-28
**Feature**: [spec.md](./spec.md) | **Research**: [research.md](./research.md)

All entities are persisted via Spring Data JPA against the local SQLite file. Per the
Données brutes d'abord constitution principle, only raw entities below are stored — no
aggregate/statistic is ever persisted as a column or row; every statistic in `contracts/` is
computed on read from these tables.

## Task

Represents a to-do item.

| Field | Type | Rules |
|---|---|---|
| `id` | UUID (PK) | Generated. |
| `title` | String | Required, non-blank. |
| `description` | String | Optional. |
| `estimatedMinutes` | Integer | Optional; if absent, contributes weight 0 (FR-027, edge case). If present, >= 0. |
| `priority` | Enum: `BASSE, NORMALE, HAUTE, CRITIQUE` | Required, default `NORMALE`. |
| `dueDate` | LocalDate | Optional. |
| `status` | Enum: `ACTIVE, EN_PAUSE, TERMINEE` | Required, default `ACTIVE`. |
| `completedAt` | Instant | Set when status becomes `TERMINEE`; cleared (`null`) when reopened (FR-032). |
| `createdAt` / `updatedAt` | Instant | Managed automatically. |
| `tags` | Many-to-many → Tag | Always at least one row (FR-035); defaults to `Autre` at creation if none chosen. |

**Derived (never stored)**: real time spent, remaining time — computed from associated
`PomodoroSession` rows (FR-029).

**State transitions**:

```
ACTIVE  <--pause/resume--> EN_PAUSE
ACTIVE  --complete-->  TERMINEE (completedAt = now)
EN_PAUSE --complete--> TERMINEE (completedAt = now)
TERMINEE --reopen--> ACTIVE (completedAt = null, time spent unchanged — FR-032)
```

## PomodoroSession

Represents one Pomodoro timer run, submitted by the front end only once it has ended
(completed or interrupted) — never while running (per the front-end timestamp-based timer
design).

| Field | Type | Rules |
|---|---|---|
| `id` | UUID (PK) | Generated. |
| `mode` | Enum: `FOCUS, PAUSE_COURTE, PAUSE_LONGUE` | Required. |
| `taskId` | UUID (FK → Task, nullable) | Optional; pauses are typically unassociated, Focus sessions usually associated. Nullable so a deleted task doesn't require cascading deletion of past sessions (see spec edge case: session time stays counted after task deletion → FK is `ON DELETE SET NULL`). |
| `startedAt` | Instant | Required. The session's calendar day = the day of `startedAt` (spec decision: "une session est rattachée au jour de son début"). |
| `endedAt` | Instant | Required, `>= startedAt`. |
| `actualDurationSeconds` | Integer | Required, `= endedAt - startedAt` in seconds, computed and sent by the front end from its own timestamps (never an incremental counter). |
| `completionStatus` | Enum: `TERMINEE, INTERROMPUE` | Required. Only `FOCUS` + `TERMINEE` sessions count toward "nombre de Pomodoros" / long-break counter (FR-033). All `FOCUS` sessions (`TERMINEE` or `INTERROMPUE`) with `actualDurationSeconds >= 60` count toward total focus time; sessions under one minute are ignored entirely from focus-time sums. |
| `createdAt` | Instant | Server-assigned receipt timestamp (audit only, not used in calculations). |

No update/delete endpoint is exposed for sessions in V1 — they are immutable once recorded,
consistent with Données brutes d'abord.

## Tag

| Field | Type | Rules |
|---|---|---|
| `id` | UUID (PK) | Generated. |
| `name` | String | Required, unique (case-insensitive). |
| `isDefault` | Boolean | `true` for the eight seeded tags (Écriture, Administratif, Maison, Enseignement, Créatif, DIY, Recherche, Autre), `false` for user-created tags. |
| `isProtected` | Boolean | `true` only for `Autre`. Enforced at the application layer: rename/delete requests on a protected tag are rejected (FR-036). |

**Seed data** (Flyway migration): the 8 default tags, with `Autre` flagged `isDefault=true,
isProtected=true`.

**Cascade rule on delete** (application-layer, not a DB cascade): deleting a non-protected tag
that is a task's *only* tag reassigns that task to `Autre` before the tag row is removed
(FR-036). Deleting a tag that is one of several tags on a task simply removes the association.

## TaskTag (join table)

Implicit many-to-many join table (`task_id`, `tag_id`), no additional attributes. Equal-split
tag allocation for statistics (FR-031 combined with the spec's tag-splitting decision) is a
read-time calculation: a task's real time (FR-029) is divided evenly across its current tag
count when aggregating tag percentages — never stored.

## MoodEntry

| Field | Type | Rules |
|---|---|---|
| `id` | UUID (PK) | Generated. |
| `date` | LocalDate | Required, unique — one entry per calendar day (FR-017). Upsert semantics: submitting a second entry for the same date updates the existing row (FR-019). |
| `moodScore` | Integer | Required, 1–7 inclusive. |
| `physicalFatigueScore` | Integer | Required, 1–7 inclusive. |
| `mentalFatigueScore` | Integer | Required, 1–7 inclusive. |
| `updatedAt` | Instant | Managed automatically. |

## Settings

Single-row table (fixed `id = 1`, enforced at the application layer — no create/delete
endpoint, only read/update).

| Field | Type | Rules | Default |
|---|---|---|---|
| `focusDurationMinutes` | Integer | > 0 | 25 |
| `shortBreakDurationMinutes` | Integer | > 0 | 5 |
| `longBreakDurationMinutes` | Integer | > 0 | 15 |
| `pomodorosBeforeLongBreak` | Integer | > 0 | 4 |
| `priorityCoefficientLow` | BigDecimal | > 0 | 1 |
| `priorityCoefficientNormal` | BigDecimal | > 0 | 1.25 |
| `priorityCoefficientHigh` | BigDecimal | > 0 | 1.5 |
| `priorityCoefficientCritical` | BigDecimal | > 0 | 2 |
| `soundNotificationsEnabled` | Boolean | — | true |

## Domain formulas (pure, no Spring dependency, TDD with JUnit 5 + AssertJ)

Each is a standalone pure Java class/function in `domain/`, taking primitives/records in and
returning primitives/records out — no entity, no repository, no Spring annotation.

| Formula | Inputs | Output | Edge case handling |
|---|---|---|---|
| `TaskWeight` | estimated minutes (nullable), priority coefficient | weight | `null`/absent estimate → weight 0 (FR-027, spec edge case). |
| `DailyProgress` | sum of weights of tasks completed today, sum of weights of tasks planned today | percentage | denominator 0 → 0% (never an error) (FR-028). |
| `RemainingTime` | estimated minutes, real minutes | `max(0, estimated - real)` | — (FR-029). |
| `TagPercentageBreakdown` | list of (tag → allocated time) pairs, total time for the period | percentage per tag | total 0 → every tag at 0% (FR-031). Allocation itself splits each task's real time evenly across its current tags before aggregating by tag (spec decision). |
| `WeeklyMoodFatigueAverage` | list of `MoodEntry` present in the week (only days with data) | average per scale, or "no data" | empty list → explicit "no data" marker, never 0 (FR-030, constitution V). |
| `PomodoroDailyCount` | list of today's `PomodoroSession` | count of `FOCUS` + `TERMINEE` sessions today | interrupted/other-mode sessions excluded (FR-033). |
| `PomodorosBeforeLongBreak` | today's completed Focus count, configured threshold | remaining count until long break | recomputed from today's sessions only — resets naturally at midnight since "today" changes (FR-037); never stored. |
| `FocusTimeTotal` | list of today's `FOCUS` sessions (any completion status) | total seconds | sessions with `actualDurationSeconds < 60` excluded from the sum (spec decision). |

## Entity relationship summary

```
Task 1---* PomodoroSession (nullable FK, ON DELETE SET NULL)
Task *---* Tag (join table task_tag)
MoodEntry (standalone, unique per date)
Settings (standalone, single row)
```
