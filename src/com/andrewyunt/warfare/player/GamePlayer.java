
package com.andrewyunt.warfare.player;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.configuration.StaticConfiguration;
import com.andrewyunt.warfare.game.Cage;
import com.andrewyunt.warfare.player.events.UpdateHotbarEvent;
import com.andrewyunt.warfare.purchases.Powerup;
import com.andrewyunt.warfare.purchases.Purchasable;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.*;

public class GamePlayer {
	
	private UUID uuid;
	private String name;
	private int points, coins, earnedCoins, wins, losses, gamesPlayed, kills, killStreak, deaths, energy;
	private boolean epcCooldown, loaded, spectating, sentActivate, hasFallen, hasBloodEffect, explosiveWeaknessCooldown, hasPlayed;
	private GamePlayer lastDamager;
	private Kit selectedKit;
	private Powerup selectedPowerup;
	private Map<Purchasable, Integer> purchases = new HashMap<>();
	
	public GamePlayer(UUID uuid) {
		
		this.uuid = uuid;

		for (Powerup powerup : Powerup.values()) {
			if (!purchases.containsKey(powerup)) {
				purchases.put(powerup, -1);
			}
		}

		// Register health objective for game servers
		if (!StaticConfiguration.LOBBY) {
			Objective healthObjective = Warfare.getInstance().getScoreboardHandler().getPlayerBoard(uuid).getScoreboard()
					.registerNewObjective(ChatColor.RED + "❤", "health");
			healthObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
		}
	}
	
	public UUID getUUID() {
		
		return uuid;
	}
	
	public Player getBukkitPlayer() {
		
		return Bukkit.getServer().getPlayer(uuid);
	}

	public int getPoints() {

		return points;
	}

	public void setPoints(int points) {

		this.points = points;
	}

	public int getLevel() {
		return (int) Math.floor(points / 150) + 1;
	}
	
	public void setCoins(int coins) {
		if(!Objects.equals(coins, this.coins)) {
			this.coins = coins;
			update();
		}
	}
	
	public int getCoins() {
		
		return coins;
	}
	
	public void setEarnedCoins(int earnedCoins) {
		if (!Objects.equals(earnedCoins, this.earnedCoins)) {
            this.earnedCoins = earnedCoins;
            update();
        }
	}
	
	public int getEarnedCoins() {
		
		return earnedCoins;
	}
	
	public void setWins(int wins) {
	    if (!Objects.equals(wins, this.wins)) {
            this.wins = wins;
            update();
        }
	}
	
	public int getWins() {
		return wins;
	}

	public void setLosses(int losses) {
		if (!Objects.equals(losses, this.losses)) {
			this.losses = losses;
			update();
		}
	}

	public int getLosses() {
		return losses;
	}

	public void setGamesPlayed(int gamesPlayed) {
		if (!Objects.equals(gamesPlayed, this.gamesPlayed)) {
			this.gamesPlayed = gamesPlayed;
			update();
		}
	}

	public int getGamesPlayed() {
		return gamesPlayed;
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
	
	public int getKills() {
		
		return kills;
	}

	public int getKillStreak() {

		return killStreak;
	}

	public void setDeaths(int deaths) {
		if (!Objects.equals(deaths, this.deaths)) {
			this.deaths = deaths;
			update();
		}
	}

	public int getDeaths() {
		return deaths;
	}

	public void addEnergy(int energy) {

		setEnergy(this.energy + energy);
	}

	public void removeEnergy(int energy) {

		setEnergy(this.energy - energy);
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

	public int getEnergy() {

		return this.energy;
	}
	
	public void setEPCCooldown(boolean cooldown) {
		
		this.epcCooldown = cooldown;
	}
	
	public boolean isEPCCooldown() {
		
		return epcCooldown;
	}
	
	public void setLoaded(boolean loaded) {
		
		this.loaded = loaded;
	}
	
	public boolean isLoaded() {
		
		return loaded;
	}
	
	public void setHasFallen(boolean hasFallen) {
		
		this.hasFallen = hasFallen;
	}
	
	public boolean hasFallen() {
		
		return hasFallen;
	}

	public void setHasBloodEffect(boolean hasBloodEffect) {

		this.hasBloodEffect = hasBloodEffect;
	}

	public boolean getHasBloodEffect() {

		return hasBloodEffect;
	}

	public void setExplosiveWeaknessCooldown(boolean explosiveWeaknessCooldown) {

		this.explosiveWeaknessCooldown = explosiveWeaknessCooldown;
	}

	public boolean isExplosiveWeaknessCooldown() {

		return explosiveWeaknessCooldown;
	}

	public void setHasPlayed(boolean hasPlayed) {
		this.hasPlayed = hasPlayed;
	}

	public boolean hasPlayed() {
		return hasPlayed;
	}
	
	public boolean isInGame() {
		return Warfare.getInstance().getGame().getPlayers().contains(this);
	}
	
	public void setLastDamager(GamePlayer lastDamager) {
		
		this.lastDamager = lastDamager;
	}
	
	public GamePlayer getLastDamager() {
		
		return lastDamager;
	}
	
	public void setSelectedKit(Kit selectedKit) {
	    if(!Objects.equals(selectedKit, this.selectedKit)) {
            this.selectedKit = selectedKit;
            update();
        }
	}
	
	public Kit getSelectedKit() {
		return selectedKit;
	}

	public Kit getSelectedKitOrPot(){
        return selectedKit == null ? Kit.POT : selectedKit;
    }

	public void setSelectedPowerup(Powerup selectedPowerup) {
		if(!Objects.equals(selectedPowerup, this.selectedPowerup)) {
			this.selectedPowerup = selectedPowerup;
			update();
		}
	}

	public Powerup getSelectedPowerup() {
		return selectedPowerup;
	}

	public Location setSpectating(boolean spectating, boolean respawn) {
		this.spectating = spectating;

		if(respawn){
		    getBukkitPlayer().spigot().respawn();
        }
		if (spectating) {
            Player player = getBukkitPlayer();
            player.setGameMode(GameMode.CREATIVE);
            player.setFireTicks(0);

            for(Player other: Bukkit.getOnlinePlayers()){
                if(other != player){
                    GamePlayer gamePlayer = Warfare.getInstance().getPlayerManager().getPlayer(other);
                    if(gamePlayer.isSpectating()){
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

			Location loc = Warfare.getInstance().getArena().getMapLocation();
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
	
	public boolean isSpectating() {
		
		return spectating;
	}

	public void setPurchases(Map<Purchasable, Integer> purchases) {

		this.purchases = purchases;
	}
	
	public Map<Purchasable, Integer> getPurchases() {
		
		return purchases;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
	    if(!Objects.equals(name, this.name)) {
            this.name = name;
            update();
        }
    }

    public void update(){
	    if(isLoaded()) {
            Warfare.getInstance().getStorageManager().savePlayerAsync(this);
        }
    }
}