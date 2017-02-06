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
package com.andrewyunt.skywarfare.menu;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.andrewyunt.skywarfare.SkyWarfare;
import com.andrewyunt.skywarfare.objects.GamePlayer;

public class TeleporterMenu implements Listener {
	
	private ItemStack glassPane = new ItemStack(Material.THIN_GLASS, 1);
	
	public TeleporterMenu() {
		
		ItemMeta glassPaneMeta = glassPane.getItemMeta();
		glassPaneMeta.setDisplayName("  ");
		glassPaneMeta.setLore(new ArrayList<String>());
		glassPane.setItemMeta(glassPaneMeta);
	}
	
	public void open(GamePlayer player) {
		
		Inventory inv = Bukkit.createInventory(null, 54, "Teleporter");
		
		for (int i = 0; i < 9; i++)
			inv.setItem(i, glassPane);
		
		for (int i = 9; i < 45; i = i + 9) {
			inv.setItem(i, glassPane);
			inv.setItem(i + 8, glassPane);
		}
		
		for (int i = 45; i < 54; i++)
			inv.setItem(i, glassPane);
		
		ItemStack goBack = new ItemStack(Material.ARROW, 1);
		ItemMeta goBackMeta = goBack.getItemMeta();
		goBackMeta.setDisplayName(ChatColor.RED + "Close");
		goBack.setItemMeta(goBackMeta);
		inv.setItem(49, goBack);
		
		List<ItemStack> toAdd = new ArrayList<ItemStack>();
		
		for (GamePlayer inGame : SkyWarfare.getInstance().getGame().getPlayers()) {
			ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
			SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
			skullMeta.setOwner(inGame.getBukkitPlayer().getName());
			skullMeta.setDisplayName(inGame.getBukkitPlayer().getName());
			skull.setItemMeta(skullMeta);
			
			toAdd.add(skull);
		}
		
		for (int i = 0; i < 45; i++) {
			ItemStack is = inv.getItem(i);
			
			if (is == null || is.getType() == Material.AIR)
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
		
		if (inv == null)
			return;
		
		String title = inv.getTitle();
		
		if (title == null)
			return;
		
		if (!title.equals("Teleporter"))
			return;
		
		event.setCancelled(true);
		
		Player player = (Player) event.getWhoClicked();
		
		ItemStack is = event.getCurrentItem();
		
		if (is.getType() == Material.THIN_GLASS)
			return;
		
		if(is == null || is.getType() == Material.AIR)
			return;
		
		if (!is.hasItemMeta())
			return;
		
		String name = is.getItemMeta().getDisplayName();
		
		if (name == null)
			return;
		
		if (name.equals(ChatColor.RED + "Close")) {
			player.closeInventory();
			return;
		}
		
		event.getWhoClicked().teleport(Bukkit.getPlayer(name));
	}
}