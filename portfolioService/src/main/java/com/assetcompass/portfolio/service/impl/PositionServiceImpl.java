package com.assetcompass.portfolio.service.impl;

import com.assetcompass.portfolio.domain.Position;
import com.assetcompass.portfolio.repository.PositionRepository;
import com.assetcompass.portfolio.service.PositionService;
import com.assetcompass.portfolio.service.dto.PositionDTO;
import com.assetcompass.portfolio.service.mapper.PositionMapper;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.assetcompass.portfolio.domain.Position}.
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
    public PositionDTO save(PositionDTO positionDTO) {
        LOG.debug("Request to save Position : {}", positionDTO);
        Position position = positionMapper.toEntity(positionDTO);
        position = positionRepository.save(position);
        return positionMapper.toDto(position);
    }

    @Override
    public PositionDTO update(PositionDTO positionDTO) {
        LOG.debug("Request to update Position : {}", positionDTO);
        Position position = positionMapper.toEntity(positionDTO);
        position = positionRepository.save(position);
        return positionMapper.toDto(position);
    }

    @Override
    public Optional<PositionDTO> partialUpdate(PositionDTO positionDTO) {
        LOG.debug("Request to partially update Position : {}", positionDTO);

        return positionRepository
            .findById(positionDTO.getId())
            .map(existingPosition -> {
                positionMapper.partialUpdate(existingPosition, positionDTO);

                return existingPosition;
            })
            .map(positionRepository::save)
            .map(positionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PositionDTO> findOne(UUID id) {
        LOG.debug("Request to get Position : {}", id);
        return positionRepository.findById(id).map(positionMapper::toDto);
    }

    @Override
    public void delete(UUID id) {
        LOG.debug("Request to delete Position : {}", id);
        positionRepository.deleteById(id);
    }
}
