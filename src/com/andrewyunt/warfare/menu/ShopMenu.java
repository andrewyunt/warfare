
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
import com.andrewyunt.warfare.objects.GamePlayer;
import com.andrewyunt.warfare.objects.HealthBoost;
import com.andrewyunt.warfare.objects.Purchasable;
import com.andrewyunt.warfare.utilities.Utils;

@Deprecated
public class ShopMenu implements Listener {
	
	public enum Type {
		MAIN,
		HEALTH_BOOSTS
	}
	
	private final ItemStack glassPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
	private final ItemStack ultimates = new ItemStack(Material.EYE_OF_ENDER, 1);
	private final ItemStack skills = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
	private final ItemStack healthBoosts = new ItemStack(Material.GOLDEN_APPLE, 1);
	
	public ShopMenu() {
		
		ItemMeta glassPaneMeta = glassPane.getItemMeta();
		ItemMeta ultimatesMeta = ultimates.getItemMeta();
		ItemMeta skillsMeta = skills.getItemMeta();
		ItemMeta healthBoostsMeta = healthBoosts.getItemMeta();
		
		glassPaneMeta.setDisplayName(" ");
		ultimatesMeta.setDisplayName(ChatColor.GOLD + "Ultimates");
		skillsMeta.setDisplayName(ChatColor.GOLD + "Skills");
		healthBoostsMeta.setDisplayName(ChatColor.GOLD + "Health Boosts");
		
		glassPaneMeta.setLore(new ArrayList<>());
		
		glassPane.setItemMeta(glassPaneMeta);
		ultimates.setItemMeta(ultimatesMeta);
		skills.setItemMeta(skillsMeta);
		healthBoosts.setItemMeta(healthBoostsMeta);
	}
	
	public void open(Type type, GamePlayer player) {
		
		Inventory inv;
		
		if (type == Type.MAIN) {
			inv = Bukkit.createInventory(null, 27, ChatColor.GREEN + ChatColor.BOLD.toString() + "Shop");
			
			for (int i = 0; i < 11; i++) {
                inv.setItem(i, glassPane);
            }
			
			inv.setItem(11, ultimates);
			inv.setItem(12, glassPane);
			inv.setItem(13, skills);
			inv.setItem(14, glassPane);
			inv.setItem(15, healthBoosts);
			
			for (int i = 16; i < 27; i++) {
                inv.setItem(i, glassPane);
            }
		} else {
			inv = Bukkit.createInventory(null, 54, "Shop - Health Boosts");
			
			for (int i = 0; i < 9; i++) {
                inv.setItem(i, glassPane);
            }
			
			for (int i = 9; i < 45; i = i + 9) {
				inv.setItem(i, glassPane);
				inv.setItem(i + 8, glassPane);
			}
			
			for (int i = 45; i < 54; i++) {
                inv.setItem(i, glassPane);
            }
			
			List<Purchasable> purchasables = Arrays.asList(HealthBoost.values());
			
			int purchasableNum = 0;
			
			for (int i = 0; i < inv.getSize(); i++) {
				if (inv.getItem(i) != null) {
                    continue;
                }
				
				if (purchasableNum > purchasables.size() - 1) {
                    break;
                }
				
				Purchasable purchasable = purchasables.get(purchasableNum);
				
				purchasableNum++;
				
				ItemStack is = purchasable.getDisplayItem().clone();
				
				for(Enchantment enchantment : is.getEnchantments().keySet()) {
                    is.removeEnchantment(enchantment);
                }
				
				ItemMeta im = is.getItemMeta();
				im.setDisplayName(ChatColor.GOLD + purchasable.getName());
				List<String> lore = Utils.colorizeList(Warfare.getInstance().getConfig().getStringList(
						"description-" + purchasable.toString()), ChatColor.YELLOW);
				lore.add("");
				NumberFormat numberFormat = NumberFormat.getInstance();
				numberFormat.setGroupingUsed(true);
				lore.add(player.getPurchases().contains(purchasable) ? ChatColor.GREEN + "Purchased"
						: ChatColor.RED + "Price: $" + numberFormat.format(purchasable.getPrice(0)));
				im.setLore(lore);
				is.setItemMeta(im);
				
				inv.setItem(i, is);
			}
			
			inv.setItem(4,healthBoosts);
			
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
			if (name.equals(ChatColor.GOLD + "Health Boosts")) {
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
			
			if (name.equals(ChatColor.GOLD + "Ultimtes") || name.equals(" ") || name.equals(ChatColor.GOLD + "Skills")
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

			if (title.contains("Health Boosts")) {
				purchasable = HealthBoost.valueOf(enumStr);
				type = Type.HEALTH_BOOSTS;
			}
			
			if (gp.getCoins() < purchasable.getPrice(0)) {
				player.sendMessage(ChatColor.RED + String.format("You do not have enough coins to purchase %s.",
						purchasable.getName()));
				return;
			}
			
			gp.setCoins(gp.getCoins() - purchasable.getPrice(0));
			gp.getPurchases().add(purchasable);
			
			player.sendMessage(ChatColor.GOLD + String.format("You purchased %s for %s coins.",
					purchasable.getName(), purchasable.getPrice(0)));
			
			open(type, gp);
		}
	}
}