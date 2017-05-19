/*
 * Unpublished Copyright (c) 2017 Andrew Yunt, All Rights Reserved.
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
package com.andrewyunt.warfare.menu;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.objects.GamePlayer;

public class TeleporterMenu implements Listener {
	
	private final ItemStack glassPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
	
	public TeleporterMenu() {
		
		ItemMeta glassPaneMeta = glassPane.getItemMeta();
		glassPaneMeta.setDisplayName("  ");
		glassPaneMeta.setLore(new ArrayList<>());
		glassPane.setItemMeta(glassPaneMeta);
	}
	
	public void open(GamePlayer player) {
		
		Inventory inv = Bukkit.createInventory(null, 27, "Teleporter");
		List<ItemStack> toAdd = new ArrayList<>();
		
		for (GamePlayer inGame : Warfare.getInstance().getGame().getPlayers()) {
			ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
			SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
			skullMeta.setOwner(inGame.getBukkitPlayer().getName());
			skullMeta.setDisplayName(ChatColor.GRAY + inGame.getBukkitPlayer().getDisplayName());
			skull.setItemMeta(skullMeta);
			
			toAdd.add(skull);
		}
		
		for (int i = 0; i < 45; i++) {
			try {
				inv.setItem(i, toAdd.get(0));
				toAdd.remove(0);
			} catch (IndexOutOfBoundsException e) {
				break;
			}
		}
		
		player.getBukkitPlayer().openInventory(inv);
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		
		Inventory inv = event.getClickedInventory();
		
		if (inv == null) {
            return;
        }
		
		String title = inv.getTitle();
		
		if (title == null) {
            return;
        }
		
		if (!title.equals("Teleporter")) {
            return;
        }
		
		event.setCancelled(true);
		
		ItemStack is = event.getCurrentItem();
		
		if (is.getType() == Material.STAINED_GLASS_PANE) {
            return;
        }
		
		if(is.getType() == Material.AIR) {
            return;
        }
		
		if (!is.hasItemMeta()) {
            return;
        }
		
		String name = is.getItemMeta().getDisplayName();
		
		if (name == null) {
            return;
        }
		
		event.getWhoClicked().teleport(Bukkit.getPlayer(ChatColor.stripColor(name)));
	}
}