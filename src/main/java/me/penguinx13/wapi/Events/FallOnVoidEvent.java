package me.penguinx13.wapi.Events;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FallOnVoidEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Entity entity;

    public FallOnVoidEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

