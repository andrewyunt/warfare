package com.andrewyunt.warfare.menu;

import java.util.ArrayList;
import com.andrewyunt.warfare.objects.Powerup;
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
import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.objects.GamePlayer;

public class PowerupSelectorMenu implements Listener {

    private final ItemStack glassPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);

    public PowerupSelectorMenu() {
        ItemMeta glassPaneMeta = glassPane.getItemMeta();
        glassPaneMeta.setDisplayName(" ");
        glassPaneMeta.setLore(new ArrayList<>());
        glassPane.setItemMeta(glassPaneMeta);
    }

    public void open(GamePlayer player) {

        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.YELLOW
                + ChatColor.BOLD.toString() + "Powerup Selector");

        for (int i = 0; i < 9; i++) {
            inv.setItem(i, glassPane);
        }

        int slot = 10;
        for (Powerup powerup : Powerup.values()) {
            if (slot == 13) {
                inv.setItem(slot, glassPane);
                slot++;
            }

            ItemStack is = powerup.getDisplayItem();
            ItemMeta im = is.getItemMeta();
            im.setDisplayName(ChatColor.GOLD + powerup.getName());
            is.setItemMeta(im);
            inv.setItem(slot, is);

            slot++;
        }

        for (int i = 18; i < 27; i++) {
            inv.setItem(i, glassPane);
        }

        player.getBukkitPlayer().openInventory(inv);
    }

    @EventHandler
    private void onInventoryClick(InventoryClickEvent event) {

        String title = event.getClickedInventory().getTitle();

        if (!title.contains(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Powerup Selector")) {
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
        Powerup powerup = Powerup.valueOf(enumStr);
        if (gp.getLevel(powerup) >= 0) {
            gp.setSelectedPowerup(powerup);
        } else {
            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You must reach lvl "
                    + powerup.getPlayerLvlNeeded(0) + " to unlock and purchase this powerup.");
        }
    }
}