package com.andrewyunt.warfare.configuration;

import com.andrewyunt.warfare.Warfare;
import org.bukkit.configuration.Configuration;

import java.util.List;

public class StaticConfiguration {

    private static Configuration config = Warfare.getInstance().getConfig();

    public static boolean LOBBY = config.getBoolean("is-lobby", false);
    public static String SERVER_NAME = config.getString("server-name");


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
    public static String NO_PERMS_CLASS_SLOT = config.getString("no-perms-class-slot");
    public static int SPECTATOR_TELEPORTER_SLOT = config.getInt("hotbar-items.spectator-items.teleporter.slot");
    public static String SPECTATOR_RETURN_TO_LOBBY_TITLE = config.getString("hotbar-items.spectator-items.return-to-lobby.title");
    public static int SPECTATOR_RETURN_TO_LOBBY_SLOT = config.getInt("hotbar-items.spectator-items.return-to-lobby.slot");
    public static String CAGE_CLASS_SELECTOR_TITLE = config.getString("hotbar-items.cage-items.class-selector.title");
    public static int CAGE_CLASS_SELECTOR_SLOT = config.getInt("hotbar-items.cage-items.class-selector.slot");
    /*
    public static String LOBBY_SHOP_TITLE = config.getString("hotbar-items.lobby-items.shop.title");
    public static int LOBBY_SHOP_SLOT = config.getInt("hotbar-items.lobby-items.shop.slot");
    */
    public static String LOBBY_CLASS_SELECTOR_TITLE = config.getString("hotbar-items.lobby-items.class-selector.title");
    public static int LOBBY_CLASS_SELECTOR_SLOT = config.getInt("hotbar-items.lobby-items.class-selector.slot");
    public static String LOBBY_PLAY_TITLE = config.getString("hotbar-items.lobby-items.play.title");
    public static int LOBBY_PLAY_SLOT = config.getInt("hotbar-items.lobby-items.play.slot");

    /*
    // ultimate descriptions
    public static List<String> DESCRIPTION_HEAL = config.getStringList("description-HEAL");
    public static List<String> DESCRIPTION_WRATH = config.getStringList("description-WRATH");
    public static List<String> DESCRIPTION_HELLS_SPAWNING = config.getStringList("description-HELLS_SPAWNING");
    public static List<String> DESCRIPTION_LEAP = config.getStringList("description-LEAP");
    public static List<String> DESCRIPTION_SONIC = config.getStringList("description-SONIC");
    public static List<String> DESCRIPTION_WITHERING = config.getStringList("description-WITHERING");
    public static List<String> DESCRIPTION_FLAMING_FEET = config.getStringList("description-FLAMING_FEET");

    // skill descriptions
    public static List<String> DESCRIPTION_RESISTANCE = config.getStringList("description-RESISTANCE");
    public static List<String> DESCRIPTION_JUGGERNAUT = config.getStringList("description-JUGGERNAUT");
    public static List<String> DESCRIPTION_CONSUMPTION = config.getStringList("description-CONSUMPTION");
    public static List<String> DESCRIPTION_HEAD_START = config.getStringList("description-HEAD_START");
    public static List<String> DESCRIPTION_GUARD = config.getStringList("description-GUARD");
    public static List<String> DESCRIPTION_FLAME = config.getStringList("description-FLAME");

    // health boost descriptions
    public static List<String> DESCRIPTION_HEALTH_BOOST_I = config.getStringList("description-HEALTH_BOOST_I");
    public static List<String> DESCRIPTION_HEALTH_BOOST_II = config.getStringList("description-HEALTH_BOOST_II");
    public static List<String> DESCRIPTION_HEALTH_BOOST_III = config.getStringList("description-HEALTH_BOOST_III");
    */
}
