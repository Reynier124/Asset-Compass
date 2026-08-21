package com.assetcompass.gateway.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Table;

public class OperationSqlHelper {

    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("type", table, columnPrefix + "_type"));
        columns.add(Column.aliased("operation_date", table, columnPrefix + "_operation_date"));
        columns.add(Column.aliased("quantity", table, columnPrefix + "_quantity"));
        columns.add(Column.aliased("price", table, columnPrefix + "_price"));
        columns.add(Column.aliased("amount", table, columnPrefix + "_amount"));
        columns.add(Column.aliased("currency", table, columnPrefix + "_currency"));
        columns.add(Column.aliased("underlying_price", table, columnPrefix + "_underlying_price"));
        columns.add(Column.aliased("commission", table, columnPrefix + "_commission"));

        columns.add(Column.aliased("account_id", table, columnPrefix + "_account_id"));
        columns.add(Column.aliased("asset_id", table, columnPrefix + "_asset_id"));
        columns.add(Column.aliased("closes_operation_id", table, columnPrefix + "_closes_operation_id"));
        return columns;
    }
}
