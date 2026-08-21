package com.assetcompass.gateway.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Table;

public class ValuationSqlHelper {

    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("snapshot_date", table, columnPrefix + "_snapshot_date"));
        columns.add(Column.aliased("total_value", table, columnPrefix + "_total_value"));
        columns.add(Column.aliased("currency", table, columnPrefix + "_currency"));

        columns.add(Column.aliased("account_id", table, columnPrefix + "_account_id"));
        return columns;
    }
}
