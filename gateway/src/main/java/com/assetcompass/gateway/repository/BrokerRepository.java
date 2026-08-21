package com.assetcompass.gateway.repository;

import com.assetcompass.gateway.domain.Broker;
import com.assetcompass.gateway.domain.criteria.BrokerCriteria;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Broker entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BrokerRepository extends ReactiveCrudRepository<Broker, UUID>, BrokerRepositoryInternal {
    @Override
    <S extends Broker> Mono<S> save(S entity);

    @Override
    Flux<Broker> findAll();

    @Override
    Mono<Broker> findById(UUID id);

    @Override
    Mono<Void> deleteById(UUID id);
}

interface BrokerRepositoryInternal {
    <S extends Broker> Mono<S> save(S entity);

    Flux<Broker> findAllBy(Pageable pageable);

    Flux<Broker> findAll();

    Mono<Broker> findById(UUID id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Broker> findAllBy(Pageable pageable, Criteria criteria);
    Flux<Broker> findByCriteria(BrokerCriteria criteria, Pageable pageable);

    Mono<Long> countByCriteria(BrokerCriteria criteria);
}
