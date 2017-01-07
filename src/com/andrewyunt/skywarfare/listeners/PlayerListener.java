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
package com.andrewyunt.skywarfare.listeners;

import java.lang.reflect.Method;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.Conversable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;

import com.andrewyunt.skywarfare.SkyWarfare;
import com.andrewyunt.skywarfare.exception.PlayerException;
import com.andrewyunt.skywarfare.exception.SignException;
import com.andrewyunt.skywarfare.menu.ClassCreatorMenu;
import com.andrewyunt.skywarfare.menu.ShopMenu;
import com.andrewyunt.skywarfare.objects.Game;
import com.andrewyunt.skywarfare.objects.Game.Stage;
import com.andrewyunt.skywarfare.objects.GamePlayer;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import SebucoHD.Selector.Main;

public class PlayerListener implements Listener {
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		
		Player player = event.getPlayer();
		GamePlayer gp = null;
		
		// Create the player's GamePlayer object
		try {
			gp = SkyWarfare.getInstance().getPlayerManager().createPlayer(player.getUniqueId());
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		final GamePlayer finalGP = gp;
		
		BukkitScheduler scheduler = SkyWarfare.getInstance().getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(SkyWarfare.getInstance(), () -> {
			if (SkyWarfare.getInstance().getConfig().getBoolean("is-lobby")) {
				finalGP.updateHotbar();
				
				player.teleport(player.getLocation().getWorld().getSpawnLocation());
			} else {
				player.setMaximumNoDamageTicks(0); // Part of the EPC
				
				Game game = SkyWarfare.getInstance().getGame();
				
				if (SkyWarfare.getInstance().getArena().isEdit()) {
					player.kickPlayer(ChatColor.RED + "The map is currently in edit mode.");
					return;
				}
				
				if (game.getStage() == Stage.WAITING) {
					player.sendMessage(ChatColor.GREEN + "You can use " + ChatColor.AQUA + "/lobby"
							+ ChatColor.GREEN + " to return to the lobby.");
					game.addPlayer(finalGP);
					return;
				} else if (game.getStage() == Stage.RESTART) {
					player.kickPlayer("You may not join during a restart.");
					return;
				}
				
				if (game.getStage() != Stage.WAITING && !player.hasPermission("skywarfare.spectatorjoin"))
					player.kickPlayer(ChatColor.RED + "You do not have permission join to spectate games.");
				else
					finalGP.setSpectating(true, false);
			}
		}, 2L);
		
		scheduler.scheduleSyncDelayedTask(SkyWarfare.getInstance(), () -> {
			// Fetch server name for scoreboards if it's null
			if (SkyWarfare.getInstance().getServerName() == null) {
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				
				out.writeUTF("GetServer");
				
				player.sendPluginMessage(SkyWarfare.getInstance(), "BungeeCord", out.toByteArray());
			}
		}, 40L);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		
		Player player = event.getPlayer();
		GamePlayer gp = null;
		
		try {
			gp = SkyWarfare.getInstance().getPlayerManager().getPlayer(player.getName());
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		if (!SkyWarfare.getInstance().getConfig().getBoolean("is-lobby"))
			SkyWarfare.getInstance().getGame().removePlayer(gp);
		
		try {
			SkyWarfare.getInstance().getPlayerManager().deletePlayer(gp);
		} catch (PlayerException e) {
			e.printStackTrace();
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		
		ItemStack item = event.getItem();
		
		if (item == null || !item.hasItemMeta())
			return;
		
		Player player = event.getPlayer();
		GamePlayer gp = null;
		
		try {
			gp = SkyWarfare.getInstance().getPlayerManager().getPlayer(player.getName());
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		Material type = item.getType();
		
		if (type == Material.COMPASS) {
			Method method = null;
			
			try {
				method = Main.getInstance().getClass().getDeclaredMethod("getInv", Player.class);
				method.setAccessible(true);
				player.openInventory((Inventory) method.invoke(Main.getInstance(), player));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (type == Material.EMERALD)
			SkyWarfare.getInstance().getShopMenu().open(ShopMenu.Type.MAIN, gp);
		else if (type == Material.CHEST)
			SkyWarfare.getInstance().getClassCreatorMenu().open(ClassCreatorMenu.Type.MAIN, gp, null);
		else if (type == Material.COMMAND)
			SkyWarfare.getInstance().getClassSelectorMenu().open(gp);
	}
	
	@EventHandler
	public void onPlayerDamage(EntityDamageByEntityEvent event) {
		
		Entity damager = event.getDamager();
		Entity damaged = event.getEntity();
		
		if (damager instanceof Projectile)
			damager = (Player) ((Projectile) damager).getShooter();
		
		if (!(damager instanceof Player) || !(damaged instanceof Player))
			return;
		
		GamePlayer damagedGP = null;
		GamePlayer damagerGP = null;
		
		try {
			damagedGP = SkyWarfare.getInstance().getPlayerManager().getPlayer(((Player) damaged).getName());
			damagerGP = SkyWarfare.getInstance().getPlayerManager().getPlayer(((Player) damager).getName());
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		if (!damagerGP.isInGame() || !damagedGP.isInGame())
			return;
		
		damagedGP.setLastDamager(damagerGP);
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		
		if (SkyWarfare.getInstance().getConfig().getBoolean("is-lobby"))
			return;
		
		event.setDroppedExp(0);
		event.setDeathMessage(null);
		
		Player player = event.getEntity();
		GamePlayer playerGP = null;

		try {
			playerGP = SkyWarfare.getInstance().getPlayerManager().getPlayer(player.getName());
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		if (!playerGP.isInGame())
			return;
		
		SkyWarfare.getInstance().getGame().removePlayer(playerGP);
		
		GamePlayer lastDamager = playerGP.getLastDamager();
		
		if (lastDamager == null)
			return;
		
		if (lastDamager == playerGP)
			return;
			
		if (!(lastDamager.isInGame()))
			return;
		
		lastDamager.addKill();
		
		Player lastDamagerBP = lastDamager.getBukkitPlayer();
		int killCoins = 20;
		
		if (lastDamagerBP.hasPermission("megatw.coins.double"))
			killCoins = 40;
		
		if (lastDamagerBP.hasPermission("megatw.coins.triple"))
			killCoins = 60;
		
		lastDamager.setCoins(lastDamager.getCoins() + killCoins);
		
		lastDamagerBP.sendMessage(ChatColor.GOLD + String.format("You killed %s and received %s coins.",
				playerGP.getBukkitPlayer().getDisplayName(), String.valueOf(killCoins)));
		
		playerGP.getBukkitPlayer().sendMessage(ChatColor.RED + String.format("You were killed by %s",
				lastDamager.getBukkitPlayer().getDisplayName()));
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		
		if (SkyWarfare.getInstance().getConfig().getBoolean("is-lobby"))
			return;
		
		Game game = SkyWarfare.getInstance().getGame();
		
		if (game == null || game.getStage() == Stage.WAITING || game.getStage() == Stage.COUNTDOWN)
			return;
		
		GamePlayer gp = null;
		
		try {
			gp = SkyWarfare.getInstance().getPlayerManager().getPlayer(event.getPlayer());
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		event.setRespawnLocation(gp.setSpectating(true, true));
	}
	
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		
		if (SkyWarfare.getInstance().getConfig().getBoolean("is-lobby"))
			return;
		
		GamePlayer player = null;
		
		try {
			player = SkyWarfare.getInstance().getPlayerManager().getPlayer(event.getEntity().getName());
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		Stage stage = SkyWarfare.getInstance().getGame().getStage();
		
		if (stage == Stage.WAITING || stage == Stage.COUNTDOWN)
			event.setCancelled(true);
		else if (player.isSpectating())
			event.setCancelled(true);
	}
	
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onPlayerFall(EntityDamageEvent event) {
		
		if (event.getCause() != DamageCause.FALL)
			return;
		
		if (!(event.getEntity() instanceof Player))
			return;
		
		GamePlayer gp = null;
		
		try {
			gp = SkyWarfare.getInstance().getPlayerManager().getPlayer(((Player) event.getEntity()).getName());
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		if (gp.isInGame() && !gp.hasFallen()) {
			gp.setHasFallen(true);
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		
		cancelCageInteractions(event, event.getPlayer());
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		
		cancelCageInteractions(event, event.getPlayer());
	}
	
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		
		if (event.getInventory().getType() == InventoryType.PLAYER)
			return;
		
		if (!event.getInventory().getTitle().equals("Class Selector"))
			cancelCageInteractions(event, (Player) event.getPlayer());
	}
	
	private void cancelCageInteractions(Cancellable cancellable, Player player) {
		
		if (SkyWarfare.getInstance().getConfig().getBoolean("is-lobby"))
			return;
		
		GamePlayer gp = null;
		
		try {
			gp = SkyWarfare.getInstance().getPlayerManager().getPlayer(player);
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		if (gp.isCaged())
			cancellable.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		
		if (!SkyWarfare.getInstance().getConfig().getBoolean("is-lobby"))
			return;
		
		Player player = event.getPlayer();
		
		if (player.getLocation().getY() < 0)
			player.teleport(player.getLocation().getWorld().getSpawnLocation());
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		
		GamePlayer gp = null;
		
		try {
			gp = SkyWarfare.getInstance().getPlayerManager().getPlayer((Player) event.getPlayer());
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		if (SkyWarfare.getInstance().getConfig().getBoolean("is-lobby"))
			event.setCancelled(true);
		else if (gp.isCaged())
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		
		for (Player player : event.getRecipients()) {
			Conversable conversable = (Conversable) player;
			
			if (conversable.isConversing())
				event.getRecipients().remove(player);
		}
	}
	
	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		
		if (event.getLine(0) == null || event.getLine(1) == null)
			return;
		
		if (!event.getLine(0).equalsIgnoreCase("[Leaderboard]"))
			return;
		
		Player player = event.getPlayer();
		
		if (!player.hasPermission("skywarfare.sign.create")) {
			player.sendMessage(ChatColor.RED + "You do not have permission to create a leaderboard sign.");
			return;
		}
		
		int place = 0;
		
		try {
			place = Integer.valueOf(event.getLine(1));
		} catch (NumberFormatException e) {
			player.sendMessage(ChatColor.RED + "You did not enter an integer for the sign place.");
			return;
		}
		
		if (place > 5) {
			player.sendMessage(ChatColor.RED + "You may not enter a place over 5.");
			return;
		}
		
		try {
			SkyWarfare.getInstance().getSignManager().createSign(
					event.getBlock().getLocation(),
					place,
					6000L);
		} catch (SignException e) {
			e.printStackTrace();
			player.sendMessage(ChatColor.RED + e.getMessage());
		}
	}
}