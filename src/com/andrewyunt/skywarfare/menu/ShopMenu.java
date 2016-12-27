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

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
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

public class ShopMenu implements Listener {
	
	public enum Type {
		MAIN,
		KITS,
		ULTIMATES,
		SKILLS
	}
	
	private ItemStack glassPane = new ItemStack(Material.THIN_GLASS, 1);
	private ItemStack kits = new ItemStack(Material.IRON_SWORD, 1);
	private ItemStack ultimates = new ItemStack(Material.EYE_OF_ENDER, 1);
	private ItemStack skills = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
	
	public ShopMenu() {
		
		ItemMeta kitsMeta = kits.getItemMeta();
		ItemMeta ultimatesMeta = ultimates.getItemMeta();
		ItemMeta skillsMeta = skills.getItemMeta();
		
		kitsMeta.setDisplayName("Kits");
		ultimatesMeta.setDisplayName("Ultimates");
		skillsMeta.setDisplayName("Skills");
		
		kits.setItemMeta(kitsMeta);
		ultimates.setItemMeta(ultimatesMeta);
		skills.setItemMeta(skillsMeta);
	}
	
	public void open(Type type, GamePlayer player) {
		
		Inventory inv = null;
		
		if (type == Type.MAIN) {
			inv = Bukkit.createInventory(null, 27, "Shop");
			
			for (int i = 0; i <= 10; i++)
				inv.setItem(i, glassPane);
			
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
			inv = Bukkit.createInventory(null, 54, "Shop - " + (type == Type.KITS ? "Kits"
					: type == Type.ULTIMATES ? "Ultimates" : "Skills"));
			
			for (int i = 0; i < 9; i++)
				inv.setItem(i, glassPane);
			
			for (int i = 9; i < 45; i = i + 9) {
				inv.setItem(i, glassPane);
				inv.setItem(i + 8, glassPane);
			}
			
			for (int i = 45; i < 54; i++)
				inv.setItem(i, glassPane);
			
			List<Purchasable> purchasables = Arrays.asList(type == Type.KITS ? Kit.values(): type == Type.ULTIMATES
					? Ultimate.values() : Skill.values());
			
			int purchasableNum = 0;
			
			for (int i = 0; i < inv.getSize(); i++) {
				if (inv.getItem(i) != null)
					continue;
				
				Purchasable purchasable = purchasables.get(purchasableNum);
				
				if (purchasableNum >= purchasables.size() - 1)
					break;
				
				purchasableNum++;
				
				ItemStack is = Utils.removeAttributes(purchasable.getDisplayItem().clone());
				
				for(Enchantment enchantment : is.getEnchantments().keySet())
					is.removeEnchantment(enchantment);
				
				ItemMeta im = is.getItemMeta();
				im.setDisplayName(ChatColor.AQUA + purchasable.getName());
				List<String> lore = Utils.colorizeList(SkyWarfare.getInstance().getConfig().getStringList(
						"description-" + purchasable.toString()), ChatColor.WHITE);
				lore.add("");
				lore.add(ChatColor.GOLD + (player.getPurchases().contains(purchasable) ?
						"Purchased" : "Price: " + purchasable.getPrice()));
				im.setLore(lore);
				is.setItemMeta(im);
				
				inv.setItem(i, is);
			}
			
			inv.setItem(4,(type == Type.KITS ? kits : type == Type.ULTIMATES ? ultimates : skills));
			
			ItemStack goBack = new ItemStack(Material.ARROW, 1);
			ItemMeta goBackMeta = goBack.getItemMeta();
			goBackMeta.setDisplayName(ChatColor.RED + "Go Back");
			goBack.setItemMeta(goBackMeta);
			inv.setItem(49, goBack);
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
			
			if (name.equals("Kits") || name.equals("Ultimtes") || name.equals("Skills"))
				return;
			
			if (im.getLore().contains(ChatColor.GOLD + "Purchased")) {
				player.sendMessage(ChatColor.RED + "You have already purchased that item.");
				return;
			}
			
			Purchasable purchasable = null;
			String enumName = ChatColor.stripColor(name.toUpperCase().replace(' ', '_'));
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