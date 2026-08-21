package com.assetcompass.gateway.service;

import com.assetcompass.gateway.domain.criteria.AssetRatioCriteria;
import com.assetcompass.gateway.service.dto.AssetRatioDTO;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link com.assetcompass.gateway.domain.AssetRatio}.
 */
public interface AssetRatioService {
    /**
     * Save a assetRatio.
     *
     * @param assetRatioDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<AssetRatioDTO> save(AssetRatioDTO assetRatioDTO);

    /**
     * Updates a assetRatio.
     *
     * @param assetRatioDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<AssetRatioDTO> update(AssetRatioDTO assetRatioDTO);

    /**
     * Partially updates a assetRatio.
     *
     * @param assetRatioDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<AssetRatioDTO> partialUpdate(AssetRatioDTO assetRatioDTO);
    /**
     * Find assetRatios by criteria.
     *
     * @return the list of entities.
     */
    Flux<AssetRatioDTO> findByCriteria(AssetRatioCriteria criteria);

    /**
     * Find the count of assetRatios by criteria.
     * @param criteria filtering criteria
     * @return the count of assetRatios
     */
    public Mono<Long> countByCriteria(AssetRatioCriteria criteria);

    /**
     * Returns the number of assetRatios available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Get the "id" assetRatio.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<AssetRatioDTO> findOne(UUID id);

    /**
     * Delete the "id" assetRatio.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(UUID id);
}
