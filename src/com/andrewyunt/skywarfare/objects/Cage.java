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

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class Cage {
	
	private String name;
	private Location location;
	private GamePlayer player;
	private Set<Block> blocks = new HashSet<Block>();
	
	public Cage(String name, Location location) {
		
		this.location = location;
		
		World world = location.getWorld();
		double X = location.getX();
		double Y = location.getY();
		double Z = location.getZ();
		
		// bottom block
		blocks.add(world.getBlockAt(new Location(world, X, Y, Z)));
		
		// side bottom blocks
		blocks.add(world.getBlockAt(new Location(world, X - 1, Y + 1, Z)));
		blocks.add(world.getBlockAt(new Location(world, X + 1, Y + 1, Z)));
		blocks.add(world.getBlockAt(new Location(world, X, Y + 1, Z - 1)));
		blocks.add(world.getBlockAt(new Location(world, X, Y + 1, Z + 1)));
		
		// side top blocks
		blocks.add(world.getBlockAt(new Location(world, X - 1, Y + 2, Z)));
		blocks.add(world.getBlockAt(new Location(world, X + 1, Y + 2, Z)));
		blocks.add(world.getBlockAt(new Location(world, X, Y + 2, Z - 1)));
		blocks.add(world.getBlockAt(new Location(world, X, Y + 2, Z + 1)));
		
		// top block
		blocks.add(world.getBlockAt(new Location(world, X, Y + 3, Z)));
		
		// set middle blocks to air
		world.getBlockAt(new Location(world, X + 1, Y, Z)).setType(Material.AIR);
		world.getBlockAt(new Location(world, X + 2, Y, Z)).setType(Material.AIR);
		
		for (Block block : blocks)
			block.setType(Material.GLASS);
	}
	
	public String getName() {
		
		return name;
	}
	
	public void setPlayer(GamePlayer player) {
		
		this.player = player;
		
		if (player == null)
			return;
		
		// Teleport the player to the location;
		Chunk chunk = location.getChunk();
		
		if (!chunk.isLoaded())
			chunk.load();
		
		location.setY(location.getY() + 1);
		
		player.getBukkitPlayer().teleport(location);
		
		location.setY(location.getY() - 1);
		
		// Update player's hotbar
		player.updateHotbar();
	}
	
	public GamePlayer getPlayer() {
		
		return player;
	}
	
	public boolean hasPlayer() {
		
		return player != null;
	}
	
	public Set<Block> getBlocks() {
		
		return blocks;
	}
	
	public void destroy() {
		
		player = null;
		
		for (Block block : blocks)
			block.setType(Material.AIR);
	}
}