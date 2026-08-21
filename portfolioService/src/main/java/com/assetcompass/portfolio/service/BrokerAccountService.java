package com.assetcompass.portfolio.service;

import com.assetcompass.portfolio.service.dto.BrokerAccountDTO;
import java.util.Optional;
import java.util.UUID;

/**
 * Service Interface for managing {@link com.assetcompass.portfolio.domain.BrokerAccount}.
 */
public interface BrokerAccountService {
    /**
     * Save a brokerAccount.
     *
     * @param brokerAccountDTO the entity to save.
     * @return the persisted entity.
     */
    BrokerAccountDTO save(BrokerAccountDTO brokerAccountDTO);

    /**
     * Updates a brokerAccount.
     *
     * @param brokerAccountDTO the entity to update.
     * @return the persisted entity.
     */
    BrokerAccountDTO update(BrokerAccountDTO brokerAccountDTO);

    /**
     * Partially updates a brokerAccount.
     *
     * @param brokerAccountDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<BrokerAccountDTO> partialUpdate(BrokerAccountDTO brokerAccountDTO);

    /**
     * Get the "id" brokerAccount.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<BrokerAccountDTO> findOne(UUID id);

    /**
     * Delete the "id" brokerAccount.
     *
     * @param id the id of the entity.
     */
    void delete(UUID id);
}
