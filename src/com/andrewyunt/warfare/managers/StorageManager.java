package com.andrewyunt.warfare.managers;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.lobby.Server;
import com.andrewyunt.warfare.lobby.SignDisplay;
import com.andrewyunt.warfare.player.GamePlayer;
import com.andrewyunt.warfare.player.Party;
import com.andrewyunt.warfare.player.Transaction;
import org.bukkit.Bukkit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

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

	public void updateServerStatusAsync() {
        if (Warfare.getInstance().isEnabled()) {
            Bukkit.getScheduler().runTaskAsynchronously(Warfare.getInstance(), this::updateServerStatus);
        } else {
            updateServerStatus();
        }
    }

	public abstract void saveParty(Party party);

	public abstract Party loadParty(UUID leaderUUID);

    public abstract void setPartyServer(Party party, String server);

    public abstract void getPartyServers();

    public abstract void saveSign(SignDisplay signDisplay);

    public abstract void deleteSign(SignDisplay signDisplay);

    public abstract void loadSigns();

    public abstract void saveMap();

    public abstract void loadMap();

    public abstract void savePendingTransaction(Transaction transaction);

    public abstract void resolvePendingTransactions(GamePlayer player);

    @Deprecated
	public abstract Map<Integer, Map.Entry<Object, Double>> getTopFiveColumn(String tableName, String select, String orderBy);
}