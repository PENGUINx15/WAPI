package me.penguinx13.wapi.commands.core.pipeline;

import me.penguinx13.wapi.commands.core.context.CommandContext;
import me.penguinx13.wapi.commands.core.result.CommandResult;

public sealed interface StageResult permits StageResult.Continue, StageResult.Stop {
    record Continue(CommandContext context) implements StageResult {}
    record Stop(CommandResult result) implements StageResult {}

    static Continue next(CommandContext context) { return new Continue(context); }
    static Stop stop(CommandResult result) { return new Stop(result); }
}
