package me.penguinx13.wapi.showcase.core.middleware;

import me.penguinx13.wapi.commands.core.error.InfrastructureException;
import me.penguinx13.wapi.commands.core.pipeline.CommandInvocation;
import me.penguinx13.wapi.commands.core.pipeline.CommandMiddleware;
import me.penguinx13.wapi.commands.core.pipeline.MiddlewareChain;
import me.penguinx13.wapi.commands.core.result.CommandResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

public final class ErrorMappingMiddleware implements CommandMiddleware {
    @Override
    public CompletionStage<CommandResult> handle(CommandInvocation invocation, MiddlewareChain chain) {
        CompletableFuture<CommandResult> mapped = new CompletableFuture<>();
        chain.next(invocation).whenComplete((result, throwable) -> {
            if (throwable == null) {
                mapped.complete(result);
                return;
            }
            Throwable cause = throwable instanceof CompletionException ce && ce.getCause() != null ? ce.getCause() : throwable;
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            mapped.completeExceptionally(new InfrastructureException("Mapped middleware exception", cause));
        });
        return mapped;
    }
}
