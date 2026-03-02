package me.penguinx13.wapi.commands.core.pipeline;

import me.penguinx13.wapi.commands.core.context.CommandContext;

public final class ValidationStage implements CommandStage {
    @Override
    public CommandContext execute(CommandContext context) {
        return context;
    }
}
