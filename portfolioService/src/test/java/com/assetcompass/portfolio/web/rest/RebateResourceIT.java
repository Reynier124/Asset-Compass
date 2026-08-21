package com.assetcompass.portfolio.web.rest;

import static com.assetcompass.portfolio.domain.RebateAsserts.*;
import static com.assetcompass.portfolio.web.rest.TestUtil.createUpdateProxyForBean;
import static com.assetcompass.portfolio.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.assetcompass.portfolio.IntegrationTest;
import com.assetcompass.portfolio.domain.BrokerAccount;
import com.assetcompass.portfolio.domain.Operation;
import com.assetcompass.portfolio.domain.Rebate;
import com.assetcompass.portfolio.repository.RebateRepository;
import com.assetcompass.portfolio.service.dto.RebateDTO;
import com.assetcompass.portfolio.service.mapper.RebateMapper;
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
 * Integration tests for the {@link RebateResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class RebateResourceIT {

    private static final LocalDate DEFAULT_REBATE_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_REBATE_DATE = LocalDate.parse("2023-12-02");
    private static final LocalDate SMALLER_REBATE_DATE = LocalDate.ofEpochDay(-1L);

    private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal(1);
    private static final BigDecimal UPDATED_AMOUNT = new BigDecimal(2);
    private static final BigDecimal SMALLER_AMOUNT = new BigDecimal(1 - 1);

    private static final String DEFAULT_CURRENCY = "AAAAAAAAAA";
    private static final String UPDATED_CURRENCY = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/rebates";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private RebateRepository rebateRepository;

    @Autowired
    private RebateMapper rebateMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restRebateMockMvc;

    private Rebate rebate;

    private Rebate insertedRebate;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Rebate createEntity(EntityManager em) {
        Rebate rebate = new Rebate().rebateDate(DEFAULT_REBATE_DATE).amount(DEFAULT_AMOUNT).currency(DEFAULT_CURRENCY);
        // Add required entity
        BrokerAccount brokerAccount;
        if (TestUtil.findAll(em, BrokerAccount.class).isEmpty()) {
            brokerAccount = BrokerAccountResourceIT.createEntity(em);
            em.persist(brokerAccount);
            em.flush();
        } else {
            brokerAccount = TestUtil.findAll(em, BrokerAccount.class).get(0);
        }
        rebate.setAccount(brokerAccount);
        return rebate;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Rebate createUpdatedEntity(EntityManager em) {
        Rebate updatedRebate = new Rebate().rebateDate(UPDATED_REBATE_DATE).amount(UPDATED_AMOUNT).currency(UPDATED_CURRENCY);
        // Add required entity
        BrokerAccount brokerAccount;
        if (TestUtil.findAll(em, BrokerAccount.class).isEmpty()) {
            brokerAccount = BrokerAccountResourceIT.createUpdatedEntity(em);
            em.persist(brokerAccount);
            em.flush();
        } else {
            brokerAccount = TestUtil.findAll(em, BrokerAccount.class).get(0);
        }
        updatedRebate.setAccount(brokerAccount);
        return updatedRebate;
    }

    @BeforeEach
    void initTest() {
        rebate = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedRebate != null) {
            rebateRepository.delete(insertedRebate);
            insertedRebate = null;
        }
    }

    @Test
    @Transactional
    void createRebate() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Rebate
        RebateDTO rebateDTO = rebateMapper.toDto(rebate);
        var returnedRebateDTO = om.readValue(
            restRebateMockMvc
                .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(rebateDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            RebateDTO.class
        );

        // Validate the Rebate in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedRebate = rebateMapper.toEntity(returnedRebateDTO);
        assertRebateUpdatableFieldsEquals(returnedRebate, getPersistedRebate(returnedRebate));

        insertedRebate = returnedRebate;
    }

    @Test
    @Transactional
    void createRebateWithExistingId() throws Exception {
        // Create the Rebate with an existing ID
        insertedRebate = rebateRepository.saveAndFlush(rebate);
        RebateDTO rebateDTO = rebateMapper.toDto(rebate);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restRebateMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(rebateDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Rebate in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkRebateDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        rebate.setRebateDate(null);

        // Create the Rebate, which fails.
        RebateDTO rebateDTO = rebateMapper.toDto(rebate);

        restRebateMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(rebateDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAmountIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        rebate.setAmount(null);

        // Create the Rebate, which fails.
        RebateDTO rebateDTO = rebateMapper.toDto(rebate);

        restRebateMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(rebateDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCurrencyIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        rebate.setCurrency(null);

        // Create the Rebate, which fails.
        RebateDTO rebateDTO = rebateMapper.toDto(rebate);

        restRebateMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(rebateDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllRebates() throws Exception {
        // Initialize the database
        insertedRebate = rebateRepository.saveAndFlush(rebate);

        // Get all the rebateList
        restRebateMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(rebate.getId().toString())))
            .andExpect(jsonPath("$.[*].rebateDate").value(hasItem(DEFAULT_REBATE_DATE.toString())))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(sameNumber(DEFAULT_AMOUNT))))
            .andExpect(jsonPath("$.[*].currency").value(hasItem(DEFAULT_CURRENCY)));
    }

    @Test
    @Transactional
    void getRebate() throws Exception {
        // Initialize the database
        insertedRebate = rebateRepository.saveAndFlush(rebate);

        // Get the rebate
        restRebateMockMvc
            .perform(get(ENTITY_API_URL_ID, rebate.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(rebate.getId().toString()))
            .andExpect(jsonPath("$.rebateDate").value(DEFAULT_REBATE_DATE.toString()))
            .andExpect(jsonPath("$.amount").value(sameNumber(DEFAULT_AMOUNT)))
            .andExpect(jsonPath("$.currency").value(DEFAULT_CURRENCY));
    }

    @Test
    @Transactional
    void getRebatesByIdFiltering() throws Exception {
        // Initialize the database
        insertedRebate = rebateRepository.saveAndFlush(rebate);

        UUID id = rebate.getId();

        defaultRebateFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    @Transactional
    void getAllRebatesByRebateDateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedRebate = rebateRepository.saveAndFlush(rebate);

        // Get all the rebateList where rebateDate equals to
        defaultRebateFiltering("rebateDate.equals=" + DEFAULT_REBATE_DATE, "rebateDate.equals=" + UPDATED_REBATE_DATE);
    }

    @Test
    @Transactional
    void getAllRebatesByRebateDateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedRebate = rebateRepository.saveAndFlush(rebate);

        // Get all the rebateList where rebateDate in
        defaultRebateFiltering("rebateDate.in=" + DEFAULT_REBATE_DATE + "," + UPDATED_REBATE_DATE, "rebateDate.in=" + UPDATED_REBATE_DATE);
    }

    @Test
    @Transactional
    void getAllRebatesByRebateDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedRebate = rebateRepository.saveAndFlush(rebate);

        // Get all the rebateList where rebateDate is not null
        defaultRebateFiltering("rebateDate.specified=true", "rebateDate.specified=false");
    }

    @Test
    @Transactional
    void getAllRebatesByRebateDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedRebate = rebateRepository.saveAndFlush(rebate);

        // Get all the rebateList where rebateDate is greater than or equal to
        defaultRebateFiltering(
            "rebateDate.greaterThanOrEqual=" + DEFAULT_REBATE_DATE,
            "rebateDate.greaterThanOrEqual=" + UPDATED_REBATE_DATE
        );
    }

    @Test
    @Transactional
    void getAllRebatesByRebateDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedRebate = rebateRepository.saveAndFlush(rebate);

        // Get all the rebateList where rebateDate is less than or equal to
        defaultRebateFiltering("rebateDate.lessThanOrEqual=" + DEFAULT_REBATE_DATE, "rebateDate.lessThanOrEqual=" + SMALLER_REBATE_DATE);
    }

    @Test
    @Transactional
    void getAllRebatesByRebateDateIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedRebate = rebateRepository.saveAndFlush(rebate);

        // Get all the rebateList where rebateDate is less than
        defaultRebateFiltering("rebateDate.lessThan=" + UPDATED_REBATE_DATE, "rebateDate.lessThan=" + DEFAULT_REBATE_DATE);
    }

    @Test
    @Transactional
    void getAllRebatesByRebateDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedRebate = rebateRepository.saveAndFlush(rebate);

        // Get all the rebateList where rebateDate is greater than
        defaultRebateFiltering("rebateDate.greaterThan=" + SMALLER_REBATE_DATE, "rebateDate.greaterThan=" + DEFAULT_REBATE_DATE);
    }

    @Test
    @Transactional
    void getAllRebatesByAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedRebate = rebateRepository.saveAndFlush(rebate);

        // Get all the rebateList where amount equals to
        defaultRebateFiltering("amount.equals=" + DEFAULT_AMOUNT, "amount.equals=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllRebatesByAmountIsInShouldWork() throws Exception {
        // Initialize the database
        insertedRebate = rebateRepository.saveAndFlush(rebate);

        // Get all the rebateList where amount in
        defaultRebateFiltering("amount.in=" + DEFAULT_AMOUNT + "," + UPDATED_AMOUNT, "amount.in=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllRebatesByAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedRebate = rebateRepository.saveAndFlush(rebate);

        // Get all the rebateList where amount is not null
        defaultRebateFiltering("amount.specified=true", "amount.specified=false");
    }

    @Test
    @Transactional
    void getAllRebatesByAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedRebate = rebateRepository.saveAndFlush(rebate);

        // Get all the rebateList where amount is greater than or equal to
        defaultRebateFiltering("amount.greaterThanOrEqual=" + DEFAULT_AMOUNT, "amount.greaterThanOrEqual=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllRebatesByAmountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedRebate = rebateRepository.saveAndFlush(rebate);

        // Get all the rebateList where amount is less than or equal to
        defaultRebateFiltering("amount.lessThanOrEqual=" + DEFAULT_AMOUNT, "amount.lessThanOrEqual=" + SMALLER_AMOUNT);
    }

    @Test
    @Transactional
    void getAllRebatesByAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedRebate = rebateRepository.saveAndFlush(rebate);

        // Get all the rebateList where amount is less than
        defaultRebateFiltering("amount.lessThan=" + UPDATED_AMOUNT, "amount.lessThan=" + DEFAULT_AMOUNT);
    }

    @Test
    @Transactional
    void getAllRebatesByAmountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedRebate = rebateRepository.saveAndFlush(rebate);

        // Get all the rebateList where amount is greater than
        defaultRebateFiltering("amount.greaterThan=" + SMALLER_AMOUNT, "amount.greaterThan=" + DEFAULT_AMOUNT);
    }

    @Test
    @Transactional
    void getAllRebatesByCurrencyIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedRebate = rebateRepository.saveAndFlush(rebate);

        // Get all the rebateList where currency equals to
        defaultRebateFiltering("currency.equals=" + DEFAULT_CURRENCY, "currency.equals=" + UPDATED_CURRENCY);
    }

    @Test
    @Transactional
    void getAllRebatesByCurrencyIsInShouldWork() throws Exception {
        // Initialize the database
        insertedRebate = rebateRepository.saveAndFlush(rebate);

        // Get all the rebateList where currency in
        defaultRebateFiltering("currency.in=" + DEFAULT_CURRENCY + "," + UPDATED_CURRENCY, "currency.in=" + UPDATED_CURRENCY);
    }

    @Test
    @Transactional
    void getAllRebatesByCurrencyIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedRebate = rebateRepository.saveAndFlush(rebate);

        // Get all the rebateList where currency is not null
        defaultRebateFiltering("currency.specified=true", "currency.specified=false");
    }

    @Test
    @Transactional
    void getAllRebatesByCurrencyContainsSomething() throws Exception {
        // Initialize the database
        insertedRebate = rebateRepository.saveAndFlush(rebate);

        // Get all the rebateList where currency contains
        defaultRebateFiltering("currency.contains=" + DEFAULT_CURRENCY, "currency.contains=" + UPDATED_CURRENCY);
    }

    @Test
    @Transactional
    void getAllRebatesByCurrencyNotContainsSomething() throws Exception {
        // Initialize the database
        insertedRebate = rebateRepository.saveAndFlush(rebate);

        // Get all the rebateList where currency does not contain
        defaultRebateFiltering("currency.doesNotContain=" + UPDATED_CURRENCY, "currency.doesNotContain=" + DEFAULT_CURRENCY);
    }

    @Test
    @Transactional
    void getAllRebatesByAccountIsEqualToSomething() throws Exception {
        BrokerAccount account;
        if (TestUtil.findAll(em, BrokerAccount.class).isEmpty()) {
            rebateRepository.saveAndFlush(rebate);
            account = BrokerAccountResourceIT.createEntity(em);
        } else {
            account = TestUtil.findAll(em, BrokerAccount.class).get(0);
        }
        em.persist(account);
        em.flush();
        rebate.setAccount(account);
        rebateRepository.saveAndFlush(rebate);
        UUID accountId = account.getId();
        // Get all the rebateList where account equals to accountId
        defaultRebateShouldBeFound("accountId.equals=" + accountId);

        // Get all the rebateList where account equals to UUID.randomUUID()
        defaultRebateShouldNotBeFound("accountId.equals=" + UUID.randomUUID());
    }

    @Test
    @Transactional
    void getAllRebatesByOperationIsEqualToSomething() throws Exception {
        Operation operation;
        if (TestUtil.findAll(em, Operation.class).isEmpty()) {
            rebateRepository.saveAndFlush(rebate);
            operation = OperationResourceIT.createEntity(em);
        } else {
            operation = TestUtil.findAll(em, Operation.class).get(0);
        }
        em.persist(operation);
        em.flush();
        rebate.setOperation(operation);
        rebateRepository.saveAndFlush(rebate);
        UUID operationId = operation.getId();
        // Get all the rebateList where operation equals to operationId
        defaultRebateShouldBeFound("operationId.equals=" + operationId);

        // Get all the rebateList where operation equals to UUID.randomUUID()
        defaultRebateShouldNotBeFound("operationId.equals=" + UUID.randomUUID());
    }

    private void defaultRebateFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultRebateShouldBeFound(shouldBeFound);
        defaultRebateShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultRebateShouldBeFound(String filter) throws Exception {
        restRebateMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(rebate.getId().toString())))
            .andExpect(jsonPath("$.[*].rebateDate").value(hasItem(DEFAULT_REBATE_DATE.toString())))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(sameNumber(DEFAULT_AMOUNT))))
            .andExpect(jsonPath("$.[*].currency").value(hasItem(DEFAULT_CURRENCY)));

        // Check, that the count call also returns 1
        restRebateMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultRebateShouldNotBeFound(String filter) throws Exception {
        restRebateMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restRebateMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingRebate() throws Exception {
        // Get the rebate
        restRebateMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingRebate() throws Exception {
        // Initialize the database
        insertedRebate = rebateRepository.saveAndFlush(rebate);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the rebate
        Rebate updatedRebate = rebateRepository.findById(rebate.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedRebate are not directly saved in db
        em.detach(updatedRebate);
        updatedRebate.rebateDate(UPDATED_REBATE_DATE).amount(UPDATED_AMOUNT).currency(UPDATED_CURRENCY);
        RebateDTO rebateDTO = rebateMapper.toDto(updatedRebate);

        restRebateMockMvc
            .perform(
                put(ENTITY_API_URL_ID, rebateDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(rebateDTO))
            )
            .andExpect(status().isOk());

        // Validate the Rebate in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedRebateToMatchAllProperties(updatedRebate);
    }

    @Test
    @Transactional
    void putNonExistingRebate() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        rebate.setId(UUID.randomUUID());

        // Create the Rebate
        RebateDTO rebateDTO = rebateMapper.toDto(rebate);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRebateMockMvc
            .perform(
                put(ENTITY_API_URL_ID, rebateDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(rebateDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Rebate in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchRebate() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        rebate.setId(UUID.randomUUID());

        // Create the Rebate
        RebateDTO rebateDTO = rebateMapper.toDto(rebate);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRebateMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(rebateDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Rebate in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamRebate() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        rebate.setId(UUID.randomUUID());

        // Create the Rebate
        RebateDTO rebateDTO = rebateMapper.toDto(rebate);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRebateMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(rebateDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Rebate in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateRebateWithPatch() throws Exception {
        // Initialize the database
        insertedRebate = rebateRepository.saveAndFlush(rebate);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the rebate using partial update
        Rebate partialUpdatedRebate = new Rebate();
        partialUpdatedRebate.setId(rebate.getId());

        partialUpdatedRebate.amount(UPDATED_AMOUNT);

        restRebateMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRebate.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedRebate))
            )
            .andExpect(status().isOk());

        // Validate the Rebate in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertRebateUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedRebate, rebate), getPersistedRebate(rebate));
    }

    @Test
    @Transactional
    void fullUpdateRebateWithPatch() throws Exception {
        // Initialize the database
        insertedRebate = rebateRepository.saveAndFlush(rebate);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the rebate using partial update
        Rebate partialUpdatedRebate = new Rebate();
        partialUpdatedRebate.setId(rebate.getId());

        partialUpdatedRebate.rebateDate(UPDATED_REBATE_DATE).amount(UPDATED_AMOUNT).currency(UPDATED_CURRENCY);

        restRebateMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRebate.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedRebate))
            )
            .andExpect(status().isOk());

        // Validate the Rebate in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertRebateUpdatableFieldsEquals(partialUpdatedRebate, getPersistedRebate(partialUpdatedRebate));
    }

    @Test
    @Transactional
    void patchNonExistingRebate() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        rebate.setId(UUID.randomUUID());

        // Create the Rebate
        RebateDTO rebateDTO = rebateMapper.toDto(rebate);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRebateMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, rebateDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(rebateDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Rebate in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchRebate() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        rebate.setId(UUID.randomUUID());

        // Create the Rebate
        RebateDTO rebateDTO = rebateMapper.toDto(rebate);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRebateMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(rebateDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Rebate in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamRebate() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        rebate.setId(UUID.randomUUID());

        // Create the Rebate
        RebateDTO rebateDTO = rebateMapper.toDto(rebate);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRebateMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(rebateDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Rebate in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteRebate() throws Exception {
        // Initialize the database
        insertedRebate = rebateRepository.saveAndFlush(rebate);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the rebate
        restRebateMockMvc
            .perform(delete(ENTITY_API_URL_ID, rebate.getId().toString()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return rebateRepository.count();
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

    protected Rebate getPersistedRebate(Rebate rebate) {
        return rebateRepository.findById(rebate.getId()).orElseThrow();
    }

    protected void assertPersistedRebateToMatchAllProperties(Rebate expectedRebate) {
        assertRebateAllPropertiesEquals(expectedRebate, getPersistedRebate(expectedRebate));
    }

    protected void assertPersistedRebateToMatchUpdatableProperties(Rebate expectedRebate) {
        assertRebateAllUpdatablePropertiesEquals(expectedRebate, getPersistedRebate(expectedRebate));
    }
}
