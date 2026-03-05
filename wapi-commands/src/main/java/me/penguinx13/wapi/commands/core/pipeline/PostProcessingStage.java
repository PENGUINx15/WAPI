package me.penguinx13.wapi.commands.core.pipeline;

import me.penguinx13.wapi.commands.core.context.CommandContext;
import me.penguinx13.wapi.commands.core.context.ExecutionState;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class PostProcessingStage implements CommandStage {
    @Override
    public CompletionStage<StageResult> execute(CommandContext context, ExecutionState state) {
        return CompletableFuture.completedFuture(StageResult.next(context));
    }
}
