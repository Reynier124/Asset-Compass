package com.assetcompass.gateway.repository;

import com.assetcompass.gateway.domain.BrokerAccount;
import com.assetcompass.gateway.domain.criteria.BrokerAccountCriteria;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the BrokerAccount entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BrokerAccountRepository extends ReactiveCrudRepository<BrokerAccount, UUID>, BrokerAccountRepositoryInternal {
    @Query("SELECT * FROM broker_account entity WHERE entity.broker_id = :id")
    Flux<BrokerAccount> findByBroker(UUID id);

    @Query("SELECT * FROM broker_account entity WHERE entity.broker_id IS NULL")
    Flux<BrokerAccount> findAllWhereBrokerIsNull();

    @Override
    <S extends BrokerAccount> Mono<S> save(S entity);

    @Override
    Flux<BrokerAccount> findAll();

    @Override
    Mono<BrokerAccount> findById(UUID id);

    @Override
    Mono<Void> deleteById(UUID id);
}

interface BrokerAccountRepositoryInternal {
    <S extends BrokerAccount> Mono<S> save(S entity);

    Flux<BrokerAccount> findAllBy(Pageable pageable);

    Flux<BrokerAccount> findAll();

    Mono<BrokerAccount> findById(UUID id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<BrokerAccount> findAllBy(Pageable pageable, Criteria criteria);
    Flux<BrokerAccount> findByCriteria(BrokerAccountCriteria criteria, Pageable pageable);

    Mono<Long> countByCriteria(BrokerAccountCriteria criteria);
}
