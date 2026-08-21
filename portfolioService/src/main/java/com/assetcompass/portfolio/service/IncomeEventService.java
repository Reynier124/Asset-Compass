package com.assetcompass.portfolio.service;

import com.assetcompass.portfolio.service.dto.IncomeEventDTO;
import java.util.Optional;
import java.util.UUID;

/**
 * Service Interface for managing {@link com.assetcompass.portfolio.domain.IncomeEvent}.
 */
public interface IncomeEventService {
    /**
     * Save a incomeEvent.
     *
     * @param incomeEventDTO the entity to save.
     * @return the persisted entity.
     */
    IncomeEventDTO save(IncomeEventDTO incomeEventDTO);

    /**
     * Updates a incomeEvent.
     *
     * @param incomeEventDTO the entity to update.
     * @return the persisted entity.
     */
    IncomeEventDTO update(IncomeEventDTO incomeEventDTO);

    /**
     * Partially updates a incomeEvent.
     *
     * @param incomeEventDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<IncomeEventDTO> partialUpdate(IncomeEventDTO incomeEventDTO);

    /**
     * Get the "id" incomeEvent.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<IncomeEventDTO> findOne(UUID id);

    /**
     * Delete the "id" incomeEvent.
     *
     * @param id the id of the entity.
     */
    void delete(UUID id);
}
