package com.assetcompass.portfolio.service;

import com.assetcompass.portfolio.service.dto.RebateDTO;
import java.util.Optional;
import java.util.UUID;

/**
 * Service Interface for managing {@link com.assetcompass.portfolio.domain.Rebate}.
 */
public interface RebateService {
    /**
     * Save a rebate.
     *
     * @param rebateDTO the entity to save.
     * @return the persisted entity.
     */
    RebateDTO save(RebateDTO rebateDTO);

    /**
     * Updates a rebate.
     *
     * @param rebateDTO the entity to update.
     * @return the persisted entity.
     */
    RebateDTO update(RebateDTO rebateDTO);

    /**
     * Partially updates a rebate.
     *
     * @param rebateDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<RebateDTO> partialUpdate(RebateDTO rebateDTO);

    /**
     * Get the "id" rebate.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<RebateDTO> findOne(UUID id);

    /**
     * Delete the "id" rebate.
     *
     * @param id the id of the entity.
     */
    void delete(UUID id);
}
