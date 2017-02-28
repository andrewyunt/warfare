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

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.objects.Game.Stage;
import com.andrewyunt.warfare.utilities.Utils;

public class GamePlayer {
	
	private UUID uuid;
	private CustomClass customClass;
	private int coins, earnedCoins, earnedCoinsGame, wins, energy, kills, killStreak;
	private boolean cooldown, hasSpeed, loaded, spectating, flamingFeet, sentActivate, hasFallen;
	private DynamicScoreboard dynamicScoreboard;
	private GamePlayer lastDamager;
	
	private final List<CustomClass> customClasses = new ArrayList<CustomClass>();
	private final List<Purchasable> purchases = new ArrayList<Purchasable>();
	private final Set<UUID> ghasts = new HashSet<UUID>();
	
	public GamePlayer(UUID uuid) {
		
		this.uuid = uuid;
		
		// Set up scoreboard
		dynamicScoreboard = new DynamicScoreboard(ChatColor.GOLD + ChatColor.BOLD.toString() + "Warfare");
		getBukkitPlayer().setScoreboard(dynamicScoreboard.getScoreboard());
		
		if (Warfare.getInstance().getConfig().getBoolean("is-lobby"))
			return;
		
		// Register health objective
		Objective healthObjective = dynamicScoreboard.getScoreboard().registerNewObjective(ChatColor.RED + "â�¤", "health");
		healthObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
	}
	
	public UUID getUUID() {
		
		return uuid;
	}
	
	public Player getBukkitPlayer() {
		
		return Bukkit.getServer().getPlayer(uuid);
	}
	
	public void setCustomClass(CustomClass customClass) {
		
		this.customClass = customClass;
		
		updateDynamicScoreboard();
	}
	
	public CustomClass getCustomClass() {
		
		return customClass;
	}
	
	public void setCoins(int coins) {
		
		this.coins = coins;
		
		updateDynamicScoreboard();
	}
	
	public int getCoins() {
		
		return coins;
	}
	
	public void setEarnedCoins(int earnedCoins) {
		
		this.earnedCoins = earnedCoins;
	}
	
	public int getEarnedCoins() {
		
		return earnedCoins;
	}
	
	public void setEarnedCoinsGame(int earnedCoinsGame) {
		
		this.earnedCoinsGame = earnedCoinsGame;
	}
	
	public int getEarnedCoinsGame() {
		
		return earnedCoinsGame;
	}
	
	public void setWins(int wins) {
		
		this.wins = wins;
		
		updateDynamicScoreboard();
	}
	
	public int getWins() {
		
		return wins;
	}
	
	public void setEnergy(int energy) {
		
		this.energy = energy;
		
		if (this.energy > 100)
			this.energy = 100;
		else
			sentActivate = false;
		
		Player player = getBukkitPlayer();
		
		if (this.energy == 100) {
			if (!sentActivate) {
				sentActivate = true;
				
				if (customClass.getUltimate() == Ultimate.LEAP) {
					getBukkitPlayer().setAllowFlight(true);
					
					player.sendMessage(ChatColor.GOLD + "Double jump to activate your ultimate!");
				} else
					player.sendMessage(ChatColor.GOLD + "Right click using your sword to activate your ultimate!");
			}
		}
		
		getBukkitPlayer().setLevel(this.energy);
		getBukkitPlayer().setExp(this.energy / 100.0F);
	}
	
	public int getEnergy() {
		
		return energy;
	}
	
	public void addKill() {
		
		this.killStreak = killStreak + 1;
		
		setKills(kills + 1);
	}
	
	public void setKills(int kills) {
		
		this.kills = kills;
		
		updateDynamicScoreboard();
	}
	
	public int getKills() {
		
		return kills;
	}
	
	public void setCooldown(boolean cooldown) {
		
		this.cooldown = cooldown;
	}
	
	public boolean isCooldown() {
		
		return cooldown;
	}
	
	public void setHasSpeed(boolean hasSpeed) {
		
		this.hasSpeed = hasSpeed;
	}
	
	public boolean hasSpeed() {
		
		return hasSpeed;
	}
	
	public void setLoaded(boolean loaded) {
		
		this.loaded = loaded;
	}
	
	public boolean isLoaded() {
		
		return loaded;
	}
	
	public void setHasFallen(boolean hasFallen) {
		
		this.hasFallen = hasFallen;
	}
	
	public boolean hasFallen() {
		
		return hasFallen;
	}
	
	public boolean isInGame() {
		
		return Warfare.getInstance().getGame().getPlayers().contains(this);
	}
	
	public void setLastDamager(GamePlayer lastDamager) {
		
		this.lastDamager = lastDamager;
	}
	
	public GamePlayer getLastDamager() {
		
		return lastDamager;
	}
	
	public Location setSpectating(boolean spectating, boolean respawn) {
		
		this.spectating = spectating;
		
		if (spectating) {
			BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
			scheduler.scheduleSyncDelayedTask(Warfare.getInstance(), new Runnable() {
				@Override
				public void run() {
					Player player = getBukkitPlayer();
					
					player.setAllowFlight(true);
					player.setFireTicks(0);
					
					for (GamePlayer toShow : Warfare.getInstance().getGame().getSpectators())
						player.showPlayer(toShow.getBukkitPlayer());
					
					for (GamePlayer toHide : Warfare.getInstance().getGame().getPlayers())
						toHide.getBukkitPlayer().hidePlayer(player);
					
					updateDynamicScoreboard();
					
					updateHotbar();
				}
			}, 5L);
			
			Location loc = Warfare.getInstance().getArena().getMapLocation();
			Chunk chunk = loc.getChunk();
			
			if (!chunk.isLoaded())
				chunk.load();
			
			loc.setY(loc.getY() + 1);
			
			if (respawn)
				return loc;
			else
				getBukkitPlayer().teleport(loc, TeleportCause.COMMAND);
		}
		
		return null;
	}
	
	public boolean isSpectating() {
		
		return spectating;
	}
	
	public void setFlamingFeet(boolean flamingFeet) {
		
		this.flamingFeet = flamingFeet;
	}
	
	public boolean hasFlamingFeet() {
		
		return flamingFeet;
	}
	
	public List<Purchasable> getPurchases() {
		
		return purchases;
	}
	
	public boolean hasPurchased(Purchasable purchasable) {
		
		for (Purchasable purchase : purchases)
			if (purchase.toString().equals(purchasable.toString()))
				return true;
		
		return false;
	}
	
	public List<CustomClass> getCustomClasses() {
		
		return customClasses;
	}
	
	public CustomClass getCustomClass(String name) {
		
		for (CustomClass customClass : customClasses)
			if (customClass.getName().equalsIgnoreCase(name))
				return customClass;
		
		return null;
	}
	
	public Set<UUID> getGhasts() {
		
		return ghasts;
	}
	
	public boolean isCaged() {
		
		return getCage() != null;
	}
	
	public Cage getCage() {
		
		if (Warfare.getInstance().getConfig().getBoolean("is-lobby"))
			return null;
		
		for (Cage cage : Warfare.getInstance().getGame().getCages())
			if (cage.hasPlayer() && cage.getPlayer() == this)
				return cage;
		
		return null;
	}
	
	public DynamicScoreboard getDynamicScoreboard() {
		
		return dynamicScoreboard;
	}
	
	public void updateDynamicScoreboard() {
		
		if (Warfare.getInstance().getConfig().getBoolean("is-lobby")){
			dynamicScoreboard.update(12, ChatColor.GRAY + ChatColor.STRIKETHROUGH.toString()
					+ "- - --- ------ --- --   - --");
			
			dynamicScoreboard.update(8, ChatColor.GOLD + ChatColor.BOLD.toString() + "Statistics"
					+ ChatColor.GRAY + ChatColor.BOLD.toString() + ":");
			
			// Display player's wins
			dynamicScoreboard.update(7, ChatColor.GOLD + "  » " + ChatColor.YELLOW + "Total Wins: "
					+ ChatColor.GREEN + String.valueOf(wins));
			
			// Display player's kills
			dynamicScoreboard.update(6, ChatColor.GOLD + "  » " + ChatColor.YELLOW + "Total Kills: "
					+ ChatColor.GREEN + String.valueOf(kills));
			
			// Display player's coins
			dynamicScoreboard.update(5, ChatColor.GOLD + "  » " + ChatColor.YELLOW + "Coin Balance: "
					+ ChatColor.GREEN + String.valueOf(coins));
			
			dynamicScoreboard.blankLine(4);
			
			// Display player's chosen class 
			dynamicScoreboard.update(3, ChatColor.GOLD + ChatColor.BOLD.toString() + "Selected Class"
					+ ChatColor.GRAY + ChatColor.BOLD.toString() + ":");
			dynamicScoreboard.update(2, ChatColor.GOLD + "  » " + ChatColor.YELLOW + ChatColor.BOLD.toString()
					+ (customClass == null ? "None" : customClass.getName()));
			
			dynamicScoreboard.update(1, ChatColor.GRAY + ChatColor.STRIKETHROUGH.toString()
					+ "-------------------------");
		} else {
			Game game = Warfare.getInstance().getGame();
			Stage stage = game.getStage();
			
			if (stage == Stage.WAITING || stage == Stage.COUNTDOWN) {
				
				dynamicScoreboard.blankLine(8);
				
				// Display players
				dynamicScoreboard.update(7, "Players: " + ChatColor.GREEN + game.getPlayers().size() + "/"
						+ game.getCages().size());
				
				dynamicScoreboard.blankLine(6);
				
				// Display seconds left
				if (stage == Stage.WAITING)
					dynamicScoreboard.update(5, "Waiting...");
				else
					dynamicScoreboard.update(5, "Starting in " + ChatColor.GREEN + game.getCountdownTime() + "s");
				
				dynamicScoreboard.blankLine(4);
				
				// Display server name
				dynamicScoreboard.update(3, "Server: " + ChatColor.GREEN + Warfare.getInstance().getServerName());
			} else {
				dynamicScoreboard.blankLine(9);
				
				dynamicScoreboard.update(8, "Next event:");
				
				dynamicScoreboard.update(7, ChatColor.GREEN + "Refill " + LocalTime.ofSecondOfDay(game
						.getRefillCountdownTime()).toString().substring(3));
				
				dynamicScoreboard.blankLine(6);
				
				dynamicScoreboard.update(5, "Players Left: " + ChatColor.GREEN + game.getPlayers().size());
				
				dynamicScoreboard.blankLine(4);
				
				dynamicScoreboard.update(3, "Killstreak: " + ChatColor.GREEN + killStreak);
			}
		}
	}
	
	public void updateHotbar() {
		
		PlayerInventory inv = getBukkitPlayer().getInventory();
		
		inv.clear();
		
		if (spectating) {
			// Delay so players don't accidentally click items after being set to spectator mode
			BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
			scheduler.scheduleSyncDelayedTask(Warfare.getInstance(), new Runnable() {
				@Override
				public void run() {
					
					ItemStack teleporter = new ItemStack(Material.COMPASS, 1);
					ItemMeta teleporterMeta = teleporter.getItemMeta();
					teleporterMeta.setDisplayName(Utils.getFormattedMessage("hotbar-items.spectator-items.teleporter.title"));
					teleporter.setItemMeta(teleporterMeta);
					inv.setItem(Warfare.getInstance().getConfig().getInt("hotbar-items.spectator-items.teleporter.slot") - 1,
							teleporter);
					
					ItemStack bed = new ItemStack(Material.BED, 1);
					ItemMeta bedMeta = bed.getItemMeta();
					bedMeta.setDisplayName(Utils.getFormattedMessage("hotbar-items.spectator-items.return-to-lobby.title"));
					bed.setItemMeta(bedMeta);
					inv.setItem(Warfare.getInstance().getConfig().getInt("hotbar-items.spectator-items.return-to-lobby.slot") - 1,
							bed);
				}
			}, 20L);
		} else {
			if (!isCaged()) {
				ItemStack shop = new ItemStack(Material.EMERALD, 1);
				ItemMeta shopMeta = shop.getItemMeta();
				shopMeta.setDisplayName(Utils.getFormattedMessage("hotbar-items.lobby-items.shop.title"));
				shop.setItemMeta(shopMeta);
				inv.setItem(Warfare.getInstance().getConfig().getInt("hotbar-items.lobby-items.shop.slot") - 1,
						shop);
				
				ItemStack classCreator = new ItemStack(Material.CHEST, 1);
				ItemMeta classCreatorMeta = classCreator.getItemMeta();
				classCreatorMeta.setDisplayName(Utils.getFormattedMessage("hotbar-items.lobby-items.class-creator.title"));
				classCreator.setItemMeta(classCreatorMeta);
				inv.setItem(Warfare.getInstance().getConfig().getInt("hotbar-items.lobby-items.class-creator.slot") - 1,
						classCreator);
				
				ItemStack classSelector = new ItemStack(Material.COMMAND, 1);
				ItemMeta classSelectorMeta = classSelector.getItemMeta();
				classSelectorMeta.setDisplayName(Utils.getFormattedMessage("hotbar-items.lobby-items.class-selector.title"));
				classSelector.setItemMeta(classSelectorMeta);
				inv.setItem(Warfare.getInstance().getConfig().getInt("hotbar-items.lobby-items.class-selector.slot") - 1,
						classSelector);
			} else {
				ItemStack classSelector = new ItemStack(Material.COMMAND, 1);
				ItemMeta classSelectorMeta = classSelector.getItemMeta();
				classSelectorMeta.setDisplayName(Utils.getFormattedMessage("hotbar-items.cage-items.class-selector.title"));
				classSelector.setItemMeta(classSelectorMeta);
				inv.setItem(Warfare.getInstance().getConfig().getInt("hotbar-items.cage-items.class-selector.slot") - 1,
						classSelector);
			}
		}
	}
}