package me.penguinx13.wapi.enchants.manager;

import java.util.Map;
import java.util.Objects;

import me.penguinx13.wapi.enchants.CustomEnchant;
import me.penguinx13.wapi.enchants.api.EnchantTrigger;
import me.penguinx13.wapi.enchants.storage.EnchantStorage;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

/**
 * Coordinates enchant lookup and trigger dispatch for item-based custom enchants.
 */
public class EnchantManager {

    private final EnchantStorage storage;

    /**
     * @param storage storage implementation used to resolve enchant levels from items
     */
    public EnchantManager(final EnchantStorage storage) {
        this.storage = Objects.requireNonNull(storage, "storage");
    }

    /**
     * Reads all enchants from an item and dispatches the trigger to every matching enchant.
     *
     * @param trigger trigger type
     * @param player player executing/receiving the action
     * @param item item that stores enchant data
     * @param event source event
     */
    public void trigger(final EnchantTrigger trigger, final Player player,
                        final ItemStack item, final Event event) {
        final Map<CustomEnchant, Integer> enchants = storage.getEnchants(item);
        for (Map.Entry<CustomEnchant, Integer> entry : enchants.entrySet()) {
            triggerToEnchant(trigger, player, item, entry.getValue(), event,
                    entry.getKey());
        }
    }

    /**
     * Dispatches a trigger to one enchantment using an explicit level.
     *
     * @param trigger trigger type
     * @param player player executing/receiving the action
     * @param item item that stores enchant data
     * @param level level value for this enchant
     * @param event source event
     * @param enchant target enchant
     */
    public void trigger(final EnchantTrigger trigger, final Player player,
            final ItemStack item, final int level, final Event event,
            final CustomEnchant enchant) {
        triggerToEnchant(trigger, player, item, level, event,
                Objects.requireNonNull(enchant, "enchant"));
    }

    private void triggerToEnchant(final EnchantTrigger trigger,
            final Player player, final ItemStack item, final int level,
            final Event event, final CustomEnchant enchant) {
        switch (trigger) {
            case ATTACK -> enchant.onAttack(player, item, level, event);
            case DAMAGED -> enchant.onDamaged(player, item, level, event);
            case BLOCK_BREAK -> enchant.onBlockBreak(player, item, level, event);
            case SHOOT -> enchant.onShoot(player, item, level, event);
            case KILL -> enchant.onKill(player, item, level, event);
        }
    }
}
