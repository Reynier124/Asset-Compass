package com.assetcompass.gateway.repository;

import com.assetcompass.gateway.domain.AssetRatio;
import com.assetcompass.gateway.domain.criteria.AssetRatioCriteria;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the AssetRatio entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AssetRatioRepository extends ReactiveCrudRepository<AssetRatio, UUID>, AssetRatioRepositoryInternal {
    @Query("SELECT * FROM asset_ratio entity WHERE entity.asset_id = :id")
    Flux<AssetRatio> findByAsset(UUID id);

    @Query("SELECT * FROM asset_ratio entity WHERE entity.asset_id IS NULL")
    Flux<AssetRatio> findAllWhereAssetIsNull();

    @Override
    <S extends AssetRatio> Mono<S> save(S entity);

    @Override
    Flux<AssetRatio> findAll();

    @Override
    Mono<AssetRatio> findById(UUID id);

    @Override
    Mono<Void> deleteById(UUID id);
}

interface AssetRatioRepositoryInternal {
    <S extends AssetRatio> Mono<S> save(S entity);

    Flux<AssetRatio> findAllBy(Pageable pageable);

    Flux<AssetRatio> findAll();

    Mono<AssetRatio> findById(UUID id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<AssetRatio> findAllBy(Pageable pageable, Criteria criteria);
    Flux<AssetRatio> findByCriteria(AssetRatioCriteria criteria, Pageable pageable);

    Mono<Long> countByCriteria(AssetRatioCriteria criteria);
}
