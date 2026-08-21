package com.assetcompass.gateway.repository;

import com.assetcompass.gateway.domain.Asset;
import com.assetcompass.gateway.domain.criteria.AssetCriteria;
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
 * Spring Data R2DBC custom repository implementation for the Asset entity.
 */
@SuppressWarnings("unused")
class AssetRepositoryInternalImpl extends SimpleR2dbcRepository<Asset, UUID> implements AssetRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final AssetRowMapper assetMapper;
    private final ColumnConverter columnConverter;

    private static final Table entityTable = Table.aliased("asset", EntityManager.ENTITY_ALIAS);

    public AssetRepositoryInternalImpl(
        R2dbcEntityTemplate template,
        EntityManager entityManager,
        AssetRowMapper assetMapper,
        R2dbcEntityOperations entityOperations,
        R2dbcConverter converter,
        ColumnConverter columnConverter
    ) {
        super(
            new MappingRelationalEntityInformation(converter.getMappingContext().getRequiredPersistentEntity(Asset.class)),
            entityOperations,
            converter
        );
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.assetMapper = assetMapper;
        this.columnConverter = columnConverter;
    }

    @Override
    public Flux<Asset> findAllBy(Pageable pageable) {
        return createQuery(pageable, null).all();
    }

    RowsFetchSpec<Asset> createQuery(Pageable pageable, Condition whereClause) {
        List<Expression> columns = AssetSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        SelectFromAndJoin selectFrom = Select.builder().select(columns).from(entityTable);
        // we do not support Criteria here for now as of https://github.com/jhipster/generator-jhipster/issues/18269
        String select = entityManager.createSelect(selectFrom, Asset.class, pageable, whereClause);
        return db.sql(select).map(this::process);
    }

    @Override
    public Flux<Asset> findAll() {
        return findAllBy(null);
    }

    @Override
    public Mono<Asset> findById(UUID id) {
        Comparison whereClause = Conditions.isEqual(entityTable.column("id"), Conditions.just(StringUtils.wrap(id.toString(), "'")));
        return createQuery(null, whereClause).one();
    }

    private Asset process(Row row, RowMetadata metadata) {
        Asset entity = assetMapper.apply(row, "e");
        return entity;
    }

    @Override
    public <S extends Asset> Mono<S> save(S entity) {
        return super.save(entity);
    }

    @Override
    public Flux<Asset> findByCriteria(AssetCriteria assetCriteria, Pageable page) {
        return createQuery(page, buildConditions(assetCriteria)).all();
    }

    @Override
    public Mono<Long> countByCriteria(AssetCriteria criteria) {
        return findByCriteria(criteria, null)
            .collectList()
            .map(collectedList -> collectedList != null ? (long) collectedList.size() : (long) 0);
    }

    private Condition buildConditions(AssetCriteria criteria) {
        ConditionBuilder builder = new ConditionBuilder(this.columnConverter);
        List<Condition> allConditions = new ArrayList<Condition>();
        if (criteria != null) {
            if (criteria.getId() != null) {
                builder.buildFilterConditionForField(criteria.getId(), entityTable.column("id"));
            }
            if (criteria.getTicket() != null) {
                builder.buildFilterConditionForField(criteria.getTicket(), entityTable.column("ticket"));
            }
            if (criteria.getCategory() != null) {
                builder.buildFilterConditionForField(criteria.getCategory(), entityTable.column("category"));
            }
            if (criteria.getCountry() != null) {
                builder.buildFilterConditionForField(criteria.getCountry(), entityTable.column("country"));
            }
            if (criteria.getDescription() != null) {
                builder.buildFilterConditionForField(criteria.getDescription(), entityTable.column("description"));
            }
        }
        return builder.buildConditions();
    }
}
