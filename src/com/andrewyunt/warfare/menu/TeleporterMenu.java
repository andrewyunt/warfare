package com.andrewyunt.warfare.menu;

import java.util.ArrayList;
import java.util.List;
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
import org.bukkit.inventory.meta.SkullMeta;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.player.GamePlayer;

public class TeleporterMenu implements Listener {

	public TeleporterMenu() {

		ItemStack glassPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
		ItemMeta glassPaneMeta = glassPane.getItemMeta();
		glassPaneMeta.setDisplayName("  ");
		glassPaneMeta.setLore(new ArrayList<>());
		glassPane.setItemMeta(glassPaneMeta);
	}
	
	public void open(GamePlayer player) {
		
		Inventory inv = Bukkit.createInventory(null, 27, "Teleporter");
		List<ItemStack> toAdd = new ArrayList<>();
		
		for (GamePlayer inGame : Warfare.getInstance().getGame().getPlayers()) {
			ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
			SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
			skullMeta.setOwner(inGame.getBukkitPlayer().getName());
			skullMeta.setDisplayName(ChatColor.GRAY + inGame.getBukkitPlayer().getDisplayName());
			skull.setItemMeta(skullMeta);
			
			toAdd.add(skull);
		}
		
		for (int i = 0; i < 45; i++) {
			try {
				inv.setItem(i, toAdd.get(0));
				toAdd.remove(0);
			} catch (IndexOutOfBoundsException e) {
				break;
			}
		}
		
		player.getBukkitPlayer().openInventory(inv);
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		
		Inventory inv = event.getClickedInventory();
		
		if (inv == null) {
            return;
        }
		
		String title = inv.getTitle();
		
		if (title == null) {
            return;
        }
		
		if (!title.equals("Teleporter")) {
            return;
        }
		
		event.setCancelled(true);
		
		ItemStack is = event.getCurrentItem();
		
		if (is.getType() == Material.STAINED_GLASS_PANE) {
            return;
        }
		
		if (is.getType() == Material.AIR) {
            return;
        }
		
		if (!is.hasItemMeta()) {
            return;
        }
		
		String name = is.getItemMeta().getDisplayName();
		
		if (name == null) {
            return;
        }

		Player player = (Player) event.getWhoClicked();

		player.teleport(Bukkit.getPlayer(ChatColor.stripColor(name)));
		player.sendMessage(String.format(ChatColor.YELLOW + "Teleported to " + ChatColor.GOLD + "%s"
						+ ChatColor.YELLOW + ".", name));
	}
}