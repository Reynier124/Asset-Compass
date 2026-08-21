package com.assetcompass.gateway.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Table;

public class IncomeEventSqlHelper {

    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("type", table, columnPrefix + "_type"));
        columns.add(Column.aliased("event_date", table, columnPrefix + "_event_date"));
        columns.add(Column.aliased("amount", table, columnPrefix + "_amount"));
        columns.add(Column.aliased("currency", table, columnPrefix + "_currency"));

        columns.add(Column.aliased("account_id", table, columnPrefix + "_account_id"));
        columns.add(Column.aliased("asset_id", table, columnPrefix + "_asset_id"));
        return columns;
    }
}
