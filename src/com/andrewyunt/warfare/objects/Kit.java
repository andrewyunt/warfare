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

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public enum Kit implements Purchasable {
	
	ARMORER("Armorer", 0),
	ARCHER("Archer", 10000),
	SWORDSMAN("Swordsman", 10000),
	BOOSTER("Booster", 30000),
	CANNONER("Cannoner", 20000),
	HEALER("Healer", 15000),
	ENCHANTER("Enchanter", 10000),
	FISHERMAN("Fisherman", 8000),
	SCOUT("Scout", 20000),
	PYROMANIAC("Pyromaniac", 30000);
	
	private final String name;
	private final int price;
	
	Kit(String name, int price) {
		
		this.name = name;
		this.price = price;
	}
	
	@Override
	public String getName() {
		
		return name;
	}
	
	@Override
	public int getPrice() {
		
		return price;
	}
	
	@Override
	public ItemStack getDisplayItem() {
		
		if (this == ARMORER) {
			return new ItemStack(Material.IRON_CHESTPLATE, 1);
		} else if (this == ARCHER) {
			ItemStack bow = new ItemStack(Material.BOW, 1);
			bow.setDurability((short) (bow.getDurability() - 370));
			bow.addEnchantment(Enchantment.ARROW_DAMAGE, 1);
			return bow;
		} else if (this == SWORDSMAN) {
			ItemStack sword = new ItemStack(Material.STONE_SWORD, 1);
			sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
			return sword;
		} else if (this == BOOSTER) {
			ItemStack bow = new ItemStack(Material.BOW);
			bow.addEnchantment(Enchantment.ARROW_KNOCKBACK, 2);
			bow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
			ItemMeta bowMeta = bow.getItemMeta();
			bowMeta.setDisplayName("Booster Bow");
			bow.setItemMeta(bowMeta);
			return bow;
		} else if (this == CANNONER) {
			ItemStack boots = new ItemStack(Material.IRON_BOOTS);
			boots.addEnchantment(Enchantment.PROTECTION_FALL, 3);
			return boots;
		} else if (this == HEALER) {
			ItemStack healItem = new ItemStack(Material.POTION, 1);
			Potion healPotion = new Potion(PotionType.INSTANT_HEAL, 2);
			healPotion.setSplash(true);
			healPotion.apply(healItem);
			return healItem;
		} else if (this == ENCHANTER) {
			return new ItemStack(Material.ENCHANTMENT_TABLE, 1);
		} else if (this == FISHERMAN) {
			return new ItemStack(Material.FISHING_ROD, 1);
		} else if (this == SCOUT) {
			ItemStack speedItem = new ItemStack(Material.POTION, 1);
			Potion frPotion = new Potion(PotionType.SPEED, 2);
			frPotion.setSplash(true);
			frPotion.apply(speedItem);
			return speedItem;
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
			inv.addItem(new ItemStack(Material.WATER_BUCKET, 1));
		} else if (this == HEALER) {
			inv.addItem(getDisplayItem());
			
			ItemStack regenItem = new ItemStack(Material.POTION);
			Potion regenPotion = new Potion(PotionType.REGEN, 1);
			regenPotion.setSplash(true);
			regenPotion.apply(regenItem);
			inv.addItem(regenItem);
		} else if (this == ENCHANTER) {
			inv.addItem(new ItemStack(Material.DIAMOND_SPADE, 1));
			inv.addItem(new ItemStack(Material.ENCHANTMENT_TABLE, 1));
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
			
			ItemStack frItem = new ItemStack(Material.POTION);
			Potion frPotion = new Potion(PotionType.FIRE_RESISTANCE, 1);
			frPotion.setSplash(true);
			frPotion.apply(frItem);
			PotionMeta frMeta = (PotionMeta) frItem.getItemMeta();
			frMeta.addCustomEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 1), true);
			frItem.setItemMeta(frMeta);
			inv.addItem(frItem);
		}
	}
}