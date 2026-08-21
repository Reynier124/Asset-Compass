package com.assetcompass.portfolio.service.impl;

import com.assetcompass.portfolio.domain.BrokerAccount;
import com.assetcompass.portfolio.repository.BrokerAccountRepository;
import com.assetcompass.portfolio.service.BrokerAccountService;
import com.assetcompass.portfolio.service.dto.BrokerAccountDTO;
import com.assetcompass.portfolio.service.mapper.BrokerAccountMapper;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.assetcompass.portfolio.domain.BrokerAccount}.
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
    public BrokerAccountDTO save(BrokerAccountDTO brokerAccountDTO) {
        LOG.debug("Request to save BrokerAccount : {}", brokerAccountDTO);
        BrokerAccount brokerAccount = brokerAccountMapper.toEntity(brokerAccountDTO);
        brokerAccount = brokerAccountRepository.save(brokerAccount);
        return brokerAccountMapper.toDto(brokerAccount);
    }

    @Override
    public BrokerAccountDTO update(BrokerAccountDTO brokerAccountDTO) {
        LOG.debug("Request to update BrokerAccount : {}", brokerAccountDTO);
        BrokerAccount brokerAccount = brokerAccountMapper.toEntity(brokerAccountDTO);
        brokerAccount = brokerAccountRepository.save(brokerAccount);
        return brokerAccountMapper.toDto(brokerAccount);
    }

    @Override
    public Optional<BrokerAccountDTO> partialUpdate(BrokerAccountDTO brokerAccountDTO) {
        LOG.debug("Request to partially update BrokerAccount : {}", brokerAccountDTO);

        return brokerAccountRepository
            .findById(brokerAccountDTO.getId())
            .map(existingBrokerAccount -> {
                brokerAccountMapper.partialUpdate(existingBrokerAccount, brokerAccountDTO);

                return existingBrokerAccount;
            })
            .map(brokerAccountRepository::save)
            .map(brokerAccountMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BrokerAccountDTO> findOne(UUID id) {
        LOG.debug("Request to get BrokerAccount : {}", id);
        return brokerAccountRepository.findById(id).map(brokerAccountMapper::toDto);
    }

    @Override
    public void delete(UUID id) {
        LOG.debug("Request to delete BrokerAccount : {}", id);
        brokerAccountRepository.deleteById(id);
    }
}
