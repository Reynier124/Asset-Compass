package com.assetcompass.gateway.service;

import com.assetcompass.gateway.domain.criteria.ValuationCriteria;
import com.assetcompass.gateway.service.dto.ValuationDTO;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link com.assetcompass.gateway.domain.Valuation}.
 */
public interface ValuationService {
    /**
     * Save a valuation.
     *
     * @param valuationDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<ValuationDTO> save(ValuationDTO valuationDTO);

    /**
     * Updates a valuation.
     *
     * @param valuationDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<ValuationDTO> update(ValuationDTO valuationDTO);

    /**
     * Partially updates a valuation.
     *
     * @param valuationDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<ValuationDTO> partialUpdate(ValuationDTO valuationDTO);
    /**
     * Find valuations by criteria.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<ValuationDTO> findByCriteria(ValuationCriteria criteria, Pageable pageable);

    /**
     * Find the count of valuations by criteria.
     * @param criteria filtering criteria
     * @return the count of valuations
     */
    public Mono<Long> countByCriteria(ValuationCriteria criteria);

    /**
     * Returns the number of valuations available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Get the "id" valuation.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<ValuationDTO> findOne(UUID id);

    /**
     * Delete the "id" valuation.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(UUID id);
}
