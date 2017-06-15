package com.andrewyunt.warfare.game;

import java.util.*;

import com.andrewyunt.warfare.player.GamePlayer;
import lombok.Getter;
import net.minecraft.server.v1_8_R3.BlockStainedGlass;
import net.minecraft.server.v1_8_R3.BlockStainedGlassPane;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Colorable;
import org.bukkit.material.Wool;

public class Cage {
	
	@Getter private String name;
	@Getter private GamePlayer player;

	@Getter private final Location location;
	private final Set<Block> blocks = new HashSet<>();
	
	public Cage(String name, Location location) {
		this.name = name;
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

		setBlocks();
	}

	public void setBlocks() {
		int durability = new Random().nextInt(13) + 1;
		List<DyeColor> colors = Arrays.asList(DyeColor.values());
		Collections.shuffle(colors);
		DyeColor color = colors.iterator().next();
		for (Block block : blocks) {
			block.setType(Material.STAINED_GLASS);
			block.setData(color.getData());
		}
	}

	public Location setPlayer(GamePlayer player) {
		this.player = player;
		
		if (player == null) {
            return null;
        }

		return location;
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