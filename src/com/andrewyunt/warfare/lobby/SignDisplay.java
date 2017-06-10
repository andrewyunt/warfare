package com.andrewyunt.warfare.lobby;

import com.andrewyunt.warfare.Warfare;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.util.Map;
import java.util.Map.Entry;

/**
 * The object used to perform operations on signs in the Warfare plugin.
 * 
 * @author Andrew Yunt
 */
public class SignDisplay {

	@Getter private final Type type;
	@Getter private final int place;

	@Getter private Location location;
	@Getter private Sign bukkitSign;
	
	public enum Type {
		KILLS_LEADERBOARD("kills", "Kills"),
		WINS_LEADERBOARD("wins", "Wins"),
		KDR_LEADERBOARD("kdr", "KDR");

		@Getter private final String id;
		@Getter private final String display;

		Type(String id, String display) {
			this.id = id;
			this.display = display;
		}
	}
	
	/**
	 * Creates a sign display with the specified location and update interval.
	 *
	 * @param location
	 * 		The location of the display.
	 * @param place
	 * 		The place on the leaderboard the sign should display.
	 */
	public SignDisplay(Location location, Type type, int place) {
		this.location = location;
		this.type = type;
		this.place = place;
		
		Block block = location.getWorld().getBlockAt(location);

		if (block.getState() instanceof Sign) {
			bukkitSign = (Sign) block.getState();
		}
	}
	
	public void refresh(Map<Integer, Entry<Object, Double>> topPlayers) {
		if (bukkitSign != null) {
			Entry<Object, Double> entry = topPlayers.get(place);
			try {
				bukkitSign.setLine(0, ChatColor.GOLD + "[" + place + "]");
				bukkitSign.setLine(1, (String) entry.getKey());
				bukkitSign.setLine(2, ChatColor.YELLOW.toString() + entry.getValue() + " " + type.getDisplay());
			} catch (NullPointerException e) {
				bukkitSign.setLine(0, "No players with");
				bukkitSign.setLine(1, place + " " + type.getDisplay());
				bukkitSign.setLine(2, "");
			}

			bukkitSign.update();
		} else {
			Warfare.getInstance().getLogger().info("Failed to update sign " + type.name() + " #" + place);
		}
	}
}