# Coding Conventions

**Analysis Date:** 2026-08-22

## Naming Patterns

**Files:**
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

**Functions/Methods:**
- camelCase naming (e.g., `save`, `update`, `partialUpdate`, `findOne`, `delete`)
- CRUD operations follow standard naming: `save()`, `update()`, `partialUpdate()`, `findOne(id)`, `delete(id)`
- Query methods: descriptive camelCase (e.g., `getCurrentUserLogin()`)
- Factory methods in tests: `createEntity()`, `createUpdatedEntity()`
- Private methods: camelCase with descriptive intent (e.g., `logAround()`, `buildErrorResponse()`)

**Variables:**
- Local variables: camelCase
- Constants: UPPERCASE_WITH_UNDERSCORES (e.g., `DEFAULT_SNAPSHOT_DATE`, `ENTITY_API_URL`)
- Static loggers: `LOG` or `LOGGER` (use SLF4J LoggerFactory)
- Field references: final keyword for immutable dependencies

**Types:**
- Enums: PascalCase (e.g., `OperationType.java`)
- Generic types: Single letter capitals in angle brackets (e.g., `Optional<UUID>`)
- DTOs/Domain objects: Plural suffixes avoided; use singular entity names

**Package Structure:**
```
com.assetcompass.portfolio
├── config/          # Application configuration, caching, security
├── domain/          # JPA entities
├── service/         # Service interfaces
│   ├── impl/        # Service implementations
│   ├── dto/         # Data Transfer Objects
│   ├── mapper/      # MapStruct mappers
│   └── criteria/    # Query criteria classes
├── repository/      # Spring Data repositories
├── web/
│   └── rest/        # REST controllers
│       └── errors/  # Error handling classes
├── security/        # Authentication/authorization utilities
├── aop/
│   └── logging/     # Aspect-oriented logging
└── broker/          # Kafka consumers/producers
```

## Code Style

**Formatting:**
- File: `.prettierrc` in each module
- Java tab width: 4 spaces
- Non-Java tab width: 2 spaces
- Line length limit: 140 characters
- Single quotes in non-Java files
- Arrow parens: avoid (ES6 syntax)
- Bracket placement: same line

**Linting:**
- Tool: Maven Checkstyle Plugin (version 13.7.0)
- Also uses: Spotless Maven Plugin for unified formatting
- Format validation runs in `mvn clean install` and CI

**Code Quality:**
- SuppressWarnings annotations used selectively:
  - `@SuppressWarnings("common-java:DuplicatedBlocks")` for generated entities
  - `@SuppressWarnings("unused")` for repository interfaces
  - `@SuppressWarnings("java:S110")` for inheritance depth concerns
- No IDE-specific configurations in source code

## Import Organization

**Order:**
1. Package declaration (blank line after)
2. Package-internal imports (`com.assetcompass.*`)
3. Java/Jakarta standard library imports (alphabetical)
4. Third-party imports (spring, hibernate, etc.) (alphabetical)
5. Internal application imports (alphabetical by package depth)

**Path Aliases:**
- No aliases configured; full package paths used throughout
- MapStruct annotation processor automatically generates mappers

**Static Imports:**
Used selectively for test assertions:
```java
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
```

## Error Handling

**Patterns:**
- Custom `BadRequestAlertException` extends Spring's `ErrorResponseException`
  - Location: `com.assetcompass.portfolio.web.rest.errors.BadRequestAlertException`
  - Contains entity name and error key for localization
  - Returns HTTP 400 with Problem Detail response body

- Error responses use RFC 7807 Problem Detail format:
  - `type`: Error type URI
  - `status`: HTTP status code
  - `title`: Human-readable summary
  - `message`: Localization key (e.g., `error.idexists`)
  - `params`: Localization parameters

- Exception handling in REST resources:
  ```java
  if (valuationDTO.getId() != null) {
      throw new BadRequestAlertException("A new valuation cannot already have an ID", 
                                         ENTITY_NAME, "idexists");
  }
  ```

- Business logic exceptions are thrown early with descriptive messages
- Input validation using Jakarta `@NotNull`, `@Valid` annotations
- No generic catch-all exception handlers; Spring handles exceptions globally

## Logging

**Framework:** SLF4J with Logback (via Spring Boot)

**Logger Declaration:**
```java
private static final Logger LOG = LoggerFactory.getLogger(ClassName.class);
```

**Logging Levels:**
- `DEBUG`: Method entry/exit and business logic flow (guarded with `isDebugEnabled()` check)
- `INFO`: Not commonly used; avoid INFO level in application code
- `ERROR`: Exception catching with full stack traces in dev profile, summary in production
- `WARN`: Important business rule violations

**Patterns:**
- Entry logging: `LOG.debug("Request to save Entity : {}", dto);`
- Exit logging: `LOG.debug("Request to get Entity : {}", id);`
- Error logging in AOP aspect (`LoggingAspect.java`):
  - Dev profile: Full exception with cause and message
  - Production: Exception summary without stack trace
- Aspect-based logging for all `@Service`, `@Repository`, and `@RestController` classes

**Aspect Configuration:**
- Location: `com.assetcompass.portfolio.aop.logging.LoggingAspect`
- Automatically logs method entry/exit and exceptions
- Pointcuts target: repositories, services, REST endpoints
- Only active in dev profile to avoid performance overhead

## Comments

**When to Comment:**
- Class-level JavaDoc for public classes required
  - Example: `/** Service Interface for managing {@link com.assetcompass.portfolio.domain.Broker}. */`
- Method-level JavaDoc for public interface methods (parameters, return values, exceptions)
- Inline comments for non-obvious business logic only
- No obvious/redundant comments (avoid "// increment counter")
- Comments should explain "why" not "what"

**JavaDoc/JSDoc:**
- Method parameters: `@param name description`
- Return values: `@return description`
- Exceptions: `@throws ExceptionType reason`
- Cross-references: `{@link ClassName#methodName}`

**Example:**
```java
/**
 * REST controller for managing {@link com.assetcompass.portfolio.domain.Valuation}.
 */
@RestController
@RequestMapping("/api/valuations")
public class ValuationResource {
    
    /**
     * {@code POST  /valuations} : Create a new valuation.
     *
     * @param valuationDTO the valuationDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)}.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ValuationDTO> createValuation(@Valid @RequestBody ValuationDTO valuationDTO)
            throws URISyntaxException {
```

## Function Design

**Size Guideline:**
- Keep methods under 30 lines when possible
- Complex business logic extracted to separate private methods
- Use method extraction if a single method has multiple responsibilities

**Parameters:**
- Constructor injection preferred (final fields)
- Parameter validation with annotations (`@NotNull`, `@Valid`)
- Use DTOs for REST endpoints, domain objects internally
- Limit to 5+ parameters; use objects to group related parameters

**Return Values:**
- `Optional<T>` for queries that may not find results (e.g., `findOne()`)
- `void` for delete operations
- DTOs for REST responses
- Domain objects in service implementations (mappers handle DTO conversion)

**Flow:**
- Early return for error conditions
- Happy path at end of method
- Logging at method entry/exit for DEBUG level

## Module Design

**Exports:**
- Service interfaces expose public API contracts
- Service implementations are internal (impl package)
- REST resources are public endpoints
- DTOs are transfer layer contracts
- Domain classes encapsulated within service modules

**Barrel Files:**
- Not used; explicit imports required
- `package-info.java` files used for package-level documentation

**Service Layer Pattern:**
```
public interface BrokerService {
    BrokerDTO save(BrokerDTO brokerDTO);
    Optional<BrokerDTO> findOne(UUID id);
    void delete(UUID id);
}

@Service
@Transactional
public class BrokerServiceImpl implements BrokerService {
    // implementation with logging, validation, repository calls
}
```

**REST Layer Pattern:**
```
@RestController
@RequestMapping("/api/brokers")
public class BrokerResource {
    private final BrokerService brokerService;
    
    @PostMapping("")
    public ResponseEntity<BrokerDTO> create(@Valid @RequestBody BrokerDTO dto) {
        // validation, service call, response
    }
}
```

**Architectural Constraints:**
- **Layering**: Config → Web → Service → Persistence → Domain
- **No circular dependencies** enforced by ArchUnit tests
- **Transactionality**: Only service layer marked `@Transactional`
- **Database access**: Only through repositories, never direct JPA in REST/service

---

*Convention analysis: 2026-08-22*
