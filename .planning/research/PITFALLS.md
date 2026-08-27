# Pitfalls Research

**Domain:** Multi-broker/exchange financial data aggregation (fintech connectors + BI) on an existing JHipster microservices codebase
**Researched:** 2026-08-26
**Confidence:** MEDIUM (cross-checked web sources + direct codebase verification; no access to Binance/IOL/Nexo sandbox accounts for live testing)

## Critical Pitfalls

### Pitfall 1: `precision=21, scale=2` cannot represent crypto-asset quantities

**What goes wrong:**
The existing schema stores `Operation.quantity`, `Position.quantity`, `Position.averageCost`, `Position.currentValue`, and `Valuation.totalValue` as `BigDecimal` with **`scale = 2`** (confirmed in `Operation.java`, `Position.java`, `Valuation.java` — not `decimal(28,10)` as might be assumed). Binance and Nexo balances routinely need 6-8 decimal places (e.g., `0.00034521 BTC`). Any value persisted through these columns silently rounds to 2 decimal places — a real BTC balance of `0.00034521` becomes `0.00`, and fractional-share/fractional-token positions disappear or misreport P&L.

**Why it happens:**
The schema was JDL-generated for a brokerage/equities mental model (2-decimal fiat amounts) before crypto connectors were in scope. Nobody revisited column precision when Binance/Nexo were added to requirements.

**How to avoid:**
Before writing the Binance/Nexo connector, add a Flyway migration widening `quantity`, `average_cost`, `current_value`, `total_value` (and any new normalized-balance columns) to at least `precision=28, scale=10` (matches what crypto exchanges actually return) and introduce an explicit `decimals`/precision field on `Asset` (or a per-asset-class constant: fiat=2, crypto=8-18 depending on token) so display rounding is a presentation concern, not a storage truncation. Never truncate incoming exchange data before persisting the raw normalized value.

**Warning signs:**
Grep for `scale = 2` in `domain/*.java` still returning hits after the connector phase starts; any BTC/ETH balance in dev/test data that rounds to `0.00`; unit tests that only use round-number fiat amounts (never catch precision loss).

**Phase to address:** Unified data model & persistence phase (must happen before any connector writes real balances) — this is a schema decision, not a connector-by-connector one.

---

### Pitfall 2: IOL's 15-minute token expires mid-sync and the failure is swallowed as "empty portfolio"

**What goes wrong:**
IOL issues a bearer token valid for exactly 15 minutes with a companion refresh token (confirmed against IOL developer docs and third-party wrapper implementations). If a scheduled snapshot job takes longer than 15 minutes end-to-end (unlikely for one account, plausible once retries/backoff for other brokers are serialized in the same job), or if the token is refreshed lazily only on 401, the naive implementation either (a) throws and the whole multi-broker sync job aborts, losing Binance/Nexo data too, or (b) catches the 401, logs it, and returns an empty balance list that gets treated as a legitimate "zero positions" snapshot — corrupting BI history with a false zero.

**Why it happens:**
Short-lived-token APIs are usually built by copy-pasting an HTTP client with a fixed `Authorization` header set once at connector construction, not one that refreshes proactively and distinguishes "auth failed" from "no positions."

**How to avoid:**
Refresh proactively (e.g., at ~80% of the 15-minute lifetime, or on every scheduled run since runs are likely spaced further apart than 15 min) rather than reactively on 401. Use a single-flight/mutex refresh so concurrent calls don't trigger duplicate refreshes, and never route the refresh call itself through the same 401-retry interceptor (infinite-loop risk). Critically: model "auth/token failure" as a distinct connector-level exception/result type, never coerce it into "zero balances" — the snapshot job must skip/flag that broker for that run, not write a zero.

**Warning signs:**
Any code path where a caught `401`/`403` from IOL falls through to returning `List.of()` or a default `Position` with zero quantity; snapshot history showing a broker's balance dropping to exactly zero for a single run and recovering next run (classic transient-auth-treated-as-real-data signature).

**Phase to address:** IOL connector phase (token refresh design), reinforced by the snapshot job phase (must treat per-broker fetch failure as "skip and flag," not "write zero").

---

### Pitfall 3: One misbehaving connector's rate-limit ban blocks all brokers in a shared job/IP

**What goes wrong:**
Binance rate-limits and bans **by IP**, not by API key (HTTP 429 on limit breach, escalating HTTP 418 bans from 2 minutes to 3 days for continuing after a 429 — confirmed in Binance's own docs). If the Binance connector doesn't back off correctly (e.g., naive per-account polling loop, or retry-without-backoff after a transient error), the whole pod's outbound IP can get banned — and since portfolioService likely runs all three connectors from the same pod/IP, this collaterally blocks IOL and Nexo calls too, or at minimum pollutes logs and triggers alerting for unrelated brokers.

**Why it happens:**
Rate limiting is easy to ignore until it's hit, and it's tempting to reuse the same generic retry/backoff (or lack thereof) across all three connectors even though Binance's limits are the strictest and most consequential.

**How to avoid:**
Implement exponential backoff with jitter and **honor the `Retry-After` header** on 429/418. Prefer batched/lighter endpoints over N calls per asset. Track and log rate-limit headers proactively rather than discovering limits via bans. Isolate connector failures: a Binance ban should degrade only the Binance portion of a snapshot (partial success), not cascade to an exception that aborts the whole job (see Pitfall 6). Given this is a single-user personal app, actual ban risk is low in practice — but the *pattern* (missing backoff entirely) is the actual codebase gap today: CONCERNS.md already flags "No API Rate Limiting" as a missing feature at the REST-API-exposed level; there's currently zero precedent for outbound backoff either.

**Warning signs:**
No test/mock coverage for `429`/`418` responses in the Binance connector's WireMock suite; connector code with `Thread.sleep(fixedMs)` retry instead of reading `Retry-After`; absence of a circuit breaker/backoff library (Resilience4j) in `pom.xml` for the connector module.

**Phase to address:** Binance connector phase — build backoff/circuit-breaker into the `BrokerConnector` adapter base, not per-connector, so IOL/Nexo inherit it.

---

### Pitfall 4: Nexo's unofficial API changes shape without notice and gets ingested as valid data

**What goes wrong:**
Nexo has no official third-party trading/read API — only community-maintained, explicitly "use at your own risk, not affiliated with Nexo" wrappers exist. These depend entirely on maintainers noticing and patching reverse-engineered endpoint/response changes; there's no changelog, versioning, or deprecation notice. A silent schema change (renamed field, new nested structure, changed unit) can produce a response that still parses (e.g., a field defaults to `null`/`0` instead of throwing) and gets ingested as a "real" zero or garbled balance rather than failing loudly.

**Why it happens:**
Reverse-engineered APIs don't publish contracts, so there's nothing to validate against except "whatever the response looked like when the connector was written." Deserializers configured leniently (ignore-unknown-fields, default-on-missing) hide breakage instead of surfacing it.

**How to avoid:**
Treat Nexo as the least-trustworthy source explicitly: strict (not lenient) JSON deserialization that fails loudly on missing/unexpected required fields rather than defaulting; a schema/contract test that runs against a recorded real response fixture and fails CI if Nexo's actual response shape ever needs to change the fixture; sanity-check bounds on ingested values (e.g., reject a balance snapshot that changes by >X% from the previous one without a matching Operation, since that's more likely a parsing bug than real market movement); isolate Nexo failures so they degrade gracefully (show "Nexo data stale/unavailable" in the dashboard) rather than corrupting the unified snapshot or crashing the job.

**Warning signs:**
Nexo connector code using permissive/ignore-unknown-properties Jackson config; no assertion in tests that unexpected/missing fields throw; BI dashboard showing implausible swings only for Nexo-sourced data.

**Phase to address:** Nexo connector phase explicitly (per PROJECT.md, already scoped as "tolerant error handling and fallback behavior") — make "tolerant" mean *tolerant of Nexo being unavailable*, not *tolerant of malformed data being silently accepted*.

---

### Pitfall 5: Broker credentials have no defined storage location yet — don't bolt them onto `BrokerAccount` as plaintext columns

**What goes wrong:**
`BrokerAccount` currently has no credential fields at all (`id`, `externalAccountId`, `displayName`, `broker` — verified directly in the entity). The natural shortcut when building the first connector is to add `apiKey`/`apiSecret`/`bearerToken` columns directly to this JPA entity, which then get serialized through the same DTO/REST layer as everything else (CONCERNS.md already flags this layer has minimal input validation and the entity is duplicated verbatim into the gateway service). That means exchange secrets could end up: persisted in plaintext in Postgres, exposed in JHipster's generated CRUD REST endpoints for `BrokerAccount`, replicated into gateway logs/audit trails, and returned in API responses unless explicitly excluded.

**Why it happens:**
JHipster's CRUD-per-entity pattern makes "add a column" the path of least resistance, and it's invisible until someone notices the `/api/broker-accounts` GET response includes a secret.

**How to avoid:**
Decide explicitly (this is a real architecture decision, not a default) whether credentials live in: (a) a separate, non-CRUD-exposed table/entity never wired to a public controller, (b) encrypted at rest via Jasypt/JCE with the key outside the DB, or (c) an external secrets store (Vault/K8s Secrets) referenced by an opaque ID in the DB. Given "Security & QA hardening" is explicitly Phase 3 of the project's own roadmap (deferred) but this milestone is when secrets *first get created and stored*, at minimum: never expose credential fields through the JHipster-generated `BrokerAccount` REST resource/DTO, mark them `@JsonIgnore`, and store secrets in a separate entity/table that has no public controller — full encryption/vault integration can wait for Phase 3, but "field never leaves the backend" cannot.

**Warning signs:**
`grep -r "apiSecret\|apiKey\|bearerToken" **/domain/BrokerAccount.java` finding plaintext columns; Postman/curl to `/api/broker-accounts` returning a secret field; secrets appearing in `git diff` of a `.yml`/`application-dev.yml` file.

**Phase to address:** Binance connector phase (first phase that needs to store a secret) — establish the pattern once, reuse for IOL/Nexo.

---

### Pitfall 6: Snapshot job aborts entirely on one broker's failure, losing data for brokers that succeeded

**What goes wrong:**
A scheduled job that loops `for broker in [binance, iol, nexo]: fetch()` and lets any exception propagate will fail the whole run if, say, IOL's token refresh throws — meaning Binance and Nexo data (which succeeded) never gets persisted either, or worse, gets persisted inconsistently (partial writes without a transaction boundary), leaving a snapshot that's neither "all brokers" nor cleanly "no brokers." The existing codebase already has a documented precedent for this class of gap: no Saga/compensating-transaction pattern for cross-service operations (CONCERNS.md), and no DLQ for failed Kafka messages — this milestone adds a new place the same "no failure isolation" pattern can recur.

**Why it happens:**
The simplest implementation is a synchronous loop with no per-broker error boundary; failure isolation requires deliberate design (catch-per-broker, partial-success result type, batch/run status tracking).

**How to avoid:**
Design the snapshot job so each broker fetch is independently caught and recorded (success/failure/skip per broker per run), persisted atomically per broker (or all-or-nothing per run via a `snapshot_run` status row marked `COMPLETE` only after all writes succeed), and surfaced to the BI dashboard as partial data with an explicit "as of" / "N of 3 brokers synced" indicator rather than presented as if it were a complete consolidated view.

**Warning signs:**
Snapshot job code with a single try/catch around the entire multi-broker loop instead of one per broker; no `status`/`completeness` column on the snapshot/valuation table; dashboard that can't distinguish "all zero because nothing held" from "zero because sync failed."

**Phase to address:** Unified data model & persistence phase (snapshot job design) — this is the same phase that should also solve Pitfall 1's precision issue, since both are schema/job-design decisions made once for all three connectors.

---

### Pitfall 7: Duplicate scheduled-job execution across multiple pod replicas (Kubernetes) creates duplicate snapshots

**What goes wrong:**
Spring's `@Scheduled` runs per JVM instance. Since the project's deployment target is Kubernetes (per PROJECT.md) and JHipster-generated services are designed to scale horizontally, if `portfolioService` is ever run with `replicas > 1`, every pod independently fires the same snapshot schedule — producing N duplicate snapshot rows per interval, each potentially hitting the broker APIs redundantly (compounding the rate-limit risk in Pitfall 3) and skewing "point in time" BI history with duplicate timestamps.

**Why it happens:**
`@Scheduled` "just works" in local/single-instance dev and CI, so the gap is invisible until someone scales replicas — which is exactly the kind of change a portfolio-piece project is likely to demo (e.g., showing HPA/scaling working in Kubernetes) without revisiting the scheduler.

**How to avoid:**
Add a distributed lock around the snapshot job before scaling past 1 replica — ShedLock (JDBC-backed, reuses the existing Postgres instance, no new infra) is the standard Spring Boot pattern; alternatively, move the snapshot trigger to a Kubernetes `CronJob` invoking a single-run endpoint instead of an in-process `@Scheduled` method. Either way, make the snapshot write idempotent per (broker, timestamp-bucket) via a unique constraint so even a duplicate trigger can't double-insert.

**Warning signs:**
`@Scheduled` annotation with no accompanying lock mechanism in the codebase; no unique constraint on `(broker_account_id, snapshot_time)` or similar in the Valuation/snapshot table; replica count in K8s manifests set >1 for portfolioService without this addressed.

**Phase to address:** Unified data model & persistence phase (snapshot job phase) — flag explicitly if/when replica count is increased later.

---

### Pitfall 8: Currency/unit mismatch across brokers makes "consolidated" BI numbers meaningless

**What goes wrong:**
Binance balances are crypto-denominated (BTC, USDT, etc.), IOL balances are ARS/USD equities and bonds, and Nexo is crypto plus its native NEXO token — three fundamentally different unit systems. The existing `Position.currency` field is a free-text string with no whitelist/validation (already flagged generically in CONCERNS.md: "Validate currency codes against whitelist"). Without a single normalization step (convert everything to one reporting currency, e.g., USD, using a consistent FX/price snapshot per run), the BI "consolidated performance" and "exposure by currency/asset" endpoints will either crash on unexpected currency strings or silently sum incompatible units (e.g., adding raw BTC quantity to raw ARS quantity).

**Why it happens:**
Each connector naturally speaks its own broker's native currency/unit; nobody is forced to reconcile them until the BI aggregation layer tries to sum across brokers, by which point the schema and per-broker ingestion logic are already built without a "reporting currency" concept baked in.

**How to avoid:**
Define a single reporting currency (or explicit multi-currency exposure breakdown, per PROJECT.md's own "exposure by currency" feature) at the data-model level, not the BI-endpoint level: every normalized snapshot row should carry both its native amount/currency *and* a converted reporting-currency amount plus the FX/price rate and timestamp used for that conversion (auditability — so a dashboard number can be traced back to which rate produced it). Validate `currency` against a whitelist (ISO 4217 for fiat + a maintained crypto ticker list) at ingestion, not display time.

**Warning signs:**
BI aggregation SQL/queries that `SUM(quantity)` or `SUM(current_value)` across rows without a `WHERE currency = X` or a pre-conversion step; `Position.currency` accepting arbitrary strings in tests; no FX rate source/table anywhere in the schema.

**Phase to address:** Unified data model & persistence phase (normalization design) before BI Dashboard phase consumes it — BI phase should be blocked from starting until this exists, since it's the foundation the dashboard's numbers depend on.

---

## Technical Debt Patterns

| Shortcut | Immediate Benefit | Long-term Cost | When Acceptable |
|----------|-------------------|-----------------|------------------|
| Store Binance/IOL secrets as plain `application-dev.yml` properties or DB columns without encryption | Fastest path to a working connector demo | Credential leak via git history, DB dump, or accidental REST exposure; real money at risk once Phase 4 (trade execution) exists | Only for local dev with test/read-only API keys in a git-ignored file; must not persist into shared config or a CRUD-exposed entity even in this milestone |
| Skip per-broker failure isolation in the snapshot job ("just let exceptions propagate") | Less code for the MVP snapshot job | Silent data loss/partial snapshots masquerading as complete ones; hard to debug after the fact since there's no per-broker status record | Never — this is cheap to do right (a try/catch per broker + a status enum) and expensive to retrofit once historical snapshot data already has gaps |
| Use lenient/permissive JSON deserialization for Nexo's unofficial API ("just don't crash") | Connector "works" through minor upstream changes | Breakage becomes silent data corruption instead of a loud, debuggable failure | Never for financial values; acceptable only for genuinely optional/decorative fields (e.g., a display label) |
| Reuse `scale = 2` columns for crypto balances rather than migrating now | No migration needed this sprint | Every BTC/crypto quantity under 0.01 silently rounds to zero; retrofitting after historical data exists means past snapshots are unrecoverably wrong | Never — must fix before the first Binance/Nexo write, not after |
| Single shared `@Scheduled` snapshot job with no distributed lock | Simple, works fine at replicas=1 | Duplicate snapshots the moment anyone scales replicas (a likely demo scenario for a portfolio project) | Acceptable only while explicitly pinned to `replicas: 1`; must be revisited before any K8s scaling demo |

## Integration Gotchas

| Integration | Common Mistake | Correct Approach |
|-------------|-----------------|-------------------|
| Binance | Polling REST endpoints per-asset on a tight loop, hitting weight limits and risking an IP-wide 418 ban that also blocks IOL/Nexo traffic from the same pod | Batch/consolidate calls, honor `Retry-After`, use WebSocket streams for anything needing frequent updates (doesn't count against REST weight), rate-limit by IP not by key |
| IOL (InvertirOnline) | Refreshing the bearer token reactively only after a 401, or catching the resulting auth failure and returning an empty position list treated as "real" zero balances | Refresh proactively before the 15-minute window elapses, single-flight the refresh, and model auth failure as a distinct "skip this broker for this run" outcome, never as a zero-balance result |
| Nexo (unofficial/reverse-engineered) | Lenient deserialization that silently accepts a changed/missing field as null/zero, corrupting ingested balances without any error | Strict deserialization with fixture-based contract tests; treat any unexpected shape as a hard failure that flags the broker as unavailable for that run, not as valid data |

## Performance Traps

| Trap | Symptoms | Prevention | When It Breaks |
|------|----------|------------|-----------------|
| Sequential (not parallel) per-broker fetch in the snapshot job | Job duration ≈ sum of all three brokers' latency + retries; risks the IOL token window elapsing mid-job as more brokers/accounts are added | Fetch brokers concurrently (e.g., `CompletableFuture` per connector) with independent timeouts, so one slow/rate-limited broker doesn't extend the whole job's wall-clock time | Becomes noticeable once more than 2-3 broker accounts exist, or once any single connector has retry/backoff delays |
| Unbounded snapshot table growth (one row per broker per scheduled interval, forever) | Slow BI historical queries over time; large Postgres table with no partitioning/archival | Add a retention/aggregation policy (e.g., keep raw snapshots for 90 days, roll up to daily/weekly aggregates beyond that) before the BI Dashboard phase ships historical charts | Not urgent at personal-app scale (single user, few accounts) but easy to design in now versus retrofit later |

## Security Mistakes

| Mistake | Risk | Prevention |
|---------|------|------------|
| Storing exchange API keys with withdrawal/trading permissions enabled when only read access is needed | A compromised key (leaked log, DB dump) allows an attacker to move funds, not just read balances | Scope every exchange API key to read-only at creation time (Binance/Nexo both support restricting withdrawal permissions); this is an operational step outside the codebase but must be documented as a setup requirement |
| Logging full broker API responses or request headers for debugging | Leaks API secrets (if sent as headers) or account balance details into log aggregation (ELK, per stack) with weaker access control than the DB | Scrub `Authorization`/API-key headers and redact balance fields in connector-level logging; never log raw response bodies at info/debug level in production profiles |
| Exposing `BrokerAccount` (or a new credentials entity) through JHipster's default generated CRUD REST resource | Secrets become readable via an authenticated API call, or via the gateway's duplicated entity/DTO layer (already a known duplication concern) | Exclude credential fields from any DTO/JSON serialization (`@JsonIgnore` at minimum); do not let JHipster's entity generator wire a public controller to a table holding secrets |

## "Looks Done But Isn't" Checklist

- [ ] **Binance/IOL/Nexo connectors "work":** Often missing rate-limit/backoff handling and token-refresh-under-load testing — verify by simulating a 429/418 (Binance) and a token expiry mid-request (IOL) in WireMock, not just the happy path
- [ ] **Unified data model "normalizes across brokers":** Often missing actual currency conversion/reporting-currency logic — verify a BI query can correctly sum a mixed BTC + ARS + USDT portfolio into one meaningful total, not just that all three brokers write to the same table shape
- [ ] **Scheduled snapshot job "runs on schedule":** Often missing duplicate-execution protection and partial-failure handling — verify behavior when one broker call throws and when the job is triggered by two replicas simultaneously (or document that replicas is pinned to 1)
- [ ] **BI dashboard "shows historical evolution":** Often missing an indicator for incomplete/partial data points — verify the dashboard visually distinguishes "all brokers synced" from "2 of 3 synced, IOL token expired" rather than presenting partial data as complete

## Recovery Strategies

| Pitfall | Recovery Cost | Recovery Steps |
|---------|---------------|-----------------|
| `scale=2` truncation already persisted real crypto data | HIGH | Widen the column via Flyway migration, but historical rounded values are unrecoverable — must re-fetch/re-ingest from broker transaction history if available, or accept a gap in early BI history with a documented caveat |
| Duplicate snapshots from unlocked multi-replica scheduling | MEDIUM | Add a unique constraint retroactively, de-duplicate existing rows (keep earliest per bucket), then add ShedLock/CronJob fix going forward |
| Partial-failure snapshot silently treated as complete | LOW-MEDIUM | Backfill a `status`/`completeness` column, mark historical rows as `UNKNOWN` completeness rather than falsely `COMPLETE`, surface that distinction in the dashboard retroactively |
| Nexo silent schema-drift corruption | MEDIUM | Once detected, add the fixture/contract test that would have caught it, quarantine/flag affected historical rows, and manually verify against the Nexo web UI for the affected date range (no better source of truth exists for an unofficial API) |

## Pitfall-to-Phase Mapping

| Pitfall | Prevention Phase | Verification |
|---------|-------------------|---------------|
| `scale=2` precision truncation | Unified data model & persistence | Flyway migration diff shows widened precision/scale before any connector persists real data; a test asserts an 8-decimal BTC quantity round-trips exactly |
| IOL token expiry mid-sync / auth-failure-as-zero | IOL connector | WireMock test simulates a 401 mid-fetch and asserts the connector returns a distinct failure/skip result, never a zero-quantity position |
| Binance rate-limit cascade | Binance connector | WireMock test simulates 429 with `Retry-After` and asserts backoff honors the header; no other broker's fetch aborts as a result |
| Nexo silent breakage | Nexo connector | Contract/fixture test fails loudly on an altered response shape; connector marks Nexo "unavailable" rather than persisting a guessed value |
| Broker credentials storage | Binance connector (first to need one) | `/api/broker-accounts` (or equivalent) response never contains a secret field in an integration test |
| Snapshot job partial-failure isolation | Unified data model & persistence | Test kills one broker mid-job and asserts the other two still persist, with a per-run/per-broker status recorded |
| Duplicate scheduled execution across replicas | Unified data model & persistence | Either replica count is documented as pinned to 1, or a ShedLock/lock test proves only one of N concurrent job invocations executes |
| Currency/unit mismatch in BI aggregation | Unified data model & persistence (blocks BI Dashboard phase) | A BI aggregation test mixes BTC/ARS/USDT source rows and asserts the consolidated total is computed via explicit conversion, not raw summation |

## Sources

- [Binance Open Platform — Rate Limiting and IP Bans](https://developers.binance.com/docs/binance-spot-api-docs/rest-api/limits) — HIGH confidence, official docs
- [Rate Limiting and IP Bans | binance/binance-spot-api-docs | DeepWiki](https://deepwiki.com/binance/binance-spot-api-docs/1.3-rate-limiting) — HIGH confidence, cross-checks official docs
- [Binance Academy — How to Avoid Getting Banned by Rate Limits](https://academy.binance.com/en/articles/how-to-avoid-getting-banned-by-rate-limits) — HIGH confidence
- [InvertirOnline API docs](https://api.invertironline.com/) and [IOL APIs DOC](https://developers.invertironline.com/) — HIGH confidence, confirms 15-minute bearer token + refresh token flow
- [python-nexo (unofficial wrapper)](https://github.com/guilyx/python-nexo) / [nexo-pro Node.js connector](https://github.com/aussedatlo/nexo-pro) — MEDIUM confidence, confirms no official third-party API exists, "use at your own risk" disclaimers
- [ropcat/reversing-unofficial-APIs](https://github.com/ropcat/reversing-unofficial-APIs) — MEDIUM confidence, general reverse-engineered API risk framing
- [Coinzhen — Binance API Key Security: Least Privilege, IP Whitelisting, Leaks](https://coinzhen.com/en/articles/binance-api-key-security.html) — MEDIUM confidence
- [Nango Blog — How to handle concurrency with OAuth token refreshes](https://nango.dev/blog/concurrency-with-oauth-token-refreshes/) — MEDIUM confidence, single-flight refresh pattern
- [Duende Software — Token Expiration & Refresh Best Practices for APIs](https://duendesoftware.com/learn/best-practices-managing-token-expiration-refresh-revocation-in-web-apis) — MEDIUM confidence
- [Montecarlo — The Comprehensive Guide To Data Reconciliation](https://montecarlo.ai/blog-data-reconciliation) — MEDIUM confidence
- [BITCAT — Avoid Common Pitfalls: Handling Currency Data in Fintech](https://bitcat.dev/avoid-common-pitfalls-fintech-currency-handling/) — MEDIUM confidence
- [ShedLock — Ensuring Exactly-Once Execution of Scheduled Tasks in Multi-Pod Spring Boot](https://medium.com/@bectorhimanshu/ensuring-exactly-once-execution-of-scheduled-tasks-in-a-multi-pod-spring-boot-application-using-d6b17d0efc9c) — MEDIUM confidence
- [The New Stack — Rethinking Java @Scheduled Tasks in Kubernetes](https://thenewstack.io/rethinking-java-scheduled-tasks-in-kubernetes/) — MEDIUM confidence
- [xflowpay — Idempotency Key in Payment APIs: A Developer's Guide](https://www.xflowpay.com/blog/idempotency-key) — MEDIUM confidence
- Direct codebase verification (2026-08-26): `portfolioService/src/main/java/com/assetcompass/portfolio/domain/{Operation,Position,Valuation,BrokerAccount,Asset,AssetRatio}.java` — HIGH confidence (primary source, read directly)
- `.planning/codebase/CONCERNS.md` (2026-08-22 audit) — HIGH confidence, primary source for pre-existing tech debt cross-referenced above

---
*Pitfalls research for: multi-broker fintech connector integration + BI aggregation (Asset Compass)*
*Researched: 2026-08-26*
