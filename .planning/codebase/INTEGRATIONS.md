# External Integrations

**Analysis Date:** 2026-08-22

## APIs & External Services

**Inter-Service Communication (Internal):**
- REST endpoints via gateway routing
  - Gateway proxies requests to microservices
  - Located in `gateway/src/main/java/com/assetcompass/gateway/config/SecurityConfiguration.java`
  - Uses Spring WebClient (reactive) for upstream calls
  - Circuit breaker enabled via Spring Cloud Resilience4j
  - Base URL pattern: `http://[service-name].[kubernetes-namespace].svc.cluster.local:[port]`

**OpenAPI Documentation:**
- SpringDoc OpenAPI 3.0.3 (`springdoc-openapi-starter-webmvc-api` 3.0.3)
  - Auto-generates OpenAPI specs at `/v3/api-docs`
  - Swagger UI at `/swagger-ui.html`
  - Configuration: `jhipster.api-docs.*` in `application.yml`
  - Enabled via `api-docs` profile

## Data Storage

**Databases:**
- **PostgreSQL 15+**
  - Per-service database instances (one per microservice)
  - Service locations:
    - portfolioService: `jdbc:postgresql://localhost:5432/portfolioService` (dev) / `portfolioservice-postgresql` (K8s)
    - tradingService: `jdbc:postgresql://localhost:5432/tradingService`
    - notificationService: `jdbc:postgresql://localhost:5432/notificationService`
    - gateway: `jdbc:postgresql://localhost:5432/gateway`
  - Connection Pool: HikariCP (auto-commit disabled, batch size 25)
  - ORM: Hibernate 6.x via Spring Data JPA (services) / R2DBC (gateway)
  - Driver: `org.postgresql:postgresql` (Maven dependency)

**Database Migrations:**
- Liquibase (Spring Boot starter)
  - Changesets: `src/main/resources/db/changelog/`
  - Managed via `spring.liquibase.contexts` (dev, faker, prod)
  - Auto-initialization: `spring.jpa.hibernate.ddl-auto: none` (Liquibase manages schema)

**File Storage:**
- Local filesystem only - No external object storage configured
- Build artifacts and logs stored locally or in container volumes

**Caching:**
- **Redis**
  - Location: `redis://localhost:6379` (dev) / `redis://redis.assetcompass.svc.cluster.local:6379` (K8s)
  - Client: Redisson 4.6.1
  - Configuration: `jhipster.cache.redis.*` in `application-[profile].yml`
  - Expiration: 3600 seconds (1 hour) by default
  - Cluster mode: Disabled (single instance)
  - Used for: User sessions, query results, distributed locks
  - Secondary cache: Caffeine (local in-memory fallback)

## Authentication & Identity

**Auth Provider:**
- **Keycloak 26.7.0**
  - OAuth2/OIDC server
  - Realm: `jhipster`
  - Admin credentials: username `admin`, password `admin` (dev only)
  - Server locations:
    - Dev: `http://localhost:9080/realms/jhipster`
    - Docker: `http://keycloak:9080/realms/jhipster`
    - Kubernetes: `http://keycloak.assetcompass.svc.cluster.local:9080/realms/jhipster`
  - Implementation approach:
    - Spring Security OAuth2 Client: `spring-boot-starter-oauth2-client`
    - OAuth2 Resource Server: `spring-boot-starter-oauth2-resource-server`
    - Client registration: `spring.security.oauth2.client.registration.oidc.*`
    - Configuration file: `src/main/docker/keycloak.yml`
    - Realm config: `src/main/docker/realm-config/` (dev)

**OAuth2 Configuration:**
- Client ID: `internal` (all services)
- Client Secret: `internal` (dev) / Environment variable (production)
- Scopes: `openid`, `profile`
- OIDC Issuer URI: From Keycloak realm
- Audience: `account`, `api://default`
- Token validation via JWT

## Messaging & Events

**Message Broker:**
- **Apache Kafka**
  - Version: Kafka Native 4.3.1 (Docker) / Latest via Spring Cloud Stream
  - Brokers:
    - Dev: `localhost:9092`
    - Docker: `kafka:9092`
    - Kubernetes: `jhipster-kafka.assetcompass.svc.cluster.local:9092`
  - Configuration: `src/main/resources/config/application-kafka.yml`
  - Spring Cloud Stream bindings:
    - Replication factor: 1
    - Auto-create topics: enabled
    - Topic: `sse-topic`
  - Consumer group: `[service-name]` (e.g., `portfolio-service`)
  - Serialization: String (text/plain content-type)
  - Integration: Spring Cloud Stream functions
    - `kafkaConsumer` - Inbound binding
    - `kafkaProducer` - Outbound binding
  - Enabled via: `kafka` profile in Spring profiles

**Event Pattern:**
- Asynchronous inter-service communication
- Services: `portfolioService`, `notificationService`, `tradingService`
- Message format: Text/plain strings
- Auto-offset-reset: `earliest` (on consumer group restart)

## Monitoring & Observability

**Metrics:**
- **Prometheus**
  - Metrics endpoint: `/management/prometheus`
  - Export format: Prometheus text format
  - Enabled via: Micrometer Registry Prometheus
  - Step: 60 seconds (export interval)
  - Exposed endpoints: All standard JVM/process/HTTP metrics
  - Custom tags: Application name added to all metrics

**Distributed Tracing:**
- **Zipkin** (optional, via Maven profile)
  - Endpoint: `http://localhost:9411/api/v2/spans` (dev)
  - Sampling probability: 100% (all traces captured in dev)
  - Implementation: Spring Cloud Sleuth
  - Baggage propagation: `x-request-id`, `x-ot-span-context`
  - Enabled for: Development environment (can be activated via Maven profile in production)

**Logs:**
- Standard: Console output with timestamps, levels, and application name
- Development: DEBUG level logging for JHipster and application code
- Production: INFO level logging
- Optional: Logstash integration (currently disabled)
  - When enabled: Socket-based log forwarding to Logstash
  - Ring buffer size: 512 events
- JSON format logging: Optional (disabled by default)

**Health Checks:**
- Kubernetes readiness probe: `/management/health/readiness` (port 8081/8080)
- Kubernetes liveness probe: `/management/health/liveness` (port 8081/8080)
- Actuator endpoints exposed:
  - `/management/health` - Overall health
  - `/management/prometheus` - Prometheus metrics
  - `/management/loggers` - Log level management
  - `/management/env` - Environment inspection
  - `/management/info` - Application info
  - `/management/liquibase` - Migration status

## Service Discovery

**Service Discovery:**
- **Kubernetes native DNS** (no Eureka/Consul)
  - Service discovery: Kubernetes Service DNS records
  - Pattern: `[service-name].[namespace].svc.cluster.local`
  - Examples:
    - `portfolioservice-postgresql.assetcompass.svc.cluster.local:5432`
    - `jhipster-kafka.assetcompass.svc.cluster.local:9092`
    - `redis.assetcompass.svc.cluster.local:6379`

## CI/CD & Deployment

**Hosting:**
- **Kubernetes 1.20+**
  - Namespace: `assetcompass`
  - Ingress: Kubernetes Ingress controller
  - Ingress domain: `assetcompass.local`
  - Service type: LoadBalancer or NodePort (configurable)
  - No Istio service mesh enabled

**Container Registry:**
- Docker images built via Jib Maven Plugin
- Repository pattern: `yourdockerhubuser/[service-name]`
- Base image: `eclipse-temurin:25-jre-noble`
- Architecture support: amd64 (default), arm64

**CI Pipeline:**
- **GitHub Actions**
  - Trigger: Pull requests to `main` branch
  - Runs on: `ubuntu-latest`
  - Build tools: Maven, JDK 21
  - Steps:
    1. Checkout code
    2. Set up JDK 21 (Temurin distribution)
    3. Spotless formatting check
    4. Maven verify (compile, test, package)
  - Workflow files: `.github/workflows/*.yml`

**Deployment Process:**
- Kubernetes manifests in `kubernetes/` directory
- Manifest organization:
  - `kubernetes/namespace.yml` - Namespace definition
  - `kubernetes/[service]-k8s/deployment.yml` - Service deployment
  - `kubernetes/[service]-k8s/service.yml` - Kubernetes Service
  - `kubernetes/[service]-k8s/postgresql.yml` - Database StatefulSet
  - `kubernetes/messagebroker-k8s/kafka.yml` - Kafka broker
  - `kubernetes/kustomization.yml` - Kustomize overlays
  - `kubernetes/skaffold.yml` - Skaffold development flow
- Deployment order: Dependencies (Kafka, PostgreSQL) → Services → Ingress

## Environment Configuration

**Required Environment Variables (Kubernetes):**
- `SPRING_PROFILES_ACTIVE=prod` - Production profile
- `SPRING_DATASOURCE_URL` - Database connection string
- `SPRING_DATASOURCE_USERNAME` - DB user
- `SPRING_DATASOURCE_PASSWORD` - DB password (from Secret)
- `SPRING_LIQUIBASE_URL` - Migration database URL
- `KAFKA_CONSUMER_BOOTSTRAP_SERVERS` - Kafka brokers
- `KAFKA_PRODUCER_BOOTSTRAP_SERVERS` - Kafka brokers
- `KAFKA_CONSUMER_GROUP_ID` - Consumer group identifier
- `JHIPSTER_CACHE_REDIS_SERVER` - Redis connection string
- `SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_OIDC_ISSUER_URI` - Keycloak realm URL
- `SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_OIDC_CLIENT_ID` - OAuth2 client ID
- `SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_OIDC_CLIENT_SECRET` - OAuth2 secret
- `JAVA_OPTS` - JVM memory settings (e.g., `-Xmx256m -Xms256m`)

**Secrets Location:**
- Development: `.env` files or environment setup scripts
- Docker: Environment variables in `docker-compose` files
- Kubernetes: Kubernetes Secrets
  - Pattern: Secret name `[service]-postgresql` contains key `postgresql-password`
  - Reference pattern: `valueFrom.secretKeyRef`
- Production: Environment-specific secret management (e.g., AWS Secrets Manager, HashiCorp Vault)

## Webhooks & Callbacks

**Incoming Webhooks:**
- None detected - No incoming webhook endpoints configured

**Outgoing Webhooks:**
- None detected - Services do not make outbound webhook calls

**Asynchronous Communication:**
- Kafka topics for event streaming (see Messaging & Events section)
- No traditional webhook pattern; Kafka serves event propagation needs

---

*Integration audit: 2026-08-22*
