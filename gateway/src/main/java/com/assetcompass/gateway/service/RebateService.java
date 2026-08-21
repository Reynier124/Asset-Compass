package com.assetcompass.gateway.service;

import com.assetcompass.gateway.domain.criteria.RebateCriteria;
import com.assetcompass.gateway.service.dto.RebateDTO;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link com.assetcompass.gateway.domain.Rebate}.
 */
public interface RebateService {
    /**
     * Save a rebate.
     *
     * @param rebateDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<RebateDTO> save(RebateDTO rebateDTO);

    /**
     * Updates a rebate.
     *
     * @param rebateDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<RebateDTO> update(RebateDTO rebateDTO);

    /**
     * Partially updates a rebate.
     *
     * @param rebateDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<RebateDTO> partialUpdate(RebateDTO rebateDTO);
    /**
     * Find rebates by criteria.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<RebateDTO> findByCriteria(RebateCriteria criteria, Pageable pageable);

    /**
     * Find the count of rebates by criteria.
     * @param criteria filtering criteria
     * @return the count of rebates
     */
    public Mono<Long> countByCriteria(RebateCriteria criteria);

    /**
     * Returns the number of rebates available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Get the "id" rebate.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<RebateDTO> findOne(UUID id);

    /**
     * Delete the "id" rebate.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(UUID id);
}
