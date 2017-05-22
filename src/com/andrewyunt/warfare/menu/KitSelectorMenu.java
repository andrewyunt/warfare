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
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;
import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.player.GamePlayer;
import com.andrewyunt.warfare.player.Kit;

public class KitSelectorMenu implements Listener {
	
	private final ItemStack glassPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
	
	public KitSelectorMenu() {
		ItemMeta glassPaneMeta = glassPane.getItemMeta();
		glassPaneMeta.setDisplayName(" ");
		glassPaneMeta.setLore(new ArrayList<>());
		glassPane.setItemMeta(glassPaneMeta);
	}
	
	public void open(GamePlayer player) {

		Player bp = player.getBukkitPlayer();
		Inventory inv = Bukkit.createInventory(null, 27, ChatColor.YELLOW
				+ ChatColor.BOLD.toString() + "Kit Selector");

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

		bp.openInventory(inv);
	}
	
	@EventHandler
	private void onInventoryClick(InventoryClickEvent event) {
		
		String title = event.getClickedInventory().getTitle();
		
		if (!title.contains(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Kit Selector")) {
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
		String enumStr = ChatColor.stripColor(name.toUpperCase().replace(' ', '_')
				.replace("'", ""));
		gp.setSelectedKit(Kit.valueOf(enumStr));
		player.closeInventory();
	}
}