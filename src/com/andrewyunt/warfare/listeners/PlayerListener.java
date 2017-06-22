package com.andrewyunt.warfare.listeners;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.player.Booster;
import com.andrewyunt.warfare.player.GamePlayer;
import com.andrewyunt.warfare.purchases.Powerup;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.time.LocalDateTime;
import java.util.*;

public abstract class PlayerListener implements Listener {

    private Map<GamePlayer, Map.Entry<BukkitTask, BukkitTask>> playerTasks = new HashMap<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);

        // Create GamePlayer object
        GamePlayer gp = Warfare.getInstance().getPlayerManager().getPlayer(event.getPlayer());

        // Add powerups to player's purchases if they don't exist
        for (Powerup powerup : Powerup.values()) {
            if (!gp.getPurchases().containsKey(powerup)) {
                gp.getPurchases().put(powerup, -1);
            }
        }

        Warfare warfare = Warfare.getInstance();

        // Check player's boosters periodically
        BukkitTask boosterTask = Bukkit.getScheduler().runTaskTimerAsynchronously(warfare, () -> {
            Set<Booster> toRemove = new HashSet<>();
            for (Booster booster : gp.getBoosters()) {
                if (LocalDateTime.now().isAfter(booster.getExpiry())) {
                    toRemove.add(booster);
                }
            }
            for (Booster booster : toRemove) {
                gp.getBoosters().remove(booster);
            }
        }, 1200L, 0L);

        // Check player's pending transactions periodically
        BukkitTask pendingTransactionsTask = Bukkit.getScheduler().runTaskTimerAsynchronously(warfare, () ->
                warfare.getStorageManager().resolvePendingTransactions(gp), 0L, 20L);

        playerTasks.put(gp, new AbstractMap.SimpleEntry<>(boosterTask, pendingTransactionsTask));

        playerJoin(gp);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        GamePlayer gp = Warfare.getInstance().getPlayerManager().getPlayer(event.getPlayer());

        Map.Entry<BukkitTask, BukkitTask> entry = playerTasks.get(gp);
        Bukkit.getScheduler().cancelTask(entry.getKey().getTaskId());
        Bukkit.getScheduler().cancelTask(entry.getKey().getTaskId());

        playerQuit(gp);
    }

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

    protected abstract void playerJoin(GamePlayer player);

    protected abstract void playerQuit(GamePlayer player);

    protected abstract boolean handleHotbarClick(Player player, String itemName);
}