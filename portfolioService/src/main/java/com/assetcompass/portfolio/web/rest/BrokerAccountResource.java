package com.assetcompass.portfolio.web.rest;

import com.assetcompass.portfolio.repository.BrokerAccountRepository;
import com.assetcompass.portfolio.service.BrokerAccountQueryService;
import com.assetcompass.portfolio.service.BrokerAccountService;
import com.assetcompass.portfolio.service.criteria.BrokerAccountCriteria;
import com.assetcompass.portfolio.service.dto.BrokerAccountDTO;
import com.assetcompass.portfolio.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.assetcompass.portfolio.domain.BrokerAccount}.
 */
@RestController
@RequestMapping("/api/broker-accounts")
public class BrokerAccountResource {

    private static final Logger LOG = LoggerFactory.getLogger(BrokerAccountResource.class);

    private static final String ENTITY_NAME = "portfolioServiceBrokerAccount";

    @Value("${jhipster.clientApp.name:portfolioService}")
    private String applicationName;

    private final BrokerAccountService brokerAccountService;

    private final BrokerAccountRepository brokerAccountRepository;

    private final BrokerAccountQueryService brokerAccountQueryService;

    public BrokerAccountResource(
        BrokerAccountService brokerAccountService,
        BrokerAccountRepository brokerAccountRepository,
        BrokerAccountQueryService brokerAccountQueryService
    ) {
        this.brokerAccountService = brokerAccountService;
        this.brokerAccountRepository = brokerAccountRepository;
        this.brokerAccountQueryService = brokerAccountQueryService;
    }

    /**
     * {@code POST  /broker-accounts} : Create a new brokerAccount.
     *
     * @param brokerAccountDTO the brokerAccountDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new brokerAccountDTO, or with status {@code 400 (Bad Request)} if the brokerAccount has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<BrokerAccountDTO> createBrokerAccount(@Valid @RequestBody BrokerAccountDTO brokerAccountDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save BrokerAccount : {}", brokerAccountDTO);
        if (brokerAccountRepository.existsById(brokerAccountDTO.getId())) {
            throw new BadRequestAlertException("A new brokerAccount cannot already have an ID", ENTITY_NAME, "idexists");
        }
        brokerAccountDTO = brokerAccountService.save(brokerAccountDTO);
        return ResponseEntity.created(new URI("/api/broker-accounts/" + brokerAccountDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, brokerAccountDTO.getId().toString()))
            .body(brokerAccountDTO);
    }

    /**
     * {@code PUT  /broker-accounts/:id} : Updates an existing brokerAccount.
     *
     * @param id the id of the brokerAccountDTO to save.
     * @param brokerAccountDTO the brokerAccountDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated brokerAccountDTO,
     * or with status {@code 400 (Bad Request)} if the brokerAccountDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the brokerAccountDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<BrokerAccountDTO> updateBrokerAccount(
        @PathVariable(value = "id", required = false) final UUID id,
        @Valid @RequestBody BrokerAccountDTO brokerAccountDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update BrokerAccount : {}, {}", id, brokerAccountDTO);
        if (brokerAccountDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, brokerAccountDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!brokerAccountRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        brokerAccountDTO = brokerAccountService.update(brokerAccountDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, brokerAccountDTO.getId().toString()))
            .body(brokerAccountDTO);
    }

    /**
     * {@code PATCH  /broker-accounts/:id} : Partial updates given fields of an existing brokerAccount, field will ignore if it is null
     *
     * @param id the id of the brokerAccountDTO to save.
     * @param brokerAccountDTO the brokerAccountDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated brokerAccountDTO,
     * or with status {@code 400 (Bad Request)} if the brokerAccountDTO is not valid,
     * or with status {@code 404 (Not Found)} if the brokerAccountDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the brokerAccountDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<BrokerAccountDTO> partialUpdateBrokerAccount(
        @PathVariable(value = "id", required = false) final UUID id,
        @NotNull @RequestBody BrokerAccountDTO brokerAccountDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update BrokerAccount partially : {}, {}", id, brokerAccountDTO);
        if (brokerAccountDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, brokerAccountDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!brokerAccountRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<BrokerAccountDTO> result = brokerAccountService.partialUpdate(brokerAccountDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, brokerAccountDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /broker-accounts} : get all the Broker Accounts.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Broker Accounts in body.
     */
    @GetMapping("")
    public ResponseEntity<List<BrokerAccountDTO>> getAllBrokerAccounts(BrokerAccountCriteria criteria) {
        LOG.debug("REST request to get BrokerAccounts by criteria: {}", criteria);

        List<BrokerAccountDTO> entityList = brokerAccountQueryService.findByCriteria(criteria);
        return ResponseEntity.ok().body(entityList);
    }

    /**
     * {@code GET  /broker-accounts/count} : count all the brokerAccounts.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countBrokerAccounts(BrokerAccountCriteria criteria) {
        LOG.debug("REST request to count BrokerAccounts by criteria: {}", criteria);
        return ResponseEntity.ok().body(brokerAccountQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /broker-accounts/:id} : get the "id" brokerAccount.
     *
     * @param id the id of the brokerAccountDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the brokerAccountDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BrokerAccountDTO> getBrokerAccount(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get BrokerAccount : {}", id);
        Optional<BrokerAccountDTO> brokerAccountDTO = brokerAccountService.findOne(id);
        return ResponseUtil.wrapOrNotFound(brokerAccountDTO);
    }

    /**
     * {@code DELETE  /broker-accounts/:id} : delete the "id" brokerAccount.
     *
     * @param id the id of the brokerAccountDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrokerAccount(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete BrokerAccount : {}", id);
        brokerAccountService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
