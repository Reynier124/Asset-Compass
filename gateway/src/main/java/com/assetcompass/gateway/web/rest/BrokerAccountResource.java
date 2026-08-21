package com.assetcompass.gateway.web.rest;

import com.assetcompass.gateway.domain.criteria.BrokerAccountCriteria;
import com.assetcompass.gateway.repository.BrokerAccountRepository;
import com.assetcompass.gateway.service.BrokerAccountService;
import com.assetcompass.gateway.service.dto.BrokerAccountDTO;
import com.assetcompass.gateway.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link com.assetcompass.gateway.domain.BrokerAccount}.
 */
@RestController
@RequestMapping("/api/broker-accounts")
public class BrokerAccountResource {

    private static final Logger LOG = LoggerFactory.getLogger(BrokerAccountResource.class);

    private static final String ENTITY_NAME = "brokerAccount";

    @Value("${jhipster.clientApp.name:gateway}")
    private String applicationName;

    private final BrokerAccountService brokerAccountService;

    private final BrokerAccountRepository brokerAccountRepository;

    public BrokerAccountResource(BrokerAccountService brokerAccountService, BrokerAccountRepository brokerAccountRepository) {
        this.brokerAccountService = brokerAccountService;
        this.brokerAccountRepository = brokerAccountRepository;
    }

    /**
     * {@code POST  /broker-accounts} : Create a new brokerAccount.
     *
     * @param brokerAccountDTO the brokerAccountDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new brokerAccountDTO, or with status {@code 400 (Bad Request)} if the brokerAccount has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<BrokerAccountDTO>> createBrokerAccount(@Valid @RequestBody BrokerAccountDTO brokerAccountDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save BrokerAccount : {}", brokerAccountDTO);
        if (brokerAccountDTO.getId() != null) {
            throw new BadRequestAlertException("A new brokerAccount cannot already have an ID", ENTITY_NAME, "idexists");
        }
        brokerAccountDTO.setId(UUID.randomUUID());
        return brokerAccountService.save(brokerAccountDTO).map(result -> {
            try {
                return ResponseEntity.created(new URI("/api/broker-accounts/" + result.getId()))
                    .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                    .body(result);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
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
    public Mono<ResponseEntity<BrokerAccountDTO>> updateBrokerAccount(
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

        return brokerAccountRepository.existsById(id).flatMap(exists -> {
            if (!exists) {
                return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
            }

            return brokerAccountService
                .update(brokerAccountDTO)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .map(result ->
                    ResponseEntity.ok()
                        .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result)
                );
        });
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
    public Mono<ResponseEntity<BrokerAccountDTO>> partialUpdateBrokerAccount(
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

        return brokerAccountRepository.existsById(id).flatMap(exists -> {
            if (!exists) {
                return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
            }

            Mono<BrokerAccountDTO> result = brokerAccountService.partialUpdate(brokerAccountDTO);

            return result.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND))).map(res ->
                ResponseEntity.ok()
                    .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId().toString()))
                    .body(res)
            );
        });
    }

    /**
     * {@code GET  /broker-accounts} : get all the Broker Accounts.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Broker Accounts in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<BrokerAccountDTO> getAllBrokerAccounts(BrokerAccountCriteria criteria) {
        LOG.debug("REST request to get BrokerAccounts by criteria: {}", criteria);
        return brokerAccountService.findByCriteria(criteria);
    }

    /**
     * {@code GET  /broker-accounts/count} : count all the brokerAccounts.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public Mono<ResponseEntity<Long>> countBrokerAccounts(BrokerAccountCriteria criteria) {
        LOG.debug("REST request to count BrokerAccounts by criteria: {}", criteria);
        return brokerAccountService.countByCriteria(criteria).map(count -> ResponseEntity.status(HttpStatus.OK).body(count));
    }

    /**
     * {@code GET  /broker-accounts/:id} : get the "id" brokerAccount.
     *
     * @param id the id of the brokerAccountDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the brokerAccountDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<BrokerAccountDTO>> getBrokerAccount(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get BrokerAccount : {}", id);
        Mono<BrokerAccountDTO> brokerAccountDTO = brokerAccountService.findOne(id);
        return ResponseUtil.wrapOrNotFound(brokerAccountDTO);
    }

    /**
     * {@code DELETE  /broker-accounts/:id} : delete the "id" brokerAccount.
     *
     * @param id the id of the brokerAccountDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteBrokerAccount(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete BrokerAccount : {}", id);
        return brokerAccountService
            .delete(id)

            .then(
                Mono.just(
                    ResponseEntity.noContent()
                        .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                        .build()
                )
            );
    }
}
