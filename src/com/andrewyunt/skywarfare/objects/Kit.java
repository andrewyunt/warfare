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
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public enum Kit implements Purchasable{
	
	ARMORER("Armorer"),
	ARCHER("Archer"),
	SWORDSMAN("Swordsman"),
	BOOSTER("Booster"),
	CANNONER("Cannoner"),
	HEALER("Healer"),
	ENCHANTER("Enchanter"),
	FISHERMAN("Fisherman"),
	SCOUT("Scout"),
	PYROMANIAC("Pyromaniac");
	
	private String name;
	
	Kit(String name) {
		
		this.name = name;
	}
	
	@Override
	public String getName() {
		
		return name;
	}
	
	@Override
	public ItemStack getDisplayItem() {
		
		if (this == ARMORER) {
			return new ItemStack(Material.IRON_CHESTPLATE, 1);
		} else if (this == ARCHER) {
			ItemStack bow = new ItemStack(Material.BOW, 1);
			bow.addEnchantment(Enchantment.ARROW_KNOCKBACK, 1);
			return bow;
		} else if (this == SWORDSMAN) {
			ItemStack sword = new ItemStack(Material.STONE_SWORD, 1);
			sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
			return sword;
		} else if (this == BOOSTER) {
			ItemStack bow = new ItemStack(Material.BOW);
			bow.addEnchantment(Enchantment.ARROW_KNOCKBACK, 2);
			bow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
			return bow;
		} else if (this == CANNONER) {
			ItemStack boots = new ItemStack(Material.IRON_BOOTS);
			boots.addEnchantment(Enchantment.PROTECTION_FALL, 3);
			return boots;
		} else if (this == HEALER) {
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
		} else if (this == ENCHANTER) {
			return new ItemStack(Material.ENCHANTMENT_TABLE, 1);
		} else if (this == FISHERMAN) {
			return new ItemStack(Material.FISHING_ROD, 1);
		} else if (this == SCOUT) {
			ItemStack speedPotion = new ItemStack(Material.POTION, 3);
			PotionMeta speedPotionMeta = (PotionMeta) speedPotion.getItemMeta();
			PotionEffect speedEffect = new PotionEffect(PotionEffectType.SPEED, 1, 2, false);
			List<String> lore = new ArrayList<String>();
			lore.add(ChatColor.RESET + "SPEED 2" + ChatColor.RED + "\u2764");
			speedPotionMeta.setLore(lore);
			speedPotionMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.DARK_RED + "Speed Potion");
			speedPotionMeta.setMainEffect(PotionEffectType.SPEED);
			speedPotionMeta.addCustomEffect(speedEffect, true);
			speedPotion.setItemMeta(speedPotionMeta);
			return speedPotion;
		} else if (this == PYROMANIAC) {
			return new ItemStack(Material.LAVA_BUCKET, 1);
		}
		
		return null;
	}
	
	public void giveItems(GamePlayer player) {
		
		PlayerInventory inv = player.getBukkitPlayer().getInventory();
		
		if (this == ARMORER) {
			inv.setHelmet(new ItemStack(Material.GOLD_HELMET));
			inv.setChestplate(new ItemStack(Material.IRON_CHESTPLATE, 1));
			inv.setLeggings(new ItemStack(Material.GOLD_LEGGINGS, 1));
		} else if (this == ARCHER) {
			inv.addItem(getDisplayItem());
			inv.addItem(new ItemStack(Material.ARROW, 32));
		} else if (this == SWORDSMAN) {
			inv.addItem(getDisplayItem());
			inv.setHelmet(new ItemStack(Material.IRON_HELMET, 1));
		} else if (this == BOOSTER) {
			inv.addItem(getDisplayItem());
			inv.addItem(new ItemStack(Material.ARROW, 1));
		} else if (this == CANNONER) {
			inv.addItem(getDisplayItem());
			inv.addItem(new ItemStack(Material.TNT, 16));
			inv.addItem(new ItemStack(Material.WOOD_PLATE, 1));
			inv.addItem(new ItemStack(Material.BUCKET, 1));
		} else if (this == HEALER) {
			inv.addItem(getDisplayItem());
			
			ItemStack regenPotion = new ItemStack(Material.POTION, 1);
			PotionMeta regenPotionMeta = (PotionMeta) regenPotion.getItemMeta();
			PotionEffect regenEffect = new PotionEffect(PotionEffectType.REGENERATION, 660, 2, false);
			List<String> lore = new ArrayList<String>();
			lore.add(ChatColor.RESET + "REGENERATION 2" + ChatColor.RED + "\u2764");
			regenPotionMeta.setLore(lore);
			regenPotionMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.DARK_RED + "Regeneration Potion");
			regenPotionMeta.setMainEffect(PotionEffectType.REGENERATION);
			regenPotionMeta.addCustomEffect(regenEffect, true);
			regenPotion.setItemMeta(regenPotionMeta);
			inv.addItem(regenPotion);
		} else if (this == ENCHANTER) {
			inv.addItem(new ItemStack(Material.DIAMOND_SPADE, 1));
			inv.addItem(new ItemStack(Material.ENCHANTMENT_TABLE, 1));
			inv.addItem(new ItemStack(Material.EXP_BOTTLE, 32));
		} else if (this == FISHERMAN) {
			inv.setHelmet(new ItemStack(Material.LEATHER_HELMET, 1));
			inv.addItem(new ItemStack(Material.FISHING_ROD, 1));
			inv.addItem(new ItemStack(Material.COOKED_FISH, 16));
		} else if (this == SCOUT) {
			inv.addItem(getDisplayItem());
			inv.addItem(new ItemStack(Material.STONE_SWORD, 1));
		} else if (this == PYROMANIAC) {
			ItemStack sword = new ItemStack(Material.WOOD_SWORD, 1);
			sword.addEnchantment(Enchantment.FIRE_ASPECT, 1);
			inv.addItem(sword);
			inv.addItem(new ItemStack(Material.LAVA_BUCKET, 5));
			inv.addItem(new ItemStack(Material.FLINT_AND_STEEL, 1));
			
			ItemStack frPotion = new ItemStack(Material.POTION, 3);
			PotionMeta frMeta = (PotionMeta) frPotion.getItemMeta();
			PotionEffect frEffect = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 2, false);
			List<String> lore = new ArrayList<String>();
			lore.add(ChatColor.RESET + "Fire Resistance 2" + ChatColor.RED + "\u2764");
			frMeta.setLore(lore);
			frMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.DARK_RED + "Fire Resistance Potion");
			frMeta.setMainEffect(PotionEffectType.FIRE_RESISTANCE);
			frMeta.addCustomEffect(frEffect, true);
			frPotion.setItemMeta(frMeta);
			inv.addItem(frPotion);
		}
	}
}