package com.assetcompass.gateway.service.impl;

import com.assetcompass.gateway.domain.criteria.TaxEventCriteria;
import com.assetcompass.gateway.repository.TaxEventRepository;
import com.assetcompass.gateway.service.TaxEventService;
import com.assetcompass.gateway.service.dto.TaxEventDTO;
import com.assetcompass.gateway.service.mapper.TaxEventMapper;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link com.assetcompass.gateway.domain.TaxEvent}.
 */
@Service
@Transactional
public class TaxEventServiceImpl implements TaxEventService {

    private static final Logger LOG = LoggerFactory.getLogger(TaxEventServiceImpl.class);

    private final TaxEventRepository taxEventRepository;

    private final TaxEventMapper taxEventMapper;

    public TaxEventServiceImpl(TaxEventRepository taxEventRepository, TaxEventMapper taxEventMapper) {
        this.taxEventRepository = taxEventRepository;
        this.taxEventMapper = taxEventMapper;
    }

    @Override
    public Mono<TaxEventDTO> save(TaxEventDTO taxEventDTO) {
        LOG.debug("Request to save TaxEvent : {}", taxEventDTO);
        return taxEventRepository.save(taxEventMapper.toEntity(taxEventDTO)).map(taxEventMapper::toDto);
    }

    @Override
    public Mono<TaxEventDTO> update(TaxEventDTO taxEventDTO) {
        LOG.debug("Request to update TaxEvent : {}", taxEventDTO);
        return taxEventRepository.save(taxEventMapper.toEntity(taxEventDTO).setIsPersisted()).map(taxEventMapper::toDto);
    }

    @Override
    public Mono<TaxEventDTO> partialUpdate(TaxEventDTO taxEventDTO) {
        LOG.debug("Request to partially update TaxEvent : {}", taxEventDTO);

        return taxEventRepository
            .findById(taxEventDTO.getId())
            .map(existingTaxEvent -> {
                taxEventMapper.partialUpdate(existingTaxEvent, taxEventDTO);

                return existingTaxEvent;
            })
            .flatMap(taxEventRepository::save)
            .map(taxEventMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<TaxEventDTO> findByCriteria(TaxEventCriteria criteria, Pageable pageable) {
        LOG.debug("Request to get all TaxEvents by Criteria");
        return taxEventRepository.findByCriteria(criteria, pageable).map(taxEventMapper::toDto);
    }

    /**
     * Find the count of taxEvents by criteria.
     * @param criteria filtering criteria
     * @return the count of taxEvents
     */
    public Mono<Long> countByCriteria(TaxEventCriteria criteria) {
        LOG.debug("Request to get the count of all TaxEvents by Criteria");
        return taxEventRepository.countByCriteria(criteria);
    }

    public Mono<Long> countAll() {
        return taxEventRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<TaxEventDTO> findOne(UUID id) {
        LOG.debug("Request to get TaxEvent : {}", id);
        return taxEventRepository.findById(id).map(taxEventMapper::toDto);
    }

    @Override
    public Mono<Void> delete(UUID id) {
        LOG.debug("Request to delete TaxEvent : {}", id);
        return taxEventRepository.deleteById(id);
    }
}
