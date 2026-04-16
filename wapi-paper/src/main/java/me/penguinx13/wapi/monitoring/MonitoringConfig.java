package me.penguinx13.wapi.monitoring;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public class MonitoringConfig {

    private final boolean enabled;
    private final int port;
    private final String serverId;
    private final String token;
    private final long cacheMs;

    public MonitoringConfig(final FileConfiguration configuration) {
        enabled = configuration.getBoolean("monitoring.enabled", true);
        port = configuration.getInt("monitoring.port", 8081);

        final String configuredServerId = configuration.getString("monitoring.server-id");
        if (configuredServerId == null || configuredServerId.isBlank()) {
            serverId = "server-" + Bukkit.getPort();
        } else {
            serverId = configuredServerId;
        }

        token = configuration.getString("monitoring.token", "WAPI_SECRET");
        cacheMs = configuration.getLong("monitoring.cache-ms", 5000L);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getPort() {
        return port;
    }

    public String getServerId() {
        return serverId;
    }

    public String getToken() {
        return token;
    }

    public long getCacheMs() {
        return cacheMs;
    }
}
