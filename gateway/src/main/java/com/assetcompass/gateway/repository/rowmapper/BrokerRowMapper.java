package com.assetcompass.gateway.repository.rowmapper;

import com.assetcompass.gateway.domain.Broker;
import io.r2dbc.spi.Row;
import java.util.UUID;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Broker}, with proper type conversions.
 */
@Service
public class BrokerRowMapper implements BiFunction<Row, String, Broker> {

    private final ColumnConverter converter;

    public BrokerRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Broker} stored in the database.
     */
    @Override
    public Broker apply(Row row, String prefix) {
        Broker entity = new Broker();
        entity.setId(converter.fromRow(row, prefix + "_id", UUID.class));
        entity.setName(converter.fromRow(row, prefix + "_name", String.class));
        return entity;
    }
}
