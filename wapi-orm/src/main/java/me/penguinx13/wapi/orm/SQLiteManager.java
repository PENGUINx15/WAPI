package me.penguinx13.wapi.orm;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BooleanSupplier;

public final class SQLiteManager {

    @FunctionalInterface
    public interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    private final File dataFolder;
    private final String databaseFileName;
    private final BooleanSupplier primaryThreadCheck;
    private final ExecutorService dbExecutor;
    private volatile boolean closed;

    public SQLiteManager(File dataFolder, String databaseFileName) {
        this(dataFolder, databaseFileName, () -> false);
    }

    public SQLiteManager(File dataFolder, String databaseFileName, BooleanSupplier primaryThreadCheck) {
        this.dataFolder = dataFolder;
        this.databaseFileName = databaseFileName;
        this.primaryThreadCheck = primaryThreadCheck;
        this.dbExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "wapi-sqlite");
            t.setDaemon(true);
            return t;
        });
    }

    public CompletionStage<Integer> update(String sql, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            ensureNotMainThread("update");
            try (Connection connection = openConnection();
                 PreparedStatement statement = prepare(connection, sql, params)) {
                return statement.executeUpdate();
            } catch (SQLException ex) {
                throw new CompletionException(new IllegalStateException("Cannot execute update", ex));
            }
        }, dbExecutor);
    }

    public <T> CompletionStage<T> query(String sql, Object[] params, ResultSetMapper<T> mapper) {
        return CompletableFuture.supplyAsync(() -> {
            ensureNotMainThread("query");
            try (Connection connection = openConnection();
                 PreparedStatement statement = prepare(connection, sql, params);
                 ResultSet rs = statement.executeQuery()) {
                return mapper.map(rs);
            } catch (SQLException ex) {
                throw new CompletionException(new IllegalStateException("Cannot execute query", ex));
            }
        }, dbExecutor);
    }

    public void shutdown() {
        closed = true;
        dbExecutor.shutdown();
    }

    private Connection openConnection() throws SQLException {
        if (closed) {
            throw new IllegalStateException("SQLiteManager is closed");
        }
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new IllegalStateException("Cannot create plugin data folder");
        }
        File dbFile = new File(dataFolder, databaseFileName);
        return DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
    }

    private PreparedStatement prepare(Connection connection, String sql, Object... params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
        return statement;
    }

    private void ensureNotMainThread(String operation) {
        if (primaryThreadCheck.getAsBoolean()) {
            throw new IllegalStateException("SQLite " + operation + " must not run on the main thread.");
        }
    }
}
