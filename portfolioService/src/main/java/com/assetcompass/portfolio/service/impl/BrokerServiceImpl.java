package com.assetcompass.portfolio.service.impl;

import com.assetcompass.portfolio.domain.Broker;
import com.assetcompass.portfolio.repository.BrokerRepository;
import com.assetcompass.portfolio.service.BrokerService;
import com.assetcompass.portfolio.service.dto.BrokerDTO;
import com.assetcompass.portfolio.service.mapper.BrokerMapper;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.assetcompass.portfolio.domain.Broker}.
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
    public BrokerDTO save(BrokerDTO brokerDTO) {
        LOG.debug("Request to save Broker : {}", brokerDTO);
        Broker broker = brokerMapper.toEntity(brokerDTO);
        broker = brokerRepository.save(broker);
        return brokerMapper.toDto(broker);
    }

    @Override
    public BrokerDTO update(BrokerDTO brokerDTO) {
        LOG.debug("Request to update Broker : {}", brokerDTO);
        Broker broker = brokerMapper.toEntity(brokerDTO);
        broker = brokerRepository.save(broker);
        return brokerMapper.toDto(broker);
    }

    @Override
    public Optional<BrokerDTO> partialUpdate(BrokerDTO brokerDTO) {
        LOG.debug("Request to partially update Broker : {}", brokerDTO);

        return brokerRepository
            .findById(brokerDTO.getId())
            .map(existingBroker -> {
                brokerMapper.partialUpdate(existingBroker, brokerDTO);

                return existingBroker;
            })
            .map(brokerRepository::save)
            .map(brokerMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BrokerDTO> findOne(UUID id) {
        LOG.debug("Request to get Broker : {}", id);
        return brokerRepository.findById(id).map(brokerMapper::toDto);
    }

    @Override
    public void delete(UUID id) {
        LOG.debug("Request to delete Broker : {}", id);
        brokerRepository.deleteById(id);
    }
}
