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
package com.andrewyunt.skywarfare.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitScheduler;

import com.andrewyunt.skywarfare.SkyWarfare;
import com.andrewyunt.skywarfare.objects.CustomClass;
import com.andrewyunt.skywarfare.objects.GamePlayer;
import com.andrewyunt.skywarfare.objects.Kit;
import com.andrewyunt.skywarfare.objects.Purchasable;
import com.andrewyunt.skywarfare.objects.Skill;
import com.andrewyunt.skywarfare.objects.Ultimate;

public class MySQLSource extends DataSource {
	
	private String ip, database, user, pass;
	private int port;
	private Connection connection;
	private PreparedStatement preparedStatement;
	
	@Override
	public boolean connect() {
		
		FileConfiguration config = SkyWarfare.getInstance().getConfig();
		
		ip = config.getString("database-ip");
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
	
	@Override
	public void disconnect() {
		
		try {
			connection.close();
		} catch (SQLException e) {
		}
	}
	@Override
	public void savePlayer(GamePlayer player) {
		
		if (!player.isLoaded())
			return;
		
		String uuid = player.getUUID().toString();
		
		if (SkyWarfare.getInstance().isEnabled()) {
			BukkitScheduler scheduler = SkyWarfare.getInstance().getServer().getScheduler();
			scheduler.runTaskAsynchronously(SkyWarfare.getInstance(), () -> {
				savePlayer(player, uuid);
			});
		} else
			savePlayer(player, uuid);
	}
	
	private void savePlayer(GamePlayer player, String uuid) {
		
		savePurchases(player);
		saveClasses(player);
		
		String query = "INSERT INTO Players (uuid, class, coins, earned_coins, kills, wins)"
				+ " VALUES (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE class = VALUES(class), coins = VALUES(coins),"
				+ " earned_coins = VALUES(earned_coins), kills = VALUES(kills), wins = VALUES(wins);";
		
		try {
			preparedStatement = connection.prepareStatement(query);
			
			preparedStatement.setString(1, uuid);
			preparedStatement.setString(2, player.getCustomClass() == null ? "none"
					: player.getCustomClass().getName());
			preparedStatement.setInt(3, player.getCoins());
			preparedStatement.setInt(4, player.getEarnedCoins());
			preparedStatement.setInt(5, player.getKills());
			preparedStatement.setInt(6, player.getWins());
			
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
	
	@Override
	public void loadPlayer(GamePlayer player) {
		
		loadPurchases(player);
		loadClasses(player);
		
		String uuid = player.getUUID().toString();
		
		BukkitScheduler scheduler = SkyWarfare.getInstance().getServer().getScheduler();
		scheduler.runTaskAsynchronously(SkyWarfare.getInstance(), () -> {
			ResultSet resultSet = null;
			
			String query = "SELECT * FROM Players WHERE uuid = ?;";
			
			try {
				preparedStatement = connection.prepareStatement(query);
				
				preparedStatement.setString(1, uuid);
				
				resultSet = preparedStatement.executeQuery();
			} catch (SQLException e) {
				return; // player does not exist, so don't load their data
			}
			
			try {
				while (resultSet.next()) {
					String classStr = resultSet.getString("class");
					
					if (!classStr.equals("none"))
						player.setCustomClass(player.getCustomClass(classStr));
					
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
	
	@Override
	public void savePurchases(GamePlayer player) {
		
		String uuid = player.getUUID().toString();
		
		for (Purchasable purchasable : player.getPurchases()) {
			String query = "INSERT INTO Purchases (uuid, purchasable) VALUES (?,?) ON DUPLICATE KEY UPDATE"
					+ " uuid = VALUES(uuid), purchasable = VALUES(purchasable);";
			
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
	
	@Override
	public void loadPurchases(GamePlayer player) {
		
		ResultSet resultSet = null;
		
		String query = "SELECT * FROM Purchases WHERE uuid = ?;";
		
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
				
				for (Kit kit : Kit.values())
					if (kit.toString().equals(purchasable))
						player.getPurchases().add(Kit.valueOf(purchasable));
				
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
	
	@Override
	public void saveClasses(GamePlayer player) {
		
		String uuid = player.getUUID().toString();
		
		for (CustomClass customClass : player.getCustomClasses()) {

			String query = "INSERT INTO Classes (uuid, name, kit, ultimate, skill_one, skill_two)"
					+ " VALUES (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE uuid = VALUES(uuid),"
					+ " name = VALUES(name), kit = VALUES(kit), ultimate = VALUES(ultimate),"
					+ " skill_one = VALUES(skill_one), skill_two = VALUES(skill_two);";
			
			try {
				preparedStatement = connection.prepareStatement(query);
				
				preparedStatement.setString(1, uuid);
				preparedStatement.setString(2, customClass.getName());
				preparedStatement.setString(3, customClass.getKit().toString());
				preparedStatement.setString(4, customClass.getUltimate().toString());
				preparedStatement.setString(5, customClass.getSkillOne().toString());
				preparedStatement.setString(6, customClass.getSkillTwo().toString());
				
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
	
	@Override
	public void loadClasses(GamePlayer player) {
		
		ResultSet resultSet = null;
		
		String query = "SELECT * FROM Classes WHERE uuid = ?;";
		
		try {
			preparedStatement = connection.prepareStatement(query);
			
			preparedStatement.setString(1, player.getUUID().toString());
			
			resultSet = preparedStatement.executeQuery();
		} catch (SQLException e) {
			return; // player does not exist, so don't load their data
		}
		
		try {
			while (resultSet.next()) {
				CustomClass customClass = new CustomClass();
				customClass.setName(resultSet.getString("name"));
				customClass.setKit(Kit.valueOf(resultSet.getString("kit")));
				customClass.setUltimate(Ultimate.valueOf(resultSet.getString("ultimate")));
				customClass.setSkillOne(Skill.valueOf(resultSet.getString("skill_one")));
				customClass.setSkillTwo(Skill.valueOf(resultSet.getString("skill_two")));
				player.getCustomClasses().add(customClass);
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
	
	@Override
	public Map<Integer, Map.Entry<OfflinePlayer, Integer>> getMostKills() {
		
		Map<Integer, Map.Entry<OfflinePlayer, Integer>> mostKills = new HashMap
				<Integer, Map.Entry<OfflinePlayer, Integer>>();
		
		String query = "SELECT `uuid`, `kills` FROM `Players` ORDER BY `kills` DESC LIMIT 5;";
		ResultSet resultSet = null;
		
		try {
			resultSet = preparedStatement.executeQuery(query);
		} catch (SQLException e) {
			return null;
		}
		
		try {
			int place = 1;
			
			while (resultSet.next()) {
				OfflinePlayer op = Bukkit.getServer().getOfflinePlayer(UUID.fromString(resultSet.getString("uuid")));
				
				mostKills.put(place, new AbstractMap.SimpleEntry<OfflinePlayer, Integer>(op, resultSet.getInt("kills")));
				
				place++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return mostKills;
	}
	
	@Override
	public void createPlayersTable() {

		String query = "CREATE TABLE IF NOT EXISTS `Players`"
				+ "  (`uuid`             CHAR(36) PRIMARY KEY NOT NULL,"
				+ "   `class`            CHAR(20) NOT NULL,"
				+ "   `coins`            INT,"
				+ "   `earned_coins`     INT,"
				+ "   `kills`            INT,"
				+ "   `wins`             INT);";
		
		try {
			preparedStatement = connection.prepareStatement(query);
			
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void createPurchasesTable() {
		
		String query = "CREATE TABLE IF NOT EXISTS `Purchases`"
				+ "  (`uuid`             CHAR(36) NOT NULL,"
				+ "   `purchasable`      CHAR(20) NOT NULL,"
				+ "   PRIMARY KEY (`uuid`, `purchasable`));";
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
	
	@Override
	public void createClassesTable() {
		
		String query = "CREATE TABLE IF NOT EXISTS `Classes`"
				+ "  (`uuid`             CHAR(36) NOT NULL,"
				+ "   `name`             CHAR(20) NOT NULL,"
				+ "   `kit`              CHAR(20) NOT NULL,"
				+ "   `ultimate`         CHAR(20) NOT NULL,"
				+ "   `skill_one`        CHAR(20) NOT NULL,"
				+ "   `skill_two`        CHAR(20) NOT NULL,"
				+ "    PRIMARY KEY (`uuid`, `name`));";
		
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