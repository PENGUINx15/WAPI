package me.penguinx13.wapi.managers;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.*;
import java.util.concurrent.CompletableFuture;

public class SQLiteManager {

    @FunctionalInterface
    public interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    private final Plugin plugin;
    private final String databaseFileName;
    private volatile Connection connection;

    public SQLiteManager(Plugin plugin, String databaseFileName) {
        this.plugin = plugin;
        this.databaseFileName = databaseFileName;
    }

    public synchronized void connect() {
        if (isConnected()) return;
        try {
            if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
                throw new IllegalStateException("Cannot create plugin data folder");
            }
            File databaseFile = new File(plugin.getDataFolder(), databaseFileName);
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
        } catch (SQLException ex) {
            throw new IllegalStateException("Cannot connect to SQLite database", ex);
        }
    }

    public synchronized void disconnect() {
        if (!isConnected()) return;
        try {
            connection.close();
            connection = null;
        } catch (SQLException ex) {
            throw new IllegalStateException("Cannot close SQLite connection", ex);
        }
    }

    public synchronized boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException ex) {
            return false;
        }
    }

    public CompletableFuture<Integer> executeUpdateAsync(String sql, Object... params) {
        assertNotPrimaryThread("executeUpdateAsync");
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement statement = prepareStatement(sql, params)) {
                return statement.executeUpdate();
            } catch (SQLException ex) {
                throw new IllegalStateException("Cannot execute update", ex);
            }
        });
    }

    public <T> CompletableFuture<T> query(String sql, Object[] params, ResultSetMapper<T> mapper) {
        assertNotPrimaryThread("query");
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement statement = prepareStatement(sql, params);
                 ResultSet rs = statement.executeQuery()) {
                return mapper.map(rs);
            } catch (SQLException ex) {
                throw new IllegalStateException("Cannot execute query", ex);
            }
        });
    }

    private PreparedStatement prepareStatement(String sql, Object... params) throws SQLException {
        connect();
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
        return statement;
    }

    private void assertNotPrimaryThread(String operation) {
        if (Bukkit.isPrimaryThread()) {
            plugin.getLogger().warning("SQLite " + operation + " invoked from main thread; call from async context.");
        }
    }
}
