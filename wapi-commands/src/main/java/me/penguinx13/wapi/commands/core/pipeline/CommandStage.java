package me.penguinx13.wapi.commands.core.pipeline;

import me.penguinx13.wapi.commands.core.context.CommandContext;
import me.penguinx13.wapi.commands.core.context.ExecutionState;

import java.util.concurrent.CompletionStage;

public interface CommandStage {
    CompletionStage<StageResult> execute(CommandContext context, ExecutionState state);
}
