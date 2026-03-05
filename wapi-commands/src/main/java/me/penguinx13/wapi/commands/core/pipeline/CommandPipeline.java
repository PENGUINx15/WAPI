package me.penguinx13.wapi.commands.core.pipeline;

import me.penguinx13.wapi.commands.core.context.CommandContext;
import me.penguinx13.wapi.commands.core.context.ExecutionState;
import me.penguinx13.wapi.commands.core.error.CommandException;
import me.penguinx13.wapi.commands.core.error.InfrastructureException;
import me.penguinx13.wapi.commands.core.result.CommandResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class CommandPipeline {
    private final List<CommandStage> stages;

    public CommandPipeline(List<CommandStage> stages) {
        this.stages = List.copyOf(stages);
    }

    public CompletionStage<CommandResult> execute(CommandContext initial, ExecutionState state) {
        return executeStage(0, initial, state);
    }

    private CompletionStage<CommandResult> executeStage(int index, CommandContext context, ExecutionState state) {
        if (state.cancelled()) {
            return CompletableFuture.completedFuture(CommandResult.failure("Execution cancelled."));
        }
        if (index >= stages.size()) {
            return CompletableFuture.completedFuture(CommandResult.emptySuccess());
        }

        CommandStage stage = stages.get(index);
        long started = System.nanoTime();
        try {
            return stage.execute(context, state)
                    .thenCompose(result -> {
                        state.recordStageTiming(stage.getClass().getSimpleName(), System.nanoTime() - started);
                        if (result instanceof StageResult.Stop stop) {
                            return CompletableFuture.completedFuture(stop.result());
                        }
                        return executeStage(index + 1, ((StageResult.Continue) result).context(), state);
                    });
        } catch (CommandException e) {
            return CompletableFuture.failedStage(e);
        } catch (Exception e) {
            return CompletableFuture.failedStage(new InfrastructureException("Pipeline stage failed", e));
        }
    }
}
