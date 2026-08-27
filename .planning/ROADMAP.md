# Roadmap: Asset Compass

## Overview

This milestone ("Consolidation & visibility") takes the existing JHipster-scaffolded `portfolioService`
from a generated skeleton to a working, demo-ready product: a single dashboard showing a unified,
accurate, read-only view of investments across Binance, IOL, and Nexo. The build order is horizontal
by design — a scaffolding/CI foundation, then a shared data foundation (precision, currency
normalization, credential storage, snapshot completeness), then one broker connector at a time in a
strict reliability gradient (Binance → IOL → Nexo, cleanest/official first, unofficial/unreliable
last), and finally the BI dashboard that reads only the already-normalized data those connectors
produce. Every phase after the first two builds directly on schema and contract decisions made once,
in Phase 2, rather than being rediscovered per connector.

## Phases

**Phase Numbering:**
- Integer phases (1, 2, 3): Planned milestone work
- Decimal phases (2.1, 2.2): Urgent insertions (marked with INSERTED)

Decimal phases appear between their surrounding integers in numeric order.

- [ ] **Phase 1: Scaffolding & CI Foundation** - Hexagonal-lite structure, Docker Compose local stack, Flyway, API docs, health/metrics, and a real README
- [ ] **Phase 2: Data Foundation** - Crypto-precision storage, base-currency normalization, protected credential storage, idempotent per-broker snapshot completeness, and the typed connector contract
- [ ] **Phase 3: Binance Connector** - Reference broker integration proving the connector pattern end to end
- [ ] **Phase 4: IOL Connector** - Second broker integration, validating the pattern against stateful bearer-token auth
- [ ] **Phase 5: Nexo Connector** - Third broker integration, validating graceful degradation against an unofficial, unreliable API
- [ ] **Phase 6: BI Dashboard & Reporting** - Unified position view, exposure breakdown, and historical chart across all three brokers

## Phase Details

### Phase 1: Scaffolding & CI Foundation
**Goal**: portfolioService has a clean architectural foundation, a reproducible local dev environment, and operational visibility — ready for feature work.
**Depends on**: Nothing (first phase; builds on the existing validated JHipster scaffold and CI)
**Requirements**: SCAF-01, SCAF-02, SCAF-03, SCAF-04, SCAF-05, SCAF-06
**Success Criteria** (what must be TRUE):
  1. Developer can bring up the full local stack (Postgres, Redis, Kafka, and Keycloak if determined necessary) with a single Docker Compose command.
  2. portfolioService's code is organized into domain/application/infrastructure layers, with no broker-specific logic inside domain code.
  3. All schema changes apply via Flyway migrations; the existing Liquibase changesets no longer drive schema state.
  4. Developer can explore and try every REST endpoint via interactive Swagger/OpenAPI docs, and check service health/metrics via Spring Boot Actuator.
  5. A new developer can follow the README to set up the local stack, run the service, and run the full test suite end to end without outside help.
**Plans**: TBD

### Phase 2: Data Foundation
**Goal**: The data model and persistence layer can correctly and safely hold real broker data — at crypto precision, normalized to one currency, with credentials protected and every snapshot run auditable — before any connector writes to it.
**Depends on**: Phase 1
**Requirements**: FOUND-01, FOUND-02, FOUND-03, FOUND-04, CONN-04
**Success Criteria** (what must be TRUE):
  1. Storing a small fractional crypto quantity (e.g. 0.00000001 BTC) round-trips through the database without truncation to zero.
  2. Every position/valuation exposed by the platform is expressed in one configurable base currency, regardless of the broker's native currency.
  3. Broker credentials live in a dedicated, non-CRUD-exposed table and never appear in any REST response, including the auto-generated BrokerAccount resource.
  4. Re-running a snapshot for the same period does not duplicate data, and the resulting record shows which brokers succeeded or failed for that run.
  5. The BrokerConnector port defines a typed sync outcome (success / auth-expired / transient-error / partial) that any connector implementation must return instead of throwing raw exceptions.
**Plans**: TBD

### Phase 3: Binance Connector
**Goal**: User's Binance account data flows into Asset Compass automatically and reliably, proving the connector pattern that IOL and Nexo will reuse.
**Depends on**: Phase 2
**Requirements**: CONN-01
**Success Criteria** (what must be TRUE):
  1. User's Binance balances and positions appear in the platform after providing an API key/secret, without manual intervention.
  2. A transient Binance API failure (rate limit, timeout) is retried automatically and does not abort the rest of the snapshot job.
  3. Each Binance sync run records its typed outcome (success / auth-expired / transient-error / partial), visible per run.
  4. CI runs and passes the full Binance connector test suite without ever contacting the real Binance API.
**Plans**: TBD

### Phase 4: IOL Connector
**Goal**: User's IOL account data flows into Asset Compass automatically, correctly handling the 15-minute bearer-token lifecycle.
**Depends on**: Phase 3 (reuses the connector pattern, resilience conventions, and typed-outcome contract established there)
**Requirements**: CONN-02
**Success Criteria** (what must be TRUE):
  1. User's IOL balances and positions appear in the platform after providing bearer-token credentials.
  2. The IOL access token is refreshed proactively before its 15-minute expiry, so a sync never fails due to token staleness under normal operation.
  3. An expired or invalid IOL token produces a typed "auth-expired" outcome and is never coerced into a false zero-balance snapshot.
  4. CI runs and passes the full IOL connector test suite without ever contacting the real IOL API.
**Plans**: TBD

### Phase 5: Nexo Connector
**Goal**: User's Nexo account data flows into Asset Compass on a best-effort basis, degrading gracefully given Nexo's lack of an official API.
**Depends on**: Phase 4 (failure-isolation and resilience patterns are proven on two working connectors before tackling the least reliable one)
**Requirements**: CONN-03
**Success Criteria** (what must be TRUE):
  1. User's Nexo balances and positions appear in the platform when the integration succeeds.
  2. When Nexo sync fails (unofficial API change, timeout, schema drift), the failure is isolated — Binance and IOL data still update normally in the same snapshot run.
  3. A Nexo outage produces a typed transient-error/partial outcome and a clearly stale/unavailable state, never a silent wrong balance.
  4. CI runs and passes using fixture/contract tests, without depending on Nexo's undocumented API being reachable.
**Plans**: TBD

### Phase 6: BI Dashboard & Reporting
**Goal**: User can see one accurate, unified view of their entire portfolio across all three brokers, including exposure and history, even when a broker is degraded.
**Depends on**: Phase 5 (needs all three connectors feeding normalized data, plus Phase 2's currency-normalization policy)
**Requirements**: DASH-01, DASH-02, DASH-03, DASH-04, DASH-05
**Success Criteria** (what must be TRUE):
  1. User sees per-broker connection status (connected/syncing/error/stale) with a last-synced timestamp for each broker.
  2. When one broker (e.g. Nexo) is unavailable, the dashboard still renders with the other brokers' data, clearly labeling what's missing or incomplete.
  3. User sees a single unified list of positions across Binance, IOL, and Nexo, normalized to one base currency.
  4. User sees an exposure/allocation breakdown by asset class, broker, and currency.
  5. User sees a historical portfolio value chart built from the scheduled snapshot history.
**Plans**: TBD
**UI hint**: yes

## Progress

**Execution Order:**
Phases execute in numeric order: 1 → 2 → 3 → 4 → 5 → 6

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Scaffolding & CI Foundation | 0/TBD | Not started | - |
| 2. Data Foundation | 0/TBD | Not started | - |
| 3. Binance Connector | 0/TBD | Not started | - |
| 4. IOL Connector | 0/TBD | Not started | - |
| 5. Nexo Connector | 0/TBD | Not started | - |
| 6. BI Dashboard & Reporting | 0/TBD | Not started | - |
