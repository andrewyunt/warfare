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
package com.andrewyunt.skywarfare.objects;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import com.andrewyunt.skywarfare.SkyWarfare;
import com.andrewyunt.skywarfare.objects.utilities.Utils;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * The class used to store game attributes, placed blocks, and players.
 * 
 * @author Andrew Yunt
 */
public class Game {
	
	public enum Stage {
		WAITING,
		BATTLE,
		END,
		RESTART
	}
	
	private short countdownTime = 10;
	private Stage stage = Stage.WAITING;
	
	private final Set<GamePlayer> players = new HashSet<GamePlayer>();
	private final Set<Cage> cages = new HashSet<Cage>();
	
	public Game() {
		
		for (Entry<String, Location> entry : SkyWarfare.getInstance().getArena().getCageLocations().entrySet())
			cages.add(new Cage(entry.getKey(), entry.getValue()));
	}
	
	/**
	 * Adds a player to the game and sets their attributes.
	 * 
	 * @param player
	 * 		The player to be added to the game.
	 */
	public void addPlayer(GamePlayer player) {
		
		// Add the player to the players set
		players.add(player);
		
		if (stage == Stage.WAITING) {
			// Get the player's bukkit player
			Player bp = player.getBukkitPlayer();
			
			// Set player's mode to survival
			bp.setGameMode(GameMode.SURVIVAL);
			
			// Update the player's scoreboard
			player.updateDynamicScoreboard();
			
			// Send the join message to the players, delayed for disguises
			BukkitScheduler scheduler = SkyWarfare.getInstance().getServer().getScheduler();
			scheduler.scheduleSyncDelayedTask(SkyWarfare.getInstance(), () -> {
				for (Cage cage : getAvailableCages()) {
					cage.setPlayer(player);
					break;
				}
				
				if (getAvailableCages().size() == 0)
					setStage(Stage.BATTLE);
				
				Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&',
						String.format("&7%s &ehas joined (&b%s&e/&b%s&e)!", bp.getDisplayName(),
								players.size(), cages.size())));
			}, 5L);
		}
	}
	
	public void removePlayer(GamePlayer player) {
		
		players.remove(player);
		
		if (stage == Stage.WAITING)
			Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&',
					String.format("&7%s &ehas quit!", player.getBukkitPlayer().getDisplayName())));
		else
			checkPlayers();
	}
	
	public void checkPlayers() {
		
		if (players.size() == 1 && (stage != Stage.WAITING && stage != Stage.END && stage != Stage.RESTART))
			setStage(Stage.END);
		else if (players.size() == 0 && stage != Stage.WAITING)
			setStage(Stage.RESTART);
	}
	
	/**
	 * Gets all players currently in the game.
	 * 
	 * @returns
	 * 		A set of players currently in the game.
	 */
	public Set<GamePlayer> getPlayers() {
		
		return players;
	}
	
	public Set<Cage> getCages() {
		
		return cages;
	}
	
	public Set<Cage> getAvailableCages() {
		
		Set<Cage> availableCages = new HashSet<Cage>();
		
		for (Cage cage : cages)
			if (!cage.hasPlayer())
				availableCages.add(cage);
		
		return availableCages;
	}
	
	public Set<GamePlayer> getSpectators() {
		
		Set<GamePlayer> spectators = new HashSet<GamePlayer>(SkyWarfare.getInstance().getPlayerManager().getPlayers());
		
		for (GamePlayer player : players)
			spectators.remove(player);
		
		return spectators;
	}
	
	public void start() {
		
		for (Entity entity : SkyWarfare.getInstance().getArena().getMapLocation().getWorld().getEntities())
			if (entity.getType() != EntityType.PLAYER)
				entity.remove();
		
		// Update player name colors
		for (GamePlayer player : players)
			Utils.colorPlayerName(player, SkyWarfare.getInstance().getGame().getPlayers());
		
		for (Cage cage : cages)
			cage.destroy();
	}
	
	/**
	 * Method is executed to end the game.
	 */
	public void end() {
		
		for (GamePlayer player : players) {
			Bukkit.getServer().broadcastMessage(ChatColor.GOLD + String.format(
					"%s has won the game!", player.getBukkitPlayer().getDisplayName()));
			
			int winCoins = 200;
			
			if (player.getBukkitPlayer().hasPermission("skywarfare.coins.double"))
				winCoins = 400;
			
			if (player.getBukkitPlayer().hasPermission("skywarfare.coins.triple"))
				winCoins = 600;
			
			player.setCoins(player.getCoins() + winCoins);
			player.setWins(player.getWins() + 1);
			
			player.getBukkitPlayer().sendMessage(ChatColor.GOLD + String.format("You earned %s for winning the game.",
					ChatColor.AQUA + String.valueOf(winCoins) + "coins" + ChatColor.GREEN));
		}
		
		BukkitScheduler scheduler = SkyWarfare.getInstance().getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(SkyWarfare.getInstance(), new Runnable() {
			@Override
			public void run() {
				
				setStage(Stage.RESTART);
			}
		}, 300L);
	}
	
	public Stage getStage() {
		
		return stage;
	}
	
	public void setStage(Stage stage) {
		
		this.stage = stage;
		
		BukkitScheduler scheduler = SkyWarfare.getInstance().getServer().getScheduler();
		
		if (stage == Stage.BATTLE) {
			
			start();
			
		} else if (stage == Stage.END) {
			
			end();
			
			scheduler.scheduleSyncDelayedTask(SkyWarfare.getInstance(), new Runnable() {
				@Override
				public void run() {
					
					setStage(Stage.RESTART);
				}
			}, 200L);
			
		} else if (stage == Stage.RESTART) {
			
			String lobbyServerName = SkyWarfare.getInstance().getConfig().getString("lobby-server");
			
			for (GamePlayer player : SkyWarfare.getInstance().getPlayerManager().getPlayers()) {
				SkyWarfare.getInstance().getDataSource().savePlayer(player);
				
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeUTF("Connect");
				out.writeUTF(lobbyServerName);
				player.getBukkitPlayer().sendPluginMessage(SkyWarfare.getInstance(), "BungeeCord", out.toByteArray());
			}
			
			scheduler.scheduleSyncDelayedTask(SkyWarfare.getInstance(), new Runnable() {
				@Override
				public void run() {
					
					SkyWarfare.getInstance().getServer().shutdown();
				}
			}, 100L);
		}
	}
	
	public void runCountdownTimer() {
		
		BukkitScheduler scheduler = SkyWarfare.getInstance().getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(SkyWarfare.getInstance(), new Runnable() {
			@Override
			public void run() {
				
				countdownTime--;
				
				checkCountdownTime();
			}
		}, 0L, 20L);
	}
	
	public void checkCountdownTime() {
		
		if (countdownTime > 0)
			Bukkit.getServer().broadcastMessage(ChatColor.RED + String.format("The game will start in %s seconds.",
					countdownTime));
		else if (countdownTime == 0)
			setStage(Stage.BATTLE);
	}
}