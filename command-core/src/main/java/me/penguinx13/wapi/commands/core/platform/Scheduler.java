package me.penguinx13.wapi.commands.core.platform;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface Scheduler {
    void runSync(Runnable runnable);
    CompletableFuture<Void> runAsync(Runnable runnable);
    <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier);
    boolean isPrimaryThread();
}
