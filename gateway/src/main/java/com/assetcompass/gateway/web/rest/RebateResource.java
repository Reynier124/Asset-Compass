package com.assetcompass.gateway.web.rest;

import com.assetcompass.gateway.domain.criteria.RebateCriteria;
import com.assetcompass.gateway.repository.RebateRepository;
import com.assetcompass.gateway.service.RebateService;
import com.assetcompass.gateway.service.dto.RebateDTO;
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
 * REST controller for managing {@link com.assetcompass.gateway.domain.Rebate}.
 */
@RestController
@RequestMapping("/api/rebates")
public class RebateResource {

    private static final Logger LOG = LoggerFactory.getLogger(RebateResource.class);

    private static final String ENTITY_NAME = "rebate";

    @Value("${jhipster.clientApp.name:gateway}")
    private String applicationName;

    private final RebateService rebateService;

    private final RebateRepository rebateRepository;

    public RebateResource(RebateService rebateService, RebateRepository rebateRepository) {
        this.rebateService = rebateService;
        this.rebateRepository = rebateRepository;
    }

    /**
     * {@code POST  /rebates} : Create a new rebate.
     *
     * @param rebateDTO the rebateDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new rebateDTO, or with status {@code 400 (Bad Request)} if the rebate has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<RebateDTO>> createRebate(@Valid @RequestBody RebateDTO rebateDTO) throws URISyntaxException {
        LOG.debug("REST request to save Rebate : {}", rebateDTO);
        if (rebateDTO.getId() != null) {
            throw new BadRequestAlertException("A new rebate cannot already have an ID", ENTITY_NAME, "idexists");
        }
        rebateDTO.setId(UUID.randomUUID());
        return rebateService.save(rebateDTO).map(result -> {
            try {
                return ResponseEntity.created(new URI("/api/rebates/" + result.getId()))
                    .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                    .body(result);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
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
    public Mono<ResponseEntity<RebateDTO>> updateRebate(
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

        return rebateRepository.existsById(id).flatMap(exists -> {
            if (!exists) {
                return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
            }

            return rebateService
                .update(rebateDTO)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .map(result ->
                    ResponseEntity.ok()
                        .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result)
                );
        });
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
    public Mono<ResponseEntity<RebateDTO>> partialUpdateRebate(
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

        return rebateRepository.existsById(id).flatMap(exists -> {
            if (!exists) {
                return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
            }

            Mono<RebateDTO> result = rebateService.partialUpdate(rebateDTO);

            return result.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND))).map(res ->
                ResponseEntity.ok()
                    .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId().toString()))
                    .body(res)
            );
        });
    }

    /**
     * {@code GET  /rebates} : get all the Rebates.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Rebates in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<RebateDTO>>> getAllRebates(
        RebateCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        LOG.debug("REST request to get Rebates by criteria: {}", criteria);
        return rebateService
            .countByCriteria(criteria)
            .zipWith(rebateService.findByCriteria(criteria, pageable).collectList())
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
     * {@code GET  /rebates/count} : count all the rebates.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public Mono<ResponseEntity<Long>> countRebates(RebateCriteria criteria) {
        LOG.debug("REST request to count Rebates by criteria: {}", criteria);
        return rebateService.countByCriteria(criteria).map(count -> ResponseEntity.status(HttpStatus.OK).body(count));
    }

    /**
     * {@code GET  /rebates/:id} : get the "id" rebate.
     *
     * @param id the id of the rebateDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the rebateDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<RebateDTO>> getRebate(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get Rebate : {}", id);
        Mono<RebateDTO> rebateDTO = rebateService.findOne(id);
        return ResponseUtil.wrapOrNotFound(rebateDTO);
    }

    /**
     * {@code DELETE  /rebates/:id} : delete the "id" rebate.
     *
     * @param id the id of the rebateDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteRebate(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete Rebate : {}", id);
        return rebateService
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
