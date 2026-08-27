package com.assetcompass.portfolio.web.rest;

import com.assetcompass.portfolio.repository.IncomeEventRepository;
import com.assetcompass.portfolio.service.IncomeEventQueryService;
import com.assetcompass.portfolio.service.IncomeEventService;
import com.assetcompass.portfolio.service.criteria.IncomeEventCriteria;
import com.assetcompass.portfolio.service.dto.IncomeEventDTO;
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
 * REST controller for managing {@link com.assetcompass.portfolio.domain.IncomeEvent}.
 */
@RestController
@RequestMapping("/api/income-events")
public class IncomeEventResource {

    private static final Logger LOG = LoggerFactory.getLogger(IncomeEventResource.class);

    private static final String ENTITY_NAME = "portfolioServiceIncomeEvent";

    @Value("${jhipster.clientApp.name:portfolioService}")
    private String applicationName;

    private final IncomeEventService incomeEventService;

    private final IncomeEventRepository incomeEventRepository;

    private final IncomeEventQueryService incomeEventQueryService;

    public IncomeEventResource(
        IncomeEventService incomeEventService,
        IncomeEventRepository incomeEventRepository,
        IncomeEventQueryService incomeEventQueryService
    ) {
        this.incomeEventService = incomeEventService;
        this.incomeEventRepository = incomeEventRepository;
        this.incomeEventQueryService = incomeEventQueryService;
    }

    /**
     * {@code POST  /income-events} : Create a new incomeEvent.
     *
     * @param incomeEventDTO the incomeEventDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new incomeEventDTO, or with status {@code 400 (Bad Request)} if the incomeEvent has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<IncomeEventDTO> createIncomeEvent(@Valid @RequestBody IncomeEventDTO incomeEventDTO) throws URISyntaxException {
        LOG.debug("REST request to save IncomeEvent : {}", incomeEventDTO);
        if (incomeEventRepository.existsById(incomeEventDTO.getId())) {
            throw new BadRequestAlertException("A new incomeEvent cannot already have an ID", ENTITY_NAME, "idexists");
        }
        incomeEventDTO = incomeEventService.save(incomeEventDTO);
        return ResponseEntity.created(new URI("/api/income-events/" + incomeEventDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, incomeEventDTO.getId().toString()))
            .body(incomeEventDTO);
    }

    /**
     * {@code PUT  /income-events/:id} : Updates an existing incomeEvent.
     *
     * @param id the id of the incomeEventDTO to save.
     * @param incomeEventDTO the incomeEventDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated incomeEventDTO,
     * or with status {@code 400 (Bad Request)} if the incomeEventDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the incomeEventDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<IncomeEventDTO> updateIncomeEvent(
        @PathVariable(value = "id", required = false) final UUID id,
        @Valid @RequestBody IncomeEventDTO incomeEventDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update IncomeEvent : {}, {}", id, incomeEventDTO);
        if (incomeEventDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, incomeEventDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!incomeEventRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        incomeEventDTO = incomeEventService.update(incomeEventDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, incomeEventDTO.getId().toString()))
            .body(incomeEventDTO);
    }

    /**
     * {@code PATCH  /income-events/:id} : Partial updates given fields of an existing incomeEvent, field will ignore if it is null
     *
     * @param id the id of the incomeEventDTO to save.
     * @param incomeEventDTO the incomeEventDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated incomeEventDTO,
     * or with status {@code 400 (Bad Request)} if the incomeEventDTO is not valid,
     * or with status {@code 404 (Not Found)} if the incomeEventDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the incomeEventDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<IncomeEventDTO> partialUpdateIncomeEvent(
        @PathVariable(value = "id", required = false) final UUID id,
        @NotNull @RequestBody IncomeEventDTO incomeEventDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update IncomeEvent partially : {}, {}", id, incomeEventDTO);
        if (incomeEventDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, incomeEventDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!incomeEventRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<IncomeEventDTO> result = incomeEventService.partialUpdate(incomeEventDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, incomeEventDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /income-events} : get all the Income Events.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Income Events in body.
     */
    @GetMapping("")
    public ResponseEntity<List<IncomeEventDTO>> getAllIncomeEvents(
        IncomeEventCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get IncomeEvents by criteria: {}", criteria);

        Page<IncomeEventDTO> page = incomeEventQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /income-events/count} : count all the incomeEvents.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countIncomeEvents(IncomeEventCriteria criteria) {
        LOG.debug("REST request to count IncomeEvents by criteria: {}", criteria);
        return ResponseEntity.ok().body(incomeEventQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /income-events/:id} : get the "id" incomeEvent.
     *
     * @param id the id of the incomeEventDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the incomeEventDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<IncomeEventDTO> getIncomeEvent(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get IncomeEvent : {}", id);
        Optional<IncomeEventDTO> incomeEventDTO = incomeEventService.findOne(id);
        return ResponseUtil.wrapOrNotFound(incomeEventDTO);
    }

    /**
     * {@code DELETE  /income-events/:id} : delete the "id" incomeEvent.
     *
     * @param id the id of the incomeEventDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncomeEvent(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete IncomeEvent : {}", id);
        incomeEventService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
