package com.andrewyunt.skywarfare;

import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.andrewyunt.skywarfare.command.SWCommand;
import com.andrewyunt.skywarfare.configuration.ArenaConfiguration;
import com.andrewyunt.skywarfare.db.DataSource;
import com.andrewyunt.skywarfare.db.MySQLSource;
import com.andrewyunt.skywarfare.listeners.PlayerListener;
import com.andrewyunt.skywarfare.listeners.PlayerSkillListener;
import com.andrewyunt.skywarfare.listeners.PlayerUltimateListener;
import com.andrewyunt.skywarfare.listeners.SpectatorsInteractionsListener;
import com.andrewyunt.skywarfare.managers.PlayerManager;
import com.andrewyunt.skywarfare.menu.ClassCreatorMenu;
import com.andrewyunt.skywarfare.menu.ShopMenu;
import com.andrewyunt.skywarfare.objects.Arena;
import com.andrewyunt.skywarfare.objects.Game;
import com.andrewyunt.skywarfare.objects.GamePlayer;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

public class SkyWarfare extends JavaPlugin implements PluginMessageListener {
	
	private static SkyWarfare instance;
	
	private final DataSource dataSource = new MySQLSource();
	private PlayerManager playerManager = new PlayerManager();
	private ArenaConfiguration arenaConfig = new ArenaConfiguration();
	private ClassCreatorMenu classCreatorMenu = new ClassCreatorMenu();
	private ShopMenu shopMenu = new ShopMenu();
	private Arena arena;
	private Game game;
	private String serverName;
	
	@Override
	public void onEnable() {
		
		instance = this;
		
		arena = Arena.loadFromConfig();
		game = new Game();
		
		// Save default configs to plugin folder
		saveDefaultConfig();
		arenaConfig.saveDefaultConfig();
		
		dataSource.connect();
		dataSource.updateDB();
		
		PluginManager pm = getServer().getPluginManager();
		
		// Register events
		pm.registerEvents(new PlayerListener(), this);
		
		if (getConfig().getBoolean("is-lobby")) {
			pm.registerEvents(classCreatorMenu, this);
			pm.registerEvents(shopMenu, this);
		} else {
			pm.registerEvents(new PlayerUltimateListener(), this);
			pm.registerEvents(new PlayerSkillListener(), this);
			pm.registerEvents(new SpectatorsInteractionsListener(), this);
		}
		
		// Register plugin messaging channels
		getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
		
		getCommand("sw").setExecutor(new SWCommand());
	}
	
	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		
		if (!channel.equals("BungeeCord"))
			return;
		
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		
		if (!in.readUTF().equals("GetServer"))
			return;
		
		serverName = in.readUTF();
		
		for (GamePlayer gp : playerManager.getPlayers())
			gp.updateDynamicScoreboard();
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
	
	public ClassCreatorMenu getClassCreatorMenu() {
		
		return classCreatorMenu;
	}
	
	public ShopMenu getShopMenu() {
		
		return shopMenu;
	}
	
	public void setServerName(String serverName) {
		
		this.serverName = serverName;
	}
	
	public String getServerName() {
		
		return serverName;
	}
}