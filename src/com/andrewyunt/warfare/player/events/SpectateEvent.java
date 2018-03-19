package com.andrewyunt.warfare.player.events;

import com.andrewyunt.warfare.player.GamePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SpectateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final GamePlayer gamePlayer;

    public SpectateEvent(GamePlayer gamePlayer) {
        super();

        this.gamePlayer = gamePlayer;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }
}