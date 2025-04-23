package me.penguinx13.wapi.Events;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;

public class FallOnVoidEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Entity entity;
    private boolean cancelled;

    public FallOnVoidEvent(Entity entity) {
        this.entity = entity;
        this.cancelled = false;
    }

    public Entity getEntity() {
        return entity;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
