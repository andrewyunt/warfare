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

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitScheduler;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.utilities.Utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

/**
 * The class used to store arena information.
 * 
 * @author Andrew Yunt
 */
public class Arena {
	
	private final Map<String, Location> cageLocations = new HashMap<String, Location>();
	private final Set<LootChest> lootChests = new HashSet<LootChest>();
	
	private boolean isEdit;
	private Location mapLocation;
	
	public void setEdit(boolean isEdit) {
		
		this.isEdit = isEdit;
	}
	
	public boolean isEdit() {
		
		return isEdit;
	}
	
	public void addCageLocation(String name, Location loc) {
		
		cageLocations.put(name, loc);
		
		save();
	}
	
	public Map<String, Location> getCageLocations() {
		
		return cageLocations;
	}
	
	public Location getMapLocation() {
		
		return mapLocation;
	}
	
	public Set<LootChest> getLootChests() {
		
		return lootChests;
	}
	
	public void save() {
		
		Warfare plugin = Warfare.getInstance();
		FileConfiguration arenaConfig = plugin.getArenaConfig().getConfig();
		
		arenaConfig.createSection("map_location", Utils.serializeLocation(mapLocation));
		
		ConfigurationSection chestsSection = arenaConfig.createSection("chests");
		
		for (LootChest lootChest : lootChests) {
			Map<String, Object> chestSection = Utils.serializeLocation(lootChest.getLocation());
			
			chestSection.put("tier", lootChest.getTier());
			
			chestsSection.createSection(UUID.randomUUID().toString(), chestSection);
		}
		
		ConfigurationSection cagesSection = arenaConfig.createSection("cages");
		
		for (Entry<String, Location> entry : cageLocations.entrySet())
			cagesSection.createSection(entry.getKey(), Utils.serializeLocation(entry.getValue()));
		
		plugin.getArenaConfig().saveConfig();
	}
	
	public static Arena loadFromConfig() {
		
		Arena arena = new Arena();
		
		FileConfiguration arenaConfig = Warfare.getInstance().getArenaConfig().getConfig();
		arena.mapLocation = Utils.deserializeLocation(arenaConfig.getConfigurationSection("map_location"));
		
		ConfigurationSection chestsSection = arenaConfig.getConfigurationSection("chests");
		
		for (String key : chestsSection.getKeys(false)) {
			ConfigurationSection chestSection = chestsSection.getConfigurationSection(key);
			Block block = Utils.deserializeLocation(chestSection).getBlock();
			
			if (block == null)
				continue;
			
			if (block.getType() != Material.CHEST)
				continue;
			
			arena.lootChests.add(new LootChest(block.getLocation(), (byte) chestSection.getInt("tier")));
		}
		
		ConfigurationSection cagesSection = arenaConfig.getConfigurationSection("cages");
		
		BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(Warfare.getInstance(), new Runnable() {
			@Override
			public void run() {
				
				for (String key : cagesSection.getKeys(false))
					Warfare.getInstance().getGame().getCages().add(new Cage(key,
							Utils.deserializeLocation(cagesSection.getConfigurationSection(key))));
			}
		}, 1L);
		
		return arena;
	}
}