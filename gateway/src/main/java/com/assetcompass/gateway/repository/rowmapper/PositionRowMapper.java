package com.assetcompass.gateway.repository.rowmapper;

import com.assetcompass.gateway.domain.Position;
import io.r2dbc.spi.Row;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Position}, with proper type conversions.
 */
@Service
public class PositionRowMapper implements BiFunction<Row, String, Position> {

    private final ColumnConverter converter;

    public PositionRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Position} stored in the database.
     */
    @Override
    public Position apply(Row row, String prefix) {
        Position entity = new Position();
        entity.setId(converter.fromRow(row, prefix + "_id", UUID.class));
        entity.setQuantity(converter.fromRow(row, prefix + "_quantity", BigDecimal.class));
        entity.setAverageCost(converter.fromRow(row, prefix + "_average_cost", BigDecimal.class));
        entity.setCurrentValue(converter.fromRow(row, prefix + "_current_value", BigDecimal.class));
        entity.setCurrency(converter.fromRow(row, prefix + "_currency", String.class));
        entity.setLastSyncedAt(converter.fromRow(row, prefix + "_last_synced_at", Instant.class));
        entity.setAccountId(converter.fromRow(row, prefix + "_account_id", UUID.class));
        entity.setAssetId(converter.fromRow(row, prefix + "_asset_id", UUID.class));
        return entity;
    }
}
