package me.penguinx13.wapi.commands.core.pipeline;

import me.penguinx13.wapi.commands.core.context.CommandContext;
import me.penguinx13.wapi.commands.core.context.ExecutionState;
import me.penguinx13.wapi.commands.core.error.InfrastructureException;
import me.penguinx13.wapi.commands.core.result.CommandResult;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class InvocationStage implements CommandStage {
    @Override
    public CompletionStage<StageResult> execute(CommandContext context, ExecutionState state) {
        var bound = context.command().orElseThrow();
        Object[] invokeArgs = new Object[bound.metadata().method().getParameterCount()];
        Set<Integer> explicitIndices = new HashSet<>();

        for (var argMeta : bound.metadata().arguments()) {
            invokeArgs[argMeta.index()] = context.parsedArguments().get(argMeta.name());
            explicitIndices.add(argMeta.index());
        }

        for (int i = 0; i < invokeArgs.length; i++) {
            if (!explicitIndices.contains(i)) {
                Class<?> paramType = bound.metadata().method().getParameterTypes()[i];
                if (paramType.isAssignableFrom(context.sender().platformSenderType())) {
                    invokeArgs[i] = context.sender().unwrap();
                }
            }
        }

        try {
            Object returned = bound.metadata().method().invoke(bound.instance(), invokeArgs);
            if (returned instanceof CompletionStage<?> asyncResult) {
                return asyncResult.thenApply(ignored -> StageResult.stop(CommandResult.emptySuccess()));
            }
            return CompletableFuture.completedFuture(StageResult.stop(CommandResult.emptySuccess()));
        } catch (IllegalAccessException | InvocationTargetException e) {
            Throwable cause = e instanceof InvocationTargetException ite && ite.getCause() != null ? ite.getCause() : e;
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new InfrastructureException("Command invocation failed", cause);
        }
    }
}
