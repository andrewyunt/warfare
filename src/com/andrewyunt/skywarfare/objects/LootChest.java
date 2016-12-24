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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LootChest {
	
	private Chest bukkitChest;
	private byte tier;
	
	private ItemStack[] tier3Items;
	private ItemStack[] tier2Items;
	private ItemStack[] tier1Items;
	
	public LootChest(Chest bukkitChest, byte tier) {
		
		this.bukkitChest = bukkitChest;
		this.tier = tier;
		
		tier3Items = new ItemStack[] {
				new ItemStack(Material.WOOD, 16),
				new ItemStack(Material.WOOD, 32),
				new ItemStack(Material.COBBLESTONE, 16),
				new ItemStack(Material.COBBLESTONE, 32),
				new ItemStack(Material.IRON_AXE, 1),
				new ItemStack(Material.STONE_SWORD, 1),
				new ItemStack(Material.IRON_SWORD, 1),
				new ItemStack(Material.IRON_HELMET, 1),
				new ItemStack(Material.IRON_CHESTPLATE, 1),
				new ItemStack(Material.IRON_LEGGINGS, 1),
				new ItemStack(Material.IRON_BOOTS, 1),
				new ItemStack(Material.COOKED_BEEF, 4),
				new ItemStack(Material.COOKED_BEEF, 16),
				new ItemStack(Material.BREAD, 4),
				new ItemStack(Material.BREAD, 16),
				new ItemStack(Material.FISHING_ROD, 1),
				new ItemStack(Material.WATER_BUCKET, 1),
				new ItemStack(Material.LAVA_BUCKET, 1),
				new ItemStack(Material.FLINT_AND_STEEL, 1),
				new ItemStack(Material.SNOW_BALL, 16),
				new ItemStack(Material.EXP_BOTTLE, 16),
				new ItemStack(Material.BOW, 1),
				new ItemStack(Material.ARROW, 16)
		};
		
		ItemStack ironSword = new ItemStack(Material.IRON_SWORD, 1);
		ironSword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
		
		ItemStack bow = new ItemStack(Material.BOW, 1);
		bow.addEnchantment(Enchantment.ARROW_DAMAGE, 1);
		
		ItemStack frPotion = new ItemStack(Material.POTION, 3);
		PotionMeta frMeta = (PotionMeta) frPotion.getItemMeta();
		PotionEffect frEffect = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 9600, 1, false);
		List<String> lore = new ArrayList<String>();
		lore.add(ChatColor.RESET + "Fire Resistance 2" + ChatColor.RED + "\u2764");
		frMeta.setLore(lore);
		frMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.DARK_RED + "Fire Resistance Potion");
		frMeta.setMainEffect(PotionEffectType.FIRE_RESISTANCE);
		frMeta.addCustomEffect(frEffect, true);
		frPotion.setItemMeta(frMeta);
		
		ItemStack speedPotion = new ItemStack(Material.POTION, 3);
		PotionMeta speedPotionMeta = (PotionMeta) speedPotion.getItemMeta();
		PotionEffect speedEffect = new PotionEffect(PotionEffectType.SPEED, 1, 2, false);
		lore = new ArrayList<String>();
		lore.add(ChatColor.RESET + "SPEED 2" + ChatColor.RED + "\u2764");
		speedPotionMeta.setLore(lore);
		speedPotionMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.DARK_RED + "Speed Potion");
		speedPotionMeta.setMainEffect(PotionEffectType.SPEED);
		speedPotionMeta.addCustomEffect(speedEffect, true);
		speedPotion.setItemMeta(speedPotionMeta);
		
		tier2Items = new ItemStack[] {
				new ItemStack(Material.WOOD, 64),
				new ItemStack(Material.COBBLESTONE, 64),
				ironSword,
				new ItemStack(Material.DIAMOND_SWORD, 1),
				new ItemStack(Material.FISHING_ROD, 1),
				new ItemStack(Material.IRON_HELMET, 1),
				new ItemStack(Material.IRON_CHESTPLATE, 1),
				new ItemStack(Material.IRON_LEGGINGS, 1),
				new ItemStack(Material.IRON_BOOTS, 1),
				new ItemStack(Material.DIAMOND_CHESTPLATE, 1),
				new ItemStack(Material.DIAMOND_BOOTS, 1),
				new ItemStack(Material.WATER_BUCKET, 1),
				new ItemStack(Material.LAVA_BUCKET, 1),
				new ItemStack(Material.SNOW_BALL, 16),
				new ItemStack(Material.COOKED_BEEF, 16),
				new ItemStack(Material.ENCHANTMENT_TABLE, 1),
				new ItemStack(Material.EXP_BOTTLE, 32),
				bow,
				new ItemStack(Material.ARROW, 32),
				new ItemStack(Material.GOLDEN_APPLE, 1),
				frPotion,
				speedPotion,
				new ItemStack(Material.ENDER_PEARL, 1)
		};
		
		ItemStack diamondSword = new ItemStack(Material.DIAMOND_SWORD, 1);
		diamondSword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
		
		ItemStack diamondChestplate = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
		diamondChestplate.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
		
		ItemStack diamondBoots = new ItemStack(Material.DIAMOND_SWORD, 1);
		diamondBoots.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
		
		ItemStack diamondBootsFalling = new ItemStack(Material.DIAMOND_SWORD, 1);
		diamondBootsFalling.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
		diamondBootsFalling.addEnchantment(Enchantment.PROTECTION_FALL, 2);
		
		ItemStack power3Bow = new ItemStack(Material.BOW, 1);
		diamondBootsFalling.addEnchantment(Enchantment.ARROW_DAMAGE, 3);
		
		tier1Items = new ItemStack[] {
				new ItemStack(Material.ENDER_PEARL, 2),
				new ItemStack(Material.ENDER_PEARL, 4),
				new ItemStack(Material.GOLDEN_APPLE, 2),
				new ItemStack(Material.GOLDEN_APPLE, 4),
				diamondSword,
				new ItemStack(Material.LOG, 64),
				new ItemStack(Material.DIAMOND_HELMET, 1),
				new ItemStack(Material.DIAMOND_LEGGINGS, 1),
				diamondChestplate,
				diamondBoots,
				diamondBootsFalling,
				new ItemStack(Material.SNOW_BALL, 64),
				power3Bow,
				new ItemStack(Material.ARROW, 64),
				new ItemStack(Material.TNT, 16)
		};
	}
	
	public Chest getBukkitChest() {
		
		return bukkitChest;
	}
	
	public byte getTier() {
		
		return tier;
	}
	
	public void fill() {
		
		Inventory inv = bukkitChest.getBlockInventory();
		
		for (short i = 0; i < ThreadLocalRandom.current().nextInt(2, 5 + 1); i++) {
			List<ItemStack> items = Arrays.asList(tier == 3 ? tier3Items : tier == 2 ? tier2Items : tier1Items);
			Collections.shuffle(items);
			ItemStack item = items.get(0);
			
			int randomSlot; 
			
			do {
				randomSlot = ThreadLocalRandom.current().nextInt(2, inv.getSize() + 1) - 1;
				
				if (inv.getItem(randomSlot) == null) {
					inv.setItem(randomSlot, item);
					break;
				}
			} while (inv.firstEmpty() != -1);
		}
	}
}