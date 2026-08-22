# Technology Stack

**Analysis Date:** 2026-08-22

## Languages

**Primary:**
- Java 21 - Backend services (portfolioService, tradingService, notificationService, gateway)
- TypeScript - Frontend/React application
- YAML - Configuration and deployment manifests

**Secondary:**
- Bash - Shell scripts for Docker and Kubernetes

## Runtime

**Environment:**
- JVM (OpenJDK Temurin 21)
- Node.js v24.18.0 (for frontend build tooling)
- Docker (container runtime)
- Kubernetes 1.x+ (orchestration)

**Package Manager:**
- Maven 3.2.5 (Java dependencies)
  - Lockfile: `pom.xml` per service
- npm 11.18.0 (Node dependencies)
  - Lockfile: `package-lock.json`

## Frameworks

**Core Backend:**
- Spring Boot 4.0.7 - Services: `portfolioService`, `tradingService`, `notificationService`
- Spring Boot 3.5.15 - Gateway service (`gateway`)
- JHipster Framework 9.1.0 - Three microservices
- JHipster Framework 8.12.0 - Gateway application

**API/Web:**
- Spring Web MVC (blocking) - `portfolioService`, `tradingService`, `notificationService`
- Spring WebFlux (reactive) - `gateway` for high-throughput request handling
- Spring Data JPA - ORM for microservices
- Spring Data R2DBC - Reactive database access for gateway

**Frontend:**
- React 18.x - UI framework for gateway
- Webpack 5.x - Module bundler (`gateway/webpack/webpack.*.js`)
- TypeScript 5.x - Frontend type safety

**Testing:**
- Spring Boot Test - Unit/integration testing foundation
- JUnit 5 - Test framework
- TestContainers - Docker-based test infrastructure
- Vitest - Frontend unit testing
- Cypress 14.x - End-to-end testing

**Build/Dev:**
- Maven (compile, package, deploy)
- Spotless 3.8.0 - Code formatting enforcement
- Checkstyle 13.7.0 - Code style validation
- ESLint - Frontend linting
- Webpack Dev Server - Frontend development server

## Key Dependencies

**Critical Backend:**
- Spring Cloud Dependencies 2025.1.2 (services) / 2025.0.0 (gateway)
- Spring Cloud Stream - Kafka integration
- Spring Cloud Circuit Breaker (Resilience4j) - Fault tolerance
- spring-boot-starter-oauth2-client - OAuth2 client
- spring-boot-starter-oauth2-resource-server - OAuth2 resource protection
- spring-boot-starter-security - Security framework

**Infrastructure:**
- PostgreSQL JDBC Driver - Database connectivity
- Liquibase 5.x - Database schema management (`spring-boot-starter-liquibase`)
- Redisson 4.6.1 - Redis client for caching and distributed operations
- HikariCP - Connection pooling
- Micrometer Registry Prometheus - Prometheus metrics export
- SpringDoc OpenAPI 3.0.3 - API documentation generation

**Data & Serialization:**
- MapStruct 1.6.3 - DTO mapping
- Jackson (multiple modules) - JSON serialization
  - jackson-datatype-hibernate7
  - jackson-datatype-hppc
  - jackson-datatype-jsr310
  - jackson-module-jaxb-annotations
- Hibernate ORM 6.x - JPA implementation
- Hibernate Validator - Bean validation
- JAXB Runtime - XML binding support

**Messaging:**
- Spring Cloud Stream Kafka Binder - Kafka integration
- Apache Kafka - Message broker (Kafka native 4.3.1 in Kubernetes)

**Observability & Monitoring:**
- Micrometer - Metrics collection
- Prometheus - Metrics storage and querying
- Spring Cloud Sleuth - Distributed tracing (optional via Maven profile)
- Zipkin - Trace visualization (optional, localhost:9411 in dev)

**Caching:**
- Caffeine - Local in-memory cache
- Redisson - Distributed Redis cache
- Hibernate JCache - Second-level cache support

**Quality & Testing:**
- ArchUnit 1.4.2 - Architecture testing
- Palantir Java Format 2.97.0 - Code formatting
- Modernizer Maven Plugin 3.4.0 - Detect outdated Java usage
- SonarQube Maven Plugin 5.7.0.6970 - Code quality analysis

**Container & Deployment:**
- Jib Maven Plugin 3.5.1 - Docker image building
- Eclipse Temurin 25 JRE - Production container base image

## Configuration

**Environment:**
- Spring Profiles:
  - `dev` - Development environment (faker data, API docs, Kafka, optional TLS)
  - `prod` - Production environment (optimized settings)
  - `api-docs` - Enable OpenAPI documentation
  - `kafka` - Enable Kafka consumers/producers
  - `tls` - Enable TLS/SSL (optional)
  - `e2e` - End-to-end testing profile

- Key Configuration Files:
  - `src/main/resources/config/application.yml` - Base configuration
  - `src/main/resources/config/application-{profile}.yml` - Profile-specific
  - `pom.xml` - Maven build and dependency configuration

**Database Migrations:**
- Liquibase changesets in `src/main/resources/db/changelog/`
- Contexts: `dev`, `faker` (development), `prod` (production)
- Automatic schema initialization enabled

**Docker Compose:**
- `src/main/docker/app.yml` - Service orchestration for development
- `src/main/docker/postgresql.yml` - PostgreSQL service
- `src/main/docker/redis.yml` - Redis cache service
- `src/main/docker/kafka.yml` - Kafka message broker
- `src/main/docker/keycloak.yml` - OAuth2/OIDC authentication server
- `src/main/docker/services.yml` - External services (Zipkin, Control Center)

**Kubernetes:**
- `kubernetes/*/service.yml` - Kubernetes Service manifests
- `kubernetes/*/deployment.yml` - Service deployments
- `kubernetes/*/postgresql.yml` - PostgreSQL StatefulSet per service
- `kubernetes/messagebroker-k8s/kafka.yml` - Kafka deployment
- `kubernetes/namespace.yml` - Namespace definition
- `kubernetes/kustomization.yml` - Kustomize configuration
- `kubernetes/skaffold.yml` - Skaffold development workflow

## Platform Requirements

**Development:**
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

**Production:**
- Kubernetes 1.20+ cluster
- Container registry (for Docker images)
- PostgreSQL 15+ (managed or self-hosted)
- Redis (managed or self-hosted)
- Apache Kafka (managed or self-hosted)
- Keycloak or external OIDC provider
- Ingress controller (for routing)

**CI/CD:**
- GitHub Actions (GitHub Runners: ubuntu-latest)
- Maven for builds
- Spotless for code formatting checks
- JUnit 5 for unit testing

## Build & Deployment

**Build Processes:**
- Maven multi-module: Each service has independent `pom.xml`
- Docker Image Build:
  - Jib Maven Plugin for efficient Docker builds
  - Target base image: `eclipse-temurin:25-jre-noble`
  - Architecture: amd64 (default) or arm64
- Frontend Bundle:
  - Webpack for production builds
  - React optimization enabled in prod profile

**Deployment Targets:**
- Docker containers (via Docker Registry)
- Kubernetes manifests (via kubectl or Kustomize)
- GitHub Actions CI/CD pipeline
- Local development (Docker Compose)

---

*Stack analysis: 2026-08-22*
