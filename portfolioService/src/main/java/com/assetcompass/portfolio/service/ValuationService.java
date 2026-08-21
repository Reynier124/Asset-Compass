package com.assetcompass.portfolio.service;

import com.assetcompass.portfolio.service.dto.ValuationDTO;
import java.util.Optional;
import java.util.UUID;

/**
 * Service Interface for managing {@link com.assetcompass.portfolio.domain.Valuation}.
 */
public interface ValuationService {
    /**
     * Save a valuation.
     *
     * @param valuationDTO the entity to save.
     * @return the persisted entity.
     */
    ValuationDTO save(ValuationDTO valuationDTO);

    /**
     * Updates a valuation.
     *
     * @param valuationDTO the entity to update.
     * @return the persisted entity.
     */
    ValuationDTO update(ValuationDTO valuationDTO);

    /**
     * Partially updates a valuation.
     *
     * @param valuationDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<ValuationDTO> partialUpdate(ValuationDTO valuationDTO);

    /**
     * Get the "id" valuation.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ValuationDTO> findOne(UUID id);

    /**
     * Delete the "id" valuation.
     *
     * @param id the id of the entity.
     */
    void delete(UUID id);
}
