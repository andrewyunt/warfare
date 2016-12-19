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

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public class Cage {
	
	private String name;
	private Location location;
	private GamePlayer player;
	
	public Cage(String name, Location location) {
		
		this.location = location;
		
		World world = location.getWorld();
		double X = location.getX();
		double Y = location.getY();
		double Z = location.getZ();
		
		// Set bottom block
		world.getBlockAt(new Location(world, X, Y, Z)).setType(Material.GLASS);
		
		// Set side bottom blocks
		world.getBlockAt(new Location(world, X - 1, Y + 1, Z)).setType(Material.GLASS);
		world.getBlockAt(new Location(world, X + 1, Y + 1, Z)).setType(Material.GLASS);
		world.getBlockAt(new Location(world, X, Y + 1, Z - 1)).setType(Material.GLASS);
		world.getBlockAt(new Location(world, X, Y + 1, Z + 1)).setType(Material.GLASS);
		
		// Set side middle blocks
		world.getBlockAt(new Location(world, X - 1, Y + 2, Z)).setType(Material.GLASS);
		world.getBlockAt(new Location(world, X + 1, Y + 2, Z)).setType(Material.GLASS);
		world.getBlockAt(new Location(world, X, Y + 2, Z - 1)).setType(Material.GLASS);
		world.getBlockAt(new Location(world, X, Y + 2, Z + 1)).setType(Material.GLASS);
		
		// Set side top blocks
		world.getBlockAt(new Location(world, X - 1, Y + 3, Z)).setType(Material.GLASS);
		world.getBlockAt(new Location(world, X + 1, Y + 3, Z)).setType(Material.GLASS);
		world.getBlockAt(new Location(world, X, Y + 3, Z - 1)).setType(Material.GLASS);
		world.getBlockAt(new Location(world, X, Y + 3, Z + 1)).setType(Material.GLASS);
		
		// Set top block
		world.getBlockAt(new Location(world, X, Y + 4, Z)).setType(Material.GLASS);
	}
	
	public String getName() {
		
		return name;
	}
	
	public void setPlayer(GamePlayer player) {
		
		this.player = player;
		
		// Teleport the player to the location;
		Chunk chunk = location.getChunk();
		
		if (!chunk.isLoaded())
			chunk.load();
		
		location.setY(location.getY() + 2);
		
		player.getBukkitPlayer().teleport(location);
		
		location.setY(location.getY() - 1);
	}
	
	public GamePlayer getPlayer() {
		
		return player;
	}
	
	public boolean hasPlayer() {
		
		return player != null;
	}
	
	public void destroy() {
		
	}
}