package com.assetcompass.gateway.web.rest;

import static com.assetcompass.gateway.domain.TaxEventAsserts.*;
import static com.assetcompass.gateway.web.rest.TestUtil.createUpdateProxyForBean;
import static com.assetcompass.gateway.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.assetcompass.gateway.IntegrationTest;
import com.assetcompass.gateway.domain.BrokerAccount;
import com.assetcompass.gateway.domain.IncomeEvent;
import com.assetcompass.gateway.domain.Operation;
import com.assetcompass.gateway.domain.TaxEvent;
import com.assetcompass.gateway.domain.enumeration.TaxType;
import com.assetcompass.gateway.repository.BrokerAccountRepository;
import com.assetcompass.gateway.repository.EntityManager;
import com.assetcompass.gateway.repository.IncomeEventRepository;
import com.assetcompass.gateway.repository.OperationRepository;
import com.assetcompass.gateway.repository.TaxEventRepository;
import com.assetcompass.gateway.service.dto.TaxEventDTO;
import com.assetcompass.gateway.service.mapper.TaxEventMapper;
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
 * Integration tests for the {@link TaxEventResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class TaxEventResourceIT {

    private static final TaxType DEFAULT_TYPE = TaxType.RETENCION_DIVIDENDO;
    private static final TaxType UPDATED_TYPE = TaxType.IVA;

    private static final LocalDate DEFAULT_TAX_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_TAX_DATE = LocalDate.parse("2023-12-22");
    private static final LocalDate SMALLER_TAX_DATE = LocalDate.ofEpochDay(-1L);

    private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal(1);
    private static final BigDecimal UPDATED_AMOUNT = new BigDecimal(2);
    private static final BigDecimal SMALLER_AMOUNT = new BigDecimal(1 - 1);

    private static final String DEFAULT_CURRENCY = "AAAAAAAAAA";
    private static final String UPDATED_CURRENCY = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/tax-events";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TaxEventRepository taxEventRepository;

    @Autowired
    private TaxEventMapper taxEventMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private TaxEvent taxEvent;

    private TaxEvent insertedTaxEvent;

    @Autowired
    private BrokerAccountRepository brokerAccountRepository;

    @Autowired
    private OperationRepository operationRepository;

    @Autowired
    private IncomeEventRepository incomeEventRepository;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TaxEvent createEntity(EntityManager em) {
        TaxEvent taxEvent = new TaxEvent()
            .id(UUID.randomUUID())
            .type(DEFAULT_TYPE)
            .taxDate(DEFAULT_TAX_DATE)
            .amount(DEFAULT_AMOUNT)
            .currency(DEFAULT_CURRENCY);
        // Add required entity
        BrokerAccount brokerAccount;
        brokerAccount = em.insert(BrokerAccountResourceIT.createEntity(em)).block();
        taxEvent.setAccount(brokerAccount);
        return taxEvent;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TaxEvent createUpdatedEntity(EntityManager em) {
        TaxEvent updatedTaxEvent = new TaxEvent()
            .id(UUID.randomUUID())
            .type(UPDATED_TYPE)
            .taxDate(UPDATED_TAX_DATE)
            .amount(UPDATED_AMOUNT)
            .currency(UPDATED_CURRENCY);
        // Add required entity
        BrokerAccount brokerAccount;
        brokerAccount = em.insert(BrokerAccountResourceIT.createUpdatedEntity(em)).block();
        updatedTaxEvent.setAccount(brokerAccount);
        return updatedTaxEvent;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(TaxEvent.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
        BrokerAccountResourceIT.deleteEntities(em);
    }

    @BeforeEach
    void setupCsrf() {
        webTestClient = webTestClient.mutateWith(csrf());
    }

    @BeforeEach
    void initTest() {
        taxEvent = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedTaxEvent != null) {
            taxEventRepository.delete(insertedTaxEvent).block();
            insertedTaxEvent = null;
        }
        deleteEntities(em);
    }

    @Test
    void createTaxEvent() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        taxEvent.setId(null);
        // Create the TaxEvent
        TaxEventDTO taxEventDTO = taxEventMapper.toDto(taxEvent);
        var returnedTaxEventDTO = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(taxEventDTO))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(TaxEventDTO.class)
            .returnResult()
            .getResponseBody();

        // Validate the TaxEvent in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedTaxEvent = taxEventMapper.toEntity(returnedTaxEventDTO);
        assertTaxEventUpdatableFieldsEquals(returnedTaxEvent, getPersistedTaxEvent(returnedTaxEvent));

        insertedTaxEvent = returnedTaxEvent;
    }

    @Test
    void createTaxEventWithExistingId() throws Exception {
        // Create the TaxEvent with an existing ID
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();
        TaxEventDTO taxEventDTO = taxEventMapper.toDto(taxEvent);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(taxEventDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the TaxEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        taxEvent.setType(null);

        // Create the TaxEvent, which fails.
        TaxEventDTO taxEventDTO = taxEventMapper.toDto(taxEvent);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(taxEventDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkTaxDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        taxEvent.setTaxDate(null);

        // Create the TaxEvent, which fails.
        TaxEventDTO taxEventDTO = taxEventMapper.toDto(taxEvent);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(taxEventDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkAmountIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        taxEvent.setAmount(null);

        // Create the TaxEvent, which fails.
        TaxEventDTO taxEventDTO = taxEventMapper.toDto(taxEvent);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(taxEventDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkCurrencyIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        taxEvent.setCurrency(null);

        // Create the TaxEvent, which fails.
        TaxEventDTO taxEventDTO = taxEventMapper.toDto(taxEvent);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(taxEventDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllTaxEvents() {
        // Initialize the database
        taxEvent.setId(UUID.randomUUID());
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();

        // Get all the taxEventList
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
            .value(hasItem(taxEvent.getId().toString()))
            .jsonPath("$.[*].type")
            .value(hasItem(DEFAULT_TYPE.toString()))
            .jsonPath("$.[*].taxDate")
            .value(hasItem(DEFAULT_TAX_DATE.toString()))
            .jsonPath("$.[*].amount")
            .value(hasItem(sameNumber(DEFAULT_AMOUNT)))
            .jsonPath("$.[*].currency")
            .value(hasItem(DEFAULT_CURRENCY));
    }

    @Test
    void getTaxEvent() {
        // Initialize the database
        taxEvent.setId(UUID.randomUUID());
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();

        // Get the taxEvent
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, taxEvent.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(taxEvent.getId().toString()))
            .jsonPath("$.type")
            .value(is(DEFAULT_TYPE.toString()))
            .jsonPath("$.taxDate")
            .value(is(DEFAULT_TAX_DATE.toString()))
            .jsonPath("$.amount")
            .value(is(sameNumber(DEFAULT_AMOUNT)))
            .jsonPath("$.currency")
            .value(is(DEFAULT_CURRENCY));
    }

    @Test
    void getTaxEventsByIdFiltering() {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();

        UUID id = taxEvent.getId();

        defaultTaxEventFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    void getAllTaxEventsByTypeIsEqualToSomething() {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();

        // Get all the taxEventList where type equals to
        defaultTaxEventFiltering("type.equals=" + DEFAULT_TYPE, "type.equals=" + UPDATED_TYPE);
    }

    @Test
    void getAllTaxEventsByTypeIsInShouldWork() {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();

        // Get all the taxEventList where type in
        defaultTaxEventFiltering("type.in=" + DEFAULT_TYPE + "," + UPDATED_TYPE, "type.in=" + UPDATED_TYPE);
    }

    @Test
    void getAllTaxEventsByTypeIsNullOrNotNull() {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();

        // Get all the taxEventList where type is not null
        defaultTaxEventFiltering("type.specified=true", "type.specified=false");
    }

    @Test
    void getAllTaxEventsByTaxDateIsEqualToSomething() {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();

        // Get all the taxEventList where taxDate equals to
        defaultTaxEventFiltering("taxDate.equals=" + DEFAULT_TAX_DATE, "taxDate.equals=" + UPDATED_TAX_DATE);
    }

    @Test
    void getAllTaxEventsByTaxDateIsInShouldWork() {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();

        // Get all the taxEventList where taxDate in
        defaultTaxEventFiltering("taxDate.in=" + DEFAULT_TAX_DATE + "," + UPDATED_TAX_DATE, "taxDate.in=" + UPDATED_TAX_DATE);
    }

    @Test
    void getAllTaxEventsByTaxDateIsNullOrNotNull() {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();

        // Get all the taxEventList where taxDate is not null
        defaultTaxEventFiltering("taxDate.specified=true", "taxDate.specified=false");
    }

    @Test
    void getAllTaxEventsByTaxDateIsGreaterThanOrEqualToSomething() {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();

        // Get all the taxEventList where taxDate is greater than or equal to
        defaultTaxEventFiltering("taxDate.greaterThanOrEqual=" + DEFAULT_TAX_DATE, "taxDate.greaterThanOrEqual=" + UPDATED_TAX_DATE);
    }

    @Test
    void getAllTaxEventsByTaxDateIsLessThanOrEqualToSomething() {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();

        // Get all the taxEventList where taxDate is less than or equal to
        defaultTaxEventFiltering("taxDate.lessThanOrEqual=" + DEFAULT_TAX_DATE, "taxDate.lessThanOrEqual=" + SMALLER_TAX_DATE);
    }

    @Test
    void getAllTaxEventsByTaxDateIsLessThanSomething() {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();

        // Get all the taxEventList where taxDate is less than
        defaultTaxEventFiltering("taxDate.lessThan=" + UPDATED_TAX_DATE, "taxDate.lessThan=" + DEFAULT_TAX_DATE);
    }

    @Test
    void getAllTaxEventsByTaxDateIsGreaterThanSomething() {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();

        // Get all the taxEventList where taxDate is greater than
        defaultTaxEventFiltering("taxDate.greaterThan=" + SMALLER_TAX_DATE, "taxDate.greaterThan=" + DEFAULT_TAX_DATE);
    }

    @Test
    void getAllTaxEventsByAmountIsEqualToSomething() {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();

        // Get all the taxEventList where amount equals to
        defaultTaxEventFiltering("amount.equals=" + DEFAULT_AMOUNT, "amount.equals=" + UPDATED_AMOUNT);
    }

    @Test
    void getAllTaxEventsByAmountIsInShouldWork() {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();

        // Get all the taxEventList where amount in
        defaultTaxEventFiltering("amount.in=" + DEFAULT_AMOUNT + "," + UPDATED_AMOUNT, "amount.in=" + UPDATED_AMOUNT);
    }

    @Test
    void getAllTaxEventsByAmountIsNullOrNotNull() {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();

        // Get all the taxEventList where amount is not null
        defaultTaxEventFiltering("amount.specified=true", "amount.specified=false");
    }

    @Test
    void getAllTaxEventsByAmountIsGreaterThanOrEqualToSomething() {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();

        // Get all the taxEventList where amount is greater than or equal to
        defaultTaxEventFiltering("amount.greaterThanOrEqual=" + DEFAULT_AMOUNT, "amount.greaterThanOrEqual=" + UPDATED_AMOUNT);
    }

    @Test
    void getAllTaxEventsByAmountIsLessThanOrEqualToSomething() {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();

        // Get all the taxEventList where amount is less than or equal to
        defaultTaxEventFiltering("amount.lessThanOrEqual=" + DEFAULT_AMOUNT, "amount.lessThanOrEqual=" + SMALLER_AMOUNT);
    }

    @Test
    void getAllTaxEventsByAmountIsLessThanSomething() {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();

        // Get all the taxEventList where amount is less than
        defaultTaxEventFiltering("amount.lessThan=" + UPDATED_AMOUNT, "amount.lessThan=" + DEFAULT_AMOUNT);
    }

    @Test
    void getAllTaxEventsByAmountIsGreaterThanSomething() {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();

        // Get all the taxEventList where amount is greater than
        defaultTaxEventFiltering("amount.greaterThan=" + SMALLER_AMOUNT, "amount.greaterThan=" + DEFAULT_AMOUNT);
    }

    @Test
    void getAllTaxEventsByCurrencyIsEqualToSomething() {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();

        // Get all the taxEventList where currency equals to
        defaultTaxEventFiltering("currency.equals=" + DEFAULT_CURRENCY, "currency.equals=" + UPDATED_CURRENCY);
    }

    @Test
    void getAllTaxEventsByCurrencyIsInShouldWork() {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();

        // Get all the taxEventList where currency in
        defaultTaxEventFiltering("currency.in=" + DEFAULT_CURRENCY + "," + UPDATED_CURRENCY, "currency.in=" + UPDATED_CURRENCY);
    }

    @Test
    void getAllTaxEventsByCurrencyIsNullOrNotNull() {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();

        // Get all the taxEventList where currency is not null
        defaultTaxEventFiltering("currency.specified=true", "currency.specified=false");
    }

    @Test
    void getAllTaxEventsByCurrencyContainsSomething() {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();

        // Get all the taxEventList where currency contains
        defaultTaxEventFiltering("currency.contains=" + DEFAULT_CURRENCY, "currency.contains=" + UPDATED_CURRENCY);
    }

    @Test
    void getAllTaxEventsByCurrencyNotContainsSomething() {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();

        // Get all the taxEventList where currency does not contain
        defaultTaxEventFiltering("currency.doesNotContain=" + UPDATED_CURRENCY, "currency.doesNotContain=" + DEFAULT_CURRENCY);
    }

    @Test
    void getAllTaxEventsByAccountIsEqualToSomething() {
        BrokerAccount account = BrokerAccountResourceIT.createEntity(em);
        brokerAccountRepository.save(account).block();
        UUID accountId = account.getId();
        taxEvent.setAccountId(accountId);
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();
        // Get all the taxEventList where account equals to accountId
        defaultTaxEventShouldBeFound("accountId.equals=" + accountId);

        // Get all the taxEventList where account equals to UUID.randomUUID()
        defaultTaxEventShouldNotBeFound("accountId.equals=" + UUID.randomUUID());
    }

    @Test
    void getAllTaxEventsByOperationIsEqualToSomething() {
        Operation operation = OperationResourceIT.createEntity(em);
        operationRepository.save(operation).block();
        UUID operationId = operation.getId();
        taxEvent.setOperationId(operationId);
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();
        // Get all the taxEventList where operation equals to operationId
        defaultTaxEventShouldBeFound("operationId.equals=" + operationId);

        // Get all the taxEventList where operation equals to UUID.randomUUID()
        defaultTaxEventShouldNotBeFound("operationId.equals=" + UUID.randomUUID());
    }

    @Test
    void getAllTaxEventsByIncomeEventIsEqualToSomething() {
        IncomeEvent incomeEvent = IncomeEventResourceIT.createEntity(em);
        incomeEventRepository.save(incomeEvent).block();
        UUID incomeEventId = incomeEvent.getId();
        taxEvent.setIncomeEventId(incomeEventId);
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();
        // Get all the taxEventList where incomeEvent equals to incomeEventId
        defaultTaxEventShouldBeFound("incomeEventId.equals=" + incomeEventId);

        // Get all the taxEventList where incomeEvent equals to UUID.randomUUID()
        defaultTaxEventShouldNotBeFound("incomeEventId.equals=" + UUID.randomUUID());
    }

    private void defaultTaxEventFiltering(String shouldBeFound, String shouldNotBeFound) {
        defaultTaxEventShouldBeFound(shouldBeFound);
        defaultTaxEventShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultTaxEventShouldBeFound(String filter) {
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
            .value(hasItem(taxEvent.getId().toString()))
            .jsonPath("$.[*].type")
            .value(hasItem(DEFAULT_TYPE.toString()))

            .jsonPath("$.[*].taxDate")
            .value(hasItem(DEFAULT_TAX_DATE.toString()))

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
    private void defaultTaxEventShouldNotBeFound(String filter) {
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
    void getNonExistingTaxEvent() {
        // Get the taxEvent
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingTaxEvent() throws Exception {
        // Initialize the database
        taxEvent.setId(UUID.randomUUID());
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the taxEvent
        TaxEvent updatedTaxEvent = taxEventRepository.findById(taxEvent.getId()).block();
        updatedTaxEvent.type(UPDATED_TYPE).taxDate(UPDATED_TAX_DATE).amount(UPDATED_AMOUNT).currency(UPDATED_CURRENCY);
        TaxEventDTO taxEventDTO = taxEventMapper.toDto(updatedTaxEvent);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, taxEventDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(taxEventDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the TaxEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedTaxEventToMatchAllProperties(updatedTaxEvent);
    }

    @Test
    void putNonExistingTaxEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        taxEvent.setId(UUID.randomUUID());

        // Create the TaxEvent
        TaxEventDTO taxEventDTO = taxEventMapper.toDto(taxEvent);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, taxEventDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(taxEventDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the TaxEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchTaxEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        taxEvent.setId(UUID.randomUUID());

        // Create the TaxEvent
        TaxEventDTO taxEventDTO = taxEventMapper.toDto(taxEvent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(taxEventDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the TaxEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamTaxEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        taxEvent.setId(UUID.randomUUID());

        // Create the TaxEvent
        TaxEventDTO taxEventDTO = taxEventMapper.toDto(taxEvent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(taxEventDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the TaxEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateTaxEventWithPatch() throws Exception {
        // Initialize the database
        taxEvent.setId(UUID.randomUUID());
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the taxEvent using partial update
        TaxEvent partialUpdatedTaxEvent = new TaxEvent();
        partialUpdatedTaxEvent.setId(taxEvent.getId());

        partialUpdatedTaxEvent.type(UPDATED_TYPE).taxDate(UPDATED_TAX_DATE).amount(UPDATED_AMOUNT);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedTaxEvent.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedTaxEvent))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the TaxEvent in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTaxEventUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedTaxEvent, taxEvent), getPersistedTaxEvent(taxEvent));
    }

    @Test
    void fullUpdateTaxEventWithPatch() throws Exception {
        // Initialize the database
        taxEvent.setId(UUID.randomUUID());
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the taxEvent using partial update
        TaxEvent partialUpdatedTaxEvent = new TaxEvent();
        partialUpdatedTaxEvent.setId(taxEvent.getId());

        partialUpdatedTaxEvent.type(UPDATED_TYPE).taxDate(UPDATED_TAX_DATE).amount(UPDATED_AMOUNT).currency(UPDATED_CURRENCY);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedTaxEvent.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedTaxEvent))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the TaxEvent in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTaxEventUpdatableFieldsEquals(partialUpdatedTaxEvent, getPersistedTaxEvent(partialUpdatedTaxEvent));
    }

    @Test
    void patchNonExistingTaxEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        taxEvent.setId(UUID.randomUUID());

        // Create the TaxEvent
        TaxEventDTO taxEventDTO = taxEventMapper.toDto(taxEvent);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, taxEventDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(taxEventDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the TaxEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchTaxEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        taxEvent.setId(UUID.randomUUID());

        // Create the TaxEvent
        TaxEventDTO taxEventDTO = taxEventMapper.toDto(taxEvent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(taxEventDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the TaxEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamTaxEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        taxEvent.setId(UUID.randomUUID());

        // Create the TaxEvent
        TaxEventDTO taxEventDTO = taxEventMapper.toDto(taxEvent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(taxEventDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the TaxEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteTaxEvent() {
        // Initialize the database
        taxEvent.setId(UUID.randomUUID());
        insertedTaxEvent = taxEventRepository.save(taxEvent).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the taxEvent
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, taxEvent.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return taxEventRepository.count().block();
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

    protected TaxEvent getPersistedTaxEvent(TaxEvent taxEvent) {
        return taxEventRepository.findById(taxEvent.getId()).block();
    }

    protected void assertPersistedTaxEventToMatchAllProperties(TaxEvent expectedTaxEvent) {
        // Test fails because reactive api returns an empty object instead of null
        // assertTaxEventAllPropertiesEquals(expectedTaxEvent, getPersistedTaxEvent(expectedTaxEvent));
        assertTaxEventUpdatableFieldsEquals(expectedTaxEvent, getPersistedTaxEvent(expectedTaxEvent));
    }

    protected void assertPersistedTaxEventToMatchUpdatableProperties(TaxEvent expectedTaxEvent) {
        // Test fails because reactive api returns an empty object instead of null
        // assertTaxEventAllUpdatablePropertiesEquals(expectedTaxEvent, getPersistedTaxEvent(expectedTaxEvent));
        assertTaxEventUpdatableFieldsEquals(expectedTaxEvent, getPersistedTaxEvent(expectedTaxEvent));
    }
}
