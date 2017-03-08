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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.andrewyunt.warfare.objects.ClassNameConversation;
import com.andrewyunt.warfare.objects.CustomClass;
import com.andrewyunt.warfare.objects.GamePlayer;
import com.andrewyunt.warfare.objects.Kit;
import com.andrewyunt.warfare.objects.Purchasable;
import com.andrewyunt.warfare.objects.Skill;
import com.andrewyunt.warfare.objects.Ultimate;
import com.andrewyunt.warfare.utilities.Utils;

public class ClassCreatorMenu implements Listener {
	
	public enum Type {
		
		MAIN("Main"),
		KIT("Kit"),
		ULTIMATE("Ultimate"),
		SKILL_ONE("Skill One"),
		SKILL_TWO("Skill Two");
		
		private String name;
		
		Type(String name) {
			
			this.name = name;
		}
		
		public String getName() {
			
			return name;
		}
	}
	
	private final ItemStack glassPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
	private Map<GamePlayer, CustomClass> creatingClasses = new HashMap<GamePlayer, CustomClass>();
	private Map<GamePlayer, CustomClass> replacingClasses = new HashMap<GamePlayer, CustomClass>();
	
	public ClassCreatorMenu() {
		
		ItemMeta glassPaneMeta = glassPane.getItemMeta();
		glassPaneMeta.setDisplayName(" ");
		glassPaneMeta.setLore(new ArrayList<String>());
		glassPane.setItemMeta(glassPaneMeta);
	}
	
	public void open(Type type, GamePlayer player, CustomClass customClass) {
		
		Player bp = player.getBukkitPlayer();
		Inventory inv;
		
		if (type == Type.MAIN) {
			inv = Bukkit.createInventory(null, 27, ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "Class Creator");
			int classSlot = 9;
			
			for (int i = 0; i < 27; i++) {
				if (i == classSlot) {
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
					
					ItemStack is = new ItemStack(Material.CHEST, 1);
					ItemMeta im = is.getItemMeta();
					
					if (bp.hasPermission("Warfare.classes." + classNum)) {
						try {
							customClass = player.getCustomClasses().get(classNum - 1);
							
							im.setDisplayName(customClass.getName());
							
							is = customClass.getKit().getDisplayItem();
						} catch (IndexOutOfBoundsException e) {
							im.setDisplayName("Class " + classNum);
						}
					} else
						im.setDisplayName(Utils.getFormattedMessage("no-perms-class-slot"));
					
					is.setItemMeta(im);
					inv.setItem(i, is);
				} else
					inv.setItem(i, glassPane);
			}
		} else {
			inv = Bukkit.createInventory(null, 54, ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "Class Creator - " + type.getName());
			
			for (int i = 0; i < 9; i++)
				inv.setItem(i, glassPane);
			
			for (int i = 9; i < 45; i = i + 9) {
				inv.setItem(i, glassPane);
				inv.setItem(i + 8, glassPane);
			}
			
			for (int i = 45; i < 54; i++)
				inv.setItem(i, glassPane);
			
			List<Purchasable> toAdd = new ArrayList<Purchasable>();
			
			for (Purchasable purchase : player.getPurchases())
				if (type == Type.KIT) {
					if (purchase instanceof Kit)
						toAdd.add(purchase);
				} else if (type == Type.ULTIMATE) {
					if (purchase instanceof Ultimate)
						toAdd.add(purchase);
				} else {
					if (purchase instanceof Skill)
						if (type == Type.SKILL_TWO) { 
							if (creatingClasses.get(player).getSkillOne() != purchase)
								toAdd.add(purchase);
						} else
							toAdd.add(purchase);
				}
			
			for (int i = 0; i < inv.getSize(); i++) {
				if (inv.getItem(i) != null)
					continue;
				
				Purchasable purchase = null;
				
				try {
					purchase = toAdd.get(0);
				} catch (IndexOutOfBoundsException e) {
					break;
				}
				
				toAdd.remove(purchase);
				
				ItemStack displayItem = purchase.getDisplayItem().clone();
				
				for(Enchantment enchantment : displayItem.getEnchantments().keySet())
					displayItem.removeEnchantment(enchantment);
				
				ItemMeta displayItemMeta = displayItem.getItemMeta();
				displayItemMeta.setDisplayName(ChatColor.GOLD + purchase.getName());
				displayItemMeta.setLore(Utils.colorizeList(Warfare.getInstance().getConfig().getStringList(
						"description-" + purchase.toString()), ChatColor.WHITE));
				displayItem.setItemMeta(displayItemMeta);
				
				inv.setItem(i, displayItem);
			}
		}
		
		bp.openInventory(inv);
	}
	
	@EventHandler
	private void onInventoryClick(InventoryClickEvent event) {
		
		String title = event.getClickedInventory().getTitle();
		
		if (!title.contains(ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "Class Creator"))
			return;
		
		event.setCancelled(true);
		
		ItemStack is = event.getCurrentItem();
		
		if (is.getType() == Material.STAINED_GLASS_PANE)
			return;
		
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
		
		if (title.equals(ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "Class Creator")) {
			if (name.equals(Utils.getFormattedMessage("no-perms-class-slot")))
				return;
			
			CustomClass customClass = new CustomClass();
			
			if (creatingClasses.containsKey(gp))
				creatingClasses.remove(gp);
			
			creatingClasses.put(gp, customClass);
			
			if (is.getType() != Material.CHEST) {
				if (replacingClasses.containsKey(gp.getCustomClass(name)))
					replacingClasses.remove(gp);
				
				replacingClasses.put(gp, gp.getCustomClass(name));
			}
			open(Type.KIT, gp, customClass);
		} else {
			String enumStr = ChatColor.stripColor(name.toUpperCase().replace(' ', '_').replace("'", ""));
			
			if (title.contains("Kit")) {
				creatingClasses.get(gp).setKit(Kit.valueOf(enumStr));
				open(Type.ULTIMATE, gp, null);
			} else if (title.contains("Ultimate")) {
				creatingClasses.get(gp).setUltimate(Ultimate.valueOf(enumStr));
				open(Type.SKILL_ONE, gp, null);
			} else if (title.contains("Skill One")) {
				creatingClasses.get(gp).setSkillOne(Skill.valueOf(enumStr));
				open(Type.SKILL_TWO, gp, null);
			} else if (title.contains("Skill Two")) {
				player.closeInventory();
				creatingClasses.get(gp).setSkillTwo(Skill.valueOf(enumStr));
				new ClassNameConversation(gp, creatingClasses.get(gp), replacingClasses.get(gp)).beginConversation();
			}
		}
	}
}