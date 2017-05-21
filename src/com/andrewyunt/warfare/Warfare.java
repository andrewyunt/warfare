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
package com.andrewyunt.warfare;

import com.andrewyunt.warfare.command.BloodToggleCommand;
import com.andrewyunt.warfare.command.LobbyCommand;
import com.andrewyunt.warfare.command.party.PartyCommand;
import com.andrewyunt.warfare.command.warfare.WarfareCommand;
import com.andrewyunt.warfare.configuration.ServerConfiguration;
import com.andrewyunt.warfare.configuration.StaticConfiguration;
import com.andrewyunt.warfare.listeners.*;
import com.andrewyunt.warfare.listeners.fixes.*;
import com.andrewyunt.warfare.managers.PartyManager;
import com.andrewyunt.warfare.managers.PlayerManager;
import com.andrewyunt.warfare.managers.SignManager;
import com.andrewyunt.warfare.managers.StorageManager;
import com.andrewyunt.warfare.managers.mongo.MongoStorageManager;
import com.andrewyunt.warfare.menu.*;
import com.andrewyunt.warfare.objects.*;
import com.andrewyunt.warfare.protocol.EPCAdapter;
import com.andrewyunt.warfare.scoreboard.ScoreboardHandler;
import com.comphenix.protocol.ProtocolLibrary;
import com.google.common.io.ByteStreams;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang.enums.Enum;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class Warfare extends JavaPlugin implements PluginMessageListener {
	
	private static Warfare instance;

    public static Permission permission = null;
    public static Economy economy = null;
    public static Chat chat = null;

    public static Permission getPermission() {
        return permission;
    }

    public static Economy getEconomy() {
        return economy;
    }

    public static Chat getChat() {
        return chat;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }

        return (chat != null);
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }
	
	private StorageManager storageManager;
	private PlayerManager playerManager;
	private SignManager signManager;
	private PartyManager partyManager;
	private ServerConfiguration serverConfiguration;
	private ShopMenu shopMenu;
	private PlayMenu playMenu;
	private KitSelectorMenu kitSelectorMenu;
	private PowerupSelectorMenu powerupSelectorMenu;
	private TeleporterMenu teleporterMenu;
	private ScoreboardHandler scoreboardHandler;
	private Arena arena;
	private Game game;
	
	@Override
	public void onEnable() {

	    Bukkit.getScheduler().runTask(this, () -> {
            setupChat();
            setupEconomy();
            setupPermissions();
        });

		instance = this;

		ProtocolLibrary.getProtocolManager().addPacketListener(new EPCAdapter(this));

		storageManager = new MongoStorageManager(this);
		playerManager = new PlayerManager();
		signManager = new SignManager(this);
		partyManager = new PartyManager();
		serverConfiguration = new ServerConfiguration();
		shopMenu = new ShopMenu();
		playMenu = new PlayMenu();
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
		
        pm.registerEvents(new PotFixListener(this), this);
        pm.registerEvents(new ColonCommandFix(this), this);
        pm.registerEvents(new WeatherFixListener(), this);
        pm.registerEvents(new InfinityArrowFixListener(), this);
        pm.registerEvents(new ChatListener(this), this);
		
		if (StaticConfiguration.LOBBY){
			storageManager.loadSigns();

			pm.registerEvents(shopMenu, this);
			pm.registerEvents(playMenu, this);
			pm.registerEvents(new PlayerLobbyListener(), this);
		} else {
			serverConfiguration.saveDefaultConfig();

			arena = storageManager.loadArena();
			game = new Game();			
			storageManager.updateServerStatusAsync();
			
			pm.registerEvents(teleporterMenu, this);
			pm.registerEvents(new EntityListener(), this);
			pm.registerEvents(new PlayerGameListener(), this);
			pm.registerEvents(new PlayerPowerupListener(), this);
			pm.registerEvents(new PlayerPerkListener(), this);
			pm.registerEvents(new SpectatorsInteractionsListener(), this);

			getCommand("lobby").setExecutor(new LobbyCommand(this));
		}
		
		getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
		
		getCommand("warfare").setExecutor(new WarfareCommand());
		getCommand("party").setExecutor(new PartyCommand());
		getCommand("bloodtoggle").setExecutor(new BloodToggleCommand());

		for(World world: Bukkit.getWorlds()){
			for(Creature creature: world.getEntitiesByClass(Creature.class)){
				creature.remove();
			}
		}
	}


	@Override
	public void onDisable() {

		if (!getConfig().getBoolean("is-lobby")) {
			if (!StaticConfiguration.LOBBY) {
			    if(game.getStage() != Game.Stage.RESTART) {
                    game.setStage(Game.Stage.RESTART);
                }
			}
		}

		storageManager.disconnect();

		ProtocolLibrary.getProtocolManager().removePacketListeners(this);
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {

		if (!channel.equals("BungeeCord")) {
			return;
		}

		String received = ByteStreams.newDataInput(message).readUTF();

		Bukkit.getServer().broadcastMessage(received);

		//TODO: Recode
		/*
		if (!received.contains("MOVEPARTY")) {
			String[] firstArray = received.split("\\\\W+");
			String[] secondArray = firstArray[1].split("\\\\W+");

			for (UUID sendUUID : partyManager.getParty(UUID.fromString(secondArray[0])).getMembers()) {
				OfflinePlayer sendPlayer = Bukkit.getServer().getOfflinePlayer(sendUUID);

				if (sendPlayer.isOnline()) {
					Utils.sendPlayerToServer((Player) sendPlayer, secondArray[1]);
				}
			}
		}
		*/
	}
	
	public static Warfare getInstance() {
		
		return instance;
	}
	
	public StorageManager getStorageManager() {
		
		return storageManager;
	}
	
	public PlayerManager getPlayerManager() {
		
		return playerManager;
	}
	
	public SignManager getSignManager() {
		
		return signManager;
	}

	public PartyManager getPartyManager() {

		return partyManager;
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

	public ServerConfiguration getServerConfiguration() {

		return serverConfiguration;
	}
	
	public ShopMenu getShopMenu() {
		
		return shopMenu;
	}

	public PlayMenu getPlayMenu() {

		return playMenu;
	}
	
	public KitSelectorMenu getKitSelectorMenu() {
		
		return kitSelectorMenu;
	}

	public PowerupSelectorMenu getPowerupSelectorMenu() {

		return powerupSelectorMenu;
	}
	
	public TeleporterMenu getTeleporterMenu() {
		
		return teleporterMenu;
	}

	public ScoreboardHandler getScoreboardHandler() {

		return scoreboardHandler;
	}
}