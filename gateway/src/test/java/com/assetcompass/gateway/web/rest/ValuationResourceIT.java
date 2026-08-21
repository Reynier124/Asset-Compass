package com.assetcompass.gateway.web.rest;

import static com.assetcompass.gateway.domain.ValuationAsserts.*;
import static com.assetcompass.gateway.web.rest.TestUtil.createUpdateProxyForBean;
import static com.assetcompass.gateway.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.assetcompass.gateway.IntegrationTest;
import com.assetcompass.gateway.domain.BrokerAccount;
import com.assetcompass.gateway.domain.Valuation;
import com.assetcompass.gateway.repository.BrokerAccountRepository;
import com.assetcompass.gateway.repository.EntityManager;
import com.assetcompass.gateway.repository.ValuationRepository;
import com.assetcompass.gateway.service.dto.ValuationDTO;
import com.assetcompass.gateway.service.mapper.ValuationMapper;
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
 * Integration tests for the {@link ValuationResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class ValuationResourceIT {

    private static final LocalDate DEFAULT_SNAPSHOT_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_SNAPSHOT_DATE = LocalDate.parse("2023-12-22");
    private static final LocalDate SMALLER_SNAPSHOT_DATE = LocalDate.ofEpochDay(-1L);

    private static final BigDecimal DEFAULT_TOTAL_VALUE = new BigDecimal(1);
    private static final BigDecimal UPDATED_TOTAL_VALUE = new BigDecimal(2);
    private static final BigDecimal SMALLER_TOTAL_VALUE = new BigDecimal(1 - 1);

    private static final String DEFAULT_CURRENCY = "AAAAAAAAAA";
    private static final String UPDATED_CURRENCY = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/valuations";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ValuationRepository valuationRepository;

    @Autowired
    private ValuationMapper valuationMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Valuation valuation;

    private Valuation insertedValuation;

    @Autowired
    private BrokerAccountRepository brokerAccountRepository;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Valuation createEntity() {
        return new Valuation()
            .id(UUID.randomUUID())
            .snapshotDate(DEFAULT_SNAPSHOT_DATE)
            .totalValue(DEFAULT_TOTAL_VALUE)
            .currency(DEFAULT_CURRENCY);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Valuation createUpdatedEntity() {
        return new Valuation()
            .id(UUID.randomUUID())
            .snapshotDate(UPDATED_SNAPSHOT_DATE)
            .totalValue(UPDATED_TOTAL_VALUE)
            .currency(UPDATED_CURRENCY);
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Valuation.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @BeforeEach
    void setupCsrf() {
        webTestClient = webTestClient.mutateWith(csrf());
    }

    @BeforeEach
    void initTest() {
        valuation = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedValuation != null) {
            valuationRepository.delete(insertedValuation).block();
            insertedValuation = null;
        }
        deleteEntities(em);
    }

    @Test
    void createValuation() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        valuation.setId(null);
        // Create the Valuation
        ValuationDTO valuationDTO = valuationMapper.toDto(valuation);
        var returnedValuationDTO = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(valuationDTO))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(ValuationDTO.class)
            .returnResult()
            .getResponseBody();

        // Validate the Valuation in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedValuation = valuationMapper.toEntity(returnedValuationDTO);
        assertValuationUpdatableFieldsEquals(returnedValuation, getPersistedValuation(returnedValuation));

        insertedValuation = returnedValuation;
    }

    @Test
    void createValuationWithExistingId() throws Exception {
        // Create the Valuation with an existing ID
        insertedValuation = valuationRepository.save(valuation).block();
        ValuationDTO valuationDTO = valuationMapper.toDto(valuation);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(valuationDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Valuation in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkSnapshotDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        valuation.setSnapshotDate(null);

        // Create the Valuation, which fails.
        ValuationDTO valuationDTO = valuationMapper.toDto(valuation);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(valuationDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkTotalValueIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        valuation.setTotalValue(null);

        // Create the Valuation, which fails.
        ValuationDTO valuationDTO = valuationMapper.toDto(valuation);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(valuationDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkCurrencyIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        valuation.setCurrency(null);

        // Create the Valuation, which fails.
        ValuationDTO valuationDTO = valuationMapper.toDto(valuation);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(valuationDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllValuations() {
        // Initialize the database
        valuation.setId(UUID.randomUUID());
        insertedValuation = valuationRepository.save(valuation).block();

        // Get all the valuationList
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
            .value(hasItem(valuation.getId().toString()))
            .jsonPath("$.[*].snapshotDate")
            .value(hasItem(DEFAULT_SNAPSHOT_DATE.toString()))
            .jsonPath("$.[*].totalValue")
            .value(hasItem(sameNumber(DEFAULT_TOTAL_VALUE)))
            .jsonPath("$.[*].currency")
            .value(hasItem(DEFAULT_CURRENCY));
    }

    @Test
    void getValuation() {
        // Initialize the database
        valuation.setId(UUID.randomUUID());
        insertedValuation = valuationRepository.save(valuation).block();

        // Get the valuation
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, valuation.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(valuation.getId().toString()))
            .jsonPath("$.snapshotDate")
            .value(is(DEFAULT_SNAPSHOT_DATE.toString()))
            .jsonPath("$.totalValue")
            .value(is(sameNumber(DEFAULT_TOTAL_VALUE)))
            .jsonPath("$.currency")
            .value(is(DEFAULT_CURRENCY));
    }

    @Test
    void getValuationsByIdFiltering() {
        // Initialize the database
        insertedValuation = valuationRepository.save(valuation).block();

        UUID id = valuation.getId();

        defaultValuationFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    void getAllValuationsBySnapshotDateIsEqualToSomething() {
        // Initialize the database
        insertedValuation = valuationRepository.save(valuation).block();

        // Get all the valuationList where snapshotDate equals to
        defaultValuationFiltering("snapshotDate.equals=" + DEFAULT_SNAPSHOT_DATE, "snapshotDate.equals=" + UPDATED_SNAPSHOT_DATE);
    }

    @Test
    void getAllValuationsBySnapshotDateIsInShouldWork() {
        // Initialize the database
        insertedValuation = valuationRepository.save(valuation).block();

        // Get all the valuationList where snapshotDate in
        defaultValuationFiltering(
            "snapshotDate.in=" + DEFAULT_SNAPSHOT_DATE + "," + UPDATED_SNAPSHOT_DATE,
            "snapshotDate.in=" + UPDATED_SNAPSHOT_DATE
        );
    }

    @Test
    void getAllValuationsBySnapshotDateIsNullOrNotNull() {
        // Initialize the database
        insertedValuation = valuationRepository.save(valuation).block();

        // Get all the valuationList where snapshotDate is not null
        defaultValuationFiltering("snapshotDate.specified=true", "snapshotDate.specified=false");
    }

    @Test
    void getAllValuationsBySnapshotDateIsGreaterThanOrEqualToSomething() {
        // Initialize the database
        insertedValuation = valuationRepository.save(valuation).block();

        // Get all the valuationList where snapshotDate is greater than or equal to
        defaultValuationFiltering(
            "snapshotDate.greaterThanOrEqual=" + DEFAULT_SNAPSHOT_DATE,
            "snapshotDate.greaterThanOrEqual=" + UPDATED_SNAPSHOT_DATE
        );
    }

    @Test
    void getAllValuationsBySnapshotDateIsLessThanOrEqualToSomething() {
        // Initialize the database
        insertedValuation = valuationRepository.save(valuation).block();

        // Get all the valuationList where snapshotDate is less than or equal to
        defaultValuationFiltering(
            "snapshotDate.lessThanOrEqual=" + DEFAULT_SNAPSHOT_DATE,
            "snapshotDate.lessThanOrEqual=" + SMALLER_SNAPSHOT_DATE
        );
    }

    @Test
    void getAllValuationsBySnapshotDateIsLessThanSomething() {
        // Initialize the database
        insertedValuation = valuationRepository.save(valuation).block();

        // Get all the valuationList where snapshotDate is less than
        defaultValuationFiltering("snapshotDate.lessThan=" + UPDATED_SNAPSHOT_DATE, "snapshotDate.lessThan=" + DEFAULT_SNAPSHOT_DATE);
    }

    @Test
    void getAllValuationsBySnapshotDateIsGreaterThanSomething() {
        // Initialize the database
        insertedValuation = valuationRepository.save(valuation).block();

        // Get all the valuationList where snapshotDate is greater than
        defaultValuationFiltering("snapshotDate.greaterThan=" + SMALLER_SNAPSHOT_DATE, "snapshotDate.greaterThan=" + DEFAULT_SNAPSHOT_DATE);
    }

    @Test
    void getAllValuationsByTotalValueIsEqualToSomething() {
        // Initialize the database
        insertedValuation = valuationRepository.save(valuation).block();

        // Get all the valuationList where totalValue equals to
        defaultValuationFiltering("totalValue.equals=" + DEFAULT_TOTAL_VALUE, "totalValue.equals=" + UPDATED_TOTAL_VALUE);
    }

    @Test
    void getAllValuationsByTotalValueIsInShouldWork() {
        // Initialize the database
        insertedValuation = valuationRepository.save(valuation).block();

        // Get all the valuationList where totalValue in
        defaultValuationFiltering(
            "totalValue.in=" + DEFAULT_TOTAL_VALUE + "," + UPDATED_TOTAL_VALUE,
            "totalValue.in=" + UPDATED_TOTAL_VALUE
        );
    }

    @Test
    void getAllValuationsByTotalValueIsNullOrNotNull() {
        // Initialize the database
        insertedValuation = valuationRepository.save(valuation).block();

        // Get all the valuationList where totalValue is not null
        defaultValuationFiltering("totalValue.specified=true", "totalValue.specified=false");
    }

    @Test
    void getAllValuationsByTotalValueIsGreaterThanOrEqualToSomething() {
        // Initialize the database
        insertedValuation = valuationRepository.save(valuation).block();

        // Get all the valuationList where totalValue is greater than or equal to
        defaultValuationFiltering(
            "totalValue.greaterThanOrEqual=" + DEFAULT_TOTAL_VALUE,
            "totalValue.greaterThanOrEqual=" + UPDATED_TOTAL_VALUE
        );
    }

    @Test
    void getAllValuationsByTotalValueIsLessThanOrEqualToSomething() {
        // Initialize the database
        insertedValuation = valuationRepository.save(valuation).block();

        // Get all the valuationList where totalValue is less than or equal to
        defaultValuationFiltering("totalValue.lessThanOrEqual=" + DEFAULT_TOTAL_VALUE, "totalValue.lessThanOrEqual=" + SMALLER_TOTAL_VALUE);
    }

    @Test
    void getAllValuationsByTotalValueIsLessThanSomething() {
        // Initialize the database
        insertedValuation = valuationRepository.save(valuation).block();

        // Get all the valuationList where totalValue is less than
        defaultValuationFiltering("totalValue.lessThan=" + UPDATED_TOTAL_VALUE, "totalValue.lessThan=" + DEFAULT_TOTAL_VALUE);
    }

    @Test
    void getAllValuationsByTotalValueIsGreaterThanSomething() {
        // Initialize the database
        insertedValuation = valuationRepository.save(valuation).block();

        // Get all the valuationList where totalValue is greater than
        defaultValuationFiltering("totalValue.greaterThan=" + SMALLER_TOTAL_VALUE, "totalValue.greaterThan=" + DEFAULT_TOTAL_VALUE);
    }

    @Test
    void getAllValuationsByCurrencyIsEqualToSomething() {
        // Initialize the database
        insertedValuation = valuationRepository.save(valuation).block();

        // Get all the valuationList where currency equals to
        defaultValuationFiltering("currency.equals=" + DEFAULT_CURRENCY, "currency.equals=" + UPDATED_CURRENCY);
    }

    @Test
    void getAllValuationsByCurrencyIsInShouldWork() {
        // Initialize the database
        insertedValuation = valuationRepository.save(valuation).block();

        // Get all the valuationList where currency in
        defaultValuationFiltering("currency.in=" + DEFAULT_CURRENCY + "," + UPDATED_CURRENCY, "currency.in=" + UPDATED_CURRENCY);
    }

    @Test
    void getAllValuationsByCurrencyIsNullOrNotNull() {
        // Initialize the database
        insertedValuation = valuationRepository.save(valuation).block();

        // Get all the valuationList where currency is not null
        defaultValuationFiltering("currency.specified=true", "currency.specified=false");
    }

    @Test
    void getAllValuationsByCurrencyContainsSomething() {
        // Initialize the database
        insertedValuation = valuationRepository.save(valuation).block();

        // Get all the valuationList where currency contains
        defaultValuationFiltering("currency.contains=" + DEFAULT_CURRENCY, "currency.contains=" + UPDATED_CURRENCY);
    }

    @Test
    void getAllValuationsByCurrencyNotContainsSomething() {
        // Initialize the database
        insertedValuation = valuationRepository.save(valuation).block();

        // Get all the valuationList where currency does not contain
        defaultValuationFiltering("currency.doesNotContain=" + UPDATED_CURRENCY, "currency.doesNotContain=" + DEFAULT_CURRENCY);
    }

    @Test
    void getAllValuationsByAccountIsEqualToSomething() {
        BrokerAccount account = BrokerAccountResourceIT.createEntity(em);
        brokerAccountRepository.save(account).block();
        UUID accountId = account.getId();
        valuation.setAccountId(accountId);
        insertedValuation = valuationRepository.save(valuation).block();
        // Get all the valuationList where account equals to accountId
        defaultValuationShouldBeFound("accountId.equals=" + accountId);

        // Get all the valuationList where account equals to UUID.randomUUID()
        defaultValuationShouldNotBeFound("accountId.equals=" + UUID.randomUUID());
    }

    private void defaultValuationFiltering(String shouldBeFound, String shouldNotBeFound) {
        defaultValuationShouldBeFound(shouldBeFound);
        defaultValuationShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultValuationShouldBeFound(String filter) {
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
            .value(hasItem(valuation.getId().toString()))
            .jsonPath("$.[*].snapshotDate")
            .value(hasItem(DEFAULT_SNAPSHOT_DATE.toString()))

            .jsonPath("$.[*].totalValue")
            .value(hasItem(sameNumber(DEFAULT_TOTAL_VALUE)))

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
    private void defaultValuationShouldNotBeFound(String filter) {
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
    void getNonExistingValuation() {
        // Get the valuation
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingValuation() throws Exception {
        // Initialize the database
        valuation.setId(UUID.randomUUID());
        insertedValuation = valuationRepository.save(valuation).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the valuation
        Valuation updatedValuation = valuationRepository.findById(valuation.getId()).block();
        updatedValuation.snapshotDate(UPDATED_SNAPSHOT_DATE).totalValue(UPDATED_TOTAL_VALUE).currency(UPDATED_CURRENCY);
        ValuationDTO valuationDTO = valuationMapper.toDto(updatedValuation);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, valuationDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(valuationDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Valuation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedValuationToMatchAllProperties(updatedValuation);
    }

    @Test
    void putNonExistingValuation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        valuation.setId(UUID.randomUUID());

        // Create the Valuation
        ValuationDTO valuationDTO = valuationMapper.toDto(valuation);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, valuationDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(valuationDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Valuation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchValuation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        valuation.setId(UUID.randomUUID());

        // Create the Valuation
        ValuationDTO valuationDTO = valuationMapper.toDto(valuation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(valuationDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Valuation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamValuation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        valuation.setId(UUID.randomUUID());

        // Create the Valuation
        ValuationDTO valuationDTO = valuationMapper.toDto(valuation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(valuationDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Valuation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateValuationWithPatch() throws Exception {
        // Initialize the database
        valuation.setId(UUID.randomUUID());
        insertedValuation = valuationRepository.save(valuation).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the valuation using partial update
        Valuation partialUpdatedValuation = new Valuation();
        partialUpdatedValuation.setId(valuation.getId());

        partialUpdatedValuation.currency(UPDATED_CURRENCY);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedValuation.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedValuation))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Valuation in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertValuationUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedValuation, valuation),
            getPersistedValuation(valuation)
        );
    }

    @Test
    void fullUpdateValuationWithPatch() throws Exception {
        // Initialize the database
        valuation.setId(UUID.randomUUID());
        insertedValuation = valuationRepository.save(valuation).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the valuation using partial update
        Valuation partialUpdatedValuation = new Valuation();
        partialUpdatedValuation.setId(valuation.getId());

        partialUpdatedValuation.snapshotDate(UPDATED_SNAPSHOT_DATE).totalValue(UPDATED_TOTAL_VALUE).currency(UPDATED_CURRENCY);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedValuation.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedValuation))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Valuation in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertValuationUpdatableFieldsEquals(partialUpdatedValuation, getPersistedValuation(partialUpdatedValuation));
    }

    @Test
    void patchNonExistingValuation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        valuation.setId(UUID.randomUUID());

        // Create the Valuation
        ValuationDTO valuationDTO = valuationMapper.toDto(valuation);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, valuationDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(valuationDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Valuation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchValuation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        valuation.setId(UUID.randomUUID());

        // Create the Valuation
        ValuationDTO valuationDTO = valuationMapper.toDto(valuation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(valuationDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Valuation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamValuation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        valuation.setId(UUID.randomUUID());

        // Create the Valuation
        ValuationDTO valuationDTO = valuationMapper.toDto(valuation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(valuationDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Valuation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteValuation() {
        // Initialize the database
        valuation.setId(UUID.randomUUID());
        insertedValuation = valuationRepository.save(valuation).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the valuation
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, valuation.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return valuationRepository.count().block();
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

    protected Valuation getPersistedValuation(Valuation valuation) {
        return valuationRepository.findById(valuation.getId()).block();
    }

    protected void assertPersistedValuationToMatchAllProperties(Valuation expectedValuation) {
        // Test fails because reactive api returns an empty object instead of null
        // assertValuationAllPropertiesEquals(expectedValuation, getPersistedValuation(expectedValuation));
        assertValuationUpdatableFieldsEquals(expectedValuation, getPersistedValuation(expectedValuation));
    }

    protected void assertPersistedValuationToMatchUpdatableProperties(Valuation expectedValuation) {
        // Test fails because reactive api returns an empty object instead of null
        // assertValuationAllUpdatablePropertiesEquals(expectedValuation, getPersistedValuation(expectedValuation));
        assertValuationUpdatableFieldsEquals(expectedValuation, getPersistedValuation(expectedValuation));
    }
}
