package me.penguinx13.wapi.examplepaper.enchants;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import me.penguinx13.wapi.enchants.api.EnchantTrigger;
import me.penguinx13.wapi.enchants.manager.EnchantManager;

/**
 * Example listener that forwards Paper events into {@link EnchantManager}.
 */
public final class ExampleEnchantListener implements Listener {

    private final EnchantManager enchantManager;

    public ExampleEnchantListener(final EnchantManager enchantManager) {
        this.enchantManager = enchantManager;
    }

    @EventHandler
    public void onAttack(final EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        final ItemStack item = player.getInventory().getItemInMainHand();
        enchantManager.trigger(EnchantTrigger.ATTACK, player, item, event);
    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final ItemStack item = player.getInventory().getItemInMainHand();
        enchantManager.trigger(EnchantTrigger.BLOCK_BREAK, player, item, event);
    }

    @EventHandler
    public void onShoot(final ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) {
            return;
        }
        final ItemStack item = player.getInventory().getItemInMainHand();
        enchantManager.trigger(EnchantTrigger.SHOOT, player, item, event);
    }

    @EventHandler
    public void onKill(final EntityDeathEvent event) {
        final Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }
        final ItemStack item = killer.getInventory().getItemInMainHand();
        enchantManager.trigger(EnchantTrigger.KILL, killer, item, event);
    }
}
