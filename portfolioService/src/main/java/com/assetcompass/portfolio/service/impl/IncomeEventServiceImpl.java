package com.assetcompass.portfolio.service.impl;

import com.assetcompass.portfolio.domain.IncomeEvent;
import com.assetcompass.portfolio.repository.IncomeEventRepository;
import com.assetcompass.portfolio.service.IncomeEventService;
import com.assetcompass.portfolio.service.dto.IncomeEventDTO;
import com.assetcompass.portfolio.service.mapper.IncomeEventMapper;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.assetcompass.portfolio.domain.IncomeEvent}.
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
    public IncomeEventDTO save(IncomeEventDTO incomeEventDTO) {
        LOG.debug("Request to save IncomeEvent : {}", incomeEventDTO);
        IncomeEvent incomeEvent = incomeEventMapper.toEntity(incomeEventDTO);
        incomeEvent = incomeEventRepository.save(incomeEvent);
        return incomeEventMapper.toDto(incomeEvent);
    }

    @Override
    public IncomeEventDTO update(IncomeEventDTO incomeEventDTO) {
        LOG.debug("Request to update IncomeEvent : {}", incomeEventDTO);
        IncomeEvent incomeEvent = incomeEventMapper.toEntity(incomeEventDTO);
        incomeEvent = incomeEventRepository.save(incomeEvent);
        return incomeEventMapper.toDto(incomeEvent);
    }

    @Override
    public Optional<IncomeEventDTO> partialUpdate(IncomeEventDTO incomeEventDTO) {
        LOG.debug("Request to partially update IncomeEvent : {}", incomeEventDTO);

        return incomeEventRepository
            .findById(incomeEventDTO.getId())
            .map(existingIncomeEvent -> {
                incomeEventMapper.partialUpdate(existingIncomeEvent, incomeEventDTO);

                return existingIncomeEvent;
            })
            .map(incomeEventRepository::save)
            .map(incomeEventMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IncomeEventDTO> findOne(UUID id) {
        LOG.debug("Request to get IncomeEvent : {}", id);
        return incomeEventRepository.findById(id).map(incomeEventMapper::toDto);
    }

    @Override
    public void delete(UUID id) {
        LOG.debug("Request to delete IncomeEvent : {}", id);
        incomeEventRepository.deleteById(id);
    }
}
