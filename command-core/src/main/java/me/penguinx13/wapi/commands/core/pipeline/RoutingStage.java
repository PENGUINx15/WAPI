package me.penguinx13.wapi.commands.core.pipeline;

import me.penguinx13.wapi.commands.core.context.CommandContext;
import me.penguinx13.wapi.commands.core.context.ExecutionState;
import me.penguinx13.wapi.commands.core.error.UserInputException;
import me.penguinx13.wapi.commands.core.runtime.CommandRuntime;
import me.penguinx13.wapi.commands.core.tree.RouteResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class RoutingStage implements CommandStage {
    @Override
    public CompletionStage<StageResult> execute(CommandContext context, ExecutionState state) {
        CommandRuntime runtime = context.service(CommandRuntime.class);
        RouteResult route = runtime.tree().route(context.tokens())
                .orElseThrow(() -> new UserInputException("Unknown command."));

        CommandContext routed = context.withRoute(
                route.command(),
                route.consumedPath(),
                route.capturedArguments()
        );
        return CompletableFuture.completedFuture(StageResult.next(routed));
    }
}
