package com.andrewyunt.warfare.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public abstract class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR  || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (item != null && item.hasItemMeta()) {
                handleHotbarClick(event.getPlayer(), item.getItemMeta().getDisplayName());
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();

        if (item != null && item.hasItemMeta()) {
            if (handleHotbarClick((Player) event.getWhoClicked(), item.getItemMeta().getDisplayName())) {
                event.setCancelled(true);
            }
        }
    }

    protected abstract boolean handleHotbarClick(Player player, String itemName);
}