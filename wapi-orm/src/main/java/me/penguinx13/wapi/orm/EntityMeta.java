package me.penguinx13.wapi.orm;

import me.penguinx13.wapi.orm.annotations.Column;
import me.penguinx13.wapi.orm.annotations.Id;
import me.penguinx13.wapi.orm.annotations.Table;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class EntityMeta {

    private final Class<?> entityClass;
    private final String tableName;
    private final Field idField;
    private final String idColumnName;
    private final Map<String, Field> columns;

    private EntityMeta(Class<?> entityClass, String tableName, Field idField, String idColumnName, Map<String, Field> columns) {
        this.entityClass = entityClass;
        this.tableName = tableName;
        this.idField = idField;
        this.idColumnName = idColumnName;
        this.columns = Collections.unmodifiableMap(columns);
    }

    public static EntityMeta fromClass(Class<?> entityClass) {
        Table table = entityClass.getAnnotation(Table.class);
        if (table == null) {
            throw new IllegalArgumentException("Entity class must be annotated with @Table: " + entityClass.getName());
        }

        String tableName = resolveName(table.value(), table.name(), entityClass.getSimpleName().toLowerCase());
        Field idField = null;
        String idColumnName = null;
        Map<String, Field> columns = new LinkedHashMap<>();

        for (Field field : entityClass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }

            if (field.isAnnotationPresent(Id.class)) {
                if (idField != null) {
                    throw new IllegalArgumentException("Entity can only have one @Id field: " + entityClass.getName());
                }
                field.setAccessible(true);
                idField = field;
                Column column = field.getAnnotation(Column.class);
                idColumnName = resolveColumnName(column, field.getName());
                continue;
            }

            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                field.setAccessible(true);
                columns.put(resolveColumnName(column, field.getName()), field);
            }
        }

        if (idField == null || idColumnName == null) {
            throw new IllegalArgumentException("Entity must define one @Id field: " + entityClass.getName());
        }

        return new EntityMeta(entityClass, tableName, idField, idColumnName, columns);
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public String getTableName() {
        return tableName;
    }

    public Field getIdField() {
        return idField;
    }

    public String getIdColumnName() {
        return idColumnName;
    }

    public Map<String, Field> getColumns() {
        return columns;
    }

    public Map<String, Field> getAllColumns() {
        Map<String, Field> allColumns = new LinkedHashMap<>();
        allColumns.put(idColumnName, idField);
        allColumns.putAll(columns);
        return allColumns;
    }

    private static String resolveColumnName(Column column, String fallbackName) {
        if (column == null) {
            return fallbackName;
        }
        return resolveName(column.value(), column.name(), fallbackName);
    }

    private static String resolveName(String value, String name, String fallback) {
        if (!value.isBlank()) {
            return value;
        }
        if (!name.isBlank()) {
            return name;
        }
        return fallback;
    }
}
