package me.penguinx13.wapi.commands.core.spi;

import me.penguinx13.wapi.commands.core.context.CommandContext;
import me.penguinx13.wapi.commands.core.result.CommandResult;

public interface MetricsSink {
    void onExecutionFinished(CommandContext context, CommandResult result, long nanos);
}
