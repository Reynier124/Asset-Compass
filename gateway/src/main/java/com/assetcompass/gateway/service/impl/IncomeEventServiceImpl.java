package com.assetcompass.gateway.service.impl;

import com.assetcompass.gateway.domain.criteria.IncomeEventCriteria;
import com.assetcompass.gateway.repository.IncomeEventRepository;
import com.assetcompass.gateway.service.IncomeEventService;
import com.assetcompass.gateway.service.dto.IncomeEventDTO;
import com.assetcompass.gateway.service.mapper.IncomeEventMapper;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link com.assetcompass.gateway.domain.IncomeEvent}.
 */
@Service
@Transactional
public class IncomeEventServiceImpl implements IncomeEventService {

    private static final Logger LOG = LoggerFactory.getLogger(IncomeEventServiceImpl.class);

    private final IncomeEventRepository incomeEventRepository;

    private final IncomeEventMapper incomeEventMapper;

    public IncomeEventServiceImpl(IncomeEventRepository incomeEventRepository, IncomeEventMapper incomeEventMapper) {
        this.incomeEventRepository = incomeEventRepository;
        this.incomeEventMapper = incomeEventMapper;
    }

    @Override
    public Mono<IncomeEventDTO> save(IncomeEventDTO incomeEventDTO) {
        LOG.debug("Request to save IncomeEvent : {}", incomeEventDTO);
        return incomeEventRepository.save(incomeEventMapper.toEntity(incomeEventDTO)).map(incomeEventMapper::toDto);
    }

    @Override
    public Mono<IncomeEventDTO> update(IncomeEventDTO incomeEventDTO) {
        LOG.debug("Request to update IncomeEvent : {}", incomeEventDTO);
        return incomeEventRepository.save(incomeEventMapper.toEntity(incomeEventDTO).setIsPersisted()).map(incomeEventMapper::toDto);
    }

    @Override
    public Mono<IncomeEventDTO> partialUpdate(IncomeEventDTO incomeEventDTO) {
        LOG.debug("Request to partially update IncomeEvent : {}", incomeEventDTO);

        return incomeEventRepository
            .findById(incomeEventDTO.getId())
            .map(existingIncomeEvent -> {
                incomeEventMapper.partialUpdate(existingIncomeEvent, incomeEventDTO);

                return existingIncomeEvent;
            })
            .flatMap(incomeEventRepository::save)
            .map(incomeEventMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<IncomeEventDTO> findByCriteria(IncomeEventCriteria criteria, Pageable pageable) {
        LOG.debug("Request to get all IncomeEvents by Criteria");
        return incomeEventRepository.findByCriteria(criteria, pageable).map(incomeEventMapper::toDto);
    }

    /**
     * Find the count of incomeEvents by criteria.
     * @param criteria filtering criteria
     * @return the count of incomeEvents
     */
    public Mono<Long> countByCriteria(IncomeEventCriteria criteria) {
        LOG.debug("Request to get the count of all IncomeEvents by Criteria");
        return incomeEventRepository.countByCriteria(criteria);
    }

    public Mono<Long> countAll() {
        return incomeEventRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<IncomeEventDTO> findOne(UUID id) {
        LOG.debug("Request to get IncomeEvent : {}", id);
        return incomeEventRepository.findById(id).map(incomeEventMapper::toDto);
    }

    @Override
    public Mono<Void> delete(UUID id) {
        LOG.debug("Request to delete IncomeEvent : {}", id);
        return incomeEventRepository.deleteById(id);
    }
}
