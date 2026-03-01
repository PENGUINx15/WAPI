package me.penguinx13.wapi.commandframework.error;

import me.penguinx13.wapi.commandframework.exception.CommandException;
import org.bukkit.command.CommandSender;

public interface CommandErrorHandler {
    void handle(CommandSender sender, CommandException exception);
}
