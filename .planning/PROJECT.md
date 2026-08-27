# Asset Compass

## What This Is

Asset Compass is a personal investment dashboard that consolidates accounts from multiple brokers/exchanges — Binance, IOL (InvertirOnline), and Nexo — into a single unified view. It has a dual purpose that matters equally: (1) genuine personal utility, seeing all investments in one place, and eventually alerts and trading; and (2) a technical portfolio piece demonstrating professional-level engineering practices (microservices, CI/CD, BI, security, testing, containers, orchestration). Scope and stack decisions get evaluated against both goals, not just one.

It's a JHipster-generated microservices monorepo: `gateway` (React/TypeScript PWA + reverse proxy + OAuth2/OIDC via Keycloak), `portfolioService` (the active core — broker connectors, financial data model, position/valuation calculations), `notificationService` and `tradingService` (scaffolded, empty — later phases).

## Core Value

A unified, accurate, read-only view of all investments across Binance, IOL, and Nexo. Everything else — alerts, hardening, trade execution — depends on this being correct first.

## Requirements

### Validated

- ✓ Microservices scaffold generated via JHipster/JDL — 4 apps (gateway, portfolioService, notificationService, tradingService), Kubernetes manifests included — existing
- ✓ PortfolioService domain model — 10 JPA entities (Asset, AssetRatio, Broker, BrokerAccount, IncomeEvent, Operation, Position, Rebate, TaxEvent, Valuation), UUID PKs, `Operation` as immutable source of truth with `Position`/`Valuation` as materialized views — existing
- ✓ REST API layer for portfolio entities — CRUD + filtered/paginated queries (QueryService + Criteria pattern) — existing
- ✓ GitHub Actions CI for portfolioService — path-filtered trigger, JDK 21, Spotless lint, `mvn verify`, CI badge in README — existing (Issue #6)

### Active

<!-- Phase 1 of the project's own roadmap ("Consolidation & visibility"), broken into its 6 milestones. These are hypotheses until shipped and validated. -->

- [ ] Hexagonal-lite package structure (domain/application/infrastructure) inside portfolioService
- [ ] Docker Compose for local dev (Postgres, Redis, Kafka, and — open question — Keycloak, since portfolioService is scaffolded expecting real OAuth2)
- [ ] Remaining scaffolding hygiene: Flyway migrations, Redis client wiring, structured logging, springdoc-openapi, Actuator, full README
- [ ] Binance connector — `BrokerConnector` interface implementation, API key/secret auth, account balances + positions, WireMock-mocked tests (CI never hits the real API)
- [ ] IOL connector — same interface, Bearer token auth including the 15-minute refresh flow, WireMock-mocked tests
- [ ] Nexo connector — same interface, reverse-engineered/best-effort (no official public API), tolerant error handling and fallback behavior
- [ ] Unified data model & persistence — normalize across all three brokers, Postgres + Flyway migrations, Spring Data JPA, scheduled snapshot job (foundation for BI history)
- [ ] BI Dashboard — backend aggregation endpoints (consolidated performance, exposure by currency/asset, historical evolution) + React/TypeScript PWA frontend with charts. Closes Phase 1: fully working, demo-ready product.

### Out of Scope

- Alerts (rules engine, email/Telegram notifications) — Phase 2 of the project's own roadmap, deferred until consolidation is solid
- Security & QA hardening as a dedicated effort (secrets management, least-privilege API keys, audit logs, full test coverage) — Phase 3, deferred, but must precede any real-money operation
- Trade execution (buy/sell/subscribe, dry-run mode, manual confirmation, hard limits) — Phase 4, explicitly gated behind Phase 3 hardening; real-money safety is non-negotiable

## Context

**Dual purpose:** Personal utility (real investment tracking, eventually alerts/trading) and professional portfolio piece (microservices, CI/CD, BI, security, testing, containers, orchestration) — both drive decisions equally.

**Project's own phase roadmap** (broader than this GSD cycle, useful framing for what comes after):
1. **Consolidation & visibility** (current) — read-only broker connectors, unified data model, BI dashboard, CI/CD end to end
2. **Alerts** — configurable rules (expirations, performance thresholds, price changes), periodic evaluation engine, email/Telegram
3. **Security & QA hardening** — no new features; audits and reinforces what's built before enabling real-money operations
4. **Trade execution** — buy/sell/subscribe, mandatory dry-run mode, manual confirmation, hard limits

**Current state (verify before redoing work):**
- Full scaffolding generated via JHipster from a JDL file (all 4 apps + K8s manifests) — satisfies the original "Project Scaffolding" intent, even though earlier issue text mentioned Spring Initializr (JHipster was the actual method used).
- CI pipeline for portfolioService is done (Spotless + `mvn verify` on PR, path-filtered). Linter/formatting setup is therefore likely already satisfied too — verify current state before redoing.
- **Known defect, unresolved:** 10 failing integration tests in portfolioService (`createXxxResourceIT` across most entities). Root cause: JHipster-generated tests POST a DTO without an `id`, expecting a DB-generated one, but entities use manually-assigned UUIDs with `@NotNull` on `id`. A previous session was asked to confirm root cause and present fix trade-offs before implementing anything — check whether that was resolved before relying on a clean `mvn verify`.
- Still pending from the current milestone: hexagonal package structure, Docker Compose (see open question below), Flyway/Redis/structured logging/springdoc/Actuator wiring, full README content.
- **Open question, do not assume:** whether Keycloak (and possibly Kafka) needs to be in Docker Compose / local dev right now, given portfolioService is scaffolded with `authenticationType oauth2` and `messageBroker kafka` even though it's the only active service. Ask before deciding, if a task touches local env setup or app startup.

**Architecture (confirmed decisions):**
- Each broker integrates via a common `BrokerConnector` interface (adapter pattern) — domain logic must not depend on any specific external API
- REST (sync) + Kafka (async, future inter-service events) for communication
- One PostgreSQL instance per service, no shared schema
- Kubernetes-native service discovery (no Eureka/Consul)
- Kubernetes deployment target, manifests generated from the JDL's `deployment` block

**Tech stack:** Java 21, Spring Boot, Maven | React + TypeScript (PWA) | PostgreSQL (one per service) | Flyway migrations | Redis (cache) | Kafka (async) | Keycloak (OAuth2/OIDC) | Docker | Kubernetes | GitHub Actions | JUnit, Mockito, Testcontainers, WireMock | springdoc-openapi/Swagger | Sentry + ELK (observability) | JHipster/JDL | Conventional Commits.

## Constraints

- **Git workflow**: GSD may create local git commits (they'll carry the user's own git identity, no AI byline) but must never push, force-push, or otherwise touch the remote — the human handles all remote operations manually. This relaxes an earlier "never run git commands" rule specifically for GSD-managed local commits.
- **No unprompted assumptions**: Ask before deciding anything not explicitly covered by the task/issue at hand — especially anything flagged as an open question (e.g., Keycloak/Kafka in local dev).
- **Explore before editing**: Always check actual repo state before making changes; never assume file paths or existing behavior — things may have changed since context was last captured.
- **Tech stack is locked in**: Scaffolded via JHipster/JDL (Java 21, Spring Boot, Maven, React/TS, Postgres, Redis, Kafka, Keycloak, Kubernetes, GitHub Actions) — not open for reconsideration within this milestone.
- **ID strategy**: UUIDs (not DB-generated) as primary keys for all entities, deliberately, so ID type/strategy matches exactly between the gateway and the owning service.
- **Real-money safety (future)**: When Phase 4 (trade execution) is eventually reached, dry-run mode, manual confirmation, and hard limits are non-negotiable safeguards — not optional hardening.

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| JHipster + JDL scaffolding (not plain Spring Initializr) | Consistent, fast scaffolding across 4 microservices including Kubernetes manifests | ✓ Good |
| UUID primary keys (not DB-generated) for all entities | ID type/strategy must match exactly between gateway and owning service in a microservices setup | ✓ Good |
| `Operation` as immutable source of truth; `Position`/`Valuation` as materialized views, never primary data | Prevents drift between raw events and computed/derived state | — Pending |
| GSD commits locally under the user's own git identity, never pushes | User wants atomic local commits from the workflow while retaining full manual control over remote history | — Pending |
| Switch from Liquibase to Flyway for schema migrations | Original plan assumed Flyway, but JHipster scaffolded Liquibase by default; user chose to switch rather than keep the scaffold default | — Pending |
| Removed `@GeneratedValue` from all 10 portfolioService domain entities | UUIDs are manually assigned, not DB-generated — `@GeneratedValue` contradicted that and actively broke the 10 `create*ResourceIT` tests (Hibernate routed client-supplied ids through `merge()` instead of `persist()`); verified schema-safe against the Liquibase changelogs (no DB-side default/generator on any `id` column) | ✓ Good |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd-transition`):
1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone** (via `/gsd-complete-milestone`):
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-08-26 after initialization*
