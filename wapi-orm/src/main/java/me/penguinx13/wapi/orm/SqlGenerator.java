package me.penguinx13.wapi.orm;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public final class SqlGenerator {

    public String createTable(EntityMeta meta) {
        List<String> definitions = new ArrayList<>();
        definitions.add(meta.getIdColumnName() + " " + sqliteType(meta.getIdField().getType()) + " PRIMARY KEY");

        for (Map.Entry<String, Field> entry : meta.getColumns().entrySet()) {
            definitions.add(entry.getKey() + " " + sqliteType(entry.getValue().getType()));
        }

        return "CREATE TABLE IF NOT EXISTS " + meta.getTableName() + " (" + String.join(", ", definitions) + ")";
    }

    public String insert(EntityMeta meta) {
        Map<String, Field> all = meta.getAllColumns();
        StringJoiner columns = new StringJoiner(", ");
        StringJoiner placeholders = new StringJoiner(", ");

        for (String columnName : all.keySet()) {
            columns.add(columnName);
            placeholders.add("?");
        }

        return "INSERT OR REPLACE INTO " + meta.getTableName() + "(" + columns + ") VALUES (" + placeholders + ")";
    }

    public String update(EntityMeta meta) {
        StringJoiner assignments = new StringJoiner(", ");
        for (String columnName : meta.getColumns().keySet()) {
            assignments.add(columnName + " = ?");
        }
        return "UPDATE " + meta.getTableName() + " SET " + assignments + " WHERE " + meta.getIdColumnName() + " = ?";
    }

    public String selectById(EntityMeta meta) {
        return "SELECT * FROM " + meta.getTableName() + " WHERE " + meta.getIdColumnName() + " = ?";
    }

    public String delete(EntityMeta meta) {
        return "DELETE FROM " + meta.getTableName() + " WHERE " + meta.getIdColumnName() + " = ?";
    }

    public String selectAll(EntityMeta meta) {
        return "SELECT * FROM " + meta.getTableName();
    }

    private String sqliteType(Class<?> type) {
        if (type == String.class || type == java.util.UUID.class) {
            return "TEXT";
        }
        if (type == int.class || type == Integer.class || type == long.class || type == Long.class
            || type == boolean.class || type == Boolean.class) {
            return "INTEGER";
        }
        if (type == double.class || type == Double.class) {
            return "REAL";
        }
        throw new IllegalArgumentException("Unsupported field type for SQLite: " + type.getName());
    }
}
