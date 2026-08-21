package com.assetcompass.gateway.web.rest;

import static com.assetcompass.gateway.domain.RebateAsserts.*;
import static com.assetcompass.gateway.web.rest.TestUtil.createUpdateProxyForBean;
import static com.assetcompass.gateway.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.assetcompass.gateway.IntegrationTest;
import com.assetcompass.gateway.domain.BrokerAccount;
import com.assetcompass.gateway.domain.Operation;
import com.assetcompass.gateway.domain.Rebate;
import com.assetcompass.gateway.repository.BrokerAccountRepository;
import com.assetcompass.gateway.repository.EntityManager;
import com.assetcompass.gateway.repository.OperationRepository;
import com.assetcompass.gateway.repository.RebateRepository;
import com.assetcompass.gateway.service.dto.RebateDTO;
import com.assetcompass.gateway.service.mapper.RebateMapper;
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
 * Integration tests for the {@link RebateResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class RebateResourceIT {

    private static final LocalDate DEFAULT_REBATE_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_REBATE_DATE = LocalDate.parse("2023-12-22");
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
    private WebTestClient webTestClient;

    private Rebate rebate;

    private Rebate insertedRebate;

    @Autowired
    private BrokerAccountRepository brokerAccountRepository;

    @Autowired
    private OperationRepository operationRepository;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Rebate createEntity(EntityManager em) {
        Rebate rebate = new Rebate()
            .id(UUID.randomUUID())
            .rebateDate(DEFAULT_REBATE_DATE)
            .amount(DEFAULT_AMOUNT)
            .currency(DEFAULT_CURRENCY);
        // Add required entity
        BrokerAccount brokerAccount;
        brokerAccount = em.insert(BrokerAccountResourceIT.createEntity(em)).block();
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
        Rebate updatedRebate = new Rebate()
            .id(UUID.randomUUID())
            .rebateDate(UPDATED_REBATE_DATE)
            .amount(UPDATED_AMOUNT)
            .currency(UPDATED_CURRENCY);
        // Add required entity
        BrokerAccount brokerAccount;
        brokerAccount = em.insert(BrokerAccountResourceIT.createUpdatedEntity(em)).block();
        updatedRebate.setAccount(brokerAccount);
        return updatedRebate;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Rebate.class).block();
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
        rebate = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedRebate != null) {
            rebateRepository.delete(insertedRebate).block();
            insertedRebate = null;
        }
        deleteEntities(em);
    }

    @Test
    void createRebate() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        rebate.setId(null);
        // Create the Rebate
        RebateDTO rebateDTO = rebateMapper.toDto(rebate);
        var returnedRebateDTO = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(rebateDTO))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(RebateDTO.class)
            .returnResult()
            .getResponseBody();

        // Validate the Rebate in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedRebate = rebateMapper.toEntity(returnedRebateDTO);
        assertRebateUpdatableFieldsEquals(returnedRebate, getPersistedRebate(returnedRebate));

        insertedRebate = returnedRebate;
    }

    @Test
    void createRebateWithExistingId() throws Exception {
        // Create the Rebate with an existing ID
        insertedRebate = rebateRepository.save(rebate).block();
        RebateDTO rebateDTO = rebateMapper.toDto(rebate);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(rebateDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Rebate in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkRebateDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        rebate.setRebateDate(null);

        // Create the Rebate, which fails.
        RebateDTO rebateDTO = rebateMapper.toDto(rebate);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(rebateDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkAmountIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        rebate.setAmount(null);

        // Create the Rebate, which fails.
        RebateDTO rebateDTO = rebateMapper.toDto(rebate);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(rebateDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkCurrencyIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        rebate.setCurrency(null);

        // Create the Rebate, which fails.
        RebateDTO rebateDTO = rebateMapper.toDto(rebate);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(rebateDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllRebates() {
        // Initialize the database
        rebate.setId(UUID.randomUUID());
        insertedRebate = rebateRepository.save(rebate).block();

        // Get all the rebateList
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
            .value(hasItem(rebate.getId().toString()))
            .jsonPath("$.[*].rebateDate")
            .value(hasItem(DEFAULT_REBATE_DATE.toString()))
            .jsonPath("$.[*].amount")
            .value(hasItem(sameNumber(DEFAULT_AMOUNT)))
            .jsonPath("$.[*].currency")
            .value(hasItem(DEFAULT_CURRENCY));
    }

    @Test
    void getRebate() {
        // Initialize the database
        rebate.setId(UUID.randomUUID());
        insertedRebate = rebateRepository.save(rebate).block();

        // Get the rebate
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, rebate.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(rebate.getId().toString()))
            .jsonPath("$.rebateDate")
            .value(is(DEFAULT_REBATE_DATE.toString()))
            .jsonPath("$.amount")
            .value(is(sameNumber(DEFAULT_AMOUNT)))
            .jsonPath("$.currency")
            .value(is(DEFAULT_CURRENCY));
    }

    @Test
    void getRebatesByIdFiltering() {
        // Initialize the database
        insertedRebate = rebateRepository.save(rebate).block();

        UUID id = rebate.getId();

        defaultRebateFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    void getAllRebatesByRebateDateIsEqualToSomething() {
        // Initialize the database
        insertedRebate = rebateRepository.save(rebate).block();

        // Get all the rebateList where rebateDate equals to
        defaultRebateFiltering("rebateDate.equals=" + DEFAULT_REBATE_DATE, "rebateDate.equals=" + UPDATED_REBATE_DATE);
    }

    @Test
    void getAllRebatesByRebateDateIsInShouldWork() {
        // Initialize the database
        insertedRebate = rebateRepository.save(rebate).block();

        // Get all the rebateList where rebateDate in
        defaultRebateFiltering("rebateDate.in=" + DEFAULT_REBATE_DATE + "," + UPDATED_REBATE_DATE, "rebateDate.in=" + UPDATED_REBATE_DATE);
    }

    @Test
    void getAllRebatesByRebateDateIsNullOrNotNull() {
        // Initialize the database
        insertedRebate = rebateRepository.save(rebate).block();

        // Get all the rebateList where rebateDate is not null
        defaultRebateFiltering("rebateDate.specified=true", "rebateDate.specified=false");
    }

    @Test
    void getAllRebatesByRebateDateIsGreaterThanOrEqualToSomething() {
        // Initialize the database
        insertedRebate = rebateRepository.save(rebate).block();

        // Get all the rebateList where rebateDate is greater than or equal to
        defaultRebateFiltering(
            "rebateDate.greaterThanOrEqual=" + DEFAULT_REBATE_DATE,
            "rebateDate.greaterThanOrEqual=" + UPDATED_REBATE_DATE
        );
    }

    @Test
    void getAllRebatesByRebateDateIsLessThanOrEqualToSomething() {
        // Initialize the database
        insertedRebate = rebateRepository.save(rebate).block();

        // Get all the rebateList where rebateDate is less than or equal to
        defaultRebateFiltering("rebateDate.lessThanOrEqual=" + DEFAULT_REBATE_DATE, "rebateDate.lessThanOrEqual=" + SMALLER_REBATE_DATE);
    }

    @Test
    void getAllRebatesByRebateDateIsLessThanSomething() {
        // Initialize the database
        insertedRebate = rebateRepository.save(rebate).block();

        // Get all the rebateList where rebateDate is less than
        defaultRebateFiltering("rebateDate.lessThan=" + UPDATED_REBATE_DATE, "rebateDate.lessThan=" + DEFAULT_REBATE_DATE);
    }

    @Test
    void getAllRebatesByRebateDateIsGreaterThanSomething() {
        // Initialize the database
        insertedRebate = rebateRepository.save(rebate).block();

        // Get all the rebateList where rebateDate is greater than
        defaultRebateFiltering("rebateDate.greaterThan=" + SMALLER_REBATE_DATE, "rebateDate.greaterThan=" + DEFAULT_REBATE_DATE);
    }

    @Test
    void getAllRebatesByAmountIsEqualToSomething() {
        // Initialize the database
        insertedRebate = rebateRepository.save(rebate).block();

        // Get all the rebateList where amount equals to
        defaultRebateFiltering("amount.equals=" + DEFAULT_AMOUNT, "amount.equals=" + UPDATED_AMOUNT);
    }

    @Test
    void getAllRebatesByAmountIsInShouldWork() {
        // Initialize the database
        insertedRebate = rebateRepository.save(rebate).block();

        // Get all the rebateList where amount in
        defaultRebateFiltering("amount.in=" + DEFAULT_AMOUNT + "," + UPDATED_AMOUNT, "amount.in=" + UPDATED_AMOUNT);
    }

    @Test
    void getAllRebatesByAmountIsNullOrNotNull() {
        // Initialize the database
        insertedRebate = rebateRepository.save(rebate).block();

        // Get all the rebateList where amount is not null
        defaultRebateFiltering("amount.specified=true", "amount.specified=false");
    }

    @Test
    void getAllRebatesByAmountIsGreaterThanOrEqualToSomething() {
        // Initialize the database
        insertedRebate = rebateRepository.save(rebate).block();

        // Get all the rebateList where amount is greater than or equal to
        defaultRebateFiltering("amount.greaterThanOrEqual=" + DEFAULT_AMOUNT, "amount.greaterThanOrEqual=" + UPDATED_AMOUNT);
    }

    @Test
    void getAllRebatesByAmountIsLessThanOrEqualToSomething() {
        // Initialize the database
        insertedRebate = rebateRepository.save(rebate).block();

        // Get all the rebateList where amount is less than or equal to
        defaultRebateFiltering("amount.lessThanOrEqual=" + DEFAULT_AMOUNT, "amount.lessThanOrEqual=" + SMALLER_AMOUNT);
    }

    @Test
    void getAllRebatesByAmountIsLessThanSomething() {
        // Initialize the database
        insertedRebate = rebateRepository.save(rebate).block();

        // Get all the rebateList where amount is less than
        defaultRebateFiltering("amount.lessThan=" + UPDATED_AMOUNT, "amount.lessThan=" + DEFAULT_AMOUNT);
    }

    @Test
    void getAllRebatesByAmountIsGreaterThanSomething() {
        // Initialize the database
        insertedRebate = rebateRepository.save(rebate).block();

        // Get all the rebateList where amount is greater than
        defaultRebateFiltering("amount.greaterThan=" + SMALLER_AMOUNT, "amount.greaterThan=" + DEFAULT_AMOUNT);
    }

    @Test
    void getAllRebatesByCurrencyIsEqualToSomething() {
        // Initialize the database
        insertedRebate = rebateRepository.save(rebate).block();

        // Get all the rebateList where currency equals to
        defaultRebateFiltering("currency.equals=" + DEFAULT_CURRENCY, "currency.equals=" + UPDATED_CURRENCY);
    }

    @Test
    void getAllRebatesByCurrencyIsInShouldWork() {
        // Initialize the database
        insertedRebate = rebateRepository.save(rebate).block();

        // Get all the rebateList where currency in
        defaultRebateFiltering("currency.in=" + DEFAULT_CURRENCY + "," + UPDATED_CURRENCY, "currency.in=" + UPDATED_CURRENCY);
    }

    @Test
    void getAllRebatesByCurrencyIsNullOrNotNull() {
        // Initialize the database
        insertedRebate = rebateRepository.save(rebate).block();

        // Get all the rebateList where currency is not null
        defaultRebateFiltering("currency.specified=true", "currency.specified=false");
    }

    @Test
    void getAllRebatesByCurrencyContainsSomething() {
        // Initialize the database
        insertedRebate = rebateRepository.save(rebate).block();

        // Get all the rebateList where currency contains
        defaultRebateFiltering("currency.contains=" + DEFAULT_CURRENCY, "currency.contains=" + UPDATED_CURRENCY);
    }

    @Test
    void getAllRebatesByCurrencyNotContainsSomething() {
        // Initialize the database
        insertedRebate = rebateRepository.save(rebate).block();

        // Get all the rebateList where currency does not contain
        defaultRebateFiltering("currency.doesNotContain=" + UPDATED_CURRENCY, "currency.doesNotContain=" + DEFAULT_CURRENCY);
    }

    @Test
    void getAllRebatesByAccountIsEqualToSomething() {
        BrokerAccount account = BrokerAccountResourceIT.createEntity(em);
        brokerAccountRepository.save(account).block();
        UUID accountId = account.getId();
        rebate.setAccountId(accountId);
        insertedRebate = rebateRepository.save(rebate).block();
        // Get all the rebateList where account equals to accountId
        defaultRebateShouldBeFound("accountId.equals=" + accountId);

        // Get all the rebateList where account equals to UUID.randomUUID()
        defaultRebateShouldNotBeFound("accountId.equals=" + UUID.randomUUID());
    }

    @Test
    void getAllRebatesByOperationIsEqualToSomething() {
        Operation operation = OperationResourceIT.createEntity(em);
        operationRepository.save(operation).block();
        UUID operationId = operation.getId();
        rebate.setOperationId(operationId);
        insertedRebate = rebateRepository.save(rebate).block();
        // Get all the rebateList where operation equals to operationId
        defaultRebateShouldBeFound("operationId.equals=" + operationId);

        // Get all the rebateList where operation equals to UUID.randomUUID()
        defaultRebateShouldNotBeFound("operationId.equals=" + UUID.randomUUID());
    }

    private void defaultRebateFiltering(String shouldBeFound, String shouldNotBeFound) {
        defaultRebateShouldBeFound(shouldBeFound);
        defaultRebateShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultRebateShouldBeFound(String filter) {
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
            .value(hasItem(rebate.getId().toString()))
            .jsonPath("$.[*].rebateDate")
            .value(hasItem(DEFAULT_REBATE_DATE.toString()))

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
    private void defaultRebateShouldNotBeFound(String filter) {
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
    void getNonExistingRebate() {
        // Get the rebate
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingRebate() throws Exception {
        // Initialize the database
        rebate.setId(UUID.randomUUID());
        insertedRebate = rebateRepository.save(rebate).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the rebate
        Rebate updatedRebate = rebateRepository.findById(rebate.getId()).block();
        updatedRebate.rebateDate(UPDATED_REBATE_DATE).amount(UPDATED_AMOUNT).currency(UPDATED_CURRENCY);
        RebateDTO rebateDTO = rebateMapper.toDto(updatedRebate);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, rebateDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(rebateDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Rebate in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedRebateToMatchAllProperties(updatedRebate);
    }

    @Test
    void putNonExistingRebate() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        rebate.setId(UUID.randomUUID());

        // Create the Rebate
        RebateDTO rebateDTO = rebateMapper.toDto(rebate);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, rebateDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(rebateDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Rebate in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchRebate() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        rebate.setId(UUID.randomUUID());

        // Create the Rebate
        RebateDTO rebateDTO = rebateMapper.toDto(rebate);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(rebateDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Rebate in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamRebate() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        rebate.setId(UUID.randomUUID());

        // Create the Rebate
        RebateDTO rebateDTO = rebateMapper.toDto(rebate);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(rebateDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Rebate in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateRebateWithPatch() throws Exception {
        // Initialize the database
        rebate.setId(UUID.randomUUID());
        insertedRebate = rebateRepository.save(rebate).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the rebate using partial update
        Rebate partialUpdatedRebate = new Rebate();
        partialUpdatedRebate.setId(rebate.getId());

        partialUpdatedRebate.currency(UPDATED_CURRENCY);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedRebate.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedRebate))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Rebate in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertRebateUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedRebate, rebate), getPersistedRebate(rebate));
    }

    @Test
    void fullUpdateRebateWithPatch() throws Exception {
        // Initialize the database
        rebate.setId(UUID.randomUUID());
        insertedRebate = rebateRepository.save(rebate).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the rebate using partial update
        Rebate partialUpdatedRebate = new Rebate();
        partialUpdatedRebate.setId(rebate.getId());

        partialUpdatedRebate.rebateDate(UPDATED_REBATE_DATE).amount(UPDATED_AMOUNT).currency(UPDATED_CURRENCY);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedRebate.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedRebate))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Rebate in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertRebateUpdatableFieldsEquals(partialUpdatedRebate, getPersistedRebate(partialUpdatedRebate));
    }

    @Test
    void patchNonExistingRebate() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        rebate.setId(UUID.randomUUID());

        // Create the Rebate
        RebateDTO rebateDTO = rebateMapper.toDto(rebate);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, rebateDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(rebateDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Rebate in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchRebate() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        rebate.setId(UUID.randomUUID());

        // Create the Rebate
        RebateDTO rebateDTO = rebateMapper.toDto(rebate);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(rebateDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Rebate in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamRebate() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        rebate.setId(UUID.randomUUID());

        // Create the Rebate
        RebateDTO rebateDTO = rebateMapper.toDto(rebate);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(rebateDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Rebate in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteRebate() {
        // Initialize the database
        rebate.setId(UUID.randomUUID());
        insertedRebate = rebateRepository.save(rebate).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the rebate
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, rebate.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return rebateRepository.count().block();
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
        return rebateRepository.findById(rebate.getId()).block();
    }

    protected void assertPersistedRebateToMatchAllProperties(Rebate expectedRebate) {
        // Test fails because reactive api returns an empty object instead of null
        // assertRebateAllPropertiesEquals(expectedRebate, getPersistedRebate(expectedRebate));
        assertRebateUpdatableFieldsEquals(expectedRebate, getPersistedRebate(expectedRebate));
    }

    protected void assertPersistedRebateToMatchUpdatableProperties(Rebate expectedRebate) {
        // Test fails because reactive api returns an empty object instead of null
        // assertRebateAllUpdatablePropertiesEquals(expectedRebate, getPersistedRebate(expectedRebate));
        assertRebateUpdatableFieldsEquals(expectedRebate, getPersistedRebate(expectedRebate));
    }
}
