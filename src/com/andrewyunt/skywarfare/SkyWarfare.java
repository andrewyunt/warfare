package com.andrewyunt.skywarfare;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.andrewyunt.skywarfare.configuration.ArenaConfiguration;
import com.andrewyunt.skywarfare.db.DataSource;
import com.andrewyunt.skywarfare.db.MySQLSource;
import com.andrewyunt.skywarfare.listeners.PlayerListener;
import com.andrewyunt.skywarfare.listeners.PlayerSkillListener;
import com.andrewyunt.skywarfare.listeners.PlayerUltimateListener;
import com.andrewyunt.skywarfare.listeners.SpectatorsInteractionsListener;
import com.andrewyunt.skywarfare.managers.PlayerManager;
import com.andrewyunt.skywarfare.objects.Arena;
import com.andrewyunt.skywarfare.objects.Game;

public class SkyWarfare extends JavaPlugin {
	
	private static SkyWarfare instance;
	
	private final DataSource dataSource = new MySQLSource();
	private PlayerManager playerManager = new PlayerManager();
	private ArenaConfiguration arenaConfig = new ArenaConfiguration();
	private Arena arena;
	private Game game;
	
	@Override
	public void onEnable() {
		
		instance = this;
		
		arena = Arena.loadFromConfig();
		game = new Game();
		
		// Save default configs to plugin folder
		saveDefaultConfig();
		arenaConfig.saveDefaultConfig();
		
		PluginManager pm = getServer().getPluginManager();
		
		// Register events
		pm.registerEvents(new PlayerListener(), this);
		pm.registerEvents(new PlayerUltimateListener(), this);
		pm.registerEvents(new PlayerSkillListener(), this);
		pm.registerEvents(new SpectatorsInteractionsListener(), this);
	}
	
	public static SkyWarfare getInstance() {
		
		return instance;
	}
	
	public DataSource getDataSource() {
		
		return dataSource;
	}
	
	public PlayerManager getPlayerManager() {
		
		return playerManager;
	}

	public ArenaConfiguration getArenaConfig() {
		
		return arenaConfig;
	}
	
	public Arena getArena() {
		
		return arena;
	}
	
	public Game getGame() {
		
		return game;
	}
	
	public void setGame(Game game) {
		
		this.game = game;
	}
}