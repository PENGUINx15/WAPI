package me.penguinx13.wapi.commands.paper.error;

import me.penguinx13.wapi.commands.core.context.CommandContext;
import me.penguinx13.wapi.commands.core.context.ExecutionState;
import me.penguinx13.wapi.commands.core.error.AuthorizationException;
import me.penguinx13.wapi.commands.core.error.CommandException;
import me.penguinx13.wapi.commands.core.error.CooldownException;
import me.penguinx13.wapi.commands.core.error.ErrorPresenter;
import me.penguinx13.wapi.commands.core.error.UserInputException;
import me.penguinx13.wapi.commands.core.error.ValidationException;
import me.penguinx13.wapi.commands.core.platform.FrameworkLogger;
import me.penguinx13.wapi.commands.core.result.CommandResult;

import java.util.Locale;

public final class DefaultErrorPresenter implements ErrorPresenter {
    private final FrameworkLogger logger;

    public DefaultErrorPresenter(FrameworkLogger logger) {
        this.logger = logger;
    }

    @Override
    public CommandResult present(CommandContext context, ExecutionState state, CommandException error, Locale locale) {
        if (error instanceof UserInputException
                || error instanceof AuthorizationException
                || error instanceof ValidationException
                || error instanceof CooldownException) {
            return CommandResult.failure(error.getMessage());
        }
        logger.error("Infrastructure failure for correlationId=" + state.correlationId(), error);
        return CommandResult.failure("An internal error occurred.");
    }
}
