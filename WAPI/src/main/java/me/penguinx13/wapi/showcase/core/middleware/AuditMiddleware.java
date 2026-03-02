package me.penguinx13.wapi.showcase.core.middleware;

import me.penguinx13.wapi.commands.core.pipeline.CommandInvocation;
import me.penguinx13.wapi.commands.core.pipeline.CommandMiddleware;
import me.penguinx13.wapi.commands.core.pipeline.MiddlewareChain;
import me.penguinx13.wapi.commands.core.result.CommandResult;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletionStage;

public final class AuditMiddleware implements CommandMiddleware {
    private final Plugin plugin;

    public AuditMiddleware(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletionStage<CommandResult> handle(CommandInvocation invocation, MiddlewareChain chain) {
        return chain.next(invocation).thenApply(result -> {
            String commandName = invocation.context().command().map(c -> String.join(" ", c.metadata().path())).orElse("unknown");
            plugin.getLogger().info("AUDIT sender=" + invocation.context().sender().name() + " command=" + commandName + " success=" + result.success());
            return result;
        });
    }
}
