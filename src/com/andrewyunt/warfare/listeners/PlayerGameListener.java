package com.andrewyunt.warfare.listeners;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.configuration.StaticConfiguration;
import com.andrewyunt.warfare.menu.ClassSelectorMenu;
import com.andrewyunt.warfare.objects.Game;
import com.andrewyunt.warfare.objects.GamePlayer;
import com.andrewyunt.warfare.objects.Kit;
import com.andrewyunt.warfare.utilities.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PlayerGameListener extends PlayerListener {

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);

        Player player = event.getPlayer();

        // Update server status
        Warfare.getInstance().getMySQLManager().updateServerStatus();

        GamePlayer gp = Warfare.getInstance().getPlayerManager().getPlayer(player);

        BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(Warfare.getInstance(), () -> {
            Game game = Warfare.getInstance().getGame();

            if (Warfare.getInstance().getArena().isEdit()) {
                player.kickPlayer(ChatColor.RED + "The map is currently in edit mode.");
                return;
            }

            if (game.getStage() == Game.Stage.WAITING) {
                game.addPlayer(gp);
            } else if (game.getStage() == Game.Stage.END) {
                player.kickPlayer("You may not join once the game has ended.");
            } else if (game.getStage() == Game.Stage.RESTART) {
                player.kickPlayer("You may not join during a restart.");
            } else {
                if (!player.hasPermission("warfare.spectatorjoin")) {
                    player.kickPlayer(ChatColor.RED + "You do not have permission join to spectate games.");
                } else {
                    gp.setSpectating(true, false);
                }
            }
        }, 1);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {

        event.setQuitMessage(null);

        Player player = event.getPlayer();
        GamePlayer gp = Warfare.getInstance().getPlayerManager().getPlayer(player);

        Warfare.getInstance().getGame().removePlayer(gp);
        Warfare.getInstance().getMySQLManager().updateServerStatus();
    }

    @Override
    protected boolean handleHotbarClick(Player player, String itemName) {
        GamePlayer gp = Warfare.getInstance().getPlayerManager().getPlayer(player);

        if (gp.isCaged()) {
            if (itemName.equals(Utils.formatMessage(StaticConfiguration.CAGE_CLASS_SELECTOR_TITLE))) {
                Warfare.getInstance().getClassSelectorMenu().open(ClassSelectorMenu.Type.KIT, gp);
                return true;
            }
        } else if (gp.isSpectating()) {
            if (itemName.equals(Utils.formatMessage(StaticConfiguration.SPECTATOR_RETURN_TO_LOBBY_TITLE))) {
                Utils.sendPlayerToServer(player, StaticConfiguration.getNextLobby());
                return true;
            } else if (itemName.equals(Utils.formatMessage(StaticConfiguration.SPECTATOR_TELEPORTER_TITLE))) {
                Warfare.getInstance().getTeleporterMenu().open(gp);
                return true;
            }
        }

        return false;
    }

    @EventHandler (priority = EventPriority.LOWEST)
    private void onPlayerDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity damaged = event.getEntity();

        if(damager instanceof Player && damaged instanceof Player) {
            GamePlayer damagedGP = Warfare.getInstance().getPlayerManager().getPlayer((damaged).getUniqueId());
            GamePlayer damagerGP = Warfare.getInstance().getPlayerManager().getPlayer((damager).getUniqueId());

            if (!damagerGP.isInGame() || !damagedGP.isInGame()) {
                return;
            }

            damagedGP.setLastDamager(damagerGP);

            if (damagerGP.getSelectedKit() == Kit.SOUP) {
                damagedGP.getBukkitPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 1));
                damagerGP.getBukkitPlayer().sendMessage(ChatColor.YELLOW + String.format("You inflicted slowness II on %s for 10 seconds", damagedGP.getBukkitPlayer().getDisplayName()));
            }
        }
    }

    @EventHandler
    private void onBlockBreak(BlockBreakEvent event) {

        cancelCageInteractions(event, event.getPlayer());
    }

    @EventHandler
    private void onBlockPlace(BlockPlaceEvent event) {

        cancelCageInteractions(event, event.getPlayer());
    }

    @EventHandler
    private void onPlayerDropItem(PlayerDropItemEvent event) {

        GamePlayer gp = Warfare.getInstance().getPlayerManager().getPlayer(event.getPlayer());

        if (gp.isCaged()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent event) {

        event.setDroppedExp(0);

        Player player = event.getEntity();
        GamePlayer playerGP = Warfare.getInstance().getPlayerManager().getPlayer(player.getName());

        if (!playerGP.isInGame()) {
            return;
        }

        Warfare.getInstance().getGame().removePlayer(playerGP);

        GamePlayer lastDamager = playerGP.getLastDamager();

        if (lastDamager == null) {
            return;
        }

        if (lastDamager == playerGP) {
            return;
        }

        if (!(lastDamager.isInGame())) {
            return;
        }

        lastDamager.addKill();

        Player lastDamagerBP = lastDamager.getBukkitPlayer();
        int killCoins = 20;

        if (lastDamagerBP.hasPermission("megatw.coins.double")) {
            killCoins = 40;
        }

        if (lastDamagerBP.hasPermission("megatw.coins.triple")) {
            killCoins = 60;
        }

        lastDamager.setCoins(lastDamager.getCoins() + killCoins);

        lastDamagerBP.sendMessage(ChatColor.GOLD + String.format("You killed %s and received %s coins.",
                playerGP.getBukkitPlayer().getDisplayName(), String.valueOf(killCoins)));
    }

    @EventHandler
    private void onDeathMessage(PlayerDeathEvent event) {

        event.setDeathMessage(null);

        Player killed = event.getEntity();
        Player killer = event.getEntity().getKiller();
        EntityDamageEvent entityDamageEvent = killed.getLastDamageCause();

        if (entityDamageEvent.getEntityType() == null) {
            return;
        }

        String msg = null;

        if (entityDamageEvent.getEntityType() != EntityType.PLAYER) {
            return;
        }

        if (!(killer instanceof Player) || !(killed instanceof Player)) {
            return;
        }

        ConfigurationSection deathMessagesSection = Warfare.getInstance().getConfig()
                .getConfigurationSection("death-messages");
        Material tool = killer.getItemInHand().getType();
        List<String> msgList;

        if (tool == Material.IRON_SWORD || tool == Material.DIAMOND_SWORD || tool == Material.STONE_SWORD
                || tool == Material.BOW) {
            msgList = deathMessagesSection.getStringList(tool.toString().toLowerCase());
        } else {
            msgList = deathMessagesSection.getStringList("melee");
        }

        Collections.shuffle(msgList);
        msg = String.format(msgList.get(0), killer.getDisplayName(), killed.getDisplayName());

        event.setDeathMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    @EventHandler
    private void onPlayerRespawn(PlayerRespawnEvent event) {

        Game game = Warfare.getInstance().getGame();

        if (game == null || game.getStage() == Game.Stage.WAITING || game.getStage() == Game.Stage.COUNTDOWN) {
            return;
        }

        GamePlayer gp = Warfare.getInstance().getPlayerManager().getPlayer(event.getPlayer());

        event.setRespawnLocation(gp.setSpectating(true, true));
    }

    @EventHandler
    private void onPrepareItemEnchant(PrepareItemEnchantEvent event) {

        for (HumanEntity he : event.getViewers()) {
            ((Player) he).setLevel(100);
        }
    }

    @EventHandler
    private void onEnchantItem(EnchantItemEvent event) {

        Map<Enchantment, Integer> enchants = event.getEnchantsToAdd();

        enchants.clear();

        Material type = event.getItem().getType();

        if (type == Material.IRON_HELMET || type == Material.IRON_CHESTPLATE || type == Material.IRON_LEGGINGS
                || type == Material.IRON_BOOTS || type == Material.DIAMOND_HELMET || type == Material.DIAMOND_CHESTPLATE
                || type == Material.DIAMOND_LEGGINGS || type == Material.DIAMOND_BOOTS) {
            enchants.put(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        } else if (type == Material.WOOD_SWORD || type == Material.STONE_SWORD || type == Material.IRON_SWORD
                || type == Material.DIAMOND_SWORD) {
            enchants.put(Enchantment.DAMAGE_ALL, 1);
        } else if (type == Material.BOW) {
            enchants.put(Enchantment.ARROW_DAMAGE, 1);
        }

        event.setExpLevelCost(0);
    }

    @EventHandler
    private void onFoodLevelChange(FoodLevelChangeEvent event) {

        GamePlayer player = Warfare.getInstance().getPlayerManager().getPlayer(event.getEntity().getName());

        Game.Stage stage = Warfare.getInstance().getGame().getStage();

        if (stage == Game.Stage.WAITING || stage == Game.Stage.COUNTDOWN) {
            event.setCancelled(true);
        } else if (player.isSpectating()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onProjectileLaunch(ProjectileLaunchEvent event) {

        if (event.getEntityType() != EntityType.ARROW) {
            return;
        }

        Projectile arrow = event.getEntity();
        ProjectileSource ps = arrow.getShooter();

        if (!(ps instanceof Player)) {
            return;
        }

        ItemStack itemInHand = ((Player) ps).getItemInHand();

        if (itemInHand == null || !itemInHand.hasItemMeta()) {
            return;
        }

        ItemMeta itemInHandMeta = itemInHand.getItemMeta();

        if (!itemInHandMeta.hasDisplayName()) {
            return;
        }

        if (itemInHandMeta.getDisplayName().equals("Booster Bow")) {
            arrow.remove();
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    private void onPlayerFall(EntityDamageEvent event) {

        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        GamePlayer gp = Warfare.getInstance().getPlayerManager().getPlayer(((Player) event.getEntity()).getName());

        if (gp.isInGame() && !gp.hasFallen()) {
            gp.setHasFallen(true);
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getInventory().getType() == InventoryType.PLAYER) {
            return;
        }
        if (!event.getInventory().getTitle().contains("Class Selector")) {
            cancelCageInteractions(event, (Player) event.getPlayer());
        }
    }

    private void cancelCageInteractions(Cancellable cancellable, Player player) {

        GamePlayer gp = Warfare.getInstance().getPlayerManager().getPlayer(player);

        if (gp.isCaged()) {
            cancellable.setCancelled(true);
        }
    }
}