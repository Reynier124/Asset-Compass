package com.assetcompass.portfolio.web.rest;

import static com.assetcompass.portfolio.domain.BrokerAccountAsserts.*;
import static com.assetcompass.portfolio.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.assetcompass.portfolio.IntegrationTest;
import com.assetcompass.portfolio.domain.Broker;
import com.assetcompass.portfolio.domain.BrokerAccount;
import com.assetcompass.portfolio.repository.BrokerAccountRepository;
import com.assetcompass.portfolio.service.dto.BrokerAccountDTO;
import com.assetcompass.portfolio.service.mapper.BrokerAccountMapper;
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
 * Integration tests for the {@link BrokerAccountResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
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
    private MockMvc restBrokerAccountMockMvc;

    private BrokerAccount brokerAccount;

    private BrokerAccount insertedBrokerAccount;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static BrokerAccount createEntity(EntityManager em) {
        BrokerAccount brokerAccount = new BrokerAccount().externalAccountId(DEFAULT_EXTERNAL_ACCOUNT_ID).displayName(DEFAULT_DISPLAY_NAME);
        // Add required entity
        Broker broker;
        if (TestUtil.findAll(em, Broker.class).isEmpty()) {
            broker = BrokerResourceIT.createEntity();
            em.persist(broker);
            em.flush();
        } else {
            broker = TestUtil.findAll(em, Broker.class).get(0);
        }
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
            .externalAccountId(UPDATED_EXTERNAL_ACCOUNT_ID)
            .displayName(UPDATED_DISPLAY_NAME);
        // Add required entity
        Broker broker;
        if (TestUtil.findAll(em, Broker.class).isEmpty()) {
            broker = BrokerResourceIT.createUpdatedEntity();
            em.persist(broker);
            em.flush();
        } else {
            broker = TestUtil.findAll(em, Broker.class).get(0);
        }
        updatedBrokerAccount.setBroker(broker);
        return updatedBrokerAccount;
    }

    @BeforeEach
    void initTest() {
        brokerAccount = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedBrokerAccount != null) {
            brokerAccountRepository.delete(insertedBrokerAccount);
            insertedBrokerAccount = null;
        }
    }

    @Test
    @Transactional
    void createBrokerAccount() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the BrokerAccount
        BrokerAccountDTO brokerAccountDTO = brokerAccountMapper.toDto(brokerAccount);
        var returnedBrokerAccountDTO = om.readValue(
            restBrokerAccountMockMvc
                .perform(
                    post(ENTITY_API_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsBytes(brokerAccountDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            BrokerAccountDTO.class
        );

        // Validate the BrokerAccount in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedBrokerAccount = brokerAccountMapper.toEntity(returnedBrokerAccountDTO);
        assertBrokerAccountUpdatableFieldsEquals(returnedBrokerAccount, getPersistedBrokerAccount(returnedBrokerAccount));

        insertedBrokerAccount = returnedBrokerAccount;
    }

    @Test
    @Transactional
    void createBrokerAccountWithExistingId() throws Exception {
        // Create the BrokerAccount with an existing ID
        insertedBrokerAccount = brokerAccountRepository.saveAndFlush(brokerAccount);
        BrokerAccountDTO brokerAccountDTO = brokerAccountMapper.toDto(brokerAccount);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restBrokerAccountMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(brokerAccountDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BrokerAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkExternalAccountIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        brokerAccount.setExternalAccountId(null);

        // Create the BrokerAccount, which fails.
        BrokerAccountDTO brokerAccountDTO = brokerAccountMapper.toDto(brokerAccount);

        restBrokerAccountMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(brokerAccountDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllBrokerAccounts() throws Exception {
        // Initialize the database
        insertedBrokerAccount = brokerAccountRepository.saveAndFlush(brokerAccount);

        // Get all the brokerAccountList
        restBrokerAccountMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(brokerAccount.getId().toString())))
            .andExpect(jsonPath("$.[*].externalAccountId").value(hasItem(DEFAULT_EXTERNAL_ACCOUNT_ID)))
            .andExpect(jsonPath("$.[*].displayName").value(hasItem(DEFAULT_DISPLAY_NAME)));
    }

    @Test
    @Transactional
    void getBrokerAccount() throws Exception {
        // Initialize the database
        insertedBrokerAccount = brokerAccountRepository.saveAndFlush(brokerAccount);

        // Get the brokerAccount
        restBrokerAccountMockMvc
            .perform(get(ENTITY_API_URL_ID, brokerAccount.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(brokerAccount.getId().toString()))
            .andExpect(jsonPath("$.externalAccountId").value(DEFAULT_EXTERNAL_ACCOUNT_ID))
            .andExpect(jsonPath("$.displayName").value(DEFAULT_DISPLAY_NAME));
    }

    @Test
    @Transactional
    void getBrokerAccountsByIdFiltering() throws Exception {
        // Initialize the database
        insertedBrokerAccount = brokerAccountRepository.saveAndFlush(brokerAccount);

        UUID id = brokerAccount.getId();

        defaultBrokerAccountFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    @Transactional
    void getAllBrokerAccountsByExternalAccountIdIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedBrokerAccount = brokerAccountRepository.saveAndFlush(brokerAccount);

        // Get all the brokerAccountList where externalAccountId equals to
        defaultBrokerAccountFiltering(
            "externalAccountId.equals=" + DEFAULT_EXTERNAL_ACCOUNT_ID,
            "externalAccountId.equals=" + UPDATED_EXTERNAL_ACCOUNT_ID
        );
    }

    @Test
    @Transactional
    void getAllBrokerAccountsByExternalAccountIdIsInShouldWork() throws Exception {
        // Initialize the database
        insertedBrokerAccount = brokerAccountRepository.saveAndFlush(brokerAccount);

        // Get all the brokerAccountList where externalAccountId in
        defaultBrokerAccountFiltering(
            "externalAccountId.in=" + DEFAULT_EXTERNAL_ACCOUNT_ID + "," + UPDATED_EXTERNAL_ACCOUNT_ID,
            "externalAccountId.in=" + UPDATED_EXTERNAL_ACCOUNT_ID
        );
    }

    @Test
    @Transactional
    void getAllBrokerAccountsByExternalAccountIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedBrokerAccount = brokerAccountRepository.saveAndFlush(brokerAccount);

        // Get all the brokerAccountList where externalAccountId is not null
        defaultBrokerAccountFiltering("externalAccountId.specified=true", "externalAccountId.specified=false");
    }

    @Test
    @Transactional
    void getAllBrokerAccountsByExternalAccountIdContainsSomething() throws Exception {
        // Initialize the database
        insertedBrokerAccount = brokerAccountRepository.saveAndFlush(brokerAccount);

        // Get all the brokerAccountList where externalAccountId contains
        defaultBrokerAccountFiltering(
            "externalAccountId.contains=" + DEFAULT_EXTERNAL_ACCOUNT_ID,
            "externalAccountId.contains=" + UPDATED_EXTERNAL_ACCOUNT_ID
        );
    }

    @Test
    @Transactional
    void getAllBrokerAccountsByExternalAccountIdNotContainsSomething() throws Exception {
        // Initialize the database
        insertedBrokerAccount = brokerAccountRepository.saveAndFlush(brokerAccount);

        // Get all the brokerAccountList where externalAccountId does not contain
        defaultBrokerAccountFiltering(
            "externalAccountId.doesNotContain=" + UPDATED_EXTERNAL_ACCOUNT_ID,
            "externalAccountId.doesNotContain=" + DEFAULT_EXTERNAL_ACCOUNT_ID
        );
    }

    @Test
    @Transactional
    void getAllBrokerAccountsByDisplayNameIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedBrokerAccount = brokerAccountRepository.saveAndFlush(brokerAccount);

        // Get all the brokerAccountList where displayName equals to
        defaultBrokerAccountFiltering("displayName.equals=" + DEFAULT_DISPLAY_NAME, "displayName.equals=" + UPDATED_DISPLAY_NAME);
    }

    @Test
    @Transactional
    void getAllBrokerAccountsByDisplayNameIsInShouldWork() throws Exception {
        // Initialize the database
        insertedBrokerAccount = brokerAccountRepository.saveAndFlush(brokerAccount);

        // Get all the brokerAccountList where displayName in
        defaultBrokerAccountFiltering(
            "displayName.in=" + DEFAULT_DISPLAY_NAME + "," + UPDATED_DISPLAY_NAME,
            "displayName.in=" + UPDATED_DISPLAY_NAME
        );
    }

    @Test
    @Transactional
    void getAllBrokerAccountsByDisplayNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedBrokerAccount = brokerAccountRepository.saveAndFlush(brokerAccount);

        // Get all the brokerAccountList where displayName is not null
        defaultBrokerAccountFiltering("displayName.specified=true", "displayName.specified=false");
    }

    @Test
    @Transactional
    void getAllBrokerAccountsByDisplayNameContainsSomething() throws Exception {
        // Initialize the database
        insertedBrokerAccount = brokerAccountRepository.saveAndFlush(brokerAccount);

        // Get all the brokerAccountList where displayName contains
        defaultBrokerAccountFiltering("displayName.contains=" + DEFAULT_DISPLAY_NAME, "displayName.contains=" + UPDATED_DISPLAY_NAME);
    }

    @Test
    @Transactional
    void getAllBrokerAccountsByDisplayNameNotContainsSomething() throws Exception {
        // Initialize the database
        insertedBrokerAccount = brokerAccountRepository.saveAndFlush(brokerAccount);

        // Get all the brokerAccountList where displayName does not contain
        defaultBrokerAccountFiltering(
            "displayName.doesNotContain=" + UPDATED_DISPLAY_NAME,
            "displayName.doesNotContain=" + DEFAULT_DISPLAY_NAME
        );
    }

    @Test
    @Transactional
    void getAllBrokerAccountsByBrokerIsEqualToSomething() throws Exception {
        Broker broker;
        if (TestUtil.findAll(em, Broker.class).isEmpty()) {
            brokerAccountRepository.saveAndFlush(brokerAccount);
            broker = BrokerResourceIT.createEntity();
        } else {
            broker = TestUtil.findAll(em, Broker.class).get(0);
        }
        em.persist(broker);
        em.flush();
        brokerAccount.setBroker(broker);
        brokerAccountRepository.saveAndFlush(brokerAccount);
        UUID brokerId = broker.getId();
        // Get all the brokerAccountList where broker equals to brokerId
        defaultBrokerAccountShouldBeFound("brokerId.equals=" + brokerId);

        // Get all the brokerAccountList where broker equals to UUID.randomUUID()
        defaultBrokerAccountShouldNotBeFound("brokerId.equals=" + UUID.randomUUID());
    }

    private void defaultBrokerAccountFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultBrokerAccountShouldBeFound(shouldBeFound);
        defaultBrokerAccountShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultBrokerAccountShouldBeFound(String filter) throws Exception {
        restBrokerAccountMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(brokerAccount.getId().toString())))
            .andExpect(jsonPath("$.[*].externalAccountId").value(hasItem(DEFAULT_EXTERNAL_ACCOUNT_ID)))
            .andExpect(jsonPath("$.[*].displayName").value(hasItem(DEFAULT_DISPLAY_NAME)));

        // Check, that the count call also returns 1
        restBrokerAccountMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultBrokerAccountShouldNotBeFound(String filter) throws Exception {
        restBrokerAccountMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restBrokerAccountMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingBrokerAccount() throws Exception {
        // Get the brokerAccount
        restBrokerAccountMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingBrokerAccount() throws Exception {
        // Initialize the database
        insertedBrokerAccount = brokerAccountRepository.saveAndFlush(brokerAccount);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the brokerAccount
        BrokerAccount updatedBrokerAccount = brokerAccountRepository.findById(brokerAccount.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedBrokerAccount are not directly saved in db
        em.detach(updatedBrokerAccount);
        updatedBrokerAccount.externalAccountId(UPDATED_EXTERNAL_ACCOUNT_ID).displayName(UPDATED_DISPLAY_NAME);
        BrokerAccountDTO brokerAccountDTO = brokerAccountMapper.toDto(updatedBrokerAccount);

        restBrokerAccountMockMvc
            .perform(
                put(ENTITY_API_URL_ID, brokerAccountDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(brokerAccountDTO))
            )
            .andExpect(status().isOk());

        // Validate the BrokerAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedBrokerAccountToMatchAllProperties(updatedBrokerAccount);
    }

    @Test
    @Transactional
    void putNonExistingBrokerAccount() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        brokerAccount.setId(UUID.randomUUID());

        // Create the BrokerAccount
        BrokerAccountDTO brokerAccountDTO = brokerAccountMapper.toDto(brokerAccount);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBrokerAccountMockMvc
            .perform(
                put(ENTITY_API_URL_ID, brokerAccountDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(brokerAccountDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BrokerAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchBrokerAccount() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        brokerAccount.setId(UUID.randomUUID());

        // Create the BrokerAccount
        BrokerAccountDTO brokerAccountDTO = brokerAccountMapper.toDto(brokerAccount);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBrokerAccountMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(brokerAccountDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BrokerAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamBrokerAccount() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        brokerAccount.setId(UUID.randomUUID());

        // Create the BrokerAccount
        BrokerAccountDTO brokerAccountDTO = brokerAccountMapper.toDto(brokerAccount);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBrokerAccountMockMvc
            .perform(
                put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(brokerAccountDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the BrokerAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateBrokerAccountWithPatch() throws Exception {
        // Initialize the database
        insertedBrokerAccount = brokerAccountRepository.saveAndFlush(brokerAccount);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the brokerAccount using partial update
        BrokerAccount partialUpdatedBrokerAccount = new BrokerAccount();
        partialUpdatedBrokerAccount.setId(brokerAccount.getId());

        partialUpdatedBrokerAccount.externalAccountId(UPDATED_EXTERNAL_ACCOUNT_ID).displayName(UPDATED_DISPLAY_NAME);

        restBrokerAccountMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBrokerAccount.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedBrokerAccount))
            )
            .andExpect(status().isOk());

        // Validate the BrokerAccount in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBrokerAccountUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedBrokerAccount, brokerAccount),
            getPersistedBrokerAccount(brokerAccount)
        );
    }

    @Test
    @Transactional
    void fullUpdateBrokerAccountWithPatch() throws Exception {
        // Initialize the database
        insertedBrokerAccount = brokerAccountRepository.saveAndFlush(brokerAccount);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the brokerAccount using partial update
        BrokerAccount partialUpdatedBrokerAccount = new BrokerAccount();
        partialUpdatedBrokerAccount.setId(brokerAccount.getId());

        partialUpdatedBrokerAccount.externalAccountId(UPDATED_EXTERNAL_ACCOUNT_ID).displayName(UPDATED_DISPLAY_NAME);

        restBrokerAccountMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedBrokerAccount.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedBrokerAccount))
            )
            .andExpect(status().isOk());

        // Validate the BrokerAccount in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertBrokerAccountUpdatableFieldsEquals(partialUpdatedBrokerAccount, getPersistedBrokerAccount(partialUpdatedBrokerAccount));
    }

    @Test
    @Transactional
    void patchNonExistingBrokerAccount() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        brokerAccount.setId(UUID.randomUUID());

        // Create the BrokerAccount
        BrokerAccountDTO brokerAccountDTO = brokerAccountMapper.toDto(brokerAccount);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBrokerAccountMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, brokerAccountDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(brokerAccountDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BrokerAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchBrokerAccount() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        brokerAccount.setId(UUID.randomUUID());

        // Create the BrokerAccount
        BrokerAccountDTO brokerAccountDTO = brokerAccountMapper.toDto(brokerAccount);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBrokerAccountMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(brokerAccountDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the BrokerAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamBrokerAccount() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        brokerAccount.setId(UUID.randomUUID());

        // Create the BrokerAccount
        BrokerAccountDTO brokerAccountDTO = brokerAccountMapper.toDto(brokerAccount);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBrokerAccountMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(brokerAccountDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the BrokerAccount in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteBrokerAccount() throws Exception {
        // Initialize the database
        insertedBrokerAccount = brokerAccountRepository.saveAndFlush(brokerAccount);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the brokerAccount
        restBrokerAccountMockMvc
            .perform(delete(ENTITY_API_URL_ID, brokerAccount.getId().toString()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return brokerAccountRepository.count();
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
        return brokerAccountRepository.findById(brokerAccount.getId()).orElseThrow();
    }

    protected void assertPersistedBrokerAccountToMatchAllProperties(BrokerAccount expectedBrokerAccount) {
        assertBrokerAccountAllPropertiesEquals(expectedBrokerAccount, getPersistedBrokerAccount(expectedBrokerAccount));
    }

    protected void assertPersistedBrokerAccountToMatchUpdatableProperties(BrokerAccount expectedBrokerAccount) {
        assertBrokerAccountAllUpdatablePropertiesEquals(expectedBrokerAccount, getPersistedBrokerAccount(expectedBrokerAccount));
    }
}
