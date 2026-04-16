package me.penguinx13.wapi.monitoring;

public class ServerStatusService {

    private final MinecraftMetricsProvider minecraftMetricsProvider;
    private final SystemMetricsProvider systemMetricsProvider;
    private final String serverId;
    private final long cacheTtlMillis;

    private ServerStatus cachedStatus;
    private long cacheExpiryTimestamp;

    public ServerStatusService(final MonitoringConfig monitoringConfig,
                               final MinecraftMetricsProvider minecraftMetricsProvider,
                               final SystemMetricsProvider systemMetricsProvider) {
        this.minecraftMetricsProvider = minecraftMetricsProvider;
        this.systemMetricsProvider = systemMetricsProvider;
        this.serverId = monitoringConfig.getServerId();
        this.cacheTtlMillis = monitoringConfig.getCacheMs();
    }

    public synchronized ServerStatus getStatus() {
        final long now = System.currentTimeMillis();
        if (cachedStatus != null && now < cacheExpiryTimestamp) {
            return cachedStatus;
        }

        final ServerStatus status = new ServerStatus();
        status.setServerId(serverId);
        status.setTps(minecraftMetricsProvider.getTps());
        status.setPlayers(minecraftMetricsProvider.getOnlinePlayers());
        status.setMaxPlayers(minecraftMetricsProvider.getMaxPlayers());
        status.setRamUsed(systemMetricsProvider.getUsedMemoryMb());
        status.setRamMax(systemMetricsProvider.getMaxMemoryMb());
        status.setTimestamp(now);

        cachedStatus = status;
        cacheExpiryTimestamp = now + cacheTtlMillis;

        return status;
    }
}
