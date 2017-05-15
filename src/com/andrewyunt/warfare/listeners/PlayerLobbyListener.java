package com.andrewyunt.warfare.listeners;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.StaticConfiguration;
import com.andrewyunt.warfare.exception.PlayerException;
import com.andrewyunt.warfare.exception.SignException;
import com.andrewyunt.warfare.menu.ClassSelectorMenu;
import com.andrewyunt.warfare.objects.GamePlayer;
import com.andrewyunt.warfare.objects.SignDisplay;
import com.andrewyunt.warfare.utilities.Utils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;

public class PlayerLobbyListener extends PlayerListener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        event.setJoinMessage(null);

        Player player = event.getPlayer();

        // Send welcome message
        player.sendMessage(ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString()
                + "-----------------------------------------------------");
        player.sendMessage(ChatColor.YELLOW + "Welcome to " + ChatColor.GOLD
                + ChatColor.BOLD.toString() + "Warfare");
        player.sendMessage(ChatColor.GOLD + " * " + ChatColor.YELLOW + "Teamspeak: "
                + ChatColor.GRAY + "ts.faithfulmc.com");
        player.sendMessage(ChatColor.GOLD + " * " + ChatColor.YELLOW + "Website: "
                + ChatColor.GRAY + "www.faithfulmc.com");
        player.sendMessage(ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString()
                + "-----------------------------------------------------");

        GamePlayer gp = null;

        // Create the player's GamePlayer object
        try {
            gp = Warfare.getInstance().getPlayerManager().createPlayer(player.getUniqueId());
        } catch (PlayerException e) {
            e.printStackTrace();
        }

        final GamePlayer finalGP = gp;

        BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(Warfare.getInstance(), () -> {
            finalGP.updateHotbar();
            player.teleport(player.getLocation().getWorld().getSpawnLocation());
        }, 2L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        event.setQuitMessage(null);

        Player player = event.getPlayer();

        try {
            Warfare.getInstance().getPlayerManager().deletePlayer(Warfare.getInstance().getPlayerManager().getPlayer(player.getName()));
        } catch (PlayerException e) {
            e.printStackTrace();
        }
    }

    protected boolean handleHotbarClick(Player player, String itemName) {

        GamePlayer gp = null;

        try {
            gp = Warfare.getInstance().getPlayerManager().getPlayer(player.getName());
        } catch (PlayerException e) {
            e.printStackTrace();
        }

        /*
        if (itemName.equals(Utils.formatMessage(StaticConfiguration.LOBBY_SHOP_TITLE))) {
            Warfare.getInstance().getShopMenu().open(ShopMenu.Type.MAIN, gp);
            return true;
        } else*/
        if (itemName.equals(Utils.formatMessage(StaticConfiguration.LOBBY_CLASS_SELECTOR_TITLE))) {
            Warfare.getInstance().getClassSelectorMenu().open(ClassSelectorMenu.Type.KIT, gp);
            return true;
        } else if (itemName.equals(Utils.formatMessage(StaticConfiguration.LOBBY_PLAY_TITLE))) {
            Warfare.getInstance().getPlayMenu().open(gp);
            return true;
        }

        return false;
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {

        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {

        if(event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        Player player = event.getPlayer();

        if (player.getLocation().getY() < 0) {
            player.teleport(player.getLocation().getWorld().getSpawnLocation());
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {

        if (event.getLine(0) == null || event.getLine(1) == null || event.getLine(2) == null) {
            return;
        }

        if (!event.getLine(0).equalsIgnoreCase("[Leaderboard]")) {
            return;
        }

        Player player = event.getPlayer();

        if (!player.hasPermission("warfare.sign.create")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to create a leaderboard sign.");
            return;
        }

        SignDisplay.Type type = null;

        if (event.getLine(1).equalsIgnoreCase("kills")) {
            type = SignDisplay.Type.KILLS_LEADERBOARD;
        } else if (event.getLine(1).equalsIgnoreCase("wins")) {
            type = SignDisplay.Type.WINS_LEADERBOARD;
        } else {
            return;
        }

        int place;

        try {
            place = Integer.valueOf(event.getLine(2));
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "You did not enter an integer for the sign place.");
            return;
        }

        if (place > 5) {
            player.sendMessage(ChatColor.RED + "You may not enter a place over 5.");
            return;
        }

        try {
            Warfare.getInstance().getSignManager().createSign(
                    event.getBlock().getLocation(),
                    type,
                    place,
                    false);
        } catch (SignException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + e.getMessage());
        }
    }
}