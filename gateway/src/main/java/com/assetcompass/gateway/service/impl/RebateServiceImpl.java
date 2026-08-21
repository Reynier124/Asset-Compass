package com.assetcompass.gateway.service.impl;

import com.assetcompass.gateway.domain.criteria.RebateCriteria;
import com.assetcompass.gateway.repository.RebateRepository;
import com.assetcompass.gateway.service.RebateService;
import com.assetcompass.gateway.service.dto.RebateDTO;
import com.assetcompass.gateway.service.mapper.RebateMapper;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link com.assetcompass.gateway.domain.Rebate}.
 */
@Service
@Transactional
public class RebateServiceImpl implements RebateService {

    private static final Logger LOG = LoggerFactory.getLogger(RebateServiceImpl.class);

    private final RebateRepository rebateRepository;

    private final RebateMapper rebateMapper;

    public RebateServiceImpl(RebateRepository rebateRepository, RebateMapper rebateMapper) {
        this.rebateRepository = rebateRepository;
        this.rebateMapper = rebateMapper;
    }

    @Override
    public Mono<RebateDTO> save(RebateDTO rebateDTO) {
        LOG.debug("Request to save Rebate : {}", rebateDTO);
        return rebateRepository.save(rebateMapper.toEntity(rebateDTO)).map(rebateMapper::toDto);
    }

    @Override
    public Mono<RebateDTO> update(RebateDTO rebateDTO) {
        LOG.debug("Request to update Rebate : {}", rebateDTO);
        return rebateRepository.save(rebateMapper.toEntity(rebateDTO).setIsPersisted()).map(rebateMapper::toDto);
    }

    @Override
    public Mono<RebateDTO> partialUpdate(RebateDTO rebateDTO) {
        LOG.debug("Request to partially update Rebate : {}", rebateDTO);

        return rebateRepository
            .findById(rebateDTO.getId())
            .map(existingRebate -> {
                rebateMapper.partialUpdate(existingRebate, rebateDTO);

                return existingRebate;
            })
            .flatMap(rebateRepository::save)
            .map(rebateMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<RebateDTO> findByCriteria(RebateCriteria criteria, Pageable pageable) {
        LOG.debug("Request to get all Rebates by Criteria");
        return rebateRepository.findByCriteria(criteria, pageable).map(rebateMapper::toDto);
    }

    /**
     * Find the count of rebates by criteria.
     * @param criteria filtering criteria
     * @return the count of rebates
     */
    public Mono<Long> countByCriteria(RebateCriteria criteria) {
        LOG.debug("Request to get the count of all Rebates by Criteria");
        return rebateRepository.countByCriteria(criteria);
    }

    public Mono<Long> countAll() {
        return rebateRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<RebateDTO> findOne(UUID id) {
        LOG.debug("Request to get Rebate : {}", id);
        return rebateRepository.findById(id).map(rebateMapper::toDto);
    }

    @Override
    public Mono<Void> delete(UUID id) {
        LOG.debug("Request to delete Rebate : {}", id);
        return rebateRepository.deleteById(id);
    }
}
