package com.assetcompass.gateway.web.rest;

import static com.assetcompass.gateway.domain.PositionAsserts.*;
import static com.assetcompass.gateway.web.rest.TestUtil.createUpdateProxyForBean;
import static com.assetcompass.gateway.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.assetcompass.gateway.IntegrationTest;
import com.assetcompass.gateway.domain.Asset;
import com.assetcompass.gateway.domain.BrokerAccount;
import com.assetcompass.gateway.domain.Position;
import com.assetcompass.gateway.repository.AssetRepository;
import com.assetcompass.gateway.repository.BrokerAccountRepository;
import com.assetcompass.gateway.repository.EntityManager;
import com.assetcompass.gateway.repository.PositionRepository;
import com.assetcompass.gateway.service.dto.PositionDTO;
import com.assetcompass.gateway.service.mapper.PositionMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the {@link PositionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class PositionResourceIT {

    private static final BigDecimal DEFAULT_QUANTITY = new BigDecimal(1);
    private static final BigDecimal UPDATED_QUANTITY = new BigDecimal(2);
    private static final BigDecimal SMALLER_QUANTITY = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_AVERAGE_COST = new BigDecimal(1);
    private static final BigDecimal UPDATED_AVERAGE_COST = new BigDecimal(2);
    private static final BigDecimal SMALLER_AVERAGE_COST = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_CURRENT_VALUE = new BigDecimal(1);
    private static final BigDecimal UPDATED_CURRENT_VALUE = new BigDecimal(2);
    private static final BigDecimal SMALLER_CURRENT_VALUE = new BigDecimal(1 - 1);

    private static final String DEFAULT_CURRENCY = "AAAAAAAAAA";
    private static final String UPDATED_CURRENCY = "BBBBBBBBBB";

    private static final Instant DEFAULT_LAST_SYNCED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_LAST_SYNCED_AT = Instant.ofEpochMilli(1703288914559L);

    private static final String ENTITY_API_URL = "/api/positions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private PositionMapper positionMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Position position;

    private Position insertedPosition;

    @Autowired
    private BrokerAccountRepository brokerAccountRepository;

    @Autowired
    private AssetRepository assetRepository;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Position createEntity(EntityManager em) {
        Position position = new Position()
            .id(UUID.randomUUID())
            .quantity(DEFAULT_QUANTITY)
            .averageCost(DEFAULT_AVERAGE_COST)
            .currentValue(DEFAULT_CURRENT_VALUE)
            .currency(DEFAULT_CURRENCY)
            .lastSyncedAt(DEFAULT_LAST_SYNCED_AT);
        // Add required entity
        BrokerAccount brokerAccount;
        brokerAccount = em.insert(BrokerAccountResourceIT.createEntity(em)).block();
        position.setAccount(brokerAccount);
        // Add required entity
        Asset asset;
        asset = em.insert(AssetResourceIT.createEntity()).block();
        position.setAsset(asset);
        return position;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Position createUpdatedEntity(EntityManager em) {
        Position updatedPosition = new Position()
            .id(UUID.randomUUID())
            .quantity(UPDATED_QUANTITY)
            .averageCost(UPDATED_AVERAGE_COST)
            .currentValue(UPDATED_CURRENT_VALUE)
            .currency(UPDATED_CURRENCY)
            .lastSyncedAt(UPDATED_LAST_SYNCED_AT);
        // Add required entity
        BrokerAccount brokerAccount;
        brokerAccount = em.insert(BrokerAccountResourceIT.createUpdatedEntity(em)).block();
        updatedPosition.setAccount(brokerAccount);
        // Add required entity
        Asset asset;
        asset = em.insert(AssetResourceIT.createUpdatedEntity()).block();
        updatedPosition.setAsset(asset);
        return updatedPosition;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Position.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
        BrokerAccountResourceIT.deleteEntities(em);
        AssetResourceIT.deleteEntities(em);
    }

    @BeforeEach
    void setupCsrf() {
        webTestClient = webTestClient.mutateWith(csrf());
    }

    @BeforeEach
    void initTest() {
        position = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedPosition != null) {
            positionRepository.delete(insertedPosition).block();
            insertedPosition = null;
        }
        deleteEntities(em);
    }

    @Test
    void createPosition() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        position.setId(null);
        // Create the Position
        PositionDTO positionDTO = positionMapper.toDto(position);
        var returnedPositionDTO = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(positionDTO))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(PositionDTO.class)
            .returnResult()
            .getResponseBody();

        // Validate the Position in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedPosition = positionMapper.toEntity(returnedPositionDTO);
        assertPositionUpdatableFieldsEquals(returnedPosition, getPersistedPosition(returnedPosition));

        insertedPosition = returnedPosition;
    }

    @Test
    void createPositionWithExistingId() throws Exception {
        // Create the Position with an existing ID
        insertedPosition = positionRepository.save(position).block();
        PositionDTO positionDTO = positionMapper.toDto(position);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(positionDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Position in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkQuantityIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        position.setQuantity(null);

        // Create the Position, which fails.
        PositionDTO positionDTO = positionMapper.toDto(position);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(positionDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkAverageCostIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        position.setAverageCost(null);

        // Create the Position, which fails.
        PositionDTO positionDTO = positionMapper.toDto(position);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(positionDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkCurrentValueIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        position.setCurrentValue(null);

        // Create the Position, which fails.
        PositionDTO positionDTO = positionMapper.toDto(position);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(positionDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkCurrencyIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        position.setCurrency(null);

        // Create the Position, which fails.
        PositionDTO positionDTO = positionMapper.toDto(position);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(positionDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkLastSyncedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        position.setLastSyncedAt(null);

        // Create the Position, which fails.
        PositionDTO positionDTO = positionMapper.toDto(position);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(positionDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllPositions() {
        // Initialize the database
        position.setId(UUID.randomUUID());
        insertedPosition = positionRepository.save(position).block();

        // Get all the positionList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(position.getId().toString()))
            .jsonPath("$.[*].quantity")
            .value(hasItem(sameNumber(DEFAULT_QUANTITY)))
            .jsonPath("$.[*].averageCost")
            .value(hasItem(sameNumber(DEFAULT_AVERAGE_COST)))
            .jsonPath("$.[*].currentValue")
            .value(hasItem(sameNumber(DEFAULT_CURRENT_VALUE)))
            .jsonPath("$.[*].currency")
            .value(hasItem(DEFAULT_CURRENCY))
            .jsonPath("$.[*].lastSyncedAt")
            .value(hasItem(DEFAULT_LAST_SYNCED_AT.toString()));
    }

    @Test
    void getPosition() {
        // Initialize the database
        position.setId(UUID.randomUUID());
        insertedPosition = positionRepository.save(position).block();

        // Get the position
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, position.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(position.getId().toString()))
            .jsonPath("$.quantity")
            .value(is(sameNumber(DEFAULT_QUANTITY)))
            .jsonPath("$.averageCost")
            .value(is(sameNumber(DEFAULT_AVERAGE_COST)))
            .jsonPath("$.currentValue")
            .value(is(sameNumber(DEFAULT_CURRENT_VALUE)))
            .jsonPath("$.currency")
            .value(is(DEFAULT_CURRENCY))
            .jsonPath("$.lastSyncedAt")
            .value(is(DEFAULT_LAST_SYNCED_AT.toString()));
    }

    @Test
    void getPositionsByIdFiltering() {
        // Initialize the database
        insertedPosition = positionRepository.save(position).block();

        UUID id = position.getId();

        defaultPositionFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    void getAllPositionsByQuantityIsEqualToSomething() {
        // Initialize the database
        insertedPosition = positionRepository.save(position).block();

        // Get all the positionList where quantity equals to
        defaultPositionFiltering("quantity.equals=" + DEFAULT_QUANTITY, "quantity.equals=" + UPDATED_QUANTITY);
    }

    @Test
    void getAllPositionsByQuantityIsInShouldWork() {
        // Initialize the database
        insertedPosition = positionRepository.save(position).block();

        // Get all the positionList where quantity in
        defaultPositionFiltering("quantity.in=" + DEFAULT_QUANTITY + "," + UPDATED_QUANTITY, "quantity.in=" + UPDATED_QUANTITY);
    }

    @Test
    void getAllPositionsByQuantityIsNullOrNotNull() {
        // Initialize the database
        insertedPosition = positionRepository.save(position).block();

        // Get all the positionList where quantity is not null
        defaultPositionFiltering("quantity.specified=true", "quantity.specified=false");
    }

    @Test
    void getAllPositionsByQuantityIsGreaterThanOrEqualToSomething() {
        // Initialize the database
        insertedPosition = positionRepository.save(position).block();

        // Get all the positionList where quantity is greater than or equal to
        defaultPositionFiltering("quantity.greaterThanOrEqual=" + DEFAULT_QUANTITY, "quantity.greaterThanOrEqual=" + UPDATED_QUANTITY);
    }

    @Test
    void getAllPositionsByQuantityIsLessThanOrEqualToSomething() {
        // Initialize the database
        insertedPosition = positionRepository.save(position).block();

        // Get all the positionList where quantity is less than or equal to
        defaultPositionFiltering("quantity.lessThanOrEqual=" + DEFAULT_QUANTITY, "quantity.lessThanOrEqual=" + SMALLER_QUANTITY);
    }

    @Test
    void getAllPositionsByQuantityIsLessThanSomething() {
        // Initialize the database
        insertedPosition = positionRepository.save(position).block();

        // Get all the positionList where quantity is less than
        defaultPositionFiltering("quantity.lessThan=" + UPDATED_QUANTITY, "quantity.lessThan=" + DEFAULT_QUANTITY);
    }

    @Test
    void getAllPositionsByQuantityIsGreaterThanSomething() {
        // Initialize the database
        insertedPosition = positionRepository.save(position).block();

        // Get all the positionList where quantity is greater than
        defaultPositionFiltering("quantity.greaterThan=" + SMALLER_QUANTITY, "quantity.greaterThan=" + DEFAULT_QUANTITY);
    }

    @Test
    void getAllPositionsByAverageCostIsEqualToSomething() {
        // Initialize the database
        insertedPosition = positionRepository.save(position).block();

        // Get all the positionList where averageCost equals to
        defaultPositionFiltering("averageCost.equals=" + DEFAULT_AVERAGE_COST, "averageCost.equals=" + UPDATED_AVERAGE_COST);
    }

    @Test
    void getAllPositionsByAverageCostIsInShouldWork() {
        // Initialize the database
        insertedPosition = positionRepository.save(position).block();

        // Get all the positionList where averageCost in
        defaultPositionFiltering(
            "averageCost.in=" + DEFAULT_AVERAGE_COST + "," + UPDATED_AVERAGE_COST,
            "averageCost.in=" + UPDATED_AVERAGE_COST
        );
    }

    @Test
    void getAllPositionsByAverageCostIsNullOrNotNull() {
        // Initialize the database
        insertedPosition = positionRepository.save(position).block();

        // Get all the positionList where averageCost is not null
        defaultPositionFiltering("averageCost.specified=true", "averageCost.specified=false");
    }

    @Test
    void getAllPositionsByAverageCostIsGreaterThanOrEqualToSomething() {
        // Initialize the database
        insertedPosition = positionRepository.save(position).block();

        // Get all the positionList where averageCost is greater than or equal to
        defaultPositionFiltering(
            "averageCost.greaterThanOrEqual=" + DEFAULT_AVERAGE_COST,
            "averageCost.greaterThanOrEqual=" + UPDATED_AVERAGE_COST
        );
    }

    @Test
    void getAllPositionsByAverageCostIsLessThanOrEqualToSomething() {
        // Initialize the database
        insertedPosition = positionRepository.save(position).block();

        // Get all the positionList where averageCost is less than or equal to
        defaultPositionFiltering(
            "averageCost.lessThanOrEqual=" + DEFAULT_AVERAGE_COST,
            "averageCost.lessThanOrEqual=" + SMALLER_AVERAGE_COST
        );
    }

    @Test
    void getAllPositionsByAverageCostIsLessThanSomething() {
        // Initialize the database
        insertedPosition = positionRepository.save(position).block();

        // Get all the positionList where averageCost is less than
        defaultPositionFiltering("averageCost.lessThan=" + UPDATED_AVERAGE_COST, "averageCost.lessThan=" + DEFAULT_AVERAGE_COST);
    }

    @Test
    void getAllPositionsByAverageCostIsGreaterThanSomething() {
        // Initialize the database
        insertedPosition = positionRepository.save(position).block();

        // Get all the positionList where averageCost is greater than
        defaultPositionFiltering("averageCost.greaterThan=" + SMALLER_AVERAGE_COST, "averageCost.greaterThan=" + DEFAULT_AVERAGE_COST);
    }

    @Test
    void getAllPositionsByCurrentValueIsEqualToSomething() {
        // Initialize the database
        insertedPosition = positionRepository.save(position).block();

        // Get all the positionList where currentValue equals to
        defaultPositionFiltering("currentValue.equals=" + DEFAULT_CURRENT_VALUE, "currentValue.equals=" + UPDATED_CURRENT_VALUE);
    }

    @Test
    void getAllPositionsByCurrentValueIsInShouldWork() {
        // Initialize the database
        insertedPosition = positionRepository.save(position).block();

        // Get all the positionList where currentValue in
        defaultPositionFiltering(
            "currentValue.in=" + DEFAULT_CURRENT_VALUE + "," + UPDATED_CURRENT_VALUE,
            "currentValue.in=" + UPDATED_CURRENT_VALUE
        );
    }

    @Test
    void getAllPositionsByCurrentValueIsNullOrNotNull() {
        // Initialize the database
        insertedPosition = positionRepository.save(position).block();

        // Get all the positionList where currentValue is not null
        defaultPositionFiltering("currentValue.specified=true", "currentValue.specified=false");
    }

    @Test
    void getAllPositionsByCurrentValueIsGreaterThanOrEqualToSomething() {
        // Initialize the database
        insertedPosition = positionRepository.save(position).block();

        // Get all the positionList where currentValue is greater than or equal to
        defaultPositionFiltering(
            "currentValue.greaterThanOrEqual=" + DEFAULT_CURRENT_VALUE,
            "currentValue.greaterThanOrEqual=" + UPDATED_CURRENT_VALUE
        );
    }

    @Test
    void getAllPositionsByCurrentValueIsLessThanOrEqualToSomething() {
        // Initialize the database
        insertedPosition = positionRepository.save(position).block();

        // Get all the positionList where currentValue is less than or equal to
        defaultPositionFiltering(
            "currentValue.lessThanOrEqual=" + DEFAULT_CURRENT_VALUE,
            "currentValue.lessThanOrEqual=" + SMALLER_CURRENT_VALUE
        );
    }

    @Test
    void getAllPositionsByCurrentValueIsLessThanSomething() {
        // Initialize the database
        insertedPosition = positionRepository.save(position).block();

        // Get all the positionList where currentValue is less than
        defaultPositionFiltering("currentValue.lessThan=" + UPDATED_CURRENT_VALUE, "currentValue.lessThan=" + DEFAULT_CURRENT_VALUE);
    }

    @Test
    void getAllPositionsByCurrentValueIsGreaterThanSomething() {
        // Initialize the database
        insertedPosition = positionRepository.save(position).block();

        // Get all the positionList where currentValue is greater than
        defaultPositionFiltering("currentValue.greaterThan=" + SMALLER_CURRENT_VALUE, "currentValue.greaterThan=" + DEFAULT_CURRENT_VALUE);
    }

    @Test
    void getAllPositionsByCurrencyIsEqualToSomething() {
        // Initialize the database
        insertedPosition = positionRepository.save(position).block();

        // Get all the positionList where currency equals to
        defaultPositionFiltering("currency.equals=" + DEFAULT_CURRENCY, "currency.equals=" + UPDATED_CURRENCY);
    }

    @Test
    void getAllPositionsByCurrencyIsInShouldWork() {
        // Initialize the database
        insertedPosition = positionRepository.save(position).block();

        // Get all the positionList where currency in
        defaultPositionFiltering("currency.in=" + DEFAULT_CURRENCY + "," + UPDATED_CURRENCY, "currency.in=" + UPDATED_CURRENCY);
    }

    @Test
    void getAllPositionsByCurrencyIsNullOrNotNull() {
        // Initialize the database
        insertedPosition = positionRepository.save(position).block();

        // Get all the positionList where currency is not null
        defaultPositionFiltering("currency.specified=true", "currency.specified=false");
    }

    @Test
    void getAllPositionsByCurrencyContainsSomething() {
        // Initialize the database
        insertedPosition = positionRepository.save(position).block();

        // Get all the positionList where currency contains
        defaultPositionFiltering("currency.contains=" + DEFAULT_CURRENCY, "currency.contains=" + UPDATED_CURRENCY);
    }

    @Test
    void getAllPositionsByCurrencyNotContainsSomething() {
        // Initialize the database
        insertedPosition = positionRepository.save(position).block();

        // Get all the positionList where currency does not contain
        defaultPositionFiltering("currency.doesNotContain=" + UPDATED_CURRENCY, "currency.doesNotContain=" + DEFAULT_CURRENCY);
    }

    @Test
    void getAllPositionsByLastSyncedAtIsEqualToSomething() {
        // Initialize the database
        insertedPosition = positionRepository.save(position).block();

        // Get all the positionList where lastSyncedAt equals to
        defaultPositionFiltering("lastSyncedAt.equals=" + DEFAULT_LAST_SYNCED_AT, "lastSyncedAt.equals=" + UPDATED_LAST_SYNCED_AT);
    }

    @Test
    void getAllPositionsByLastSyncedAtIsInShouldWork() {
        // Initialize the database
        insertedPosition = positionRepository.save(position).block();

        // Get all the positionList where lastSyncedAt in
        defaultPositionFiltering(
            "lastSyncedAt.in=" + DEFAULT_LAST_SYNCED_AT + "," + UPDATED_LAST_SYNCED_AT,
            "lastSyncedAt.in=" + UPDATED_LAST_SYNCED_AT
        );
    }

    @Test
    void getAllPositionsByLastSyncedAtIsNullOrNotNull() {
        // Initialize the database
        insertedPosition = positionRepository.save(position).block();

        // Get all the positionList where lastSyncedAt is not null
        defaultPositionFiltering("lastSyncedAt.specified=true", "lastSyncedAt.specified=false");
    }

    @Test
    void getAllPositionsByAccountIsEqualToSomething() {
        BrokerAccount account = BrokerAccountResourceIT.createEntity(em);
        brokerAccountRepository.save(account).block();
        UUID accountId = account.getId();
        position.setAccountId(accountId);
        insertedPosition = positionRepository.save(position).block();
        // Get all the positionList where account equals to accountId
        defaultPositionShouldBeFound("accountId.equals=" + accountId);

        // Get all the positionList where account equals to UUID.randomUUID()
        defaultPositionShouldNotBeFound("accountId.equals=" + UUID.randomUUID());
    }

    @Test
    void getAllPositionsByAssetIsEqualToSomething() {
        Asset asset = AssetResourceIT.createEntity();
        assetRepository.save(asset).block();
        UUID assetId = asset.getId();
        position.setAssetId(assetId);
        insertedPosition = positionRepository.save(position).block();
        // Get all the positionList where asset equals to assetId
        defaultPositionShouldBeFound("assetId.equals=" + assetId);

        // Get all the positionList where asset equals to UUID.randomUUID()
        defaultPositionShouldNotBeFound("assetId.equals=" + UUID.randomUUID());
    }

    private void defaultPositionFiltering(String shouldBeFound, String shouldNotBeFound) {
        defaultPositionShouldBeFound(shouldBeFound);
        defaultPositionShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultPositionShouldBeFound(String filter) {
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc&" + filter)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(position.getId().toString()))
            .jsonPath("$.[*].quantity")
            .value(hasItem(sameNumber(DEFAULT_QUANTITY)))

            .jsonPath("$.[*].averageCost")
            .value(hasItem(sameNumber(DEFAULT_AVERAGE_COST)))

            .jsonPath("$.[*].currentValue")
            .value(hasItem(sameNumber(DEFAULT_CURRENT_VALUE)))

            .jsonPath("$.[*].currency")
            .value(hasItem(DEFAULT_CURRENCY))

            .jsonPath("$.[*].lastSyncedAt")
            .value(hasItem(DEFAULT_LAST_SYNCED_AT.toString()));

        // Check, that the count call also returns 1
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "/count?sort=id,desc&" + filter)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$")
            .value(is(1));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultPositionShouldNotBeFound(String filter) {
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc&" + filter)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$")
            .isArray()
            .jsonPath("$")
            .isEmpty();

        // Check, that the count call also returns 0
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "/count?sort=id,desc&" + filter)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$")
            .value(is(0));
    }

    @Test
    void getNonExistingPosition() {
        // Get the position
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingPosition() throws Exception {
        // Initialize the database
        position.setId(UUID.randomUUID());
        insertedPosition = positionRepository.save(position).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the position
        Position updatedPosition = positionRepository.findById(position.getId()).block();
        updatedPosition
            .quantity(UPDATED_QUANTITY)
            .averageCost(UPDATED_AVERAGE_COST)
            .currentValue(UPDATED_CURRENT_VALUE)
            .currency(UPDATED_CURRENCY)
            .lastSyncedAt(UPDATED_LAST_SYNCED_AT);
        PositionDTO positionDTO = positionMapper.toDto(updatedPosition);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, positionDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(positionDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Position in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedPositionToMatchAllProperties(updatedPosition);
    }

    @Test
    void putNonExistingPosition() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        position.setId(UUID.randomUUID());

        // Create the Position
        PositionDTO positionDTO = positionMapper.toDto(position);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, positionDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(positionDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Position in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchPosition() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        position.setId(UUID.randomUUID());

        // Create the Position
        PositionDTO positionDTO = positionMapper.toDto(position);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(positionDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Position in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamPosition() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        position.setId(UUID.randomUUID());

        // Create the Position
        PositionDTO positionDTO = positionMapper.toDto(position);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(positionDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Position in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdatePositionWithPatch() throws Exception {
        // Initialize the database
        position.setId(UUID.randomUUID());
        insertedPosition = positionRepository.save(position).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the position using partial update
        Position partialUpdatedPosition = new Position();
        partialUpdatedPosition.setId(position.getId());

        partialUpdatedPosition.quantity(UPDATED_QUANTITY).currentValue(UPDATED_CURRENT_VALUE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedPosition.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedPosition))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Position in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPositionUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedPosition, position), getPersistedPosition(position));
    }

    @Test
    void fullUpdatePositionWithPatch() throws Exception {
        // Initialize the database
        position.setId(UUID.randomUUID());
        insertedPosition = positionRepository.save(position).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the position using partial update
        Position partialUpdatedPosition = new Position();
        partialUpdatedPosition.setId(position.getId());

        partialUpdatedPosition
            .quantity(UPDATED_QUANTITY)
            .averageCost(UPDATED_AVERAGE_COST)
            .currentValue(UPDATED_CURRENT_VALUE)
            .currency(UPDATED_CURRENCY)
            .lastSyncedAt(UPDATED_LAST_SYNCED_AT);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedPosition.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedPosition))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Position in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPositionUpdatableFieldsEquals(partialUpdatedPosition, getPersistedPosition(partialUpdatedPosition));
    }

    @Test
    void patchNonExistingPosition() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        position.setId(UUID.randomUUID());

        // Create the Position
        PositionDTO positionDTO = positionMapper.toDto(position);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, positionDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(positionDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Position in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchPosition() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        position.setId(UUID.randomUUID());

        // Create the Position
        PositionDTO positionDTO = positionMapper.toDto(position);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(positionDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Position in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamPosition() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        position.setId(UUID.randomUUID());

        // Create the Position
        PositionDTO positionDTO = positionMapper.toDto(position);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(positionDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Position in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deletePosition() {
        // Initialize the database
        position.setId(UUID.randomUUID());
        insertedPosition = positionRepository.save(position).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the position
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, position.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return positionRepository.count().block();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Position getPersistedPosition(Position position) {
        return positionRepository.findById(position.getId()).block();
    }

    protected void assertPersistedPositionToMatchAllProperties(Position expectedPosition) {
        // Test fails because reactive api returns an empty object instead of null
        // assertPositionAllPropertiesEquals(expectedPosition, getPersistedPosition(expectedPosition));
        assertPositionUpdatableFieldsEquals(expectedPosition, getPersistedPosition(expectedPosition));
    }

    protected void assertPersistedPositionToMatchUpdatableProperties(Position expectedPosition) {
        // Test fails because reactive api returns an empty object instead of null
        // assertPositionAllUpdatablePropertiesEquals(expectedPosition, getPersistedPosition(expectedPosition));
        assertPositionUpdatableFieldsEquals(expectedPosition, getPersistedPosition(expectedPosition));
    }
}
