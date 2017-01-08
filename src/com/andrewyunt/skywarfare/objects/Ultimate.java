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
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;
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
	HELLS_SPAWNING("Hell's Spawning", 40000, 2),
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
		} else if (this == HELLS_SPAWNING) {
			return new ItemStack(Material.FIREBALL, 1);
		} else if (this == LEAP) {
			return new ItemStack(Material.FISHING_ROD, 1);
		} else if (this == SONIC) {
			return new ItemStack(Material.FEATHER, 1);
		} else if (this == WITHERING) {
			return new ItemStack(Material.SKULL_ITEM, 1, (short) 1);
		} else if (this == FLAMING_FEET) {
			ItemStack ironBoots = new ItemStack(Material.IRON_BOOTS, 1);
			ironBoots.addEnchantment(Enchantment.PROTECTION_FIRE, 1);
			return ironBoots;
		}
		
		return null;
	}
	
	public void use(GamePlayer player) {
		
		if (player.getEnergy() < 100)
			return;
		
		Player bp = player.getBukkitPlayer();
		
		if (this == HEAL) {
			
			double newHealth = ((Damageable) bp).getHealth() + 6;
			
			if (newHealth < 40)
				((Damageable) bp).setHealth(newHealth);
			else
				((Damageable) bp).setHealth(40D);
			
			Location loc = bp.getEyeLocation().clone();
			loc.getWorld().spigot().playEffect(loc.add(0.0D, 0.8D, 0.0D), Effect.HEART);
		
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
				
				if (dmgVictim.getHealth() <= 5)
					dmgVictim.setHealth(0D);
				else
					dmgVictim.setHealth(dmgVictim.getHealth() - 5);
				
				count++;
			}
			
			if (count == 0)
				player.getBukkitPlayer().sendMessage(ChatColor.RED + "No targets within range found!");
			
		} else if (this == HELLS_SPAWNING) {
			
			Ghast ghast = (Ghast) bp.getLocation().getWorld().spawnEntity(bp.getLocation().add(
					new Location(bp.getLocation().getWorld(), 0, 10, 0)), EntityType.GHAST);
			
			player.getGhasts().add(ghast.getUniqueId());
			
			BukkitScheduler scheduler = SkyWarfare.getInstance().getServer().getScheduler();
			scheduler.scheduleSyncRepeatingTask(SkyWarfare.getInstance(), new Runnable() {
				@Override
				public void run() {
					
					if (!ghast.isDead())
						ghast.teleport(bp.getLocation().add(new Location(bp.getLocation().getWorld(), 0, 10, 0)));
				}
			}, 0L, 200L);
		
		} else if (this == LEAP) {
			
			return;
		
		} else if (this == SONIC) {
			
			bp.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 2));
			
		} else if (this == WITHERING) {
			
			WitherSkull skull = bp.launchProjectile(WitherSkull.class, bp.getEyeLocation().getDirection());
			skull.setMetadata("SkyWarfare", new FixedMetadataValue(SkyWarfare.getInstance(), true));
			
		} else if (this == FLAMING_FEET) {
			
			bp.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));
			bp.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 200, 0));
			
			player.setFlamingFeet(true);
			
			BukkitScheduler scheduler = SkyWarfare.getInstance().getServer().getScheduler();
			scheduler.scheduleSyncDelayedTask(SkyWarfare.getInstance(), new Runnable() {
				@Override
				public void run() {
					
					player.setFlamingFeet(false);
				}
			}, 100);
		}
		
		player.setEnergy(0);
		
		bp.sendMessage(ChatColor.GOLD + String.format("You have used the %s ultimate.", getName()));
	}
}