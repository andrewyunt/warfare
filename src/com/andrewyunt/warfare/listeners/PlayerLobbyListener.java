package com.andrewyunt.warfare.listeners;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.configuration.StaticConfiguration;
import com.andrewyunt.warfare.managers.SignManager;
import com.andrewyunt.warfare.menu.ShopMenu;
import com.andrewyunt.warfare.player.GamePlayer;
import com.andrewyunt.warfare.lobby.SignDisplay;
import com.andrewyunt.warfare.player.events.UpdateHotbarEvent;
import com.andrewyunt.warfare.utilities.Utils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class PlayerLobbyListener extends PlayerListener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        event.setJoinMessage(null);

        Player player = event.getPlayer();

        // Send welcome message
        player.sendMessage(ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString()
                + "-----------------------------------------------------");
        player.sendMessage(ChatColor.YELLOW + "Welcome to " + ChatColor.GOLD + ChatColor.BOLD.toString() + "Warfare");
        player.sendMessage(ChatColor.GOLD + " * " + ChatColor.YELLOW + "Teamspeak: " + ChatColor.GRAY + "ts.faithfulmc.com");
        player.sendMessage(ChatColor.GOLD + " * " + ChatColor.YELLOW + "Website: " + ChatColor.GRAY + "www.faithfulmc.com");
        player.sendMessage(ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString()
                + "-----------------------------------------------------");

        GamePlayer gp = Warfare.getInstance().getPlayerManager().getPlayer(player.getUniqueId());

        Bukkit.getServer().getPluginManager().callEvent(new UpdateHotbarEvent(gp));
    }

    @EventHandler
    public void onPlayerSpawnLocation(PlayerSpawnLocationEvent event) {
        Location spawnLocation = event.getPlayer().getLocation().getWorld().getSpawnLocation();

        event.setSpawnLocation(new Location(spawnLocation.getWorld(), spawnLocation.getX() + 0.5,
                spawnLocation.getY(), spawnLocation.getZ() + 0.5, 90, 0));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        event.setQuitMessage(null);

        Player player = event.getPlayer();
        Warfare.getInstance().getPlayerManager().deletePlayer(Warfare.getInstance().getPlayerManager().getPlayer(player));
    }

    @EventHandler
    private void onUpdateHotbar(UpdateHotbarEvent event) {
        GamePlayer gamePlayer = event.getGamePlayer();
        PlayerInventory inv = gamePlayer.getBukkitPlayer().getInventory();
        inv.clear();

        ItemStack shop = new ItemStack(Material.CHEST, 1);
        ItemMeta shopMeta = shop.getItemMeta();
        shopMeta.setDisplayName(Utils.formatMessage(StaticConfiguration.LOBBY_SHOP_TITLE));
        shop.setItemMeta(shopMeta);
        inv.setItem(StaticConfiguration.LOBBY_SHOP_SLOT - 1, shop);

        ItemStack play = new ItemStack(Material.COMPASS, 1);
        ItemMeta playMeta = play.getItemMeta();
        playMeta.setDisplayName(Utils.formatMessage(StaticConfiguration.LOBBY_PLAY_TITLE));
        play.setItemMeta(playMeta);
        inv.setItem(StaticConfiguration.LOBBY_PLAY_SLOT - 1, play);

        ItemStack classSelector = new ItemStack(Material.ENDER_CHEST, 1);
        ItemMeta classSelectorMeta = classSelector.getItemMeta();
        classSelectorMeta.setDisplayName(Utils.formatMessage(StaticConfiguration.LOBBY_KIT_SELECTOR_TITLE));
        classSelector.setItemMeta(classSelectorMeta);
        inv.setItem(StaticConfiguration.LOBBY_KIT_SELECTOR_SLOT - 1, classSelector);

        gamePlayer.getBukkitPlayer().updateInventory();
    }

    protected boolean handleHotbarClick(Player player, String itemName) {
        GamePlayer gp = Warfare.getInstance().getPlayerManager().getPlayer(player.getName());

        if (itemName.equals(Utils.formatMessage(StaticConfiguration.LOBBY_SHOP_TITLE))) {
            Warfare.getInstance().getShopMenu().open(ShopMenu.Type.MAIN, gp);
            return true;
        } else if (itemName.equals(Utils.formatMessage(StaticConfiguration.LOBBY_KIT_SELECTOR_TITLE))) {
            Warfare.getInstance().getKitSelectorMenu().open(gp);
            return true;
        } else if (itemName.equals(Utils.formatMessage(StaticConfiguration.LOBBY_PLAY_TITLE))) {
            Warfare.getInstance().getPlayMenu().open(gp);
            return true;
        }

        return false;
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerInteractHigh(PlayerInteractEvent event) {
        if(event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }

        SignManager signManager = Warfare.getInstance().getSignManager();
        Location location = event.getBlock().getLocation();

        try {
            signManager.deleteSign(signManager.getSign(location));
        } catch (NullPointerException e) {
            // do nothing if sign doesn't exist
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if(event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockDamageEvent event) {
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

        SignDisplay.Type type;

        switch (event.getLine(1).toLowerCase()) {
            case "kills":
                type = SignDisplay.Type.KILLS_LEADERBOARD;
                break;
            case "wins":
                type = SignDisplay.Type.WINS_LEADERBOARD;
                break;
            case "kdr":
                type = SignDisplay.Type.KDR_LEADERBOARD;
                break;
            default:
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

        Warfare.getInstance().getSignManager().createSign(
                event.getBlock().getLocation(),
                type,
                place,
                false);
    }
}