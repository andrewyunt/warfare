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

import com.andrewyunt.warfare.configuration.StaticConfiguration;
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
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.objects.GamePlayer;
import com.andrewyunt.warfare.objects.Kit;
import com.andrewyunt.warfare.utilities.Utils;

public class ClassSelectorMenu implements Listener {
	
	public enum Type {
		KIT("Kit");
		
		private final String name;
		
		Type(String name) {
			
			this.name = name;
		}
		
		public String getName() {
			
			return name;
		}
	}
	
	private final ItemStack glassPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
	
	public ClassSelectorMenu() {
		ItemMeta glassPaneMeta = glassPane.getItemMeta();
		glassPaneMeta.setDisplayName(" ");
		glassPaneMeta.setLore(new ArrayList<>());
		glassPane.setItemMeta(glassPaneMeta);
	}
	
	public void open(Type type, GamePlayer player) {
		
		Player bp = player.getBukkitPlayer();
		Inventory inv;
		
		inv = Bukkit.createInventory(null, type == Type.KIT ? 27 : 54, ChatColor.YELLOW
				+ ChatColor.BOLD.toString() + "Class Selector - " + type.getName());

		if (type == Type.KIT) {
			for (int i = 0; i < 11; i++) {
                inv.setItem(i, glassPane);
            }

			ItemStack uhc = new ItemStack(Material.GOLDEN_APPLE, 1);
			ItemMeta uhcMeta = uhc.getItemMeta();
			uhcMeta.setDisplayName(ChatColor.GOLD + "UHC");
			uhc.setItemMeta(uhcMeta);
			inv.setItem(11, uhc);

			inv.setItem(12, glassPane);

			Potion healPotion = new Potion(PotionType.INSTANT_HEAL, 2);
			healPotion.setSplash(true);
			ItemStack pot = healPotion.toItemStack(1);
			ItemMeta potMeta = pot.getItemMeta();
			potMeta.setDisplayName(ChatColor.GOLD + "Pot");
			pot.setItemMeta(potMeta);
			inv.setItem(13, pot);

			inv.setItem(14, glassPane);

			ItemStack soup = new ItemStack(Material.MUSHROOM_SOUP, 1);
			ItemMeta soupMeta = soup.getItemMeta();
			soupMeta.setDisplayName(ChatColor.GOLD + "Soup");
			soup.setItemMeta(soupMeta);
			inv.setItem(15, soup);

			for (int i = 16; i < 27; i++) {
                inv.setItem(i, glassPane);
            }
		}
		
		bp.openInventory(inv);
	}
	
	@EventHandler
	private void onInventoryClick(InventoryClickEvent event) {
		
		String title = event.getClickedInventory().getTitle();
		
		if (!title.contains(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Class Selector")) {
            return;
        }
		
		event.setCancelled(true);
		
		ItemStack is = event.getCurrentItem();
		
		if (is.getType() == Material.STAINED_GLASS_PANE) {
            return;
        }
		
		if (!is.hasItemMeta()) {
            return;
        }
		
		ItemMeta im = is.getItemMeta();
		String name = im.getDisplayName();
		Player player = (Player) event.getWhoClicked();
		GamePlayer gp = Warfare.getInstance().getPlayerManager().getPlayer(player);
		
		if (title.equals(ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "Class Selector")) {
			if (name.equals(Utils.formatMessage(StaticConfiguration.NO_PERMS_CLASS_SLOT))) {
                return;
            }
			open(Type.KIT, gp);
		} else {
			String enumStr = ChatColor.stripColor(name.toUpperCase().replace(' ', '_').replace("'", ""));
			if (title.contains("Kit")) {
				gp.setSelectedKit(Kit.valueOf(enumStr));
				player.closeInventory();
			}
		}
	}
}