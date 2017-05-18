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

import java.util.*;

import com.andrewyunt.warfare.configuration.StaticConfiguration;
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
	
	private final List<Purchasable> purchases = new ArrayList<Purchasable>();
	private final Set<UUID> ghasts = new HashSet<UUID>();
	
	public GamePlayer(UUID uuid) {
		
		this.uuid = uuid;

		// Register health objective for game servers
		if (!StaticConfiguration.LOBBY) {
			Objective healthObjective = Warfare.getInstance().getScoreboardHandler().getPlayerBoard(uuid).getScoreboard()
					.registerNewObjective(ChatColor.RED + "❤", "health");
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
		if(!Objects.equals(earnedCoins, this.earnedCoins)) {
            this.earnedCoins = earnedCoins;
            update();
        }
	}
	
	public int getEarnedCoins() {
		
		return earnedCoins;
	}
	
	public void setWins(int wins) {
	    if(!Objects.equals(wins, this.wins)) {
            this.wins = wins;
            update();
        }
	}
	
	public int getWins() {
		return wins;
	}
	
	public void setEnergy(int energy) {
		this.energy = energy;
		
		if (this.energy > 100) {
            this.energy = 100;
        } else {
            sentActivate = false;
        }
		
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
	    if(!Objects.equals(kills, this.kills)) {
            this.kills = kills;
            update();
        }
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
	    if(!Objects.equals(selectedKit, this.selectedKit)) {
            this.selectedKit = selectedKit;
            update();
        }
	}
	
	public Kit getSelectedKit() {
		return selectedKit;
	}

	public Kit getSelectedKitOrPot(){
        return selectedKit == null ? Kit.POT : selectedKit;
    }
	
	public Location setSpectating(boolean spectating, boolean respawn) {
		this.spectating = spectating;
		
		if (spectating) {
			BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
			scheduler.runTask(Warfare.getInstance(), () -> {
                Player player = getBukkitPlayer();
                if(player != null) {
                    player.spigot().setCollidesWithEntities(false);

                    player.setAllowFlight(true);
                    player.setFireTicks(0);

                    for (GamePlayer toShow : Warfare.getInstance().getGame().getSpectators()) {
                        player.showPlayer(toShow.getBukkitPlayer());
                    }

                    for (GamePlayer toHide : Warfare.getInstance().getGame().getPlayers()) {
                        toHide.getBukkitPlayer().hidePlayer(player);
                    }

                    updateHotbar();
                }
            });
			
			Location loc = Warfare.getInstance().getArena().getMapLocation();
			Chunk chunk = loc.getChunk();
			
			if (!chunk.isLoaded()) {
                chunk.load();
            }
			
			loc.setY(loc.getY() + 1);
			
			if (respawn) {
                return loc;
            } else {
                getBukkitPlayer().teleport(loc, TeleportCause.COMMAND);
            }
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
	
	public Set<UUID> getGhasts() {
		
		return ghasts;
	}
	
	public boolean isCaged() {
		
		return getCage() != null;
	}
	
	public Cage getCage() {
		
		if (StaticConfiguration.LOBBY) {
            return null;
        }
		
		for (Cage cage : Warfare.getInstance().getGame().getCages()) {
            if (cage.hasPlayer() && cage.getPlayer() == this) {
                return cage;
            }
        }
		
		return null;
	}

	public void updateHotbar() {
		PlayerInventory inv = getBukkitPlayer().getInventory();
		inv.clear();
        if (spectating) {
			BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
			scheduler.runTaskLater(Warfare.getInstance(), () -> {
                ItemStack teleporter = new ItemStack(Material.COMPASS, 1);
                ItemMeta teleporterMeta = teleporter.getItemMeta();
                teleporterMeta.setDisplayName(Utils.formatMessage(StaticConfiguration.SPECTATOR_TELEPORTER_TITLE));
                teleporter.setItemMeta(teleporterMeta);
                inv.setItem(StaticConfiguration.SPECTATOR_TELEPORTER_SLOT - 1, teleporter);

                ItemStack bed = new ItemStack(Material.BED, 1);
                ItemMeta bedMeta = bed.getItemMeta();
                bedMeta.setDisplayName(Utils.formatMessage(StaticConfiguration.SPECTATOR_RETURN_TO_LOBBY_TITLE));
                bed.setItemMeta(bedMeta);
                inv.setItem(StaticConfiguration.SPECTATOR_RETURN_TO_LOBBY_SLOT - 1, bed);
            }, 20);
		} else {
			ItemStack shop = new ItemStack(Material.CHEST, 1);
			ItemMeta shopMeta = shop.getItemMeta();
			shopMeta.setDisplayName(Utils.formatMessage(StaticConfiguration.LOBBY_SHOP_TITLE));
			shop.setItemMeta(shopMeta);
			inv.setItem(StaticConfiguration.LOBBY_SHOP_SLOT - 1, shop);

			ItemStack play = new ItemStack(Material.COMPASS, 1);
			ItemMeta playMeta = play.getItemMeta();
			playMeta.setDisplayName(Utils.formatMessage(StaticConfiguration.LOBBY_PLAY_TITLE));
			play.setItemMeta(playMeta);
			inv.setItem(StaticConfiguration.LOBBY_PLAY_SLOT - 1, play);

			ItemStack classSelector = new ItemStack(Material.ENDER_CHEST, 1);
			ItemMeta classSelectorMeta = classSelector.getItemMeta();
			classSelectorMeta.setDisplayName(Utils.formatMessage(StaticConfiguration.LOBBY_CLASS_SELECTOR_TITLE));
			classSelector.setItemMeta(classSelectorMeta);
			inv.setItem(StaticConfiguration.LOBBY_CLASS_SELECTOR_SLOT - 1, classSelector);
		}
		getBukkitPlayer().updateInventory();
	}

	public void update(){
	    Warfare.getInstance().getMySQLManager().savePlayerAsync(this);
    }
}