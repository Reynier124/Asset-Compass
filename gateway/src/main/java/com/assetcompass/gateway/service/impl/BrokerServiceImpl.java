package com.assetcompass.gateway.service.impl;

import com.assetcompass.gateway.domain.criteria.BrokerCriteria;
import com.assetcompass.gateway.repository.BrokerRepository;
import com.assetcompass.gateway.service.BrokerService;
import com.assetcompass.gateway.service.dto.BrokerDTO;
import com.assetcompass.gateway.service.mapper.BrokerMapper;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link com.assetcompass.gateway.domain.Broker}.
 */
@Service
@Transactional
public class BrokerServiceImpl implements BrokerService {

    private static final Logger LOG = LoggerFactory.getLogger(BrokerServiceImpl.class);

    private final BrokerRepository brokerRepository;

    private final BrokerMapper brokerMapper;

    public BrokerServiceImpl(BrokerRepository brokerRepository, BrokerMapper brokerMapper) {
        this.brokerRepository = brokerRepository;
        this.brokerMapper = brokerMapper;
    }

    @Override
    public Mono<BrokerDTO> save(BrokerDTO brokerDTO) {
        LOG.debug("Request to save Broker : {}", brokerDTO);
        return brokerRepository.save(brokerMapper.toEntity(brokerDTO)).map(brokerMapper::toDto);
    }

    @Override
    public Mono<BrokerDTO> update(BrokerDTO brokerDTO) {
        LOG.debug("Request to update Broker : {}", brokerDTO);
        return brokerRepository.save(brokerMapper.toEntity(brokerDTO).setIsPersisted()).map(brokerMapper::toDto);
    }

    @Override
    public Mono<BrokerDTO> partialUpdate(BrokerDTO brokerDTO) {
        LOG.debug("Request to partially update Broker : {}", brokerDTO);

        return brokerRepository
            .findById(brokerDTO.getId())
            .map(existingBroker -> {
                brokerMapper.partialUpdate(existingBroker, brokerDTO);

                return existingBroker;
            })
            .flatMap(brokerRepository::save)
            .map(brokerMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<BrokerDTO> findByCriteria(BrokerCriteria criteria) {
        LOG.debug("Request to get all Brokers by Criteria");
        return brokerRepository.findByCriteria(criteria, null).map(brokerMapper::toDto);
    }

    /**
     * Find the count of brokers by criteria.
     * @param criteria filtering criteria
     * @return the count of brokers
     */
    public Mono<Long> countByCriteria(BrokerCriteria criteria) {
        LOG.debug("Request to get the count of all Brokers by Criteria");
        return brokerRepository.countByCriteria(criteria);
    }

    public Mono<Long> countAll() {
        return brokerRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<BrokerDTO> findOne(UUID id) {
        LOG.debug("Request to get Broker : {}", id);
        return brokerRepository.findById(id).map(brokerMapper::toDto);
    }

    @Override
    public Mono<Void> delete(UUID id) {
        LOG.debug("Request to delete Broker : {}", id);
        return brokerRepository.deleteById(id);
    }
}
