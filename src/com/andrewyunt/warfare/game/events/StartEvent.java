package com.andrewyunt.warfare.game.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class StartEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public StartEvent() {
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