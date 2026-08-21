package com.assetcompass.gateway.service;

import com.assetcompass.gateway.domain.criteria.OperationCriteria;
import com.assetcompass.gateway.service.dto.OperationDTO;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link com.assetcompass.gateway.domain.Operation}.
 */
public interface OperationService {
    /**
     * Save a operation.
     *
     * @param operationDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<OperationDTO> save(OperationDTO operationDTO);

    /**
     * Updates a operation.
     *
     * @param operationDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<OperationDTO> update(OperationDTO operationDTO);

    /**
     * Partially updates a operation.
     *
     * @param operationDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<OperationDTO> partialUpdate(OperationDTO operationDTO);
    /**
     * Find operations by criteria.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<OperationDTO> findByCriteria(OperationCriteria criteria, Pageable pageable);

    /**
     * Find the count of operations by criteria.
     * @param criteria filtering criteria
     * @return the count of operations
     */
    public Mono<Long> countByCriteria(OperationCriteria criteria);

    /**
     * Returns the number of operations available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Get the "id" operation.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<OperationDTO> findOne(UUID id);

    /**
     * Delete the "id" operation.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(UUID id);
}
