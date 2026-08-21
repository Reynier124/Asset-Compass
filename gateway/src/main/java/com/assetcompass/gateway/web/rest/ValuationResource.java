package com.assetcompass.gateway.web.rest;

import com.assetcompass.gateway.domain.criteria.ValuationCriteria;
import com.assetcompass.gateway.repository.ValuationRepository;
import com.assetcompass.gateway.service.ValuationService;
import com.assetcompass.gateway.service.dto.ValuationDTO;
import com.assetcompass.gateway.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.ForwardedHeaderUtils;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link com.assetcompass.gateway.domain.Valuation}.
 */
@RestController
@RequestMapping("/api/valuations")
public class ValuationResource {

    private static final Logger LOG = LoggerFactory.getLogger(ValuationResource.class);

    private static final String ENTITY_NAME = "valuation";

    @Value("${jhipster.clientApp.name:gateway}")
    private String applicationName;

    private final ValuationService valuationService;

    private final ValuationRepository valuationRepository;

    public ValuationResource(ValuationService valuationService, ValuationRepository valuationRepository) {
        this.valuationService = valuationService;
        this.valuationRepository = valuationRepository;
    }

    /**
     * {@code POST  /valuations} : Create a new valuation.
     *
     * @param valuationDTO the valuationDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new valuationDTO, or with status {@code 400 (Bad Request)} if the valuation has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<ValuationDTO>> createValuation(@Valid @RequestBody ValuationDTO valuationDTO) throws URISyntaxException {
        LOG.debug("REST request to save Valuation : {}", valuationDTO);
        if (valuationDTO.getId() != null) {
            throw new BadRequestAlertException("A new valuation cannot already have an ID", ENTITY_NAME, "idexists");
        }
        valuationDTO.setId(UUID.randomUUID());
        return valuationService.save(valuationDTO).map(result -> {
            try {
                return ResponseEntity.created(new URI("/api/valuations/" + result.getId()))
                    .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                    .body(result);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
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
    public Mono<ResponseEntity<ValuationDTO>> updateValuation(
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

        return valuationRepository.existsById(id).flatMap(exists -> {
            if (!exists) {
                return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
            }

            return valuationService
                .update(valuationDTO)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .map(result ->
                    ResponseEntity.ok()
                        .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result)
                );
        });
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
    public Mono<ResponseEntity<ValuationDTO>> partialUpdateValuation(
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

        return valuationRepository.existsById(id).flatMap(exists -> {
            if (!exists) {
                return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
            }

            Mono<ValuationDTO> result = valuationService.partialUpdate(valuationDTO);

            return result.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND))).map(res ->
                ResponseEntity.ok()
                    .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId().toString()))
                    .body(res)
            );
        });
    }

    /**
     * {@code GET  /valuations} : get all the Valuations.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Valuations in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<ValuationDTO>>> getAllValuations(
        ValuationCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        LOG.debug("REST request to get Valuations by criteria: {}", criteria);
        return valuationService
            .countByCriteria(criteria)
            .zipWith(valuationService.findByCriteria(criteria, pageable).collectList())
            .map(countWithEntities ->
                ResponseEntity.ok()
                    .headers(
                        PaginationUtil.generatePaginationHttpHeaders(
                            ForwardedHeaderUtils.adaptFromForwardedHeaders(request.getURI(), request.getHeaders()),
                            new PageImpl<>(countWithEntities.getT2(), pageable, countWithEntities.getT1())
                        )
                    )
                    .body(countWithEntities.getT2())
            );
    }

    /**
     * {@code GET  /valuations/count} : count all the valuations.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public Mono<ResponseEntity<Long>> countValuations(ValuationCriteria criteria) {
        LOG.debug("REST request to count Valuations by criteria: {}", criteria);
        return valuationService.countByCriteria(criteria).map(count -> ResponseEntity.status(HttpStatus.OK).body(count));
    }

    /**
     * {@code GET  /valuations/:id} : get the "id" valuation.
     *
     * @param id the id of the valuationDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the valuationDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ValuationDTO>> getValuation(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get Valuation : {}", id);
        Mono<ValuationDTO> valuationDTO = valuationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(valuationDTO);
    }

    /**
     * {@code DELETE  /valuations/:id} : delete the "id" valuation.
     *
     * @param id the id of the valuationDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteValuation(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete Valuation : {}", id);
        return valuationService
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
