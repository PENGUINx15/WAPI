package me.penguinx13.wapi.Managers;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Простая обёртка для работы с SQLite в рамках Bukkit/Paper плагина.
 *
 * <p>Использование:</p>
 * <ol>
 *   <li>Создать экземпляр {@code new SQLiteManager(plugin, "file.db")}.</li>
 *   <li>Вызвать {@link #connect()} в {@code onEnable()}.</li>
 *   <li>Выполнять SQL через {@link #executeUpdate(String)} /
 *       {@link #executeUpdate(String, Object...)} / {@link #prepareStatement(String, Object...)}.</li>
 *   <li>Вызвать {@link #disconnect()} в {@code onDisable()}.</li>
 * </ol>
 */
public class SQLiteManager {

    private final Plugin plugin;
    private final String databaseFileName;
    private Connection connection;

    public SQLiteManager(Plugin plugin, String databaseFileName) {
        this.plugin = plugin;
        this.databaseFileName = databaseFileName;
    }

    public void connect() {
        if (isConnected()) {
            return;
        }

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

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException ex) {
            return false;
        }
    }

    public void disconnect() {
        if (!isConnected()) {
            return;
        }

        try {
            connection.close();
        } catch (SQLException ex) {
            throw new IllegalStateException("Cannot close SQLite connection", ex);
        }
    }

    public Connection getConnection() {
        connect();
        return connection;
    }

    public void executeUpdate(String sql) {
        try (Statement statement = getConnection().createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException ex) {
            throw new IllegalStateException("Cannot execute update", ex);
        }
    }

    public int executeUpdate(String sql, Object... params) {
        try (PreparedStatement statement = prepareStatement(sql, params)) {
            return statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Cannot execute prepared update", ex);
        }
    }

    public ResultSet executeQuery(String sql) {
        try {
            return getConnection().createStatement().executeQuery(sql);
        } catch (SQLException ex) {
            throw new IllegalStateException("Cannot execute query", ex);
        }
    }

    public PreparedStatement prepareStatement(String sql, Object... params) {
        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);

            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }

            return statement;
        } catch (SQLException ex) {
            throw new IllegalStateException("Cannot prepare statement", ex);
        }
    }
}
