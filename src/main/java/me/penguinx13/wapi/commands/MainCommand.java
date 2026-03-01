package me.penguinx13.wapi.commands;

import me.penguinx13.wapi.WAPI;
import me.penguinx13.wapi.commandframework.annotations.Arg;
import me.penguinx13.wapi.commandframework.annotations.Range;
import me.penguinx13.wapi.commandframework.annotations.RootCommand;
import me.penguinx13.wapi.commandframework.annotations.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

@RootCommand(value = "main", permission = "wapi.main")
public class MainCommand {
    private final WAPI plugin;

    public MainCommand(WAPI plugin) {
        this.plugin = plugin;
    }

    @SubCommand(value = "reload", permission = "wapi.main.reload")
    public void reload(CommandSender sender) {
        plugin.reloadConfig();
        sender.sendMessage(ChatColor.GREEN + "Configuration reloaded.");
    }

    @SubCommand(value = "give", permission = "wapi.main.give", playerOnly = true)
    public void give(
            Player sender,
            @Arg("target") Player target,
            @Arg("amount") @Range(min = 1, max = 100) int amount,
            @Arg(value = "silent", optional = true, defaultValue = "false") boolean silent
    ) {
        target.giveExp(amount);
        if (!silent) {
            sender.sendMessage(ChatColor.YELLOW + "Gave " + amount + " XP to " + target.getName() + '.');
            if (!target.equals(sender)) {
                target.sendMessage(ChatColor.GOLD + sender.getName() + " gave you " + amount + " XP.");
            }
        }
    }

    @SubCommand(value = "debug uuid")
    public void debugUuid(CommandSender sender, @Arg("uuid") UUID uuid) {
        sender.sendMessage(ChatColor.AQUA + "Parsed UUID: " + uuid);
    }

    @SubCommand(value = "debug mode")
    public void debugMode(CommandSender sender, @Arg("mode") DebugMode mode) {
        sender.sendMessage(ChatColor.AQUA + "Debug mode set to " + mode.name().toLowerCase() + '.');
    }

    private enum DebugMode {
        OFF,
        BASIC,
        VERBOSE
    }
}
