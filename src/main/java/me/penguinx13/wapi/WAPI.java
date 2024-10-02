package me.penguinx13.wapi;

import org.bukkit.plugin.java.JavaPlugin;

public final class WAPI extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("WAIP Enabled successful");
        ConfigManager configManager = new ConfigManager(this);
        MessageManager messageManager = new MessageManager();
        CooldownManager cooldownManager = new CooldownManager();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
