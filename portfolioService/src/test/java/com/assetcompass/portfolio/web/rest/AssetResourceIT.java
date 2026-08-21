package com.assetcompass.portfolio.web.rest;

import static com.assetcompass.portfolio.domain.AssetAsserts.*;
import static com.assetcompass.portfolio.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.assetcompass.portfolio.IntegrationTest;
import com.assetcompass.portfolio.domain.Asset;
import com.assetcompass.portfolio.repository.AssetRepository;
import com.assetcompass.portfolio.service.dto.AssetDTO;
import com.assetcompass.portfolio.service.mapper.AssetMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
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
 * Integration tests for the {@link AssetResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
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
    private MockMvc restAssetMockMvc;

    private Asset asset;

    private Asset insertedAsset;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Asset createEntity() {
        return new Asset().ticket(DEFAULT_TICKET).category(DEFAULT_CATEGORY).country(DEFAULT_COUNTRY).description(DEFAULT_DESCRIPTION);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Asset createUpdatedEntity() {
        return new Asset().ticket(UPDATED_TICKET).category(UPDATED_CATEGORY).country(UPDATED_COUNTRY).description(UPDATED_DESCRIPTION);
    }

    @BeforeEach
    void initTest() {
        asset = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedAsset != null) {
            assetRepository.delete(insertedAsset);
            insertedAsset = null;
        }
    }

    @Test
    @Transactional
    void createAsset() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Asset
        AssetDTO assetDTO = assetMapper.toDto(asset);
        var returnedAssetDTO = om.readValue(
            restAssetMockMvc
                .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(assetDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            AssetDTO.class
        );

        // Validate the Asset in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedAsset = assetMapper.toEntity(returnedAssetDTO);
        assertAssetUpdatableFieldsEquals(returnedAsset, getPersistedAsset(returnedAsset));

        insertedAsset = returnedAsset;
    }

    @Test
    @Transactional
    void createAssetWithExistingId() throws Exception {
        // Create the Asset with an existing ID
        insertedAsset = assetRepository.saveAndFlush(asset);
        AssetDTO assetDTO = assetMapper.toDto(asset);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAssetMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(assetDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Asset in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTicketIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        asset.setTicket(null);

        // Create the Asset, which fails.
        AssetDTO assetDTO = assetMapper.toDto(asset);

        restAssetMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(assetDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllAssets() throws Exception {
        // Initialize the database
        insertedAsset = assetRepository.saveAndFlush(asset);

        // Get all the assetList
        restAssetMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(asset.getId().toString())))
            .andExpect(jsonPath("$.[*].ticket").value(hasItem(DEFAULT_TICKET)))
            .andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY)))
            .andExpect(jsonPath("$.[*].country").value(hasItem(DEFAULT_COUNTRY)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));
    }

    @Test
    @Transactional
    void getAsset() throws Exception {
        // Initialize the database
        insertedAsset = assetRepository.saveAndFlush(asset);

        // Get the asset
        restAssetMockMvc
            .perform(get(ENTITY_API_URL_ID, asset.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(asset.getId().toString()))
            .andExpect(jsonPath("$.ticket").value(DEFAULT_TICKET))
            .andExpect(jsonPath("$.category").value(DEFAULT_CATEGORY))
            .andExpect(jsonPath("$.country").value(DEFAULT_COUNTRY))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION));
    }

    @Test
    @Transactional
    void getAssetsByIdFiltering() throws Exception {
        // Initialize the database
        insertedAsset = assetRepository.saveAndFlush(asset);

        UUID id = asset.getId();

        defaultAssetFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    @Transactional
    void getAllAssetsByTicketIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAsset = assetRepository.saveAndFlush(asset);

        // Get all the assetList where ticket equals to
        defaultAssetFiltering("ticket.equals=" + DEFAULT_TICKET, "ticket.equals=" + UPDATED_TICKET);
    }

    @Test
    @Transactional
    void getAllAssetsByTicketIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAsset = assetRepository.saveAndFlush(asset);

        // Get all the assetList where ticket in
        defaultAssetFiltering("ticket.in=" + DEFAULT_TICKET + "," + UPDATED_TICKET, "ticket.in=" + UPDATED_TICKET);
    }

    @Test
    @Transactional
    void getAllAssetsByTicketIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAsset = assetRepository.saveAndFlush(asset);

        // Get all the assetList where ticket is not null
        defaultAssetFiltering("ticket.specified=true", "ticket.specified=false");
    }

    @Test
    @Transactional
    void getAllAssetsByTicketContainsSomething() throws Exception {
        // Initialize the database
        insertedAsset = assetRepository.saveAndFlush(asset);

        // Get all the assetList where ticket contains
        defaultAssetFiltering("ticket.contains=" + DEFAULT_TICKET, "ticket.contains=" + UPDATED_TICKET);
    }

    @Test
    @Transactional
    void getAllAssetsByTicketNotContainsSomething() throws Exception {
        // Initialize the database
        insertedAsset = assetRepository.saveAndFlush(asset);

        // Get all the assetList where ticket does not contain
        defaultAssetFiltering("ticket.doesNotContain=" + UPDATED_TICKET, "ticket.doesNotContain=" + DEFAULT_TICKET);
    }

    @Test
    @Transactional
    void getAllAssetsByCategoryIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAsset = assetRepository.saveAndFlush(asset);

        // Get all the assetList where category equals to
        defaultAssetFiltering("category.equals=" + DEFAULT_CATEGORY, "category.equals=" + UPDATED_CATEGORY);
    }

    @Test
    @Transactional
    void getAllAssetsByCategoryIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAsset = assetRepository.saveAndFlush(asset);

        // Get all the assetList where category in
        defaultAssetFiltering("category.in=" + DEFAULT_CATEGORY + "," + UPDATED_CATEGORY, "category.in=" + UPDATED_CATEGORY);
    }

    @Test
    @Transactional
    void getAllAssetsByCategoryIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAsset = assetRepository.saveAndFlush(asset);

        // Get all the assetList where category is not null
        defaultAssetFiltering("category.specified=true", "category.specified=false");
    }

    @Test
    @Transactional
    void getAllAssetsByCategoryContainsSomething() throws Exception {
        // Initialize the database
        insertedAsset = assetRepository.saveAndFlush(asset);

        // Get all the assetList where category contains
        defaultAssetFiltering("category.contains=" + DEFAULT_CATEGORY, "category.contains=" + UPDATED_CATEGORY);
    }

    @Test
    @Transactional
    void getAllAssetsByCategoryNotContainsSomething() throws Exception {
        // Initialize the database
        insertedAsset = assetRepository.saveAndFlush(asset);

        // Get all the assetList where category does not contain
        defaultAssetFiltering("category.doesNotContain=" + UPDATED_CATEGORY, "category.doesNotContain=" + DEFAULT_CATEGORY);
    }

    @Test
    @Transactional
    void getAllAssetsByCountryIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAsset = assetRepository.saveAndFlush(asset);

        // Get all the assetList where country equals to
        defaultAssetFiltering("country.equals=" + DEFAULT_COUNTRY, "country.equals=" + UPDATED_COUNTRY);
    }

    @Test
    @Transactional
    void getAllAssetsByCountryIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAsset = assetRepository.saveAndFlush(asset);

        // Get all the assetList where country in
        defaultAssetFiltering("country.in=" + DEFAULT_COUNTRY + "," + UPDATED_COUNTRY, "country.in=" + UPDATED_COUNTRY);
    }

    @Test
    @Transactional
    void getAllAssetsByCountryIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAsset = assetRepository.saveAndFlush(asset);

        // Get all the assetList where country is not null
        defaultAssetFiltering("country.specified=true", "country.specified=false");
    }

    @Test
    @Transactional
    void getAllAssetsByCountryContainsSomething() throws Exception {
        // Initialize the database
        insertedAsset = assetRepository.saveAndFlush(asset);

        // Get all the assetList where country contains
        defaultAssetFiltering("country.contains=" + DEFAULT_COUNTRY, "country.contains=" + UPDATED_COUNTRY);
    }

    @Test
    @Transactional
    void getAllAssetsByCountryNotContainsSomething() throws Exception {
        // Initialize the database
        insertedAsset = assetRepository.saveAndFlush(asset);

        // Get all the assetList where country does not contain
        defaultAssetFiltering("country.doesNotContain=" + UPDATED_COUNTRY, "country.doesNotContain=" + DEFAULT_COUNTRY);
    }

    @Test
    @Transactional
    void getAllAssetsByDescriptionIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedAsset = assetRepository.saveAndFlush(asset);

        // Get all the assetList where description equals to
        defaultAssetFiltering("description.equals=" + DEFAULT_DESCRIPTION, "description.equals=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllAssetsByDescriptionIsInShouldWork() throws Exception {
        // Initialize the database
        insertedAsset = assetRepository.saveAndFlush(asset);

        // Get all the assetList where description in
        defaultAssetFiltering("description.in=" + DEFAULT_DESCRIPTION + "," + UPDATED_DESCRIPTION, "description.in=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllAssetsByDescriptionIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedAsset = assetRepository.saveAndFlush(asset);

        // Get all the assetList where description is not null
        defaultAssetFiltering("description.specified=true", "description.specified=false");
    }

    @Test
    @Transactional
    void getAllAssetsByDescriptionContainsSomething() throws Exception {
        // Initialize the database
        insertedAsset = assetRepository.saveAndFlush(asset);

        // Get all the assetList where description contains
        defaultAssetFiltering("description.contains=" + DEFAULT_DESCRIPTION, "description.contains=" + UPDATED_DESCRIPTION);
    }

    @Test
    @Transactional
    void getAllAssetsByDescriptionNotContainsSomething() throws Exception {
        // Initialize the database
        insertedAsset = assetRepository.saveAndFlush(asset);

        // Get all the assetList where description does not contain
        defaultAssetFiltering("description.doesNotContain=" + UPDATED_DESCRIPTION, "description.doesNotContain=" + DEFAULT_DESCRIPTION);
    }

    private void defaultAssetFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultAssetShouldBeFound(shouldBeFound);
        defaultAssetShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultAssetShouldBeFound(String filter) throws Exception {
        restAssetMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(asset.getId().toString())))
            .andExpect(jsonPath("$.[*].ticket").value(hasItem(DEFAULT_TICKET)))
            .andExpect(jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY)))
            .andExpect(jsonPath("$.[*].country").value(hasItem(DEFAULT_COUNTRY)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));

        // Check, that the count call also returns 1
        restAssetMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultAssetShouldNotBeFound(String filter) throws Exception {
        restAssetMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restAssetMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingAsset() throws Exception {
        // Get the asset
        restAssetMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingAsset() throws Exception {
        // Initialize the database
        insertedAsset = assetRepository.saveAndFlush(asset);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the asset
        Asset updatedAsset = assetRepository.findById(asset.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedAsset are not directly saved in db
        em.detach(updatedAsset);
        updatedAsset.ticket(UPDATED_TICKET).category(UPDATED_CATEGORY).country(UPDATED_COUNTRY).description(UPDATED_DESCRIPTION);
        AssetDTO assetDTO = assetMapper.toDto(updatedAsset);

        restAssetMockMvc
            .perform(
                put(ENTITY_API_URL_ID, assetDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(assetDTO))
            )
            .andExpect(status().isOk());

        // Validate the Asset in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedAssetToMatchAllProperties(updatedAsset);
    }

    @Test
    @Transactional
    void putNonExistingAsset() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        asset.setId(UUID.randomUUID());

        // Create the Asset
        AssetDTO assetDTO = assetMapper.toDto(asset);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAssetMockMvc
            .perform(
                put(ENTITY_API_URL_ID, assetDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(assetDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Asset in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchAsset() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        asset.setId(UUID.randomUUID());

        // Create the Asset
        AssetDTO assetDTO = assetMapper.toDto(asset);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAssetMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(assetDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Asset in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAsset() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        asset.setId(UUID.randomUUID());

        // Create the Asset
        AssetDTO assetDTO = assetMapper.toDto(asset);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAssetMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(assetDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Asset in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateAssetWithPatch() throws Exception {
        // Initialize the database
        insertedAsset = assetRepository.saveAndFlush(asset);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the asset using partial update
        Asset partialUpdatedAsset = new Asset();
        partialUpdatedAsset.setId(asset.getId());

        partialUpdatedAsset.country(UPDATED_COUNTRY);

        restAssetMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAsset.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAsset))
            )
            .andExpect(status().isOk());

        // Validate the Asset in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAssetUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedAsset, asset), getPersistedAsset(asset));
    }

    @Test
    @Transactional
    void fullUpdateAssetWithPatch() throws Exception {
        // Initialize the database
        insertedAsset = assetRepository.saveAndFlush(asset);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the asset using partial update
        Asset partialUpdatedAsset = new Asset();
        partialUpdatedAsset.setId(asset.getId());

        partialUpdatedAsset.ticket(UPDATED_TICKET).category(UPDATED_CATEGORY).country(UPDATED_COUNTRY).description(UPDATED_DESCRIPTION);

        restAssetMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAsset.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedAsset))
            )
            .andExpect(status().isOk());

        // Validate the Asset in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertAssetUpdatableFieldsEquals(partialUpdatedAsset, getPersistedAsset(partialUpdatedAsset));
    }

    @Test
    @Transactional
    void patchNonExistingAsset() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        asset.setId(UUID.randomUUID());

        // Create the Asset
        AssetDTO assetDTO = assetMapper.toDto(asset);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAssetMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, assetDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(assetDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Asset in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAsset() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        asset.setId(UUID.randomUUID());

        // Create the Asset
        AssetDTO assetDTO = assetMapper.toDto(asset);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAssetMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(assetDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Asset in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAsset() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        asset.setId(UUID.randomUUID());

        // Create the Asset
        AssetDTO assetDTO = assetMapper.toDto(asset);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAssetMockMvc
            .perform(patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(assetDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Asset in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteAsset() throws Exception {
        // Initialize the database
        insertedAsset = assetRepository.saveAndFlush(asset);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the asset
        restAssetMockMvc
            .perform(delete(ENTITY_API_URL_ID, asset.getId().toString()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return assetRepository.count();
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
        return assetRepository.findById(asset.getId()).orElseThrow();
    }

    protected void assertPersistedAssetToMatchAllProperties(Asset expectedAsset) {
        assertAssetAllPropertiesEquals(expectedAsset, getPersistedAsset(expectedAsset));
    }

    protected void assertPersistedAssetToMatchUpdatableProperties(Asset expectedAsset) {
        assertAssetAllUpdatablePropertiesEquals(expectedAsset, getPersistedAsset(expectedAsset));
    }
}
