package me.penguinx13.wapi.managers;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CooldownManager {

    private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();

    public CooldownManager() {
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpired, 1, 1, TimeUnit.MINUTES);
    }

    public void markCooldown(UUID uuid, String name, Duration duration) {
        cooldowns.put(key(uuid, name), System.currentTimeMillis() + duration.toMillis());
    }

    public boolean isOnCooldown(UUID uuid, String name) {
        Long until = cooldowns.get(key(uuid, name));
        return until != null && until > System.currentTimeMillis();
    }

    public long remainingSeconds(UUID uuid, String name) {
        Long until = cooldowns.get(key(uuid, name));
        if (until == null) {
            return 0;
        }
        return Math.max(0, (until - System.currentTimeMillis()) / 1000);
    }

    private void cleanupExpired() {
        long now = System.currentTimeMillis();
        cooldowns.entrySet().removeIf(entry -> entry.getValue() <= now);
    }

    private String key(UUID uuid, String name) {
        return uuid + ":" + name;
    }
}
