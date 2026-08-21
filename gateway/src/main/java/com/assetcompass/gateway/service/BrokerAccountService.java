package com.assetcompass.gateway.service;

import com.assetcompass.gateway.domain.criteria.BrokerAccountCriteria;
import com.assetcompass.gateway.service.dto.BrokerAccountDTO;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Interface for managing {@link com.assetcompass.gateway.domain.BrokerAccount}.
 */
public interface BrokerAccountService {
    /**
     * Save a brokerAccount.
     *
     * @param brokerAccountDTO the entity to save.
     * @return the persisted entity.
     */
    Mono<BrokerAccountDTO> save(BrokerAccountDTO brokerAccountDTO);

    /**
     * Updates a brokerAccount.
     *
     * @param brokerAccountDTO the entity to update.
     * @return the persisted entity.
     */
    Mono<BrokerAccountDTO> update(BrokerAccountDTO brokerAccountDTO);

    /**
     * Partially updates a brokerAccount.
     *
     * @param brokerAccountDTO the entity to update partially.
     * @return the persisted entity.
     */
    Mono<BrokerAccountDTO> partialUpdate(BrokerAccountDTO brokerAccountDTO);
    /**
     * Find brokerAccounts by criteria.
     *
     * @return the list of entities.
     */
    Flux<BrokerAccountDTO> findByCriteria(BrokerAccountCriteria criteria);

    /**
     * Find the count of brokerAccounts by criteria.
     * @param criteria filtering criteria
     * @return the count of brokerAccounts
     */
    public Mono<Long> countByCriteria(BrokerAccountCriteria criteria);

    /**
     * Returns the number of brokerAccounts available.
     * @return the number of entities in the database.
     *
     */
    Mono<Long> countAll();

    /**
     * Get the "id" brokerAccount.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<BrokerAccountDTO> findOne(UUID id);

    /**
     * Delete the "id" brokerAccount.
     *
     * @param id the id of the entity.
     * @return a Mono to signal the deletion
     */
    Mono<Void> delete(UUID id);
}
