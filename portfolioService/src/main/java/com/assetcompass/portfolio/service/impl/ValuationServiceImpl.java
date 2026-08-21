package com.assetcompass.portfolio.service.impl;

import com.assetcompass.portfolio.domain.Valuation;
import com.assetcompass.portfolio.repository.ValuationRepository;
import com.assetcompass.portfolio.service.ValuationService;
import com.assetcompass.portfolio.service.dto.ValuationDTO;
import com.assetcompass.portfolio.service.mapper.ValuationMapper;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.assetcompass.portfolio.domain.Valuation}.
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
    public ValuationDTO save(ValuationDTO valuationDTO) {
        LOG.debug("Request to save Valuation : {}", valuationDTO);
        Valuation valuation = valuationMapper.toEntity(valuationDTO);
        valuation = valuationRepository.save(valuation);
        return valuationMapper.toDto(valuation);
    }

    @Override
    public ValuationDTO update(ValuationDTO valuationDTO) {
        LOG.debug("Request to update Valuation : {}", valuationDTO);
        Valuation valuation = valuationMapper.toEntity(valuationDTO);
        valuation = valuationRepository.save(valuation);
        return valuationMapper.toDto(valuation);
    }

    @Override
    public Optional<ValuationDTO> partialUpdate(ValuationDTO valuationDTO) {
        LOG.debug("Request to partially update Valuation : {}", valuationDTO);

        return valuationRepository
            .findById(valuationDTO.getId())
            .map(existingValuation -> {
                valuationMapper.partialUpdate(existingValuation, valuationDTO);

                return existingValuation;
            })
            .map(valuationRepository::save)
            .map(valuationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ValuationDTO> findOne(UUID id) {
        LOG.debug("Request to get Valuation : {}", id);
        return valuationRepository.findById(id).map(valuationMapper::toDto);
    }

    @Override
    public void delete(UUID id) {
        LOG.debug("Request to delete Valuation : {}", id);
        valuationRepository.deleteById(id);
    }
}
