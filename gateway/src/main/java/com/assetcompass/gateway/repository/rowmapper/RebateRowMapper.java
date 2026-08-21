package com.assetcompass.gateway.repository.rowmapper;

import com.assetcompass.gateway.domain.Rebate;
import io.r2dbc.spi.Row;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Rebate}, with proper type conversions.
 */
@Service
public class RebateRowMapper implements BiFunction<Row, String, Rebate> {

    private final ColumnConverter converter;

    public RebateRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Rebate} stored in the database.
     */
    @Override
    public Rebate apply(Row row, String prefix) {
        Rebate entity = new Rebate();
        entity.setId(converter.fromRow(row, prefix + "_id", UUID.class));
        entity.setRebateDate(converter.fromRow(row, prefix + "_rebate_date", LocalDate.class));
        entity.setAmount(converter.fromRow(row, prefix + "_amount", BigDecimal.class));
        entity.setCurrency(converter.fromRow(row, prefix + "_currency", String.class));
        entity.setAccountId(converter.fromRow(row, prefix + "_account_id", UUID.class));
        entity.setOperationId(converter.fromRow(row, prefix + "_operation_id", UUID.class));
        return entity;
    }
}
