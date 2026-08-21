package com.assetcompass.gateway.web.rest;

import static com.assetcompass.gateway.domain.OperationAsserts.*;
import static com.assetcompass.gateway.web.rest.TestUtil.createUpdateProxyForBean;
import static com.assetcompass.gateway.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import com.assetcompass.gateway.IntegrationTest;
import com.assetcompass.gateway.domain.Asset;
import com.assetcompass.gateway.domain.BrokerAccount;
import com.assetcompass.gateway.domain.Operation;
import com.assetcompass.gateway.domain.Operation;
import com.assetcompass.gateway.domain.enumeration.OperationType;
import com.assetcompass.gateway.repository.AssetRepository;
import com.assetcompass.gateway.repository.BrokerAccountRepository;
import com.assetcompass.gateway.repository.EntityManager;
import com.assetcompass.gateway.repository.OperationRepository;
import com.assetcompass.gateway.repository.OperationRepository;
import com.assetcompass.gateway.service.dto.OperationDTO;
import com.assetcompass.gateway.service.mapper.OperationMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
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
 * Integration tests for the {@link OperationResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class OperationResourceIT {

    private static final OperationType DEFAULT_TYPE = OperationType.BUY;
    private static final OperationType UPDATED_TYPE = OperationType.SELL;

    private static final LocalDate DEFAULT_OPERATION_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_OPERATION_DATE = LocalDate.parse("2023-12-22");
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
    private WebTestClient webTestClient;

    private Operation operation;

    private Operation insertedOperation;

    @Autowired
    private BrokerAccountRepository brokerAccountRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private OperationRepository operationRepository;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Operation createEntity(EntityManager em) {
        Operation operation = new Operation()
            .id(UUID.randomUUID())
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
        brokerAccount = em.insert(BrokerAccountResourceIT.createEntity(em)).block();
        operation.setAccount(brokerAccount);
        // Add required entity
        Asset asset;
        asset = em.insert(AssetResourceIT.createEntity()).block();
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
            .id(UUID.randomUUID())
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
        brokerAccount = em.insert(BrokerAccountResourceIT.createUpdatedEntity(em)).block();
        updatedOperation.setAccount(brokerAccount);
        // Add required entity
        Asset asset;
        asset = em.insert(AssetResourceIT.createUpdatedEntity()).block();
        updatedOperation.setAsset(asset);
        return updatedOperation;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Operation.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
        BrokerAccountResourceIT.deleteEntities(em);
        AssetResourceIT.deleteEntities(em);
    }

    @BeforeEach
    void setupCsrf() {
        webTestClient = webTestClient.mutateWith(csrf());
    }

    @BeforeEach
    void initTest() {
        operation = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedOperation != null) {
            operationRepository.delete(insertedOperation).block();
            insertedOperation = null;
        }
        deleteEntities(em);
    }

    @Test
    void createOperation() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        operation.setId(null);
        // Create the Operation
        OperationDTO operationDTO = operationMapper.toDto(operation);
        var returnedOperationDTO = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(operationDTO))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(OperationDTO.class)
            .returnResult()
            .getResponseBody();

        // Validate the Operation in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedOperation = operationMapper.toEntity(returnedOperationDTO);
        assertOperationUpdatableFieldsEquals(returnedOperation, getPersistedOperation(returnedOperation));

        insertedOperation = returnedOperation;
    }

    @Test
    void createOperationWithExistingId() throws Exception {
        // Create the Operation with an existing ID
        insertedOperation = operationRepository.save(operation).block();
        OperationDTO operationDTO = operationMapper.toDto(operation);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(operationDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Operation in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkTypeIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        operation.setType(null);

        // Create the Operation, which fails.
        OperationDTO operationDTO = operationMapper.toDto(operation);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(operationDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkOperationDateIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        operation.setOperationDate(null);

        // Create the Operation, which fails.
        OperationDTO operationDTO = operationMapper.toDto(operation);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(operationDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkQuantityIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        operation.setQuantity(null);

        // Create the Operation, which fails.
        OperationDTO operationDTO = operationMapper.toDto(operation);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(operationDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkPriceIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        operation.setPrice(null);

        // Create the Operation, which fails.
        OperationDTO operationDTO = operationMapper.toDto(operation);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(operationDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkAmountIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        operation.setAmount(null);

        // Create the Operation, which fails.
        OperationDTO operationDTO = operationMapper.toDto(operation);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(operationDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void checkCurrencyIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        operation.setCurrency(null);

        // Create the Operation, which fails.
        OperationDTO operationDTO = operationMapper.toDto(operation);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(operationDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllOperations() {
        // Initialize the database
        operation.setId(UUID.randomUUID());
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList
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
            .value(hasItem(operation.getId().toString()))
            .jsonPath("$.[*].type")
            .value(hasItem(DEFAULT_TYPE.toString()))
            .jsonPath("$.[*].operationDate")
            .value(hasItem(DEFAULT_OPERATION_DATE.toString()))
            .jsonPath("$.[*].quantity")
            .value(hasItem(sameNumber(DEFAULT_QUANTITY)))
            .jsonPath("$.[*].price")
            .value(hasItem(sameNumber(DEFAULT_PRICE)))
            .jsonPath("$.[*].amount")
            .value(hasItem(sameNumber(DEFAULT_AMOUNT)))
            .jsonPath("$.[*].currency")
            .value(hasItem(DEFAULT_CURRENCY))
            .jsonPath("$.[*].underlyingPrice")
            .value(hasItem(sameNumber(DEFAULT_UNDERLYING_PRICE)))
            .jsonPath("$.[*].commission")
            .value(hasItem(sameNumber(DEFAULT_COMMISSION)));
    }

    @Test
    void getOperation() {
        // Initialize the database
        operation.setId(UUID.randomUUID());
        insertedOperation = operationRepository.save(operation).block();

        // Get the operation
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, operation.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(operation.getId().toString()))
            .jsonPath("$.type")
            .value(is(DEFAULT_TYPE.toString()))
            .jsonPath("$.operationDate")
            .value(is(DEFAULT_OPERATION_DATE.toString()))
            .jsonPath("$.quantity")
            .value(is(sameNumber(DEFAULT_QUANTITY)))
            .jsonPath("$.price")
            .value(is(sameNumber(DEFAULT_PRICE)))
            .jsonPath("$.amount")
            .value(is(sameNumber(DEFAULT_AMOUNT)))
            .jsonPath("$.currency")
            .value(is(DEFAULT_CURRENCY))
            .jsonPath("$.underlyingPrice")
            .value(is(sameNumber(DEFAULT_UNDERLYING_PRICE)))
            .jsonPath("$.commission")
            .value(is(sameNumber(DEFAULT_COMMISSION)));
    }

    @Test
    void getOperationsByIdFiltering() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        UUID id = operation.getId();

        defaultOperationFiltering("id.equals=" + id, "id.notEquals=" + id);
    }

    @Test
    void getAllOperationsByTypeIsEqualToSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where type equals to
        defaultOperationFiltering("type.equals=" + DEFAULT_TYPE, "type.equals=" + UPDATED_TYPE);
    }

    @Test
    void getAllOperationsByTypeIsInShouldWork() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where type in
        defaultOperationFiltering("type.in=" + DEFAULT_TYPE + "," + UPDATED_TYPE, "type.in=" + UPDATED_TYPE);
    }

    @Test
    void getAllOperationsByTypeIsNullOrNotNull() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where type is not null
        defaultOperationFiltering("type.specified=true", "type.specified=false");
    }

    @Test
    void getAllOperationsByOperationDateIsEqualToSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where operationDate equals to
        defaultOperationFiltering("operationDate.equals=" + DEFAULT_OPERATION_DATE, "operationDate.equals=" + UPDATED_OPERATION_DATE);
    }

    @Test
    void getAllOperationsByOperationDateIsInShouldWork() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where operationDate in
        defaultOperationFiltering(
            "operationDate.in=" + DEFAULT_OPERATION_DATE + "," + UPDATED_OPERATION_DATE,
            "operationDate.in=" + UPDATED_OPERATION_DATE
        );
    }

    @Test
    void getAllOperationsByOperationDateIsNullOrNotNull() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where operationDate is not null
        defaultOperationFiltering("operationDate.specified=true", "operationDate.specified=false");
    }

    @Test
    void getAllOperationsByOperationDateIsGreaterThanOrEqualToSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where operationDate is greater than or equal to
        defaultOperationFiltering(
            "operationDate.greaterThanOrEqual=" + DEFAULT_OPERATION_DATE,
            "operationDate.greaterThanOrEqual=" + UPDATED_OPERATION_DATE
        );
    }

    @Test
    void getAllOperationsByOperationDateIsLessThanOrEqualToSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where operationDate is less than or equal to
        defaultOperationFiltering(
            "operationDate.lessThanOrEqual=" + DEFAULT_OPERATION_DATE,
            "operationDate.lessThanOrEqual=" + SMALLER_OPERATION_DATE
        );
    }

    @Test
    void getAllOperationsByOperationDateIsLessThanSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where operationDate is less than
        defaultOperationFiltering("operationDate.lessThan=" + UPDATED_OPERATION_DATE, "operationDate.lessThan=" + DEFAULT_OPERATION_DATE);
    }

    @Test
    void getAllOperationsByOperationDateIsGreaterThanSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where operationDate is greater than
        defaultOperationFiltering(
            "operationDate.greaterThan=" + SMALLER_OPERATION_DATE,
            "operationDate.greaterThan=" + DEFAULT_OPERATION_DATE
        );
    }

    @Test
    void getAllOperationsByQuantityIsEqualToSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where quantity equals to
        defaultOperationFiltering("quantity.equals=" + DEFAULT_QUANTITY, "quantity.equals=" + UPDATED_QUANTITY);
    }

    @Test
    void getAllOperationsByQuantityIsInShouldWork() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where quantity in
        defaultOperationFiltering("quantity.in=" + DEFAULT_QUANTITY + "," + UPDATED_QUANTITY, "quantity.in=" + UPDATED_QUANTITY);
    }

    @Test
    void getAllOperationsByQuantityIsNullOrNotNull() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where quantity is not null
        defaultOperationFiltering("quantity.specified=true", "quantity.specified=false");
    }

    @Test
    void getAllOperationsByQuantityIsGreaterThanOrEqualToSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where quantity is greater than or equal to
        defaultOperationFiltering("quantity.greaterThanOrEqual=" + DEFAULT_QUANTITY, "quantity.greaterThanOrEqual=" + UPDATED_QUANTITY);
    }

    @Test
    void getAllOperationsByQuantityIsLessThanOrEqualToSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where quantity is less than or equal to
        defaultOperationFiltering("quantity.lessThanOrEqual=" + DEFAULT_QUANTITY, "quantity.lessThanOrEqual=" + SMALLER_QUANTITY);
    }

    @Test
    void getAllOperationsByQuantityIsLessThanSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where quantity is less than
        defaultOperationFiltering("quantity.lessThan=" + UPDATED_QUANTITY, "quantity.lessThan=" + DEFAULT_QUANTITY);
    }

    @Test
    void getAllOperationsByQuantityIsGreaterThanSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where quantity is greater than
        defaultOperationFiltering("quantity.greaterThan=" + SMALLER_QUANTITY, "quantity.greaterThan=" + DEFAULT_QUANTITY);
    }

    @Test
    void getAllOperationsByPriceIsEqualToSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where price equals to
        defaultOperationFiltering("price.equals=" + DEFAULT_PRICE, "price.equals=" + UPDATED_PRICE);
    }

    @Test
    void getAllOperationsByPriceIsInShouldWork() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where price in
        defaultOperationFiltering("price.in=" + DEFAULT_PRICE + "," + UPDATED_PRICE, "price.in=" + UPDATED_PRICE);
    }

    @Test
    void getAllOperationsByPriceIsNullOrNotNull() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where price is not null
        defaultOperationFiltering("price.specified=true", "price.specified=false");
    }

    @Test
    void getAllOperationsByPriceIsGreaterThanOrEqualToSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where price is greater than or equal to
        defaultOperationFiltering("price.greaterThanOrEqual=" + DEFAULT_PRICE, "price.greaterThanOrEqual=" + UPDATED_PRICE);
    }

    @Test
    void getAllOperationsByPriceIsLessThanOrEqualToSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where price is less than or equal to
        defaultOperationFiltering("price.lessThanOrEqual=" + DEFAULT_PRICE, "price.lessThanOrEqual=" + SMALLER_PRICE);
    }

    @Test
    void getAllOperationsByPriceIsLessThanSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where price is less than
        defaultOperationFiltering("price.lessThan=" + UPDATED_PRICE, "price.lessThan=" + DEFAULT_PRICE);
    }

    @Test
    void getAllOperationsByPriceIsGreaterThanSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where price is greater than
        defaultOperationFiltering("price.greaterThan=" + SMALLER_PRICE, "price.greaterThan=" + DEFAULT_PRICE);
    }

    @Test
    void getAllOperationsByAmountIsEqualToSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where amount equals to
        defaultOperationFiltering("amount.equals=" + DEFAULT_AMOUNT, "amount.equals=" + UPDATED_AMOUNT);
    }

    @Test
    void getAllOperationsByAmountIsInShouldWork() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where amount in
        defaultOperationFiltering("amount.in=" + DEFAULT_AMOUNT + "," + UPDATED_AMOUNT, "amount.in=" + UPDATED_AMOUNT);
    }

    @Test
    void getAllOperationsByAmountIsNullOrNotNull() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where amount is not null
        defaultOperationFiltering("amount.specified=true", "amount.specified=false");
    }

    @Test
    void getAllOperationsByAmountIsGreaterThanOrEqualToSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where amount is greater than or equal to
        defaultOperationFiltering("amount.greaterThanOrEqual=" + DEFAULT_AMOUNT, "amount.greaterThanOrEqual=" + UPDATED_AMOUNT);
    }

    @Test
    void getAllOperationsByAmountIsLessThanOrEqualToSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where amount is less than or equal to
        defaultOperationFiltering("amount.lessThanOrEqual=" + DEFAULT_AMOUNT, "amount.lessThanOrEqual=" + SMALLER_AMOUNT);
    }

    @Test
    void getAllOperationsByAmountIsLessThanSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where amount is less than
        defaultOperationFiltering("amount.lessThan=" + UPDATED_AMOUNT, "amount.lessThan=" + DEFAULT_AMOUNT);
    }

    @Test
    void getAllOperationsByAmountIsGreaterThanSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where amount is greater than
        defaultOperationFiltering("amount.greaterThan=" + SMALLER_AMOUNT, "amount.greaterThan=" + DEFAULT_AMOUNT);
    }

    @Test
    void getAllOperationsByCurrencyIsEqualToSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where currency equals to
        defaultOperationFiltering("currency.equals=" + DEFAULT_CURRENCY, "currency.equals=" + UPDATED_CURRENCY);
    }

    @Test
    void getAllOperationsByCurrencyIsInShouldWork() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where currency in
        defaultOperationFiltering("currency.in=" + DEFAULT_CURRENCY + "," + UPDATED_CURRENCY, "currency.in=" + UPDATED_CURRENCY);
    }

    @Test
    void getAllOperationsByCurrencyIsNullOrNotNull() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where currency is not null
        defaultOperationFiltering("currency.specified=true", "currency.specified=false");
    }

    @Test
    void getAllOperationsByCurrencyContainsSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where currency contains
        defaultOperationFiltering("currency.contains=" + DEFAULT_CURRENCY, "currency.contains=" + UPDATED_CURRENCY);
    }

    @Test
    void getAllOperationsByCurrencyNotContainsSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where currency does not contain
        defaultOperationFiltering("currency.doesNotContain=" + UPDATED_CURRENCY, "currency.doesNotContain=" + DEFAULT_CURRENCY);
    }

    @Test
    void getAllOperationsByUnderlyingPriceIsEqualToSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where underlyingPrice equals to
        defaultOperationFiltering(
            "underlyingPrice.equals=" + DEFAULT_UNDERLYING_PRICE,
            "underlyingPrice.equals=" + UPDATED_UNDERLYING_PRICE
        );
    }

    @Test
    void getAllOperationsByUnderlyingPriceIsInShouldWork() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where underlyingPrice in
        defaultOperationFiltering(
            "underlyingPrice.in=" + DEFAULT_UNDERLYING_PRICE + "," + UPDATED_UNDERLYING_PRICE,
            "underlyingPrice.in=" + UPDATED_UNDERLYING_PRICE
        );
    }

    @Test
    void getAllOperationsByUnderlyingPriceIsNullOrNotNull() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where underlyingPrice is not null
        defaultOperationFiltering("underlyingPrice.specified=true", "underlyingPrice.specified=false");
    }

    @Test
    void getAllOperationsByUnderlyingPriceIsGreaterThanOrEqualToSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where underlyingPrice is greater than or equal to
        defaultOperationFiltering(
            "underlyingPrice.greaterThanOrEqual=" + DEFAULT_UNDERLYING_PRICE,
            "underlyingPrice.greaterThanOrEqual=" + UPDATED_UNDERLYING_PRICE
        );
    }

    @Test
    void getAllOperationsByUnderlyingPriceIsLessThanOrEqualToSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where underlyingPrice is less than or equal to
        defaultOperationFiltering(
            "underlyingPrice.lessThanOrEqual=" + DEFAULT_UNDERLYING_PRICE,
            "underlyingPrice.lessThanOrEqual=" + SMALLER_UNDERLYING_PRICE
        );
    }

    @Test
    void getAllOperationsByUnderlyingPriceIsLessThanSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where underlyingPrice is less than
        defaultOperationFiltering(
            "underlyingPrice.lessThan=" + UPDATED_UNDERLYING_PRICE,
            "underlyingPrice.lessThan=" + DEFAULT_UNDERLYING_PRICE
        );
    }

    @Test
    void getAllOperationsByUnderlyingPriceIsGreaterThanSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where underlyingPrice is greater than
        defaultOperationFiltering(
            "underlyingPrice.greaterThan=" + SMALLER_UNDERLYING_PRICE,
            "underlyingPrice.greaterThan=" + DEFAULT_UNDERLYING_PRICE
        );
    }

    @Test
    void getAllOperationsByCommissionIsEqualToSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where commission equals to
        defaultOperationFiltering("commission.equals=" + DEFAULT_COMMISSION, "commission.equals=" + UPDATED_COMMISSION);
    }

    @Test
    void getAllOperationsByCommissionIsInShouldWork() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where commission in
        defaultOperationFiltering("commission.in=" + DEFAULT_COMMISSION + "," + UPDATED_COMMISSION, "commission.in=" + UPDATED_COMMISSION);
    }

    @Test
    void getAllOperationsByCommissionIsNullOrNotNull() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where commission is not null
        defaultOperationFiltering("commission.specified=true", "commission.specified=false");
    }

    @Test
    void getAllOperationsByCommissionIsGreaterThanOrEqualToSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where commission is greater than or equal to
        defaultOperationFiltering(
            "commission.greaterThanOrEqual=" + DEFAULT_COMMISSION,
            "commission.greaterThanOrEqual=" + UPDATED_COMMISSION
        );
    }

    @Test
    void getAllOperationsByCommissionIsLessThanOrEqualToSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where commission is less than or equal to
        defaultOperationFiltering("commission.lessThanOrEqual=" + DEFAULT_COMMISSION, "commission.lessThanOrEqual=" + SMALLER_COMMISSION);
    }

    @Test
    void getAllOperationsByCommissionIsLessThanSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where commission is less than
        defaultOperationFiltering("commission.lessThan=" + UPDATED_COMMISSION, "commission.lessThan=" + DEFAULT_COMMISSION);
    }

    @Test
    void getAllOperationsByCommissionIsGreaterThanSomething() {
        // Initialize the database
        insertedOperation = operationRepository.save(operation).block();

        // Get all the operationList where commission is greater than
        defaultOperationFiltering("commission.greaterThan=" + SMALLER_COMMISSION, "commission.greaterThan=" + DEFAULT_COMMISSION);
    }

    @Test
    void getAllOperationsByAccountIsEqualToSomething() {
        BrokerAccount account = BrokerAccountResourceIT.createEntity(em);
        brokerAccountRepository.save(account).block();
        UUID accountId = account.getId();
        operation.setAccountId(accountId);
        insertedOperation = operationRepository.save(operation).block();
        // Get all the operationList where account equals to accountId
        defaultOperationShouldBeFound("accountId.equals=" + accountId);

        // Get all the operationList where account equals to UUID.randomUUID()
        defaultOperationShouldNotBeFound("accountId.equals=" + UUID.randomUUID());
    }

    @Test
    void getAllOperationsByAssetIsEqualToSomething() {
        Asset asset = AssetResourceIT.createEntity();
        assetRepository.save(asset).block();
        UUID assetId = asset.getId();
        operation.setAssetId(assetId);
        insertedOperation = operationRepository.save(operation).block();
        // Get all the operationList where asset equals to assetId
        defaultOperationShouldBeFound("assetId.equals=" + assetId);

        // Get all the operationList where asset equals to UUID.randomUUID()
        defaultOperationShouldNotBeFound("assetId.equals=" + UUID.randomUUID());
    }

    @Test
    void getAllOperationsByClosesOperationIsEqualToSomething() {
        Operation closesOperation = OperationResourceIT.createEntity(em);
        operationRepository.save(closesOperation).block();
        UUID closesOperationId = closesOperation.getId();
        operation.setClosesOperationId(closesOperationId);
        insertedOperation = operationRepository.save(operation).block();
        // Get all the operationList where closesOperation equals to closesOperationId
        defaultOperationShouldBeFound("closesOperationId.equals=" + closesOperationId);

        // Get all the operationList where closesOperation equals to UUID.randomUUID()
        defaultOperationShouldNotBeFound("closesOperationId.equals=" + UUID.randomUUID());
    }

    private void defaultOperationFiltering(String shouldBeFound, String shouldNotBeFound) {
        defaultOperationShouldBeFound(shouldBeFound);
        defaultOperationShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultOperationShouldBeFound(String filter) {
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
            .value(hasItem(operation.getId().toString()))
            .jsonPath("$.[*].type")
            .value(hasItem(DEFAULT_TYPE.toString()))

            .jsonPath("$.[*].operationDate")
            .value(hasItem(DEFAULT_OPERATION_DATE.toString()))

            .jsonPath("$.[*].quantity")
            .value(hasItem(sameNumber(DEFAULT_QUANTITY)))

            .jsonPath("$.[*].price")
            .value(hasItem(sameNumber(DEFAULT_PRICE)))

            .jsonPath("$.[*].amount")
            .value(hasItem(sameNumber(DEFAULT_AMOUNT)))

            .jsonPath("$.[*].currency")
            .value(hasItem(DEFAULT_CURRENCY))

            .jsonPath("$.[*].underlyingPrice")
            .value(hasItem(sameNumber(DEFAULT_UNDERLYING_PRICE)))

            .jsonPath("$.[*].commission")
            .value(hasItem(sameNumber(DEFAULT_COMMISSION)));

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
    private void defaultOperationShouldNotBeFound(String filter) {
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
    void getNonExistingOperation() {
        // Get the operation
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingOperation() throws Exception {
        // Initialize the database
        operation.setId(UUID.randomUUID());
        insertedOperation = operationRepository.save(operation).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the operation
        Operation updatedOperation = operationRepository.findById(operation.getId()).block();
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

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, operationDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(operationDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Operation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedOperationToMatchAllProperties(updatedOperation);
    }

    @Test
    void putNonExistingOperation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        operation.setId(UUID.randomUUID());

        // Create the Operation
        OperationDTO operationDTO = operationMapper.toDto(operation);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, operationDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(operationDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Operation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchOperation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        operation.setId(UUID.randomUUID());

        // Create the Operation
        OperationDTO operationDTO = operationMapper.toDto(operation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(operationDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Operation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamOperation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        operation.setId(UUID.randomUUID());

        // Create the Operation
        OperationDTO operationDTO = operationMapper.toDto(operation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(operationDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Operation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateOperationWithPatch() throws Exception {
        // Initialize the database
        operation.setId(UUID.randomUUID());
        insertedOperation = operationRepository.save(operation).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the operation using partial update
        Operation partialUpdatedOperation = new Operation();
        partialUpdatedOperation.setId(operation.getId());

        partialUpdatedOperation.operationDate(UPDATED_OPERATION_DATE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedOperation.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedOperation))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Operation in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertOperationUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedOperation, operation),
            getPersistedOperation(operation)
        );
    }

    @Test
    void fullUpdateOperationWithPatch() throws Exception {
        // Initialize the database
        operation.setId(UUID.randomUUID());
        insertedOperation = operationRepository.save(operation).block();

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

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedOperation.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedOperation))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Operation in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertOperationUpdatableFieldsEquals(partialUpdatedOperation, getPersistedOperation(partialUpdatedOperation));
    }

    @Test
    void patchNonExistingOperation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        operation.setId(UUID.randomUUID());

        // Create the Operation
        OperationDTO operationDTO = operationMapper.toDto(operation);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, operationDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(operationDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Operation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchOperation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        operation.setId(UUID.randomUUID());

        // Create the Operation
        OperationDTO operationDTO = operationMapper.toDto(operation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(operationDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Operation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamOperation() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        operation.setId(UUID.randomUUID());

        // Create the Operation
        OperationDTO operationDTO = operationMapper.toDto(operation);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(operationDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Operation in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteOperation() {
        // Initialize the database
        operation.setId(UUID.randomUUID());
        insertedOperation = operationRepository.save(operation).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the operation
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, operation.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return operationRepository.count().block();
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
        return operationRepository.findById(operation.getId()).block();
    }

    protected void assertPersistedOperationToMatchAllProperties(Operation expectedOperation) {
        // Test fails because reactive api returns an empty object instead of null
        // assertOperationAllPropertiesEquals(expectedOperation, getPersistedOperation(expectedOperation));
        assertOperationUpdatableFieldsEquals(expectedOperation, getPersistedOperation(expectedOperation));
    }

    protected void assertPersistedOperationToMatchUpdatableProperties(Operation expectedOperation) {
        // Test fails because reactive api returns an empty object instead of null
        // assertOperationAllUpdatablePropertiesEquals(expectedOperation, getPersistedOperation(expectedOperation));
        assertOperationUpdatableFieldsEquals(expectedOperation, getPersistedOperation(expectedOperation));
    }
}
