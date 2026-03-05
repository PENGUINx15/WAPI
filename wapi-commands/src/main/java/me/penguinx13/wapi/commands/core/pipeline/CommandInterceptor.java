package me.penguinx13.wapi.commands.core.pipeline;

import me.penguinx13.wapi.commands.core.context.CommandContext;

public interface CommandInterceptor {
    default CommandContext beforeStage(String stageName, CommandContext context) { return context; }
    default CommandContext afterStage(String stageName, CommandContext context) { return context; }
}
