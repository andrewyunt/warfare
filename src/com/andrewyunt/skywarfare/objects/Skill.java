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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * The enumeration for abilities, their names, and the method to use them.
 * 
 * @author Andrew Yunt
 */
public enum Skill implements Purchasable {
	
	RESISTANCE("Resistance"),
	JUGGERNAUT("Juggernaut"),
	CONSUMPTION("Consumption"),
	HEAD_START("Head Start"),
	GUARD("Guard"),
	FLAME("Flame");
	
	final String name;
	
	Skill(String name) {
		
		this.name = name;
	}
	
	@Override
	public String getName() {
		
		return name;
	}
	
	@Override
	public ItemStack getDisplayItem() {
		
		if (this == RESISTANCE) {
			return new ItemStack(Material.STONE_SWORD, 1);
		} else if (this == JUGGERNAUT) {
			return new ItemStack(Material.POTION, 1, (short) 0);
		} else if (this == CONSUMPTION) {
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
			return regenPotion;
		} else if (this == HEAD_START) {
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
		} else if (this == GUARD) {
			return new ItemStack(Material.CHEST, 1);
		} else if (this == FLAME) {
			return new ItemStack(Material.FIRE, 1);
		}
		
		return null;
	}
}