package me.penguinx13.wapiexample.showcase.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

@Plugin(id = "wapienterpriseshowcase", name = "WAPIEnterpriseShowcase", version = "2.0.0")
public final class WAPIEnterpriseShowcaseVelocity {
    private final ProxyServer proxyServer;
    private final Logger logger;

    @Inject
    public WAPIEnterpriseShowcaseVelocity(ProxyServer proxyServer, Logger logger) {
        this.proxyServer = proxyServer;
        this.logger = logger;
    }

    @Subscribe
    public void onInit(ProxyInitializeEvent event) {
        logger.info("WAPIEnterpriseShowcase velocity bootstrap initialized. OnlinePlayers={}", proxyServer.getPlayerCount());
    }

    @Subscribe
    public void onShutdown(ProxyShutdownEvent event) {
        logger.info("WAPIEnterpriseShowcase velocity shutdown complete.");
    }
}
