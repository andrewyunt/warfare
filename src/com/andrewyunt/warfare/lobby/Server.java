package com.andrewyunt.warfare.lobby;

import com.andrewyunt.warfare.game.Game;
import lombok.Getter;
import lombok.Setter;

public class Server {

    @Getter private final String name;
    @Getter private final ServerType serverType;
    @Getter private final Game.Stage gameStage;
    @Getter private final String mapName;
    @Getter @Setter private int onlinePlayers;
    @Getter private final int maxPlayers;

    public Server(String name, ServerType serverType, Game.Stage gameStage, String mapName, int onlinePlayers, int maxPlayers) {
        this.name = name;
        this.serverType = serverType;
        this.gameStage = gameStage;
        this.mapName = mapName;
        this.onlinePlayers = onlinePlayers;
        this.maxPlayers = maxPlayers;
    }

    public enum ServerType {
        LOBBY, TEAMS, SOLO
    }
}