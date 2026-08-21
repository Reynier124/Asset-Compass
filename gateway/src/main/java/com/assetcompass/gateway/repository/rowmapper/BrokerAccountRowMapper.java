package com.assetcompass.gateway.repository.rowmapper;

import com.assetcompass.gateway.domain.BrokerAccount;
import io.r2dbc.spi.Row;
import java.util.UUID;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link BrokerAccount}, with proper type conversions.
 */
@Service
public class BrokerAccountRowMapper implements BiFunction<Row, String, BrokerAccount> {

    private final ColumnConverter converter;

    public BrokerAccountRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link BrokerAccount} stored in the database.
     */
    @Override
    public BrokerAccount apply(Row row, String prefix) {
        BrokerAccount entity = new BrokerAccount();
        entity.setId(converter.fromRow(row, prefix + "_id", UUID.class));
        entity.setExternalAccountId(converter.fromRow(row, prefix + "_external_account_id", String.class));
        entity.setDisplayName(converter.fromRow(row, prefix + "_display_name", String.class));
        entity.setBrokerId(converter.fromRow(row, prefix + "_broker_id", UUID.class));
        return entity;
    }
}
