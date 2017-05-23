package com.andrewyunt.warfare.game;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.game.events.AddPlayerEvent;
import com.andrewyunt.warfare.game.events.RemovePlayerEvent;
import com.andrewyunt.warfare.game.events.StageChangeEvent;
import com.andrewyunt.warfare.player.GamePlayer;
import org.bukkit.*;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The class used to store game attributes, placed blocks, and players.
 * 
 * @author Andrew Yunt
 */
public class Game {
	
	public enum Stage {
        COUNTDOWN(1, DyeColor.GREEN, ChatColor.GREEN, "Starting"),
        WAITING(0, DyeColor.YELLOW, ChatColor.YELLOW, "Waiting for players"),
		BATTLE(2, DyeColor.RED, ChatColor.RED, "Game in progress"),
		END(3, DyeColor.RED, ChatColor.RED, "Game finished"),
		RESTART(4, DyeColor.RED, ChatColor.RED, "Server restarting");

        private final int order;
		private final DyeColor dyeColor;
		private final ChatColor color;
		private final String description;

        Stage(int order, DyeColor dyeColor, ChatColor color, String description) {
            this.order = order;
            this.dyeColor = dyeColor;
            this.color = color;
            this.description = description;
        }

        public DyeColor getDyeColor() {
            return dyeColor;
        }

        public ChatColor getColor() {
            return color;
        }

        public String getDescription() {
            return description;
        }

        public String getDisplay(){
            return color + description;
        }

        public int getOrder() {
            return order;
        }
    }
	
	private short countdownTime = 10, gameTime;
	private Stage stage = Stage.WAITING;
	
	private final Set<GamePlayer> players = new HashSet<>();
	private final Set<Cage> cages = new HashSet<>();
	
	public Game() {
		Arena arena = Warfare.getInstance().getArena();
		
		for (Entry<String, Location> entry : arena.getCageLocations().entrySet()) {
			cages.add(new Cage(entry.getKey(), entry.getValue()));
		}

		BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(Warfare.getInstance(), () -> arena.getMapLocation().getWorld().setTime(6000), 20L, 0L);
	}
	
	/**
	 * Adds a player to the game and sets their attributes.
	 * 
	 * @param player
	 * 		The player to be added to the game.
	 */
	public void addPlayer(GamePlayer player) {
		// Add the player to the players set
		players.add(player);

		Bukkit.getServer().getPluginManager().callEvent(new AddPlayerEvent(player));
	}
	
	public void removePlayer(GamePlayer player) {
		// Remove the player from the players set
		players.remove(player);

		Bukkit.getServer().getPluginManager().callEvent(new RemovePlayerEvent(player));
	}

	/**
	 * Gets all players currently in the game.
	 *
	 * @returns
	 * 		A set of players currently in the game.
	 */
	public Set<GamePlayer> getPlayers() {
		return players;
	}

	public void checkPlayers() {
		if (players.size() == 1 && (stage != Stage.WAITING && stage != Stage.END && stage != Stage.RESTART)) {
			setStage(Stage.END);
		} else if (players.size() == 0 && stage != Stage.WAITING) {
			setStage(Stage.RESTART);
		}
	}
	
	public Set<Cage> getCages() {
		return cages;
	}
	
	public Set<Cage> getAvailableCages() {
		return cages.stream().filter(cage -> !cage.hasPlayer()).collect(Collectors.toSet());
	}
	
	public void setStage(Stage stage) {
		this.stage = stage;

		Bukkit.getServer().getPluginManager().callEvent(new StageChangeEvent(stage));
	}

	public Stage getStage() {
		return stage;
	}

	public void fillChests() {
		for (LootChest lootChest : Warfare.getInstance().getArena().getLootChests()) {
			if (lootChest.getLocation().getBlock().getType() != Material.CHEST) {
				continue;
			}

			lootChest.fill();
		}
	}
	
	public void runCountdownTimer() {
		BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(Warfare.getInstance(), () -> {
            countdownTime--;

            checkCountdownTime();
        }, 0L, 20L);
	}
	
	public void checkCountdownTime() {
		if (countdownTime > 0) {
			Bukkit.getServer().broadcastMessage(String.format(ChatColor.YELLOW + "The game will start in " + ChatColor.GOLD
							+ ChatColor.BOLD.toString() + "%s" + ChatColor.YELLOW + " seconds.",
					countdownTime));
		} else if (countdownTime == 0) {
			setStage(Stage.BATTLE);
		}
	}

	public short getCountdownTime() {
		return countdownTime;
	}

	public void runGameTimer() {
		BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(Warfare.getInstance(), () -> {
			gameTime++;

			checkGameTime();
		}, 0L, 20L);
	}

	public void checkGameTime() {
		if (gameTime == 900) {
			setStage(Stage.END);
			Bukkit.getServer().broadcastMessage(ChatColor.RED + ChatColor.BOLD.toString() + "The game timer ran out.");
		} else if (gameTime % 300 == 0) {
			fillChests();
			Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + "All chests have been refilled");
		}
	}

	public short getGameTime() {
		return gameTime;
	}
}