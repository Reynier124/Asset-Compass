package com.assetcompass.portfolio.web.rest;

import com.assetcompass.portfolio.repository.RebateRepository;
import com.assetcompass.portfolio.service.RebateQueryService;
import com.assetcompass.portfolio.service.RebateService;
import com.assetcompass.portfolio.service.criteria.RebateCriteria;
import com.assetcompass.portfolio.service.dto.RebateDTO;
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
 * REST controller for managing {@link com.assetcompass.portfolio.domain.Rebate}.
 */
@RestController
@RequestMapping("/api/rebates")
public class RebateResource {

    private static final Logger LOG = LoggerFactory.getLogger(RebateResource.class);

    private static final String ENTITY_NAME = "portfolioServiceRebate";

    @Value("${jhipster.clientApp.name:portfolioService}")
    private String applicationName;

    private final RebateService rebateService;

    private final RebateRepository rebateRepository;

    private final RebateQueryService rebateQueryService;

    public RebateResource(RebateService rebateService, RebateRepository rebateRepository, RebateQueryService rebateQueryService) {
        this.rebateService = rebateService;
        this.rebateRepository = rebateRepository;
        this.rebateQueryService = rebateQueryService;
    }

    /**
     * {@code POST  /rebates} : Create a new rebate.
     *
     * @param rebateDTO the rebateDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new rebateDTO, or with status {@code 400 (Bad Request)} if the rebate has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<RebateDTO> createRebate(@Valid @RequestBody RebateDTO rebateDTO) throws URISyntaxException {
        LOG.debug("REST request to save Rebate : {}", rebateDTO);
        if (rebateDTO.getId() != null) {
            throw new BadRequestAlertException("A new rebate cannot already have an ID", ENTITY_NAME, "idexists");
        }
        rebateDTO = rebateService.save(rebateDTO);
        return ResponseEntity.created(new URI("/api/rebates/" + rebateDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, rebateDTO.getId().toString()))
            .body(rebateDTO);
    }

    /**
     * {@code PUT  /rebates/:id} : Updates an existing rebate.
     *
     * @param id the id of the rebateDTO to save.
     * @param rebateDTO the rebateDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated rebateDTO,
     * or with status {@code 400 (Bad Request)} if the rebateDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the rebateDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<RebateDTO> updateRebate(
        @PathVariable(value = "id", required = false) final UUID id,
        @Valid @RequestBody RebateDTO rebateDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Rebate : {}, {}", id, rebateDTO);
        if (rebateDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, rebateDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!rebateRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        rebateDTO = rebateService.update(rebateDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, rebateDTO.getId().toString()))
            .body(rebateDTO);
    }

    /**
     * {@code PATCH  /rebates/:id} : Partial updates given fields of an existing rebate, field will ignore if it is null
     *
     * @param id the id of the rebateDTO to save.
     * @param rebateDTO the rebateDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated rebateDTO,
     * or with status {@code 400 (Bad Request)} if the rebateDTO is not valid,
     * or with status {@code 404 (Not Found)} if the rebateDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the rebateDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<RebateDTO> partialUpdateRebate(
        @PathVariable(value = "id", required = false) final UUID id,
        @NotNull @RequestBody RebateDTO rebateDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Rebate partially : {}, {}", id, rebateDTO);
        if (rebateDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, rebateDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!rebateRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<RebateDTO> result = rebateService.partialUpdate(rebateDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, rebateDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /rebates} : get all the Rebates.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Rebates in body.
     */
    @GetMapping("")
    public ResponseEntity<List<RebateDTO>> getAllRebates(
        RebateCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Rebates by criteria: {}", criteria);

        Page<RebateDTO> page = rebateQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /rebates/count} : count all the rebates.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countRebates(RebateCriteria criteria) {
        LOG.debug("REST request to count Rebates by criteria: {}", criteria);
        return ResponseEntity.ok().body(rebateQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /rebates/:id} : get the "id" rebate.
     *
     * @param id the id of the rebateDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the rebateDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RebateDTO> getRebate(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get Rebate : {}", id);
        Optional<RebateDTO> rebateDTO = rebateService.findOne(id);
        return ResponseUtil.wrapOrNotFound(rebateDTO);
    }

    /**
     * {@code DELETE  /rebates/:id} : delete the "id" rebate.
     *
     * @param id the id of the rebateDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRebate(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete Rebate : {}", id);
        rebateService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
