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

import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;

import com.andrewyunt.skywarfare.SkyWarfare;
import com.andrewyunt.skywarfare.exception.PlayerException;
import com.andrewyunt.skywarfare.menu.ClassCreatorMenu;
import com.andrewyunt.skywarfare.menu.ShopMenu;
import com.andrewyunt.skywarfare.objects.CustomClass;
import com.andrewyunt.skywarfare.objects.Game;
import com.andrewyunt.skywarfare.objects.Game.Stage;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.andrewyunt.skywarfare.objects.GamePlayer;
import com.andrewyunt.skywarfare.objects.Kit;
import com.andrewyunt.skywarfare.objects.Purchasable;
import com.andrewyunt.skywarfare.objects.Skill;
import com.andrewyunt.skywarfare.objects.Ultimate;

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
			Set<Purchasable> purchases = finalGP.getPurchases();
			
			if (!purchases.contains(Kit.ARMORER))
				purchases.add(Kit.ARMORER);
			
			if (!purchases.contains(Ultimate.HEAL))
				purchases.add(Ultimate.HEAL);
			
			if (!purchases.contains(Skill.HEAD_START))
				purchases.add(Skill.HEAD_START);
			
			if (!purchases.contains(Skill.GUARD))
				purchases.add(Skill.GUARD);
			
			CustomClass defaultClass = new CustomClass();
			
			defaultClass.setKit(Kit.ARMORER);
			defaultClass.setUltimate(Ultimate.HEAL);
			defaultClass.setSkillOne(Skill.HEAD_START);
			defaultClass.setSkillTwo(Skill.GUARD);
			defaultClass.setName("Default");
			
			List<CustomClass> customClasses = finalGP.getCustomClasses();
			
			if (customClasses.size() == 0)
				customClasses.add(defaultClass);
			
			if (finalGP.getCustomClass() == null)
				finalGP.setCustomClass(defaultClass);
			
			if (SkyWarfare.getInstance().getConfig().getBoolean("is-lobby")) {
				finalGP.updateHotbar();
			} else {
				Player bp = event.getPlayer();
				
				bp.setMaximumNoDamageTicks(0); // Part of the EPC
				
				Game game = SkyWarfare.getInstance().getGame();
				
				if (SkyWarfare.getInstance().getArena().isEdit()) {
					bp.kickPlayer(ChatColor.RED + "The map is currently in edit mode.");
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
				
				if (game.getStage() != Stage.WAITING && !bp.hasPermission("skywarfare.spectatorjoin"))
					bp.kickPlayer(ChatColor.RED + "You do not have permission join to spectate games.");
				else
					finalGP.setSpectating(true);
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
		
		if (!SkyWarfare.getInstance().getConfig().getBoolean("is-lobby"))
			return;
		
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
		
		if (type == Material.EMERALD)
			SkyWarfare.getInstance().getShopMenu().open(ShopMenu.Type.MAIN, gp);
		else if (type == Material.CHEST)
			SkyWarfare.getInstance().getClassCreatorMenu().open(ClassCreatorMenu.Type.MAIN, gp, null);
		else if (type == Material.COMMAND)
			SkyWarfare.getInstance().getClassSelectorMenu().open(gp);
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		
		if (SkyWarfare.getInstance().getConfig().getBoolean("is-lobby"))
			return;
		
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
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		
		if (SkyWarfare.getInstance().getConfig().getBoolean("is-lobby"))
			return;
		
		GamePlayer player = null;
		
		try {
			player = SkyWarfare.getInstance().getPlayerManager().getPlayer(event.getPlayer());
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		if (!player.isCaged())
			return;
		
		if (player.getCage().getBlocks().contains(event.getBlock()))
			event.setCancelled(true);
	}
}