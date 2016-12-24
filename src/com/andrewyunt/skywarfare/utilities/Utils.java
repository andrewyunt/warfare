package com.andrewyunt.skywarfare.utilities;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.andrewyunt.skywarfare.objects.GamePlayer;

import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.NBTTagList;

public class Utils {

	public static Location deserializeLocation(ConfigurationSection section) {

		return new Location(Bukkit.getWorld(section.getString("w")), section.getDouble("x"), section.getDouble("y"),
				section.getDouble("z"));
	}

	public static Map<String, Object> serializeLocation(Location loc) {

		Map<String, Object> map = new HashMap<String, Object>();

		map.put("w", loc.getWorld().getName());
		map.put("x", loc.getX());
		map.put("y", loc.getY());
		map.put("z", loc.getZ());

		return map;
	}

	public static ItemStack removeAttributes(ItemStack is) {
		
		net.minecraft.server.v1_7_R4.ItemStack nmsStack = CraftItemStack.asNMSCopy(is);
		NBTTagCompound tag;
		
		if (nmsStack == null)
			return is;
		
		if (!nmsStack.hasTag()) {
			tag = new NBTTagCompound();
			nmsStack.setTag(tag);
		} else
			tag = nmsStack.getTag();
		
		NBTTagList am = new NBTTagList();
		tag.set("AttributeModifiers", am);
		nmsStack.setTag(tag);
		
		return CraftItemStack.asCraftMirror(nmsStack);
	}
	
	public static void colorPlayerName(GamePlayer toColor, Collection<GamePlayer> toShowPlayers) {

		for (GamePlayer toShow : toShowPlayers) {
			if (!toShow.isInGame())
				continue;

			if (toShow == toColor)
				continue;

			Scoreboard scoreboard = toShow.getDynamicScoreboard().getScoreboard();

			Team team = scoreboard.getTeam("enemies");

			if (team == null) {
				team = scoreboard.registerNewTeam("enemies");
				team.setPrefix(ChatColor.RED.toString());
			}

			team.addPlayer(toColor.getBukkitPlayer());
		}
	}
}