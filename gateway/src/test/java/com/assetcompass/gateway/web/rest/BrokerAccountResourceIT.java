package com.assetcompass.gateway.web.rest;

import static com.assetcompass.gateway.domain.BrokerAccountAsserts.*;
import static com.assetcompass.gateway.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.assetcompass.gateway.IntegrationTest;
import com.assetcompass.gateway.domain.Broker;
import com.assetcompass.gateway.domain.BrokerAccount;
import com.assetcompass.gateway.repository.BrokerAccountRepository;
import com.assetcompass.gateway.repository.BrokerRepository;
import com.assetcompass.gateway.repository.EntityManager;
import com.assetcompass.gateway.service.dto.BrokerAccountDTO;
import com.assetcompass.gateway.service.mapper.BrokerAccountMapper;
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
 * Integration tests for the {@link BrokerAccountResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class BrokerAccountResourceIT {

    private static final String DEFAULT_EXTERNAL_ACCOUNT_ID = "AAAAAAAAAA";
    private static final String UPDATED_EXTERNAL_ACCOUNT_ID = "BBBBBBBBBB";

    private static final String DEFAULT_DISPLAY_NAME = "AAAAAAAAAA";
    private static final String UPDATED_DISPLAY_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/broker-accounts";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private BrokerAccountRepository brokerAccountRepository;

    @Autowired
    private BrokerAccountMapper brokerAccountMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private BrokerAccount brokerAccount;

    private BrokerAccount insertedBrokerAccount;

    @Autowired
    private BrokerRepository brokerRepository;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BrokerAccount createEntity(EntityManager em) {
        BrokerAccount brokerAccount = new BrokerAccount()
            .id(UUID.randomUUID())
            .externalAccountId(DEFAULT_EXTERNAL_ACCOUNT_ID)
            .displayName(DEFAULT_DISPLAY_NAME);
        // Add required entity
        Broker broker;
        broker = em.insert(BrokerResourceIT.createEntity()).block();
        brokerAccount.setBroker(broker);
        return brokerAccount;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BrokerAccount createUpdatedEntity(EntityManager em) {
        BrokerAccount updatedBrokerAccount = new BrokerAccount()
            .id(UUID.randomUUID())
            .externalAccountId(UPDATED_EXTERNAL_ACCOUNT_ID)
            .displayName(UPDATED_DISPLAY_NAME);
        // Add required entity
        Broker broker;
        broker = em.insert(BrokerResourceIT.createUpdatedEntity()).block();
        updatedBrokerAccount.setBroker(broker);
        return updatedBrokerAccount;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(BrokerAccount.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
        BrokerResourceIT.deleteEntities(em);
    }

    @BeforeEach
    void setupCsrf() {
        webTestClient = webTestClient.mutateWith(csrf());
    }

    @BeforeEach
    void initTest() {
        brokerAccount = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedBrokerAccount != null) {
            brokerAccountRepository.delete(insertedBrokerAccount).block();
            insertedBrokerAccount = null;
        }
        deleteEntities(em);
    }

    @Test
    void createBrokerAccount() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        brokerAccount.setId(null);
        // Create the BrokerAccount
        BrokerAccountDTO brokerAccountDTO = brokerAccountMapper.toDto(brokerAccount);
        var returnedBrokerAccountDTO = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(brokerAccountDTO))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(BrokerAccountDTO.class)
            .returnResult()
            .getResponseBody();

        // Validate the BrokerAccount in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedBrokerAccount = brokerAccountMapper.toEntity(returnedBrokerAccountDTO);
        assertBrokerAccountUpdatableFieldsEquals(returnedBrokerAccount, getPersistedBrokerAccount(returnedBrokerAccount));

        insertedBrokerAccount = returnedBrokerAccount;
    }

    @Test
    void createBrokerAccountWithExistingId() throws Exception {
        // Create the BrokerAccount with an existing ID
        insertedBrokerAccount = brokerAccountRepository.save(brokerAccount).block();
        BrokerAccountDTO brokerAccountDTO = brokerAccountMapper.toDto(brokerAccount);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(brokerAccountDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the BrokerAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkExternalAccountIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        brokerAccount.setExternalAccountId(null);

        // Create the BrokerAccount, which fails.
        BrokerAccountDTO brokerAccountDTO = brokerAccountMapper.toDto(brokerAccount);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(brokerAccountDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllBrokerAccounts() {
        // Initialize the database
        brokerAccount.setId(UUID.randomUUID());
        insertedBrokerAccount = brokerAccountRepository.save(brokerAccount).block();

        // Get all the brokerAccountList
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
            .value(hasItem(brokerAccount.getId().toString()))
            .jsonPath("$.[*].externalAccountId")
            .value(hasItem(DEFAULT_EXTERNAL_ACCOUNT_ID))
            .jsonPath("$.[*].displayName")
            .value(hasItem(DEFAULT_DISPLAY_NAME));
    }

    @Test
    void getBrokerAccount() {
        // Initialize the database
        brokerAccount.setId(UUID.randomUUID());
        insertedBrokerAccount = brokerAccountRepository.save(brokerAccount).block();

        // Get the brokerAccount
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, brokerAccount.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(brokerAccount.getId().toString()))
            .jsonPath("$.externalAccountId")
            .value(is(DEFAULT_EXTERNAL_ACCOUNT_ID))
            .jsonPath("$.displayName")
            .value(is(DEFAULT_DISPLAY_NAME));
    }

    @Test
    void getBrokerAccountsByIdFiltering() {
        // Initialize the database
        insertedBrokerAccount = brokerAccountRepository.save(brokerAccount).block();

        UUID id = brokerAccount.getId();

        defaultBrokerAccountFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    void getAllBrokerAccountsByExternalAccountIdIsEqualToSomething() {
        // Initialize the database
        insertedBrokerAccount = brokerAccountRepository.save(brokerAccount).block();

        // Get all the brokerAccountList where externalAccountId equals to
        defaultBrokerAccountFiltering(
            "externalAccountId.equals=" + DEFAULT_EXTERNAL_ACCOUNT_ID,
            "externalAccountId.equals=" + UPDATED_EXTERNAL_ACCOUNT_ID
        );
    }

    @Test
    void getAllBrokerAccountsByExternalAccountIdIsInShouldWork() {
        // Initialize the database
        insertedBrokerAccount = brokerAccountRepository.save(brokerAccount).block();

        // Get all the brokerAccountList where externalAccountId in
        defaultBrokerAccountFiltering(
            "externalAccountId.in=" + DEFAULT_EXTERNAL_ACCOUNT_ID + "," + UPDATED_EXTERNAL_ACCOUNT_ID,
            "externalAccountId.in=" + UPDATED_EXTERNAL_ACCOUNT_ID
        );
    }

    @Test
    void getAllBrokerAccountsByExternalAccountIdIsNullOrNotNull() {
        // Initialize the database
        insertedBrokerAccount = brokerAccountRepository.save(brokerAccount).block();

        // Get all the brokerAccountList where externalAccountId is not null
        defaultBrokerAccountFiltering("externalAccountId.specified=true", "externalAccountId.specified=false");
    }

    @Test
    void getAllBrokerAccountsByExternalAccountIdContainsSomething() {
        // Initialize the database
        insertedBrokerAccount = brokerAccountRepository.save(brokerAccount).block();

        // Get all the brokerAccountList where externalAccountId contains
        defaultBrokerAccountFiltering(
            "externalAccountId.contains=" + DEFAULT_EXTERNAL_ACCOUNT_ID,
            "externalAccountId.contains=" + UPDATED_EXTERNAL_ACCOUNT_ID
        );
    }

    @Test
    void getAllBrokerAccountsByExternalAccountIdNotContainsSomething() {
        // Initialize the database
        insertedBrokerAccount = brokerAccountRepository.save(brokerAccount).block();

        // Get all the brokerAccountList where externalAccountId does not contain
        defaultBrokerAccountFiltering(
            "externalAccountId.doesNotContain=" + UPDATED_EXTERNAL_ACCOUNT_ID,
            "externalAccountId.doesNotContain=" + DEFAULT_EXTERNAL_ACCOUNT_ID
        );
    }

    @Test
    void getAllBrokerAccountsByDisplayNameIsEqualToSomething() {
        // Initialize the database
        insertedBrokerAccount = brokerAccountRepository.save(brokerAccount).block();

        // Get all the brokerAccountList where displayName equals to
        defaultBrokerAccountFiltering("displayName.equals=" + DEFAULT_DISPLAY_NAME, "displayName.equals=" + UPDATED_DISPLAY_NAME);
    }

    @Test
    void getAllBrokerAccountsByDisplayNameIsInShouldWork() {
        // Initialize the database
        insertedBrokerAccount = brokerAccountRepository.save(brokerAccount).block();

        // Get all the brokerAccountList where displayName in
        defaultBrokerAccountFiltering(
            "displayName.in=" + DEFAULT_DISPLAY_NAME + "," + UPDATED_DISPLAY_NAME,
            "displayName.in=" + UPDATED_DISPLAY_NAME
        );
    }

    @Test
    void getAllBrokerAccountsByDisplayNameIsNullOrNotNull() {
        // Initialize the database
        insertedBrokerAccount = brokerAccountRepository.save(brokerAccount).block();

        // Get all the brokerAccountList where displayName is not null
        defaultBrokerAccountFiltering("displayName.specified=true", "displayName.specified=false");
    }

    @Test
    void getAllBrokerAccountsByDisplayNameContainsSomething() {
        // Initialize the database
        insertedBrokerAccount = brokerAccountRepository.save(brokerAccount).block();

        // Get all the brokerAccountList where displayName contains
        defaultBrokerAccountFiltering("displayName.contains=" + DEFAULT_DISPLAY_NAME, "displayName.contains=" + UPDATED_DISPLAY_NAME);
    }

    @Test
    void getAllBrokerAccountsByDisplayNameNotContainsSomething() {
        // Initialize the database
        insertedBrokerAccount = brokerAccountRepository.save(brokerAccount).block();

        // Get all the brokerAccountList where displayName does not contain
        defaultBrokerAccountFiltering(
            "displayName.doesNotContain=" + UPDATED_DISPLAY_NAME,
            "displayName.doesNotContain=" + DEFAULT_DISPLAY_NAME
        );
    }

    @Test
    void getAllBrokerAccountsByBrokerIsEqualToSomething() {
        Broker broker = BrokerResourceIT.createEntity();
        brokerRepository.save(broker).block();
        UUID brokerId = broker.getId();
        brokerAccount.setBrokerId(brokerId);
        insertedBrokerAccount = brokerAccountRepository.save(brokerAccount).block();
        // Get all the brokerAccountList where broker equals to brokerId
        defaultBrokerAccountShouldBeFound("brokerId.equals=" + brokerId);

        // Get all the brokerAccountList where broker equals to UUID.randomUUID()
        defaultBrokerAccountShouldNotBeFound("brokerId.equals=" + UUID.randomUUID());
    }

    private void defaultBrokerAccountFiltering(String shouldBeFound, String shouldNotBeFound) {
        defaultBrokerAccountShouldBeFound(shouldBeFound);
        defaultBrokerAccountShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultBrokerAccountShouldBeFound(String filter) {
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
            .value(hasItem(brokerAccount.getId().toString()))
            .jsonPath("$.[*].externalAccountId")
            .value(hasItem(DEFAULT_EXTERNAL_ACCOUNT_ID))

            .jsonPath("$.[*].displayName")
            .value(hasItem(DEFAULT_DISPLAY_NAME));

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
    private void defaultBrokerAccountShouldNotBeFound(String filter) {
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
    void getNonExistingBrokerAccount() {
        // Get the brokerAccount
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingBrokerAccount() throws Exception {
        // Initialize the database
        brokerAccount.setId(UUID.randomUUID());
        insertedBrokerAccount = brokerAccountRepository.save(brokerAccount).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the brokerAccount
        BrokerAccount updatedBrokerAccount = brokerAccountRepository.findById(brokerAccount.getId()).block();
        updatedBrokerAccount.externalAccountId(UPDATED_EXTERNAL_ACCOUNT_ID).displayName(UPDATED_DISPLAY_NAME);
        BrokerAccountDTO brokerAccountDTO = brokerAccountMapper.toDto(updatedBrokerAccount);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, brokerAccountDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(brokerAccountDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the BrokerAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedBrokerAccountToMatchAllProperties(updatedBrokerAccount);
    }

    @Test
    void putNonExistingBrokerAccount() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        brokerAccount.setId(UUID.randomUUID());

        // Create the BrokerAccount
        BrokerAccountDTO brokerAccountDTO = brokerAccountMapper.toDto(brokerAccount);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, brokerAccountDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(brokerAccountDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the BrokerAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchBrokerAccount() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        brokerAccount.setId(UUID.randomUUID());

        // Create the BrokerAccount
        BrokerAccountDTO brokerAccountDTO = brokerAccountMapper.toDto(brokerAccount);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(brokerAccountDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the BrokerAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamBrokerAccount() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        brokerAccount.setId(UUID.randomUUID());

        // Create the BrokerAccount
        BrokerAccountDTO brokerAccountDTO = brokerAccountMapper.toDto(brokerAccount);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(brokerAccountDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the BrokerAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateBrokerAccountWithPatch() throws Exception {
        // Initialize the database
        brokerAccount.setId(UUID.randomUUID());
        insertedBrokerAccount = brokerAccountRepository.save(brokerAccount).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the brokerAccount using partial update
        BrokerAccount partialUpdatedBrokerAccount = new BrokerAccount();
        partialUpdatedBrokerAccount.setId(brokerAccount.getId());

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedBrokerAccount.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedBrokerAccount))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the BrokerAccount in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBrokerAccountUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedBrokerAccount, brokerAccount),
            getPersistedBrokerAccount(brokerAccount)
        );
    }

    @Test
    void fullUpdateBrokerAccountWithPatch() throws Exception {
        // Initialize the database
        brokerAccount.setId(UUID.randomUUID());
        insertedBrokerAccount = brokerAccountRepository.save(brokerAccount).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the brokerAccount using partial update
        BrokerAccount partialUpdatedBrokerAccount = new BrokerAccount();
        partialUpdatedBrokerAccount.setId(brokerAccount.getId());

        partialUpdatedBrokerAccount.externalAccountId(UPDATED_EXTERNAL_ACCOUNT_ID).displayName(UPDATED_DISPLAY_NAME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedBrokerAccount.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedBrokerAccount))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the BrokerAccount in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBrokerAccountUpdatableFieldsEquals(partialUpdatedBrokerAccount, getPersistedBrokerAccount(partialUpdatedBrokerAccount));
    }

    @Test
    void patchNonExistingBrokerAccount() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        brokerAccount.setId(UUID.randomUUID());

        // Create the BrokerAccount
        BrokerAccountDTO brokerAccountDTO = brokerAccountMapper.toDto(brokerAccount);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, brokerAccountDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(brokerAccountDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the BrokerAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchBrokerAccount() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        brokerAccount.setId(UUID.randomUUID());

        // Create the BrokerAccount
        BrokerAccountDTO brokerAccountDTO = brokerAccountMapper.toDto(brokerAccount);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(brokerAccountDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the BrokerAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamBrokerAccount() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        brokerAccount.setId(UUID.randomUUID());

        // Create the BrokerAccount
        BrokerAccountDTO brokerAccountDTO = brokerAccountMapper.toDto(brokerAccount);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(brokerAccountDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the BrokerAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteBrokerAccount() {
        // Initialize the database
        brokerAccount.setId(UUID.randomUUID());
        insertedBrokerAccount = brokerAccountRepository.save(brokerAccount).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the brokerAccount
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, brokerAccount.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return brokerAccountRepository.count().block();
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

    protected BrokerAccount getPersistedBrokerAccount(BrokerAccount brokerAccount) {
        return brokerAccountRepository.findById(brokerAccount.getId()).block();
    }

    protected void assertPersistedBrokerAccountToMatchAllProperties(BrokerAccount expectedBrokerAccount) {
        // Test fails because reactive api returns an empty object instead of null
        // assertBrokerAccountAllPropertiesEquals(expectedBrokerAccount, getPersistedBrokerAccount(expectedBrokerAccount));
        assertBrokerAccountUpdatableFieldsEquals(expectedBrokerAccount, getPersistedBrokerAccount(expectedBrokerAccount));
    }

    protected void assertPersistedBrokerAccountToMatchUpdatableProperties(BrokerAccount expectedBrokerAccount) {
        // Test fails because reactive api returns an empty object instead of null
        // assertBrokerAccountAllUpdatablePropertiesEquals(expectedBrokerAccount, getPersistedBrokerAccount(expectedBrokerAccount));
        assertBrokerAccountUpdatableFieldsEquals(expectedBrokerAccount, getPersistedBrokerAccount(expectedBrokerAccount));
    }
}
