package me.penguinx13.wapi;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public int getInt(String configName, String path) {
        File configFile = new File(plugin.getDataFolder(), configName);
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        if (!config.isSet(path)) {
            return 0;
        }

        return config.getInt(path);
    }

    public String getString(String configName, String path) {
        File configFile = new File(plugin.getDataFolder(), configName);
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        if (!config.isSet(path)) {
            return "";
        }

        return config.getString(path);
    }
    
    public List<String> getStringList(String configName, String path) {
        File configFile = new File(plugin.getDataFolder(), configName);
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        if (!config.isSet(path)) {
            return new ArrayList<>();
        }

        return config.getStringList(path);
    }
    public Map<String, Object> getConfigurationSection(String configName, String path) {
        File configFile = new File(plugin.getDataFolder(), configName);
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        if (!config.isConfigurationSection(path)) {
            return null;
        }

        ConfigurationSection section = config.getConfigurationSection(path);
        return section.getValues(false);
    }
}
