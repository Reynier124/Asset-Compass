package com.assetcompass.gateway.web.rest;

import com.assetcompass.gateway.domain.criteria.PositionCriteria;
import com.assetcompass.gateway.repository.PositionRepository;
import com.assetcompass.gateway.service.PositionService;
import com.assetcompass.gateway.service.dto.PositionDTO;
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
 * REST controller for managing {@link com.assetcompass.gateway.domain.Position}.
 */
@RestController
@RequestMapping("/api/positions")
public class PositionResource {

    private static final Logger LOG = LoggerFactory.getLogger(PositionResource.class);

    private static final String ENTITY_NAME = "position";

    @Value("${jhipster.clientApp.name:gateway}")
    private String applicationName;

    private final PositionService positionService;

    private final PositionRepository positionRepository;

    public PositionResource(PositionService positionService, PositionRepository positionRepository) {
        this.positionService = positionService;
        this.positionRepository = positionRepository;
    }

    /**
     * {@code POST  /positions} : Create a new position.
     *
     * @param positionDTO the positionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new positionDTO, or with status {@code 400 (Bad Request)} if the position has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<PositionDTO>> createPosition(@Valid @RequestBody PositionDTO positionDTO) throws URISyntaxException {
        LOG.debug("REST request to save Position : {}", positionDTO);
        if (positionDTO.getId() != null) {
            throw new BadRequestAlertException("A new position cannot already have an ID", ENTITY_NAME, "idexists");
        }
        positionDTO.setId(UUID.randomUUID());
        return positionService.save(positionDTO).map(result -> {
            try {
                return ResponseEntity.created(new URI("/api/positions/" + result.getId()))
                    .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                    .body(result);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * {@code PUT  /positions/:id} : Updates an existing position.
     *
     * @param id the id of the positionDTO to save.
     * @param positionDTO the positionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated positionDTO,
     * or with status {@code 400 (Bad Request)} if the positionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the positionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<PositionDTO>> updatePosition(
        @PathVariable(value = "id", required = false) final UUID id,
        @Valid @RequestBody PositionDTO positionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Position : {}, {}", id, positionDTO);
        if (positionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, positionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return positionRepository.existsById(id).flatMap(exists -> {
            if (!exists) {
                return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
            }

            return positionService
                .update(positionDTO)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .map(result ->
                    ResponseEntity.ok()
                        .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result)
                );
        });
    }

    /**
     * {@code PATCH  /positions/:id} : Partial updates given fields of an existing position, field will ignore if it is null
     *
     * @param id the id of the positionDTO to save.
     * @param positionDTO the positionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated positionDTO,
     * or with status {@code 400 (Bad Request)} if the positionDTO is not valid,
     * or with status {@code 404 (Not Found)} if the positionDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the positionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<PositionDTO>> partialUpdatePosition(
        @PathVariable(value = "id", required = false) final UUID id,
        @NotNull @RequestBody PositionDTO positionDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Position partially : {}, {}", id, positionDTO);
        if (positionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, positionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return positionRepository.existsById(id).flatMap(exists -> {
            if (!exists) {
                return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
            }

            Mono<PositionDTO> result = positionService.partialUpdate(positionDTO);

            return result.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND))).map(res ->
                ResponseEntity.ok()
                    .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId().toString()))
                    .body(res)
            );
        });
    }

    /**
     * {@code GET  /positions} : get all the Positions.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Positions in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<PositionDTO>>> getAllPositions(
        PositionCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        LOG.debug("REST request to get Positions by criteria: {}", criteria);
        return positionService
            .countByCriteria(criteria)
            .zipWith(positionService.findByCriteria(criteria, pageable).collectList())
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
     * {@code GET  /positions/count} : count all the positions.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public Mono<ResponseEntity<Long>> countPositions(PositionCriteria criteria) {
        LOG.debug("REST request to count Positions by criteria: {}", criteria);
        return positionService.countByCriteria(criteria).map(count -> ResponseEntity.status(HttpStatus.OK).body(count));
    }

    /**
     * {@code GET  /positions/:id} : get the "id" position.
     *
     * @param id the id of the positionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the positionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<PositionDTO>> getPosition(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get Position : {}", id);
        Mono<PositionDTO> positionDTO = positionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(positionDTO);
    }

    /**
     * {@code DELETE  /positions/:id} : delete the "id" position.
     *
     * @param id the id of the positionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deletePosition(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete Position : {}", id);
        return positionService
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
