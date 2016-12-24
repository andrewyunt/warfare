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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.andrewyunt.skywarfare.SkyWarfare;
import com.andrewyunt.skywarfare.exception.PlayerException;
import com.andrewyunt.skywarfare.objects.ClassNameConversation;
import com.andrewyunt.skywarfare.objects.CustomClass;
import com.andrewyunt.skywarfare.objects.GamePlayer;
import com.andrewyunt.skywarfare.objects.Kit;
import com.andrewyunt.skywarfare.objects.Purchasable;
import com.andrewyunt.skywarfare.objects.Skill;
import com.andrewyunt.skywarfare.objects.Ultimate;

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
	
	private ItemStack glassPane = new ItemStack(Material.THIN_GLASS, 1);
	private Map<GamePlayer, CustomClass> editingClasses = new HashMap<GamePlayer, CustomClass>();
	
	public void open(Type type, GamePlayer player, CustomClass customClass) {
		
		Player bp = player.getBukkitPlayer();
		Inventory inv;
		
		if (type == Type.MAIN) {
			inv = Bukkit.createInventory(null, 27, "Class Creator");
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
					
					if (bp.hasPermission("skywarfare.classes." + classNum)) {
						try {
							customClass = player.getCustomClasses().get(classNum - 1);
							
							im.setDisplayName(customClass.getName());
							
							is = customClass.getKit().getDisplayItem();
						} catch (IndexOutOfBoundsException e) {
							im.setDisplayName("Class " + classNum);
						}
					} else
						im.setDisplayName(ChatColor.RED + "Donate @ amosita.net for more class slots.");
					
					is.setItemMeta(im);
					inv.setItem(i, is);
				} else
					if (i == 22) {
						ItemStack close = new ItemStack(Material.ARROW, 1);
						ItemMeta closeMeta = close.getItemMeta();
						closeMeta.setDisplayName(ChatColor.RED + "Close");
						close.setItemMeta(closeMeta);
						inv.setItem(22, close);
					} else
						inv.setItem(i, glassPane);
			}
		} else {
			inv = Bukkit.createInventory(null, 18, "Class Creator - " + type.getName());
			int i =  0;
			
			for (Purchasable purchasable : player.getPurchases()) {
				if (type == Type.KIT) {
					if (!(purchasable instanceof Kit))
						continue;
				} else if (type == Type.ULTIMATE) {
					if (!(purchasable instanceof Ultimate))
						continue;
				} else
					if (!(purchasable instanceof Skill))
						continue;
				
				ItemStack displayItem = purchasable.getDisplayItem();
				ItemMeta displayItemMeta = displayItem.getItemMeta();
				displayItemMeta.setDisplayName(purchasable.getName());
				
				List<String> lore = SkyWarfare.getInstance().getConfig().getStringList(
						"description-" + purchasable.toString());
				lore.add("");
				lore.add(player.getPurchases().contains(purchasable) ? "PURCHASED" : "Price: " + purchasable.getPrice());
				displayItemMeta.setLore(lore);
				
				displayItem.setItemMeta(displayItemMeta);
				
				inv.setItem(i, displayItem);
				
				i++;
			}
		}
		
		bp.openInventory(inv);
	}
	
	@EventHandler
	private void onInventoryClick(InventoryClickEvent event) {
		
		String title = event.getClickedInventory().getTitle();
		
		if (!title.contains("Class Creator"))
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
		
		if (title.equals("Class Creator")) {
			if (name.equals(ChatColor.RED + "Close")) {
				player.closeInventory();
				return;
			} else if (name.equals(ChatColor.RED + "Donate @ amosita.net for more class slots."))
				return;
			
			CustomClass customClass = new CustomClass();
			
			if (editingClasses.containsKey(gp))
				editingClasses.remove(gp);
			
			editingClasses.put(gp, customClass);
			open(Type.KIT, gp, customClass);
		} else {
			String enumStr = name.toUpperCase().replace(" ", "_");
			
			if (title.contains("Kit")) {
				editingClasses.get(gp).setKit(Kit.valueOf(enumStr));
				open(Type.ULTIMATE, gp, null);
			} else if (title.contains("Ultimate")) {
				editingClasses.get(gp).setUltimate(Ultimate.valueOf(enumStr));
				open(Type.SKILL_ONE, gp, null);
			} else if (title.contains("Skill One")) {
				editingClasses.get(gp).setSkillOne(Skill.valueOf(enumStr));
				open(Type.SKILL_TWO, gp, null);
			} else if (title.contains("Skill Two")) {
				player.closeInventory();
				editingClasses.get(gp).setSkillTwo(Skill.valueOf(enumStr));
				new ClassNameConversation(gp, editingClasses.get(gp)).beginConversation();
			}
		}
	}
}