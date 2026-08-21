package com.assetcompass.portfolio.web.rest;

import com.assetcompass.portfolio.repository.AssetRatioRepository;
import com.assetcompass.portfolio.service.AssetRatioQueryService;
import com.assetcompass.portfolio.service.AssetRatioService;
import com.assetcompass.portfolio.service.criteria.AssetRatioCriteria;
import com.assetcompass.portfolio.service.dto.AssetRatioDTO;
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
 * REST controller for managing {@link com.assetcompass.portfolio.domain.AssetRatio}.
 */
@RestController
@RequestMapping("/api/asset-ratios")
public class AssetRatioResource {

    private static final Logger LOG = LoggerFactory.getLogger(AssetRatioResource.class);

    private static final String ENTITY_NAME = "portfolioServiceAssetRatio";

    @Value("${jhipster.clientApp.name:portfolioService}")
    private String applicationName;

    private final AssetRatioService assetRatioService;

    private final AssetRatioRepository assetRatioRepository;

    private final AssetRatioQueryService assetRatioQueryService;

    public AssetRatioResource(
        AssetRatioService assetRatioService,
        AssetRatioRepository assetRatioRepository,
        AssetRatioQueryService assetRatioQueryService
    ) {
        this.assetRatioService = assetRatioService;
        this.assetRatioRepository = assetRatioRepository;
        this.assetRatioQueryService = assetRatioQueryService;
    }

    /**
     * {@code POST  /asset-ratios} : Create a new assetRatio.
     *
     * @param assetRatioDTO the assetRatioDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new assetRatioDTO, or with status {@code 400 (Bad Request)} if the assetRatio has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<AssetRatioDTO> createAssetRatio(@Valid @RequestBody AssetRatioDTO assetRatioDTO) throws URISyntaxException {
        LOG.debug("REST request to save AssetRatio : {}", assetRatioDTO);
        if (assetRatioDTO.getId() != null) {
            throw new BadRequestAlertException("A new assetRatio cannot already have an ID", ENTITY_NAME, "idexists");
        }
        assetRatioDTO = assetRatioService.save(assetRatioDTO);
        return ResponseEntity.created(new URI("/api/asset-ratios/" + assetRatioDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, assetRatioDTO.getId().toString()))
            .body(assetRatioDTO);
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
    public ResponseEntity<AssetRatioDTO> updateAssetRatio(
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

        if (!assetRatioRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        assetRatioDTO = assetRatioService.update(assetRatioDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, assetRatioDTO.getId().toString()))
            .body(assetRatioDTO);
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
    public ResponseEntity<AssetRatioDTO> partialUpdateAssetRatio(
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

        if (!assetRatioRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<AssetRatioDTO> result = assetRatioService.partialUpdate(assetRatioDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, assetRatioDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /asset-ratios} : get all the Asset Ratios.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Asset Ratios in body.
     */
    @GetMapping("")
    public ResponseEntity<List<AssetRatioDTO>> getAllAssetRatios(AssetRatioCriteria criteria) {
        LOG.debug("REST request to get AssetRatios by criteria: {}", criteria);

        List<AssetRatioDTO> entityList = assetRatioQueryService.findByCriteria(criteria);
        return ResponseEntity.ok().body(entityList);
    }

    /**
     * {@code GET  /asset-ratios/count} : count all the assetRatios.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countAssetRatios(AssetRatioCriteria criteria) {
        LOG.debug("REST request to count AssetRatios by criteria: {}", criteria);
        return ResponseEntity.ok().body(assetRatioQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /asset-ratios/:id} : get the "id" assetRatio.
     *
     * @param id the id of the assetRatioDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the assetRatioDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AssetRatioDTO> getAssetRatio(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get AssetRatio : {}", id);
        Optional<AssetRatioDTO> assetRatioDTO = assetRatioService.findOne(id);
        return ResponseUtil.wrapOrNotFound(assetRatioDTO);
    }

    /**
     * {@code DELETE  /asset-ratios/:id} : delete the "id" assetRatio.
     *
     * @param id the id of the assetRatioDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssetRatio(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete AssetRatio : {}", id);
        assetRatioService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
