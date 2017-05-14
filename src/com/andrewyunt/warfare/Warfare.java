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

import com.andrewyunt.warfare.command.party.PartyCommand;
import com.andrewyunt.warfare.managers.PartyManager;
import com.andrewyunt.warfare.configuration.StaticConfiguration;
import com.andrewyunt.warfare.menu.PlayMenu;
import com.andrewyunt.warfare.scoreboard.ScoreboardHandler;
import com.andrewyunt.warfare.utilities.Utils;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import com.andrewyunt.warfare.command.warfare.WarfareCommand;
import com.andrewyunt.warfare.configuration.ArenaConfiguration;
import com.andrewyunt.warfare.configuration.SignConfiguration;
import com.andrewyunt.warfare.exception.PlayerException;
import com.andrewyunt.warfare.listeners.EntityListener;
import com.andrewyunt.warfare.listeners.PlayerListener;
import com.andrewyunt.warfare.listeners.PlayerSkillListener;
import com.andrewyunt.warfare.listeners.PlayerUltimateListener;
import com.andrewyunt.warfare.listeners.SpectatorsInteractionsListener;
import com.andrewyunt.warfare.managers.MySQLManager;
import com.andrewyunt.warfare.managers.PlayerManager;
import com.andrewyunt.warfare.managers.SignManager;
import com.andrewyunt.warfare.menu.ClassSelectorMenu;
import com.andrewyunt.warfare.menu.ShopMenu;
import com.andrewyunt.warfare.menu.TeleporterMenu;
import com.andrewyunt.warfare.objects.Arena;
import com.andrewyunt.warfare.objects.Game;
import com.andrewyunt.warfare.objects.GamePlayer;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.UUID;

public class Warfare extends JavaPlugin implements PluginMessageListener {
	
	private static Warfare instance;
	
	private MySQLManager mysqlManager;
	private PlayerManager playerManager;
	private SignManager signManager;
	private PartyManager partyManager;
	private ArenaConfiguration arenaConfig;
	private SignConfiguration signConfig;
	private ShopMenu shopMenu;
	private PlayMenu playMenu;
	private ClassSelectorMenu classSelectorMenu;
	private TeleporterMenu teleporterMenu;
	private ScoreboardHandler scoreboardHandler;
	private Arena arena;
	private Game game;
	
	@Override
	public void onEnable() {

		instance = this;

		mysqlManager = new MySQLManager();
		playerManager = new PlayerManager();
		signManager = new SignManager();
		partyManager = new PartyManager();
		arenaConfig = new ArenaConfiguration();
		signConfig = new SignConfiguration();
		shopMenu = new ShopMenu();
		playMenu = new PlayMenu();
		classSelectorMenu = new ClassSelectorMenu();
		teleporterMenu = new TeleporterMenu();
		scoreboardHandler = new ScoreboardHandler();

		saveDefaultConfig();
		
		PluginManager pm = getServer().getPluginManager();
		
		// Connect to the database
		if (!mysqlManager.connect()) {
			getLogger().severe("Could not connect to the database, shutting down...");
			pm.disablePlugin(this);
			return;
		}
		
		mysqlManager.updateDB();
		
		for (Player player : getServer().getOnlinePlayers()) {
			try {
				Warfare.getInstance().getPlayerManager().createPlayer(player.getUniqueId());
			} catch (PlayerException e) {
				e.printStackTrace();
			}
		}
		
		pm.registerEvents(classSelectorMenu, this);
		pm.registerEvents(scoreboardHandler, this);
		pm.registerEvents(new PlayerListener(), this);
		
		if (StaticConfiguration.LOBBY){
			signConfig.saveDefaultConfig();
			signManager.loadSigns();

			pm.registerEvents(shopMenu, this);
			pm.registerEvents(playMenu, this);
		} else {
			arenaConfig.saveDefaultConfig();
			arena = Arena.loadFromConfig();
			game = new Game();			
			mysqlManager.updateServerStatus();
			
			pm.registerEvents(teleporterMenu, this);
			pm.registerEvents(new EntityListener(), this);
			pm.registerEvents(new PlayerUltimateListener(), this);
			pm.registerEvents(new PlayerSkillListener(), this);
			pm.registerEvents(new SpectatorsInteractionsListener(), this);
		}
		
		getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		
		getCommand("warfare").setExecutor(new WarfareCommand());
		getCommand("party").setExecutor(new PartyCommand());
	}


	@Override
	public void onDisable() {

		if (!getConfig().getBoolean("is-lobby")) {
			if (!StaticConfiguration.LOBBY)
				game.setStage(Game.Stage.END);
		}

		for (GamePlayer player : playerManager.getPlayers()) {
			mysqlManager.savePlayer(player);
		}

		mysqlManager.disconnect();
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {

		if (!channel.equals("BungeeCord")) {
			return;
		}

		String received = ByteStreams.newDataInput(message).readUTF();

		Bukkit.getServer().broadcastMessage(received);

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
	}
	
	public static Warfare getInstance() {
		
		return instance;
	}
	
	public MySQLManager getMySQLManager() {
		
		return mysqlManager;
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

	public ArenaConfiguration getArenaConfig() {
		
		return arenaConfig;
	}
	
	public SignConfiguration getSignConfig() {
		
		return signConfig;
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
	
	public ShopMenu getShopMenu() {
		
		return shopMenu;
	}

	public PlayMenu getPlayMenu() {

		return playMenu;
	}
	
	public ClassSelectorMenu getClassSelectorMenu() {
		
		return classSelectorMenu;
	}
	
	public TeleporterMenu getTeleporterMenu() {
		
		return teleporterMenu;
	}

	public ScoreboardHandler getScoreboardHandler() {

		return scoreboardHandler;
	}
}