package com.assetcompass.portfolio.service;

import com.assetcompass.portfolio.service.dto.TaxEventDTO;
import java.util.Optional;
import java.util.UUID;

/**
 * Service Interface for managing {@link com.assetcompass.portfolio.domain.TaxEvent}.
 */
public interface TaxEventService {
    /**
     * Save a taxEvent.
     *
     * @param taxEventDTO the entity to save.
     * @return the persisted entity.
     */
    TaxEventDTO save(TaxEventDTO taxEventDTO);

    /**
     * Updates a taxEvent.
     *
     * @param taxEventDTO the entity to update.
     * @return the persisted entity.
     */
    TaxEventDTO update(TaxEventDTO taxEventDTO);

    /**
     * Partially updates a taxEvent.
     *
     * @param taxEventDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<TaxEventDTO> partialUpdate(TaxEventDTO taxEventDTO);

    /**
     * Get the "id" taxEvent.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<TaxEventDTO> findOne(UUID id);

    /**
     * Delete the "id" taxEvent.
     *
     * @param id the id of the entity.
     */
    void delete(UUID id);
}
