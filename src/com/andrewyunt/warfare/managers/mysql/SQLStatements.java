package com.andrewyunt.warfare.managers.mysql;

public class SQLStatements {

    // Tables
    public static final String DB_PLAYER = "Players";
    public static final String DB_PURCHASES = "Purchases";
    public static final String DB_SERVERS = "GameServers";
    public static final String DB_PARTIES = "Parties";
    public static final String DB_SIGNS = "Signs";
    public static final String DB_ARENAS = "Arenas";

    // Table Creation
    public static final String DB_PLAYER_CREATE = "CREATE TABLE IF NOT EXISTS `" + DB_PLAYER + "`"
            + "  (`uuid`             CHAR(36) PRIMARY KEY NOT NULL,"
            + "   `party`            CHAR(36) NOT NULL,"
            + "   `kit`              CHAR(20) NOT NULL,"
            + "   `ultimate`         CHAR(20) NOT NULL,"
            + "   `skill`            CHAR(20) NOT NULL,"
            + "   `coins`            INT NOT NULL,"
            + "   `earned_coins`     INT NOT NULL,"
            + "   `kills`            INT,"
            + "   `wins`             INT);";
    public static final String DB_PURCHASES_CREATE = "CREATE TABLE IF NOT EXISTS `" + DB_PURCHASES + "`"
            + "  (`uuid`             CHAR(36) NOT NULL,"
            + "   `purchasable`      CHAR(20) NOT NULL,"
            + "   PRIMARY KEY (`uuid`, `purchasable`));";
    public static final String DB_SERVERS_CREATE = "CREATE TABLE IF NOT EXISTS `" + DB_SERVERS + "`"
            + "  (`name`             CHAR(20) PRIMARY KEY NOT NULL,"
            + "   `stage`            CHAR(20) NOT NULL,"
            + "   `online_players`   INT NOT NULL);"; //TODO: Maximum players & map_name
    public static final String DB_PARTIES_CREATE = "CREATE TABLE IF NOT EXISTS `" + DB_PARTIES + "`"
            + "  (`leader`           CHAR(36) PRIMARY KEY NOT NULL,"
            + "   `members`          TEXT);";

    public static final String DB_ARENAS_CREATE = "CREATE CREATE TABLE IF NOT EXISTS `" + DB_ARENAS + "`"
            + "  (`map_location`     ";

    // Data
    public static final String LOAD_PLAYER = "SELECT * FROM " + DB_PLAYER + " WHERE uuid = ?;";
    public static final String SAVE_PLAYER = "REPLACE INTO " + DB_PLAYER + " (uuid, party, kit, ultimate, skill, coins, earned_coins, kills, wins) VALUES (?,?,?,?,?,?,?,?,?);";
    public static final String LOAD_PURCHASES = "SELECT * FROM " + DB_PURCHASES + " WHERE uuid = ?;";
    public static final String SAVE_PURCHASES = "REPLACE INTO " + DB_PURCHASES + " (uuid, purchasable) VALUES (?,?);";
    public static final String LOAD_SERVERS = "SELECT `name`, `stage`, `online_players` FROM `" + DB_SERVERS + "`;";
    public static final String SAVE_SERVER = "REPLACE INTO " + DB_SERVERS + " (name, stage, online_players) VALUES (?,?,?);";
    public static final String LOAD_PARTY = "SELECT * FROM " + DB_PARTIES + " WHERE uuid = ?;";
    public static final String SAVE_PARTY = "REPLACE INTO " + DB_PARTIES + " (leader, members) VALUES (?,?);";
}
