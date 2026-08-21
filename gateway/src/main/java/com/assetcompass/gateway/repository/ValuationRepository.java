package com.assetcompass.gateway.repository;

import com.assetcompass.gateway.domain.Valuation;
import com.assetcompass.gateway.domain.criteria.ValuationCriteria;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Valuation entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ValuationRepository extends ReactiveCrudRepository<Valuation, UUID>, ValuationRepositoryInternal {
    Flux<Valuation> findAllBy(Pageable pageable);

    @Query("SELECT * FROM valuation entity WHERE entity.account_id = :id")
    Flux<Valuation> findByAccount(UUID id);

    @Query("SELECT * FROM valuation entity WHERE entity.account_id IS NULL")
    Flux<Valuation> findAllWhereAccountIsNull();

    @Override
    <S extends Valuation> Mono<S> save(S entity);

    @Override
    Flux<Valuation> findAll();

    @Override
    Mono<Valuation> findById(UUID id);

    @Override
    Mono<Void> deleteById(UUID id);
}

interface ValuationRepositoryInternal {
    <S extends Valuation> Mono<S> save(S entity);

    Flux<Valuation> findAllBy(Pageable pageable);

    Flux<Valuation> findAll();

    Mono<Valuation> findById(UUID id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Valuation> findAllBy(Pageable pageable, Criteria criteria);
    Flux<Valuation> findByCriteria(ValuationCriteria criteria, Pageable pageable);

    Mono<Long> countByCriteria(ValuationCriteria criteria);
}
