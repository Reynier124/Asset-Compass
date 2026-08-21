package com.assetcompass.portfolio.service;

import com.assetcompass.portfolio.service.dto.AssetRatioDTO;
import java.util.Optional;
import java.util.UUID;

/**
 * Service Interface for managing {@link com.assetcompass.portfolio.domain.AssetRatio}.
 */
public interface AssetRatioService {
    /**
     * Save a assetRatio.
     *
     * @param assetRatioDTO the entity to save.
     * @return the persisted entity.
     */
    AssetRatioDTO save(AssetRatioDTO assetRatioDTO);

    /**
     * Updates a assetRatio.
     *
     * @param assetRatioDTO the entity to update.
     * @return the persisted entity.
     */
    AssetRatioDTO update(AssetRatioDTO assetRatioDTO);

    /**
     * Partially updates a assetRatio.
     *
     * @param assetRatioDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<AssetRatioDTO> partialUpdate(AssetRatioDTO assetRatioDTO);

    /**
     * Get the "id" assetRatio.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AssetRatioDTO> findOne(UUID id);

    /**
     * Delete the "id" assetRatio.
     *
     * @param id the id of the entity.
     */
    void delete(UUID id);
}
