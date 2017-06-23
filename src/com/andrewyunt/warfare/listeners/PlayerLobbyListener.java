package com.andrewyunt.warfare.listeners;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.configuration.StaticConfiguration;
import com.andrewyunt.warfare.game.Game;
import com.andrewyunt.warfare.lobby.*;
import com.andrewyunt.warfare.lobby.Server;
import com.andrewyunt.warfare.managers.SignManager;
import com.andrewyunt.warfare.menu.ShopMenu;
import com.andrewyunt.warfare.player.GamePlayer;
import com.andrewyunt.warfare.player.Party;
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
import java.util.*;
import java.util.stream.Collectors;

public class PlayerLobbyListener extends PlayerListener {

    @Override
    protected void playerJoin(GamePlayer player) {
        Player bp = player.getBukkitPlayer();

        // Send welcome message
        bp.sendMessage(ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString()
                + "-----------------------------------------------------");
        bp.sendMessage(ChatColor.YELLOW + "Welcome to " + ChatColor.GOLD + ChatColor.BOLD.toString() + "Warfare");
        bp.sendMessage(ChatColor.GOLD + " * " + ChatColor.YELLOW + "Teamspeak: " + ChatColor.GRAY + "ts.faithfulmc.com");
        bp.sendMessage(ChatColor.GOLD + " * " + ChatColor.YELLOW + "Website: " + ChatColor.GRAY + "www.faithfulmc.com");
        bp.sendMessage(ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString()
                + "-----------------------------------------------------");

        Bukkit.getServer().getPluginManager().callEvent(new UpdateHotbarEvent(player));
    }

    @EventHandler
    public void onPlayerSpawnLocation(PlayerSpawnLocationEvent event) {
        Location spawnLocation = event.getPlayer().getLocation().getWorld().getSpawnLocation();

        event.setSpawnLocation(new Location(spawnLocation.getWorld(), spawnLocation.getX() + 0.5,
                spawnLocation.getY(), spawnLocation.getZ() + 0.5, 90, 0));
    }

    @Override
    protected void playerQuit(GamePlayer player) {
        Warfare.getInstance().getPlayerManager().deletePlayer(player);
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

        ItemStack joinSolo = new ItemStack(Material.IRON_SWORD, 1);
        ItemMeta joinSoloMeta = joinSolo.getItemMeta();
        joinSoloMeta.setDisplayName(Utils.formatMessage(StaticConfiguration.LOBBY_JOIN_SOLO_TITLE));
        joinSolo.setItemMeta(joinSoloMeta);
        inv.setItem(StaticConfiguration.LOBBY_JOIN_SOLO_SLOT - 1, joinSolo);

        ItemStack joinTeams = new ItemStack(Material.DIAMOND_SWORD, 1);
        ItemMeta joinTeamsMeta = joinTeams.getItemMeta();
        joinTeamsMeta.setDisplayName(Utils.formatMessage(StaticConfiguration.LOBBY_JOIN_TEAMS_TITLE));
        joinTeams.setItemMeta(joinTeamsMeta);
        inv.setItem(StaticConfiguration.LOBBY_JOIN_TEAMS_SLOT - 1, joinTeams);

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
        } else if (itemName.equals(Utils.formatMessage(StaticConfiguration.LOBBY_JOIN_SOLO_TITLE))) {
            quickJoin(player, Server.ServerType.valueOf("SOLO"));
            return true;
        } else if (itemName.equals(Utils.formatMessage(StaticConfiguration.LOBBY_JOIN_TEAMS_TITLE))) {
            quickJoin(player, Server.ServerType.valueOf("TEAMS"));
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
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
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
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockDamageEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
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

    public abstract class PlayersEntity {
        protected UUID player;

        public PlayersEntity(UUID player) {
            this.player = player;
        }

        public Player getPlayer() {
            return Bukkit.getPlayer(player);
        }

        public abstract void sendToServer(String servername);
        public abstract int size();
    }

    public class SinglePlayerEntity extends PlayersEntity {
        public SinglePlayerEntity(UUID player) {
            super(player);
        }

        public int size() {
            return 1;
        }

        public void sendToServer(String servername) {
            Utils.sendPlayerToServer(Bukkit.getPlayer(player), servername);
        }
    }

    public class PartyPlayerEntity extends PlayersEntity {
        public PartyPlayerEntity(UUID partyLeader) {
            super(partyLeader);
        }

        public boolean hasFailed() {
            return Bukkit.getPlayer(player) == null || Warfare.getInstance().getPartyManager().getParty(player) == null;
        }

        public int size() {
            return Warfare.getInstance().getPartyManager().getParty(player).getMembers().size();
        }

        public void sendToServer(String servername) {
            Utils.sendPartyToServer(Bukkit.getPlayer(player), Warfare.getInstance().getPartyManager().getParty(player), servername);
        }
    }

    private void quickJoin(Player player, Server.ServerType serverType) {

        PlayersEntity playerEntity;
        Party party = Warfare.getInstance().getPartyManager().getParty(player.getUniqueId());
        if (party == null) {
            playerEntity = new SinglePlayerEntity(player.getUniqueId());
        } else {
            if (Objects.equals(party.getLeader(), player.getUniqueId())) {
                playerEntity = new PartyPlayerEntity(player.getUniqueId());
            } else {
                player.sendMessage(ChatColor.RED + "You must be the party leader to do this");
                Bukkit.getScheduler().runTask(Warfare.getInstance(), player::closeInventory);
                return;
            }
        }

        List<com.andrewyunt.warfare.lobby.Server> quickJoinServers = new ArrayList<>(Warfare.getInstance().getStorageManager()
                .getServers()).stream().filter(server -> server.getMaxPlayers() > 0 && server.getGameStage() == Game.Stage.COUNTDOWN
                || server.getGameStage() == Game.Stage.WAITING).collect(Collectors.toList());
        quickJoinServers.sort(Comparator.comparingInt(server -> (server.getGameStage().ordinal() * 1000) - server.getOnlinePlayers()));

        for (com.andrewyunt.warfare.lobby.Server server: quickJoinServers) {
            if (server.getServerType() == serverType) {
                int size = playerEntity.size();
                int amount = size == 1 ? 1 : size + 2;
                if (server.getOnlinePlayers() + amount <= server.getMaxPlayers()) {
                    playerEntity.sendToServer(server.getName());
                    if (playerEntity instanceof PartyPlayerEntity) {
                        Warfare.getInstance().getStorageManager().setPartyServer(party, server.getName());
                    }
                    server.setOnlinePlayers(server.getOnlinePlayers() + size);
                    return;
                }
            }
        }

        player.sendMessage(ChatColor.RED + "There are currently no available " + serverType.toString().toLowerCase() + " games");
    }
}