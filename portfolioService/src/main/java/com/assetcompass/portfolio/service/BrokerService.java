package com.assetcompass.portfolio.service;

import com.assetcompass.portfolio.service.dto.BrokerDTO;
import java.util.Optional;
import java.util.UUID;

/**
 * Service Interface for managing {@link com.assetcompass.portfolio.domain.Broker}.
 */
public interface BrokerService {
    /**
     * Save a broker.
     *
     * @param brokerDTO the entity to save.
     * @return the persisted entity.
     */
    BrokerDTO save(BrokerDTO brokerDTO);

    /**
     * Updates a broker.
     *
     * @param brokerDTO the entity to update.
     * @return the persisted entity.
     */
    BrokerDTO update(BrokerDTO brokerDTO);

    /**
     * Partially updates a broker.
     *
     * @param brokerDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<BrokerDTO> partialUpdate(BrokerDTO brokerDTO);

    /**
     * Get the "id" broker.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<BrokerDTO> findOne(UUID id);

    /**
     * Delete the "id" broker.
     *
     * @param id the id of the entity.
     */
    void delete(UUID id);
}
