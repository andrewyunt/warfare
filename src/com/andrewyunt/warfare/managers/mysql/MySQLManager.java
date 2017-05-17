/*
 * Unpublished Copyright (c) 2016 Andrew Yunt, All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of Andrew Yunt. The intellectual and technical concepts contained
 * herein are proprietary to Andrew Yunt and may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from Andrew Yunt. Access to the source code contained herein is hereby forbidden to anyone except current Andrew Yunt and those who have executed
 * Confidentiality and Non-disclosure agreements explicitly covering such access.
 *
 * The copyright notice above does not evidence any actual or intended publication or disclosure of this source code, which includes
 * information that is confidential and/or proprietary, and is a trade secret, of COMPANY. ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC PERFORMANCE,
 * OR PUBLIC DISPLAY OF OR THROUGH USE OF THIS SOURCE CODE WITHOUT THE EXPRESS WRITTEN CONSENT OF ANDREW YUNT IS STRICTLY PROHIBITED, AND IN VIOLATION OF
 * APPLICABLE LAWS AND INTERNATIONAL TREATIES. THE RECEIPT OR POSSESSION OF THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS
 * TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT MAY DESCRIBE, IN WHOLE OR IN PART.
 */
package com.andrewyunt.warfare.managers.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.andrewyunt.warfare.configuration.StaticConfiguration;
import com.andrewyunt.warfare.exception.SignException;
import com.andrewyunt.warfare.objects.*;
import com.google.common.base.Joiner;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;

import com.andrewyunt.warfare.Warfare;

public class MySQLManager {

	private ComboPooledDataSource cpds;

	public Connection getConnection() throws SQLException {

		return cpds.getConnection();
	}

	public boolean connect() {

		FileConfiguration config = Warfare.getInstance().getConfig();
		String hostname = config.getString("database-ip");
		int port = config.getInt("database-port");
		String database = config.getString("database-name");
		String username = config.getString("database-user");
		String password = config.getString("database-pass");

		try {
            cpds = new ComboPooledDataSource();
			cpds.setDriverClass("com.mysql.jdbc.Driver");
			cpds.setJdbcUrl( "jdbc:mysql://" + hostname + ":" + port + "/" + database);
			cpds.setUser(username);
			cpds.setPassword(password);
			getConnection();
		} catch (Exception exception) {
			handleException(exception);
			return false;
		}

		cpds.setMaxPoolSize( 16 );
		return true;
	}

	public void disconnect() {

		cpds.close();
	}

	public void handleException(Exception exception) {

		Warfare.getInstance().getLogger().log(Level.SEVERE, "Exception occurred in MySQL manager ", exception);
	}

	public void updateDB() {

		createPlayersTable();
		createPurchasesTable();
		createGameServersTable();
		createPartiesTable();
		createSignsTable();
		createArenasTable();
	}

	public void savePlayerAsync(GamePlayer player) {

        if (player.isLoaded()) {
            if (Warfare.getInstance().isEnabled()) {
                Bukkit.getScheduler().runTaskAsynchronously(Warfare.getInstance(), () -> savePlayer(player));
            } else {
                savePlayer(player);
            }
        }
	}

	public void savePlayer(GamePlayer player) {

		savePurchases(player);
		try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SQLStatements.SAVE_PLAYER)) {
			preparedStatement.setString(1, player.getUUID().toString());
			Party party = Warfare.getInstance().getPartyManager().getParty(player.getUUID());
			preparedStatement.setString(2, party == null ? "none" : party.getLeader().toString());
			preparedStatement.setString(3, player.getSelectedKit() == null ? "none"
					: player.getSelectedKit().toString());
			preparedStatement.setInt(4, player.getCoins());
			preparedStatement.setInt(5, player.getEarnedCoins());
			preparedStatement.setInt(6, player.getKills());
			preparedStatement.setInt(7, player.getWins());

			preparedStatement.executeUpdate();
		} catch (SQLException exception) {
			handleException(exception);
		}
	}

	public void loadPlayerAsync(GamePlayer gamePlayer) {

        Bukkit.getScheduler().runTaskAsynchronously(Warfare.getInstance(), () -> loadPlayer(gamePlayer));
    }

	public void loadPlayer(GamePlayer gamePlayer) {

        loadPurchases(gamePlayer);

        try (Connection connection = getConnection();PreparedStatement preparedStatement = connection.prepareStatement(SQLStatements.LOAD_PLAYER)) {
            preparedStatement.setString(1, gamePlayer.getUUID().toString());
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                if (!resultSet.getString("party").equals("none")) {
                    loadParty(UUID.fromString(resultSet.getString("party")));
                }

                String kitStr = resultSet.getString("kit");

                if (!kitStr.equals("none")) {
                    gamePlayer.setSelectedKit(Kit.valueOf(kitStr));
                }

                gamePlayer.setCoins(resultSet.getInt("coins"));
                gamePlayer.setEarnedCoins(resultSet.getInt("earned_coins"));
                gamePlayer.setKills(resultSet.getInt("kills"));
                gamePlayer.setWins(resultSet.getInt("wins"));
            }

            gamePlayer.setLoaded(true);
        } catch (SQLException exception) {
            handleException(exception);
        }
    }

	public void savePurchases(GamePlayer gamePlayer) {

	    try (Connection connection = getConnection()) {
            for (Purchasable purchasable : gamePlayer.getPurchases()) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(SQLStatements.SAVE_PURCHASES)) {
                    preparedStatement.setString(1, gamePlayer.getUUID().toString());
                    preparedStatement.setString(2, purchasable.toString());
                    preparedStatement.execute();
                }
            }
        } catch (SQLException exception) {
	        handleException(exception);
        }
    }

	public void loadPurchases(GamePlayer gamePlayer) {

        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SQLStatements.LOAD_PURCHASES)) {
            preparedStatement.setString(1, gamePlayer.getUUID().toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String purchasable = resultSet.getString("purchasable");

            }
        } catch (SQLException exception) {
            handleException(exception);
        }
    }

    public List<Server> getServers(){
        List<Server> servers = new ArrayList<>();
        try(Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SQLStatements.LOAD_SERVERS); ResultSet resultSet = preparedStatement.executeQuery()){
            while (resultSet.next()) {
                servers.add(new Server(
                        resultSet.getString("name"),
                        Server.ServerType.valueOf(resultSet.getString("type")),
                        Game.Stage.valueOf(resultSet.getString("stage")),
                        "",
                        resultSet.getInt("online_players"),
                        resultSet.getInt("max_players")
                ));
            }
        } catch (SQLException exception) {
            handleException(exception);
        }
        return servers;
    }

	public void updateServerStatus() {
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SQLStatements.SAVE_SERVER)) {
            preparedStatement.setString(1, StaticConfiguration.SERVER_NAME);
            preparedStatement.setString(2, (StaticConfiguration.LOBBY ? Server.ServerType.LOBBY : Server.ServerType.GAME).name());
            preparedStatement.setString(3, Warfare.getInstance().getGame().getStage().toString());
            preparedStatement.setString(4, ""); //TODO: Arena name
            preparedStatement.setInt(5, Warfare.getInstance().getGame().getPlayers().size());
            preparedStatement.setInt(6, Warfare.getInstance().getGame().getCages().size());
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            handleException(exception);
        }
    }

	public void saveParty(Party party) {

	    try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SQLStatements.SAVE_PARTY)) {
            preparedStatement.setString(1, party.getLeader().toString());
            StringBuilder membersStr = new StringBuilder();
            for (Iterator<UUID> iterator = party.getMembers().iterator(); iterator.hasNext();) {
                membersStr.append(iterator.next().toString()).append(iterator.hasNext() ? "," : "");
            }

            preparedStatement.setString(2, membersStr.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
	        handleException(exception);
        }
    }

	public Party loadParty(UUID leaderUUID) {

	    try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SQLStatements.LOAD_PARTY)) {
            preparedStatement.setString(1, leaderUUID.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Party party = new Party(UUID.fromString(resultSet.getString("leader")));
                for (String member : Arrays.asList(resultSet.getString("members").split("\\s*,\\s*"))) {
                    party.getMembers().add(UUID.fromString(member));
                }
                return party;
            }
        } catch (SQLException exception) {
	        handleException(exception);
        }
        return null;
    }

    public void saveSign(SignDisplay signDisplay) {

        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SQLStatements.SAVE_SIGN)) {
            preparedStatement.setString(1, StaticConfiguration.SERVER_NAME);
            preparedStatement.setString(2, serializeLocation(signDisplay.getBukkitSign().getLocation()));
            preparedStatement.setString(3, signDisplay.getType().name());
            preparedStatement.setInt(4, signDisplay.getPlace());
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            handleException(exception);
        }
    }

    public void deleteSign(SignDisplay signDisplay) {
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SQLStatements.DELETE_SIGN)) {
            preparedStatement.setString(1, StaticConfiguration.SERVER_NAME);
            preparedStatement.setString(2, serializeLocation(signDisplay.getBukkitSign().getLocation()));
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            handleException(exception);
        }
    }

    public void loadSigns() {

        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SQLStatements.LOAD_SIGNS)) {
            preparedStatement.setString(1, StaticConfiguration.SERVER_NAME);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                try {
                    Warfare.getInstance().getSignManager().createSign(
                            deserializeLocation(resultSet.getString("location")),
                            SignDisplay.Type.valueOf(resultSet.getString("type")),
                            resultSet.getInt("place"),
                            true);
                } catch (SignException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException exception) {
            handleException(exception);
        }
    }

    private String serializeArray(Set<String> deserialized){
        return "[" + Joiner.on(",").join(deserialized) + "]";
    }

    private Set<String> deserializeArray(String serialized){
        serialized = serialized.substring(1, serialized.length() - 1);
        if(serialized.isEmpty()){
            return Collections.emptySet();
        }
        String[] split = serialized.split(",");
        return new HashSet<>(Arrays.asList(split));
    }

    private String serializeEntry(String[] entry){
        return "{" + Joiner.on(":").join(entry) + "}";
    }

    private String[] deserializeEntry(String serialized){
        serialized = serialized.substring(1, serialized.length() - 1);
        return serialized.split(":");
    }

    private String serializeLocation(Location deserialized) {

        return "[" + deserialized.getWorld().getName() + ";" + deserialized.getBlockX() + ";" + deserialized.getBlockY() + ";" + deserialized.getBlockZ() + "]";
    }

    private Location deserializeLocation(String serialized) {

        serialized = serialized.substring(1, serialized.length() - 1);
        String[] split = serialized.split(";");

        return new Location(
                Bukkit.getWorld(split[0]),
                Integer.parseInt(split[1]),
                Integer.parseInt(split[2]),
                Integer.parseInt(split[3]));
    }

    public void saveArena() {

        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SQLStatements.SAVE_ARENA)) {
            Arena arena = Warfare.getInstance().getArena();
            preparedStatement.setString(1, StaticConfiguration.SERVER_NAME);
            preparedStatement.setString(2, arena.getMapLocation() == null ? "none" : serializeLocation(arena.getMapLocation()));
            preparedStatement.setString(3, serializeArray(
                    arena.getCageLocations().entrySet()
                            .stream()
                            .map(entry -> serializeEntry(new String[]{
                                    entry.getKey(),
                                    serializeLocation(entry.getValue())
                            }))
                            .collect(Collectors.toSet())
            ));
            preparedStatement.setString(4, serializeArray(
                    arena.getLootChests()
                            .stream()
                            .map(chest -> serializeEntry(new String[]{
                                    String.valueOf(chest.getTier()),
                                    serializeLocation(chest.getLocation())
                            }))
                            .collect(Collectors.toSet())
            ));
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            handleException(exception);
        }
    }

    public Arena loadArena() {

        Arena arena = new Arena();

        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SQLStatements.LOAD_ARENA)) {
            preparedStatement.setString(1, StaticConfiguration.SERVER_NAME);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                arena.setMapLocation(deserializeLocation(resultSet.getString("map_location")));
                String cages = resultSet.getString("cages");
                if(!cages.isEmpty()) {
                    Set<String> array = deserializeArray(cages);
                    if(!array.isEmpty()) {
                        array.stream()
                                .map(this::deserializeEntry)
                                .forEach(entry -> arena.addCageLocation(entry[0], deserializeLocation(entry[1])));
                    }
                }
                String lootchests = resultSet.getString("loot_chests");
                if(!lootchests.isEmpty()) {
                    arena.setLootChests(deserializeArray(lootchests)
                            .stream()
                            .map(this::deserializeEntry)
                            .map(array -> new LootChest(deserializeLocation(array[1]), (byte) Integer.parseInt(array[0])))
                            .collect(Collectors.toSet()));
                }

            }
        } catch (SQLException exception) {
            handleException(exception);
        }

        return arena;
    }

	//TODO: REDO query selection
	@Deprecated
	public Map<Integer, Map.Entry<OfflinePlayer, Integer>> getTopFiveColumn(String tableName, String select, String orderBy) {

		Map<Integer, Map.Entry<OfflinePlayer, Integer>> highestValues = new HashMap<>();
		String query = "SELECT `" + select + "`, " + orderBy + " FROM `" + tableName + "` ORDER BY " + orderBy + " DESC LIMIT 5;";
		PreparedStatement preparedStatement;
		ResultSet resultSet;
		Connection connection = null;

		try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();
			int place = 1;

			while (resultSet.next()) {
				OfflinePlayer op = Bukkit.getServer().getOfflinePlayer(UUID.fromString(resultSet.getString("uuid")));

				highestValues.put(place, new AbstractMap.SimpleEntry<>(op, resultSet.getInt(orderBy)));

				place++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		    if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
		return highestValues;
	}

	public void createPlayersTable() {

	    try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SQLStatements.DB_PLAYERS_CREATE)){
	        preparedStatement.executeUpdate();
        } catch (SQLException exception) {
	        handleException(exception);
        }
    }

    public void createPurchasesTable() {

	    try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SQLStatements.DB_PURCHASES_CREATE)){
            preparedStatement.execute();
        } catch (SQLException exception) {
            handleException(exception);
        }
    }

    public void createGameServersTable() {

        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SQLStatements.DB_SERVERS_CREATE)){
            preparedStatement.execute();
        } catch (SQLException exception) {
            handleException(exception);
        }
    }

    public void createPartiesTable() {

        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SQLStatements.DB_PARTIES_CREATE)){
            preparedStatement.execute();
        } catch (SQLException exception) {
            handleException(exception);
        }
    }

    public void createSignsTable() {

        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SQLStatements.DB_SIGNS_CREATE)){
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            handleException(exception);
        }
    }

    public void createArenasTable() {

        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SQLStatements.DB_ARENAS_CREATE)){
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            handleException(exception);
        }
    }
}