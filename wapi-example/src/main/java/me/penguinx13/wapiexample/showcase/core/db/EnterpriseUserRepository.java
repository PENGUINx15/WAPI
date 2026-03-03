package me.penguinx13.wapiexample.showcase.core.db;

import me.penguinx13.wapi.commands.core.error.InfrastructureException;
import me.penguinx13.wapiexample.showcase.core.model.EnterpriseUser;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public final class EnterpriseUserRepository {
    private final Plugin plugin;
    private final String databaseFile;
    private final ExecutorService dbExecutor;

    public EnterpriseUserRepository(Plugin plugin, String databaseFile) {
        this.plugin = plugin;
        this.databaseFile = databaseFile;
        this.dbExecutor = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "enterprise-sqlite");
            t.setDaemon(true);
            return t;
        });
    }

    public CompletionStage<Void> initializeSchema() {
        String sql = "CREATE TABLE IF NOT EXISTS users (name TEXT PRIMARY KEY, age INTEGER NOT NULL)";
        return runUpdate(sql).thenApply(ignored -> null);
    }

    public CompletionStage<Integer> create(String name, int age) {
        return runUpdate("INSERT OR REPLACE INTO users(name, age) VALUES (?, ?)", name, age);
    }

    public CompletionStage<Optional<EnterpriseUser>> findByName(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = openConnection();
                 PreparedStatement statement = prepare(connection, "SELECT name, age FROM users WHERE name = ?", name);
                 ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new EnterpriseUser(rs.getString("name"), rs.getInt("age")));
            } catch (SQLException ex) {
                throw new CompletionException(new InfrastructureException("Failed to fetch user", ex));
            }
        }, dbExecutor);
    }

    public CompletionStage<Integer> deleteByName(String name) {
        return runUpdate("DELETE FROM users WHERE name = ?", name);
    }

    public CompletionStage<List<String>> suggestNames(String prefix, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> names = new ArrayList<>();
            try (Connection connection = openConnection();
                 PreparedStatement statement = prepare(connection,
                         "SELECT name FROM users WHERE lower(name) LIKE ? ORDER BY name ASC LIMIT ?",
                         prefix.toLowerCase() + "%", limit);
                 ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    names.add(rs.getString("name"));
                }
                return names;
            } catch (SQLException ex) {
                throw new CompletionException(new InfrastructureException("Failed to suggest users", ex));
            }
        }, dbExecutor);
    }

    public void shutdown() {
        dbExecutor.shutdown();
    }

    private CompletionStage<Integer> runUpdate(String sql, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = openConnection();
                 PreparedStatement statement = prepare(connection, sql, params)) {
                return statement.executeUpdate();
            } catch (SQLException ex) {
                throw new CompletionException(new InfrastructureException("Database write failure", ex));
            }
        }, dbExecutor);
    }

    private Connection openConnection() throws SQLException {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            throw new SQLException("Cannot create data folder");
        }
        File db = new File(plugin.getDataFolder(), databaseFile);
        return DriverManager.getConnection("jdbc:sqlite:" + db.getAbsolutePath());
    }

    private PreparedStatement prepare(Connection connection, String sql, Object... params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
        return statement;
    }
}
