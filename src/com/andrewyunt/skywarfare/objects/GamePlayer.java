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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import com.andrewyunt.skywarfare.SkyWarfare;

public class GamePlayer {
	
	private UUID uuid;
	private CustomClass customClass;
	private Map<Upgradable, Integer> upgradeLevels = new HashMap<Upgradable, Integer>();
	private int coins, wins, energy;
	private boolean cooldown, hasSpeed, loaded, spectating;
	private DynamicScoreboard dynamicScoreboard;
	
	public GamePlayer(UUID uuid) {
		
		this.uuid = uuid;
		
		// Load upgradable levels
		/*for (Ultimate ultimate : Ultimate.values()) {
			int level = SkyWarfare.getInstance().getDataSource().getLevel(this, ultimate);
			upgradeLevels.put(ultimate, level);
		}
		
		for (Skill skill : Skill.values()) {
			int level = SkyWarfare.getInstance().getDataSource().getLevel(this, skill);
			upgradeLevels.put(skill, level);
		}*/
		
		// Set up scoreboard
		dynamicScoreboard = new DynamicScoreboard(ChatColor.YELLOW + "" + ChatColor.BOLD + "MEGATW");
		getBukkitPlayer().setScoreboard(dynamicScoreboard.getScoreboard());
		
		// Register health objective
		Objective healthObjective = dynamicScoreboard.getScoreboard().registerNewObjective(ChatColor.RED + "❤", "health");
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
	}
	
	public CustomClass getCustomClass() {
		
		return new CustomClass();
		//return customClass;
	}
	
	public int getLevel(Upgradable upgradable) {
		
		if (upgradeLevels.containsKey(upgradable))
			return upgradeLevels.get(upgradable);
		
		return 1;
	}
	
	public Map<Upgradable, Integer> getUpgradeLevels() {
		
		return upgradeLevels;
	}
	
	public void setCoins(int coins) {
		
		this.coins = coins;
	}
	
	public int getCoins() {
		
		return coins;
	}
	
	public void setWins(int wins) {
		
		this.wins = wins;
	}
	
	public int getWins() {
		
		return wins;
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
	
	public void addEnergy(int energy) {
		
		this.energy = this.energy + energy;
	}
	
	public int getEnergy() {
		
		return energy;
	}
	
	public void setLoaded(boolean loaded) {
		
		this.loaded = loaded;
	}
	
	public boolean isLoaded() {
		
		return loaded;
	}
	
	public boolean isInGame() {
		
		return SkyWarfare.getInstance().getGame().getPlayers().contains(this);
	}
	
	public GamePlayer getLastDamager() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void setSpectating(boolean spectating) {
		
		this.spectating = spectating;
		
		if (spectating) {
			Player player = getBukkitPlayer();
			
			player.setAllowFlight(true);
			player.setFireTicks(0);
			player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 15), true);
			
			updateDynamicScoreboard();
			
			Location loc = SkyWarfare.getInstance().getArena().getMapLocation();
			Chunk chunk = loc.getChunk();
			
			if (!chunk.isLoaded())
				chunk.load();
			
			loc.setY(loc.getY() + 1);
			
			player.teleport(loc, TeleportCause.COMMAND);
		}
	}
	
	public boolean isSpectating() {
		
		return spectating;
	}
	
	public DynamicScoreboard getDynamicScoreboard() {
		
		return dynamicScoreboard;
	}
	
	public void updateDynamicScoreboard() {
		// TODO Auto-generated method stub
		
	}
}