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
package com.andrewyunt.skywarfare;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.andrewyunt.skywarfare.command.SWCommand;
import com.andrewyunt.skywarfare.configuration.ArenaConfiguration;
import com.andrewyunt.skywarfare.configuration.SignConfiguration;
import com.andrewyunt.skywarfare.db.DataSource;
import com.andrewyunt.skywarfare.db.MySQLSource;
import com.andrewyunt.skywarfare.listeners.PlayerListener;
import com.andrewyunt.skywarfare.listeners.PlayerSkillListener;
import com.andrewyunt.skywarfare.listeners.PlayerUltimateListener;
import com.andrewyunt.skywarfare.listeners.SpectatorsInteractionsListener;
import com.andrewyunt.skywarfare.managers.PlayerManager;
import com.andrewyunt.skywarfare.managers.SignManager;
import com.andrewyunt.skywarfare.menu.ClassCreatorMenu;
import com.andrewyunt.skywarfare.menu.ClassSelectorMenu;
import com.andrewyunt.skywarfare.menu.ShopMenu;
import com.andrewyunt.skywarfare.objects.Arena;
import com.andrewyunt.skywarfare.objects.Game;
import com.andrewyunt.skywarfare.objects.GamePlayer;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

public class SkyWarfare extends JavaPlugin implements PluginMessageListener, Listener {
	
	private static SkyWarfare instance;
	
	private final DataSource dataSource = new MySQLSource();
	
	private PlayerManager playerManager = new PlayerManager();
	private SignManager signManager = new SignManager();
	private ArenaConfiguration arenaConfig = new ArenaConfiguration();
	private SignConfiguration signConfig = new SignConfiguration();
	private ShopMenu shopMenu = new ShopMenu();
	private ClassCreatorMenu classCreatorMenu = new ClassCreatorMenu();
	private ClassSelectorMenu classSelectorMenu = new ClassSelectorMenu();
	private Arena arena;
	private Game game;
	private String serverName;
	
	@Override
	public void onEnable() {
		
		instance = this;
		
		saveDefaultConfig();
		signConfig.saveDefaultConfig();
		
		dataSource.connect();
		dataSource.updateDB();
		
		PluginManager pm = getServer().getPluginManager();
		
		pm.registerEvents(new PlayerListener(), this);
		pm.registerEvents(classSelectorMenu, this);
		
		if (getConfig().getBoolean("is-lobby")) {
			pm.registerEvents(shopMenu, this);
			pm.registerEvents(classCreatorMenu, this);
		} else {
			arenaConfig.saveDefaultConfig();
			
			arena = Arena.loadFromConfig();
			game = new Game();
			
			pm.registerEvents(new PlayerUltimateListener(), this);
			pm.registerEvents(new PlayerSkillListener(), this);
			pm.registerEvents(new SpectatorsInteractionsListener(), this);
		}
		
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
	
	public SignManager getSignManager() {
		
		return signManager;
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
	
	public ClassCreatorMenu getClassCreatorMenu() {
		
		return classCreatorMenu;
	}
	
	public ClassSelectorMenu getClassSelectorMenu() {
		
		return classSelectorMenu;
	}
	
	public void setServerName(String serverName) {
		
		this.serverName = serverName;
	}
	
	public String getServerName() {
		
		return serverName;
	}
}