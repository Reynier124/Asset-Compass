package com.assetcompass.gateway.repository.rowmapper;

import com.assetcompass.gateway.domain.IncomeEvent;
import com.assetcompass.gateway.domain.enumeration.IncomeType;
import io.r2dbc.spi.Row;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link IncomeEvent}, with proper type conversions.
 */
@Service
public class IncomeEventRowMapper implements BiFunction<Row, String, IncomeEvent> {

    private final ColumnConverter converter;

    public IncomeEventRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link IncomeEvent} stored in the database.
     */
    @Override
    public IncomeEvent apply(Row row, String prefix) {
        IncomeEvent entity = new IncomeEvent();
        entity.setId(converter.fromRow(row, prefix + "_id", UUID.class));
        entity.setType(converter.fromRow(row, prefix + "_type", IncomeType.class));
        entity.setEventDate(converter.fromRow(row, prefix + "_event_date", LocalDate.class));
        entity.setAmount(converter.fromRow(row, prefix + "_amount", BigDecimal.class));
        entity.setCurrency(converter.fromRow(row, prefix + "_currency", String.class));
        entity.setAccountId(converter.fromRow(row, prefix + "_account_id", UUID.class));
        entity.setAssetId(converter.fromRow(row, prefix + "_asset_id", UUID.class));
        return entity;
    }
}
