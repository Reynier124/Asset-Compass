package com.assetcompass.gateway.service;

import com.assetcompass.gateway.domain.criteria.PositionCriteria;
import com.assetcompass.gateway.service.dto.PositionDTO;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link com.assetcompass.gateway.domain.Position}.
 */
public interface PositionService {
    /**
     * Save a position.
     *
     * @param positionDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<PositionDTO> save(PositionDTO positionDTO);

    /**
     * Updates a position.
     *
     * @param positionDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<PositionDTO> update(PositionDTO positionDTO);

    /**
     * Partially updates a position.
     *
     * @param positionDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<PositionDTO> partialUpdate(PositionDTO positionDTO);
    /**
     * Find positions by criteria.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<PositionDTO> findByCriteria(PositionCriteria criteria, Pageable pageable);

    /**
     * Find the count of positions by criteria.
     * @param criteria filtering criteria
     * @return the count of positions
     */
    public Mono<Long> countByCriteria(PositionCriteria criteria);

    /**
     * Returns the number of positions available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Get the "id" position.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<PositionDTO> findOne(UUID id);

    /**
     * Delete the "id" position.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(UUID id);
}
