package me.penguinx13.wapi.commands.paper.platform;

import me.penguinx13.wapi.commands.core.platform.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.*;
import java.util.function.Supplier;

public final class PaperScheduler implements Scheduler {
    private final Plugin plugin;
    public PaperScheduler(Plugin plugin) { this.plugin = plugin; }
    public void runSync(Runnable runnable) { Bukkit.getScheduler().runTask(plugin, runnable); }
    public CompletableFuture<Void> runAsync(Runnable runnable) { return CompletableFuture.runAsync(runnable); }
    public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) { return CompletableFuture.supplyAsync(supplier); }
    public boolean isPrimaryThread() { return Bukkit.isPrimaryThread(); }
}
