package com.andrewyunt.warfare.game.events;

import com.andrewyunt.warfare.player.GamePlayer;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AddPlayerEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @Getter private final GamePlayer gamePlayer;

    public AddPlayerEvent(GamePlayer gamePlayer) {
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
}