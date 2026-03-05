package me.penguinx13.wapi.commands.core.runtime;

import me.penguinx13.wapi.commands.core.context.CommandContext;
import me.penguinx13.wapi.commands.core.result.CommandResult;
import me.penguinx13.wapi.commands.core.spi.MetricsSink;

public final class NoopMetricsSink implements MetricsSink {
    @Override
    public void onExecutionFinished(CommandContext context, CommandResult result, long nanos) {
    }
}
