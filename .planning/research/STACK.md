# Stack Research

**Domain:** Multi-broker fintech data aggregation (read-only broker/exchange integrations + BI/analytics layer) on an existing JHipster/Spring Boot 4 microservices monorepo
**Researched:** 2026-08-26
**Confidence:** MEDIUM (HIGH for Binance/IOL auth mechanics and Spring-ecosystem patterns; LOW for Nexo, inherent to the domain — there is no official API to verify against)

This document covers only what needs to be **added** for the broker connectors and BI dashboard milestone. The existing scaffold (Java 21, Spring Boot 4.0.7, Postgres, Redis, Kafka, Keycloak, Liquibase, Testcontainers, WireMock, Resilience4j-capable via Spring Cloud Circuit Breaker) is locked in per `.planning/codebase/STACK.md` and is not re-litigated here.

## Recommended Stack

### Core Technologies (broker integration layer)

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| Spring `RestClient` | Bundled with Spring Framework 6.1+ (already on the Spring Boot 4.0.7 classpath) | Synchronous HTTP client for all three broker connectors | Modern, non-deprecated replacement for `RestTemplate`; blocking model matches this service's existing Spring MVC (not WebFlux) stack; zero extra dependency. Each `BrokerConnector` implementation (Binance/IOL/Nexo) wraps its own `RestClient` instance — keeps the adapter pattern already decided in `ARCHITECTURE.md` clean, since every broker's quirks (auth header, signing, base URL) stay inside its own adapter instead of leaking into a shared SDK's abstractions. |
| Resilience4j (via `spring-cloud-starter-circuitbreaker-resilience4j`, already a stack dependency per `.planning/codebase/STACK.md`) | Latest 2.x compatible with Spring Boot 4 (confirm exact BOM-managed version at implementation time via Spring Cloud Dependencies 2025.1.2) | Circuit breaker, retry with backoff, rate limiter per broker connector | It's already in the stack (listed as "Spring Cloud Circuit Breaker (Resilience4j)"), so this is wiring, not a new dependency decision. Apply per-broker: `@RateLimiter` for Binance (respect endpoint weight budgets), `@Retry` with exponential backoff for IOL (token refresh races, transient 5xx), `@CircuitBreaker` + generous fallback for Nexo (expect it to break more often, tolerate degraded/stale data instead of failing the whole dashboard). |
| WireMock (already in stack) + Testcontainers `wiremock` module | Match current WireMock/Testcontainers versions already pinned in `pom.xml` | Mock all three broker APIs in CI; never call real broker endpoints in tests | Standard 2025/2026 pattern for third-party API testing in Spring Boot: stub at the **HTTP protocol level** (not the Java client interface) using either the WireMock JUnit 5 `@WireMockTest` extension or the Testcontainers WireMock module, with `@DynamicPropertySource` pointing each connector's configured `base-url` property at the mock server port. This catches real (de)serialization bugs, is fast/repeatable, and matches what the codebase already uses. |

### Supporting Libraries

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| ShedLock (`net.javacrumbs.shedlock:shedlock-spring` + `shedlock-provider-jdbc-template`) | Latest 5.x | Prevents the scheduled portfolio-snapshot job from running twice if portfolioService is ever scaled to >1 pod | Add when the snapshot `@Scheduled` job is introduced. Not strictly needed at 1 replica, but cheap insurance and a legitimate "professional engineering practice" talking point for the portfolio-piece goal — uses the existing Postgres instance as the lock store, no new infra. |
| `okhttp` (transitively, only if `binance-connector-java` is adopted) | 3.4.1 (connector) | Official Binance Java SDK | **Do not use** for this project — see "What NOT to Use" below. Listed here only because it's the one existing named library for Binance and the roadmap consumer needs to know it was evaluated and rejected, not missed. |
| Jackson (already in stack) | existing pinned version | Parse Binance/IOL JSON responses into DTOs | No new dependency — reuse the Jackson stack already configured for the rest of portfolioService. |
| `springdoc-openapi` (already planned per PROJECT.md active scope) | existing | Document the new BI aggregation endpoints | Reuse; no separate BI-specific API doc tool needed. |

### Development Tools

| Tool | Purpose | Notes |
|------|---------|-------|
| WireMock stub JSON fixtures per broker | Capture realistic Binance/IOL response shapes as static JSON files under `src/test/resources/wiremock/{broker}/` | For IOL and Binance, generate fixtures from real (sanitized) sandbox/testnet responses where possible (Binance has a public Testnet; IOL does not, so fixtures for IOL must be hand-built from documented response shapes and cross-checked against community wrapper source code). |
| Binance Spot Testnet (`testnet.binance.vision`) | Manual/exploratory verification of request signing and response shapes before writing WireMock stubs | Free, official, requires separate testnet API key — use only for exploration, never wire the app to hit it in CI. |
| Postman/Insomnia collection (manual, IOL only) | Exploratory calls against the real IOL API during development, since there's no public sandbox | IOL requires an activated brokerage account to get real credentials; do this once, capture response shapes into fixtures, then develop entirely against WireMock afterward. |

## Installation

```xml
<!-- portfolioService/pom.xml — additions only; RestClient, Jackson, Resilience4j, WireMock, Testcontainers are already present transitively via existing starters -->

<!-- ShedLock, for safe scheduled snapshot job (add when Position/Valuation history job is built) -->
<dependency>
    <groupId>net.javacrumbs.shedlock</groupId>
    <artifactId>shedlock-spring</artifactId>
    <version>5.16.0</version> <!-- verify current at implementation time -->
</dependency>
<dependency>
    <groupId>net.javacrumbs.shedlock</groupId>
    <artifactId>shedlock-provider-jdbc-template</artifactId>
    <version>5.16.0</version>
</dependency>
```

No Maven dependency is needed for Binance or IOL connectors themselves — both are implemented as thin `RestClient`-based adapters behind the existing `BrokerConnector` interface. Do not add `io.github.binance:binance-connector-java` (see below).

## Alternatives Considered

| Category | Recommended | Alternative | Why Not |
|----------|-------------|-------------|---------|
| Binance HTTP client | Hand-rolled `RestClient` adapter, signing done manually (HMAC-SHA256 over query string, standard `javax.crypto.Mac`) | Official `io.github.binance:binance-connector-java` (v3.4.1) | Brings its own OkHttp-based HTTP stack alongside Spring's, duplicating what `RestClient` already does; couples the `BrokerConnector` adapter to a third-party SDK's object model instead of keeping broker-specific detail fully contained per the architecture's adapter-pattern intent; SDK also covers futures/margin/websocket surface this project doesn't need (read-only spot balances only) — unnecessary weight for the actual requirement. Reconsider only if trading (Phase 4, tradingService) needs order-placement/websocket streaming later, at which point re-evaluate for that service specifically. |
| BI time-series storage | Plain Postgres table (`portfolio_snapshot`), Liquibase-managed, one row per account/asset/day | TimescaleDB extension (hypertables + compression) | TimescaleDB gives large wins (1.2–5x aggregation, ~20x insert throughput) but only matters at high-frequency/high-volume time-series scale; this is a personal portfolio with daily/periodic snapshots — orders of magnitude below where those gains matter. Adding it now means a new Postgres extension to provision in Docker Compose/K8s for no measurable benefit at current scale, working against the "one Postgres per service" simplicity already decided. Revisit if snapshot granularity moves to intraday/high-frequency. |
| Synchronous HTTP client (general) | `RestClient` | `WebClient` (reactive) | portfolioService is Spring MVC (blocking), not WebFlux; introducing `WebClient` would mean either running a reactive client on a blocking thread-pool service (extra complexity, no payoff) or partially migrating the service to reactive — out of scope for this milestone. `WebClient`/reactive is the right call for `gateway` (already WebFlux) but not here. |
| Synchronous HTTP client (general) | `RestClient` | OpenFeign / `@FeignClient` | Feign is already used in the codebase for inter-service REST calls per `ARCHITECTURE.md`, but it's declining in the ecosystem (effectively maintenance mode, superseded by Spring's own `@HttpExchange`/`RestClient` combo) and its declarative-interface style hides the exact per-broker request-building/signing logic this integration needs full control over (e.g., Binance's non-standard signature query param, IOL's token-refresh interceptor). Reserve Feign usage for the inter-service calls it's already used for; use `RestClient` directly for external broker calls. |

## What NOT to Use

| Avoid | Why | Use Instead |
|-------|-----|-------------|
| `io.github.binance:binance-connector-java` | Extra HTTP stack (OkHttp) parallel to Spring's; couples adapter to SDK's response model; scope (futures/margin/websockets) far exceeds this milestone's read-only spot-balance need | Hand-rolled `RestClient` + manual HMAC-SHA256 signing (well-documented, ~30 lines) |
| Netflix Hystrix | Deprecated/in maintenance mode, superseded project-wide by Resilience4j | Resilience4j (already in stack) |
| Any third-party "Nexo API" Node/Python wrapper as a design reference for the retail Nexo product | The only unofficial libraries found (`python-nexo`, `nexo-pro` Node connector) wrap **Nexo Pro**, a separate institutional/trading-focused API surface requiring its own API-key generation flow — this is very likely NOT the same product as the retail Nexo wallet/earn account a personal user holds. Treating it as equivalent risks building against endpoints the user's actual account can't even authenticate to. | Before writing any Nexo connector code: confirm which Nexo product/account type is actually held (retail app vs Nexo Pro) and whether that product exposes any API keys at all in account settings. If retail-only with no API surface, the "connector" may need to be a scraper/manual-export-import fallback instead of a REST client — this is a decision the roadmap needs to surface explicitly, not assume. |
| TimescaleDB (for now) | Solves a scale problem this project doesn't have yet; adds infra (Postgres extension, or a different base image) not currently in Docker Compose/K8s manifests | Plain Postgres table + Liquibase migration for the snapshot job; revisit only if intraday-frequency snapshotting is ever required |
| Quartz Scheduler | Adds persistence/clustering complexity (its own DB tables) that ShedLock + Spring's built-in `@Scheduled` already cover for this project's single daily/periodic snapshot job | Spring `@Scheduled` + ShedLock |

## Stack Patterns by Variant

**For Binance (has official, well-documented, versioned REST API):**
- Use `RestClient` with a request interceptor that computes the HMAC-SHA256 signature and appends it as a query param, plus an `X-MBX-APIKEY` header.
- Target `GET /api/v3/account` (weight 20) for balances; treat weight-based rate limiting seriously — surface a `Resilience4j RateLimiter` sized conservatively below Binance's published per-minute weight budget, since this is shared with sub-account/other usage of the same key.
- Percent-encode the exact bytes being signed — this is the single most common integration bug reported for Binance signed endpoints.

**For IOL (has a real but sparsely-documented REST API, 15-minute token lifetime):**
- Because the bearer token is short-lived, implement the token acquisition/refresh as a small internal component (not just inline in the connector) that the `IolConnector` calls before each outbound request, caching the current token and its expiry, and proactively refreshing (e.g., at ~12 minutes) rather than reactively retrying on 401 — a reactive-only approach adds latency and risks races under concurrent requests.
- Cache the bearer token in Redis (already in the stack) if `portfolioService` is ever scaled beyond one replica, so pods share one token lifecycle instead of each independently authenticating and potentially invalidating each other's session.
- Because IOL has no public sandbox, budget explicit manual-exploration time against the real API (with real, low-risk credentials) early, before committing to WireMock fixture shapes.

**For Nexo (no confirmed official public API):**
- Design the `NexoConnector` to fail soft by default: on any unexpected response shape, timeout, or auth failure, return a "stale/unavailable" state for that broker rather than propagating an exception that would break the unified dashboard for the other two (working) brokers.
- Treat whatever integration approach is chosen (community wrapper reference, manual export/import, or scraping) as inherently unstable — version-pin and snapshot-test against fixed fixtures aggressively, since there's no changelog or deprecation notice to rely on when it breaks.
- Resolve the retail-vs-Nexo-Pro account-type question (see "What NOT to Use") before implementation planning locks in a specific technical approach — this changes whether this is a REST connector at all.

**For the BI/analytics aggregation layer:**
- Model `Operation` as the immutable event log (already decided in PROJECT.md) and derive both `Position`/`Valuation` (current state) and the new periodic snapshot table (historical state) from it — don't let the BI layer read/write anything but derived, recomputable data.
- Expose aggregation as new read-only REST endpoints in `portfolioService` (consolidated performance, exposure by currency/asset, historical evolution) computed via `QueryService`-style repository queries or a dedicated `PortfolioAnalyticsService`, following the same layered pattern (`Resource` → `Service` → `Repository`) already used for the 10 existing entities — no new architectural pattern needed for this milestone.
- Keep the scheduled snapshot job idempotent (upsert by account+asset+date) so a re-run after a failure doesn't duplicate rows.

## Version Compatibility

| Package A | Compatible With | Notes |
|-----------|-----------------|-------|
| `RestClient` | Spring Framework 6.1+ / Spring Boot 3.2+ | Already satisfied — portfolioService runs Spring Boot 4.0.7. No version action needed. |
| `shedlock-provider-jdbc-template` 5.16.x | Spring Boot 3.x/4.x, PostgreSQL (via existing HikariCP datasource) | Verify exact latest patch at implementation time; API has been stable across recent majors. |
| Resilience4j (via Spring Cloud Circuit Breaker) | Spring Cloud Dependencies 2025.1.2 (already the pinned BOM per `.planning/codebase/STACK.md`) | Version is BOM-managed already — do not pin Resilience4j directly, let the existing Spring Cloud BOM resolve it, to avoid drift from the rest of the stack. |
| WireMock + Testcontainers | Whatever is already pinned in `pom.xml` | Confirm current pinned versions before adding new stub complexity; no upgrade required by this research. |

## Sources

- [Account Endpoints | Binance Open Platform](https://developers.binance.com/docs/binance-spot-api-docs/rest-api/account-endpoints) — MEDIUM confidence (web search, cross-referenced across multiple official Binance doc pages)
- [Maven Central: io.github.binance:binance-connector-java](https://central.sonatype.com/artifact/io.github.binance/binance-connector-java) / [Libraries.io](https://libraries.io/maven/io.github.binance:binance-connector-java) — MEDIUM confidence, version 3.4.1 confirmed
- [InvertirOnline Autenticacion](https://api.invertironline.com/Help/Autenticacion) — MEDIUM confidence; token endpoint and 15-minute expiry corroborate the project's own documented assumption in PROJECT.md
- [IOL API / Documentación API](https://www.invertironline.com/api), [developers.invertironline.com](https://developers.invertironline.com/) — LOW confidence (full spec gated behind an activated account; endpoint shapes inferred from community wrappers, not the primary doc itself)
- Nexo official API search (status.nexo.com and general web) — LOW confidence, essentially a negative result (no official API found), which is itself the useful finding
- [python-nexo](https://github.com/guilyx/python-nexo), [nexo-pro Node connector](https://github.com/aussedatlo/nexo-pro) — LOW confidence, unofficial community projects targeting "Nexo Pro" specifically
- [Testcontainers: Testing REST API integrations using WireMock](https://testcontainers.com/guides/testing-rest-api-integrations-using-wiremock/), [Baeldung: Integrating WireMock with Spring Boot](https://www.baeldung.com/spring-boot-wiremock) — MEDIUM confidence
- [TimescaleDB vs Postgres](https://github.com/timescale/docs.timescale.com-content/blob/master/introduction/timescaledb-vs-postgres.md) — MEDIUM confidence
- [Baeldung: Spring Boot FeignClient vs. WebClient](https://www.baeldung.com/spring-boot-feignclient-vs-webclient), general RestClient/HttpExchange 2025 commentary — MEDIUM confidence
- Resilience4j Spring Boot 3/4 integration articles (multiple, cross-checked) — MEDIUM confidence
- `.planning/codebase/STACK.md`, `.planning/codebase/ARCHITECTURE.md`, `.planning/PROJECT.md` — HIGH confidence (primary, internal sources)

---
*Stack research for: multi-broker fintech read-only integration + BI aggregation layer*
*Researched: 2026-08-26*
