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

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.andrewyunt.skywarfare.SkyWarfare;
import com.andrewyunt.skywarfare.exception.PlayerException;
import com.andrewyunt.skywarfare.objects.GamePlayer;
import com.andrewyunt.skywarfare.objects.Kit;
import com.andrewyunt.skywarfare.objects.Purchasable;
import com.andrewyunt.skywarfare.objects.Skill;
import com.andrewyunt.skywarfare.objects.Ultimate;
import com.andrewyunt.skywarfare.utilities.Utils;

import net.md_5.bungee.api.ChatColor;

public class ShopMenu implements Listener {
	
	public enum Type {
		MAIN,
		KITS,
		ULTIMATES,
		SKILLS
	}
	
	private ItemStack glassPane = new ItemStack(Material.THIN_GLASS, 1);
	
	public void open(Type type, GamePlayer player) {
		
		Inventory inv = null;
		
		if (type == Type.MAIN) {
			inv = Bukkit.createInventory(null, 27, "Shop");
			
			for (int i = 0; i <= 10; i++)
				inv.setItem(i, glassPane);
			
			ItemStack kits = new ItemStack(Material.IRON_SWORD, 1);
			ItemStack ultimates = new ItemStack(Material.EYE_OF_ENDER, 1);
			ItemStack skills = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
			
			ItemMeta kitsMeta = kits.getItemMeta();
			ItemMeta ultimatesMeta = ultimates.getItemMeta();
			ItemMeta skillsMeta = skills.getItemMeta();
			
			kitsMeta.setDisplayName("Kits");
			ultimatesMeta.setDisplayName("Ultimates");
			skillsMeta.setDisplayName("Skills");
			
			kits.setItemMeta(kitsMeta);
			ultimates.setItemMeta(ultimatesMeta);
			skills.setItemMeta(skillsMeta);
			
			inv.setItem(11, kits);
			inv.setItem(12, glassPane);
			inv.setItem(13, ultimates);
			inv.setItem(14, glassPane);
			inv.setItem(15, skills);
			
			for (int i = 16; i <= 21; i++)
				inv.setItem(i, glassPane);
			
			ItemStack close = new ItemStack(Material.ARROW, 1);
			ItemMeta closeMeta = close.getItemMeta();
			closeMeta.setDisplayName(ChatColor.RED + "Close");
			close.setItemMeta(closeMeta);
			inv.setItem(22, close);
			
			for (int i = 23; i <= 26; i++)
				inv.setItem(i, glassPane);
		} else {
			inv = Bukkit.createInventory(null, 18, "Shop - " + (type == Type.KITS ? "Kits"
					: type == Type.ULTIMATES ? "Ultimates" : "Skills"));
			
			Purchasable[] purchasables = type == Type.KITS ? Kit.values(): type == Type.ULTIMATES
					? Ultimate.values() : Skill.values();
			
			for (Purchasable purchasable : purchasables) {
				ItemStack is = Utils.removeAttributes(purchasable.getDisplayItem());
				ItemMeta im = is.getItemMeta();
				
				im.setDisplayName(purchasable.getName());
				
				List<String> lore = SkyWarfare.getInstance().getConfig().getStringList(
						"description-" + purchasable.toString());
				lore.add("");
				lore.add(player.getPurchases().contains(purchasable) ? "PURCHASED" : "Price: " + purchasable.getPrice());
				im.setLore(lore);
				
				is.setItemMeta(im);
				inv.addItem(is);
			}
			
			ItemStack goBack = new ItemStack(Material.ARROW, 1);
			ItemMeta goBackMeta = goBack.getItemMeta();
			goBackMeta.setDisplayName(ChatColor.RED + "Go Back");
			goBack.setItemMeta(goBackMeta);
			inv.setItem(13, goBack);
		}
		
		player.getBukkitPlayer().openInventory(inv);
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		
		String title = event.getClickedInventory().getTitle();
		
		if (!title.contains("Shop"))
			return;
		
		event.setCancelled(true);
		
		ItemStack is = event.getCurrentItem();
		
		if (!is.hasItemMeta())
			return;
		
		ItemMeta im = is.getItemMeta();
		String name = im.getDisplayName();
		Player player = (Player) event.getWhoClicked();
		GamePlayer gp = null;
		
		try {
			gp = SkyWarfare.getInstance().getPlayerManager().getPlayer(player);
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		if (title.equals("Shop")) {
			if (name.equals("Kits"))
				open(Type.KITS, gp);
			else if (name.equals("Ultimates"))
				open(Type.ULTIMATES, gp);
			else if (name.equals("Skills"))
				open(Type.SKILLS, gp);
			else if (name.equals(ChatColor.RED + "Close"))
				player.closeInventory();
		} else {
			if (name.equals(ChatColor.RED + "Go Back")) {
				open(Type.MAIN, gp);
				return;
			}
			
			if (im.getLore().contains("PURCHASED")) {
				player.sendMessage(ChatColor.RED + "You have already purchased that item.");
				return;
			}
			
			Purchasable purchasable = null;
			String enumName = name.toUpperCase().replace(' ', '_');
			Type type = null;
			
			if (title.contains("Kits")) {
				purchasable = Kit.valueOf(enumName);
				type = Type.KITS;
			} else if (title.contains("Ultimates")){
				purchasable = Ultimate.valueOf(enumName);
				type = Type.ULTIMATES;
			} else if (title.contains("Skills")) {
				purchasable = Skill.valueOf(enumName);
				type = Type.SKILLS;
			}
			
			if (gp.getCoins() < purchasable.getPrice()) {
				player.sendMessage(ChatColor.RED + String.format("You do not have enough coins to purchase %s.",
						purchasable.getName()));
				return;
			}
			
			gp.setCoins(gp.getCoins() - purchasable.getPrice());
			gp.getPurchases().add(purchasable);
			
			player.sendMessage(ChatColor.GOLD + String.format("You purchased %s for %s coins.",
					purchasable.getName(), purchasable.getPrice()));
			
			open(type, gp);
		}
	}
}