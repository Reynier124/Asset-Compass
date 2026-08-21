package com.assetcompass.gateway.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Table;

public class AssetRatioSqlHelper {

    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("ratio", table, columnPrefix + "_ratio"));
        columns.add(Column.aliased("effective_from", table, columnPrefix + "_effective_from"));

        columns.add(Column.aliased("asset_id", table, columnPrefix + "_asset_id"));
        return columns;
    }
}
