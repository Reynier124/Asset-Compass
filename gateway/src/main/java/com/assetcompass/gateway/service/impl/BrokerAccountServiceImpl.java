package com.assetcompass.gateway.service.impl;

import com.assetcompass.gateway.domain.criteria.BrokerAccountCriteria;
import com.assetcompass.gateway.repository.BrokerAccountRepository;
import com.assetcompass.gateway.service.BrokerAccountService;
import com.assetcompass.gateway.service.dto.BrokerAccountDTO;
import com.assetcompass.gateway.service.mapper.BrokerAccountMapper;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link com.assetcompass.gateway.domain.BrokerAccount}.
 */
@Service
@Transactional
public class BrokerAccountServiceImpl implements BrokerAccountService {

    private static final Logger LOG = LoggerFactory.getLogger(BrokerAccountServiceImpl.class);

    private final BrokerAccountRepository brokerAccountRepository;

    private final BrokerAccountMapper brokerAccountMapper;

    public BrokerAccountServiceImpl(BrokerAccountRepository brokerAccountRepository, BrokerAccountMapper brokerAccountMapper) {
        this.brokerAccountRepository = brokerAccountRepository;
        this.brokerAccountMapper = brokerAccountMapper;
    }

    @Override
    public Mono<BrokerAccountDTO> save(BrokerAccountDTO brokerAccountDTO) {
        LOG.debug("Request to save BrokerAccount : {}", brokerAccountDTO);
        return brokerAccountRepository.save(brokerAccountMapper.toEntity(brokerAccountDTO)).map(brokerAccountMapper::toDto);
    }

    @Override
    public Mono<BrokerAccountDTO> update(BrokerAccountDTO brokerAccountDTO) {
        LOG.debug("Request to update BrokerAccount : {}", brokerAccountDTO);
        return brokerAccountRepository
            .save(brokerAccountMapper.toEntity(brokerAccountDTO).setIsPersisted())
            .map(brokerAccountMapper::toDto);
    }

    @Override
    public Mono<BrokerAccountDTO> partialUpdate(BrokerAccountDTO brokerAccountDTO) {
        LOG.debug("Request to partially update BrokerAccount : {}", brokerAccountDTO);

        return brokerAccountRepository
            .findById(brokerAccountDTO.getId())
            .map(existingBrokerAccount -> {
                brokerAccountMapper.partialUpdate(existingBrokerAccount, brokerAccountDTO);

                return existingBrokerAccount;
            })
            .flatMap(brokerAccountRepository::save)
            .map(brokerAccountMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<BrokerAccountDTO> findByCriteria(BrokerAccountCriteria criteria) {
        LOG.debug("Request to get all BrokerAccounts by Criteria");
        return brokerAccountRepository.findByCriteria(criteria, null).map(brokerAccountMapper::toDto);
    }

    /**
     * Find the count of brokerAccounts by criteria.
     * @param criteria filtering criteria
     * @return the count of brokerAccounts
     */
    public Mono<Long> countByCriteria(BrokerAccountCriteria criteria) {
        LOG.debug("Request to get the count of all BrokerAccounts by Criteria");
        return brokerAccountRepository.countByCriteria(criteria);
    }

    public Mono<Long> countAll() {
        return brokerAccountRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<BrokerAccountDTO> findOne(UUID id) {
        LOG.debug("Request to get BrokerAccount : {}", id);
        return brokerAccountRepository.findById(id).map(brokerAccountMapper::toDto);
    }

    @Override
    public Mono<Void> delete(UUID id) {
        LOG.debug("Request to delete BrokerAccount : {}", id);
        return brokerAccountRepository.deleteById(id);
    }
}
