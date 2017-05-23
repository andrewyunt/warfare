package com.andrewyunt.warfare.game.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EndEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public EndEvent() {
        super();
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}