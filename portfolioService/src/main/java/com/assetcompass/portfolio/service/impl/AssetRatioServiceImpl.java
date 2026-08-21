package com.assetcompass.portfolio.service.impl;

import com.assetcompass.portfolio.domain.AssetRatio;
import com.assetcompass.portfolio.repository.AssetRatioRepository;
import com.assetcompass.portfolio.service.AssetRatioService;
import com.assetcompass.portfolio.service.dto.AssetRatioDTO;
import com.assetcompass.portfolio.service.mapper.AssetRatioMapper;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.assetcompass.portfolio.domain.AssetRatio}.
 */
@Service
@Transactional
public class AssetRatioServiceImpl implements AssetRatioService {

    private static final Logger LOG = LoggerFactory.getLogger(AssetRatioServiceImpl.class);

    private final AssetRatioRepository assetRatioRepository;

    private final AssetRatioMapper assetRatioMapper;

    public AssetRatioServiceImpl(AssetRatioRepository assetRatioRepository, AssetRatioMapper assetRatioMapper) {
        this.assetRatioRepository = assetRatioRepository;
        this.assetRatioMapper = assetRatioMapper;
    }

    @Override
    public AssetRatioDTO save(AssetRatioDTO assetRatioDTO) {
        LOG.debug("Request to save AssetRatio : {}", assetRatioDTO);
        AssetRatio assetRatio = assetRatioMapper.toEntity(assetRatioDTO);
        assetRatio = assetRatioRepository.save(assetRatio);
        return assetRatioMapper.toDto(assetRatio);
    }

    @Override
    public AssetRatioDTO update(AssetRatioDTO assetRatioDTO) {
        LOG.debug("Request to update AssetRatio : {}", assetRatioDTO);
        AssetRatio assetRatio = assetRatioMapper.toEntity(assetRatioDTO);
        assetRatio = assetRatioRepository.save(assetRatio);
        return assetRatioMapper.toDto(assetRatio);
    }

    @Override
    public Optional<AssetRatioDTO> partialUpdate(AssetRatioDTO assetRatioDTO) {
        LOG.debug("Request to partially update AssetRatio : {}", assetRatioDTO);

        return assetRatioRepository
            .findById(assetRatioDTO.getId())
            .map(existingAssetRatio -> {
                assetRatioMapper.partialUpdate(existingAssetRatio, assetRatioDTO);

                return existingAssetRatio;
            })
            .map(assetRatioRepository::save)
            .map(assetRatioMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AssetRatioDTO> findOne(UUID id) {
        LOG.debug("Request to get AssetRatio : {}", id);
        return assetRatioRepository.findById(id).map(assetRatioMapper::toDto);
    }

    @Override
    public void delete(UUID id) {
        LOG.debug("Request to delete AssetRatio : {}", id);
        assetRatioRepository.deleteById(id);
    }
}
