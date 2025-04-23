package me.penguinx13.wapi.Managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class ConfigManager {

    private final Plugin plugin;

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void registerConfig(String configName) {
        File configFile = new File(plugin.getDataFolder(), configName);

        if (!configFile.exists()) {
            plugin.saveResource(configName, false);
        }
    }

    public FileConfiguration getConfig(String configName) {
        return  YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), configName));
    }

}
