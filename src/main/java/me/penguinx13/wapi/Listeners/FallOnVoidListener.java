package me.penguinx13.wapi.Listeners;

import me.penguinx13.wapi.Events.FallOnVoidEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class FallOnVoidListener implements Listener {
    @EventHandler
    public void onPlayerVoidDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            Entity entity = event.getEntity();
            FallOnVoidEvent voidEvent = new FallOnVoidEvent(entity);
            Bukkit.getPluginManager().callEvent(voidEvent);
            if (voidEvent.isCancelled()) {
                event.setCancelled(true);
            }
        }
    }
}
