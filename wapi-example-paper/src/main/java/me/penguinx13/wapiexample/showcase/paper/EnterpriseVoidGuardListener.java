package me.penguinx13.wapiexample.showcase.paper;

import me.penguinx13.wapi.events.FallOnVoidEvent;
import me.penguinx13.wapi.managers.MessageManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class EnterpriseVoidGuardListener implements Listener {
    @EventHandler
    public void onFall(FallOnVoidEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        event.setCancelled(true);
        Location safe = player.getWorld().getSpawnLocation();
        player.teleport(safe);
        MessageManager.sendMessage(player, "{action}<red>Void fall prevented by Enterprise Guard</red>");
    }
}
