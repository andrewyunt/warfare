package com.andrewyunt.warfare.objects;

public class Server {
    private final int id;
    private final String name;
    private final ServerType serverType;
    private final Game.Stage gameStage;
    private int onlinePlayers;
    private final int maxPlayers = 12;

    public Server(int id, String name, ServerType serverType, Game.Stage gameStage, int onlinePlayers) {
        this.name = name;
        this.serverType = serverType;
        this.gameStage = gameStage;
        this.onlinePlayers = onlinePlayers;
        this.id = id;
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

    public enum ServerType{
        LOBBY, GAME;
    }
}
