package com.assetcompass.portfolio.service;

import com.assetcompass.portfolio.service.dto.PositionDTO;
import java.util.Optional;
import java.util.UUID;

/**
 * Service Interface for managing {@link com.assetcompass.portfolio.domain.Position}.
 */
public interface PositionService {
    /**
     * Save a position.
     *
     * @param positionDTO the entity to save.
     * @return the persisted entity.
     */
    PositionDTO save(PositionDTO positionDTO);

    /**
     * Updates a position.
     *
     * @param positionDTO the entity to update.
     * @return the persisted entity.
     */
    PositionDTO update(PositionDTO positionDTO);

    /**
     * Partially updates a position.
     *
     * @param positionDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<PositionDTO> partialUpdate(PositionDTO positionDTO);

    /**
     * Get the "id" position.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<PositionDTO> findOne(UUID id);

    /**
     * Delete the "id" position.
     *
     * @param id the id of the entity.
     */
    void delete(UUID id);
}
