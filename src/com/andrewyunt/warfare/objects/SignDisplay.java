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

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.scheduler.BukkitScheduler;

import com.andrewyunt.warfare.Warfare;

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
		KILLS_LEADERBOARD,
		WINS_LEADERBOARD
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
            BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
            scheduler.runTaskTimer(Warfare.getInstance(), this::refresh, 0L, 6000L);
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
	
	public void refresh() {
		Bukkit.getScheduler().runTaskAsynchronously(Warfare.getInstance(), () -> {
			Map<Integer, Entry<OfflinePlayer, Integer>> mostKills = Warfare.getInstance().getMySQLManager()
					.getTopFiveColumn("Players", "uuid", type == Type.KILLS_LEADERBOARD ? "kills" : "wins");
			Entry<OfflinePlayer, Integer> entry = mostKills.get(place);
			OfflinePlayer op = entry.getKey();
			String name = op.getName();
			Bukkit.getScheduler().runTask(Warfare.getInstance(), () -> {
				bukkitSign.setLine(0, ChatColor.GOLD + "[" + place + "]");
				bukkitSign.setLine(1, name);
				bukkitSign.setLine(2, ChatColor.YELLOW.toString() + entry.getValue() + (type == Type.KILLS_LEADERBOARD ? " Kills" : " Wins"));
				bukkitSign.update();
			});
		});
	}
}