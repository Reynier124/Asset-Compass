package com.assetcompass.portfolio.web.rest;

import static com.assetcompass.portfolio.domain.BrokerAsserts.*;
import static com.assetcompass.portfolio.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.assetcompass.portfolio.IntegrationTest;
import com.assetcompass.portfolio.domain.Broker;
import com.assetcompass.portfolio.repository.BrokerRepository;
import com.assetcompass.portfolio.service.dto.BrokerDTO;
import com.assetcompass.portfolio.service.mapper.BrokerMapper;
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
 * Integration tests for the {@link BrokerResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class BrokerResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/brokers";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private BrokerRepository brokerRepository;

    @Autowired
    private BrokerMapper brokerMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restBrokerMockMvc;

    private Broker broker;

    private Broker insertedBroker;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Broker createEntity() {
        return new Broker().name(DEFAULT_NAME);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Broker createUpdatedEntity() {
        return new Broker().name(UPDATED_NAME);
    }

    @BeforeEach
    void initTest() {
        broker = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedBroker != null) {
            brokerRepository.delete(insertedBroker);
            insertedBroker = null;
        }
    }

    @Test
    @Transactional
    void createBroker() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Broker
        BrokerDTO brokerDTO = brokerMapper.toDto(broker);
        var returnedBrokerDTO = om.readValue(
            restBrokerMockMvc
                .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(brokerDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            BrokerDTO.class
        );

        // Validate the Broker in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedBroker = brokerMapper.toEntity(returnedBrokerDTO);
        assertBrokerUpdatableFieldsEquals(returnedBroker, getPersistedBroker(returnedBroker));

        insertedBroker = returnedBroker;
    }

    @Test
    @Transactional
    void createBrokerWithExistingId() throws Exception {
        // Create the Broker with an existing ID
        insertedBroker = brokerRepository.saveAndFlush(broker);
        BrokerDTO brokerDTO = brokerMapper.toDto(broker);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restBrokerMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(brokerDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Broker in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        broker.setName(null);

        // Create the Broker, which fails.
        BrokerDTO brokerDTO = brokerMapper.toDto(broker);

        restBrokerMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(brokerDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllBrokers() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        // Get all the brokerList
        restBrokerMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(broker.getId().toString())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));
    }

    @Test
    @Transactional
    void getBroker() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        // Get the broker
        restBrokerMockMvc
            .perform(get(ENTITY_API_URL_ID, broker.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(broker.getId().toString()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME));
    }

    @Test
    @Transactional
    void getBrokersByIdFiltering() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        UUID id = broker.getId();

        defaultBrokerFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    @Transactional
    void getAllBrokersByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        // Get all the brokerList where name equals to
        defaultBrokerFiltering("name.equals=" + DEFAULT_NAME, "name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllBrokersByNameIsInShouldWork() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        // Get all the brokerList where name in
        defaultBrokerFiltering("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME, "name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllBrokersByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        // Get all the brokerList where name is not null
        defaultBrokerFiltering("name.specified=true", "name.specified=false");
    }

    @Test
    @Transactional
    void getAllBrokersByNameContainsSomething() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        // Get all the brokerList where name contains
        defaultBrokerFiltering("name.contains=" + DEFAULT_NAME, "name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllBrokersByNameNotContainsSomething() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        // Get all the brokerList where name does not contain
        defaultBrokerFiltering("name.doesNotContain=" + UPDATED_NAME, "name.doesNotContain=" + DEFAULT_NAME);
    }

    private void defaultBrokerFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultBrokerShouldBeFound(shouldBeFound);
        defaultBrokerShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultBrokerShouldBeFound(String filter) throws Exception {
        restBrokerMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(broker.getId().toString())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));

        // Check, that the count call also returns 1
        restBrokerMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultBrokerShouldNotBeFound(String filter) throws Exception {
        restBrokerMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restBrokerMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingBroker() throws Exception {
        // Get the broker
        restBrokerMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingBroker() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the broker
        Broker updatedBroker = brokerRepository.findById(broker.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedBroker are not directly saved in db
        em.detach(updatedBroker);
        updatedBroker.name(UPDATED_NAME);
        BrokerDTO brokerDTO = brokerMapper.toDto(updatedBroker);

        restBrokerMockMvc
            .perform(
                put(ENTITY_API_URL_ID, brokerDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(brokerDTO))
            )
            .andExpect(status().isOk());

        // Validate the Broker in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedBrokerToMatchAllProperties(updatedBroker);
    }

    @Test
    @Transactional
    void putNonExistingBroker() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        broker.setId(UUID.randomUUID());

        // Create the Broker
        BrokerDTO brokerDTO = brokerMapper.toDto(broker);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBrokerMockMvc
            .perform(
                put(ENTITY_API_URL_ID, brokerDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(brokerDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Broker in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchBroker() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        broker.setId(UUID.randomUUID());

        // Create the Broker
        BrokerDTO brokerDTO = brokerMapper.toDto(broker);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBrokerMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(brokerDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Broker in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamBroker() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        broker.setId(UUID.randomUUID());

        // Create the Broker
        BrokerDTO brokerDTO = brokerMapper.toDto(broker);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBrokerMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(brokerDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Broker in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateBrokerWithPatch() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the broker using partial update
        Broker partialUpdatedBroker = new Broker();
        partialUpdatedBroker.setId(broker.getId());

        restBrokerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBroker.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedBroker))
            )
            .andExpect(status().isOk());

        // Validate the Broker in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBrokerUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedBroker, broker), getPersistedBroker(broker));
    }

    @Test
    @Transactional
    void fullUpdateBrokerWithPatch() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the broker using partial update
        Broker partialUpdatedBroker = new Broker();
        partialUpdatedBroker.setId(broker.getId());

        partialUpdatedBroker.name(UPDATED_NAME);

        restBrokerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBroker.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedBroker))
            )
            .andExpect(status().isOk());

        // Validate the Broker in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBrokerUpdatableFieldsEquals(partialUpdatedBroker, getPersistedBroker(partialUpdatedBroker));
    }

    @Test
    @Transactional
    void patchNonExistingBroker() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        broker.setId(UUID.randomUUID());

        // Create the Broker
        BrokerDTO brokerDTO = brokerMapper.toDto(broker);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBrokerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, brokerDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(brokerDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Broker in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchBroker() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        broker.setId(UUID.randomUUID());

        // Create the Broker
        BrokerDTO brokerDTO = brokerMapper.toDto(broker);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBrokerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(brokerDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Broker in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamBroker() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        broker.setId(UUID.randomUUID());

        // Create the Broker
        BrokerDTO brokerDTO = brokerMapper.toDto(broker);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBrokerMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(brokerDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Broker in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteBroker() throws Exception {
        // Initialize the database
        insertedBroker = brokerRepository.saveAndFlush(broker);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the broker
        restBrokerMockMvc
            .perform(delete(ENTITY_API_URL_ID, broker.getId().toString()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return brokerRepository.count();
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

    protected Broker getPersistedBroker(Broker broker) {
        return brokerRepository.findById(broker.getId()).orElseThrow();
    }

    protected void assertPersistedBrokerToMatchAllProperties(Broker expectedBroker) {
        assertBrokerAllPropertiesEquals(expectedBroker, getPersistedBroker(expectedBroker));
    }

    protected void assertPersistedBrokerToMatchUpdatableProperties(Broker expectedBroker) {
        assertBrokerAllUpdatablePropertiesEquals(expectedBroker, getPersistedBroker(expectedBroker));
    }
}
