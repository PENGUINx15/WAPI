package me.penguinx13.wapi.commands.paper.platform;

import me.penguinx13.wapi.commands.core.platform.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public final class PaperScheduler implements Scheduler {
    private final Plugin plugin;
    private final ExecutorService asyncExecutor;

    public PaperScheduler(Plugin plugin) {
        this.plugin = plugin;
        this.asyncExecutor = Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors() / 2), r -> {
            Thread t = new Thread(r, "wapi-command-async");
            t.setDaemon(true);
            return t;
        });
    }

    public void runSync(Runnable runnable) { Bukkit.getScheduler().runTask(plugin, runnable); }
    public CompletableFuture<Void> runAsync(Runnable runnable) { return CompletableFuture.runAsync(runnable, asyncExecutor); }
    public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) { return CompletableFuture.supplyAsync(supplier, asyncExecutor); }
    public boolean isPrimaryThread() { return Bukkit.isPrimaryThread(); }

    public void shutdown() {
        asyncExecutor.shutdown();
    }
}
