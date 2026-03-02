package me.penguinx13.wapi.commands.core.pipeline;

import me.penguinx13.wapi.commands.core.result.CommandResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class MiddlewareChain {
    @FunctionalInterface
    public interface TerminalInvoker {
        CompletionStage<CommandResult> invoke(CommandInvocation invocation);
    }

    private final List<CommandMiddleware> middleware;
    private final TerminalInvoker terminalInvoker;
    private final int index;

    public MiddlewareChain(List<CommandMiddleware> middleware, TerminalInvoker terminalInvoker) {
        this(middleware, terminalInvoker, 0);
    }

    private MiddlewareChain(List<CommandMiddleware> middleware, TerminalInvoker terminalInvoker, int index) {
        this.middleware = middleware;
        this.terminalInvoker = terminalInvoker;
        this.index = index;
    }

    public CompletionStage<CommandResult> next(CommandInvocation invocation) {
        if (invocation.state().cancelled()) {
            return CompletableFuture.completedFuture(CommandResult.failure("Execution cancelled."));
        }
        if (index >= middleware.size()) {
            return terminalInvoker.invoke(invocation);
        }
        return middleware.get(index).handle(invocation, new MiddlewareChain(middleware, terminalInvoker, index + 1));
    }
}
