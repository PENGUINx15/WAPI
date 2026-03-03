package me.penguinx13.wapiexample.showcase.core.service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class EnterpriseCooldownService {
    private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();

    public long remainingMillis(String key) {
        long now = System.currentTimeMillis();
        long expiry = cooldowns.getOrDefault(key, 0L);
        return Math.max(0L, expiry - now);
    }

    public void mark(String key, Duration duration) {
        cooldowns.put(key, System.currentTimeMillis() + duration.toMillis());
    }
}
