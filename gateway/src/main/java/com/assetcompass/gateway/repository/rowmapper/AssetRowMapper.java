package com.assetcompass.gateway.repository.rowmapper;

import com.assetcompass.gateway.domain.Asset;
import io.r2dbc.spi.Row;
import java.util.UUID;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Asset}, with proper type conversions.
 */
@Service
public class AssetRowMapper implements BiFunction<Row, String, Asset> {

    private final ColumnConverter converter;

    public AssetRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Asset} stored in the database.
     */
    @Override
    public Asset apply(Row row, String prefix) {
        Asset entity = new Asset();
        entity.setId(converter.fromRow(row, prefix + "_id", UUID.class));
        entity.setTicket(converter.fromRow(row, prefix + "_ticket", String.class));
        entity.setCategory(converter.fromRow(row, prefix + "_category", String.class));
        entity.setCountry(converter.fromRow(row, prefix + "_country", String.class));
        entity.setDescription(converter.fromRow(row, prefix + "_description", String.class));
        return entity;
    }
}
