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
package com.andrewyunt.warfare.objects;

import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.andrewyunt.warfare.configuration.StaticConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.utilities.Utils;

/**
 * The class used to store game attributes, placed blocks, and players.
 * 
 * @author Andrew Yunt
 */
public class Game {
	
	public enum Stage {
		WAITING,
		COUNTDOWN,
		BATTLE,
		END,
		RESTART
	}
	
	private short countdownTime = 10, refillCountdownTime = 300;
	private Stage stage = Stage.WAITING;
	
	private final Set<GamePlayer> players = new HashSet<GamePlayer>();
	private final Set<Cage> cages = new HashSet<Cage>();
	
	public Game() {
		
		for (Entry<String, Location> entry : Warfare.getInstance().getArena().getCageLocations().entrySet())
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

		// Get the player's bukkit player
		Player bp = player.getBukkitPlayer();

		// Set player's mode to survival
		bp.setGameMode(GameMode.SURVIVAL);

		BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(Warfare.getInstance(), () -> {
			for (Cage cage : getAvailableCages()) {
				cage.setPlayer(player);
				break;
			}

			if (getAvailableCages().size() == 0)
				setStage(Stage.COUNTDOWN);

			// Send the join message to the players
			Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&',
					String.format("&7%s &6has joined &7(&6%s&7/&6%s&7)!", bp.getDisplayName(),
							players.size(), cages.size())));
		}, 5L);
	}
	
	public void removePlayer(GamePlayer player) {
		
		players.remove(player);
		
		if (stage == Stage.WAITING) {
			player.getCage().setPlayer(null);
			
			Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&',
					String.format("&7%s &6has quit!", player.getBukkitPlayer().getDisplayName())));
		} else
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
		
		Set<GamePlayer> spectators = new HashSet<GamePlayer>(Warfare.getInstance().getPlayerManager().getPlayers());
		
		for (GamePlayer player : players)
			spectators.remove(player);
		
		return spectators;
	}
	
	public void start() {
		
		for (Entity entity : Warfare.getInstance().getArena().getMapLocation().getWorld().getEntities())
			if (entity.getType() != EntityType.PLAYER)
				entity.remove();
		
		// Destroy cages
		for (Cage cage : cages)
			cage.destroy();
		
		for (GamePlayer player : players) {
			Player bp = player.getBukkitPlayer();
			
			// Update player's name color
			Utils.colorPlayerName(player, Warfare.getInstance().getGame().getPlayers());
			
			// Give player speed 2 for 10 seconds if they have HEAD_START Skill
			bp.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 2));
			
			// Clear player's inventory to remove class selector
			bp.getInventory().clear();
			
			// Give player kit items
			player.getSelectedKit().giveItems(player);
			
			// Close player's inventory to keep them from using the class selector in-game
			bp.closeInventory();
			
			// Set player's health
			List<Purchasable> purchases = player.getPurchases();
			double health = 24;
			
			if (purchases.contains(HealthBoost.HEALTH_BOOST_I))
				health = 26;
			else if (purchases.contains(HealthBoost.HEALTH_BOOST_II))
				health = 28;
			else if (purchases.contains(HealthBoost.HEALTH_BOOST_III))
				health = 30;
			
			bp.setMaxHealth(health);
			bp.setHealth(health);
		}
		
		// Fill chests
		fillChests();
		
		// Start chest refill timer
		runRefillTimer();
	}
	
	/**
	 * Method is executed to end the game.
	 */
	public void end() {
		
		// Only one player should be in-game when the game ends
		for (GamePlayer player : players) {
			Bukkit.getServer().broadcastMessage(ChatColor.RED + ChatColor.BOLD.toString() +
					String.format("%s has won the game!", player.getBukkitPlayer().getDisplayName()));
			
			int winCoins = 200;
			
			if (player.getBukkitPlayer().hasPermission("Warfare.coins.double"))
				winCoins = 400;
			
			if (player.getBukkitPlayer().hasPermission("Warfare.coins.triple"))
				winCoins = 600;
			
			player.setCoins(player.getCoins() + winCoins);
			player.setWins(player.getWins() + 1);
			
			player.getBukkitPlayer().sendMessage(ChatColor.GOLD + String.format(
					"You earned %s coins for winning the game.",
					String.valueOf(winCoins)));
		}

		Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "Thanks for playing!");

		BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(Warfare.getInstance(), () -> setStage(Stage.RESTART), 200L);
	}
	
	public Stage getStage() {
		
		return stage;
	}
	
	public void setStage(Stage stage) {
		
		this.stage = stage;
		Warfare.getInstance().getMySQLManager().updateServerStatus();
		
		BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
		
		if (stage == Stage.COUNTDOWN) {
			
			runCountdownTimer();
			
		} else if (stage == Stage.BATTLE) {
			
			start();
			
		} else if (stage == Stage.END) {
			
			end();
			
			scheduler.scheduleSyncDelayedTask(Warfare.getInstance(), () -> setStage(Stage.RESTART), 200L);
			
		} else if (stage == Stage.RESTART) {
			
			for (GamePlayer player : Warfare.getInstance().getPlayerManager().getPlayers()) {
				Warfare.getInstance().getMySQLManager().savePlayer(player);
				
				if (Warfare.getInstance().getArena().isEdit())
					if (player.getBukkitPlayer().hasPermission("Warfare.edit"))
						continue;

				Utils.sendPlayerToServer(player.getBukkitPlayer(), StaticConfiguration.LOBBY_SERVER);
			}
			
			if (Warfare.getInstance().getArena().isEdit())
				return;
			
			scheduler.scheduleSyncDelayedTask(Warfare.getInstance(), () -> Warfare.getInstance().getServer().shutdown(), 100L);
		}
	}
	
	public void runCountdownTimer() {
		
		BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(Warfare.getInstance(), () -> {
            countdownTime--;

            checkCountdownTime();
        }, 0L, 20L);
	}
	
	public void checkCountdownTime() {
		
		if (countdownTime > 0) {
			Bukkit.getServer().broadcastMessage(ChatColor.RED + String.format("The game will start in %s seconds.",
					countdownTime));
		} else if (countdownTime == 0)
			setStage(Stage.BATTLE);
	}

	public short getCountdownTime() {
		
		return countdownTime;
	}
	
	public void fillChests() {
		
		for (LootChest lootChest : Warfare.getInstance().getArena().getLootChests()) {
			if (lootChest.getLocation().getBlock().getType() != Material.CHEST)
				continue;
			
			lootChest.fill();
		}
	}
	
	public void runRefillTimer() {
		
		BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(Warfare.getInstance(), () -> {
            refillCountdownTime--;

            checkRefillTime();
        }, 0L, 20L);
	}
	
	public void checkRefillTime() {
		
		if (refillCountdownTime == 0) {
			fillChests();
			
			refillCountdownTime = 300;
			
			Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "Chests have been refilled. Next refill is in 5 minutes.");
		}
	}
	
	public short getRefillCountdownTime() {
		
		return refillCountdownTime;
	}
}