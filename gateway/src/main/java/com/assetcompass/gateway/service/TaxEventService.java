package com.assetcompass.gateway.service;

import com.assetcompass.gateway.domain.criteria.TaxEventCriteria;
import com.assetcompass.gateway.service.dto.TaxEventDTO;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link com.assetcompass.gateway.domain.TaxEvent}.
 */
public interface TaxEventService {
    /**
     * Save a taxEvent.
     *
     * @param taxEventDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<TaxEventDTO> save(TaxEventDTO taxEventDTO);

    /**
     * Updates a taxEvent.
     *
     * @param taxEventDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<TaxEventDTO> update(TaxEventDTO taxEventDTO);

    /**
     * Partially updates a taxEvent.
     *
     * @param taxEventDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<TaxEventDTO> partialUpdate(TaxEventDTO taxEventDTO);
    /**
     * Find taxEvents by criteria.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<TaxEventDTO> findByCriteria(TaxEventCriteria criteria, Pageable pageable);

    /**
     * Find the count of taxEvents by criteria.
     * @param criteria filtering criteria
     * @return the count of taxEvents
     */
    public Mono<Long> countByCriteria(TaxEventCriteria criteria);

    /**
     * Returns the number of taxEvents available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Get the "id" taxEvent.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<TaxEventDTO> findOne(UUID id);

    /**
     * Delete the "id" taxEvent.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(UUID id);
}
