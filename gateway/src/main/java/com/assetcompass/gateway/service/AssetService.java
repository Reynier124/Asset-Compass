package com.assetcompass.gateway.service;

import com.assetcompass.gateway.domain.criteria.AssetCriteria;
import com.assetcompass.gateway.service.dto.AssetDTO;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link com.assetcompass.gateway.domain.Asset}.
 */
public interface AssetService {
    /**
     * Save a asset.
     *
     * @param assetDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<AssetDTO> save(AssetDTO assetDTO);

    /**
     * Updates a asset.
     *
     * @param assetDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<AssetDTO> update(AssetDTO assetDTO);

    /**
     * Partially updates a asset.
     *
     * @param assetDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<AssetDTO> partialUpdate(AssetDTO assetDTO);
    /**
     * Find assets by criteria.
     *
     * @return the list of entities.
     */
    Flux<AssetDTO> findByCriteria(AssetCriteria criteria);

    /**
     * Find the count of assets by criteria.
     * @param criteria filtering criteria
     * @return the count of assets
     */
    public Mono<Long> countByCriteria(AssetCriteria criteria);

    /**
     * Returns the number of assets available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Get the "id" asset.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<AssetDTO> findOne(UUID id);

    /**
     * Delete the "id" asset.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(UUID id);
}
