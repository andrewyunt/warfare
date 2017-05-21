
package com.andrewyunt.warfare.menu;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.andrewyunt.warfare.objects.*;
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
import com.andrewyunt.warfare.utilities.Utils;

public class ShopMenu implements Listener {
	
	public enum Type {
		MAIN,
		POWERUPS,
		PERKS,
		HEALTH_BOOSTS
	}
	
	private final ItemStack glassPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
	private final ItemStack powerups = new ItemStack(Material.EYE_OF_ENDER, 1);
	private final ItemStack perks = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
	private final ItemStack healthBoosts = new ItemStack(Material.GOLDEN_APPLE, 1);
	
	public ShopMenu() {
		
		ItemMeta glassPaneMeta = glassPane.getItemMeta();
		ItemMeta powerupsMeta = powerups.getItemMeta();
		ItemMeta perksMeta = perks.getItemMeta();
		ItemMeta healthBoostsMeta = healthBoosts.getItemMeta();
		
		glassPaneMeta.setDisplayName(" ");
		powerupsMeta.setDisplayName(ChatColor.GOLD + "Powerups");
		perksMeta.setDisplayName(ChatColor.GOLD + "Perks");
		healthBoostsMeta.setDisplayName(ChatColor.GOLD + "Health Boosts");
		
		glassPaneMeta.setLore(new ArrayList<>());
		
		glassPane.setItemMeta(glassPaneMeta);
		powerups.setItemMeta(powerupsMeta);
		perks.setItemMeta(perksMeta);
		healthBoosts.setItemMeta(healthBoostsMeta);
	}
	
	public void open(Type type, GamePlayer player) {
		
		Inventory inv;
		
		if (type == Type.MAIN) {
			inv = Bukkit.createInventory(null, 27, ChatColor.GREEN + ChatColor.BOLD.toString() + "Shop");
			
			for (int i = 0; i < 11; i++) {
                inv.setItem(i, glassPane);
            }
			
			inv.setItem(11, powerups);
			inv.setItem(12, glassPane);
			inv.setItem(13, perks);
			inv.setItem(14, glassPane);
			inv.setItem(15, healthBoosts);
			
			for (int i = 16; i < 27; i++) {
                inv.setItem(i, glassPane);
            }
		} else if (type == Type.PERKS || type == Type.HEALTH_BOOSTS) {
			inv = Bukkit.createInventory(null, 54, "Shop - " + (type == Type.PERKS ? "Perks" : "Health Boosts"));
			
			for (int i = 0; i < 9; i++) {
                inv.setItem(i, glassPane);
            }
			
			for (int i = 9; i < 45; i = i + 9) {
				inv.setItem(i, glassPane);
				inv.setItem(i + 1, glassPane);
				inv.setItem(i + 7, glassPane);
				inv.setItem(i + 8, glassPane);
			}
			
			for (int i = 45; i < 54; i++) {
                inv.setItem(i, glassPane);
            }

			List<Purchasable> purchasables = Arrays.asList(type == Type.POWERUPS ? Powerup.values()
					: type == Type.PERKS ? Perk.values() : HealthBoost.values());
			
			int purchasableNum = 0;
			
			for (int i = 0; i < inv.getSize(); i++) {
				if (inv.getItem(i) != null) {
                    continue;
                }
				
				if (purchasableNum > purchasables.size() - 1) {
                    break;
                }

				Purchasable purchasable = purchasables.get(purchasableNum);
				ItemStack is = purchasable.getDisplayItem().clone();
				
				for(Enchantment enchantment : is.getEnchantments().keySet()) {
                    is.removeEnchantment(enchantment);
                }
				
				ItemMeta im = is.getItemMeta();
				im.setDisplayName(ChatColor.GOLD + purchasable.getName());
				List<String> lore = Utils.colorizeList(Warfare.getInstance().getConfig().getStringList(
						"descriptions." + purchasable.toString() + ".0"), ChatColor.YELLOW);
				lore.add("");
				NumberFormat numberFormat = NumberFormat.getInstance();
				numberFormat.setGroupingUsed(true);
				lore.add(player.getPurchases().containsKey(purchasable) ? ChatColor.GREEN + "Purchased"
						: ChatColor.RED + "Price: $" + numberFormat.format(purchasable.getPrice(0)));
				im.setLore(lore);
				is.setItemMeta(im);
				
				inv.setItem(i, is);

				purchasableNum++;
			}

			inv.setItem(4,(type == Type.POWERUPS ? powerups : type == Type.PERKS ? perks : healthBoosts));
			
			ItemStack goBack = new ItemStack(Material.ARROW, 1);
			ItemMeta goBackMeta = goBack.getItemMeta();
			goBackMeta.setDisplayName(ChatColor.RED + "Go Back");
			goBack.setItemMeta(goBackMeta);
			inv.setItem(49, goBack);
		} else {
			inv = Bukkit.createInventory(null, 54, "Shop - Powerups");

			for (int i = 0; i < 54; i = i + 9) {
				inv.setItem(i, glassPane);
				inv.setItem(i + 1, glassPane);
				inv.setItem(i + 7, glassPane);
				inv.setItem(i + 8, glassPane);
			}

			int row = 0;
			for (Powerup powerup : Powerup.values()) {
				ItemStack is = powerup.getDisplayItem().clone();

				for(Enchantment enchantment : is.getEnchantments().keySet()) {
					is.removeEnchantment(enchantment);
				}

				ItemMeta im = is.getItemMeta();
				im.setDisplayName(ChatColor.GOLD + powerup.getName());
				is.setItemMeta(im);
				inv.setItem(row * 9 + 2, is);

				for (int level = 0; level < 4; level++) {
					is = player.getPurchases().get(powerup) >= level ?
							new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5) :
							player.getLevel() >= powerup.getPlayerLvlNeeded(level) ?
							new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 1) :
							new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
					im = is.getItemMeta();
					im.setDisplayName(ChatColor.GOLD + powerup.getName() + " - Level " + level);
					List<String> lore = Utils.colorizeList(Warfare.getInstance().getConfig().getStringList(
							"descriptions." + powerup.toString() + "." + level), ChatColor.YELLOW);
					lore.add("");
					NumberFormat numberFormat = NumberFormat.getInstance();
					numberFormat.setGroupingUsed(true);
					if (player.getLevel() < powerup.getPlayerLvlNeeded(level)) {
						lore.add(ChatColor.RED + ChatColor.BOLD.toString() + "MUST BE LVL "
								+ powerup.getPlayerLvlNeeded(level) + " TO PURCHASE");
					}
					lore.add(player.getPurchases().get(powerup) >= level ? ChatColor.GREEN + "Purchased" :
							ChatColor.RED + "Price: $" + numberFormat.format(powerup.getPrice(level)));
					im.setLore(lore);
					is.setItemMeta(im);
					inv.setItem(row * 9 + level + 3, is);
				}

				row++;
			}
		}
		
		player.getBukkitPlayer().openInventory(inv);
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		
		String title = event.getClickedInventory().getTitle();
		
		if (!title.contains("Shop")) {
            return;
        }
		
		event.setCancelled(true);
		
		ItemStack is = event.getCurrentItem();
		
		if (!is.hasItemMeta()) {
            return;
        }
		
		ItemMeta im = is.getItemMeta();
		String name = im.getDisplayName();
		Player player = (Player) event.getWhoClicked();
		GamePlayer gp = Warfare.getInstance().getPlayerManager().getPlayer(player);
		
		if (title.equals(ChatColor.GREEN + ChatColor.BOLD.toString() + "Shop")) {
			if (name.equals(ChatColor.GOLD + "Powerups")) {
				open(Type.POWERUPS, gp);
			} else if (name.equals(ChatColor.GOLD + "Perks")) {
				open(Type.PERKS, gp);
			} else if (name.equals(ChatColor.GOLD + "Health Boosts")) {
                open(Type.HEALTH_BOOSTS, gp);
            }
		} else {
			if (name.equals(ChatColor.RED + "Go Back")) {
				open(Type.MAIN, gp);
				return;
			}
			
			if (name.equals(" ")) {
                return;
            }
			
			if (name.equals(ChatColor.GOLD + "Powerups") || name.equals(" ") || name.equals(ChatColor.GOLD + "Perks")
					|| name.equals(ChatColor.GOLD + "Health Boosts")) {
                return;
            }
			
			if (!im.hasLore()) {
                return;
            }
			
			if (im.getLore().contains(ChatColor.GOLD + "Purchased")) {
				player.sendMessage(ChatColor.RED + "You have already purchased that item.");
				return;
			}

			Purchasable purchasable = null;
			String enumStr = ChatColor.stripColor(name.toUpperCase().replace(' ', '_').replace("'", ""));
			Type type = null;
			int level = 0;

			if (title.contains("Powerups")) {
				purchasable = Powerup.valueOf(enumStr.substring(0, name.length() - 12));
				type = Type.POWERUPS;
				level = Integer.valueOf(String.valueOf(name.charAt(name.length() -1)));

				if (is.getDurability() == 14) {
					player.sendMessage(ChatColor.RED + "You haven't unlocked that item yet.");
					return;
				}
			} else if (title.contains("Perks")) {
				purchasable = Perk.valueOf(enumStr);
				type = Type.PERKS;
			} if (title.contains("Health Boosts")) {
				purchasable = HealthBoost.valueOf(enumStr);
				type = Type.HEALTH_BOOSTS;
			}
			
			if (gp.getCoins() < purchasable.getPrice(level)) {
				player.sendMessage(ChatColor.RED + String.format("You do not have enough coins to purchase %s.",
						purchasable.getName()));
				return;
			}
			
			gp.setCoins(gp.getCoins() - purchasable.getPrice(level));
			gp.getPurchases().put(purchasable, level);
			
			player.sendMessage(ChatColor.GOLD + String.format("You purchased %s for %s coins.",
					name, purchasable.getPrice(level)));
			
			open(type, gp);
		}
	}
}