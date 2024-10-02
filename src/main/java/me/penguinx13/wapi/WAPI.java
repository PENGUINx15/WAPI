package me.penguinx13.wapi;

import org.bukkit.plugin.java.JavaPlugin;

public final class WAPI extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("WAIP Enabled successful");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
