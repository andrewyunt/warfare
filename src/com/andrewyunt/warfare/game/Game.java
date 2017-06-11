package com.andrewyunt.warfare.game;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.game.events.AddPlayerEvent;
import com.andrewyunt.warfare.game.events.RemovePlayerEvent;
import com.andrewyunt.warfare.game.events.StageChangeEvent;
import com.andrewyunt.warfare.game.loot.LootChest;
import com.andrewyunt.warfare.managers.mongo.MongoStorageManager;
import com.andrewyunt.warfare.player.GamePlayer;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
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

        public String getDisplay() {
            return color + description;
        }
    }

	@Getter @Setter private Set<LootChest> lootChests = new HashSet<>();
	@Getter @Setter private Set<Cage> cages = new HashSet<>();
	@Getter @Setter private Map<Integer, Location> teamSpawns = new HashMap<>();
	@Getter private boolean teams;
	@Getter @Setter private boolean edit;
	@Getter @Setter private Location mapLocation, waitingLocation;
	@Getter @Setter private int teamSize = 25;
	@Getter private short countdownTime = 10, gameTime;
	@Getter private Stage stage = Stage.WAITING;
	@Getter @Setter private int games = 0;
	@Getter private final int maxGames = 10 + ThreadLocalRandom.current().nextInt(5);
	@Getter private final long startupTime = System.currentTimeMillis();

	@Getter private final Set<GamePlayer> players = new HashSet<>();
	@Getter private final Set<Side> sides = new HashSet<>();

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
		if (gameTime == 900) {
			setStage(Stage.END);
			Bukkit.getServer().broadcastMessage(ChatColor.RED + ChatColor.BOLD.toString() + "The game timer ran out.");
		} else if (gameTime % 300 == 0) {
			fillChests();
			Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + "All chests have been refilled");
		}
	}

    public boolean needsRestart(){
	    return games >= maxGames || System.currentTimeMillis() - startupTime > TimeUnit.DAYS.toMillis(7);
    }

	public void resetGame(){
        MongoStorageManager mongoStorageManager = (MongoStorageManager) Warfare.getInstance().getStorageManager();
        mongoStorageManager.setHasInserted(false);
        mongoStorageManager.setServerId(new ObjectId());
        games++;
        for(Cage cage: cages){
            cage.setPlayer(null);
            cage.setBlocks();
        }
        players.clear();
        countdownTime = 0;
        gameTime = 0;
        edit = false;
        setStage(Stage.WAITING);
    }
}