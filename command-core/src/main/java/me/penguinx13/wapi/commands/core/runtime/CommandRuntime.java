package me.penguinx13.wapi.commands.core.runtime;

import me.penguinx13.wapi.commands.core.context.CommandContext;
import me.penguinx13.wapi.commands.core.context.ExecutionState;
import me.penguinx13.wapi.commands.core.error.CommandException;
import me.penguinx13.wapi.commands.core.error.ErrorPresenter;
import me.penguinx13.wapi.commands.core.error.InfrastructureException;
import me.penguinx13.wapi.commands.core.pipeline.*;
import me.penguinx13.wapi.commands.core.resolver.ResolverRegistry;
import me.penguinx13.wapi.commands.core.result.CommandResult;
import me.penguinx13.wapi.commands.core.spi.MetricsSink;
import me.penguinx13.wapi.commands.core.spi.PlatformBridge;
import me.penguinx13.wapi.commands.core.tree.CommandTree;
import me.penguinx13.wapi.commands.core.validation.ValidationService;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class CommandRuntime {
    private final CommandTree tree;
    private final CommandPipeline pipeline;
    private final ResolverRegistry resolverRegistry;
    private final ValidationService validationService;
    private final ErrorPresenter errorPresenter;
    private final List<CommandMiddleware> middleware;
    private final PlatformBridge platformBridge;
    private final MetricsSink metricsSink;

    public CommandRuntime(CommandTree tree,
                          CommandPipeline pipeline,
                          ResolverRegistry resolverRegistry,
                          ValidationService validationService,
                          ErrorPresenter errorPresenter,
                          List<CommandMiddleware> middleware,
                          PlatformBridge platformBridge,
                          MetricsSink metricsSink) {
        this.tree = tree;
        this.pipeline = pipeline;
        this.resolverRegistry = resolverRegistry;
        this.validationService = validationService;
        this.errorPresenter = errorPresenter;
        this.middleware = List.copyOf(middleware);
        this.platformBridge = platformBridge;
        this.metricsSink = metricsSink;
    }

    public CommandTree tree() { return tree; }
    public ResolverRegistry resolverRegistry() { return resolverRegistry; }
    public ValidationService validationService() { return validationService; }
    public PlatformBridge platformBridge() { return platformBridge; }

    public CompletionStage<CommandResult> execute(CommandContext context, ExecutionState state) {
        long start = System.nanoTime();
        MiddlewareChain chain = new MiddlewareChain(middleware, invocation -> pipeline.execute(invocation.context(), invocation.state()));

        return chain.next(new CommandInvocation(context, state))
                .handle((result, throwable) -> {
                    CommandResult finalResult;
                    if (throwable == null) {
                        finalResult = result;
                    } else {
                        Throwable cause = throwable instanceof java.util.concurrent.CompletionException ce ? ce.getCause() : throwable;
                        if (cause instanceof Error error) throw error;
                        CommandException wrapped = cause instanceof CommandException commandException
                                ? commandException
                                : new InfrastructureException("Unhandled command failure", cause);
                        finalResult = errorPresenter.present(context, state, wrapped, Locale.ENGLISH);
                    }
                    metricsSink.onExecutionFinished(context, finalResult, System.nanoTime() - start);
                    return finalResult;
                });
    }

    public CompletionStage<Void> executeAndRespond(CommandContext context, ExecutionState state) {
        return execute(context, state).thenCompose(result -> platformBridge.deliverResult(context, result));
    }
}
