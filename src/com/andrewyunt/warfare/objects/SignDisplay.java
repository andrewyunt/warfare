/*
 * Unpublished Copyright (c) 2016 Andrew Yunt, All Rights Reerved.
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
package com.andrewyunt.warfare.objects;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitScheduler;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.utilities.Utils;

/**
 * The object used to perform operations on signs in the Warfare plugin.
 * 
 * @author Andrew Yunt
 */
public class SignDisplay {
	
	private final int configNumber;
	private Sign bukkitSign;
	private final Type type;
	private final int place;
	
	public enum Type {
		KILLS_LEADERBOARD,
		WINS_LEADERBOARD
	}
	
	/**
	 * Creates a sign display with the specified location and update interval.
	 * 
	 * @param configNumber
	 * 		The number for the sign in the signs configuration section;
	 * @param loc
	 * 		The location of the display.
	 * @param place
	 * 		The place on the leaderboard the sign should display.
	 * @param load
	 * 		Set this to true if the sign was loaded from a the configuration.
	 */
	public SignDisplay(int configNumber, Location loc, Type type, int place, boolean load) {
		
		this.configNumber = configNumber;
		this.type = type;
		this.place = place;
		
		Block block = loc.getWorld().getBlockAt(loc);
		
		if (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN)
			bukkitSign =(Sign) block.getState();
		
		if (!load)
			save();
		
		BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(Warfare.getInstance(), new Runnable() {
			boolean refresh = !load;
			
			@Override
			public void run() {
				
				if (refresh)
					refresh();
				
				refresh = true;
			}
		}, 0L, 6000L);
	}
	
	public int getConfigNumber() {
		
		return configNumber;
	}
	
	public Type getType() {
		
		return type;
	}
	
	public Sign getBukkitSign() {
		
		return bukkitSign;
	}
	
	public void refresh() {
		
		
		
		Map<Integer, Entry<OfflinePlayer, Integer>> mostKills = Warfare.getInstance().getMySQLManager()
				.getTopFiveColumn("uuid", "Players", type == Type.KILLS_LEADERBOARD ? "kills" : "wins");
		Entry<OfflinePlayer, Integer> entry = mostKills.get(place);
		
		OfflinePlayer op = entry.getKey();
		
		bukkitSign.setLine(0, op.getName());
		bukkitSign.setLine(1, entry.getValue() + (type == Type.KILLS_LEADERBOARD ? " Kills" : " Wins"));
		bukkitSign.setLine(3, place + Utils.getNumberSuffix(place) + " Place");

		bukkitSign.update();
	}
	
	public void save() {
		
		Warfare plugin = Warfare.getInstance();
		FileConfiguration signConfig = plugin.getSignConfig().getConfig();
		
		signConfig.set("signs." + configNumber + ".type", type.toString());
		signConfig.set("signs." + configNumber + ".place", place);
		
		signConfig.createSection("signs." + configNumber + ".location",
				Utils.serializeLocation(bukkitSign.getLocation()));
		
		Warfare.getInstance().getSignConfig().saveConfig();
		
		Warfare.getInstance().getSignManager().loadSign(
				signConfig.getConfigurationSection("signs." + String.valueOf(configNumber)));
	}
	
	public static SignDisplay loadFromConfig(ConfigurationSection section) {
		
		SignDisplay signDisplay = null;
		Type type = Type.valueOf(section.getString("type"));
		int place = section.getInt("place");
		Location loc = Utils.deserializeLocation(section.getConfigurationSection("location"));
		
		signDisplay = new SignDisplay(Integer.valueOf(section.getName()), loc, type, place, true);
		
		return signDisplay;
	}
}