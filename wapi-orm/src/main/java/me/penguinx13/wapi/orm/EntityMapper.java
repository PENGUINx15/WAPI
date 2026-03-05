package me.penguinx13.wapi.orm;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public final class EntityMapper {

    public <T> T map(ResultSet resultSet, EntityMeta meta, Class<T> entityType) throws SQLException {
        try {
            Constructor<T> constructor = entityType.getDeclaredConstructor();
            constructor.setAccessible(true);
            T entity = constructor.newInstance();

            setFieldValue(entity, meta.getIdField(), readValue(resultSet, meta.getIdColumnName(), meta.getIdField().getType()));
            for (Map.Entry<String, Field> entry : meta.getColumns().entrySet()) {
                Object value = readValue(resultSet, entry.getKey(), entry.getValue().getType());
                setFieldValue(entity, entry.getValue(), value);
            }
            return entity;
        } catch (ReflectiveOperationException ex) {
            throw new SQLException("Failed to map result set to entity " + entityType.getName(), ex);
        }
    }

    public void bindInsert(PreparedStatement statement, EntityMeta meta, Object entity) throws SQLException {
        try {
            int parameterIndex = 1;
            for (Field field : meta.getAllColumns().values()) {
                writeValue(statement, parameterIndex++, field.get(entity), field.getType());
            }
        } catch (IllegalAccessException ex) {
            throw new SQLException("Failed to bind entity for insert", ex);
        }
    }

    public void bindUpdate(PreparedStatement statement, EntityMeta meta, Object entity) throws SQLException {
        try {
            int parameterIndex = 1;
            for (Field field : meta.getColumns().values()) {
                writeValue(statement, parameterIndex++, field.get(entity), field.getType());
            }
            writeValue(statement, parameterIndex, meta.getIdField().get(entity), meta.getIdField().getType());
        } catch (IllegalAccessException ex) {
            throw new SQLException("Failed to bind entity for update", ex);
        }
    }

    public Object getIdValue(EntityMeta meta, Object entity) {
        try {
            return meta.getIdField().get(entity);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("Failed to read id value", ex);
        }
    }

    private Object readValue(ResultSet resultSet, String columnName, Class<?> type) throws SQLException {
        if (type == UUID.class) {
            String value = resultSet.getString(columnName);
            if (value == null) {
                return null;
            }
            return UUID.fromString(value);
        }
        if (type == String.class) {
            return resultSet.getString(columnName);
        }
        if (type == int.class || type == Integer.class) {
            int value = resultSet.getInt(columnName);
            if (resultSet.wasNull() && type == Integer.class) {
                return null;
            }
            return value;
        }
        if (type == long.class || type == Long.class) {
            long value = resultSet.getLong(columnName);
            if (resultSet.wasNull() && type == Long.class) {
                return null;
            }
            return value;
        }
        if (type == boolean.class || type == Boolean.class) {
            boolean value = resultSet.getBoolean(columnName);
            if (resultSet.wasNull() && type == Boolean.class) {
                return null;
            }
            return value;
        }
        if (type == double.class || type == Double.class) {
            double value = resultSet.getDouble(columnName);
            if (resultSet.wasNull() && type == Double.class) {
                return null;
            }
            return value;
        }
        throw new SQLException("Unsupported field type: " + type.getName());
    }

    private void writeValue(PreparedStatement statement, int parameterIndex, Object value, Class<?> type) throws SQLException {
        if (value == null) {
            statement.setObject(parameterIndex, null);
            return;
        }
        if (type == UUID.class) {
            statement.setString(parameterIndex, value.toString());
            return;
        }
        if (type == String.class) {
            statement.setString(parameterIndex, (String) value);
            return;
        }
        if (type == int.class || type == Integer.class) {
            statement.setInt(parameterIndex, ((Number) value).intValue());
            return;
        }
        if (type == long.class || type == Long.class) {
            statement.setLong(parameterIndex, ((Number) value).longValue());
            return;
        }
        if (type == boolean.class || type == Boolean.class) {
            statement.setBoolean(parameterIndex, (Boolean) value);
            return;
        }
        if (type == double.class || type == Double.class) {
            statement.setDouble(parameterIndex, ((Number) value).doubleValue());
            return;
        }
        throw new SQLException("Unsupported field type: " + type.getName());
    }

    private void setFieldValue(Object entity, Field field, Object value) throws IllegalAccessException {
        field.set(entity, value);
    }
}
