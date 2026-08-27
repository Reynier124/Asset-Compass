package com.assetcompass.portfolio.web.rest;

import static com.assetcompass.portfolio.domain.PositionAsserts.*;
import static com.assetcompass.portfolio.web.rest.TestUtil.createUpdateProxyForBean;
import static com.assetcompass.portfolio.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.assetcompass.portfolio.IntegrationTest;
import com.assetcompass.portfolio.domain.Asset;
import com.assetcompass.portfolio.domain.BrokerAccount;
import com.assetcompass.portfolio.domain.Position;
import com.assetcompass.portfolio.repository.PositionRepository;
import com.assetcompass.portfolio.service.dto.PositionDTO;
import com.assetcompass.portfolio.service.mapper.PositionMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
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
 * Integration tests for the {@link PositionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class PositionResourceIT {

    private static final BigDecimal DEFAULT_QUANTITY = new BigDecimal(1);
    private static final BigDecimal UPDATED_QUANTITY = new BigDecimal(2);
    private static final BigDecimal SMALLER_QUANTITY = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_AVERAGE_COST = new BigDecimal(1);
    private static final BigDecimal UPDATED_AVERAGE_COST = new BigDecimal(2);
    private static final BigDecimal SMALLER_AVERAGE_COST = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_CURRENT_VALUE = new BigDecimal(1);
    private static final BigDecimal UPDATED_CURRENT_VALUE = new BigDecimal(2);
    private static final BigDecimal SMALLER_CURRENT_VALUE = new BigDecimal(1 - 1);

    private static final String DEFAULT_CURRENCY = "AAAAAAAAAA";
    private static final String UPDATED_CURRENCY = "BBBBBBBBBB";

    private static final Instant DEFAULT_LAST_SYNCED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_LAST_SYNCED_AT = Instant.ofEpochMilli(1701517477007L);

    private static final String ENTITY_API_URL = "/api/positions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private PositionMapper positionMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPositionMockMvc;

    private Position position;

    private Position insertedPosition;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Position createEntity(EntityManager em) {
        Position position = new Position()
            .quantity(DEFAULT_QUANTITY)
            .averageCost(DEFAULT_AVERAGE_COST)
            .currentValue(DEFAULT_CURRENT_VALUE)
            .currency(DEFAULT_CURRENCY)
            .lastSyncedAt(DEFAULT_LAST_SYNCED_AT);
        // Add required entity
        BrokerAccount brokerAccount;
        if (TestUtil.findAll(em, BrokerAccount.class).isEmpty()) {
            brokerAccount = BrokerAccountResourceIT.createEntity(em);
            em.persist(brokerAccount);
            em.flush();
        } else {
            brokerAccount = TestUtil.findAll(em, BrokerAccount.class).get(0);
        }
        position.setAccount(brokerAccount);
        // Add required entity
        Asset asset;
        if (TestUtil.findAll(em, Asset.class).isEmpty()) {
            asset = AssetResourceIT.createEntity();
            em.persist(asset);
            em.flush();
        } else {
            asset = TestUtil.findAll(em, Asset.class).get(0);
        }
        position.setAsset(asset);
        return position;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Position createUpdatedEntity(EntityManager em) {
        Position updatedPosition = new Position()
            .quantity(UPDATED_QUANTITY)
            .averageCost(UPDATED_AVERAGE_COST)
            .currentValue(UPDATED_CURRENT_VALUE)
            .currency(UPDATED_CURRENCY)
            .lastSyncedAt(UPDATED_LAST_SYNCED_AT);
        // Add required entity
        BrokerAccount brokerAccount;
        if (TestUtil.findAll(em, BrokerAccount.class).isEmpty()) {
            brokerAccount = BrokerAccountResourceIT.createUpdatedEntity(em);
            em.persist(brokerAccount);
            em.flush();
        } else {
            brokerAccount = TestUtil.findAll(em, BrokerAccount.class).get(0);
        }
        updatedPosition.setAccount(brokerAccount);
        // Add required entity
        Asset asset;
        if (TestUtil.findAll(em, Asset.class).isEmpty()) {
            asset = AssetResourceIT.createUpdatedEntity();
            em.persist(asset);
            em.flush();
        } else {
            asset = TestUtil.findAll(em, Asset.class).get(0);
        }
        updatedPosition.setAsset(asset);
        return updatedPosition;
    }

    @BeforeEach
    void initTest() {
        position = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedPosition != null) {
            positionRepository.delete(insertedPosition);
            insertedPosition = null;
        }
    }

    @Test
    @Transactional
    void createPosition() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Position
        PositionDTO positionDTO = positionMapper.toDto(position);
        positionDTO.setId(UUID.randomUUID());
        var returnedPositionDTO = om.readValue(
            restPositionMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(positionDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            PositionDTO.class
        );

        // Validate the Position in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedPosition = positionMapper.toEntity(returnedPositionDTO);
        assertPositionUpdatableFieldsEquals(returnedPosition, getPersistedPosition(returnedPosition));

        insertedPosition = returnedPosition;
    }

    @Test
    @Transactional
    void createPositionWithExistingId() throws Exception {
        // Create the Position with an existing ID
        insertedPosition = positionRepository.saveAndFlush(position);
        PositionDTO positionDTO = positionMapper.toDto(position);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restPositionMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(positionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Position in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkQuantityIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        position.setQuantity(null);

        // Create the Position, which fails.
        PositionDTO positionDTO = positionMapper.toDto(position);

        restPositionMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(positionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAverageCostIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        position.setAverageCost(null);

        // Create the Position, which fails.
        PositionDTO positionDTO = positionMapper.toDto(position);

        restPositionMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(positionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCurrentValueIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        position.setCurrentValue(null);

        // Create the Position, which fails.
        PositionDTO positionDTO = positionMapper.toDto(position);

        restPositionMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(positionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCurrencyIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        position.setCurrency(null);

        // Create the Position, which fails.
        PositionDTO positionDTO = positionMapper.toDto(position);

        restPositionMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(positionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkLastSyncedAtIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        position.setLastSyncedAt(null);

        // Create the Position, which fails.
        PositionDTO positionDTO = positionMapper.toDto(position);

        restPositionMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(positionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllPositions() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList
        restPositionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(position.getId().toString())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(sameNumber(DEFAULT_QUANTITY))))
            .andExpect(jsonPath("$.[*].averageCost").value(hasItem(sameNumber(DEFAULT_AVERAGE_COST))))
            .andExpect(jsonPath("$.[*].currentValue").value(hasItem(sameNumber(DEFAULT_CURRENT_VALUE))))
            .andExpect(jsonPath("$.[*].currency").value(hasItem(DEFAULT_CURRENCY)))
            .andExpect(jsonPath("$.[*].lastSyncedAt").value(hasItem(DEFAULT_LAST_SYNCED_AT.toString())));
    }

    @Test
    @Transactional
    void getPosition() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get the position
        restPositionMockMvc
            .perform(get(ENTITY_API_URL_ID, position.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(position.getId().toString()))
            .andExpect(jsonPath("$.quantity").value(sameNumber(DEFAULT_QUANTITY)))
            .andExpect(jsonPath("$.averageCost").value(sameNumber(DEFAULT_AVERAGE_COST)))
            .andExpect(jsonPath("$.currentValue").value(sameNumber(DEFAULT_CURRENT_VALUE)))
            .andExpect(jsonPath("$.currency").value(DEFAULT_CURRENCY))
            .andExpect(jsonPath("$.lastSyncedAt").value(DEFAULT_LAST_SYNCED_AT.toString()));
    }

    @Test
    @Transactional
    void getPositionsByIdFiltering() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        UUID id = position.getId();

        defaultPositionFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    @Transactional
    void getAllPositionsByQuantityIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where quantity equals to
        defaultPositionFiltering("quantity.equals=" + DEFAULT_QUANTITY, "quantity.equals=" + UPDATED_QUANTITY);
    }

    @Test
    @Transactional
    void getAllPositionsByQuantityIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where quantity in
        defaultPositionFiltering("quantity.in=" + DEFAULT_QUANTITY + "," + UPDATED_QUANTITY, "quantity.in=" + UPDATED_QUANTITY);
    }

    @Test
    @Transactional
    void getAllPositionsByQuantityIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where quantity is not null
        defaultPositionFiltering("quantity.specified=true", "quantity.specified=false");
    }

    @Test
    @Transactional
    void getAllPositionsByQuantityIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where quantity is greater than or equal to
        defaultPositionFiltering("quantity.greaterThanOrEqual=" + DEFAULT_QUANTITY, "quantity.greaterThanOrEqual=" + UPDATED_QUANTITY);
    }

    @Test
    @Transactional
    void getAllPositionsByQuantityIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where quantity is less than or equal to
        defaultPositionFiltering("quantity.lessThanOrEqual=" + DEFAULT_QUANTITY, "quantity.lessThanOrEqual=" + SMALLER_QUANTITY);
    }

    @Test
    @Transactional
    void getAllPositionsByQuantityIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where quantity is less than
        defaultPositionFiltering("quantity.lessThan=" + UPDATED_QUANTITY, "quantity.lessThan=" + DEFAULT_QUANTITY);
    }

    @Test
    @Transactional
    void getAllPositionsByQuantityIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where quantity is greater than
        defaultPositionFiltering("quantity.greaterThan=" + SMALLER_QUANTITY, "quantity.greaterThan=" + DEFAULT_QUANTITY);
    }

    @Test
    @Transactional
    void getAllPositionsByAverageCostIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where averageCost equals to
        defaultPositionFiltering("averageCost.equals=" + DEFAULT_AVERAGE_COST, "averageCost.equals=" + UPDATED_AVERAGE_COST);
    }

    @Test
    @Transactional
    void getAllPositionsByAverageCostIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where averageCost in
        defaultPositionFiltering(
            "averageCost.in=" + DEFAULT_AVERAGE_COST + "," + UPDATED_AVERAGE_COST,
            "averageCost.in=" + UPDATED_AVERAGE_COST
        );
    }

    @Test
    @Transactional
    void getAllPositionsByAverageCostIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where averageCost is not null
        defaultPositionFiltering("averageCost.specified=true", "averageCost.specified=false");
    }

    @Test
    @Transactional
    void getAllPositionsByAverageCostIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where averageCost is greater than or equal to
        defaultPositionFiltering(
            "averageCost.greaterThanOrEqual=" + DEFAULT_AVERAGE_COST,
            "averageCost.greaterThanOrEqual=" + UPDATED_AVERAGE_COST
        );
    }

    @Test
    @Transactional
    void getAllPositionsByAverageCostIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where averageCost is less than or equal to
        defaultPositionFiltering(
            "averageCost.lessThanOrEqual=" + DEFAULT_AVERAGE_COST,
            "averageCost.lessThanOrEqual=" + SMALLER_AVERAGE_COST
        );
    }

    @Test
    @Transactional
    void getAllPositionsByAverageCostIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where averageCost is less than
        defaultPositionFiltering("averageCost.lessThan=" + UPDATED_AVERAGE_COST, "averageCost.lessThan=" + DEFAULT_AVERAGE_COST);
    }

    @Test
    @Transactional
    void getAllPositionsByAverageCostIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where averageCost is greater than
        defaultPositionFiltering("averageCost.greaterThan=" + SMALLER_AVERAGE_COST, "averageCost.greaterThan=" + DEFAULT_AVERAGE_COST);
    }

    @Test
    @Transactional
    void getAllPositionsByCurrentValueIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where currentValue equals to
        defaultPositionFiltering("currentValue.equals=" + DEFAULT_CURRENT_VALUE, "currentValue.equals=" + UPDATED_CURRENT_VALUE);
    }

    @Test
    @Transactional
    void getAllPositionsByCurrentValueIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where currentValue in
        defaultPositionFiltering(
            "currentValue.in=" + DEFAULT_CURRENT_VALUE + "," + UPDATED_CURRENT_VALUE,
            "currentValue.in=" + UPDATED_CURRENT_VALUE
        );
    }

    @Test
    @Transactional
    void getAllPositionsByCurrentValueIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where currentValue is not null
        defaultPositionFiltering("currentValue.specified=true", "currentValue.specified=false");
    }

    @Test
    @Transactional
    void getAllPositionsByCurrentValueIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where currentValue is greater than or equal to
        defaultPositionFiltering(
            "currentValue.greaterThanOrEqual=" + DEFAULT_CURRENT_VALUE,
            "currentValue.greaterThanOrEqual=" + UPDATED_CURRENT_VALUE
        );
    }

    @Test
    @Transactional
    void getAllPositionsByCurrentValueIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where currentValue is less than or equal to
        defaultPositionFiltering(
            "currentValue.lessThanOrEqual=" + DEFAULT_CURRENT_VALUE,
            "currentValue.lessThanOrEqual=" + SMALLER_CURRENT_VALUE
        );
    }

    @Test
    @Transactional
    void getAllPositionsByCurrentValueIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where currentValue is less than
        defaultPositionFiltering("currentValue.lessThan=" + UPDATED_CURRENT_VALUE, "currentValue.lessThan=" + DEFAULT_CURRENT_VALUE);
    }

    @Test
    @Transactional
    void getAllPositionsByCurrentValueIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where currentValue is greater than
        defaultPositionFiltering("currentValue.greaterThan=" + SMALLER_CURRENT_VALUE, "currentValue.greaterThan=" + DEFAULT_CURRENT_VALUE);
    }

    @Test
    @Transactional
    void getAllPositionsByCurrencyIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where currency equals to
        defaultPositionFiltering("currency.equals=" + DEFAULT_CURRENCY, "currency.equals=" + UPDATED_CURRENCY);
    }

    @Test
    @Transactional
    void getAllPositionsByCurrencyIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where currency in
        defaultPositionFiltering("currency.in=" + DEFAULT_CURRENCY + "," + UPDATED_CURRENCY, "currency.in=" + UPDATED_CURRENCY);
    }

    @Test
    @Transactional
    void getAllPositionsByCurrencyIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where currency is not null
        defaultPositionFiltering("currency.specified=true", "currency.specified=false");
    }

    @Test
    @Transactional
    void getAllPositionsByCurrencyContainsSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where currency contains
        defaultPositionFiltering("currency.contains=" + DEFAULT_CURRENCY, "currency.contains=" + UPDATED_CURRENCY);
    }

    @Test
    @Transactional
    void getAllPositionsByCurrencyNotContainsSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where currency does not contain
        defaultPositionFiltering("currency.doesNotContain=" + UPDATED_CURRENCY, "currency.doesNotContain=" + DEFAULT_CURRENCY);
    }

    @Test
    @Transactional
    void getAllPositionsByLastSyncedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where lastSyncedAt equals to
        defaultPositionFiltering("lastSyncedAt.equals=" + DEFAULT_LAST_SYNCED_AT, "lastSyncedAt.equals=" + UPDATED_LAST_SYNCED_AT);
    }

    @Test
    @Transactional
    void getAllPositionsByLastSyncedAtIsInShouldWork() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where lastSyncedAt in
        defaultPositionFiltering(
            "lastSyncedAt.in=" + DEFAULT_LAST_SYNCED_AT + "," + UPDATED_LAST_SYNCED_AT,
            "lastSyncedAt.in=" + UPDATED_LAST_SYNCED_AT
        );
    }

    @Test
    @Transactional
    void getAllPositionsByLastSyncedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        // Get all the positionList where lastSyncedAt is not null
        defaultPositionFiltering("lastSyncedAt.specified=true", "lastSyncedAt.specified=false");
    }

    @Test
    @Transactional
    void getAllPositionsByAccountIsEqualToSomething() throws Exception {
        BrokerAccount account;
        if (TestUtil.findAll(em, BrokerAccount.class).isEmpty()) {
            positionRepository.saveAndFlush(position);
            account = BrokerAccountResourceIT.createEntity(em);
        } else {
            account = TestUtil.findAll(em, BrokerAccount.class).get(0);
        }
        em.persist(account);
        em.flush();
        position.setAccount(account);
        positionRepository.saveAndFlush(position);
        UUID accountId = account.getId();
        // Get all the positionList where account equals to accountId
        defaultPositionShouldBeFound("accountId.equals=" + accountId);

        // Get all the positionList where account equals to UUID.randomUUID()
        defaultPositionShouldNotBeFound("accountId.equals=" + UUID.randomUUID());
    }

    @Test
    @Transactional
    void getAllPositionsByAssetIsEqualToSomething() throws Exception {
        Asset asset;
        if (TestUtil.findAll(em, Asset.class).isEmpty()) {
            positionRepository.saveAndFlush(position);
            asset = AssetResourceIT.createEntity();
        } else {
            asset = TestUtil.findAll(em, Asset.class).get(0);
        }
        em.persist(asset);
        em.flush();
        position.setAsset(asset);
        positionRepository.saveAndFlush(position);
        UUID assetId = asset.getId();
        // Get all the positionList where asset equals to assetId
        defaultPositionShouldBeFound("assetId.equals=" + assetId);

        // Get all the positionList where asset equals to UUID.randomUUID()
        defaultPositionShouldNotBeFound("assetId.equals=" + UUID.randomUUID());
    }

    private void defaultPositionFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultPositionShouldBeFound(shouldBeFound);
        defaultPositionShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultPositionShouldBeFound(String filter) throws Exception {
        restPositionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(position.getId().toString())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(sameNumber(DEFAULT_QUANTITY))))
            .andExpect(jsonPath("$.[*].averageCost").value(hasItem(sameNumber(DEFAULT_AVERAGE_COST))))
            .andExpect(jsonPath("$.[*].currentValue").value(hasItem(sameNumber(DEFAULT_CURRENT_VALUE))))
            .andExpect(jsonPath("$.[*].currency").value(hasItem(DEFAULT_CURRENCY)))
            .andExpect(jsonPath("$.[*].lastSyncedAt").value(hasItem(DEFAULT_LAST_SYNCED_AT.toString())));

        // Check, that the count call also returns 1
        restPositionMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultPositionShouldNotBeFound(String filter) throws Exception {
        restPositionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restPositionMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingPosition() throws Exception {
        // Get the position
        restPositionMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingPosition() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the position
        Position updatedPosition = positionRepository.findById(position.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedPosition are not directly saved in db
        em.detach(updatedPosition);
        updatedPosition
            .quantity(UPDATED_QUANTITY)
            .averageCost(UPDATED_AVERAGE_COST)
            .currentValue(UPDATED_CURRENT_VALUE)
            .currency(UPDATED_CURRENCY)
            .lastSyncedAt(UPDATED_LAST_SYNCED_AT);
        PositionDTO positionDTO = positionMapper.toDto(updatedPosition);

        restPositionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, positionDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(positionDTO))
            )
            .andExpect(status().isOk());

        // Validate the Position in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedPositionToMatchAllProperties(updatedPosition);
    }

    @Test
    @Transactional
    void putNonExistingPosition() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        position.setId(UUID.randomUUID());

        // Create the Position
        PositionDTO positionDTO = positionMapper.toDto(position);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPositionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, positionDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(positionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Position in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchPosition() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        position.setId(UUID.randomUUID());

        // Create the Position
        PositionDTO positionDTO = positionMapper.toDto(position);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPositionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(positionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Position in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPosition() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        position.setId(UUID.randomUUID());

        // Create the Position
        PositionDTO positionDTO = positionMapper.toDto(position);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPositionMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(positionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Position in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdatePositionWithPatch() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the position using partial update
        Position partialUpdatedPosition = new Position();
        partialUpdatedPosition.setId(position.getId());

        partialUpdatedPosition.averageCost(UPDATED_AVERAGE_COST).lastSyncedAt(UPDATED_LAST_SYNCED_AT);

        restPositionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPosition.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPosition))
            )
            .andExpect(status().isOk());

        // Validate the Position in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPositionUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedPosition, position), getPersistedPosition(position));
    }

    @Test
    @Transactional
    void fullUpdatePositionWithPatch() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the position using partial update
        Position partialUpdatedPosition = new Position();
        partialUpdatedPosition.setId(position.getId());

        partialUpdatedPosition
            .quantity(UPDATED_QUANTITY)
            .averageCost(UPDATED_AVERAGE_COST)
            .currentValue(UPDATED_CURRENT_VALUE)
            .currency(UPDATED_CURRENCY)
            .lastSyncedAt(UPDATED_LAST_SYNCED_AT);

        restPositionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPosition.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPosition))
            )
            .andExpect(status().isOk());

        // Validate the Position in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPositionUpdatableFieldsEquals(partialUpdatedPosition, getPersistedPosition(partialUpdatedPosition));
    }

    @Test
    @Transactional
    void patchNonExistingPosition() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        position.setId(UUID.randomUUID());

        // Create the Position
        PositionDTO positionDTO = positionMapper.toDto(position);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPositionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, positionDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(positionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Position in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPosition() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        position.setId(UUID.randomUUID());

        // Create the Position
        PositionDTO positionDTO = positionMapper.toDto(position);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPositionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(positionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Position in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPosition() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        position.setId(UUID.randomUUID());

        // Create the Position
        PositionDTO positionDTO = positionMapper.toDto(position);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPositionMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(positionDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Position in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deletePosition() throws Exception {
        // Initialize the database
        insertedPosition = positionRepository.saveAndFlush(position);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the position
        restPositionMockMvc
            .perform(delete(ENTITY_API_URL_ID, position.getId().toString()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return positionRepository.count();
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

    protected Position getPersistedPosition(Position position) {
        return positionRepository.findById(position.getId()).orElseThrow();
    }

    protected void assertPersistedPositionToMatchAllProperties(Position expectedPosition) {
        assertPositionAllPropertiesEquals(expectedPosition, getPersistedPosition(expectedPosition));
    }

    protected void assertPersistedPositionToMatchUpdatableProperties(Position expectedPosition) {
        assertPositionAllUpdatablePropertiesEquals(expectedPosition, getPersistedPosition(expectedPosition));
    }
}
