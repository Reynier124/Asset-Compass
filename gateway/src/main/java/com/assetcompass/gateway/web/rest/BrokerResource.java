package com.assetcompass.gateway.web.rest;

import com.assetcompass.gateway.domain.criteria.BrokerCriteria;
import com.assetcompass.gateway.repository.BrokerRepository;
import com.assetcompass.gateway.service.BrokerService;
import com.assetcompass.gateway.service.dto.BrokerDTO;
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
 * REST controller for managing {@link com.assetcompass.gateway.domain.Broker}.
 */
@RestController
@RequestMapping("/api/brokers")
public class BrokerResource {

    private static final Logger LOG = LoggerFactory.getLogger(BrokerResource.class);

    private static final String ENTITY_NAME = "broker";

    @Value("${jhipster.clientApp.name:gateway}")
    private String applicationName;

    private final BrokerService brokerService;

    private final BrokerRepository brokerRepository;

    public BrokerResource(BrokerService brokerService, BrokerRepository brokerRepository) {
        this.brokerService = brokerService;
        this.brokerRepository = brokerRepository;
    }

    /**
     * {@code POST  /brokers} : Create a new broker.
     *
     * @param brokerDTO the brokerDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new brokerDTO, or with status {@code 400 (Bad Request)} if the broker has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<BrokerDTO>> createBroker(@Valid @RequestBody BrokerDTO brokerDTO) throws URISyntaxException {
        LOG.debug("REST request to save Broker : {}", brokerDTO);
        if (brokerDTO.getId() != null) {
            throw new BadRequestAlertException("A new broker cannot already have an ID", ENTITY_NAME, "idexists");
        }
        brokerDTO.setId(UUID.randomUUID());
        return brokerService.save(brokerDTO).map(result -> {
            try {
                return ResponseEntity.created(new URI("/api/brokers/" + result.getId()))
                    .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                    .body(result);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * {@code PUT  /brokers/:id} : Updates an existing broker.
     *
     * @param id the id of the brokerDTO to save.
     * @param brokerDTO the brokerDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated brokerDTO,
     * or with status {@code 400 (Bad Request)} if the brokerDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the brokerDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<BrokerDTO>> updateBroker(
        @PathVariable(value = "id", required = false) final UUID id,
        @Valid @RequestBody BrokerDTO brokerDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Broker : {}, {}", id, brokerDTO);
        if (brokerDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, brokerDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return brokerRepository.existsById(id).flatMap(exists -> {
            if (!exists) {
                return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
            }

            return brokerService
                .update(brokerDTO)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .map(result ->
                    ResponseEntity.ok()
                        .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result)
                );
        });
    }

    /**
     * {@code PATCH  /brokers/:id} : Partial updates given fields of an existing broker, field will ignore if it is null
     *
     * @param id the id of the brokerDTO to save.
     * @param brokerDTO the brokerDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated brokerDTO,
     * or with status {@code 400 (Bad Request)} if the brokerDTO is not valid,
     * or with status {@code 404 (Not Found)} if the brokerDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the brokerDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<BrokerDTO>> partialUpdateBroker(
        @PathVariable(value = "id", required = false) final UUID id,
        @NotNull @RequestBody BrokerDTO brokerDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Broker partially : {}, {}", id, brokerDTO);
        if (brokerDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, brokerDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return brokerRepository.existsById(id).flatMap(exists -> {
            if (!exists) {
                return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
            }

            Mono<BrokerDTO> result = brokerService.partialUpdate(brokerDTO);

            return result.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND))).map(res ->
                ResponseEntity.ok()
                    .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId().toString()))
                    .body(res)
            );
        });
    }

    /**
     * {@code GET  /brokers} : get all the Brokers.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Brokers in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<BrokerDTO> getAllBrokers(BrokerCriteria criteria) {
        LOG.debug("REST request to get Brokers by criteria: {}", criteria);
        return brokerService.findByCriteria(criteria);
    }

    /**
     * {@code GET  /brokers/count} : count all the brokers.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public Mono<ResponseEntity<Long>> countBrokers(BrokerCriteria criteria) {
        LOG.debug("REST request to count Brokers by criteria: {}", criteria);
        return brokerService.countByCriteria(criteria).map(count -> ResponseEntity.status(HttpStatus.OK).body(count));
    }

    /**
     * {@code GET  /brokers/:id} : get the "id" broker.
     *
     * @param id the id of the brokerDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the brokerDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<BrokerDTO>> getBroker(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get Broker : {}", id);
        Mono<BrokerDTO> brokerDTO = brokerService.findOne(id);
        return ResponseUtil.wrapOrNotFound(brokerDTO);
    }

    /**
     * {@code DELETE  /brokers/:id} : delete the "id" broker.
     *
     * @param id the id of the brokerDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteBroker(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete Broker : {}", id);
        return brokerService
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
