package com.assetcompass.gateway.repository;

import com.assetcompass.gateway.domain.Position;
import com.assetcompass.gateway.domain.criteria.PositionCriteria;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Position entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PositionRepository extends ReactiveCrudRepository<Position, UUID>, PositionRepositoryInternal {
    Flux<Position> findAllBy(Pageable pageable);

    @Query("SELECT * FROM position entity WHERE entity.account_id = :id")
    Flux<Position> findByAccount(UUID id);

    @Query("SELECT * FROM position entity WHERE entity.account_id IS NULL")
    Flux<Position> findAllWhereAccountIsNull();

    @Query("SELECT * FROM position entity WHERE entity.asset_id = :id")
    Flux<Position> findByAsset(UUID id);

    @Query("SELECT * FROM position entity WHERE entity.asset_id IS NULL")
    Flux<Position> findAllWhereAssetIsNull();

    @Override
    <S extends Position> Mono<S> save(S entity);

    @Override
    Flux<Position> findAll();

    @Override
    Mono<Position> findById(UUID id);

    @Override
    Mono<Void> deleteById(UUID id);
}

interface PositionRepositoryInternal {
    <S extends Position> Mono<S> save(S entity);

    Flux<Position> findAllBy(Pageable pageable);

    Flux<Position> findAll();

    Mono<Position> findById(UUID id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Position> findAllBy(Pageable pageable, Criteria criteria);
    Flux<Position> findByCriteria(PositionCriteria criteria, Pageable pageable);

    Mono<Long> countByCriteria(PositionCriteria criteria);
}
