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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

public enum Kit {
	
	UHC("UHC"),
	POT("Pot"),
	SOUP("Soup");
	
	private final String name;
	
	Kit(String name) {
		
		this.name = name;
	}
	
	public String getName() {
		
		return name;
	}
	
	public void giveItems(GamePlayer player) {
		
		PlayerInventory inv = player.getBukkitPlayer().getInventory();
		
		if (this == UHC) {
			inv.setItem(0, new ItemStack(Material.WOOD_SWORD, 1));
			inv.setItem(1, new ItemStack(Material.FISHING_ROD, 1));
			inv.setItem(2, new ItemStack(Material.IRON_PICKAXE, 1));
			inv.setItem(3, new ItemStack(Material.COBBLESTONE, 32));
			inv.setItem(4, new ItemStack(Material.GOLDEN_APPLE, 6));
		} else if (this == POT) {
			inv.setItem(0, new ItemStack(Material.WOOD_SWORD, 1));
			inv.setItem(1, new ItemStack(Material.ENDER_PEARL, 1));
			Potion speedPotion = new Potion(PotionType.SPEED, 2);
			inv.setItem(2, speedPotion.toItemStack(1));
			Potion healPotion = new Potion(PotionType.INSTANT_HEAL, 2);
			healPotion.setSplash(true);
			for (int i = 3; i < 8; i++) {
                inv.setItem(i, healPotion.toItemStack(1));
            }
			inv.setItem(8, new ItemStack(Material.BAKED_POTATO, 5));
		} else if (this == SOUP) {
			inv.setItem(0, new ItemStack(Material.WOOD_SWORD, 1));
			inv.setItem(1, new ItemStack(Material.ENCHANTMENT_TABLE, 1));
			inv.setItem(2, new ItemStack(Material.EXP_BOTTLE, 16));
			for (int i = 3; i < 8; i++) {
                inv.setItem(i, new ItemStack(Material.MUSHROOM_SOUP, 1));
            }
			inv.setItem(8, new ItemStack(Material.MELON));
		}
	}
}