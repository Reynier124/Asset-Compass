package com.assetcompass.gateway.service.impl;

import com.assetcompass.gateway.domain.criteria.AssetCriteria;
import com.assetcompass.gateway.repository.AssetRepository;
import com.assetcompass.gateway.service.AssetService;
import com.assetcompass.gateway.service.dto.AssetDTO;
import com.assetcompass.gateway.service.mapper.AssetMapper;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link com.assetcompass.gateway.domain.Asset}.
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
    public Mono<AssetDTO> save(AssetDTO assetDTO) {
        LOG.debug("Request to save Asset : {}", assetDTO);
        return assetRepository.save(assetMapper.toEntity(assetDTO)).map(assetMapper::toDto);
    }

    @Override
    public Mono<AssetDTO> update(AssetDTO assetDTO) {
        LOG.debug("Request to update Asset : {}", assetDTO);
        return assetRepository.save(assetMapper.toEntity(assetDTO).setIsPersisted()).map(assetMapper::toDto);
    }

    @Override
    public Mono<AssetDTO> partialUpdate(AssetDTO assetDTO) {
        LOG.debug("Request to partially update Asset : {}", assetDTO);

        return assetRepository
            .findById(assetDTO.getId())
            .map(existingAsset -> {
                assetMapper.partialUpdate(existingAsset, assetDTO);

                return existingAsset;
            })
            .flatMap(assetRepository::save)
            .map(assetMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<AssetDTO> findByCriteria(AssetCriteria criteria) {
        LOG.debug("Request to get all Assets by Criteria");
        return assetRepository.findByCriteria(criteria, null).map(assetMapper::toDto);
    }

    /**
     * Find the count of assets by criteria.
     * @param criteria filtering criteria
     * @return the count of assets
     */
    public Mono<Long> countByCriteria(AssetCriteria criteria) {
        LOG.debug("Request to get the count of all Assets by Criteria");
        return assetRepository.countByCriteria(criteria);
    }

    public Mono<Long> countAll() {
        return assetRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<AssetDTO> findOne(UUID id) {
        LOG.debug("Request to get Asset : {}", id);
        return assetRepository.findById(id).map(assetMapper::toDto);
    }

    @Override
    public Mono<Void> delete(UUID id) {
        LOG.debug("Request to delete Asset : {}", id);
        return assetRepository.deleteById(id);
    }
}
