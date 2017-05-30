package com.andrewyunt.warfare.game;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.game.events.AddPlayerEvent;
import com.andrewyunt.warfare.game.events.RemovePlayerEvent;
import com.andrewyunt.warfare.game.events.StageChangeEvent;
import com.andrewyunt.warfare.player.GamePlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

        @Getter private final int order;
		@Getter private final DyeColor dyeColor;
		@Getter private final ChatColor color;
		@Getter private final String description;

        Stage(int order, DyeColor dyeColor, ChatColor color, String description) {
            this.order = order;
            this.dyeColor = dyeColor;
            this.color = color;
            this.description = description;
        }

        public String getDisplay(){
            return color + description;
        }
    }

	@Getter @Setter private Set<LootChest> lootChests = new HashSet<>();
	@Getter @Setter private Set<Cage> cages = new HashSet<>();
	@Getter @Setter private boolean teams;
	@Getter @Setter private boolean edit;
	@Getter @Setter private Location mapLocation;
	@Getter private short countdownTime = 10, gameTime;
	@Getter private Stage stage = Stage.WAITING;

	@Getter private final Set<GamePlayer> players = new HashSet<>();
	@Getter private final Set<Side> sides = new HashSet<>();
	
	public Game() {
		if (teams) {
			sides.add(new Side(1, "Team 1"));
			sides.add(new Side(2, "Team 2"));
		}

		BukkitScheduler scheduler = Warfare.getInstance().getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(Warfare.getInstance(), () -> mapLocation.getWorld().setTime(6000), 20L, 0L);
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
			if (cage.getName().equals(name)) {
				cage = itrCage;
			}
		}

		return cage;
	}
	
	public void setStage(Stage stage) {
		this.stage = stage;

		Bukkit.getServer().getPluginManager().callEvent(new StageChangeEvent(stage));
	}

	public void fillChests() {
		for (LootChest lootChest : lootChests) {
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
}