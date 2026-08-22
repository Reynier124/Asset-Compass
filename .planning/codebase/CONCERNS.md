# Codebase Concerns

**Analysis Date:** 2026-08-22

## Tech Debt

**Code Duplication Across Microservices:**
- Issue: Gateway service contains complete copies of all domain entities (Asset, Operation, Position, etc.) from portfolioService, leading to synchronized maintenance across multiple services
- Files: `gateway/src/main/java/com/assetcompass/gateway/domain/` mirrors `portfolioService/src/main/java/com/assetcompass/portfolio/domain/`
- Impact: 
  - Schema changes require updates in multiple places
  - Higher risk of inconsistency between gateway and service entities
  - Increased testing burden (64+ test files duplicated)
- Fix approach: Consider shared domain library or code generation; evaluate if gateway truly needs its own domain layer or should proxy through service clients only

**Async Task Executor Configuration Minimal:**
- Issue: `AsyncConfiguration.java` defines a `ThreadPoolTaskExecutor` with default settings and no queue capacity/rejection policy specified
- Files: `portfolioService/src/main/java/com/assetcompass/portfolio/config/AsyncConfiguration.java`
- Impact: Under high load, async task submission may fail silently or queue unbounded memory consumption
- Fix approach: Add explicit queue size limits, thread pool size tuning, and rejection policy configuration for production readiness

**No Dead-Letter Queue for Kafka Messages:**
- Issue: Kafka consumers (`KafkaConsumer.java`, `KafkaProducer.java`) lack dead-letter queue configuration for failed message processing
- Files: `portfolioService/src/main/java/com/assetcompass/portfolio/broker/KafkaConsumer.java`
- Impact: Failed message processing is lost; no audit trail for debugging async failures
- Fix approach: Implement Kafka DLQ pattern with error event publishing and monitoring

## Known Bugs

**Thread-Unsafe SSE Emitter Storage:**
- Symptoms: Multiple concurrent clients registering/unregistering SSE connections may corrupt the emitters map
- Files: `portfolioService/src/main/java/com/assetcompass/portfolio/broker/KafkaConsumer.java` (line 21: `private Map<String, SseEmitter> emitters = new HashMap<>()`)
- Trigger: Simultaneous client connection/disconnection under load
- Workaround: Use `ConcurrentHashMap` instead of `HashMap`

**Silent IOException Swallowing in SSE Broadcasting:**
- Symptoms: Client disconnects or timeouts during SSE event send fail silently with only debug-level logging; no metrics or alerts
- Files: `portfolioService/src/main/java/com/assetcompass/portfolio/broker/KafkaConsumer.java` (lines 39-45)
- Trigger: Network latency, slow clients, or emitter timeout
- Workaround: Enhance error handling to log warning/error level, track failed sends, and remove failed emitters from the map

## Security Considerations

**Overly Permissive CORS in Development Profile:**
- Risk: If CORS is enabled for all origins in development and accidentally deployed to production, API is vulnerable to cross-origin attacks
- Files: Configuration likely in `src/main/resources/config/application.yml` or Spring Security beans
- Current mitigation: OAuth2 resource server authentication is in place; CSRF disabled
- Recommendations: 
  - Explicitly define allowed origins per environment
  - Add profile-based CORS configuration (stricter for prod)
  - Document CORS settings in security docs

**OAuth2 Audience Validation Dependency:**
- Risk: Keycloak (external IdP) is required; misconfiguration or outage blocks all authentication
- Files: `portfolioService/src/main/java/com/assetcompass/portfolio/config/SecurityConfiguration.java` (lines 87-88)
- Current mitigation: JWT validation with issuer and audience checks in place
- Recommendations: 
  - Implement circuit breaker for token validation
  - Add offline token validation cache with TTL
  - Test failover scenarios

**No Input Validation on Sensitive Financial Data:**
- Risk: REST endpoints for Operation, Position, Valuation lack comprehensive input validation (BigDecimal amounts, currency codes)
- Files: DTOs in `portfolioService/src/main/java/com/assetcompass/portfolio/service/dto/` lack granular validation annotations
- Current mitigation: Spring validation framework available but may not be fully utilized
- Recommendations:
  - Add `@Min`, `@Max`, `@Pattern` annotations to all numeric/financial fields
  - Validate currency codes against whitelist
  - Implement custom validators for business rules (e.g., average cost ≥ 0)

## Performance Bottlenecks

**N+1 Query Risk in Entity Relationships:**
- Problem: Heavy use of ManyToOne relationships (Position → BrokerAccount → Broker, Operation → Asset) without explicit fetch strategy may cause N+1 queries
- Files: Domain entities in `portfolioService/src/main/java/com/assetcompass/portfolio/domain/` (Operation.java, Position.java, etc.)
- Cause: Default JPA lazy loading; pagination queries without eager loading
- Improvement path:
  - Add `@NamedEntityGraphs` to heavy entities (Operation, Position, Valuation)
  - Use `@Fetch(FetchMode.JOIN)` or DTO projections for listing endpoints
  - Profile with Spring Data Web `@RepositoryRestResource` or custom projections

**Unbounded Pagination Default:**
- Problem: Endpoints like `/api/operations` paginate with default page size; no limit enforced if client requests size=10000
- Files: Paginated endpoints in `portfolioService/src/main/java/com/assetcompass/portfolio/web/rest/`
- Cause: JHipster default pageable handling without max-size constraints
- Improvement path: Add custom `PageableDefault` with `@PageableDefault(size=50)` and override page size max in `WebConfig`

**Redis Cache Without Eviction Policy:**
- Problem: Redis cache `cacheProvider: redis` configured without explicit TTL or eviction strategy for session/token caching
- Files: `portfolioService/pom.xml` (Redisson dependency at line 292-295)
- Cause: Cache configuration relies on Spring Boot defaults
- Improvement path: Configure Redis eviction policy, add cache TTL per entity type, monitor cache hit/miss rates

## Fragile Areas

**SSE Emitter Lifecycle Management:**
- Files: `portfolioService/src/main/java/com/assetcompass/portfolio/broker/KafkaConsumer.java`
- Why fragile: Reliance on callbacks for cleanup; no timeout handling; manual map management prone to leaks
- Safe modification: Consider using `ConcurrentHashMap`, add explicit timeout checking, or switch to WebSocket for persistent connections
- Test coverage: Integration tests for SSE don't cover high-concurrency scenarios or network failure recovery

**Liquibase Migration Assumptions:**
- Files: `portfolioService/src/main/java/com/assetcompass/portfolio/config/LiquibaseConfiguration.java`
- Why fragile: Assumes Liquibase changelog files exist and are consistent; no rollback testing in CI
- Safe modification: Version all changesets, test migrations on copy of production schema, add rollback checks to CI
- Test coverage: No integration tests for failed migration recovery

**Kafka Consumer Error Handling:**
- Files: `portfolioService/src/main/java/com/assetcompass/portfolio/broker/KafkaConsumer.java`, `KafkaProducer.java`
- Why fragile: No retry logic, no poison pill handling, exceptions logged at debug level
- Safe modification: Add Spring Retry annotation, configure DLQ binding, add metrics for failed messages
- Test coverage: No tests for malformed Kafka messages or broker unavailability

## Scaling Limits

**SSE Emitter Memory Growth:**
- Current capacity: HashMap unbounded; one emitter per connected client
- Limit: Linear growth in memory per concurrent client; with 10k clients, expect ~1MB+ emitter overhead
- Scaling path: Implement broadcast queue (pub/sub) instead of per-emitter storage; use Redis-backed session store

**Single PostgreSQL Instance Per Service:**
- Current capacity: Single DB bottleneck for read/write heavy workloads
- Limit: No connection pooling tuning visible; HikariCP default pool size is 10
- Scaling path: Add read replicas, implement connection pooling tuning (`spring.datasource.hikari.maximum-pool-size`), consider CQRS for heavy reads

**Kafka Partition Assignment:**
- Current capacity: Topic partition count not specified in codebase; default to 1
- Limit: Single partition = single consumer thread; no parallelism
- Scaling path: Pre-create Kafka topics with appropriate partition count based on expected throughput

## Dependencies at Risk

**Spring Boot 4.0.7 (Next Gen):**
- Risk: Relatively new version; limited production feedback; potential for breaking changes in minor updates
- Impact: Dependency chain may have unpatched vulnerabilities in transitive deps
- Migration plan: Monitor Spring Security and Spring Cloud updates; test against latest version before adopting

**JHipster Framework 9.1.0:**
- Risk: Scaffolded code may contain JHipster-specific patterns that diverge from Spring best practices; generator dependency lock
- Impact: Difficult to maintain without JHipster CLI; code generation can introduce inconsistencies
- Migration plan: Gradually decouple from generated code patterns; extract reusable libraries; document manual code ownership

**Redisson 4.6.1:**
- Risk: Distributed lock library; Redis connection failures cascade to app-level deadlocks
- Impact: Unreliable caching and distributed locking under network partition
- Migration plan: Add circuit breaker for Redis operations; implement fallback to in-memory cache; test failover scenarios

## Missing Critical Features

**No API Rate Limiting:**
- Problem: REST endpoints lack rate limiting; vulnerable to DoS attacks and resource exhaustion
- Blocks: SLA compliance, fair resource sharing, brute-force attack prevention
- Recommendation: Add Spring Cloud CircuitBreaker with RateLimiter, or implement custom rate limiter interceptor

**No Distributed Tracing:**
- Problem: Kafka events and inter-service calls lack correlation IDs; debugging async failures is difficult
- Blocks: Root cause analysis for cross-service issues; performance profiling
- Recommendation: Add Spring Cloud Sleuth or Micrometer tracing; correlate logs with trace IDs

**No Saga Pattern for Distributed Transactions:**
- Problem: Cross-service operations (e.g., create Operation + emit tax event) lack transactional guarantees
- Blocks: Consistency guarantees across microservices
- Recommendation: Implement compensating transactions (Axon Framework or custom saga orchestrator)

## Test Coverage Gaps

**No Integration Tests for Kafka Messaging:**
- What's not tested: End-to-end message flow from producer to consumer; error scenarios (broker down, malformed messages)
- Files: `portfolioService/src/test/java/` lacks Kafka integration tests (TestContainers for Kafka available in pom.xml but not used)
- Risk: Silent failures in async messaging go undetected until production
- Priority: High

**Limited Security Testing:**
- What's not tested: Unauthorized API access, JWT validation edge cases (expired tokens, forged JWTs), role-based access control
- Files: `portfolioService/src/test/java/` has 64 test files but security tests are minimal
- Risk: Authentication/authorization bypass vulnerabilities
- Priority: High

**No Performance/Load Tests:**
- What's not tested: Pagination limits, N+1 query impact at scale, SSE broadcaster throughput, Kafka consumer lag
- Files: No JMeter or Gatling scripts in repo
- Risk: Production outages under expected load
- Priority: Medium

**SSE Concurrency and Timeout Scenarios Untested:**
- What's not tested: Concurrent client registration/unregistration, long-lived emitter timeout, network failure recovery
- Files: `portfolioService/src/test/java/` lacks integration tests for `KafkaConsumer`
- Risk: Memory leaks, client connection exhaustion, silent failures in production
- Priority: Medium

---

*Concerns audit: 2026-08-22*
