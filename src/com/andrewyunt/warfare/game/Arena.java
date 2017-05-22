
package com.andrewyunt.warfare.game;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The class used to store arena information.
 * 
 * @author Andrew Yunt
 */
public class Arena {
	
	private final Map<String, Location> cageLocations = new HashMap<>();

	private Set<LootChest> lootChests = new HashSet<>();
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
}