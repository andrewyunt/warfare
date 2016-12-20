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
package com.andrewyunt.skywarfare.menu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.andrewyunt.skywarfare.objects.GamePlayer;
import com.andrewyunt.skywarfare.objects.Kit;
import com.andrewyunt.skywarfare.objects.Purchasable;
import com.andrewyunt.skywarfare.objects.Skill;
import com.andrewyunt.skywarfare.objects.Ultimate;

public class ShopMenu implements Listener {
	
	public enum Type {
		MAIN,
		KITS,
		ULTIMATES,
		SKILLS
	}
	
	ItemStack glassPane = new ItemStack(Material.GLASS, 1);
	
	public void open(GamePlayer player, Type type) {
		
		Inventory inv = null;
		
		if (type == Type.MAIN) {
			inv = Bukkit.createInventory(null, 27);
			
			for (int i = 0; i <= 11; i++)
				inv.addItem(glassPane);
			
			inv.addItem(new ItemStack(Material.IRON_SWORD, 1));
			inv.addItem(glassPane);
			inv.addItem(new ItemStack(Material.EYE_OF_ENDER, 1));
			inv.addItem(glassPane);
			inv.addItem(new ItemStack(Material.DIAMOND_CHESTPLATE, 1));
			
			for (int i = 17; i <= 21; i++)
				inv.addItem(glassPane);
			
			ItemStack goBack = new ItemStack(Material.ARROW, 1);
			ItemMeta goBackMeta = goBack.getItemMeta();
			goBackMeta.setDisplayName("Go Back");
			inv.addItem(goBack);
			
			for (int i = 23; i <= 26; i++)
				inv.addItem(glassPane);
			
			player.getBukkitPlayer().openInventory(inv);
		} else {
			inv = Bukkit.createInventory(null, 9);
			
			Purchasable[] purchasables = type == Type.KITS ? Kit.values(): type == Type.ULTIMATES
					? Ultimate.values() : Skill.values();
			
			for (Purchasable purchasable : purchasables) {
				ItemStack is = purchasable.getDisplayItem();
				ItemMeta im = is.getItemMeta();
				im.setDisplayName(purchasable.getName());
				inv.addItem(is);
			}
		}
	}
}