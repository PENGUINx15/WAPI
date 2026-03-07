package me.penguinx13.wapi.examplepaper.enchants;

import java.util.Set;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import wapi.enchants.api.CustomEnchant;

/**
 * Example enchantment that heals the attacker based on enchant level.
 */
public final class LifestealEnchant extends CustomEnchant {

    public LifestealEnchant() {
        super("lifesteal", 3, false, false, Set.of(
                Material.WOODEN_SWORD,
                Material.STONE_SWORD,
                Material.IRON_SWORD,
                Material.GOLDEN_SWORD,
                Material.DIAMOND_SWORD,
                Material.NETHERITE_SWORD
        ));
    }

    @Override
    public void onAttack(final Player player, final ItemStack item, final int level,
            final Event event) {
        if (!(event instanceof EntityDamageByEntityEvent damageEvent)) {
            return;
        }
        if (!(damageEvent.getEntity() instanceof LivingEntity)) {
            return;
        }

        final Double maxHealth = player.getAttribute(Attribute.MAX_HEALTH) == null
                ? null : player.getAttribute(Attribute.MAX_HEALTH).getValue();
        if (maxHealth == null) {
            return;
        }

        final double heal = 0.5D * level;
        player.setHealth(Math.min(maxHealth, player.getHealth() + heal));
    }
}
