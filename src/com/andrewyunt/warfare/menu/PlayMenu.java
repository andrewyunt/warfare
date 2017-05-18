package com.andrewyunt.warfare.menu;

import com.andrewyunt.warfare.objects.Game;
import com.andrewyunt.warfare.objects.Party;
import com.andrewyunt.warfare.objects.Server;
import com.andrewyunt.warfare.utilities.Utils;
import com.faithfulmc.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.objects.GamePlayer;

import java.util.*;
import java.util.stream.Collectors;

public class PlayMenu implements Listener, InventoryHolder {

    private final int SIZE = 6 * 9;

    private final int QUICK_JOIN_SLOT = 49;
    private final ItemStack QUICK_JOIN_ITEM = new ItemBuilder(Material.IRON_SWORD).displayName(ChatColor.GOLD + "Quick Join").lore(ChatColor.GRAY + "Click to join a game").build();

    private final ItemStack PANE = new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).displayName(" ").build();

    private final Inventory inventory;
    private List<Server> inventoryServers = new ArrayList<>();
    private List<Server> quickJoinServers = new ArrayList<>();

    public PlayMenu() {
        inventory = Bukkit.createInventory(this, SIZE, ChatColor.YELLOW + "Join Game");
        Bukkit.getScheduler().runTaskTimerAsynchronously(Warfare.getInstance(), () -> {
            List<Server> serverList = Warfare.getInstance().getMySQLManager().getServers();
            inventoryServers = new ArrayList<>(serverList);
            inventoryServers.sort(Comparator.comparingInt(server -> (server.getGameStage().getOrder() * 1000) - server.getOnlinePlayers()));
            quickJoinServers = new ArrayList<>(serverList).stream().filter(server -> server.getGameStage() == Game.Stage.COUNTDOWN || server.getGameStage() == Game.Stage.WAITING).collect(Collectors.toList());
            quickJoinServers.sort(Comparator.comparingInt(server -> (server.getGameStage().ordinal() * 1000) - server.getOnlinePlayers()));
            Bukkit.getScheduler().runTask(Warfare.getInstance(), () -> inventory.setContents(getContents()));
        }, 0, 2);
    }

    public ItemStack[] getContents(){
        ItemStack[] itemStacks = new ItemStack[SIZE];
        for (int i = 0; i < 9; i++) {
            itemStacks[i] = PANE.clone();
        }
        for (int i = 9; i < 45; i = i + 9) {
            itemStacks[i] = PANE.clone();
            itemStacks[i + 8] = PANE.clone();
        }
        for (int i = 45; i < 54; i++) {
            itemStacks[i] = PANE.clone();
        }

        List<ItemStack> toAdd = new ArrayList<>();

        for(Server server: inventoryServers){
            ItemStack itemStack = createServerItem(server);
            if(itemStack != null){
                toAdd.add(itemStack);
            }
        }

        for (int i = 0; i < 45; i++) {
            ItemStack is = itemStacks[i];
            if (is == null || is.getType() == Material.AIR) {
                try {
                    itemStacks[i] = toAdd.remove(0);
                } catch (IndexOutOfBoundsException e) {
                    break;
                }
            }
        }

        itemStacks[QUICK_JOIN_SLOT] = QUICK_JOIN_ITEM.clone();

        return itemStacks;
    }

    public Inventory getInventory(){
        return inventory;
    }

    public ItemStack createServerItem(Server server){
        if(server.getServerType() == Server.ServerType.GAME && server.getGameStage().ordinal() < Game.Stage.END.ordinal()){
            return new ItemBuilder(Material.STAINED_GLASS_PANE, 1, server.getGameStage().getDyeColor().getData())
                    .displayName(ChatColor.GOLD + ChatColor.BOLD.toString() + server.getName())
                    .lore(
                            "",
                            ChatColor.GOLD + "Players: " + ChatColor.GRAY + " " + server.getOnlinePlayers() + "/" + server.getMaxPlayers(),
                            ChatColor.GOLD + "Stage: " + ChatColor.GRAY + server.getGameStage().getDisplay(),
                            ""
                    )
                    .build();
        }
        return null;
    }

    public void open(GamePlayer player) {
        player.getBukkitPlayer().openInventory(getInventory());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getClickedInventory();
        if(inv != null && inv.getHolder() == this) {
            event.setCancelled(true);

            int slot = event.getSlot();

            Player player = (Player) event.getWhoClicked();

            PlayersEntity playerEntity;
            Party party = Warfare.getInstance().getPartyManager().getParty(player.getUniqueId());
            if (party == null) {
                playerEntity = new SinglePlayerEntity(player.getUniqueId());
            } else {
                if (party.getLeader() == player.getUniqueId()) {
                    playerEntity = new PartyPlayerEntity(player.getUniqueId());
                } else {
                    player.sendMessage(ChatColor.RED + "You must be the party leader to do this");
                    Bukkit.getScheduler().runTask(Warfare.getInstance(), player::closeInventory);
                    return;
                }
            }

            if (slot == QUICK_JOIN_SLOT) {
                Bukkit.getScheduler().runTask(Warfare.getInstance(), player::closeInventory);
                for(Server server: quickJoinServers){
                    int size = playerEntity.size();
                    int amount = size == 1 ? 1 : size + 2;
                    if(server.getOnlinePlayers() + amount <= server.getMaxPlayers()) {
                        playerEntity.sendToServer(server.getName());
                        server.setOnlinePlayers(server.getOnlinePlayers() + size);
                        return;
                    }
                }
                player.sendMessage(ChatColor.RED + "There are currently no available games");
            } else {
                int row = slot / 9;
                int column = slot % 9;
                if(row > 0 && row < 5){
                    if(column > 0 && column < 8){
                        int serverID = (row - 1) * 7 + column - 1;
                        Server server = serverID < inventoryServers.size() ? inventoryServers.get(serverID) : null;
                        if(server != null){
                            playerEntity.sendToServer(server.getName());
                            Bukkit.getScheduler().runTask(Warfare.getInstance(), player::closeInventory);
                        }
                    }
                }
            }
        }
    }

    public abstract class PlayersEntity{
        private int ticks = 0;
        protected UUID player;

        public PlayersEntity(UUID player) {
            this.player = player;
        }

        public boolean hasFailed(){
            return Bukkit.getPlayer(player) == null;
        }

        public int ticks(){
            return ticks++;
        }

        public UUID getPlayerUUID(){
            return player;
        }

        public Player getPlayer(){
            return Bukkit.getPlayer(player);
        }

        public abstract void sendToServer(String servername);
        public abstract int size();
    }

    public class SinglePlayerEntity extends PlayersEntity{
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

    public class PartyPlayerEntity extends PlayersEntity{
        public PartyPlayerEntity(UUID partyLeader) {
            super(partyLeader);
        }

        public boolean hasFailed(){
            return Bukkit.getPlayer(player) == null || Warfare.getInstance().getPartyManager().getParty(player) == null;
        }

        public int size(){
            return Warfare.getInstance().getPartyManager().getParty(player).getMembers().size();
        }

        public void sendToServer(String servername) {
            Utils.sendPartyToServer(Bukkit.getPlayer(player), Warfare.getInstance().getPartyManager().getParty(player), servername);
        }
    }
}