package com.andrewyunt.warfare.objects;

import com.andrewyunt.warfare.Warfare;
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

	private Sign bukkitSign;
	private final Type type;
	private final int place;
	
	public enum Type {
		KILLS_LEADERBOARD("kills"),
		WINS_LEADERBOARD("wins");

		private final String id;

		Type(String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}

		public String id(){
		    return id;
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

		if(block.getState() instanceof Sign){
            bukkitSign = (Sign) block.getState();
        }
	}
	
	public Type getType() {
		
		return type;
	}
	
	public Sign getBukkitSign() {
		
		return bukkitSign;
	}

	public int getPlace() {

		return place;
	}
	
	public void refresh(Map<Integer, Entry<Object, Integer>> mostKills) {
	    if(bukkitSign != null) {
            Entry<Object, Integer> entry = mostKills.get(place);
            String name = (String) entry.getKey();
            bukkitSign.setLine(0, ChatColor.GOLD + "[" + place + "]");
            bukkitSign.setLine(1, name);
            bukkitSign.setLine(2, ChatColor.YELLOW.toString() + entry.getValue() + (type == Type.KILLS_LEADERBOARD ? " Kills" : " Wins"));
            bukkitSign.update();
        }
        else{
	        Warfare.getInstance().getLogger().info("Failed to update sign " + type.name() + " #" + place);
        }
	}
}