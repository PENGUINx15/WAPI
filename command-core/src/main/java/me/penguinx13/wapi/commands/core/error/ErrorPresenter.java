package me.penguinx13.wapi.commands.core.error;

import me.penguinx13.wapi.commands.core.context.CommandContext;

public interface ErrorPresenter {
    void present(CommandContext context, Throwable error);
}
