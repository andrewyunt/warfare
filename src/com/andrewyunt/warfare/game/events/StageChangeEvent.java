package com.andrewyunt.warfare.game.events;

import com.andrewyunt.warfare.game.Game;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class StageChangeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Game.Stage stage;

    public StageChangeEvent(Game.Stage stage) {
        super();

        this.stage = stage;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Game.Stage getStage() {
        return stage;
    }
}