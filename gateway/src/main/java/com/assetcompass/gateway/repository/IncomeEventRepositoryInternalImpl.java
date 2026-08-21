package com.assetcompass.gateway.repository;

import com.assetcompass.gateway.domain.IncomeEvent;
import com.assetcompass.gateway.domain.criteria.IncomeEventCriteria;
import com.assetcompass.gateway.repository.rowmapper.AssetRowMapper;
import com.assetcompass.gateway.repository.rowmapper.BrokerAccountRowMapper;
import com.assetcompass.gateway.repository.rowmapper.ColumnConverter;
import com.assetcompass.gateway.repository.rowmapper.IncomeEventRowMapper;
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
 * Spring Data R2DBC custom repository implementation for the IncomeEvent entity.
 */
@SuppressWarnings("unused")
class IncomeEventRepositoryInternalImpl extends SimpleR2dbcRepository<IncomeEvent, UUID> implements IncomeEventRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final BrokerAccountRowMapper brokeraccountMapper;
    private final AssetRowMapper assetMapper;
    private final IncomeEventRowMapper incomeeventMapper;
    private final ColumnConverter columnConverter;

    private static final Table entityTable = Table.aliased("income_event", EntityManager.ENTITY_ALIAS);
    private static final Table accountTable = Table.aliased("broker_account", "e_account");
    private static final Table assetTable = Table.aliased("asset", "asset");

    public IncomeEventRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        BrokerAccountRowMapper brokeraccountMapper,
        AssetRowMapper assetMapper,
        IncomeEventRowMapper incomeeventMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter,
        ColumnConverter columnConverter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(IncomeEvent.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.brokeraccountMapper = brokeraccountMapper;
        this.assetMapper = assetMapper;
        this.incomeeventMapper = incomeeventMapper;
        this.columnConverter = columnConverter;
    }

    @Override
    public Flux<IncomeEvent> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<IncomeEvent> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = IncomeEventSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(BrokerAccountSqlHelper.getColumns(accountTable, "account"));
        columns.addAll(AssetSqlHelper.getColumns(assetTable, "asset"));
        SelectFromAndJoinCondition selectFrom = Select.builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(accountTable)
            .on(Column.create("account_id", entityTable))
            .equals(Column.create("id", accountTable))
            .leftOuterJoin(assetTable)
            .on(Column.create("asset_id", entityTable))
            .equals(Column.create("id", assetTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, IncomeEvent.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<IncomeEvent> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<IncomeEvent> findById(UUID id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(StringUtils.wrap(id.toString(), "'")));
        return createQuery(null, whereClause).one();
    }

    private IncomeEvent process(Row row, RowMetadata metadata) {
        IncomeEvent entity = incomeeventMapper.apply(row, "e");
        entity.setAccount(brokeraccountMapper.apply(row, "account"));
        entity.setAsset(assetMapper.apply(row, "asset"));
        return entity;
    }

    @Override
    public <S extends IncomeEvent> Mono<S> save(S entity) {
        return super.save(entity);
    }

    @Override
    public Flux<IncomeEvent> findByCriteria(IncomeEventCriteria incomeEventCriteria, Pageable page) {
        return createQuery(page, buildConditions(incomeEventCriteria)).all();
    }

    @Override
    public Mono<Long> countByCriteria(IncomeEventCriteria criteria) {
        return findByCriteria(criteria, null)
            .collectList()
            .map(collectedList -> collectedList != null ? (long) collectedList.size() : (long) 0);
    }

    private Condition buildConditions(IncomeEventCriteria criteria) {
        ConditionBuilder builder = new ConditionBuilder(this.columnConverter);
        List<Condition> allConditions = new ArrayList<Condition>();
        if (criteria != null) {
            if (criteria.getId() != null) {
                builder.buildFilterConditionForField(criteria.getId(), entityTable.column("id"));
            }
            if (criteria.getType() != null) {
                builder.buildFilterConditionForField(criteria.getType(), entityTable.column("type"));
            }
            if (criteria.getEventDate() != null) {
                builder.buildFilterConditionForField(criteria.getEventDate(), entityTable.column("event_date"));
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
            if (criteria.getAssetId() != null) {
                builder.buildFilterConditionForField(criteria.getAssetId(), assetTable.column("id"));
            }
        }
        return builder.buildConditions();
    }
}
