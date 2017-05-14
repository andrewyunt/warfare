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

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.exception.PlayerException;
import com.andrewyunt.warfare.objects.GamePlayer;

/**
 * The listener class used for abilities which holds methods to listen on events.
 * 
 * @author Andrew Yunt
 */
public class PlayerUltimateListener implements Listener {
	
	@EventHandler (priority = EventPriority.MONITOR)
	private void onPlayerInteract(PlayerInteractEvent event) {
		
		ItemStack item = event.getItem();
		
		if (item == null)
			return;
		
		Material type = item.getType();
		Action action = event.getAction();
		Player player = event.getPlayer();
		GamePlayer gp = null;
		
		try {
			gp = Warfare.getInstance().getPlayerManager().getPlayer(player.getName());
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
			
			if (!gp.isInGame())
				return;
			
			if (!type.toString().toLowerCase().contains("sword"))
				return;
			
			gp.getSelectedUltimate().use(gp);
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	private void EPC(EntityDamageByEntityEvent event) {
		
		if (event.getCause() == DamageCause.FALL)
			return;
		
		if (!(event.getEntity() instanceof Player))
			return;
		
		Player damaged = (Player) event.getEntity();
		GamePlayer damagedGP = null;
		
		try {
			damagedGP = Warfare.getInstance().getPlayerManager().getPlayer(damaged.getName());
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		final GamePlayer finalDamagedGP = damagedGP;
		
		if (damagedGP.isEPCCooldown())
			event.setCancelled(true);
		else {
			damagedGP.setEPCCooldown(true);
			
			new BukkitRunnable() {
				@Override
				public void run() {
					
					finalDamagedGP.setEPCCooldown(false);
				}
			}.runTaskLater(Warfare.getInstance(), 10L);
		}
		
		// Give energy
		if (event.getDamager() instanceof Player) {
			Player damager = (Player) event.getDamager();
			GamePlayer gpDamager = null;
			
			try{
				gpDamager = Warfare.getInstance().getPlayerManager().getPlayer(damager.getName());
			} catch(PlayerException e) {
				return;
			}
			
			gpDamager.setEnergy(gpDamager.getEnergy() + gpDamager.getSelectedUltimate().getEnergyPerClick());
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	private void EPC(EntityDamageEvent event) {
		
		if (event.getCause() == DamageCause.FALL)
			return;
		
		if (event instanceof EntityDamageByEntityEvent)
			return;
		
		if (!(event.getEntity() instanceof Player))
			return;
		
		Player damaged = (Player) event.getEntity();
		GamePlayer damagedGP = null;
		
		try {
			damagedGP = Warfare.getInstance().getPlayerManager().getPlayer(damaged.getName());
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		final GamePlayer finalDamagedGP = damagedGP;
		
		if (damagedGP.isEPCCooldown())
			event.setCancelled(true);
		else {
			damagedGP.setEPCCooldown(true);
			
			new BukkitRunnable() {
				@Override
				public void run() {
					
					finalDamagedGP.setEPCCooldown(false);
				}
			}.runTaskLater(Warfare.getInstance(), 10L);
		}
	}
	
	@EventHandler
	private void onEntityDamage(EntityDamageByEntityEvent event) {
		
		if (event.getCause() == DamageCause.ENTITY_EXPLOSION && (event.getDamager().getType() != EntityType.PRIMED_TNT))
				event.setCancelled(true);
	}
	
	@EventHandler
	private void onProjectileHit(ProjectileHitEvent event) {
		
		Entity entity = event.getEntity();
		
		if (!(entity instanceof Arrow))
			return;
		
		if (!entity.hasMetadata("MegaTW"))
			return;
		
		Player shooter = (Player) ((Projectile) entity).getShooter();
		
		Location loc = entity.getLocation();
		
		loc.getWorld().createExplosion(loc, 5);
		
		for (Entity nearby : entity.getNearbyEntities(5D, 3D, 5D)) {
			if (!(nearby instanceof Player))
				continue;
			
			if (nearby == shooter)
				continue;
			
			Player nearbyPlayer = (Player) nearby;
			GamePlayer nearbyGP = null;
			
			try {
				nearbyGP = Warfare.getInstance().getPlayerManager().getPlayer(nearbyPlayer.getName());
			} catch (PlayerException e) {
				e.printStackTrace();
			}
			
			if (!nearbyGP.isInGame())
				return;
			
			Damageable dmgPlayer = nearbyPlayer;
			
			if (dmgPlayer.getHealth() < 5) {
				dmgPlayer.setHealth(0D);
				return;
			} else
				nearbyPlayer.setHealth(nearbyPlayer.getHealth() - 5);
		}
	}
	
	@EventHandler
	private void onFlamingFeetMove(PlayerMoveEvent event) {
		
		Player player = event.getPlayer();
		GamePlayer gp = null;
		
		try {
			gp = Warfare.getInstance().getPlayerManager().getPlayer(event.getPlayer());
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		if (!gp.hasFlamingFeet())
			return;
		
		Block block = player.getLocation().getBlock();
		
		if (block.getType() == Material.AIR)
			block.setType(Material.FIRE);
	}
}