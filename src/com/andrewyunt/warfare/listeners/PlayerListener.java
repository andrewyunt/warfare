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
package com.andrewyunt.warfare.listeners;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.andrewyunt.warfare.objects.Kit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.Conversable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.exception.PlayerException;
import com.andrewyunt.warfare.exception.SignException;
import com.andrewyunt.warfare.menu.ClassSelectorMenu;
import com.andrewyunt.warfare.menu.ShopMenu;
import com.andrewyunt.warfare.objects.Game;
import com.andrewyunt.warfare.objects.Game.Stage;
import com.andrewyunt.warfare.objects.GamePlayer;
import com.andrewyunt.warfare.objects.SignDisplay;
import com.andrewyunt.warfare.objects.SignDisplay.Type;
import com.andrewyunt.warfare.utilities.Utils;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class PlayerListener implements Listener {
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {

		event.setJoinMessage(null);

		Player player = event.getPlayer();

		if (!Warfare.getInstance().getConfig().getBoolean("is-lobby")) {
			// Update server status
			Warfare.getInstance().getMySQLManager().updateServerStatus();
		} else {
			// Send welcome message
			player.sendMessage(ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString()
					+ "-----------------------------------------------------");
			player.sendMessage(ChatColor.YELLOW + "Welcome to " + ChatColor.GOLD
					+ ChatColor.BOLD.toString() + "Warfare");
			player.sendMessage(ChatColor.GOLD + " * " + ChatColor.YELLOW + "Teamspeak: "
					+ ChatColor.GRAY + "ts.faithfulmc.com");
			player.sendMessage(ChatColor.GOLD + " * " + ChatColor.YELLOW + "Website: "
					+ ChatColor.GRAY + "www.faithfulmc.com");
			player.sendMessage(ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString()
					+ "-----------------------------------------------------");
		}

		GamePlayer gp = null;
		
		// Create the player's GamePlayer object
		try {
			gp = Warfare.getInstance().getPlayerManager().createPlayer(player.getUniqueId());
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		final GamePlayer finalGP = gp;
		
		BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(Warfare.getInstance(), () -> {
			if (Warfare.getInstance().getConfig().getBoolean("is-lobby")) {
				finalGP.updateHotbar();
				
				player.teleport(player.getLocation().getWorld().getSpawnLocation());
			} else {
				player.setMaximumNoDamageTicks(0); // Part of the EPC
				
				Game game = Warfare.getInstance().getGame();
				
				if (Warfare.getInstance().getArena().isEdit()) {
					player.kickPlayer(ChatColor.RED + "The map is currently in edit mode.");
					return;
				}
				
				if (game.getStage() == Game.Stage.WAITING) {
					game.addPlayer(finalGP);
					return;
				} else if (game.getStage() == Game.Stage.END) {
					player.kickPlayer("You may not join once the game has ended.");
					return;
				} else if (game.getStage() == Stage.RESTART) {
					player.kickPlayer("You may not join during a restart.");
					return;
				} else {
					if (!player.hasPermission("warfare.spectatorjoin"))
						player.kickPlayer(ChatColor.RED + "You do not have permission join to spectate games.");
					else
						finalGP.setSpectating(true, false);
				}
			}
		}, 2L);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {

		event.setQuitMessage(null);

		Player player = event.getPlayer();
		GamePlayer gp = null;
		
		try {
			gp = Warfare.getInstance().getPlayerManager().getPlayer(player.getName());
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		if (!Warfare.getInstance().getConfig().getBoolean("is-lobby")) {
			Warfare.getInstance().getGame().removePlayer(gp);
			Warfare.getInstance().getMySQLManager().updateServerStatus();
		}
		
		try {
			Warfare.getInstance().getPlayerManager().deletePlayer(gp);
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
		
		handleHotbarClick(event.getPlayer(), item.getItemMeta().getDisplayName());
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		
		ItemStack item = event.getCurrentItem();
		
		if (item == null || !item.hasItemMeta())
			return;

		if (handleHotbarClick((Player) event.getWhoClicked(), item.getItemMeta().getDisplayName()))
			event.setCancelled(true);
	}
	
	private boolean handleHotbarClick(Player player, String itemName) {
		
		GamePlayer gp = null;
		
		try {
			gp = Warfare.getInstance().getPlayerManager().getPlayer(player.getName());
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		if (Warfare.getInstance().getConfig().getBoolean("is-lobby")) {
			if (itemName.equals(Utils.getFormattedMessage("hotbar-items.lobby-items.shop.title"))) {
				Warfare.getInstance().getShopMenu().open(ShopMenu.Type.MAIN, gp);
				return true;
			} else if (itemName.equals(Utils.getFormattedMessage("hotbar-items.lobby-items.class-selector.title"))) {
				Warfare.getInstance().getClassSelectorMenu().open(ClassSelectorMenu.Type.KIT, gp);
				return true;
			} else if (itemName.equals(Utils.getFormattedMessage("hotbar-items.lobby-items.play.title"))) {
				Warfare.getInstance().getPlayMenu().open(gp);
				return true;
			}
		} else if (gp.isCaged()) {
			if (itemName.equals(Utils.getFormattedMessage("hotbar-items.cage-items.class-selector.title"))) {
				Warfare.getInstance().getClassSelectorMenu().open(ClassSelectorMenu.Type.KIT, gp);
				return true;
			}
		} else if (gp.isSpectating()) {
			if (itemName.equals(Utils.getFormattedMessage("hotbar-items.spectator-items.return-to-lobby.title"))) {
				Utils.sendPlayerToServer(player, Warfare.getInstance().getConfig().getString("lobby-server"));
				return true;
			} else if (itemName.equals(Utils.getFormattedMessage("hotbar-items.spectator-items.teleporter.title"))) {
				Warfare.getInstance().getTeleporterMenu().open(gp);
				return true;
			}
		}
		
		return false;
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void onPlayerDamage(EntityDamageByEntityEvent event) {
		
		if (Warfare.getInstance().getConfig().getBoolean("is-lobby")) {
			event.setCancelled(true);
		} else {
			Entity damager = event.getDamager();
			Entity damaged = event.getEntity();
			
			if (!(damager instanceof Player) || !(damaged instanceof Player))
				return;
			
			GamePlayer damagedGP = null;
			GamePlayer damagerGP = null;
			
			try {
				damagedGP = Warfare.getInstance().getPlayerManager().getPlayer(((Player) damaged).getName());
				damagerGP = Warfare.getInstance().getPlayerManager().getPlayer(((Player) damager).getName());
			} catch (PlayerException e) {
				e.printStackTrace();
			}
			
			if (!damagerGP.isInGame() || !damagedGP.isInGame())
				return;
			
			damagedGP.setLastDamager(damagerGP);

			if (damagerGP.getSelectedKit() == Kit.SOUP) {
				damagedGP.getBukkitPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 1));
				damagerGP.getBukkitPlayer().sendMessage(ChatColor.YELLOW + String.format("You inflicted slowness II on %s for 10 seconds",
						damagedGP.getBukkitPlayer().getDisplayName()));
			}
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		
		if (Warfare.getInstance().getConfig().getBoolean("is-lobby"))
			return;
		
		event.setDroppedExp(0);
		event.setDeathMessage(null);
		
		Player player = event.getEntity();
		GamePlayer playerGP = null;

		try {
			playerGP = Warfare.getInstance().getPlayerManager().getPlayer(player.getName());
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		if (!playerGP.isInGame())
			return;
		
		Warfare.getInstance().getGame().removePlayer(playerGP);
		
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
		
		if (Warfare.getInstance().getConfig().getBoolean("is-lobby"))
			return;
		
		Game game = Warfare.getInstance().getGame();
		
		if (game == null || game.getStage() == Stage.WAITING || game.getStage() == Stage.COUNTDOWN)
			return;
		
		GamePlayer gp = null;
		UUID uuid = event.getPlayer().getUniqueId();
		
		
		try {
			gp = Warfare.getInstance().getPlayerManager().getPlayer(uuid);
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		event.setRespawnLocation(gp.setSpectating(true, true));
	}
	
	@EventHandler
	public void onPrepareItemEnchant(PrepareItemEnchantEvent event) {
		
		for (HumanEntity he : event.getViewers())
			((Player) he).setLevel(100);
	}
	
	@EventHandler
	public void onEnchantItem(EnchantItemEvent event) {
		
		Map<Enchantment, Integer> enchants = event.getEnchantsToAdd();
		
		enchants.clear();
		
		Material type = event.getItem().getType();
		
		if (type == Material.IRON_HELMET || type == Material.IRON_CHESTPLATE || type == Material.IRON_LEGGINGS
				|| type == Material.IRON_BOOTS || type == Material.DIAMOND_HELMET || type == Material.DIAMOND_CHESTPLATE
				|| type == Material.DIAMOND_LEGGINGS || type == Material.DIAMOND_BOOTS)
			enchants.put(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
		else if (type == Material.WOOD_SWORD || type == Material.STONE_SWORD || type == Material.IRON_SWORD
				|| type == Material.DIAMOND_SWORD)
			enchants.put(Enchantment.DAMAGE_ALL, 1);
		else if (type == Material.BOW)
			enchants.put(Enchantment.ARROW_DAMAGE, 1);
		
		event.setExpLevelCost(0);
	}
	
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		
		if (Warfare.getInstance().getConfig().getBoolean("is-lobby"))
			return;
		
		GamePlayer player = null;
		
		try {
			player = Warfare.getInstance().getPlayerManager().getPlayer(event.getEntity().getName());
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		Stage stage = Warfare.getInstance().getGame().getStage();
		
		if (stage == Stage.WAITING || stage == Stage.COUNTDOWN)
			event.setCancelled(true);
		else if (player.isSpectating())
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		
		if (event.getEntityType() != EntityType.ARROW)
			return;
		
		Projectile arrow = event.getEntity();
		ProjectileSource ps = arrow.getShooter();
		
		if (!(ps instanceof Player))
			return;
		
		ItemStack itemInHand = ((Player) ps).getItemInHand();
		
		if (itemInHand == null || !itemInHand.hasItemMeta())
			return;
		
		ItemMeta itemInHandMeta = itemInHand.getItemMeta();
		
		if (!itemInHandMeta.hasDisplayName())
			return;
		
		if (itemInHandMeta.getDisplayName().equals("Booster Bow"))
			arrow.remove();
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onPlayerFall(EntityDamageEvent event) {
		
		if (event.getCause() != DamageCause.FALL)
			return;
		
		if (!(event.getEntity() instanceof Player))
			return;
		
		GamePlayer gp = null;
		
		try {
			gp = Warfare.getInstance().getPlayerManager().getPlayer(((Player) event.getEntity()).getName());
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
		
		if (Warfare.getInstance().getConfig().getBoolean("is-lobby"))
			event.setCancelled(true);
		else
			cancelCageInteractions(event, event.getPlayer());
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		
		if (Warfare.getInstance().getConfig().getBoolean("is-lobby"))
			event.setCancelled(true);
		else
			cancelCageInteractions(event, event.getPlayer());
	}
	
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		
		if (event.getInventory().getType() == InventoryType.PLAYER)
			return;
		
		if (!event.getInventory().getTitle().contains("Class Selector"))
			cancelCageInteractions(event, (Player) event.getPlayer());
	}
	
	private void cancelCageInteractions(Cancellable cancellable, Player player) {
		
		if (Warfare.getInstance().getConfig().getBoolean("is-lobby"))
			return;
		
		GamePlayer gp = null;
		
		try {
			gp = Warfare.getInstance().getPlayerManager().getPlayer(player);
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		if (gp.isCaged())
			cancellable.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		
		if (!Warfare.getInstance().getConfig().getBoolean("is-lobby"))
			return;
		
		Player player = event.getPlayer();
		
		if (player.getLocation().getY() < 0)
			player.teleport(player.getLocation().getWorld().getSpawnLocation());
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		
		GamePlayer gp = null;
		
		try {
			gp = Warfare.getInstance().getPlayerManager().getPlayer(event.getPlayer());
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		if (Warfare.getInstance().getConfig().getBoolean("is-lobby"))
			event.setCancelled(true);
		else if (gp.isCaged())
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		
		for (Player player : event.getRecipients()) {
			Conversable conversable = player;
			
			if (conversable.isConversing())
				event.getRecipients().remove(player);
		}
	}
	
	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		
		if (event.getLine(0) == null || event.getLine(1) == null || event.getLine(2) == null)
			return;
		
		if (!event.getLine(0).equalsIgnoreCase("[Leaderboard]"))
			return;
		
		Player player = event.getPlayer();
		
		if (!player.hasPermission("Warfare.sign.create")) {
			player.sendMessage(ChatColor.RED + "You do not have permission to create a leaderboard sign.");
			return;
		}
		
		SignDisplay.Type type = null;
		
		if (event.getLine(1).equalsIgnoreCase("kills"))
			type = Type.KILLS_LEADERBOARD;
		else if (event.getLine(1).equalsIgnoreCase("wins"))
			type = Type.WINS_LEADERBOARD;
		else
			return;
		
		int place = 0;
		
		try {
			place = Integer.valueOf(event.getLine(2));
		} catch (NumberFormatException e) {
			player.sendMessage(ChatColor.RED + "You did not enter an integer for the sign place.");
			return;
		}
		
		if (place > 5) {
			player.sendMessage(ChatColor.RED + "You may not enter a place over 5.");
			return;
		}
		
		try {
			Warfare.getInstance().getSignManager().createSign(
					event.getBlock().getLocation(),
					type,
					place);
		} catch (SignException e) {
			e.printStackTrace();
			player.sendMessage(ChatColor.RED + e.getMessage());
		}
	}

	// Event handlers for power ups
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onGrapple(PlayerFishEvent event) {

		if (Warfare.getInstance().getConfig().getBoolean("is-lobby"))
			return;

		Player player = event.getPlayer();
		GamePlayer gp = null;

		try {
			gp = Warfare.getInstance().getPlayerManager().getPlayer(player);
		} catch (PlayerException e) {
			e.printStackTrace();
		}

		gp.setPowerupCooldown(true);

		GamePlayer finalGP = gp;

		new BukkitRunnable() {
			@Override
			public void run() {

				finalGP.setPowerupCooldown(false);
			}
		}.runTaskLater(Warfare.getInstance(), 1200L);

		if(!player.getItemInHand().getItemMeta().getDisplayName().equals(ChatColor.GOLD + ChatColor.BOLD.toString() + "Grappling Hook"))
			return;

		if(event.getState() == PlayerFishEvent.State.IN_GROUND  || event.getState() == PlayerFishEvent.State.FAILED_ATTEMPT)
			player.teleport(event.getHook().getLocation());
	}

	@EventHandler
	public void onPowerup(PlayerInteractEvent event) {

		if (Warfare.getInstance().getConfig().getBoolean("is-lobby"))
			return;

		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		ItemStack item = event.getItem();

		if (item == null || !item.hasItemMeta())
			return;

		if (item.getType() != Material.INK_SACK)
			return;

		if (!item.getItemMeta().getDisplayName().equalsIgnoreCase("Powerup"))
			return;

		Player player = event.getPlayer();
		GamePlayer gp = null;

		try {
			gp = Warfare.getInstance().getPlayerManager().getPlayer(player);
		} catch (PlayerException e) {
			e.printStackTrace();
		}

		if (gp.getSelectedKit() == Kit.POT) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 2, 200));
			player.sendMessage(ChatColor.YELLOW + "You have been given regen III for 10 seconds.");
		}

		player.setItemInHand(new ItemStack(Material.INK_SACK, 1, (short) 10));
		gp.setPowerupActivated(true);

		GamePlayer finalGP = gp;

		new BukkitRunnable() {
			@Override
			public void run() {

				player.setItemInHand(new ItemStack(Material.INK_SACK, 1, (short) 8));
				finalGP.setPowerupActivated(false);
			}
		}.runTaskLater(Warfare.getInstance(), 200L);
	}
}