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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.objects.GamePlayer;

/**
 * The class used to cache players, create players, and perform operations on them.
 * 
 * @author Andrew Yunt
 */
public class PlayerManager {

	private final Map<UUID, GamePlayer> players = new ConcurrentHashMap<>();

	private GamePlayer createPlayer(UUID uuid){
		GamePlayer player = new GamePlayer(uuid);
        Warfare.getInstance().getMySQLManager().loadPlayerAsync(player);
		players.put(uuid, player);
		return player;
	}

	public void deletePlayer(GamePlayer player) {
	    players.remove(player.getUUID());
	}

	public Collection<GamePlayer> getPlayers() {
		return players.values();
	}

	public GamePlayer getPlayer(String name){
		UUID uuid = Bukkit.getPlayer(name).getUniqueId();
		return players.get(uuid);
	}
	
	public GamePlayer getPlayer(Player player){
		return getPlayer(player.getUniqueId());
	}
	
	public GamePlayer getPlayer(UUID uuid) {
		if (players.containsKey(uuid)) {
            return players.get(uuid);
        }
        return createPlayer(uuid);
	}
}