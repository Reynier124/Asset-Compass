package com.assetcompass.gateway.repository;

import com.assetcompass.gateway.domain.Rebate;
import com.assetcompass.gateway.domain.criteria.RebateCriteria;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Rebate entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RebateRepository extends ReactiveCrudRepository<Rebate, UUID>, RebateRepositoryInternal {
    Flux<Rebate> findAllBy(Pageable pageable);

    @Query("SELECT * FROM rebate entity WHERE entity.account_id = :id")
    Flux<Rebate> findByAccount(UUID id);

    @Query("SELECT * FROM rebate entity WHERE entity.account_id IS NULL")
    Flux<Rebate> findAllWhereAccountIsNull();

    @Query("SELECT * FROM rebate entity WHERE entity.operation_id = :id")
    Flux<Rebate> findByOperation(UUID id);

    @Query("SELECT * FROM rebate entity WHERE entity.operation_id IS NULL")
    Flux<Rebate> findAllWhereOperationIsNull();

    @Override
    <S extends Rebate> Mono<S> save(S entity);

    @Override
    Flux<Rebate> findAll();

    @Override
    Mono<Rebate> findById(UUID id);

    @Override
    Mono<Void> deleteById(UUID id);
}

interface RebateRepositoryInternal {
    <S extends Rebate> Mono<S> save(S entity);

    Flux<Rebate> findAllBy(Pageable pageable);

    Flux<Rebate> findAll();

    Mono<Rebate> findById(UUID id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Rebate> findAllBy(Pageable pageable, Criteria criteria);
    Flux<Rebate> findByCriteria(RebateCriteria criteria, Pageable pageable);

    Mono<Long> countByCriteria(RebateCriteria criteria);
}
