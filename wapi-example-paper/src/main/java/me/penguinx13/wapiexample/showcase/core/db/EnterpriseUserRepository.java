package me.penguinx13.wapiexample.showcase.core.db;

import me.penguinx13.wapi.commands.core.error.InfrastructureException;
import me.penguinx13.wapi.orm.SQLiteManager;
import me.penguinx13.wapiexample.showcase.core.model.EnterpriseUser;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

public final class EnterpriseUserRepository {
    private final SQLiteManager sqliteManager;

    public EnterpriseUserRepository(Plugin plugin, String databaseFile) {
        this.sqliteManager = new SQLiteManager(plugin.getDataFolder(), databaseFile, Bukkit::isPrimaryThread);
    }

    public CompletionStage<Void> initializeSchema() {
        String sql = "CREATE TABLE IF NOT EXISTS users (name TEXT PRIMARY KEY, age INTEGER NOT NULL)";
        return runUpdate(sql).thenApply(ignored -> null);
    }

    public CompletionStage<Integer> create(String name, int age) {
        return runUpdate("INSERT OR REPLACE INTO users(name, age) VALUES (?, ?)", name, age);
    }

    public CompletionStage<Optional<EnterpriseUser>> findByName(String name) {
        return sqliteManager.query("SELECT name, age FROM users WHERE name = ?", new Object[]{name}, rs -> {
            if (!rs.next()) {
                return Optional.empty();
            }
            return Optional.of(new EnterpriseUser(rs.getString("name"), rs.getInt("age")));
        }).exceptionallyCompose(this::mapFailure);
    }

    public CompletionStage<Integer> deleteByName(String name) {
        return runUpdate("DELETE FROM users WHERE name = ?", name);
    }

    public CompletionStage<List<String>> suggestNames(String prefix, int limit) {
        return sqliteManager.query(
                "SELECT name FROM users WHERE lower(name) LIKE ? ORDER BY name ASC LIMIT ?",
                new Object[]{prefix.toLowerCase() + "%", limit},
                rs -> {
                    List<String> names = new ArrayList<>();
                    while (rs.next()) {
                        names.add(rs.getString("name"));
                    }
                    return names;
                }
        ).exceptionallyCompose(this::mapFailure);
    }

    public void shutdown() {
        sqliteManager.shutdown();
    }

    private CompletionStage<Integer> runUpdate(String sql, Object... params) {
        return sqliteManager.update(sql, params).exceptionallyCompose(this::mapFailure);
    }

    private <T> CompletableFuture<T> mapFailure(Throwable throwable) {
        Throwable cause = throwable instanceof CompletionException && throwable.getCause() != null
                ? throwable.getCause()
                : throwable;
        CompletableFuture<T> failed = new CompletableFuture<>();
        failed.completeExceptionally(new InfrastructureException("Database operation failure", cause));
        return failed;
    }
}
