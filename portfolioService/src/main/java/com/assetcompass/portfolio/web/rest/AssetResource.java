package com.assetcompass.portfolio.web.rest;

import com.assetcompass.portfolio.repository.AssetRepository;
import com.assetcompass.portfolio.service.AssetQueryService;
import com.assetcompass.portfolio.service.AssetService;
import com.assetcompass.portfolio.service.criteria.AssetCriteria;
import com.assetcompass.portfolio.service.dto.AssetDTO;
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
 * REST controller for managing {@link com.assetcompass.portfolio.domain.Asset}.
 */
@RestController
@RequestMapping("/api/assets")
public class AssetResource {

    private static final Logger LOG = LoggerFactory.getLogger(AssetResource.class);

    private static final String ENTITY_NAME = "portfolioServiceAsset";

    @Value("${jhipster.clientApp.name:portfolioService}")
    private String applicationName;

    private final AssetService assetService;

    private final AssetRepository assetRepository;

    private final AssetQueryService assetQueryService;

    public AssetResource(AssetService assetService, AssetRepository assetRepository, AssetQueryService assetQueryService) {
        this.assetService = assetService;
        this.assetRepository = assetRepository;
        this.assetQueryService = assetQueryService;
    }

    /**
     * {@code POST  /assets} : Create a new asset.
     *
     * @param assetDTO the assetDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new assetDTO, or with status {@code 400 (Bad Request)} if the asset has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<AssetDTO> createAsset(@Valid @RequestBody AssetDTO assetDTO) throws URISyntaxException {
        LOG.debug("REST request to save Asset : {}", assetDTO);
        if (assetDTO.getId() != null) {
            throw new BadRequestAlertException("A new asset cannot already have an ID", ENTITY_NAME, "idexists");
        }
        assetDTO = assetService.save(assetDTO);
        return ResponseEntity.created(new URI("/api/assets/" + assetDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, assetDTO.getId().toString()))
            .body(assetDTO);
    }

    /**
     * {@code PUT  /assets/:id} : Updates an existing asset.
     *
     * @param id the id of the assetDTO to save.
     * @param assetDTO the assetDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated assetDTO,
     * or with status {@code 400 (Bad Request)} if the assetDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the assetDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AssetDTO> updateAsset(
        @PathVariable(value = "id", required = false) final UUID id,
        @Valid @RequestBody AssetDTO assetDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Asset : {}, {}", id, assetDTO);
        if (assetDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, assetDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!assetRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        assetDTO = assetService.update(assetDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, assetDTO.getId().toString()))
            .body(assetDTO);
    }

    /**
     * {@code PATCH  /assets/:id} : Partial updates given fields of an existing asset, field will ignore if it is null
     *
     * @param id the id of the assetDTO to save.
     * @param assetDTO the assetDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated assetDTO,
     * or with status {@code 400 (Bad Request)} if the assetDTO is not valid,
     * or with status {@code 404 (Not Found)} if the assetDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the assetDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<AssetDTO> partialUpdateAsset(
        @PathVariable(value = "id", required = false) final UUID id,
        @NotNull @RequestBody AssetDTO assetDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Asset partially : {}, {}", id, assetDTO);
        if (assetDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, assetDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!assetRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<AssetDTO> result = assetService.partialUpdate(assetDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, assetDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /assets} : get all the Assets.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Assets in body.
     */
    @GetMapping("")
    public ResponseEntity<List<AssetDTO>> getAllAssets(AssetCriteria criteria) {
        LOG.debug("REST request to get Assets by criteria: {}", criteria);

        List<AssetDTO> entityList = assetQueryService.findByCriteria(criteria);
        return ResponseEntity.ok().body(entityList);
    }

    /**
     * {@code GET  /assets/count} : count all the assets.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countAssets(AssetCriteria criteria) {
        LOG.debug("REST request to count Assets by criteria: {}", criteria);
        return ResponseEntity.ok().body(assetQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /assets/:id} : get the "id" asset.
     *
     * @param id the id of the assetDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the assetDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AssetDTO> getAsset(@PathVariable("id") UUID id) {
        LOG.debug("REST request to get Asset : {}", id);
        Optional<AssetDTO> assetDTO = assetService.findOne(id);
        return ResponseUtil.wrapOrNotFound(assetDTO);
    }

    /**
     * {@code DELETE  /assets/:id} : delete the "id" asset.
     *
     * @param id the id of the assetDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAsset(@PathVariable("id") UUID id) {
        LOG.debug("REST request to delete Asset : {}", id);
        assetService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
