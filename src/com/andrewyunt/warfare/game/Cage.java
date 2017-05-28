package com.andrewyunt.warfare.game;

import java.util.HashSet;
import java.util.Set;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.player.GamePlayer;
import lombok.Getter;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public class Cage {
	
	@Getter private String name;
	@Getter private GamePlayer player;

	private final Location location;
	private final Set<Block> blocks = new HashSet<>();
	
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
		world.getBlockAt(new Location(world, X, Y + 1, Z)).setType(Material.AIR);
		world.getBlockAt(new Location(world, X, Y + 2, Z)).setType(Material.AIR);
		
		for (Block block : blocks) {
            block.setType(Material.GLASS);
        }
	}

	public void setPlayer(GamePlayer player) {
		
		this.player = player;
		
		if (player == null) {
            return;
        }
		
		// Teleport the player to the location;
		Chunk chunk = location.getChunk();
		
		if (!chunk.isLoaded()) {
            chunk.load();
        }

        Location location = this.location.clone();
		location.setX(location.getBlockX() + 0.5);
		location.setY(location.getBlockY() + 1);
		location.setZ(location.getBlockZ() + 0.5);

		Vector vector = Warfare.getInstance().getArena().getMapLocation().toVector().subtract(location.toVector()).normalize();
		vector.setY(0.5);

		location.setDirection(vector);
		location.setPitch(0);

		player.getBukkitPlayer().teleport(location);
	}
	
	public boolean hasPlayer() {
		return player != null;
	}
	
	public void destroy() {
		
		player = null;
		
		for (Block block : blocks) {
            block.setType(Material.AIR);
        }
	}
}