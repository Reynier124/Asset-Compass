package com.assetcompass.gateway.repository;

import com.assetcompass.gateway.domain.Operation;
import com.assetcompass.gateway.domain.criteria.OperationCriteria;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Operation entity.
 */
@SuppressWarnings("unused")
@Repository
public interface OperationRepository extends ReactiveCrudRepository<Operation, UUID>, OperationRepositoryInternal {
    Flux<Operation> findAllBy(Pageable pageable);

    @Query("SELECT * FROM operation entity WHERE entity.account_id = :id")
    Flux<Operation> findByAccount(UUID id);

    @Query("SELECT * FROM operation entity WHERE entity.account_id IS NULL")
    Flux<Operation> findAllWhereAccountIsNull();

    @Query("SELECT * FROM operation entity WHERE entity.asset_id = :id")
    Flux<Operation> findByAsset(UUID id);

    @Query("SELECT * FROM operation entity WHERE entity.asset_id IS NULL")
    Flux<Operation> findAllWhereAssetIsNull();

    @Query("SELECT * FROM operation entity WHERE entity.closes_operation_id = :id")
    Flux<Operation> findByClosesOperation(UUID id);

    @Query("SELECT * FROM operation entity WHERE entity.closes_operation_id IS NULL")
    Flux<Operation> findAllWhereClosesOperationIsNull();

    @Override
    <S extends Operation> Mono<S> save(S entity);

    @Override
    Flux<Operation> findAll();

    @Override
    Mono<Operation> findById(UUID id);

    @Override
    Mono<Void> deleteById(UUID id);
}

interface OperationRepositoryInternal {
    <S extends Operation> Mono<S> save(S entity);

    Flux<Operation> findAllBy(Pageable pageable);

    Flux<Operation> findAll();

    Mono<Operation> findById(UUID id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Operation> findAllBy(Pageable pageable, Criteria criteria);
    Flux<Operation> findByCriteria(OperationCriteria criteria, Pageable pageable);

    Mono<Long> countByCriteria(OperationCriteria criteria);
}
