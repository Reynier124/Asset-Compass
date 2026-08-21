package com.assetcompass.portfolio.service.impl;

import com.assetcompass.portfolio.domain.TaxEvent;
import com.assetcompass.portfolio.repository.TaxEventRepository;
import com.assetcompass.portfolio.service.TaxEventService;
import com.assetcompass.portfolio.service.dto.TaxEventDTO;
import com.assetcompass.portfolio.service.mapper.TaxEventMapper;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.assetcompass.portfolio.domain.TaxEvent}.
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
    public TaxEventDTO save(TaxEventDTO taxEventDTO) {
        LOG.debug("Request to save TaxEvent : {}", taxEventDTO);
        TaxEvent taxEvent = taxEventMapper.toEntity(taxEventDTO);
        taxEvent = taxEventRepository.save(taxEvent);
        return taxEventMapper.toDto(taxEvent);
    }

    @Override
    public TaxEventDTO update(TaxEventDTO taxEventDTO) {
        LOG.debug("Request to update TaxEvent : {}", taxEventDTO);
        TaxEvent taxEvent = taxEventMapper.toEntity(taxEventDTO);
        taxEvent = taxEventRepository.save(taxEvent);
        return taxEventMapper.toDto(taxEvent);
    }

    @Override
    public Optional<TaxEventDTO> partialUpdate(TaxEventDTO taxEventDTO) {
        LOG.debug("Request to partially update TaxEvent : {}", taxEventDTO);

        return taxEventRepository
            .findById(taxEventDTO.getId())
            .map(existingTaxEvent -> {
                taxEventMapper.partialUpdate(existingTaxEvent, taxEventDTO);

                return existingTaxEvent;
            })
            .map(taxEventRepository::save)
            .map(taxEventMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TaxEventDTO> findOne(UUID id) {
        LOG.debug("Request to get TaxEvent : {}", id);
        return taxEventRepository.findById(id).map(taxEventMapper::toDto);
    }

    @Override
    public void delete(UUID id) {
        LOG.debug("Request to delete TaxEvent : {}", id);
        taxEventRepository.deleteById(id);
    }
}
