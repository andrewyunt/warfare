package com.andrewyunt.warfare.utilities;

import java.util.*;
import java.util.stream.Collectors;

import com.andrewyunt.warfare.configuration.StaticConfiguration;
import com.andrewyunt.warfare.objects.Party;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.objects.GamePlayer;

public class Utils {
	
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
			if (!toShow.isInGame()) {
                continue;
            }

			if (toShow == toColor) {
                continue;
            }

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
		
		if (section == null) {
            return 1;
        }
		
		Set<String> keys = section.getKeys(false);
		
		if (keys.size() == 0) {
            return 0;
        }
		
		for (String key : section.getKeys(false)) {
			int num = Integer.valueOf(key);
			
			if (highest < num) {
                highest = num;
            }
		}
		
		return highest;
	}
	
	public static String getNumberSuffix(int num) {
		
		num = num % 100;
		
		if (num >= 11 && num <= 13) {
            return "th";
        }
		
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
	
	public static String formatMessage(String message) {
		
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public static void sendPlayerToServer(Player player, String serverName) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Connect");
		out.writeUTF(serverName);
		player.sendPluginMessage(Warfare.getInstance(), "BungeeCord", out.toByteArray());
	}

	public static void sendPartyToServer(Player player, Party party, String serverName) {
		for(String lobby: StaticConfiguration.LOBBY_SERVERS){
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("Forward");
			out.writeUTF(lobby);
			out.writeUTF("SendParty");
			out.writeUTF(party.getMembers().stream().map(UUID::toString).collect(Collectors.joining(",")));
			out.writeUTF(serverName);
			player.sendPluginMessage(Warfare.getInstance(), "BungeeCord", out.toByteArray());
		}
	}

	/**
	 * Rotates the given vector around the y axis. This modifies the given
	 * vector.
	 *
	 * @author blablubbabc
	 * @param vector
	 * 		the vector to rotate
	 * @param angleD
	 * 		the angle of the rotation in degrees
	 */
	public static void rotateYAxis(Vector vector, double angleD) {

		vector = vector.clone();

		// Validate.notNull(vector);
		if (angleD == 0.0D) {
			return;
		}

		double angleR = Math.toRadians(angleD);
		double x = vector.getX();
		double z = vector.getZ();
		double cos = Math.cos(angleR);
		double sin = Math.sin(angleR);

		vector.setX(x * cos + z * (-sin));
		vector.setZ(x * sin + z * cos);
	}

	public static List<org.bukkit.entity.Entity> getNearbyEntities(Location loc, int distance) {

		return loc.getWorld().getEntities().stream()
				.filter(e -> loc.distanceSquared(e.getLocation()) <= distance * distance).collect(Collectors.toList());
	}

	/**
	 * Plays blood effect for players (in the radius) around the specified
	 * player that are in game and has blood effect on.
	 *
	 * <p>
	 * Note that the effect will not be played for the specified player.
	 * </p>
	 *
	 * @param damagedPlayer
	 * 		The damaged player (blood will be played on him) - He will not see the effect.
	 * @param bloodRadius
	 * 		The radius from damagedPlayer the effect will be played in.
	 */
	public static void playBloodEffect(Player damagedPlayer, int bloodRadius) {

		GamePlayer damagedGP = Warfare.getInstance().getPlayerManager().getPlayer(damagedPlayer.getName());

		if (!damagedGP.isInGame())
			return;

		Location loc = damagedPlayer.getLocation();

		for (Entity entity : Utils.getNearbyEntities(loc, bloodRadius)) {
			if (!(entity instanceof Player))
				continue;

			Player player = (Player) entity;
			GamePlayer gp = Warfare.getInstance().getPlayerManager().getPlayer(player.getName());

			if (player == damagedPlayer)
				continue;

			if (!gp.isInGame())
				continue;

			if (!gp.getHasBloodEffect())
				continue;

			player.playEffect(loc.add(0.0D, 0.8D, 0.0D), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
		}
	}
}