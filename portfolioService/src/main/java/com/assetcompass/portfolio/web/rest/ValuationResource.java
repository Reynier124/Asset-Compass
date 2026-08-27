package com.assetcompass.portfolio.web.rest;

import com.assetcompass.portfolio.repository.ValuationRepository;
import com.assetcompass.portfolio.service.ValuationQueryService;
import com.assetcompass.portfolio.service.ValuationService;
import com.assetcompass.portfolio.service.criteria.ValuationCriteria;
import com.assetcompass.portfolio.service.dto.ValuationDTO;
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
 * REST controller for managing {@link com.assetcompass.portfolio.domain.Valuation}.
 */
@RestController
@RequestMapping("/api/valuations")
public class ValuationResource {

    private static final Logger LOG = LoggerFactory.getLogger(ValuationResource.class);

    private static final String ENTITY_NAME = "portfolioServiceValuation";

    @Value("${jhipster.clientApp.name:portfolioService}")
    private String applicationName;

    private final ValuationService valuationService;

    private final ValuationRepository valuationRepository;

    private final ValuationQueryService valuationQueryService;

    public ValuationResource(
        ValuationService valuationService,
        ValuationRepository valuationRepository,
        ValuationQueryService valuationQueryService
    ) {
        this.valuationService = valuationService;
        this.valuationRepository = valuationRepository;
        this.valuationQueryService = valuationQueryService;
    }

    /**
     * {@code POST  /valuations} : Create a new valuation.
     *
     * @param valuationDTO the valuationDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new valuationDTO, or with status {@code 400 (Bad Request)} if the valuation has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ValuationDTO> createValuation(@Valid @RequestBody ValuationDTO valuationDTO) throws URISyntaxException {
        LOG.debug("REST request to save Valuation : {}", valuationDTO);
        if (valuationRepository.existsById(valuationDTO.getId())) {
            throw new BadRequestAlertException("A new valuation cannot already have an ID", ENTITY_NAME, "idexists");
        }
        valuationDTO = valuationService.save(valuationDTO);
        return ResponseEntity.created(new URI("/api/valuations/" + valuationDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, valuationDTO.getId().toString()))
            .body(valuationDTO);
    }

    /**
     * {@code PUT  /valuations/:id} : Updates an existing valuation.
     *
     * @param id the id of the valuationDTO to save.
     * @param valuationDTO the valuationDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated valuationDTO,
     * or with status {@code 400 (Bad Request)} if the valuationDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the valuationDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ValuationDTO> updateValuation(
        @PathVariable(value = "id", required = false) final UUID id,
        @Valid @RequestBody ValuationDTO valuationDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Valuation : {}, {}", id, valuationDTO);
        if (valuationDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, valuationDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!valuationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        valuationDTO = valuationService.update(valuationDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, valuationDTO.getId().toString()))
            .body(valuationDTO);
    }

    /**
     * {@code PATCH  /valuations/:id} : Partial updates given fields of an existing valuation, field will ignore if it is null
     *
     * @param id the id of the valuationDTO to save.
     * @param valuationDTO the valuationDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated valuationDTO,
     * or with status {@code 400 (Bad Request)} if the valuationDTO is not valid,
     * or with status {@code 404 (Not Found)} if the valuationDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the valuationDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ValuationDTO> partialUpdateValuation(
        @PathVariable(value = "id", required = false) final UUID id,
        @NotNull @RequestBody ValuationDTO valuationDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Valuation partially : {}, {}", id, valuationDTO);
        if (valuationDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, valuationDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!valuationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ValuationDTO> result = valuationService.partialUpdate(valuationDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, valuationDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /valuations} : get all the Valuations.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Valuations in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ValuationDTO>> getAllValuations(
        ValuationCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Valuations by criteria: {}", criteria);

        Page<ValuationDTO> page = valuationQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /valuations/count} : count all the valuations.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countValuations(ValuationCriteria criteria) {
        LOG.debug("REST request to count Valuations by criteria: {}", criteria);
        return ResponseEntity.ok().body(valuationQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /valuations/:id} : get the "id" valuation.
     *
     * @param id the id of the valuationDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the valuationDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ValuationDTO> getValuation(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get Valuation : {}", id);
        Optional<ValuationDTO> valuationDTO = valuationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(valuationDTO);
    }

    /**
     * {@code DELETE  /valuations/:id} : delete the "id" valuation.
     *
     * @param id the id of the valuationDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteValuation(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete Valuation : {}", id);
        valuationService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
