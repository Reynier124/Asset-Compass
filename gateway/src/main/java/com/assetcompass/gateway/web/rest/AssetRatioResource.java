package com.assetcompass.gateway.web.rest;

import com.assetcompass.gateway.domain.criteria.AssetRatioCriteria;
import com.assetcompass.gateway.repository.AssetRatioRepository;
import com.assetcompass.gateway.service.AssetRatioService;
import com.assetcompass.gateway.service.dto.AssetRatioDTO;
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
 * REST controller for managing {@link com.assetcompass.gateway.domain.AssetRatio}.
 */
@RestController
@RequestMapping("/api/asset-ratios")
public class AssetRatioResource {

    private static final Logger LOG = LoggerFactory.getLogger(AssetRatioResource.class);

    private static final String ENTITY_NAME = "assetRatio";

    @Value("${jhipster.clientApp.name:gateway}")
    private String applicationName;

    private final AssetRatioService assetRatioService;

    private final AssetRatioRepository assetRatioRepository;

    public AssetRatioResource(AssetRatioService assetRatioService, AssetRatioRepository assetRatioRepository) {
        this.assetRatioService = assetRatioService;
        this.assetRatioRepository = assetRatioRepository;
    }

    /**
     * {@code POST  /asset-ratios} : Create a new assetRatio.
     *
     * @param assetRatioDTO the assetRatioDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new assetRatioDTO, or with status {@code 400 (Bad Request)} if the assetRatio has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<AssetRatioDTO>> createAssetRatio(@Valid @RequestBody AssetRatioDTO assetRatioDTO) throws URISyntaxException {
        LOG.debug("REST request to save AssetRatio : {}", assetRatioDTO);
        if (assetRatioDTO.getId() != null) {
            throw new BadRequestAlertException("A new assetRatio cannot already have an ID", ENTITY_NAME, "idexists");
        }
        assetRatioDTO.setId(UUID.randomUUID());
        return assetRatioService.save(assetRatioDTO).map(result -> {
            try {
                return ResponseEntity.created(new URI("/api/asset-ratios/" + result.getId()))
                    .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                    .body(result);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * {@code PUT  /asset-ratios/:id} : Updates an existing assetRatio.
     *
     * @param id the id of the assetRatioDTO to save.
     * @param assetRatioDTO the assetRatioDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated assetRatioDTO,
     * or with status {@code 400 (Bad Request)} if the assetRatioDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the assetRatioDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<AssetRatioDTO>> updateAssetRatio(
        @PathVariable(value = "id", required = false) final UUID id,
        @Valid @RequestBody AssetRatioDTO assetRatioDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update AssetRatio : {}, {}", id, assetRatioDTO);
        if (assetRatioDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, assetRatioDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return assetRatioRepository.existsById(id).flatMap(exists -> {
            if (!exists) {
                return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
            }

            return assetRatioService
                .update(assetRatioDTO)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .map(result ->
                    ResponseEntity.ok()
                        .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result)
                );
        });
    }

    /**
     * {@code PATCH  /asset-ratios/:id} : Partial updates given fields of an existing assetRatio, field will ignore if it is null
     *
     * @param id the id of the assetRatioDTO to save.
     * @param assetRatioDTO the assetRatioDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated assetRatioDTO,
     * or with status {@code 400 (Bad Request)} if the assetRatioDTO is not valid,
     * or with status {@code 404 (Not Found)} if the assetRatioDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the assetRatioDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<AssetRatioDTO>> partialUpdateAssetRatio(
        @PathVariable(value = "id", required = false) final UUID id,
        @NotNull @RequestBody AssetRatioDTO assetRatioDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update AssetRatio partially : {}, {}", id, assetRatioDTO);
        if (assetRatioDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, assetRatioDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return assetRatioRepository.existsById(id).flatMap(exists -> {
            if (!exists) {
                return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
            }

            Mono<AssetRatioDTO> result = assetRatioService.partialUpdate(assetRatioDTO);

            return result.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND))).map(res ->
                ResponseEntity.ok()
                    .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId().toString()))
                    .body(res)
            );
        });
    }

    /**
     * {@code GET  /asset-ratios} : get all the Asset Ratios.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Asset Ratios in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<AssetRatioDTO> getAllAssetRatios(AssetRatioCriteria criteria) {
        LOG.debug("REST request to get AssetRatios by criteria: {}", criteria);
        return assetRatioService.findByCriteria(criteria);
    }

    /**
     * {@code GET  /asset-ratios/count} : count all the assetRatios.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public Mono<ResponseEntity<Long>> countAssetRatios(AssetRatioCriteria criteria) {
        LOG.debug("REST request to count AssetRatios by criteria: {}", criteria);
        return assetRatioService.countByCriteria(criteria).map(count -> ResponseEntity.status(HttpStatus.OK).body(count));
    }

    /**
     * {@code GET  /asset-ratios/:id} : get the "id" assetRatio.
     *
     * @param id the id of the assetRatioDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the assetRatioDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<AssetRatioDTO>> getAssetRatio(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get AssetRatio : {}", id);
        Mono<AssetRatioDTO> assetRatioDTO = assetRatioService.findOne(id);
        return ResponseUtil.wrapOrNotFound(assetRatioDTO);
    }

    /**
     * {@code DELETE  /asset-ratios/:id} : delete the "id" assetRatio.
     *
     * @param id the id of the assetRatioDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteAssetRatio(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete AssetRatio : {}", id);
        return assetRatioService
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
