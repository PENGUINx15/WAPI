package me.penguinx13.wapi.enchants.listener;

import java.util.Objects;
import me.penguinx13.wapi.enchants.util.CustomEnchantItemUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;

/**
 * Prevents enchanting table usage on items that already contain custom enchants.
 */
public final class EnchantingTableProtectionListener implements Listener {

    private final CustomEnchantItemUtil itemUtil;

    /**
     * @param itemUtil utility used to inspect item PDC state
     */
    public EnchantingTableProtectionListener(final CustomEnchantItemUtil itemUtil) {
        this.itemUtil = Objects.requireNonNull(itemUtil, "itemUtil");
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEnchantItem(final EnchantItemEvent event) {
        if (itemUtil.hasAnyCustomEnchant(event.getItem())) {
            event.setCancelled(true);
        }
    }
}
