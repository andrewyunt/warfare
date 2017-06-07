package com.andrewyunt.warfare.player;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.configuration.StaticConfiguration;
import com.andrewyunt.warfare.game.Cage;
import com.andrewyunt.warfare.game.Side;
import com.andrewyunt.warfare.player.events.UpdateHotbarEvent;
import com.andrewyunt.warfare.purchases.Powerup;
import com.andrewyunt.warfare.purchases.Purchasable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import java.util.*;

public class GamePlayer {
	
	@Getter private UUID UUID;
	@Getter private String name;
	@Getter @Setter private Map<Purchasable, Integer> purchases = new HashMap<>();
	@Getter @Setter private Set<Booster> boosters = new HashSet<>();
	@Getter private int points, coins, earnedCoins, wins, losses, gamesPlayed, kills, killStreak, deaths, energy;
	@Getter @Setter private boolean loaded, hasPlayed, hasFallen, hasBloodEffect, explosiveWeaknessCooldown;
	@Getter private boolean spectating, sentActivate;
	@Getter @Setter private GamePlayer lastDamager;
	@Getter private Kit selectedKit;
	@Getter private Powerup selectedPowerup;
	@Getter @Setter Side side;
	@Getter @Setter int lives;
	
	public GamePlayer(UUID UUID) {
		this.UUID = UUID;
	}
	
	public Player getBukkitPlayer() {
		return Bukkit.getServer().getPlayer(UUID);
	}

	public int getLevel() {
		return (int) Math.floor(points / 150);
	}

	public void setPoints(int points) {
		if (!Objects.equals(points, this.points)) {
			this.points = points;
			update();
		}
	}

	public void setCoins(int coins) {
		if (!Objects.equals(coins, this.coins)) {
			this.coins = coins;
			update();
		}
	}
	
	public void setEarnedCoins(int earnedCoins) {
		if (!Objects.equals(earnedCoins, this.earnedCoins)) {
            this.earnedCoins = earnedCoins;
            update();
        }
	}
	
	public void setWins(int wins) {
	    if (!Objects.equals(wins, this.wins)) {
            this.wins = wins;
            update();
        }
	}

	public void setLosses(int losses) {
		if (!Objects.equals(losses, this.losses)) {
			this.losses = losses;
			update();
		}
	}

	public void setGamesPlayed(int gamesPlayed) {
		if (!Objects.equals(gamesPlayed, this.gamesPlayed)) {
			this.gamesPlayed = gamesPlayed;
			update();
		}
	}

	public void addKill() {
		this.killStreak = killStreak + 1;
		
		setKills(kills + 1);
	}
	
	public void setKills(int kills) {
	    if (!Objects.equals(kills, this.kills)) {
            this.kills = kills;
            update();
        }
	}

	public void setDeaths(int deaths) {
		if (!Objects.equals(deaths, this.deaths)) {
			this.deaths = deaths;
			update();
		}
	}

	public void addEnergy(int energy) {
		setEnergy(this.energy + energy);
	}

	public void setEnergy(int energy) {
	    this.energy = energy;

		if (this.energy > 100) {
			this.energy = 100;
		} else {
			sentActivate = false;
		}

		if (!sentActivate && this.energy == 100) {
            sentActivate = true;
            if (selectedPowerup == Powerup.MARKSMAN) {
                getBukkitPlayer().sendMessage(ChatColor.GOLD + "Left click" + ChatColor.YELLOW + " using your bow to activate your ability!");
            } else {
                getBukkitPlayer().sendMessage(ChatColor.GOLD + "Right click" + ChatColor.YELLOW + " using your sword to activate your ability!");
            }
		}

		getBukkitPlayer().setLevel(this.energy);
		getBukkitPlayer().setExp(this.energy / 100.0F);
	}
	
	public boolean isInGame() {
		return Warfare.getInstance().getGame().getPlayers().contains(this);
	}
	
	public void setSelectedKit(Kit selectedKit) {
	    if (!Objects.equals(selectedKit, this.selectedKit)) {
            this.selectedKit = selectedKit;
            update();
        }
	}

	public Kit getSelectedKitOrPot() {
        return selectedKit == null ? Kit.POT : selectedKit;
    }

	public void setSelectedPowerup(Powerup selectedPowerup) {
		if (!Objects.equals(selectedPowerup, this.selectedPowerup)) {
			this.selectedPowerup = selectedPowerup;
			update();
		}
	}

	public Location setSpectating(boolean spectating, boolean respawn) {
		this.spectating = spectating;

		if (respawn) {
		    getBukkitPlayer().spigot().respawn();
        }
		if (spectating) {
            Player player = getBukkitPlayer();
            player.setGameMode(GameMode.CREATIVE);
            player.setFireTicks(0);

            for (Player other: Bukkit.getOnlinePlayers()) {
                if (other != player) {
                    GamePlayer gamePlayer = Warfare.getInstance().getPlayerManager().getPlayer(other);
                    if (gamePlayer.isSpectating()) {
                        other.showPlayer(player);
                        player.showPlayer(other);
                    }
                    else{
                        other.hidePlayer(player);
                        player.showPlayer(other);
                    }
                }
            }
            player.spigot().setCollidesWithEntities(false);
            player.spigot().setViewDistance(4);

			Bukkit.getServer().getPluginManager().callEvent(new UpdateHotbarEvent(this));

			Location loc = Warfare.getInstance().getGame().getMapLocation();
			Chunk chunk = loc.getChunk();
			
			if (!chunk.isLoaded()) {
                chunk.load();
            }
			
			loc.setY(loc.getY() + 1);
			
			if (respawn) {
                return loc;
            } else {
                getBukkitPlayer().teleport(loc, TeleportCause.COMMAND);
            }
		}
		
		return null;
	}

	public int getLevel(Purchasable purchasable) {
		return purchases.get(purchasable);
	}
	
	public boolean isCaged() {
		return getCage() != null;
	}
	
	public Cage getCage() {
		if (StaticConfiguration.LOBBY) {
            return null;
        }
		
		for (Cage cage : Warfare.getInstance().getGame().getCages()) {
            if (cage.hasPlayer() && cage.getPlayer() == this) {
                return cage;
            }
        }
		
		return null;
	}

    public void setName(String name) {
	    if (!Objects.equals(name, this.name)) {
            this.name = name;
            update();
        }
    }

    public void update() {
	    if (isLoaded()) {
            Warfare.getInstance().getStorageManager().savePlayerAsync(this);
        }
    }

    public int getBoost() {
    	int boost = 1;
    	for (Booster booster : boosters) {
    		if (booster.getLevel() > boost) {
    			boost = booster.getLevel();
			}
		}
		return boost;
	}
}