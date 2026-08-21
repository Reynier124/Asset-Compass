package com.assetcompass.gateway.repository;

import com.assetcompass.gateway.domain.BrokerAccount;
import com.assetcompass.gateway.domain.criteria.BrokerAccountCriteria;
import com.assetcompass.gateway.repository.rowmapper.BrokerAccountRowMapper;
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
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Comparison;
import org.springframework.data.relational.core.sql.Condition;
import org.springframework.data.relational.core.sql.Conditions;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoinCondition;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.data.relational.repository.support.MappingRelationalEntityInformation;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.service.ConditionBuilder;

/**
 * Spring Data R2DBC custom repository implementation for the BrokerAccount entity.
 */
@SuppressWarnings("unused")
class BrokerAccountRepositoryInternalImpl extends SimpleR2dbcRepository<BrokerAccount, UUID> implements BrokerAccountRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final BrokerRowMapper brokerMapper;
    private final BrokerAccountRowMapper brokeraccountMapper;
    private final ColumnConverter columnConverter;

    private static final Table entityTable = Table.aliased("broker_account", EntityManager.ENTITY_ALIAS);
    private static final Table brokerTable = Table.aliased("broker", "broker");

    public BrokerAccountRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        BrokerRowMapper brokerMapper,
        BrokerAccountRowMapper brokeraccountMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter,
        ColumnConverter columnConverter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(BrokerAccount.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.brokerMapper = brokerMapper;
        this.brokeraccountMapper = brokeraccountMapper;
        this.columnConverter = columnConverter;
    }

    @Override
    public Flux<BrokerAccount> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<BrokerAccount> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = BrokerAccountSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(BrokerSqlHelper.getColumns(brokerTable, "broker"));
        SelectFromAndJoinCondition selectFrom = Select.builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(brokerTable)
            .on(Column.create("broker_id", entityTable))
            .equals(Column.create("id", brokerTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, BrokerAccount.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<BrokerAccount> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<BrokerAccount> findById(UUID id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(StringUtils.wrap(id.toString(), "'")));
        return createQuery(null, whereClause).one();
    }

    private BrokerAccount process(Row row, RowMetadata metadata) {
        BrokerAccount entity = brokeraccountMapper.apply(row, "e");
        entity.setBroker(brokerMapper.apply(row, "broker"));
        return entity;
    }

    @Override
    public <S extends BrokerAccount> Mono<S> save(S entity) {
        return super.save(entity);
    }

    @Override
    public Flux<BrokerAccount> findByCriteria(BrokerAccountCriteria brokerAccountCriteria, Pageable page) {
        return createQuery(page, buildConditions(brokerAccountCriteria)).all();
    }

    @Override
    public Mono<Long> countByCriteria(BrokerAccountCriteria criteria) {
        return findByCriteria(criteria, null)
            .collectList()
            .map(collectedList -> collectedList != null ? (long) collectedList.size() : (long) 0);
    }

    private Condition buildConditions(BrokerAccountCriteria criteria) {
        ConditionBuilder builder = new ConditionBuilder(this.columnConverter);
        List<Condition> allConditions = new ArrayList<Condition>();
        if (criteria != null) {
            if (criteria.getId() != null) {
                builder.buildFilterConditionForField(criteria.getId(), entityTable.column("id"));
            }
            if (criteria.getExternalAccountId() != null) {
                builder.buildFilterConditionForField(criteria.getExternalAccountId(), entityTable.column("external_account_id"));
            }
            if (criteria.getDisplayName() != null) {
                builder.buildFilterConditionForField(criteria.getDisplayName(), entityTable.column("display_name"));
            }
            if (criteria.getBrokerId() != null) {
                builder.buildFilterConditionForField(criteria.getBrokerId(), brokerTable.column("id"));
            }
        }
        return builder.buildConditions();
    }
}
