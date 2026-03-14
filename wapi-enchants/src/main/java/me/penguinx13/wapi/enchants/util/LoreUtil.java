package me.penguinx13.wapi.enchants.util;

import java.util.*;

import me.penguinx13.wapi.enchants.CustomEnchant;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Utility methods for generating and applying enchantment lore entries.
 */
public final class LoreUtil {

    private LoreUtil() {
    }

    /**
     * Rebuilds lore entries using the supplied enchant map.
     *
     * @param item item to update
     * @param enchants enchant-level map
     */
    public static void updateLore(final ItemStack item, final Map<CustomEnchant, Integer> enchants) {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(enchants, "enchants");

        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        final List<String> lore = new ArrayList<>();
        enchants.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().getId()))
                .forEach(entry -> lore.add(formatLoreLine(entry.getKey(), entry.getValue())));

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    /**
     * Formats a lore line in the style: {@code §7Lifesteal III}.
     *
     * @param enchant enchantment
     * @param level level
     * @return formatted lore line
     */
    public static String formatLoreLine(final CustomEnchant enchant, final int level) {
        return "§7" + humanizeId(enchant.getId()) + " " + toRoman(Math.max(level, 1));
    }

    private static String humanizeId(final String id) {
        final String[] tokens = id.replace('-', '_').split("_");
        final StringBuilder builder = new StringBuilder();
        for (String token : tokens) {
            if (token.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            final String lower = token.toLowerCase(Locale.ROOT);
            builder.append(Character.toUpperCase(lower.charAt(0))).append(lower.substring(1));
        }
        return builder.isEmpty() ? id : builder.toString();
    }

    private static String toRoman(final int value) {
        final int[] numbers = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        final String[] numerals = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

        int remaining = value;
        StringBuilder roman = new StringBuilder();
        for (int i = 0; i < numbers.length; i++) {
            while (remaining >= numbers[i]) {
                roman.append(numerals[i]);
                remaining -= numbers[i];
            }
        }
        return roman.toString();
    }
}
