# Architecture Research

**Domain:** Read-only multi-broker portfolio aggregation (adapter-pattern connectors + scheduled polling + BI aggregation), Spring Boot microservice
**Researched:** 2026-08-26
**Confidence:** MEDIUM-HIGH (hexagonal/ports-and-adapters and Resilience4j patterns are well-established, cross-checked across multiple independent sources = MEDIUM per source hierarchy; the Operation-vs-Snapshot data-modeling call below is project-specific reasoning, not a sourced fact — flagged separately)

## Standard Architecture

### System Overview

```
┌───────────────────────────────────────────────────────────────────────────┐
│                     portfolioService (hexagonal-lite)                     │
│                                                                             │
│  application/                                                             │
│  ┌────────────────────────┐   ┌───────────────────────────────────────┐  │
│  │ SnapshotSchedulerJob    │   │ PortfolioAggregationService (BI)      │  │
│  │ (@Scheduled, 1 per      │   │ reads Position/Valuation across all   │  │
│  │  broker, own cadence)   │   │ BrokerAccounts → BI DTOs              │  │
│  └───────────┬─────────────┘   └───────────────────┬───────────────────┘  │
│              ▼                                      ▲                     │
│  ┌────────────────────────┐                         │                     │
│  │ SnapshotOrchestrationSvc│─────────────────────────┘                     │
│  │ loops enabled connectors,│  persists normalized snapshot →             │
│  │ isolates failures per    │  Position/Valuation (upsert)                │
│  │ broker                   │                                             │
│  └───────────┬─────────────┘                                             │
│              │ depends on (interface only)                                │
│              ▼                                                            │
│  domain/port/  BrokerConnector  (fetchSnapshot(BrokerAccount) → Snapshot) │
│              ▲          ▲          ▲                                     │
│  ────────────┼──────────┼──────────┼──────── hexagon boundary ───────────│
│              │          │          │                                     │
│  infrastructure/connector/                                                │
│  ┌───────────┴──┐ ┌─────┴──────┐ ┌─┴───────────┐                         │
│  │ Binance      │ │ Iol        │ │ Nexo        │  ← implements the port  │
│  │ Connector    │ │ Connector  │ │ Connector   │                         │
│  │ (key/secret, │ │ (bearer +  │ │ (unofficial,│                         │
│  │  HMAC sign)  │ │  15-min    │ │  tolerant   │                         │
│  │              │ │  refresh)  │ │  parsing)   │                         │
│  └──────┬───────┘ └─────┬──────┘ └─────┬───────┘                         │
│         │ RestClient +   │ RestClient + │ RestClient +                   │
│         │ Resilience4j   │ Resilience4j │ Resilience4j (tighter          │
│         │ (RateLimiter,  │ (RateLimiter,│  CircuitBreaker — flaky)       │
│         │  CircuitBreaker│  Retry,      │                                │
│         │  matched to    │  token cache)│                                │
│         │  published     │              │                                │
│         │  weight limit) │              │                                │
└─────────┼───────────────┼──────────────┼─────────────────────────────────┘
          ▼               ▼              ▼
  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
  │ Binance API  │ │  IOL API     │ │  Nexo (no    │
  │ (official)   │ │  (official)  │ │  official API)│
  └──────────────┘ └──────────────┘ └──────────────┘
```

### Component Responsibilities

| Component | Responsibility | Typical Implementation |
|-----------|----------------|------------------------|
| `BrokerConnector` (port) | Single method contract: given a `BrokerAccount`, return a normalized snapshot or throw a typed exception. No auth-specific methods on the interface. | Interface in `domain/port/`, zero Spring/HTTP imports |
| Per-broker connector (adapter) | Implements the port; owns everything broker-specific: HTTP client, auth (key/secret signing, bearer refresh, scraping), response parsing, retry/rate-limit decoration | `infrastructure/connector/{binance,iol,nexo}/*Connector.java` |
| `SnapshotOrchestrationService` | Iterates all enabled `BrokerConnector` beans, calls each independently, catches/logs per-broker failures so one broker never blocks another, persists results | `application/service/` |
| `SnapshotSchedulerJob` | Triggers orchestration on a schedule; ideally one trigger per broker (different rate limits / reliability profiles) rather than one global cron for all three | `application/scheduler/`, `@Scheduled` |
| Normalized snapshot model | Broker-agnostic DTO (balances/positions by asset, as-of timestamp, source broker id) — the single shape all three adapters must produce | `domain/model/snapshot/BrokerSnapshot.java` |
| `PortfolioAggregationService` (BI) | Reads persisted `Position`/`Valuation` across all `BrokerAccount`s, computes consolidated performance, exposure by currency/asset, historical evolution | `application/service/` |
| Resilience wrapper per client | Rate limiter matched to each broker's published/observed limit, circuit breaker isolating a flaky broker (Nexo) from healthy ones, retry with backoff for transient errors | Resilience4j annotations or `Decorators` builder, one named instance per broker in config |

## Recommended Project Structure

Extending the existing `hexagonal-lite` package already listed as an active requirement in PROJECT.md:

```
portfolioService/src/main/java/com/assetcompass/portfolio/
├── domain/
│   ├── model/                    # existing: Asset, Operation, Position, Valuation, BrokerAccount, ...
│   │   └── snapshot/             # NEW: BrokerSnapshot, NormalizedBalance, NormalizedPosition
│   └── port/                     # NEW: BrokerConnector (outbound port), BrokerConnectorException
├── application/
│   ├── service/                  # NEW: SnapshotOrchestrationService, PortfolioAggregationService
│   └── scheduler/                # NEW: SnapshotSchedulerJob (per-broker @Scheduled methods/beans)
├── infrastructure/
│   ├── connector/
│   │   ├── binance/              # BinanceConnector, BinanceHttpClient, BinanceHmacSigner, BinanceResponseMapper
│   │   ├── iol/                  # IolConnector, IolTokenManager (owns 15-min refresh + cache), IolResponseMapper
│   │   └── nexo/                 # NexoConnector, NexoHttpClient (tolerant/defensive), NexoResponseMapper
│   ├── persistence/               # existing repository/ (JPA) — snapshot persistence lands here too
│   ├── web/rest/                  # existing REST controllers + new BI aggregation endpoints
│   └── config/                    # NEW: Resilience4jConfig (per-broker instances), SchedulerConfig, RestClient beans
├── repository/                    # existing (kept as-is if not yet renamed under infrastructure/persistence)
├── service/                       # existing CRUD/QueryService layer — untouched by this milestone
└── web/rest/                      # existing REST controllers
```

### Structure Rationale

- **`domain/port/`:** Owns the `BrokerConnector` interface. This package must have zero dependency on Spring, HTTP clients, or any broker SDK — that's what makes it mockable/fakeable without WireMock or Spring context.
- **`domain/model/snapshot/`:** The normalized shape is domain vocabulary, not infrastructure vocabulary. It must not contain Binance/IOL/Nexo field names or nesting — that leakage is the single most common hexagonal-lite violation to watch for in code review.
- **`infrastructure/connector/{broker}/`:** Everything broker-specific — including auth — lives here, one subpackage per broker. This is where WireMock-mocked tests target the HTTP boundary per PROJECT.md's stated test strategy.
- **`application/`:** New for this milestone. This is genuinely new orchestration logic (loop-and-isolate over connectors, BI reads) that doesn't fit cleanly in the existing `service/` (CRUD) layer — separating it avoids overloading `service/` with two very different responsibilities.

## Architectural Patterns

### Pattern 1: Adapter Pattern with Auth Encapsulated Inside the Adapter

**What:** `BrokerConnector` has exactly one method (`fetchSnapshot(BrokerAccount) → BrokerSnapshot`). Auth mechanics (API key/secret signing, bearer token + 15-min refresh, unofficial-API session handling) are never exposed on the interface — each adapter manages its own auth internally, as a private collaborator (e.g., `IolTokenManager` held by `IolConnector`).

**When to use:** Whenever you're normalizing N external providers with different auth schemes behind one contract, and callers (the orchestration service) must not need to know or care which auth style is in play.

**Trade-offs:** Keeps the port trivially simple and stable across all three brokers — the crucial win, since a stable port lets you swap/add brokers without touching orchestration or the BI layer. Cost: each adapter carries more internal complexity (e.g., IOL's connector must handle "is my cached token still valid, refresh if not, retry the call once" as an internal concern) — but that complexity is broker-specific by nature and would leak elsewhere if not contained here.

**Example:**
```java
// domain/port/BrokerConnector.java — no Spring, no HTTP imports
public interface BrokerConnector {
    BrokerId brokerId();
    BrokerSnapshot fetchSnapshot(BrokerAccount account) throws BrokerConnectorException;
}

// infrastructure/connector/iol/IolConnector.java
@Component
public class IolConnector implements BrokerConnector {
    private final IolHttpClient httpClient;
    private final IolTokenManager tokenManager; // owns the 15-min refresh, invisible to the port

    public BrokerSnapshot fetchSnapshot(BrokerAccount account) {
        String token = tokenManager.getValidToken(account); // refreshes internally if expired
        var raw = httpClient.getPositions(account, token);
        return IolResponseMapper.toSnapshot(raw, account);
    }
}
```

### Pattern 2: Per-Broker Resilience4j Instances (Not One Shared Config)

**What:** Each broker gets its own named `RateLimiter`, `CircuitBreaker`, and `Retry` instance, tuned to that broker's actual limits and reliability (Binance: generous official weight-based limit; IOL: moderate, official; Nexo: conservative, since it's unofficial and more likely to break or throttle silently).

**When to use:** Any time you integrate multiple external APIs with materially different rate limits/reliability inside the same service — a single shared limiter either starves the fast broker or lets the flaky one blow through its real limit.

**Trade-offs:** More config entries to maintain (one block per broker), but isolates failure domains correctly: Nexo's circuit tripping open never throttles Binance/IOL calls, which matters directly for the scheduled-job design below.

**Example:**
```yaml
resilience4j:
  ratelimiter:
    instances:
      binance: { limitForPeriod: 1200, limitRefreshPeriod: 1m, timeoutDuration: 5s }
      iol:     { limitForPeriod: 60,   limitRefreshPeriod: 1m, timeoutDuration: 5s }
      nexo:    { limitForPeriod: 20,   limitRefreshPeriod: 1m, timeoutDuration: 5s }
  circuitbreaker:
    instances:
      nexo: { failureRateThreshold: 40, waitDurationInOpenState: 5m, slidingWindowSize: 10 }
```

### Pattern 3: Isolated-Failure Orchestration Loop, Per-Broker Scheduling

**What:** The scheduler does not run one global job that fans out synchronously to all three connectors and aborts on first exception. Instead, either (a) one `@Scheduled` trigger per broker calling that broker's connector directly, or (b) one orchestration entry point that iterates `List<BrokerConnector>` and wraps each call in its own try/catch, logging and persisting partial results even when one broker fails.

**When to use:** Always, once you have ≥2 external dependencies with independent failure modes in a scheduled batch job — this is the single most important pitfall to design against up front (see Anti-Patterns below).

**Trade-offs:** Slightly more code than a naive `for` loop with no exception handling, but the alternative silently loses Binance/IOL data every time Nexo (the least reliable of the three) has a bad day — unacceptable for a BI product whose entire value proposition is consolidated, reliable history.

## Data Flow

### Snapshot Ingestion Flow (Read Path Into the System)

```
External Broker API (Binance / IOL / Nexo)
    ↓  broker-specific HTTP call, broker-specific auth (key/secret | bearer+refresh | unofficial)
Infrastructure Adapter (BinanceConnector / IolConnector / NexoConnector)
    ↓  maps raw JSON → BrokerSnapshot (domain vocabulary: balances/positions by asset, as-of timestamp)
SnapshotOrchestrationService (application layer)
    ↓  per-broker try/catch, isolates failures, upserts by (brokerAccountId, asOfTimestamp)
Persistence: Position / Valuation (existing materialized-view entities)
    ↓  read-side, no additional transform needed for simple totals
PortfolioAggregationService (application layer, BI)
    ↓  consolidated performance, exposure by currency/asset, historical evolution
REST BI endpoints (web/rest)
    ↓
Gateway (reverse proxy, JWT validation) → React/TypeScript PWA (charts)
```

### Key Data Flows

1. **Polling → normalize → persist:** Each broker's raw API shape never crosses the `infrastructure/connector/{broker}` boundary. The mapper inside each adapter is the single translation point (anti-corruption layer) — this is what keeps three very different external APIs from bleeding broker-specific quirks into `Position`/`Valuation`.
2. **BI read path:** The aggregation layer never talks to `BrokerConnector` directly — it only reads already-persisted `Position`/`Valuation` rows. This decouples "is Nexo up right now" from "can the user see their dashboard right now" — the dashboard should always render off the last successful snapshot, not fail because a live broker call failed.

### Genuinely Open Design Question: Where Do Observed Balances Fit the Operation/Position/Valuation Model?

PROJECT.md's confirmed architecture states `Operation` is the immutable source of truth, with `Position`/`Valuation` as materialized views *derived from* operations. Broker polling, however, does not observe individual buy/sell operations — Binance/IOL/Nexo "balance" endpoints return **current state**, not an event log (Binance's REST API can expose trade history too, but IOL and especially the unofficial Nexo integration realistically only expose current holdings).

This is a fork worth deciding explicitly before building the unified data model, not discovering mid-connector-work:

- **Recommended for this milestone:** Treat each broker snapshot as a **directly observed `Valuation`/`Position`** write (upsert keyed by `brokerAccountId` + `asOfTimestamp`), bypassing `Operation` entirely for connector-sourced data. This matches the Active requirement's own phrasing — "scheduled snapshot job (foundation for BI history)" — which describes an observation cadence, not an event derivation. It's also strictly simpler and ships faster.
- **Explicitly deferred, not attempted now:** Reconstructing synthetic `Operation` rows from broker trade history (only some brokers even expose this) to preserve full event-sourcing purity. This is a legitimate future enhancement per-broker (Binance's trade history endpoint would support it; Nexo's likely wouldn't), but doing it now would gate all three connectors on the hardest case and isn't needed for the BI dashboard goal.
- **Implication for `Operation`'s "immutable source of truth" invariant:** That invariant continues to hold for user-entered/manually-recorded operations (the current CRUD API). Connector-sourced `Position`/`Valuation` rows become a second, independent input to those same tables, tagged by source (`brokerAccountId`) — not a violation of the invariant, but it does mean `Position`/`Valuation` are no longer *purely* derived from `Operation` once connectors ship. Worth a one-line ADR when this phase is planned, so it isn't re-litigated per-connector.

## Scaling Considerations

This is a personal-use tool (single user, three broker accounts), so classic user-count scaling is not the relevant axis. What actually breaks first:

| Concern | Current scale (1 user, 3 brokers) | If broker count grows (5-10) | If polling frequency increases (near real-time) |
|---------|-----------------------------------|-------------------------------|--------------------------------------------------|
| External rate limits | Each broker's own limit is generous at low polling frequency (e.g. hourly) | Still fine — connectors are independent, no shared limiter | Binance/IOL limits become the real constraint; Nexo (unofficial) breaks first under any aggressive polling — throttle it hardest |
| Job overlap / concurrency | Single instance, `@Scheduled` is sufficient, no locking needed | Same — connectors run independently, no contention | Consider non-blocking/reactive HTTP clients if synchronous polling starts taking longer than the schedule interval |
| Multi-instance scheduling | N/A — single pod | If the service is ever scaled to >1 replica, `@Scheduled` without a distributed lock will double-fire jobs | Add ShedLock (or equivalent) only if/when replica count > 1 — premature before then |

### Scaling Priorities

1. **First real constraint:** Nexo's unofficial/reverse-engineered nature — not user count, not database size. Its circuit breaker and conservative rate limit are the actual bottleneck to design around now.
2. **Second, much later:** If `notificationService`/`tradingService` come online and this service scales to multiple replicas, add a distributed scheduler lock (ShedLock) before that point — not needed today with a single active replica.

## Anti-Patterns

### Anti-Pattern 1: One Global Scheduled Job That Fails Fast on the First Broker Error

**What people do:** A single `@Scheduled` method loops `for (connector : connectors) { connector.fetchSnapshot(...) }` with no per-iteration exception handling — an exception from Nexo aborts the whole run, silently dropping the Binance/IOL snapshots for that cycle too.

**Why it's wrong:** Nexo is explicitly the least reliable of the three (unofficial API). Coupling all three brokers' data freshness to Nexo's uptime defeats the entire point of "consolidated, reliable view."

**Do this instead:** Isolate each connector call in its own try/catch inside the orchestration service (or use independent `@Scheduled` triggers per broker), log/metric each failure individually, and persist whatever snapshots did succeed.

### Anti-Pattern 2: Auth Leaking Into the Port Interface

**What people do:** Add methods like `refreshToken()`, `getApiKey()`, or auth-scheme-specific parameters to `BrokerConnector` "for convenience," or have the orchestration service manage each broker's token lifecycle.

**Why it's wrong:** Immediately couples the orchestration/application layer to three different auth schemes, defeats the purpose of a common port, and makes adding a fourth broker with yet another auth scheme require touching the port and every existing caller.

**Do this instead:** Keep auth entirely inside each adapter as a private collaborator (see Pattern 1). The port stays a one-method contract regardless of how many brokers or auth schemes exist behind it.

### Anti-Pattern 3: Broker-Specific DTOs Reaching the Domain or Persistence Layer

**What people do:** Map Binance's raw JSON response directly to `Position`/`Valuation` entities, or pass Binance-shaped objects into `PortfolioAggregationService`.

**Why it's wrong:** Any change to Binance's API response shape now risks breaking domain logic or persistence; the domain model becomes implicitly coupled to whichever broker happened to be implemented first.

**Do this instead:** Each connector's mapper (`BinanceResponseMapper`, etc.) is the only place broker-specific shapes exist; it always outputs the shared `BrokerSnapshot` domain type. This is exactly the anti-corruption-layer responsibility that makes the adapter pattern worth the extra ceremony.

### Anti-Pattern 4: Mocking the Port Interface in Infrastructure Tests (or the HTTP Client in Domain/Application Tests)

**What people do:** Write `SnapshotOrchestrationServiceTest` using WireMock, or write `BinanceConnectorTest` using a hand-rolled fake `BrokerConnector`.

**Why it's wrong:** Backwards test doubles at the wrong layer — WireMock is expensive and irrelevant for testing orchestration logic (which only needs a fake/mock `BrokerConnector`); a fake connector tells you nothing about whether `BinanceConnector` actually parses Binance's real response shape or handles its real HTTP errors.

**Do this instead (also answers the testability question directly):**
- **`application/` tests** (`SnapshotOrchestrationServiceTest`, `PortfolioAggregationServiceTest`): plain JUnit + Mockito, inject fake/mock `BrokerConnector` implementations (in-memory, return canned `BrokerSnapshot`s or throw `BrokerConnectorException`). No Spring context, no HTTP, fast (matches the "domain tests run as plain JUnit with no Spring context" pattern already used for `service/`).
- **`infrastructure/connector/{broker}/` tests** (`BinanceConnectorIT`, `IolConnectorIT`, `NexoConnectorIT`): WireMock stubs standing in for the real broker API, per PROJECT.md's stated CI strategy ("CI never hits the real API"). These tests verify HTTP mapping, auth header/signing construction, retry-on-5xx, and the mapper's normalization logic — nothing about orchestration.
- This split is the direct payoff of putting the hexagonal boundary at `BrokerConnector`: two independent, fast, deterministic test suites instead of one slow suite that has to fake both HTTP and orchestration simultaneously.

## Integration Points

### External Services

| Service | Integration Pattern | Notes |
|---------|---------------------|-------|
| Binance | REST, API key + HMAC-SHA256 request signing, official documented weight-based rate limit | Cleanest of the three — build first as the reference implementation that proves the `BrokerConnector` contract and the WireMock test pattern |
| IOL (InvertirOnline) | REST, OAuth-style bearer token with ~15-minute expiry, official | Requires an internal token cache + proactive/reactive refresh inside `IolConnector`; validates that the port doesn't need to change to accommodate stateful auth |
| Nexo | Unofficial/reverse-engineered, no public API contract | Build last; needs the most tolerant parsing (missing/renamed fields should degrade gracefully, not throw), the tightest circuit breaker, and explicit fallback (e.g., return last-known snapshot or a clearly-flagged "unavailable" result rather than propagating raw HTTP failures) |

### Internal Boundaries

| Boundary | Communication | Notes |
|----------|---------------|-------|
| `application/` ↔ `domain/port` | Direct interface call (in-process) | No HTTP, no Kafka — this is a same-JVM dependency inversion, not a service boundary |
| `SnapshotOrchestrationService` ↔ `infrastructure/connector/*` | Direct call through the `BrokerConnector` interface, decorated with Resilience4j at the adapter | Failure isolation happens here, not at the port |
| `portfolioService` snapshot job ↔ Keycloak/Kafka | **None required.** The connectors make outbound calls to external broker APIs using their own stored credentials — they do not need inbound OAuth2 validation, and the snapshot job doesn't need Kafka to function correctly for this milestone. | Directly relevant to the open Keycloak/Kafka-in-local-Compose question: the connector + scheduling + BI work in this milestone can be built and tested (WireMock, plain JUnit) without Keycloak or Kafka running locally at all. Kafka only becomes relevant if/when `notificationService` needs to react to new snapshots — that's Phase 2, not this milestone. This is an architectural fact this research surfaced, not a decision — it narrows, but doesn't fully resolve, the open question, since the REST API layer (`web/rest`) is separately scaffolded to expect OAuth2 regardless of what the connector layer itself needs. |
| `PortfolioAggregationService` ↔ `web/rest` BI endpoints | Direct call, synchronous | Standard existing layered pattern (controller → service), no change from current architecture |

## Sources

- [Transforming a Traditional Spring Boot App into a Hexagonal Architecture](https://eliedhr.medium.com/transforming-a-traditional-spring-boot-app-into-a-hexagonal-architecture-0040b85add57) — ports/adapters package split, dependency-inversion rule (MEDIUM confidence, cross-checked against 3+ independent sources)
- [Hexagonal Architecture in Spring Boot — The Ultimate Guide](https://medium.com/@shubhamvartak01/hexagonal-architecture-in-spring-boot-the-ultimate-guide-for-building-maintainable-apps-fa54fbb473a6) — testability payoff of plain-JUnit domain tests (MEDIUM confidence)
- [DEV Community: Hexagonal Architecture in Spring Boot Applications](https://dev.to/virajlakshitha/hexagonal-architecture-in-spring-boot-applications-1ibm) — common pitfalls (blurred port boundaries, mixed domain/framework annotations) (MEDIUM confidence)
- [Mastering Resilience4j Rate Limiter with Spring Boot](https://bootcamptoprod.com/resilience4j-rate-limiter/) — per-client rate limiter configuration pattern (MEDIUM confidence)
- [Coding Shuttle: Circuit Breaker, Retry, and Rate Limiter with Resilience4J](https://www.codingshuttle.com/spring-boot-handbook/microservice-circuit-breaker-retry-and-rate-limiter-with-resilience4-j/) — combined-pattern approach (circuit breaker + retry + rate limiter + bulkhead layering) (MEDIUM confidence)
- Existing codebase docs (`.planning/codebase/ARCHITECTURE.md`, `.planning/codebase/STRUCTURE.md`, `.planning/PROJECT.md`) — current layered structure, confirmed architecture decisions, active requirements (HIGH confidence — primary source, verified against actual repo)

---
*Architecture research for: read-only broker connector integration + BI aggregation in a Spring Boot microservice*
*Researched: 2026-08-26*
