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
package com.andrewyunt.warfare.managers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

import com.andrewyunt.warfare.objects.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitScheduler;

import com.andrewyunt.warfare.Warfare;

public class MySQLManager {

	private String database;
	private String user;
	private String pass;
	private int port;
	private Connection connection;
	
	public boolean connect() {
		
		FileConfiguration config = Warfare.getInstance().getConfig();

		String ip = config.getString("database-ip");
		port = config.getInt("database-port");
		database = config.getString("database-name");
		user = config.getString("database-user");
		pass = config.getString("database-pass");
		
		try {
			if (connection != null && !connection.isClosed())
				return true;
			
			synchronized (this) {
				java.lang.Class.forName("com.mysql.jdbc.Driver");
				connection = DriverManager.getConnection("jdbc:mysql://" + ip + ":" + port + "/" + database, user, pass);
			}
		} catch (SQLException | ClassNotFoundException e) {
			return false;
		}
		
		return true;
	}
	
	public void disconnect() {
		
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void updateDB() {
		
		createPlayersTable();
		createPurchasesTable();
		createGameServersTable();
		createPartiesTable();
	}
	
	public void savePlayer(GamePlayer player) {
		
		if (!player.isLoaded())
			return;
		
		String uuid = player.getUUID().toString();
		
		if (Warfare.getInstance().isEnabled()) {
			BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
			scheduler.runTaskAsynchronously(Warfare.getInstance(), () -> savePlayer(player, uuid));
		} else {
			savePlayer(player, uuid);
		}
	}
	
	private void savePlayer(GamePlayer player, String uuid) {
		
		savePurchases(player);
		
		String query = "INSERT INTO Players (uuid, party, kit, ultimate, skill, coins, earned_coins, kills, wins)"
				+ " VALUES (?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE party = VALUES(party), kit = VALUES(kit),"
				+ " ultimate = VALUES(ultimate), skill = VALUES(skill), coins = VALUES(coins),"
				+ " earned_coins = VALUES(earned_coins), kills = VALUES(kills), wins = VALUES(wins);";
		
		PreparedStatement preparedStatement = null;
		
		try {
			preparedStatement = connection.prepareStatement(query);
			
			preparedStatement.setString(1, uuid);
			preparedStatement.setString(2, Warfare.getInstance().getPartyManager().getParty(
					UUID.fromString(uuid)).getLeader().toString());
			preparedStatement.setString(3, player.getSelectedKit() == null ? "none"
					: player.getSelectedKit().toString());
			preparedStatement.setString(4, player.getSelectedUltimate() == null ? "none"
					: player.getSelectedUltimate().toString());
			preparedStatement.setString(5, player.getSelectedSkill() == null ? "none"
					: player.getSelectedSkill().toString());
			preparedStatement.setInt(6, player.getCoins());
			preparedStatement.setInt(7, player.getEarnedCoins());
			preparedStatement.setInt(8, player.getKills());
			preparedStatement.setInt(9, player.getWins());
			
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (preparedStatement != null)
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}
	
	public void loadPlayer(GamePlayer player) {
		
		loadPurchases(player);
		
		UUID uuid = player.getUUID();
		
		BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
		scheduler.runTaskAsynchronously(Warfare.getInstance(), () -> {
			String query = "SELECT * FROM Players WHERE uuid = ?;";
			
			PreparedStatement preparedStatement;
			ResultSet resultSet;
			
			try {
				preparedStatement = connection.prepareStatement(query);
				
				preparedStatement.setString(1, uuid.toString());
				
				resultSet = preparedStatement.executeQuery();
			} catch (SQLException e) {
				return; // player does not exist, so don't load their data
			}
			
			try {
				while (resultSet.next()) {
					loadParty(resultSet.getString("party"));

					String kitStr = resultSet.getString("kit");
					String ultimateStr = resultSet.getString("ultimate");
					String skillStr = resultSet.getString("skill");
					
					if (!kitStr.equals("none"))
						player.setSelectedKit(Kit.valueOf(kitStr));
					if (!ultimateStr.equals("none"))
						player.setSelectedUltimate(Ultimate.valueOf(ultimateStr));
					if (!skillStr.equals("none"))
						player.setSelectedSkill(Skill.valueOf(skillStr));

					player.setCoins(resultSet.getInt("coins"));
					player.setEarnedCoins(resultSet.getInt("earned_coins"));
					player.setKills(resultSet.getInt("kills"));
					player.setWins(resultSet.getInt("wins"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (preparedStatement != null)
					try {
						preparedStatement.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
			}
			
			player.setLoaded(true);
		});
	}
	
	public void savePurchases(GamePlayer player) {
		
		String uuid = player.getUUID().toString();
		
		for (Purchasable purchasable : player.getPurchases()) {
			String query = "INSERT INTO Purchases (uuid, purchasable) VALUES (?,?) ON DUPLICATE KEY UPDATE"
					+ " uuid = VALUES(uuid), purchasable = VALUES(purchasable);";
			
			PreparedStatement preparedStatement = null;
			
			try {
				preparedStatement = connection.prepareStatement(query);
				
				preparedStatement.setString(1, uuid); 
				preparedStatement.setString(2, purchasable.toString());
				
				preparedStatement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (preparedStatement != null)
					try {
						preparedStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
			}
		}
	}
	
	public void loadPurchases(GamePlayer player) {
		
		String query = "SELECT * FROM Purchases WHERE uuid = ?;";
		
		PreparedStatement preparedStatement;
		ResultSet resultSet;
		
		try {
			preparedStatement = connection.prepareStatement(query);
			
			preparedStatement.setString(1, player.getUUID().toString());
			
			resultSet = preparedStatement.executeQuery();
		} catch (SQLException e) {
			return; // player does not exist, so don't load their data
		}
		
		try {
			while (resultSet.next()) {
				String purchasable = resultSet.getString("purchasable");
				
				for (Ultimate ultimate : Ultimate.values())
					if (ultimate.toString().equals(purchasable))
						player.getPurchases().add(Ultimate.valueOf(purchasable));
				
				for (Skill skill : Skill.values())
					if (skill.toString().equals(purchasable))
						player.getPurchases().add(Skill.valueOf(purchasable));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (preparedStatement != null)
				try {
					preparedStatement.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		
		player.setLoaded(true);
	}
	
	public Map<String, Entry<Game.Stage, Integer>> getServers() {
		
		Map<String, Entry<Game.Stage, Integer>> servers = new HashMap<String, Entry<Game.Stage, Integer>>();
		
		String query = "SELECT `name`, `stage`, `online_players` FROM `GameServers`;";
		
		PreparedStatement preparedStatement;
		ResultSet resultSet;
		
		try {
			preparedStatement = connection.prepareStatement(query);
			
			resultSet = preparedStatement.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		
		try {
			while (resultSet.next())
				servers.put(resultSet.getString("name"), new AbstractMap.SimpleEntry<Game.Stage, Integer>(
						Game.Stage.valueOf(resultSet.getString("stage")), resultSet.getInt("online_players")));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return servers;
	}
	
	public void updateServerStatus() {
		
		String query = "INSERT INTO GameServers (name, stage, online_players) VALUES (?,?,?) ON DUPLICATE KEY UPDATE"
				+ " name = VALUES(name), stage = VALUES(stage), online_players = VALUES(online_players);";
		
		PreparedStatement preparedStatement = null;
		
		try {
			preparedStatement = connection.prepareStatement(query);
			
			preparedStatement.setString(1, Warfare.getInstance().getConfig().getString("server-name"));
			preparedStatement.setString(2, Warfare.getInstance().getGame().getStage().toString());
			preparedStatement.setInt(3, Bukkit.getServer().getOnlinePlayers().length);
			
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (preparedStatement != null)
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}

	public void saveParty(Party party) {

		String query = "INSERT INTO Players (leader, members) VALUES (?,?) ON DUPLICATE KEY UPDATE" +
				" leader = VALUES(leader), members = VALUES(members);";

		PreparedStatement preparedStatement = null;

		try {
			preparedStatement = connection.prepareStatement(query);

			preparedStatement.setString(1, party.getLeader().toString());

			String membersStr = "";
			for (Iterator<UUID> iterator = party.getMembers().iterator(); iterator.hasNext();) {
				membersStr += iterator.next().toString() + (iterator.hasNext() ? "," : "");
			}

			preparedStatement.setString(2, membersStr);

			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (preparedStatement != null)
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}

	public Party loadParty(String leaderUUID) {

		String query = "SELECT * FROM Parties WHERE uuid = ?;";

		PreparedStatement preparedStatement;
		ResultSet resultSet;

		try {
			preparedStatement = connection.prepareStatement(query);

			preparedStatement.setString(1, leaderUUID);

			resultSet = preparedStatement.executeQuery();
		} catch (SQLException e) {
			return null;
		}

		Party party = null;

		try {
			while (resultSet.next()) {
				party = new Party(UUID.fromString(resultSet.getString("leader")));

				for (String member : Arrays.asList(resultSet.getString("members").split("\\s*,\\s*"))) {
					party.getMembers().add(UUID.fromString(member));
				}

				return party;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (preparedStatement != null)
				try {
					preparedStatement.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
		}

		return party;
	}
	
	public Map<Integer, Map.Entry<OfflinePlayer, Integer>> getTopFiveColumn(String tableName, String select, String orderBy) {
		
		Map<Integer, Map.Entry<OfflinePlayer, Integer>> highestValues = new HashMap
				<Integer, Map.Entry<OfflinePlayer, Integer>>();
		
		String query = "SELECT `" + select + "`, " + orderBy + " FROM `" + tableName + "` ORDER BY " + orderBy + " DESC LIMIT 5;";
		
		PreparedStatement preparedStatement;
		ResultSet resultSet;
		
		try {
			preparedStatement = connection.prepareStatement(query);
			
			resultSet = preparedStatement.executeQuery();
		} catch (SQLException e) {
			return null;
		}
		
		try {
			int place = 1;
			
			while (resultSet.next()) {
				OfflinePlayer op = Bukkit.getServer().getOfflinePlayer(UUID.fromString(resultSet.getString("uuid")));
				
				highestValues.put(place, new AbstractMap.SimpleEntry<OfflinePlayer, Integer>(op, resultSet.getInt(orderBy)));
				
				place++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return highestValues;
	}
	
	public void createPlayersTable() {

		String query = "CREATE TABLE IF NOT EXISTS `Players`"
				+ "  (`uuid`             CHAR(36) PRIMARY KEY NOT NULL,"
				+ "   `party`            CHAR(36) NOT NULL,"
				+ "   `kit`              CHAR(20) NOT NULL,"
				+ "   `ultimate`         CHAR(20) NOT NULL,"
				+ "   `skill`            CHAR(20) NOT NULL,"
				+ "   `coins`            INT NOT NULL,"
				+ "   `earned_coins`     INT NOT NULL,"
				+ "   `kills`            INT,"
				+ "   `wins`             INT);";
		
		PreparedStatement preparedStatement;
		
		try {
			preparedStatement = connection.prepareStatement(query);
			
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void createPurchasesTable() {
		
		String query = "CREATE TABLE IF NOT EXISTS `Purchases`"
				+ "  (`uuid`             CHAR(36) NOT NULL,"
				+ "   `purchasable`      CHAR(20) NOT NULL,"
				+ "   PRIMARY KEY (`uuid`, `purchasable`));";
		
		PreparedStatement preparedStatement = null;
		
		try {
			preparedStatement = connection.prepareStatement(query);
			
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (preparedStatement != null)
				try {
					preparedStatement.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}
	
	public void createGameServersTable() {
		
		String query = "CREATE TABLE IF NOT EXISTS `GameServers`"
				+ "  (`name`            CHAR(20) PRIMARY KEY NOT NULL,"
				+ "   `stage`           CHAR(20) NOT NULL,"
				+ "   `online_players`  INT NOT NULL);";
		
		PreparedStatement preparedStatement = null;
		
		try {
			preparedStatement = connection.prepareStatement(query);
			
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (preparedStatement != null)
				try {
					preparedStatement.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}

	public void createPartiesTable() {

		String query = "CREATE TABLE IF NOT EXISTS `Parties`"
				+ "  (`leader`            CHAR(36) PRIMARY KEY NOT NULL,"
				+ "   `members`           TEXT);";

		PreparedStatement preparedStatement = null;

		try {
			preparedStatement = connection.prepareStatement(query);

			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (preparedStatement != null)
				try {
					preparedStatement.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}
}