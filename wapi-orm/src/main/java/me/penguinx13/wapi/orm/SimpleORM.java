package me.penguinx13.wapi.orm;

import me.penguinx13.wapi.managers.SQLiteManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class SimpleORM {

    private final SQLiteManager sqliteManager;
    private final SqlGenerator sqlGenerator;
    private final EntityMapper entityMapper;
    private final Executor asyncExecutor;
    private final Map<Class<?>, Repository<?, ?>> repositories;

    public SimpleORM(SQLiteManager sqliteManager) {
        this(sqliteManager, true, Executors.newCachedThreadPool());
    }

    public SimpleORM(SQLiteManager sqliteManager, boolean cacheEnabled, Executor asyncExecutor) {
        this.sqliteManager = sqliteManager;
        this.sqlGenerator = new SqlGenerator();
        this.entityMapper = new EntityMapper();
        this.asyncExecutor = asyncExecutor;
        this.repositories = new ConcurrentHashMap<>();
        this.cacheEnabled = cacheEnabled;
    }

    private final boolean cacheEnabled;

    public <T> void registerEntity(Class<T> entityClass) {
        EntityMeta meta = EntityMeta.fromClass(entityClass);
        SQLiteRepository<T, Object> repository = new SQLiteRepository<>(
            sqliteManager,
            entityClass,
            meta,
            sqlGenerator,
            entityMapper,
            cacheEnabled,
            asyncExecutor
        );
        repository.init();
        repositories.put(entityClass, repository);
    }

    @SuppressWarnings("unchecked")
    public <T, ID> Repository<T, ID> getRepository(Class<T> entityClass) {
        Repository<?, ?> repository = repositories.get(entityClass);
        if (repository == null) {
            throw new IllegalStateException("Entity is not registered: " + entityClass.getName());
        }
        return (Repository<T, ID>) repository;
    }
}
