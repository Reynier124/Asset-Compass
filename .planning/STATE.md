---
gsd_state_version: '1.0'
status: planning
progress:
  total_phases: 6
  completed_phases: 0
  total_plans: 0
  completed_plans: 0
  percent: 0
---

# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-08-26)

**Core value:** A unified, accurate, read-only view of all investments across Binance, IOL, and Nexo. Everything else — alerts, hardening, trade execution — depends on this being correct first.
**Current focus:** Phase 1 — Scaffolding & CI Foundation

## Current Position

Phase: 1 of 6 (Scaffolding & CI Foundation)
Plan: 0 of TBD in current phase
Status: Ready to plan
Last activity: 2026-08-26 — ROADMAP.md and STATE.md created; 19/19 v1 requirements mapped across 6 phases

Progress: [░░░░░░░░░░] 0%

## Performance Metrics

**Velocity:**
- Total plans completed: 0
- Average duration: - min
- Total execution time: 0 hours

**By Phase:**

| Phase | Plans | Total | Avg/Plan |
|-------|-------|-------|----------|
| - | - | - | - |

**Recent Trend:**
- Last 5 plans: -
- Trend: -

*Updated after each plan completion*

## Accumulated Context

### Decisions

Decisions are logged in PROJECT.md Key Decisions table.
Recent decisions affecting current work:

- [Roadmap]: Horizontal-layer build order chosen per research/SUMMARY.md — Data Foundation before any connector (schema/currency/precision decisions are shared and expensive to retrofit), then Binance → IOL → Nexo by reliability gradient (official/documented first, unofficial/unreliable last), then BI Dashboard last since it depends on all three connectors plus currency normalization.
- [Roadmap]: Scaffolding hygiene (hexagonal structure, Docker Compose, Flyway, springdoc, Actuator, README) placed as Phase 1, ahead of Data Foundation, since Data Foundation's precision-widening migration and BrokerConnector port depend on Flyway and the hexagonal package layout already being in place.
- [PROJECT.md, pending]: `Operation` remains the immutable source of truth; `Position`/`Valuation` are materialized views — this fork for connector-sourced data (direct snapshot writes vs. routing through `Operation`) is flagged by research as needing a one-line ADR during Phase 2 planning.
- [PROJECT.md, pending]: GSD commits locally under the user's own git identity, never pushes.
- [PROJECT.md, pending]: Switched from Liquibase to Flyway for schema migrations (JHipster scaffolded Liquibase by default).

### Pending Todos

None yet.

### Blockers/Concerns

- **Open question carried from PROJECT.md**: whether Keycloak (and possibly Kafka) needs to be in Docker Compose / local dev now, given portfolioService is scaffolded with `authenticationType oauth2` and `messageBroker kafka` even though it's the only active service. Ask before deciding — relevant to Phase 1 (SCAF-02).
- **Known defect, unresolved**: 10 failing integration tests in portfolioService (`createXxxResourceIT` across most entities) — JHipster-generated tests POST a DTO without an `id` expecting a DB-generated one, but entities use manually-assigned UUIDs with `@NotNull` on `id`. Confirm root cause and fix trade-offs before relying on a clean `mvn verify`; relevant before/during Phase 1.
- **Research flag**: Nexo account type (retail vs. Nexo Pro) is unresolved — determines whether Phase 5 is a REST connector at all or needs a scraper/manual-import fallback. Must be resolved during Phase 5 discussion, not assumed.
- **Research flag**: IOL's full API spec is gated behind an activated brokerage account; endpoint shapes are inferred from community wrappers, not primary docs. Budget manual-exploration time early in Phase 4 before finalizing WireMock fixtures.
- **Research flag**: Exact FX rate source for currency normalization (provider/API, refresh cadence, historical rate availability for backfill) is not yet decided — needs a concrete decision during Phase 2 planning.

## Deferred Items

Items acknowledged and deferred at milestone close, most recent first:

| Category | Item | Status | Deferred At | Milestone |
|----------|------|--------|-------------|-----------|
| v2 | EXTRA-01/02/03, ANLY-01/02/03, SYNC-01 (manual/CSV fallback, CSV export, simple gain/loss, TWRR/MWRR, per-broker comparison, real-time sync) | Deferred | Roadmap creation | v1 (Consolidation & visibility) |

## Session Continuity

Last session: 2026-08-26
Stopped at: ROADMAP.md and STATE.md written; REQUIREMENTS.md traceability updated. Next step is `/gsd-plan-phase 1`.
Resume file: None
