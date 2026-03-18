package me.penguinx13.wapi.enchants.util;

import java.util.Map;
import java.util.Objects;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

/**
 * Utility methods for interacting with custom enchant metadata stored in item PDC.
 */
public final class CustomEnchantItemUtil {

    private static final String CUSTOM_ENCHANT_PREFIX = "enchant_";
    private static final Enchantment FAKE_GLOW_ENCHANT = Enchantment.DURABILITY;
    private static final int FAKE_GLOW_LEVEL = 1;

    private final NamespacedKey managedGlowKey;

    /**
     * @param plugin plugin used as key namespace owner for internal markers
     */
    public CustomEnchantItemUtil(final Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        this.managedGlowKey = new NamespacedKey(plugin, "custom_enchant_glow");
    }

    /**
     * Checks whether an item has any custom enchants stored as {@code enchant_<id>} keys.
     *
     * @param item item to inspect
     * @return true when at least one custom enchant key is present
     */
    public boolean hasCustomEnchants(final ItemStack item) {
        final ItemMeta meta = getMetaOrNull(item);
        if (meta == null) {
            return false;
        }

        for (NamespacedKey key : meta.getPersistentDataContainer().getKeys()) {
            if (key.getKey().startsWith(CUSTOM_ENCHANT_PREFIX)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Alias for readability in listeners.
     *
     * @param item item to inspect
     * @return true when the item contains custom enchant PDC keys
     */
    public boolean hasAnyCustomEnchant(final ItemStack item) {
        return hasCustomEnchants(item);
    }

    /**
     * Applies a visual enchantment glow using a hidden fake enchant when needed.
     *
     * @param item item to update
     */
    public void applyGlowIfNeeded(final ItemStack item) {
        final ItemMeta meta = getMetaOrNull(item);
        if (meta == null) {
            return;
        }

        if (!hasCustomEnchants(item)) {
            removeGlowIfNeeded(item);
            return;
        }

        if (hasRealBukkitEnchants(meta)) {
            clearManagedGlow(meta);
            item.setItemMeta(meta);
            return;
        }

        if (!meta.hasEnchant(FAKE_GLOW_ENCHANT)) {
            meta.addEnchant(FAKE_GLOW_ENCHANT, FAKE_GLOW_LEVEL, true);
        }
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        markManagedGlow(meta.getPersistentDataContainer(), true);
        item.setItemMeta(meta);
    }

    /**
     * Removes the fake glow enchant when the item no longer has custom enchants.
     *
     * @param item item to update
     */
    public void removeGlowIfNeeded(final ItemStack item) {
        final ItemMeta meta = getMetaOrNull(item);
        if (meta == null || hasCustomEnchants(item)) {
            return;
        }

        if (!isManagedGlow(meta.getPersistentDataContainer())) {
            return;
        }

        clearManagedGlow(meta);
        item.setItemMeta(meta);
    }

    private boolean hasRealBukkitEnchants(final ItemMeta meta) {
        final Map<Enchantment, Integer> enchants = meta.getEnchants();
        if (enchants.isEmpty()) {
            return false;
        }

        if (!isManagedGlow(meta.getPersistentDataContainer())) {
            return true;
        }

        if (enchants.size() > 1) {
            return true;
        }

        final Integer level = enchants.get(FAKE_GLOW_ENCHANT);
        return level == null || level != FAKE_GLOW_LEVEL;
    }

    private void clearManagedGlow(final ItemMeta meta) {
        if (isManagedGlow(meta.getPersistentDataContainer())
                && meta.getEnchantLevel(FAKE_GLOW_ENCHANT) == FAKE_GLOW_LEVEL) {
            meta.removeEnchant(FAKE_GLOW_ENCHANT);
            meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        markManagedGlow(meta.getPersistentDataContainer(), false);
    }

    private boolean isManagedGlow(final PersistentDataContainer container) {
        final Byte value = container.get(managedGlowKey, PersistentDataType.BYTE);
        return value != null && value == (byte) 1;
    }

    private void markManagedGlow(final PersistentDataContainer container, final boolean value) {
        if (value) {
            container.set(managedGlowKey, PersistentDataType.BYTE, (byte) 1);
            return;
        }
        container.remove(managedGlowKey);
    }

    private ItemMeta getMetaOrNull(final ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return null;
        }
        return item.getItemMeta();
    }
}
