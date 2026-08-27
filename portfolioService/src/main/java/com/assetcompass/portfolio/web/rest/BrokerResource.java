package com.assetcompass.portfolio.web.rest;

import com.assetcompass.portfolio.repository.BrokerRepository;
import com.assetcompass.portfolio.service.BrokerQueryService;
import com.assetcompass.portfolio.service.BrokerService;
import com.assetcompass.portfolio.service.criteria.BrokerCriteria;
import com.assetcompass.portfolio.service.dto.BrokerDTO;
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
 * REST controller for managing {@link com.assetcompass.portfolio.domain.Broker}.
 */
@RestController
@RequestMapping("/api/brokers")
public class BrokerResource {

    private static final Logger LOG = LoggerFactory.getLogger(BrokerResource.class);

    private static final String ENTITY_NAME = "portfolioServiceBroker";

    @Value("${jhipster.clientApp.name:portfolioService}")
    private String applicationName;

    private final BrokerService brokerService;

    private final BrokerRepository brokerRepository;

    private final BrokerQueryService brokerQueryService;

    public BrokerResource(BrokerService brokerService, BrokerRepository brokerRepository, BrokerQueryService brokerQueryService) {
        this.brokerService = brokerService;
        this.brokerRepository = brokerRepository;
        this.brokerQueryService = brokerQueryService;
    }

    /**
     * {@code POST  /brokers} : Create a new broker.
     *
     * @param brokerDTO the brokerDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new brokerDTO, or with status {@code 400 (Bad Request)} if the broker has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<BrokerDTO> createBroker(@Valid @RequestBody BrokerDTO brokerDTO) throws URISyntaxException {
        LOG.debug("REST request to save Broker : {}", brokerDTO);
        if (brokerRepository.existsById(brokerDTO.getId())) {
            throw new BadRequestAlertException("A new broker cannot already have an ID", ENTITY_NAME, "idexists");
        }
        brokerDTO = brokerService.save(brokerDTO);
        return ResponseEntity.created(new URI("/api/brokers/" + brokerDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, brokerDTO.getId().toString()))
            .body(brokerDTO);
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
    public ResponseEntity<BrokerDTO> updateBroker(
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

        if (!brokerRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        brokerDTO = brokerService.update(brokerDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, brokerDTO.getId().toString()))
            .body(brokerDTO);
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
    public ResponseEntity<BrokerDTO> partialUpdateBroker(
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

        if (!brokerRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<BrokerDTO> result = brokerService.partialUpdate(brokerDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, brokerDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /brokers} : get all the Brokers.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Brokers in body.
     */
    @GetMapping("")
    public ResponseEntity<List<BrokerDTO>> getAllBrokers(BrokerCriteria criteria) {
        LOG.debug("REST request to get Brokers by criteria: {}", criteria);

        List<BrokerDTO> entityList = brokerQueryService.findByCriteria(criteria);
        return ResponseEntity.ok().body(entityList);
    }

    /**
     * {@code GET  /brokers/count} : count all the brokers.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countBrokers(BrokerCriteria criteria) {
        LOG.debug("REST request to count Brokers by criteria: {}", criteria);
        return ResponseEntity.ok().body(brokerQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /brokers/:id} : get the "id" broker.
     *
     * @param id the id of the brokerDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the brokerDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BrokerDTO> getBroker(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get Broker : {}", id);
        Optional<BrokerDTO> brokerDTO = brokerService.findOne(id);
        return ResponseUtil.wrapOrNotFound(brokerDTO);
    }

    /**
     * {@code DELETE  /brokers/:id} : delete the "id" broker.
     *
     * @param id the id of the brokerDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBroker(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete Broker : {}", id);
        brokerService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
