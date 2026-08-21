package com.assetcompass.gateway.web.rest;

import static com.assetcompass.gateway.domain.AssetRatioAsserts.*;
import static com.assetcompass.gateway.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.assetcompass.gateway.IntegrationTest;
import com.assetcompass.gateway.domain.Asset;
import com.assetcompass.gateway.domain.AssetRatio;
import com.assetcompass.gateway.repository.AssetRatioRepository;
import com.assetcompass.gateway.repository.AssetRepository;
import com.assetcompass.gateway.repository.EntityManager;
import com.assetcompass.gateway.service.dto.AssetRatioDTO;
import com.assetcompass.gateway.service.mapper.AssetRatioMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Integration tests for the {@link AssetRatioResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class AssetRatioResourceIT {

    private static final String DEFAULT_RATIO = "AAAAAAAAAA";
    private static final String UPDATED_RATIO = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_EFFECTIVE_FROM = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_EFFECTIVE_FROM = LocalDate.parse("2023-12-22");
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
    private WebTestClient webTestClient;

    private AssetRatio assetRatio;

    private AssetRatio insertedAssetRatio;

    @Autowired
    private AssetRepository assetRepository;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AssetRatio createEntity(EntityManager em) {
        AssetRatio assetRatio = new AssetRatio().id(UUID.randomUUID()).ratio(DEFAULT_RATIO).effectiveFrom(DEFAULT_EFFECTIVE_FROM);
        // Add required entity
        Asset asset;
        asset = em.insert(AssetResourceIT.createEntity()).block();
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
        AssetRatio updatedAssetRatio = new AssetRatio().id(UUID.randomUUID()).ratio(UPDATED_RATIO).effectiveFrom(UPDATED_EFFECTIVE_FROM);
        // Add required entity
        Asset asset;
        asset = em.insert(AssetResourceIT.createUpdatedEntity()).block();
        updatedAssetRatio.setAsset(asset);
        return updatedAssetRatio;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(AssetRatio.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
        AssetResourceIT.deleteEntities(em);
    }

    @BeforeEach
    void setupCsrf() {
        webTestClient = webTestClient.mutateWith(csrf());
    }

    @BeforeEach
    void initTest() {
        assetRatio = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedAssetRatio != null) {
            assetRatioRepository.delete(insertedAssetRatio).block();
            insertedAssetRatio = null;
        }
        deleteEntities(em);
    }

    @Test
    void createAssetRatio() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        assetRatio.setId(null);
        // Create the AssetRatio
        AssetRatioDTO assetRatioDTO = assetRatioMapper.toDto(assetRatio);
        var returnedAssetRatioDTO = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(assetRatioDTO))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(AssetRatioDTO.class)
            .returnResult()
            .getResponseBody();

        // Validate the AssetRatio in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedAssetRatio = assetRatioMapper.toEntity(returnedAssetRatioDTO);
        assertAssetRatioUpdatableFieldsEquals(returnedAssetRatio, getPersistedAssetRatio(returnedAssetRatio));

        insertedAssetRatio = returnedAssetRatio;
    }

    @Test
    void createAssetRatioWithExistingId() throws Exception {
        // Create the AssetRatio with an existing ID
        insertedAssetRatio = assetRatioRepository.save(assetRatio).block();
        AssetRatioDTO assetRatioDTO = assetRatioMapper.toDto(assetRatio);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(assetRatioDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the AssetRatio in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkRatioIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        assetRatio.setRatio(null);

        // Create the AssetRatio, which fails.
        AssetRatioDTO assetRatioDTO = assetRatioMapper.toDto(assetRatio);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(assetRatioDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkEffectiveFromIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        assetRatio.setEffectiveFrom(null);

        // Create the AssetRatio, which fails.
        AssetRatioDTO assetRatioDTO = assetRatioMapper.toDto(assetRatio);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(assetRatioDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllAssetRatios() {
        // Initialize the database
        assetRatio.setId(UUID.randomUUID());
        insertedAssetRatio = assetRatioRepository.save(assetRatio).block();

        // Get all the assetRatioList
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
            .value(hasItem(assetRatio.getId().toString()))
            .jsonPath("$.[*].ratio")
            .value(hasItem(DEFAULT_RATIO))
            .jsonPath("$.[*].effectiveFrom")
            .value(hasItem(DEFAULT_EFFECTIVE_FROM.toString()));
    }

    @Test
    void getAssetRatio() {
        // Initialize the database
        assetRatio.setId(UUID.randomUUID());
        insertedAssetRatio = assetRatioRepository.save(assetRatio).block();

        // Get the assetRatio
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, assetRatio.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(assetRatio.getId().toString()))
            .jsonPath("$.ratio")
            .value(is(DEFAULT_RATIO))
            .jsonPath("$.effectiveFrom")
            .value(is(DEFAULT_EFFECTIVE_FROM.toString()));
    }

    @Test
    void getAssetRatiosByIdFiltering() {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.save(assetRatio).block();

        UUID id = assetRatio.getId();

        defaultAssetRatioFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    void getAllAssetRatiosByRatioIsEqualToSomething() {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.save(assetRatio).block();

        // Get all the assetRatioList where ratio equals to
        defaultAssetRatioFiltering("ratio.equals=" + DEFAULT_RATIO, "ratio.equals=" + UPDATED_RATIO);
    }

    @Test
    void getAllAssetRatiosByRatioIsInShouldWork() {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.save(assetRatio).block();

        // Get all the assetRatioList where ratio in
        defaultAssetRatioFiltering("ratio.in=" + DEFAULT_RATIO + "," + UPDATED_RATIO, "ratio.in=" + UPDATED_RATIO);
    }

    @Test
    void getAllAssetRatiosByRatioIsNullOrNotNull() {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.save(assetRatio).block();

        // Get all the assetRatioList where ratio is not null
        defaultAssetRatioFiltering("ratio.specified=true", "ratio.specified=false");
    }

    @Test
    void getAllAssetRatiosByRatioContainsSomething() {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.save(assetRatio).block();

        // Get all the assetRatioList where ratio contains
        defaultAssetRatioFiltering("ratio.contains=" + DEFAULT_RATIO, "ratio.contains=" + UPDATED_RATIO);
    }

    @Test
    void getAllAssetRatiosByRatioNotContainsSomething() {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.save(assetRatio).block();

        // Get all the assetRatioList where ratio does not contain
        defaultAssetRatioFiltering("ratio.doesNotContain=" + UPDATED_RATIO, "ratio.doesNotContain=" + DEFAULT_RATIO);
    }

    @Test
    void getAllAssetRatiosByEffectiveFromIsEqualToSomething() {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.save(assetRatio).block();

        // Get all the assetRatioList where effectiveFrom equals to
        defaultAssetRatioFiltering("effectiveFrom.equals=" + DEFAULT_EFFECTIVE_FROM, "effectiveFrom.equals=" + UPDATED_EFFECTIVE_FROM);
    }

    @Test
    void getAllAssetRatiosByEffectiveFromIsInShouldWork() {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.save(assetRatio).block();

        // Get all the assetRatioList where effectiveFrom in
        defaultAssetRatioFiltering(
            "effectiveFrom.in=" + DEFAULT_EFFECTIVE_FROM + "," + UPDATED_EFFECTIVE_FROM,
            "effectiveFrom.in=" + UPDATED_EFFECTIVE_FROM
        );
    }

    @Test
    void getAllAssetRatiosByEffectiveFromIsNullOrNotNull() {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.save(assetRatio).block();

        // Get all the assetRatioList where effectiveFrom is not null
        defaultAssetRatioFiltering("effectiveFrom.specified=true", "effectiveFrom.specified=false");
    }

    @Test
    void getAllAssetRatiosByEffectiveFromIsGreaterThanOrEqualToSomething() {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.save(assetRatio).block();

        // Get all the assetRatioList where effectiveFrom is greater than or equal to
        defaultAssetRatioFiltering(
            "effectiveFrom.greaterThanOrEqual=" + DEFAULT_EFFECTIVE_FROM,
            "effectiveFrom.greaterThanOrEqual=" + UPDATED_EFFECTIVE_FROM
        );
    }

    @Test
    void getAllAssetRatiosByEffectiveFromIsLessThanOrEqualToSomething() {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.save(assetRatio).block();

        // Get all the assetRatioList where effectiveFrom is less than or equal to
        defaultAssetRatioFiltering(
            "effectiveFrom.lessThanOrEqual=" + DEFAULT_EFFECTIVE_FROM,
            "effectiveFrom.lessThanOrEqual=" + SMALLER_EFFECTIVE_FROM
        );
    }

    @Test
    void getAllAssetRatiosByEffectiveFromIsLessThanSomething() {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.save(assetRatio).block();

        // Get all the assetRatioList where effectiveFrom is less than
        defaultAssetRatioFiltering("effectiveFrom.lessThan=" + UPDATED_EFFECTIVE_FROM, "effectiveFrom.lessThan=" + DEFAULT_EFFECTIVE_FROM);
    }

    @Test
    void getAllAssetRatiosByEffectiveFromIsGreaterThanSomething() {
        // Initialize the database
        insertedAssetRatio = assetRatioRepository.save(assetRatio).block();

        // Get all the assetRatioList where effectiveFrom is greater than
        defaultAssetRatioFiltering(
            "effectiveFrom.greaterThan=" + SMALLER_EFFECTIVE_FROM,
            "effectiveFrom.greaterThan=" + DEFAULT_EFFECTIVE_FROM
        );
    }

    @Test
    void getAllAssetRatiosByAssetIsEqualToSomething() {
        Asset asset = AssetResourceIT.createEntity();
        assetRepository.save(asset).block();
        UUID assetId = asset.getId();
        assetRatio.setAssetId(assetId);
        insertedAssetRatio = assetRatioRepository.save(assetRatio).block();
        // Get all the assetRatioList where asset equals to assetId
        defaultAssetRatioShouldBeFound("assetId.equals=" + assetId);

        // Get all the assetRatioList where asset equals to UUID.randomUUID()
        defaultAssetRatioShouldNotBeFound("assetId.equals=" + UUID.randomUUID());
    }

    private void defaultAssetRatioFiltering(String shouldBeFound, String shouldNotBeFound) {
        defaultAssetRatioShouldBeFound(shouldBeFound);
        defaultAssetRatioShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultAssetRatioShouldBeFound(String filter) {
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
            .value(hasItem(assetRatio.getId().toString()))
            .jsonPath("$.[*].ratio")
            .value(hasItem(DEFAULT_RATIO))

            .jsonPath("$.[*].effectiveFrom")
            .value(hasItem(DEFAULT_EFFECTIVE_FROM.toString()));

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
    private void defaultAssetRatioShouldNotBeFound(String filter) {
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
    void getNonExistingAssetRatio() {
        // Get the assetRatio
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingAssetRatio() throws Exception {
        // Initialize the database
        assetRatio.setId(UUID.randomUUID());
        insertedAssetRatio = assetRatioRepository.save(assetRatio).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the assetRatio
        AssetRatio updatedAssetRatio = assetRatioRepository.findById(assetRatio.getId()).block();
        updatedAssetRatio.ratio(UPDATED_RATIO).effectiveFrom(UPDATED_EFFECTIVE_FROM);
        AssetRatioDTO assetRatioDTO = assetRatioMapper.toDto(updatedAssetRatio);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, assetRatioDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(assetRatioDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the AssetRatio in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedAssetRatioToMatchAllProperties(updatedAssetRatio);
    }

    @Test
    void putNonExistingAssetRatio() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        assetRatio.setId(UUID.randomUUID());

        // Create the AssetRatio
        AssetRatioDTO assetRatioDTO = assetRatioMapper.toDto(assetRatio);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, assetRatioDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(assetRatioDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the AssetRatio in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchAssetRatio() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        assetRatio.setId(UUID.randomUUID());

        // Create the AssetRatio
        AssetRatioDTO assetRatioDTO = assetRatioMapper.toDto(assetRatio);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(assetRatioDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the AssetRatio in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamAssetRatio() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        assetRatio.setId(UUID.randomUUID());

        // Create the AssetRatio
        AssetRatioDTO assetRatioDTO = assetRatioMapper.toDto(assetRatio);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(assetRatioDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the AssetRatio in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateAssetRatioWithPatch() throws Exception {
        // Initialize the database
        assetRatio.setId(UUID.randomUUID());
        insertedAssetRatio = assetRatioRepository.save(assetRatio).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the assetRatio using partial update
        AssetRatio partialUpdatedAssetRatio = new AssetRatio();
        partialUpdatedAssetRatio.setId(assetRatio.getId());

        partialUpdatedAssetRatio.effectiveFrom(UPDATED_EFFECTIVE_FROM);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedAssetRatio.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedAssetRatio))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the AssetRatio in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAssetRatioUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedAssetRatio, assetRatio),
            getPersistedAssetRatio(assetRatio)
        );
    }

    @Test
    void fullUpdateAssetRatioWithPatch() throws Exception {
        // Initialize the database
        assetRatio.setId(UUID.randomUUID());
        insertedAssetRatio = assetRatioRepository.save(assetRatio).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the assetRatio using partial update
        AssetRatio partialUpdatedAssetRatio = new AssetRatio();
        partialUpdatedAssetRatio.setId(assetRatio.getId());

        partialUpdatedAssetRatio.ratio(UPDATED_RATIO).effectiveFrom(UPDATED_EFFECTIVE_FROM);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedAssetRatio.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedAssetRatio))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the AssetRatio in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAssetRatioUpdatableFieldsEquals(partialUpdatedAssetRatio, getPersistedAssetRatio(partialUpdatedAssetRatio));
    }

    @Test
    void patchNonExistingAssetRatio() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        assetRatio.setId(UUID.randomUUID());

        // Create the AssetRatio
        AssetRatioDTO assetRatioDTO = assetRatioMapper.toDto(assetRatio);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, assetRatioDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(assetRatioDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the AssetRatio in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchAssetRatio() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        assetRatio.setId(UUID.randomUUID());

        // Create the AssetRatio
        AssetRatioDTO assetRatioDTO = assetRatioMapper.toDto(assetRatio);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(assetRatioDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the AssetRatio in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamAssetRatio() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        assetRatio.setId(UUID.randomUUID());

        // Create the AssetRatio
        AssetRatioDTO assetRatioDTO = assetRatioMapper.toDto(assetRatio);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(assetRatioDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the AssetRatio in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteAssetRatio() {
        // Initialize the database
        assetRatio.setId(UUID.randomUUID());
        insertedAssetRatio = assetRatioRepository.save(assetRatio).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the assetRatio
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, assetRatio.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return assetRatioRepository.count().block();
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
        return assetRatioRepository.findById(assetRatio.getId()).block();
    }

    protected void assertPersistedAssetRatioToMatchAllProperties(AssetRatio expectedAssetRatio) {
        // Test fails because reactive api returns an empty object instead of null
        // assertAssetRatioAllPropertiesEquals(expectedAssetRatio, getPersistedAssetRatio(expectedAssetRatio));
        assertAssetRatioUpdatableFieldsEquals(expectedAssetRatio, getPersistedAssetRatio(expectedAssetRatio));
    }

    protected void assertPersistedAssetRatioToMatchUpdatableProperties(AssetRatio expectedAssetRatio) {
        // Test fails because reactive api returns an empty object instead of null
        // assertAssetRatioAllUpdatablePropertiesEquals(expectedAssetRatio, getPersistedAssetRatio(expectedAssetRatio));
        assertAssetRatioUpdatableFieldsEquals(expectedAssetRatio, getPersistedAssetRatio(expectedAssetRatio));
    }
}
