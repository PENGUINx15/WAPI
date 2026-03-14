package me.penguinx13.wapi.examplepaper.commands;

import java.util.Map;
import me.penguinx13.wapi.commands.annotations.Arg;
import me.penguinx13.wapi.commands.annotations.Min;
import me.penguinx13.wapi.commands.annotations.RootCommand;
import me.penguinx13.wapi.commands.annotations.SubCommand;
import me.penguinx13.wapi.enchants.CustomEnchant;
import me.penguinx13.wapi.enchants.storage.EnchantStorage;
import me.penguinx13.wapi.enchants.util.LoreUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Demonstrates how to apply and inspect custom enchants through commands.
 */
@RootCommand("wexample")
public final class ExampleEnchantCommand {

    private final EnchantStorage enchantStorage;
    private final CustomEnchant lifestealEnchant;

    public ExampleEnchantCommand(final EnchantStorage enchantStorage,
            final CustomEnchant lifestealEnchant) {
        this.enchantStorage = enchantStorage;
        this.lifestealEnchant = lifestealEnchant;
    }

    @SubCommand(value = "enchant add lifesteal", playerOnly = true)
    public void addLifesteal(final Player sender, @Arg("level") @Min(1) final int level) {
        if (level > lifestealEnchant.getMaxLevel()) {
            sender.sendMessage("§cМаксимальный уровень: "
                    + lifestealEnchant.getMaxLevel());
            return;
        }

        final ItemStack item = sender.getInventory().getItemInMainHand();
        try {
            enchantStorage.addEnchant(item, lifestealEnchant, level);
            LoreUtil.updateLore(item, enchantStorage.getEnchants(item));
            sender.sendMessage("§aНаложено зачарование Lifesteal " + level + ".");
        } catch (IllegalArgumentException ex) {
            sender.sendMessage("§cНе удалось наложить зачарование: " + ex.getMessage());
        }
    }

    @SubCommand(value = "enchant remove lifesteal", playerOnly = true)
    public void removeLifesteal(final Player sender) {
        final ItemStack item = sender.getInventory().getItemInMainHand();
        enchantStorage.removeEnchant(item, lifestealEnchant);
        LoreUtil.updateLore(item, enchantStorage.getEnchants(item));
        sender.sendMessage("§eLifesteal удалён с предмета.");
    }

    @SubCommand(value = "enchant list", playerOnly = true)
    public void listMainHandEnchants(final Player sender) {
        final ItemStack item = sender.getInventory().getItemInMainHand();
        final Map<CustomEnchant, Integer> enchants = enchantStorage.getEnchants(item);
        if (enchants.isEmpty()) {
            sender.sendMessage("§7На предмете нет кастомных зачарований.");
            return;
        }
        sender.sendMessage("§6Кастомные зачарования предмета:");
        for (Map.Entry<CustomEnchant, Integer> entry : enchants.entrySet()) {
            sender.sendMessage(" §e- §f" + entry.getKey().getId() + " " + entry.getValue());
        }
    }
}
