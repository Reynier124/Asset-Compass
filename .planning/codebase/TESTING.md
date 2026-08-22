# Testing Patterns

**Analysis Date:** 2026-08-22

## Test Framework

**Runner:**
- JUnit 5 (Jupiter)
- Version: Latest in Spring Boot 4.0.7
- Config: Spring Boot's auto-configuration via `@SpringBootTest`

**Assertion Library:**
- AssertJ (via spring-boot-starter-test)
- Hamcrest matchers (for REST response assertions)

**Additional Testing Libraries:**
- Spring Security Test (`spring-security-test`)
- Spring Test MVC (`spring-boot-starter-webmvc-test`)
- Testcontainers for database/cache testing
- ArchUnit (v1.4.2) for architecture validation

**Run Commands:**
```bash
mvn clean test                    # Run all unit tests (surefire)
mvn clean verify                  # Run all tests including integration tests (failsafe)
mvn clean test -DargLine="-Xmx1G" # Unit tests with memory limit
mvn clean verify -DargLine="-Xmx1G" # All tests with memory limit
mvn jacoco:report                 # Generate code coverage report
```

**Coverage Tools:**
- JaCoCo v0.8.15 (Java Code Coverage)
- Report location: `target/site/jacoco/` (unit tests)
- Report location: `target/site/jacoco-it/` (integration tests)

## Test File Organization

**Location:**
- Unit tests: Co-located in `src/test/java/` with same package structure as source
- Integration tests: Also in `src/test/java/` (not separate `src/it/`)

**Naming Convention:**
- Unit tests: `*UnitTest` suffix
  - Example: `src/test/java/com/assetcompass/portfolio/security/SecurityUtilsUnitTest.java`
- Integration tests: `*IT` suffix (Integration Test)
  - Example: `src/test/java/com/assetcompass/portfolio/web/rest/ValuationResourceIT.java`
- Architecture tests: `TechnicalStructureTest.java`
- Configuration tests: Config test classes in `src/test/java/com/assetcompass/portfolio/config/`

**Directory Structure:**
```
src/test/java/
├── com/assetcompass/portfolio/
│   ├── IntegrationTest.java               # Base annotation for integration tests
│   ├── TechnicalStructureTest.java        # Architecture validation
│   ├── config/
│   │   ├── AsyncSyncConfiguration.java    # Test configuration
│   │   ├── DatabaseTestcontainer.java     # PostgreSQL container setup
│   │   ├── RedisTestContainer.java        # Redis container setup
│   │   └── TestSecurityConfiguration.java # Security setup
│   ├── domain/                            # Domain entity test fixtures
│   ├── security/                          # Security utility tests
│   ├── service/                           # Service implementation tests
│   ├── web/rest/                          # REST endpoint integration tests
│   │   ├── TestUtil.java                  # Shared testing utilities
│   │   ├── ValuationResourceIT.java
│   │   └── BrokerResourceIT.java
│   └── repository/                        # Repository tests
```

**Test Resource Files:**
```
src/test/resources/
├── config/
│   ├── application-testdev.yml            # Development test profile
│   └── application-testprod.yml           # Production test profile
└── logback-test.xml                       # Test logging configuration
```

## Test Structure

**Suite Organization - Unit Test:**
```java
package com.assetcompass.portfolio.security;

/**
 * Test class for the {@link SecurityUtils} utility class.
 */
class SecurityUtilsUnitTest {

    @BeforeEach
    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetCurrentUserLogin() {
        // Arrange
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken("admin", "admin"));
        SecurityContextHolder.setContext(securityContext);
        
        // Act
        Optional<String> login = SecurityUtils.getCurrentUserLogin();
        
        // Assert
        assertThat(login).contains("admin");
    }
}
```

**Suite Organization - Integration Test:**
```java
/**
 * Integration tests for the {@link ValuationResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ValuationResourceIT {

    private static final LocalDate DEFAULT_SNAPSHOT_DATE = LocalDate.ofEpochDay(0L);
    private static final BigDecimal DEFAULT_TOTAL_VALUE = new BigDecimal(1);
    private static final String ENTITY_API_URL = "/api/valuations";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ValuationRepository valuationRepository;

    @Autowired
    private MockMvc restValuationMockMvc;

    private Valuation valuation;
    private Valuation insertedValuation;

    @BeforeEach
    void initTest() {
        valuation = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedValuation != null) {
            valuationRepository.delete(insertedValuation);
            insertedValuation = null;
        }
    }

    @Test
    @Transactional
    void createValuation() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        
        ValuationDTO valuationDTO = valuationMapper.toDto(valuation);
        var returnedValuationDTO = om.readValue(
            restValuationMockMvc
                .perform(post(ENTITY_API_URL)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(valuationDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ValuationDTO.class);

        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        insertedValuation = valuationMapper.toEntity(returnedValuationDTO);
    }
}
```

**Patterns:**
- `@BeforeEach`: Per-test setup (create fixtures, prepare mocks)
- `@AfterEach`: Per-test cleanup (clear contexts, delete test data)
- `@IntegrationTest`: Custom annotation combining Spring Boot Test + Testcontainers
- `@AutoConfigureMockMvc`: Enables MockMvc for REST testing
- `@WithMockUser`: Provides authenticated security context
- `@Transactional`: Automatically rollbacks after each test
- Constants at class level for test data (DEFAULT_* and UPDATED_* pairs)

## Mocking

**Framework:** Mockito (via spring-boot-starter-test)

**Patterns:**

**REST Mocking (MockMvc):**
```java
@Autowired
private MockMvc restValuationMockMvc;

@Test
void getValuation() throws Exception {
    restValuationMockMvc
        .perform(get(ENTITY_API_URL_ID, id))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(id.toString()));
}
```

**Database Mocking (Testcontainers):**
```java
@ImportTestcontainers({DatabaseTestcontainer.class, RedisTestContainer.class})
```
- Location: `com.assetcompass.portfolio.config.DatabaseTestcontainer`
- Uses: PostgreSQL testcontainer for real database testing
- Location: `com.assetcompass.portfolio.config.RedisTestContainer`
- Uses: Redis testcontainer for cache testing
- Auto-configured in test profile

**Security Mocking:**
```java
import org.springframework.security.test.context.support.WithMockUser;

@Test
@WithMockUser(username = "user", authorities = {"ROLE_USER"})
void testSecuredEndpoint() {
    // Test authenticated request
}

// CSRF token in requests
.perform(post(ENTITY_API_URL)
    .with(csrf())
    .contentType(MediaType.APPLICATION_JSON))
```

**Custom Assertions (ArchUnit):**
```java
@AnalyzeClasses(packagesOf = PortfolioServiceApp.class)
class TechnicalStructureTest {
    
    @ArchTest
    static final ArchRule respectsTechnicalArchitectureLayers = layeredArchitecture()
        .layer("Config").definedBy("..config..")
        .layer("Web").definedBy("..web..")
        .layer("Service").definedBy("..service..")
        .layer("Persistence").definedBy("..repository..")
        .layer("Domain").definedBy("..domain..")
        .whereLayer("Web").mayOnlyBeAccessedByLayers("Config")
        .whereLayer("Service").mayOnlyBeAccessedByLayers("Web", "Config");
}
```

**What to Mock:**
- External service calls (via Spring RestTemplate or Feign)
- Email sending (JavaMailSender)
- File system operations
- Kafka producers/consumers (in isolation tests)

**What NOT to Mock:**
- Database (use Testcontainers for real database)
- Spring components managed by container
- Repositories (test with real database)
- Mappers (test real object transformation)
- Security context (use `@WithMockUser`)

## Fixtures and Factories

**Test Data - Factory Methods Pattern:**
```java
public static Valuation createEntity() {
    return new Valuation()
        .snapshotDate(DEFAULT_SNAPSHOT_DATE)
        .totalValue(DEFAULT_TOTAL_VALUE)
        .currency(DEFAULT_CURRENCY);
}

public static Valuation createUpdatedEntity() {
    return new Valuation()
        .snapshotDate(UPDATED_SNAPSHOT_DATE)
        .totalValue(UPDATED_TOTAL_VALUE)
        .currency(UPDATED_CURRENCY);
}
```

**Location:**
- Factory methods: Inside each `*IT` class as static methods
- Constants: Class-level static fields in test class
- Shared utilities: `TestUtil.java` in `com.assetcompass.portfolio.web.rest` package

**Test Utilities (`TestUtil.java`):**
```java
public static byte[] createByteArray(int size, String data);
public static class ZonedDateTimeMatcher extends TypeSafeDiagnosingMatcher<String>;
public static <T> T createUpdateProxyForBean(Class<T> clazz, EntityManager em);
public static boolean sameNumber(BigDecimal num1, BigDecimal num2);
```

**Entity Assertions - Custom Matchers:**
- Location: `com.assetcompass.portfolio.domain.*Asserts` classes
- Patterns:
  ```java
  assertValuationUpdatableFieldsEquals(expected, actual);
  assertIncrementedRepositoryCount(previousCount);
  getPersistedValuation(id);
  ```

## Coverage

**Requirements:** 
- Target: Minimum 80% code coverage for critical paths
- Not enforced as build failure, but monitored

**View Coverage:**
```bash
# After running tests
mvn clean verify

# Open in browser
open target/site/jacoco/index.html
open target/site/jacoco-it/index.html
```

**Coverage Configuration:**
- Configured via `jacoco-maven-plugin` in pom.xml
- Excludes: Generated code, configuration classes, main entry point
- Reports: Both unit test and integration test coverage tracked separately

## Test Types

**Unit Tests** (`*UnitTest.java`):
- Scope: Single class in isolation
- Approach: No Spring context, no database
- Dependencies: Mocked with Mockito
- Examples: `SecurityUtilsUnitTest.java`
- Execution: `mvn clean test` via maven-surefire-plugin

**Integration Tests** (`*IT.java`):
- Scope: Full slice (controller + service + repository + database)
- Approach: Spring Boot test context, Testcontainers database
- Dependencies: Real beans from Spring context
- Examples: `ValuationResourceIT.java`, REST endpoint tests
- Execution: `mvn clean verify` via maven-failsafe-plugin

**Architecture Tests** (`TechnicalStructureTest.java`):
- Scope: Package structure and layer dependencies
- Approach: ArchUnit analyzes compiled classes
- Validates: Layering rules, no circular dependencies
- Runs: Part of integration test phase
- Example: `TechnicalStructureTest.java` in each service

**E2E Tests:**
- Not used currently
- If needed: Use Spring Boot `TestRestTemplate` or Testcontainers for container-based testing

## Common Patterns

**Async Testing:**
Not commonly used; services are synchronous.

For reactive scenarios (if needed):
```java
// Blocking awaits for Optional/CompletableFuture
CompletableFuture<BrokerDTO> result = brokerService.saveAsync(dto);
assertThat(result.get()).isNotNull();
```

**Error Testing:**
```java
@Test
void createWithExistingId() throws Exception {
    insertedValuation = valuationRepository.saveAndFlush(valuation);
    
    restValuationMockMvc
        .perform(post(ENTITY_API_URL)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsBytes(valuationDTO)))
        .andExpect(status().isBadRequest());
}

@Test
@Transactional
void deleteValuation() throws Exception {
    insertedValuation = valuationRepository.saveAndFlush(valuation);
    
    restValuationMockMvc
        .perform(delete(ENTITY_API_URL_ID, insertedValuation.getId())
            .with(csrf())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
    
    assertThat(valuationRepository.existsById(insertedValuation.getId())).isFalse();
}
```

**Database State Assertions:**
```java
long databaseSizeBeforeCreate = getRepositoryCount();
// ... perform action ...
assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
```

**Testing Pagination/Filtering:**
```java
@Test
void getAllValuations() throws Exception {
    restValuationMockMvc
        .perform(get(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.[*].id").value(hasItem(id.toString())));
}
```

**Testing with Criteria/Filtering:**
```java
@Test
void getValuationsByCriteria() throws Exception {
    ValuationCriteria criteria = new ValuationCriteria();
    criteria.setSnapshotDate(new LocalDateFilter());
    criteria.getSnapshotDate().setEquals(DEFAULT_SNAPSHOT_DATE);
    
    assertThat(valuationQueryService.findByCriteria(criteria)).hasSize(1);
}
```

## Test Configuration

**Base Annotations:**
```java
@IntegrationTest           // Custom annotation for integration tests
@AutoConfigureMockMvc      // Enable MockMvc
@WithMockUser              // Provides authenticated principal
@Transactional             // Automatic rollback after test
@SpringBootTest(classes = {PortfolioServiceApp.class, ...})
@ImportTestcontainers({DatabaseTestcontainer.class, RedisTestContainer.class})
```

**Test Profiles:**
- `application-testdev.yml`: Dev profile for testing
- `application-testprod.yml`: Prod profile for testing (security checks, no debug logging)
- Activated via Spring environment variables during test execution

**Spring Boot Test Properties:**
```yaml
# application-testdev.yml
server.servlet.context-path: /
spring.test.database.replace: any
spring.jpa.hibernate.ddl-auto: create-drop
spring.datasource.url: jdbc:tc:postgresql:16:///test # Testcontainers URL
logging.level.com.assetcompass: DEBUG
```

---

*Testing analysis: 2026-08-22*
