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
import java.util.UUID;
import java.util.stream.Collectors;

import com.andrewyunt.warfare.configuration.StaticConfiguration;
import org.bukkit.*;
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
        COUNTDOWN(1, DyeColor.GREEN, ChatColor.GREEN, "Starting"),
        WAITING(0, DyeColor.YELLOW, ChatColor.YELLOW, "Waiting for players"),
		BATTLE(2, DyeColor.RED, ChatColor.RED, "Game in progress"),
		END(3, DyeColor.RED, ChatColor.RED, "Game finished"),
		RESTART(4, DyeColor.RED, ChatColor.RED, "Server restarting");

        private final int order;
		private final DyeColor dyeColor;
		private final ChatColor color;
		private final String description;

        Stage(int order, DyeColor dyeColor, ChatColor color, String description) {
            this.order = order;
            this.dyeColor = dyeColor;
            this.color = color;
            this.description = description;
        }

        public DyeColor getDyeColor() {
            return dyeColor;
        }

        public ChatColor getColor() {
            return color;
        }

        public String getDescription() {
            return description;
        }

        public String getDisplay(){
            return color + description;
        }

        public int getOrder() {
            return order;
        }
    }
	
	private short countdownTime = 10, refillCountdownTime = 300;
	private Stage stage = Stage.WAITING;
	
	private final Set<GamePlayer> players = new HashSet<GamePlayer>();
	private final Set<Cage> cages = new HashSet<Cage>();
	
	public Game() {

		Arena arena = Warfare.getInstance().getArena();
		
		for (Entry<String, Location> entry : arena.getCageLocations().entrySet()) {
			cages.add(new Cage(entry.getKey(), entry.getValue()));
		}

		BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(Warfare.getInstance(), () -> arena.getMapLocation().getWorld().setTime(6000), 20L, 0L);
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
			getAvailableCages().iterator().next().setPlayer(player);

			if (getAvailableCages().size() == 0) {
				setStage(Stage.COUNTDOWN);
			}

			// Send the join message to the players
			Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&',
					String.format("&6%s &ehas joined &7(&6%s&7/&6%s&7)!", bp.getDisplayName(),
							players.size(), cages.size())));
		}, 5L);
	}
	
	public void removePlayer(GamePlayer player) {

		players.remove(player);
		
		if (stage == Stage.WAITING) {
			player.getCage().setPlayer(null);
			
			Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&',
					String.format("&7%s &6has quit!", player.getBukkitPlayer().getDisplayName())));
		} else {
			checkPlayers();
		}
	}
	
	public void checkPlayers() {
		
		if (players.size() == 1 && (stage != Stage.WAITING && stage != Stage.END && stage != Stage.RESTART)) {
			setStage(Stage.END);
		} else if (players.size() == 0 && stage != Stage.WAITING) {
			setStage(Stage.RESTART);
		}
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
		return cages.stream().filter(cage -> !cage.hasPlayer()).collect(Collectors.toSet());
	}
	
	public Set<GamePlayer> getSpectators() {
		Set<GamePlayer> spectators = new HashSet<>(Warfare.getInstance().getPlayerManager().getPlayers());
		spectators.removeAll(players);
		return spectators;
	}
	
	public void start() {
		
		for (Entity entity : Warfare.getInstance().getArena().getMapLocation().getWorld().getEntities()) {
			if (entity.getType() != EntityType.PLAYER) {
				entity.remove();
			}
		}
		
		// Destroy cages
		for (Cage cage : cages) {
			cage.destroy();
		}
		
		for (GamePlayer player : players) {
			Player bp = player.getBukkitPlayer();
			
			// Update player's name color
			Utils.colorPlayerName(player, Warfare.getInstance().getGame().getPlayers());

			bp.setWalkSpeed(bp.getWalkSpeed() * 1.25f);
			
			// Clear player's inventory to remove class selector
			bp.getInventory().clear();
			
			// Give player kit items
			player.getSelectedKitOrPot().giveItems(player);
			
			// Close player's inventory to keep them from using the class selector in-game
			bp.closeInventory();
			
			// Set player's health
			List<Purchasable> purchases = player.getPurchases();
			double health = 24;
			
			if (purchases.contains(HealthBoost.HEALTH_BOOST_I)) {
				health = 26;
			} else if (purchases.contains(HealthBoost.HEALTH_BOOST_II)) {
				health = 28;
			} else if (purchases.contains(HealthBoost.HEALTH_BOOST_III)) {
				health = 30;
			}
			
			bp.setMaxHealth(health);
			bp.setHealth(health);

			Bukkit.getScheduler().runTaskLater(Warfare.getInstance(), () -> {
				bp.setWalkSpeed(bp.getWalkSpeed() / 1.25f);
			}, 20 * 10);
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
			Bukkit.getServer().broadcastMessage(ChatColor.GOLD +
					String.format("%s " + ChatColor.YELLOW + "has won the game!", player.getBukkitPlayer().getDisplayName()));
			
			int winCoins = 200;
			
			if (player.getBukkitPlayer().hasPermission("Warfare.coins.double")) {
				winCoins = 400;
			}
			
			if (player.getBukkitPlayer().hasPermission("Warfare.coins.triple")) {
				winCoins = 600;
			}
			
			player.setCoins(player.getCoins() + winCoins);
			player.setWins(player.getWins() + 1);
			
			player.getBukkitPlayer().sendMessage(ChatColor.YELLOW + String.format(
					"You earned " + ChatColor.GOLD + "%s" + ChatColor.YELLOW + " coins for winning the game.",
					String.valueOf(winCoins)));
		}

		Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "Thanks for playing!");

		if(Warfare.getInstance().isEnabled()) {
			BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
			scheduler.scheduleSyncDelayedTask(Warfare.getInstance(), () -> setStage(Stage.RESTART), 120L);
		}
		else{
			setStage(Stage.RESTART);
		}
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

		} else if (stage == Stage.RESTART) {

            if(!Warfare.getInstance().isEnabled()){
                return;
            }

			for (GamePlayer player : Warfare.getInstance().getPlayerManager().getPlayers()) {
                Player bukkitPlayer = player.getBukkitPlayer();
                if(bukkitPlayer != null) {
                    if(!Warfare.getInstance().getArena().isEdit() || !bukkitPlayer.hasPermission("warfare.edit")) {
                        Party party = Warfare.getInstance().getPartyManager().getParty(player.getUUID());
                        if (party == null) {
                            Utils.sendPlayerToServer(player.getBukkitPlayer(), StaticConfiguration.getNextLobby());
                        } else {
                            UUID leader = party.getLeader();
                            if (leader == player.getUUID()) {
                                String lobby = StaticConfiguration.getNextLobby();
                                for (UUID member : party.getMembers()) {
                                    Player other = Bukkit.getPlayer(member);
                                    Utils.sendPlayerToServer(other, lobby);
                                }
                            } else if (Bukkit.getPlayer(leader) == null) {
                                Utils.sendPlayerToServer(player.getBukkitPlayer(), StaticConfiguration.getNextLobby());
                            }
                        }
                    }
                }
			}
			
			if (Warfare.getInstance().getArena().isEdit()) {
				return;
			}
			
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
		} else if (countdownTime == 0) {
			setStage(Stage.BATTLE);
		}
	}

	public short getCountdownTime() {
		
		return countdownTime;
	}
	
	public void fillChests() {
		
		for (LootChest lootChest : Warfare.getInstance().getArena().getLootChests()) {
			if (lootChest.getLocation().getBlock().getType() != Material.CHEST) {
				continue;
			}
			
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
			
			Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + "All chests have been refilled");
		}
	}
	
	public short getRefillCountdownTime() {
		
		return refillCountdownTime;
	}
}