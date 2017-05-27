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

	@Getter private Sign bukkitSign;
	@Getter private final Type type;
	@Getter private final int place;
	
	public enum Type {
		KILLS_LEADERBOARD("kills"),
		WINS_LEADERBOARD("wins"),
		KDR_LEADERBOARD("kdr");

		@Getter private final String id;

		Type(String id) {
			this.id = id;
		}
	}
	
	/**
	 * Creates a sign display with the specified location and update interval.
	 *
	 * @param loc
	 * 		The location of the display.
	 * @param place
	 * 		The place on the leaderboard the sign should display.
	 */
	public SignDisplay(Location loc, Type type, int place) {
		this.type = type;
		this.place = place;
		
		Block block = loc.getWorld().getBlockAt(loc);

		if (block.getState() instanceof Sign) {
            bukkitSign = (Sign) block.getState();
        }
	}
	
	public void refresh(Map<Integer, Entry<Object, Integer>> topPlayers) {
	    if (bukkitSign != null) {
            Entry<Object, Integer> entry = topPlayers.get(place);
            String name = (String) entry.getKey();
            bukkitSign.setLine(0, ChatColor.GOLD + "[" + place + "]");
            bukkitSign.setLine(1, name);
            bukkitSign.setLine(2, ChatColor.YELLOW.toString() + entry.getValue() + (type == Type.KILLS_LEADERBOARD
					? " Kills" : type == Type.WINS_LEADERBOARD ? " Wins" : "KDR"));
            bukkitSign.update();
        } else {
	        Warfare.getInstance().getLogger().info("Failed to update sign " + type.name() + " #" + place);
        }
	}
}