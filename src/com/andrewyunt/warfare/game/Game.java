package com.andrewyunt.warfare.game;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.game.events.AddPlayerEvent;
import com.andrewyunt.warfare.game.events.RemovePlayerEvent;
import com.andrewyunt.warfare.game.events.StageChangeEvent;
import com.andrewyunt.warfare.game.loot.Island;
import com.andrewyunt.warfare.game.loot.LootChest;
import com.andrewyunt.warfare.player.GamePlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The class used to store game attributes, placed blocks, and players.
 * 
 * @author Andrew Yunt
 */
@Getter
public class Game {

	@Getter
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

        public String getDisplay() {
            return color + description;
        }
    }

	@Setter private Set<Island> islands = new HashSet<>();
	@Setter private Set<LootChest> lootChests = new HashSet<>();
	@Setter private Set<Cage> cages = new HashSet<>();
	@Setter private Map<Integer, Location> teamSpawns = new HashMap<>();
	private boolean teams;
	@Setter private boolean edit;
	@Setter private Location mapLocation, waitingLocation;
	@Setter private int teamSize = 25;
	private short countdownTime = 10, gameTime;
	private Stage stage = Stage.WAITING;

	private final Set<GamePlayer> players = new HashSet<>();
	private final Set<Side> sides = new HashSet<>();
	private final Map<Location, BlockState> brokenBlocks = new HashMap<>();
	private final Set<Block> placedBlocks = new HashSet<>();

	public void setTeams(boolean teams) {
		this.teams = teams;

		if (teams) {
			sides.add(new Side(1, "Team 1"));
			sides.add(new Side(2, "Team 2"));
		}
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

	public void checkPlayers() {
		if (players.size() == 1 && (stage != Stage.WAITING && stage != Stage.END && stage != Stage.RESTART)) {
			setStage(Stage.END);
		} else if (players.size() == 0 && stage != Stage.WAITING) {
			setStage(Stage.RESTART);
		}
	}
	
	public Set<Cage> getAvailableCages() {
		return cages.stream().filter(cage -> !cage.hasPlayer()).collect(Collectors.toSet());
	}

	public Cage getCage(String name) {
		Cage cage = null;

		for (Cage itrCage : cages) {
			if (itrCage.getName().equals(name)) {
				cage = itrCage;
			}
		}

		return cage;
	}

	public Island getIsland(String name) {
		Island island = null;

		for (Island itrIsland : islands) {
			if (itrIsland.getName().equals(name)) {
				island = itrIsland;
			}
		}

		return island;
	}
	
	public void setStage(Stage stage) {
		this.stage = stage;

		Warfare.getInstance().getStorageManager().updateServerStatusAsync();

		Bukkit.getServer().getPluginManager().callEvent(new StageChangeEvent(stage));
	}

	public void fillChests() {
		for (LootChest lootChest : lootChests) {
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

	public void runGameTimer() {
		BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(Warfare.getInstance(), () -> {
			gameTime++;

			checkGameTime();
		}, 0L, 20L);
	}

	public void checkGameTime() {
		if ((!teams && gameTime == 900) || (teams && gameTime == 1800)) {
			setStage(Stage.END);
			Bukkit.getServer().broadcastMessage(ChatColor.RED + ChatColor.BOLD.toString() + "The game timer ran out.");
		} else if (gameTime % 300 == 0) {
			fillChests();
			Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + "All chests have been refilled");
		}
	}
}