package com.assetcompass.gateway.repository;

import com.assetcompass.gateway.domain.Operation;
import com.assetcompass.gateway.domain.criteria.OperationCriteria;
import com.assetcompass.gateway.repository.rowmapper.AssetRowMapper;
import com.assetcompass.gateway.repository.rowmapper.BrokerAccountRowMapper;
import com.assetcompass.gateway.repository.rowmapper.ColumnConverter;
import com.assetcompass.gateway.repository.rowmapper.OperationRowMapper;
import com.assetcompass.gateway.repository.rowmapper.OperationRowMapper;
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
 * Spring Data R2DBC custom repository implementation for the Operation entity.
 */
@SuppressWarnings("unused")
class OperationRepositoryInternalImpl extends SimpleR2dbcRepository<Operation, UUID> implements OperationRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final BrokerAccountRowMapper brokeraccountMapper;
    private final AssetRowMapper assetMapper;
    private final OperationRowMapper operationMapper;
    private final ColumnConverter columnConverter;

    private static final Table entityTable = Table.aliased("operation", EntityManager.ENTITY_ALIAS);
    private static final Table accountTable = Table.aliased("broker_account", "e_account");
    private static final Table assetTable = Table.aliased("asset", "asset");
    private static final Table closesOperationTable = Table.aliased("operation", "closesOperation");

    public OperationRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        BrokerAccountRowMapper brokeraccountMapper,
        AssetRowMapper assetMapper,
        OperationRowMapper operationMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter,
        ColumnConverter columnConverter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(Operation.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.brokeraccountMapper = brokeraccountMapper;
        this.assetMapper = assetMapper;
        this.operationMapper = operationMapper;
        this.columnConverter = columnConverter;
    }

    @Override
    public Flux<Operation> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<Operation> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = OperationSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(BrokerAccountSqlHelper.getColumns(accountTable, "account"));
        columns.addAll(AssetSqlHelper.getColumns(assetTable, "asset"));
        columns.addAll(OperationSqlHelper.getColumns(closesOperationTable, "closesOperation"));
        SelectFromAndJoinCondition selectFrom = Select.builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(accountTable)
            .on(Column.create("account_id", entityTable))
            .equals(Column.create("id", accountTable))
            .leftOuterJoin(assetTable)
            .on(Column.create("asset_id", entityTable))
            .equals(Column.create("id", assetTable))
            .leftOuterJoin(closesOperationTable)
            .on(Column.create("closes_operation_id", entityTable))
            .equals(Column.create("id", closesOperationTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, Operation.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<Operation> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<Operation> findById(UUID id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(StringUtils.wrap(id.toString(), "'")));
        return createQuery(null, whereClause).one();
    }

    private Operation process(Row row, RowMetadata metadata) {
        Operation entity = operationMapper.apply(row, "e");
        entity.setAccount(brokeraccountMapper.apply(row, "account"));
        entity.setAsset(assetMapper.apply(row, "asset"));
        entity.setClosesOperation(operationMapper.apply(row, "closesOperation"));
        return entity;
    }

    @Override
    public <S extends Operation> Mono<S> save(S entity) {
        return super.save(entity);
    }

    @Override
    public Flux<Operation> findByCriteria(OperationCriteria operationCriteria, Pageable page) {
        return createQuery(page, buildConditions(operationCriteria)).all();
    }

    @Override
    public Mono<Long> countByCriteria(OperationCriteria criteria) {
        return findByCriteria(criteria, null)
            .collectList()
            .map(collectedList -> collectedList != null ? (long) collectedList.size() : (long) 0);
    }

    private Condition buildConditions(OperationCriteria criteria) {
        ConditionBuilder builder = new ConditionBuilder(this.columnConverter);
        List<Condition> allConditions = new ArrayList<Condition>();
        if (criteria != null) {
            if (criteria.getId() != null) {
                builder.buildFilterConditionForField(criteria.getId(), entityTable.column("id"));
            }
            if (criteria.getType() != null) {
                builder.buildFilterConditionForField(criteria.getType(), entityTable.column("type"));
            }
            if (criteria.getOperationDate() != null) {
                builder.buildFilterConditionForField(criteria.getOperationDate(), entityTable.column("operation_date"));
            }
            if (criteria.getQuantity() != null) {
                builder.buildFilterConditionForField(criteria.getQuantity(), entityTable.column("quantity"));
            }
            if (criteria.getPrice() != null) {
                builder.buildFilterConditionForField(criteria.getPrice(), entityTable.column("price"));
            }
            if (criteria.getAmount() != null) {
                builder.buildFilterConditionForField(criteria.getAmount(), entityTable.column("amount"));
            }
            if (criteria.getCurrency() != null) {
                builder.buildFilterConditionForField(criteria.getCurrency(), entityTable.column("currency"));
            }
            if (criteria.getUnderlyingPrice() != null) {
                builder.buildFilterConditionForField(criteria.getUnderlyingPrice(), entityTable.column("underlying_price"));
            }
            if (criteria.getCommission() != null) {
                builder.buildFilterConditionForField(criteria.getCommission(), entityTable.column("commission"));
            }
            if (criteria.getAccountId() != null) {
                builder.buildFilterConditionForField(criteria.getAccountId(), accountTable.column("id"));
            }
            if (criteria.getAssetId() != null) {
                builder.buildFilterConditionForField(criteria.getAssetId(), assetTable.column("id"));
            }
            if (criteria.getClosesOperationId() != null) {
                builder.buildFilterConditionForField(criteria.getClosesOperationId(), closesOperationTable.column("id"));
            }
        }
        return builder.buildConditions();
    }
}
