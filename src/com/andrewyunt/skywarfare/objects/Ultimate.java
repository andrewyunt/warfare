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
package com.andrewyunt.skywarfare.objects;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;

import com.andrewyunt.skywarfare.SkyWarfare;
import com.andrewyunt.skywarfare.exception.PlayerException;

/**
 * The enumeration for abilities, their names, and the method to use them.
 * 
 * @author Andrew Yunt
 */
public enum Ultimate implements Purchasable {

	HEAL("Heal", 0, 4),
	WRATH("Wrath", 20000, 6),
	HELL_SPAWNING("Hell's Spawning", 40000, 2),
	LEAP("Leap", 50000, 4),
	SONIC("Sonic", 25000, 3),
	WITHERING("Withering", 20000, 5),
	FLAMING_FEET("Flaming Feet", 50000, 4);
	
	private final String name;
	private final int price;
	private final int energyPerClick;

	Ultimate(String name, int price, int energyPerClick) {

		this.name = name;
		this.price = price;
		this.energyPerClick = energyPerClick;
	}

	@Override
	public String getName() {

		return name;
	}
	
	@Override
	public int getPrice() {
		
		return price;
	}

	public int getEnergyPerClick() {
		
		return energyPerClick;
	}
	
	@Override
	public ItemStack getDisplayItem() {
		
		if (this == HEAL) {
			ItemStack healingPotion = new ItemStack(Material.POTION, 1);
			PotionMeta healingPotionMeta = (PotionMeta) healingPotion.getItemMeta();
			PotionEffect healingEffect = new PotionEffect(PotionEffectType.HEAL, 1, 2, false);
			List<String> lore = new ArrayList<String>();
			lore.add(ChatColor.RESET + "HEAL 2" + ChatColor.RED + "\u2764");
			healingPotionMeta.setLore(lore);
			healingPotionMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.DARK_RED + "Health Potion");
			healingPotionMeta.setMainEffect(PotionEffectType.HEAL);
			healingPotionMeta.addCustomEffect(healingEffect, true);
			healingPotion.setItemMeta(healingPotionMeta);
			return healingPotion;
		} else if (this == WRATH) {
			return new ItemStack(Material.DIAMOND_SWORD, 1);
		} else if (this == HELL_SPAWNING) {
			return new ItemStack(Material.FIREBALL, 1);
		} else if (this == LEAP) {
			return new ItemStack(Material.FISHING_ROD, 1);
		} else if (this == SONIC) {
			return new ItemStack(Material.FEATHER, 1);
		} else if (this == WITHERING) {
			return new ItemStack(Material.SKULL, 1, (short) 1);
		} else if (this == FLAMING_FEET) {
			ItemStack ironBoots = new ItemStack(Material.IRON_BOOTS, 1);
			ironBoots.addEnchantment(Enchantment.PROTECTION_FIRE, 1);
			return ironBoots;
		}
		
		return null;
	}
	
	public void use(GamePlayer player) {
		
		Player bp = player.getBukkitPlayer();
		
		player.setEnergy(0);
		
		if (this == HEAL) {
			
			bp.setHealth(((Damageable) bp).getHealth() + 6);
		
		} else if (this == WRATH) {
			
			int count = 0;

			for (Entity entity : player.getBukkitPlayer().getNearbyEntities(3, 3, 3)) {
				if (!(entity instanceof Player))
					continue;

				Player entityPlayer = (Player) entity;
				GamePlayer entityAP = null;

				try {
					entityAP = SkyWarfare.getInstance().getPlayerManager().getPlayer(entityPlayer.getName());
				} catch (PlayerException e) {
					e.printStackTrace();
				}

				if (!entityAP.isInGame())
					continue;
				
				entityPlayer.getWorld().strikeLightningEffect(entityPlayer.getLocation());
				Damageable dmgVictim = (Damageable) entityPlayer;
				dmgVictim.damage(0.00001D); // Just so an actual hit will register
				
				if (dmgVictim.getHealth() <= 5)
					dmgVictim.setHealth(0D);
				else
					dmgVictim.setHealth(dmgVictim.getHealth() - 5);
				
				count++;
			}
			
			if (count == 0) {
				player.getBukkitPlayer().sendMessage(ChatColor.RED + "No targets within range found!");
				return;
			}
			
		} else if (this == HELL_SPAWNING) {
			
			Location loc = bp.getLocation();
			
			player.getGhasts().add(loc.getWorld().spawnEntity(loc, EntityType.GHAST).getUniqueId());
			
		} else if (this == LEAP) {
			
			bp.setVelocity(bp.getEyeLocation().getDirection().multiply(5.0));
			
		} else if (this == SONIC) {
			
			bp.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 2));
			
		} else if (this == WITHERING) {
			
			WitherSkull skull = bp.launchProjectile(WitherSkull.class, bp.getEyeLocation().getDirection());
			skull.setMetadata("SkyWarfare", new FixedMetadataValue(SkyWarfare.getInstance(), true));
			
		} else if (this == FLAMING_FEET) {
			
			player.setFlamingFeet(true);
			
			BukkitScheduler scheduler = SkyWarfare.getInstance().getServer().getScheduler();
			scheduler.scheduleSyncDelayedTask(SkyWarfare.getInstance(), new Runnable() {
				@Override
				public void run() {
					
					player.setFlamingFeet(false);
				}
			}, 100);
		}
		
		bp.sendMessage(ChatColor.GOLD + String.format("You have used the %s ability.", getName()));
	}
}