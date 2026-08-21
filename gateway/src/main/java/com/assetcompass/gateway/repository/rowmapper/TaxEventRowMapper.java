package com.assetcompass.gateway.repository.rowmapper;

import com.assetcompass.gateway.domain.TaxEvent;
import com.assetcompass.gateway.domain.enumeration.TaxType;
import io.r2dbc.spi.Row;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link TaxEvent}, with proper type conversions.
 */
@Service
public class TaxEventRowMapper implements BiFunction<Row, String, TaxEvent> {

    private final ColumnConverter converter;

    public TaxEventRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link TaxEvent} stored in the database.
     */
    @Override
    public TaxEvent apply(Row row, String prefix) {
        TaxEvent entity = new TaxEvent();
        entity.setId(converter.fromRow(row, prefix + "_id", UUID.class));
        entity.setType(converter.fromRow(row, prefix + "_type", TaxType.class));
        entity.setTaxDate(converter.fromRow(row, prefix + "_tax_date", LocalDate.class));
        entity.setAmount(converter.fromRow(row, prefix + "_amount", BigDecimal.class));
        entity.setCurrency(converter.fromRow(row, prefix + "_currency", String.class));
        entity.setAccountId(converter.fromRow(row, prefix + "_account_id", UUID.class));
        entity.setOperationId(converter.fromRow(row, prefix + "_operation_id", UUID.class));
        entity.setIncomeEventId(converter.fromRow(row, prefix + "_income_event_id", UUID.class));
        return entity;
    }
}
