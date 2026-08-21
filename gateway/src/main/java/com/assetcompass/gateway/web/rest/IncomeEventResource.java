package com.assetcompass.gateway.web.rest;

import com.assetcompass.gateway.domain.criteria.IncomeEventCriteria;
import com.assetcompass.gateway.repository.IncomeEventRepository;
import com.assetcompass.gateway.service.IncomeEventService;
import com.assetcompass.gateway.service.dto.IncomeEventDTO;
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
 * REST controller for managing {@link com.assetcompass.gateway.domain.IncomeEvent}.
 */
@RestController
@RequestMapping("/api/income-events")
public class IncomeEventResource {

    private static final Logger LOG = LoggerFactory.getLogger(IncomeEventResource.class);

    private static final String ENTITY_NAME = "incomeEvent";

    @Value("${jhipster.clientApp.name:gateway}")
    private String applicationName;

    private final IncomeEventService incomeEventService;

    private final IncomeEventRepository incomeEventRepository;

    public IncomeEventResource(IncomeEventService incomeEventService, IncomeEventRepository incomeEventRepository) {
        this.incomeEventService = incomeEventService;
        this.incomeEventRepository = incomeEventRepository;
    }

    /**
     * {@code POST  /income-events} : Create a new incomeEvent.
     *
     * @param incomeEventDTO the incomeEventDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new incomeEventDTO, or with status {@code 400 (Bad Request)} if the incomeEvent has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<IncomeEventDTO>> createIncomeEvent(@Valid @RequestBody IncomeEventDTO incomeEventDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save IncomeEvent : {}", incomeEventDTO);
        if (incomeEventDTO.getId() != null) {
            throw new BadRequestAlertException("A new incomeEvent cannot already have an ID", ENTITY_NAME, "idexists");
        }
        incomeEventDTO.setId(UUID.randomUUID());
        return incomeEventService.save(incomeEventDTO).map(result -> {
            try {
                return ResponseEntity.created(new URI("/api/income-events/" + result.getId()))
                    .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                    .body(result);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
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
    public Mono<ResponseEntity<IncomeEventDTO>> updateIncomeEvent(
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

        return incomeEventRepository.existsById(id).flatMap(exists -> {
            if (!exists) {
                return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
            }

            return incomeEventService
                .update(incomeEventDTO)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .map(result ->
                    ResponseEntity.ok()
                        .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result)
                );
        });
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
    public Mono<ResponseEntity<IncomeEventDTO>> partialUpdateIncomeEvent(
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

        return incomeEventRepository.existsById(id).flatMap(exists -> {
            if (!exists) {
                return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
            }

            Mono<IncomeEventDTO> result = incomeEventService.partialUpdate(incomeEventDTO);

            return result.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND))).map(res ->
                ResponseEntity.ok()
                    .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId().toString()))
                    .body(res)
            );
        });
    }

    /**
     * {@code GET  /income-events} : get all the Income Events.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Income Events in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<IncomeEventDTO>>> getAllIncomeEvents(
        IncomeEventCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        LOG.debug("REST request to get IncomeEvents by criteria: {}", criteria);
        return incomeEventService
            .countByCriteria(criteria)
            .zipWith(incomeEventService.findByCriteria(criteria, pageable).collectList())
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
     * {@code GET  /income-events/count} : count all the incomeEvents.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public Mono<ResponseEntity<Long>> countIncomeEvents(IncomeEventCriteria criteria) {
        LOG.debug("REST request to count IncomeEvents by criteria: {}", criteria);
        return incomeEventService.countByCriteria(criteria).map(count -> ResponseEntity.status(HttpStatus.OK).body(count));
    }

    /**
     * {@code GET  /income-events/:id} : get the "id" incomeEvent.
     *
     * @param id the id of the incomeEventDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the incomeEventDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<IncomeEventDTO>> getIncomeEvent(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get IncomeEvent : {}", id);
        Mono<IncomeEventDTO> incomeEventDTO = incomeEventService.findOne(id);
        return ResponseUtil.wrapOrNotFound(incomeEventDTO);
    }

    /**
     * {@code DELETE  /income-events/:id} : delete the "id" incomeEvent.
     *
     * @param id the id of the incomeEventDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteIncomeEvent(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete IncomeEvent : {}", id);
        return incomeEventService
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
