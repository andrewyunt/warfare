package com.andrewyunt.warfare.player;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.configuration.StaticConfiguration;
import com.andrewyunt.warfare.game.Cage;
import com.andrewyunt.warfare.game.Side;
import com.andrewyunt.warfare.player.events.SpectateEvent;
import com.andrewyunt.warfare.purchases.Powerup;
import com.andrewyunt.warfare.purchases.Purchasable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public class GamePlayer {
	
	private UUID UUID;
	private String name;
	@Setter private Map<Purchasable, Integer> purchases = new HashMap<>();
	@Setter private Set<Booster> boosters = new HashSet<>();
	private int points, coins, earnedCoins, wins, losses, gamesPlayed, kills, deaths, energy, gameKills, gamePoints, gameCoins;
	@Setter private boolean loaded, hasPlayed, hasFallen, hasBloodEffect, explosiveWeaknessCooldown;
	private boolean spectating, sentActivate;
	@Setter private GamePlayer lastDamager;
	private Kit selectedKit;
	private Powerup selectedPowerup;
	@Setter Side side;
	@Setter int lives, killStreak;
	
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
		killStreak++;
		gameKills++;
		
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

	public void setSpectating(boolean spectating) {
		this.spectating = spectating;

		if (spectating) {
			Bukkit.getServer().getPluginManager().callEvent(new SpectateEvent(this));
		}
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