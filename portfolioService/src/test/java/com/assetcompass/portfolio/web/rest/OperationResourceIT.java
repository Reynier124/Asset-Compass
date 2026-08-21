package com.assetcompass.portfolio.web.rest;

import static com.assetcompass.portfolio.domain.OperationAsserts.*;
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
import com.assetcompass.portfolio.domain.Operation;
import com.assetcompass.portfolio.domain.Operation;
import com.assetcompass.portfolio.domain.enumeration.OperationType;
import com.assetcompass.portfolio.repository.OperationRepository;
import com.assetcompass.portfolio.service.dto.OperationDTO;
import com.assetcompass.portfolio.service.mapper.OperationMapper;
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
 * Integration tests for the {@link OperationResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class OperationResourceIT {

    private static final OperationType DEFAULT_TYPE = OperationType.BUY;
    private static final OperationType UPDATED_TYPE = OperationType.SELL;

    private static final LocalDate DEFAULT_OPERATION_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_OPERATION_DATE = LocalDate.parse("2023-12-02");
    private static final LocalDate SMALLER_OPERATION_DATE = LocalDate.ofEpochDay(-1L);

    private static final BigDecimal DEFAULT_QUANTITY = new BigDecimal(1);
    private static final BigDecimal UPDATED_QUANTITY = new BigDecimal(2);
    private static final BigDecimal SMALLER_QUANTITY = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_PRICE = new BigDecimal(1);
    private static final BigDecimal UPDATED_PRICE = new BigDecimal(2);
    private static final BigDecimal SMALLER_PRICE = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_AMOUNT = new BigDecimal(1);
    private static final BigDecimal UPDATED_AMOUNT = new BigDecimal(2);
    private static final BigDecimal SMALLER_AMOUNT = new BigDecimal(1 - 1);

    private static final String DEFAULT_CURRENCY = "AAAAAAAAAA";
    private static final String UPDATED_CURRENCY = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_UNDERLYING_PRICE = new BigDecimal(1);
    private static final BigDecimal UPDATED_UNDERLYING_PRICE = new BigDecimal(2);
    private static final BigDecimal SMALLER_UNDERLYING_PRICE = new BigDecimal(1 - 1);

    private static final BigDecimal DEFAULT_COMMISSION = new BigDecimal(1);
    private static final BigDecimal UPDATED_COMMISSION = new BigDecimal(2);
    private static final BigDecimal SMALLER_COMMISSION = new BigDecimal(1 - 1);

    private static final String ENTITY_API_URL = "/api/operations";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private OperationRepository operationRepository;

    @Autowired
    private OperationMapper operationMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restOperationMockMvc;

    private Operation operation;

    private Operation insertedOperation;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Operation createEntity(EntityManager em) {
        Operation operation = new Operation()
            .type(DEFAULT_TYPE)
            .operationDate(DEFAULT_OPERATION_DATE)
            .quantity(DEFAULT_QUANTITY)
            .price(DEFAULT_PRICE)
            .amount(DEFAULT_AMOUNT)
            .currency(DEFAULT_CURRENCY)
            .underlyingPrice(DEFAULT_UNDERLYING_PRICE)
            .commission(DEFAULT_COMMISSION);
        // Add required entity
        BrokerAccount brokerAccount;
        if (TestUtil.findAll(em, BrokerAccount.class).isEmpty()) {
            brokerAccount = BrokerAccountResourceIT.createEntity(em);
            em.persist(brokerAccount);
            em.flush();
        } else {
            brokerAccount = TestUtil.findAll(em, BrokerAccount.class).get(0);
        }
        operation.setAccount(brokerAccount);
        // Add required entity
        Asset asset;
        if (TestUtil.findAll(em, Asset.class).isEmpty()) {
            asset = AssetResourceIT.createEntity();
            em.persist(asset);
            em.flush();
        } else {
            asset = TestUtil.findAll(em, Asset.class).get(0);
        }
        operation.setAsset(asset);
        return operation;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Operation createUpdatedEntity(EntityManager em) {
        Operation updatedOperation = new Operation()
            .type(UPDATED_TYPE)
            .operationDate(UPDATED_OPERATION_DATE)
            .quantity(UPDATED_QUANTITY)
            .price(UPDATED_PRICE)
            .amount(UPDATED_AMOUNT)
            .currency(UPDATED_CURRENCY)
            .underlyingPrice(UPDATED_UNDERLYING_PRICE)
            .commission(UPDATED_COMMISSION);
        // Add required entity
        BrokerAccount brokerAccount;
        if (TestUtil.findAll(em, BrokerAccount.class).isEmpty()) {
            brokerAccount = BrokerAccountResourceIT.createUpdatedEntity(em);
            em.persist(brokerAccount);
            em.flush();
        } else {
            brokerAccount = TestUtil.findAll(em, BrokerAccount.class).get(0);
        }
        updatedOperation.setAccount(brokerAccount);
        // Add required entity
        Asset asset;
        if (TestUtil.findAll(em, Asset.class).isEmpty()) {
            asset = AssetResourceIT.createUpdatedEntity();
            em.persist(asset);
            em.flush();
        } else {
            asset = TestUtil.findAll(em, Asset.class).get(0);
        }
        updatedOperation.setAsset(asset);
        return updatedOperation;
    }

    @BeforeEach
    void initTest() {
        operation = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedOperation != null) {
            operationRepository.delete(insertedOperation);
            insertedOperation = null;
        }
    }

    @Test
    @Transactional
    void createOperation() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Operation
        OperationDTO operationDTO = operationMapper.toDto(operation);
        var returnedOperationDTO = om.readValue(
            restOperationMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(operationDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            OperationDTO.class
        );

        // Validate the Operation in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedOperation = operationMapper.toEntity(returnedOperationDTO);
        assertOperationUpdatableFieldsEquals(returnedOperation, getPersistedOperation(returnedOperation));

        insertedOperation = returnedOperation;
    }

    @Test
    @Transactional
    void createOperationWithExistingId() throws Exception {
        // Create the Operation with an existing ID
        insertedOperation = operationRepository.saveAndFlush(operation);
        OperationDTO operationDTO = operationMapper.toDto(operation);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restOperationMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(operationDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Operation in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        operation.setType(null);

        // Create the Operation, which fails.
        OperationDTO operationDTO = operationMapper.toDto(operation);

        restOperationMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(operationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkOperationDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        operation.setOperationDate(null);

        // Create the Operation, which fails.
        OperationDTO operationDTO = operationMapper.toDto(operation);

        restOperationMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(operationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkQuantityIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        operation.setQuantity(null);

        // Create the Operation, which fails.
        OperationDTO operationDTO = operationMapper.toDto(operation);

        restOperationMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(operationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPriceIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        operation.setPrice(null);

        // Create the Operation, which fails.
        OperationDTO operationDTO = operationMapper.toDto(operation);

        restOperationMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(operationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkAmountIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        operation.setAmount(null);

        // Create the Operation, which fails.
        OperationDTO operationDTO = operationMapper.toDto(operation);

        restOperationMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(operationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkCurrencyIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        operation.setCurrency(null);

        // Create the Operation, which fails.
        OperationDTO operationDTO = operationMapper.toDto(operation);

        restOperationMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(operationDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllOperations() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList
        restOperationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(operation.getId().toString())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].operationDate").value(hasItem(DEFAULT_OPERATION_DATE.toString())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(sameNumber(DEFAULT_QUANTITY))))
            .andExpect(jsonPath("$.[*].price").value(hasItem(sameNumber(DEFAULT_PRICE))))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(sameNumber(DEFAULT_AMOUNT))))
            .andExpect(jsonPath("$.[*].currency").value(hasItem(DEFAULT_CURRENCY)))
            .andExpect(jsonPath("$.[*].underlyingPrice").value(hasItem(sameNumber(DEFAULT_UNDERLYING_PRICE))))
            .andExpect(jsonPath("$.[*].commission").value(hasItem(sameNumber(DEFAULT_COMMISSION))));
    }

    @Test
    @Transactional
    void getOperation() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get the operation
        restOperationMockMvc
            .perform(get(ENTITY_API_URL_ID, operation.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(operation.getId().toString()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.operationDate").value(DEFAULT_OPERATION_DATE.toString()))
            .andExpect(jsonPath("$.quantity").value(sameNumber(DEFAULT_QUANTITY)))
            .andExpect(jsonPath("$.price").value(sameNumber(DEFAULT_PRICE)))
            .andExpect(jsonPath("$.amount").value(sameNumber(DEFAULT_AMOUNT)))
            .andExpect(jsonPath("$.currency").value(DEFAULT_CURRENCY))
            .andExpect(jsonPath("$.underlyingPrice").value(sameNumber(DEFAULT_UNDERLYING_PRICE)))
            .andExpect(jsonPath("$.commission").value(sameNumber(DEFAULT_COMMISSION)));
    }

    @Test
    @Transactional
    void getOperationsByIdFiltering() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        UUID id = operation.getId();

        defaultOperationFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    @Transactional
    void getAllOperationsByTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where type equals to
        defaultOperationFiltering("type.equals=" + DEFAULT_TYPE, "type.equals=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllOperationsByTypeIsInShouldWork() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where type in
        defaultOperationFiltering("type.in=" + DEFAULT_TYPE + "," + UPDATED_TYPE, "type.in=" + UPDATED_TYPE);
    }

    @Test
    @Transactional
    void getAllOperationsByTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where type is not null
        defaultOperationFiltering("type.specified=true", "type.specified=false");
    }

    @Test
    @Transactional
    void getAllOperationsByOperationDateIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where operationDate equals to
        defaultOperationFiltering("operationDate.equals=" + DEFAULT_OPERATION_DATE, "operationDate.equals=" + UPDATED_OPERATION_DATE);
    }

    @Test
    @Transactional
    void getAllOperationsByOperationDateIsInShouldWork() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where operationDate in
        defaultOperationFiltering(
            "operationDate.in=" + DEFAULT_OPERATION_DATE + "," + UPDATED_OPERATION_DATE,
            "operationDate.in=" + UPDATED_OPERATION_DATE
        );
    }

    @Test
    @Transactional
    void getAllOperationsByOperationDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where operationDate is not null
        defaultOperationFiltering("operationDate.specified=true", "operationDate.specified=false");
    }

    @Test
    @Transactional
    void getAllOperationsByOperationDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where operationDate is greater than or equal to
        defaultOperationFiltering(
            "operationDate.greaterThanOrEqual=" + DEFAULT_OPERATION_DATE,
            "operationDate.greaterThanOrEqual=" + UPDATED_OPERATION_DATE
        );
    }

    @Test
    @Transactional
    void getAllOperationsByOperationDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where operationDate is less than or equal to
        defaultOperationFiltering(
            "operationDate.lessThanOrEqual=" + DEFAULT_OPERATION_DATE,
            "operationDate.lessThanOrEqual=" + SMALLER_OPERATION_DATE
        );
    }

    @Test
    @Transactional
    void getAllOperationsByOperationDateIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where operationDate is less than
        defaultOperationFiltering("operationDate.lessThan=" + UPDATED_OPERATION_DATE, "operationDate.lessThan=" + DEFAULT_OPERATION_DATE);
    }

    @Test
    @Transactional
    void getAllOperationsByOperationDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where operationDate is greater than
        defaultOperationFiltering(
            "operationDate.greaterThan=" + SMALLER_OPERATION_DATE,
            "operationDate.greaterThan=" + DEFAULT_OPERATION_DATE
        );
    }

    @Test
    @Transactional
    void getAllOperationsByQuantityIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where quantity equals to
        defaultOperationFiltering("quantity.equals=" + DEFAULT_QUANTITY, "quantity.equals=" + UPDATED_QUANTITY);
    }

    @Test
    @Transactional
    void getAllOperationsByQuantityIsInShouldWork() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where quantity in
        defaultOperationFiltering("quantity.in=" + DEFAULT_QUANTITY + "," + UPDATED_QUANTITY, "quantity.in=" + UPDATED_QUANTITY);
    }

    @Test
    @Transactional
    void getAllOperationsByQuantityIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where quantity is not null
        defaultOperationFiltering("quantity.specified=true", "quantity.specified=false");
    }

    @Test
    @Transactional
    void getAllOperationsByQuantityIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where quantity is greater than or equal to
        defaultOperationFiltering("quantity.greaterThanOrEqual=" + DEFAULT_QUANTITY, "quantity.greaterThanOrEqual=" + UPDATED_QUANTITY);
    }

    @Test
    @Transactional
    void getAllOperationsByQuantityIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where quantity is less than or equal to
        defaultOperationFiltering("quantity.lessThanOrEqual=" + DEFAULT_QUANTITY, "quantity.lessThanOrEqual=" + SMALLER_QUANTITY);
    }

    @Test
    @Transactional
    void getAllOperationsByQuantityIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where quantity is less than
        defaultOperationFiltering("quantity.lessThan=" + UPDATED_QUANTITY, "quantity.lessThan=" + DEFAULT_QUANTITY);
    }

    @Test
    @Transactional
    void getAllOperationsByQuantityIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where quantity is greater than
        defaultOperationFiltering("quantity.greaterThan=" + SMALLER_QUANTITY, "quantity.greaterThan=" + DEFAULT_QUANTITY);
    }

    @Test
    @Transactional
    void getAllOperationsByPriceIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where price equals to
        defaultOperationFiltering("price.equals=" + DEFAULT_PRICE, "price.equals=" + UPDATED_PRICE);
    }

    @Test
    @Transactional
    void getAllOperationsByPriceIsInShouldWork() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where price in
        defaultOperationFiltering("price.in=" + DEFAULT_PRICE + "," + UPDATED_PRICE, "price.in=" + UPDATED_PRICE);
    }

    @Test
    @Transactional
    void getAllOperationsByPriceIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where price is not null
        defaultOperationFiltering("price.specified=true", "price.specified=false");
    }

    @Test
    @Transactional
    void getAllOperationsByPriceIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where price is greater than or equal to
        defaultOperationFiltering("price.greaterThanOrEqual=" + DEFAULT_PRICE, "price.greaterThanOrEqual=" + UPDATED_PRICE);
    }

    @Test
    @Transactional
    void getAllOperationsByPriceIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where price is less than or equal to
        defaultOperationFiltering("price.lessThanOrEqual=" + DEFAULT_PRICE, "price.lessThanOrEqual=" + SMALLER_PRICE);
    }

    @Test
    @Transactional
    void getAllOperationsByPriceIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where price is less than
        defaultOperationFiltering("price.lessThan=" + UPDATED_PRICE, "price.lessThan=" + DEFAULT_PRICE);
    }

    @Test
    @Transactional
    void getAllOperationsByPriceIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where price is greater than
        defaultOperationFiltering("price.greaterThan=" + SMALLER_PRICE, "price.greaterThan=" + DEFAULT_PRICE);
    }

    @Test
    @Transactional
    void getAllOperationsByAmountIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where amount equals to
        defaultOperationFiltering("amount.equals=" + DEFAULT_AMOUNT, "amount.equals=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllOperationsByAmountIsInShouldWork() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where amount in
        defaultOperationFiltering("amount.in=" + DEFAULT_AMOUNT + "," + UPDATED_AMOUNT, "amount.in=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllOperationsByAmountIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where amount is not null
        defaultOperationFiltering("amount.specified=true", "amount.specified=false");
    }

    @Test
    @Transactional
    void getAllOperationsByAmountIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where amount is greater than or equal to
        defaultOperationFiltering("amount.greaterThanOrEqual=" + DEFAULT_AMOUNT, "amount.greaterThanOrEqual=" + UPDATED_AMOUNT);
    }

    @Test
    @Transactional
    void getAllOperationsByAmountIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where amount is less than or equal to
        defaultOperationFiltering("amount.lessThanOrEqual=" + DEFAULT_AMOUNT, "amount.lessThanOrEqual=" + SMALLER_AMOUNT);
    }

    @Test
    @Transactional
    void getAllOperationsByAmountIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where amount is less than
        defaultOperationFiltering("amount.lessThan=" + UPDATED_AMOUNT, "amount.lessThan=" + DEFAULT_AMOUNT);
    }

    @Test
    @Transactional
    void getAllOperationsByAmountIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where amount is greater than
        defaultOperationFiltering("amount.greaterThan=" + SMALLER_AMOUNT, "amount.greaterThan=" + DEFAULT_AMOUNT);
    }

    @Test
    @Transactional
    void getAllOperationsByCurrencyIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where currency equals to
        defaultOperationFiltering("currency.equals=" + DEFAULT_CURRENCY, "currency.equals=" + UPDATED_CURRENCY);
    }

    @Test
    @Transactional
    void getAllOperationsByCurrencyIsInShouldWork() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where currency in
        defaultOperationFiltering("currency.in=" + DEFAULT_CURRENCY + "," + UPDATED_CURRENCY, "currency.in=" + UPDATED_CURRENCY);
    }

    @Test
    @Transactional
    void getAllOperationsByCurrencyIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where currency is not null
        defaultOperationFiltering("currency.specified=true", "currency.specified=false");
    }

    @Test
    @Transactional
    void getAllOperationsByCurrencyContainsSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where currency contains
        defaultOperationFiltering("currency.contains=" + DEFAULT_CURRENCY, "currency.contains=" + UPDATED_CURRENCY);
    }

    @Test
    @Transactional
    void getAllOperationsByCurrencyNotContainsSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where currency does not contain
        defaultOperationFiltering("currency.doesNotContain=" + UPDATED_CURRENCY, "currency.doesNotContain=" + DEFAULT_CURRENCY);
    }

    @Test
    @Transactional
    void getAllOperationsByUnderlyingPriceIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where underlyingPrice equals to
        defaultOperationFiltering(
            "underlyingPrice.equals=" + DEFAULT_UNDERLYING_PRICE,
            "underlyingPrice.equals=" + UPDATED_UNDERLYING_PRICE
        );
    }

    @Test
    @Transactional
    void getAllOperationsByUnderlyingPriceIsInShouldWork() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where underlyingPrice in
        defaultOperationFiltering(
            "underlyingPrice.in=" + DEFAULT_UNDERLYING_PRICE + "," + UPDATED_UNDERLYING_PRICE,
            "underlyingPrice.in=" + UPDATED_UNDERLYING_PRICE
        );
    }

    @Test
    @Transactional
    void getAllOperationsByUnderlyingPriceIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where underlyingPrice is not null
        defaultOperationFiltering("underlyingPrice.specified=true", "underlyingPrice.specified=false");
    }

    @Test
    @Transactional
    void getAllOperationsByUnderlyingPriceIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where underlyingPrice is greater than or equal to
        defaultOperationFiltering(
            "underlyingPrice.greaterThanOrEqual=" + DEFAULT_UNDERLYING_PRICE,
            "underlyingPrice.greaterThanOrEqual=" + UPDATED_UNDERLYING_PRICE
        );
    }

    @Test
    @Transactional
    void getAllOperationsByUnderlyingPriceIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where underlyingPrice is less than or equal to
        defaultOperationFiltering(
            "underlyingPrice.lessThanOrEqual=" + DEFAULT_UNDERLYING_PRICE,
            "underlyingPrice.lessThanOrEqual=" + SMALLER_UNDERLYING_PRICE
        );
    }

    @Test
    @Transactional
    void getAllOperationsByUnderlyingPriceIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where underlyingPrice is less than
        defaultOperationFiltering(
            "underlyingPrice.lessThan=" + UPDATED_UNDERLYING_PRICE,
            "underlyingPrice.lessThan=" + DEFAULT_UNDERLYING_PRICE
        );
    }

    @Test
    @Transactional
    void getAllOperationsByUnderlyingPriceIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where underlyingPrice is greater than
        defaultOperationFiltering(
            "underlyingPrice.greaterThan=" + SMALLER_UNDERLYING_PRICE,
            "underlyingPrice.greaterThan=" + DEFAULT_UNDERLYING_PRICE
        );
    }

    @Test
    @Transactional
    void getAllOperationsByCommissionIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where commission equals to
        defaultOperationFiltering("commission.equals=" + DEFAULT_COMMISSION, "commission.equals=" + UPDATED_COMMISSION);
    }

    @Test
    @Transactional
    void getAllOperationsByCommissionIsInShouldWork() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where commission in
        defaultOperationFiltering("commission.in=" + DEFAULT_COMMISSION + "," + UPDATED_COMMISSION, "commission.in=" + UPDATED_COMMISSION);
    }

    @Test
    @Transactional
    void getAllOperationsByCommissionIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where commission is not null
        defaultOperationFiltering("commission.specified=true", "commission.specified=false");
    }

    @Test
    @Transactional
    void getAllOperationsByCommissionIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where commission is greater than or equal to
        defaultOperationFiltering(
            "commission.greaterThanOrEqual=" + DEFAULT_COMMISSION,
            "commission.greaterThanOrEqual=" + UPDATED_COMMISSION
        );
    }

    @Test
    @Transactional
    void getAllOperationsByCommissionIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where commission is less than or equal to
        defaultOperationFiltering("commission.lessThanOrEqual=" + DEFAULT_COMMISSION, "commission.lessThanOrEqual=" + SMALLER_COMMISSION);
    }

    @Test
    @Transactional
    void getAllOperationsByCommissionIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where commission is less than
        defaultOperationFiltering("commission.lessThan=" + UPDATED_COMMISSION, "commission.lessThan=" + DEFAULT_COMMISSION);
    }

    @Test
    @Transactional
    void getAllOperationsByCommissionIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        // Get all the operationList where commission is greater than
        defaultOperationFiltering("commission.greaterThan=" + SMALLER_COMMISSION, "commission.greaterThan=" + DEFAULT_COMMISSION);
    }

    @Test
    @Transactional
    void getAllOperationsByAccountIsEqualToSomething() throws Exception {
        BrokerAccount account;
        if (TestUtil.findAll(em, BrokerAccount.class).isEmpty()) {
            operationRepository.saveAndFlush(operation);
            account = BrokerAccountResourceIT.createEntity(em);
        } else {
            account = TestUtil.findAll(em, BrokerAccount.class).get(0);
        }
        em.persist(account);
        em.flush();
        operation.setAccount(account);
        operationRepository.saveAndFlush(operation);
        UUID accountId = account.getId();
        // Get all the operationList where account equals to accountId
        defaultOperationShouldBeFound("accountId.equals=" + accountId);

        // Get all the operationList where account equals to UUID.randomUUID()
        defaultOperationShouldNotBeFound("accountId.equals=" + UUID.randomUUID());
    }

    @Test
    @Transactional
    void getAllOperationsByAssetIsEqualToSomething() throws Exception {
        Asset asset;
        if (TestUtil.findAll(em, Asset.class).isEmpty()) {
            operationRepository.saveAndFlush(operation);
            asset = AssetResourceIT.createEntity();
        } else {
            asset = TestUtil.findAll(em, Asset.class).get(0);
        }
        em.persist(asset);
        em.flush();
        operation.setAsset(asset);
        operationRepository.saveAndFlush(operation);
        UUID assetId = asset.getId();
        // Get all the operationList where asset equals to assetId
        defaultOperationShouldBeFound("assetId.equals=" + assetId);

        // Get all the operationList where asset equals to UUID.randomUUID()
        defaultOperationShouldNotBeFound("assetId.equals=" + UUID.randomUUID());
    }

    @Test
    @Transactional
    void getAllOperationsByClosesOperationIsEqualToSomething() throws Exception {
        Operation closesOperation;
        if (TestUtil.findAll(em, Operation.class).isEmpty()) {
            operationRepository.saveAndFlush(operation);
            closesOperation = OperationResourceIT.createEntity(em);
        } else {
            closesOperation = TestUtil.findAll(em, Operation.class).get(0);
        }
        em.persist(closesOperation);
        em.flush();
        operation.setClosesOperation(closesOperation);
        operationRepository.saveAndFlush(operation);
        UUID closesOperationId = closesOperation.getId();
        // Get all the operationList where closesOperation equals to closesOperationId
        defaultOperationShouldBeFound("closesOperationId.equals=" + closesOperationId);

        // Get all the operationList where closesOperation equals to UUID.randomUUID()
        defaultOperationShouldNotBeFound("closesOperationId.equals=" + UUID.randomUUID());
    }

    private void defaultOperationFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultOperationShouldBeFound(shouldBeFound);
        defaultOperationShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultOperationShouldBeFound(String filter) throws Exception {
        restOperationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(operation.getId().toString())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
            .andExpect(jsonPath("$.[*].operationDate").value(hasItem(DEFAULT_OPERATION_DATE.toString())))
            .andExpect(jsonPath("$.[*].quantity").value(hasItem(sameNumber(DEFAULT_QUANTITY))))
            .andExpect(jsonPath("$.[*].price").value(hasItem(sameNumber(DEFAULT_PRICE))))
            .andExpect(jsonPath("$.[*].amount").value(hasItem(sameNumber(DEFAULT_AMOUNT))))
            .andExpect(jsonPath("$.[*].currency").value(hasItem(DEFAULT_CURRENCY)))
            .andExpect(jsonPath("$.[*].underlyingPrice").value(hasItem(sameNumber(DEFAULT_UNDERLYING_PRICE))))
            .andExpect(jsonPath("$.[*].commission").value(hasItem(sameNumber(DEFAULT_COMMISSION))));

        // Check, that the count call also returns 1
        restOperationMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultOperationShouldNotBeFound(String filter) throws Exception {
        restOperationMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restOperationMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingOperation() throws Exception {
        // Get the operation
        restOperationMockMvc.perform(get(ENTITY_API_URL_ID, UUID.randomUUID().toString())).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingOperation() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the operation
        Operation updatedOperation = operationRepository.findById(operation.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedOperation are not directly saved in db
        em.detach(updatedOperation);
        updatedOperation
            .type(UPDATED_TYPE)
            .operationDate(UPDATED_OPERATION_DATE)
            .quantity(UPDATED_QUANTITY)
            .price(UPDATED_PRICE)
            .amount(UPDATED_AMOUNT)
            .currency(UPDATED_CURRENCY)
            .underlyingPrice(UPDATED_UNDERLYING_PRICE)
            .commission(UPDATED_COMMISSION);
        OperationDTO operationDTO = operationMapper.toDto(updatedOperation);

        restOperationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, operationDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(operationDTO))
            )
            .andExpect(status().isOk());

        // Validate the Operation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedOperationToMatchAllProperties(updatedOperation);
    }

    @Test
    @Transactional
    void putNonExistingOperation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        operation.setId(UUID.randomUUID());

        // Create the Operation
        OperationDTO operationDTO = operationMapper.toDto(operation);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restOperationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, operationDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(operationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Operation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchOperation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        operation.setId(UUID.randomUUID());

        // Create the Operation
        OperationDTO operationDTO = operationMapper.toDto(operation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOperationMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(operationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Operation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamOperation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        operation.setId(UUID.randomUUID());

        // Create the Operation
        OperationDTO operationDTO = operationMapper.toDto(operation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOperationMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(operationDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Operation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateOperationWithPatch() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the operation using partial update
        Operation partialUpdatedOperation = new Operation();
        partialUpdatedOperation.setId(operation.getId());

        partialUpdatedOperation
            .type(UPDATED_TYPE)
            .amount(UPDATED_AMOUNT)
            .currency(UPDATED_CURRENCY)
            .underlyingPrice(UPDATED_UNDERLYING_PRICE)
            .commission(UPDATED_COMMISSION);

        restOperationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedOperation.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedOperation))
            )
            .andExpect(status().isOk());

        // Validate the Operation in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertOperationUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedOperation, operation),
            getPersistedOperation(operation)
        );
    }

    @Test
    @Transactional
    void fullUpdateOperationWithPatch() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the operation using partial update
        Operation partialUpdatedOperation = new Operation();
        partialUpdatedOperation.setId(operation.getId());

        partialUpdatedOperation
            .type(UPDATED_TYPE)
            .operationDate(UPDATED_OPERATION_DATE)
            .quantity(UPDATED_QUANTITY)
            .price(UPDATED_PRICE)
            .amount(UPDATED_AMOUNT)
            .currency(UPDATED_CURRENCY)
            .underlyingPrice(UPDATED_UNDERLYING_PRICE)
            .commission(UPDATED_COMMISSION);

        restOperationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedOperation.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedOperation))
            )
            .andExpect(status().isOk());

        // Validate the Operation in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertOperationUpdatableFieldsEquals(partialUpdatedOperation, getPersistedOperation(partialUpdatedOperation));
    }

    @Test
    @Transactional
    void patchNonExistingOperation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        operation.setId(UUID.randomUUID());

        // Create the Operation
        OperationDTO operationDTO = operationMapper.toDto(operation);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restOperationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, operationDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(operationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Operation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchOperation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        operation.setId(UUID.randomUUID());

        // Create the Operation
        OperationDTO operationDTO = operationMapper.toDto(operation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOperationMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(operationDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Operation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamOperation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        operation.setId(UUID.randomUUID());

        // Create the Operation
        OperationDTO operationDTO = operationMapper.toDto(operation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restOperationMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(operationDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Operation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteOperation() throws Exception {
        // Initialize the database
        insertedOperation = operationRepository.saveAndFlush(operation);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the operation
        restOperationMockMvc
            .perform(delete(ENTITY_API_URL_ID, operation.getId().toString()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return operationRepository.count();
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

    protected Operation getPersistedOperation(Operation operation) {
        return operationRepository.findById(operation.getId()).orElseThrow();
    }

    protected void assertPersistedOperationToMatchAllProperties(Operation expectedOperation) {
        assertOperationAllPropertiesEquals(expectedOperation, getPersistedOperation(expectedOperation));
    }

    protected void assertPersistedOperationToMatchUpdatableProperties(Operation expectedOperation) {
        assertOperationAllUpdatablePropertiesEquals(expectedOperation, getPersistedOperation(expectedOperation));
    }
}
