package com.assetcompass.gateway.repository;

import com.assetcompass.gateway.domain.IncomeEvent;
import com.assetcompass.gateway.domain.criteria.IncomeEventCriteria;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the IncomeEvent entity.
 */
@SuppressWarnings("unused")
@Repository
public interface IncomeEventRepository extends ReactiveCrudRepository<IncomeEvent, UUID>, IncomeEventRepositoryInternal {
    Flux<IncomeEvent> findAllBy(Pageable pageable);

    @Query("SELECT * FROM income_event entity WHERE entity.account_id = :id")
    Flux<IncomeEvent> findByAccount(UUID id);

    @Query("SELECT * FROM income_event entity WHERE entity.account_id IS NULL")
    Flux<IncomeEvent> findAllWhereAccountIsNull();

    @Query("SELECT * FROM income_event entity WHERE entity.asset_id = :id")
    Flux<IncomeEvent> findByAsset(UUID id);

    @Query("SELECT * FROM income_event entity WHERE entity.asset_id IS NULL")
    Flux<IncomeEvent> findAllWhereAssetIsNull();

    @Override
    <S extends IncomeEvent> Mono<S> save(S entity);

    @Override
    Flux<IncomeEvent> findAll();

    @Override
    Mono<IncomeEvent> findById(UUID id);

    @Override
    Mono<Void> deleteById(UUID id);
}

interface IncomeEventRepositoryInternal {
    <S extends IncomeEvent> Mono<S> save(S entity);

    Flux<IncomeEvent> findAllBy(Pageable pageable);

    Flux<IncomeEvent> findAll();

    Mono<IncomeEvent> findById(UUID id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<IncomeEvent> findAllBy(Pageable pageable, Criteria criteria);
    Flux<IncomeEvent> findByCriteria(IncomeEventCriteria criteria, Pageable pageable);

    Mono<Long> countByCriteria(IncomeEventCriteria criteria);
}
