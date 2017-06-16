package com.andrewyunt.warfare.configuration;

import com.andrewyunt.warfare.Warfare;
import org.bukkit.configuration.Configuration;

import java.util.List;

public class StaticConfiguration {

    private static Configuration config = Warfare.getInstance().getConfig();

    public static boolean MONGO = config.getBoolean("mongo.enabled", false);

    public static boolean LOBBY = config.getBoolean("is-lobby", false);
    public static String SERVER_NAME = Warfare.getInstance().getServerConfiguration()
            .getConfig().getString("server-name");
    public static String MAP_NAME = Warfare.getInstance().getServerConfiguration()
            .getConfig().getString("map-name");


    public static List<String> LOBBY_SERVERS = config.getStringList("lobby-servers");

    public static int CURRENT_LOBBY = 0;

    public static String getNextLobby() {
        CURRENT_LOBBY %= LOBBY_SERVERS.size();
        String server = LOBBY_SERVERS.get(CURRENT_LOBBY);
        CURRENT_LOBBY += 1;
        return server;
    }

    // hotbar items
    public static String SPECTATOR_TELEPORTER_TITLE = config.getString("hotbar-items.spectator-items.teleporter.title");
    public static int SPECTATOR_TELEPORTER_SLOT = config.getInt("hotbar-items.spectator-items.teleporter.slot");
    public static String SPECTATOR_RETURN_TO_LOBBY_TITLE = config.getString("hotbar-items.spectator-items.return-to-lobby.title");
    public static int SPECTATOR_RETURN_TO_LOBBY_SLOT = config.getInt("hotbar-items.spectator-items.return-to-lobby.slot");
    public static String CAGE_KIT_SELECTOR_TITLE = config.getString("hotbar-items.cage-items.kit-selector.title");
    public static int CAGE_KIT_SELECTOR_SLOT = config.getInt("hotbar-items.cage-items.kit-selector.slot");
    public static String CAGE_POWERUP_SELECTOR_TITLE = config.getString("hotbar-items.cage-items.powerup-selector.title");
    public static int CAGE_POWERUP_SELECTOR_SLOT = config.getInt("hotbar-items.cage-items.powerup-selector.slot");
    public static String CAGE_RETURN_TO_LOBBY_TITLE = config.getString("hotbar-items.cage-items.return-to-lobby.title");
    public static int CAGE_RETURN_TO_LOBBY_SLOT = config.getInt("hotbar-items.cage-items.return-to-lobby.slot");
    public static String LOBBY_SHOP_TITLE = config.getString("hotbar-items.lobby-items.shop.title");
    public static int LOBBY_SHOP_SLOT = config.getInt("hotbar-items.lobby-items.shop.slot");
    public static String LOBBY_KIT_SELECTOR_TITLE = config.getString("hotbar-items.lobby-items.kit-selector.title");
    public static int LOBBY_KIT_SELECTOR_SLOT = config.getInt("hotbar-items.lobby-items.kit-selector.slot");
    public static String LOBBY_JOIN_SOLO_TITLE = config.getString("hotbar-items.lobby-items.join-solo.title");
    public static int LOBBY_JOIN_SOLO_SLOT = config.getInt("hotbar-items.lobby-items.join-solo.slot");
    public static String LOBBY_JOIN_TEAMS_TITLE = config.getString("hotbar-items.lobby-items.join-teams.title");
    public static int LOBBY_JOIN_TEAMS_SLOT = config.getInt("hotbar-items.lobby-items.join-teams.slot");
}
