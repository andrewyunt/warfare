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
import com.andrewyunt.warfare.utilities.Utils;

public class GamePlayer {
	
	private UUID uuid;
	private int coins, earnedCoins, wins, energy, kills, killStreak;
	private boolean epcCooldown, powerupCooldown, powerupActivated, loaded, spectating, flamingFeet, sentActivate, hasFallen;
	private GamePlayer lastDamager;
	private Kit selectedKit;
	private Skill selectedSkill;
	private Ultimate selectedUltimate;
	
	private final List<Purchasable> purchases = new ArrayList<Purchasable>();
	private final Set<UUID> ghasts = new HashSet<UUID>();
	
	public GamePlayer(UUID uuid) {
		
		this.uuid = uuid;

		// Register health objective for game servers
		if (!Warfare.getInstance().getConfig().getBoolean("is-lobby")) {
			Objective healthObjective = Warfare.getInstance().getScoreboardHandler().getPlayerBoard(uuid).getScoreboard()
					.registerNewObjective(ChatColor.RED + "â�¤", "health");
			healthObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
		}
	}
	
	public UUID getUUID() {
		
		return uuid;
	}
	
	public Player getBukkitPlayer() {
		
		return Bukkit.getServer().getPlayer(uuid);
	}
	
	public void setCoins(int coins) {
		
		this.coins = coins;
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
	
	public void setWins(int wins) {
		
		this.wins = wins;
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
		
		Warfare.getInstance().getScoreboardHandler().getPlayerBoard(uuid);
	}
	
	public int getKills() {
		
		return kills;
	}

	public int getKillStreak() {

		return killStreak;
	}
	
	public void setEPCCooldown(boolean cooldown) {
		
		this.epcCooldown = cooldown;
	}
	
	public boolean isEPCCooldown() {
		
		return epcCooldown;
	}

	public void setPowerupCooldown(boolean powerupCooldown) {

		this.powerupCooldown = powerupCooldown;
	}

	public boolean isPowerupCooldown() {

		return powerupCooldown;
	}

	public void setPowerupActivated(boolean powerupActivated) {

		this.powerupActivated = powerupActivated;
	}

	public boolean isPowerupActivated() {

		return powerupActivated;
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
	
	public void setSelectedKit(Kit selectedKit) {
		
		this.selectedKit = selectedKit;
	}
	
	public Kit getSelectedKit() {
		
		return selectedKit;
	}
	
	public void setSelectedSkill(Skill selectedSkill) {
		
		this.selectedSkill = selectedSkill;
	}
	
	public Skill getSelectedSkill() {
		
		return selectedSkill;
	}
	
	public void setSelectedUltimate(Ultimate selectedUltimate) {
		
		this.selectedUltimate = selectedUltimate;
	}
	
	public Ultimate getSelectedUltimate() {
		
		return selectedUltimate;
	}
	
	public Location setSpectating(boolean spectating, boolean respawn) {
		
		this.spectating = spectating;
		
		if (spectating) {
			BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
			scheduler.scheduleSyncDelayedTask(Warfare.getInstance(), () -> {
                Player player = getBukkitPlayer();

                player.setAllowFlight(true);
                player.setFireTicks(0);

                for (GamePlayer toShow : Warfare.getInstance().getGame().getSpectators())
                    player.showPlayer(toShow.getBukkitPlayer());

                for (GamePlayer toHide : Warfare.getInstance().getGame().getPlayers())
                    toHide.getBukkitPlayer().hidePlayer(player);

                updateHotbar();
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

	public void updateHotbar() {
		
		PlayerInventory inv = getBukkitPlayer().getInventory();
		
		inv.clear();
		
		if (spectating) {
			// Delay so players don't accidentally click items after being set to spectator mode
			BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
			scheduler.scheduleSyncDelayedTask(Warfare.getInstance(), () -> {

                ItemStack teleporter = new ItemStack(Material.COMPASS, 1);
                ItemMeta teleporterMeta = teleporter.getItemMeta();
                teleporterMeta.setDisplayName(Utils.getFormattedMessage("hotbar-items.spectator-items.teleporter.title"));
                teleporter.setItemMeta(teleporterMeta);
                inv.setItem(Warfare.getInstance().getConfig().getInt("hotbar-items.spectator-items.teleporter.slot") - 1, teleporter);

                ItemStack bed = new ItemStack(Material.BED, 1);
                ItemMeta bedMeta = bed.getItemMeta();
                bedMeta.setDisplayName(Utils.getFormattedMessage("hotbar-items.spectator-items.return-to-lobby.title"));
                bed.setItemMeta(bedMeta);
                inv.setItem(Warfare.getInstance().getConfig().getInt("hotbar-items.spectator-items.return-to-lobby.slot") - 1, bed);
            }, 20L);
		} else {
			if (!isCaged()) {
				ItemStack shop = new ItemStack(Material.CHEST, 1);
				ItemMeta shopMeta = shop.getItemMeta();
				shopMeta.setDisplayName(Utils.getFormattedMessage("hotbar-items.lobby-items.shop.title"));
				shop.setItemMeta(shopMeta);
				inv.setItem(Warfare.getInstance().getConfig().getInt("hotbar-items.lobby-items.shop.slot") - 1, shop);
				
				ItemStack classSelector = new ItemStack(Material.COMMAND, 1);
				ItemMeta classSelectorMeta = classSelector.getItemMeta();
				classSelectorMeta.setDisplayName(Utils.getFormattedMessage("hotbar-items.lobby-items.class-selector.title"));
				classSelector.setItemMeta(classSelectorMeta);
				inv.setItem(Warfare.getInstance().getConfig().getInt("hotbar-items.lobby-items.class-selector.slot") - 1, classSelector);
				
				ItemStack play = new ItemStack(Material.DIAMOND_SWORD, 1);
				ItemMeta playMeta = play.getItemMeta();
				playMeta.setDisplayName(Utils.getFormattedMessage("hotbar-items.lobby-items.play.title"));
				play.setItemMeta(playMeta);
				inv.setItem(Warfare.getInstance().getConfig().getInt("hotbar-items.lobby-items.play.slot") - 1, play);
			} else {
				ItemStack classSelector = new ItemStack(Material.COMMAND, 1);
				ItemMeta classSelectorMeta = classSelector.getItemMeta();
				classSelectorMeta.setDisplayName(Utils.getFormattedMessage("hotbar-items.cage-items.class-selector.title"));
				classSelector.setItemMeta(classSelectorMeta);
				inv.setItem(Warfare.getInstance().getConfig().getInt("hotbar-items.cage-items.class-selector.slot") - 1, classSelector);
			}
		}
	}
}