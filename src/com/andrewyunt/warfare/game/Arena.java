package com.andrewyunt.warfare.game;

import lombok.Getter;
import lombok.Setter;
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
	
	@Getter private final Map<String, Location> cageLocations = new HashMap<>();

	@Getter @Setter private Set<LootChest> lootChests = new HashSet<>();
	@Getter @Setter private boolean isEdit;
	@Getter @Setter private Location mapLocation;
}