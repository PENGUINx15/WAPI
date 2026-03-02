package me.penguinx13.wapi.showcase.core.middleware;

import me.penguinx13.wapi.commands.core.error.CooldownException;
import me.penguinx13.wapi.commands.core.pipeline.CommandInvocation;
import me.penguinx13.wapi.commands.core.pipeline.CommandMiddleware;
import me.penguinx13.wapi.commands.core.pipeline.MiddlewareChain;
import me.penguinx13.wapi.commands.core.result.CommandResult;
import me.penguinx13.wapi.showcase.core.service.EnterpriseCooldownService;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class DeleteUserCooldownMiddleware implements CommandMiddleware {
    private final EnterpriseCooldownService cooldownService;

    public DeleteUserCooldownMiddleware(EnterpriseCooldownService cooldownService) {
        this.cooldownService = cooldownService;
    }

    @Override
    public CompletionStage<CommandResult> handle(CommandInvocation invocation, MiddlewareChain chain) {
        var commandPath = invocation.context().command().map(c -> String.join(" ", c.metadata().path())).orElse("");
        if (!"user delete".equalsIgnoreCase(commandPath)) {
            return chain.next(invocation);
        }

        String key = invocation.context().sender().uniqueId() + ":" + commandPath;
        long remaining = cooldownService.remainingMillis(key);
        if (remaining > 0) {
            return CompletableFuture.failedStage(new CooldownException("Delete command cooldown: " + (remaining / 1000.0) + "s"));
        }

        cooldownService.mark(key, Duration.ofSeconds(5));
        return chain.next(invocation);
    }
}
