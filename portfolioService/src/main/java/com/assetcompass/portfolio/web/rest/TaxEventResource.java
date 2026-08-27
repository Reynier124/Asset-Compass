package com.assetcompass.portfolio.web.rest;

import com.assetcompass.portfolio.repository.TaxEventRepository;
import com.assetcompass.portfolio.service.TaxEventQueryService;
import com.assetcompass.portfolio.service.TaxEventService;
import com.assetcompass.portfolio.service.criteria.TaxEventCriteria;
import com.assetcompass.portfolio.service.dto.TaxEventDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.assetcompass.portfolio.domain.TaxEvent}.
 */
@RestController
@RequestMapping("/api/tax-events")
public class TaxEventResource {

    private static final Logger LOG = LoggerFactory.getLogger(TaxEventResource.class);

    private static final String ENTITY_NAME = "portfolioServiceTaxEvent";

    @Value("${jhipster.clientApp.name:portfolioService}")
    private String applicationName;

    private final TaxEventService taxEventService;

    private final TaxEventRepository taxEventRepository;

    private final TaxEventQueryService taxEventQueryService;

    public TaxEventResource(
        TaxEventService taxEventService,
        TaxEventRepository taxEventRepository,
        TaxEventQueryService taxEventQueryService
    ) {
        this.taxEventService = taxEventService;
        this.taxEventRepository = taxEventRepository;
        this.taxEventQueryService = taxEventQueryService;
    }

    /**
     * {@code POST  /tax-events} : Create a new taxEvent.
     *
     * @param taxEventDTO the taxEventDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new taxEventDTO, or with status {@code 400 (Bad Request)} if the taxEvent has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<TaxEventDTO> createTaxEvent(@Valid @RequestBody TaxEventDTO taxEventDTO) throws URISyntaxException {
        LOG.debug("REST request to save TaxEvent : {}", taxEventDTO);
        if (taxEventRepository.existsById(taxEventDTO.getId())) {
            throw new BadRequestAlertException("A new taxEvent cannot already have an ID", ENTITY_NAME, "idexists");
        }
        taxEventDTO = taxEventService.save(taxEventDTO);
        return ResponseEntity.created(new URI("/api/tax-events/" + taxEventDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, taxEventDTO.getId().toString()))
            .body(taxEventDTO);
    }

    /**
     * {@code PUT  /tax-events/:id} : Updates an existing taxEvent.
     *
     * @param id the id of the taxEventDTO to save.
     * @param taxEventDTO the taxEventDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated taxEventDTO,
     * or with status {@code 400 (Bad Request)} if the taxEventDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the taxEventDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaxEventDTO> updateTaxEvent(
        @PathVariable(value = "id", required = false) final UUID id,
        @Valid @RequestBody TaxEventDTO taxEventDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update TaxEvent : {}, {}", id, taxEventDTO);
        if (taxEventDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, taxEventDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!taxEventRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        taxEventDTO = taxEventService.update(taxEventDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, taxEventDTO.getId().toString()))
            .body(taxEventDTO);
    }

    /**
     * {@code PATCH  /tax-events/:id} : Partial updates given fields of an existing taxEvent, field will ignore if it is null
     *
     * @param id the id of the taxEventDTO to save.
     * @param taxEventDTO the taxEventDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated taxEventDTO,
     * or with status {@code 400 (Bad Request)} if the taxEventDTO is not valid,
     * or with status {@code 404 (Not Found)} if the taxEventDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the taxEventDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<TaxEventDTO> partialUpdateTaxEvent(
        @PathVariable(value = "id", required = false) final UUID id,
        @NotNull @RequestBody TaxEventDTO taxEventDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update TaxEvent partially : {}, {}", id, taxEventDTO);
        if (taxEventDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, taxEventDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!taxEventRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<TaxEventDTO> result = taxEventService.partialUpdate(taxEventDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, taxEventDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /tax-events} : get all the Tax Events.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Tax Events in body.
     */
    @GetMapping("")
    public ResponseEntity<List<TaxEventDTO>> getAllTaxEvents(
        TaxEventCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get TaxEvents by criteria: {}", criteria);

        Page<TaxEventDTO> page = taxEventQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /tax-events/count} : count all the taxEvents.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countTaxEvents(TaxEventCriteria criteria) {
        LOG.debug("REST request to count TaxEvents by criteria: {}", criteria);
        return ResponseEntity.ok().body(taxEventQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /tax-events/:id} : get the "id" taxEvent.
     *
     * @param id the id of the taxEventDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the taxEventDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaxEventDTO> getTaxEvent(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get TaxEvent : {}", id);
        Optional<TaxEventDTO> taxEventDTO = taxEventService.findOne(id);
        return ResponseUtil.wrapOrNotFound(taxEventDTO);
    }

    /**
     * {@code DELETE  /tax-events/:id} : delete the "id" taxEvent.
     *
     * @param id the id of the taxEventDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTaxEvent(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete TaxEvent : {}", id);
        taxEventService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
