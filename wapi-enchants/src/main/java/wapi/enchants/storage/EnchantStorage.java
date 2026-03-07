package wapi.enchants.storage;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import wapi.enchants.api.CustomEnchant;
import wapi.enchants.api.EnchantRegistry;

/**
 * Reads and writes custom enchant levels from {@link PersistentDataContainer}.
 */
public class EnchantStorage {

    private final Plugin plugin;
    private final EnchantRegistry registry;

    /**
     * @param plugin plugin used as namespace owner for keys
     * @param registry registry used to resolve available enchant IDs
     */
    public EnchantStorage(final Plugin plugin, final EnchantRegistry registry) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    /**
     * Adds or updates an enchant level on an item.
     *
     * @param item item to update
     * @param enchant enchantment to apply
     * @param level level to set
     * @throws IllegalArgumentException when validation fails
     */
    public void addEnchant(final ItemStack item, final CustomEnchant enchant, final int level) {
        validateAdd(item, enchant, level);

        final ItemMeta meta = requireItemMeta(item);
        final PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(keyOf(enchant), PersistentDataType.INTEGER, level);
        item.setItemMeta(meta);
    }

    /**
     * Removes an enchant from an item.
     *
     * @param item item to update
     * @param enchant enchant to remove
     */
    public void removeEnchant(final ItemStack item, final CustomEnchant enchant) {
        final ItemMeta meta = requireItemMeta(item);
        meta.getPersistentDataContainer().remove(keyOf(Objects.requireNonNull(enchant, "enchant")));
        item.setItemMeta(meta);
    }

    /**
     * Gets the level of a specific enchantment on an item.
     *
     * @param item item to inspect
     * @param enchant enchantment to look up
     * @return level, or 0 when absent
     */
    public int getLevel(final ItemStack item, final CustomEnchant enchant) {
        final ItemMeta meta = requireItemMeta(item);
        final Integer value = meta.getPersistentDataContainer().get(keyOf(Objects.requireNonNull(enchant, "enchant")), PersistentDataType.INTEGER);
        return value == null ? 0 : value;
    }

    /**
     * Reads all registered enchantments from the supplied item.
     *
     * @param item item to inspect
     * @return map of enchantments to level values
     */
    public Map<CustomEnchant, Integer> getEnchants(final ItemStack item) {
        final ItemMeta meta = requireItemMeta(item);
        final PersistentDataContainer container = meta.getPersistentDataContainer();

        final Map<CustomEnchant, Integer> result = new LinkedHashMap<>();
        for (CustomEnchant enchant : registry.getAll()) {
            final Integer level = container.get(keyOf(enchant), PersistentDataType.INTEGER);
            if (level != null && level > 0) {
                result.put(enchant, level);
            }
        }
        return Map.copyOf(result);
    }

    private void validateAdd(final ItemStack item, final CustomEnchant enchant, final int level) {
        final ItemStack nonNullItem = Objects.requireNonNull(item, "item");
        final CustomEnchant nonNullEnchant = Objects.requireNonNull(enchant, "enchant");

        if (level < 1 || level > nonNullEnchant.getMaxLevel()) {
            throw new IllegalArgumentException("Level out of range for enchant " + nonNullEnchant.getId());
        }

        if (!nonNullEnchant.canEnchant(nonNullItem)) {
            throw new IllegalArgumentException("Enchant " + nonNullEnchant.getId() + " does not support item " + nonNullItem.getType());
        }

        for (CustomEnchant existing : getEnchants(nonNullItem).keySet()) {
            if (existing.conflictsWith(nonNullEnchant) || nonNullEnchant.conflictsWith(existing)) {
                throw new IllegalArgumentException("Enchant conflict between " + existing.getId() + " and " + nonNullEnchant.getId());
            }
        }
    }

    private ItemMeta requireItemMeta(final ItemStack item) {
        final ItemStack nonNullItem = Objects.requireNonNull(item, "item");
        final ItemMeta meta = nonNullItem.getItemMeta();
        if (meta == null) {
            throw new IllegalArgumentException("Item does not support ItemMeta: " + nonNullItem.getType());
        }
        return meta;
    }

    private NamespacedKey keyOf(final CustomEnchant enchant) {
        return new NamespacedKey(plugin, "enchant_" + enchant.getId().toLowerCase());
    }
}
