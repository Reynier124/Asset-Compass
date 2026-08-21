package com.assetcompass.gateway.service.impl;

import com.assetcompass.gateway.domain.criteria.AssetRatioCriteria;
import com.assetcompass.gateway.repository.AssetRatioRepository;
import com.assetcompass.gateway.service.AssetRatioService;
import com.assetcompass.gateway.service.dto.AssetRatioDTO;
import com.assetcompass.gateway.service.mapper.AssetRatioMapper;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link com.assetcompass.gateway.domain.AssetRatio}.
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
    public Mono<AssetRatioDTO> save(AssetRatioDTO assetRatioDTO) {
        LOG.debug("Request to save AssetRatio : {}", assetRatioDTO);
        return assetRatioRepository.save(assetRatioMapper.toEntity(assetRatioDTO)).map(assetRatioMapper::toDto);
    }

    @Override
    public Mono<AssetRatioDTO> update(AssetRatioDTO assetRatioDTO) {
        LOG.debug("Request to update AssetRatio : {}", assetRatioDTO);
        return assetRatioRepository.save(assetRatioMapper.toEntity(assetRatioDTO).setIsPersisted()).map(assetRatioMapper::toDto);
    }

    @Override
    public Mono<AssetRatioDTO> partialUpdate(AssetRatioDTO assetRatioDTO) {
        LOG.debug("Request to partially update AssetRatio : {}", assetRatioDTO);

        return assetRatioRepository
            .findById(assetRatioDTO.getId())
            .map(existingAssetRatio -> {
                assetRatioMapper.partialUpdate(existingAssetRatio, assetRatioDTO);

                return existingAssetRatio;
            })
            .flatMap(assetRatioRepository::save)
            .map(assetRatioMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<AssetRatioDTO> findByCriteria(AssetRatioCriteria criteria) {
        LOG.debug("Request to get all AssetRatios by Criteria");
        return assetRatioRepository.findByCriteria(criteria, null).map(assetRatioMapper::toDto);
    }

    /**
     * Find the count of assetRatios by criteria.
     * @param criteria filtering criteria
     * @return the count of assetRatios
     */
    public Mono<Long> countByCriteria(AssetRatioCriteria criteria) {
        LOG.debug("Request to get the count of all AssetRatios by Criteria");
        return assetRatioRepository.countByCriteria(criteria);
    }

    public Mono<Long> countAll() {
        return assetRatioRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<AssetRatioDTO> findOne(UUID id) {
        LOG.debug("Request to get AssetRatio : {}", id);
        return assetRatioRepository.findById(id).map(assetRatioMapper::toDto);
    }

    @Override
    public Mono<Void> delete(UUID id) {
        LOG.debug("Request to delete AssetRatio : {}", id);
        return assetRatioRepository.deleteById(id);
    }
}
