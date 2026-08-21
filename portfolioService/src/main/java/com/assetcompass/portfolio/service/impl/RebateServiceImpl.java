package com.assetcompass.portfolio.service.impl;

import com.assetcompass.portfolio.domain.Rebate;
import com.assetcompass.portfolio.repository.RebateRepository;
import com.assetcompass.portfolio.service.RebateService;
import com.assetcompass.portfolio.service.dto.RebateDTO;
import com.assetcompass.portfolio.service.mapper.RebateMapper;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.assetcompass.portfolio.domain.Rebate}.
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
    public RebateDTO save(RebateDTO rebateDTO) {
        LOG.debug("Request to save Rebate : {}", rebateDTO);
        Rebate rebate = rebateMapper.toEntity(rebateDTO);
        rebate = rebateRepository.save(rebate);
        return rebateMapper.toDto(rebate);
    }

    @Override
    public RebateDTO update(RebateDTO rebateDTO) {
        LOG.debug("Request to update Rebate : {}", rebateDTO);
        Rebate rebate = rebateMapper.toEntity(rebateDTO);
        rebate = rebateRepository.save(rebate);
        return rebateMapper.toDto(rebate);
    }

    @Override
    public Optional<RebateDTO> partialUpdate(RebateDTO rebateDTO) {
        LOG.debug("Request to partially update Rebate : {}", rebateDTO);

        return rebateRepository
            .findById(rebateDTO.getId())
            .map(existingRebate -> {
                rebateMapper.partialUpdate(existingRebate, rebateDTO);

                return existingRebate;
            })
            .map(rebateRepository::save)
            .map(rebateMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RebateDTO> findOne(UUID id) {
        LOG.debug("Request to get Rebate : {}", id);
        return rebateRepository.findById(id).map(rebateMapper::toDto);
    }

    @Override
    public void delete(UUID id) {
        LOG.debug("Request to delete Rebate : {}", id);
        rebateRepository.deleteById(id);
    }
}
