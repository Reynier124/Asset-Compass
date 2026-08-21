package com.assetcompass.gateway.repository;

import com.assetcompass.gateway.domain.TaxEvent;
import com.assetcompass.gateway.domain.criteria.TaxEventCriteria;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the TaxEvent entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TaxEventRepository extends ReactiveCrudRepository<TaxEvent, UUID>, TaxEventRepositoryInternal {
    Flux<TaxEvent> findAllBy(Pageable pageable);

    @Query("SELECT * FROM tax_event entity WHERE entity.account_id = :id")
    Flux<TaxEvent> findByAccount(UUID id);

    @Query("SELECT * FROM tax_event entity WHERE entity.account_id IS NULL")
    Flux<TaxEvent> findAllWhereAccountIsNull();

    @Query("SELECT * FROM tax_event entity WHERE entity.operation_id = :id")
    Flux<TaxEvent> findByOperation(UUID id);

    @Query("SELECT * FROM tax_event entity WHERE entity.operation_id IS NULL")
    Flux<TaxEvent> findAllWhereOperationIsNull();

    @Query("SELECT * FROM tax_event entity WHERE entity.income_event_id = :id")
    Flux<TaxEvent> findByIncomeEvent(UUID id);

    @Query("SELECT * FROM tax_event entity WHERE entity.income_event_id IS NULL")
    Flux<TaxEvent> findAllWhereIncomeEventIsNull();

    @Override
    <S extends TaxEvent> Mono<S> save(S entity);

    @Override
    Flux<TaxEvent> findAll();

    @Override
    Mono<TaxEvent> findById(UUID id);

    @Override
    Mono<Void> deleteById(UUID id);
}

interface TaxEventRepositoryInternal {
    <S extends TaxEvent> Mono<S> save(S entity);

    Flux<TaxEvent> findAllBy(Pageable pageable);

    Flux<TaxEvent> findAll();

    Mono<TaxEvent> findById(UUID id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<TaxEvent> findAllBy(Pageable pageable, Criteria criteria);
    Flux<TaxEvent> findByCriteria(TaxEventCriteria criteria, Pageable pageable);

    Mono<Long> countByCriteria(TaxEventCriteria criteria);
}
