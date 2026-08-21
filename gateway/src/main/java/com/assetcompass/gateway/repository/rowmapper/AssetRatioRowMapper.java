package com.assetcompass.gateway.repository.rowmapper;

import com.assetcompass.gateway.domain.AssetRatio;
import io.r2dbc.spi.Row;
import java.time.LocalDate;
import java.util.UUID;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link AssetRatio}, with proper type conversions.
 */
@Service
public class AssetRatioRowMapper implements BiFunction<Row, String, AssetRatio> {

    private final ColumnConverter converter;

    public AssetRatioRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link AssetRatio} stored in the database.
     */
    @Override
    public AssetRatio apply(Row row, String prefix) {
        AssetRatio entity = new AssetRatio();
        entity.setId(converter.fromRow(row, prefix + "_id", UUID.class));
        entity.setRatio(converter.fromRow(row, prefix + "_ratio", String.class));
        entity.setEffectiveFrom(converter.fromRow(row, prefix + "_effective_from", LocalDate.class));
        entity.setAssetId(converter.fromRow(row, prefix + "_asset_id", UUID.class));
        return entity;
    }
}
