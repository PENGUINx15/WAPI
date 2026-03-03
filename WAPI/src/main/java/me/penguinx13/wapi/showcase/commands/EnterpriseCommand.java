package me.penguinx13.wapi.showcase.commands;

import me.penguinx13.wapi.CustomSkulls;
import me.penguinx13.wapi.EntityName;
import me.penguinx13.wapi.Tree;
import me.penguinx13.wapi.commands.annotations.*;
import me.penguinx13.wapi.commands.core.context.ExecutionState;
import me.penguinx13.wapi.commands.core.error.UserInputException;
import me.penguinx13.wapi.managers.ConfigManager;
import me.penguinx13.wapi.managers.MessageManager;
import me.penguinx13.wapi.showcase.core.db.EnterpriseUserRepository;
import me.penguinx13.wapi.showcase.core.metrics.EnterpriseMetricsSink;
import me.penguinx13.wapi.showcase.core.model.EnterpriseDebugMode;
import me.penguinx13.wapi.showcase.core.model.EnterpriseUser;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.*;

@RootCommand(value = "enterprise", permission = "wapi.enterprise")
public final class EnterpriseCommand {
    private final Plugin plugin;
    private final ConfigManager configManager;
    private final EnterpriseUserRepository repository;
    private final EnterpriseMetricsSink metricsSink;
    private final ExecutorService heavyExecutor;

    public EnterpriseCommand(Plugin plugin,
                             ConfigManager configManager,
                             EnterpriseUserRepository repository,
                             EnterpriseMetricsSink metricsSink,
                             ExecutorService heavyExecutor) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.repository = repository;
        this.metricsSink = metricsSink;
        this.heavyExecutor = heavyExecutor;
    }

    @SubCommand(value = "reload", permission = "wapi.enterprise.reload", description = "Reload enterprise configs")
    public void reload(Player sender) {
        configManager.registerConfig("messages.yml");
        configManager.registerConfig("database.yml");
        MessageManager.sendMessage(sender, "{message}<green>Enterprise configuration reloaded</green>");
    }

    @SubCommand(value = "user create", permission = "wapi.enterprise.user.create")
    public CompletionStage<Void> createUser(Player sender,
                                            @Arg("name") @Regex("^[A-Za-z0-9_]{3,16}$") @NotReservedName String name,
                                            @Arg("age") @Min(13) @Max(120) int age) {
        return repository.create(name, age).thenAccept(rows ->
                MessageManager.sendMessage(sender, "{message}<green>User {name} created with age {age}</green>",
                        Map.of("name", name, "age", age))
        );
    }

    @SubCommand(value = "user info", permission = "wapi.enterprise.user.info")
    public void userInfo(Player sender, @Arg("user") EnterpriseUser user) {
        if (user == null) {
            throw new UserInputException("User does not exist");
        }
        MessageManager.sendMessage(sender, "{message}<aqua>User {name} age={age}</aqua>",
                Map.of("name", user.name(), "age", user.age()));
    }

    @SubCommand(value = "user delete", permission = "wapi.enterprise.user.delete")
    public CompletionStage<Void> userDelete(Player sender, @Arg("name") String name) {
        return repository.deleteByName(name).thenAccept(affected -> {
            if (affected == 0) {
                throw new UserInputException("User not found: " + name);
            }
            MessageManager.sendMessage(sender, "{message}<yellow>Deleted user {name}</yellow>", Map.of("name", name));
        });
    }

    @SubCommand(value = "give", permission = "wapi.enterprise.give", playerOnly = true)
    public void give(Player sender,
                     @Arg("player") Player player,
                     @Arg("amount") @Range(min = 1, max = 6400) int amount,
                     @Arg(value = "silent", optional = true, defaultValue = "false") boolean silent) {
        player.giveExp(amount);
        if (!silent) {
            MessageManager.sendMessage(sender, "{message}<gold>Granted {amount} XP to {player}</gold>",
                    Map.of("amount", amount, "player", player.getName()));
        }
    }

    @SubCommand(value = "debug uuid")
    public void debugUuid(CommandSender sender, @Arg("uuid") UUID uuid) {
        sender.sendMessage("Parsed UUID: " + uuid);
    }

    @SubCommand(value = "debug mode")
    public void debugMode(CommandSender sender, @Arg("mode") EnterpriseDebugMode mode) {
        sender.sendMessage("Debug mode set to: " + mode.name());
    }

    @SubCommand(value = "tree scan", playerOnly = true)
    public void treeScan(Player sender) {
        Tree tree = new Tree(sender.getLocation().getBlock());
        tree.collect();
        int scanned = tree.getLogs().size() + tree.getLeaves().size();
        MessageManager.sendMessage(sender, "{message}<gray>Tree scan found {count} connected blocks</gray>", Map.of("count", scanned));
    }

    @SubCommand(value = "skull give", playerOnly = true)
    public void giveSkull(Player sender, @Arg("texture") String texture) {
        ItemStack skull = CustomSkulls.getSkull(texture);
        sender.getInventory().addItem(skull);
        MessageManager.sendMessage(sender, "{message}<green>Custom skull granted</green>");
    }

    @SubCommand(value = "entity name")
    public void entityName(CommandSender sender, @Arg("type") EntityType type) {
        sender.sendMessage("Localized entity: " + EntityName.getName(type));
    }

    @SubCommand(value = "stats")
    public void stats(CommandSender sender) {
        sender.sendMessage("Total commands: " + metricsSink.totalCommands());
        sender.sendMessage("Average latency (ms): " + metricsSink.averageLatencyMillis());
        sender.sendMessage("Slow commands: " + metricsSink.slowCommands());
    }

    @SubCommand(value = "heavy")
    public CompletionStage<Void> heavy(Player sender, @Arg("seconds") @Range(min = 1, max = 30) int seconds) {
        return CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(seconds * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, heavyExecutor).thenRun(() -> Bukkit.getScheduler().runTask(plugin,
                () -> MessageManager.sendMessage(sender, "{message}<green>Heavy async task completed in {seconds}s</green>", Map.of("seconds", seconds))));
    }
}
