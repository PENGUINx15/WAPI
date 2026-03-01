package me.penguinx13.wapi.commandframework.core;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class CommandExecutorAdapter implements CommandExecutor, TabCompleter {
    private final CommandRegistry commandRegistry;
    private final String rootCommand;

    public CommandExecutorAdapter(CommandRegistry commandRegistry, String rootCommand) {
        this.commandRegistry = commandRegistry;
        this.rootCommand = rootCommand;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        commandRegistry.execute(sender, rootCommand, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return commandRegistry.tabComplete(sender, rootCommand, args);
    }
}
