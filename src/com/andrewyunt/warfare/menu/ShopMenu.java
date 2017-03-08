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

import java.text.NumberFormat;
import java.util.ArrayList;
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

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.exception.PlayerException;
import com.andrewyunt.warfare.objects.GamePlayer;
import com.andrewyunt.warfare.objects.HealthBoost;
import com.andrewyunt.warfare.objects.Kit;
import com.andrewyunt.warfare.objects.Purchasable;
import com.andrewyunt.warfare.objects.Skill;
import com.andrewyunt.warfare.objects.Ultimate;
import com.andrewyunt.warfare.utilities.Utils;

public class ShopMenu implements Listener {
	
	public enum Type {
		MAIN,
		KITS,
		ULTIMATES,
		SKILLS,
		HEALTH_BOOSTS
	}
	
	private final ItemStack glassPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
	private ItemStack kits = new ItemStack(Material.IRON_SWORD, 1);
	private ItemStack ultimates = new ItemStack(Material.EYE_OF_ENDER, 1);
	private ItemStack skills = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
	private ItemStack healthBoosts = new ItemStack(Material.GOLDEN_APPLE, 1);
	
	public ShopMenu() {
		
		ItemMeta glassPaneMeta = glassPane.getItemMeta();
		ItemMeta kitsMeta = kits.getItemMeta();
		ItemMeta ultimatesMeta = ultimates.getItemMeta();
		ItemMeta skillsMeta = skills.getItemMeta();
		ItemMeta healthBoostsMeta = healthBoosts.getItemMeta();
		
		glassPaneMeta.setDisplayName(" ");
		kitsMeta.setDisplayName(ChatColor.GOLD + "Kits");
		ultimatesMeta.setDisplayName(ChatColor.GOLD + "Ultimates");
		skillsMeta.setDisplayName(ChatColor.GOLD + "Skills");
		healthBoostsMeta.setDisplayName(ChatColor.GOLD + "Health Boosts");
		
		glassPaneMeta.setLore(new ArrayList<String>());
		
		glassPane.setItemMeta(glassPaneMeta);
		kits.setItemMeta(kitsMeta);
		ultimates.setItemMeta(ultimatesMeta);
		skills.setItemMeta(skillsMeta);
		healthBoosts.setItemMeta(healthBoostsMeta);
	}
	
	public void open(Type type, GamePlayer player) {
		
		Inventory inv = null;
		
		if (type == Type.MAIN) {
			inv = Bukkit.createInventory(null, 27, ChatColor.GREEN + ChatColor.BOLD.toString() + "Shop");
			
			for (int i = 0; i < 10; i++)
				inv.setItem(i, glassPane);
			
			inv.setItem(10, kits);
			inv.setItem(11, glassPane);
			inv.setItem(12, ultimates);
			inv.setItem(13, glassPane);
			inv.setItem(14, skills);
			inv.setItem(15, glassPane);
			inv.setItem(16, healthBoosts);
			
			for (int i = 17; i < 27; i++)
				inv.setItem(i, glassPane);
		} else {
			inv = Bukkit.createInventory(null, 54, "Shop - " + (type == Type.KITS ? "Kits" : type == Type.ULTIMATES
					? "Ultimates" : type == Type.SKILLS ? "Skills" : "Health Boosts"));
			
			for (int i = 0; i < 9; i++)
				inv.setItem(i, glassPane);
			
			for (int i = 9; i < 45; i = i + 9) {
				inv.setItem(i, glassPane);
				inv.setItem(i + 8, glassPane);
			}
			
			for (int i = 45; i < 54; i++)
				inv.setItem(i, glassPane);
			
			List<Purchasable> purchasables = Arrays.asList(type == Type.KITS ? Kit.values(): type == Type.ULTIMATES
					? Ultimate.values() : type == Type.SKILLS ? Skill.values() : HealthBoost.values());
			
			int purchasableNum = 0;
			
			for (int i = 0; i < inv.getSize(); i++) {
				if (inv.getItem(i) != null)
					continue;
				
				if (purchasableNum > purchasables.size() - 1)
					break;
				
				Purchasable purchasable = purchasables.get(purchasableNum);
				
				purchasableNum++;
				
				ItemStack is = purchasable.getDisplayItem().clone();
				
				for(Enchantment enchantment : is.getEnchantments().keySet())
					is.removeEnchantment(enchantment);
				
				ItemMeta im = is.getItemMeta();
				im.setDisplayName(ChatColor.GOLD + purchasable.getName());
				List<String> lore = Utils.colorizeList(Warfare.getInstance().getConfig().getStringList(
						"description-" + purchasable.toString()), ChatColor.WHITE);
				lore.add("");
				NumberFormat numberFormat = NumberFormat.getInstance();
				numberFormat.setGroupingUsed(true);
				lore.add(ChatColor.GREEN + (player.getPurchases().contains(purchasable) ?
						"Purchased" : "Price: $" + numberFormat.format(purchasable.getPrice())));
				im.setLore(lore);
				is.setItemMeta(im);
				
				inv.setItem(i, is);
			}
			
			inv.setItem(4,(type == Type.KITS ? kits : type == Type.ULTIMATES ? ultimates
					: type == Type.SKILLS ? skills : healthBoosts));
			
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
			gp = Warfare.getInstance().getPlayerManager().getPlayer(player);
		} catch (PlayerException e) {
			e.printStackTrace();
		}
		
		if (title.equals(ChatColor.GREEN + ChatColor.BOLD.toString() + "Shop")) {
			if (name.equals(ChatColor.GOLD + "Kits"))
				open(Type.KITS, gp);
			else if (name.equals(ChatColor.GOLD + "Ultimates"))
				open(Type.ULTIMATES, gp);
			else if (name.equals(ChatColor.GOLD + "Skills"))
				open(Type.SKILLS, gp);
			else if (name.equals(ChatColor.GOLD + "Health Boosts"))
				open(Type.HEALTH_BOOSTS, gp);
		} else {
			if (name.equals(ChatColor.RED + "Go Back")) {
				open(Type.MAIN, gp);
				return;
			}
			
			if (name.equals(" "))
				return;
			
			if (name.equals(ChatColor.GOLD + "Kits") || name.equals(ChatColor.GOLD + "Ultimtes") || name.equals(" ")
					|| name.equals(ChatColor.GOLD + "Skills") || name.equals(ChatColor.GOLD + "Health Boosts"))
				return;
			
			if (!im.hasLore())
				return;
			
			if (im.getLore().contains(ChatColor.GOLD + "Purchased")) {
				player.sendMessage(ChatColor.RED + "You have already purchased that item.");
				return;
			}
			
			Purchasable purchasable = null;
			String enumStr = ChatColor.stripColor(name.toUpperCase().replace(' ', '_').replace("'", ""));
			Type type = null;
			
			if (title.contains("Kits")) {
				purchasable = Kit.valueOf(enumStr);
				type = Type.KITS;
			} else if (title.contains("Ultimates")){
				purchasable = Ultimate.valueOf(enumStr);
				type = Type.ULTIMATES;
			} else if (title.contains("Skills")) {
				purchasable = Skill.valueOf(enumStr);
				type = Type.SKILLS;
			} else if (title.contains("Health Boosts")) {
				purchasable = HealthBoost.valueOf(enumStr);
				type = Type.HEALTH_BOOSTS;
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