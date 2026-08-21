package com.assetcompass.gateway.repository;

import com.assetcompass.gateway.domain.Broker;
import com.assetcompass.gateway.domain.criteria.BrokerCriteria;
import com.assetcompass.gateway.repository.rowmapper.BrokerRowMapper;
import com.assetcompass.gateway.repository.rowmapper.ColumnConverter;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.support.SimpleR2dbcRepository;
import org.springframework.data.relational.core.sql.Comparison;
import org.springframework.data.relational.core.sql.Condition;
import org.springframework.data.relational.core.sql.Conditions;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoin;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.data.relational.repository.support.MappingRelationalEntityInformation;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.service.ConditionBuilder;

/**
 * Spring Data R2DBC custom repository implementation for the Broker entity.
 */
@SuppressWarnings("unused")
class BrokerRepositoryInternalImpl extends SimpleR2dbcRepository<Broker, UUID> implements BrokerRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final BrokerRowMapper brokerMapper;
    private final ColumnConverter columnConverter;

    private static final Table entityTable = Table.aliased("broker", EntityManager.ENTITY_ALIAS);

    public BrokerRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        BrokerRowMapper brokerMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter,
        ColumnConverter columnConverter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(Broker.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.brokerMapper = brokerMapper;
        this.columnConverter = columnConverter;
    }

    @Override
    public Flux<Broker> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<Broker> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = BrokerSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        SelectFromAndJoin selectFrom = Select.builder().select(columns).from(entityTable);
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, Broker.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<Broker> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<Broker> findById(UUID id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(StringUtils.wrap(id.toString(), "'")));
        return createQuery(null, whereClause).one();
    }

    private Broker process(Row row, RowMetadata metadata) {
        Broker entity = brokerMapper.apply(row, "e");
        return entity;
    }

    @Override
    public <S extends Broker> Mono<S> save(S entity) {
        return super.save(entity);
    }

    @Override
    public Flux<Broker> findByCriteria(BrokerCriteria brokerCriteria, Pageable page) {
        return createQuery(page, buildConditions(brokerCriteria)).all();
    }

    @Override
    public Mono<Long> countByCriteria(BrokerCriteria criteria) {
        return findByCriteria(criteria, null)
            .collectList()
            .map(collectedList -> collectedList != null ? (long) collectedList.size() : (long) 0);
    }

    private Condition buildConditions(BrokerCriteria criteria) {
        ConditionBuilder builder = new ConditionBuilder(this.columnConverter);
        List<Condition> allConditions = new ArrayList<Condition>();
        if (criteria != null) {
            if (criteria.getId() != null) {
                builder.buildFilterConditionForField(criteria.getId(), entityTable.column("id"));
            }
            if (criteria.getName() != null) {
                builder.buildFilterConditionForField(criteria.getName(), entityTable.column("name"));
            }
        }
        return builder.buildConditions();
    }
}
