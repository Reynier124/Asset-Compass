# Requirements: Asset Compass

**Defined:** 2026-08-26
**Core Value:** A unified, accurate, read-only view of all investments across Binance, IOL, and Nexo. Everything else — alerts, hardening, trade execution — depends on this being correct first.

## v1 Requirements

Requirements for the "Consolidation & visibility" milestone. Each maps to roadmap phases.

### Scaffolding & CI Base

- [ ] **SCAF-01**: portfolioService's code is organized into hexagonal-lite layers (domain/application/infrastructure) so broker-specific logic never leaks into domain code
- [ ] **SCAF-02**: Developer can bring up the full local stack (Postgres, Redis, Kafka — and Keycloak if determined necessary) via a single Docker Compose command
- [ ] **SCAF-03**: All database schema changes are managed via Flyway migrations, replacing the existing Liquibase changesets
- [ ] **SCAF-04**: portfolioService exposes interactive API documentation (OpenAPI/Swagger) for all REST endpoints
- [ ] **SCAF-05**: portfolioService exposes health and metrics endpoints via Spring Boot Actuator
- [ ] **SCAF-06**: README documents local dev setup, architecture overview, and how to run tests end to end

### Data Foundation

- [ ] **FOUND-01**: All monetary/quantity columns support crypto-level precision — no silent truncation of small quantities (e.g. fractional BTC)
- [ ] **FOUND-02**: All broker-sourced values are normalized to a single configurable base currency for display and aggregation
- [ ] **FOUND-03**: Broker credentials are stored in a dedicated, non-CRUD-exposed table/entity, never returned by any auto-generated REST resource
- [ ] **FOUND-04**: Each snapshot run records per-broker completeness (which brokers succeeded/failed) and is idempotent (safe to re-run without duplicating data)

### Broker Connectors

- [ ] **CONN-01**: User's Binance account balances and positions sync into the platform via API key/secret authentication
- [ ] **CONN-02**: User's IOL account balances and positions sync into the platform via bearer-token authentication, including automatic token refresh before the 15-minute expiry
- [ ] **CONN-03**: User's Nexo account balances and positions sync into the platform on a best-effort basis, tolerating the exchange's lack of an official API
- [ ] **CONN-04**: Each connector reports a typed sync outcome (success / auth-expired / transient-error / partial) rather than raw exceptions

### Dashboard & Reporting

- [ ] **DASH-01**: User sees per-broker connection status (connected/syncing/error/stale) with a last-synced timestamp
- [ ] **DASH-02**: Dashboard renders correctly with partial data when one broker (e.g. Nexo) is unavailable, clearly labeling what's incomplete
- [ ] **DASH-03**: User sees a single unified list of positions across all three brokers, normalized to one base currency
- [ ] **DASH-04**: User sees an exposure/allocation breakdown by asset class, broker, and currency
- [ ] **DASH-05**: User sees a historical portfolio value chart backed by scheduled snapshots

## v2 Requirements

Deferred to future release. Tracked but not in current roadmap.

### Extras (decision explicitly deferred by user — revisit when reaching the relevant connector milestone)

- **EXTRA-01**: Manual/CSV fallback entry for Nexo when it can't sync (modeled as a `source: MANUAL` Operation, not a side-channel)
- **EXTRA-02**: CSV export of the consolidated dashboard view
- **EXTRA-03**: Simple gain/loss per position/broker (not yet TWRR/MWRR)

### Advanced Analytics

- **ANLY-01**: Time-Weighted Return (TWRR) per position/portfolio
- **ANLY-02**: Money-Weighted Return (MWRR/IRR) per position/portfolio
- **ANLY-03**: Per-broker vs. consolidated performance comparison

### Sync

- **SYNC-01**: Real-time/streaming sync (vs. scheduled polling)

## Out of Scope

Explicitly excluded. Documented to prevent scope creep.

| Feature | Reason |
|---------|--------|
| Price/performance alerts (email, Telegram, push) | Phase 2 of the project's own roadmap — this milestone should expose data for a future rules engine, not build the engine itself |
| Trade execution / rebalancing / one-click buy-sell | Phase 4, explicitly gated behind Phase 3 security hardening; real-money safety (dry-run, manual confirmation, hard limits) not yet built |
| Full secrets-management overhaul / vault UI | Phase 3's job; read-only credentials carry materially lower risk than trading credentials, so adequate-for-read-only handling is sufficient now |
| Full transaction-level audit log / tax reporting (forms, jurisdiction rules) | Disproportionate scope for a consolidation-and-visibility milestone; `TaxEvent` keeps capturing raw data, but building actual reports is a distinct future effort |
| Social/sharing features (public portfolio links, leaderboards) | Zero alignment with either stated project goal; pure scope creep |
| Generic "connect any broker" plugin marketplace | Premature abstraction for 3 known, named brokers — the existing `BrokerConnector` interface is sufficient extensibility |
| DeFi wallet / NFT tracking | None of Binance/IOL/Nexo require it; no named use case |

## Traceability

Which phases cover which requirements. Populated during roadmap creation.

| Requirement | Phase | Status |
|-------------|-------|--------|
| SCAF-01 | Phase 1 | Pending |
| SCAF-02 | Phase 1 | Pending |
| SCAF-03 | Phase 1 | Pending |
| SCAF-04 | Phase 1 | Pending |
| SCAF-05 | Phase 1 | Pending |
| SCAF-06 | Phase 1 | Pending |
| FOUND-01 | Phase 2 | Pending |
| FOUND-02 | Phase 2 | Pending |
| FOUND-03 | Phase 2 | Pending |
| FOUND-04 | Phase 2 | Pending |
| CONN-04 | Phase 2 | Pending |
| CONN-01 | Phase 3 | Pending |
| CONN-02 | Phase 4 | Pending |
| CONN-03 | Phase 5 | Pending |
| DASH-01 | Phase 6 | Pending |
| DASH-02 | Phase 6 | Pending |
| DASH-03 | Phase 6 | Pending |
| DASH-04 | Phase 6 | Pending |
| DASH-05 | Phase 6 | Pending |

**Coverage:**
- v1 requirements: 19 total
- Mapped to phases: 19
- Unmapped: 0 ✓

---
*Requirements defined: 2026-08-26*
*Last updated: 2026-08-26 after roadmap creation (6 phases, full v1 coverage)*
