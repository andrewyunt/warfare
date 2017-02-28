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
package com.andrewyunt.warfare.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LootChest {
	
	private static ItemStack bow = new ItemStack(Material.BOW, 1);
	private static ItemStack ironSword = new ItemStack(Material.IRON_SWORD, 1);
	private static ItemStack diamondSword = new ItemStack(Material.DIAMOND_SWORD, 1);
	private static ItemStack diamondChestplate = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
	private static ItemStack diamondBoots = new ItemStack(Material.DIAMOND_SWORD, 1);
	private static ItemStack diamondBootsFalling = new ItemStack(Material.DIAMOND_SWORD, 1);
	private static ItemStack power3Bow = new ItemStack(Material.BOW, 1);
	private static ItemStack frPotion = new ItemStack(Material.POTION, 3);
	private static ItemStack speedPotion = new ItemStack(Material.POTION, 3);
	
	static {
		bow.addEnchantment(Enchantment.ARROW_DAMAGE, 1);
		ironSword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
		diamondSword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
		diamondChestplate.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
		//diamondBoots.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
		//diamondBootsFalling.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
		//diamondBootsFalling.addEnchantment(Enchantment.PROTECTION_FALL, 2);
		
		PotionMeta frMeta = (PotionMeta) frPotion.getItemMeta();
		PotionEffect frEffect = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 9600, 1, false);
		List<String> lore = new ArrayList<String>();
		lore.add(ChatColor.RESET + "Fire Resistance 2" + ChatColor.RED + "\u2764");
		frMeta.setLore(lore);
		frMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.DARK_RED + "Fire Resistance Potion");
		frMeta.setMainEffect(PotionEffectType.FIRE_RESISTANCE);
		frMeta.addCustomEffect(frEffect, true);
		frPotion.setItemMeta(frMeta);
		
		PotionMeta speedPotionMeta = (PotionMeta) speedPotion.getItemMeta();
		PotionEffect speedEffect = new PotionEffect(PotionEffectType.SPEED, 1, 2, false);
		lore = new ArrayList<String>();
		lore.add(ChatColor.RESET + "SPEED 2" + ChatColor.RED + "\u2764");
		speedPotionMeta.setLore(lore);
		speedPotionMeta.setDisplayName(ChatColor.RESET + "" + ChatColor.DARK_RED + "Speed Potion");
		speedPotionMeta.setMainEffect(PotionEffectType.SPEED);
		speedPotionMeta.addCustomEffect(speedEffect, true);
		speedPotion.setItemMeta(speedPotionMeta);
	}
	
	private enum LootItem {
		
		I(3, 0, new ItemStack(Material.IRON_HELMET, 1)),
		II(3, 0, new ItemStack(Material.IRON_CHESTPLATE, 1)),
		III(3, 0, new ItemStack(Material.IRON_LEGGINGS, 1)),
		IV(3, 0, new ItemStack(Material.IRON_BOOTS, 1)),
		V(3, 0, new ItemStack(Material.FISHING_ROD, 1)),
		VI(3, 0, new ItemStack(Material.SNOW_BALL, 16)),
		VII(3, 0, new ItemStack(Material.BOW, 1)),
		VIII(3, 0, new ItemStack(Material.ARROW, 16)),
		IX(3, 1, new ItemStack(Material.WOOD, 16)),
		X(3, 1, new ItemStack(Material.WOOD, 32)),
		XI(3, 1, new ItemStack(Material.COBBLESTONE, 16)),
		XII(3, 1, new ItemStack(Material.COBBLESTONE, 32)),
		XIII(3, 2, new ItemStack(Material.IRON_AXE, 1)),
		XIV(3, 2, new ItemStack(Material.STONE_SWORD, 1)),
		XV(3, 2, new ItemStack(Material.IRON_SWORD, 1)),
		XVI(3, 3, new ItemStack(Material.COOKED_BEEF, 4)),
		XVII(3, 3, new ItemStack(Material.COOKED_BEEF, 16)),
		XVIII(3, 3, new ItemStack(Material.BREAD, 4)),
		XIX(3, 3, new ItemStack(Material.BREAD, 16)),
		XX(3, 4, new ItemStack(Material.WATER_BUCKET, 1)),
		XXI(3, 4, new ItemStack(Material.LAVA_BUCKET, 1)),
		XXIII(3, 4, new ItemStack(Material.FLINT_AND_STEEL, 1)),
		XXIV(2, 0, new ItemStack(Material.FISHING_ROD, 1)),
		XXV(2, 0, new ItemStack(Material.IRON_HELMET, 1)),
		XXVI(2, 0, new ItemStack(Material.IRON_CHESTPLATE, 1)),
		XXVII(2, 0, new ItemStack(Material.IRON_LEGGINGS, 1)),
		XXVIII(2, 0, new ItemStack(Material.IRON_BOOTS, 1)),
		XXIX(2, 0, new ItemStack(Material.SNOW_BALL, 16)),
		XXX(2, 0, new ItemStack(Material.COOKED_BEEF, 16)),
		XXXI(2, 0, new ItemStack(Material.LOG, 64)),
		XXXII(2, 0, new ItemStack(Material.ENCHANTMENT_TABLE, 1)),
		XXXIII(2, 0, new ItemStack(Material.EXP_BOTTLE, 32)),
		XXXIV(2, 0, new ItemStack(Material.GOLDEN_APPLE, 1)),
		XXXV(2, 0, new ItemStack(Material.ENDER_PEARL, 1)),
		XXXVI(2, 0, new ItemStack(Material.ARROW, 32)),
		XXXVII(2, 0, bow),
		XXXVIII(2, 1, new ItemStack(Material.WOOD, 64)),
		XXXIX(2, 1, new ItemStack(Material.COBBLESTONE, 64)),
		XL(2, 2, ironSword),
		XLI(2, 2, new ItemStack(Material.DIAMOND_SWORD, 1)),
		XLII(2, 3, new ItemStack(Material.DIAMOND_CHESTPLATE, 1)),
		XLIII(2, 3, new ItemStack(Material.DIAMOND_BOOTS, 1)),
		XLIV(2, 4, new ItemStack(Material.WATER_BUCKET, 1)),
		XLV(2, 4, new ItemStack(Material.LAVA_BUCKET, 1)),
		XLVII(2, 5, frPotion),
		XLVIII(2, 5, speedPotion),
		XLIX(1, 0, new ItemStack(Material.FISHING_ROD, 1)),
		L(1, 0, new ItemStack(Material.DIAMOND_HELMET, 1)),
		LI(1, 0, new ItemStack(Material.DIAMOND_LEGGINGS, 1)),
		LII(1, 0, new ItemStack(Material.ARROW, 64)),
		LIII(1, 0, new ItemStack(Material.TNT, 16)),
		LIV(1, 0, new ItemStack(Material.SNOW_BALL, 64)),
		LV(1, 0, diamondSword),
		LVI(1, 0, power3Bow),
		LVII(1, 0, diamondChestplate),
		LVIII(1, 1, new ItemStack(Material.ENDER_PEARL, 2)),
		LIX(1, 1, new ItemStack(Material.ENDER_PEARL, 4)),
		LX(1, 2, new ItemStack(Material.GOLDEN_APPLE, 2)),
		LXI(1, 2, new ItemStack(Material.GOLDEN_APPLE, 4)),
		LXII(1, 3, diamondBoots),
		LXIII(1, 3, diamondBootsFalling);
		
		private int tier, group;
		private ItemStack itemStack;
		
		LootItem(int tier, int group, ItemStack itemStack) {
			
			this.tier = tier;
			this.group = group;
			this.itemStack = itemStack;
		}
	}
	
	private Location location;
	private byte tier;
	
	public LootChest(Location location, byte tier) {
		
		this.location = location;
		this.tier = tier;
	}
	
	public Location getLocation() {
		
		return location;
	}
	
	public byte getTier() {
		
		return tier;
	}
	
	private LootItem getRandomLootItem(int group) {
		
		List<LootItem> lootItems = Arrays.asList(LootItem.values());
		
		Collections.shuffle(lootItems);
		
		for (LootItem lootItem : lootItems)
			if (lootItem.tier == tier && lootItem.group == group)
				return lootItem;
		
		return null;
	}
	
	public void fill() {
		
		Chest chest = ((Chest) location.getBlock().getState());
		Inventory inv = chest.getBlockInventory();
		
		List<LootItem> lootItems = new ArrayList<LootItem>();
		
		if (tier == 3) {
			lootItems.add(getRandomLootItem(1));
			lootItems.add(getRandomLootItem(2));
		} else if (tier == 2)
			lootItems.add(getRandomLootItem(1));
		
		int random = new Random().nextInt(4 - 3 + 1) + 3;
		
		for (int i = 0; i <= 5; i++) {
			if (lootItems.size() >= random)
				continue;
			
			boolean containsGroup = false;
			
			for (LootItem lootItem : lootItems)
				if (lootItem.group == i)
					containsGroup = true;
			
			if (containsGroup)
				continue;
			
			LootItem randomItem = getRandomLootItem(i);
			
			if (randomItem != null)
				lootItems.add(randomItem);
		}
		
		for (LootItem lootItem : lootItems) {
			int randomSlot;
			
			do {
				randomSlot = ThreadLocalRandom.current().nextInt(2, inv.getSize() + 1) - 1;
				
				if (inv.getItem(randomSlot) == null) {
					inv.setItem(randomSlot, lootItem.itemStack.clone());
					break;
				}
			} while (inv.firstEmpty() != -1);
		}
		
		chest.update();
	}
}