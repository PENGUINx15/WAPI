package wapi.enchants.api;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

/**
 * Base class for defining API-only custom enchantments backed by item persistent data.
 *
 * <p>This type intentionally does not integrate with Bukkit's native enchantment
 * registry. Implementations should provide behavior through event-driven hooks.
 */
public abstract class CustomEnchant {

    private final String id;
    private final int maxLevel;
    private final boolean treasure;
    private final boolean curse;
    private final Set<Material> supportedItems;

    /**
     * Creates a new custom enchantment descriptor.
     *
     * @param id unique ID used in PDC keys
     * @param maxLevel maximum supported enchant level
     * @param treasure whether this enchantment is considered treasure-only
     * @param curse whether this enchantment is considered a curse
     * @param supportedItems materials this enchantment can be applied to
     */
    protected CustomEnchant(
            final String id,
            final int maxLevel,
            final boolean treasure,
            final boolean curse,
            final Set<Material> supportedItems
    ) {
        this.id = Objects.requireNonNull(id, "id").toLowerCase();
        if (maxLevel < 1) {
            throw new IllegalArgumentException("maxLevel must be >= 1");
        }
        this.maxLevel = maxLevel;
        this.treasure = treasure;
        this.curse = curse;
        this.supportedItems = Collections.unmodifiableSet(
                new HashSet<>(Objects.requireNonNull(supportedItems, "supportedItems"))
        );
    }

    /**
     * @return unique enchantment identifier
     */
    public String getId() {
        return id;
    }

    /**
     * @return maximum supported level
     */
    public int getMaxLevel() {
        return maxLevel;
    }

    /**
     * @return whether this enchantment is considered treasure-only
     */
    public boolean isTreasure() {
        return treasure;
    }

    /**
     * @return whether this enchantment is considered a curse
     */
    public boolean isCurse() {
        return curse;
    }

    /**
     * Checks whether the supplied item can receive this enchantment.
     *
     * @param item item to check
     * @return true if supported
     */
    public boolean canEnchant(final ItemStack item) {
        return item != null
                && item.getType() != Material.AIR
                && supportedItems.contains(item.getType());
    }

    /**
     * Determines whether this enchant conflicts with another enchant.
     *
     * @param other other enchantment
     * @return true if the enchantments conflict
     */
    public boolean conflictsWith(final CustomEnchant other) {
        return false;
    }

    /**
     * Called when the holder attacks.
     */
    public void onAttack(final Player player, final ItemStack item, final int level,
            final Event event) {
    }

    /**
     * Called when the holder is damaged.
     */
    public void onDamaged(final Player player, final ItemStack item, final int level,
            final Event event) {
    }

    /**
     * Called when the holder breaks a block.
     */
    public void onBlockBreak(final Player player, final ItemStack item, final int level,
            final Event event) {
    }

    /**
     * Called when the holder shoots a projectile.
     */
    public void onShoot(final Player player, final ItemStack item, final int level,
            final Event event) {
    }

    /**
     * Called when the holder kills an entity.
     */
    public void onKill(final Player player, final ItemStack item, final int level,
            final Event event) {
    }
}
