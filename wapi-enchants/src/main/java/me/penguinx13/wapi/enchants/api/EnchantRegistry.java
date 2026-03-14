package me.penguinx13.wapi.enchants.api;

import me.penguinx13.wapi.enchants.CustomEnchant;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory registry for custom enchantments.
 */
public class EnchantRegistry {

    private final Map<String, CustomEnchant> enchantsById = new ConcurrentHashMap<>();

    /**
     * Registers a custom enchantment.
     *
     * @param enchant enchantment to register
     * @throws IllegalArgumentException when an enchantment with the same ID already exists
     */
    public void register(final CustomEnchant enchant) {
        final CustomEnchant nonNullEnchant = Objects.requireNonNull(enchant, "enchant");
        final CustomEnchant previous = enchantsById.putIfAbsent(
                nonNullEnchant.getId(), nonNullEnchant
        );
        if (previous != null) {
            throw new IllegalArgumentException("Duplicate enchant id: "
                    + nonNullEnchant.getId());
        }
    }

    /**
     * Fetches an enchantment by ID.
     *
     * @param id enchantment ID
     * @return enchantment or {@code null} if absent
     */
    public CustomEnchant get(final String id) {
        return enchantsById.get(Objects.requireNonNull(id, "id").toLowerCase());
    }

    /**
     * @return immutable view of all registered enchantments
     */
    public Collection<CustomEnchant> getAll() {
        return Collections.unmodifiableCollection(enchantsById.values());
    }
}
