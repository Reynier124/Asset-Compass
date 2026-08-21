package com.assetcompass.portfolio.web.rest;

import static com.assetcompass.portfolio.domain.TaxEventAsserts.*;
import static com.assetcompass.portfolio.web.rest.TestUtil.createUpdateProxyForBean;
import static com.assetcompass.portfolio.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.assetcompass.portfolio.IntegrationTest;
import com.assetcompass.portfolio.domain.BrokerAccount;
import com.assetcompass.portfolio.domain.IncomeEvent;
import com.assetcompass.portfolio.domain.Operation;
import com.assetcompass.portfolio.domain.TaxEvent;
import com.assetcompass.portfolio.domain.enumeration.TaxType;
import com.assetcompass.portfolio.repository.TaxEventRepository;
import com.assetcompass.portfolio.service.dto.TaxEventDTO;
import com.assetcompass.portfolio.service.mapper.TaxEventMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link TaxEventResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class TaxEventResourceIT {

    private static final TaxType DEFAULT_TYPE = TaxType.RETENCION_DIVIDENDO;
    private static final TaxType UPDATED_TYPE = TaxType.IVA;

    private static final LocalDate DEFAULT_TAX_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_TAX_DATE = LocalDate.parse("2023-12-02");
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
    private MockMvc restTaxEventMockMvc;

    private TaxEvent taxEvent;

    private TaxEvent insertedTaxEvent;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TaxEvent createEntity(EntityManager em) {
        TaxEvent taxEvent = new TaxEvent().type(DEFAULT_TYPE).taxDate(DEFAULT_TAX_DATE).amount(DEFAULT_AMOUNT).currency(DEFAULT_CURRENCY);
        // Add required entity
        BrokerAccount brokerAccount;
        if (TestUtil.findAll(em, BrokerAccount.class).isEmpty()) {
            brokerAccount = BrokerAccountResourceIT.createEntity(em);
            em.persist(brokerAccount);
            em.flush();
        } else {
            brokerAccount = TestUtil.findAll(em, BrokerAccount.class).get(0);
        }
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
            .type(UPDATED_TYPE)
            .taxDate(UPDATED_TAX_DATE)
            .amount(UPDATED_AMOUNT)
            .currency(UPDATED_CURRENCY);
        // Add required entity
        BrokerAccount brokerAccount;
        if (TestUtil.findAll(em, BrokerAccount.class).isEmpty()) {
            brokerAccount = BrokerAccountResourceIT.createUpdatedEntity(em);
            em.persist(brokerAccount);
            em.flush();
        } else {
            brokerAccount = TestUtil.findAll(em, BrokerAccount.class).get(0);
        }
        updatedTaxEvent.setAccount(brokerAccount);
        return updatedTaxEvent;
    }

    @BeforeEach
    void initTest() {
        taxEvent = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedTaxEvent != null) {
            taxEventRepository.delete(insertedTaxEvent);
            insertedTaxEvent = null;
        }
    }

    @Test
    @Transactional
    void createTaxEvent() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the TaxEvent
        TaxEventDTO taxEventDTO = taxEventMapper.toDto(taxEvent);
        var returnedTaxEventDTO = om.readValue(
            restTaxEventMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(taxEventDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            TaxEventDTO.class
        );

        // Validate the TaxEvent in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedTaxEvent = taxEventMapper.toEntity(returnedTaxEventDTO);
        assertTaxEventUpdatableFieldsEquals(returnedTaxEvent, getPersistedTaxEvent(returnedTaxEvent));

        insertedTaxEvent = returnedTaxEvent;
    }

    @Test
    @Transactional
    void createTaxEventWithExistingId() throws Exception {
        // Create the TaxEvent with an existing ID
        insertedTaxEvent = taxEventRepository.saveAndFlush(taxEvent);
        TaxEventDTO taxEventDTO = taxEventMapper.toDto(taxEvent);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTaxEventMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(taxEventDTO)))
            .andExpect(status().isBadRequest());

        // Validate the TaxEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        taxEvent.setType(null);

        // Create the TaxEvent, which fails.
        TaxEventDTO taxEventDTO = taxEventMapper.toDto(taxEvent);

        restTaxEventMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(taxEventDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTaxDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        taxEvent.setTaxDate(null);

        // Create the TaxEvent, which fails.
        TaxEventDTO taxEventDTO = taxEventMapper.toDto(taxEvent);

        restTaxEventMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(taxEventDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAmountIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        taxEvent.setAmount(null);

        // Create the TaxEvent, which fails.
        TaxEventDTO taxEventDTO = taxEventMapper.toDto(taxEvent);

        restTaxEventMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(taxEventDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCurrencyIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        taxEvent.setCurrency(null);

        // Create the TaxEvent, which fails.
        TaxEventDTO taxEventDTO = taxEventMapper.toDto(taxEvent);

        restTaxEventMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(taxEventDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllTaxEvents() throws Exception {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.saveAndFlush(taxEvent);

        // Get all the taxEventList
        restTaxEventMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(taxEvent.getId().toString())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].taxDate").value(hasItem(DEFAULT_TAX_DATE.toString())))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(sameNumber(DEFAULT_AMOUNT))))
            .andExpect(jsonPath("$.[*].currency").value(hasItem(DEFAULT_CURRENCY)));
    }

    @Test
    @Transactional
    void getTaxEvent() throws Exception {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.saveAndFlush(taxEvent);

        // Get the taxEvent
        restTaxEventMockMvc
            .perform(get(ENTITY_API_URL_ID, taxEvent.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(taxEvent.getId().toString()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.taxDate").value(DEFAULT_TAX_DATE.toString()))
            .andExpect(jsonPath("$.amount").value(sameNumber(DEFAULT_AMOUNT)))
            .andExpect(jsonPath("$.currency").value(DEFAULT_CURRENCY));
    }

    @Test
    @Transactional
    void getTaxEventsByIdFiltering() throws Exception {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.saveAndFlush(taxEvent);

        UUID id = taxEvent.getId();

        defaultTaxEventFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    @Transactional
    void getAllTaxEventsByTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.saveAndFlush(taxEvent);

        // Get all the taxEventList where type equals to
        defaultTaxEventFiltering("type.equals=" + DEFAULT_TYPE, "type.equals=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllTaxEventsByTypeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.saveAndFlush(taxEvent);

        // Get all the taxEventList where type in
        defaultTaxEventFiltering("type.in=" + DEFAULT_TYPE + "," + UPDATED_TYPE, "type.in=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllTaxEventsByTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.saveAndFlush(taxEvent);

        // Get all the taxEventList where type is not null
        defaultTaxEventFiltering("type.specified=true", "type.specified=false");
    }

    @Test
    @Transactional
    void getAllTaxEventsByTaxDateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.saveAndFlush(taxEvent);

        // Get all the taxEventList where taxDate equals to
        defaultTaxEventFiltering("taxDate.equals=" + DEFAULT_TAX_DATE, "taxDate.equals=" + UPDATED_TAX_DATE);
    }

    @Test
    @Transactional
    void getAllTaxEventsByTaxDateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.saveAndFlush(taxEvent);

        // Get all the taxEventList where taxDate in
        defaultTaxEventFiltering("taxDate.in=" + DEFAULT_TAX_DATE + "," + UPDATED_TAX_DATE, "taxDate.in=" + UPDATED_TAX_DATE);
    }

    @Test
    @Transactional
    void getAllTaxEventsByTaxDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.saveAndFlush(taxEvent);

        // Get all the taxEventList where taxDate is not null
        defaultTaxEventFiltering("taxDate.specified=true", "taxDate.specified=false");
    }

    @Test
    @Transactional
    void getAllTaxEventsByTaxDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.saveAndFlush(taxEvent);

        // Get all the taxEventList where taxDate is greater than or equal to
        defaultTaxEventFiltering("taxDate.greaterThanOrEqual=" + DEFAULT_TAX_DATE, "taxDate.greaterThanOrEqual=" + UPDATED_TAX_DATE);
    }

    @Test
    @Transactional
    void getAllTaxEventsByTaxDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.saveAndFlush(taxEvent);

        // Get all the taxEventList where taxDate is less than or equal to
        defaultTaxEventFiltering("taxDate.lessThanOrEqual=" + DEFAULT_TAX_DATE, "taxDate.lessThanOrEqual=" + SMALLER_TAX_DATE);
    }

    @Test
    @Transactional
    void getAllTaxEventsByTaxDateIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.saveAndFlush(taxEvent);

        // Get all the taxEventList where taxDate is less than
        defaultTaxEventFiltering("taxDate.lessThan=" + UPDATED_TAX_DATE, "taxDate.lessThan=" + DEFAULT_TAX_DATE);
    }

    @Test
    @Transactional
    void getAllTaxEventsByTaxDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.saveAndFlush(taxEvent);

        // Get all the taxEventList where taxDate is greater than
        defaultTaxEventFiltering("taxDate.greaterThan=" + SMALLER_TAX_DATE, "taxDate.greaterThan=" + DEFAULT_TAX_DATE);
    }

    @Test
    @Transactional
    void getAllTaxEventsByAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.saveAndFlush(taxEvent);

        // Get all the taxEventList where amount equals to
        defaultTaxEventFiltering("amount.equals=" + DEFAULT_AMOUNT, "amount.equals=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTaxEventsByAmountIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.saveAndFlush(taxEvent);

        // Get all the taxEventList where amount in
        defaultTaxEventFiltering("amount.in=" + DEFAULT_AMOUNT + "," + UPDATED_AMOUNT, "amount.in=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTaxEventsByAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.saveAndFlush(taxEvent);

        // Get all the taxEventList where amount is not null
        defaultTaxEventFiltering("amount.specified=true", "amount.specified=false");
    }

    @Test
    @Transactional
    void getAllTaxEventsByAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.saveAndFlush(taxEvent);

        // Get all the taxEventList where amount is greater than or equal to
        defaultTaxEventFiltering("amount.greaterThanOrEqual=" + DEFAULT_AMOUNT, "amount.greaterThanOrEqual=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTaxEventsByAmountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.saveAndFlush(taxEvent);

        // Get all the taxEventList where amount is less than or equal to
        defaultTaxEventFiltering("amount.lessThanOrEqual=" + DEFAULT_AMOUNT, "amount.lessThanOrEqual=" + SMALLER_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTaxEventsByAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.saveAndFlush(taxEvent);

        // Get all the taxEventList where amount is less than
        defaultTaxEventFiltering("amount.lessThan=" + UPDATED_AMOUNT, "amount.lessThan=" + DEFAULT_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTaxEventsByAmountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.saveAndFlush(taxEvent);

        // Get all the taxEventList where amount is greater than
        defaultTaxEventFiltering("amount.greaterThan=" + SMALLER_AMOUNT, "amount.greaterThan=" + DEFAULT_AMOUNT);
    }

    @Test
    @Transactional
    void getAllTaxEventsByCurrencyIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.saveAndFlush(taxEvent);

        // Get all the taxEventList where currency equals to
        defaultTaxEventFiltering("currency.equals=" + DEFAULT_CURRENCY, "currency.equals=" + UPDATED_CURRENCY);
    }

    @Test
    @Transactional
    void getAllTaxEventsByCurrencyIsInShouldWork() throws Exception {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.saveAndFlush(taxEvent);

        // Get all the taxEventList where currency in
        defaultTaxEventFiltering("currency.in=" + DEFAULT_CURRENCY + "," + UPDATED_CURRENCY, "currency.in=" + UPDATED_CURRENCY);
    }

    @Test
    @Transactional
    void getAllTaxEventsByCurrencyIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.saveAndFlush(taxEvent);

        // Get all the taxEventList where currency is not null
        defaultTaxEventFiltering("currency.specified=true", "currency.specified=false");
    }

    @Test
    @Transactional
    void getAllTaxEventsByCurrencyContainsSomething() throws Exception {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.saveAndFlush(taxEvent);

        // Get all the taxEventList where currency contains
        defaultTaxEventFiltering("currency.contains=" + DEFAULT_CURRENCY, "currency.contains=" + UPDATED_CURRENCY);
    }

    @Test
    @Transactional
    void getAllTaxEventsByCurrencyNotContainsSomething() throws Exception {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.saveAndFlush(taxEvent);

        // Get all the taxEventList where currency does not contain
        defaultTaxEventFiltering("currency.doesNotContain=" + UPDATED_CURRENCY, "currency.doesNotContain=" + DEFAULT_CURRENCY);
    }

    @Test
    @Transactional
    void getAllTaxEventsByAccountIsEqualToSomething() throws Exception {
        BrokerAccount account;
        if (TestUtil.findAll(em, BrokerAccount.class).isEmpty()) {
            taxEventRepository.saveAndFlush(taxEvent);
            account = BrokerAccountResourceIT.createEntity(em);
        } else {
            account = TestUtil.findAll(em, BrokerAccount.class).get(0);
        }
        em.persist(account);
        em.flush();
        taxEvent.setAccount(account);
        taxEventRepository.saveAndFlush(taxEvent);
        UUID accountId = account.getId();
        // Get all the taxEventList where account equals to accountId
        defaultTaxEventShouldBeFound("accountId.equals=" + accountId);

        // Get all the taxEventList where account equals to UUID.randomUUID()
        defaultTaxEventShouldNotBeFound("accountId.equals=" + UUID.randomUUID());
    }

    @Test
    @Transactional
    void getAllTaxEventsByOperationIsEqualToSomething() throws Exception {
        Operation operation;
        if (TestUtil.findAll(em, Operation.class).isEmpty()) {
            taxEventRepository.saveAndFlush(taxEvent);
            operation = OperationResourceIT.createEntity(em);
        } else {
            operation = TestUtil.findAll(em, Operation.class).get(0);
        }
        em.persist(operation);
        em.flush();
        taxEvent.setOperation(operation);
        taxEventRepository.saveAndFlush(taxEvent);
        UUID operationId = operation.getId();
        // Get all the taxEventList where operation equals to operationId
        defaultTaxEventShouldBeFound("operationId.equals=" + operationId);

        // Get all the taxEventList where operation equals to UUID.randomUUID()
        defaultTaxEventShouldNotBeFound("operationId.equals=" + UUID.randomUUID());
    }

    @Test
    @Transactional
    void getAllTaxEventsByIncomeEventIsEqualToSomething() throws Exception {
        IncomeEvent incomeEvent;
        if (TestUtil.findAll(em, IncomeEvent.class).isEmpty()) {
            taxEventRepository.saveAndFlush(taxEvent);
            incomeEvent = IncomeEventResourceIT.createEntity(em);
        } else {
            incomeEvent = TestUtil.findAll(em, IncomeEvent.class).get(0);
        }
        em.persist(incomeEvent);
        em.flush();
        taxEvent.setIncomeEvent(incomeEvent);
        taxEventRepository.saveAndFlush(taxEvent);
        UUID incomeEventId = incomeEvent.getId();
        // Get all the taxEventList where incomeEvent equals to incomeEventId
        defaultTaxEventShouldBeFound("incomeEventId.equals=" + incomeEventId);

        // Get all the taxEventList where incomeEvent equals to UUID.randomUUID()
        defaultTaxEventShouldNotBeFound("incomeEventId.equals=" + UUID.randomUUID());
    }

    private void defaultTaxEventFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultTaxEventShouldBeFound(shouldBeFound);
        defaultTaxEventShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultTaxEventShouldBeFound(String filter) throws Exception {
        restTaxEventMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(taxEvent.getId().toString())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].taxDate").value(hasItem(DEFAULT_TAX_DATE.toString())))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(sameNumber(DEFAULT_AMOUNT))))
            .andExpect(jsonPath("$.[*].currency").value(hasItem(DEFAULT_CURRENCY)));

        // Check, that the count call also returns 1
        restTaxEventMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultTaxEventShouldNotBeFound(String filter) throws Exception {
        restTaxEventMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restTaxEventMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingTaxEvent() throws Exception {
        // Get the taxEvent
        restTaxEventMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingTaxEvent() throws Exception {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.saveAndFlush(taxEvent);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the taxEvent
        TaxEvent updatedTaxEvent = taxEventRepository.findById(taxEvent.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedTaxEvent are not directly saved in db
        em.detach(updatedTaxEvent);
        updatedTaxEvent.type(UPDATED_TYPE).taxDate(UPDATED_TAX_DATE).amount(UPDATED_AMOUNT).currency(UPDATED_CURRENCY);
        TaxEventDTO taxEventDTO = taxEventMapper.toDto(updatedTaxEvent);

        restTaxEventMockMvc
            .perform(
                put(ENTITY_API_URL_ID, taxEventDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(taxEventDTO))
            )
            .andExpect(status().isOk());

        // Validate the TaxEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedTaxEventToMatchAllProperties(updatedTaxEvent);
    }

    @Test
    @Transactional
    void putNonExistingTaxEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        taxEvent.setId(UUID.randomUUID());

        // Create the TaxEvent
        TaxEventDTO taxEventDTO = taxEventMapper.toDto(taxEvent);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTaxEventMockMvc
            .perform(
                put(ENTITY_API_URL_ID, taxEventDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(taxEventDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TaxEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchTaxEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        taxEvent.setId(UUID.randomUUID());

        // Create the TaxEvent
        TaxEventDTO taxEventDTO = taxEventMapper.toDto(taxEvent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTaxEventMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(taxEventDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TaxEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTaxEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        taxEvent.setId(UUID.randomUUID());

        // Create the TaxEvent
        TaxEventDTO taxEventDTO = taxEventMapper.toDto(taxEvent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTaxEventMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(taxEventDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the TaxEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateTaxEventWithPatch() throws Exception {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.saveAndFlush(taxEvent);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the taxEvent using partial update
        TaxEvent partialUpdatedTaxEvent = new TaxEvent();
        partialUpdatedTaxEvent.setId(taxEvent.getId());

        partialUpdatedTaxEvent.currency(UPDATED_CURRENCY);

        restTaxEventMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTaxEvent.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTaxEvent))
            )
            .andExpect(status().isOk());

        // Validate the TaxEvent in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTaxEventUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedTaxEvent, taxEvent), getPersistedTaxEvent(taxEvent));
    }

    @Test
    @Transactional
    void fullUpdateTaxEventWithPatch() throws Exception {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.saveAndFlush(taxEvent);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the taxEvent using partial update
        TaxEvent partialUpdatedTaxEvent = new TaxEvent();
        partialUpdatedTaxEvent.setId(taxEvent.getId());

        partialUpdatedTaxEvent.type(UPDATED_TYPE).taxDate(UPDATED_TAX_DATE).amount(UPDATED_AMOUNT).currency(UPDATED_CURRENCY);

        restTaxEventMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTaxEvent.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTaxEvent))
            )
            .andExpect(status().isOk());

        // Validate the TaxEvent in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTaxEventUpdatableFieldsEquals(partialUpdatedTaxEvent, getPersistedTaxEvent(partialUpdatedTaxEvent));
    }

    @Test
    @Transactional
    void patchNonExistingTaxEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        taxEvent.setId(UUID.randomUUID());

        // Create the TaxEvent
        TaxEventDTO taxEventDTO = taxEventMapper.toDto(taxEvent);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTaxEventMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, taxEventDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(taxEventDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TaxEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTaxEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        taxEvent.setId(UUID.randomUUID());

        // Create the TaxEvent
        TaxEventDTO taxEventDTO = taxEventMapper.toDto(taxEvent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTaxEventMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(taxEventDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TaxEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTaxEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        taxEvent.setId(UUID.randomUUID());

        // Create the TaxEvent
        TaxEventDTO taxEventDTO = taxEventMapper.toDto(taxEvent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTaxEventMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(taxEventDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the TaxEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTaxEvent() throws Exception {
        // Initialize the database
        insertedTaxEvent = taxEventRepository.saveAndFlush(taxEvent);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the taxEvent
        restTaxEventMockMvc
            .perform(delete(ENTITY_API_URL_ID, taxEvent.getId().toString()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return taxEventRepository.count();
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
        return taxEventRepository.findById(taxEvent.getId()).orElseThrow();
    }

    protected void assertPersistedTaxEventToMatchAllProperties(TaxEvent expectedTaxEvent) {
        assertTaxEventAllPropertiesEquals(expectedTaxEvent, getPersistedTaxEvent(expectedTaxEvent));
    }

    protected void assertPersistedTaxEventToMatchUpdatableProperties(TaxEvent expectedTaxEvent) {
        assertTaxEventAllUpdatablePropertiesEquals(expectedTaxEvent, getPersistedTaxEvent(expectedTaxEvent));
    }
}
