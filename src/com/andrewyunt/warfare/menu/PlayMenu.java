package com.andrewyunt.warfare.menu;

import com.andrewyunt.warfare.configuration.StaticConfiguration;
import com.andrewyunt.warfare.objects.Game;
import com.andrewyunt.warfare.utilities.Utils;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
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

import java.util.*;

public class PlayMenu implements Listener {

    private final ItemStack glassPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);

    public PlayMenu() {

        ItemMeta glassPaneMeta = glassPane.getItemMeta();
        glassPaneMeta.setDisplayName("  ");
        glassPaneMeta.setLore(new ArrayList<String>());
        glassPane.setItemMeta(glassPaneMeta);
    }

    public void open(GamePlayer player) {

        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.RED + ChatColor.BOLD.toString() + "Join Game");

        for (int i = 0; i < 9; i++)
            inv.setItem(i, glassPane);

        for (int i = 9; i < 45; i = i + 9) {
            inv.setItem(i, glassPane);
            inv.setItem(i + 8, glassPane);
        }

        for (int i = 45; i < 54; i++)
            inv.setItem(i, glassPane);

        ItemStack quickJoin = new ItemStack(Material.IRON_SWORD);
        ItemMeta quickJoinMeta = quickJoin.getItemMeta();
        quickJoinMeta.setDisplayName(ChatColor.GOLD + "Quick Join");
        quickJoin.setItemMeta(quickJoinMeta);
        inv.setItem(49, quickJoin);

        List<ItemStack> toAdd = new ArrayList<ItemStack>();

        // There are separate loops for adding join and spectate items because we want to add spectate items last
        for (Map.Entry<String, Map.Entry<Game.Stage, Integer>> entry : Warfare.getInstance().getMySQLManager().getServers().entrySet()) {
            if (entry.getValue().getKey() != Game.Stage.WAITING)
                continue;

            ItemStack join = new ItemStack(Material.STAINED_CLAY, 1, (short) 5);
            ItemMeta joinMeta = join.getItemMeta();
            joinMeta.setDisplayName(ChatColor.GOLD + entry.getKey());
            List<String> lore = new ArrayList<String>();
            lore.add(ChatColor.YELLOW + "Waiting for players...");
            joinMeta.setLore(lore);
            join.setItemMeta(joinMeta);

            toAdd.add(join);
        }

        for (Map.Entry<String, Map.Entry<Game.Stage, Integer>> entry : Warfare.getInstance().getMySQLManager().getServers().entrySet()) {
            if (entry.getValue().getKey() != Game.Stage.COUNTDOWN || entry.getValue().getKey() != Game.Stage.BATTLE)
                continue;

            ItemStack spectate = new ItemStack(Material.STAINED_CLAY, 1, (short) 4);
            ItemMeta spectateMeta = spectate.getItemMeta();
            spectateMeta.setDisplayName(ChatColor.GOLD + entry.getKey());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.YELLOW + "Spectate");
            spectateMeta.setLore(lore);
            spectate.setItemMeta(spectateMeta);

            toAdd.add(spectate);
        }

        for (int i = 0; i < 45; i++) {
            ItemStack is = inv.getItem(i);

            if (is == null || is.getType() == Material.AIR)
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

        if (inv == null)
            return;

        String title = inv.getTitle();

        if (title == null)
            return;

        if (!title.equals(ChatColor.RED + ChatColor.BOLD.toString() + "Join Game"))
            return;

        event.setCancelled(true);

        ItemStack is = event.getCurrentItem();

        if (is.getType() == Material.STAINED_GLASS_PANE)
            return;

        if(is.getType() == Material.AIR)
            return;

        if (!is.hasItemMeta())
            return;

        String name = is.getItemMeta().getDisplayName();

        if (name == null)
            return;

        Player player = (Player) event.getWhoClicked();
        String sendServer = null;

        if (name.equals(ChatColor.GOLD + "Quick Join")) {
            Map<String, Map.Entry<Game.Stage, Integer>> servers = Warfare.getInstance().getMySQLManager().getServers();
            Map<String, Integer> playableServers = new HashMap<String, Integer>();

            for (Map.Entry<String, Map.Entry<Game.Stage, Integer>> entry : servers.entrySet())
                if (entry.getValue().getKey() == Game.Stage.WAITING)
                    playableServers.put(entry.getKey(), entry.getValue().getValue());

            if (playableServers.size() == 0)  {
                player.sendMessage(ChatColor.RED + "There are no available servers at the moment.");
                return;
            }

            String mostPlayers = null;
            int mostPlayersCount = 0;

            for (Map.Entry<String, Integer> entry : playableServers.entrySet())
                if (entry.getValue() >= mostPlayersCount)
                    mostPlayers = entry.getKey();

            sendServer = mostPlayers;
        } else {
            sendServer = ChatColor.stripColor(name);
        }

        Utils.sendPlayerToServer(player, sendServer);

        UUID uuid = player.getUniqueId();

        if (Warfare.getInstance().getPartyManager().getParty(uuid).getLeader() == uuid) {
            for(String server : StaticConfiguration.LOBBY_SERVERS) {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Forward");
                out.writeUTF(server);
                out.writeUTF("MOVEPARTY " + uuid + " " + sendServer);
                player.sendPluginMessage(Warfare.getInstance(), "BungeeCord", out.toByteArray());
            }
        }
    }
}