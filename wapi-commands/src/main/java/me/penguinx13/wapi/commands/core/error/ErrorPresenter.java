package me.penguinx13.wapi.commands.core.error;

import me.penguinx13.wapi.commands.core.context.CommandContext;
import me.penguinx13.wapi.commands.core.context.ExecutionState;
import me.penguinx13.wapi.commands.core.result.CommandResult;

import java.util.Locale;

public interface ErrorPresenter {
    CommandResult present(CommandContext context, ExecutionState state, CommandException error, Locale locale);
}
