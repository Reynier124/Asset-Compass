package com.assetcompass.portfolio.web.rest;

import static com.assetcompass.portfolio.domain.IncomeEventAsserts.*;
import static com.assetcompass.portfolio.web.rest.TestUtil.createUpdateProxyForBean;
import static com.assetcompass.portfolio.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.assetcompass.portfolio.IntegrationTest;
import com.assetcompass.portfolio.domain.Asset;
import com.assetcompass.portfolio.domain.BrokerAccount;
import com.assetcompass.portfolio.domain.IncomeEvent;
import com.assetcompass.portfolio.domain.enumeration.IncomeType;
import com.assetcompass.portfolio.repository.IncomeEventRepository;
import com.assetcompass.portfolio.service.dto.IncomeEventDTO;
import com.assetcompass.portfolio.service.mapper.IncomeEventMapper;
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
 * Integration tests for the {@link IncomeEventResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class IncomeEventResourceIT {

    private static final IncomeType DEFAULT_TYPE = IncomeType.DIVIDENDO;
    private static final IncomeType UPDATED_TYPE = IncomeType.RENTA;

    private static final LocalDate DEFAULT_EVENT_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_EVENT_DATE = LocalDate.parse("2023-12-02");
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
    private MockMvc restIncomeEventMockMvc;

    private IncomeEvent incomeEvent;

    private IncomeEvent insertedIncomeEvent;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static IncomeEvent createEntity(EntityManager em) {
        IncomeEvent incomeEvent = new IncomeEvent()
            .type(DEFAULT_TYPE)
            .eventDate(DEFAULT_EVENT_DATE)
            .amount(DEFAULT_AMOUNT)
            .currency(DEFAULT_CURRENCY);
        // Add required entity
        BrokerAccount brokerAccount;
        if (TestUtil.findAll(em, BrokerAccount.class).isEmpty()) {
            brokerAccount = BrokerAccountResourceIT.createEntity(em);
            em.persist(brokerAccount);
            em.flush();
        } else {
            brokerAccount = TestUtil.findAll(em, BrokerAccount.class).get(0);
        }
        incomeEvent.setAccount(brokerAccount);
        // Add required entity
        Asset asset;
        if (TestUtil.findAll(em, Asset.class).isEmpty()) {
            asset = AssetResourceIT.createEntity();
            em.persist(asset);
            em.flush();
        } else {
            asset = TestUtil.findAll(em, Asset.class).get(0);
        }
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
            .type(UPDATED_TYPE)
            .eventDate(UPDATED_EVENT_DATE)
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
        updatedIncomeEvent.setAccount(brokerAccount);
        // Add required entity
        Asset asset;
        if (TestUtil.findAll(em, Asset.class).isEmpty()) {
            asset = AssetResourceIT.createUpdatedEntity();
            em.persist(asset);
            em.flush();
        } else {
            asset = TestUtil.findAll(em, Asset.class).get(0);
        }
        updatedIncomeEvent.setAsset(asset);
        return updatedIncomeEvent;
    }

    @BeforeEach
    void initTest() {
        incomeEvent = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedIncomeEvent != null) {
            incomeEventRepository.delete(insertedIncomeEvent);
            insertedIncomeEvent = null;
        }
    }

    @Test
    @Transactional
    void createIncomeEvent() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the IncomeEvent
        IncomeEventDTO incomeEventDTO = incomeEventMapper.toDto(incomeEvent);
        var returnedIncomeEventDTO = om.readValue(
            restIncomeEventMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(incomeEventDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            IncomeEventDTO.class
        );

        // Validate the IncomeEvent in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedIncomeEvent = incomeEventMapper.toEntity(returnedIncomeEventDTO);
        assertIncomeEventUpdatableFieldsEquals(returnedIncomeEvent, getPersistedIncomeEvent(returnedIncomeEvent));

        insertedIncomeEvent = returnedIncomeEvent;
    }

    @Test
    @Transactional
    void createIncomeEventWithExistingId() throws Exception {
        // Create the IncomeEvent with an existing ID
        insertedIncomeEvent = incomeEventRepository.saveAndFlush(incomeEvent);
        IncomeEventDTO incomeEventDTO = incomeEventMapper.toDto(incomeEvent);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restIncomeEventMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(incomeEventDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the IncomeEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        incomeEvent.setType(null);

        // Create the IncomeEvent, which fails.
        IncomeEventDTO incomeEventDTO = incomeEventMapper.toDto(incomeEvent);

        restIncomeEventMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(incomeEventDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEventDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        incomeEvent.setEventDate(null);

        // Create the IncomeEvent, which fails.
        IncomeEventDTO incomeEventDTO = incomeEventMapper.toDto(incomeEvent);

        restIncomeEventMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(incomeEventDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAmountIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        incomeEvent.setAmount(null);

        // Create the IncomeEvent, which fails.
        IncomeEventDTO incomeEventDTO = incomeEventMapper.toDto(incomeEvent);

        restIncomeEventMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(incomeEventDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCurrencyIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        incomeEvent.setCurrency(null);

        // Create the IncomeEvent, which fails.
        IncomeEventDTO incomeEventDTO = incomeEventMapper.toDto(incomeEvent);

        restIncomeEventMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(incomeEventDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllIncomeEvents() throws Exception {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.saveAndFlush(incomeEvent);

        // Get all the incomeEventList
        restIncomeEventMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(incomeEvent.getId().toString())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].eventDate").value(hasItem(DEFAULT_EVENT_DATE.toString())))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(sameNumber(DEFAULT_AMOUNT))))
            .andExpect(jsonPath("$.[*].currency").value(hasItem(DEFAULT_CURRENCY)));
    }

    @Test
    @Transactional
    void getIncomeEvent() throws Exception {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.saveAndFlush(incomeEvent);

        // Get the incomeEvent
        restIncomeEventMockMvc
            .perform(get(ENTITY_API_URL_ID, incomeEvent.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(incomeEvent.getId().toString()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.eventDate").value(DEFAULT_EVENT_DATE.toString()))
            .andExpect(jsonPath("$.amount").value(sameNumber(DEFAULT_AMOUNT)))
            .andExpect(jsonPath("$.currency").value(DEFAULT_CURRENCY));
    }

    @Test
    @Transactional
    void getIncomeEventsByIdFiltering() throws Exception {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.saveAndFlush(incomeEvent);

        UUID id = incomeEvent.getId();

        defaultIncomeEventFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    @Transactional
    void getAllIncomeEventsByTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.saveAndFlush(incomeEvent);

        // Get all the incomeEventList where type equals to
        defaultIncomeEventFiltering("type.equals=" + DEFAULT_TYPE, "type.equals=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllIncomeEventsByTypeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.saveAndFlush(incomeEvent);

        // Get all the incomeEventList where type in
        defaultIncomeEventFiltering("type.in=" + DEFAULT_TYPE + "," + UPDATED_TYPE, "type.in=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllIncomeEventsByTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.saveAndFlush(incomeEvent);

        // Get all the incomeEventList where type is not null
        defaultIncomeEventFiltering("type.specified=true", "type.specified=false");
    }

    @Test
    @Transactional
    void getAllIncomeEventsByEventDateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.saveAndFlush(incomeEvent);

        // Get all the incomeEventList where eventDate equals to
        defaultIncomeEventFiltering("eventDate.equals=" + DEFAULT_EVENT_DATE, "eventDate.equals=" + UPDATED_EVENT_DATE);
    }

    @Test
    @Transactional
    void getAllIncomeEventsByEventDateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.saveAndFlush(incomeEvent);

        // Get all the incomeEventList where eventDate in
        defaultIncomeEventFiltering("eventDate.in=" + DEFAULT_EVENT_DATE + "," + UPDATED_EVENT_DATE, "eventDate.in=" + UPDATED_EVENT_DATE);
    }

    @Test
    @Transactional
    void getAllIncomeEventsByEventDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.saveAndFlush(incomeEvent);

        // Get all the incomeEventList where eventDate is not null
        defaultIncomeEventFiltering("eventDate.specified=true", "eventDate.specified=false");
    }

    @Test
    @Transactional
    void getAllIncomeEventsByEventDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.saveAndFlush(incomeEvent);

        // Get all the incomeEventList where eventDate is greater than or equal to
        defaultIncomeEventFiltering(
            "eventDate.greaterThanOrEqual=" + DEFAULT_EVENT_DATE,
            "eventDate.greaterThanOrEqual=" + UPDATED_EVENT_DATE
        );
    }

    @Test
    @Transactional
    void getAllIncomeEventsByEventDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.saveAndFlush(incomeEvent);

        // Get all the incomeEventList where eventDate is less than or equal to
        defaultIncomeEventFiltering("eventDate.lessThanOrEqual=" + DEFAULT_EVENT_DATE, "eventDate.lessThanOrEqual=" + SMALLER_EVENT_DATE);
    }

    @Test
    @Transactional
    void getAllIncomeEventsByEventDateIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.saveAndFlush(incomeEvent);

        // Get all the incomeEventList where eventDate is less than
        defaultIncomeEventFiltering("eventDate.lessThan=" + UPDATED_EVENT_DATE, "eventDate.lessThan=" + DEFAULT_EVENT_DATE);
    }

    @Test
    @Transactional
    void getAllIncomeEventsByEventDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.saveAndFlush(incomeEvent);

        // Get all the incomeEventList where eventDate is greater than
        defaultIncomeEventFiltering("eventDate.greaterThan=" + SMALLER_EVENT_DATE, "eventDate.greaterThan=" + DEFAULT_EVENT_DATE);
    }

    @Test
    @Transactional
    void getAllIncomeEventsByAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.saveAndFlush(incomeEvent);

        // Get all the incomeEventList where amount equals to
        defaultIncomeEventFiltering("amount.equals=" + DEFAULT_AMOUNT, "amount.equals=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllIncomeEventsByAmountIsInShouldWork() throws Exception {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.saveAndFlush(incomeEvent);

        // Get all the incomeEventList where amount in
        defaultIncomeEventFiltering("amount.in=" + DEFAULT_AMOUNT + "," + UPDATED_AMOUNT, "amount.in=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllIncomeEventsByAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.saveAndFlush(incomeEvent);

        // Get all the incomeEventList where amount is not null
        defaultIncomeEventFiltering("amount.specified=true", "amount.specified=false");
    }

    @Test
    @Transactional
    void getAllIncomeEventsByAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.saveAndFlush(incomeEvent);

        // Get all the incomeEventList where amount is greater than or equal to
        defaultIncomeEventFiltering("amount.greaterThanOrEqual=" + DEFAULT_AMOUNT, "amount.greaterThanOrEqual=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllIncomeEventsByAmountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.saveAndFlush(incomeEvent);

        // Get all the incomeEventList where amount is less than or equal to
        defaultIncomeEventFiltering("amount.lessThanOrEqual=" + DEFAULT_AMOUNT, "amount.lessThanOrEqual=" + SMALLER_AMOUNT);
    }

    @Test
    @Transactional
    void getAllIncomeEventsByAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.saveAndFlush(incomeEvent);

        // Get all the incomeEventList where amount is less than
        defaultIncomeEventFiltering("amount.lessThan=" + UPDATED_AMOUNT, "amount.lessThan=" + DEFAULT_AMOUNT);
    }

    @Test
    @Transactional
    void getAllIncomeEventsByAmountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.saveAndFlush(incomeEvent);

        // Get all the incomeEventList where amount is greater than
        defaultIncomeEventFiltering("amount.greaterThan=" + SMALLER_AMOUNT, "amount.greaterThan=" + DEFAULT_AMOUNT);
    }

    @Test
    @Transactional
    void getAllIncomeEventsByCurrencyIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.saveAndFlush(incomeEvent);

        // Get all the incomeEventList where currency equals to
        defaultIncomeEventFiltering("currency.equals=" + DEFAULT_CURRENCY, "currency.equals=" + UPDATED_CURRENCY);
    }

    @Test
    @Transactional
    void getAllIncomeEventsByCurrencyIsInShouldWork() throws Exception {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.saveAndFlush(incomeEvent);

        // Get all the incomeEventList where currency in
        defaultIncomeEventFiltering("currency.in=" + DEFAULT_CURRENCY + "," + UPDATED_CURRENCY, "currency.in=" + UPDATED_CURRENCY);
    }

    @Test
    @Transactional
    void getAllIncomeEventsByCurrencyIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.saveAndFlush(incomeEvent);

        // Get all the incomeEventList where currency is not null
        defaultIncomeEventFiltering("currency.specified=true", "currency.specified=false");
    }

    @Test
    @Transactional
    void getAllIncomeEventsByCurrencyContainsSomething() throws Exception {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.saveAndFlush(incomeEvent);

        // Get all the incomeEventList where currency contains
        defaultIncomeEventFiltering("currency.contains=" + DEFAULT_CURRENCY, "currency.contains=" + UPDATED_CURRENCY);
    }

    @Test
    @Transactional
    void getAllIncomeEventsByCurrencyNotContainsSomething() throws Exception {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.saveAndFlush(incomeEvent);

        // Get all the incomeEventList where currency does not contain
        defaultIncomeEventFiltering("currency.doesNotContain=" + UPDATED_CURRENCY, "currency.doesNotContain=" + DEFAULT_CURRENCY);
    }

    @Test
    @Transactional
    void getAllIncomeEventsByAccountIsEqualToSomething() throws Exception {
        BrokerAccount account;
        if (TestUtil.findAll(em, BrokerAccount.class).isEmpty()) {
            incomeEventRepository.saveAndFlush(incomeEvent);
            account = BrokerAccountResourceIT.createEntity(em);
        } else {
            account = TestUtil.findAll(em, BrokerAccount.class).get(0);
        }
        em.persist(account);
        em.flush();
        incomeEvent.setAccount(account);
        incomeEventRepository.saveAndFlush(incomeEvent);
        UUID accountId = account.getId();
        // Get all the incomeEventList where account equals to accountId
        defaultIncomeEventShouldBeFound("accountId.equals=" + accountId);

        // Get all the incomeEventList where account equals to UUID.randomUUID()
        defaultIncomeEventShouldNotBeFound("accountId.equals=" + UUID.randomUUID());
    }

    @Test
    @Transactional
    void getAllIncomeEventsByAssetIsEqualToSomething() throws Exception {
        Asset asset;
        if (TestUtil.findAll(em, Asset.class).isEmpty()) {
            incomeEventRepository.saveAndFlush(incomeEvent);
            asset = AssetResourceIT.createEntity();
        } else {
            asset = TestUtil.findAll(em, Asset.class).get(0);
        }
        em.persist(asset);
        em.flush();
        incomeEvent.setAsset(asset);
        incomeEventRepository.saveAndFlush(incomeEvent);
        UUID assetId = asset.getId();
        // Get all the incomeEventList where asset equals to assetId
        defaultIncomeEventShouldBeFound("assetId.equals=" + assetId);

        // Get all the incomeEventList where asset equals to UUID.randomUUID()
        defaultIncomeEventShouldNotBeFound("assetId.equals=" + UUID.randomUUID());
    }

    private void defaultIncomeEventFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultIncomeEventShouldBeFound(shouldBeFound);
        defaultIncomeEventShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultIncomeEventShouldBeFound(String filter) throws Exception {
        restIncomeEventMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(incomeEvent.getId().toString())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].eventDate").value(hasItem(DEFAULT_EVENT_DATE.toString())))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(sameNumber(DEFAULT_AMOUNT))))
            .andExpect(jsonPath("$.[*].currency").value(hasItem(DEFAULT_CURRENCY)));

        // Check, that the count call also returns 1
        restIncomeEventMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultIncomeEventShouldNotBeFound(String filter) throws Exception {
        restIncomeEventMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restIncomeEventMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingIncomeEvent() throws Exception {
        // Get the incomeEvent
        restIncomeEventMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingIncomeEvent() throws Exception {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.saveAndFlush(incomeEvent);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the incomeEvent
        IncomeEvent updatedIncomeEvent = incomeEventRepository.findById(incomeEvent.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedIncomeEvent are not directly saved in db
        em.detach(updatedIncomeEvent);
        updatedIncomeEvent.type(UPDATED_TYPE).eventDate(UPDATED_EVENT_DATE).amount(UPDATED_AMOUNT).currency(UPDATED_CURRENCY);
        IncomeEventDTO incomeEventDTO = incomeEventMapper.toDto(updatedIncomeEvent);

        restIncomeEventMockMvc
            .perform(
                put(ENTITY_API_URL_ID, incomeEventDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(incomeEventDTO))
            )
            .andExpect(status().isOk());

        // Validate the IncomeEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedIncomeEventToMatchAllProperties(updatedIncomeEvent);
    }

    @Test
    @Transactional
    void putNonExistingIncomeEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        incomeEvent.setId(UUID.randomUUID());

        // Create the IncomeEvent
        IncomeEventDTO incomeEventDTO = incomeEventMapper.toDto(incomeEvent);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restIncomeEventMockMvc
            .perform(
                put(ENTITY_API_URL_ID, incomeEventDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(incomeEventDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the IncomeEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchIncomeEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        incomeEvent.setId(UUID.randomUUID());

        // Create the IncomeEvent
        IncomeEventDTO incomeEventDTO = incomeEventMapper.toDto(incomeEvent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restIncomeEventMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(incomeEventDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the IncomeEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamIncomeEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        incomeEvent.setId(UUID.randomUUID());

        // Create the IncomeEvent
        IncomeEventDTO incomeEventDTO = incomeEventMapper.toDto(incomeEvent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restIncomeEventMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(incomeEventDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the IncomeEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateIncomeEventWithPatch() throws Exception {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.saveAndFlush(incomeEvent);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the incomeEvent using partial update
        IncomeEvent partialUpdatedIncomeEvent = new IncomeEvent();
        partialUpdatedIncomeEvent.setId(incomeEvent.getId());

        restIncomeEventMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedIncomeEvent.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedIncomeEvent))
            )
            .andExpect(status().isOk());

        // Validate the IncomeEvent in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertIncomeEventUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedIncomeEvent, incomeEvent),
            getPersistedIncomeEvent(incomeEvent)
        );
    }

    @Test
    @Transactional
    void fullUpdateIncomeEventWithPatch() throws Exception {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.saveAndFlush(incomeEvent);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the incomeEvent using partial update
        IncomeEvent partialUpdatedIncomeEvent = new IncomeEvent();
        partialUpdatedIncomeEvent.setId(incomeEvent.getId());

        partialUpdatedIncomeEvent.type(UPDATED_TYPE).eventDate(UPDATED_EVENT_DATE).amount(UPDATED_AMOUNT).currency(UPDATED_CURRENCY);

        restIncomeEventMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedIncomeEvent.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedIncomeEvent))
            )
            .andExpect(status().isOk());

        // Validate the IncomeEvent in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertIncomeEventUpdatableFieldsEquals(partialUpdatedIncomeEvent, getPersistedIncomeEvent(partialUpdatedIncomeEvent));
    }

    @Test
    @Transactional
    void patchNonExistingIncomeEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        incomeEvent.setId(UUID.randomUUID());

        // Create the IncomeEvent
        IncomeEventDTO incomeEventDTO = incomeEventMapper.toDto(incomeEvent);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restIncomeEventMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, incomeEventDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(incomeEventDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the IncomeEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchIncomeEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        incomeEvent.setId(UUID.randomUUID());

        // Create the IncomeEvent
        IncomeEventDTO incomeEventDTO = incomeEventMapper.toDto(incomeEvent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restIncomeEventMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(incomeEventDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the IncomeEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamIncomeEvent() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        incomeEvent.setId(UUID.randomUUID());

        // Create the IncomeEvent
        IncomeEventDTO incomeEventDTO = incomeEventMapper.toDto(incomeEvent);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restIncomeEventMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(incomeEventDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the IncomeEvent in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteIncomeEvent() throws Exception {
        // Initialize the database
        insertedIncomeEvent = incomeEventRepository.saveAndFlush(incomeEvent);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the incomeEvent
        restIncomeEventMockMvc
            .perform(delete(ENTITY_API_URL_ID, incomeEvent.getId().toString()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return incomeEventRepository.count();
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
        return incomeEventRepository.findById(incomeEvent.getId()).orElseThrow();
    }

    protected void assertPersistedIncomeEventToMatchAllProperties(IncomeEvent expectedIncomeEvent) {
        assertIncomeEventAllPropertiesEquals(expectedIncomeEvent, getPersistedIncomeEvent(expectedIncomeEvent));
    }

    protected void assertPersistedIncomeEventToMatchUpdatableProperties(IncomeEvent expectedIncomeEvent) {
        assertIncomeEventAllUpdatablePropertiesEquals(expectedIncomeEvent, getPersistedIncomeEvent(expectedIncomeEvent));
    }
}
