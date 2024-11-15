package me.penguinx13.wapi;

import org.bukkit.plugin.java.JavaPlugin;

public final class WAPI extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("WAPI enabled successful");
    }

    @Override
    public void onDisable() {
        getLogger().info("WAPI disabled successful");
    }
}
