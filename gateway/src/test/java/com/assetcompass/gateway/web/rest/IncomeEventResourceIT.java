package com.assetcompass.gateway.web.rest;

import static com.assetcompass.gateway.domain.IncomeEventAsserts.*;
import static com.assetcompass.gateway.web.rest.TestUtil.createUpdateProxyForBean;
import static com.assetcompass.gateway.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.assetcompass.gateway.IntegrationTest;
import com.assetcompass.gateway.domain.Asset;
import com.assetcompass.gateway.domain.BrokerAccount;
import com.assetcompass.gateway.domain.IncomeEvent;
import com.assetcompass.gateway.domain.enumeration.IncomeType;
import com.assetcompass.gateway.repository.AssetRepository;
import com.assetcompass.gateway.repository.BrokerAccountRepository;
import com.assetcompass.gateway.repository.EntityManager;
import com.assetcompass.gateway.repository.IncomeEventRepository;
import com.assetcompass.gateway.service.dto.IncomeEventDTO;
import com.assetcompass.gateway.service.mapper.IncomeEventMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
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
 * Integration tests for the {@link IncomeEventResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class IncomeEventResourceIT {

    private static final IncomeType DEFAULT_TYPE = IncomeType.DIVIDENDO;
    private static final IncomeType UPDATED_TYPE = IncomeType.RENTA;

    private static final LocalDate DEFAULT_EVENT_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_EVENT_DATE = LocalDate.parse("2023-12-22");
    private static final LocalDate SMALLER_EVENT_DATE = LocalDate.ofEpochDay(-1L);

    private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal(1);
    private static final BigDecimal UPDATED_AMOUNT = new BigDecimal(2);
    private static final BigDecimal SMALLER_AMOUNT = new BigDecimal(1 - 1);

    private static final String DEFAULT_CURRENCY = "AAAAAAAAAA";
    private static final String UPDATED_CURRENCY = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/income-events";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private IncomeEventRepository incomeEventRepository;

    @Autowired
    private IncomeEventMapper incomeEventMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private IncomeEvent incomeEvent;

    private IncomeEvent insertedIncomeEvent;

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
    public static IncomeEvent createEntity(EntityManager em) {
        IncomeEvent incomeEvent = new IncomeEvent()
            .id(UUID.randomUUID())
            .type(DEFAULT_TYPE)
            .eventDate(DEFAULT_EVENT_DATE)
            .amount(DEFAULT_AMOUNT)
            .currency(DEFAULT_CURRENCY);
        // Add required entity
        BrokerAccount brokerAccount;
        brokerAccount = em.insert(BrokerAccountResourceIT.createEntity(em)).block();
        incomeEvent.setAccount(brokerAccount);
        // Add required entity
        Asset asset;
        asset = em.insert(AssetResourceIT.createEntity()).block();
        incomeEvent.setAsset(asset);
        return incomeEvent;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static IncomeEvent createUpdatedEntity(EntityManager em) {
        IncomeEvent updatedIncomeEvent = new IncomeEvent()
            .id(UUID.randomUUID())
            .type(UPDATED_TYPE)
            .eventDate(UPDATED_EVENT_DATE)
            .amount(UPDATED_AMOUNT)
            .currency(UPDATED_CURRENCY);
        // Add required entity
        BrokerAccount brokerAccount;
        brokerAccount = em.insert(BrokerAccountResourceIT.createUpdatedEntity(em)).block();
        updatedIncomeEvent.setAccount(brokerAccount);
        // Add required entity
        Asset asset;
        asset = em.insert(AssetResourceIT.createUpdatedEntity()).block();
        updatedIncomeEvent.setAsset(asset);
        return updatedIncomeEvent;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(IncomeEvent.class).block();
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
        incomeEvent = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedIncomeEvent != null) {
            incomeEventRepository.delete(insertedIncomeEvent).block();
            insertedIncomeEvent = null;
        }
        deleteEntities(em);
    }

    @Test
    void createIncomeEvent() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        incomeEvent.setId(null);
        // Create the IncomeEvent
        IncomeEventDTO incomeEventDTO = incomeEventMapper.toDto(incomeEvent);
        var returnedIncomeEventDTO = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(incomeEventDTO))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(IncomeEventDTO.class)
            .returnResult()
            .getResponseBody();

        // Validate the IncomeEvent in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedIncomeEvent = incomeEventMapper.toEntity(returnedIncomeEventDTO);
        assertIncomeEventUpdatableFieldsEquals(returnedIncomeEvent, getPersistedIncomeEvent(returnedIncomeEvent));

        insertedIncomeEvent = returnedIncomeEvent;
    }

    @Test
    void createIncomeEventWithExistingId() throws Exception {
        // Create the IncomeEvent with an existing ID
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();
        IncomeEventDTO incomeEventDTO = incomeEventMapper.toDto(incomeEvent);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(incomeEventDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the IncomeEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        incomeEvent.setType(null);

        // Create the IncomeEvent, which fails.
        IncomeEventDTO incomeEventDTO = incomeEventMapper.toDto(incomeEvent);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(incomeEventDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkEventDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        incomeEvent.setEventDate(null);

        // Create the IncomeEvent, which fails.
        IncomeEventDTO incomeEventDTO = incomeEventMapper.toDto(incomeEvent);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(incomeEventDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkAmountIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        incomeEvent.setAmount(null);

        // Create the IncomeEvent, which fails.
        IncomeEventDTO incomeEventDTO = incomeEventMapper.toDto(incomeEvent);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(incomeEventDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkCurrencyIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        incomeEvent.setCurrency(null);

        // Create the IncomeEvent, which fails.
        IncomeEventDTO incomeEventDTO = incomeEventMapper.toDto(incomeEvent);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(incomeEventDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllIncomeEvents() {
        // Initialize the database
        incomeEvent.setId(UUID.randomUUID());
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();

        // Get all the incomeEventList
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
            .value(hasItem(incomeEvent.getId().toString()))
            .jsonPath("$.[*].type")
            .value(hasItem(DEFAULT_TYPE.toString()))
            .jsonPath("$.[*].eventDate")
            .value(hasItem(DEFAULT_EVENT_DATE.toString()))
            .jsonPath("$.[*].amount")
            .value(hasItem(sameNumber(DEFAULT_AMOUNT)))
            .jsonPath("$.[*].currency")
            .value(hasItem(DEFAULT_CURRENCY));
    }

    @Test
    void getIncomeEvent() {
        // Initialize the database
        incomeEvent.setId(UUID.randomUUID());
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();

        // Get the incomeEvent
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, incomeEvent.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(incomeEvent.getId().toString()))
            .jsonPath("$.type")
            .value(is(DEFAULT_TYPE.toString()))
            .jsonPath("$.eventDate")
            .value(is(DEFAULT_EVENT_DATE.toString()))
            .jsonPath("$.amount")
            .value(is(sameNumber(DEFAULT_AMOUNT)))
            .jsonPath("$.currency")
            .value(is(DEFAULT_CURRENCY));
    }

    @Test
    void getIncomeEventsByIdFiltering() {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();

        UUID id = incomeEvent.getId();

        defaultIncomeEventFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    void getAllIncomeEventsByTypeIsEqualToSomething() {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();

        // Get all the incomeEventList where type equals to
        defaultIncomeEventFiltering("type.equals=" + DEFAULT_TYPE, "type.equals=" + UPDATED_TYPE);
    }

    @Test
    void getAllIncomeEventsByTypeIsInShouldWork() {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();

        // Get all the incomeEventList where type in
        defaultIncomeEventFiltering("type.in=" + DEFAULT_TYPE + "," + UPDATED_TYPE, "type.in=" + UPDATED_TYPE);
    }

    @Test
    void getAllIncomeEventsByTypeIsNullOrNotNull() {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();

        // Get all the incomeEventList where type is not null
        defaultIncomeEventFiltering("type.specified=true", "type.specified=false");
    }

    @Test
    void getAllIncomeEventsByEventDateIsEqualToSomething() {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();

        // Get all the incomeEventList where eventDate equals to
        defaultIncomeEventFiltering("eventDate.equals=" + DEFAULT_EVENT_DATE, "eventDate.equals=" + UPDATED_EVENT_DATE);
    }

    @Test
    void getAllIncomeEventsByEventDateIsInShouldWork() {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();

        // Get all the incomeEventList where eventDate in
        defaultIncomeEventFiltering("eventDate.in=" + DEFAULT_EVENT_DATE + "," + UPDATED_EVENT_DATE, "eventDate.in=" + UPDATED_EVENT_DATE);
    }

    @Test
    void getAllIncomeEventsByEventDateIsNullOrNotNull() {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();

        // Get all the incomeEventList where eventDate is not null
        defaultIncomeEventFiltering("eventDate.specified=true", "eventDate.specified=false");
    }

    @Test
    void getAllIncomeEventsByEventDateIsGreaterThanOrEqualToSomething() {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();

        // Get all the incomeEventList where eventDate is greater than or equal to
        defaultIncomeEventFiltering(
            "eventDate.greaterThanOrEqual=" + DEFAULT_EVENT_DATE,
            "eventDate.greaterThanOrEqual=" + UPDATED_EVENT_DATE
        );
    }

    @Test
    void getAllIncomeEventsByEventDateIsLessThanOrEqualToSomething() {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();

        // Get all the incomeEventList where eventDate is less than or equal to
        defaultIncomeEventFiltering("eventDate.lessThanOrEqual=" + DEFAULT_EVENT_DATE, "eventDate.lessThanOrEqual=" + SMALLER_EVENT_DATE);
    }

    @Test
    void getAllIncomeEventsByEventDateIsLessThanSomething() {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();

        // Get all the incomeEventList where eventDate is less than
        defaultIncomeEventFiltering("eventDate.lessThan=" + UPDATED_EVENT_DATE, "eventDate.lessThan=" + DEFAULT_EVENT_DATE);
    }

    @Test
    void getAllIncomeEventsByEventDateIsGreaterThanSomething() {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();

        // Get all the incomeEventList where eventDate is greater than
        defaultIncomeEventFiltering("eventDate.greaterThan=" + SMALLER_EVENT_DATE, "eventDate.greaterThan=" + DEFAULT_EVENT_DATE);
    }

    @Test
    void getAllIncomeEventsByAmountIsEqualToSomething() {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();

        // Get all the incomeEventList where amount equals to
        defaultIncomeEventFiltering("amount.equals=" + DEFAULT_AMOUNT, "amount.equals=" + UPDATED_AMOUNT);
    }

    @Test
    void getAllIncomeEventsByAmountIsInShouldWork() {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();

        // Get all the incomeEventList where amount in
        defaultIncomeEventFiltering("amount.in=" + DEFAULT_AMOUNT + "," + UPDATED_AMOUNT, "amount.in=" + UPDATED_AMOUNT);
    }

    @Test
    void getAllIncomeEventsByAmountIsNullOrNotNull() {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();

        // Get all the incomeEventList where amount is not null
        defaultIncomeEventFiltering("amount.specified=true", "amount.specified=false");
    }

    @Test
    void getAllIncomeEventsByAmountIsGreaterThanOrEqualToSomething() {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();

        // Get all the incomeEventList where amount is greater than or equal to
        defaultIncomeEventFiltering("amount.greaterThanOrEqual=" + DEFAULT_AMOUNT, "amount.greaterThanOrEqual=" + UPDATED_AMOUNT);
    }

    @Test
    void getAllIncomeEventsByAmountIsLessThanOrEqualToSomething() {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();

        // Get all the incomeEventList where amount is less than or equal to
        defaultIncomeEventFiltering("amount.lessThanOrEqual=" + DEFAULT_AMOUNT, "amount.lessThanOrEqual=" + SMALLER_AMOUNT);
    }

    @Test
    void getAllIncomeEventsByAmountIsLessThanSomething() {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();

        // Get all the incomeEventList where amount is less than
        defaultIncomeEventFiltering("amount.lessThan=" + UPDATED_AMOUNT, "amount.lessThan=" + DEFAULT_AMOUNT);
    }

    @Test
    void getAllIncomeEventsByAmountIsGreaterThanSomething() {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();

        // Get all the incomeEventList where amount is greater than
        defaultIncomeEventFiltering("amount.greaterThan=" + SMALLER_AMOUNT, "amount.greaterThan=" + DEFAULT_AMOUNT);
    }

    @Test
    void getAllIncomeEventsByCurrencyIsEqualToSomething() {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();

        // Get all the incomeEventList where currency equals to
        defaultIncomeEventFiltering("currency.equals=" + DEFAULT_CURRENCY, "currency.equals=" + UPDATED_CURRENCY);
    }

    @Test
    void getAllIncomeEventsByCurrencyIsInShouldWork() {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();

        // Get all the incomeEventList where currency in
        defaultIncomeEventFiltering("currency.in=" + DEFAULT_CURRENCY + "," + UPDATED_CURRENCY, "currency.in=" + UPDATED_CURRENCY);
    }

    @Test
    void getAllIncomeEventsByCurrencyIsNullOrNotNull() {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();

        // Get all the incomeEventList where currency is not null
        defaultIncomeEventFiltering("currency.specified=true", "currency.specified=false");
    }

    @Test
    void getAllIncomeEventsByCurrencyContainsSomething() {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();

        // Get all the incomeEventList where currency contains
        defaultIncomeEventFiltering("currency.contains=" + DEFAULT_CURRENCY, "currency.contains=" + UPDATED_CURRENCY);
    }

    @Test
    void getAllIncomeEventsByCurrencyNotContainsSomething() {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();

        // Get all the incomeEventList where currency does not contain
        defaultIncomeEventFiltering("currency.doesNotContain=" + UPDATED_CURRENCY, "currency.doesNotContain=" + DEFAULT_CURRENCY);
    }

    @Test
    void getAllIncomeEventsByAccountIsEqualToSomething() {
        BrokerAccount account = BrokerAccountResourceIT.createEntity(em);
        brokerAccountRepository.save(account).block();
        UUID accountId = account.getId();
        incomeEvent.setAccountId(accountId);
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();
        // Get all the incomeEventList where account equals to accountId
        defaultIncomeEventShouldBeFound("accountId.equals=" + accountId);

        // Get all the incomeEventList where account equals to UUID.randomUUID()
        defaultIncomeEventShouldNotBeFound("accountId.equals=" + UUID.randomUUID());
    }

    @Test
    void getAllIncomeEventsByAssetIsEqualToSomething() {
        Asset asset = AssetResourceIT.createEntity();
        assetRepository.save(asset).block();
        UUID assetId = asset.getId();
        incomeEvent.setAssetId(assetId);
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();
        // Get all the incomeEventList where asset equals to assetId
        defaultIncomeEventShouldBeFound("assetId.equals=" + assetId);

        // Get all the incomeEventList where asset equals to UUID.randomUUID()
        defaultIncomeEventShouldNotBeFound("assetId.equals=" + UUID.randomUUID());
    }

    private void defaultIncomeEventFiltering(String shouldBeFound, String shouldNotBeFound) {
        defaultIncomeEventShouldBeFound(shouldBeFound);
        defaultIncomeEventShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultIncomeEventShouldBeFound(String filter) {
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
            .value(hasItem(incomeEvent.getId().toString()))
            .jsonPath("$.[*].type")
            .value(hasItem(DEFAULT_TYPE.toString()))

            .jsonPath("$.[*].eventDate")
            .value(hasItem(DEFAULT_EVENT_DATE.toString()))

            .jsonPath("$.[*].amount")
            .value(hasItem(sameNumber(DEFAULT_AMOUNT)))

            .jsonPath("$.[*].currency")
            .value(hasItem(DEFAULT_CURRENCY));

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
    private void defaultIncomeEventShouldNotBeFound(String filter) {
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
    void getNonExistingIncomeEvent() {
        // Get the incomeEvent
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingIncomeEvent() throws Exception {
        // Initialize the database
        incomeEvent.setId(UUID.randomUUID());
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the incomeEvent
        IncomeEvent updatedIncomeEvent = incomeEventRepository.findById(incomeEvent.getId()).block();
        updatedIncomeEvent.type(UPDATED_TYPE).eventDate(UPDATED_EVENT_DATE).amount(UPDATED_AMOUNT).currency(UPDATED_CURRENCY);
        IncomeEventDTO incomeEventDTO = incomeEventMapper.toDto(updatedIncomeEvent);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, incomeEventDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(incomeEventDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the IncomeEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedIncomeEventToMatchAllProperties(updatedIncomeEvent);
    }

    @Test
    void putNonExistingIncomeEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        incomeEvent.setId(UUID.randomUUID());

        // Create the IncomeEvent
        IncomeEventDTO incomeEventDTO = incomeEventMapper.toDto(incomeEvent);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, incomeEventDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(incomeEventDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the IncomeEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchIncomeEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        incomeEvent.setId(UUID.randomUUID());

        // Create the IncomeEvent
        IncomeEventDTO incomeEventDTO = incomeEventMapper.toDto(incomeEvent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(incomeEventDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the IncomeEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamIncomeEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        incomeEvent.setId(UUID.randomUUID());

        // Create the IncomeEvent
        IncomeEventDTO incomeEventDTO = incomeEventMapper.toDto(incomeEvent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(incomeEventDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the IncomeEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateIncomeEventWithPatch() throws Exception {
        // Initialize the database
        incomeEvent.setId(UUID.randomUUID());
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the incomeEvent using partial update
        IncomeEvent partialUpdatedIncomeEvent = new IncomeEvent();
        partialUpdatedIncomeEvent.setId(incomeEvent.getId());

        partialUpdatedIncomeEvent.type(UPDATED_TYPE).currency(UPDATED_CURRENCY);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedIncomeEvent.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedIncomeEvent))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the IncomeEvent in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertIncomeEventUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedIncomeEvent, incomeEvent),
            getPersistedIncomeEvent(incomeEvent)
        );
    }

    @Test
    void fullUpdateIncomeEventWithPatch() throws Exception {
        // Initialize the database
        incomeEvent.setId(UUID.randomUUID());
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the incomeEvent using partial update
        IncomeEvent partialUpdatedIncomeEvent = new IncomeEvent();
        partialUpdatedIncomeEvent.setId(incomeEvent.getId());

        partialUpdatedIncomeEvent.type(UPDATED_TYPE).eventDate(UPDATED_EVENT_DATE).amount(UPDATED_AMOUNT).currency(UPDATED_CURRENCY);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedIncomeEvent.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedIncomeEvent))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the IncomeEvent in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertIncomeEventUpdatableFieldsEquals(partialUpdatedIncomeEvent, getPersistedIncomeEvent(partialUpdatedIncomeEvent));
    }

    @Test
    void patchNonExistingIncomeEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        incomeEvent.setId(UUID.randomUUID());

        // Create the IncomeEvent
        IncomeEventDTO incomeEventDTO = incomeEventMapper.toDto(incomeEvent);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, incomeEventDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(incomeEventDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the IncomeEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchIncomeEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        incomeEvent.setId(UUID.randomUUID());

        // Create the IncomeEvent
        IncomeEventDTO incomeEventDTO = incomeEventMapper.toDto(incomeEvent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(incomeEventDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the IncomeEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamIncomeEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        incomeEvent.setId(UUID.randomUUID());

        // Create the IncomeEvent
        IncomeEventDTO incomeEventDTO = incomeEventMapper.toDto(incomeEvent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(incomeEventDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the IncomeEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteIncomeEvent() {
        // Initialize the database
        incomeEvent.setId(UUID.randomUUID());
        insertedIncomeEvent = incomeEventRepository.save(incomeEvent).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the incomeEvent
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, incomeEvent.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return incomeEventRepository.count().block();
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

    protected IncomeEvent getPersistedIncomeEvent(IncomeEvent incomeEvent) {
        return incomeEventRepository.findById(incomeEvent.getId()).block();
    }

    protected void assertPersistedIncomeEventToMatchAllProperties(IncomeEvent expectedIncomeEvent) {
        // Test fails because reactive api returns an empty object instead of null
        // assertIncomeEventAllPropertiesEquals(expectedIncomeEvent, getPersistedIncomeEvent(expectedIncomeEvent));
        assertIncomeEventUpdatableFieldsEquals(expectedIncomeEvent, getPersistedIncomeEvent(expectedIncomeEvent));
    }

    protected void assertPersistedIncomeEventToMatchUpdatableProperties(IncomeEvent expectedIncomeEvent) {
        // Test fails because reactive api returns an empty object instead of null
        // assertIncomeEventAllUpdatablePropertiesEquals(expectedIncomeEvent, getPersistedIncomeEvent(expectedIncomeEvent));
        assertIncomeEventUpdatableFieldsEquals(expectedIncomeEvent, getPersistedIncomeEvent(expectedIncomeEvent));
    }
}
