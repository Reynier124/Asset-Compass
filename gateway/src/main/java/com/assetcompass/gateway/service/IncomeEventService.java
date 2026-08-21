package com.assetcompass.gateway.service;

import com.assetcompass.gateway.domain.criteria.IncomeEventCriteria;
import com.assetcompass.gateway.service.dto.IncomeEventDTO;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link com.assetcompass.gateway.domain.IncomeEvent}.
 */
public interface IncomeEventService {
    /**
     * Save a incomeEvent.
     *
     * @param incomeEventDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<IncomeEventDTO> save(IncomeEventDTO incomeEventDTO);

    /**
     * Updates a incomeEvent.
     *
     * @param incomeEventDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<IncomeEventDTO> update(IncomeEventDTO incomeEventDTO);

    /**
     * Partially updates a incomeEvent.
     *
     * @param incomeEventDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<IncomeEventDTO> partialUpdate(IncomeEventDTO incomeEventDTO);
    /**
     * Find incomeEvents by criteria.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<IncomeEventDTO> findByCriteria(IncomeEventCriteria criteria, Pageable pageable);

    /**
     * Find the count of incomeEvents by criteria.
     * @param criteria filtering criteria
     * @return the count of incomeEvents
     */
    public Mono<Long> countByCriteria(IncomeEventCriteria criteria);

    /**
     * Returns the number of incomeEvents available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Get the "id" incomeEvent.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<IncomeEventDTO> findOne(UUID id);

    /**
     * Delete the "id" incomeEvent.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(UUID id);
}
