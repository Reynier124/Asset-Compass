package com.assetcompass.gateway.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Table;

public class BrokerAccountSqlHelper {

    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("external_account_id", table, columnPrefix + "_external_account_id"));
        columns.add(Column.aliased("display_name", table, columnPrefix + "_display_name"));

        columns.add(Column.aliased("broker_id", table, columnPrefix + "_broker_id"));
        return columns;
    }
}
