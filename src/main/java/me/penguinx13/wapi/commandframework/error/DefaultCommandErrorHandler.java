package me.penguinx13.wapi.commandframework.error;

import me.penguinx13.wapi.commandframework.exception.CommandException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class DefaultCommandErrorHandler implements CommandErrorHandler {
    @Override
    public void handle(CommandSender sender, CommandException exception) {
        sender.sendMessage(ChatColor.RED + exception.getMessage());
    }
}
