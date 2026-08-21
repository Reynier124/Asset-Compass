package com.assetcompass.gateway.web.rest;

import static com.assetcompass.gateway.domain.BrokerAsserts.*;
import static com.assetcompass.gateway.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.assetcompass.gateway.IntegrationTest;
import com.assetcompass.gateway.domain.Broker;
import com.assetcompass.gateway.repository.BrokerRepository;
import com.assetcompass.gateway.repository.EntityManager;
import com.assetcompass.gateway.service.dto.BrokerDTO;
import com.assetcompass.gateway.service.mapper.BrokerMapper;
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
 * Integration tests for the {@link BrokerResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
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
    private WebTestClient webTestClient;

    private Broker broker;

    private Broker insertedBroker;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Broker createEntity() {
        return new Broker().id(UUID.randomUUID()).name(DEFAULT_NAME);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Broker createUpdatedEntity() {
        return new Broker().id(UUID.randomUUID()).name(UPDATED_NAME);
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Broker.class).block();
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
        broker = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedBroker != null) {
            brokerRepository.delete(insertedBroker).block();
            insertedBroker = null;
        }
        deleteEntities(em);
    }

    @Test
    void createBroker() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        broker.setId(null);
        // Create the Broker
        BrokerDTO brokerDTO = brokerMapper.toDto(broker);
        var returnedBrokerDTO = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(brokerDTO))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(BrokerDTO.class)
            .returnResult()
            .getResponseBody();

        // Validate the Broker in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedBroker = brokerMapper.toEntity(returnedBrokerDTO);
        assertBrokerUpdatableFieldsEquals(returnedBroker, getPersistedBroker(returnedBroker));

        insertedBroker = returnedBroker;
    }

    @Test
    void createBrokerWithExistingId() throws Exception {
        // Create the Broker with an existing ID
        insertedBroker = brokerRepository.save(broker).block();
        BrokerDTO brokerDTO = brokerMapper.toDto(broker);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(brokerDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Broker in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        broker.setName(null);

        // Create the Broker, which fails.
        BrokerDTO brokerDTO = brokerMapper.toDto(broker);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(brokerDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllBrokers() {
        // Initialize the database
        broker.setId(UUID.randomUUID());
        insertedBroker = brokerRepository.save(broker).block();

        // Get all the brokerList
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
            .value(hasItem(broker.getId().toString()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME));
    }

    @Test
    void getBroker() {
        // Initialize the database
        broker.setId(UUID.randomUUID());
        insertedBroker = brokerRepository.save(broker).block();

        // Get the broker
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, broker.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(broker.getId().toString()))
            .jsonPath("$.name")
            .value(is(DEFAULT_NAME));
    }

    @Test
    void getBrokersByIdFiltering() {
        // Initialize the database
        insertedBroker = brokerRepository.save(broker).block();

        UUID id = broker.getId();

        defaultBrokerFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    void getAllBrokersByNameIsEqualToSomething() {
        // Initialize the database
        insertedBroker = brokerRepository.save(broker).block();

        // Get all the brokerList where name equals to
        defaultBrokerFiltering("name.equals=" + DEFAULT_NAME, "name.equals=" + UPDATED_NAME);
    }

    @Test
    void getAllBrokersByNameIsInShouldWork() {
        // Initialize the database
        insertedBroker = brokerRepository.save(broker).block();

        // Get all the brokerList where name in
        defaultBrokerFiltering("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME, "name.in=" + UPDATED_NAME);
    }

    @Test
    void getAllBrokersByNameIsNullOrNotNull() {
        // Initialize the database
        insertedBroker = brokerRepository.save(broker).block();

        // Get all the brokerList where name is not null
        defaultBrokerFiltering("name.specified=true", "name.specified=false");
    }

    @Test
    void getAllBrokersByNameContainsSomething() {
        // Initialize the database
        insertedBroker = brokerRepository.save(broker).block();

        // Get all the brokerList where name contains
        defaultBrokerFiltering("name.contains=" + DEFAULT_NAME, "name.contains=" + UPDATED_NAME);
    }

    @Test
    void getAllBrokersByNameNotContainsSomething() {
        // Initialize the database
        insertedBroker = brokerRepository.save(broker).block();

        // Get all the brokerList where name does not contain
        defaultBrokerFiltering("name.doesNotContain=" + UPDATED_NAME, "name.doesNotContain=" + DEFAULT_NAME);
    }

    private void defaultBrokerFiltering(String shouldBeFound, String shouldNotBeFound) {
        defaultBrokerShouldBeFound(shouldBeFound);
        defaultBrokerShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultBrokerShouldBeFound(String filter) {
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
            .value(hasItem(broker.getId().toString()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME));

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
    private void defaultBrokerShouldNotBeFound(String filter) {
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
    void getNonExistingBroker() {
        // Get the broker
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingBroker() throws Exception {
        // Initialize the database
        broker.setId(UUID.randomUUID());
        insertedBroker = brokerRepository.save(broker).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the broker
        Broker updatedBroker = brokerRepository.findById(broker.getId()).block();
        updatedBroker.name(UPDATED_NAME);
        BrokerDTO brokerDTO = brokerMapper.toDto(updatedBroker);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, brokerDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(brokerDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Broker in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedBrokerToMatchAllProperties(updatedBroker);
    }

    @Test
    void putNonExistingBroker() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        broker.setId(UUID.randomUUID());

        // Create the Broker
        BrokerDTO brokerDTO = brokerMapper.toDto(broker);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, brokerDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(brokerDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Broker in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchBroker() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        broker.setId(UUID.randomUUID());

        // Create the Broker
        BrokerDTO brokerDTO = brokerMapper.toDto(broker);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(brokerDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Broker in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamBroker() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        broker.setId(UUID.randomUUID());

        // Create the Broker
        BrokerDTO brokerDTO = brokerMapper.toDto(broker);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(brokerDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Broker in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateBrokerWithPatch() throws Exception {
        // Initialize the database
        broker.setId(UUID.randomUUID());
        insertedBroker = brokerRepository.save(broker).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the broker using partial update
        Broker partialUpdatedBroker = new Broker();
        partialUpdatedBroker.setId(broker.getId());

        partialUpdatedBroker.name(UPDATED_NAME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedBroker.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedBroker))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Broker in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBrokerUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedBroker, broker), getPersistedBroker(broker));
    }

    @Test
    void fullUpdateBrokerWithPatch() throws Exception {
        // Initialize the database
        broker.setId(UUID.randomUUID());
        insertedBroker = brokerRepository.save(broker).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the broker using partial update
        Broker partialUpdatedBroker = new Broker();
        partialUpdatedBroker.setId(broker.getId());

        partialUpdatedBroker.name(UPDATED_NAME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedBroker.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedBroker))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Broker in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBrokerUpdatableFieldsEquals(partialUpdatedBroker, getPersistedBroker(partialUpdatedBroker));
    }

    @Test
    void patchNonExistingBroker() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        broker.setId(UUID.randomUUID());

        // Create the Broker
        BrokerDTO brokerDTO = brokerMapper.toDto(broker);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, brokerDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(brokerDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Broker in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchBroker() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        broker.setId(UUID.randomUUID());

        // Create the Broker
        BrokerDTO brokerDTO = brokerMapper.toDto(broker);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(brokerDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Broker in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamBroker() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        broker.setId(UUID.randomUUID());

        // Create the Broker
        BrokerDTO brokerDTO = brokerMapper.toDto(broker);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(brokerDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Broker in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteBroker() {
        // Initialize the database
        broker.setId(UUID.randomUUID());
        insertedBroker = brokerRepository.save(broker).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the broker
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, broker.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return brokerRepository.count().block();
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
        return brokerRepository.findById(broker.getId()).block();
    }

    protected void assertPersistedBrokerToMatchAllProperties(Broker expectedBroker) {
        // Test fails because reactive api returns an empty object instead of null
        // assertBrokerAllPropertiesEquals(expectedBroker, getPersistedBroker(expectedBroker));
        assertBrokerUpdatableFieldsEquals(expectedBroker, getPersistedBroker(expectedBroker));
    }

    protected void assertPersistedBrokerToMatchUpdatableProperties(Broker expectedBroker) {
        // Test fails because reactive api returns an empty object instead of null
        // assertBrokerAllUpdatablePropertiesEquals(expectedBroker, getPersistedBroker(expectedBroker));
        assertBrokerUpdatableFieldsEquals(expectedBroker, getPersistedBroker(expectedBroker));
    }
}
