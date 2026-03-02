package me.penguinx13.wapi;

import me.penguinx13.wapi.commands.MainCommand;
import me.penguinx13.wapi.commands.integration.CommandFrameworkBootstrap;
import me.penguinx13.wapi.listeners.FallOnVoidListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class WAPI extends JavaPlugin {

    private CommandFrameworkBootstrap commandFramework;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new FallOnVoidListener(), this);

        commandFramework = new CommandFrameworkBootstrap(this);
        commandFramework.register(new MainCommand(this));
        commandFramework.buildAndBind();

        getLogger().info("WAPI enabled successful");
    }

    @Override
    public void onDisable() {
        getLogger().info("WAPI disabled successful");
    }
}
