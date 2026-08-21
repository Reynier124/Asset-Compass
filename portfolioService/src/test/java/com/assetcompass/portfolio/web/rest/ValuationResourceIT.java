package com.assetcompass.portfolio.web.rest;

import static com.assetcompass.portfolio.domain.ValuationAsserts.*;
import static com.assetcompass.portfolio.web.rest.TestUtil.createUpdateProxyForBean;
import static com.assetcompass.portfolio.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.assetcompass.portfolio.IntegrationTest;
import com.assetcompass.portfolio.domain.BrokerAccount;
import com.assetcompass.portfolio.domain.Valuation;
import com.assetcompass.portfolio.repository.ValuationRepository;
import com.assetcompass.portfolio.service.dto.ValuationDTO;
import com.assetcompass.portfolio.service.mapper.ValuationMapper;
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
 * Integration tests for the {@link ValuationResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ValuationResourceIT {

    private static final LocalDate DEFAULT_SNAPSHOT_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_SNAPSHOT_DATE = LocalDate.parse("2023-12-02");
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
    private MockMvc restValuationMockMvc;

    private Valuation valuation;

    private Valuation insertedValuation;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Valuation createEntity() {
        return new Valuation().snapshotDate(DEFAULT_SNAPSHOT_DATE).totalValue(DEFAULT_TOTAL_VALUE).currency(DEFAULT_CURRENCY);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Valuation createUpdatedEntity() {
        return new Valuation().snapshotDate(UPDATED_SNAPSHOT_DATE).totalValue(UPDATED_TOTAL_VALUE).currency(UPDATED_CURRENCY);
    }

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
        // Create the Valuation
        ValuationDTO valuationDTO = valuationMapper.toDto(valuation);
        var returnedValuationDTO = om.readValue(
            restValuationMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(valuationDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ValuationDTO.class
        );

        // Validate the Valuation in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedValuation = valuationMapper.toEntity(returnedValuationDTO);
        assertValuationUpdatableFieldsEquals(returnedValuation, getPersistedValuation(returnedValuation));

        insertedValuation = returnedValuation;
    }

    @Test
    @Transactional
    void createValuationWithExistingId() throws Exception {
        // Create the Valuation with an existing ID
        insertedValuation = valuationRepository.saveAndFlush(valuation);
        ValuationDTO valuationDTO = valuationMapper.toDto(valuation);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restValuationMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(valuationDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Valuation in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkSnapshotDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        valuation.setSnapshotDate(null);

        // Create the Valuation, which fails.
        ValuationDTO valuationDTO = valuationMapper.toDto(valuation);

        restValuationMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(valuationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTotalValueIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        valuation.setTotalValue(null);

        // Create the Valuation, which fails.
        ValuationDTO valuationDTO = valuationMapper.toDto(valuation);

        restValuationMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(valuationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCurrencyIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        valuation.setCurrency(null);

        // Create the Valuation, which fails.
        ValuationDTO valuationDTO = valuationMapper.toDto(valuation);

        restValuationMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(valuationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllValuations() throws Exception {
        // Initialize the database
        insertedValuation = valuationRepository.saveAndFlush(valuation);

        // Get all the valuationList
        restValuationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(valuation.getId().toString())))
            .andExpect(jsonPath("$.[*].snapshotDate").value(hasItem(DEFAULT_SNAPSHOT_DATE.toString())))
            .andExpect(jsonPath("$.[*].totalValue").value(hasItem(sameNumber(DEFAULT_TOTAL_VALUE))))
            .andExpect(jsonPath("$.[*].currency").value(hasItem(DEFAULT_CURRENCY)));
    }

    @Test
    @Transactional
    void getValuation() throws Exception {
        // Initialize the database
        insertedValuation = valuationRepository.saveAndFlush(valuation);

        // Get the valuation
        restValuationMockMvc
            .perform(get(ENTITY_API_URL_ID, valuation.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(valuation.getId().toString()))
            .andExpect(jsonPath("$.snapshotDate").value(DEFAULT_SNAPSHOT_DATE.toString()))
            .andExpect(jsonPath("$.totalValue").value(sameNumber(DEFAULT_TOTAL_VALUE)))
            .andExpect(jsonPath("$.currency").value(DEFAULT_CURRENCY));
    }

    @Test
    @Transactional
    void getValuationsByIdFiltering() throws Exception {
        // Initialize the database
        insertedValuation = valuationRepository.saveAndFlush(valuation);

        UUID id = valuation.getId();

        defaultValuationFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    @Transactional
    void getAllValuationsBySnapshotDateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedValuation = valuationRepository.saveAndFlush(valuation);

        // Get all the valuationList where snapshotDate equals to
        defaultValuationFiltering("snapshotDate.equals=" + DEFAULT_SNAPSHOT_DATE, "snapshotDate.equals=" + UPDATED_SNAPSHOT_DATE);
    }

    @Test
    @Transactional
    void getAllValuationsBySnapshotDateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedValuation = valuationRepository.saveAndFlush(valuation);

        // Get all the valuationList where snapshotDate in
        defaultValuationFiltering(
            "snapshotDate.in=" + DEFAULT_SNAPSHOT_DATE + "," + UPDATED_SNAPSHOT_DATE,
            "snapshotDate.in=" + UPDATED_SNAPSHOT_DATE
        );
    }

    @Test
    @Transactional
    void getAllValuationsBySnapshotDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedValuation = valuationRepository.saveAndFlush(valuation);

        // Get all the valuationList where snapshotDate is not null
        defaultValuationFiltering("snapshotDate.specified=true", "snapshotDate.specified=false");
    }

    @Test
    @Transactional
    void getAllValuationsBySnapshotDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedValuation = valuationRepository.saveAndFlush(valuation);

        // Get all the valuationList where snapshotDate is greater than or equal to
        defaultValuationFiltering(
            "snapshotDate.greaterThanOrEqual=" + DEFAULT_SNAPSHOT_DATE,
            "snapshotDate.greaterThanOrEqual=" + UPDATED_SNAPSHOT_DATE
        );
    }

    @Test
    @Transactional
    void getAllValuationsBySnapshotDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedValuation = valuationRepository.saveAndFlush(valuation);

        // Get all the valuationList where snapshotDate is less than or equal to
        defaultValuationFiltering(
            "snapshotDate.lessThanOrEqual=" + DEFAULT_SNAPSHOT_DATE,
            "snapshotDate.lessThanOrEqual=" + SMALLER_SNAPSHOT_DATE
        );
    }

    @Test
    @Transactional
    void getAllValuationsBySnapshotDateIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedValuation = valuationRepository.saveAndFlush(valuation);

        // Get all the valuationList where snapshotDate is less than
        defaultValuationFiltering("snapshotDate.lessThan=" + UPDATED_SNAPSHOT_DATE, "snapshotDate.lessThan=" + DEFAULT_SNAPSHOT_DATE);
    }

    @Test
    @Transactional
    void getAllValuationsBySnapshotDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedValuation = valuationRepository.saveAndFlush(valuation);

        // Get all the valuationList where snapshotDate is greater than
        defaultValuationFiltering("snapshotDate.greaterThan=" + SMALLER_SNAPSHOT_DATE, "snapshotDate.greaterThan=" + DEFAULT_SNAPSHOT_DATE);
    }

    @Test
    @Transactional
    void getAllValuationsByTotalValueIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedValuation = valuationRepository.saveAndFlush(valuation);

        // Get all the valuationList where totalValue equals to
        defaultValuationFiltering("totalValue.equals=" + DEFAULT_TOTAL_VALUE, "totalValue.equals=" + UPDATED_TOTAL_VALUE);
    }

    @Test
    @Transactional
    void getAllValuationsByTotalValueIsInShouldWork() throws Exception {
        // Initialize the database
        insertedValuation = valuationRepository.saveAndFlush(valuation);

        // Get all the valuationList where totalValue in
        defaultValuationFiltering(
            "totalValue.in=" + DEFAULT_TOTAL_VALUE + "," + UPDATED_TOTAL_VALUE,
            "totalValue.in=" + UPDATED_TOTAL_VALUE
        );
    }

    @Test
    @Transactional
    void getAllValuationsByTotalValueIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedValuation = valuationRepository.saveAndFlush(valuation);

        // Get all the valuationList where totalValue is not null
        defaultValuationFiltering("totalValue.specified=true", "totalValue.specified=false");
    }

    @Test
    @Transactional
    void getAllValuationsByTotalValueIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedValuation = valuationRepository.saveAndFlush(valuation);

        // Get all the valuationList where totalValue is greater than or equal to
        defaultValuationFiltering(
            "totalValue.greaterThanOrEqual=" + DEFAULT_TOTAL_VALUE,
            "totalValue.greaterThanOrEqual=" + UPDATED_TOTAL_VALUE
        );
    }

    @Test
    @Transactional
    void getAllValuationsByTotalValueIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedValuation = valuationRepository.saveAndFlush(valuation);

        // Get all the valuationList where totalValue is less than or equal to
        defaultValuationFiltering("totalValue.lessThanOrEqual=" + DEFAULT_TOTAL_VALUE, "totalValue.lessThanOrEqual=" + SMALLER_TOTAL_VALUE);
    }

    @Test
    @Transactional
    void getAllValuationsByTotalValueIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedValuation = valuationRepository.saveAndFlush(valuation);

        // Get all the valuationList where totalValue is less than
        defaultValuationFiltering("totalValue.lessThan=" + UPDATED_TOTAL_VALUE, "totalValue.lessThan=" + DEFAULT_TOTAL_VALUE);
    }

    @Test
    @Transactional
    void getAllValuationsByTotalValueIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedValuation = valuationRepository.saveAndFlush(valuation);

        // Get all the valuationList where totalValue is greater than
        defaultValuationFiltering("totalValue.greaterThan=" + SMALLER_TOTAL_VALUE, "totalValue.greaterThan=" + DEFAULT_TOTAL_VALUE);
    }

    @Test
    @Transactional
    void getAllValuationsByCurrencyIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedValuation = valuationRepository.saveAndFlush(valuation);

        // Get all the valuationList where currency equals to
        defaultValuationFiltering("currency.equals=" + DEFAULT_CURRENCY, "currency.equals=" + UPDATED_CURRENCY);
    }

    @Test
    @Transactional
    void getAllValuationsByCurrencyIsInShouldWork() throws Exception {
        // Initialize the database
        insertedValuation = valuationRepository.saveAndFlush(valuation);

        // Get all the valuationList where currency in
        defaultValuationFiltering("currency.in=" + DEFAULT_CURRENCY + "," + UPDATED_CURRENCY, "currency.in=" + UPDATED_CURRENCY);
    }

    @Test
    @Transactional
    void getAllValuationsByCurrencyIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedValuation = valuationRepository.saveAndFlush(valuation);

        // Get all the valuationList where currency is not null
        defaultValuationFiltering("currency.specified=true", "currency.specified=false");
    }

    @Test
    @Transactional
    void getAllValuationsByCurrencyContainsSomething() throws Exception {
        // Initialize the database
        insertedValuation = valuationRepository.saveAndFlush(valuation);

        // Get all the valuationList where currency contains
        defaultValuationFiltering("currency.contains=" + DEFAULT_CURRENCY, "currency.contains=" + UPDATED_CURRENCY);
    }

    @Test
    @Transactional
    void getAllValuationsByCurrencyNotContainsSomething() throws Exception {
        // Initialize the database
        insertedValuation = valuationRepository.saveAndFlush(valuation);

        // Get all the valuationList where currency does not contain
        defaultValuationFiltering("currency.doesNotContain=" + UPDATED_CURRENCY, "currency.doesNotContain=" + DEFAULT_CURRENCY);
    }

    @Test
    @Transactional
    void getAllValuationsByAccountIsEqualToSomething() throws Exception {
        BrokerAccount account;
        if (TestUtil.findAll(em, BrokerAccount.class).isEmpty()) {
            valuationRepository.saveAndFlush(valuation);
            account = BrokerAccountResourceIT.createEntity(em);
        } else {
            account = TestUtil.findAll(em, BrokerAccount.class).get(0);
        }
        em.persist(account);
        em.flush();
        valuation.setAccount(account);
        valuationRepository.saveAndFlush(valuation);
        UUID accountId = account.getId();
        // Get all the valuationList where account equals to accountId
        defaultValuationShouldBeFound("accountId.equals=" + accountId);

        // Get all the valuationList where account equals to UUID.randomUUID()
        defaultValuationShouldNotBeFound("accountId.equals=" + UUID.randomUUID());
    }

    private void defaultValuationFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultValuationShouldBeFound(shouldBeFound);
        defaultValuationShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultValuationShouldBeFound(String filter) throws Exception {
        restValuationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(valuation.getId().toString())))
            .andExpect(jsonPath("$.[*].snapshotDate").value(hasItem(DEFAULT_SNAPSHOT_DATE.toString())))
            .andExpect(jsonPath("$.[*].totalValue").value(hasItem(sameNumber(DEFAULT_TOTAL_VALUE))))
            .andExpect(jsonPath("$.[*].currency").value(hasItem(DEFAULT_CURRENCY)));

        // Check, that the count call also returns 1
        restValuationMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultValuationShouldNotBeFound(String filter) throws Exception {
        restValuationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restValuationMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingValuation() throws Exception {
        // Get the valuation
        restValuationMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingValuation() throws Exception {
        // Initialize the database
        insertedValuation = valuationRepository.saveAndFlush(valuation);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the valuation
        Valuation updatedValuation = valuationRepository.findById(valuation.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedValuation are not directly saved in db
        em.detach(updatedValuation);
        updatedValuation.snapshotDate(UPDATED_SNAPSHOT_DATE).totalValue(UPDATED_TOTAL_VALUE).currency(UPDATED_CURRENCY);
        ValuationDTO valuationDTO = valuationMapper.toDto(updatedValuation);

        restValuationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, valuationDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(valuationDTO))
            )
            .andExpect(status().isOk());

        // Validate the Valuation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedValuationToMatchAllProperties(updatedValuation);
    }

    @Test
    @Transactional
    void putNonExistingValuation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        valuation.setId(UUID.randomUUID());

        // Create the Valuation
        ValuationDTO valuationDTO = valuationMapper.toDto(valuation);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restValuationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, valuationDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(valuationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Valuation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchValuation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        valuation.setId(UUID.randomUUID());

        // Create the Valuation
        ValuationDTO valuationDTO = valuationMapper.toDto(valuation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restValuationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(valuationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Valuation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamValuation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        valuation.setId(UUID.randomUUID());

        // Create the Valuation
        ValuationDTO valuationDTO = valuationMapper.toDto(valuation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restValuationMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(valuationDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Valuation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateValuationWithPatch() throws Exception {
        // Initialize the database
        insertedValuation = valuationRepository.saveAndFlush(valuation);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the valuation using partial update
        Valuation partialUpdatedValuation = new Valuation();
        partialUpdatedValuation.setId(valuation.getId());

        partialUpdatedValuation.currency(UPDATED_CURRENCY);

        restValuationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedValuation.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedValuation))
            )
            .andExpect(status().isOk());

        // Validate the Valuation in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertValuationUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedValuation, valuation),
            getPersistedValuation(valuation)
        );
    }

    @Test
    @Transactional
    void fullUpdateValuationWithPatch() throws Exception {
        // Initialize the database
        insertedValuation = valuationRepository.saveAndFlush(valuation);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the valuation using partial update
        Valuation partialUpdatedValuation = new Valuation();
        partialUpdatedValuation.setId(valuation.getId());

        partialUpdatedValuation.snapshotDate(UPDATED_SNAPSHOT_DATE).totalValue(UPDATED_TOTAL_VALUE).currency(UPDATED_CURRENCY);

        restValuationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedValuation.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedValuation))
            )
            .andExpect(status().isOk());

        // Validate the Valuation in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertValuationUpdatableFieldsEquals(partialUpdatedValuation, getPersistedValuation(partialUpdatedValuation));
    }

    @Test
    @Transactional
    void patchNonExistingValuation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        valuation.setId(UUID.randomUUID());

        // Create the Valuation
        ValuationDTO valuationDTO = valuationMapper.toDto(valuation);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restValuationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, valuationDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(valuationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Valuation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchValuation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        valuation.setId(UUID.randomUUID());

        // Create the Valuation
        ValuationDTO valuationDTO = valuationMapper.toDto(valuation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restValuationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(valuationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Valuation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamValuation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        valuation.setId(UUID.randomUUID());

        // Create the Valuation
        ValuationDTO valuationDTO = valuationMapper.toDto(valuation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restValuationMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(valuationDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Valuation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteValuation() throws Exception {
        // Initialize the database
        insertedValuation = valuationRepository.saveAndFlush(valuation);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the valuation
        restValuationMockMvc
            .perform(delete(ENTITY_API_URL_ID, valuation.getId().toString()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return valuationRepository.count();
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
        return valuationRepository.findById(valuation.getId()).orElseThrow();
    }

    protected void assertPersistedValuationToMatchAllProperties(Valuation expectedValuation) {
        assertValuationAllPropertiesEquals(expectedValuation, getPersistedValuation(expectedValuation));
    }

    protected void assertPersistedValuationToMatchUpdatableProperties(Valuation expectedValuation) {
        assertValuationAllUpdatablePropertiesEquals(expectedValuation, getPersistedValuation(expectedValuation));
    }
}
