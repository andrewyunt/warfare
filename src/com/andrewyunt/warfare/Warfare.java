package com.andrewyunt.warfare;

import com.andrewyunt.warfare.command.*;
import com.andrewyunt.warfare.command.party.PartyCommand;
import com.andrewyunt.warfare.command.warfare.WarfareCommand;
import com.andrewyunt.warfare.configuration.ServerConfiguration;
import com.andrewyunt.warfare.configuration.StaticConfiguration;
import com.andrewyunt.warfare.game.Game;
import com.andrewyunt.warfare.listeners.*;
import com.andrewyunt.warfare.listeners.fixes.*;
import com.andrewyunt.warfare.managers.PartyManager;
import com.andrewyunt.warfare.managers.PlayerManager;
import com.andrewyunt.warfare.managers.SignManager;
import com.andrewyunt.warfare.managers.StorageManager;
import com.andrewyunt.warfare.managers.mongo.MongoStorageManager;
import com.andrewyunt.warfare.menu.*;
import com.andrewyunt.warfare.protocol.EPCAdapter;
import com.andrewyunt.warfare.scoreboard.ScoreboardHandler;
import com.comphenix.protocol.ProtocolLibrary;
import com.faithfulmc.framework.BasePlugin;
import com.faithfulmc.framework.server.FaithfulServer;
import com.faithfulmc.framework.server.ServerSettings;
import lombok.Getter;
import lombok.Setter;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Creature;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Getter
public class Warfare extends JavaPlugin {

	@Getter private static Warfare instance;
	@Getter private static Permission permission = null;
	@Getter private static Economy economy = null;
	@Getter private static Chat chat = null;

	private StorageManager storageManager;
	private PlayerManager playerManager;
	private SignManager signManager;
	private PartyManager partyManager;
	private ServerConfiguration serverConfiguration;
	private ShopMenu shopMenu;
	private KitSelectorMenu kitSelectorMenu;
	private PowerupSelectorMenu powerupSelectorMenu;
	private TeleporterMenu teleporterMenu;
	private ScoreboardHandler scoreboardHandler;
	@Setter private Game game;
	private boolean isProtocoLib;
	@Setter private int games = 0;

    private final int maxGames = 24 + ThreadLocalRandom.current().nextInt(20);
	private final long startupTime = System.currentTimeMillis();
	
	@Override
	public void onEnable() {
		Bukkit.getScheduler().runTask(this, () -> {
			setupChat();
            setupEconomy();
            setupPermissions();
        });

	    instance = this;

	    if (getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
		    isProtocoLib = true;
            ProtocolLibrary.getProtocolManager().addPacketListener(new EPCAdapter());
        } else {
		    isProtocoLib = false;
        }

		storageManager = new MongoStorageManager();
		playerManager = new PlayerManager();
		signManager = new SignManager();
		partyManager = new PartyManager();
		serverConfiguration = new ServerConfiguration();
		shopMenu = new ShopMenu();
		kitSelectorMenu = new KitSelectorMenu();
		powerupSelectorMenu = new PowerupSelectorMenu();
		teleporterMenu = new TeleporterMenu();
		scoreboardHandler = new ScoreboardHandler();

		saveDefaultConfig();
		
		PluginManager pm = getServer().getPluginManager();
		
		// Connect to the database
		if (!storageManager.connect()) {
			getLogger().severe("Could not connect to the database, shutting down...");
			pm.disablePlugin(this);
			return;
		}
		
		storageManager.updateDB();
		
		pm.registerEvents(kitSelectorMenu, this);
		pm.registerEvents(powerupSelectorMenu, this);
		pm.registerEvents(scoreboardHandler, this);

        pm.registerEvents(new ColonCommandFixListener(), this);
        pm.registerEvents(new WeatherFixListener(), this);
        pm.registerEvents(new DecayFixListener(), this);
        pm.registerEvents(new ChatListener(), this);
		
		if (StaticConfiguration.LOBBY) {
			storageManager.loadSigns();

			getCommand("spawn").setExecutor(new SpawnCommand());

			pm.registerEvents(shopMenu, this);
			pm.registerEvents(new PlayerLobbyListener(), this);
		} else {
			serverConfiguration.saveDefaultConfig();

			game = new Game();
			storageManager.loadMap();

			BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
			scheduler.scheduleSyncDelayedTask(Warfare.getInstance(), () -> storageManager.updateServerStatusAsync(), 600);

			pm.registerEvents(teleporterMenu, this);
			pm.registerEvents(new GameListener(), this);
			pm.registerEvents(new EntityListener(), this);
			pm.registerEvents(new PlayerGameListener(), this);
			pm.registerEvents(new PlayerPowerupListener(), this);
			pm.registerEvents(new PlayerPerkListener(), this);
			pm.registerEvents(new SpectatorsInteractionsListener(), this);
			pm.registerEvents(new PotFixListener(), this);
			pm.registerEvents(new InfinityArrowFixListener(), this);
		}
		
		getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		
		getCommand("warfare").setExecutor(new WarfareCommand());
		getCommand("party").setExecutor(new PartyCommand());
		getCommand("lobby").setExecutor(new LobbyCommand());
		getCommand("bloodtoggle").setExecutor(new BloodToggleCommand());
		getCommand("setspawn").setExecutor(new SetSpawnCommand());
		getCommand("coins").setExecutor(new CoinsCommand());
		getCommand("stats").setExecutor(new StatsCommand());

		for (World world: Bukkit.getWorlds()) {
			for (Creature creature: world.getEntitiesByClass(Creature.class)) {
				creature.remove();
			}
		}

		for (World world: Bukkit.getWorlds()) {
		    world.setAutoSave(false);
		    world.setAmbientSpawnLimit(0);
		    world.setAnimalSpawnLimit(0);
		    world.setMonsterSpawnLimit(0);
		    world.setWaterAnimalSpawnLimit(0);
		    world.setKeepSpawnInMemory(StaticConfiguration.LOBBY);
		    world.setPVP(!StaticConfiguration.LOBBY);
		    world.setTicksPerAnimalSpawns(100000);
            world.setTicksPerMonsterSpawns(100000);
            world.setTime(0);
            world.setGameRuleValue("doDaylightCycle", "false");
        }

		ServerSettings.SAVE_ENTRIES = false;
		ServerSettings.setName(StaticConfiguration.SERVER_NAME);
		ServerSettings.ACCEPTING_NEW = false;

		Bukkit.getScheduler().runTask(this, () -> BasePlugin.getPlugin().setFaithfulServer(new FaithfulServer(BasePlugin.getPlugin())));
	}

	@Override
	public void onDisable() {
		if (!StaticConfiguration.LOBBY) {
			if (game.getStage() != Game.Stage.RESTART) {
				game.setStage(Game.Stage.RESTART);
			}
		}

		storageManager.disconnect();

        if (isProtocoLib) {
            ProtocolLibrary.getProtocolManager().removePacketListeners(this);
        }
	}

	private void setupPermissions() {
		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		if (permissionProvider != null) {
			permission = permissionProvider.getProvider();
		}
	}

	private void setupChat() {
		RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
		if (chatProvider != null) {
			chat = chatProvider.getProvider();
		}
	}

	private void setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
	}

	public boolean needsRestart() {
		return games >= maxGames || System.currentTimeMillis() - startupTime > TimeUnit.DAYS.toMillis(7);
	}
}