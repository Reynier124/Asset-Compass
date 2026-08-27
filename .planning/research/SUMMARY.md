# Project Research Summary

**Project:** Asset Compass
**Domain:** Multi-broker fintech data aggregation (read-only Binance/IOL/Nexo connectors + BI/analytics dashboard) on an existing JHipster/Spring Boot 4 microservices monorepo
**Researched:** 2026-08-26
**Confidence:** MEDIUM

## Executive Summary

This milestone ("Consolidation & visibility") is a well-understood category — multi-broker net-worth/portfolio consolidation, comparable to Kubera, Delta, and CoinStats — being built on top of an already-scaffolded `portfolioService`. Experts in this space converge on the same shape: one read-only "connector" adapter per external provider behind a stable port interface, per-provider resilience (rate limiting, circuit breaking, retry) tuned to that provider's actual reliability, a scheduled polling job that isolates each provider's failures from the others, and a BI layer that reads only already-persisted, currency-normalized data rather than talking live to brokers. All four research tracks (stack, features, architecture, pitfalls) agree on this shape independently, which is the strongest signal in this research: build Binance first (cleanest, official, well-documented), IOL second (official but stateful auth), Nexo last (unofficial, must fail soft).

The recommended approach adds almost nothing new to the stack — Spring `RestClient` (already on the classpath) for all three connectors, Resilience4j (already a stack dependency) configured per-broker, WireMock/Testcontainers (already in use) to mock all three broker APIs in CI, and ShedLock only once the service scales beyond one replica. The single highest-leverage design decision is **currency normalization**: nearly every table-stakes feature (unified position view, exposure breakdown, historical chart) is architecturally blocked on a defined base-currency/FX-conversion policy, so this must be designed before or alongside the unified data model, not discovered mid-BI-phase.

The key risks are concentrated, not diffuse, and all research tracks flag the same ones: (1) the existing `scale=2` BigDecimal columns silently truncate crypto quantities to zero and must be widened before any connector writes real data; (2) IOL's 15-minute bearer token must be refreshed proactively and its failure must never be coerced into a "zero balance" that corrupts BI history; (3) the snapshot job must isolate each broker's failure (try/catch per broker, not one global try/catch) so a flaky Nexo never blocks Binance/IOL data; and (4) broker credentials must never be exposed through JHipster's default generated CRUD REST resource for `BrokerAccount`. Mitigating these four up front, in the "unified data model & persistence" and "Binance connector" phases specifically, de-risks everything downstream.

## Key Findings

### Recommended Stack

No new core dependencies are needed beyond what's already in the JHipster scaffold. Each broker connector is a hand-rolled `RestClient` adapter (not the official Binance SDK, which brings a redundant OkHttp stack and out-of-scope futures/margin/websocket surface) wrapped in per-broker Resilience4j instances. WireMock + Testcontainers mock all three broker APIs at the HTTP boundary in CI — real broker endpoints are never called in tests. ShedLock is deferred until the service scales past one replica. Plain Postgres (not TimescaleDB) is sufficient for the snapshot table at this milestone's scale.

**Core technologies:**
- Spring `RestClient` (already bundled): synchronous HTTP client for all three connectors — matches the existing blocking Spring MVC stack, keeps broker-specific quirks (signing, auth headers) inside each adapter
- Resilience4j via `spring-cloud-starter-circuitbreaker-resilience4j` (already in stack): per-broker `RateLimiter`/`CircuitBreaker`/`Retry`, tuned individually — Nexo gets the tightest circuit breaker, Binance the most generous rate limit
- WireMock + Testcontainers (already in stack): stub all three broker APIs at the HTTP protocol level for CI; never hit real endpoints
- ShedLock (new, add only when scheduled job ships and/or replicas > 1): JDBC-backed distributed lock reusing the existing Postgres instance

### Expected Features

**Must have (table stakes, P1):**
- Per-broker connection status (connected/syncing/error/stale) with last-synced timestamp, visible per `BrokerAccount`
- Typed sync outcomes (success / auth-expired / transient-error / partial) at the `BrokerConnector` interface level, not raw exceptions
- Graceful partial-failure dashboard — renders with 2-of-3 brokers if one (esp. Nexo) is down
- Unified cross-broker position list, normalized to one base currency
- Base-currency FX normalization policy (rate source, conversion timing) applied consistently — the single highest-leverage design decision
- Exposure/allocation breakdown by asset class, broker, currency
- Historical portfolio value chart, backed by the scheduled snapshot job

**Should have (P2, add after core validated):**
- Manual/CSV fallback entry scoped to Nexo, modeled as a `source: MANUAL` Operation (not a side-channel), not a full ledger replacement
- CSV export of the consolidated view
- Simple gain/loss per position/broker (not yet TWRR/MWRR)

**Defer (v2+):**
- TWRR/MWRR performance metrics (need accumulated historical snapshots + cash-flow classification first)
- Per-broker performance comparison
- Real-time/streaming sync (scheduled polling is the right MVP)
- Price/performance alerts, trade execution, DeFi/NFT tracking, social features, plugin marketplace — all explicitly out of scope per PROJECT.md's own phase roadmap

### Architecture Approach

Extend the existing hexagonal-lite `portfolioService` with a `domain/port/BrokerConnector` interface (one method: `fetchSnapshot(BrokerAccount) → BrokerSnapshot`, zero Spring/HTTP imports), one adapter per broker under `infrastructure/connector/{binance,iol,nexo}/` that owns all broker-specific auth and parsing internally, and new `application/` services (`SnapshotOrchestrationService`, `PortfolioAggregationService`) that isolate per-broker failures and read only persisted state for BI. The BI layer never calls brokers directly — it always reads the last successful snapshot, decoupling "is Nexo up" from "can the user see their dashboard."

**Major components:**
1. `BrokerConnector` (port) — one-method contract per broker; auth mechanics never leak onto the interface
2. `SnapshotOrchestrationService` + `SnapshotSchedulerJob` — loops connectors with per-broker try/catch, persists partial results, one schedule per broker
3. `PortfolioAggregationService` — reads persisted `Position`/`Valuation` across all `BrokerAccount`s, computes consolidated/normalized BI views, exposed via new REST endpoints following the existing Resource→Service→Repository layering

### Critical Pitfalls

1. **`scale=2` BigDecimal columns silently truncate crypto quantities to zero** — widen `quantity`/`averageCost`/`currentValue`/`totalValue` to `precision=28,scale=10` via Flyway migration before any connector writes real data; this is a schema decision made once for all three connectors, not per-connector.
2. **IOL's 15-minute token expiring mid-sync gets swallowed as "empty portfolio"** — refresh proactively (~80% of lifetime), single-flight the refresh, and model auth failure as a distinct "skip and flag" outcome, never coerced into a zero-balance snapshot.
3. **One broker's failure aborts the whole snapshot job** — catch and record success/failure per broker independently; never let a single global try/catch around the loop cause Nexo's flakiness to lose Binance/IOL data too.
4. **Broker credentials bolted onto `BrokerAccount` as plaintext columns get exposed via JHipster's generated CRUD REST resource** — store secrets in a separate, non-CRUD-exposed entity/table, `@JsonIgnore` at minimum, from the first connector (Binance) onward.
5. **Currency/unit mismatch makes "consolidated" BI numbers meaningless** — every normalized snapshot row must carry native amount+currency plus a converted reporting-currency amount, rate, and timestamp; validate currency against a whitelist at ingestion, not display time.

## Implications for Roadmap

Based on research, suggested phase structure (extending, not replacing, PROJECT.md's already-listed Active scope):

### Phase 1: Unified Data Model & Persistence Foundation
**Rationale:** Nearly every downstream feature and pitfall traces back to schema/data-model decisions that are cheap to make once and expensive to retrofit (precision, currency normalization, snapshot completeness tracking, credential storage). Must happen before any connector writes real data.
**Delivers:** Widened precision columns (Flyway migration), `BrokerConnector` port + normalized `BrokerSnapshot` domain model, currency normalization policy (base currency, FX rate source, conversion-timing), snapshot table with per-run/per-broker completeness status and idempotent upsert keying, credentials storage pattern (separate non-CRUD entity).
**Addresses:** Base-currency FX normalization, typed sync outcomes (table stakes)
**Avoids:** Pitfalls 1 (precision truncation), 5 (credential exposure), 6 (partial-failure data loss), 7 (duplicate scheduling), 8 (currency mismatch)

### Phase 2: Binance Connector (Reference Implementation)
**Rationale:** Cleanest, officially documented, versioned API — proves the `BrokerConnector` contract, the WireMock test pattern, and the resilience/backoff pattern that IOL and Nexo will inherit. Build first.
**Delivers:** `BinanceConnector` with HMAC-SHA256 signing, per-broker Resilience4j instances (rate limiter matched to published weight budget), WireMock fixture suite including 429/418 simulation.
**Uses:** Spring `RestClient`, Resilience4j, WireMock/Testcontainers
**Implements:** Adapter pattern with auth encapsulated inside the adapter; isolated-failure orchestration loop

### Phase 3: IOL Connector
**Rationale:** Official but stateful (15-minute bearer token) — validates that the port doesn't need to change to accommodate a different, more complex auth scheme than Binance's.
**Delivers:** `IolConnector` + `IolTokenManager` (proactive refresh, single-flight, cached in Redis if scaled), typed auth-expired outcome distinct from zero balances.
**Avoids:** Pitfall 2 (token-expiry-as-zero-balance)

### Phase 4: Nexo Connector
**Rationale:** No official API, least reliable — built last so the resilience/failure-isolation patterns are already proven on two working connectors before tackling the riskiest one. Requires resolving the retail-vs-Nexo-Pro account-type question before implementation locks in an approach.
**Delivers:** `NexoConnector` with strict (not lenient) deserialization, fixture/contract tests, tight circuit breaker, graceful "stale/unavailable" fallback; optional manual/CSV fallback entry scoped to Nexo.
**Avoids:** Pitfall 4 (silent schema-drift corruption)

### Phase 5: BI Aggregation & Dashboard
**Rationale:** Depends on all three connectors feeding normalized, currency-converted data into the snapshot table from Phase 1's design — cannot start meaningfully before currency normalization exists.
**Delivers:** `PortfolioAggregationService` + REST endpoints for unified position list, exposure/allocation breakdown, historical value chart (from scheduled snapshots), per-broker/overall staleness indicators, partial-data ("N of 3 brokers synced") UI treatment; simple gain/loss per position.
**Addresses:** All P1 table-stakes features plus P2 CSV export if time allows

### Phase Ordering Rationale

- Data model/persistence must precede all three connectors because precision, currency normalization, and completeness-tracking are schema decisions shared by all three — building even one connector against the wrong schema means redoing it three times.
- Connector order (Binance → IOL → Nexo) follows a strict reliability/complexity gradient recommended by both ARCHITECTURE.md and PITFALLS.md: prove the pattern on the easiest case, add statefulness, then add unreliability last so failure-isolation is already battle-tested.
- BI Dashboard is explicitly blocked on currency normalization (Phase 1) and at least the connectors that are in scope having landed — this avoids building aggregation logic against data that isn't yet normalized.

### Research Flags

Phases likely needing deeper research during planning:
- **Nexo Connector phase:** Sparse/no official documentation, unresolved retail-vs-Nexo-Pro account-type question — needs `--research-phase` to pin down actual API surface (or confirm a scraper/manual-import fallback is required) before planning locks in an approach.
- **IOL Connector phase:** No public sandbox exists; endpoint shapes are inferred from community wrappers, not primary docs — needs manual exploratory verification against a real account before WireMock fixtures are finalized.

Phases with standard patterns (skip research-phase):
- **Unified Data Model & Persistence:** Precision fix, currency normalization, and snapshot idempotency are well-established patterns (Flyway migration, standard normalization table design) with HIGH-confidence sources.
- **Binance Connector:** Official, versioned, well-documented API with HIGH-confidence sources on auth, rate limits, and signing.
- **BI Aggregation & Dashboard:** Standard layered Resource→Service→Repository pattern already used elsewhere in the codebase; no new architectural pattern required.

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | MEDIUM | HIGH for Binance/IOL auth mechanics and Spring-ecosystem patterns (RestClient, Resilience4j, WireMock all cross-checked); LOW for Nexo specifically since no official API exists to verify against |
| Features | MEDIUM | Web sources cross-checked across multiple independent competitor products (Kubera, Delta, CoinStats, Plaid); no primary API docs for connector-specific UX, so feature-to-connector mapping is inference |
| Architecture | MEDIUM-HIGH | Hexagonal/ports-and-adapters and Resilience4j patterns are well-established and cross-checked across 3+ sources; the Operation-vs-Snapshot data-modeling resolution is project-specific reasoning, not a sourced fact |
| Pitfalls | MEDIUM | Cross-checked web sources plus direct codebase verification (schema files read directly — HIGH confidence for those specific findings); no access to live Binance/IOL/Nexo sandbox accounts for testing |

**Overall confidence:** MEDIUM

### Gaps to Address

- **Nexo account type (retail vs Nexo Pro):** Must be resolved before Nexo connector planning — determines whether this is a REST connector at all or requires a scraper/manual-import fallback. Flag explicitly during Phase 4 discussion.
- **Operation/Position/Valuation model fork for connector-sourced data:** ARCHITECTURE.md identifies that broker polling observes current state, not discrete operations, which means `Position`/`Valuation` become partially independent of `Operation` once connectors ship. Recommended resolution (direct snapshot writes, bypassing `Operation`) should be captured as a one-line ADR during Phase 1 planning so it isn't re-litigated per connector.
- **IOL endpoint shapes:** Full API spec is gated behind an activated brokerage account; budget explicit manual-exploration time early in Phase 3 before committing to WireMock fixtures.
- **Exact FX rate source for currency normalization:** Not resolved by any research track — needs a concrete decision (which provider/API, refresh cadence, historical rate availability for backfill) during Phase 1 planning.

## Sources

### Primary (HIGH confidence)
- `.planning/codebase/STACK.md`, `.planning/codebase/ARCHITECTURE.md`, `.planning/codebase/CONCERNS.md`, `.planning/PROJECT.md` — internal, verified against actual repo
- Direct codebase verification: `portfolioService/src/main/java/com/assetcompass/portfolio/domain/{Operation,Position,Valuation,BrokerAccount,Asset,AssetRatio}.java`
- [Binance Open Platform — Rate Limiting and IP Bans](https://developers.binance.com/docs/binance-spot-api-docs/rest-api/limits)
- [Account Endpoints | Binance Open Platform](https://developers.binance.com/docs/binance-spot-api-docs/rest-api/account-endpoints)
- [InvertirOnline Autenticacion](https://api.invertironline.com/Help/Autenticacion)

### Secondary (MEDIUM confidence)
- [Kubera Net Worth Tracker](https://www.kubera.com/net-worth-tracker) / [Delta by eToro Features](https://delta.app/en/features) / [CoinStats](https://coinstats.app/connect-portfolio/) / [Plaid Docs — Institution status](https://plaid.com/docs/link/institution-status/)
- [Transforming a Traditional Spring Boot App into a Hexagonal Architecture](https://eliedhr.medium.com/transforming-a-traditional-spring-boot-app-into-a-hexagonal-architecture-0040b85add57)
- [Mastering Resilience4j Rate Limiter with Spring Boot](https://bootcamptoprod.com/resilience4j-rate-limiter/)
- [Testcontainers: Testing REST API integrations using WireMock](https://testcontainers.com/guides/testing-rest-api-integrations-using-wiremock/)
- [ShedLock — Ensuring Exactly-Once Execution in Multi-Pod Spring Boot](https://medium.com/@bectorhimanshu/ensuring-exactly-once-execution-of-scheduled-tasks-in-a-multi-pod-spring-boot-application-using-d6b17d0efc9c)
- [The Mathematics of Portfolio Return: Simple, Money-Weighted, Time-Weighted](https://portfoliooptimizer.io/blog/the-mathematics-of-portfolio-return-simple-return-money-weighted-return-and-time-weighted-return/)

### Tertiary (LOW confidence)
- [python-nexo](https://github.com/guilyx/python-nexo), [nexo-pro Node connector](https://github.com/aussedatlo/nexo-pro) — unofficial wrappers targeting Nexo Pro specifically, not confirmed to match the retail product
- Nexo official API search (status.nexo.com and general web) — negative result, no official API found
- [IOL developers.invertironline.com](https://developers.invertironline.com/) — full spec gated behind an activated account, endpoint shapes inferred from community wrappers

---
*Research completed: 2026-08-26*
*Ready for roadmap: yes*
