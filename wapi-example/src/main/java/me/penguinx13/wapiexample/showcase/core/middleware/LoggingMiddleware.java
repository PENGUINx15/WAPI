package me.penguinx13.wapiexample.showcase.core.middleware;

import me.penguinx13.wapi.commands.core.pipeline.CommandInvocation;
import me.penguinx13.wapi.commands.core.pipeline.CommandMiddleware;
import me.penguinx13.wapi.commands.core.pipeline.MiddlewareChain;
import me.penguinx13.wapi.commands.core.result.CommandResult;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletionStage;

public final class LoggingMiddleware implements CommandMiddleware {
    private final Plugin plugin;

    public LoggingMiddleware(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletionStage<CommandResult> handle(CommandInvocation invocation, MiddlewareChain chain) {
        long start = System.nanoTime();
        var state = invocation.state();
        plugin.getLogger().info("Command start correlationId=" + state.correlationId() + " input='" + invocation.context().rawInput() + "'");
        return chain.next(invocation).thenApply(result -> {
            long elapsed = System.nanoTime() - start;
            plugin.getLogger().info("Command finish correlationId=" + state.correlationId() + " latencyMs=" + (elapsed / 1_000_000.0));
            return result;
        });
    }
}
