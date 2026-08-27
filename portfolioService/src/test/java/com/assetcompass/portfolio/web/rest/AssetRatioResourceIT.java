package com.assetcompass.portfolio.web.rest;

import static com.assetcompass.portfolio.domain.AssetRatioAsserts.*;
import static com.assetcompass.portfolio.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.assetcompass.portfolio.IntegrationTest;
import com.assetcompass.portfolio.domain.Asset;
import com.assetcompass.portfolio.domain.AssetRatio;
import com.assetcompass.portfolio.repository.AssetRatioRepository;
import com.assetcompass.portfolio.service.dto.AssetRatioDTO;
import com.assetcompass.portfolio.service.mapper.AssetRatioMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
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
 * Integration tests for the {@link AssetRatioResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class AssetRatioResourceIT {

    private static final String DEFAULT_RATIO = "AAAAAAAAAA";
    private static final String UPDATED_RATIO = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_EFFECTIVE_FROM = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_EFFECTIVE_FROM = LocalDate.parse("2023-12-02");
    private static final LocalDate SMALLER_EFFECTIVE_FROM = LocalDate.ofEpochDay(-1L);

    private static final String ENTITY_API_URL = "/api/asset-ratios";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private AssetRatioRepository assetRatioRepository;

    @Autowired
    private AssetRatioMapper assetRatioMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAssetRatioMockMvc;

    private AssetRatio assetRatio;

    private AssetRatio insertedAssetRatio;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AssetRatio createEntity(EntityManager em) {
        AssetRatio assetRatio = new AssetRatio().ratio(DEFAULT_RATIO).effectiveFrom(DEFAULT_EFFECTIVE_FROM);
        // Add required entity
        Asset asset;
        if (TestUtil.findAll(em, Asset.class).isEmpty()) {
            asset = AssetResourceIT.createEntity();
            em.persist(asset);
            em.flush();
        } else {
            asset = TestUtil.findAll(em, Asset.class).get(0);
        }
        assetRatio.setAsset(asset);
        return assetRatio;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AssetRatio createUpdatedEntity(EntityManager em) {
        AssetRatio updatedAssetRatio = new AssetRatio().ratio(UPDATED_RATIO).effectiveFrom(UPDATED_EFFECTIVE_FROM);
        // Add required entity
        Asset asset;
        if (TestUtil.findAll(em, Asset.class).isEmpty()) {
            asset = AssetResourceIT.createUpdatedEntity();
            em.persist(asset);
            em.flush();
        } else {
            asset = TestUtil.findAll(em, Asset.class).get(0);
        }
        updatedAssetRatio.setAsset(asset);
        return updatedAssetRatio;
    }

    @BeforeEach
    void initTest() {
        assetRatio = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedAssetRatio != null) {
            assetRatioRepository.delete(insertedAssetRatio);
            insertedAssetRatio = null;
        }
    }

    @Test
    @Transactional
    void createAssetRatio() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the AssetRatio
        AssetRatioDTO assetRatioDTO = assetRatioMapper.toDto(assetRatio);
        assetRatioDTO.setId(UUID.randomUUID());
        var returnedAssetRatioDTO = om.readValue(
            restAssetRatioMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(assetRatioDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            AssetRatioDTO.class
        );

        // Validate the AssetRatio in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedAssetRatio = assetRatioMapper.toEntity(returnedAssetRatioDTO);
        assertAssetRatioUpdatableFieldsEquals(returnedAssetRatio, getPersistedAssetRatio(returnedAssetRatio));

        insertedAssetRatio = returnedAssetRatio;
    }

    @Test
    @Transactional
    void createAssetRatioWithExistingId() throws Exception {
        // Create the AssetRatio with an existing ID
        insertedAssetRatio = assetRatioRepository.saveAndFlush(assetRatio);
        AssetRatioDTO assetRatioDTO = assetRatioMapper.toDto(assetRatio);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAssetRatioMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(assetRatioDTO)))
            .andExpect(status().isBadRequest());

        // Validate the AssetRatio in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkRatioIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        assetRatio.setRatio(null);

        // Create the AssetRatio, which fails.
        AssetRatioDTO assetRatioDTO = assetRatioMapper.toDto(assetRatio);

        restAssetRatioMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(assetRatioDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEffectiveFromIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        assetRatio.setEffectiveFrom(null);

        // Create the AssetRatio, which fails.
        AssetRatioDTO assetRatioDTO = assetRatioMapper.toDto(assetRatio);

        restAssetRatioMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(assetRatioDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllAssetRatios() throws Exception {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.saveAndFlush(assetRatio);

        // Get all the assetRatioList
        restAssetRatioMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(assetRatio.getId().toString())))
            .andExpect(jsonPath("$.[*].ratio").value(hasItem(DEFAULT_RATIO)))
            .andExpect(jsonPath("$.[*].effectiveFrom").value(hasItem(DEFAULT_EFFECTIVE_FROM.toString())));
    }

    @Test
    @Transactional
    void getAssetRatio() throws Exception {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.saveAndFlush(assetRatio);

        // Get the assetRatio
        restAssetRatioMockMvc
            .perform(get(ENTITY_API_URL_ID, assetRatio.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(assetRatio.getId().toString()))
            .andExpect(jsonPath("$.ratio").value(DEFAULT_RATIO))
            .andExpect(jsonPath("$.effectiveFrom").value(DEFAULT_EFFECTIVE_FROM.toString()));
    }

    @Test
    @Transactional
    void getAssetRatiosByIdFiltering() throws Exception {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.saveAndFlush(assetRatio);

        UUID id = assetRatio.getId();

        defaultAssetRatioFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    @Transactional
    void getAllAssetRatiosByRatioIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.saveAndFlush(assetRatio);

        // Get all the assetRatioList where ratio equals to
        defaultAssetRatioFiltering("ratio.equals=" + DEFAULT_RATIO, "ratio.equals=" + UPDATED_RATIO);
    }

    @Test
    @Transactional
    void getAllAssetRatiosByRatioIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.saveAndFlush(assetRatio);

        // Get all the assetRatioList where ratio in
        defaultAssetRatioFiltering("ratio.in=" + DEFAULT_RATIO + "," + UPDATED_RATIO, "ratio.in=" + UPDATED_RATIO);
    }

    @Test
    @Transactional
    void getAllAssetRatiosByRatioIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.saveAndFlush(assetRatio);

        // Get all the assetRatioList where ratio is not null
        defaultAssetRatioFiltering("ratio.specified=true", "ratio.specified=false");
    }

    @Test
    @Transactional
    void getAllAssetRatiosByRatioContainsSomething() throws Exception {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.saveAndFlush(assetRatio);

        // Get all the assetRatioList where ratio contains
        defaultAssetRatioFiltering("ratio.contains=" + DEFAULT_RATIO, "ratio.contains=" + UPDATED_RATIO);
    }

    @Test
    @Transactional
    void getAllAssetRatiosByRatioNotContainsSomething() throws Exception {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.saveAndFlush(assetRatio);

        // Get all the assetRatioList where ratio does not contain
        defaultAssetRatioFiltering("ratio.doesNotContain=" + UPDATED_RATIO, "ratio.doesNotContain=" + DEFAULT_RATIO);
    }

    @Test
    @Transactional
    void getAllAssetRatiosByEffectiveFromIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.saveAndFlush(assetRatio);

        // Get all the assetRatioList where effectiveFrom equals to
        defaultAssetRatioFiltering("effectiveFrom.equals=" + DEFAULT_EFFECTIVE_FROM, "effectiveFrom.equals=" + UPDATED_EFFECTIVE_FROM);
    }

    @Test
    @Transactional
    void getAllAssetRatiosByEffectiveFromIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.saveAndFlush(assetRatio);

        // Get all the assetRatioList where effectiveFrom in
        defaultAssetRatioFiltering(
            "effectiveFrom.in=" + DEFAULT_EFFECTIVE_FROM + "," + UPDATED_EFFECTIVE_FROM,
            "effectiveFrom.in=" + UPDATED_EFFECTIVE_FROM
        );
    }

    @Test
    @Transactional
    void getAllAssetRatiosByEffectiveFromIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.saveAndFlush(assetRatio);

        // Get all the assetRatioList where effectiveFrom is not null
        defaultAssetRatioFiltering("effectiveFrom.specified=true", "effectiveFrom.specified=false");
    }

    @Test
    @Transactional
    void getAllAssetRatiosByEffectiveFromIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.saveAndFlush(assetRatio);

        // Get all the assetRatioList where effectiveFrom is greater than or equal to
        defaultAssetRatioFiltering(
            "effectiveFrom.greaterThanOrEqual=" + DEFAULT_EFFECTIVE_FROM,
            "effectiveFrom.greaterThanOrEqual=" + UPDATED_EFFECTIVE_FROM
        );
    }

    @Test
    @Transactional
    void getAllAssetRatiosByEffectiveFromIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.saveAndFlush(assetRatio);

        // Get all the assetRatioList where effectiveFrom is less than or equal to
        defaultAssetRatioFiltering(
            "effectiveFrom.lessThanOrEqual=" + DEFAULT_EFFECTIVE_FROM,
            "effectiveFrom.lessThanOrEqual=" + SMALLER_EFFECTIVE_FROM
        );
    }

    @Test
    @Transactional
    void getAllAssetRatiosByEffectiveFromIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.saveAndFlush(assetRatio);

        // Get all the assetRatioList where effectiveFrom is less than
        defaultAssetRatioFiltering("effectiveFrom.lessThan=" + UPDATED_EFFECTIVE_FROM, "effectiveFrom.lessThan=" + DEFAULT_EFFECTIVE_FROM);
    }

    @Test
    @Transactional
    void getAllAssetRatiosByEffectiveFromIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.saveAndFlush(assetRatio);

        // Get all the assetRatioList where effectiveFrom is greater than
        defaultAssetRatioFiltering(
            "effectiveFrom.greaterThan=" + SMALLER_EFFECTIVE_FROM,
            "effectiveFrom.greaterThan=" + DEFAULT_EFFECTIVE_FROM
        );
    }

    @Test
    @Transactional
    void getAllAssetRatiosByAssetIsEqualToSomething() throws Exception {
        Asset asset;
        if (TestUtil.findAll(em, Asset.class).isEmpty()) {
            assetRatioRepository.saveAndFlush(assetRatio);
            asset = AssetResourceIT.createEntity();
        } else {
            asset = TestUtil.findAll(em, Asset.class).get(0);
        }
        em.persist(asset);
        em.flush();
        assetRatio.setAsset(asset);
        assetRatioRepository.saveAndFlush(assetRatio);
        UUID assetId = asset.getId();
        // Get all the assetRatioList where asset equals to assetId
        defaultAssetRatioShouldBeFound("assetId.equals=" + assetId);

        // Get all the assetRatioList where asset equals to UUID.randomUUID()
        defaultAssetRatioShouldNotBeFound("assetId.equals=" + UUID.randomUUID());
    }

    private void defaultAssetRatioFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultAssetRatioShouldBeFound(shouldBeFound);
        defaultAssetRatioShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultAssetRatioShouldBeFound(String filter) throws Exception {
        restAssetRatioMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(assetRatio.getId().toString())))
            .andExpect(jsonPath("$.[*].ratio").value(hasItem(DEFAULT_RATIO)))
            .andExpect(jsonPath("$.[*].effectiveFrom").value(hasItem(DEFAULT_EFFECTIVE_FROM.toString())));

        // Check, that the count call also returns 1
        restAssetRatioMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultAssetRatioShouldNotBeFound(String filter) throws Exception {
        restAssetRatioMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restAssetRatioMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingAssetRatio() throws Exception {
        // Get the assetRatio
        restAssetRatioMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingAssetRatio() throws Exception {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.saveAndFlush(assetRatio);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the assetRatio
        AssetRatio updatedAssetRatio = assetRatioRepository.findById(assetRatio.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedAssetRatio are not directly saved in db
        em.detach(updatedAssetRatio);
        updatedAssetRatio.ratio(UPDATED_RATIO).effectiveFrom(UPDATED_EFFECTIVE_FROM);
        AssetRatioDTO assetRatioDTO = assetRatioMapper.toDto(updatedAssetRatio);

        restAssetRatioMockMvc
            .perform(
                put(ENTITY_API_URL_ID, assetRatioDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(assetRatioDTO))
            )
            .andExpect(status().isOk());

        // Validate the AssetRatio in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedAssetRatioToMatchAllProperties(updatedAssetRatio);
    }

    @Test
    @Transactional
    void putNonExistingAssetRatio() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        assetRatio.setId(UUID.randomUUID());

        // Create the AssetRatio
        AssetRatioDTO assetRatioDTO = assetRatioMapper.toDto(assetRatio);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAssetRatioMockMvc
            .perform(
                put(ENTITY_API_URL_ID, assetRatioDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(assetRatioDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AssetRatio in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchAssetRatio() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        assetRatio.setId(UUID.randomUUID());

        // Create the AssetRatio
        AssetRatioDTO assetRatioDTO = assetRatioMapper.toDto(assetRatio);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAssetRatioMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(assetRatioDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AssetRatio in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAssetRatio() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        assetRatio.setId(UUID.randomUUID());

        // Create the AssetRatio
        AssetRatioDTO assetRatioDTO = assetRatioMapper.toDto(assetRatio);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAssetRatioMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(assetRatioDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the AssetRatio in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateAssetRatioWithPatch() throws Exception {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.saveAndFlush(assetRatio);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the assetRatio using partial update
        AssetRatio partialUpdatedAssetRatio = new AssetRatio();
        partialUpdatedAssetRatio.setId(assetRatio.getId());

        partialUpdatedAssetRatio.ratio(UPDATED_RATIO);

        restAssetRatioMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAssetRatio.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAssetRatio))
            )
            .andExpect(status().isOk());

        // Validate the AssetRatio in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAssetRatioUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedAssetRatio, assetRatio),
            getPersistedAssetRatio(assetRatio)
        );
    }

    @Test
    @Transactional
    void fullUpdateAssetRatioWithPatch() throws Exception {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.saveAndFlush(assetRatio);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the assetRatio using partial update
        AssetRatio partialUpdatedAssetRatio = new AssetRatio();
        partialUpdatedAssetRatio.setId(assetRatio.getId());

        partialUpdatedAssetRatio.ratio(UPDATED_RATIO).effectiveFrom(UPDATED_EFFECTIVE_FROM);

        restAssetRatioMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAssetRatio.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAssetRatio))
            )
            .andExpect(status().isOk());

        // Validate the AssetRatio in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAssetRatioUpdatableFieldsEquals(partialUpdatedAssetRatio, getPersistedAssetRatio(partialUpdatedAssetRatio));
    }

    @Test
    @Transactional
    void patchNonExistingAssetRatio() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        assetRatio.setId(UUID.randomUUID());

        // Create the AssetRatio
        AssetRatioDTO assetRatioDTO = assetRatioMapper.toDto(assetRatio);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAssetRatioMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, assetRatioDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(assetRatioDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AssetRatio in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAssetRatio() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        assetRatio.setId(UUID.randomUUID());

        // Create the AssetRatio
        AssetRatioDTO assetRatioDTO = assetRatioMapper.toDto(assetRatio);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAssetRatioMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(assetRatioDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the AssetRatio in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAssetRatio() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        assetRatio.setId(UUID.randomUUID());

        // Create the AssetRatio
        AssetRatioDTO assetRatioDTO = assetRatioMapper.toDto(assetRatio);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAssetRatioMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(assetRatioDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the AssetRatio in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteAssetRatio() throws Exception {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.saveAndFlush(assetRatio);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the assetRatio
        restAssetRatioMockMvc
            .perform(delete(ENTITY_API_URL_ID, assetRatio.getId().toString()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return assetRatioRepository.count();
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

    protected AssetRatio getPersistedAssetRatio(AssetRatio assetRatio) {
        return assetRatioRepository.findById(assetRatio.getId()).orElseThrow();
    }

    protected void assertPersistedAssetRatioToMatchAllProperties(AssetRatio expectedAssetRatio) {
        assertAssetRatioAllPropertiesEquals(expectedAssetRatio, getPersistedAssetRatio(expectedAssetRatio));
    }

    protected void assertPersistedAssetRatioToMatchUpdatableProperties(AssetRatio expectedAssetRatio) {
        assertAssetRatioAllUpdatablePropertiesEquals(expectedAssetRatio, getPersistedAssetRatio(expectedAssetRatio));
    }
}
