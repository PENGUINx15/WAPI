package me.penguinx13.wapi;

import me.penguinx13.wapi.Listeners.FallOnVoidListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class WAPI extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new FallOnVoidListener(), this);
        getLogger().info("WAPI enabled successful");
    }

    @Override
    public void onDisable() {
        getLogger().info("WAPI disabled successful");
    }
}
