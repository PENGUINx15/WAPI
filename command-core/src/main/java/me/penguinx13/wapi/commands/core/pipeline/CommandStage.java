package me.penguinx13.wapi.commands.core.pipeline;

import me.penguinx13.wapi.commands.core.context.CommandContext;

public interface CommandStage {
    CommandContext execute(CommandContext context);
}
