package com.andrewyunt.warfare.managers;

import java.util.*;
import java.util.logging.Level;

import com.andrewyunt.warfare.objects.*;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.andrewyunt.warfare.Warfare;

public abstract class StorageManager {
	public abstract boolean connect();

	public abstract void disconnect();

	public void handleException(Exception exception) {
		Warfare.getInstance().getLogger().log(Level.SEVERE, "Exception occurred in MySQL manager ", exception);
	}

	public abstract void updateDB();

	public void savePlayerAsync(GamePlayer player) {
        if (player.isLoaded()) {
            if (Warfare.getInstance().isEnabled()) {
                Bukkit.getScheduler().runTaskAsynchronously(Warfare.getInstance(), () -> savePlayer(player));
            } else {
                savePlayer(player);
            }
        }
	}

	public abstract void savePlayer(GamePlayer player);

	public void loadPlayerAsync(GamePlayer gamePlayer) {
        Bukkit.getScheduler().runTaskAsynchronously(Warfare.getInstance(), () -> loadPlayer(gamePlayer));
    }

	public abstract void loadPlayer(GamePlayer gamePlayer);

    public abstract List<Server> getServers();

	public abstract void updateServerStatus();

	public abstract void saveParty(Party party);

	public abstract Party loadParty(UUID leaderUUID);

    public abstract void saveSign(SignDisplay signDisplay);

    public abstract void deleteSign(SignDisplay signDisplay);

    public abstract void loadSigns();

    public abstract void saveArena();

    public abstract Arena loadArena();

	@Deprecated
	public abstract Map<Integer, Map.Entry<OfflinePlayer, Integer>> getTopFiveColumn(String tableName, String select, String orderBy);
}