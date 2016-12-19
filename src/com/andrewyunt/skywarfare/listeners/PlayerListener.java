/*
 * Unpublished Copyright (c) 2016 Andrew Yunt, All Rights Reserved.
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
package com.andrewyunt.skywarfare.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;

import com.andrewyunt.skywarfare.SkyWarfare;
import com.andrewyunt.skywarfare.exception.PlayerException;
import com.andrewyunt.skywarfare.objects.Game;
import com.andrewyunt.skywarfare.objects.Game.Stage;
import com.andrewyunt.skywarfare.objects.GamePlayer;

public class PlayerListener implements Listener {
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		
		final Player bp = event.getPlayer();
		
		bp.setMaximumNoDamageTicks(0); // Part of the EPC
		
		BukkitScheduler scheduler = SkyWarfare.getInstance().getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(SkyWarfare.getInstance(), () -> {
			GamePlayer player = null;
			
			// Get the player's GamePlayer object and if it doesn't exist, add it
			try {
				player = SkyWarfare.getInstance().getPlayerManager().createPlayer(bp.getUniqueId());
			} catch (PlayerException e) {
				e.printStackTrace();
			}
			
			Game game = SkyWarfare.getInstance().getGame();
			
			if (SkyWarfare.getInstance().getArena().isEdit()) {
				bp.kickPlayer(ChatColor.RED + "The map is currently in edit mode.");
				return;
			}
			
			if (game.getStage() == Stage.WAITING) {
				player.getBukkitPlayer().sendMessage(ChatColor.GREEN + "You can use " + ChatColor.AQUA + "/lobby"
						+ ChatColor.GREEN + " to return to the lobby.");
				game.addPlayer(player);
				return;
			} else if (game.getStage() == Stage.RESTART) {
				player.getBukkitPlayer().kickPlayer("You may not join during a restart.");
				return;
			}
			
			if (game.getStage() != Stage.WAITING && !bp.hasPermission("skywarfare.spectatorjoin"))
				bp.kickPlayer(ChatColor.RED + "You do not have permission join to spectate games.");
			else
				player.setSpectating(true);
		}, 1L);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		
		Player player = event.getPlayer();
		GamePlayer gp = null;
		
		try {
			gp = SkyWarfare.getInstance().getPlayerManager().getPlayer(player.getName());
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		SkyWarfare.getInstance().getGame().removePlayer(gp);
		
		try {
			SkyWarfare.getInstance().getPlayerManager().deletePlayer(gp);
		} catch (PlayerException e) {
			e.printStackTrace();
		}
	}
}