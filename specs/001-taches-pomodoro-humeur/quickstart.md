# Quickstart: Les Fragments d'Instants — V1

**Data model**: [data-model.md](./data-model.md) | **Contracts**: [contracts/api-overview.md](./contracts/api-overview.md)

## Prerequisites

- Java 21 (`java -version`)
- Maven 3.9+ (or use the generated `backend/mvnw`)
- Node.js >= 22.22.3 (Angular CLI 22 requirement) — already satisfies the root `package.json`
- No external services, no accounts: everything runs locally.

## 1. Start the backend

```bash
cd backend
./mvnw spring-boot:run
```

- Flyway runs migrations automatically against `backend/data/fragments-instants.db` (created on
  first run).
- Swagger UI available at `http://localhost:8080/swagger-ui.html` — use it to sanity-check any
  endpoint from `contracts/api-overview.md` manually.
- Backend domain tests: `./mvnw test` — must be green (formulas in `domain/` are written and
  tested before their use cases, per the requested TDD flow).

## 2. Start the frontend

From the repository root (already scaffolded Angular app):

```bash
npm install   # first time only
ng serve
```

- Open `http://localhost:4200`.
- CORS is configured backend-side for this origin only.

## 3. Validation scenarios (one per P1 user story)

Each scenario below is runnable end-to-end through the UI once its vertical slice is
implemented; the matching acceptance scenario in `spec.md` is referenced for exact expected
values.

1. **Tâches** (spec US1): create a task titled "Préparer l'atelier", priority "haute", estimated
   60 min, no tag chosen → task appears in "Toutes" with `Autre` tag, temps réel 0, temps restant
   60 min. Filter "Importantes" shows it. Complete it, then reopen it: it returns to "Toutes"
   with temps réel unchanged.
2. **Pomodoro** (spec US2): start a Focus session on that task, let it run ~10s, then reset it
   before completion → dashboard focus time increases by ~10s but the Pomodoro count and
   long-break counter do not increment (FR-033). Start another Focus session and let it complete
   normally → count increments by 1.
3. **Journée** (spec US3): open the dashboard with the task above (partially weighted, not
   completed) → weighted progress % matches `weight(60, 1.5) → contributes only to denominator`
   until completed.
4. **Tags** (spec US4): create a custom tag "Voyage", assign it to the task alongside another tag
   → `Autre` is automatically removed; remove both tags → `Autre` is automatically reassigned.
   Attempt to rename/delete `Autre` → rejected.
5. **Humeur/fatigue** (spec US5): submit today's three scores (5/3/4) → saved. Resubmit with a
   different mood score → same day's entry updates, no duplicate row (check via
   `GET /mood-entries?from=<today>&to=<today>` returning exactly one entry).
6. **Visualisations** (spec US6): after generating a few days of data, check the tag donut sums
   to 100% (SC-008), the weekly histogram shows all 7 days including zero-focus days, and hiding
   the "fatigue mentale" series on the trend chart leaves the other two visible.
7. **Persistance** (spec US7): stop both processes, restart them → all tasks/sessions/mood
   entries/settings from steps above are still present (SQLite file persisted under
   `backend/data/`).
8. **Paramètres** (spec US8): change Focus duration to 30 min in Settings → the next Pomodoro
   Focus session started uses 30 min. Change the "critique" coefficient to 2.5 → dashboard
   progress recalculates using the new coefficient on next load.

## 4. Test suites to run before calling a slice "done"

```bash
# Backend — domain formulas, then controllers
cd backend && ./mvnw test

# Frontend — critical screens (dashboard, task list, Pomodoro timer, mood entry)
ng test
```
