package com.andrewyunt.warfare.managers.mysql;

public class SQLStatements {

    // Tables
    public static final String DB_PLAYERS = "Players";
    public static final String DB_PURCHASES = "Purchases";
    public static final String DB_SERVERS = "GameServers";
    public static final String DB_PARTIES = "Parties";
    public static final String DB_SIGNS = "Signs";
    public static final String DB_ARENAS = "Arenas";

    // Table Creation
    public static final String DB_PLAYERS_CREATE = "CREATE TABLE IF NOT EXISTS `" + DB_PLAYERS + "`"
            + "  (`uuid`             CHAR(36) PRIMARY KEY NOT NULL,"
            + "   `name`             CHAR(16) NOT NULL"
            + "   `party`            CHAR(36) NOT NULL,"
            + "   `kit`              CHAR(20) NOT NULL,"
            + "   `coins`            INT NOT NULL,"
            + "   `earned_coins`     INT NOT NULL,"
            + "   `kills`            INT NOT NULL,"
            + "   `wins`             INT NOT NULL);";
    public static final String DB_PURCHASES_CREATE = "CREATE TABLE IF NOT EXISTS `" + DB_PURCHASES + "`"
            + "  (`uuid`             CHAR(36) NOT NULL,"
            + "   `purchasable`      CHAR(20) NOT NULL,"
            + "   PRIMARY KEY (`uuid`, `purchasable`));";
    public static final String DB_SERVERS_CREATE = "CREATE TABLE IF NOT EXISTS `" + DB_SERVERS + "`"
            + "  (`name`             CHAR(20) PRIMARY KEY NOT NULL,"
            + "   `type`            CHAR(20) NOT NULL,"
            + "   `stage`            CHAR(20) NOT NULL,"
            + "   `map_name`            CHAR(30) NOT NULL,"
            + "   `online_players`   INT NOT NULL,"
            + "   `max_players`   INT NOT NULL);";
    public static final String DB_PARTIES_CREATE = "CREATE TABLE IF NOT EXISTS `" + DB_PARTIES + "`"
            + "  (`leader`           CHAR(36) PRIMARY KEY NOT NULL,"
            + "   `members`          TEXT);";
    public static final String DB_SIGNS_CREATE = "CREATE TABLE IF NOT EXISTS `" + DB_SIGNS + "`"
            + "  (`server_name`      CHAR(20) NOT NULL,"
            + "   `location`         CHAR(30) PRIMARY KEY NOT NULL,"
            + "   `type`             CHAR(20) NOT NULL,"
            + "   `place`            INT NOT NULL);";
    public static final String DB_ARENAS_CREATE = "CREATE TABLE IF NOT EXISTS `" + DB_ARENAS + "`"
            + "  (`server_name`     CHAR(20) PRIMARY KEY NOT NULL,"
            + "   `map_location`    CHAR(30) NOT NULL,"
            + "   `cages`           TEXT NOT NULL,"
            + "   `loot_chests`     TEXT NOT NULL);";

    // Data
    public static final String LOAD_PLAYER = "SELECT * FROM " + DB_PLAYERS + " WHERE uuid = ?;";
    public static final String SAVE_PLAYER = "REPLACE INTO " + DB_PLAYERS + " (uuid, name, party, kit,  coins, earned_coins, kills, wins) VALUES (?,?,?,?,?,?,?,?);";
    public static final String LOAD_PURCHASES = "SELECT * FROM " + DB_PURCHASES + " WHERE uuid = ?;";
    public static final String SAVE_PURCHASES = "REPLACE INTO " + DB_PURCHASES + " (uuid, purchasable) VALUES (?,?);";
    public static final String LOAD_SERVERS = "SELECT * FROM " + DB_SERVERS + ";";
    public static final String SAVE_SERVER = "REPLACE INTO " + DB_SERVERS + " (name, type, stage, map_name, online_players, max_players) VALUES (?,?,?,?,?,?);";
    public static final String LOAD_PARTY = "SELECT * FROM " + DB_PARTIES + " WHERE uuid = ?;";
    public static final String SAVE_PARTY = "REPLACE INTO " + DB_PARTIES + " (leader, members) VALUES (?,?);";
    public static final String LOAD_SIGNS = "SELECT * FROM " + DB_SIGNS + " WHERE server_name = ?;";
    public static final String SAVE_SIGN = "REPLACE INTO " + DB_SIGNS + " (server_name, location, type, place) VALUES (?,?,?,?);";
    public static final String DELETE_SIGN = "DELETE FROM " + DB_SIGNS + " WHERE server_name = ? AND location = ?;";
    public static final String LOAD_ARENA = "SELECT * FROM " + DB_ARENAS + " WHERE server_name = ?;";
    public static final String SAVE_ARENA = "REPLACE INTO " + DB_ARENAS + " (server_name, map_location, cages, loot_chests) VALUES (?,?,?,?);";
}
