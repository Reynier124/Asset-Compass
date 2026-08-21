package com.assetcompass.gateway.web.rest;

import static com.assetcompass.gateway.domain.AssetAsserts.*;
import static com.assetcompass.gateway.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.assetcompass.gateway.IntegrationTest;
import com.assetcompass.gateway.domain.Asset;
import com.assetcompass.gateway.repository.AssetRepository;
import com.assetcompass.gateway.repository.EntityManager;
import com.assetcompass.gateway.service.dto.AssetDTO;
import com.assetcompass.gateway.service.mapper.AssetMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Integration tests for the {@link AssetResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class AssetResourceIT {

    private static final String DEFAULT_TICKET = "AAAAAAAAAA";
    private static final String UPDATED_TICKET = "BBBBBBBBBB";

    private static final String DEFAULT_CATEGORY = "AAAAAAAAAA";
    private static final String UPDATED_CATEGORY = "BBBBBBBBBB";

    private static final String DEFAULT_COUNTRY = "AAAAAAAAAA";
    private static final String UPDATED_COUNTRY = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/assets";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetMapper assetMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Asset asset;

    private Asset insertedAsset;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Asset createEntity() {
        return new Asset()
            .id(UUID.randomUUID())
            .ticket(DEFAULT_TICKET)
            .category(DEFAULT_CATEGORY)
            .country(DEFAULT_COUNTRY)
            .description(DEFAULT_DESCRIPTION);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Asset createUpdatedEntity() {
        return new Asset()
            .id(UUID.randomUUID())
            .ticket(UPDATED_TICKET)
            .category(UPDATED_CATEGORY)
            .country(UPDATED_COUNTRY)
            .description(UPDATED_DESCRIPTION);
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Asset.class).block();
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
        asset = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedAsset != null) {
            assetRepository.delete(insertedAsset).block();
            insertedAsset = null;
        }
        deleteEntities(em);
    }

    @Test
    void createAsset() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        asset.setId(null);
        // Create the Asset
        AssetDTO assetDTO = assetMapper.toDto(asset);
        var returnedAssetDTO = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(assetDTO))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(AssetDTO.class)
            .returnResult()
            .getResponseBody();

        // Validate the Asset in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedAsset = assetMapper.toEntity(returnedAssetDTO);
        assertAssetUpdatableFieldsEquals(returnedAsset, getPersistedAsset(returnedAsset));

        insertedAsset = returnedAsset;
    }

    @Test
    void createAssetWithExistingId() throws Exception {
        // Create the Asset with an existing ID
        insertedAsset = assetRepository.save(asset).block();
        AssetDTO assetDTO = assetMapper.toDto(asset);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(assetDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Asset in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkTicketIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        asset.setTicket(null);

        // Create the Asset, which fails.
        AssetDTO assetDTO = assetMapper.toDto(asset);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(assetDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllAssets() {
        // Initialize the database
        asset.setId(UUID.randomUUID());
        insertedAsset = assetRepository.save(asset).block();

        // Get all the assetList
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
            .value(hasItem(asset.getId().toString()))
            .jsonPath("$.[*].ticket")
            .value(hasItem(DEFAULT_TICKET))
            .jsonPath("$.[*].category")
            .value(hasItem(DEFAULT_CATEGORY))
            .jsonPath("$.[*].country")
            .value(hasItem(DEFAULT_COUNTRY))
            .jsonPath("$.[*].description")
            .value(hasItem(DEFAULT_DESCRIPTION));
    }

    @Test
    void getAsset() {
        // Initialize the database
        asset.setId(UUID.randomUUID());
        insertedAsset = assetRepository.save(asset).block();

        // Get the asset
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, asset.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(asset.getId().toString()))
            .jsonPath("$.ticket")
            .value(is(DEFAULT_TICKET))
            .jsonPath("$.category")
            .value(is(DEFAULT_CATEGORY))
            .jsonPath("$.country")
            .value(is(DEFAULT_COUNTRY))
            .jsonPath("$.description")
            .value(is(DEFAULT_DESCRIPTION));
    }

    @Test
    void getAssetsByIdFiltering() {
        // Initialize the database
        insertedAsset = assetRepository.save(asset).block();

        UUID id = asset.getId();

        defaultAssetFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    void getAllAssetsByTicketIsEqualToSomething() {
        // Initialize the database
        insertedAsset = assetRepository.save(asset).block();

        // Get all the assetList where ticket equals to
        defaultAssetFiltering("ticket.equals=" + DEFAULT_TICKET, "ticket.equals=" + UPDATED_TICKET);
    }

    @Test
    void getAllAssetsByTicketIsInShouldWork() {
        // Initialize the database
        insertedAsset = assetRepository.save(asset).block();

        // Get all the assetList where ticket in
        defaultAssetFiltering("ticket.in=" + DEFAULT_TICKET + "," + UPDATED_TICKET, "ticket.in=" + UPDATED_TICKET);
    }

    @Test
    void getAllAssetsByTicketIsNullOrNotNull() {
        // Initialize the database
        insertedAsset = assetRepository.save(asset).block();

        // Get all the assetList where ticket is not null
        defaultAssetFiltering("ticket.specified=true", "ticket.specified=false");
    }

    @Test
    void getAllAssetsByTicketContainsSomething() {
        // Initialize the database
        insertedAsset = assetRepository.save(asset).block();

        // Get all the assetList where ticket contains
        defaultAssetFiltering("ticket.contains=" + DEFAULT_TICKET, "ticket.contains=" + UPDATED_TICKET);
    }

    @Test
    void getAllAssetsByTicketNotContainsSomething() {
        // Initialize the database
        insertedAsset = assetRepository.save(asset).block();

        // Get all the assetList where ticket does not contain
        defaultAssetFiltering("ticket.doesNotContain=" + UPDATED_TICKET, "ticket.doesNotContain=" + DEFAULT_TICKET);
    }

    @Test
    void getAllAssetsByCategoryIsEqualToSomething() {
        // Initialize the database
        insertedAsset = assetRepository.save(asset).block();

        // Get all the assetList where category equals to
        defaultAssetFiltering("category.equals=" + DEFAULT_CATEGORY, "category.equals=" + UPDATED_CATEGORY);
    }

    @Test
    void getAllAssetsByCategoryIsInShouldWork() {
        // Initialize the database
        insertedAsset = assetRepository.save(asset).block();

        // Get all the assetList where category in
        defaultAssetFiltering("category.in=" + DEFAULT_CATEGORY + "," + UPDATED_CATEGORY, "category.in=" + UPDATED_CATEGORY);
    }

    @Test
    void getAllAssetsByCategoryIsNullOrNotNull() {
        // Initialize the database
        insertedAsset = assetRepository.save(asset).block();

        // Get all the assetList where category is not null
        defaultAssetFiltering("category.specified=true", "category.specified=false");
    }

    @Test
    void getAllAssetsByCategoryContainsSomething() {
        // Initialize the database
        insertedAsset = assetRepository.save(asset).block();

        // Get all the assetList where category contains
        defaultAssetFiltering("category.contains=" + DEFAULT_CATEGORY, "category.contains=" + UPDATED_CATEGORY);
    }

    @Test
    void getAllAssetsByCategoryNotContainsSomething() {
        // Initialize the database
        insertedAsset = assetRepository.save(asset).block();

        // Get all the assetList where category does not contain
        defaultAssetFiltering("category.doesNotContain=" + UPDATED_CATEGORY, "category.doesNotContain=" + DEFAULT_CATEGORY);
    }

    @Test
    void getAllAssetsByCountryIsEqualToSomething() {
        // Initialize the database
        insertedAsset = assetRepository.save(asset).block();

        // Get all the assetList where country equals to
        defaultAssetFiltering("country.equals=" + DEFAULT_COUNTRY, "country.equals=" + UPDATED_COUNTRY);
    }

    @Test
    void getAllAssetsByCountryIsInShouldWork() {
        // Initialize the database
        insertedAsset = assetRepository.save(asset).block();

        // Get all the assetList where country in
        defaultAssetFiltering("country.in=" + DEFAULT_COUNTRY + "," + UPDATED_COUNTRY, "country.in=" + UPDATED_COUNTRY);
    }

    @Test
    void getAllAssetsByCountryIsNullOrNotNull() {
        // Initialize the database
        insertedAsset = assetRepository.save(asset).block();

        // Get all the assetList where country is not null
        defaultAssetFiltering("country.specified=true", "country.specified=false");
    }

    @Test
    void getAllAssetsByCountryContainsSomething() {
        // Initialize the database
        insertedAsset = assetRepository.save(asset).block();

        // Get all the assetList where country contains
        defaultAssetFiltering("country.contains=" + DEFAULT_COUNTRY, "country.contains=" + UPDATED_COUNTRY);
    }

    @Test
    void getAllAssetsByCountryNotContainsSomething() {
        // Initialize the database
        insertedAsset = assetRepository.save(asset).block();

        // Get all the assetList where country does not contain
        defaultAssetFiltering("country.doesNotContain=" + UPDATED_COUNTRY, "country.doesNotContain=" + DEFAULT_COUNTRY);
    }

    @Test
    void getAllAssetsByDescriptionIsEqualToSomething() {
        // Initialize the database
        insertedAsset = assetRepository.save(asset).block();

        // Get all the assetList where description equals to
        defaultAssetFiltering("description.equals=" + DEFAULT_DESCRIPTION, "description.equals=" + UPDATED_DESCRIPTION);
    }

    @Test
    void getAllAssetsByDescriptionIsInShouldWork() {
        // Initialize the database
        insertedAsset = assetRepository.save(asset).block();

        // Get all the assetList where description in
        defaultAssetFiltering("description.in=" + DEFAULT_DESCRIPTION + "," + UPDATED_DESCRIPTION, "description.in=" + UPDATED_DESCRIPTION);
    }

    @Test
    void getAllAssetsByDescriptionIsNullOrNotNull() {
        // Initialize the database
        insertedAsset = assetRepository.save(asset).block();

        // Get all the assetList where description is not null
        defaultAssetFiltering("description.specified=true", "description.specified=false");
    }

    @Test
    void getAllAssetsByDescriptionContainsSomething() {
        // Initialize the database
        insertedAsset = assetRepository.save(asset).block();

        // Get all the assetList where description contains
        defaultAssetFiltering("description.contains=" + DEFAULT_DESCRIPTION, "description.contains=" + UPDATED_DESCRIPTION);
    }

    @Test
    void getAllAssetsByDescriptionNotContainsSomething() {
        // Initialize the database
        insertedAsset = assetRepository.save(asset).block();

        // Get all the assetList where description does not contain
        defaultAssetFiltering("description.doesNotContain=" + UPDATED_DESCRIPTION, "description.doesNotContain=" + DEFAULT_DESCRIPTION);
    }

    private void defaultAssetFiltering(String shouldBeFound, String shouldNotBeFound) {
        defaultAssetShouldBeFound(shouldBeFound);
        defaultAssetShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultAssetShouldBeFound(String filter) {
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
            .value(hasItem(asset.getId().toString()))
            .jsonPath("$.[*].ticket")
            .value(hasItem(DEFAULT_TICKET))

            .jsonPath("$.[*].category")
            .value(hasItem(DEFAULT_CATEGORY))

            .jsonPath("$.[*].country")
            .value(hasItem(DEFAULT_COUNTRY))

            .jsonPath("$.[*].description")
            .value(hasItem(DEFAULT_DESCRIPTION));

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
    private void defaultAssetShouldNotBeFound(String filter) {
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
    void getNonExistingAsset() {
        // Get the asset
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingAsset() throws Exception {
        // Initialize the database
        asset.setId(UUID.randomUUID());
        insertedAsset = assetRepository.save(asset).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the asset
        Asset updatedAsset = assetRepository.findById(asset.getId()).block();
        updatedAsset.ticket(UPDATED_TICKET).category(UPDATED_CATEGORY).country(UPDATED_COUNTRY).description(UPDATED_DESCRIPTION);
        AssetDTO assetDTO = assetMapper.toDto(updatedAsset);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, assetDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(assetDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Asset in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedAssetToMatchAllProperties(updatedAsset);
    }

    @Test
    void putNonExistingAsset() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        asset.setId(UUID.randomUUID());

        // Create the Asset
        AssetDTO assetDTO = assetMapper.toDto(asset);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, assetDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(assetDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Asset in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchAsset() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        asset.setId(UUID.randomUUID());

        // Create the Asset
        AssetDTO assetDTO = assetMapper.toDto(asset);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(assetDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Asset in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamAsset() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        asset.setId(UUID.randomUUID());

        // Create the Asset
        AssetDTO assetDTO = assetMapper.toDto(asset);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(assetDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Asset in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateAssetWithPatch() throws Exception {
        // Initialize the database
        asset.setId(UUID.randomUUID());
        insertedAsset = assetRepository.save(asset).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the asset using partial update
        Asset partialUpdatedAsset = new Asset();
        partialUpdatedAsset.setId(asset.getId());

        partialUpdatedAsset.ticket(UPDATED_TICKET).category(UPDATED_CATEGORY);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedAsset.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedAsset))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Asset in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAssetUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedAsset, asset), getPersistedAsset(asset));
    }

    @Test
    void fullUpdateAssetWithPatch() throws Exception {
        // Initialize the database
        asset.setId(UUID.randomUUID());
        insertedAsset = assetRepository.save(asset).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the asset using partial update
        Asset partialUpdatedAsset = new Asset();
        partialUpdatedAsset.setId(asset.getId());

        partialUpdatedAsset.ticket(UPDATED_TICKET).category(UPDATED_CATEGORY).country(UPDATED_COUNTRY).description(UPDATED_DESCRIPTION);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedAsset.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedAsset))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Asset in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAssetUpdatableFieldsEquals(partialUpdatedAsset, getPersistedAsset(partialUpdatedAsset));
    }

    @Test
    void patchNonExistingAsset() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        asset.setId(UUID.randomUUID());

        // Create the Asset
        AssetDTO assetDTO = assetMapper.toDto(asset);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, assetDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(assetDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Asset in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchAsset() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        asset.setId(UUID.randomUUID());

        // Create the Asset
        AssetDTO assetDTO = assetMapper.toDto(asset);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(assetDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Asset in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamAsset() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        asset.setId(UUID.randomUUID());

        // Create the Asset
        AssetDTO assetDTO = assetMapper.toDto(asset);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(assetDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Asset in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteAsset() {
        // Initialize the database
        asset.setId(UUID.randomUUID());
        insertedAsset = assetRepository.save(asset).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the asset
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, asset.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return assetRepository.count().block();
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

    protected Asset getPersistedAsset(Asset asset) {
        return assetRepository.findById(asset.getId()).block();
    }

    protected void assertPersistedAssetToMatchAllProperties(Asset expectedAsset) {
        // Test fails because reactive api returns an empty object instead of null
        // assertAssetAllPropertiesEquals(expectedAsset, getPersistedAsset(expectedAsset));
        assertAssetUpdatableFieldsEquals(expectedAsset, getPersistedAsset(expectedAsset));
    }

    protected void assertPersistedAssetToMatchUpdatableProperties(Asset expectedAsset) {
        // Test fails because reactive api returns an empty object instead of null
        // assertAssetAllUpdatablePropertiesEquals(expectedAsset, getPersistedAsset(expectedAsset));
        assertAssetUpdatableFieldsEquals(expectedAsset, getPersistedAsset(expectedAsset));
    }
}
