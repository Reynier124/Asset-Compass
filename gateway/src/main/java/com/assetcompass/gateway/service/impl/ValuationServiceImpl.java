package com.assetcompass.gateway.service.impl;

import com.assetcompass.gateway.domain.criteria.ValuationCriteria;
import com.assetcompass.gateway.repository.ValuationRepository;
import com.assetcompass.gateway.service.ValuationService;
import com.assetcompass.gateway.service.dto.ValuationDTO;
import com.assetcompass.gateway.service.mapper.ValuationMapper;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link com.assetcompass.gateway.domain.Valuation}.
 */
@Service
@Transactional
public class ValuationServiceImpl implements ValuationService {

    private static final Logger LOG = LoggerFactory.getLogger(ValuationServiceImpl.class);

    private final ValuationRepository valuationRepository;

    private final ValuationMapper valuationMapper;

    public ValuationServiceImpl(ValuationRepository valuationRepository, ValuationMapper valuationMapper) {
        this.valuationRepository = valuationRepository;
        this.valuationMapper = valuationMapper;
    }

    @Override
    public Mono<ValuationDTO> save(ValuationDTO valuationDTO) {
        LOG.debug("Request to save Valuation : {}", valuationDTO);
        return valuationRepository.save(valuationMapper.toEntity(valuationDTO)).map(valuationMapper::toDto);
    }

    @Override
    public Mono<ValuationDTO> update(ValuationDTO valuationDTO) {
        LOG.debug("Request to update Valuation : {}", valuationDTO);
        return valuationRepository.save(valuationMapper.toEntity(valuationDTO).setIsPersisted()).map(valuationMapper::toDto);
    }

    @Override
    public Mono<ValuationDTO> partialUpdate(ValuationDTO valuationDTO) {
        LOG.debug("Request to partially update Valuation : {}", valuationDTO);

        return valuationRepository
            .findById(valuationDTO.getId())
            .map(existingValuation -> {
                valuationMapper.partialUpdate(existingValuation, valuationDTO);

                return existingValuation;
            })
            .flatMap(valuationRepository::save)
            .map(valuationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<ValuationDTO> findByCriteria(ValuationCriteria criteria, Pageable pageable) {
        LOG.debug("Request to get all Valuations by Criteria");
        return valuationRepository.findByCriteria(criteria, pageable).map(valuationMapper::toDto);
    }

    /**
     * Find the count of valuations by criteria.
     * @param criteria filtering criteria
     * @return the count of valuations
     */
    public Mono<Long> countByCriteria(ValuationCriteria criteria) {
        LOG.debug("Request to get the count of all Valuations by Criteria");
        return valuationRepository.countByCriteria(criteria);
    }

    public Mono<Long> countAll() {
        return valuationRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<ValuationDTO> findOne(UUID id) {
        LOG.debug("Request to get Valuation : {}", id);
        return valuationRepository.findById(id).map(valuationMapper::toDto);
    }

    @Override
    public Mono<Void> delete(UUID id) {
        LOG.debug("Request to delete Valuation : {}", id);
        return valuationRepository.deleteById(id);
    }
}
