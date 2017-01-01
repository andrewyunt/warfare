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

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import com.andrewyunt.skywarfare.SkyWarfare;
import com.andrewyunt.skywarfare.exception.PlayerException;
import com.andrewyunt.skywarfare.objects.GamePlayer;
import com.andrewyunt.skywarfare.objects.Skill;

/**
 * The listener class used for skills which holds methods to listen on events.
 * 
 * @author Andrew Yunt
 */
public class PlayerSkillListener implements Listener {

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

		if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player))
			return;

		Player damaged = (Player) event.getEntity();
		GamePlayer damagedGP = null;

		try {
			damagedGP = SkyWarfare.getInstance().getPlayerManager().getPlayer(damaged);
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		if (damagedGP.getCustomClass().getSkillOne() != Skill.RESISTANCE
				&& damagedGP.getCustomClass().getSkillTwo() != Skill.RESISTANCE)
			return;

		if (Math.random() > 0.20D)
			return;
		
		damaged.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20, 1));
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {

		if (!(event.getEntity() instanceof Player))
			return;

		GamePlayer killedGP = null;

		try {
			killedGP = SkyWarfare.getInstance().getPlayerManager().getPlayer((Player) event.getEntity());
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		GamePlayer lastDamagerGP = killedGP.getLastDamager();
		
		if (lastDamagerGP == null)
			return;
		
		Player lastDamager = lastDamagerGP.getBukkitPlayer();
		
		if (lastDamagerGP.getCustomClass().getSkillOne() == Skill.JUGGERNAUT
				&& lastDamagerGP.getCustomClass().getSkillTwo() == Skill.JUGGERNAUT) {
			
			double health = ((Damageable) lastDamager).getHealth();
			double maxHealth = ((Damageable) lastDamager).getMaxHealth();
			
			if (health + 2 > maxHealth)
				lastDamager.setHealth(maxHealth);
			else
				lastDamager.setHealth(health + 2);
			
		} else if (lastDamagerGP.getCustomClass().getSkillOne() == Skill.CONSUMPTION
				&& lastDamagerGP.getCustomClass().getSkillTwo() == Skill.CONSUMPTION) {
			
			lastDamager.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, 2));
		}
	}
	
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		
		if (event.getInventory().getType() != InventoryType.CHEST)
			return;
		
		Player player = (Player) event.getPlayer();
		GamePlayer gp = null;
		
		try {
			gp = SkyWarfare.getInstance().getPlayerManager().getPlayer(player);
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		if (gp.getCustomClass().getSkillOne() == Skill.GUARD
				&& gp.getCustomClass().getSkillTwo() == Skill.GUARD)
			player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 2));
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		
		Player player = (Player) event.getPlayer();
		GamePlayer gp = null;
		
		try {
			gp = SkyWarfare.getInstance().getPlayerManager().getPlayer(player);
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		if (event.getInventory().getType() == InventoryType.ENCHANTING)
			gp.setEnergy(gp.getEnergy());
		else if (event.getInventory().getType() == InventoryType.CHEST)
			if (gp.getCustomClass().getSkillOne() == Skill.GUARD && gp.getCustomClass().getSkillTwo() == Skill.GUARD)
				player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
	}
	
	@EventHandler
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		
		Projectile projectile = event.getEntity();
		
		if (projectile.getType() != EntityType.ARROW)
			return;
		
		ProjectileSource ps = projectile.getShooter();
		
		if (!(ps instanceof Player))
			return;
		
		GamePlayer gp = null;
		
		try {
			gp = SkyWarfare.getInstance().getPlayerManager().getPlayer((Player) ps);
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		if (gp.getCustomClass().getSkillOne() == Skill.FLAME
				&& gp.getCustomClass().getSkillTwo() == Skill.FLAME)
			projectile.setFireTicks(Integer.MAX_VALUE);
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
}