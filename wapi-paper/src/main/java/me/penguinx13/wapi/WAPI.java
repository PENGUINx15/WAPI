package me.penguinx13.wapi;

import me.penguinx13.wapi.monitoring.HttpStatusEndpoint;
import me.penguinx13.wapi.monitoring.MinecraftMetricsProvider;
import me.penguinx13.wapi.monitoring.MonitoringConfig;
import me.penguinx13.wapi.monitoring.ServerStatusService;
import me.penguinx13.wapi.monitoring.SystemMetricsProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class WAPI extends JavaPlugin {

    private HttpStatusEndpoint httpStatusEndpoint;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        final MonitoringConfig monitoringConfig = new MonitoringConfig(getConfig());
        if (monitoringConfig.isEnabled()) {
            final ServerStatusService serverStatusService = new ServerStatusService(
                monitoringConfig,
                new MinecraftMetricsProvider(),
                new SystemMetricsProvider()
            );

            httpStatusEndpoint = new HttpStatusEndpoint(
                serverStatusService,
                this,
                monitoringConfig.getPort(),
                monitoringConfig.getToken()
            );
            httpStatusEndpoint.start();
        }

        getLogger().info("WAPI enabled");
    }

    @Override
    public void onDisable() {
        if (httpStatusEndpoint != null) {
            httpStatusEndpoint.stop();
        }
    }
}
