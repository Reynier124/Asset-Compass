package com.assetcompass.gateway.repository;

import com.assetcompass.gateway.domain.Rebate;
import com.assetcompass.gateway.domain.criteria.RebateCriteria;
import com.assetcompass.gateway.repository.rowmapper.BrokerAccountRowMapper;
import com.assetcompass.gateway.repository.rowmapper.ColumnConverter;
import com.assetcompass.gateway.repository.rowmapper.OperationRowMapper;
import com.assetcompass.gateway.repository.rowmapper.RebateRowMapper;
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
 * Spring Data R2DBC custom repository implementation for the Rebate entity.
 */
@SuppressWarnings("unused")
class RebateRepositoryInternalImpl extends SimpleR2dbcRepository<Rebate, UUID> implements RebateRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final BrokerAccountRowMapper brokeraccountMapper;
    private final OperationRowMapper operationMapper;
    private final RebateRowMapper rebateMapper;
    private final ColumnConverter columnConverter;

    private static final Table entityTable = Table.aliased("rebate", EntityManager.ENTITY_ALIAS);
    private static final Table accountTable = Table.aliased("broker_account", "e_account");
    private static final Table operationTable = Table.aliased("operation", "operation");

    public RebateRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        BrokerAccountRowMapper brokeraccountMapper,
        OperationRowMapper operationMapper,
        RebateRowMapper rebateMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter,
        ColumnConverter columnConverter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(Rebate.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.brokeraccountMapper = brokeraccountMapper;
        this.operationMapper = operationMapper;
        this.rebateMapper = rebateMapper;
        this.columnConverter = columnConverter;
    }

    @Override
    public Flux<Rebate> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<Rebate> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = RebateSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(BrokerAccountSqlHelper.getColumns(accountTable, "account"));
        columns.addAll(OperationSqlHelper.getColumns(operationTable, "operation"));
        SelectFromAndJoinCondition selectFrom = Select.builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(accountTable)
            .on(Column.create("account_id", entityTable))
            .equals(Column.create("id", accountTable))
            .leftOuterJoin(operationTable)
            .on(Column.create("operation_id", entityTable))
            .equals(Column.create("id", operationTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, Rebate.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<Rebate> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<Rebate> findById(UUID id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(StringUtils.wrap(id.toString(), "'")));
        return createQuery(null, whereClause).one();
    }

    private Rebate process(Row row, RowMetadata metadata) {
        Rebate entity = rebateMapper.apply(row, "e");
        entity.setAccount(brokeraccountMapper.apply(row, "account"));
        entity.setOperation(operationMapper.apply(row, "operation"));
        return entity;
    }

    @Override
    public <S extends Rebate> Mono<S> save(S entity) {
        return super.save(entity);
    }

    @Override
    public Flux<Rebate> findByCriteria(RebateCriteria rebateCriteria, Pageable page) {
        return createQuery(page, buildConditions(rebateCriteria)).all();
    }

    @Override
    public Mono<Long> countByCriteria(RebateCriteria criteria) {
        return findByCriteria(criteria, null)
            .collectList()
            .map(collectedList -> collectedList != null ? (long) collectedList.size() : (long) 0);
    }

    private Condition buildConditions(RebateCriteria criteria) {
        ConditionBuilder builder = new ConditionBuilder(this.columnConverter);
        List<Condition> allConditions = new ArrayList<Condition>();
        if (criteria != null) {
            if (criteria.getId() != null) {
                builder.buildFilterConditionForField(criteria.getId(), entityTable.column("id"));
            }
            if (criteria.getRebateDate() != null) {
                builder.buildFilterConditionForField(criteria.getRebateDate(), entityTable.column("rebate_date"));
            }
            if (criteria.getAmount() != null) {
                builder.buildFilterConditionForField(criteria.getAmount(), entityTable.column("amount"));
            }
            if (criteria.getCurrency() != null) {
                builder.buildFilterConditionForField(criteria.getCurrency(), entityTable.column("currency"));
            }
            if (criteria.getAccountId() != null) {
                builder.buildFilterConditionForField(criteria.getAccountId(), accountTable.column("id"));
            }
            if (criteria.getOperationId() != null) {
                builder.buildFilterConditionForField(criteria.getOperationId(), operationTable.column("id"));
            }
        }
        return builder.buildConditions();
    }
}
