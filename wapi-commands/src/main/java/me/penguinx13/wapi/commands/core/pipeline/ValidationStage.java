package me.penguinx13.wapi.commands.core.pipeline;

import me.penguinx13.wapi.commands.core.context.CommandContext;
import me.penguinx13.wapi.commands.core.context.ExecutionState;
import me.penguinx13.wapi.commands.core.runtime.CommandRuntime;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class ValidationStage implements CommandStage {
    @Override
    public CompletionStage<StageResult> execute(CommandContext context, ExecutionState state) {
        CommandRuntime runtime = context.service(CommandRuntime.class);
        var method = context.command().orElseThrow();
        for (var arg : method.metadata().arguments()) {
            Object value = context.parsedArguments().get(arg.name());
            runtime.validationService().validate(arg, value);
        }
        return CompletableFuture.completedFuture(StageResult.next(context));
    }
}
