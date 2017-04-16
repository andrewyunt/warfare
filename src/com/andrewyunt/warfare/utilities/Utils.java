package com.andrewyunt.warfare.utilities;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.objects.GamePlayer;

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
	
	public static List<String> colorizeList(List<String> list, ChatColor color) {

		return list.stream().map(line -> color + line).collect(Collectors.toList());
	}

	/*public static ItemStack removeAttributes(ItemStack is) {
		
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
	}*/
	
	public static void colorPlayerName(GamePlayer toColor, Collection<GamePlayer> toShowPlayers) {

		for (GamePlayer toShow : toShowPlayers) {
			if (!toShow.isInGame())
				continue;

			if (toShow == toColor)
				continue;

			Scoreboard scoreboard = Warfare.getInstance().getScoreboardHandler().getPlayerBoard(toShow.getBukkitPlayer().getUniqueId()).getScoreboard();
			Team team = scoreboard.getTeam("enemies");

			if (team == null) {
				team = scoreboard.registerNewTeam("enemies");
				team.setPrefix(ChatColor.RED.toString());
			}

			team.addPlayer(toColor.getBukkitPlayer());
		}
	}
	
	public static int getHighestEntry(ConfigurationSection section) {
		
		int highest = 0;
		
		if (section == null)
			return 1;
		
		Set<String> keys = section.getKeys(false);
		
		if (keys.size() == 0)
			return 0;
		
		for (String key : section.getKeys(false)) {
			int num = Integer.valueOf(key);
			
			if (highest < num)
				highest = num;
		}
		
		return highest;
	}
	
	public static String getNumberSuffix(int num) {
		
		num = num % 100;
		
		if (num >= 11 && num <= 13)
			return "th";
		
		switch (num % 10) {
		case 1:
			return "st";
		case 2:
			return "nd";
		case 3:
			return "rd";
		default:
			return "th";
		}
	}
	
	public static String getFormattedMessage(String configPath) {
		
		return ChatColor.translateAlternateColorCodes('&', Warfare.getInstance().getConfig().getString(configPath));
	}
}