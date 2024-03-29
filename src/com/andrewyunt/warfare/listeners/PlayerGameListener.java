package com.andrewyunt.warfare.listeners;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.configuration.StaticConfiguration;
import com.andrewyunt.warfare.game.Cage;
import com.andrewyunt.warfare.game.Game;
import com.andrewyunt.warfare.player.GamePlayer;
import com.andrewyunt.warfare.player.Kit;
import com.andrewyunt.warfare.player.Transaction;
import com.andrewyunt.warfare.player.events.SpectateEvent;
import com.andrewyunt.warfare.player.events.UpdateHotbarEvent;
import com.andrewyunt.warfare.utilities.Utils;
import org.bukkit.*;
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
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerGameListener extends PlayerListener {

    @EventHandler
    public void onPlayerPreJoin(AsyncPlayerPreLoginEvent event) {
        Game game = Warfare.getInstance().getGame();

        if (Warfare.getInstance().getGame().isEdit()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "The map is currently in edit mode.");
        } else if ((game.getStage() == Game.Stage.WAITING || game.getStage() == Game.Stage.COUNTDOWN) && (game.getCages().size() != 0 && game.getAvailableCages().isEmpty())) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Server is currently full");
        } else if (game.getStage() == Game.Stage.RESTART) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "The warfare server server is currently restarting");
        }
    }

    @Override
    protected void playerJoin(GamePlayer player) {
        // Update server status
        Warfare.getInstance().getStorageManager().updateServerStatusAsync();

        // Add player to the game
        Game game = Warfare.getInstance().getGame();

        if (game.getStage() == Game.Stage.WAITING) {
            Warfare.getInstance().getGame().addPlayer(player);
        } else if (game.getStage() == Game.Stage.COUNTDOWN && game.getAvailableCages().size() > 0 && !game.isTeams()) {
            Warfare.getInstance().getGame().addPlayer(player);
        }

        // Register health objective for game servers
        if (!StaticConfiguration.LOBBY) {
			Objective healthObjective = Warfare.getInstance().getScoreboardHandler().getPlayerBoard(player.getUUID()).getScoreboard()
					.registerNewObjective(ChatColor.RED + "❤", "health");
			healthObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
		}
    }

    @EventHandler
    public void onPlayerJoinCage(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = Warfare.getInstance().getPlayerManager().getPlayer(player);
        Cage cage = gamePlayer.getCage();
        if (cage != null) {
            player.teleport(cage.getLocation());
        }
    }

    @EventHandler
    public void onPlayerSpawnLocationEvent(PlayerSpawnLocationEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = Warfare.getInstance().getPlayerManager().getPlayer(player);
        Game game = Warfare.getInstance().getGame();
        Location spawnAt;

        if (game.getStage() != Game.Stage.WAITING && (game.getStage() != Game.Stage.COUNTDOWN && !game.isTeams())) {
            spawnAt = Warfare.getInstance().getGame().getMapLocation();
            BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
            scheduler.scheduleSyncDelayedTask(Warfare.getInstance(), () -> {
                gamePlayer.setSpectating(true);
                Bukkit.getServer().getPluginManager().callEvent(new UpdateHotbarEvent(gamePlayer));
            }, 1L);
        } else if (game.isTeams()) {
            if (game.getStage() == Game.Stage.WAITING) {
                spawnAt = game.getWaitingLocation();
            } else {
                spawnAt = game.getMapLocation();
            }
        } else {
            spawnAt = game.getAvailableCages().iterator().next().setPlayer(gamePlayer);

            spawnAt.setX(spawnAt.getBlockX() + 0.5);
            spawnAt.setY(spawnAt.getBlockY() + 2);
            spawnAt.setZ(spawnAt.getBlockZ() + 0.5);

            org.bukkit.util.Vector vector = Warfare.getInstance().getGame().getMapLocation().toVector().subtract(spawnAt.toVector()).normalize();
            vector.setY(0.5);

            spawnAt.setDirection(vector);
            spawnAt.setPitch(0);
        }

        if (spawnAt != null) {
            spawnAt = spawnAt.clone();

            Chunk chunk = spawnAt.getChunk();

            if (!chunk.isLoaded()) {
                chunk.load();
            }

            event.setSpawnLocation(spawnAt);

            final Location finalSpawnAt = spawnAt;

            BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
            scheduler.scheduleSyncDelayedTask(Warfare.getInstance(), () -> player.teleport(finalSpawnAt), 5L);
        }
    }

    @Override
    protected void playerQuit(GamePlayer player) {
        Warfare.getInstance().getGame().removePlayer(player);
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
        if (Warfare.getInstance().getGame().getStage() == Game.Stage.WAITING) {
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

            if (Warfare.getInstance().getGame().getStage() == Game.Stage.WAITING) {
                event.setCancelled(true);
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
        cancelWaitingInteractions(event);
    }

    @EventHandler
    private void onBlockPlace(BlockPlaceEvent event) {
        cancelWaitingInteractions(event);
    }

    @EventHandler
    private void onPlayerDropItem(PlayerDropItemEvent event) {
        cancelWaitingInteractions(event);
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        GamePlayer gp = Warfare.getInstance().getPlayerManager().getPlayer(player.getName());

        if (!gp.isInGame()) {
            return;
        }

        for (ItemStack is : event.getDrops()) {
            player.getLocation().getWorld().dropItemNaturally(player.getLocation(), is);
        }

        event.getDrops().clear();
        event.setDroppedExp(0);

        gp.setLives(gp.getLives() - 1);
        gp.setKillStreak(0);

        BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(Warfare.getInstance(), () -> player.spigot().respawn(), 20L);

        if (gp.getLives() == 0) {
            gp.setSpectating(true);
            scheduler.scheduleSyncDelayedTask(Warfare.getInstance(), () -> Warfare.getInstance().getGame().removePlayer(gp), 100L);
        }

        gp.setDeaths(gp.getDeaths() + 1);

        // Give last damager coins and kills
        GamePlayer lastDamager = gp.getLastDamager();

        if (lastDamager == null || lastDamager == gp || !lastDamager.isInGame()) {
            return;
        }

        lastDamager.addKill();

        int killCoins = 30;
        List<String> groups = Arrays.asList(Warfare.getPermission().getPlayerGroups(lastDamager.getBukkitPlayer()))
                .stream().map(String::toLowerCase).collect(Collectors.toList());

        if (groups.contains("platinum")) {
            killCoins = 100;
        } else if (groups.contains("sapphire")) {
            killCoins = 90;
        }  else if (groups.contains("ruby")) {
            killCoins = 80;
        } else if (groups.contains("emerald")) {
            killCoins = 70;
        } else if (groups.contains("diamond")) {
            killCoins = 60;
        } else if (groups.contains("gold")) {
            killCoins = 50;
        } else if (groups.contains("iron")) {
            killCoins = 40;
        }

        String transactionMessage = ChatColor.YELLOW + "You received " + ChatColor.GOLD
                + ChatColor.BOLD.toString() + killCoins + ChatColor.YELLOW + " coins and " + ChatColor.GOLD
                + ChatColor.BOLD.toString() + 5 + ChatColor.YELLOW + " points";
        Warfare.getInstance().getStorageManager().savePendingTransaction(new Transaction(lastDamager.getUUID(),
                transactionMessage, killCoins * lastDamager.getBoost(), 5));
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
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        GamePlayer gp = Warfare.getInstance().getPlayerManager().getPlayer(player);
        Game game = Warfare.getInstance().getGame();
        Location respawnAt;

        if (gp.isSpectating()) {
            respawnAt = game.getMapLocation();
            BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
            scheduler.scheduleSyncDelayedTask(Warfare.getInstance(), () -> Bukkit.getServer().getPluginManager().callEvent(new UpdateHotbarEvent(gp)), 1L);
        } else {
            respawnAt = game.getTeamSpawns().get(gp.getSide().getSideNum());

            respawnAt.setX(respawnAt.getBlockX() + 0.5);
            respawnAt.setY(respawnAt.getBlockY() + 1);
            respawnAt.setZ(respawnAt.getBlockZ() + 0.5);

            org.bukkit.util.Vector vector = Warfare.getInstance().getGame().getMapLocation().toVector().subtract(respawnAt.toVector()).normalize();
            vector.setY(0.5);

            respawnAt.setDirection(vector);
            respawnAt.setPitch(0);
        }

        respawnAt = respawnAt.clone();

        Chunk chunk = respawnAt.getChunk();

        if (!chunk.isLoaded()) {
            chunk.load();
        }

        event.setRespawnLocation(respawnAt);
    }

    @EventHandler
    private void onSpectate(SpectateEvent event) {
        Player player = event.getGamePlayer().getBukkitPlayer();
        player.setGameMode(GameMode.CREATIVE);
        player.setFireTicks(0);

        for (Player other: Bukkit.getOnlinePlayers()) {
            if (other != player) {
                GamePlayer gamePlayer = Warfare.getInstance().getPlayerManager().getPlayer(other);
                if (gamePlayer.isSpectating()) {
                    other.showPlayer(player);
                    player.showPlayer(other);
                } else {
                    other.hidePlayer(player);
                    player.showPlayer(other);
                }
            }
        }
        player.spigot().setCollidesWithEntities(false);
        player.spigot().setViewDistance(4);
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
                if (player.getHealth() < player.getMaxHealth()) {
                    if (player.getHealth() + 6 > player.getMaxHealth()) {
                        player.setHealth(player.getMaxHealth());
                    } else {
                        event.getPlayer().setHealth(event.getPlayer().getHealth() + 6L);
                    }

                    item.setType(Material.BOWL);
                }
            }
        }
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        GamePlayer gamePlayer = Warfare.getInstance().getPlayerManager().getPlayer(event.getPlayer());

        if (gamePlayer.isSpectating()) {
            Location playerLoc = event.getPlayer().getLocation();
            Location centerLoc = Warfare.getInstance().getGame().getMapLocation();

            if (playerLoc != null && centerLoc != null) {
                if (Math.abs(centerLoc.getX() - playerLoc.getX()) > 300 || Math.abs(centerLoc.getZ() - playerLoc.getZ()) > 300) {
                    event.getPlayer().teleport(centerLoc);
                }
            }
        }
    }

    @EventHandler
    private void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getInventory().getType() == InventoryType.CHEST) {
            if (!event.getInventory().getTitle().contains("Kit Selector") && !event.getInventory().getTitle().contains("Powerup Selector")) {
                cancelWaitingInteractions(event);
            }
        }
    }

    private void cancelWaitingInteractions(Cancellable cancellable) {
        if (Warfare.getInstance().getGame().getStage() == Game.Stage.WAITING || Warfare.getInstance().getGame().getStage() == Game.Stage.COUNTDOWN) {
            cancellable.setCancelled(true);
        }
    }
}