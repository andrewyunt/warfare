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

import org.bukkit.Bukkit;
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

	private Set<LootChest> lootChests = new HashSet<LootChest>();
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
	}
	
	public Map<String, Location> getCageLocations() {
		
		return cageLocations;
	}

	public void setMapLocation(Location mapLocation) {

		this.mapLocation = mapLocation;
	}

	public Location getMapLocation() {
		
		return mapLocation;
	}

	public void setLootChests(Set<LootChest> lootChests) {
		this.lootChests = lootChests;
	}

	public Set<LootChest> getLootChests() {
		
		return lootChests;
	}

	public void save() {

		Bukkit.getScheduler().runTaskAsynchronously(Warfare.getInstance(), () -> Warfare.getInstance().getMySQLManager().saveArena());
	}
}