package com.assetcompass.gateway.repository.rowmapper;

import com.assetcompass.gateway.domain.Valuation;
import io.r2dbc.spi.Row;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Valuation}, with proper type conversions.
 */
@Service
public class ValuationRowMapper implements BiFunction<Row, String, Valuation> {

    private final ColumnConverter converter;

    public ValuationRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Valuation} stored in the database.
     */
    @Override
    public Valuation apply(Row row, String prefix) {
        Valuation entity = new Valuation();
        entity.setId(converter.fromRow(row, prefix + "_id", UUID.class));
        entity.setSnapshotDate(converter.fromRow(row, prefix + "_snapshot_date", LocalDate.class));
        entity.setTotalValue(converter.fromRow(row, prefix + "_total_value", BigDecimal.class));
        entity.setCurrency(converter.fromRow(row, prefix + "_currency", String.class));
        entity.setAccountId(converter.fromRow(row, prefix + "_account_id", UUID.class));
        return entity;
    }
}
