<!-- refreshed: 2026-08-22 -->
# Architecture

**Analysis Date:** 2026-08-22

## System Overview

Asset Compass is a **microservices architecture** consolidating multi-broker portfolio positions into a unified dashboard. Four independent services communicate via REST (synchronous, through API Gateway) and Kafka (asynchronous events). Service discovery is native to Kubernetes DNS (no Eureka/Consul registry).

```text
┌─────────────────────────────────────────────────────────────────────────┐
│                     Gateway (API Gateway & Web)                         │
│                  `gateway/src/main/java/...`                            │
│                    React + TypeScript PWA frontend                       │
│                    OAuth2/OIDC client (Keycloak)                        │
│                    Reverse proxy → microservices                         │
└────────────┬──────────────────────┬──────────────────────┬──────────────┘
             │                      │                      │
             ▼                      ▼                      ▼
   ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
   │ PortfolioService │  │  TradingService  │  │NotificationSvc   │
   │ (Phase 1 - Core) │  │ (Phase 4 - TODO) │  │ (Phase 2 - TODO) │
   │ `portfolioService`│ │ `tradingService` │  │ `notification`   │
   │ 10 entities      │  │ No entities yet  │  │ No entities yet  │
   └────────┬─────────┘  └────────┬─────────┘  └────────┬─────────┘
            │                     │                     │
            └─────────────────────┴─────────────────────┘
                          │
                          ▼
            ┌─────────────────────────────────┐
            │ Message Broker (Apache Kafka)   │
            │ For async event communication   │
            └─────────────────────────────────┘
                          │
                          ▼
            ┌─────────────────────────────────┐
            │ Data Persistence Layer          │
            │ PostgreSQL (1 DB per service)   │
            │ + Redis (L2 Cache)              │
            │ + Liquibase (migrations)        │
            └─────────────────────────────────┘
```

## Component Responsibilities

| Component | Responsibility | File |
|-----------|----------------|------|
| **Gateway** | HTTP entry point, JWT/OAuth2 validation, request routing, React frontend | `gateway/src/main/java/com/assetcompass/gateway/` |
| **PortfolioService** | Portfolio domain logic (positions, operations, valuations, tax events, broker data) | `portfolioService/src/main/java/com/assetcompass/portfolio/` |
| **TradingService** | Trade order execution, fills, slippage tracking (Phase 4 — no entities yet) | `tradingService/src/main/java/com/assetcompass/trading/` |
| **NotificationService** | Alert rules, notifications, webhooks (Phase 2 — no entities yet) | `notificationService/src/main/java/com/assetcompass/notification/` |
| **Kafka Consumer** | Receives async events from other services, broadcasts via SSE | `*/src/main/java/.../broker/KafkaConsumer.java` |
| **REST Controllers** | HTTP endpoints, request/response marshaling, validation | `*/src/main/java/.../web/rest/*Resource.java` |
| **Service Layer** | Business logic, transactions, DTO mapping | `*/src/main/java/.../service/*Service.java` and `*QueryService.java` |
| **Repository Layer** | JPA data access, query building | `*/src/main/java/.../repository/*Repository.java` |
| **Domain Entities** | JPA entities, database schema definitions | `*/src/main/java/.../domain/*.java` |

## Pattern Overview

**Overall:** Microservices pattern with JHipster scaffolding, following **layered architecture** per service:

**Key Characteristics:**
- **Service autonomy**: Each service owns its PostgreSQL database (no shared schema)
- **API-first**: REST via Spring Boot + Spring Web MVC; request/response via DTOs
- **Event-driven**: Kafka topics for inter-service notifications (async notifications, portfolio updates)
- **Spring Cloud integration**: Feign clients for REST-to-REST calls, Spring Cloud Config for externalized config
- **Security**: OAuth2/OIDC token validation via Keycloak; roles/permissions enforce access control
- **Framework**: JHipster-generated Spring Boot 4.0.7, Java 21, Maven builds
- **Caching**: Redis L2 cache (configured via `cacheProvider redis`)

## Layers

**Web Layer (REST Controllers):**
- Purpose: HTTP API endpoints, request validation, response formatting
- Location: `*/src/main/java/.../web/rest/*Resource.java`
- Contains: @RestController classes, one per domain entity (AssetResource, OperationResource, etc.)
- Depends on: Service layer (AssetService, AssetQueryService), Repository, DTOs
- Used by: Gateway (reverse proxy), external clients (REST calls)
- Pattern: Spring MVC @RestController, @RequestMapping annotations, ResponseEntity wrapper

**Service Layer:**
- Purpose: Business logic, entity lifecycle, DTO ↔ entity mapping, transaction boundaries
- Location: `*/src/main/java/.../service/` and `*/src/main/java/.../service/impl/`
- Contains: Service interfaces + implementations, QueryService classes
- Depends on: Repository layer, MapStruct mappers, domain entities
- Used by: REST controllers, other services (via Feign clients)
- Pattern: Dual-service per entity (e.g., AssetService + AssetQueryService for read/write split)

**Query Service Layer:**
- Purpose: Filtered/paginated queries with complex WHERE clauses, sort, pagination
- Location: `*/src/main/java/.../service/*QueryService.java`
- Contains: Criteria-based queries (AssetCriteria, OperationCriteria, etc.)
- Depends on: Repository, Criteria objects
- Used by: REST controllers (@GetMapping endpoints with filtering/sorting)
- Pattern: QueryService accepts Criteria object → Specification → Repository.findAll(spec, page)

**Repository Layer:**
- Purpose: Spring Data JPA persistence, query delegation to database
- Location: `*/src/main/java/.../repository/*Repository.java`
- Contains: JpaRepository<Entity, UUID> subclasses, optional custom @Query methods
- Depends on: Hibernate/JPA, database (PostgreSQL)
- Used by: Service layer
- Pattern: Spring Data JPA repositories (auto-generated CRUD + pagination/filtering via Specification)

**Domain Layer (Entity Model):**
- Purpose: JPA entity definitions, database schema mapping, business invariants
- Location: `*/src/main/java/.../domain/*.java`
- Contains: @Entity classes with @ManyToOne, @OneToMany relationships, enums (OperationType, TaxType, etc.)
- Depends on: Jakarta Persistence API (jakarta.persistence.*)
- Used by: Repository layer (ORM mapping), Service layer (business logic)
- Pattern: JPA entity + Lombok @Getter/@Setter, @ToString; AbstractAuditingEntity superclass (createdBy, createdDate, lastModifiedBy, lastModifiedDate)

**Configuration Layer:**
- Purpose: Spring configuration, properties management, cross-cutting setup
- Location: `*/src/main/java/.../config/*Configuration.java`
- Contains: DatabaseConfiguration, CacheConfiguration, SecurityConfiguration, WebConfigurer, LiquibaseConfiguration
- Depends on: Spring Boot configuration, external integrations (Redis, PostgreSQL)
- Used by: Spring context initialization
- Pattern: @Configuration classes with @Bean factories, ConfigurationProperties for externalized values

**Security Layer:**
- Purpose: OAuth2/OIDC authentication, token validation, authorization rules
- Location: `*/src/main/java/.../security/SecurityConfiguration.java`, `*/src/main/java/.../security/oauth2/...`
- Contains: SecurityConfiguration (chain of filters, CORS, CSRF), OAuth2ResourceServerConfig, JwtDecoder beans
- Depends on: Spring Security, Keycloak (external)
- Used by: HTTP request filter chain
- Pattern: Spring Security 6 with OAuth2 Resource Server, JWT token validation against public key

**Message Broker (Kafka):**
- Purpose: Async event communication between services
- Location: `*/src/main/java/.../broker/KafkaConsumer.java`
- Contains: Consumer<String> implementation, SSE emitter registration/broadcast
- Depends on: Spring Cloud Stream, Kafka
- Used by: PortfolioService REST endpoints (SSE streaming of portfolio updates)
- Pattern: Functional programming (Consumer<T>), Server-Sent Events (SSE) for real-time client push

**AOP/Cross-Cutting:**
- Purpose: Logging, metrics, request tracing
- Location: `*/src/main/java/.../aop/logging/LoggingAspect.java`
- Contains: @Aspect class with @Around pointcuts
- Depends on: Spring AOP
- Used by: Spring context (automatic pointcut weaving)
- Pattern: @Aspect with @Pointcut annotations, method entry/exit logging

## Data Flow

### Primary Request Path (Read Portfolio)

1. **HTTP Request enters Gateway** (`gateway/src/main/java/com/assetcompass/gateway/`)
   - Client requests: `GET /api/positions?page=0&size=20&sort=lastSyncedAt,desc`
   - Gateway validates JWT token via SecurityConfiguration

2. **Route to PortfolioService** (reverse proxy)
   - Gateway forwards to `http://portfolio-service:8081/api/positions`

3. **REST Controller receives request** (`portfolioService/src/main/java/com/assetcompass/portfolio/web/rest/PositionResource.java`)
   - @GetMapping endpoint extracts query params (page, size, sort, filters)
   - Builds PositionCriteria object

4. **Service layer executes query** (`portfolioService/src/main/java/com/assetcompass/portfolio/service/PositionQueryService.java`)
   - PositionQueryService.findByCriteria(criteria, pageable) is called
   - Builds JPA Specification from criteria

5. **Repository executes query** (`portfolioService/src/main/java/com/assetcompass/portfolio/repository/PositionRepository.java`)
   - PositionRepository.findAll(spec, pageable) retrieves data from PostgreSQL
   - Hibernate ORM hydrates Position entities with lazy-loaded relationships

6. **MapStruct mapper converts** (`portfolioService/src/main/java/com/assetcompass/portfolio/service/mapper/PositionMapper.java`)
   - Entity → DTO (PositionDTO with nested AssetDTO, BrokerAccountDTO)

7. **Response serialized** (REST controller)
   - Page<PositionDTO> is wrapped in ResponseEntity with HTTP 200
   - Spring returns JSON: `{ "content": [...], "totalElements": N, "totalPages": M }`

### Create Operation (Write Portfolio)

1. **Client POST** `POST /api/operations` with OperationDTO JSON body
2. **REST controller** (`OperationResource.java`) validates @Valid OperationDTO
3. **Service layer** (`OperationService.java`) invokes save(operationDTO)
   - MapStruct converts DTO → Operation entity
   - Resolves foreign keys (BrokerAccount, Asset, closesOperation)
   - Publishes event to Kafka topic (e.g., `portfolio-operation-created`)
4. **Repository** saves Operation to PostgreSQL
5. **Kafka event** triggers downstream: TradingService listens → rebalance calculations; NotificationService listens → alert rules check

### Async Event Flow (Kafka → SSE)

1. **PortfolioService publishes event** when operation created (KafkaProducer sends to topic `portfolio-events`)
2. **PortfolioService consumes own event** (KafkaConsumer receives from `portfolio-events` topic)
3. **KafkaConsumer.accept(String message)** broadcasts to all registered SSE emitters
4. **Client SSE listener** receives real-time update → UI re-renders positions, valuations

**State Management:**
- **Transactional**: Each service manages transaction boundaries via @Transactional on service methods
- **Eventual consistency**: Kafka events are fire-and-forget; no global transaction across services
- **Caching**: Redis L2 cache on frequently-read entities (Asset, Broker); invalidated on writes
- **Database constraints**: PostgreSQL foreign keys enforce referential integrity within each service

## Key Abstractions

**QueryService Pattern:**
- Purpose: Separation of complex filtered queries from simple CRUD
- Examples: `AssetQueryService`, `OperationQueryService`, `PositionQueryService`
- Pattern: Accepts @RequestParam criteria fields → builds Specification → delegates to Repository.findAll(spec, pageable)
- Benefit: DRY (don't repeat WHERE clause logic); testable in isolation

**DTO/Entity Mapper:**
- Purpose: Decouple REST API shape from database schema
- Examples: MapStruct mappers in `service/mapper/` (PositionMapper, AssetMapper, etc.)
- Pattern: Interface with @Mapping annotations; MapStruct generates implementation at compile time
- Benefit: Allows API evolution without schema migration; nested DTOs reduce round-trips

**Criteria Objects:**
- Purpose: Type-safe query filter definitions
- Examples: `OperationCriteria`, `AssetCriteria`, `PositionCriteria` in `service/criteria/`
- Pattern: Setter fields for each filterable column; passed to QueryService
- Benefit: Clear query semantics; IDE autocomplete; no string-based WHERE

**Repository Pattern:**
- Purpose: Abstraction of data access
- Examples: `JpaRepository<Entity, UUID>` subclasses in `repository/`
- Pattern: Spring Data JPA generates queries from method signatures; custom @Query for complex SQL
- Benefit: Testability (mock repository); portability (swap JpaRepository for MongoDB, etc.)

**Spring Data Specification:**
- Purpose: Dynamic query building
- Pattern: Criteria object → QueryService → Specification<Entity> → Repository.findAll(spec, pageable)
- Benefit: Dynamic WHERE clauses without SQL concatenation (SQL injection safe)

## Entry Points

**HTTP Entry (Gateway):**
- Location: `gateway/src/main/java/com/assetcompass/gateway/GatewayApp.java` (main method)
- Triggers: Application startup (java -jar gateway.jar or K8s pod initialization)
- Responsibilities:
  - Bootstrap Spring context
  - Initialize security (load OAuth2 client config from Keycloak)
  - Start embedded Tomcat on port 8080
  - Expose React SPA + REST proxy endpoints

**PortfolioService Entry:**
- Location: `portfolioService/src/main/java/com/assetcompass/portfolio/PortfolioServiceApp.java` (main method)
- Triggers: Application startup (K8s pod for portfolio-service)
- Responsibilities:
  - Bootstrap Spring context, load configuration from `application.yml` (profile-driven)
  - Initialize PostgreSQL connection pool, Liquibase migrations
  - Start embedded Tomcat on port 8081
  - Initialize Kafka consumer for async events
  - Expose REST /api/assets, /api/operations, /api/positions, etc.

**REST Endpoint (Example: Asset retrieval):**
- Location: `portfolioService/src/main/java/com/assetcompass/portfolio/web/rest/AssetResource.java`, line 29 @GetMapping("/")
- Triggers: HTTP GET /api/assets
- Responsibilities: Extract query params → call AssetQueryService → map DTOs → return JSON

**Kafka Consumer Entry:**
- Location: `portfolioService/src/main/java/com/assetcompass/portfolio/broker/KafkaConsumer.java`
- Triggers: Spring Cloud Stream binds input stream to Kafka topic (auto-subscribed at boot)
- Responsibilities: Receive messages → broadcast to SSE emitters → push to connected clients

## Architectural Constraints

- **Threading:** Single-threaded event loop (standard Spring Boot Tomcat + async servlets for SSE); async tasks via @Scheduled (pool size 2, line 166-175 `application.yml`)
- **Global state:** KafkaConsumer.emitters HashMap stores SSE connections; not thread-safe by default (SseEmitter is per-thread); no other module-level singletons
- **Circular imports:** None detected; layered imports (web → service → repository → domain)
- **Database per service:** Each microservice owns PostgreSQL schema; no cross-database transactions (rely on Kafka for eventual consistency)
- **Kafka broker**: External dependency (Docker Compose or K8s pod); required for async communication; single Kafka cluster shared by all services
- **OAuth2 server:** External Keycloak instance; gateway/microservices validate JWT tokens against public key (no direct Keycloak calls at runtime)
- **Service discovery:** Kubernetes DNS only (no Eureka/Consul); services reference each other by Pod name or Kubernetes Service DNS (e.g., `portfolio-service.assetcompass.svc.cluster.local`)

## Anti-Patterns

### Shared Database Schema Across Services

**What happens:** Multiple microservices access a single PostgreSQL database schema, sharing tables.

**Why it's wrong:** Violates service autonomy (data coupling); makes it impossible to scale/redeploy a single service without coordinating schema migrations; changes to one service's entity can break others.

**Do this instead:** Give each service its own PostgreSQL database. Define relationships via REST/Feign calls or Kafka events. See `asset-compass.jdl` lines 220-231: `kubernetesNamespace assetcompass` → each service deploys independently with own DB instance.

### Synchronous REST Calls for Non-Real-Time Events

**What happens:** Service A makes a blocking REST call to Service B to notify of state change, waits for response.

**Why it's wrong:** Creates tight coupling; if Service B is slow, Service A blocks; cascading failures (B down → A fails too).

**Do this instead:** Use Kafka for async events. PortfolioService publishes `portfolio-operation-created` to Kafka topic; TradingService and NotificationService consume independently. See `KafkaConsumer.java`.

### Putting Business Logic in Controllers

**What happens:** REST controller directly queries repository, manipulates entities, no service layer.

**Why it's wrong:** Controllers become fat, testability suffers, transaction boundaries unclear, DTOs not decoupled from entities.

**Do this instead:** Route through Service layer (AssetService → AssetQueryService → Repository). Controllers call service, never touch repository directly. See `AssetResource.java` lines 45-50: constructor injects AssetService + AssetQueryService + AssetRepository.

### Lazy Loading in Response Marshaling

**What happens:** REST endpoint returns entity with @JsonProperty on lazy-loaded relationship, Hibernate tries to fetch it after session is closed.

**Why it's wrong:** Causes LazyInitializationException or N+1 queries; performance degradation.

**Do this instead:** Eager-fetch relationships in QueryService using @EntityGraph or explicit joins. Let MapStruct map only loaded relationships to DTO. Entities returned by repository already hydrated before session closes (open-session-in-view pattern is explicitly disabled in `application.yml` line 130: `spring.jpa.open-in-view: false`).

## Error Handling

**Strategy:** Exception translation to HTTP response codes, structured error payback with ErrorVM payload.

**Patterns:**
- **BadRequestAlertException** (`web/rest/errors/BadRequestAlertException.java`): Validation failures → HTTP 400 with error key
- **ResponseStatusException** (Spring framework): Route to appropriate HTTP status (404 Not Found, 500 Internal Server Error)
- **Spring Security exceptions** (AuthenticationException, AccessDeniedException) → HTTP 401/403
- **@RestControllerAdvice** (if configured, not yet seen in exploration) → centralized error response mapping
- **Kafka consumer failures** (`KafkaConsumer.accept()` line 42) → catch IOException, log debug, continue (graceful degradation; SSE client reconnects)

**Response format:**
```json
{
  "type": "https://jhipster.tech/error-keys/problem-with-message",
  "title": "Constraint violation",
  "status": 400,
  "detail": "Field 'quantity' must be positive",
  "path": "/api/operations",
  "message": "error.http.400"
}
```

## Cross-Cutting Concerns

**Logging:** AOP aspect (`aop/logging/LoggingAspect.java`) intercepts service/repository method entry/exit, logs parameters, return values, exceptions. Levels: DEBUG for entry/exit, ERROR for exceptions. No PII logging (passwords, tokens).

**Validation:** @Valid on @RequestBody in REST controllers triggers Bean Validation (jakarta.validation.*). Custom validators via Specification API for complex rules (e.g., "OperationType=SELL requires closesOperation to reference existing BUY").

**Authentication:** Spring Security filter chain (SecurityConfiguration) validates JWT bearer token from Authorization header, populates SecurityContext with UserDetails.

**Authorization:** @PreAuthorize annotations (if used) enforce role-based access (ROLE_USER, ROLE_ADMIN). OAuth2 audience claim must include "api://default" (line 220 `application.yml`).

**Metrics/Observability:** Micrometer/Actuator exposes metrics at `/management/prometheus` (Prometheus format). JHipster custom metrics (jhimetrics endpoint) for HTTP request timing, cache hits, database queries. Logs aggregated to stdout (12-factor app pattern).

---

*Architecture analysis: 2026-08-22*
