package com.assetcompass.gateway.repository;

import com.assetcompass.gateway.domain.AssetRatio;
import com.assetcompass.gateway.domain.criteria.AssetRatioCriteria;
import com.assetcompass.gateway.repository.rowmapper.AssetRatioRowMapper;
import com.assetcompass.gateway.repository.rowmapper.AssetRowMapper;
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
 * Spring Data R2DBC custom repository implementation for the AssetRatio entity.
 */
@SuppressWarnings("unused")
class AssetRatioRepositoryInternalImpl extends SimpleR2dbcRepository<AssetRatio, UUID> implements AssetRatioRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final AssetRowMapper assetMapper;
    private final AssetRatioRowMapper assetratioMapper;
    private final ColumnConverter columnConverter;

    private static final Table entityTable = Table.aliased("asset_ratio", EntityManager.ENTITY_ALIAS);
    private static final Table assetTable = Table.aliased("asset", "asset");

    public AssetRatioRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        AssetRowMapper assetMapper,
        AssetRatioRowMapper assetratioMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter,
        ColumnConverter columnConverter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(AssetRatio.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.assetMapper = assetMapper;
        this.assetratioMapper = assetratioMapper;
        this.columnConverter = columnConverter;
    }

    @Override
    public Flux<AssetRatio> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<AssetRatio> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = AssetRatioSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        columns.addAll(AssetSqlHelper.getColumns(assetTable, "asset"));
        SelectFromAndJoinCondition selectFrom = Select.builder()
            .select(columns)
            .from(entityTable)
            .leftOuterJoin(assetTable)
            .on(Column.create("asset_id", entityTable))
            .equals(Column.create("id", assetTable));
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, AssetRatio.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<AssetRatio> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<AssetRatio> findById(UUID id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(StringUtils.wrap(id.toString(), "'")));
        return createQuery(null, whereClause).one();
    }

    private AssetRatio process(Row row, RowMetadata metadata) {
        AssetRatio entity = assetratioMapper.apply(row, "e");
        entity.setAsset(assetMapper.apply(row, "asset"));
        return entity;
    }

    @Override
    public <S extends AssetRatio> Mono<S> save(S entity) {
        return super.save(entity);
    }

    @Override
    public Flux<AssetRatio> findByCriteria(AssetRatioCriteria assetRatioCriteria, Pageable page) {
        return createQuery(page, buildConditions(assetRatioCriteria)).all();
    }

    @Override
    public Mono<Long> countByCriteria(AssetRatioCriteria criteria) {
        return findByCriteria(criteria, null)
            .collectList()
            .map(collectedList -> collectedList != null ? (long) collectedList.size() : (long) 0);
    }

    private Condition buildConditions(AssetRatioCriteria criteria) {
        ConditionBuilder builder = new ConditionBuilder(this.columnConverter);
        List<Condition> allConditions = new ArrayList<Condition>();
        if (criteria != null) {
            if (criteria.getId() != null) {
                builder.buildFilterConditionForField(criteria.getId(), entityTable.column("id"));
            }
            if (criteria.getRatio() != null) {
                builder.buildFilterConditionForField(criteria.getRatio(), entityTable.column("ratio"));
            }
            if (criteria.getEffectiveFrom() != null) {
                builder.buildFilterConditionForField(criteria.getEffectiveFrom(), entityTable.column("effective_from"));
            }
            if (criteria.getAssetId() != null) {
                builder.buildFilterConditionForField(criteria.getAssetId(), assetTable.column("id"));
            }
        }
        return builder.buildConditions();
    }
}
