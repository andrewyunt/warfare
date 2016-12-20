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

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitScheduler;

import com.andrewyunt.skywarfare.SkyWarfare;
import com.andrewyunt.skywarfare.utilities.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The class used to store arena information.
 * 
 * @author Andrew Yunt
 */
public class Arena {
	
	private final Map<String, Location> cageLocations = new HashMap<String, Location>();
	
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
	
	public void save() {
		
		SkyWarfare plugin = SkyWarfare.getInstance();
		FileConfiguration arenaConfig = plugin.getArenaConfig().getConfig();
		
		arenaConfig.createSection("map_location", Utils.serializeLocation(mapLocation));
		
		ConfigurationSection cagesSection = arenaConfig.createSection("cages");
		
		for (Entry<String, Location> entry : cageLocations.entrySet())
			cagesSection.createSection(entry.getKey(), Utils.serializeLocation(entry.getValue()));
		
		plugin.getArenaConfig().saveConfig();
	}
	
	public static Arena loadFromConfig() {
		
		Arena arena = new Arena();
		
		FileConfiguration arenaConfig = SkyWarfare.getInstance().getArenaConfig().getConfig();
		arena.mapLocation = Utils.deserializeLocation(arenaConfig.getConfigurationSection("map_location"));
		ConfigurationSection cagesSection = arenaConfig.getConfigurationSection("cages");
		
		BukkitScheduler scheduler = SkyWarfare.getInstance().getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(SkyWarfare.getInstance(), new Runnable() {
			@Override
			public void run() {
				
				for (String key : cagesSection.getKeys(false))
					SkyWarfare.getInstance().getGame().getCages().add(new Cage(key,
							Utils.deserializeLocation(cagesSection.getConfigurationSection(key))));
			}
		}, 1L);
		
		return arena;
	}
}