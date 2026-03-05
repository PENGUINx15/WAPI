package me.penguinx13.wapi.orm;

import me.penguinx13.wapi.managers.SQLiteManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public final class SQLiteRepository<T, ID> implements Repository<T, ID> {

    private final SQLiteManager sqliteManager;
    private final EntityMeta meta;
    private final SqlGenerator sqlGenerator;
    private final EntityMapper entityMapper;
    private final Class<T> entityType;
    private final Map<ID, T> cache;
    private final boolean cacheEnabled;
    private final Executor asyncExecutor;

    public SQLiteRepository(
        SQLiteManager sqliteManager,
        Class<T> entityType,
        EntityMeta meta,
        SqlGenerator sqlGenerator,
        EntityMapper entityMapper,
        boolean cacheEnabled,
        Executor asyncExecutor
    ) {
        this.sqliteManager = sqliteManager;
        this.entityType = entityType;
        this.meta = meta;
        this.sqlGenerator = sqlGenerator;
        this.entityMapper = entityMapper;
        this.cacheEnabled = cacheEnabled;
        this.asyncExecutor = asyncExecutor;
        this.cache = new ConcurrentHashMap<>();
    }

    public void init() {
        executeUpdate(sqlGenerator.createTable(meta));
    }

    @Override
    public Optional<T> findById(ID id) {
        if (cacheEnabled) {
            T cached = cache.get(id);
            if (cached != null) {
                return Optional.of(cached);
            }
        }

        Optional<T> result = executeQuery(
            sqlGenerator.selectById(meta),
            new Object[]{id},
            rs -> {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(entityMapper.map(rs, meta, entityType));
            }
        );

        if (cacheEnabled && result.isPresent()) {
            cache.put(id, result.get());
        }
        return result;
    }

    @Override
    public CompletableFuture<Optional<T>> findByIdAsync(ID id) {
        return CompletableFuture.supplyAsync(() -> findById(id), asyncExecutor);
    }

    @Override
    public void save(T entity) {
        executeUpdate(sqlGenerator.insert(meta), buildInsertParameters(entity));
        if (cacheEnabled) {
            ID id = extractId(entity);
            cache.put(id, entity);
        }
    }

    @Override
    public CompletableFuture<Void> saveAsync(T entity) {
        return CompletableFuture.supplyAsync(() -> {
            save(entity);
            return null;
        }, asyncExecutor);
    }

    @Override
    public void delete(ID id) {
        executeUpdate(sqlGenerator.delete(meta), id);
        if (cacheEnabled) {
            cache.remove(id);
        }
    }

    @Override
    public List<T> findAll() {
        return executeQuery(sqlGenerator.selectAll(meta), new Object[0], rs -> {
            List<T> entities = new ArrayList<>();
            while (rs.next()) {
                entities.add(entityMapper.map(rs, meta, entityType));
            }
            return entities;
        });
    }

    private Object[] buildInsertParameters(T entity) {
        Map<String, java.lang.reflect.Field> allColumns = meta.getAllColumns();
        Object[] params = new Object[allColumns.size()];
        int index = 0;
        for (java.lang.reflect.Field field : allColumns.values()) {
            params[index++] = readField(field, entity);
        }
        return params;
    }

    @SuppressWarnings("unchecked")
    private ID extractId(T entity) {
        return (ID) entityMapper.getIdValue(meta, entity);
    }

    private Object readField(java.lang.reflect.Field field, T entity) {
        try {
            Object value = field.get(entity);
            if (value instanceof java.util.UUID) {
                return value.toString();
            }
            return value;
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("Cannot read entity field: " + field.getName(), ex);
        }
    }

    private void executeUpdate(String sql, Object... params) {
        try {
            sqliteManager.update(sql, params).toCompletableFuture().join();
        } catch (CompletionException ex) {
            throw unwrap(ex);
        }
    }

    private <R> R executeQuery(String sql, Object[] params, SQLiteManager.ResultSetMapper<R> mapper) {
        try {
            return sqliteManager.query(sql, params, mapper).toCompletableFuture().join();
        } catch (CompletionException ex) {
            throw unwrap(ex);
        }
    }

    private RuntimeException unwrap(CompletionException ex) {
        if (ex.getCause() instanceof RuntimeException) {
            return (RuntimeException) ex.getCause();
        }
        return new IllegalStateException("SQLite repository operation failed", ex);
    }
}
