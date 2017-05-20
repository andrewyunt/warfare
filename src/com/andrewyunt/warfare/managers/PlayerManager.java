
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