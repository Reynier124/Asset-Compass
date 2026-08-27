# Feature Research

**Domain:** Multi-broker portfolio consolidation / net-worth aggregation dashboard (fintech data aggregation)
**Researched:** 2026-08-26
**Confidence:** MEDIUM (web sources cross-checked across multiple independent products; no primary API docs for Binance/IOL/Nexo connector UX specifically — those remain phase-specific gaps)

## Context Recap

This is the "Consolidation & visibility" milestone: three read-only connectors (Binance, IOL, Nexo), a unified cross-broker data model built on the existing `Operation`-as-source-of-truth / `Position`+`Valuation`-as-materialized-views pattern, and a BI dashboard. Alerts, security hardening, and trade execution are explicitly deferred — this document does not propose features from those buckets.

Comparable products researched: **Kubera** (net-worth tracker, banks+brokerages+crypto+real assets, $249/yr, no free tier), **Delta by eToro** (300+ exchange/broker/wallet sync, crypto+stocks+ETFs+forex), **CoinStats** (crypto-focused exchange aggregator with manual/CSV fallback), and **Plaid** (the connection-health/reliability UX reference point for account aggregation generally, not a consumer product but the industry-standard pattern for "connector honesty").

## Feature Landscape

### Table Stakes (Users Expect These)

Features users assume exist. Missing these = the dashboard feels broken or untrustworthy — directly relevant since this milestone's stated Core Value is "a unified, **accurate**, read-only view."

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| Per-broker connection status (connected / syncing / error / stale) | Every reviewed product (Kubera, Delta, CoinStats) and the Plaid reference pattern treat this as baseline; users need to know *which* number is trustworthy right now | LOW-MEDIUM | Surface at the `BrokerAccount` level: last successful sync timestamp, current status enum, human-readable error reason. Backend: add `lastSyncStatus`/`lastSyncedAt`/`lastSyncError` to `BrokerAccount` or a companion sync-log entity. |
| Distinct treatment of auth-expired vs transient-error vs partial-data failures | Plaid's ITEM_LOGIN_REQUIRED pattern: a re-auth-needed state is actionable by the user (re-enter API key / refresh IOL token), a transient 5xx is not — conflating them causes users to distrust the whole app | MEDIUM | Maps directly onto IOL's known 15-minute Bearer refresh flow and Nexo's "no official API, best-effort" reality already noted in PROJECT.md. Connector interface should return a typed sync outcome, not just success/exception. |
| "Data as of [timestamp]" staleness indicator on every consolidated view | Users expect fintech aggregators to be honest about staleness rather than silently showing old numbers as if current (Plaid finding: trust drops fast when this is hidden) | LOW | Show per-broker and overall "oldest data" timestamp on the dashboard header. Cheap to build given `lastSyncedAt` already exists as a table-stakes field above. |
| Unified position list across all brokers/asset classes in one table/view | This is the literal Core Value statement in PROJECT.md; Kubera and Delta both lead with "see everything in one place" | MEDIUM | Already partially modeled: `Position` entity exists per broker account; needs a cross-broker aggregation endpoint that groups/sums by `Asset` regardless of source `BrokerAccount`. |
| Base-currency normalization for all totals | Portfolio spans ARS (IOL), USD/USDT (Binance, Nexo) — without normalization, "total net worth" is meaningless; every reviewed product treats this as non-negotiable, not a nice-to-have | MEDIUM-HIGH | Requires an FX rate source and a defined normalization policy (see Dependencies below). This is the single biggest hidden-complexity item in this milestone — see Pitfalls research for detail. |
| Historical value chart (portfolio value over time) | Baseline expectation for any "BI dashboard" claim; Delta's "institutional-grade charts" and Kubera's net-worth-over-time chart are the category norm | MEDIUM | Depends on the scheduled snapshot job already listed as Active in PROJECT.md ("foundation for BI history"). Without daily snapshots there is no time series to chart. |
| Exposure/allocation breakdown (by asset class, by broker, by currency) | "Cornerstone of any investment dashboard" per research; answers "what do I actually own" which is the reason a consolidator exists at all | MEDIUM | Depends on currency normalization (can't meaningfully sum a BTC position and an ARS bond position without a common denominator) and on the unified position view. |
| Manual/CSV fallback for a connector that can't sync | CoinStats and Delta both treat this as essential, and it directly de-risks the milestone's own admitted uncertainty around Nexo ("no official public API, reverse-engineered, best-effort") | MEDIUM | Even a minimal manual position entry as a break-glass path prevents Nexo flakiness from making the whole dashboard look broken. Could be scoped down to "manually mark last-known-good Nexo balance" rather than full manual transaction entry. |
| Graceful partial-failure dashboard (2-of-3 brokers synced) | If one broker's connector is down, the other two should still render — the whole product must not go blank because Nexo (explicitly the riskiest connector) is unavailable | MEDIUM | Architectural requirement more than a "feature": aggregation endpoints must tolerate partial data and clearly label which portion is incomplete, rather than failing the whole request. |

### Differentiators (Competitive Advantage — Nice to Have This Milestone)

Not required to avoid feeling broken, but valuable and aligned with the "professional portfolio piece" half of the project's dual purpose. None of these should block milestone completion.

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| Time-Weighted Return (TWRR) alongside simple gain/loss | Research finding: TWRR isolates portfolio/asset performance from the timing of deposits — meaningful once operations flow in at different times per broker. Most competitor products stop at simple P&L; doing TWRR is a step above table stakes | HIGH | Needs a correct cash-flow-aware calculation on top of the `Operation` ledger; genuinely differentiating both for the personal-utility and portfolio-piece goals, but real effort — good candidate for its own sub-phase rather than bundled into "BI Dashboard." |
| Money-Weighted Return (MWRR/IRR) shown alongside TWRR | Research: presenting only TWRR *or* only simple return is "misleading" for a multi-broker consolidator; showing both signals sophistication | HIGH | Same dependency as TWRR (accurate operation timestamps + cash flow classification). Bundle these two together if pursued — doing one without the other is architecturally almost the same cost. |
| Per-broker vs. consolidated performance comparison | Lets the user see "is my Binance account or my IOL account actually doing better," a natural extension once normalization + TWRR exist | MEDIUM | Pure aggregation-layer feature once the return calculations exist; low incremental cost after TWRR/MWRR land. |
| Cost-basis / unrealized gain tracking normalized to base currency | Research: correct multi-currency cost basis (convert at trade-date rate, not display-time) is what separates "toy tracker" from "trustworthy tool" — directly reinforces the personal-utility goal | MEDIUM-HIGH | The domain already has `Operation` as immutable truth, which is the right foundation; this is mostly a calculation-layer feature once currency normalization policy is settled. |
| DeFi/staking or NFT support (Kubera-style) | Category leader (Kubera) treats this as a headline feature | HIGH | Out of proportion to this milestone's three named brokers (Binance, IOL, Nexo) — none of which specifically require DeFi wallet tracking. Defer indefinitely unless a broker requires it. |
| CSV export of consolidated data | Common in Delta/CoinStats-tier products, low cost, good demo value for the "portfolio piece" side of the dual purpose | LOW | Cheap add-on once the unified view exists; good candidate to tack onto the BI Dashboard phase if time allows, not a blocker. |
| Real-time (sub-minute) sync via webhooks/streaming | Delta/Kubera advertise "real-time" sync as a premium differentiator | HIGH | Binance has WebSocket APIs; IOL and Nexo do not offer equivalent streaming in a way that's reliably documented. Given this milestone's already-heavy scope (3 connectors + unified model + dashboard), scheduled polling (already implied by the "scheduled snapshot job") is the right MVP; real-time is a legitimate v2 differentiator, not now. |

### Anti-Features (Explicitly Out of Scope for This Milestone)

Features that seem natural for a "portfolio dashboard" category but are deliberately excluded per PROJECT.md's own phase roadmap, or that create risk disproportionate to this milestone's read-only, consolidation-focused goal.

| Feature | Why Requested | Why Problematic (for this milestone) | Alternative |
|---------|---------------|------------------------------------|-------------|
| Price/performance alerts (email, Telegram, push) | Natural pairing with a dashboard — "notify me when X happens" is the obvious next feature every reviewed competitor (Delta, CoinStats) ships | Explicitly Phase 2 of the project's own roadmap; building it now duplicates future `notificationService` work and pulls focus from getting the connectors/data model right first | Defer to the dedicated Alerts milestone; this milestone should expose the aggregation data that a future rules engine will consume, nothing more |
| Trade execution / rebalancing suggestions / one-click buy-sell | Category-adjacent products (Delta, eToro) blur tracker and broker; "just add a trade button" looks like small scope | Explicitly Phase 4, explicitly gated behind Phase 3 security hardening; real-money safety (dry-run, manual confirmation, hard limits) is non-negotiable and has not been built | Keep this milestone strictly read-only; do not add any write-back-to-broker capability, even "convenience" ones like auto-syncing manual trades into the broker |
| Storing/managing broker credentials beyond what's minimally needed for each connector's auth flow (e.g. building a full secrets vault UI) | Feels responsible to build "properly" now | Explicitly Phase 3 ("Security & QA hardening... deferred, but must precede any real-money operation") — over-building security infra now is scope creep against the milestone's own boundary, and read-only credentials carry materially lower risk than trading credentials | Use adequate-for-read-only credential handling now (e.g., encrypted at rest, least-privilege read-only API keys where the broker supports scoping); treat a dedicated secrets-management overhaul as Phase 3's job |
| Full transaction-level audit log / compliance reporting (tax forms, 8949-style exports) | "Tax event" entity already exists in the domain model, so it's tempting to build full tax reporting | Disproportionate scope for a consolidation-and-visibility milestone; tax reporting is a distinct feature domain (jurisdiction rules, forms) not implied by "unified view" | `TaxEvent` entity can continue capturing raw data as operations are ingested; defer building actual tax *reports* to a future milestone if ever prioritized |
| Social/sharing features (public portfolio links, leaderboards) | Some trackers (eToro-adjacent) lean into social investing | Zero alignment with either stated goal (personal utility or engineering portfolio piece); pure scope creep | Do not build; not on the project's own roadmap at all |
| Building a generic "connect any broker" plugin marketplace | Feels like good architecture ("make it extensible!") | Over-engineering for 3 known, named brokers; premature abstraction beyond what the existing `BrokerConnector` interface already provides for exactly this purpose | The `BrokerConnector` interface (already a confirmed architecture decision) is sufficient extensibility — don't build discovery/marketplace/dynamic-plugin-loading machinery for it |
| DeFi wallet / NFT tracking | Category leader (Kubera) does this | None of Binance/IOL/Nexo require it; adds an entirely new asset-class taxonomy for no named use case | Skip entirely for this milestone; revisit only if a future broker addition requires it |

## Feature Dependencies

```
Base-currency FX normalization
    └──requires──> Unified position/valuation across brokers
                       └──requires──> All 3 connectors ingesting into common Operation model
                                          └──requires──> Binance connector, IOL connector, Nexo connector (each independently)

Exposure/allocation breakdown charts
    └──requires──> Base-currency FX normalization
    └──requires──> Unified position/valuation across brokers

Historical value time-series chart
    └──requires──> Scheduled snapshot job (Valuation history)
    └──requires──> Base-currency FX normalization (or charts are per-currency, less useful)

TWRR / MWRR performance metrics
    └──requires──> Base-currency FX normalization
    └──requires──> Accurate Operation timestamps + cash-flow classification (deposit vs trade vs income)
    └──requires──> Historical value time-series (need periodic valuations, not just point-in-time)

Per-broker connection status / staleness indicator
    └──requires──> Sync outcome typed at the BrokerConnector interface level (not raw exceptions)
    └──enhances──> Graceful partial-failure dashboard rendering

Manual/CSV fallback for a broker
    └──enhances──> Nexo connector reliability (mitigates "no official API, best-effort" risk)
    └──conflicts with──> "Operation as immutable source of truth" purity, unless manual entries are modeled as a distinct OperationSource (MANUAL) rather than bypassing the ledger — must be designed in, not bolted on

Alerts (Phase 2, out of scope)
    └──requires──> Unified position/valuation across brokers (this milestone's output is Phase 2's input)

Trade execution (Phase 4, out of scope)
    └──requires──> Security hardening (Phase 3, out of scope)
    └──requires──> Unified data model + connectors (this milestone)
```

### Dependency Notes

- **Currency normalization is the load-bearing dependency for almost everything else.** Exposure charts, allocation breakdowns, and any cross-broker "total" are meaningless without it. It must be designed (base currency choice, FX rate source, conversion timing policy — see PITFALLS.md) before the BI Dashboard phase starts, ideally as part of the "Unified data model & persistence" phase already in PROJECT.md's Active list.
- **Connection status/staleness must be a first-class part of the connector interface, not an afterthought bolted onto the dashboard.** If `BrokerConnector` implementations just throw exceptions on failure, retrofitting a status taxonomy later means touching all three connectors again. Design the typed sync-outcome contract (SUCCESS / AUTH_EXPIRED / TRANSIENT_ERROR / PARTIAL) when building the Binance connector (first one), so IOL and Nexo inherit the same contract.
- **TWRR/MWRR enhance but do not block the MVP dashboard.** A simple "current value minus cost basis" gain/loss is acceptable table-stakes; the more sophisticated return metrics are differentiators layered on afterward once the historical snapshot data exists to compute them meaningfully.
- **Manual/CSV fallback conflicts with the "Operation is immutable source of truth" architectural decision** unless deliberately designed as a `source: MANUAL` operation rather than a side-channel override of `Position`/`Valuation`. If pursued, this must go through the same ledger, just with a different origin, to avoid the "materialized view drifts from source of truth" anti-pattern already flagged in PROJECT.md.
- **Graceful partial failure is a cross-cutting requirement**, not a single feature — it must be built into every aggregation endpoint from day one (Binance connector), since Nexo (the connector most likely to fail, per PROJECT.md's own risk note) ships last but the dashboard must not silently regress to "broken" whenever it does.

## MVP Definition (This Milestone's Scope)

### Launch With (v1 — required to close "Consolidation & visibility")

- [ ] Per-broker connection status + last-synced timestamp, visible per `BrokerAccount` — without this, users can't trust any number on the dashboard
- [ ] Typed sync outcomes (success / auth-expired / transient-error / partial) at the `BrokerConnector` interface level — cheapest to build now, expensive to retrofit
- [ ] Graceful partial-failure aggregation (dashboard renders with 2-of-3 brokers if one is down) — directly de-risks the admittedly-flaky Nexo connector
- [ ] Unified cross-broker position list, normalized to a single base currency — this *is* the Core Value statement
- [ ] Base-currency FX normalization policy (rate source, conversion timing) applied consistently to totals, allocation, and cost basis — the single highest-leverage design decision this milestone
- [ ] Exposure/allocation breakdown by asset class, broker, and currency — the "cornerstone" of any BI dashboard claim
- [ ] Historical portfolio value chart, backed by the scheduled snapshot job — minimum bar for "BI dashboard," not just a live-balance page

### Add After Core Validated (still within this milestone, if time allows)

- [ ] Manual/CSV fallback entry, scoped narrowly to Nexo (the connector most likely to need it) — add once the three real connectors are proven, not before
- [ ] CSV export of the consolidated view — cheap, good demo value for the "portfolio piece" goal
- [ ] Simple gain/loss (not yet TWRR/MWRR) per position and per broker

### Future Consideration (next milestone or later)

- [ ] TWRR and MWRR performance metrics — defer until historical snapshots have accumulated enough data points to be meaningful, and until the simpler gain/loss view is validated
- [ ] Per-broker performance comparison — natural follow-on to TWRR/MWRR
- [ ] Real-time/streaming sync (vs. scheduled polling) — legitimate differentiator, disproportionate effort for this milestone
- [ ] DeFi/NFT tracking, social features, plugin marketplace — not aligned with named brokers or either stated project goal; do not build

## Feature Prioritization Matrix

| Feature | User Value | Implementation Cost | Priority |
|---------|------------|---------------------|----------|
| Per-broker connection status / staleness | HIGH | LOW-MEDIUM | P1 |
| Typed connector sync outcomes | HIGH | MEDIUM | P1 |
| Graceful partial-failure dashboard | HIGH | MEDIUM | P1 |
| Unified cross-broker position view | HIGH | MEDIUM | P1 |
| Base-currency FX normalization | HIGH | MEDIUM-HIGH | P1 |
| Exposure/allocation breakdown | HIGH | MEDIUM | P1 |
| Historical value time-series chart | HIGH | MEDIUM | P1 |
| Manual/CSV fallback (Nexo-scoped) | MEDIUM | MEDIUM | P2 |
| CSV export | LOW-MEDIUM | LOW | P2 |
| Simple gain/loss per position | MEDIUM | LOW-MEDIUM | P2 |
| TWRR / MWRR | MEDIUM-HIGH | HIGH | P3 |
| Per-broker performance comparison | MEDIUM | MEDIUM | P3 |
| Real-time streaming sync | LOW-MEDIUM | HIGH | P3 |
| DeFi/NFT/social/marketplace | LOW (not aligned) | HIGH | Reject |

**Priority key:**
- P1: Must have to close this milestone credibly
- P2: Should have if time allows within this milestone
- P3: Nice to have, next milestone or later

## Competitor Feature Analysis

| Feature | Kubera | Delta (eToro) | CoinStats | Our Approach |
|---------|--------|---------------|-----------|--------------|
| Multi-broker/exchange sync | 20,000+ banks/brokerages + major crypto exchanges + DeFi | 300+ exchanges/brokers/wallets | Crypto exchanges + wallets, custom-exchange connect flow | 3 named connectors only (Binance, IOL, Nexo) — scope is intentionally narrow, not "connect anything" |
| Connection error handling | Not detailed in research, but real-time sync implies active monitoring | Not detailed; auto-sync framed as "just works" | Explicit guidance: check credentials, retry, fallback to manual/CSV, support escalation | Adopt CoinStats' fallback pattern + Plaid's typed-status pattern (best of both): visible status + manual fallback for the flakiest connector (Nexo) |
| Multi-currency | Native multi-currency support | Implied (multi-asset, no explicit FX detail found) | Not a focus (crypto-centric) | First-class requirement — IOL is ARS, Binance/Nexo are USD/crypto; must normalize from day one, not bolt on later |
| Performance metrics | Net worth over time (implied) | Gains/losses, asset allocation, "Portfolio Insights" advanced metrics | Portfolio-level P&L | Simple gain/loss for v1 (P1); TWRR/MWRR as a differentiator once historical data exists (P3) |
| Manual fallback for failed connections | Not detailed | CSV import + manual transactions | Manual entry + CSV import, explicitly documented as fallback | Scoped manual fallback for Nexo specifically, modeled as a MANUAL-source Operation, not a side-channel |
| Pricing/access model | Paid only, $249/yr, no free tier | Freemium (implied by consumer app model) | Freemium | Not applicable — personal-use single-tenant app, not a commercial product this milestone |

## Sources

- [Kubera Net Worth Tracker](https://www.kubera.com/net-worth-tracker)
- [The Multi Currency Portfolio Tracker: Kubera](https://www.kubera.com/blog/multi-currency-portfolio-tracker)
- [Kubera Review — Jean Galea](https://jeangalea.com/kubera-review/)
- [Delta by etoro — Features](https://delta.app/en/features)
- [Delta by etoro — Effortlessly Sync & Link Your Portfolio](https://delta.app/en/features/link)
- [Delta by etoro — Track Crypto & Connect Wallets](https://delta.app/en/crypto-tracker)
- [CoinStats — Connect Your Crypto Exchanges and Wallets](https://coinstats.app/connect-portfolio/)
- [CoinStats — How to Connect Custom Exchange](https://coinstats.app/connect/any-exchange/)
- [Plaid Docs — Link: Institution status in Link](https://plaid.com/docs/link/institution-status/)
- [Plaid Docs — Errors: Item errors](https://plaid.com/docs/errors/item/)
- [Plaid — Item Debugger and Institution Status improvements](https://plaid.com/blog/item-debugger-institution-status/)
- [Multi-Currency Portfolio Tracking: How to Measure Performance Without Mixing Up FX and Returns](https://blog.deepdigitalventures.com/multi-currency-portfolio-tracking-how-to-measure-performance-without-mixing-up-fx-and-returns/)
- [Multi-Currency Portfolio Tracking Guide](https://www.allinvestview.com/articles/multi-currency-portfolio-guide/)
- [Wealthfolio — Market Data & FX](https://wealthfolio.app/docs/concepts/market-data-and-fx/)
- [The Mathematics of Portfolio Return: Simple, Money-Weighted, Time-Weighted](https://portfoliooptimizer.io/blog/the-mathematics-of-portfolio-return-simple-return-money-weighted-return-and-time-weighted-return/)
- [Time-weighted vs. money-weighted rates of return — Sharesight](https://www.sharesight.com/blog/time-weighted-vs-money-weighted-rates-of-return/)
- [Time-Weighted Return vs Money-Weighted Return — Universal Asset Owners](https://www.universalassetowners.com/time-weighted-return-vs-money-weighted-return/)
- Project context: `/home/lucianotoneatti/Proyectos-CC/Asset-Compass/.planning/PROJECT.md`, `/home/lucianotoneatti/Proyectos-CC/Asset-Compass/.planning/codebase/ARCHITECTURE.md`

---
*Feature research for: multi-broker portfolio consolidation dashboard (Asset Compass, "Consolidation & visibility" milestone)*
*Researched: 2026-08-26*
