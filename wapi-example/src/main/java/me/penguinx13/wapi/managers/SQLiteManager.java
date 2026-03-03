package me.penguinx13.wapi.managers;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.*;
import java.util.concurrent.*;

public final class SQLiteManager {

    @FunctionalInterface
    public interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    private final Plugin plugin;
    private final String databaseFileName;
    private final ExecutorService dbExecutor;
    private volatile boolean closed;

    public SQLiteManager(Plugin plugin, String databaseFileName) {
        this.plugin = plugin;
        this.databaseFileName = databaseFileName;
        this.dbExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "wapi-sqlite");
            t.setDaemon(true);
            return t;
        });
    }

    public CompletionStage<Integer> update(String sql, Object... params) {
        ensureNotMainThread("update");
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = openConnection();
                 PreparedStatement statement = prepare(connection, sql, params)) {
                return statement.executeUpdate();
            } catch (SQLException ex) {
                throw new CompletionException(new IllegalStateException("Cannot execute update", ex));
            }
        }, dbExecutor);
    }

    public <T> CompletionStage<T> query(String sql, Object[] params, ResultSetMapper<T> mapper) {
        ensureNotMainThread("query");
        return CompletableFuture.supplyAsync(() -> {
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
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            throw new IllegalStateException("Cannot create plugin data folder");
        }
        File dbFile = new File(plugin.getDataFolder(), databaseFileName);
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
        if (Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("SQLite " + operation + " must not run on the main thread.");
        }
    }
}
