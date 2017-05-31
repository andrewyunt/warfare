package com.andrewyunt.warfare.listeners;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.configuration.StaticConfiguration;
import com.andrewyunt.warfare.game.Game;
import com.andrewyunt.warfare.player.GamePlayer;
import com.andrewyunt.warfare.player.Kit;
import com.andrewyunt.warfare.player.events.UpdateHotbarEvent;
import com.andrewyunt.warfare.utilities.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
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
    public void onPlayerPreJoin(AsyncPlayerPreLoginEvent event){
        Game game = Warfare.getInstance().getGame();

        if (Warfare.getInstance().getGame().isEdit()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "The map is currently in edit mode.");
        } else if((game.getStage() == Game.Stage.WAITING || game.getStage() == Game.Stage.COUNTDOWN) && game.getAvailableCages().size() <= 0) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Server is currently full");
        } else if(game.getStage() == Game.Stage.RESTART) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "The warfare server server is currently restarting");
        }
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);

        Player player = event.getPlayer();

        // Update server status
        Warfare.getInstance().getStorageManager().updateServerStatusAsync();

        GamePlayer gp = Warfare.getInstance().getPlayerManager().getPlayer(player);

        BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(Warfare.getInstance(), () -> {
            Game game = Warfare.getInstance().getGame();
            if (Warfare.getInstance().getGame().isEdit()) {
                player.kickPlayer(ChatColor.RED + "The map is currently in edit mode.");
                return;
            }
            if (game.getStage() == Game.Stage.WAITING) {
                game.addPlayer(gp);
            } else if(game.getStage() == Game.Stage.COUNTDOWN) {
                if (game.getAvailableCages().size() > 0) {
                    game.addPlayer(gp);
                } else{
                    player.kickPlayer(ChatColor.RED + "This game has already started.");
                }
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
        Warfare.getInstance().getStorageManager().updateServerStatusAsync();
    }

    @EventHandler
    private void onUpdateHotbar(UpdateHotbarEvent event) {
        GamePlayer gamePlayer = event.getGamePlayer();
        PlayerInventory inv = gamePlayer.getBukkitPlayer().getInventory();
        inv.clear();

        if (gamePlayer.isSpectating()) {
            ItemStack teleporter = new ItemStack(Material.COMPASS, 1);
            ItemMeta teleporterMeta = teleporter.getItemMeta();
            teleporterMeta.setDisplayName(Utils.formatMessage(StaticConfiguration.SPECTATOR_TELEPORTER_TITLE));
            teleporter.setItemMeta(teleporterMeta);
            inv.setItem(StaticConfiguration.SPECTATOR_TELEPORTER_SLOT - 1, teleporter);

            ItemStack bed = new ItemStack(Material.BED, 1);
            ItemMeta bedMeta = bed.getItemMeta();
            bedMeta.setDisplayName(Utils.formatMessage(StaticConfiguration.SPECTATOR_RETURN_TO_LOBBY_TITLE));
            bed.setItemMeta(bedMeta);
            inv.setItem(StaticConfiguration.SPECTATOR_RETURN_TO_LOBBY_SLOT - 1, bed);
        } else {
            ItemStack kitSelector = new ItemStack(Material.ENDER_CHEST, 1);
            ItemMeta kitSelectorMeta = kitSelector.getItemMeta();
            kitSelectorMeta.setDisplayName(Utils.formatMessage(StaticConfiguration.CAGE_KIT_SELECTOR_TITLE));
            kitSelector.setItemMeta(kitSelectorMeta);
            inv.setItem(StaticConfiguration.CAGE_KIT_SELECTOR_SLOT - 1, kitSelector);

            ItemStack powerupSelector = new ItemStack(Material.CHEST, 1);
            ItemMeta powerupSelectorMeta = powerupSelector.getItemMeta();
            powerupSelectorMeta.setDisplayName(Utils.formatMessage(StaticConfiguration.CAGE_POWERUP_SELECTOR_TITLE));
            powerupSelector.setItemMeta(powerupSelectorMeta);
            inv.setItem(StaticConfiguration.CAGE_POWERUP_SELECTOR_SLOT - 1, powerupSelector);

            ItemStack bed = new ItemStack(Material.BED, 1);
            ItemMeta bedMeta = bed.getItemMeta();
            bedMeta.setDisplayName(Utils.formatMessage(StaticConfiguration.CAGE_RETURN_TO_LOBBY_TITLE));
            bed.setItemMeta(bedMeta);
            inv.setItem(StaticConfiguration.CAGE_RETURN_TO_LOBBY_SLOT - 1, bed);
        }

        gamePlayer.getBukkitPlayer().updateInventory();
    }

    @Override
    protected boolean handleHotbarClick(Player player, String itemName) {
        GamePlayer gp = Warfare.getInstance().getPlayerManager().getPlayer(player);
        if (gp.isCaged()) {
            if (itemName.equals(Utils.formatMessage(StaticConfiguration.CAGE_KIT_SELECTOR_TITLE))) {
                Warfare.getInstance().getKitSelectorMenu().open(gp);
                return true;
            } else if (itemName.equals(Utils.formatMessage(StaticConfiguration.CAGE_POWERUP_SELECTOR_TITLE))) {
                Warfare.getInstance().getPowerupSelectorMenu().open(gp);
                return true;
            } else if (itemName.equals(Utils.formatMessage(StaticConfiguration.CAGE_RETURN_TO_LOBBY_TITLE))) {
                Utils.sendPlayerToServer(player, StaticConfiguration.getNextLobby());
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

        if (damager instanceof Player && damaged instanceof Player) {
            GamePlayer damagedGP = Warfare.getInstance().getPlayerManager().getPlayer((damaged).getUniqueId());
            GamePlayer damagerGP = Warfare.getInstance().getPlayerManager().getPlayer((damager).getUniqueId());

            if (!damagerGP.isInGame() || !damagedGP.isInGame()) {
                return;
            }

            if (damagedGP.getSide() == damagerGP.getSide()) {
                event.setCancelled(true);
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

        playerGP.setDeaths(playerGP.getDeaths() + 1);
        Warfare.getInstance().getGame().removePlayer(playerGP);

        GamePlayer lastDamager = playerGP.getLastDamager();

        if (lastDamager == null || lastDamager == playerGP || !lastDamager.isInGame()) {
            return;
        }

        lastDamager.addKill();

        int killCoins = 20 * lastDamager.getBoost();
        lastDamager.setCoins(lastDamager.getCoins() + lastDamager.getBoost());
        lastDamager.setPoints(lastDamager.getPoints() + 5);
        lastDamager.getBukkitPlayer().sendMessage(ChatColor.YELLOW + "You received " + ChatColor.GOLD
                + ChatColor.BOLD.toString() + killCoins + ChatColor.YELLOW + " coins and " + ChatColor.GOLD
                + ChatColor.BOLD.toString() + 5 + ChatColor.YELLOW + " points");

        Bukkit.getScheduler().runTask(Warfare.getInstance(), () -> {
            if(player.isOnline()){
                playerGP.setSpectating(true, true);
            }
        });
    }

    @EventHandler
    private void onDeathMessage(PlayerDeathEvent event) {
        event.setDeathMessage(null);

        String msg;
        List<String> msgList;
        Player killed = event.getEntity();
        Player killer = event.getEntity().getKiller();
        EntityDamageEvent entityDamageEvent = killed.getLastDamageCause();
        ConfigurationSection deathMessagesSection = Warfare.getInstance().getConfig()
                .getConfigurationSection("death-messages");

        if (entityDamageEvent.getCause() == DamageCause.VOID) {
            msgList = deathMessagesSection.getStringList("void");
            Collections.shuffle(msgList);
            msg = String.format(msgList.get(0), killed.getDisplayName());
        } else if (entityDamageEvent.getCause() == DamageCause.ENTITY_ATTACK) {
            if (entityDamageEvent.getEntityType() != EntityType.PLAYER || !(killer instanceof Player)
                    || !(killed instanceof Player)) {
                return;
            }

            Material tool = killer.getItemInHand().getType();

            if (tool == Material.IRON_SWORD || tool == Material.DIAMOND_SWORD || tool == Material.STONE_SWORD
                    || tool == Material.WOOD_SWORD || tool == Material.BOW) {
                msgList = deathMessagesSection.getStringList(tool.toString().toLowerCase());
            } else {
                msgList = deathMessagesSection.getStringList("melee");
            }

            Collections.shuffle(msgList);
            msg = String.format(msgList.get(0), killer.getDisplayName(), killed.getDisplayName());
        } else {
            return;
        }

        event.setDeathMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    @EventHandler
    private void onPlayerRespawn(PlayerRespawnEvent event) {
        Game game = Warfare.getInstance().getGame();

        if (game == null || game.getStage() == Game.Stage.WAITING || game.getStage() == Game.Stage.COUNTDOWN) {
            return;
        }

        GamePlayer gp = Warfare.getInstance().getPlayerManager().getPlayer(event.getPlayer());
        event.setRespawnLocation(gp.setSpectating(true, false));
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

        if (gp.isInGame() && !gp.isHasFallen()) {
            gp.setHasFallen(true);
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onSoup(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();

            if (item != null && item.getType() == Material.MUSHROOM_SOUP) {
                Player player = event.getPlayer();
                if (player.getHealth() + 6 > player.getMaxHealth()) {
                    player.setHealth(player.getMaxHealth());
                } else {
                    event.getPlayer().setHealth(event.getPlayer().getHealth() + 6L);
                }

                item.setType(Material.BOWL);
            }
        }
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        GamePlayer gamePlayer = Warfare.getInstance().getPlayerManager().getPlayer(event.getPlayer());

        if (gamePlayer.isSpectating()) {
            Location playerLoc = event.getPlayer().getLocation();
            Location centerLoc = Warfare.getInstance().getGame().getMapLocation();
            if (Math.abs(centerLoc.getX() - playerLoc.getX()) > 300 || Math.abs(centerLoc.getZ() - playerLoc.getZ()) > 300) {
                event.getPlayer().teleport(centerLoc);
            }
        }
    }

    @EventHandler
    private void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getInventory().getType() == InventoryType.CHEST) {
            if (!event.getInventory().getTitle().contains("Kit Selector") && !event.getInventory().getTitle().contains("Powerup Selector")) {
                cancelCageInteractions(event, (Player) event.getPlayer());
            }
        }
    }

    private void cancelCageInteractions(Cancellable cancellable, Player player) {
        GamePlayer gp = Warfare.getInstance().getPlayerManager().getPlayer(player);

        if (gp.isCaged()) {
            cancellable.setCancelled(true);
        }
    }
}