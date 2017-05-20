
package com.andrewyunt.warfare.managers;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.objects.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The class used to cache players, create players, and perform operations on them.
 * 
 * @author Andrew Yunt
 */
public class PlayerManager {

	private final Map<UUID, GamePlayer> players = new ConcurrentHashMap<>();

	private GamePlayer createPlayer(Player player){
		GamePlayer gamePlayer = new GamePlayer(player.getUniqueId());
        Warfare.getInstance().getStorageManager().loadPlayerAsync(gamePlayer);
		players.put(player.getUniqueId(), gamePlayer);
		return gamePlayer;
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

	public GamePlayer getPlayer(UUID uuid){
	    GamePlayer gamePlayer = players.get(uuid);
	    if(gamePlayer == null){
	        Player player = Bukkit.getPlayer(uuid);
	        if(player != null){
	            return createPlayer(player);
            }
            return null;
        }
        return gamePlayer;
    }
	
	public GamePlayer getPlayer(Player player) {
		if (players.containsKey(player.getUniqueId())) {
            return players.get(player.getUniqueId());
        }
        return createPlayer(player);
	}
}