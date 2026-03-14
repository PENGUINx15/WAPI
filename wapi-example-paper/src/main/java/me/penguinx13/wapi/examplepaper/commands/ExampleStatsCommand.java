package me.penguinx13.wapi.examplepaper.commands;

import me.penguinx13.wapi.commands.annotations.Arg;
import me.penguinx13.wapi.commands.annotations.Min;
import me.penguinx13.wapi.commands.annotations.RootCommand;
import me.penguinx13.wapi.commands.annotations.SubCommand;
import me.penguinx13.wapi.examplepaper.model.PlayerStats;
import me.penguinx13.wapi.orm.Repository;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@RootCommand("wexample")
public final class ExampleStatsCommand {

    private final JavaPlugin plugin;
    private final Repository<PlayerStats, UUID> repository;

    public ExampleStatsCommand(JavaPlugin plugin, Repository<PlayerStats, UUID> repository) {
        this.plugin = plugin;
        this.repository = repository;
    }

    @SubCommand(value = "stats add", permission = "wexample.stats.add", playerOnly = true)
    public CompletionStage<Void> add(Player sender, @Arg(value = "amount", placeholder = "amount") @Min(1) int amount) {
        return repository.findByIdAsync(sender.getUniqueId())
                .thenCompose(existing -> {
                    PlayerStats stats = existing.orElseGet(() -> new PlayerStats(sender.getUniqueId()));
                    stats.addPoints(amount);
                    return repository.saveAsync(stats)
                            .thenRun(() -> runSync(() -> sender.sendMessage("§aДобавлено " + amount
                                    + " очков. Теперь: " + stats.getPoints())));
                });
    }

    @SubCommand(value = "stats show", permission = "wexample.stats.show")
    public CompletionStage<Void> show(CommandSender sender, @Arg(value = "target", placeholder = "player", optional = true) Player target) {
        Player selected = target;
        if (selected == null) {
            if (!(sender instanceof Player self)) {
                sender.sendMessage("§cДля консоли укажите игрока: /wexample stats show <nick>");
                return CompletableFuture.completedFuture(null);
            }
            selected = self;
        }

        Player finalSelected = selected;
        return repository.findByIdAsync(finalSelected.getUniqueId())
                .thenAccept(existing -> {
                    int points = existing.map(PlayerStats::getPoints).orElse(0);
                    runSync(() -> sender.sendMessage("§eСтатистика §6" + finalSelected.getName()
                            + "§e: " + points + " очков."));
                });
    }

    @SubCommand("ping")
    public void ping(CommandSender sender) {
        sender.sendMessage("§aWAPI example работает.");
    }

    @SubCommand("help")
    public void help(CommandSender sender) {
        sender.sendMessage("§6Доступные команды /wexample:");
        sender.sendMessage(" §e/wexample help §7- показать это меню");
        sender.sendMessage(" §e/wexample ping §7- проверить работу примера");
        sender.sendMessage(" §e/wexample stats add <amount> §7- добавить очки");
        sender.sendMessage(" §e/wexample stats show [player] §7- показать очки");
        sender.sendMessage(" §e/wexample enchant add lifesteal <level> §7- наложить Lifesteal");
        sender.sendMessage(" §e/wexample enchant remove lifesteal §7- убрать Lifesteal");
        sender.sendMessage(" §e/wexample enchant list §7- список чар на предмете");
    }

    private void runSync(Runnable action) {
        if (Bukkit.isPrimaryThread()) {
            action.run();
            return;
        }
        Bukkit.getScheduler().runTask(plugin, action);
    }
}
