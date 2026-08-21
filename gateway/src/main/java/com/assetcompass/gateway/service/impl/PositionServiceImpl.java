package com.assetcompass.gateway.service.impl;

import com.assetcompass.gateway.domain.criteria.PositionCriteria;
import com.assetcompass.gateway.repository.PositionRepository;
import com.assetcompass.gateway.service.PositionService;
import com.assetcompass.gateway.service.dto.PositionDTO;
import com.assetcompass.gateway.service.mapper.PositionMapper;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link com.assetcompass.gateway.domain.Position}.
 */
@Service
@Transactional
public class PositionServiceImpl implements PositionService {

    private static final Logger LOG = LoggerFactory.getLogger(PositionServiceImpl.class);

    private final PositionRepository positionRepository;

    private final PositionMapper positionMapper;

    public PositionServiceImpl(PositionRepository positionRepository, PositionMapper positionMapper) {
        this.positionRepository = positionRepository;
        this.positionMapper = positionMapper;
    }

    @Override
    public Mono<PositionDTO> save(PositionDTO positionDTO) {
        LOG.debug("Request to save Position : {}", positionDTO);
        return positionRepository.save(positionMapper.toEntity(positionDTO)).map(positionMapper::toDto);
    }

    @Override
    public Mono<PositionDTO> update(PositionDTO positionDTO) {
        LOG.debug("Request to update Position : {}", positionDTO);
        return positionRepository.save(positionMapper.toEntity(positionDTO).setIsPersisted()).map(positionMapper::toDto);
    }

    @Override
    public Mono<PositionDTO> partialUpdate(PositionDTO positionDTO) {
        LOG.debug("Request to partially update Position : {}", positionDTO);

        return positionRepository
            .findById(positionDTO.getId())
            .map(existingPosition -> {
                positionMapper.partialUpdate(existingPosition, positionDTO);

                return existingPosition;
            })
            .flatMap(positionRepository::save)
            .map(positionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<PositionDTO> findByCriteria(PositionCriteria criteria, Pageable pageable) {
        LOG.debug("Request to get all Positions by Criteria");
        return positionRepository.findByCriteria(criteria, pageable).map(positionMapper::toDto);
    }

    /**
     * Find the count of positions by criteria.
     * @param criteria filtering criteria
     * @return the count of positions
     */
    public Mono<Long> countByCriteria(PositionCriteria criteria) {
        LOG.debug("Request to get the count of all Positions by Criteria");
        return positionRepository.countByCriteria(criteria);
    }

    public Mono<Long> countAll() {
        return positionRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<PositionDTO> findOne(UUID id) {
        LOG.debug("Request to get Position : {}", id);
        return positionRepository.findById(id).map(positionMapper::toDto);
    }

    @Override
    public Mono<Void> delete(UUID id) {
        LOG.debug("Request to delete Position : {}", id);
        return positionRepository.deleteById(id);
    }
}
