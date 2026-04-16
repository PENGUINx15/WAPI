package me.penguinx13.wapi.monitoring;

import org.bukkit.Bukkit;

public class MinecraftMetricsProvider {

    public double getTps() {
        final double[] tpsValues = Bukkit.getTPS();
        if (tpsValues == null || tpsValues.length == 0) {
            return 0.0D;
        }
        return tpsValues[0];
    }

    public int getOnlinePlayers() {
        return Bukkit.getOnlinePlayers().size();
    }

    public int getMaxPlayers() {
        return Bukkit.getMaxPlayers();
    }
}
