package com.assetcompass.gateway.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Table;

public class AssetSqlHelper {

    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("ticket", table, columnPrefix + "_ticket"));
        columns.add(Column.aliased("category", table, columnPrefix + "_category"));
        columns.add(Column.aliased("country", table, columnPrefix + "_country"));
        columns.add(Column.aliased("description", table, columnPrefix + "_description"));

        return columns;
    }
}
