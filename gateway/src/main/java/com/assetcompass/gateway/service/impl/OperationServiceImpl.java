package com.assetcompass.gateway.service.impl;

import com.assetcompass.gateway.domain.criteria.OperationCriteria;
import com.assetcompass.gateway.repository.OperationRepository;
import com.assetcompass.gateway.service.OperationService;
import com.assetcompass.gateway.service.dto.OperationDTO;
import com.assetcompass.gateway.service.mapper.OperationMapper;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link com.assetcompass.gateway.domain.Operation}.
 */
@Service
@Transactional
public class OperationServiceImpl implements OperationService {

    private static final Logger LOG = LoggerFactory.getLogger(OperationServiceImpl.class);

    private final OperationRepository operationRepository;

    private final OperationMapper operationMapper;

    public OperationServiceImpl(OperationRepository operationRepository, OperationMapper operationMapper) {
        this.operationRepository = operationRepository;
        this.operationMapper = operationMapper;
    }

    @Override
    public Mono<OperationDTO> save(OperationDTO operationDTO) {
        LOG.debug("Request to save Operation : {}", operationDTO);
        return operationRepository.save(operationMapper.toEntity(operationDTO)).map(operationMapper::toDto);
    }

    @Override
    public Mono<OperationDTO> update(OperationDTO operationDTO) {
        LOG.debug("Request to update Operation : {}", operationDTO);
        return operationRepository.save(operationMapper.toEntity(operationDTO).setIsPersisted()).map(operationMapper::toDto);
    }

    @Override
    public Mono<OperationDTO> partialUpdate(OperationDTO operationDTO) {
        LOG.debug("Request to partially update Operation : {}", operationDTO);

        return operationRepository
            .findById(operationDTO.getId())
            .map(existingOperation -> {
                operationMapper.partialUpdate(existingOperation, operationDTO);

                return existingOperation;
            })
            .flatMap(operationRepository::save)
            .map(operationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<OperationDTO> findByCriteria(OperationCriteria criteria, Pageable pageable) {
        LOG.debug("Request to get all Operations by Criteria");
        return operationRepository.findByCriteria(criteria, pageable).map(operationMapper::toDto);
    }

    /**
     * Find the count of operations by criteria.
     * @param criteria filtering criteria
     * @return the count of operations
     */
    public Mono<Long> countByCriteria(OperationCriteria criteria) {
        LOG.debug("Request to get the count of all Operations by Criteria");
        return operationRepository.countByCriteria(criteria);
    }

    public Mono<Long> countAll() {
        return operationRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<OperationDTO> findOne(UUID id) {
        LOG.debug("Request to get Operation : {}", id);
        return operationRepository.findById(id).map(operationMapper::toDto);
    }

    @Override
    public Mono<Void> delete(UUID id) {
        LOG.debug("Request to delete Operation : {}", id);
        return operationRepository.deleteById(id);
    }
}
