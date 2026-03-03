package me.penguinx13.wapiexample.commands;

import me.penguinx13.wapi.commands.annotations.Arg;
import me.penguinx13.wapi.commands.annotations.Range;
import me.penguinx13.wapi.commands.annotations.RootCommand;
import me.penguinx13.wapi.commands.annotations.SubCommand;
import me.penguinx13.wapiexample.WAPIExample;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

@RootCommand(value = "main", permission = "wapi.main")
public class MainCommand {
    private final WAPIExample plugin;

    public MainCommand(WAPIExample plugin) {
        this.plugin = plugin;
    }

    @SubCommand(value = "reload", permission = "wapi.main.reload", description = "Reload plugin configuration")
    public void reload(CommandSender sender) {
        plugin.reloadConfig();
        sender.sendMessage(ChatColor.GREEN + "Configuration reloaded.");
    }

    @SubCommand(value = "give", permission = "wapi.main.give", playerOnly = true, description = "Give XP to a player")
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

    @SubCommand(value = "admin reset", permission = "wapi.main.admin.reset", playerOnly = true,
            description = "Reset a target player")
    public void reset(Player sender, @Arg("target") Player target) {
        target.setHealth(target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        target.setFoodLevel(20);
        sender.sendMessage(ChatColor.GREEN + "Reset stats for " + target.getName() + '.');
    }

    @SubCommand(value = "debug uuid", description = "Parse a UUID")
    public void debugUuid(CommandSender sender, @Arg("uuid") UUID uuid) {
        sender.sendMessage(ChatColor.AQUA + "Parsed UUID: " + uuid);
    }

    @SubCommand(value = "debug mode", description = "Change debug mode")
    public void debugMode(CommandSender sender, @Arg("mode") DebugMode mode) {
        sender.sendMessage(ChatColor.AQUA + "Debug mode set to " + mode.name().toLowerCase() + '.');
    }

    private enum DebugMode {
        OFF,
        BASIC,
        VERBOSE
    }
}
