package com.assetcompass.gateway.repository;

import com.assetcompass.gateway.domain.Valuation;
import com.assetcompass.gateway.domain.criteria.ValuationCriteria;
import com.assetcompass.gateway.repository.rowmapper.BrokerAccountRowMapper;
import com.assetcompass.gateway.repository.rowmapper.ColumnConverter;
import com.assetcompass.gateway.repository.rowmapper.ValuationRowMapper;
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
 * Spring Data R2DBC custom repository implementation for the Valuation entity.
 */
@SuppressWarnings("unused")
class ValuationRepositoryInternalImpl extends SimpleR2dbcRepository<Valuation, UUID> implements ValuationRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final BrokerAccountRowMapper brokeraccountMapper;
    private final ValuationRowMapper valuationMapper;
    private final ColumnConverter columnConverter;

    private static final Table entityTable = Table.aliased("valuation", EntityManager.ENTITY_ALIAS);
    private static final Table accountTable = Table.aliased("broker_account", "e_account");

    public ValuationRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        BrokerAccountRowMapper brokeraccountMapper,
        ValuationRowMapper valuationMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter,
        ColumnConverter columnConverter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(Valuation.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.brokeraccountMapper = brokeraccountMapper;
        this.valuationMapper = valuationMapper;
        this.columnConverter = columnConverter;
    }

    @Override
    public Flux<Valuation> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<Valuation> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = ValuationSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(BrokerAccountSqlHelper.getColumns(accountTable, "account"));
        SelectFromAndJoinCondition selectFrom = Select.builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(accountTable)
            .on(Column.create("account_id", entityTable))
            .equals(Column.create("id", accountTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, Valuation.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<Valuation> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<Valuation> findById(UUID id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(StringUtils.wrap(id.toString(), "'")));
        return createQuery(null, whereClause).one();
    }

    private Valuation process(Row row, RowMetadata metadata) {
        Valuation entity = valuationMapper.apply(row, "e");
        entity.setAccount(brokeraccountMapper.apply(row, "account"));
        return entity;
    }

    @Override
    public <S extends Valuation> Mono<S> save(S entity) {
        return super.save(entity);
    }

    @Override
    public Flux<Valuation> findByCriteria(ValuationCriteria valuationCriteria, Pageable page) {
        return createQuery(page, buildConditions(valuationCriteria)).all();
    }

    @Override
    public Mono<Long> countByCriteria(ValuationCriteria criteria) {
        return findByCriteria(criteria, null)
            .collectList()
            .map(collectedList -> collectedList != null ? (long) collectedList.size() : (long) 0);
    }

    private Condition buildConditions(ValuationCriteria criteria) {
        ConditionBuilder builder = new ConditionBuilder(this.columnConverter);
        List<Condition> allConditions = new ArrayList<Condition>();
        if (criteria != null) {
            if (criteria.getId() != null) {
                builder.buildFilterConditionForField(criteria.getId(), entityTable.column("id"));
            }
            if (criteria.getSnapshotDate() != null) {
                builder.buildFilterConditionForField(criteria.getSnapshotDate(), entityTable.column("snapshot_date"));
            }
            if (criteria.getTotalValue() != null) {
                builder.buildFilterConditionForField(criteria.getTotalValue(), entityTable.column("total_value"));
            }
            if (criteria.getCurrency() != null) {
                builder.buildFilterConditionForField(criteria.getCurrency(), entityTable.column("currency"));
            }
            if (criteria.getAccountId() != null) {
                builder.buildFilterConditionForField(criteria.getAccountId(), accountTable.column("id"));
            }
        }
        return builder.buildConditions();
    }
}
