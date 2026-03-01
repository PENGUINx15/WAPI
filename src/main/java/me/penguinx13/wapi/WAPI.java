package me.penguinx13.wapi;

import me.penguinx13.wapi.commandframework.core.CommandRegistry;
import me.penguinx13.wapi.commands.MainCommand;
import me.penguinx13.wapi.listeners.FallOnVoidListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class WAPI extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new FallOnVoidListener(), this);

        CommandRegistry commandRegistry = new CommandRegistry(this);
        commandRegistry.registerCommand(new MainCommand(this));

        getLogger().info("WAPI enabled successful");
    }

    @Override
    public void onDisable() {
        getLogger().info("WAPI disabled successful");
    }
}
