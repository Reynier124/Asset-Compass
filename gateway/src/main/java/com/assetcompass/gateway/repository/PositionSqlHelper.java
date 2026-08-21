package com.assetcompass.gateway.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Table;

public class PositionSqlHelper {

    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("quantity", table, columnPrefix + "_quantity"));
        columns.add(Column.aliased("average_cost", table, columnPrefix + "_average_cost"));
        columns.add(Column.aliased("current_value", table, columnPrefix + "_current_value"));
        columns.add(Column.aliased("currency", table, columnPrefix + "_currency"));
        columns.add(Column.aliased("last_synced_at", table, columnPrefix + "_last_synced_at"));

        columns.add(Column.aliased("account_id", table, columnPrefix + "_account_id"));
        columns.add(Column.aliased("asset_id", table, columnPrefix + "_asset_id"));
        return columns;
    }
}
