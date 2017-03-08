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
package com.andrewyunt.warfare.menu;

import java.util.ArrayList;

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

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.exception.PlayerException;
import com.andrewyunt.warfare.objects.CustomClass;
import com.andrewyunt.warfare.objects.GamePlayer;

/**
 * The class used to create instances of the class selector menu.
 * 
 * @author Andrew Yunt
 */
public class ClassSelectorMenu implements Listener {
	
	private final ItemStack glassPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
	
	public ClassSelectorMenu() {
		
		ItemMeta glassPaneMeta = glassPane.getItemMeta();
		glassPaneMeta.setDisplayName("  ");
		glassPaneMeta.setLore(new ArrayList<String>());
		glassPane.setItemMeta(glassPaneMeta);
	}

	public void open(GamePlayer player) {

		Inventory inv = Bukkit.createInventory(null, 27, ChatColor.AQUA + ChatColor.BOLD.toString() + "Select Class");
		int classSlot = 9;
		
		for (int i = 0; i < 10; i++)
			inv.setItem(i, glassPane);
		
		for (int i = 18; i < 27; i++)
			inv.setItem(i, glassPane);
		
		for (int i = 10; i < 18; i = i + 2)
			inv.setItem(i, glassPane);
		
		for (int i = 9; i < 18; i = i + 2) {
			if (i < 16)
				classSlot = classSlot + 2;
			
			int classNum = 1;
			
			switch (i) {
			case 11:
				classNum = 2;
				break;
			case 13:
				classNum = 3;
				break;
			case 15:
				classNum = 4;
				break;
			case 17:
				classNum = 5;
			}
			
			if (player.getBukkitPlayer().hasPermission("warfare.classes." + classNum)) {
				try {
					CustomClass customClass = player.getCustomClasses().get(classNum - 1);
					ItemStack is = customClass.getKit().getDisplayItem();
					ItemMeta im = is.getItemMeta();
					im.setDisplayName(customClass.getName());
					is.setItemMeta(im);
					inv.setItem(i, is);
				} catch (IndexOutOfBoundsException e) {
					inv.setItem(i, glassPane);
				}
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
		
		if (!title.equals(ChatColor.AQUA + ChatColor.BOLD.toString() + "Select Class"))
			return;
		
		event.setCancelled(true);

		Player player = (Player) event.getWhoClicked();
		ItemStack is = event.getCurrentItem();
		
		if (is.getType() == Material.STAINED_GLASS_PANE)
			return;
		
		if(is == null || is.getType() == Material.AIR)
			return;
		
		if (!is.hasItemMeta())
			return;

		String name = is.getItemMeta().getDisplayName();

		if (name == null || name.equals(" "))
			return;
		
		GamePlayer gp = null;
		
		try {
			gp = Warfare.getInstance().getPlayerManager().getPlayer(player);
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		gp.setCustomClass(gp.getCustomClass(name));
		
		player.sendMessage(ChatColor.GOLD +  String.format("You selected the %s class.", name));
		
		player.closeInventory();
	}
}