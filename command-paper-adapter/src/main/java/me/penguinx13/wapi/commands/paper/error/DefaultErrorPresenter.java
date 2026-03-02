package me.penguinx13.wapi.commands.paper.error;

import me.penguinx13.wapi.commands.core.context.CommandContext;
import me.penguinx13.wapi.commands.core.error.*;

public final class DefaultErrorPresenter implements ErrorPresenter {
    @Override
    public void present(CommandContext context, Throwable error) {
        if (error instanceof UserInputException || error instanceof AuthorizationException) {
            context.sender().sendMessage("§c" + error.getMessage());
            return;
        }
        context.sender().sendMessage("§cAn internal error occurred.");
    }
}
