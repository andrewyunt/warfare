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
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * The enumeration for abilities, their names, and the method to use them.
 * 
 * @author Andrew Yunt
 */
public enum Ultimate implements Upgradable {

	HEAL("Heal", 4),
	WRATH("Weath", 6),
	HELL_SPAWNING("Hell's Spawning", 2),
	LEAP("Leap", 4),
	SONIC("Sonic", 3),
	WITHERING("Withering", 5),
	FLAMING_FEET("Flaming Feet", 4);
	
	private String name;
	private int energyPerClick;

	Ultimate(String name, int energyPerClick) {

		this.name = name;
		this.energyPerClick = energyPerClick;
	}

	@Override
	public String getName() {

		return name;
	}

	public int getEnergyPerClick() {
		
		return energyPerClick;
	}
	
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
		
		if (this == HEAL) {
			
		}
	}
}