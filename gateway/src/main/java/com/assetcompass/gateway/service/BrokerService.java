package com.assetcompass.gateway.service;

import com.assetcompass.gateway.domain.criteria.BrokerCriteria;
import com.assetcompass.gateway.service.dto.BrokerDTO;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link com.assetcompass.gateway.domain.Broker}.
 */
public interface BrokerService {
    /**
     * Save a broker.
     *
     * @param brokerDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<BrokerDTO> save(BrokerDTO brokerDTO);

    /**
     * Updates a broker.
     *
     * @param brokerDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<BrokerDTO> update(BrokerDTO brokerDTO);

    /**
     * Partially updates a broker.
     *
     * @param brokerDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<BrokerDTO> partialUpdate(BrokerDTO brokerDTO);
    /**
     * Find brokers by criteria.
     *
     * @return the list of entities.
     */
    Flux<BrokerDTO> findByCriteria(BrokerCriteria criteria);

    /**
     * Find the count of brokers by criteria.
     * @param criteria filtering criteria
     * @return the count of brokers
     */
    public Mono<Long> countByCriteria(BrokerCriteria criteria);

    /**
     * Returns the number of brokers available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Get the "id" broker.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<BrokerDTO> findOne(UUID id);

    /**
     * Delete the "id" broker.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(UUID id);
}
