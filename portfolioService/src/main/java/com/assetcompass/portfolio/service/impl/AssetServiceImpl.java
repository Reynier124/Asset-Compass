package com.assetcompass.portfolio.service.impl;

import com.assetcompass.portfolio.domain.Asset;
import com.assetcompass.portfolio.repository.AssetRepository;
import com.assetcompass.portfolio.service.AssetService;
import com.assetcompass.portfolio.service.dto.AssetDTO;
import com.assetcompass.portfolio.service.mapper.AssetMapper;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.assetcompass.portfolio.domain.Asset}.
 */
@Service
@Transactional
public class AssetServiceImpl implements AssetService {

    private static final Logger LOG = LoggerFactory.getLogger(AssetServiceImpl.class);

    private final AssetRepository assetRepository;

    private final AssetMapper assetMapper;

    public AssetServiceImpl(AssetRepository assetRepository, AssetMapper assetMapper) {
        this.assetRepository = assetRepository;
        this.assetMapper = assetMapper;
    }

    @Override
    public AssetDTO save(AssetDTO assetDTO) {
        LOG.debug("Request to save Asset : {}", assetDTO);
        Asset asset = assetMapper.toEntity(assetDTO);
        asset = assetRepository.save(asset);
        return assetMapper.toDto(asset);
    }

    @Override
    public AssetDTO update(AssetDTO assetDTO) {
        LOG.debug("Request to update Asset : {}", assetDTO);
        Asset asset = assetMapper.toEntity(assetDTO);
        asset = assetRepository.save(asset);
        return assetMapper.toDto(asset);
    }

    @Override
    public Optional<AssetDTO> partialUpdate(AssetDTO assetDTO) {
        LOG.debug("Request to partially update Asset : {}", assetDTO);

        return assetRepository
            .findById(assetDTO.getId())
            .map(existingAsset -> {
                assetMapper.partialUpdate(existingAsset, assetDTO);

                return existingAsset;
            })
            .map(assetRepository::save)
            .map(assetMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AssetDTO> findOne(UUID id) {
        LOG.debug("Request to get Asset : {}", id);
        return assetRepository.findById(id).map(assetMapper::toDto);
    }

    @Override
    public void delete(UUID id) {
        LOG.debug("Request to delete Asset : {}", id);
        assetRepository.deleteById(id);
    }
}
