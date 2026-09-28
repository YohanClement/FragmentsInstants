# Phase 0 Research: Les Fragments d'Instants — V1

**Date**: 2026-09-28
**Feature**: [spec.md](./spec.md)

## Scope of this research

This feature's technical context was provided in detail by the user (architecture, layering,
timer approach, business-rule decisions). Phase 0 therefore focuses on: (1) verifying current
stable versions of every named dependency, since the request explicitly asked for this, and
(2) resolving the two ambiguities/conflicts found between the technical-context request and the
approved spec. No `NEEDS CLARIFICATION` markers remain after this phase.

## Conflicts resolved with the user before proceeding

### Decision: Spring Boot 4.1.1 instead of Spring Boot 3

- **Rationale**: The Spring Boot 3.x line reached open-source end of life on 2026-06-30 (final
  patch 3.5.16). Spring Boot 4.1.1 is the current actively maintained stable release. The user
  was asked and chose 4.1.1 over the EOL 3.5.16, since this is a new project starting today with
  no legacy Spring Boot 3 code to migrate.
- **Impact**: Jakarta EE 11 baseline (Servlet 6.1, JPA 3.2, Bean Validation 3.1), Java 17 minimum
  (Java 21 as requested remains fully supported, up to Java 26). No impact on the layered
  architecture, Flyway usage, or SQLite storage — these are independent of the Spring Boot major
  version.
- **Alternatives considered**: Spring Boot 3.5.16 — rejected because it is EOL with no further
  security patches, which is inappropriate to start a new project on.

### Decision: Drop the "variation vs. previous value" formula

- **Rationale**: The technical-context input listed a formula for "variation with previous value
  at 0" among the pure domain calculations to implement. No functional requirement in the
  approved spec calls for a trend/variation calculation, and the spec explicitly places
  "comparaisons entre semaines" (week-over-week comparisons) out of scope for V1. The user
  confirmed dropping this formula rather than building an unused calculation.
- **Impact**: The domain layer implements exactly the formulas backed by FR-027 through FR-031
  (weight, daily progress, remaining time, tag percentage, weekly mood/fatigue averages). No
  variation/trend class is created in V1.
- **Alternatives considered**: Implement it anyway as a general-purpose unused utility — rejected
  as speculative/unused code, contrary to the Simplicité constitution principle.

## Dependency version research (verified 2026-09-28)

| Dependency | Version | Notes |
|---|---|---|
| Java | 21 | As requested; Spring Boot 4.1.1 supports Java 17–26. |
| Spring Boot | 4.1.1 | Current stable (see decision above). Brings Spring Framework 7.0.x, Hibernate ORM 7.x. |
| Maven | 3.9+ | Standard build tool, no pinned version needed (wrapper via `mvnw`). |
| Spring Data JPA | Managed by Spring Boot 4.1.1 BOM | No explicit version pin needed. |
| Flyway | flyway-core 13.7.0 (or latest 13.x at implementation time) | Managed via `flyway-core` + dedicated SQLite module (see below). |
| Flyway SQLite support | `org.flywaydb:flyway-database-nc-sqlite:13.0.0` (align to the `flyway-core` version used) | Flyway does not support SQLite natively in `flyway-core`; SQLite is one of the "community/nc" dialect modules and must be added explicitly, matching the core version. |
| SQLite JDBC driver | `org.xerial:sqlite-jdbc:3.53.4.0` | Latest stable driver on Maven Central. |
| Hibernate SQLite dialect | `org.hibernate.orm:hibernate-community-dialects` (version = Hibernate ORM version bundled with Spring Boot 4.1.1) → `org.hibernate.community.dialect.SQLiteDialect` | Hibernate has no official SQLite dialect in `hibernate-core`; the community dialects module is required and is best-effort maintained (documented risk, acceptable for a local single-user app). |
| springdoc-openapi | `springdoc-openapi-starter-webmvc-ui:3.1.1` | Explicitly supports Spring Boot 3.x and 4.x with Java 21+ and OpenAPI 3.1. |
| JUnit 5 + AssertJ | Managed by `spring-boot-starter-test` in the 4.1.1 BOM | No explicit version pin needed. |
| Angular | 22.2.0 | **Already scaffolded in this repository at the project root** (`package.json`, `angular.json`); matches "latest stable" at research time (Angular 22, stable since 2026-06-03). |
| TypeScript | ~6.0.2 (already pinned in the scaffold) | Angular 22 requires TypeScript >=6.0 <6.1; strict mode already the project default. |
| ng2-charts | 10.0.0 | Peer dependencies: `@angular/core` etc. `>=21.0.0`, `chart.js` `^3.4.0 || ^4.0.0`, `rxjs` `^6.5.3 || ^7.4.0` — compatible with the scaffolded Angular 22.2.0 / RxJS 7.8. |
| chart.js | ^4.x (latest 4.x) | Peer dependency of ng2-charts 10.0.0; version 4 chosen over 3 as the actively developed major. |
| Angular test runner | Vitest (already the scaffold's default, `ng test`) | Angular 22 defaults `ng new` to Vitest instead of Karma/Jasmine; the scaffolded project already uses it (`vitest`, `jsdom` devDependencies) — no change needed, "tests de composants Angular" will run under Vitest + Angular Testing Library primitives (`TestBed`). |
| Node.js | >=22.22.3 (or 24.15.0 / 26.0.0) | Minimum required by Angular CLI 22; a prerequisite for `quickstart.md`, not a project dependency. |

## Other decisions

### Decision: SSR scaffold present but unused for this feature

- **Rationale**: The existing Angular scaffold at the repository root was generated with SSR
  (Express server, `platform-server`, `src/server.ts`, `outputMode: server`). Les Fragments
  d'Instants is a local, single-user client talking to a local Spring Boot API on the same
  machine (Local-first principle) — there is no SEO or server-rendering need. Implementation will
  use `ng serve` / the browser build for development and validation; the SSR entry points are
  left untouched (not removed, to avoid unrelated scope) but are not exercised by this feature.
- **Impact**: `quickstart.md` validates the app via `ng serve`, not the SSR server bundle.
- **Alternatives considered**: Stripping SSR from the scaffold — rejected as out of scope for this
  feature's plan; it is a pre-existing project-setup concern, not part of this feature's
  requirements.

### Decision: Backend lives in a new `backend/` directory; Angular stays at repository root

- **Rationale**: The Angular application is already scaffolded at the repository root (not under
  a `frontend/` folder). Moving it would be unnecessary churn. The Spring Boot backend is added as
  a sibling `backend/` directory, keeping each toolchain (`npm`/Angular CLI at root, `mvn` inside
  `backend/`) self-contained.
- **Alternatives considered**: Relocating the Angular app under `frontend/` to mirror the generic
  "web application" template — rejected, since it would move already-scaffolded, unrelated files
  for no functional benefit.

### Decision: SQLite file location and CORS

- **Rationale**: The SQLite database file is stored inside `backend/data/` (gitignored), read at
  startup via a JDBC URL pointing to a fixed local path. CORS is enabled on the Spring Boot side
  for `http://localhost:4200` (the Angular dev server origin), as both processes run locally with
  no authentication (Local-first, no distributed deployment in V1).
- **Alternatives considered**: Embedding the Angular build inside the Spring Boot JAR (single
  deployable) — rejected for V1 to keep the two toolchains and their test suites fully
  independent during vertical-slice development, per the user's explicit two-process
  client-server architecture request.

### Decision: Package base name

- **Decision**: Backend Java base package is `com.fragmentsinstants`, with `domain`,
  `application`, and `infrastructure` sub-packages as specified.
- **Rationale**: No existing backend code or naming convention exists yet in the repository; this
  follows the reverse-domain convention used by the project name.
