package com.assetcompass.gateway.repository.rowmapper;

import com.assetcompass.gateway.domain.Operation;
import com.assetcompass.gateway.domain.enumeration.OperationType;
import io.r2dbc.spi.Row;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Operation}, with proper type conversions.
 */
@Service
public class OperationRowMapper implements BiFunction<Row, String, Operation> {

    private final ColumnConverter converter;

    public OperationRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Operation} stored in the database.
     */
    @Override
    public Operation apply(Row row, String prefix) {
        Operation entity = new Operation();
        entity.setId(converter.fromRow(row, prefix + "_id", UUID.class));
        entity.setType(converter.fromRow(row, prefix + "_type", OperationType.class));
        entity.setOperationDate(converter.fromRow(row, prefix + "_operation_date", LocalDate.class));
        entity.setQuantity(converter.fromRow(row, prefix + "_quantity", BigDecimal.class));
        entity.setPrice(converter.fromRow(row, prefix + "_price", BigDecimal.class));
        entity.setAmount(converter.fromRow(row, prefix + "_amount", BigDecimal.class));
        entity.setCurrency(converter.fromRow(row, prefix + "_currency", String.class));
        entity.setUnderlyingPrice(converter.fromRow(row, prefix + "_underlying_price", BigDecimal.class));
        entity.setCommission(converter.fromRow(row, prefix + "_commission", BigDecimal.class));
        entity.setAccountId(converter.fromRow(row, prefix + "_account_id", UUID.class));
        entity.setAssetId(converter.fromRow(row, prefix + "_asset_id", UUID.class));
        entity.setClosesOperationId(converter.fromRow(row, prefix + "_closes_operation_id", UUID.class));
        return entity;
    }
}
