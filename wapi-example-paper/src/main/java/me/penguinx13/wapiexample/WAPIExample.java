package me.penguinx13.wapiexample;

import me.penguinx13.wapi.listeners.FallOnVoidListener;
import me.penguinx13.wapi.managers.ConfigManager;
import me.penguinx13.wapiexample.showcase.commands.EnterpriseCommand;
import me.penguinx13.wapiexample.showcase.core.db.EnterpriseUserRepository;
import me.penguinx13.wapiexample.showcase.core.metrics.EnterpriseMetricsSink;
import me.penguinx13.wapiexample.showcase.paper.EnterprisePaperBootstrap;
import me.penguinx13.wapiexample.showcase.paper.EnterpriseVoidGuardListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.*;

public final class WAPIExample extends JavaPlugin {

    private EnterpriseUserRepository repository;
    private ExecutorService heavyExecutor;

    @Override
    public void onEnable() {
        ConfigManager configManager = new ConfigManager(this);
        configManager.registerConfig("messages.yml");
        configManager.registerConfig("database.yml");

        heavyExecutor = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "enterprise-heavy");
            t.setDaemon(true);
            return t;
        });

        repository = new EnterpriseUserRepository(this, "enterprise.db");
        repository.initializeSchema().exceptionally(ex -> {
            getLogger().severe("Failed to initialize schema: " + ex.getMessage());
            return null;
        });

        EnterpriseMetricsSink metricsSink = new EnterpriseMetricsSink(this);
        EnterpriseCommand command = new EnterpriseCommand(this, configManager, repository, metricsSink, heavyExecutor);
        new EnterprisePaperBootstrap().build(this, command, repository, metricsSink, heavyExecutor);

        getServer().getPluginManager().registerEvents(new FallOnVoidListener(), this);
        getServer().getPluginManager().registerEvents(new EnterpriseVoidGuardListener(), this);

        getLogger().info("WAPIEnterpriseShowcase enabled");
    }

    @Override
    public void onDisable() {
        if (repository != null) {
            repository.shutdown();
        }
        if (heavyExecutor != null) {
            heavyExecutor.shutdown();
        }
    }
}
