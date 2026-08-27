<!-- GSD:project-start source:PROJECT.md -->

## Project

**Asset Compass**

Asset Compass is a personal investment dashboard that consolidates accounts from multiple brokers/exchanges — Binance, IOL (InvertirOnline), and Nexo — into a single unified view. It has a dual purpose that matters equally: (1) genuine personal utility, seeing all investments in one place, and eventually alerts and trading; and (2) a technical portfolio piece demonstrating professional-level engineering practices (microservices, CI/CD, BI, security, testing, containers, orchestration). Scope and stack decisions get evaluated against both goals, not just one.

It's a JHipster-generated microservices monorepo: `gateway` (React/TypeScript PWA + reverse proxy + OAuth2/OIDC via Keycloak), `portfolioService` (the active core — broker connectors, financial data model, position/valuation calculations), `notificationService` and `tradingService` (scaffolded, empty — later phases).

**Core Value:** A unified, accurate, read-only view of all investments across Binance, IOL, and Nexo. Everything else — alerts, hardening, trade execution — depends on this being correct first.

### Constraints

- **Git workflow**: GSD may create local git commits (they'll carry the user's own git identity, no AI byline) but must never push, force-push, or otherwise touch the remote — the human handles all remote operations manually. This relaxes an earlier "never run git commands" rule specifically for GSD-managed local commits.
- **No unprompted assumptions**: Ask before deciding anything not explicitly covered by the task/issue at hand — especially anything flagged as an open question (e.g., Keycloak/Kafka in local dev).
- **Explore before editing**: Always check actual repo state before making changes; never assume file paths or existing behavior — things may have changed since context was last captured.
- **Tech stack is locked in**: Scaffolded via JHipster/JDL (Java 21, Spring Boot, Maven, React/TS, Postgres, Redis, Kafka, Keycloak, Kubernetes, GitHub Actions) — not open for reconsideration within this milestone.
- **ID strategy**: UUIDs (not DB-generated) as primary keys for all entities, deliberately, so ID type/strategy matches exactly between the gateway and the owning service.
- **Real-money safety (future)**: When Phase 4 (trade execution) is eventually reached, dry-run mode, manual confirmation, and hard limits are non-negotiable safeguards — not optional hardening.

<!-- GSD:project-end -->

<!-- GSD:stack-start source:codebase/STACK.md -->

## Technology Stack

## Languages

- Java 21 - Backend services (portfolioService, tradingService, notificationService, gateway)
- TypeScript - Frontend/React application
- YAML - Configuration and deployment manifests
- Bash - Shell scripts for Docker and Kubernetes

## Runtime

- JVM (OpenJDK Temurin 21)
- Node.js v24.18.0 (for frontend build tooling)
- Docker (container runtime)
- Kubernetes 1.x+ (orchestration)
- Maven 3.2.5 (Java dependencies)
- npm 11.18.0 (Node dependencies)

## Frameworks

- Spring Boot 4.0.7 - Services: `portfolioService`, `tradingService`, `notificationService`
- Spring Boot 3.5.15 - Gateway service (`gateway`)
- JHipster Framework 9.1.0 - Three microservices
- JHipster Framework 8.12.0 - Gateway application
- Spring Web MVC (blocking) - `portfolioService`, `tradingService`, `notificationService`
- Spring WebFlux (reactive) - `gateway` for high-throughput request handling
- Spring Data JPA - ORM for microservices
- Spring Data R2DBC - Reactive database access for gateway
- React 18.x - UI framework for gateway
- Webpack 5.x - Module bundler (`gateway/webpack/webpack.*.js`)
- TypeScript 5.x - Frontend type safety
- Spring Boot Test - Unit/integration testing foundation
- JUnit 5 - Test framework
- TestContainers - Docker-based test infrastructure
- Vitest - Frontend unit testing
- Cypress 14.x - End-to-end testing
- Maven (compile, package, deploy)
- Spotless 3.8.0 - Code formatting enforcement
- Checkstyle 13.7.0 - Code style validation
- ESLint - Frontend linting
- Webpack Dev Server - Frontend development server

## Key Dependencies

- Spring Cloud Dependencies 2025.1.2 (services) / 2025.0.0 (gateway)
- Spring Cloud Stream - Kafka integration
- Spring Cloud Circuit Breaker (Resilience4j) - Fault tolerance
- spring-boot-starter-oauth2-client - OAuth2 client
- spring-boot-starter-oauth2-resource-server - OAuth2 resource protection
- spring-boot-starter-security - Security framework
- PostgreSQL JDBC Driver - Database connectivity
- Liquibase 5.x - Database schema management (`spring-boot-starter-liquibase`)
- Redisson 4.6.1 - Redis client for caching and distributed operations
- HikariCP - Connection pooling
- Micrometer Registry Prometheus - Prometheus metrics export
- SpringDoc OpenAPI 3.0.3 - API documentation generation
- MapStruct 1.6.3 - DTO mapping
- Jackson (multiple modules) - JSON serialization
- Hibernate ORM 6.x - JPA implementation
- Hibernate Validator - Bean validation
- JAXB Runtime - XML binding support
- Spring Cloud Stream Kafka Binder - Kafka integration
- Apache Kafka - Message broker (Kafka native 4.3.1 in Kubernetes)
- Micrometer - Metrics collection
- Prometheus - Metrics storage and querying
- Spring Cloud Sleuth - Distributed tracing (optional via Maven profile)
- Zipkin - Trace visualization (optional, localhost:9411 in dev)
- Caffeine - Local in-memory cache
- Redisson - Distributed Redis cache
- Hibernate JCache - Second-level cache support
- ArchUnit 1.4.2 - Architecture testing
- Palantir Java Format 2.97.0 - Code formatting
- Modernizer Maven Plugin 3.4.0 - Detect outdated Java usage
- SonarQube Maven Plugin 5.7.0.6970 - Code quality analysis
- Jib Maven Plugin 3.5.1 - Docker image building
- Eclipse Temurin 25 JRE - Production container base image

## Configuration

- Spring Profiles:
- Key Configuration Files:
- Liquibase changesets in `src/main/resources/db/changelog/`
- Contexts: `dev`, `faker` (development), `prod` (production)
- Automatic schema initialization enabled
- `src/main/docker/app.yml` - Service orchestration for development
- `src/main/docker/postgresql.yml` - PostgreSQL service
- `src/main/docker/redis.yml` - Redis cache service
- `src/main/docker/kafka.yml` - Kafka message broker
- `src/main/docker/keycloak.yml` - OAuth2/OIDC authentication server
- `src/main/docker/services.yml` - External services (Zipkin, Control Center)
- `kubernetes/*/service.yml` - Kubernetes Service manifests
- `kubernetes/*/deployment.yml` - Service deployments
- `kubernetes/*/postgresql.yml` - PostgreSQL StatefulSet per service
- `kubernetes/messagebroker-k8s/kafka.yml` - Kafka deployment
- `kubernetes/namespace.yml` - Namespace definition
- `kubernetes/kustomization.yml` - Kustomize configuration
- `kubernetes/skaffold.yml` - Skaffold development workflow

## Platform Requirements

- Java 21 (JDK)
- Maven 3.2.5+
- Node.js 24.18.0
- npm 11.18.0
- Docker Desktop or Docker Engine
- Docker Compose (for local development)
- PostgreSQL 15+ (can run in Docker)
- Redis (can run in Docker)
- Kafka (can run in Docker)
- Keycloak 26.7.0 (for auth, can run in Docker)
- Kubernetes 1.20+ cluster
- Container registry (for Docker images)
- PostgreSQL 15+ (managed or self-hosted)
- Redis (managed or self-hosted)
- Apache Kafka (managed or self-hosted)
- Keycloak or external OIDC provider
- Ingress controller (for routing)
- GitHub Actions (GitHub Runners: ubuntu-latest)
- Maven for builds
- Spotless for code formatting checks
- JUnit 5 for unit testing

## Build & Deployment

- Maven multi-module: Each service has independent `pom.xml`
- Docker Image Build:
- Frontend Bundle:
- Docker containers (via Docker Registry)
- Kubernetes manifests (via kubectl or Kustomize)
- GitHub Actions CI/CD pipeline
- Local development (Docker Compose)

<!-- GSD:stack-end -->

<!-- GSD:conventions-start source:CONVENTIONS.md -->

## Conventions

## Naming Patterns

- Entity classes: PascalCase, single name (e.g., `Asset.java`, `Operation.java`)
- REST controllers: `*Resource` suffix (e.g., `ValuationResource.java`)
- Service interfaces: `*Service` suffix (e.g., `BrokerService.java`)
- Service implementations: `*ServiceImpl` in `service/impl/` directory (e.g., `BrokerServiceImpl.java`)
- Query services: `*QueryService` suffix (e.g., `PositionQueryService.java`)
- Data Transfer Objects: `*DTO` suffix (e.g., `OperationDTO.java`)
- Repositories: `*Repository` suffix (e.g., `BrokerRepository.java`)
- Unit tests: `*UnitTest` suffix (e.g., `SecurityUtilsUnitTest.java`)
- Integration tests: `*IT` suffix (e.g., `ValuationResourceIT.java`)
- Test configurations: PascalCase without suffix (e.g., `DatabaseTestcontainer.java`)
- camelCase naming (e.g., `save`, `update`, `partialUpdate`, `findOne`, `delete`)
- CRUD operations follow standard naming: `save()`, `update()`, `partialUpdate()`, `findOne(id)`, `delete(id)`
- Query methods: descriptive camelCase (e.g., `getCurrentUserLogin()`)
- Factory methods in tests: `createEntity()`, `createUpdatedEntity()`
- Private methods: camelCase with descriptive intent (e.g., `logAround()`, `buildErrorResponse()`)
- Local variables: camelCase
- Constants: UPPERCASE_WITH_UNDERSCORES (e.g., `DEFAULT_SNAPSHOT_DATE`, `ENTITY_API_URL`)
- Static loggers: `LOG` or `LOGGER` (use SLF4J LoggerFactory)
- Field references: final keyword for immutable dependencies
- Enums: PascalCase (e.g., `OperationType.java`)
- Generic types: Single letter capitals in angle brackets (e.g., `Optional<UUID>`)
- DTOs/Domain objects: Plural suffixes avoided; use singular entity names

## Code Style

- File: `.prettierrc` in each module
- Java tab width: 4 spaces
- Non-Java tab width: 2 spaces
- Line length limit: 140 characters
- Single quotes in non-Java files
- Arrow parens: avoid (ES6 syntax)
- Bracket placement: same line
- Tool: Maven Checkstyle Plugin (version 13.7.0)
- Also uses: Spotless Maven Plugin for unified formatting
- Format validation runs in `mvn clean install` and CI
- SuppressWarnings annotations used selectively:
- No IDE-specific configurations in source code

## Import Organization

- No aliases configured; full package paths used throughout
- MapStruct annotation processor automatically generates mappers

## Error Handling

- Custom `BadRequestAlertException` extends Spring's `ErrorResponseException`
- Error responses use RFC 7807 Problem Detail format:
- Exception handling in REST resources:
- Business logic exceptions are thrown early with descriptive messages
- Input validation using Jakarta `@NotNull`, `@Valid` annotations
- No generic catch-all exception handlers; Spring handles exceptions globally

## Logging

- `DEBUG`: Method entry/exit and business logic flow (guarded with `isDebugEnabled()` check)
- `INFO`: Not commonly used; avoid INFO level in application code
- `ERROR`: Exception catching with full stack traces in dev profile, summary in production
- `WARN`: Important business rule violations
- Entry logging: `LOG.debug("Request to save Entity : {}", dto);`
- Exit logging: `LOG.debug("Request to get Entity : {}", id);`
- Error logging in AOP aspect (`LoggingAspect.java`):
- Aspect-based logging for all `@Service`, `@Repository`, and `@RestController` classes
- Location: `com.assetcompass.portfolio.aop.logging.LoggingAspect`
- Automatically logs method entry/exit and exceptions
- Pointcuts target: repositories, services, REST endpoints
- Only active in dev profile to avoid performance overhead

## Comments

- Class-level JavaDoc for public classes required
- Method-level JavaDoc for public interface methods (parameters, return values, exceptions)
- Inline comments for non-obvious business logic only
- No obvious/redundant comments (avoid "// increment counter")
- Comments should explain "why" not "what"
- Method parameters: `@param name description`
- Return values: `@return description`
- Exceptions: `@throws ExceptionType reason`
- Cross-references: `{@link ClassName#methodName}`

## Function Design

- Keep methods under 30 lines when possible
- Complex business logic extracted to separate private methods
- Use method extraction if a single method has multiple responsibilities
- Constructor injection preferred (final fields)
- Parameter validation with annotations (`@NotNull`, `@Valid`)
- Use DTOs for REST endpoints, domain objects internally
- Limit to 5+ parameters; use objects to group related parameters
- `Optional<T>` for queries that may not find results (e.g., `findOne()`)
- `void` for delete operations
- DTOs for REST responses
- Domain objects in service implementations (mappers handle DTO conversion)
- Early return for error conditions
- Happy path at end of method
- Logging at method entry/exit for DEBUG level

## Module Design

- Service interfaces expose public API contracts
- Service implementations are internal (impl package)
- REST resources are public endpoints
- DTOs are transfer layer contracts
- Domain classes encapsulated within service modules
- Not used; explicit imports required
- `package-info.java` files used for package-level documentation
- **Layering**: Config → Web → Service → Persistence → Domain
- **No circular dependencies** enforced by ArchUnit tests
- **Transactionality**: Only service layer marked `@Transactional`
- **Database access**: Only through repositories, never direct JPA in REST/service

<!-- GSD:conventions-end -->

<!-- GSD:architecture-start source:ARCHITECTURE.md -->

## Architecture

## System Overview

```text

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

- **Service autonomy**: Each service owns its PostgreSQL database (no shared schema)
- **API-first**: REST via Spring Boot + Spring Web MVC; request/response via DTOs
- **Event-driven**: Kafka topics for inter-service notifications (async notifications, portfolio updates)
- **Spring Cloud integration**: Feign clients for REST-to-REST calls, Spring Cloud Config for externalized config
- **Security**: OAuth2/OIDC token validation via Keycloak; roles/permissions enforce access control
- **Framework**: JHipster-generated Spring Boot 4.0.7, Java 21, Maven builds
- **Caching**: Redis L2 cache (configured via `cacheProvider redis`)

## Layers

- Purpose: HTTP API endpoints, request validation, response formatting
- Location: `*/src/main/java/.../web/rest/*Resource.java`
- Contains: @RestController classes, one per domain entity (AssetResource, OperationResource, etc.)
- Depends on: Service layer (AssetService, AssetQueryService), Repository, DTOs
- Used by: Gateway (reverse proxy), external clients (REST calls)
- Pattern: Spring MVC @RestController, @RequestMapping annotations, ResponseEntity wrapper
- Purpose: Business logic, entity lifecycle, DTO ↔ entity mapping, transaction boundaries
- Location: `*/src/main/java/.../service/` and `*/src/main/java/.../service/impl/`
- Contains: Service interfaces + implementations, QueryService classes
- Depends on: Repository layer, MapStruct mappers, domain entities
- Used by: REST controllers, other services (via Feign clients)
- Pattern: Dual-service per entity (e.g., AssetService + AssetQueryService for read/write split)
- Purpose: Filtered/paginated queries with complex WHERE clauses, sort, pagination
- Location: `*/src/main/java/.../service/*QueryService.java`
- Contains: Criteria-based queries (AssetCriteria, OperationCriteria, etc.)
- Depends on: Repository, Criteria objects
- Used by: REST controllers (@GetMapping endpoints with filtering/sorting)
- Pattern: QueryService accepts Criteria object → Specification → Repository.findAll(spec, page)
- Purpose: Spring Data JPA persistence, query delegation to database
- Location: `*/src/main/java/.../repository/*Repository.java`
- Contains: JpaRepository<Entity, UUID> subclasses, optional custom @Query methods
- Depends on: Hibernate/JPA, database (PostgreSQL)
- Used by: Service layer
- Pattern: Spring Data JPA repositories (auto-generated CRUD + pagination/filtering via Specification)
- Purpose: JPA entity definitions, database schema mapping, business invariants
- Location: `*/src/main/java/.../domain/*.java`
- Contains: @Entity classes with @ManyToOne, @OneToMany relationships, enums (OperationType, TaxType, etc.)
- Depends on: Jakarta Persistence API (jakarta.persistence.*)
- Used by: Repository layer (ORM mapping), Service layer (business logic)
- Pattern: JPA entity + Lombok @Getter/@Setter, @ToString; AbstractAuditingEntity superclass (createdBy, createdDate, lastModifiedBy, lastModifiedDate)
- Purpose: Spring configuration, properties management, cross-cutting setup
- Location: `*/src/main/java/.../config/*Configuration.java`
- Contains: DatabaseConfiguration, CacheConfiguration, SecurityConfiguration, WebConfigurer, LiquibaseConfiguration
- Depends on: Spring Boot configuration, external integrations (Redis, PostgreSQL)
- Used by: Spring context initialization
- Pattern: @Configuration classes with @Bean factories, ConfigurationProperties for externalized values
- Purpose: OAuth2/OIDC authentication, token validation, authorization rules
- Location: `*/src/main/java/.../security/SecurityConfiguration.java`, `*/src/main/java/.../security/oauth2/...`
- Contains: SecurityConfiguration (chain of filters, CORS, CSRF), OAuth2ResourceServerConfig, JwtDecoder beans
- Depends on: Spring Security, Keycloak (external)
- Used by: HTTP request filter chain
- Pattern: Spring Security 6 with OAuth2 Resource Server, JWT token validation against public key
- Purpose: Async event communication between services
- Location: `*/src/main/java/.../broker/KafkaConsumer.java`
- Contains: Consumer<String> implementation, SSE emitter registration/broadcast
- Depends on: Spring Cloud Stream, Kafka
- Used by: PortfolioService REST endpoints (SSE streaming of portfolio updates)
- Pattern: Functional programming (Consumer<T>), Server-Sent Events (SSE) for real-time client push
- Purpose: Logging, metrics, request tracing
- Location: `*/src/main/java/.../aop/logging/LoggingAspect.java`
- Contains: @Aspect class with @Around pointcuts
- Depends on: Spring AOP
- Used by: Spring context (automatic pointcut weaving)
- Pattern: @Aspect with @Pointcut annotations, method entry/exit logging

## Data Flow

### Primary Request Path (Read Portfolio)

### Create Operation (Write Portfolio)

### Async Event Flow (Kafka → SSE)

- **Transactional**: Each service manages transaction boundaries via @Transactional on service methods
- **Eventual consistency**: Kafka events are fire-and-forget; no global transaction across services
- **Caching**: Redis L2 cache on frequently-read entities (Asset, Broker); invalidated on writes
- **Database constraints**: PostgreSQL foreign keys enforce referential integrity within each service

## Key Abstractions

- Purpose: Separation of complex filtered queries from simple CRUD
- Examples: `AssetQueryService`, `OperationQueryService`, `PositionQueryService`
- Pattern: Accepts @RequestParam criteria fields → builds Specification → delegates to Repository.findAll(spec, pageable)
- Benefit: DRY (don't repeat WHERE clause logic); testable in isolation
- Purpose: Decouple REST API shape from database schema
- Examples: MapStruct mappers in `service/mapper/` (PositionMapper, AssetMapper, etc.)
- Pattern: Interface with @Mapping annotations; MapStruct generates implementation at compile time
- Benefit: Allows API evolution without schema migration; nested DTOs reduce round-trips
- Purpose: Type-safe query filter definitions
- Examples: `OperationCriteria`, `AssetCriteria`, `PositionCriteria` in `service/criteria/`
- Pattern: Setter fields for each filterable column; passed to QueryService
- Benefit: Clear query semantics; IDE autocomplete; no string-based WHERE
- Purpose: Abstraction of data access
- Examples: `JpaRepository<Entity, UUID>` subclasses in `repository/`
- Pattern: Spring Data JPA generates queries from method signatures; custom @Query for complex SQL
- Benefit: Testability (mock repository); portability (swap JpaRepository for MongoDB, etc.)
- Purpose: Dynamic query building
- Pattern: Criteria object → QueryService → Specification<Entity> → Repository.findAll(spec, pageable)
- Benefit: Dynamic WHERE clauses without SQL concatenation (SQL injection safe)

## Entry Points

- Location: `gateway/src/main/java/com/assetcompass/gateway/GatewayApp.java` (main method)
- Triggers: Application startup (java -jar gateway.jar or K8s pod initialization)
- Responsibilities:
- Location: `portfolioService/src/main/java/com/assetcompass/portfolio/PortfolioServiceApp.java` (main method)
- Triggers: Application startup (K8s pod for portfolio-service)
- Responsibilities:
- Location: `portfolioService/src/main/java/com/assetcompass/portfolio/web/rest/AssetResource.java`, line 29 @GetMapping("/")
- Triggers: HTTP GET /api/assets
- Responsibilities: Extract query params → call AssetQueryService → map DTOs → return JSON
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

### Synchronous REST Calls for Non-Real-Time Events

### Putting Business Logic in Controllers

### Lazy Loading in Response Marshaling

## Error Handling

- **BadRequestAlertException** (`web/rest/errors/BadRequestAlertException.java`): Validation failures → HTTP 400 with error key
- **ResponseStatusException** (Spring framework): Route to appropriate HTTP status (404 Not Found, 500 Internal Server Error)
- **Spring Security exceptions** (AuthenticationException, AccessDeniedException) → HTTP 401/403
- **@RestControllerAdvice** (if configured, not yet seen in exploration) → centralized error response mapping
- **Kafka consumer failures** (`KafkaConsumer.accept()` line 42) → catch IOException, log debug, continue (graceful degradation; SSE client reconnects)

```json

```

## Cross-Cutting Concerns

<!-- GSD:architecture-end -->

<!-- GSD:skills-start source:skills/ -->

## Project Skills

No project skills found. Add skills to any of: `.claude/skills/`, `.agents/skills/`, `.cursor/skills/`, `.github/skills/`, or `.codex/skills/` with a `SKILL.md` index file.
<!-- GSD:skills-end -->

<!-- GSD:workflow-start source:GSD defaults -->

## GSD Workflow Enforcement

Before using Edit, Write, or other file-changing tools, start work through a GSD command so planning artifacts and execution context stay in sync.

Use these entry points:

- `/gsd-quick` for small fixes, doc updates, and ad-hoc tasks
- `/gsd-debug` for investigation and bug fixing
- `/gsd-execute-phase` for planned phase work

Do not make direct repo edits outside a GSD workflow unless the user explicitly asks to bypass it.
<!-- GSD:workflow-end -->

<!-- GSD:profile-start -->

## Developer Profile

> Profile not yet configured. Run `/gsd-profile-user` to generate your developer profile.
> This section is managed by `generate-claude-profile` -- do not edit manually.
<!-- GSD:profile-end -->
