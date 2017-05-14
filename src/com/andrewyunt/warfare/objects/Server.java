package com.andrewyunt.warfare.objects;

public class Server {
    private final int id;
    private final String name;
    private final ServerType serverType;
    private final Game.Stage gameStage;
    private final String mapName;
    private int onlinePlayers;
    private final int maxPlayers;

    public Server(int id, String name, ServerType serverType, Game.Stage gameStage, String mapName, int onlinePlayers, int maxPlayers) {
        this.id = id;
        this.name = name;
        this.serverType = serverType;
        this.gameStage = gameStage;
        this.mapName = mapName;
        this.onlinePlayers = onlinePlayers;
        this.maxPlayers = maxPlayers;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ServerType getServerType() {
        return serverType;
    }

    public Game.Stage getGameStage() {
        return gameStage;
    }

    public void setOnlinePlayers(int onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
    }

    public int getOnlinePlayers() {
        return onlinePlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public String getMapName() {
        return mapName;
    }

    public enum ServerType{
        LOBBY, GAME;
    }
}
