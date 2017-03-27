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
package com.andrewyunt.warfare.managers;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.exception.PlayerException;
import com.andrewyunt.warfare.objects.GamePlayer;
import com.andrewyunt.warfare.objects.Kit;
import com.andrewyunt.warfare.objects.Purchasable;
import com.andrewyunt.warfare.objects.Skill;
import com.andrewyunt.warfare.objects.Ultimate;

/**
 * The class used to cache players, create players, and perform operations on them.
 * 
 * @author Andrew Yunt
 */
public class PlayerManager {

	private final Map<UUID, GamePlayer> players = new HashMap<UUID, GamePlayer>();

	/**
	 * Creates a GamePlayer with the specified name and adds it to the players map.
	 * 
	 * @param uuid
	 * 		The UUID of the player to be created.
	 * @return
	 * 		The player created with the specified UUID.
	 * @throws PlayerException
	 * 		If a player with the specified UUID already exists, throw PlayerException.
	 */
	public GamePlayer createPlayer(UUID uuid) throws PlayerException {
		
		if (players.containsKey(uuid))
			throw new PlayerException(String.format("The player with the UUID %s already exists.", uuid));
		
		final GamePlayer player = new GamePlayer(uuid);
		
		BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(Warfare.getInstance(), () -> {
			Warfare.getInstance().getMySQLManager().loadPlayer(player);
			
			List<Purchasable> purchases = player.getPurchases();
	
			if (!purchases.contains(Ultimate.HEAL))
				purchases.add(Ultimate.HEAL);
			if (!purchases.contains(Skill.GUARD))
				purchases.add(Skill.GUARD);
			
			if (player.getSelectedKit() == null)
				player.setSelectedKit(Kit.UHC);
			if (player.getSelectedUltimate() == null)
				player.setSelectedUltimate(Ultimate.HEAL);
			if (player.getSelectedSkill() == null)
				player.setSelectedSkill(Skill.GUARD);
		}, 20L);
		
		// Add player to plugin's player map
		players.put(uuid, player);
		
		return player;
	}
	
	/**
	 * Deletes a specified player by removing it from the players map.
	 * 
	 * @param player
	 * 		The player to be deleted from the plugin's records.
	 * @throws PlayerException
	 * 		If the players map does not contain the specified player, throw PlayerException.
	 */
	public void deletePlayer(GamePlayer player) throws PlayerException {
		
		if (!players.containsKey(player.getUUID()))
			throw new PlayerException("The player specified is not in the plugin's records.");
		
		Warfare.getInstance().getMySQLManager().savePlayer(player);
		
		players.remove(player.getUUID());
	}
	
	/**
	 * Gets a collection of all registered players from the players map.
	 * 
	 * @return
	 * 		A collection of players fetched from the players map.
	 */
	public Collection<GamePlayer> getPlayers() {

		return players.values();
	}

	/**
	 * Gets a player with the specified name from the players map.
	 * 
	 * @param name
	 * 		The name of the player to be fetched from the players map.
	 * @return
	 * 		The player instance fetched from the players map.
	 * @throws PlayerException
	 * 		If the players map does not contain a player with the specified 
	 * 		name, throw PlayerException.
	 */
	public GamePlayer getPlayer(String name) throws PlayerException {

		UUID uuid = Bukkit.getPlayer(name).getUniqueId();
		
		if (!players.containsKey(uuid))
			throw new PlayerException("The specified player does not exist.");

		return players.get(uuid);
	}
	
	public GamePlayer getPlayer(Player player) throws PlayerException {
		
		return getPlayer(player.getName());
	}
	
	public GamePlayer getPlayer(UUID uuid) throws PlayerException {
		
		if (players.containsKey(uuid))
			return players.get(uuid);
		else
			throw new PlayerException("No player with the specified UUID exists.");
	}
}