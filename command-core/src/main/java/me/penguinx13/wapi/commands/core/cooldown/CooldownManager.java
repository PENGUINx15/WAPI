package me.penguinx13.wapi.commands.core.cooldown;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.*;

public final class CooldownManager {
    private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();

    public CooldownManager() {
        cleaner.scheduleAtFixedRate(this::cleanup, 1, 1, TimeUnit.MINUTES);
    }

    public boolean isOnCooldown(String key) {
        Long expiry = cooldowns.get(key);
        return expiry != null && expiry > System.currentTimeMillis();
    }

    public void mark(String key, Duration duration) {
        cooldowns.put(key, System.currentTimeMillis() + duration.toMillis());
    }

    private void cleanup() {
        long now = System.currentTimeMillis();
        cooldowns.entrySet().removeIf(e -> e.getValue() <= now);
    }
}
