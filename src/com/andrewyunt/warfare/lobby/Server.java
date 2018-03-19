package com.andrewyunt.warfare.lobby;

import com.andrewyunt.warfare.game.Game;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Server {

    private final String name;
    private final ServerType serverType;
    private final Game.Stage gameStage;
    private final String mapName;
    @Setter private int onlinePlayers;
    private final int maxPlayers;

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