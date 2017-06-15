package com.andrewyunt.warfare.listeners;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.configuration.StaticConfiguration;
import com.andrewyunt.warfare.game.Cage;
import com.andrewyunt.warfare.game.Game;
import com.andrewyunt.warfare.game.Side;
import com.andrewyunt.warfare.game.events.*;
import com.andrewyunt.warfare.player.GamePlayer;
import com.andrewyunt.warfare.player.Party;
import com.andrewyunt.warfare.player.events.UpdateHotbarEvent;
import com.andrewyunt.warfare.purchases.HealthBoost;
import com.andrewyunt.warfare.purchases.Purchasable;
import com.andrewyunt.warfare.utilities.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;
import java.util.stream.Collectors;

public class GameListener implements Listener {

    @EventHandler
    public void onAddPlayer(AddPlayerEvent event) {
        GamePlayer gamePlayer = event.getGamePlayer();
        Player player = gamePlayer.getBukkitPlayer();
        Game game = Warfare.getInstance().getGame();

        gamePlayer.setHasPlayed(true);

        // Set player's mode to survival
        player.setGameMode(GameMode.SURVIVAL);

        // Set the player's lives
        if (game.isTeams()) {
            gamePlayer.setLives(3);
        } else {
            gamePlayer.setLives(1);
        }

        // Call hotbar update event
        Bukkit.getServer().getPluginManager().callEvent(new UpdateHotbarEvent(gamePlayer));

        // Set player's side
        if (game.isTeams()) {
            Side leastPlayers = null;
            for (Side side : game.getSides()) {
                if (leastPlayers == null) {
                    leastPlayers = side;
                } else if (side.getPlayers().size() < leastPlayers.getPlayers().size()) {
                    leastPlayers = side;
                }
            }

            if (leastPlayers.getPlayers().size() == 0) {
                leastPlayers.setName(player.getDisplayName());
            }

            gamePlayer.setSide(leastPlayers);

            if (game.getPlayers().size() == game.getTeamSize()) {
                game.setStage(Game.Stage.COUNTDOWN);
            }
        } else {
            gamePlayer.setSide(new Side(0, player.getDisplayName()));

            if (game.getAvailableCages().size() <= 2) {
                game.setStage(Game.Stage.COUNTDOWN);
            }
        }

        // Send the join message to the players
        Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', String.format(
                "&6%s &ehas joined &7(&6%s&7/&6%s&7)!",
                player.getDisplayName(),
                game.getPlayers().size(),
                game.isTeams() ? game.getTeamSize() * 2 : game.getCages().size())));
    }

    @EventHandler
    public void onRemovePlayer(RemovePlayerEvent event) {
        GamePlayer gamePlayer = event.getGamePlayer();
        Game game = Warfare.getInstance().getGame();

        if (game.getStage() == Game.Stage.WAITING) {
            if (gamePlayer.isCaged()) {
                gamePlayer.getCage().setPlayer(null);
            }

            Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                    String.format("&6%s &ehas left the game!", gamePlayer.getBukkitPlayer().getDisplayName())));
        } else {
            game.checkPlayers();
        }
    }

    @EventHandler
    public void onStageChange(StageChangeEvent event) {
        Game game = Warfare.getInstance().getGame();

        switch (game.getStage()) {
            case COUNTDOWN:
                game.runCountdownTimer();
                break;
            case BATTLE:
                Bukkit.getServer().getPluginManager().callEvent(new StartEvent());
                break;
            case END:
                Bukkit.getServer().getPluginManager().callEvent(new EndEvent());
                break;
            case RESTART:
                Bukkit.getServer().getPluginManager().callEvent(new RestartEvent());
                break;
        }
    }

    @EventHandler
    public void onStart(StartEvent event) {
        Game game = Warfare.getInstance().getGame();

        for (Entity entity : Warfare.getInstance().getGame().getMapLocation().getWorld().getEntities()) {
            if (entity.getType() != EntityType.PLAYER) {
                entity.remove();
            }
        }

        // Destroy cages if the game is solo
        if (!game.isTeams()) {
            for (Cage cage : game.getCages()) {
                cage.destroy();
            }
        }

        for (GamePlayer player : game.getPlayers()) {
            Player bp = player.getBukkitPlayer();

            // Teleport player to team spawn location if the game is teams
            if (game.isTeams()) {
                Location loc = game.getTeamSpawns().get(player.getSide().getSideNum());

                loc.setX(loc.getBlockX() + 0.5);
                loc.setY(loc.getBlockY() + 1);
                loc.setZ(loc.getBlockZ() + 0.5);

                org.bukkit.util.Vector vector = Warfare.getInstance().getGame().getMapLocation().toVector().subtract(loc.toVector()).normalize();
                vector.setY(0.5);

                loc.setDirection(vector);
                loc.setPitch(0);

                bp.teleport(loc);
            }

            // Update player's name color
            Utils.colorPlayerName(player, Warfare.getInstance().getGame().getPlayers());

            bp.setWalkSpeed(bp.getWalkSpeed() * 1.25f);

            // Clear player's inventory to remove class selector
            bp.getInventory().clear();

            // Give player kit items
            player.getSelectedKitOrPot().giveItems(player);

            // Close player's inventory to keep them from using the class selector in-game
            bp.closeInventory();

            // Set player's health
            Set<Purchasable> purchases = player.getPurchases().keySet();
            double health = 24;

            if (purchases.contains(HealthBoost.HEALTH_BOOST_I)) {
                health = 26;
            } else if (purchases.contains(HealthBoost.HEALTH_BOOST_II)) {
                health = 28;
            } else if (purchases.contains(HealthBoost.HEALTH_BOOST_III)) {
                health = 30;
            } else if (purchases.contains(HealthBoost.HEALTH_BOOST_IV)) {
                health = 32;
            } else if (purchases.contains(HealthBoost.HEALTH_BOOST_V)) {
                health = 34;
            }

            bp.setMaxHealth(health);
            bp.setHealth(health);

            Bukkit.getScheduler().runTaskLater(Warfare.getInstance(), () -> bp.setWalkSpeed(bp.getWalkSpeed() / 1.25f), 20 * 10);
        }

        // Fill chests
        game.fillChests();

        // Start game timer
        game.runGameTimer();
    }

    @EventHandler
    public void onEnd(EndEvent event) {
        Game game = Warfare.getInstance().getGame();
        Collection<GamePlayer> players = Warfare.getInstance().getPlayerManager().getPlayers();
        GamePlayer winningPlayer = players.stream().filter(GamePlayer::isInGame).iterator().next();
        final Side winningSide = winningPlayer.getSide();

        for (GamePlayer winner : winningSide.getPlayers()) {
            int winCoins = 1000 * winner.getBoost();

            winner.setCoins(winner.getCoins() + winCoins);
            winner.addPoints(winner.getPoints() + 30);
            winner.setWins(winner.getWins() + 1);

            if (!winner.getBukkitPlayer().isOnline()) {
                continue;
            }

            winner.getBukkitPlayer().sendMessage(ChatColor.YELLOW + String.format(
                    "You earned " + ChatColor.GOLD + "%s" + ChatColor.YELLOW + " coins for winning the game.",
                    String.valueOf(winCoins)));
        }

        Set<GamePlayer> losers = Warfare.getInstance().getPlayerManager().getPlayers().stream()
                .filter(player -> player.getSide() != winningSide).collect(Collectors.toSet());

        for (GamePlayer loser : losers) {
            loser.setLosses(loser.getLosses() + 1);
        }

        for (GamePlayer player : players) {
            player.setGamesPlayed(player.getGamesPlayed() + 1);
        }

        Bukkit.broadcastMessage(ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString() +
                "-----------------------------------------------------");
        Bukkit.broadcastMessage("");
        Bukkit.getServer().broadcastMessage(String.format(ChatColor.GOLD + ChatColor.BOLD.toString() + "%s" +
                        ChatColor.YELLOW + ChatColor.BOLD.toString() + " has won the game!",
                game.isTeams() ? winningSide.getName() + "'s side" : winningSide.getName()));
        for (GamePlayer player : Warfare.getInstance().getPlayerManager().getPlayers()) {
            if (player.isHasPlayed()) {
                player.getBukkitPlayer().sendMessage(String.format(ChatColor.YELLOW + "You earned " + ChatColor.GOLD + "%s" + ChatColor.YELLOW + " points, " +
                                ChatColor.GOLD + "%s" + ChatColor.YELLOW + " coins, and " + ChatColor.GOLD + "%s" + ChatColor.YELLOW + " kills total.",
                        String.valueOf(player.getGamePoints()),
                        String.valueOf(player.getGameCoins()),
                        String.valueOf(player.getGameKills())));
                Bukkit.broadcastMessage("");
            }
        }
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Thanks for playing!");
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString() +
                "-----------------------------------------------------");

        for (int i = 0; i < new Random().nextInt(30) + 20; i++) {
            Location randomLoc = Warfare.getInstance().getGame().getMapLocation().clone();
            randomLoc.setX(randomLoc.getX() + Math.random() * 50 * 2 - 50);
            randomLoc.setZ(randomLoc.getZ() + Math.random() * 50 * 2 - 50);
            randomLoc.setY(randomLoc.getY() + (Math.random() * 5 - 1));

            Firework firework = (Firework) randomLoc.getWorld().spawnEntity(randomLoc, EntityType.FIREWORK);
            FireworkMeta fireworkMeta = firework.getFireworkMeta();
            FireworkEffect effect = FireworkEffect.builder().withColor(
                    Arrays.asList(Color.fromRGB((int) Math.random() * 256, (int) Math.random() * 256, (int) Math.random() * 256)))
                    .with(Arrays.asList(FireworkEffect.Type.values()).iterator().next()).build();
            fireworkMeta.addEffect(effect);
            fireworkMeta.setPower(0);
            firework.setFireworkMeta(fireworkMeta);
        }

        if (Warfare.getInstance().isEnabled()) {
            BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
            scheduler.scheduleSyncDelayedTask(Warfare.getInstance(), () -> game.setStage(Game.Stage.RESTART), 120L);
        } else {
            game.setStage(Game.Stage.RESTART);
        }
    }

    @EventHandler
    public void onRestart(RestartEvent event) {
        Warfare warfare = Warfare.getInstance();

        if (!warfare.isEnabled()) {
            return;
        }

        for (GamePlayer player : warfare.getPlayerManager().getPlayers()) {
            Player bukkitPlayer = player.getBukkitPlayer();
            if (bukkitPlayer != null) {
                if (!warfare.getGame().isEdit() || !bukkitPlayer.hasPermission("warfare.edit")) {
                    Party party = warfare.getPartyManager().getParty(player.getUUID());
                    if (party == null) {
                        Utils.sendPlayerToServer(player.getBukkitPlayer(), StaticConfiguration.getNextLobby());
                    } else {
                        UUID leader = party.getLeader();
                        if (leader == player.getUUID()) {
                            String lobby = StaticConfiguration.getNextLobby();
                            for (UUID member : party.getMembers()) {
                                Player other = Bukkit.getPlayer(member);
                                Utils.sendPlayerToServer(other, lobby);
                            }
                        } else if (Bukkit.getPlayer(leader) == null) {
                            Utils.sendPlayerToServer(player.getBukkitPlayer(), StaticConfiguration.getNextLobby());
                        }
                    }
                }
            }
        }

        if (warfare.getGame().isEdit()) {
            return;
        }

        BukkitScheduler scheduler = warfare.getServer().getScheduler();
        if (warfare.needsRestart()) {
            scheduler.scheduleSyncDelayedTask(warfare, () -> warfare.getServer().shutdown(), 100L);
        } else {
            for (Block block : warfare.getInstance().getGame().getPlacedBlocks()) {
                block.setType(Material.AIR);
            }

            for (Map.Entry<Location, BlockState> entry : warfare.getInstance().getGame().getBrokenBlocks().entrySet()) {
                Location brokenLocation = entry.getKey();
                BlockState brokenState = entry.getValue();
                Block block = brokenLocation.getWorld().getBlockAt(brokenLocation);
                block.setType(entry.getValue().getType());
                block.setData(brokenState.getRawData());
                block.getState().setData(brokenState.getData());
                if (brokenState instanceof Chest && block.getState() instanceof Chest) {
                    Chest brokenChest = (Chest) brokenState;
                    Chest placedChest = (Chest) block.getState();
                    for (ItemStack is : brokenChest.getBlockInventory().getContents()) {
                        if (is != null) {
                            placedChest.getBlockInventory().addItem(is);
                        }
                    }
                    placedChest.update();
                }
            }

            Warfare.getInstance().getPlayerManager().getPlayers().clear();
            Bukkit.getScheduler().cancelTasks(Warfare.getInstance());

            warfare.getInstance().setGame(new Game());
            warfare.getStorageManager().loadMap();
        }
    }

    // Block place and block damage listeners
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        checkBroken(event, event.getBlock());
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockExplode(BlockExplodeEvent event) {
        checkBroken(event, event.getBlock());
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockExplode(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            checkBroken(event, block);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockBurn(BlockBurnEvent event) {
        checkBroken(event, event.getBlock());
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockFade(BlockFadeEvent event) {
        checkBroken(event, event.getBlock());
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        checkBroken(event, event.getBlockClicked());
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        checkPlaced(event, event.getBlock());
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockFromTo(BlockFromToEvent event) {
        checkPlaced(event, event.getToBlock());
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockFormEvent(BlockFormEvent event) {
        checkPlaced(event, event.getBlock());
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockSpread(BlockSpreadEvent event) {
        checkPlaced(event, event.getBlock());
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        checkPlaced(event, event.getBlockClicked().getRelative(event.getBlockFace()));
    }

    private void checkBroken(Cancellable cancellable, Block block) {
        if (!cancellable.isCancelled()) {
            if (!Warfare.getInstance().getGame().getPlacedBlocks().contains(block)) {
                Warfare.getInstance().getGame().getBrokenBlocks().put(block.getLocation(), block.getState());
            }
        }
    }

    private void checkPlaced(Cancellable cancellable, Block block) {
        if (!cancellable.isCancelled()) {
            Warfare.getInstance().getGame().getPlacedBlocks().add(block);
        }
    }
}